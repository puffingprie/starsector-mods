package data.hullmods.ix;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class CrampedMissileBays extends BaseHullMod {

	private static float AMMO_PENALTY = 15f;
	private static String CONFLICT_MOD_ID = "missleracks";
	
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getMissileAmmoBonus().modifyMult(id, 1f - AMMO_PENALTY * 0.01f);
		stats.getVariant().getHullMods().remove(CONFLICT_MOD_ID);
	}

	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) AMMO_PENALTY + "%";
		return null;
	}
}