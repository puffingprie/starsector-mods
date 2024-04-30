package data.hullmods.vice;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class AquilonSpatialCharge extends BaseHullMod {

	//dummy hullmod, system switch handled by Aquilon Nanoforge
	private static String MISSILE_COUNT = "8";
	private static String MISSILE_DAMAGE = "150 energy";
	private static String MISSILE_EMP = "300 EMP";
	private static String SYSTEM_ACTIVATION_COST = "10%";
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return MISSILE_COUNT;
		if (index == 1) return MISSILE_DAMAGE;
		if (index == 2) return MISSILE_EMP;
		if (index == 3) return SYSTEM_ACTIVATION_COST;
		return null;
	}
}