package data.scripts.industry;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.ids.Strings;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD.RaidDangerLevel;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;


public class acs_industry_frontier_protocol extends BaseIndustry {

	public static float acs_FLEET_SIZE_BONUS_DEFAULT = 0.1f;
	public static float acs_FLEET_SIZE_BONUS_IMPROVE = 0.15f;
	public static float acs_FLEET_SIZE_BONUS_ALPHA = 0.25f;
	public static int acs_STABILITY_BONUS = 1;
	
	public void apply() {
		super.apply(true);

		int acs_size = market.getSize();
		
		//start acs
		market.setHidden(true);
		
		demand(Commodities.SUPPLIES, acs_size - 2);
		demand(Commodities.FUEL, acs_size - 2);
		demand(Commodities.SHIPS, acs_size - 2);
		
		supply(Commodities.CREW, acs_size - 1);

		if (!isFunctional()) {
			supply.clear();
			unapply();
		}
	}

	@Override
	public void unapply() {
		super.unapply();
		
		//start acs
		market.setHidden(false);
		
		unmodifyStabilityWithBaseMod();
        market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).unmodify(getModId());
		market.getStability().unmodify(getModId());
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
	protected int getBaseStabilityMod() {
		return acs_STABILITY_BONUS;
	}

	@Override
	protected Pair<String, Integer> getStabilityAffectingDeficit() {
		return getMaxDeficit(Commodities.SUPPLIES, Commodities.FUEL, Commodities.SHIPS);
	}

	
	public boolean isDemandLegal(CommodityOnMarketAPI com) {
		return true;
	}

	public boolean isSupplyLegal(CommodityOnMarketAPI com) {
		return true;
	}

	public boolean isAvailableToBuild() {
		return market.hasSpaceport();
	}
	
	public String getUnavailableReason() {
		return "Requires a functional spaceport";
	}

	@Override
	protected void applyAlphaCoreModifiers() {
		market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).modifyMult("acs_alpha_" + getModId(), 1f + acs_FLEET_SIZE_BONUS_ALPHA, "Alpha core (" + getNameForModifier() + ")");
	}
	
	@Override
	protected void applyNoAICoreModifiers() {
		market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).unmodifyMult("acs_alpha_" + getModId());
	}
	
	@Override
	protected void applyAlphaCoreSupplyAndDemandModifiers() {
		demandReduction.modifyFlat(getModId(0), DEMAND_REDUCTION, "Alpha core");
	}
	
	protected void addAlphaCoreDescription(TooltipMakerAPI tooltip, AICoreDescriptionMode mode) {
		float acs_opad = 1f;
		
		String acs_pre = "Alpha-level AI core currently assigned. ";
		if (mode == AICoreDescriptionMode.MANAGE_CORE_DIALOG_LIST || mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
			acs_pre = "Alpha-level AI core. ";
		}

		String acs_str = Strings.X + (1f + acs_FLEET_SIZE_BONUS_ALPHA);
		
		if (mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
			CommoditySpecAPI acs_coreSpec = Global.getSettings().getCommoditySpec(aiCoreId);
			TooltipMakerAPI acs_text = tooltip.beginImageWithText(acs_coreSpec.getIconName(), 48);
			acs_text.addPara(acs_pre + "Reduces upkeep cost by %s. Reduces demand by %s unit. " + "Increases fleet size by %s.",
					0f, Misc.getHighlightColor(), "" + 25 + "%", "" + 1 + "x", acs_str);
			tooltip.addImageWithText(acs_opad);
			return;
		}
		
		tooltip.addPara(acs_pre + "Reduces upkeep cost by %s. Reduces demand by %s unit. " + "Increases fleet size by %s.",
				acs_opad, Misc.getHighlightColor(), "" + 25 + "%", "" + 1 + "x", acs_str);
		
	}
	
	
	@Override
	public boolean canImprove() {
		return true;
	}
	
	protected void applyImproveModifiers() {
		if (isImproved()) {
			market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).modifyMult(getModId(), 1f + acs_FLEET_SIZE_BONUS_DEFAULT + acs_FLEET_SIZE_BONUS_IMPROVE, "Auxiliary ship-bays");
		} else {
			market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).modifyMult(getModId(), 1f + acs_FLEET_SIZE_BONUS_DEFAULT, "Auxiliary ship-bays");
		}
	}
	
	public void addImproveDesc(TooltipMakerAPI info, ImprovementDescriptionMode mode) {
		float acs_opad = 1f;
		
		if (mode == ImprovementDescriptionMode.INDUSTRY_TOOLTIP) {
			info.addPara("Increases the fleet size multiplier to %s.", 0f, Misc.getHighlightColor(), Strings.X + (1f + acs_FLEET_SIZE_BONUS_DEFAULT + acs_FLEET_SIZE_BONUS_IMPROVE));
		}
		else {
			info.addPara("Increases the fleet size multiplier to %s.", 0f, Misc.getHighlightColor(), Strings.X + (1f + acs_FLEET_SIZE_BONUS_DEFAULT + acs_FLEET_SIZE_BONUS_IMPROVE));
		}

		info.addSpacer(acs_opad);
		super.addImproveDesc(info, mode);
	}

	
	@Override
	public RaidDangerLevel adjustCommodityDangerLevel(String commodityId, RaidDangerLevel level) {
		return level.next();
	}

	@Override
	public RaidDangerLevel adjustItemDangerLevel(String itemId, String data, RaidDangerLevel level) {
		return level.next();
	}

	
}




