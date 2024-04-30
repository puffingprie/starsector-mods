package data.hullmods.ix;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class EntropyArrester extends BaseHullMod {

	private static float HEAL_HULL_AMOUNT = 1f; //+1% per second
	private static float REPAIR_BONUS = 50f;
	private static String CONFLICT_MOD = "autorepair";
	
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getVariant().getHullMods().remove(CONFLICT_MOD);
		stats.getCombatEngineRepairTimeMult().modifyMult(id, 1f - REPAIR_BONUS * 0.01f);
		stats.getCombatWeaponRepairTimeMult().modifyMult(id, 1f - REPAIR_BONUS * 0.01f);
	}
	
	@Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        if (!ship.isAlive()) return;
		float currentHP = ship.getHitpoints();
		float maxHP = ship.getMaxHitpoints();

		if (currentHP < maxHP) {
			float newHP = currentHP + (maxHP * amount * HEAL_HULL_AMOUNT * 0.01f);
			if (newHP < maxHP) ship.setHitpoints(newHP);
			else ship.setHitpoints(maxHP);
		}
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) HEAL_HULL_AMOUNT + "%";
		if (index == 1) return "" + (int) REPAIR_BONUS + "%";
		return null;
	}
}