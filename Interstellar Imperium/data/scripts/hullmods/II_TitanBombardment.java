package data.scripts.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.hullmods.CompromisedStructure;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.everyframe.II_BlockedHullmodDisplayScript;
import data.scripts.everyframe.II_TitanPlugin;
import java.awt.Color;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class II_TitanBombardment extends BaseHullMod {

    public static final float ERADICATION_CR_PENALTY = 0.6f;

    public static final float DP_PENALTY = 20f;

    public static final float GROUND_BONUS = 250;
    public static final float BOMBARD_BONUS = 3000;
    public static final float ERADICATION_POWER = 5000;

    private static final float PARA_PAD = 10f;
    private static final float SECTION_PAD = 10f;
    private static final String BOMBARD_BONUS_ID = "ii_titanbombardmentbonus";
    private static final String ERADICATION_POWER_ID = "ii_titaneradicationpower";

    private static final Set<String> BLOCKED_HULLMODS = new HashSet<>(1);

    static {
        BLOCKED_HULLMODS.add("efficiency_overhaul");
    }

    public static float getCRPenalty(ShipVariantAPI variant) {
        float scale = 1f;

        Collection<String> hullMods = variant.getHullMods();
        for (String hullMod : hullMods) {
            HullModSpecAPI modSpec = Global.getSettings().getHullModSpec(hullMod);
            if (modSpec.hasTag(Tags.HULLMOD_DMOD)) {
                scale /= CompromisedStructure.DEPLOYMENT_COST_MULT;
            }
        }

        return scale * II_TitanPlugin.CR_PENALTY;
    }

    public static float getEradicationCRPenalty(ShipVariantAPI variant) {
        float scale = 1f;

        Collection<String> hullMods = variant.getHullMods();
        for (String hullMod : hullMods) {
            HullModSpecAPI modSpec = Global.getSettings().getHullModSpec(hullMod);
            if (modSpec.hasTag(Tags.HULLMOD_DMOD)) {
                scale /= CompromisedStructure.DEPLOYMENT_COST_MULT;
            }
        }

        return scale * ERADICATION_CR_PENALTY;
    }

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getDynamic().getMod(Stats.FLEET_GROUND_SUPPORT).modifyFlat(id, GROUND_BONUS);
        stats.getDynamic().getMod(Stats.DEPLOYMENT_POINTS_MOD).modifyFlat(id, -DP_PENALTY);
    }

    @Override
    public void advanceInCampaign(FleetMemberAPI member, float amount) {
        if (member.getRepairTracker().getBaseCR() >= getCRPenalty(member.getVariant())) {
            member.getStats().getDynamic().getMod(Stats.FLEET_BOMBARD_COST_REDUCTION).modifyFlat(BOMBARD_BONUS_ID, BOMBARD_BONUS);
        } else {
            member.getStats().getDynamic().getMod(Stats.FLEET_BOMBARD_COST_REDUCTION).unmodify(BOMBARD_BONUS_ID);
        }
        if (member.getRepairTracker().getBaseCR() >= getEradicationCRPenalty(member.getVariant())) {
            member.getStats().getDynamic().getMod("ii_eradication_power").modifyFlat(ERADICATION_POWER_ID, ERADICATION_POWER);
        } else {
            member.getStats().getDynamic().getMod("ii_eradication_power").unmodify(ERADICATION_POWER_ID);
        }
    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        updateDecoWeapons(ship);
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        for (String tmp : BLOCKED_HULLMODS) {
            if (ship.getVariant().getNonBuiltInHullmods().contains(tmp) && !ship.getVariant().getSMods().contains(tmp)) {
                ship.getVariant().removeMod(tmp);
                II_BlockedHullmodDisplayScript.showBlocked(ship);
            }
        }

        updateDecoWeapons(ship);
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
        switch (index) {
            case 0:
                return "" + (int) GROUND_BONUS;
            case 1:
                return "" + (int) BOMBARD_BONUS;
            case 2:
                return "" + (int) Math.round(DP_PENALTY);
            default:
                break;
        }
        return null;
    }

    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        HullModSpecAPI efficiencyOverhaul = Global.getSettings().getHullModSpec("efficiency_overhaul");
        if (efficiencyOverhaul != null) {
            LabelAPI label = tooltip.addPara("Incompatible with " + efficiencyOverhaul.getDisplayName() + ".", PARA_PAD);
            label.setHighlightColor(Misc.getNegativeHighlightColor());
            label.setHighlight(efficiencyOverhaul.getDisplayName());
        }

        float CR = ship.getCurrentCR();
        if (ship.getFleetMember() != null) {
            CR = ship.getFleetMember().getRepairTracker().getBaseCR();
        }

        Color hc1 = Misc.getHighlightColor();
        Color hc2 = Misc.getHighlightColor();
        if (CR < getCRPenalty(ship.getVariant())) {
            hc1 = Misc.getNegativeHighlightColor();
        }
        if (CR < getEradicationCRPenalty(ship.getVariant())) {
            hc2 = Misc.getNegativeHighlightColor();
        }
        tooltip.addSectionHeading("Combat Readiness",
                Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"), Global.getSettings().getColor("buttonBgDark"), Alignment.TMID, SECTION_PAD);
        tooltip.addPara("The Titan is available in combat while the ship has at least %s combat readiness. After combat ends "
                + "(assuming the Titan was fired), combat readiness will be reduced by %s.", PARA_PAD,
                hc1, "" + (int) Math.round(getCRPenalty(ship.getVariant()) * 100f) + "%", "" + (int) Math.round(getCRPenalty(ship.getVariant()) * 100f) + "%");

        tooltip.addPara("This ship can engage in bombardment operations while it has at least %s combat readiness. Engaging in "
                + "a bombardment operation will reduce combat readiness by %s.", PARA_PAD,
                hc1, "" + (int) Math.round(getCRPenalty(ship.getVariant()) * 100f) + "%", "" + (int) Math.round(getCRPenalty(ship.getVariant()) * 100f) + "%");
        if (getCRPenalty(ship.getVariant()) > II_TitanPlugin.CR_PENALTY) {
            float penaltyScalePct = 100f * ((getCRPenalty(ship.getVariant()) / II_TitanPlugin.CR_PENALTY) - 1f);
            tooltip.addPara("Combat readiness cost is increased by %s due to hull defects.", PARA_PAD, Misc.getNegativeHighlightColor(), "" + Math.round(penaltyScalePct) + "%");
        } else {
            tooltip.addPara("Combat readiness cost will be increased if the ship suffers any hull defects.", PARA_PAD);
        }
        if (CR < getCRPenalty(ship.getVariant())) {
            tooltip.addPara("Insufficient combat readiness for Titan deployment!", Misc.getNegativeHighlightColor(), PARA_PAD);
            tooltip.addPara("Insufficient combat readiness for bombardment operations!", Misc.getNegativeHighlightColor(), PARA_PAD);
        }

        tooltip.addSectionHeading("Market Eradication",
                Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"), Global.getSettings().getColor("buttonBgDark"), Alignment.TMID, SECTION_PAD);
        tooltip.addPara("Enables the eradication of worlds, which immediately decivilizes and permanently desecrates the target.", PARA_PAD);
        tooltip.addPara("Market eradication is possible while the ship has at least %s combat readiness. Eradicating a market "
                + "will reduce combat readiness by %s.", PARA_PAD,
                hc2, "" + (int) Math.round(getEradicationCRPenalty(ship.getVariant()) * 100f) + "%", "" + (int) Math.round(getEradicationCRPenalty(ship.getVariant()) * 100f) + "%");
        if (CR < getEradicationCRPenalty(ship.getVariant())) {
            tooltip.addPara("Insufficient combat readiness for market eradication!", Misc.getNegativeHighlightColor(), PARA_PAD);
        }
    }

    private void updateDecoWeapons(ShipAPI ship) {
        boolean armor = ship.getVariant().hasHullMod(II_BasePackage.ARMOR_PACKAGE);
        boolean targeting = ship.getVariant().hasHullMod(II_BasePackage.TARGETING_PACKAGE);
        boolean elite = ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE);

        if (!armor && !targeting && !elite) {
            boolean titanWeaponHasAmmo = false;
            for (WeaponAPI weapon : ship.getAllWeapons()) {
                if (weapon.getId().contentEquals("ii_titan_w")) {
                    if (weapon.getAmmo() > 0) {
                        titanWeaponHasAmmo = true;
                    }
                    break;
                }
            }
            float CR = ship.getCurrentCR();
            if (ship.getFleetMember() != null) {
                CR = ship.getFleetMember().getRepairTracker().getBaseCR();
            }
            if (CR < getCRPenalty(ship.getVariant())) {
                titanWeaponHasAmmo = false;
            }

            for (WeaponAPI weapon : ship.getAllWeapons()) {
                int frame;

                switch (weapon.getId()) {
                    case "ii_titan_deco":
                        if (!titanWeaponHasAmmo) {
                            frame = 4;
                        } else {
                            frame = 0;
                        }
                        break;
                    case "ii_titan_armor_door":
                        frame = 0;
                        break;
                    case "ii_titan_targeting_door":
                        frame = 0;
                        break;
                    default:
                        continue;
                }

                weapon.getAnimation().setFrame(frame);
            }
        }
    }

    @Override
    public Color getBorderColor() {
        return new Color(255, 150, 0);
    }

    @Override
    public Color getNameColor() {
        return new Color(255, 150, 0);
    }
}
