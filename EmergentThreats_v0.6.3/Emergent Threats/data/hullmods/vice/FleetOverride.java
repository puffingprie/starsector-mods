package data.hullmods.vice;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class FleetOverride extends BaseHullMod {

	private static float FIGHTER_DAMAGE_BONUS = 10f;
	private static float PILOT_SURVIVAL_PENALTY = 25f;
	
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getDynamic().getStat(Stats.FIGHTER_CREW_LOSS_MULT).modifyMult(id, 1f + PILOT_SURVIVAL_PENALTY * 0.01f);
	}
	
	@Override
	public void applyEffectsToFighterSpawnedByShip(ShipAPI fighter, ShipAPI ship, String id) {
		MutableShipStatsAPI stats = fighter.getMutableStats();
		stats.getBallisticWeaponDamageMult().modifyMult(id, 1f + FIGHTER_DAMAGE_BONUS * 0.01f);
		stats.getEnergyWeaponDamageMult().modifyMult(id, 1f + FIGHTER_DAMAGE_BONUS * 0.01f);
		stats.getMissileWeaponDamageMult().modifyMult(id, 1f + FIGHTER_DAMAGE_BONUS * 0.01f);
		
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) FIGHTER_DAMAGE_BONUS + "%";
		if (index == 1) return "" + (int) PILOT_SURVIVAL_PENALTY + "%";
		return null;
	}
}
