package data.hullmods.vice;

import java.util.Collection;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class ConvertedEscortCarrier extends BaseHullMod {
	
	private static int EXTRA_SQUADRONS = 1; //text only
	private static String SHIP_SYSTEM = "targeting sweep"; //text only
	private static float CR_INCREASE = 66.66f;
	private static float CAPACITY_PENALTY_PERCENT = 80f;
	
	private static String BUILT_IN_FIGHTERS = "Gladius (LG)";
	
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getDynamic().getMod("deployment_points_mod").modifyMult(id, 1f + CR_INCREASE * 0.01f);
		stats.getSuppliesToRecover().modifyMult(id, 1f + CR_INCREASE * 0.01f);
		stats.getSuppliesPerMonth().modifyMult(id, 1f + CR_INCREASE * 0.01f);
		stats.getCargoMod().modifyMult(id, 1f - CAPACITY_PENALTY_PERCENT * 0.01f);
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) EXTRA_SQUADRONS;
		if (index == 1) return SHIP_SYSTEM;
		if (index == 2) return "" + (int) Math.ceil(CR_INCREASE) + "%";
		if (index == 3) return "" + (int) CAPACITY_PENALTY_PERCENT + "%";
		return null;
	}
}