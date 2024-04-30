package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.utils.I18nUtil;


public class FantasyBulletMod extends BaseHullMod {
    public static final float PROJECT_SPEED = 33f;
    public static final float FLUX_COST = 20f;
    public static final float ROF_BONUS = 25f;

    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        float percent1 = (ROF_BONUS);
        float mult2 = 1f - (PROJECT_SPEED * 0.01f);
        float mult3 = 1f - (FLUX_COST * 0.01f);
        stats.getBallisticRoFMult().modifyPercent(id, percent1);
        stats.getEnergyRoFMult().modifyPercent(id, percent1);
        stats.getProjectileSpeedMult().modifyMult(id, mult2);
        stats.getBallisticWeaponFluxCostMod().modifyMult(id, mult3);
        stats.getEnergyWeaponFluxCostMod().modifyMult(id, mult3);
        stats.getBeamWeaponFluxCostMult().modifyMult(id, 1f);

    }

    public boolean isApplicableToShip(ShipAPI ship) {
        return ship.getVariant().hasHullMod(FantasyBasicMod.FANTASYBASICMOD);
    }

    public String getUnapplicableReason(ShipAPI ship) {

        if (!ship.getVariant().hasHullMod(FantasyBasicMod.FANTASYBASICMOD)) {
            return I18nUtil.getHullModString("FM_HullModRequireBasicMod");
        }

        return null;
    }

    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        tooltip.addSpacer(10f);
        //描述与评价
        tooltip.addSectionHeading(I18nUtil.getHullModString("FM_DescriptionAndEvaluation"), Alignment.TMID, 4f);
        tooltip.addPara(I18nUtil.getHullModString("FantasyBulletMod_DAE_0")
                , Misc.getTextColor(), 4f);
        tooltip.addSpacer(10f);
        tooltip.addPara(I18nUtil.getHullModString("FantasyBulletMod_DAE_1"), Misc.getGrayColor(), 4f);
    }


    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) return "" + (int) ROF_BONUS + "%";
        if (index == 1) return "" + (int) FLUX_COST + "%";
        if (index == 2) return "" + (int) (100f - PROJECT_SPEED) + "%";
        return null;
    }


}
