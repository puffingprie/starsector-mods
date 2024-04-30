package data.hullmods.ix;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import data.scripts.ix.PanopticInterfaceUtil;

public class PanopticInterfaceStrategic extends BaseHullMod {
	
	//dp bonus equal to cr penalty from PanopticInterfaceUtil.getReadinessPenaltyForHull()
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		if (PanopticInterfaceUtil.hasConflictMod(stats.getVariant())) return;
		float deployReduction = PanopticInterfaceUtil.getReadinessPenaltyForHull(hullSize) - 1f;
		stats.getDynamic().getMod(Stats.DEPLOYMENT_POINTS_MOD).modifyFlat(id, -deployReduction);
		if (stats.getFleetMember() == null || stats.getFleetMember().getOwner() != 0) return;
		float crPenalty = PanopticInterfaceUtil.getReadinessPenalty(stats.getFleetMember(), hullSize);
		stats.getMaxCombatReadiness().modifyFlat(id, -crPenalty * 0.01f, "Panoptic Interface");
	}
	
	@Override
	public boolean shouldAddDescriptionToTooltip(HullSize hullSize, ShipAPI ship, boolean isForModSpec) {
		return true;
	}
	
	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		if (ship == null) return;
		String s = "";
		float crPenalty = 0f;
		if (PanopticInterfaceUtil.hasConflictMod(ship.getVariant())) s = "Warning: Incompatible AI system present. Interface is inactive.";
		else if (Global.getSector() != null) {
			crPenalty = PanopticInterfaceUtil.getReadinessPenalty(ship.getFleetMember(), hullSize);
			int fleetCount = PanopticInterfaceUtil.getPanopticShipCount(ship.getFleetMember());
			s = "Combat Readiness reduced by " + (int) crPenalty + "%";
			String word = fleetCount > 1 ? "ships" : "ship";
			String append = " from " + fleetCount + " interfaced " + word;
			s += append;
		}
		if (!s.isEmpty()) {
			if (crPenalty == 0f) {
				s = "CR penalty is currently negated throughout the fleet";
				tooltip.addPara(s, Misc.getPositiveHighlightColor(), 10f);
			}
			else tooltip.addPara(s, Misc.getNegativeHighlightColor(), 10f);
		}
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) (PanopticInterfaceUtil.PENALTY_FRIGATE - 1f);
		if (index == 1) return "" + (int) (PanopticInterfaceUtil.PENALTY_DESTROYER - 1f);
		if (index == 2) return "" + (int) (PanopticInterfaceUtil.PENALTY_CRUISER - 1f);
		if (index == 3) return "" + (int) (PanopticInterfaceUtil.PENALTY_CAPITAL - 1f);
		if (index == 4) return "" + (int) PanopticInterfaceUtil.PENALTY_FRIGATE;
		if (index == 5) return "" + (int) PanopticInterfaceUtil.PENALTY_DESTROYER;
		if (index == 6) return "" + (int) PanopticInterfaceUtil.PENALTY_CRUISER;
		if (index == 7) return "" + (int) PanopticInterfaceUtil.PENALTY_CAPITAL + "%";
		if (index == 8) return "" + (int) PanopticInterfaceUtil.CR_PENALTY_MAX + "%";
		return null;
	}
}