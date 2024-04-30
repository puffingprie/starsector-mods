package data.hullmods.vice;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class SelfEvolvingHull extends BaseHullMod {
	
	private static float REACTOR_BONUS = 80f;
	private static float MAX_BURN_BONUS = 1f;
	private static float SHIELD_ARC_BONUS = 120f;
	private static float SHIELD_BONUS = 20f;
	private static float SHIELD_FLUX_PENLTY = 20f;
	private static float FITTING_BONUS = 30f; //text only, value already increased on Mimesis (P)
	private static float CR_INCREASE = 75f;
	
	private static String ADD_HULLMOD_DISPLAY = "Adaptive Drone Bay";
	private static String LUX = "Lux";
	
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getFluxCapacity().modifyMult(id, 1f + REACTOR_BONUS * 0.01f);
		stats.getFluxDissipation().modifyMult(id, 1f + REACTOR_BONUS * 0.01f);
		stats.getMaxBurnLevel().modifyFlat(id, MAX_BURN_BONUS);
		stats.getShieldArcBonus().modifyFlat(id, SHIELD_ARC_BONUS);
		stats.getShieldDamageTakenMult().modifyMult(id, 1f - SHIELD_BONUS * 0.01f);
		stats.getShieldUpkeepMult().modifyMult(id, 1f + SHIELD_FLUX_PENLTY * 0.01f);
		
		stats.getDynamic().getMod("deployment_points_mod").modifyMult(id, 1f + CR_INCREASE * 0.01f);
		stats.getSuppliesToRecover().modifyMult(id, 1f + CR_INCREASE * 0.01f);
		stats.getSuppliesPerMonth().modifyMult(id, 1f + CR_INCREASE * 0.01f);
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) REACTOR_BONUS + "%";
		if (index == 1) return "" + (int) MAX_BURN_BONUS;
		if (index == 2) return "" + (int) SHIELD_ARC_BONUS;
		if (index == 3) return "" + (int) SHIELD_BONUS + "%";
		if (index == 4) return "" + (int) SHIELD_FLUX_PENLTY + "%";
		if (index == 5) return "" + (int) FITTING_BONUS;
		if (index == 6) return "" + (int) CR_INCREASE + "%";
		if (index == 7) return ADD_HULLMOD_DISPLAY;
		if (index == 8) return LUX;
		return null;
	}
}
