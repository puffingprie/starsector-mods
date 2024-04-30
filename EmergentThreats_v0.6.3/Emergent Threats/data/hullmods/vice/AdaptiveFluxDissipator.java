package data.hullmods.vice;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

import data.scripts.vice.hullmods.RemnantSubsystemsUtil;

public class AdaptiveFluxDissipator extends BaseHullMod {

	private static float FLUX_DISSIPATION = 10f;
	private static String THIS_MOD = "vice_adaptive_flux_dissipator";
	
	//Utility variables
	private RemnantSubsystemsUtil util = new RemnantSubsystemsUtil();
	
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		util.applyShipwideHullMod(stats.getVariant(), id, true);
		util.addModuleHandler(stats);
		stats.getHardFluxDissipationFraction().modifyMult(id, 1f + FLUX_DISSIPATION * 0.01f);
	}
	
	@Override
    public boolean isApplicableToShip(ShipAPI ship) {
		if (util.isModuleCheck(ship)) return false;
		if (ship.getShield() == null) return false;
		if (ship.getVariant().hasHullMod("automated") 
				&& ship.getVariant().hasHullMod("ix_plasma_ramjet") 
				&& util.isOnlyRemnantMod(ship)) return true;
		return (util.isApplicable(ship) && util.isOnlyRemnantMod(ship));
	}
	
	public String getUnapplicableReason(ShipAPI ship) {
		if (ship.getVariant().hasHullMod("vice_shipwide_integration") && (ship.getVariant().hasHullMod(THIS_MOD))) return null;
		if (util.isModuleCheck(ship)) return util.getIncompatibleCauseString("hub");
		if (!util.isApplicable(ship)) return util.getIncompatibleCauseString("manufacturer");
		if (!util.isOnlyRemnantMod(ship)) return util.getIncompatibleCauseString("modcount");
		if (ship.getShield() == null) return util.getIncompatibleCauseString("noshields");
		return null;
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) FLUX_DISSIPATION + "%";		
		return null;
	}
}