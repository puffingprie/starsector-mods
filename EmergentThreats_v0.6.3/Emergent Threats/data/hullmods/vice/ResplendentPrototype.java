package data.hullmods.vice;

import java.util.Collection;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class ResplendentPrototype extends BaseHullMod {
	
	private static String TERMINATOR = "Terminator";
	private static float THURSTER_BONUS = 33.34f;
	private static float MAX_BURN_BONUS = 1f;
	private static String DUAL_TERMINATOR_CORE = "Dual Terminator Core";
	private static float REACTOR_PENALTY = 20f;
	private static float FITTING_PENALTY = 40f; //text only, actual value 350 set in vice_resplendent.skin
	
	private static float MAX_CREW_MULT = 0.6f;

	private static String AFC_HULLMOD = "vice_adaptive_flight_command";
	private static String AFC_HULLMOD_DISPLAY = "Adaptive Flight Command";
	private static String AFC_SYSTEM = "vice_targetingsweep";
	private static String GRAV_HULLMOD = "vice_adaptive_gravity_drive";
	private static String GRAV_HULLMOD_DISPLAY = "Adaptive Gravity Drive";
	private static String GRAV_SYSTEM = "vice_fleetjump";
	private static String BASE_SYSTEM = "vice_massdeployment";
	
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		float modifier = THURSTER_BONUS * 0.01f;
		stats.getMaxSpeed().modifyMult(id, 1f + modifier);
		stats.getAcceleration().modifyMult(id, 1f + modifier);
		stats.getDeceleration().modifyMult(id, 1f + modifier);
		stats.getTurnAcceleration().modifyMult(id, 1f + modifier);
		stats.getMaxTurnRate().modifyMult(id, 1f + modifier);
		stats.getMaxBurnLevel().modifyFlat(id, MAX_BURN_BONUS);
		
		modifier = REACTOR_PENALTY * -0.01f;
		stats.getFluxCapacity().modifyMult(id, 1f + modifier);
		stats.getFluxDissipation().modifyMult(id, 1f + modifier);
		
		stats.getMaxCrewMod().modifyMult(id, 1f -MAX_CREW_MULT);
		
		stats.getVariant().getHullSpec().setShipSystemId(BASE_SYSTEM);
		Collection<String> mods = stats.getVariant().getNonBuiltInHullmods();
		for (String s : mods) {
			if (s.equals(GRAV_HULLMOD)) stats.getVariant().getHullSpec().setShipSystemId(GRAV_SYSTEM);
			else if (s.equals(AFC_HULLMOD)) stats.getVariant().getHullSpec().setShipSystemId(AFC_SYSTEM);
		}
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return TERMINATOR;
		if (index == 1) return "" + (int) THURSTER_BONUS + "%";
		if (index == 2) return "" + (int) MAX_BURN_BONUS;
		if (index == 3) return DUAL_TERMINATOR_CORE;
		if (index == 4) return "" + (int) REACTOR_PENALTY + "%";
		if (index == 5) return "" + (int) FITTING_PENALTY;
		if (index == 6) return AFC_HULLMOD_DISPLAY;
		if (index == 7) return GRAV_HULLMOD_DISPLAY;
		return null;
	}
}