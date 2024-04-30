package data.hullmods.ix;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class TerminusRelayBuiltIn extends BaseHullMod {
	
	//display texts only
	private static int DAMAGE_MISSILES_PERCENT = 100;
	private static int WEAPON_RANGE_BONUS = 300;
	private static int RELAY_RANGE = 800;
	private static int DAMAGE = 300;
	private static int EMP_DAMAGE = 300;
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + DAMAGE_MISSILES_PERCENT + "%";
		if (index == 1) return "" + WEAPON_RANGE_BONUS;
		if (index == 2) return "" + RELAY_RANGE;
		if (index == 3) return "" + DAMAGE;
		if (index == 4) return "" + EMP_DAMAGE;
		return null;
	}
}