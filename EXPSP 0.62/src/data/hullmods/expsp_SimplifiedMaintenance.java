package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class expsp_SimplifiedMaintenance extends BaseHullMod {

	private static final float SUPPLY_USE_MULT = 6f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getSuppliesPerMonth().modifyFlat(id, SUPPLY_USE_MULT*-1);
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int)(SUPPLY_USE_MULT) + " units per month";
		return null;
	}


}
