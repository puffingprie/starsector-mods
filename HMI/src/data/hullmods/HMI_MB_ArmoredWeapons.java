package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class HMI_MB_ArmoredWeapons extends BaseHullMod {

	public static float FR_BONUS = 33f;
	public static float FLUX_MALUS = 15f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getBallisticWeaponFluxCostMod().modifyMult(id, 1f + (0.01f * FLUX_MALUS));
		stats.getBallisticRoFMult().modifyMult(id, 1f + FR_BONUS * 0.01f);

	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) FR_BONUS + "%";
		if (index == 1) return "" + (int) FLUX_MALUS + "%";
		return null;
	}
}
