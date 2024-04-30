package data.scripts.ix;

import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;

public class PanopticInterfaceUtil {
 
	private static String PANOPTIC_MOD_S = "ix_panoptic_strategic";
	private static String PANOPTIC_MOD_T = "ix_panoptic_tactical";
	private static String PANOPTIC_MOD_C = "ix_panoptic_command";
	private static String PANOPTIC_MOD_A = "ix_panoptic_automated";
	
	private static String CONFLICT_MOD_0 = "automated";
	private static String CONFLICT_MOD_1 = "specialsphmod_alpha_core_module_extension";
	private static String CONFLICT_MOD_2 = "specialsphmod_beta_core_module_extension";
	private static String CONFLICT_MOD_3 = "specialsphmod_gamma_core_module_extension";
	
	public static float PENALTY_FRIGATE = 2f;
	public static float PENALTY_DESTROYER = 3f;
	public static float PENALTY_CRUISER = 4f;
	public static float PENALTY_CAPITAL = 5f;
	public static float CR_PENALTY_MAX = 30f;
	
	public static boolean hasConflictMod(ShipVariantAPI variant) {
		if (variant.hasHullMod(CONFLICT_MOD_0) 
				|| variant.hasHullMod(CONFLICT_MOD_1) 
				|| variant.hasHullMod(CONFLICT_MOD_2) 
				|| variant.hasHullMod(CONFLICT_MOD_3)) return true;
		return false;
	}
	
	public static boolean hasPanopticMod(ShipVariantAPI variant) {
		if (variant.hasHullMod(PANOPTIC_MOD_S) 
				|| variant.hasHullMod(PANOPTIC_MOD_T) 
				|| variant.hasHullMod(PANOPTIC_MOD_C)) return true; 
		return false;
	}
	
	public static boolean hasPanopticAuto(ShipVariantAPI variant) {
		return variant.hasHullMod(PANOPTIC_MOD_A);
	}
	
	public static float getReadinessPenalty(FleetMemberAPI member, HullSize hullSize) {
		float penalty = 0f;
		if (member == null || member.getFleetData() == null 
				|| member.getFleetData().getMembersListCopy() == null) penalty = getReadinessPenaltyForHull(hullSize);
		else {
			List<FleetMemberAPI> fleetList = new ArrayList<FleetMemberAPI>();
			List<FleetMemberAPI> autoList = new ArrayList<FleetMemberAPI>();
			List<FleetMemberAPI> members = member.getFleetData().getMembersListCopy();
			for (FleetMemberAPI member : members) {
				if (hasPanopticMod(member.getVariant()) && !hasConflictMod(member.getVariant())) {
					fleetList.add(member);
				}
				else if (hasPanopticAuto(member.getVariant())) autoList.add(member);
			}
			for (FleetMemberAPI m : fleetList) {
				penalty += getReadinessPenaltyForHull(m.getVariant().getHullSize());
			}
			for (FleetMemberAPI m : autoList) {
				penalty -= getReadinessPenaltyForHull(m.getVariant().getHullSize());
			}
		}
		if (penalty < 0) penalty = 0;
		else if (penalty > CR_PENALTY_MAX) penalty = CR_PENALTY_MAX;
		return penalty;
	}
	
	public static int getPanopticShipCount (FleetMemberAPI member) {
		List<FleetMemberAPI> fleetList = new ArrayList<FleetMemberAPI>();
		List<FleetMemberAPI> members = member.getFleetData().getMembersListCopy();
		for (FleetMemberAPI member : members) {
			if (hasPanopticMod(member.getVariant()) || hasPanopticAuto(member.getVariant())) {
				fleetList.add(member);
			}
		}
		return fleetList.size();
	}
	
	public static float getReadinessPenaltyForHull(HullSize hullSize) {
		float deployReduction = 0f;
		if (hullSize.equals(HullSize.FRIGATE)) deployReduction = PENALTY_FRIGATE;
		else if (hullSize.equals(HullSize.DESTROYER)) deployReduction = PENALTY_DESTROYER;
		else if (hullSize.equals(HullSize.CRUISER)) deployReduction = PENALTY_CRUISER;
		else if (hullSize.equals(HullSize.CAPITAL_SHIP)) deployReduction = PENALTY_CAPITAL;
		return deployReduction;
	}
}