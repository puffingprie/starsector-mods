package data.hullmods.vice;

import java.util.LinkedHashSet;

import com.fs.starfarer.api.impl.hullmods.BaseLogisticsHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;

import data.scripts.vice.hullmods.RemnantSubsystemsUtil;

public class AISubsystemIntegration extends BaseLogisticsHullMod {
	
	private static float NO_MIN_CREW_BONUS = 0f;
	private static float CREWED_SQUADRON_SPEED_PENALTY = 20f;
	private static String ADAPTIVE_SUBSYSTEMS = "adaptive subsystems";
	private static String FIGHTER_AUTOMATION = "fighters are automated";
	
	private static String REPLACEMENT_S_MOD = "vice_abomination_interface";
	private static String SHIPWIDE_INTEGRATION_CHECKER = "vice_shipwide_integration_checker";
	
	//Utility variables
	private RemnantSubsystemsUtil util = new RemnantSubsystemsUtil();
	
	//RemnantSubsystemsUtil uses this hullmod to check for permission to equip subsystems on non-remnant ships.
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		ShipVariantAPI variant = stats.getVariant();
		if (isBuiltInMod(variant, id)) {
			if (!variant.getModuleSlots().isEmpty()) variant.addPermaMod(SHIPWIDE_INTEGRATION_CHECKER);
			stats.getMinCrewMod().modifyMult(id, 0);
			stats.getDynamic().getStat(Stats.FIGHTER_CREW_LOSS_MULT).modifyMult(id, NO_MIN_CREW_BONUS);
			util.applyShipwideIntegration(variant);
		}
	}
	
	@Override
	//backwards compatability for people who built AI Integration into ships that no longer qualify
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		if (isSMod(ship) && util.isAbomination(ship)) {
			ShipVariantAPI variant = ship.getVariant();
			variant.addPermaMod(REPLACEMENT_S_MOD, true);
			variant.getSMods().remove(id);
			variant.getPermaMods().remove(id);
			variant.getHullMods().remove(id);
		}
	}
	
	//needed due to bounty ships with non-smod built-in versions
	private boolean isBuiltInMod(ShipVariantAPI variant, String id) {
		if (variant.getHullSpec().isBuiltInMod(id)) return true;
		LinkedHashSet<String> sMods = variant.getSMods();
		for (String mod : sMods) {
			if (mod.equals(id)) return true;
		}
		return false;
	}
	
	@Override
	public void applyEffectsToFighterSpawnedByShip(ShipAPI fighter, ShipAPI ship, String id) {
		ShipVariantAPI variant = ship.getVariant();
		if (!isBuiltInMod(variant, id)) return;
		if (fighter.getHullSpec().getMinCrew() != 0) {
			MutableShipStatsAPI stats = fighter.getMutableStats();
			stats.getMaxSpeed().modifyMult(id, 1f - CREWED_SQUADRON_SPEED_PENALTY * 0.01f);		
		}
	}
	
	@Override
    public boolean isApplicableToShip(ShipAPI ship) {
		if (util.isModuleCheck(ship)) return false;
		return (util.isSubsystemIntegrationApplicable(ship));
	}
	
	public String getUnapplicableReason(ShipAPI ship) {
		if (util.isModuleCheck(ship)) return "Integration is performed on the central hub";
		if (!util.isMinCrewWithinAutomationLimit(ship)) return "Minimum crew exceeds " + (int) util.getAutomationCrewLimit();
		if (util.isAbomination(ship)) return "Hull is incompatible with automation overhaul";
		if (!util.isSubsystemIntegrationApplicable(ship)) return util.getIncompatibleCauseString("unnecessary");
		return null;
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return ADAPTIVE_SUBSYSTEMS;
		if (index == 1) return "" + (int) NO_MIN_CREW_BONUS;
		if (index == 2) return FIGHTER_AUTOMATION;
		if (index == 3) return "" + (int) CREWED_SQUADRON_SPEED_PENALTY + "%";
		
		return null;
	}
}
