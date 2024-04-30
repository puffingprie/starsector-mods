package data.hullmods.ix;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class AquilonFlareLauncher extends BaseHullMod {

	//dummy hullmod, system switch handled by Aquilon Nanoforge
	private static String MISSILE_COUNT = "12";
	private static String SYSTEM_ACTIVATION_COST = "10%";
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return MISSILE_COUNT;
		if (index == 1) return SYSTEM_ACTIVATION_COST;
		return null;
	}
}