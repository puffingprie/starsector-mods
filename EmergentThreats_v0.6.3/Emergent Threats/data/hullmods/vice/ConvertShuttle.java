package data.hullmods.vice;

import java.util.LinkedHashSet;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;

public class ConvertShuttle extends BaseHullMod {
	
	private static int MIN_OP_REMAINING = 10;
	private static int SHUTTLE_BAY_ID = 3;
	
	private static String ELITE_HULLMOD = "vice_converted_battlecarrier";
	//private static String ELITE_WING_ID = "vice_kite_lg_wing_elite"; //reset by Converted Battlecarrier
	private static String STANDARD_WING_ID = "vice_kite_lg_wing";
	private static String REQUIRED_HULLMOD = "vice_converted_battlecarrier";
	
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		ShipVariantAPI variant = stats.getVariant();
		variant.setWingId(SHUTTLE_BAY_ID, STANDARD_WING_ID);
		variant.getHullMods().remove(id);
	}
	
	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {

	}
	
	private int getRemainingOP(ShipAPI ship) {
		int unusedOP = 0;
		try {
			unusedOP = ship.getVariant().getUnusedOP(Global.getSector().getCharacterData().getPerson().getFleetCommanderStats()); 
		}
		catch (Exception e) {
			unusedOP = ship.getVariant().getUnusedOP(Global.getFactory().createPerson().getFleetCommanderStats());
		}
		return unusedOP;	
	}
	
	private boolean isCorrectHull(ShipAPI ship) {
		return (ship.getVariant().getHullSpec().isBuiltInMod(REQUIRED_HULLMOD));
	}
	
	
	@Override
    public boolean isApplicableToShip(ShipAPI ship) {
		if (getRemainingOP(ship) < MIN_OP_REMAINING) return false;
		if (ship.getVariant().getWingId(SHUTTLE_BAY_ID) != null) return false;
		if (ship.getHullSpec().getFighterBays() < SHUTTLE_BAY_ID + 1) return false;
		return isCorrectHull(ship);
	}
	
	public String getUnapplicableReason(ShipAPI ship) {
		if (!isCorrectHull(ship)) return "Incompatible hull";
		if (getRemainingOP(ship) < MIN_OP_REMAINING) return "Insufficient ordnance points remaining";
		if (ship.getVariant().getWingId(SHUTTLE_BAY_ID) != null) return "Bay 4 is occupied";
		if (ship.getHullSpec().getFighterBays() < SHUTTLE_BAY_ID + 1) return "Bay 4 is not present";
		return null;
	}	
	
	public String getDescriptionParam(int index, HullSize hullSize) {

		return null;
	}
}