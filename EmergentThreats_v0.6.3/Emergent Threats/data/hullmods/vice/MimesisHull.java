package data.hullmods.vice;

import java.util.Collection;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;

public class MimesisHull extends BaseHullMod {
	
	private static float REACTOR_BONUS = 50f;
	private static float MAX_BURN_BONUS = 1f;
	private static float SHIELD_ARC_BONUS = 120f;
	private static float SHIELD_BONUS = 20f;
	private static float SHIELD_FLUX_PENLTY = 20f;
	private static float FITTING_BONUS = 30f; //text only, value already increased on Mimesis (P)
	private static float CR_INCREASE = 50f;
	
	private static String ATC_HULLMOD = "vice_adaptive_thruster_control";
	private static String ATC_HULLMOD_DISPLAY = "Adaptive Thruster Control";
	private static String ATC_SYSTEM = "plasmajets";
	private static String BASE_SYSTEM = "maneuveringjets";
	
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getFluxCapacity().modifyMult(id, 1f + REACTOR_BONUS * 0.01f);
		stats.getFluxDissipation().modifyMult(id, 1f + REACTOR_BONUS * 0.01f);
		stats.getMaxBurnLevel().modifyFlat(id, MAX_BURN_BONUS);
		stats.getShieldArcBonus().modifyFlat(id, SHIELD_ARC_BONUS);
		stats.getShieldDamageTakenMult().modifyMult(id, 1f - SHIELD_BONUS * 0.01f);
		stats.getShieldUpkeepMult().modifyMult(id, 1f + SHIELD_FLUX_PENLTY * 0.01f);
		
		stats.getDynamic().getMod("deployment_points_mod").modifyMult(id, 1f + CR_INCREASE * 0.01f);
		stats.getSuppliesToRecover().modifyMult(id, 1f + CR_INCREASE * 0.01f);
		stats.getSuppliesPerMonth().modifyMult(id, 1f + CR_INCREASE * 0.01f);
		
		ShipVariantAPI variant = stats.getVariant();
		if (variant.getModuleVariant("SM 1") == null) return;
		ShipVariantAPI module = variant.getModuleVariant("SM 1");
		module.setVariantDisplayName("Active");
		
		stats.getVariant().getHullSpec().setShipSystemId(BASE_SYSTEM);
		Collection<String> mods = stats.getVariant().getNonBuiltInHullmods();
		for (String s : mods) {
			if (s.equals(ATC_HULLMOD)) stats.getVariant().getHullSpec().setShipSystemId(ATC_SYSTEM);
		}
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) REACTOR_BONUS + "%";
		if (index == 1) return "" + (int) MAX_BURN_BONUS;
		if (index == 2) return "" + (int) SHIELD_ARC_BONUS;
		if (index == 3) return "" + (int) SHIELD_BONUS + "%";
		if (index == 4) return "" + (int) SHIELD_FLUX_PENLTY + "%";
		if (index == 5) return "" + (int) FITTING_BONUS;
		if (index == 6) return "" + (int) CR_INCREASE + "%";
		if (index == 7) return ATC_HULLMOD_DISPLAY;
		return null;
	}
}
