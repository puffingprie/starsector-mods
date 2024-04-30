package data.hullmods.ix;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import data.scripts.ix.PanopticInterfaceUtil;
import data.scripts.ix.PanopticonCorePlugin;

public class PanopticInterfaceCommand extends BaseHullMod {
	
	private static String COMMAND_CORE_ID = "ix_command_core";
	private static String HANDLER_MOD_ID = "ix_panoptic_command_handler";
	
	private static String SKILL_NAME_1 = "Field Modulation";
	private static String SKILL_NAME_2 = "Gunnery Implant";
	private static String SKILL_NAME_3 = "Target Analysis";
	private static String SKILL_NAME_4 = "Sword of the Fleet";
	
	private static float CR_PENALTY_MAX = 30f;
	
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getVariant().addMod(HANDLER_MOD_ID);
		if (stats.getFleetMember() == null 
				|| stats.getFleetMember().getCaptain() == null 
				|| !stats.getFleetMember().getCaptain().isDefault()) return;
		if (PanopticInterfaceUtil.hasConflictMod(stats.getVariant())) return;
		PersonAPI p = new PanopticonCorePlugin().createPerson(COMMAND_CORE_ID, "player", null);
		stats.getFleetMember().setCaptain(p);
		removeCore();
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
	
	private void removeCore() {
		try {
			CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
			cargo.removeCommodity(COMMAND_CORE_ID, 1f);
		}
		catch (Exception e) {}
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return SKILL_NAME_1;
		if (index == 1) return SKILL_NAME_2;
		if (index == 2) return SKILL_NAME_3;
		if (index == 3) return SKILL_NAME_4;
		if (index == 4) return "" + (int) PanopticInterfaceUtil.PENALTY_FRIGATE;
		if (index == 5) return "" + (int) PanopticInterfaceUtil.PENALTY_DESTROYER;
		if (index == 6) return "" + (int) PanopticInterfaceUtil.PENALTY_CRUISER;
		if (index == 7) return "" + (int) PanopticInterfaceUtil.PENALTY_CAPITAL + "%";
		if (index == 8) return "" + (int) PanopticInterfaceUtil.CR_PENALTY_MAX + "%";
		return null;
	}
}