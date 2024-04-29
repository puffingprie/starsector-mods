package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class HMI_AAC extends BaseHullMod {

	public static final float TURN_BONUS = 50f;
	public static final float RECOIL_BONUS = 50f;
	public static final float RECOIL_DECAY_BONUS = 50f;
	public static final float AUTOAIM_BONUS = 35f;

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getRecoilPerShotMult().modifyMult(id, 1f - (RECOIL_BONUS * 0.01f));
		stats.getRecoilDecayMult().modifyMult(id, 1f + (RECOIL_DECAY_BONUS * 0.01f));
		stats.getWeaponTurnRateBonus().modifyMult(id, 1f + TURN_BONUS * 0.01f);
		stats.getAutofireAimAccuracy().modifyMult(id, 1f + AUTOAIM_BONUS * 0.01f);
	}

	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) AUTOAIM_BONUS + "%";
		if (index == 1) return "" + (int) TURN_BONUS + "%";
		if (index == 2) return "" + (int) RECOIL_BONUS + "%";
		if (index == 3) return "" + (int) RECOIL_DECAY_BONUS + "%";
		return null;
	}


}
