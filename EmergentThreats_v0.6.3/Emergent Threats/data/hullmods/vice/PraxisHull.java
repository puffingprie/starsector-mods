package data.hullmods.vice;

import java.util.Collection;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;

public class PraxisHull extends BaseHullMod {
	
	private static float REACTOR_BONUS = 50f;
	private static float MAX_BURN_BONUS = 1f;
	private static float SHIELD_BONUS = 30f;
	private static float MIN_CREW_REDUCTION = 75f;
	private static float FITTING_BONUS = 20f; //text only, value already increased on Mimesis (P)
	private static float CR_INCREASE = 50f;
	
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getFluxCapacity().modifyMult(id, 1f + REACTOR_BONUS * 0.01f);
		stats.getFluxDissipation().modifyMult(id, 1f + REACTOR_BONUS * 0.01f);
		stats.getMaxBurnLevel().modifyFlat(id, MAX_BURN_BONUS);
		stats.getShieldDamageTakenMult().modifyMult(id, 1f - SHIELD_BONUS * 0.01f);
		stats.getMinCrewMod().modifyMult(id, 1f - MIN_CREW_REDUCTION * 0.01f);
		stats.getDynamic().getMod("deployment_points_mod").modifyMult(id, 1f + CR_INCREASE * 0.01f);
		stats.getSuppliesToRecover().modifyMult(id, 1f + CR_INCREASE * 0.01f);
		stats.getSuppliesPerMonth().modifyMult(id, 1f + CR_INCREASE * 0.01f);
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) REACTOR_BONUS + "%";
		if (index == 1) return "" + (int) MAX_BURN_BONUS;
		if (index == 2) return "" + (int) SHIELD_BONUS + "%";
		if (index == 3) return "" + (int) MIN_CREW_REDUCTION + "%";
		if (index == 4) return "" + (int) FITTING_BONUS;
		if (index == 5) return "" + (int) CR_INCREASE + "%";
		return null;
	}
}
