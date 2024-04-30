package data.hullmods.vice;

import java.util.Collection;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class ConvertedCarrier extends BaseHullMod {
	
	private static float REACTOR_PENALTY = 20f;
	private static float FITTING_PENALTY = 15f; //text only, actual value 131 set in vice_unifier.skin
	private static String BUILT_IN_FIGHTERS = "Gladius (LG)";
	
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getFluxCapacity().modifyMult(id, 1f + REACTOR_PENALTY * -0.01f);
		stats.getFluxDissipation().modifyMult(id, 1f + REACTOR_PENALTY * -0.01f);
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) REACTOR_PENALTY + "%";
		if (index == 1) return "" + (int) FITTING_PENALTY;
		if (index == 2) return BUILT_IN_FIGHTERS;
		return null;
	}
}