package data.hullmods.ix;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

import data.scripts.ix.PanopticInterfaceUtil;

public class PanopticInterfaceAutomated extends BaseHullMod {
	
	private static String PANOPTICON_CORE_ID = "ix_panopticon_core";
	private static float P_CORE_MULT = 2f;
	
	@Override
	//reduces automated ship points multiplier from x3.5 to x2 when piloted by a Panopticon Core
	//also reduces fleet wide interface cr penalty, handled by PanopticInterfaceUtil.getReadinessPenalty
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		if (Global.getSettings().getModManager().isModEnabled("TrulyAutomatedShips")) return;
		if (stats.getFleetMember() == null 
				|| stats.getFleetMember().getCaptain() == null 
				|| !stats.getFleetMember().getCaptain().isAICore()) return;
		PersonAPI p = stats.getFleetMember().getCaptain();
		String coreId = p.getAICoreId();
		if (PANOPTICON_CORE_ID.equals(coreId)) p.getMemoryWithoutUpdate().set("$autoPointsMult", P_CORE_MULT);
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "x3.5"; //default
		if (index == 1) return "x" + (int) P_CORE_MULT;
		if (index == 2) return "" + (int) PanopticInterfaceUtil.getReadinessPenaltyForHull(hullSize) + "%";
		return null;
	}
}