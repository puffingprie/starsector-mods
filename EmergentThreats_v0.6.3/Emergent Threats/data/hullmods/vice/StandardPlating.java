package data.hullmods.vice;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;

//dummy hullmod for ExperimentalHull to check for which skin to apply
public class StandardPlating extends BaseHullMod {
	
	private static String EX_HULLMOD = "vice_experimental_hull";
	
	@Override
    public boolean isApplicableToShip(ShipAPI ship) {
		return ship.getVariant().hasHullMod(EX_HULLMOD);
	}
	
	public String getUnapplicableReason(ShipAPI ship) {
		if (!ship.getVariant().hasHullMod(EX_HULLMOD)) return "Ship lacks appropriate armor plating";
		return null;
	}
}