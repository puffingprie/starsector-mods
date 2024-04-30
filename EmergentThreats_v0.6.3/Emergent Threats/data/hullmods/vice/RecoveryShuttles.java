package data.hullmods.vice;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class RecoveryShuttles extends BaseHullMod {

	public static float CREW_LOSS_MULT = 0.25f;
	public static float SMOD_CREW_LOSS_MULT = 0.05f;
	private static String TEMPTEST = "tempest";
	private static String SEMI_AUTOMATED = "vice_ai_subsystem_integration";
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		boolean sMod = isSMod(stats);
		float mult = CREW_LOSS_MULT;
		if (sMod) mult = SMOD_CREW_LOSS_MULT; 
		stats.getDynamic().getStat(Stats.FIGHTER_CREW_LOSS_MULT).modifyMult(id, mult);
	}
		
	public String getSModDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) ((1f - SMOD_CREW_LOSS_MULT) * 100f) + "%";
		return null;
	}
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) ((1f - CREW_LOSS_MULT) * 100f) + "%";
		return null;
	}
	
	private boolean hasIncompatibleHullmod(ShipAPI ship) {
		if (ship == null) return true;
		if (ship.getVariant().hasHullMod(HullMods.AUTOMATED) || ship.getVariant().hasHullMod(SEMI_AUTOMATED)) return true;
		return false;
	}
	
	@Override
	public boolean isApplicableToShip(ShipAPI ship) {
		if (hasIncompatibleHullmod(ship)) return false;
		if (ship.getHullSpec().getHullId().equals(TEMPTEST)) return false;
		int bays = (int) ship.getMutableStats().getNumFighterBays().getModifiedValue();
		return bays > 0; 
	}
	
	public String getUnapplicableReason(ShipAPI ship) {
		if (hasIncompatibleHullmod(ship)) return "Cannot be installed on automated ships";
		return "Ship does not have standard fighter bays";
	}
}




