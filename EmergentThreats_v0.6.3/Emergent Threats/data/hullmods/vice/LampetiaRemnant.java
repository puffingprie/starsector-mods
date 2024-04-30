package data.hullmods.vice;

import java.util.Collection;
import java.util.LinkedHashSet;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;

public class LampetiaRemnant extends BaseHullMod {

	private static float SHIELD_BONUS = 40f;
	private static float CR_INCREASE = 11.1f;
	
    private static String BUILT_IN_DRONES = "Spark";
	private static String BUILT_IN_HULLMOD = "Drone Autoforge";
	private static String SECONDARY_HANGAR = "secondary drone bay";
	private static String GRAV_HULLMOD = "vice_adaptive_gravity_drive";
	private static String GRAV_HULLMOD_DISPLAY = "Adaptive Gravity Drive";
	private static String GRAV_SYSTEM = "vice_fleetjump";
	private static String BASE_SYSTEM = "displacer";
	
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getShieldDamageTakenMult().modifyMult(id, 1f - SHIELD_BONUS * 0.01f);
		stats.getDynamic().getMod("deployment_points_mod").modifyMult(id, 1f + CR_INCREASE * 0.01f);
		stats.getSuppliesToRecover().modifyMult(id, 1f + CR_INCREASE * 0.01f);
		stats.getSuppliesPerMonth().modifyMult(id, 1f + CR_INCREASE * 0.01f);
		
		if (stats.getVariant().getModuleVariant("SM 1") == null) return;
		ShipVariantAPI module = stats.getVariant().getModuleVariant("SM 1");
		if (stats.getVariant().hasHullMod("vice_advanced_terminator_core")) {
			module.removeMod("vice_system_inhibitor");
			module.setWingId(0, "terminator_wing");
			LinkedHashSet<String> sMods = stats.getVariant().getSMods();
			for (String s : sMods) {
				if (s.equals("vice_advanced_terminator_core")) module.setWingId(0, "vice_terminator_triple_wing");
			}
		}
		else {
			module.setWingId(0, "vice_spark_wing");
			module.addMod("vice_system_inhibitor");
		}
		
		stats.getVariant().getHullSpec().setShipSystemId(BASE_SYSTEM);
		Collection<String> mods = stats.getVariant().getNonBuiltInHullmods();
		for (String s : mods) {
			if (s.equals(GRAV_HULLMOD)) stats.getVariant().getHullSpec().setShipSystemId(GRAV_SYSTEM);
		}
	}
	
	@Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		deleteLPC("vice_spark_wing");
		deleteLPC("terminator_wing");
		deleteLPC("vice_terminator_triple_wing");
	}

	private void deleteLPC(String wing) {
		try {
			CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
			for (CargoStackAPI s : cargo.getStacksCopy()) {
				if (s.isFighterWingStack() && s.getFighterWingSpecIfWing().getId().equals(wing)) cargo.removeStack(s);
			}
		}
		catch (Exception e) {}
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return BUILT_IN_DRONES;
		if (index == 1) return SECONDARY_HANGAR;
		if (index == 2) return "" + (int) SHIELD_BONUS + "%";
		if (index == 3) return BUILT_IN_HULLMOD;
		if (index == 4) return "" + (int) CR_INCREASE + "%";
		if (index == 5) return GRAV_HULLMOD_DISPLAY;
		return null;
	}
}
