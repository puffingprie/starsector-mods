package data.scripts.ix.luna;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import lunalib.lunaRefit.BaseRefitButton;

public class PanopticTacticalRefitButton extends BaseRefitButton {

	private static String CORE_ID = "ix_panopticon_core";

	private static String MOD_ID = "ix_panoptic_tactical";
	private static String MOD_ID_1 = "ix_panoptic_strategic";
	private static String MOD_ID_2 = "ix_panoptic_automated";
	private static String MOD_ID_3 = "ix_panoptic_command";
	
	private static String CONFLICT_MOD_1 = "specialsphmod_alpha_core_module_extension";
	private static String CONFLICT_MOD_2 = "specialsphmod_beta_core_module_extension";
	private static String CONFLICT_MOD_3 = "specialsphmod_gamma_core_module_extension";

	@Override
	public String getButtonName(FleetMemberAPI member, ShipVariantAPI variant) {
		return "Panoptic Interface (Tactical)";
	}

	@Override
	public String getIconName(FleetMemberAPI member, ShipVariantAPI variant) {
		return "graphics/icons/ix_panopticon_core.png";
	}

	@Override
	public int getOrder(FleetMemberAPI member, ShipVariantAPI variant) {
		return 5003;
	}

	@Override
	public boolean hasTooltip(FleetMemberAPI member, ShipVariantAPI variant, MarketAPI market) {
		return true;
	}

	@Override
	public void addTooltip(TooltipMakerAPI tooltip, FleetMemberAPI member, ShipVariantAPI variant, MarketAPI market) {
		tooltip.addPara("Panoptic Interface (Tactical)", 0f, Misc.getBasePlayerColor(), Misc.getBasePlayerColor());
		
		tooltip.addSpacer(5f);
		tooltip.addPara("Spend a Story Point to integrate a Panopticon Core tactical module into this ship. Grants bonuses based on the extent of the captain's training in the Sword of the Fleet skill.", 0f);
		
		tooltip.addSpacer(5f);
		tooltip.addPara("No skill: +15/10/5/5 speed, based on hull size", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
		tooltip.addPara("Rank 1: +10%% energy weapon damage", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
		tooltip.addPara("Rank 2: +200 su beam weapon base range, up to 900 su", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
		
		tooltip.addSpacer(5f);
		tooltip.addPara("Panoptic Interfaces will form an increasingly complex mesh network that strains standard crew operations, causing a %s for all interfaced ships in the fleet that scales based on each ship's size.", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "cumulative combat readiness reduction");
		
		if (!hasPanopticonCore()) {
			tooltip.addSpacer(10f);
			tooltip.addPara("No Panopticon Core available", 0f, Misc.getNegativeHighlightColor(), Misc.getNegativeHighlightColor());
		}
		else if (Global.getSector().getCharacterData().getPerson().getStats().getStoryPoints() < 1) {
			tooltip.addSpacer(10f);
			tooltip.addPara("Insufficient Story Points", 0f, Misc.getNegativeHighlightColor(), Misc.getNegativeHighlightColor());
		}
		else if (hasConflictMod(variant)) {
			tooltip.addSpacer(10f);
			tooltip.addPara("Incompatible with Armament Support System", 0f, Misc.getNegativeHighlightColor(), Misc.getNegativeHighlightColor());
		}
	}

	@Override
	public void onClick(FleetMemberAPI member, ShipVariantAPI variant, InputEventAPI event, MarketAPI market) {
		variant.addPermaMod(MOD_ID);
		refreshVariant();
		removePanopticonCore();
		Global.getSector().getCharacterData().getPerson().getStats().spendStoryPoints(1, false, null, false, "");
		refreshButtonList();
	}

	//Makes the button not clickable if mod cannot be fitted
	@Override
	public boolean isClickable(FleetMemberAPI member, ShipVariantAPI variant, MarketAPI market) {
		if (hasInterface(variant)) return false;
		if (hasConflictMod(variant)) return false;
		if (Global.getSector().getCharacterData().getPerson().getStats().getStoryPoints() < 1) return false;
		return (hasPanopticonCore());
	}

	@Override
	public boolean shouldShow(FleetMemberAPI member, ShipVariantAPI variant, MarketAPI market) {
		if (hasInterface(variant)) return false;
		if (variant.hasHullMod("automated")) return false;
		if (variant.getHullSpec().getSuppliesPerMonth() == 0 && variant.getHullSpec().getEngineSpec().getMaxSpeed() == 0) return false;
		return true;
	}
	
	private boolean hasInterface(ShipVariantAPI variant) {
		return (variant.hasHullMod(MOD_ID) 
					|| variant.hasHullMod(MOD_ID_1) 
					|| variant.hasHullMod(MOD_ID_2) 
					|| variant.hasHullMod(MOD_ID_3));
	}
	
	private boolean hasConflictMod(ShipVariantAPI variant) {
		return (variant.hasHullMod(CONFLICT_MOD_1) 
					|| variant.hasHullMod(CONFLICT_MOD_2) 
					|| variant.hasHullMod(CONFLICT_MOD_3));
	}
	
	private void removePanopticonCore() {
		CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
		if (cargo != null) cargo.removeCommodity(CORE_ID, 1f);
	}
	
	private boolean hasPanopticonCore() {
		CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
		if (cargo == null) return false;
		return cargo.getCommodityQuantity(CORE_ID) >= 1;
	}
}