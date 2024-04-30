package data.hullmods.ix;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class TerminusCore extends BaseHullMod {

	private static float DAMAGE_MISSILES_PERCENT = 100f;
	private static float DAMAGE_FIGHTERS_PERCENT = 100f;
	private static float WEAPON_RANGE_BONUS = 300f;
	private static float WEAPON_TURN_RATE_BONUS = 200f;
	private static float AUTOFIRE_ACCURACY_PERCENT = 100f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getDamageToMissiles().modifyPercent(id, DAMAGE_MISSILES_PERCENT);
		stats.getDamageToFighters().modifyPercent(id, DAMAGE_FIGHTERS_PERCENT);
		stats.getEnergyWeaponRangeBonus().modifyFlat(id, WEAPON_RANGE_BONUS);
		stats.getAutofireAimAccuracy().modifyFlat(id, 1f);
		stats.getEngineDamageTakenMult().modifyMult(id, 0f);
		stats.getDynamic().getMod(Stats.PD_BEST_TARGET_LEADING).modifyFlat(id, 1f);
		stats.getDynamic().getMod(Stats.PD_IGNORES_FLARES).modifyFlat(id, 1f);
	}

	public String getDescriptionParam(int index, HullSize hullSize) {
		return null;
	}
}