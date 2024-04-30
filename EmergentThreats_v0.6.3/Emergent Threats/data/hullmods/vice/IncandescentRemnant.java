package data.hullmods.vice;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShieldAPI;
import com.fs.starfarer.api.combat.ShieldAPI.ShieldType;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class IncandescentRemnant extends BaseHullMod {

	private static float SHIELD_BONUS = 25f;
	private static float SPEED_BONUS = 5f;
	private static float MAX_BURN_BONUS = 1f;
	private static float FLUX_DISSIPATION_BONUS = 150f;
	private static float CR_INCREASE = 9.1f;
	
    private static String BUILT_IN_WEAPONS = "Hydromagnetic Cannons";
	private static String SHIELD_BONUS_TEXT = "20%"; //+20% reduction from base value of 1
	
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getShieldDamageTakenMult().modifyMult(id, 1f - SHIELD_BONUS * 0.01f);
		stats.getFluxDissipation().modifyFlat(id, FLUX_DISSIPATION_BONUS);
		stats.getMaxSpeed().modifyFlat(id, SPEED_BONUS);
		stats.getMaxBurnLevel().modifyFlat(id, MAX_BURN_BONUS);
		
		stats.getDynamic().getMod("deployment_points_mod").modifyMult(id, 1f + CR_INCREASE * 0.01f);
		stats.getSuppliesToRecover().modifyMult(id, 1f + CR_INCREASE * 0.01f);
		stats.getSuppliesPerMonth().modifyMult(id, 1f + CR_INCREASE * 0.01f);
	}
	
	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {

	}

	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return BUILT_IN_WEAPONS;
		if (index == 1) return SHIELD_BONUS_TEXT;
		if (index == 2) return "" + (int) FLUX_DISSIPATION_BONUS;
		if (index == 3) return "" + (int) SPEED_BONUS;
		if (index == 4) return "" + (int) MAX_BURN_BONUS;
		if (index == 5) return "" + (int) CR_INCREASE + "%";
		
		return null;
	}
}