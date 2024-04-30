package data.hullmods.ix;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

//dummy hullmod, actual weapon switch handled by OdysseyRetrofit
public class AntecedentDevice extends BaseHullMod {

	private static String SHIP_TYPE = "Odyssey (IX)";
	private static String WEAPON_TYPE = "Antecedent Device";
	
	@Override
    public boolean isApplicableToShip(ShipAPI ship) {
		return (ship.getVariant().hasHullMod("ix_odyssey_retrofit"));
		
	}
	
	public String getUnapplicableReason(ShipAPI ship) {
		if (!ship.getVariant().hasHullMod("ix_odyssey_retrofit")) return "Can only be fitted to Odyssey (IX)";
		return null;
	}	

	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return SHIP_TYPE;
		if (index == 1) return WEAPON_TYPE;
		return null;
	}
}