package data.hullmods.vice;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class AdvancedTerminatorCore extends BaseHullMod {

	private static String TERMINATOR = "Terminator";
	private static int NEW_WING_SIZE = 3;
		
	//dummy hullmod, Terminator drones added by Odyssey (TT) or Lampetia innate hullmod if this mod is present
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		
	}
	
	@Override
    public boolean isApplicableToShip(ShipAPI ship) {
		return hasHullMod(ship);
	}
	
	private boolean hasHullMod(ShipAPI ship) {
		if (ship.getVariant().getHullSpec().isBuiltInMod("vice_lampetia_remnant")) return true;
		if (ship.getVariant().getHullSpec().isBuiltInMod("vice_odyssey_milspec")) return true;
		if (ship.getVariant().getHullSpec().isBuiltInMod("vice_resplendent_prototype")) return true;
		return false;
	}
	
	public String getUnapplicableReason(ShipAPI ship) {
		if (!hasHullMod(ship)) return "Incompatible hull";
		return null;
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return TERMINATOR;
		return null;
	}
	
	public String getSModDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + NEW_WING_SIZE;
		return null;
	}
}