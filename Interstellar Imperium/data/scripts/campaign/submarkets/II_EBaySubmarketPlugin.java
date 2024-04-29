package data.scripts.campaign.submarkets;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignUIAPI.CoreUITradeMode;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemType;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.CoreUIAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.FactionAPI.ShipPickMode;
import com.fs.starfarer.api.campaign.FactionDoctrineAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.SpecialItemPlugin;
import com.fs.starfarer.api.campaign.SpecialItemSpecAPI;
import com.fs.starfarer.api.campaign.impl.items.BlueprintProviderItem;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.impl.campaign.submarkets.MilitarySubmarketPlugin;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.util.Highlights;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import data.scripts.util.II_Util;
import java.awt.Color;
import java.util.List;
import java.util.Set;

public class II_EBaySubmarketPlugin extends MilitarySubmarketPlugin {

    private static final RepLevel MIN_STANDING = RepLevel.NEUTRAL;

    private transient WeightedRandomPicker<BlueprintPick> bpPicker;

    static class BlueprintPick {

        static enum BlueprintType {
            FIGHTER,
            WEAPON,
            SHIP,
            INDUSTRY
        }

        BlueprintType type;
        String specID;

        BlueprintPick(BlueprintType type, String specID) {
            this.type = type;
            this.specID = specID;
        }
    }

    @Override
    protected Object writeReplace() {
        if (okToUpdateShipsAndWeapons()) {
            pruneWeapons(0f);
            getCargo().getMothballedShips().clear();
        }
        return this;
    }

    @Override
    public String getIllegalTransferText(CargoStackAPI stack, TransferAction action) {
        RepLevel req = getRequiredLevelAssumingLegal(stack, action);

        if (req != null) {
            if (requiresCommission(stack)) {
                return "Req: " + submarket.getFaction().getDisplayName() + " - " + req.getDisplayName().toLowerCase() + ", commission";
            }
            return "Req: " + submarket.getFaction().getDisplayName() + " - " + req.getDisplayName().toLowerCase();
        }

        return "Cannot trade in " + stack.getDisplayName() + " here";
    }

    @Override
    public String getIllegalTransferText(FleetMemberAPI member, TransferAction action) {
        RepLevel req = getRequiredLevelAssumingLegal(member, action);
        if (req != null) {
            String str = "";
            RepLevel level = submarket.getFaction().getRelationshipLevel(Global.getSector().getFaction(Factions.PLAYER));
            if (!level.isAtWorst(req)) {
                str += "Req: " + submarket.getFaction().getDisplayName() + " - " + req.getDisplayName().toLowerCase();
            }
            return str;
        }

        if (action == TransferAction.PLAYER_BUY) {
            return "Cannot buy"; // this shouldn't happen
        } else {
            return "Cannot sell";
        }
    }

    @Override
    public Highlights getIllegalTransferTextHighlights(CargoStackAPI stack, TransferAction action) {
        RepLevel req = getRequiredLevelAssumingLegal(stack, action);
        if (req != null) {
            Color c = Misc.getNegativeHighlightColor();
            Highlights h = new Highlights();
            RepLevel level = submarket.getFaction().getRelationshipLevel(Global.getSector().getFaction(Factions.PLAYER));
            if (!level.isAtWorst(req)) {
                h.append(submarket.getFaction().getDisplayName() + " - " + req.getDisplayName().toLowerCase(), c);
            }
            if (requiresCommission(stack) && !hasCommission()) {
                h.append("commission", c);
            }
            return h;
        }
        return null;
    }

    @Override
    public Highlights getIllegalTransferTextHighlights(FleetMemberAPI member, TransferAction action) {
        RepLevel req = getRequiredLevelAssumingLegal(member, action);
        if (req != null) {
            Color c = Misc.getNegativeHighlightColor();
            Highlights h = new Highlights();
            RepLevel level = submarket.getFaction().getRelationshipLevel(Global.getSector().getFaction(Factions.PLAYER));
            if (!level.isAtWorst(req)) {
                h.append("Req: " + submarket.getFaction().getDisplayName() + " - " + req.getDisplayName().toLowerCase(),
                        c);
            }
            return h;
        }
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public float getTariff() {
        return 0.4f;
    }

    @Override
    public String getTooltipAppendix(CoreUIAPI ui) {
        if (!isEnabled(ui)) {
            return "Requires: " + submarket.getFaction().getDisplayName() + " - "
                    + MIN_STANDING.getDisplayName().toLowerCase();
        }
        if (ui.getTradeMode() == CoreUITradeMode.SNEAK) {
            return "Requires: proper docking authorization (transponder on)";
        }
        return null;
    }

    @Override
    public boolean isEnabled(CoreUIAPI ui) {
        if (ui.getTradeMode() == CoreUITradeMode.SNEAK) {
            return false;
        }

        RepLevel level = submarket.getFaction().getRelationshipLevel(Global.getSector().getFaction(Factions.PLAYER));
        return level.isAtWorst(MIN_STANDING);
    }

    @Override
    public boolean isIllegalOnSubmarket(String commodityId, TransferAction action) {
        return true;
    }

    @Override
    public boolean isIllegalOnSubmarket(CargoStackAPI stack, TransferAction action) {
        if (stack.isCommodityStack()) {
            return isIllegalOnSubmarket((String) stack.getData(), action);
        }

        RepLevel req = getRequiredLevelAssumingLegal(stack, action);
        if (req == null) {
            return false;
        }

        RepLevel level = submarket.getFaction().getRelationshipLevel(Global.getSector().getFaction(Factions.PLAYER));
        boolean legal = level.isAtWorst(req);
        if (requiresCommission(stack)) {
            legal &= hasCommission();
        }
        return !legal;
    }

    @Override
    public boolean isIllegalOnSubmarket(FleetMemberAPI member, TransferAction action) {
        RepLevel req = getRequiredLevelAssumingLegal(member, action);
        if (req == null) {
            return false;
        }

        RepLevel level = submarket.getFaction().getRelationshipLevel(Global.getSector().getFaction(Factions.PLAYER));
        boolean legal = level.isAtWorst(req);
        return !legal;
    }

    @Override
    public void updateCargoPrePlayerInteraction() {
        sinceLastCargoUpdate = 0f;

        if (okToUpdateShipsAndWeapons()) {
            sinceSWUpdate = 0f;

            WeightedRandomPicker<String> factionPicker = new WeightedRandomPicker<>();
            factionPicker.add(submarket.getFaction().getId(), 15f);
            WeightedRandomPicker<String> weaponFactionPicker = new WeightedRandomPicker<>();
            weaponFactionPicker.add(submarket.getFaction().getId(), 25f);
            for (FactionAPI faction : Global.getSector().getAllFactions()) {
                if (!II_Util.FACTION_WHITELIST.contains(faction.getId())) {
                    continue;
                }
                if (!faction.isShowInIntelTab()) {
                    continue;
                }
                if (faction.isHostileTo(submarket.getFaction())) {
                    continue;
                }
                float weight;
                if (faction.isAtWorst(submarket.getFaction(), RepLevel.COOPERATIVE)) {
                    weight = 10f;
                } else if (faction.isAtWorst(submarket.getFaction(), RepLevel.FRIENDLY)) {
                    weight = 7f;
                } else if (faction.isAtWorst(submarket.getFaction(), RepLevel.WELCOMING)) {
                    weight = 5f;
                } else if (faction.isAtWorst(submarket.getFaction(), RepLevel.FAVORABLE)) {
                    weight = 4f;
                } else if (faction.isAtWorst(submarket.getFaction(), RepLevel.NEUTRAL)) {
                    weight = 3f;
                } else if (faction.isAtWorst(submarket.getFaction(), RepLevel.SUSPICIOUS)) {
                    weight = 2f;
                } else {
                    weight = 1f;
                }
                factionPicker.add(faction.getId(), weight);
                weaponFactionPicker.add(faction.getId(), weight * 0.5f);
                weaponFactionPicker.add(submarket.getFaction().getId(), weight * 0.5f);
            }

            pruneWeapons(0f);

            int weapons = 10 + Math.max(0, market.getSize() - 1) * 3; // 20 to 24
            int fighters = 4 + Math.max(0, market.getSize() - 3); // 8 to 10

            addWeapons(weapons * 2, (weapons + 4) * 2, 3, weaponFactionPicker);
            pruneWeapons(0.5f);

            addFighters(fighters, fighters + 2, 3, factionPicker);

            getCargo().getMothballedShips().clear();
            for (int i = 0; i < 4; i++) {
                String factionId = factionPicker.pick();
                FactionAPI faction = Global.getSector().getFaction(factionId);
                FactionDoctrineAPI doctrineOverride = faction.getDoctrine().clone();
                doctrineOverride.setShipQuality(5);
                doctrineOverride.setNumShips(1);

                float stability = market.getStabilityValue();
                float sMult = Math.max(0.1f, stability / 10f);

                // larger ships at lower stability to compensate for the reduced number of ships
                // so that low stability doesn't restrict the options to more or less just frigates 
                // and the occasional destroyer
                int size = 3;
                int add = 0;
                if (stability <= 4) {
                    add = 2;
                } else if (stability <= 6) {
                    add = 1;
                }
                size += add;
                if (size > 5) {
                    size = 5;
                }
                doctrineOverride.setShipSize(size);

                addShips(factionId,
                        40f * sMult, // combat
                        itemGenRandom.nextFloat() > 0.75 ? 10f : 0f, // freighter 
                        itemGenRandom.nextFloat() > 0.75 ? 10f : 0f, // tanker
                        itemGenRandom.nextFloat() > 0.75 ? 10f : 0f, // transport
                        itemGenRandom.nextFloat() > 0.75 ? 10f : 0f, // liner
                        itemGenRandom.nextFloat() > 0.75 ? 10f : 0f, // utilityPts
                        null, // qualityOverride
                        0f, // qualityMod
                        ShipPickMode.ALL,
                        doctrineOverride);
            }

            addHullMods(4, 3 + itemGenRandom.nextInt(5));

            if (submarket.getFaction().getId().contentEquals("interstellarimperium")) {
                bpPicker = new WeightedRandomPicker<>(itemGenRandom);

                Set<String> knownFighters = submarket.getFaction().getKnownFighters();
                for (String fighterID : knownFighters) {
                    FighterWingSpecAPI fighterSpec = Global.getSettings().getFighterWingSpec(fighterID);
                    if (fighterSpec != null) {
                        if (fighterSpec.hasTag("ii_common_bp") || fighterSpec.hasTag("ii_ftr_bp")) {
                            if (!Global.getSector().getPlayerFaction().knowsFighter(fighterID)) {
                                bpPicker.add(new BlueprintPick(BlueprintPick.BlueprintType.FIGHTER, fighterID), 1f / (1 + fighterSpec.getTier()));
                            }
                        }
                    }
                }

                Set<String> knownWeapons = submarket.getFaction().getKnownWeapons();
                for (String weaponID : knownWeapons) {
                    WeaponSpecAPI weaponSpec = Global.getSettings().getWeaponSpec(weaponID);
                    if (weaponSpec != null) {
                        if (weaponSpec.hasTag("ii_common_bp") || weaponSpec.hasTag("ii_wpn_bp")) {
                            if (!Global.getSector().getPlayerFaction().knowsWeapon(weaponID)) {
                                bpPicker.add(new BlueprintPick(BlueprintPick.BlueprintType.WEAPON, weaponID), 1f / (1 + weaponSpec.getTier()));
                            }
                        }
                    }
                }

                Set<String> knownShips = submarket.getFaction().getKnownShips();
                for (String shipID : knownShips) {
                    ShipHullSpecAPI hullSpec = Global.getSettings().getHullSpec(shipID);
                    if (hullSpec != null) {
                        if (hullSpec.hasTag("ii_common_bp") || hullSpec.hasTag("ii_ship_bp")) {
                            if (!Global.getSector().getPlayerFaction().knowsShip(shipID)) {
                                bpPicker.add(new BlueprintPick(BlueprintPick.BlueprintType.SHIP, shipID), 10f / (10 + hullSpec.getFleetPoints()));
                            }
                        }
                    }
                }

                if (!Global.getSector().getPlayerFaction().knowsIndustry("ii_orbitalstation")) {
                    bpPicker.add(new BlueprintPick(BlueprintPick.BlueprintType.INDUSTRY, "ii_orbitalstation"), 3f);
                }
            }

            addBlueprints(itemGenRandom.nextInt(Math.max(1, market.getSize() - 2)));
        }

        getCargo().sort();
    }

    private void addBlueprints(int num) {
        /* Get rid of the blueprints before re-adding them */
        CargoAPI ourCargo = getCargo();
        for (CargoStackAPI stack : ourCargo.getStacksCopy()) {
            SpecialItemPlugin plugin = stack.getPlugin();
            if (plugin instanceof BlueprintProviderItem) {
                ourCargo.removeStack(stack);
            }
        }

        if (bpPicker != null) {
            for (int i = 0; i < num; i++) {
                BlueprintPick pick = bpPicker.pickAndRemove();
                if (pick == null) {
                    break;
                }

                String id = pick.specID;

                switch (pick.type) {
                    case FIGHTER:
                        ourCargo.addItems(CargoItemType.SPECIAL, new SpecialItemData(Items.FIGHTER_BP, id), 1);
                        break;
                    case WEAPON:
                        ourCargo.addItems(CargoItemType.SPECIAL, new SpecialItemData(Items.WEAPON_BP, id), 1);
                        break;
                    case SHIP:
                        ourCargo.addItems(CargoItemType.SPECIAL, new SpecialItemData(Items.SHIP_BP, id), 1);
                        break;
                    case INDUSTRY:
                        ourCargo.addItems(CargoItemType.SPECIAL, new SpecialItemData(Items.INDUSTRY_BP, id), 1);
                        break;
                    default:
                        break;
                }
            }
        }

    }

    private RepLevel getRequiredLevelAssumingLegal(CargoStackAPI stack, TransferAction action) {
        int tier = -1;
        int opDiff = 0;
        RepLevel repLevel = null;
        if (stack.isWeaponStack()) {
            WeaponSpecAPI weaponSpec = stack.getWeaponSpecIfWeapon();
            tier = weaponSpec.getTier();
            opDiff = Math.round(weaponSpec.getOrdnancePointCost(null));
            switch (weaponSpec.getSize()) {
                default:
                case SMALL:
                    opDiff -= 5;
                    break;
                case MEDIUM:
                    opDiff -= 10;
                    break;
                case LARGE:
                    opDiff -= 20;
                    break;
            }
        } else if (stack.isSpecialStack()) {
            HullModSpecAPI modSpec = stack.getHullModSpecIfHullMod();
            if (modSpec != null) {
                tier = modSpec.getTier();
            }

            SpecialItemSpecAPI specialSpec = stack.getSpecialItemSpecIfSpecial();
            if (specialSpec != null) {
                SpecialItemPlugin plugin = stack.getPlugin();
                if (plugin instanceof BlueprintProviderItem) {
                    BlueprintProviderItem bpProvider = (BlueprintProviderItem) plugin;

                    List<String> providedList = bpProvider.getProvidedFighters();
                    if (providedList != null) {
                        for (String provided : providedList) {
                            FighterWingSpecAPI fighterSpec = Global.getSettings().getFighterWingSpec(provided);
                            if (fighterSpec != null) {
                                int iTier = fighterSpec.getTier();

                                if (iTier > tier) {
                                    tier = iTier;
                                }
                            }
                        }
                    }

                    providedList = bpProvider.getProvidedShips();
                    if (providedList != null) {
                        for (String provided : providedList) {
                            ShipHullSpecAPI hullSpec = Global.getSettings().getHullSpec(provided);
                            if (hullSpec != null) {
                                int fp = hullSpec.getFleetPoints();
                                HullSize size = hullSpec.getHullSize();

                                RepLevel iRepLevel;
                                if (fp >= 25) {
                                    iRepLevel = RepLevel.COOPERATIVE;
                                } else if ((size == HullSize.CAPITAL_SHIP) || (fp >= 15)) {
                                    iRepLevel = RepLevel.FRIENDLY;
                                } else if ((size == HullSize.CRUISER) || (fp >= 10)) {
                                    iRepLevel = RepLevel.WELCOMING;
                                } else if ((size == HullSize.DESTROYER) || (fp >= 5)) {
                                    iRepLevel = RepLevel.FAVORABLE;
                                } else {
                                    iRepLevel = RepLevel.NEUTRAL;
                                }

                                if ((repLevel == null) || repLevel.isAtBest(iRepLevel)) {
                                    repLevel = iRepLevel;
                                }
                            }
                        }
                    }

                    providedList = bpProvider.getProvidedWeapons();
                    if (providedList != null) {
                        for (String provided : providedList) {
                            WeaponSpecAPI weaponSpec = Global.getSettings().getWeaponSpec(provided);
                            if (weaponSpec != null) {
                                int iTier = weaponSpec.getTier();
                                int iOPDiff = Math.round(weaponSpec.getOrdnancePointCost(null));
                                switch (weaponSpec.getSize()) {
                                    default:
                                    case SMALL:
                                        iOPDiff -= 5;
                                        break;
                                    case MEDIUM:
                                        iOPDiff -= 10;
                                        break;
                                    case LARGE:
                                        iOPDiff -= 20;
                                        break;
                                }

                                if (iTier > tier) {
                                    tier = iTier;
                                    opDiff = iOPDiff;
                                }
                            }
                        }
                    }

                    providedList = bpProvider.getProvidedIndustries();
                    if ((providedList != null) && !providedList.isEmpty()) {
                        repLevel = RepLevel.WELCOMING;
                    }
                }
            }
        } else if (stack.isFighterWingStack()) {
            FighterWingSpecAPI fighterSpec = stack.getFighterWingSpecIfWing();
            tier = fighterSpec.getTier();
        }

        if (tier >= 0) {
            if (action == TransferAction.PLAYER_BUY) {
                if (tier > 3 || opDiff >= 7) {
                    return RepLevel.COOPERATIVE;
                }
                if (tier == 3 || opDiff >= 5) {
                    return RepLevel.FRIENDLY;
                }
                if (tier == 2 || opDiff >= 3) {
                    return RepLevel.WELCOMING;
                }
                if (tier == 1 || opDiff >= 1) {
                    return RepLevel.FAVORABLE;
                }
                return RepLevel.NEUTRAL;
            }
            return RepLevel.VENGEFUL;
        }

        return repLevel;
    }

    private RepLevel getRequiredLevelAssumingLegal(FleetMemberAPI member, TransferAction action) {
        if (action == TransferAction.PLAYER_BUY) {
            int fp = member.getFleetPointCost();
            HullSize size = member.getHullSpec().getHullSize();

            if (fp >= 25) {
                return RepLevel.COOPERATIVE;
            }
            if ((size == HullSize.CAPITAL_SHIP) || (fp >= 15)) {
                return RepLevel.FRIENDLY;
            }
            if ((size == HullSize.CRUISER) || (fp >= 10)) {
                return RepLevel.WELCOMING;
            }
            if ((size == HullSize.DESTROYER) || (fp >= 5)) {
                return RepLevel.FAVORABLE;
            }
            return RepLevel.NEUTRAL;
        }
        return null;
    }

    @Override
    protected boolean requiresCommission(RepLevel req) {
        return false;
    }

    @Override
    public boolean isMilitaryMarket() {
        return false;
    }

    private boolean requiresCommission(CargoStackAPI stack) {
        SpecialItemSpecAPI specialSpec = stack.getSpecialItemSpecIfSpecial();
        if (specialSpec != null) {
            SpecialItemPlugin plugin = stack.getPlugin();
            if (plugin instanceof BlueprintProviderItem) {
                BlueprintProviderItem bpProvider = (BlueprintProviderItem) plugin;

                List<String> providedList = bpProvider.getProvidedFighters();
                if (providedList != null) {
                    for (String provided : providedList) {
                        FighterWingSpecAPI fighterSpec = Global.getSettings().getFighterWingSpec(provided);
                        if (fighterSpec != null) {
                            if (fighterSpec.hasTag("ii_ftr_bp") || fighterSpec.hasTag("ii_rare_bp")) {
                                return true;
                            }
                        }
                    }
                }

                providedList = bpProvider.getProvidedShips();
                if (providedList != null) {
                    for (String provided : providedList) {
                        ShipHullSpecAPI hullSpec = Global.getSettings().getHullSpec(provided);
                        if (hullSpec != null) {
                            if (hullSpec.hasTag("ii_ship_bp") || hullSpec.hasTag("ii_rare_bp")) {
                                return true;
                            }
                        }
                    }
                }

                providedList = bpProvider.getProvidedWeapons();
                if (providedList != null) {
                    for (String provided : providedList) {
                        WeaponSpecAPI weaponSpec = Global.getSettings().getWeaponSpec(provided);
                        if (weaponSpec != null) {
                            if (weaponSpec.hasTag("ii_wpn_bp") || weaponSpec.hasTag("ii_rare_bp")) {
                                return true;
                            }
                        }
                    }
                }

                providedList = bpProvider.getProvidedIndustries();
                if ((providedList != null) && !providedList.isEmpty()) {
                    return true;
                }
            }
        }

        return false;
    }
}
