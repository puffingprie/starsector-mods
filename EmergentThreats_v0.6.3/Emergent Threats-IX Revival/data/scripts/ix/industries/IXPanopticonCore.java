package data.scripts.ix.industries;

import java.util.List;
import java.util.Random;
import lunalib.lunaSettings.LunaSettings;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.FactionAPI.ShipPickMode;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.Industry.IndustryTooltipMode;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.econ.impl.MilitaryBase.PatrolFleetData;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactory.PatrolType;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.fleets.PatrolAssignmentAIV4;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.OptionalFleetData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteFleetSpawner;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteSegment;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD.RaidDangerLevel;
import com.fs.starfarer.api.loading.VariantSource;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class IXPanopticonCore extends BaseIndustry implements RouteFleetSpawner, FleetEventListener {
	
	private static float DEFAULT_PATHER_INTEREST = 10f;
	private static float DEFENSE_BONUS_CORE = 1f;
	private static int STABILITY_BONUS = 5; //display only
	private static String IX_CORE = "ix_core";
	private static String IX_FACTION = "ix_battlegroup";
	private static String PANOPTICON = "ix_panopticon";
	private static String NODE = "ix_panopticon_node";
	private static String MONITORED = "ix_monitored";
	private static String CARTEL = "ix_cartel_activity";
	private static String FLEET_COMMAND = Industries.HIGHCOMMAND;
	private static String FLEET_COMMAND_STATION = "ix_zorya_vertex";
	private static String HONOR_GUARD_SUBMARKET = "IX_honor_guard_market";
	
	@Override
	public boolean isHidden() {
		return !market.getFactionId().equals(IX_FACTION);
	}
	
	@Override
	public boolean isFunctional() {
		return panopticonIsActiveCheck();
	}
	
	@Override
	public void apply() {
		super.apply(false);
		if (market.getFactionId().equals(IX_FACTION) && !market.hasSubmarket(HONOR_GUARD_SUBMARKET)) {
			market.addSubmarket(HONOR_GUARD_SUBMARKET);
		}
		if (isFunctional()) {
			applySurveillance();
			market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD)
							.modifyMult(getModId(), 1f + DEFENSE_BONUS_CORE, getNameForModifier());
			market.suppressCondition(Conditions.PIRATE_ACTIVITY);

		}
		else unapply();
	}

	@Override
	public void unapply() {
		super.unapply();
		if (!market.getFactionId().equals(IX_FACTION) && market.hasSubmarket(HONOR_GUARD_SUBMARKET)) {
			market.removeSubmarket(HONOR_GUARD_SUBMARKET);
		}
		unapplySurveillance();
		market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).unmodifyMult(getModId());
		market.unsuppressCondition(Conditions.PIRATE_ACTIVITY);
	}
	
	protected IntervalUtil tracker = new IntervalUtil(Global.getSettings().getFloat("averagePatrolSpawnInterval") * 0.7f,
													  Global.getSettings().getFloat("averagePatrolSpawnInterval") * 1.3f);
	
	protected float returningPatrolValue = 0f;
	
	@Override
	protected void buildingFinished() {
		super.buildingFinished();
		
		tracker.forceIntervalElapsed();
	}
	
	@Override
	protected void upgradeFinished(Industry previous) {
		super.upgradeFinished(previous);
		
		tracker.forceIntervalElapsed();
	}

	@Override
	public void advance(float amount) {
		super.advance(amount);
		
		if (Global.getSector().getEconomy().isSimMode()) return;

		if (!isFunctional()) return;
		
		float days = Global.getSector().getClock().convertToDays(amount);
		
		float spawnRate = 1f;
		float rateMult = market.getStats().getDynamic().getStat(Stats.COMBAT_FLEET_SPAWN_RATE_MULT).getModifiedValue();
		spawnRate *= rateMult;
		
		
		float extraTime = 0f;
		if (returningPatrolValue > 0) {
			// apply "returned patrols" to spawn rate, at a maximum rate of 1 interval per day
			float interval = tracker.getIntervalDuration();
			extraTime = interval * days;
			returningPatrolValue -= days;
			if (returningPatrolValue < 0) returningPatrolValue = 0;
		}
		tracker.advance(days * spawnRate + extraTime);
		
		//tracker.advance(days * spawnRate * 100f);
		
		if (tracker.intervalElapsed()) {
			String sid = getRouteSourceId();
			
			int light = getCount(PatrolType.FAST);
			int medium = getCount(PatrolType.COMBAT);
			int heavy = getCount(PatrolType.HEAVY);

			//overrides high command spawns
			int maxLight = 3;
			int maxMedium = 4;
			int maxHeavy = 2;
			
			if (isHidden()) {
				maxLight = 0;
				maxMedium = 0;
				maxHeavy = 0;
			}
			
			WeightedRandomPicker<PatrolType> picker = new WeightedRandomPicker<PatrolType>();
			picker.add(PatrolType.HEAVY, maxHeavy - heavy); 
			picker.add(PatrolType.COMBAT, maxMedium - medium); 
			picker.add(PatrolType.FAST, maxLight - light);
			
			if (picker.isEmpty()) return;
			
			PatrolType type = (PatrolType) picker.pick();
			PatrolFleetData custom = new PatrolFleetData(type);
			
			OptionalFleetData extra = new OptionalFleetData(market);
			extra.fleetType = type.getFleetType();
			
			RouteData route = RouteManager.getInstance().addRoute(sid, market, Misc.genRandomSeed(), extra, this, custom);
			float patrolDays = 35f + (float) Math.random() * 10f;
			
			route.addSegment(new RouteSegment(patrolDays, market.getPrimaryEntity()));
		}
	}
	
	public void reportAboutToBeDespawnedByRouteManager(RouteData route) {
	}
	
	public boolean shouldRepeat(RouteData route) {
		return false;
	}
	
	public int getCount(PatrolType ... types) {
		int count = 0;
		for (RouteData data : RouteManager.getInstance().getRoutesForSource(getRouteSourceId())) {
			if (data.getCustom() instanceof PatrolFleetData) {
				PatrolFleetData custom = (PatrolFleetData) data.getCustom();
				for (PatrolType type : types) {
					if (type == custom.type) {
						count++;
						break;
					}
				}
			}
		}
		return count;
	}

	public int getMaxPatrols(PatrolType type) {
		if (type == PatrolType.FAST) {
			return (int) market.getStats().getDynamic().getMod(Stats.PATROL_NUM_LIGHT_MOD).computeEffective(0);
		}
		if (type == PatrolType.COMBAT) {
			return (int) market.getStats().getDynamic().getMod(Stats.PATROL_NUM_MEDIUM_MOD).computeEffective(0);
		}
		if (type == PatrolType.HEAVY) {
			return (int) market.getStats().getDynamic().getMod(Stats.PATROL_NUM_HEAVY_MOD).computeEffective(0);
		}
		return 0;
	}
	
	public boolean shouldCancelRouteAfterDelayCheck(RouteData route) {
		return false;
	}

	public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {
		
	}

	public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, FleetDespawnReason reason, Object param) {
		if (!isFunctional()) return;
		
		if (reason == FleetDespawnReason.REACHED_DESTINATION) {
			RouteData route = RouteManager.getInstance().getRoute(getRouteSourceId(), fleet);
			if (route.getCustom() instanceof PatrolFleetData) {
				PatrolFleetData custom = (PatrolFleetData) route.getCustom();
				if (custom.spawnFP > 0) {
					float fraction  = fleet.getFleetPoints() / custom.spawnFP;
					returningPatrolValue += fraction;
				}
			}
		}
	}
	
	public CampaignFleetAPI spawnFleet(RouteData route) {
		
		PatrolFleetData custom = (PatrolFleetData) route.getCustom();
		PatrolType type = custom.type;
		
		Random random = route.getRandom();
		
		float combat = 0f;
		String fleetType = type.getFleetType();
		if (type == PatrolType.FAST) combat = Math.round(3f + (float) random.nextFloat() * 2f) * 5f;
		if (type == PatrolType.COMBAT) combat = Math.round(6f + (float) random.nextFloat() * 3f) * 5f;
		if (type == PatrolType.HEAVY) combat = Math.round(10f + (float) random.nextFloat() * 5f) * 5f;
		
		//Spawn Honor Guard for medium sized patrols only
		String faction = type == PatrolType.COMBAT ? IX_CORE : IX_FACTION;
		
		FleetParamsV3 params = new FleetParamsV3(
				market, 
				null, // loc in hyper; don't need if have market
				faction,
				route.getQualityOverride(), // quality override
				fleetType,
				combat, // combatPts
				0f, // freighterPts 
				0f, // tankerPts
				0f, // transportPts
				0f, // linerPts
				0f, // utilityPts
				0f // qualityMod
				);
		params.timestamp = route.getTimestamp();
		params.random = random;
		params.modeOverride = Misc.getShipPickMode(market);
		params.modeOverride = ShipPickMode.PRIORITY_THEN_ALL;
		CampaignFleetAPI fleet = FleetFactoryV3.createFleet(params);
		
		if (fleet == null || fleet.isEmpty()) return null;
		
		fleet.setFaction(market.getFactionId(), true);
		//fleet.setNoFactionInName(true);
		
		fleet.addEventListener(this);
		
//		PatrolAssignmentAIV2 ai = new PatrolAssignmentAIV2(fleet, custom);
//		fleet.addScript(ai);
		
		fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_PATROL_FLEET, true);
		fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_IGNORES_OTHER_FLEETS, true, 0.3f);
		
		/**
		if (type == PatrolType.FAST || type == PatrolType.COMBAT) {
			fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_CUSTOMS_INSPECTOR, true);
		}
		**/
		
		String postId = Ranks.POST_PATROL_COMMANDER;
		String rankId = Ranks.SPACE_COMMANDER;
		if (type == PatrolType.HEAVY) rankId = Ranks.SPACE_CAPTAIN;
		
		fleet.getCommander().setPostId(postId);
		fleet.getCommander().setRankId(rankId);
		
		for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
			member.setVariant(member.getVariant().clone(), false, false);
			member.getVariant().setSource(VariantSource.REFIT);
		}
		
		market.getContainingLocation().addEntity(fleet);
		fleet.setFacing((float) Math.random() * 360f);
		// this will get overridden by the patrol assignment AI, depending on route-time elapsed etc
		fleet.setLocation(market.getPrimaryEntity().getLocation().x, market.getPrimaryEntity().getLocation().y);
		
		fleet.addScript(new PatrolAssignmentAIV4(fleet, route));
		
		//market.getContainingLocation().addEntity(fleet);
		//fleet.setLocation(market.getPrimaryEntity().getLocation().x, market.getPrimaryEntity().getLocation().y);
		
		if (custom.spawnFP <= 0) {
			custom.spawnFP = fleet.getFleetPoints();
		}
		
		return fleet;
	}
	
	@Override
	public float getPatherInterest() {
		float interest = DEFAULT_PATHER_INTEREST;
		if (panopticonIsActiveCheck()) {
			for (Industry industry : market.getIndustries()) {
				if (industry.getId().equals(PANOPTICON)) interest -= (int) DEFAULT_PATHER_INTEREST;
				else interest -= (int) industry.getPatherInterest();
			}	
		}
		return interest;
	}
	
	private boolean panopticonIsActiveCheck() {
		MarketAPI m = Global.getSector().getEntityById(FLEET_COMMAND_STATION).getMarket();
		boolean isMonitored = true;
		if (isHidden()) isMonitored = false;
		else if (m == null || !m.hasIndustry(FLEET_COMMAND)) isMonitored = false;
		else if (m.getIndustry(FLEET_COMMAND).isDisrupted()) isMonitored = false;
		else if (!Commodities.ALPHA_CORE.equals(m.getIndustry(FLEET_COMMAND).getAICoreId())) isMonitored = false;
		return isMonitored;
	}
	
	private void applySurveillance() {
		List<MarketAPI> markets = Global.getSector().getEconomy().getMarketsCopy();
		boolean isMonitorEnabled = LunaSettings.getBoolean("EmergentThreats_IX_Revival", "ix_monitor_enabled");
		for (MarketAPI m : markets) {
			if (m.getFactionId().equals(IX_FACTION) && (!m.hasCondition(CARTEL))) {
				if (!m.hasIndustry(NODE) && !m.hasIndustry(PANOPTICON)) m.addIndustry(NODE);
				if (isMonitorEnabled) m.removeSubmarket(Submarkets.SUBMARKET_BLACK);
				else if (!m.hasSubmarket(Submarkets.SUBMARKET_BLACK)) m.addSubmarket(Submarkets.SUBMARKET_BLACK);
				m.addCondition(MONITORED);
			}
		}
	}
	
	private void unapplySurveillance() {
		List<MarketAPI> markets = Global.getSector().getEconomy().getMarketsCopy();
		for (MarketAPI m : markets) {
			if (m.getFactionId().equals("ix_battlegroup")) {
				if (!m.hasSubmarket(Submarkets.SUBMARKET_BLACK)) m.addSubmarket(Submarkets.SUBMARKET_BLACK);
				m.removeCondition(MONITORED);
			}
		}
	}
	
	public String getRouteSourceId() {
		return getMarket().getId() + "_" + "military";
	}
	
	public boolean hasPostDemandSection(boolean hasDemand, IndustryTooltipMode mode) {
		return mode != IndustryTooltipMode.NORMAL || isFunctional();
	}
	
	@Override
	public void addPostDemandSection(TooltipMakerAPI tooltip, boolean hasDemand, IndustryTooltipMode mode) {
		if (mode != IndustryTooltipMode.NORMAL || isFunctional()) {
			String s = "Stability bonus: %s";
			tooltip.addPara(s, 10f, Misc.getHighlightColor(), "+" + STABILITY_BONUS);
			addGroundDefensesImpactSection(tooltip, DEFENSE_BONUS_CORE, (String[])null);
		}
	}
	
	@Override
	public boolean isAvailableToBuild() {
		return false;
	}
	
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
