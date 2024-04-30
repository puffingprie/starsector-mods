package data.scripts.ix.industries;

import java.awt.Color;
import java.util.Random;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.DebugFlags;
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
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.ids.Strings;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD.RaidDangerLevel;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.DynamicStatsAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class IXMarzannaBase extends BaseIndustry implements RouteFleetSpawner, FleetEventListener {

	private static float OFFICER_PROB = 0.2f;
	private static float DEFENSE_BONUS = 0.2f;
	//private static int STABILITY_BONUS = 2; //done through condition
	private static String CARTEL_CONDITION = "ix_cartel_activity";
	private static String FACTION_ID = "ix_marzanna";
	
	@Override
	public boolean isHidden() {
		return (market.hasIndustry(Industries.MILITARYBASE) 
				|| market.hasIndustry(Industries.HIGHCOMMAND));
	}	
	
	@Override
	public boolean isFunctional() {
		return (!market.hasIndustry(Industries.MILITARYBASE) 
				&& !market.hasIndustry(Industries.HIGHCOMMAND));
	}
	
	public void apply() {
		super.apply(true);
		applyIncomeAndUpkeep(3);
		
		int size = market.getSize();
		int light = 1;
		int medium = 0;
		int heavy = 0;
		
		if (size <= 3) {
			light = 1;
			medium = 0;
			heavy = 0;
		}
		else if (size == 4) {
			light = 2;
			medium = 0;
			heavy = 0;
		}
		else if (size == 5) {
			light = 3;
			medium = 0;
			heavy = 0;
		}
		else if (size == 6) {
			light = 3;
			medium = 1;
			heavy = 0;
		}
		else if (size == 7) {
			light = 3;
			medium = 2;
			heavy = 0;
		}
		else if (size >= 8) {
			light = 3;
			medium = 3;
			heavy = 0;
		}
		
		DynamicStatsAPI dynamic = market.getStats().getDynamic();
		
		dynamic.getMod(Stats.PATROL_NUM_LIGHT_MOD).modifyFlat(getModId(), light);
		dynamic.getMod(Stats.PATROL_NUM_MEDIUM_MOD).modifyFlat(getModId(), medium);
		dynamic.getMod(Stats.PATROL_NUM_HEAVY_MOD).modifyFlat(getModId(), heavy);
		
		demand(Commodities.SUPPLIES, size);
		demand(Commodities.FUEL, size);
		demand(Commodities.SHIPS, size);
		
		supply(Commodities.CREW, size - 1);
		supply(Commodities.MARINES, size - 1);
		supply(Commodities.DRUGS, size - 3);
		supply(Commodities.ORGANS, size - 4);
		
		//modifyStabilityWithBaseMod();
		
		float mult = getDeficitMult(Commodities.SUPPLIES);
		String extra = "";
		if (mult != 1) {
			String com = (String) getMaxDeficit(Commodities.SUPPLIES).one;
			extra = " (" + getDeficitText(com).toLowerCase() + ")";
		}

		dynamic.getMod(Stats.GROUND_DEFENSES_MOD)
				.modifyMult(getModId(), 1f + DEFENSE_BONUS * mult, getNameForModifier() + extra);
		
		MemoryAPI memory = market.getMemoryWithoutUpdate();
		Misc.setFlagWithReason(memory, MemFlags.MARKET_PATROL, getModId(), true, -1);
		Misc.setFlagWithReason(memory, MemFlags.MARKET_MILITARY, getModId(), true, -1);
		
		dynamic.getMod(Stats.OFFICER_PROB_MOD).modifyFlat(getModId(0), OFFICER_PROB);		
		
		//market.addCondition(CARTEL_CONDITION);
		
		if (market.getSubmarket(Submarkets.SUBMARKET_BLACK) != null) {
			market.getSubmarket(Submarkets.SUBMARKET_BLACK).setFaction(Global.getSector().getFaction("ix_marzanna"));
		}
		
		if (!isFunctional()) {
			supply.clear();
			unapply();
		}
	}

	@Override
	public void unapply() {
		super.unapply();
		
		MemoryAPI memory = market.getMemoryWithoutUpdate();
		Misc.setFlagWithReason(memory, MemFlags.MARKET_PATROL, getModId(), false, -1);
		Misc.setFlagWithReason(memory, MemFlags.MARKET_MILITARY, getModId(), false, -1);
		
		//unmodifyStabilityWithBaseMod();
		
		DynamicStatsAPI dynamic = market.getStats().getDynamic();
		
		dynamic.getMod(Stats.PATROL_NUM_LIGHT_MOD).unmodifyFlat(getModId());
		dynamic.getMod(Stats.PATROL_NUM_MEDIUM_MOD).unmodifyFlat(getModId());
		dynamic.getMod(Stats.PATROL_NUM_HEAVY_MOD).unmodifyFlat(getModId());
		dynamic.getMod(Stats.GROUND_DEFENSES_MOD).unmodifyMult(getModId());
		dynamic.getMod(Stats.OFFICER_PROB_MOD).unmodifyFlat(getModId(0));
		
		//market.removeCondition(CARTEL_CONDITION);
		
		if (market.getSubmarket(Submarkets.SUBMARKET_BLACK) != null) {
			market.getSubmarket(Submarkets.SUBMARKET_BLACK).setFaction(Global.getSector().getFaction("pirates"));
		}
	}
	
	protected boolean hasPostDemandSection(boolean hasDemand, IndustryTooltipMode mode) {
		return mode != IndustryTooltipMode.NORMAL || isFunctional();
	}
	
	@Override
	protected void addPostDemandSection(TooltipMakerAPI tooltip, boolean hasDemand, IndustryTooltipMode mode) {
		if (mode != IndustryTooltipMode.NORMAL || isFunctional()) {
			//addStabilityPostDemandSection(tooltip, hasDemand, mode);
			addGroundDefensesImpactSection(tooltip, DEFENSE_BONUS, Commodities.SUPPLIES);
		}
	}
	
	/*
	@Override
	protected int getBaseStabilityMod() {
		return STABILITY_BONUS;
	}
	*/
	
	public String getNameForModifier() {
		if (getSpec().getName().contains("HQ")) return getSpec().getName();
		return Misc.ucFirst(getSpec().getName().toLowerCase());
	}
	
	/*
	@Override
	protected Pair<String, Integer> getStabilityAffectingDeficit() {
		return getMaxDeficit(Commodities.SUPPLIES, Commodities.FUEL, Commodities.SHIPS);
	}
	*/
	
	@Override
	public String getCurrentImage() {
		return super.getCurrentImage();
	}

	public boolean isDemandLegal(CommodityOnMarketAPI com) {
		return true;
	}

	public boolean isSupplyLegal(CommodityOnMarketAPI com) {
		return true;
	}
	
	@Override
	public boolean isAvailableToBuild() {
		return false;
	}
	
	@Override
	public String getUnavailableReason() {
		return "";
	}
	
	@Override
	public boolean showWhenUnavailable() {
		return false;
	}
	
	@Override
	public boolean canImprove() {
		return false;
	}
	
	private float patrolSpawnInterval = Global.getSettings().getFloat("averagePatrolSpawnInterval");
	protected IntervalUtil tracker = new IntervalUtil(patrolSpawnInterval * 0.7f, patrolSpawnInterval * 1.3f);
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
		
		if (Global.getSector().isInNewGameAdvance()) spawnRate *= 3f;
		
		float extraTime = 0f;
		if (returningPatrolValue > 0) {
			float interval = tracker.getIntervalDuration();
			extraTime = interval * days;
			returningPatrolValue -= days;
			if (returningPatrolValue < 0) returningPatrolValue = 0;
		}
		tracker.advance(days * spawnRate + extraTime);

		if (DebugFlags.FAST_PATROL_SPAWN) tracker.advance(days * spawnRate * 100f);
		
		if (tracker.intervalElapsed()) {
			String sid = getRouteSourceId();
			
			int light = getCount(PatrolType.FAST);
			int medium = getCount(PatrolType.COMBAT);
			int heavy = getCount(PatrolType.HEAVY);

			int maxLight = getMaxPatrols(PatrolType.FAST);
			int maxMedium = getMaxPatrols(PatrolType.COMBAT);
			int maxHeavy = getMaxPatrols(PatrolType.HEAVY);
			
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
			extra.strength = (float) getPatrolCombatFP(type, route.getRandom());
			extra.strength = Misc.getAdjustedStrength(extra.strength, market);
			
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
		DynamicStatsAPI dynamic = market.getStats().getDynamic();
		if (type == PatrolType.FAST) return (int) dynamic.getMod(Stats.PATROL_NUM_LIGHT_MOD).computeEffective(0);
		if (type == PatrolType.COMBAT) return (int) dynamic.getMod(Stats.PATROL_NUM_MEDIUM_MOD).computeEffective(0);
		if (type == PatrolType.HEAVY) return (int) dynamic.getMod(Stats.PATROL_NUM_HEAVY_MOD).computeEffective(0);
		return 0;
	}
	
	public String getRouteSourceId() {
		return getMarket().getId() + "_" + "military";
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
	
	public static int getPatrolCombatFP(PatrolType type, Random random) {
		float combat = 0;
		if (type == PatrolType.FAST) combat = Math.round(3f + (float) random.nextFloat() * 2f) * 5f;
		else if (type == PatrolType.COMBAT) combat = Math.round(6f + (float) random.nextFloat() * 3f) * 5f;
		else if (type == PatrolType.HEAVY) combat = Math.round(10f + (float) random.nextFloat() * 5f) * 5f;
		return (int) Math.round(combat);
	}
	
	public CampaignFleetAPI spawnFleet(RouteData route) {
		PatrolFleetData custom = (PatrolFleetData) route.getCustom();
		PatrolType type = custom.type;
		Random random = route.getRandom();
		CampaignFleetAPI fleet = createPatrol(type, FACTION_ID, route, market, null, random);
		if (fleet == null || fleet.isEmpty()) return null;
		fleet.addEventListener(this);
		
		market.getContainingLocation().addEntity(fleet);
		fleet.setFacing((float) Math.random() * 360f);
	
		fleet.setLocation(market.getPrimaryEntity().getLocation().x, market.getPrimaryEntity().getLocation().y);	
		fleet.addScript(new PatrolAssignmentAIV4(fleet, route));
		fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_IGNORES_OTHER_FLEETS, true, 0.3f);
		
		if (custom.spawnFP <= 0) custom.spawnFP = fleet.getFleetPoints();
		
		return fleet;
	}
	
	public static CampaignFleetAPI createPatrol(PatrolType type, String factionId, RouteData route, MarketAPI market, Vector2f locInHyper, Random random) {
		if (random == null) random = new Random();	
		float combat = getPatrolCombatFP(type, random);
		float tanker = 0f;
		float freighter = 0f;
		String fleetType = type.getFleetType();
		/*
		if (type == PatrolType.COMBAT) tanker = Math.round((float) random.nextFloat() * 5f);
		else if (type == PatrolType.HEAVY) {
			tanker = Math.round((float) random.nextFloat() * 10f);
			freighter = Math.round((float) random.nextFloat() * 10f);
		}
		*/
		FleetParamsV3 params = new FleetParamsV3(
				market, 
				locInHyper,
				factionId,
				route == null ? null : route.getQualityOverride(),
				fleetType,
				combat, // combatPts
				freighter, // freighterPts 
				tanker, // tankerPts
				0f, // transportPts
				0f, // linerPts
				0f, // utilityPts
				0f // qualityMod
				);
		if (route != null) params.timestamp = route.getTimestamp();
		params.random = random;
		CampaignFleetAPI fleet = FleetFactoryV3.createFleet(params);
		
		if (fleet == null || fleet.isEmpty()) return null;
		
		if (!fleet.getFaction().getCustomBoolean(Factions.CUSTOM_PATROLS_HAVE_NO_PATROL_MEMORY_KEY)) {
			fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_PATROL_FLEET, true);
			/*
			if (type == PatrolType.FAST || type == PatrolType.COMBAT) {
				fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_CUSTOMS_INSPECTOR, true);
			}
			*/
		} 
		else if (fleet.getFaction().getCustomBoolean(Factions.CUSTOM_PIRATE_BEHAVIOR)) {
			fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_PIRATE, true);
			if (market != null && market.isHidden()) fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_RAIDER, true);
		}
		
		String postId = Ranks.POST_PATROL_COMMANDER;
		String rankId = Ranks.SPACE_COMMANDER;
		if (type == PatrolType.FAST) rankId = Ranks.SPACE_LIEUTENANT;
		else if (type == PatrolType.COMBAT) rankId = Ranks.SPACE_COMMANDER;
		else if (type == PatrolType.HEAVY) rankId = Ranks.SPACE_CAPTAIN;
		
		fleet.getCommander().setPostId(postId);
		fleet.getCommander().setRankId(rankId);
		
		return fleet;
	}
	
	/*
	@Override
	public boolean canInstallAICores() {
		return false;
	}
	*/
	
	public static float ALPHA_CORE_BONUS = 0.25f;
	@Override
	protected void applyAlphaCoreModifiers() {
		market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).modifyMult(
				getModId(), 1f + ALPHA_CORE_BONUS, "Alpha core (" + getNameForModifier() + ")");
	}
	
	@Override
	protected void applyNoAICoreModifiers() {
		market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).unmodifyMult(getModId());
	}
	
	@Override
	protected void applyAlphaCoreSupplyAndDemandModifiers() {
		demandReduction.modifyFlat(getModId(0), DEMAND_REDUCTION, "Alpha core");
	}
	
	protected void addAlphaCoreDescription(TooltipMakerAPI tooltip, AICoreDescriptionMode mode) {
		float opad = 10f;
		Color highlight = Misc.getHighlightColor();
		
		String pre = "Alpha-level AI core currently assigned. ";
		if (mode == AICoreDescriptionMode.MANAGE_CORE_DIALOG_LIST || mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
			pre = "Alpha-level AI core. ";
		}
		
		float a = ALPHA_CORE_BONUS;
		String str = Strings.X + (1f + a);
		
		if (mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
			CommoditySpecAPI coreSpec = Global.getSettings().getCommoditySpec(aiCoreId);
			TooltipMakerAPI text = tooltip.beginImageWithText(coreSpec.getIconName(), 48);
			text.addPara(pre + "Reduces upkeep cost by %s. Reduces demand by %s unit. " +
					"Increases fleet size by %s.", 0f, highlight,
					"" + (int)((1f - UPKEEP_MULT) * 100f) + "%", "" + DEMAND_REDUCTION,
					str);
			tooltip.addImageWithText(opad);
			return;
		}
		
		tooltip.addPara(pre + "Reduces upkeep cost by %s. Reduces demand by %s unit. " +
				"Increases fleet size by %s.", opad, highlight,
				"" + (int)((1f - UPKEEP_MULT) * 100f) + "%", "" + DEMAND_REDUCTION,
				str);
	}
}