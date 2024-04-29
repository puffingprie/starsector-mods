package data.scripts.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import java.util.HashMap;
import java.util.Map;

public class II_CoreUpgrades extends II_BasePackage {

    public static final float CR_BONUS = 0.15f;
    public static final float PEAK_TIME_BONUS = 1f / 3f;
    public static final float SHIELD_EFFICIENCY_MULT = 0.9f;
    public static final float HULL_BONUS = 0.2f;
    public static final float MANEUVERABILITY_BONUS = 0.2f;

    public static final Map<HullSize, Float> SPEED_BONUS = new HashMap<>(4);

    static {
        SPEED_BONUS.put(HullSize.FRIGATE, 20f);
        SPEED_BONUS.put(HullSize.DESTROYER, 15f);
        SPEED_BONUS.put(HullSize.CRUISER, 10f);
        SPEED_BONUS.put(HullSize.CAPITAL_SHIP, 5f);
    }

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        if (isSMod(stats)) {
            stats.getMaxCombatReadiness().modifyFlat(id, CR_BONUS);
            stats.getPeakCRDuration().modifyPercent(id, PEAK_TIME_BONUS * 100f);

            stats.getShieldDamageTakenMult().modifyMult(id, SHIELD_EFFICIENCY_MULT);
            stats.getPhaseCloakActivationCostBonus().modifyMult(id, SHIELD_EFFICIENCY_MULT);
            stats.getPhaseCloakUpkeepCostBonus().modifyMult(id, SHIELD_EFFICIENCY_MULT);

            stats.getHullBonus().modifyPercent(id, HULL_BONUS * 100f);

            stats.getMaxSpeed().modifyFlat(id, SPEED_BONUS.get(hullSize));

            stats.getAcceleration().modifyPercent(id, MANEUVERABILITY_BONUS * 100f);
            stats.getDeceleration().modifyPercent(id, MANEUVERABILITY_BONUS * 100f);
            stats.getMaxTurnRate().modifyPercent(id, MANEUVERABILITY_BONUS * 100f);
            stats.getTurnAcceleration().modifyPercent(id, MANEUVERABILITY_BONUS * 100f);
        }
    }

    @Override
    protected String getHullModId() {
        return CORE_UPGRADES;
    }

    @Override
    protected String getAltSpriteSuffix() {
        return null;
    }

    @Override
    protected void updateDecoWeapons(ShipAPI ship) {
    }

    @Override
    protected String getFlavorText() {
        return "Rigorous calibrations and tune-ups, combined with like-for-like part swaps in favor of best-in-class "
                + "ship components, results in a marked improvement across the board. Although difficult to apply, this "
                + "enhancement suite offers a straightforward, doctrine-neutral upgrade over baseline. However, as these "
                + "upgrades require stripping out the ship's modularity in the name of efficiency, any ship that "
                + "undergoes the process permanently loses the ability to install Imperial packages.";
    }

    @Override
    protected void addCompatibilityStatement(TooltipMakerAPI tooltip) {
        LabelAPI label = tooltip.addPara("Only compatible with Imperium hulls. Incompatible with the Imperial Packages.", PARA_PAD);
        label.setHighlightColors(Global.getSettings().getDesignTypeColor("Imperium"), Global.getSettings().getDesignTypeColor("Imperium"));
        label.setHighlight("Imperium", "Imperial Packages");
    }

    @Override
    public boolean hasSModEffect() {
        return true;
    }

    @Override
    public void addSModSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec, boolean isForBuildInList) {
        addCompatibilityStatement(tooltip);

        if (isForBuildInList) {
            tooltip.addSectionHeading(spec.getDisplayName() + " effect", Misc.getStoryOptionColor(), Misc.getStoryDarkColor(), Alignment.MID, PARA_PAD);
        } else {
            tooltip.addSectionHeading("S-mod effect", Misc.getStoryOptionColor(), Misc.getStoryDarkColor(), Alignment.MID, PARA_PAD);
        }

        if (!isSMod(ship) && !isForBuildInList) {
            tooltip.addPara("This hullmod only applies if it is built into the hull using a story point. Otherwise, it has no effect.",
                    PARA_PAD, Misc.getStoryOptionColor(), "story point");
        }
        addPrimaryDescription(tooltip);

        if (isForModSpec) {
            tooltip.addSpacer(PARA_PAD);
        }

        if ((getFlavorText() != null) && !isForBuildInList) {
            LabelAPI label = tooltip.addPara(getFlavorText(), Misc.getGrayColor(), PARA_PAD);
            label.setAlignment(Alignment.MID);
        }
    }

    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
    }

    @Override
    public void addSModEffectSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec, boolean isForBuildInList) {
        addPrimaryDescription(tooltip);
    }

    @Override
    protected void addPrimaryDescription(TooltipMakerAPI tooltip) {
        LabelAPI bullet;
        tooltip.setBulletedListMode("    • ");
        bullet = tooltip.addPara("Maximum combat readiness %s", BULLET_PAD, Global.getSettings().getColor("standardTextColor"), Misc.getPositiveHighlightColor(),
                "+" + Math.round(CR_BONUS * 100f) + "%");
        bullet = tooltip.addPara("Peak performance time %s", BULLET_PAD, Global.getSettings().getColor("standardTextColor"), Misc.getPositiveHighlightColor(),
                "+" + Math.round(PEAK_TIME_BONUS * 100f) + "%");
        bullet = tooltip.addPara("Shield/phase efficiency %s", BULLET_PAD, Global.getSettings().getColor("standardTextColor"), Misc.getPositiveHighlightColor(),
                "+" + Math.round((1f - SHIELD_EFFICIENCY_MULT) * 100f) + "%");
        bullet = tooltip.addPara("Hull integrity %s", BULLET_PAD, Global.getSettings().getColor("standardTextColor"), Misc.getPositiveHighlightColor(),
                "+" + Math.round(HULL_BONUS * 100f) + "%");
        bullet = tooltip.addPara("Top speed %s/%s/%s/%s (by hull size)", BULLET_PAD, Global.getSettings().getColor("standardTextColor"), Misc.getPositiveHighlightColor(),
                "+" + Math.round(SPEED_BONUS.get(HullSize.FRIGATE)), "+" + Math.round(SPEED_BONUS.get(HullSize.DESTROYER)), "+" + Math.round(SPEED_BONUS.get(HullSize.CRUISER)), "+" + Math.round(SPEED_BONUS.get(HullSize.CAPITAL_SHIP)));
        bullet = tooltip.addPara("Maneuverability %s", BULLET_PAD, Global.getSettings().getColor("standardTextColor"), Misc.getPositiveHighlightColor(),
                "+" + Math.round(MANEUVERABILITY_BONUS * 100f) + "%");
        tooltip.setBulletedListMode(null);
    }

    @Override
    public boolean showInRefitScreenModPickerFor(ShipAPI ship) {
        if (!super.showInRefitScreenModPickerFor(ship)) {
            return false;
        }
        if (ship == null) {
            return false;
        }
        return hasSModEffectSection(ship.getHullSize(), ship, false);
    }

    @Override
    protected void addEmptySysModText(TooltipMakerAPI tooltip) {
    }

    @Override
    protected void addEmptyMiscModText(TooltipMakerAPI tooltip) {
    }

    @Override
    protected void addImperialFlaresSysModText(TooltipMakerAPI text) {
    }

    @Override
    protected void addMicroForgeSysModText(TooltipMakerAPI text, ShipAPI ship) {
    }

    @Override
    protected void addTurbofeederSysModText(TooltipMakerAPI text, ShipAPI ship) {
    }

    @Override
    protected void addImpulseBoosterSysModText(TooltipMakerAPI text, ShipAPI ship) {
    }

    @Override
    protected void addOverdriveSysModText(TooltipMakerAPI text, ShipAPI ship) {
    }

    @Override
    protected void addMagnumSalvoSysModText(TooltipMakerAPI text, ShipAPI ship) {
    }

    @Override
    protected void addCommandCenterSysModText(TooltipMakerAPI text, ShipAPI ship) {
    }

    @Override
    protected void addShockBusterSysModText(TooltipMakerAPI text, ShipAPI ship) {
    }

    @Override
    protected void addCelerityDriveSysModText(TooltipMakerAPI text, ShipAPI ship) {
    }

    @Override
    protected void addLuxFinisSysModText(TooltipMakerAPI text, ShipAPI ship) {
    }

    @Override
    protected void addArbalestLoaderSysModText(TooltipMakerAPI text, ShipAPI ship) {
    }

    @Override
    protected void addCargoMiscModText(TooltipMakerAPI text, ShipAPI ship) {
    }

    @Override
    protected void addLightspearMiscModText(TooltipMakerAPI text, ShipAPI ship) {
    }

    @Override
    protected void addTitanMiscModText(TooltipMakerAPI text, ShipAPI ship) {
    }

    @Override
    protected void addMagnaFulmenMiscModText(TooltipMakerAPI text, ShipAPI ship) {
    }
}
