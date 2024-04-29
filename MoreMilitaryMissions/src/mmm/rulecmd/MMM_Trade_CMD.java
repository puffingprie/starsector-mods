package mmm.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.SettingsAPI;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin;
import com.fs.starfarer.api.impl.campaign.DevMenuOptions;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.impl.campaign.rulecmd.FireAll;
import com.fs.starfarer.api.impl.campaign.rulecmd.ShowRemainingCapacity;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import mmm.Utils;
import mmm.missions.OrbitalMissionBase;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.magiclib.util.MagicSettings;

import java.text.MessageFormat;
import java.util.*;

public class MMM_Trade_CMD extends BaseCommandPlugin implements InteractionDialogPlugin {
    private static final String MOD_ID = Utils.MOD_ID;
    private static final Logger log = Global.getLogger(MMM_Trade_CMD.class);
    static {
        if (Utils.DEBUG) {
            log.setLevel(Level.ALL);
        }
    }

    public enum State {
        // enum name must not contain double underscore or else ParseOptionId would not work.
        INIT,
        CONTINUE,
        EXIT,
        BUY_OPTIONS,
        BUY_SELECT,
        SELL_OPTIONS,
        SELL_SELECT
    }

    public static class OptionId {
        State state;
        Object arg = null;
        boolean addOptionSelectedText = false;
        OptionId(State state) {
            this.state = state;
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

    // Strings / constants
    public static final String FUEL_SUPPLIES = Commodities.FUEL + ',' + Commodities.SUPPLIES;

    // Memory flags
    public static final String LAST_OPTION = "$mmm_trade_last_option";
    public static final String TRADE_DONE = "$mmm_trade_done";
    // Comma separated list of commodities, where the first is the commodity picked to sell, while the rest are the buy
    // options. Here the buy/sell side is taken from the perspective of the player fleet, so buy means commodity flowing
    // from the salvage fleet to the player fleet.
    public static final String COMMODITIES = "$mmm_trade_commodities";
    public static final String BOUGHT_CARGO_RATIO = "$mmm_trade_boughtCargoRatio";
    public static final String BOUGHT_FUEL_RATIO = "$mmm_trade_boughtFuelRatio";

    // Settings
    // Both the buy and sell side has to obey this limit.
    public static final float MIN_TRADE_SIZE = 10f;
    // How many days of inactivity before the salvage fleet can change what it's buying/selling.
    public static final float DECISION_TIMEOUT = 30f;
    // Person relation gains per salvage fleet.
    public static final float PERSON_REL_GAIN = 40f;
    // Ratio of commodities that will be involved in trade. By default 15 * 0.5 / 2 = 4 buy/sell slots.
    public static final float DEFAULT_TRADE_SLOT_RATIO = 0.5f;
    // How many commodities the salvage fleet will sell you.
    public static final int BUY_SLOTS = MagicSettings.getInteger(MOD_ID, "TradeBuySlots");
    // How many commodities the salvage fleet will buy from you.
    public static final int SELL_SLOTS = MagicSettings.getInteger(MOD_ID, "TradeSellSlots");
    // Chance that the salvage fleet will want to buy something based on your inventory.

    protected InteractionDialogPlugin originalPlugin = null;
    protected InteractionDialogAPI dialog = null;
    protected Map<String, MemoryAPI> memoryMap = null;
    protected MemoryAPI memory = null;

    protected String buyCommodity = null;
    protected TradeLimits tradeLimits = null;

    public static void showCargoAndCapacity(InteractionDialogAPI dialog, PickedCommodities commodities,
                                            boolean show_capacity) {
        CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
        ArrayList<String> highlights = new ArrayList<>();
        ArrayList<String> parts = new ArrayList<>();
        for (List<String> commodity_ids : Arrays.asList(commodities.buy_ids, commodities.sell_ids)) {
            for (String id : commodity_ids) {
                int units = Math.round(cargo.getCommodityQuantity(id));
                if (units < 1) continue;

                CommoditySpecAPI sell_comm = Global.getSettings().getCommoditySpec(id);
                String amount = Misc.getWithDGS(units);
                String name = sell_comm.getLowerCaseName();
                highlights.add(amount);
                highlights.add(name);
                parts.add(MessageFormat.format("{0} units of {1}", amount, name));
            }
        }

        if (!parts.isEmpty()) {
            String msg = "Your fleet has " + Misc.getAndJoined(parts) + ".";
            dialog.getTextPanel().addPara(msg, Misc.getHighlightColor(), highlights.toArray(new String[0]));
        }

        if (!show_capacity) return;

        String fuel_id = null;
        String cargo_id = null;
        ShowRemainingCapacity plugin = new ShowRemainingCapacity();
        for (String buy_id : commodities.buy_ids) {
            CommoditySpecAPI buy_comm = Global.getSettings().getCommoditySpec(buy_id);
            if (buy_comm.isFuel()) {
                fuel_id = buy_id;
            } else {
                cargo_id = buy_id;
            }
        }

        if (fuel_id != null) {
            plugin.execute(null, dialog,
                    Collections.singletonList(new Misc.Token(fuel_id, Misc.TokenType.LITERAL)), null);
        }
        if (cargo_id != null) {
            plugin.execute(null, dialog,
                    Collections.singletonList(new Misc.Token(cargo_id, Misc.TokenType.LITERAL)), null);
        }
    }

    public static class PickedCommodities {
        // Buy/sell from your perspective.
        public List<String> buy_ids;
        public List<String> sell_ids;
        public PickedCommodities(List<String> buy_ids, List<String> sell_ids) {
            this.buy_ids = buy_ids;
            this.sell_ids = sell_ids;
        }

        @Override
        public String toString() {
            StringBuilder result = new StringBuilder();
            for (List<String> ids : Arrays.asList(buy_ids, sell_ids)) {
                if (ids != buy_ids) result.append(':');
                for (int i = 0; i < ids.size(); ++i) {
                    if (i > 0) result.append(',');
                    result.append(ids.get(i));
                }
            }
            return result.toString();
        }

        // Returns null if empty string or malformed.
        public static PickedCommodities fromString(String str) {
            if (str.isEmpty()) return null;
            String[] parts = str.split(":");
            if (parts.length != 2) return null;
            List<String> buy_ids = Arrays.asList(parts[0].split(","));
            List<String> sell_ids = Arrays.asList(parts[1].split(","));
            return new PickedCommodities(buy_ids, sell_ids);
        }
    }
    // Pick commodities to buy and sell; returns null on failure.
    public static PickedCommodities pickCommodities(SectorEntityToken fleet) {
        SettingsAPI settings =  Global.getSettings();
        ArrayList<CommoditySpecAPI> eligible_commodities = new ArrayList<>();
        for (CommoditySpecAPI comm : settings.getAllCommoditySpecs()) {
            if (comm.isPersonnel() || comm.isMeta() || comm.isNonEcon()) continue;
            eligible_commodities.add(comm);
        }

        int buy_slots = BUY_SLOTS >= 1 ? BUY_SLOTS :
                Math.round(eligible_commodities.size() * DEFAULT_TRADE_SLOT_RATIO / 2);
        int sell_slots = SELL_SLOTS >= 1 ? SELL_SLOTS :
                Math.round(eligible_commodities.size() * DEFAULT_TRADE_SLOT_RATIO / 2);

        ArrayList<String> buy_ids = new ArrayList<>();
        ArrayList<String> sell_ids = new ArrayList<>();
        ArrayList<String> eligible = new ArrayList<>();

        CargoAPI player_cargo = Global.getSector().getPlayerFleet().getCargo();

        CampaignClockAPI clock = Global.getSector().getClock();
        long seed = OrbitalMissionBase.mixSeeds(Misc.getSalvageSeed(fleet),
                "MMM_Trade_CMD".hashCode(), clock.getCycle() * 100L + clock.getMonth());
        Random random = new Random(seed);

        // To aid exploration, always have either supplies, fuel, or both to buy as the first option, rotating them
        // by disallowing the previously chosen choice.
        MemoryAPI memory = Global.getSector().getMemoryWithoutUpdate();
        ArrayList<String> choices = new ArrayList<>(Arrays.asList(FUEL_SUPPLIES, Commodities.FUEL, Commodities.SUPPLIES));
        if (memory.contains(LAST_OPTION)) {
            choices.remove(memory.getString(LAST_OPTION));
        }

        WeightedRandomPicker<String> fuel_supplies_picker = new WeightedRandomPicker<>(random);
        for (String choice : choices) {
            fuel_supplies_picker.add(choice);
        }

        String first_pick = fuel_supplies_picker.pick();
        if (first_pick.equals(FUEL_SUPPLIES)) {
            buy_ids.add(Commodities.FUEL);
            buy_ids.add(Commodities.SUPPLIES);
        } else {
            buy_ids.add(first_pick);
        }

        // picker0: weigh things by size * space / value; this prioritizes getting rid of low value commodities.
        // picker1: weigh things by sqrt (size * space); this prioritizes getting rid of things taking up space.
        // uniform_picker: pick commodities by random
        WeightedRandomPicker<String> weighted_picker0 = new WeightedRandomPicker<>(random);
        WeightedRandomPicker<String> weighted_picker1 = new WeightedRandomPicker<>(random);
        WeightedRandomPicker<String> uniform_picker = new WeightedRandomPicker<>(random);
        for (CommoditySpecAPI comm : eligible_commodities) {
            eligible.add(comm.getId());
            uniform_picker.add(comm.getId());
            float units = player_cargo.getCommodityQuantity(comm.getId());
            float size = comm.isFuel() ? 1f : comm.getCargoSpace();
            if (units >= 1f) {
                weighted_picker0.add(comm.getId(), units * size / comm.getBasePrice());
                weighted_picker1.add(comm.getId(), (float) Math.sqrt(units * size));
            }
        }

        List< WeightedRandomPicker<String>> pickers = Arrays.asList(weighted_picker0, weighted_picker1, uniform_picker);
        int picker0_limit = Math.max(1, sell_slots / 4);
        int picker1_limit = Math.max(picker0_limit + 1, (int) Math.round(Math.ceil(sell_slots / 2.0)));


        while (!uniform_picker.isEmpty() && (buy_ids.size() < buy_slots || sell_ids.size() < sell_slots)) {
            boolean buy = sell_ids.size() >= sell_slots ||
                    (buy_ids.size() < buy_slots && buy_ids.size() <= sell_ids.size());
            ArrayList<String> list = buy ? buy_ids : sell_ids;

            int picker_index = buy ? 2 : list.size() < picker0_limit ? 0 : list.size() < picker1_limit ? 1 : 2;
            boolean picked = false;
            for (int i = picker_index; !picked && i < pickers.size(); ++i) {
                while (!picked && !pickers.get(i).isEmpty()) {
                    String pick = pickers.get(i).pickAndRemove();
                    if (!buy_ids.contains(pick) && !sell_ids.contains(pick)) {
                        list.add(pick);
                        picked = true;
                    }
                }
            }
        }

        if (buy_ids.isEmpty() || sell_ids.isEmpty()) return null;  // Sanity check

        Collections.sort(buy_ids);
        Collections.sort(sell_ids);

        log.debug(MessageFormat.format("picked commodities eligible={0}, buy={1}, sell={2}",
                eligible, buy_ids, sell_ids));
        return new PickedCommodities(buy_ids, sell_ids);
    }

    public static class TradeLimits {
        PickedCommodities commodities;
        // map<buyId, map<sellId, sellAmount>>; can be empty if no trade is possible right now.
        Map<String, Map<String, Integer>> limits;
        public TradeLimits(PickedCommodities commodities,  Map<String, Map<String, Integer>> limits) {
            this.commodities = commodities;
            this.limits = limits;
        }

        @Override
        public String toString() {
            return MessageFormat.format("commodities={0}, limits={1}", commodities, limits);
        }
    }

    // Determine whether we can make any trades for the provided commodities according to what free fuel/cargo space
    // the salvage fleet has as well as how much has already been bought already. Returns null if no trade is possible;
    // otherwise TradeLimits contains an updated PickedCommodities.
    public static TradeLimits computeTradeLimits(SectorEntityToken fleet, MemoryAPI memory,
                                                 PickedCommodities commodities) {
        if (commodities == null || commodities.buy_ids.isEmpty() || commodities.sell_ids.isEmpty()) return null;

        SettingsAPI settings =  Global.getSettings();
        CargoAPI player_cargo = Global.getSector().getPlayerFleet().getCargo();
        CargoAPI other_cargo = fleet.getCargo();
        float bought_cargo_ratio = memory.getFloat(BOUGHT_CARGO_RATIO);
        float bought_fuel_ratio = memory.getFloat(BOUGHT_FUEL_RATIO);

        // Update buy/sell if the salvage fleet is no longer interested due to lack of cargo/fuel space.
        ArrayList<String> new_sell_ids = new ArrayList<>();
        for (String sell_id : commodities.sell_ids) {
            CommoditySpecAPI sell_comm = settings.getCommoditySpec(sell_id);
            float free_space = sell_comm.isFuel() ? other_cargo.getFreeFuelSpace() : other_cargo.getSpaceLeft();
            float free_space_units = free_space / (sell_comm.isFuel() ? 1f : sell_comm.getCargoSpace());
            if (free_space_units >= MIN_TRADE_SIZE) {
                new_sell_ids.add(sell_id);
            }
        }
        // Salvage fleet has no free space to buy anything it wants.
        if (new_sell_ids.isEmpty()) {
            log.info("Salvage fleet is full.");
            return null;
        }

        LinkedHashMap<String, Map<String, Integer>> limits = new LinkedHashMap<>();
        ArrayList<String> new_buy_ids = new ArrayList<>();
        for (String buy_id : commodities.buy_ids) {
            CommoditySpecAPI buy_comm = settings.getCommoditySpec(buy_id);
            float available = buy_comm.isFuel() ? (1f - bought_fuel_ratio) * other_cargo.getMaxFuel() :
                    (1f - bought_cargo_ratio) * other_cargo.getMaxCapacity();
            float buy_limit = available / (buy_comm.isFuel() ? 1f : buy_comm.getCargoSpace());
            if (buy_limit < MIN_TRADE_SIZE) continue;

            new_buy_ids.add(buy_id);
            LinkedHashMap<String, Integer> sell_limits = new LinkedHashMap<>();
            for (String sell_id : new_sell_ids) {
                CommoditySpecAPI sell_comm = settings.getCommoditySpec(sell_id);
                float free_space = sell_comm.isFuel() ? other_cargo.getFreeFuelSpace() : other_cargo.getSpaceLeft();
                float free_space_units = free_space / (sell_comm.isFuel() ? 1f : sell_comm.getCargoSpace());

                float exchanged_limit = buy_limit * buy_comm.getBasePrice() / sell_comm.getBasePrice();
                float player_units = player_cargo.getCommodityQuantity(sell_id);
                float sell = Math.min(Math.min(free_space_units, player_units), exchanged_limit);

                if (sell >= MIN_TRADE_SIZE) {
                    sell_limits.put(sell_id, Math.round(sell));
                }
            }
            if (!sell_limits.isEmpty()) {
                limits.put(buy_id, sell_limits);
            }
        }
        PickedCommodities new_commodities = new PickedCommodities(new_buy_ids, new_sell_ids);
        log.debug(MessageFormat.format(
                "Salvage fleet: cargo_space={0}/{1}, fuel_space={2}/{3}, bought_cargo_ratio={4}, " +
                        "bought_fuel_ratio={5}, commodities={6}, new_commodities={7}, limits={8}",
                other_cargo.getSpaceLeft(), other_cargo.getMaxCapacity(), other_cargo.getFreeFuelSpace(),
                other_cargo.getMaxFuel(), bought_cargo_ratio, bought_fuel_ratio, commodities, new_commodities, limits));

        // Salvage fleet is out of commodities
        return new_buy_ids.isEmpty() ? null : new TradeLimits(new_commodities, limits);
    }

    public static boolean addDevOptions(InteractionDialogAPI dialog) {
        if (Global.getSettings().isDevMode()) {
            DevMenuOptions.addOptions(dialog);
        }
        return true;
    }

    public class TradeCargoPickerListener implements CargoPickerListener {
        private boolean done = false;
        @Override
        public void pickedCargo(CargoAPI cargo) {
            // Update inventory
            CargoAPI player_cargo = Global.getSector().getPlayerFleet().getCargo();
            CargoAPI other_cargo = dialog.getInteractionTarget().getCargo();

            cargo.sort();
            float value = 0f;
            for (CargoStackAPI stack : cargo.getStacksCopy()) {
                value += stack.getSize() * stack.getBaseValuePerUnit();
                AddRemoveCommodity.addCommodityLossText(stack.getCommodityId(), Math.round(stack.getSize()),
                        dialog.getTextPanel());
                player_cargo.removeCommodity(stack.getCommodityId(), stack.getSize());
                other_cargo.addCommodity(stack.getCommodityId(), stack.getSize());
            }

            CommoditySpecAPI buy_comm = Global.getSettings().getCommoditySpec(buyCommodity);

            int bought = (int) Math.floor(value / buy_comm.getBasePrice());
            AddRemoveCommodity.addCommodityGainText(buyCommodity, bought, dialog.getTextPanel());
            player_cargo.addCommodity(buyCommodity, bought);

            // Update memory flags
            float capacity = buy_comm.isFuel() ? other_cargo.getMaxFuel() : other_cargo.getMaxCapacity();
            float size = buy_comm.isFuel() ? 1f : buy_comm.getCargoSpace();
            float delta = bought * size / capacity;

            String flag = buy_comm.isFuel() ? BOUGHT_FUEL_RATIO : BOUGHT_CARGO_RATIO;
            float current = memory.getFloat(flag);
            memory.set(flag, current + delta);

            // Update relationship.
            int relation_delta = Math.round((current + delta) * PERSON_REL_GAIN) - Math.round(current * PERSON_REL_GAIN);
            CoreReputationPlugin.CustomRepImpact impact = new CoreReputationPlugin.CustomRepImpact();
            impact.delta = relation_delta * 0.01f;
            Global.getSector().adjustPlayerReputation(
                    new CoreReputationPlugin.RepActionEnvelope(CoreReputationPlugin.RepActions.CUSTOM, impact,
                            null, dialog.getTextPanel(), true),
                    dialog.getInteractionTarget().getActivePerson());

//            FireBest.fire(null, dialog, memoryMap, "mmm_trade_continue");
            done = true;
            stateMachine(State.CONTINUE, null);
        }

        @Override
        public void cancelledCargoSelection() {
            // On cancel the options for SELL_OPTIONS is still on screen
            done = true;
        }

        // Semantics:
        // cargo: what you have in your "Selected" window
        // pickedUp: can be null, what you have in your cursor
        // combined: cargo + pickedUp, with all stacks combined, except pickedUp is still separate
        // after combined.sort() call, cargo + pickedUp is combined.
        @Override
        public void recreateTextPanel(TooltipMakerAPI panel, CargoAPI cargo, CargoStackAPI pickedUp,
                                      boolean pickedUpFromSource, CargoAPI combined) {
            // For som reason we get extra calls to recreateTextPanel after pickedCargo so we guard against it here.
            if (done) return;
            combined.sort();

            float value = 0f;
            float free_fuel_space = 0f;
            float free_cargo_space = 0f;
            for (CargoStackAPI stack : combined.getStacksCopy()) {
                if (stack.getSize() < 1f) continue;
                value += stack.getSize() * stack.getBaseValuePerUnit();
                if (stack.isFuelStack()) {
                    free_fuel_space = stack.getSize();
                } else {
                    free_cargo_space = stack.getSize();
                }
            }

            CommoditySpecAPI buy_comm = Global.getSettings().getCommoditySpec(buyCommodity);
            int bought = (int) Math.floor(value / buy_comm.getBasePrice());

            CargoAPI player_cargo = Global.getSector().getPlayerFleet().getCargo();
            int total = Math.round(player_cargo.getCommodityQuantity(buyCommodity) + bought);

            panel.addImage(buy_comm.getIconName(), 40f, 3f);
            panel.addPara("You will receive %s units of %s (%s total).", 10f, Misc.getHighlightColor(),
                    Misc.getWithDGS(bought), buy_comm.getLowerCaseName(), Misc.getWithDGS(total));

            String fmt;
            int space;
            if (buy_comm.isFuel()) {
                space = Math.round(player_cargo.getFreeFuelSpace() + free_fuel_space - bought);
                fmt = space > 0 ? "Your fleet''s fuel tanks will be able to hold an additional {0} units of fuel." :
                        space < 0 ? "Your fleet will be over fuel capacity by {0} units." :
                                "Your fleet's fuel tanks will be full.";
            } else {
                space = Math.round(player_cargo.getSpaceLeft() + free_cargo_space - bought);
                fmt = space > 0 ? "Your fleet''s holds will be able to accommodate an additional {0} units of cargo." :
                        space < 0 ? "Your fleet will be over cargo capacity by {0} units." :
                                "Your fleet's holds will be full.";
            }
            String space_dgs = Misc.getWithDGS(Math.abs(space));
            String msg = MessageFormat.format(fmt, space_dgs);
            if (space < 0) {
                panel.addPara(msg, Misc.getNegativeHighlightColor(), 10f);
            } else {
                panel.addPara(msg, 10f,  Misc.getHighlightColor(), space_dgs);
            }

            for (CargoStackAPI stack : combined.getStacksCopy()) {
                if (stack.getSize() < 1f) continue;
                float remaining = player_cargo.getCommodityQuantity(stack.getCommodityId()) - stack.getSize();
                panel.addPara("You will have %s units of %s remaining.", 10f, Misc.getHighlightColor(),
                        Misc.getWithDGS(Math.round(remaining)), stack.getResourceIfResource().getLowerCaseName());
            }
        }
    }

    public boolean stateMachine(State state, Object arg) {
//        log.debug(MessageFormat.format("stateMachine state={0}, arg={1}, this={2}", state, arg, this));
        SectorEntityToken fleet = dialog.getInteractionTarget();
        if (fleet == null || state == null || memory == null || dialog == null) return false;  // Sanity checks

        while (true) {
//            log.debug(MessageFormat.format("switching to state={0}, arg={1}", state, arg));
            switch (state) {
                case INIT:
                case CONTINUE: {
                    tradeLimits = null;
                    buyCommodity = null;
                    String commodities_str = memory.getString(COMMODITIES);
                    PickedCommodities commodities;
                    if (commodities_str == null) {
                        commodities = pickCommodities(fleet);
                    } else if (!commodities_str.isEmpty()) {
                        commodities = PickedCommodities.fromString(commodities_str);
                    } else {
                        commodities = null;
                    }

                    tradeLimits = computeTradeLimits(fleet, memory, commodities);
                    if (tradeLimits == null) {
                        String msg = state.equals(State.INIT) ?
                                "You compared manifests but no deals can be made." : "No more deals can be made.";
                        dialog.getTextPanel().addPara(msg);
                        // No trade is possible; avoid recomputing commodities once we made this decisions.
                        memory.set(COMMODITIES, "", DECISION_TIMEOUT);
                        memory.set(TRADE_DONE, true, 0);
                        state = State.EXIT;
                        break;
                    }

                    if (tradeLimits.commodities.sell_ids.isEmpty() || tradeLimits.commodities.buy_ids.isEmpty()) {
                        return false;  // Sanity check
                    }

                    // Update buy choice (only fuel/supplies) if we called pickCommodities.
                    if (commodities_str == null) {
                        List<String> buy_ids = tradeLimits.commodities.buy_ids;
                        boolean has_fuel = buy_ids.contains(Commodities.FUEL);
                        boolean has_supplies = buy_ids.contains(Commodities.SUPPLIES);
                        if (has_fuel || has_supplies) {
                            String choice = has_fuel && has_supplies ? FUEL_SUPPLIES : has_fuel ?
                                    Commodities.FUEL : Commodities.SUPPLIES;
                            Global.getSector().getMemoryWithoutUpdate().set(LAST_OPTION, choice, 30f);
                        } else {
                            log.error("Unexpected buy_ids:" + tradeLimits.commodities);
                        }
                    }
                    memory.set(COMMODITIES, tradeLimits.commodities.toString(), DECISION_TIMEOUT);

                    if (state.equals(State.INIT)) {
                        ArrayList<String> highlights = new ArrayList<>();
                        ArrayList<String> sellNames = new ArrayList<>();
                        ArrayList<String> buyNames = new ArrayList<>();

                        for (String sell_id : tradeLimits.commodities.sell_ids) {
                            CommoditySpecAPI comm = Global.getSettings().getCommoditySpec(sell_id);
                            String name = comm.getLowerCaseName();
                            highlights.add(name);
                            sellNames.add(name);
                        }

                        for (String buy_id : tradeLimits.commodities.buy_ids) {
                            CommoditySpecAPI comm = Global.getSettings().getCommoditySpec(buy_id);
                            String name = comm.getLowerCaseName();
                            highlights.add(name);
                            buyNames.add(name);
                        }

                        String fmt = "\"We are especially interested in {0}, and are willing to part with {1} in exchange.\"";
                        String msg = MessageFormat.format(fmt, Misc.getAndJoined(sellNames), Misc.getAndJoined(buyNames));
                        dialog.getTextPanel().addPara(msg, Misc.getHighlightColor(), highlights.toArray(new String[0]));
                    }

                    if (tradeLimits.limits.isEmpty()) {
                        showCargoAndCapacity(dialog, tradeLimits.commodities, false);
                        dialog.getTextPanel().addPara("It does not appear that a deal can made at this time.");
                        memory.set(TRADE_DONE, true, 0);
                        state = State.EXIT;
                        break;
                    }

                    showCargoAndCapacity(dialog, commodities, true);
                    state = State.BUY_OPTIONS;
                    break;
                }
                case EXIT: {
                    dialog.setPlugin(originalPlugin);
                    return FireAll.fire(null, dialog, memoryMap, "PopulateOptions");
                }
                case BUY_OPTIONS: {
                    if (tradeLimits == null) return false;  // Sanity check

                    dialog.getOptionPanel().clearOptions();
                    for (String buy_id : tradeLimits.limits.keySet()) {
                        CommoditySpecAPI comm = Global.getSettings().getCommoditySpec(buy_id);
                        dialog.getOptionPanel().addOption("Buy " + comm.getLowerCaseName(),
                                new OptionId(State.BUY_SELECT, buy_id, true),
                                Misc.getDGSCredits(comm.getBasePrice()) + " base price");
                    }
                    dialog.getOptionPanel().addOption("Never mind", new OptionId(State.EXIT));
                    return addDevOptions(dialog);
                }
                case BUY_SELECT: {
                    if (!(arg instanceof String)) return false;  // Sanity check
                    state = State.SELL_OPTIONS;
                    buyCommodity = (String) arg;
                    break;
                }
                case SELL_OPTIONS: {
                    if (buyCommodity == null || tradeLimits == null) return false;  // Sanity check
                    Map<String, Integer> sell_limits = tradeLimits.limits.get(buyCommodity);
                    if (sell_limits == null) return false;  // Sanity check

                    CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();

                    dialog.getOptionPanel().clearOptions();
                    for (String sell_id : sell_limits.keySet()) {
                        CommoditySpecAPI comm = Global.getSettings().getCommoditySpec(sell_id);
                        dialog.getOptionPanel().addOption("Sell " + comm.getLowerCaseName(),
                                new OptionId(State.SELL_SELECT, sell_id, true),
                                 MessageFormat.format("{0} units ({1} base price)",
                                         Misc.getWithDGS(cargo.getCommodityQuantity(sell_id)),
                                         Misc.getDGSCredits(comm.getBasePrice())));
                    }
                    dialog.getOptionPanel().addOption("Never mind", new OptionId(State.BUY_OPTIONS));
                    return addDevOptions(dialog);
                }
                case SELL_SELECT: {
                    // Sanity check
                    if (tradeLimits == null || buyCommodity == null || !(arg instanceof String)) {
                        return false;
                    }

                    Map<String, Integer> sell_limits = tradeLimits.limits.get(buyCommodity);
                    if (sell_limits == null || !sell_limits.containsKey(arg)) return false;  // Sanity check

                    CargoAPI cargo_copy = Global.getFactory().createCargo(false);
                    String sell_id = (String) arg;
                    cargo_copy.addCommodity(sell_id, sell_limits.get(sell_id));
                    String name = Global.getSettings().getCommoditySpec(buyCommodity).getLowerCaseName();

                    final float width = 310f;
                    dialog.showCargoPickerDialog("Buy " + name, "Confirm", "Cancel", true,
                            width, cargo_copy, new TradeCargoPickerListener());

                    // dialog.showCargoPickerDialog does not block until the picker is finished, so we return instead
//                    dialog.setPlugin(originalPlugin);
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
//        log.debug(MessageFormat.format("optionSelected optionData={0}, this={1}", optionData, this));
        if (optionData instanceof OptionId) {
            OptionId option = (OptionId) optionData;
            if (option.addOptionSelectedText) {
                dialog.addOptionSelectedText(option);
            }
            stateMachine(option.state, option.arg);
        } else if (originalPlugin != null) {
            // If we do not recognize the option, then it must be a dev option.
            originalPlugin.optionSelected(optionText, optionData);
        }
    }

    @Override
    public void optionMousedOver(String optionText, Object optionData) { }

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params,
                           Map<String, MemoryAPI> memoryMap) {
        log.debug(MessageFormat.format("execute ruleId={0}, params={1}, this={2}", ruleId, params, this));
        // sanity checks
        if (dialog == null || params == null || memoryMap == null || memoryMap.get(MemKeys.LOCAL) == null ||
                dialog.getInteractionTarget() == null || dialog.getInteractionTarget().getActivePerson() == null) {
            return false;
        }
        this.dialog = dialog;
        this.memoryMap = memoryMap;
        this.memory = memoryMap.get(MemKeys.LOCAL);
        originalPlugin = dialog.getPlugin();
        dialog.setPlugin(this);
        return stateMachine(State.INIT, null);
    }
}