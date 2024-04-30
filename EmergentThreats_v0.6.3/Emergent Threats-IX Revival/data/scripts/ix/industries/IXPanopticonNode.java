package data.scripts.ix.industries;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.Industry.IndustryTooltipMode;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD.RaidDangerLevel;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class IXPanopticonNode extends BaseIndustry {

	private static float DEFAULT_PATHER_INTEREST = 4f;
	private static float DEFENSE_BONUS_NODE = 0.5f;
	private static int STABILITY_BONUS = 5; //display only
	private static String IX_FACTION = "ix_battlegroup";
	private static String CORE = "ix_panopticon";
	private static String NODE = "ix_panopticon_node";
	private static String FLEET_COMMAND = Industries.HIGHCOMMAND;
	
	public void apply() {
		super.apply(false);
		market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD)
						.modifyMult(getModId(), 1f + DEFENSE_BONUS_NODE, getNameForModifier());
		market.suppressCondition(Conditions.PIRATE_ACTIVITY);
		if (!isFunctional()) {
			unapply();
		}
	}

	@Override
	public boolean isHidden() {
		return !market.getFactionId().equals(IX_FACTION);
	}
	
	@Override
	public boolean isFunctional() {
		return panopticonIsActiveCheck();
	}
	
	@Override
	public void unapply() {
		super.unapply();
		market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).unmodifyMult(getModId());
		market.unsuppressCondition(Conditions.PIRATE_ACTIVITY);
	}

	@Override
	public float getPatherInterest() {
		float interest = DEFAULT_PATHER_INTEREST;
		if (panopticonIsActiveCheck()) {
			for (Industry industry : market.getIndustries()) {
				if (industry.getId().equals(NODE)) interest -= (int) DEFAULT_PATHER_INTEREST;
				else interest -= (int) industry.getPatherInterest();
			}
		}
		return interest;
	}

	private boolean panopticonIsActiveCheck () {
		MarketAPI m = Global.getSector().getEntityById("ix_zorya_vertex").getMarket();
		boolean isMonitored = true;
		if (isHidden() || m == null || !m.hasIndustry(CORE)) isMonitored = false;
		else isMonitored = m.getIndustry(CORE).isFunctional();
		return isMonitored;
	}
	
	public boolean hasPostDemandSection(boolean hasDemand, IndustryTooltipMode mode) {
		return mode != IndustryTooltipMode.NORMAL || isFunctional();
	}
	
	@Override
	public void addPostDemandSection(TooltipMakerAPI tooltip, boolean hasDemand, IndustryTooltipMode mode) {
		if (mode != IndustryTooltipMode.NORMAL || isFunctional()) {
			String s = "Stability bonus: %s";
			tooltip.addPara(s, 10f, Misc.getHighlightColor(), "+" + STABILITY_BONUS);
			addGroundDefensesImpactSection(tooltip, DEFENSE_BONUS_NODE, (String[])null);
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
	
	@Override
	public boolean canImprove() {
		return false;
	}
	
	@Override
	public boolean canInstallAICores() {
		return false;
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
