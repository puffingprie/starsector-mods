package data.hullmods.vice;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

import data.scripts.vice.hullmods.RemnantSubsystemsUtil;

public class AdaptiveEntropyArrester extends BaseHullMod {

	private static float HEAL_HULL_AMOUNT = 1f; //+1% per second
	private static float REPAIR_BONUS = 50f;
	private static String CONFLICT_MOD = "autorepair";
	private static String THIS_MOD = "vice_adaptive_entropy_arrester";
	//Utility variables
	private RemnantSubsystemsUtil util = new RemnantSubsystemsUtil();
	
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getVariant().getHullMods().remove(CONFLICT_MOD);
		stats.getCombatEngineRepairTimeMult().modifyMult(id, 1f - REPAIR_BONUS * 0.01f);
		stats.getCombatWeaponRepairTimeMult().modifyMult(id, 1f - REPAIR_BONUS * 0.01f);
		util.applyShipwideHullMod(stats.getVariant(), id, true);
		util.addModuleHandler(stats);
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
	
	@Override
    public boolean isApplicableToShip(ShipAPI ship) {
		if (ship.getVariant().hasHullMod(CONFLICT_MOD)) return false;
		if (util.isModuleCheck(ship)) return false;
		if (ship.getVariant().hasHullMod("automated") 
				&& ship.getVariant().hasHullMod("ix_plasma_ramjet") 
				&& util.isOnlyRemnantMod(ship)) return true;
		return (util.isApplicable(ship) && util.isOnlyRemnantMod(ship));
	}

	public String getUnapplicableReason(ShipAPI ship) {
		if (ship.getVariant().hasHullMod("vice_shipwide_integration") && (ship.getVariant().hasHullMod(THIS_MOD))) return null;
		if (util.isModuleCheck(ship)) return util.getIncompatibleCauseString("hub");
		if (!util.isApplicable(ship)) return util.getIncompatibleCauseString("manufacturer");
		if (ship.getVariant().hasHullMod(CONFLICT_MOD)) return "Incompatible with Automated Repair Unit";
		if (!util.isOnlyRemnantMod(ship)) return util.getIncompatibleCauseString("modcount");
		return null;
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) HEAL_HULL_AMOUNT + "%";
		if (index == 1) return "" + (int) REPAIR_BONUS + "%";
		return null;
	}
}