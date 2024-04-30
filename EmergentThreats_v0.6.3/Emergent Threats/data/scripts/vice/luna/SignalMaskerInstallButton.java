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

public class SignalMaskerInstallButton extends BaseRefitButton {

	private static String ITEM_ID = "vice_signal_masker_device";	
	private static String HULLMOD_ID = "vice_signal_masker";
	private static String SHIP_TAG = "ship_unique_signature";

	@Override
	public String getButtonName(FleetMemberAPI member, ShipVariantAPI variant) {
		return "Install Signal Masker";
	}

	@Override
	public String getIconName(FleetMemberAPI member, ShipVariantAPI variant) {
		return "graphics/vice/icons/signal_masker.png";
	}

	@Override
	public int getOrder(FleetMemberAPI member, ShipVariantAPI variant) {
		return 4001;
	}

	@Override
	public boolean hasTooltip(FleetMemberAPI member, ShipVariantAPI variant, MarketAPI market) {
		return true;
	}

	@Override
	public void addTooltip(TooltipMakerAPI tooltip, FleetMemberAPI member, ShipVariantAPI variant, MarketAPI market) {
		tooltip.addPara("Install Signal Masker", 0f, Misc.getBasePlayerColor(), Misc.getBasePlayerColor());
		
		tooltip.addSpacer(5f);
		tooltip.addPara("Install the Signal Masker onto this unique ship to prevent its drive signature from revealing the identity of the fleet when transponders are off.", 0f);
		
		if (!hasMaskerInInventory()) {
			tooltip.addSpacer(10f);
			tooltip.addPara("No Signal Masker available", 0f, Misc.getNegativeHighlightColor(), Misc.getNegativeHighlightColor());
		}
	}

	@Override
	public void onClick(FleetMemberAPI member, ShipVariantAPI variant, InputEventAPI event, MarketAPI market) {
		removeMaskerFromInventory();
		variant.addPermaMod(HULLMOD_ID);
		refreshVariant();
		refreshButtonList();
	}

	//Makes the button not clickable if mod cannot be fitted
	@Override
	public boolean isClickable(FleetMemberAPI member, ShipVariantAPI variant, MarketAPI market) {
		if (!hasMaskerInInventory()) return false;
		return variant.hasTag(SHIP_TAG);
	}

	@Override
	public boolean shouldShow(FleetMemberAPI member, ShipVariantAPI variant, MarketAPI market) {
		return variant.hasTag(SHIP_TAG);
	}
	
	private boolean hasMaskerInInventory() {
		boolean hasMasker = false;
		CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
		for (CargoStackAPI s : cargo.getStacksCopy()) {
			if (s.isSpecialStack() && s.getSpecialItemSpecIfSpecial().getId().equals(ITEM_ID)) {
				hasMasker = true;
			}
		}
		return hasMasker;
	}
	
	private void removeMaskerFromInventory() {
		boolean hasDeleted = false;
		CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
		for (CargoStackAPI s : cargo.getStacksCopy()) {
			if (s.isSpecialStack() && s.getSpecialItemSpecIfSpecial().getId().equals(ITEM_ID)) {
				if (!hasDeleted) {
					s.subtract(1f);
					hasDeleted = true;
				}
			}
		}
	}
}