package data.hullmods.vice;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

import data.scripts.vice.hullmods.RemnantSubsystemsUtil;

public class AdaptiveMetastaticGrowth extends BaseHullMod {

	private static float HULL_BONUS = 20f;
	private static float MISSILE_HULL_BONUS = 20f;
	private static float MISSILE_DAMAGE_BONUS = 10f;
	private static String THIS_MOD = "vice_adaptive_metastatic_growth";
	
	//Utility variables
	private RemnantSubsystemsUtil util = new RemnantSubsystemsUtil();
	
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		util.applyShipwideHullMod(stats.getVariant(), id, true);
		util.addModuleHandler(stats);
		
		stats.getHullBonus().modifyPercent(id, HULL_BONUS);
		stats.getMissileHealthBonus().modifyMult(id, 1f + MISSILE_HULL_BONUS * 0.01f);
		stats.getMissileWeaponDamageMult().modifyMult(id, 1f + MISSILE_DAMAGE_BONUS * 0.01f);
	}

	@Override
    public boolean isApplicableToShip(ShipAPI ship) {
		if (util.isModuleCheck(ship)) return false;
		return (util.isApplicable(ship) && util.isOnlyRemnantMod(ship));
	}
	
	public String getUnapplicableReason(ShipAPI ship) {
		if (ship.getVariant().hasHullMod("vice_shipwide_integration") && (ship.getVariant().hasHullMod(THIS_MOD))) return null;
		if (util.isModuleCheck(ship)) return util.getIncompatibleCauseString("hub");
		if (!util.isApplicable(ship)) return util.getIncompatibleCauseString("manufacturer");
		if (!util.isOnlyRemnantMod(ship)) return util.getIncompatibleCauseString("modcount");
		return null;
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) HULL_BONUS + "%";
		if (index == 1) return "" + (int) MISSILE_HULL_BONUS + "%";
		if (index == 2) return "" + (int) MISSILE_DAMAGE_BONUS + "%";
		return null;
	}
}