package data.hullmods.vice;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class AquilonMicroMissile extends BaseHullMod {
	
	//dummy hullmod, system switch handled by Aquilon Nanoforge
	private static String MISSILE_COUNT = "24";
	private static String MISSILE_DAMAGE = "200 fragmentation";
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return MISSILE_COUNT;
		if (index == 1) return MISSILE_DAMAGE;
		return null;
	}
}