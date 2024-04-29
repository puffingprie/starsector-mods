package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class HMI_CategoryRange extends BaseHullMod {
	
    private static final float RANGE_BONUS = 200f;
	private static final float RECOIL_BONUS = 50f;

	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {

		stats.getBallisticWeaponRangeBonus().modifyFlat(id, RANGE_BONUS);

		stats.getRecoilDecayMult().modifyPercent(id,-RECOIL_BONUS);
		stats.getRecoilPerShotMult().modifyPercent(id,-RECOIL_BONUS);
		stats.getRecoilDecayMult().modifyPercent(id,RECOIL_BONUS);
	}

	public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
		if (index == 0) return "" + (int) (RECOIL_BONUS)+ "%";
		if (index == 1) return "" + (int) (RANGE_BONUS)+ "units";
		return null;
	}

}
