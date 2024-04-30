package data.hullmods.vice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;

import data.scripts.vice.hullmods.RemnantSubsystemsUtil;

public class ModuleHandler extends BaseHullMod {

	private static String DRONE_BAY = "vice_adaptive_drone_bay";
	private static String EMITTER_DIODES = "vice_adaptive_emitter_diodes";
	private static String FLIGHT_COMMAND = "vice_adaptive_flight_command";
	private static String FLUX_DISSIPATOR = "vice_adaptive_flux_dissipator";
	private static String GRAVITY_DRIVE = "vice_adaptive_gravity_drive";
	private static String PULSE_RESONATOR = "vice_adaptive_pulse_resonator";
	private static String REACTOR_CHAMBER = "vice_adaptive_reactor_chamber";
	
	private static String METASTATIC_GROWTH = "vice_adaptive_metastatic_growth";
	private static String NEURAL_NET = "vice_adaptive_neural_net";
	private static String PHASE_COILS = "vice_adaptive_phase_coils";
	private static String TACTICAL_CORE = "vice_adaptive_tactical_core";
	private static String TEMPORAL_SHELL = "vice_adaptive_temporal_shell";
	private static String STRIP_FITTING_FIXER = "vice_strip_fixer";
	
	private static List<String> MULTI_MODULE_HULLMODS = new ArrayList<String>();
	static {
		MULTI_MODULE_HULLMODS.add(FLUX_DISSIPATOR);
		MULTI_MODULE_HULLMODS.add(METASTATIC_GROWTH);
		MULTI_MODULE_HULLMODS.add(NEURAL_NET);
		MULTI_MODULE_HULLMODS.add(PHASE_COILS);
		MULTI_MODULE_HULLMODS.add(TACTICAL_CORE);
		MULTI_MODULE_HULLMODS.add(TEMPORAL_SHELL);
	}
	
	private static List<String> ADAPTIVE_HULLMODS = new ArrayList<String>();
	static {
		ADAPTIVE_HULLMODS.add(DRONE_BAY);
		ADAPTIVE_HULLMODS.add(EMITTER_DIODES);
		ADAPTIVE_HULLMODS.add(FLIGHT_COMMAND);
		ADAPTIVE_HULLMODS.add(GRAVITY_DRIVE);
		ADAPTIVE_HULLMODS.add(PULSE_RESONATOR);
		ADAPTIVE_HULLMODS.add(REACTOR_CHAMBER);

		ADAPTIVE_HULLMODS.add(FLUX_DISSIPATOR);
		ADAPTIVE_HULLMODS.add(METASTATIC_GROWTH);
		ADAPTIVE_HULLMODS.add(NEURAL_NET);
		ADAPTIVE_HULLMODS.add(PHASE_COILS);
		ADAPTIVE_HULLMODS.add(TACTICAL_CORE);
		ADAPTIVE_HULLMODS.add(TEMPORAL_SHELL);
	}
	
	//Utility variables
	private RemnantSubsystemsUtil util = new RemnantSubsystemsUtil();
	
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		ShipVariantAPI variant = stats.getVariant();
		Collection<String> modlist = variant.getNonBuiltInHullmods();
		int modCount = 0;
		
		//if multi-module subsystem is on the main ship, delete all other adaptive subsystems from modules
		//then add permamod version of multi-module subsystem to modules
		//if multi-module subsystem is not on the main ship, delete copies of it on modules
		for (String mod : MULTI_MODULE_HULLMODS) {
			if (variant.hasHullMod(mod)) {
				modCount++;
				util.applyShipwideHullMod(variant, mod, true);
				for (String s : ADAPTIVE_HULLMODS) {
					if (!s.equals(mod)) util.removeShipwideHullMod(variant, s);
				}
			}
			else util.removeShipwideHullMod(variant, mod);
		}
		if (modCount == 0 && util.isModuleCheck(stats)) variant.addPermaMod(STRIP_FITTING_FIXER);
	}
	
	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		MutableShipStatsAPI stats = ship.getMutableStats();
		ShipVariantAPI variant = stats.getVariant();
	
		//handles weirdness with multiple permamods showing up on modules when clicking undo
		int modCount = 0;
		String firstMod = "";
		if (util.isModuleCheck(ship)) {
			if (variant.hasHullMod(STRIP_FITTING_FIXER)) modCount++;
			for (String mod : MULTI_MODULE_HULLMODS) {
				if (variant.hasHullMod(mod)) modCount++;
			}
			if (modCount > 1) {
				for (String s : ADAPTIVE_HULLMODS) variant.removePermaMod(s);
				variant.removePermaMod(STRIP_FITTING_FIXER);
			}
		}
		
		//done here since captains are added after applyEffectsBeforeShipCreation
		if (!util.isWithoutCaptain(ship.getMutableStats())) variant.getHullMods().remove(TACTICAL_CORE);
	}
}