package data.hullmods.vice;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class EffulgentRemnant extends BaseHullMod {

	private static String FIRST_BONUS_TEXT = "Large Missile Hardpoint";
	private static String SECOND_BONUS_TEXT = "Missile Autoforge";
	private static float CR_INCREASE = 27.3f;
	
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getDynamic().getMod("deployment_points_mod").modifyMult(id, 1f + CR_INCREASE * 0.01f);
		stats.getSuppliesToRecover().modifyMult(id, 1f + CR_INCREASE * 0.01f);
		stats.getSuppliesPerMonth().modifyMult(id, 1f + CR_INCREASE * 0.01f);
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return FIRST_BONUS_TEXT;
		if (index == 1) return SECOND_BONUS_TEXT;
		if (index == 2) return "" + (int) CR_INCREASE + "%";
		return null;
	}
}