package data.campaign.econ.industries;

import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import data.campaign.econ.HMI_items;

public class HMI_Mess_Scavange extends BaseIndustry {

	public void apply() {
		super.apply(true);

		int size = market.getSize();

		demand(Commodities.SUPPLIES, size- 1);
		demand(Commodities.FUEL, size- 1);
		demand(Commodities.CREW, size - 2);

		supply(HMI_items.MESS, size + 2);

		float mult = getDeficitMult(Commodities.SUPPLIES, Commodities.FUEL, Commodities.CREW);

		String extra = "";
		if (mult != 1) {
			String com = getMaxDeficit(Commodities.SUPPLIES, Commodities.FUEL, Commodities.CREW).one;
			extra = " (" + getDeficitText(com).toLowerCase() + ")";
		}


		float bonus = 2f;//DEFENSE_BONUS_BATTERIES;
		market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD)
				.modifyMult(getModId(), 1f + bonus * mult, getNameForModifier() + extra);


			if (!isFunctional()) {
			supply.clear();
			unapply();
			}
        }

	@Override
	public void unapply() {
		super.unapply();
		market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).unmodifyMult(getModId());
	}

	protected boolean hasPostDemandSection(boolean hasDemand, IndustryTooltipMode mode) {
		return mode != IndustryTooltipMode.NORMAL || isFunctional();
	}

	@Override
	protected void addPostDemandSection(TooltipMakerAPI tooltip, boolean hasDemand, IndustryTooltipMode mode) {
		if (mode != IndustryTooltipMode.NORMAL || isFunctional()) {
			addStabilityPostDemandSection(tooltip, hasDemand, mode);
		}
	}
	@Override
	public boolean isAvailableToBuild() {
		return false;
	}

	@Override
	public boolean showWhenUnavailable() {
		return false;
	}
}

