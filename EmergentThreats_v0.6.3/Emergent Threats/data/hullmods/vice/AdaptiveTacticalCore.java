package data.hullmods.vice;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;

import data.scripts.vice.hullmods.RemnantSubsystemsUtil;

public class AdaptiveTacticalCore extends BaseHullMod {
	
	public static float FIGHTER_DAMAGE_BONUS = 50f;
	public static float MISSILE_DAMAGE_BONUS = 50f;
	public static float DAMAGE_TO_DESTROYERS = 10;
	public static float DAMAGE_TO_CRUISERS = 15;
	public static float DAMAGE_TO_CAPITALS = 20;
	
	private static String FIELD_MODULATION = "Field Modulation";
	private static String POINT_DEFENSE = "Point Defense";
	private static String TARGET_ANALYSIS = "Target Analysis";
	private static String THIS_MOD = "vice_adaptive_tactical_core";
	private static String RAT_CONFLICT_MOD = "rat_delta_assistant";
	
	//Utility variables
	private RemnantSubsystemsUtil util = new RemnantSubsystemsUtil();
	
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		util.applyShipwideHullMod(stats.getVariant(), id, true);
		util.addModuleHandler(stats);
	}
	
	//put here because effect always gets applied if left in applyEffectsBeforeShipCreation
	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		MutableShipStatsAPI stats = ship.getMutableStats();
		ShipVariantAPI variant = stats.getVariant();
		if (util.isWithoutCaptain(stats)) {
			stats.getDamageToFighters().modifyFlat(id, FIGHTER_DAMAGE_BONUS / 100f);
			stats.getDamageToMissiles().modifyFlat(id, MISSILE_DAMAGE_BONUS / 100f);
			stats.getDamageToDestroyers().modifyPercent(id, DAMAGE_TO_DESTROYERS);
			stats.getDamageToCruisers().modifyPercent(id, DAMAGE_TO_CRUISERS);
			stats.getDamageToCapital().modifyPercent(id, DAMAGE_TO_CAPITALS);
		}
	}
	
	@Override
    public boolean isApplicableToShip(ShipAPI ship) {
		if (!util.isWithoutCaptain(ship.getMutableStats())
				|| ship.getVariant().hasHullMod(RAT_CONFLICT_MOD)) return false;
		if (util.isModuleCheck(ship)) return false;
		if (ship.getVariant().hasHullMod(HullMods.NEURAL_INTERFACE) 
				|| ship.getVariant().hasHullMod(HullMods.NEURAL_INTEGRATOR)) return false;
		return (util.isApplicable(ship) && util.isOnlyRemnantMod(ship));
	}
	
	public String getUnapplicableReason(ShipAPI ship) {
		if (ship.getVariant().hasHullMod("vice_shipwide_integration") && (ship.getVariant().hasHullMod(THIS_MOD))) return null;
		if (!util.isWithoutCaptain(ship.getMutableStats())
				|| ship.getVariant().hasHullMod(RAT_CONFLICT_MOD)) return "Ship has human or AI captain present";
		if (util.isModuleCheck(ship)) return util.getIncompatibleCauseString("hub");
		if (ship.getVariant().hasHullMod(HullMods.NEURAL_INTERFACE) 
				|| ship.getVariant().hasHullMod(HullMods.NEURAL_INTEGRATOR)) return "Incompatible neural interface present";
		if (!util.isApplicable(ship)) return util.getIncompatibleCauseString("manufacturer");
		if (!util.isOnlyRemnantMod(ship)) return util.getIncompatibleCauseString("modcount");
		return null;
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return POINT_DEFENSE;
		if (index == 1) return TARGET_ANALYSIS;
		
		return null;
	}
}