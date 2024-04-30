package data.hullmods.vice;

import java.awt.Color;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

import data.scripts.vice.hullmods.RemnantSubsystemsUtil;

public class AdaptiveTemporalShell extends BaseHullMod {

	private static float TIME_ACCELERATION_BONUS = 10f;
	private static float SHIELD_PENALTY = 25f;
	private static Color JITTER_UNDER_COLOR = new Color(90,165,255,155);
	private static String THIS_MOD = "vice_adaptive_temporal_shell";
	
	//Utility variables
	private RemnantSubsystemsUtil util = new RemnantSubsystemsUtil();
	
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		util.applyShipwideHullMod(stats.getVariant(), id, true);
		util.addModuleHandler(stats);
		
		stats.getShieldDamageTakenMult().modifyMult(id, 1f + SHIELD_PENALTY * 0.01f);
	}
	
	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		ship.getMutableStats().getTimeMult().modifyMult(id, 1f + TIME_ACCELERATION_BONUS * 0.01f);
	}
	
    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        if (!ship.isAlive() || ship.isPhased()) return;
		ship.setJitterUnder(ship, JITTER_UNDER_COLOR, 1f, 10, 12f);
	}
	
	@Override
    public boolean isApplicableToShip(ShipAPI ship) {
		if (util.isModuleCheck(ship)) return false;
		if (ship.getShield() == null) return false;
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
		if (index == 0) return "" + (int) TIME_ACCELERATION_BONUS + "%";
		if (index == 1) return "" + (int) SHIELD_PENALTY + "%";
		
		return null;
	}
}