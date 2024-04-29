package data.campaign.econ.industries;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketImmigrationModifier;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.population.PopulationComposition;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.campaign.econ.HMI_items;


public class HMI_Mental_Influence extends BaseIndustry implements MarketImmigrationModifier {
	
	protected float ACCESSIBILITY_BOOST = 5f;
	public static final float STABILITY_BOOST = 8;
	
	
	public void apply() {
		super.apply(true);
		
		int size = market.getSize();
		int adjust = 11 - size;

		demand(Commodities.HEAVY_MACHINERY, size-2);
		demand(Commodities.SUPPLIES, size);
		demand(Commodities.DOMESTIC_GOODS, size);

		market.getAccessibilityMod().modifyFlat(id, (ACCESSIBILITY_BOOST * adjust)/100, "Mental Influence Systems");
		market.getStability().modifyFlat(id, STABILITY_BOOST - size, "Mental Influence Systems");
		market.addTransientImmigrationModifier(this);
	}

	@Override
	public void unapply() {
		super.unapply();
		market.getAccessibilityMod().unmodifyFlat(id);
		market.getStability().unmodifyFlat(id);
		market.removeTransientImmigrationModifier(this);
	}


	@Override
	public void modifyIncoming(MarketAPI market, PopulationComposition incoming) {
		incoming.add(Factions.INDEPENDENT, 10f);
		incoming.add(Factions.TRITACHYON, 5f);
		incoming.getWeight().modifyFlat(getModId(), getThisImmigrationBonus());
	}

	private float getThisImmigrationBonus() {
		return 10-2*market.getSize();
	}

	@Override
	protected void addPostDemandSection(TooltipMakerAPI tooltip, boolean hasDemand, IndustryTooltipMode mode) {
		int size = market.getSize();
		int adjust = 11 - size;
		if (mode != IndustryTooltipMode.NORMAL || isFunctional()) {

			tooltip.addPara(
					"%s stability.",
					10f,
					Misc.getHighlightColor(),
					"+" + (int) (STABILITY_BOOST - size)
			);

			tooltip.addPara(
					"%s accessibility.",
					10f,
					Misc.getHighlightColor(),
					"+" + (int) (ACCESSIBILITY_BOOST * adjust) + "%"
			);

			tooltip.addPara(
					"%s population growth (based on market size).",
					10f,
					Misc.getHighlightColor(),
					"+" + (int) getThisImmigrationBonus()
			);
		}
	}

	public boolean isAvailableToBuild() {
		if (!Global.getSector().getPlayerFaction().knowsIndustry(getId())) {
			return false;
		}
		return market.getPlanetEntity() != null;
	}

	public boolean showWhenUnavailable() {
		return Global.getSector().getPlayerFaction().knowsIndustry(getId());
	}

	protected boolean hasPostDemandSection(boolean hasDemand, IndustryTooltipMode mode) {
		return mode != IndustryTooltipMode.NORMAL || isFunctional();
	}

//	@Override
//	public void advance(float amount) {
//		super.advance(amount);

//		if (building
//				&& !isDisrupted()
//				&& market.hasSubmarket(Submarkets.LOCAL_RESOURCES)
//				&& market.getSubmarket(Submarkets.LOCAL_RESOURCES).getCargo().getCommodityQuantity(HMI_items.MESS) > 1) {

//			float commoditiesPerDay = 100f;
//			float maxQuant = (getBuildTime() - buildProgress) * commoditiesPerDay;
//			float availableQuant = market.getSubmarket(Submarkets.LOCAL_RESOURCES).getCargo().getCommodityQuantity(HMI_items.MESS);
//			float total = Math.min(availableQuant, maxQuant);
//			market.getSubmarket(Submarkets.LOCAL_RESOURCES).getCargo().removeCommodity(HMI_items.MESS, total);

//			buildProgress += Math.round(total/commoditiesPerDay);

//			if (buildProgress >= buildTime) {
//				finishBuildingOrUpgrading();
//			}
//		}
//	}
}

