package data.hullmods.vice;

import java.awt.Color;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

import data.scripts.vice.hullmods.RemnantSubsystemsUtil;

public class AdaptivePhaseCoilsRemnant extends BaseHullMod {

	//Has different effect for modded remnant phase ships

	public static float FLUX_THRESHOLD_BONUS = 50f;
	public static float FLUX_THRESHOLD_NEW = 75f; //text only
	public static float SENSOR_PROFILE_REDUCTION = 50f;
	public static float DAMAGE_REDUCTION = 10f;
	public static String NO_OP_COST = "does not require OP";
	private static Color JITTER_UNDER_COLOR = new Color(225,100,255,155);
	private static String THIS_MOD = "vice_adaptive_phase_coils";
	
	//Utility variables
	private RemnantSubsystemsUtil util = new RemnantSubsystemsUtil();
	
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		util.applyShipwideHullMod(stats.getVariant(), id, true);
		util.addModuleHandler(stats);
		
		if (stats.getVariant().hasHullMod("phasefield")) {
			stats.getDynamic().getMod(Stats.PHASE_CLOAK_FLUX_LEVEL_FOR_MIN_SPEED_MOD).modifyPercent(id, FLUX_THRESHOLD_BONUS);
		}
		else {
			stats.getSensorProfile().modifyMult(id, 1f - SENSOR_PROFILE_REDUCTION * 0.01f);
			stats.getShieldDamageTakenMult().modifyMult(id, 1f - DAMAGE_REDUCTION * 0.01f);
			stats.getArmorDamageTakenMult().modifyMult(id, 1f - DAMAGE_REDUCTION * 0.01f);
			stats.getHullDamageTakenMult().modifyMult(id, 1f - DAMAGE_REDUCTION * 0.01f);
		}
	}

	@Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        if (!ship.isAlive() || ship.isPhased()) return;
		ship.setJitterUnder(ship, JITTER_UNDER_COLOR, 1f, 10, 12f);
	}
	
	@Override
    public boolean isApplicableToShip(ShipAPI ship) {
		if (util.isModuleCheck(ship)) return false;
		if (ship.getVariant().hasHullMod("phase_anchor") || ship.getVariant().hasHullMod("adaptive_coils")) return false;
		if (util.hasSierraMods(ship)) return false;
		return (util.isApplicable(ship) && util.isOnlyRemnantMod(ship));
	}
	
	public String getUnapplicableReason(ShipAPI ship) {
		if (ship.getVariant().hasHullMod("vice_shipwide_integration") && (ship.getVariant().hasHullMod(THIS_MOD))) return null;
		if (util.isModuleCheck(ship)) return util.getIncompatibleCauseString("hub");
		if (ship.getVariant().hasHullMod("phase_anchor")) return "Incompatible with Phase Anchor";
		if (ship.getVariant().hasHullMod("adaptive_coils")) return "Adaptive Phase Coils already installed";
		if (util.hasSierraMods(ship)) return util.getIncompatibleCauseString("sierra");
		if (!util.isApplicable(ship)) return util.getIncompatibleCauseString("manufacturer");
		if (!util.isOnlyRemnantMod(ship)) return util.getIncompatibleCauseString("modcount");
		return null;
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) FLUX_THRESHOLD_BONUS + "%";
		if (index == 1) return "" + (int) FLUX_THRESHOLD_NEW + "%";
		if (index == 2) return "" + (int) SENSOR_PROFILE_REDUCTION + "%";
		if (index == 3) return "" + (int) DAMAGE_REDUCTION + "%";
		if (index == 4) return NO_OP_COST;
		return null;
	}
}