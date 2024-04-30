package data.scripts.ix.luna;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import lunalib.lunaRefit.BaseRefitButton;

public class BiochipSotFButton extends BaseRefitButton {

	private static String BIOCHIP_SKILL_ID = "ix_sword_of_the_fleet";
	private static String BIOCHIP_ID = "ix_biochip_sotf";
	
	@Override
	public String getButtonName(FleetMemberAPI member, ShipVariantAPI variant) {
		return "Biochip (Sword of the Fleet)";
	}

	@Override
	public String getIconName(FleetMemberAPI member, ShipVariantAPI variant) {
		return "graphics/icons/ix_biochip_sotf.png";
	}

	@Override
	public int getOrder(FleetMemberAPI member, ShipVariantAPI variant) {
		return 5000;
	}

	@Override
	public boolean hasTooltip(FleetMemberAPI member, ShipVariantAPI variant, MarketAPI market) {
		return true;
	}

	@Override
	public void addTooltip(TooltipMakerAPI tooltip, FleetMemberAPI member, ShipVariantAPI variant, MarketAPI market) {
		tooltip.addPara("Cerebral Biochip (Sword of the Fleet)", 0f, Misc.getBasePlayerColor(), Misc.getBasePlayerColor());
		tooltip.addSpacer(5f);

		tooltip.addPara("Implant a captain with a biochip, granting them the Sword of the Fleet skill. The commander will gain the elite rank of the skill, while officers will need additional training to reach elite rank.", 0f);

		if (!isValidCaptain(member.getCaptain())) {
			tooltip.addSpacer(10f);
			tooltip.addPara("Suitable implant recipient not present. Select a ship with a human captain who lacks the Sword of the Fleet skill.", 0f, Misc.getNegativeHighlightColor(), Misc.getNegativeHighlightColor());
		}
	}

	@Override
	public void onClick(FleetMemberAPI member, ShipVariantAPI variant, InputEventAPI event, MarketAPI market) {
		PersonAPI person = member.getCaptain();
		person.getStats().increaseSkill(BIOCHIP_SKILL_ID);
		if (person.equals(Global.getSector().getPlayerPerson())) person.getStats().increaseSkill(BIOCHIP_SKILL_ID);
		removeBiochip();
		refreshButtonList();
	}

	//Makes the button not clickable if mod cannot be fitted
	@Override
	public boolean isClickable(FleetMemberAPI member, ShipVariantAPI variant, MarketAPI market) {
		if (variant.hasHullMod("automated")) return false;
		if (!isValidCaptain(member.getCaptain())) return false;
		return hasBiochip();
	}

	@Override
	public boolean shouldShow(FleetMemberAPI member, ShipVariantAPI variant, MarketAPI market) {
		return hasBiochip();
	}
	
	private boolean isValidCaptain(PersonAPI person) {
		if (person == null || person.isAICore() || person.isDefault()) return false;
		return !person.getStats().hasSkill(BIOCHIP_SKILL_ID);
	}
	
	private boolean hasBiochip() {
		boolean hasChip = false;
		CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
		for (CargoStackAPI s : cargo.getStacksCopy()) {
			if (s.isSpecialStack() && s.getSpecialItemSpecIfSpecial().getId().equals(BIOCHIP_ID)) {
				hasChip = true;
			}
		}
		return hasChip;
	}
	
	private void removeBiochip() {
		boolean hasDeleted = false;
		CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
		for (CargoStackAPI s : cargo.getStacksCopy()) {
			if (s.isSpecialStack() && s.getSpecialItemSpecIfSpecial().getId().equals(BIOCHIP_ID)) {
				if (!hasDeleted) {
					s.subtract(1f);
					hasDeleted = true;
				}
			}
		}
	}
}