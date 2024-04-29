package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class HMI_Horror_Boost extends BaseHullMod {

	public static float DAMAGE_INCREASE = 10f;
	public static float FLUX_BOOST = 0.75f;
	public static final float CR_BONUS = 30f;

	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {

		stats.getEnergyWeaponFluxCostMod().modifyMult(id, FLUX_BOOST);
		stats.getWeaponTurnRateBonus().modifyPercent(id, (DAMAGE_INCREASE * 5f));
		stats.getWeaponDamageTakenMult().modifyMult(id, 0f);
		stats.getShieldUnfoldRateMult().modifyPercent(id, DAMAGE_INCREASE * 2f);
		stats.getMaxCombatReadiness().modifyFlat(id, CR_BONUS * 0.01f);
	}

		public String getDescriptionParam(int index, HullSize hullSize) {
		return null;
	}
	
}
