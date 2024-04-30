package data.hullmods.vice;

import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

import data.scripts.vice.hullmods.RemnantSubsystemsUtil;

public class AdaptiveThrusterControl extends BaseHullMod {

	private static Map THURSTER_BONUS = new HashMap();
	static {
		THURSTER_BONUS.put(HullSize.FRIGATE, 15f);
		THURSTER_BONUS.put(HullSize.DESTROYER, 20f);
		THURSTER_BONUS.put(HullSize.CRUISER, 25f);
		THURSTER_BONUS.put(HullSize.CAPITAL_SHIP, 30f);
	}
	
	//Utility variables
	private RemnantSubsystemsUtil util = new RemnantSubsystemsUtil();
	
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		float bonus = (Float) THURSTER_BONUS.get(hullSize) * 0.01f;
		stats.getMaxSpeed().modifyMult(id, 1f + bonus);
		stats.getAcceleration().modifyMult(id, 1f + bonus );
		stats.getDeceleration().modifyMult(id, 1f + bonus);
		stats.getTurnAcceleration().modifyMult(id, 1f + bonus);
		stats.getMaxTurnRate().modifyMult(id, 1f + bonus);
	}
	
	private boolean hasThrusterModOverlap(ShipAPI ship) {
		return (ship.getVariant().getHullMods().contains("auxiliarythrusters") || ship.getVariant().getHullMods().contains("unstable_injector"));
	}
	
	@Override
    public boolean isApplicableToShip(ShipAPI ship) {
		if (util.isModuleCheck(ship)) return false;
		if (hasThrusterModOverlap(ship)) return false;
		return (util.isApplicable(ship) && util.isOnlyRemnantMod(ship));
	}
	
	public String getUnapplicableReason(ShipAPI ship) {
		if (util.isModuleCheck(ship)) return util.getIncompatibleCauseString("hub");
		if (hasThrusterModOverlap(ship)) return "Incompatible engine modification present";
		if (!util.isApplicable(ship)) return util.getIncompatibleCauseString("manufacturer");
		if (!util.isOnlyRemnantMod(ship)) return util.getIncompatibleCauseString("modcount");
		return null;
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + ((Float) THURSTER_BONUS.get(HullSize.FRIGATE)).intValue() + "%";
		if (index == 1) return "" + ((Float) THURSTER_BONUS.get(HullSize.DESTROYER)).intValue() + "%";
		if (index == 2) return "" + ((Float) THURSTER_BONUS.get(HullSize.CRUISER)).intValue() + "%";
		if (index == 3) return "" + ((Float) THURSTER_BONUS.get(HullSize.CAPITAL_SHIP)).intValue() + "%";
		return null;
	}
}