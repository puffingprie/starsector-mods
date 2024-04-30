package data.scripts.ix.luna;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import lunalib.lunaRefit.BaseRefitButton;

public class SalvagePanopticonCoreButton extends BaseRefitButton {

	private static float SUCCESS_ODDS = 0.35f;
	private static String CORE_ID = "ix_panopticon_core";	
	private static String MOD_ID_0 = "ix_panoptic_automated";
	private static String MOD_ID_1 = "ix_panoptic_tactical";
	private static String MOD_ID_2 = "ix_panoptic_strategic";
	private static String MOD_ID_3 = "ix_panoptic_command";
	
	@Override
	public String getButtonName(FleetMemberAPI member, ShipVariantAPI variant) {
		return "Salvage Panopticon Core";
	}

	@Override
	public String getIconName(FleetMemberAPI member, ShipVariantAPI variant) {
		return "graphics/hullmods/ix_system_reset.png";
	}

	@Override
	public int getOrder(FleetMemberAPI member, ShipVariantAPI variant) {
		return 5004;
	}

	@Override
	public boolean hasTooltip(FleetMemberAPI member, ShipVariantAPI variant, MarketAPI market) {
		return true;
	}

	@Override
	public void addTooltip(TooltipMakerAPI tooltip, FleetMemberAPI member, ShipVariantAPI variant, MarketAPI market) {
		tooltip.addPara("Salvage Panopticon Core", 0f, Misc.getBasePlayerColor(), Misc.getBasePlayerColor());
		tooltip.addSpacer(5f);

		tooltip.addPara("Attempt to extract a Panopticon Core from this ship. The extraction process will remove the Panopticon Interface hullmod and have a 35% chance of recovering the core intact.", 0f);

		if ((!hasInterface(variant) || variant.hasHullMod(MOD_ID_0)) && !isCaptainPCore(member)) {
			tooltip.addSpacer(10f);
			tooltip.addPara("No interfaced Panopticon Core detected", 0f, Misc.getNegativeHighlightColor(), Misc.getNegativeHighlightColor());
		}
		
		else if (isCaptainPCore(member) && !isIntegratedPCore(member)) {
			tooltip.addSpacer(10f);
			tooltip.addPara("Panopticon Core is not fully integrated into drone ship and can be extracted without special procedures.", 0f, Misc.getNegativeHighlightColor(), Misc.getNegativeHighlightColor());
		}
		
		else if (isCaptainPCore(member) && isIntegratedPCore(member) && !hasInterface(variant)) {
			tooltip.addSpacer(10f);
			tooltip.addPara("Panoptic Interface is missing, core extraction can only be performed by scuttling the ship.", 0f, Misc.getNegativeHighlightColor(), Misc.getNegativeHighlightColor());
		}
		
		else if (isIntegratedPCore(member)) {
			tooltip.addSpacer(10f);
			tooltip.addPara("Warning: You cannot rebuild the Panoptic Interface (Primary). Removing the integrated core from this ship will destroy the hullmod.", 0f, Misc.getNegativeHighlightColor(), Misc.getNegativeHighlightColor());
		}
	}

	@Override
	public void onClick(FleetMemberAPI member, ShipVariantAPI variant, InputEventAPI event, MarketAPI market) {
		variant.getPermaMods().remove(MOD_ID_0);
		variant.getPermaMods().remove(MOD_ID_1);
		variant.getPermaMods().remove(MOD_ID_2);
		variant.getPermaMods().remove(MOD_ID_3);
		variant.getHullMods().remove(MOD_ID_0);
		variant.getHullMods().remove(MOD_ID_1);
		variant.getHullMods().remove(MOD_ID_2);
		variant.getHullMods().remove(MOD_ID_3);
		if (isCaptainPCore(member)) member.setCaptain(Global.getFactory().createPerson());
		if (Math.random() <= SUCCESS_ODDS) addCore();
		else addJunk();
		refreshVariant();
		refreshButtonList();
	}

	//Makes the button not clickable if mod cannot be fitted
	@Override
	public boolean isClickable(FleetMemberAPI member, ShipVariantAPI variant, MarketAPI market) {
		if (variant.hasHullMod(MOD_ID_0) && !isCaptainPCore(member)) return false;
		if (isCaptainPCore(member) && !isIntegratedPCore(member)) return false;
		return hasInterface(variant);
	}

	@Override
	public boolean shouldShow(FleetMemberAPI member, ShipVariantAPI variant, MarketAPI market) {
		return true;
	}
	
	private boolean hasInterface(ShipVariantAPI variant) {
		return (variant.hasHullMod(MOD_ID_0) 
				|| variant.hasHullMod(MOD_ID_1) 
				|| variant.hasHullMod(MOD_ID_2) 
				|| variant.hasHullMod(MOD_ID_3));
	}
	
	private boolean isCaptainPCore(FleetMemberAPI member) {
		return CORE_ID.equals(member.getCaptain().getAICoreId());
	}
	
	private boolean isIntegratedPCore(FleetMemberAPI member) {
		if (isCaptainPCore(member) && Misc.isUnremovable(member.getCaptain())) return true;
		else return false;
	}
	
	private void addCore() {
		CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
		cargo.addCommodity("ix_panopticon_core", 1);
	}
	
	private void addJunk() {
		CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
		cargo.addCommodity("ix_broken_core", 1);
	}
}