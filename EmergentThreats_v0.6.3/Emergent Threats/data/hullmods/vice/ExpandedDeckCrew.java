package data.hullmods.vice;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class ExpandedDeckCrew extends BaseHullMod {

	private static float RATE_DECREASE_MODIFIER = 15f;
	private static float RATE_INCREASE_MODIFIER = 25f;
	private static float CREW_PER_DECK = 20f;
	private static String TEMPTEST = "tempest";
	private static String SEMI_AUTOMATED = "vice_ai_subsystem_integration";
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getDynamic().getStat(Stats.REPLACEMENT_RATE_DECREASE_MULT).modifyMult(id, 1f - RATE_DECREASE_MODIFIER / 100f);
		stats.getDynamic().getStat(Stats.REPLACEMENT_RATE_INCREASE_MULT).modifyPercent(id, RATE_INCREASE_MODIFIER);
		int crew = (int) (stats.getNumFighterBays().getBaseValue() * CREW_PER_DECK);
		stats.getMinCrewMod().modifyFlat(id, crew);
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) RATE_DECREASE_MODIFIER + "%";
		if (index == 1) return "" + (int) RATE_INCREASE_MODIFIER + "%";
		if (index == 2) return "" + (int) CREW_PER_DECK + "";
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
		int baysModified = (int) ship.getMutableStats().getNumFighterBays().getModifiedValue();
		if (baysModified <= 0) return false; // only count removed bays, not added bays for this
		int bays = (int) ship.getMutableStats().getNumFighterBays().getBaseValue();
		return bays > 0; 
	}
	
	public String getUnapplicableReason(ShipAPI ship) {
		if (hasIncompatibleHullmod(ship)) return "Cannot be installed on automated ships";
		return "Ship does not have standard fighter bays";
	}
}