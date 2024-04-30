package data.hullmods.vice;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class ShipwideIntegration extends BaseHullMod {
	
	//dummy hullmod, exists for other hullmods to check for compatibility through RemnantSubsystemsUtil
	private static String ADAPTIVE_SUBSYSTEMS = "adaptive subsystems";
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + ADAPTIVE_SUBSYSTEMS;
		return null;
	}
}