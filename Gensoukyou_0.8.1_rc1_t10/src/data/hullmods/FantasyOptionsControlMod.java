package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.utils.I18nUtil;

public class FantasyOptionsControlMod extends BaseHullMod {


    public static final float RANGE_MODIFIER = 0.8f;
    public static final float FIGHTERS_MODIFIER = 25f;
    public static final float LOWEST_RANGE = 0.5f;

    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        int bays = ship.getNumFighterBays();

        ship.getMutableStats().getFighterWingRange().modifyMult(id, LOWEST_RANGE);
    }

    @Override
    public void applyEffectsToFighterSpawnedByShip(ShipAPI fighter, ShipAPI ship, String id) {
        super.applyEffectsToFighterSpawnedByShip(fighter, ship, id);
        MutableShipStatsAPI fStats = fighter.getMutableStats();

        fStats.getBallisticWeaponDamageMult().modifyFlat(id, FIGHTERS_MODIFIER / 100);
        fStats.getEnergyWeaponDamageMult().modifyFlat(id, FIGHTERS_MODIFIER / 100);
    }

    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {

        if (index == 0) return "" + (int) (LOWEST_RANGE * 100) + "%";
        if (index == 1) return "" + (int) FIGHTERS_MODIFIER + "%";

        return null;
    }

    public boolean isApplicableToShip(ShipAPI ship) {
        int bays = (int) ship.getMutableStats().getNumFighterBays().getBaseValue();
//		if (ship != null && ship.getVariant().getHullSpec().getBuiltInWings().size() >= bays) {
//			return false;
//		}
        return bays > 0 && ship.getVariant().hasHullMod("FantasyBasicMod");
    }

    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
//        tooltip.addSpacer(10f);
//        tooltip.addSectionHeading(I18nUtil.getHullModString("FantasyOptionsControlMod_SP"), Alignment.TMID,4f);
//        tooltip.addPara(I18nUtil.getHullModString("FantasyOptionsControlMod_SP_0") + (int)(effect * 100) + "%" , Misc.getHighlightColor(), 4f);
        tooltip.addSpacer(10f);
        //描述与评价
        tooltip.addSectionHeading(I18nUtil.getHullModString("FM_DescriptionAndEvaluation"), Alignment.TMID, 4f);
        tooltip.addPara(I18nUtil.getHullModString("FantasyOptionsControlMod_DAE_0"), Misc.getTextColor(), 4f);
        tooltip.addSpacer(10f);
        tooltip.addPara(I18nUtil.getHullModString("FantasyOptionsControlMod_DAE_1"), Misc.getGrayColor(), 4f);
    }


    public String getUnapplicableReason(ShipAPI ship) {

        if (!ship.getVariant().getHullMods().contains(FantasyBasicMod.FANTASYBASICMOD)) {
            return I18nUtil.getHullModString("FM_HullModRequireBasicMod");
        }
        if (ship.getMutableStats().getNumFighterBays().getBaseValue() <= 0) {
            return I18nUtil.getHullModString("FM_HullModRequireFighterBay");
        }
        return null;
    }

//    private float effect_final (float base, float exponent, float lowest_standard){
//
//        float effect_final_0 = (float) Math.pow(base,exponent);
//        return Math.max(effect_final_0, lowest_standard);
//
//    }
}
