package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class HMI_MB_Militarization extends BaseHullMod {


	public static final float ROF_BONUS = 50f;
	public static final float FLUX_BONUS = 50f;
	public static final float RANGE_BONUS = 200f;
	public static final float ARMOUR_BONUS = 200f;

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getBallisticRoFMult().modifyMult(id, 1f + (ROF_BONUS * 0.01f));
		stats.getBallisticWeaponFluxCostMod().modifyMult(id, 1f - (FLUX_BONUS * 0.01f));
		stats.getBallisticWeaponRangeBonus().modifyFlat(id, RANGE_BONUS);

		stats.getArmorBonus().modifyFlat(id, ARMOUR_BONUS);
	}

	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) ROF_BONUS + "%";
		if (index == 1) return "" + (int) FLUX_BONUS + "%";
		if (index == 2) return "" + (int) RANGE_BONUS;
		if (index == 3) return "" + (int) ARMOUR_BONUS;
		return null;
	}


}
