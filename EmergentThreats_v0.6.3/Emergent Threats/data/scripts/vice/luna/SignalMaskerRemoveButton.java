package data.scripts.vice.luna;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import lunalib.lunaRefit.BaseRefitButton;

public class SignalMaskerRemoveButton extends BaseRefitButton {

	private static String ITEM_ID = "vice_signal_masker_device";	
	private static String HULLMOD_ID = "vice_signal_masker";
	private static String SHIP_TAG = "ship_unique_signature";
	
	@Override
	public String getButtonName(FleetMemberAPI member, ShipVariantAPI variant) {
		return "Remove Signal Masker";
	}

	@Override
	public String getIconName(FleetMemberAPI member, ShipVariantAPI variant) {
		return "graphics/vice/icons/signal_masker.png";
	}

	@Override
	public int getOrder(FleetMemberAPI member, ShipVariantAPI variant) {
		return 4002;
	}

	@Override
	public boolean hasTooltip(FleetMemberAPI member, ShipVariantAPI variant, MarketAPI market) {
		return true;
	}

	@Override
	public void addTooltip(TooltipMakerAPI tooltip, FleetMemberAPI member, ShipVariantAPI variant, MarketAPI market) {
		tooltip.addPara("Remove Signal Masker", 0f, Misc.getBasePlayerColor(), Misc.getBasePlayerColor());
		tooltip.addSpacer(5f);

		tooltip.addPara("Extract the Signal Masker from this ship, leaving it identifiable even when transponders are off.", 0f);
	}

	@Override
	public void onClick(FleetMemberAPI member, ShipVariantAPI variant, InputEventAPI event, MarketAPI market) {
		variant.getPermaMods().remove(HULLMOD_ID);
		variant.getHullMods().remove(HULLMOD_ID);
		variant.addTag(SHIP_TAG);
		addMaskerToInventory();
		refreshVariant();
		refreshButtonList();
	}

	//Makes the button not clickable if mod cannot be fitted
	@Override
	public boolean isClickable(FleetMemberAPI member, ShipVariantAPI variant, MarketAPI market) {
		return variant.hasHullMod(HULLMOD_ID);
	}

	@Override
	public boolean shouldShow(FleetMemberAPI member, ShipVariantAPI variant, MarketAPI market) {
		return variant.hasHullMod(HULLMOD_ID);
	}
	
	private void addMaskerToInventory() {
		boolean hasAdded = false;
		CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
		for (CargoStackAPI s : cargo.getStacksCopy()) {
			if (s.isSpecialStack() && s.getSpecialItemSpecIfSpecial().getId().equals(ITEM_ID)) {
				if (!hasAdded) {
					s.add(1f);
					hasAdded = true;
				}
			}
		}
	}
}