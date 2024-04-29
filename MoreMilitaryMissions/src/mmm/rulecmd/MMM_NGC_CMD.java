package mmm.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.DevMenuOptions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.ShipRoles;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.impl.campaign.rulecmd.FireAll;
import com.fs.starfarer.api.impl.campaign.rulecmd.FireBest;
import com.fs.starfarer.api.impl.campaign.rulecmd.NGCAddShip;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import mmm.Utils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.magiclib.util.MagicSettings;

import java.text.MessageFormat;
import java.util.*;
import java.util.List;

public class MMM_NGC_CMD extends BaseCommandPlugin implements InteractionDialogPlugin {
    private static final String MOD_ID = Utils.MOD_ID;
    private static final Logger log = Global.getLogger(MMM_NGC_CMD.class);
    static {
        if (Utils.DEBUG) {
            log.setLevel(Level.ALL);
        }
    }

    // Settings
    public static final List<String> FACTION_WHITELIST =
            MagicSettings.getList(MOD_ID, "DmReinforcementFactionWhitelist");

    public enum ShipType {
        MILITARY,
        CARRIER,
        PHASE,
        CIVILIAN,
        FREIGHTER,
        TANKER;
    }

    public enum State {
        // enum name must not contain double underscore or else ParseOptionId would not work.
        INIT,
        EXIT,
        DONE,
        FACTION_OPTIONS,
        FACTION_SELECT,
        SIZE_OPTIONS,
        SIZE_SELECT,
        TYPE_OPTIONS,
        TYPE_SELECT,
        VARIANT_RANDOM_OPTIONS,
        VARIANT_OPTIONS,
        VARIANT_RANDOM_SELECT,
        VARIANT_SELECT
    }

    public static class OptionId {
        State state;
        Object arg = null;
        boolean addOptionSelectedText = false;
        OptionId(State state) {
            this.state = state;
        }
        OptionId(State state, Object arg) {
            this.state = state;
            this.arg = arg;
        }
        OptionId(State state, Object arg, boolean addOptionSelectedText) {
            this.state = state;
            this.arg = arg;
            this.addOptionSelectedText = addOptionSelectedText;
        }

        @Override
        public String toString() {
            return MessageFormat.format("state={0}, arg={1}", state, arg);
        }
    }

    // Strings
    public static final String BACK = "back";
    public static final String MORE = "more";
    public static final String AGAIN = "again";

    // Memory flags
    public static final String FACTION_PICKED = "$mmm_ngc_factionPicked";
    public static final String SIZE_PICKED = "$mmm_ngc_sizePicked";
    public static final String TYPE_PICKED = "$mmm_ngc_typePicked";
    public static final String SHIPS_PICKED = "$mmm_ngc_shipsPicked";

    // Settings
    public static final int MAX_SELECTIONS = 8;
    // Minimum number of points (see addMilitaryShipSizeSum) before we call the quick start instead of normal.
    public static final int MIN_POINTS_BEFORE_QUICK = 5;
    public static final int SHIPS_PICKER_ROWS = 5;
    public static final int SHIPS_PICKER_COLUMNS = 6;

    protected InteractionDialogPlugin originalPlugin = null;
    protected InteractionDialogAPI dialog = null;
    protected Map<String, MemoryAPI> memoryMap = null;
    protected MemoryAPI memory = null;

    protected int faction_index = 0;
    protected List<FactionAPI> factions = null;
    protected int variant_index = 0;
    protected List<String> matchingVariants = null;
    protected ArrayList<String> variantsPicked = null;
    // A fleet corresponding to variantsPicked; used for visual.
    protected CampaignFleetAPI fleetPicked = null;

    // Prefer larger ships, then sort by name.
    private static class VariantComparator implements Comparator<String> {
        @Override
        public int compare(String o1, String o2) {
            ShipVariantAPI v1 = Global.getSettings().getVariant(o1);
            ShipVariantAPI v2 = Global.getSettings().getVariant(o2);
            int result = -v1.getHullSize().compareTo(v2.getHullSize());
            if (result == 0) {
                result = v1.getHullSpec().getHullName().compareTo(v2.getHullSpec().getHullName());
            }
            if (result == 0) {
                result = v1.getFullDesignationWithHullName().compareTo(v2.getFullDesignationWithHullName());
            }
            return result;
        }
    }

    // Sum of the points of military ships: frigate=1, destroyer=2, cruiser=4, capital=8.
    public static int addMilitaryShipSizeSum(List<String> variantIds) {
        int sum = 0;
        for (String variantId : variantIds) {
            ShipVariantAPI variant = Global.getSettings().getVariant(variantId);
            if (variant.isCombat() || !variant.isCivilian()) {
                switch (variant.getHullSize()) {
                    case FRIGATE: {
                        sum += 1;
                        break;
                    }
                    case DESTROYER: {
                        sum += 2;
                        break;
                    }
                    case CRUISER: {
                        sum += 4;
                        break;
                    }
                    case CAPITAL_SHIP: {
                        sum += 8;
                        break;
                    }
                }
            }
        }
        return sum;
    }

    public static boolean factionHasIntel(FactionAPI faction) {
        return faction.isShowInIntelTab() || FACTION_WHITELIST.contains(faction.getId());
    }

    // List of all faction FactionAPI that are eligible, sorted by name.
    public static List<FactionAPI> getAllFactions(boolean has_intel) {
        ArrayList<FactionAPI> result = new ArrayList<>();
        for (FactionAPI faction : Global.getSector().getAllFactions()) {
            if (faction.getKnownShips().isEmpty()) continue;
            if (factionHasIntel(faction) != has_intel) continue;
            result.add(faction);
        }
        Collections.sort(result, new Comparator<FactionAPI>() {
            @Override
            public int compare(FactionAPI o1, FactionAPI o2) {
                return o1.getDisplayName().compareTo(o2.getDisplayName());
            }
        });
        return result;
    }

    protected static List<String> allShipRoles() {
        return Arrays.asList(
                ShipRoles.COMBAT_SMALL, ShipRoles.COMBAT_MEDIUM, ShipRoles.COMBAT_LARGE, ShipRoles.COMBAT_CAPITAL,
                ShipRoles.COMBAT_FREIGHTER_SMALL, ShipRoles.COMBAT_FREIGHTER_MEDIUM, ShipRoles.COMBAT_FREIGHTER_LARGE,
                ShipRoles.CIV_RANDOM,
                ShipRoles.PHASE_SMALL, ShipRoles.PHASE_MEDIUM, ShipRoles.PHASE_LARGE, ShipRoles.PHASE_CAPITAL,
                ShipRoles.CARRIER_SMALL, ShipRoles.CARRIER_MEDIUM, ShipRoles.CARRIER_LARGE,
                ShipRoles.FREIGHTER_SMALL, ShipRoles.FREIGHTER_MEDIUM, ShipRoles.FREIGHTER_LARGE,
                ShipRoles.TANKER_SMALL, ShipRoles.TANKER_MEDIUM, ShipRoles.TANKER_LARGE,
                ShipRoles.PERSONNEL_SMALL, ShipRoles.PERSONNEL_MEDIUM, ShipRoles.PERSONNEL_LARGE,
                ShipRoles.LINER_SMALL, ShipRoles.LINER_MEDIUM, ShipRoles.LINER_LARGE,
                ShipRoles.TUG, ShipRoles.UTILITY);
    }

    // Here size cannot be null. If faction is null then any non-hidden faction. If type is null then any non-civilian
    // ships.
    public static List<String> findMatchingVariants(MemoryAPI memory) {
        String faction_id = memory.getString(FACTION_PICKED);
        String size_str = memory.getString(SIZE_PICKED);
        String type_str = memory.getString(TYPE_PICKED);
        FactionAPI faction = faction_id == null ? null : Global.getSector().getFaction(faction_id);
        HullSize size = size_str == null ? null : HullSize.valueOf(size_str);
        ShipType type = type_str == null ? null : ShipType.valueOf(type_str);

        HashSet<String> variantIds = new HashSet<>();
        List<FactionAPI> factions = faction != null ? Collections.singletonList(faction) : getAllFactions(true);
        for (FactionAPI current : factions) {
            // Go through all the known ship roles and sum up the ships.
            HashSet<String> faction_variants = new HashSet<>();
            for (String role : allShipRoles()) {
                faction_variants.addAll(current.getVariantsForRole(role));
            }

            for (String variantId : faction_variants) {
                ShipVariantAPI variant = Global.getSettings().getVariant(variantId);
//                log.debug(MessageFormat.format(
//                        "Considering variant={0}, size={1}, isFreighter={2}, isCombat={3}, isCivilian={4}, isCarrier={5}, isPhase={6}",
//                        variantId, variant.getHullSize(), variant.isFreighter(), variant.isCombat(), variant.isCivilian(), variant.isCarrier(), variant.getHullSpec().isPhase(), variant.getHints()));
                // Do some sanity check on the ship variant to ensure it won't cause crash.
                if (variant.isEmptyHullVariant() || variant.isStation() || variant.isFighter()) continue;
                ShipHullSpecAPI spec = variant.getHullSpec();
                if (spec == null || spec.getHullNameWithDashClass() == null || spec.getDesignation() == null) continue;

                if (size != null && !variant.getHullSize().equals(size)) continue;
                if (type != null) {
                    boolean matching = false;
                    switch (type) {
                        case MILITARY: {
                            // variant.isCombat() doesn't work for some reason
                            matching = !variant.isCivilian() || variant.isCombat();
                            break;
                        }
                        case CARRIER: {
                            matching = variant.isCarrier();
                            break;
                        }
                        case PHASE: {
                            matching = variant.getHullSpec().isPhase();
                            break;
                        }
                        case CIVILIAN: {
                            matching = variant.isCivilian();
                            break;
                        }
                        case FREIGHTER: {
                            matching = variant.isFreighter();
                            break;
                        }
                        case TANKER: {
                            matching = variant.isTanker();
                            break;
                        }
                    }
                    if (!matching) continue;
                }
                variantIds.add(variantId);
            }
        }
        ArrayList<String> result = new ArrayList<>(variantIds);
        Collections.sort(result, new VariantComparator());
        log.debug(MessageFormat.format("findMatchingVariants: faction={0}, size={1}, type={2}, variants={3}",
                faction_id, size_str, type_str, result.size()));
//        for (String variant : result) {
//            ShipVariantAPI ship = Global.getSettings().getVariant(variant);
//            log.debug(MessageFormat.format("ShipVariant: getHullName={0}, getFullDesignationWithHullName={1}",
//                    ship.getHullSpec().getHullName(), ship.getFullDesignationWithHullName()));
//        }
        return result;
    }

    public static boolean addDevOptions(InteractionDialogAPI dialog) {
        if (Global.getSettings().isDevMode()) {
            DevMenuOptions.addOptions(dialog);
        }
        return true;
    }

    protected boolean saved = false;
    public void restoreVisualIfSaved() {
        // We need to do this since restoreSavedVisual is not idempotent and clears saved visual.
        if (saved) {
            dialog.getVisualPanel().restoreSavedVisual();
            saved = false;
        }
    }

    public void saveVisualIfNotSaved() {
        if (!saved) {
            dialog.getVisualPanel().saveCurrentVisual();
            saved = true;
        }
    }

    public void setAndShowPickedShips() {
        Collections.sort(variantsPicked, new VariantComparator());
        StringBuilder value = new StringBuilder();
        for (int i = 0; i < variantsPicked.size(); ++i) {
            if (i > 0) value.append(';');
            value.append(variantsPicked.get(i));
        }
        memory.set(SHIPS_PICKED, value.toString(), 0);

        ArrayList<String> highlights = new ArrayList<>();
        List<FleetMemberAPI> members = new ArrayList<>();
        for (FleetMemberAPI member : fleetPicked.getMembersWithFightersCopy()) {
            if (member.isFighterWing()) continue;
            members.add(member);
            highlights.add(member.getVariant().getFullDesignationWithHullName());
        }

        TextPanelAPI text = dialog.getTextPanel();
        String msg = "You have picked " + Misc.getAndJoined(highlights);
        dialog.getTextPanel().addPara(msg, Misc.getHighlightColor(),
                highlights.toArray(new String[0]));
        text.beginTooltip().showShips(members, 100, true, 0f);
        text.addTooltip();
    }

    public boolean stateMachine(State state, Object arg) {
//        log.debug(MessageFormat.format("stateMachine state={0}, arg={1}, text={2}", state, arg, optionText));
        if (state == null || memory == null || dialog == null) return false;  // Sanity checks

        // Note that arg is set to null after every loop.
        while (true) {
//            log.debug(MessageFormat.format("switching to state={0}, arg={1}", state, arg));
            restoreVisualIfSaved();
            switch (state) {
                case INIT: {
                    faction_index = 0;
                    factions = null;
                    variant_index = 0;
                    matchingVariants = null;
                    memory.unset(FACTION_PICKED);
                    memory.unset(SIZE_PICKED);
                    memory.unset(TYPE_PICKED);
                    state = State.FACTION_OPTIONS;
                    break;
                }
                case EXIT: {
                    dialog.setPlugin(originalPlugin);
                    return FireAll.fire(null, dialog, memoryMap, "AddNewGameChoices");
                }
                case DONE: {
                    dialog.setPlugin(originalPlugin);
                    if (variantsPicked == null) return false;  // Sanity check
                    NGCAddShip cmd = new NGCAddShip();
                    boolean quick = addMilitaryShipSizeSum(variantsPicked) >= MIN_POINTS_BEFORE_QUICK;
                    for (String variantId : variantsPicked) {
                        log.info("Adding ship variant " + variantId);
                        cmd.execute(null, dialog,
                                Collections.singletonList(new Misc.Token(variantId, Misc.TokenType.LITERAL)), memoryMap);
                    }
                    return FireBest.fire(null, dialog, memoryMap, quick ? "mmm_ngc_DoneQuick" : "mmm_ngc_Done");
                }
                case FACTION_OPTIONS: {
                    memory.unset(FACTION_PICKED);

                    final int CURRENT_MAX = MAX_SELECTIONS - 3;
                    final String SHOW_HIDDEN = "showHidden";
                    if (factions == null || arg == null) {
                        faction_index = 0;
                        factions = getAllFactions(true);
                    } else {
                        if (arg.equals(BACK)) {
                            faction_index -= CURRENT_MAX;
                        } else if (arg.equals(MORE)) {
                            faction_index += CURRENT_MAX;
                        } else if (arg.equals(SHOW_HIDDEN)) {
                            factions = getAllFactions(false);
                            faction_index = 0;
                        }
                    }
                    if (factions.isEmpty()) return false;  // Sanity check

                    int page = faction_index / CURRENT_MAX + 1;
                    int pages = factions.size() % CURRENT_MAX == 0 ?
                            factions.size() / CURRENT_MAX : factions.size() / CURRENT_MAX + 1;

                    dialog.getOptionPanel().clearOptions();
                    boolean has_intel = factionHasIntel(factions.get(0));
                    if (faction_index == 0 && has_intel) {
                        String first_or_next = variantsPicked.isEmpty() ? "first" : "next";
                        dialog.getTextPanel().addPara(MessageFormat.format(
                                "Which faction did your {0} ship came from?", first_or_next));
                        dialog.getOptionPanel().addOption("I don't remember (any non-hidden faction)",
                                new OptionId(State.SIZE_OPTIONS, null, true));
                    }

                    int limit = Math.min(faction_index + CURRENT_MAX, factions.size());
                    for (int i = faction_index; i < limit; ++i) {
                        FactionAPI faction = factions.get(i);
                        dialog.getOptionPanel().addOption(faction.getDisplayName(),
                                new OptionId(State.FACTION_SELECT, faction.getId()), faction.getId());
                    }

                    if (page < pages) {
                        dialog.getOptionPanel().addOption(
                                MessageFormat.format("Next (page {0}/{1})", page + 1, pages),
                                new OptionId(State.FACTION_OPTIONS, MORE));
                    } else if (has_intel) {
                        dialog.getOptionPanel().addOption("Show hidden factions (select at your own risk)",
                                new OptionId(State.FACTION_OPTIONS, SHOW_HIDDEN));
                    }

                    if (page == 1) {
                        if (has_intel) {
                            if (variantsPicked.isEmpty()) {
                                dialog.getOptionPanel().addOption("Exit", new OptionId(State.EXIT));
                            } else {
                                dialog.getOptionPanel().addOption("No more ships",
                                        new OptionId(State.DONE, null, true),
                                        Misc.getHighlightColor(), null);
                            }
                        } else {
                            dialog.getOptionPanel().addOption("Restart faction",
                                    new OptionId(State.FACTION_OPTIONS));
                        }
                    } else {
                        dialog.getOptionPanel().addOption(
                                MessageFormat.format("Previous (page {0}/{1})", page - 1, pages),
                                new OptionId(State.FACTION_OPTIONS, BACK));
                    }

                    return addDevOptions(dialog);
                }
                case FACTION_SELECT: {
                    if (!(arg instanceof String)) return false;  // Sanity check

                    FactionAPI faction = Global.getSector().getFaction((String) arg);
                    if (faction == null) return false;  // Sanity check

                    String fmt = MessageFormat.format("Your ship came from %s.",
                            faction.getDisplayNameLongWithArticle());
                    dialog.getTextPanel().addPara(fmt, faction.getBaseUIColor(), faction.getDisplayName());
                    memory.set(FACTION_PICKED, arg, 0);
                    state = State.SIZE_OPTIONS;
                    break;
                }
                case SIZE_OPTIONS: {
                    memory.unset(SIZE_PICKED);

                    dialog.getTextPanel().addPara("How big is your ship?");

                    dialog.getOptionPanel().clearOptions();
                    dialog.getOptionPanel().addOption("I don't remember (any size)",
                            new OptionId(State.TYPE_OPTIONS, null, true));
                    dialog.getOptionPanel().addOption("Frigate",
                            new OptionId(State.SIZE_SELECT, HullSize.FRIGATE, true));
                    dialog.getOptionPanel().addOption("Destroyer",
                            new OptionId(State.SIZE_SELECT, HullSize.DESTROYER, true));
                    dialog.getOptionPanel().addOption("Cruiser",
                            new OptionId(State.SIZE_SELECT, HullSize.CRUISER, true));
                    dialog.getOptionPanel().addOption("Capital",
                            new OptionId(State.SIZE_SELECT, HullSize.CAPITAL_SHIP, true));
                    dialog.getOptionPanel().addOption("Back to factions",
                            new OptionId(State.FACTION_OPTIONS, AGAIN));
                    return addDevOptions(dialog);
                }
                case SIZE_SELECT: {
                    if (arg == null) return false;  // Sanity check
//                    dialog.getTextPanel().addPara("You have a %s.", Misc.getHighlightColor(), optionText);
                    memory.set(SIZE_PICKED, arg, 0);
                    state = State.TYPE_OPTIONS;
                    break;
                }
                case TYPE_OPTIONS: {
                    memory.unset(TYPE_PICKED);

                    dialog.getTextPanel().addPara("What kind of ship is it?");

                    dialog.getOptionPanel().clearOptions();
                    dialog.getOptionPanel().addOption("I don't remember (any type)",
                            new OptionId(State.VARIANT_RANDOM_OPTIONS, null, true));
                    dialog.getOptionPanel().addOption("military ship", new OptionId(State.TYPE_SELECT,
                            ShipType.MILITARY, true));
                    dialog.getOptionPanel().addOption("carrier", new OptionId(State.TYPE_SELECT,
                            ShipType.CARRIER, true));
                    dialog.getOptionPanel().addOption("phase ship", new OptionId(State.TYPE_SELECT,
                            ShipType.PHASE, true));
                    dialog.getOptionPanel().addOption("civilian ship", new OptionId(State.TYPE_SELECT,
                            ShipType.CIVILIAN, true));
                    dialog.getOptionPanel().addOption("freighter", new OptionId(State.TYPE_SELECT,
                            ShipType.FREIGHTER, true));
                    dialog.getOptionPanel().addOption("tanker", new OptionId(State.TYPE_SELECT,
                            ShipType.TANKER, true));
                    dialog.getOptionPanel().addOption("Back to size", new OptionId(State.SIZE_OPTIONS));
                    return addDevOptions(dialog);
                }
                case TYPE_SELECT: {
                    if (arg == null) return false;  // Sanity check
//                    dialog.getTextPanel().addPara("You have a %s.", Misc.getHighlightColor(), optionText);
                    memory.set(TYPE_PICKED, arg, 0);
                    state = State.VARIANT_RANDOM_OPTIONS;
                    break;
                }
                case VARIANT_RANDOM_OPTIONS: {
                    // arg == null if previous state is TYPE_SELECT or VARIANT_RANDOM_OPTIONS
                    if (matchingVariants == null || arg == null) {
                        variant_index = 0;
                        matchingVariants = findMatchingVariants(memory);
                    }

                    String variantId;
                    dialog.getOptionPanel().clearOptions();
                    if (matchingVariants.isEmpty()) {
                        variantId = null;
                        dialog.getTextPanel().addPara("There's no such ship.", Misc.getNegativeHighlightColor());
                    } else if (matchingVariants.size() == 1) {
                        variantId = matchingVariants.get(0);
                        String name = Global.getSettings().getVariant(variantId).getFullDesignationWithHullName();
                        dialog.getTextPanel().addPara("This is the only possibility: %s",
                                Misc.getHighlightColor(), name);
                        dialog.getOptionPanel().addOption("That's my ship",
                                new OptionId(State.VARIANT_RANDOM_SELECT, variantId), variantId);
                    } else {
                        WeightedRandomPicker<String> picker = new WeightedRandomPicker<>();
                        picker.addAll(matchingVariants);
                        variantId = picker.pick();
                        String name = Global.getSettings().getVariant(variantId).getFullDesignationWithHullName();
                        dialog.getTextPanel().addPara("This is your ship (picked randomly): %s",
                                Misc.getHighlightColor(), name);
                        dialog.getOptionPanel().addOption("Yes",
                                new OptionId(State.VARIANT_RANDOM_SELECT, variantId), variantId);
                        dialog.getOptionPanel().addOption("No", new OptionId(State.VARIANT_RANDOM_OPTIONS, AGAIN));
                        dialog.getOptionPanel().addOption("Let me pick", new OptionId(State.VARIANT_OPTIONS));
                    }
                    dialog.getOptionPanel().addOption("Back to ship type", new OptionId(State.TYPE_OPTIONS));

                    if (variantId != null) {
                        saveVisualIfNotSaved();
                        CampaignFleetAPI fleet = Global.getFactory().createEmptyFleet(
                                Factions.PLAYER, null, true);
                        dialog.getVisualPanel().showFleetMemberInfo(fleet.getFleetData().addFleetMember(variantId));
                    }

                    return addDevOptions(dialog);
                }
                case VARIANT_OPTIONS: {
                    if (matchingVariants == null || matchingVariants.isEmpty()) return false; // Sanity check

                    final int CURRENT_MAX = MAX_SELECTIONS - 2;
                    final int SHIPS_PER_PICKER = SHIPS_PICKER_ROWS * SHIPS_PICKER_COLUMNS;
                    final int SHIPS_PER_PAGE = SHIPS_PER_PICKER * CURRENT_MAX;

                    if (arg != null) {
                        if (arg.equals(BACK)) {
                            variant_index -= SHIPS_PER_PAGE;
                        } else if (arg.equals(MORE)) {
                            variant_index += SHIPS_PER_PAGE;
                        }
                    }

                    final int page = variant_index / SHIPS_PER_PAGE + 1;
                    final int pages = matchingVariants.size() % SHIPS_PER_PAGE == 0 ?
                            matchingVariants.size() / SHIPS_PER_PAGE : matchingVariants.size() / SHIPS_PER_PAGE + 1;
                    final boolean first_page = page <= 1;
                    final boolean last_page = page >= pages;
                    final int limit = Math.min(variant_index + SHIPS_PER_PAGE, matchingVariants.size());

                    dialog.getOptionPanel().clearOptions();
                    for (int i = variant_index; i < limit; i += SHIPS_PER_PICKER) {
                        ShipVariantAPI ship = Global.getSettings().getVariant(matchingVariants.get(i));
                        dialog.getOptionPanel().addOption(ship.getHullSpec().getHullName() + "...",
                                new OptionId(State.VARIANT_SELECT, i));
                    }

                    if (!last_page) {
                        dialog.getOptionPanel().addOption(
                                MessageFormat.format("Next (page {0}/{1})", page + 1, pages),
                                new OptionId(State.VARIANT_OPTIONS, MORE));
                    }

                    if (first_page) {
                        dialog.getOptionPanel().addOption("Back",
                                new OptionId(State.VARIANT_RANDOM_OPTIONS, AGAIN));
                    } else {
                        dialog.getOptionPanel().addOption(
                                MessageFormat.format("Previous (page {0}/{1})", page - 1, pages),
                                new OptionId(State.VARIANT_OPTIONS, BACK));
                    }

                    return addDevOptions(dialog);
                }
                case VARIANT_RANDOM_SELECT: {
                    if (variantsPicked == null || arg == null) return false;  // Sanity check
                    variantsPicked.add((String) arg);
                    fleetPicked.getFleetData().addFleetMember((String) arg);
                    setAndShowPickedShips();
                    state = State.INIT;
                    break;
                }
                case VARIANT_SELECT: {
                    if (variantsPicked == null || matchingVariants == null || matchingVariants.isEmpty()) {
                        return false;  // Sanity check
                    }

                    final int SHIPS_PER_PICKER = SHIPS_PICKER_ROWS * SHIPS_PICKER_COLUMNS;

                    List<String> variants;
                    int start = (Integer) arg;
                    variants = matchingVariants.subList(start,
                            Math.min(start + SHIPS_PER_PICKER, matchingVariants.size()));

                    CampaignFleetAPI fleet = Global.getFactory().createEmptyFleet(
                            Factions.PLAYER, null, true);
                    ArrayList<FleetMemberAPI> members = new ArrayList<>();
                    for (String variantId : variants) {
                        members.add(fleet.getFleetData().addFleetMember(variantId));
                    }

                    String title = variants.size() > 1 ? "Pick Ships" :
                            Global.getSettings().getVariant(variants.get(0)).getFullDesignationWithHullName();
                    int rows = variants.size() > 1 ? SHIPS_PICKER_ROWS : 1;
                    int columns = variants.size() > 1 ? SHIPS_PICKER_COLUMNS : 4;
                    dialog.showFleetMemberPickerDialog(
                            title, "Ok", "Cancel", rows, columns,
                            116f, true, true, members,
                            new FleetMemberPickerListener() {
                                @Override
                                public void pickedFleetMembers(List<FleetMemberAPI> members) {
                                    if (members.isEmpty()) return;  // Cancel
                                    for (FleetMemberAPI member : members) {
                                        variantsPicked.add(member.getVariant().getHullVariantId());
                                        fleetPicked.getFleetData().addFleetMember(member);
                                    }
                                    setAndShowPickedShips();
                                    stateMachine(State.INIT, null);
                                }
                                @Override
                                public void cancelledFleetMemberPicking() { }
                            });

                    // Return control back to the caller since showFleetMemberPickerDialog don't block; if cancelled
                    // options created by VARIANT_RANDOM_OPTIONS or VARIANT_OPTIONS will remain on screen
                    return true;
                }
                default: {
                    log.error("Unhandled state " + state);
                    return false;
                }
            }

            arg = null;
        }
    }

    @Override
    public void init(InteractionDialogAPI dialog) { this.dialog = dialog; }
    @Override
    public void advance(float amount) {}
    @Override
    public void backFromEngagement(EngagementResultAPI battleResult) {}
    @Override
    public Object getContext() { return null; }
    @Override
    public Map<String, MemoryAPI> getMemoryMap() { return memoryMap; }

    @Override
    public void optionSelected(String optionText, Object optionData) {
//        log.debug(MessageFormat.format("optionSelected optionData={0}", optionData));
        if (optionData instanceof OptionId) {
            OptionId option = (OptionId) optionData;
            if (option.addOptionSelectedText) {
                dialog.addOptionSelectedText(optionData);
            }
            stateMachine(option.state, option.arg);
        } else if (originalPlugin != null) {
            // If we do not recognize the option, then it must be a dev option.
            originalPlugin.optionSelected(optionText, optionData);
        }
    }

    @Override
    public void optionMousedOver(String optionText, Object optionData) {
//        log.debug(MessageFormat.format("optionMousedOver optionData={0}", optionData));
    }

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params,
                           Map<String, MemoryAPI> memoryMap) {
        log.debug(MessageFormat.format("execute ruleId={0}, params={1}, this={2}", ruleId, params, this));
        // sanity checks
        if (dialog == null || params == null || memoryMap == null || memoryMap.get(MemKeys.LOCAL) == null) {
            return false;
        }
        this.dialog = dialog;
        this.memoryMap = memoryMap;
        this.memory = memoryMap.get(MemKeys.LOCAL);

        variantsPicked = new ArrayList<>();
        originalPlugin = dialog.getPlugin();
        fleetPicked = Global.getFactory().createEmptyFleet(Factions.PLAYER, null, true);

        dialog.setPlugin(this);
        return stateMachine(State.INIT, null);
    }
}