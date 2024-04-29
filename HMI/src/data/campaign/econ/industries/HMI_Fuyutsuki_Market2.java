package data.campaign.econ.industries;

import java.awt.*;
import java.util.Random;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.campaign.FactionAPI.ShipPickMode;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.DebugFlags;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.econ.impl.MilitaryBase.PatrolFleetData;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.fleets.PatrolAssignmentAIV4;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactory.PatrolType;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.OptionalFleetData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteFleetSpawner;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteSegment;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
import com.fs.starfarer.api.util.WeightedRandomPicker;



public class HMI_Fuyutsuki_Market2 extends BaseIndustry implements RouteFleetSpawner, FleetEventListener {

	public static float DEFENSE_BONUS_BATTERIES = 2f;
	public static float OFFICER_PROB_MOD_HMI = 0.3f;
	protected transient SubmarketAPI saved = null;

	@Override
	public boolean isHidden() {
		return !market.getFactionId().equals("HMI");
	}
	
	@Override
	public boolean isFunctional() {
		return super.isFunctional() && market.getFactionId().equals("HMI");
	}

	public void apply() {
		super.apply(true);

		if (isFunctional()) {
			SubmarketAPI fuyutsuki = market.getSubmarket("hmi_exec_market");
			if (fuyutsuki == null) {
				if (saved != null) {
					market.addSubmarket(saved);
				} else {
					market.addSubmarket("hmi_exec_market");
					Global.getSector().getEconomy().forceStockpileUpdate(market);
				}
			}
		} else {
			market.removeSubmarket("hmi_exec_market");
		}

		int size = market.getSize();

		demand(Commodities.HEAVY_MACHINERY, size + 2);
		demand(Commodities.HAND_WEAPONS, size + 2);
		demand(Commodities.MARINES, size + 2);
		demand(Commodities.SUPPLIES, size + 1);
		demand(Commodities.FUEL, size + 1);
		demand(Commodities.CREW, size + 1);
		demand(Commodities.SHIPS, size );

		supply(Commodities.VOLATILES, size - 3);
		supply(Commodities.ORE, size + 2);
		supply(Commodities.RARE_ORE, size);
		supply(Commodities.ORGANICS, size - 1);

		MemoryAPI memory = market.getMemoryWithoutUpdate();
		Misc.setFlagWithReason(memory, MemFlags.MARKET_PATROL, getModId(), true, -1);
		Misc.setFlagWithReason(memory, MemFlags.MARKET_MILITARY, getModId(), true, -1);
		
		float mult = getDeficitMult(Commodities.SUPPLIES, Commodities.MARINES, Commodities.HAND_WEAPONS);
		String extra = "";
		if (mult != 1) {
			String com = getMaxDeficit(Commodities.SUPPLIES, Commodities.MARINES, Commodities.HAND_WEAPONS).one;
			extra = " (" + getDeficitText(com).toLowerCase() + ")";
		}
		
		float bonus = DEFENSE_BONUS_BATTERIES;
		market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD)
						.modifyMult(getModId(), 1f + bonus * mult, getNameForModifier() + extra);

		modifyStabilityWithBaseMod();

		float officerProb = OFFICER_PROB_MOD_HMI;
		market.getStats().getDynamic().getMod(Stats.OFFICER_PROB_MOD).modifyFlat(getModId(0), officerProb);



		if (!isFunctional()) {
			supply.clear();
			unapply();
		}

	}

	@Override
	public void unapply() {
		super.unapply();

		SubmarketAPI fuyutsuki = market.getSubmarket("hmi_exec_market");
		saved = fuyutsuki;
		market.removeSubmarket("hmi_exec_market");


		market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).unmodifyMult(getModId());

		market.getStats().getDynamic().getMod(Stats.OFFICER_PROB_MOD).unmodifyFlat(getModId(0));

		unmodifyStabilityWithBaseMod();

		MemoryAPI memory = market.getMemoryWithoutUpdate();
		Misc.setFlagWithReason(memory, MemFlags.MARKET_PATROL, getModId(), false, -1);
		Misc.setFlagWithReason(memory, MemFlags.MARKET_MILITARY, getModId(), false, -1);
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
		return 2;
	}

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

	protected IntervalUtil tracker = new IntervalUtil(Global.getSettings().getFloat("averagePatrolSpawnInterval") * 0.7f,
													  Global.getSettings().getFloat("averagePatrolSpawnInterval") * 1.3f);

	protected Object readResolve() {
		if (tracker == null) tracker = new IntervalUtil(Global.getSettings().getFloat("averagePatrolSpawnInterval") * 0.7f,
				Global.getSettings().getFloat("averagePatrolSpawnInterval") * 1.3f);
		return this;
	}

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

		if (Global.getSector().isInNewGameAdvance()) {
			spawnRate *= 3f;
		}

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

		//DebugFlags.FAST_PATROL_SPAWN = true;
		if (DebugFlags.FAST_PATROL_SPAWN) {
			tracker.advance(days * spawnRate * 100f);
		}


		if (tracker.intervalElapsed()) {
			String sid = getRouteSourceId();

			int light = getCount(PatrolType.FAST);
			int medium = getCount(PatrolType.COMBAT);
			int heavy = getCount(PatrolType.HEAVY);

			int maxLight = 2;
			int maxMedium = 1;
			int maxHeavy = 1;

			WeightedRandomPicker<PatrolType> picker = new WeightedRandomPicker<PatrolType>();
			picker.add(PatrolType.HEAVY, maxHeavy - heavy);
			picker.add(PatrolType.COMBAT, maxMedium - medium);
			picker.add(PatrolType.FAST, maxLight - light);

			if (picker.isEmpty()) return;

			PatrolType type = picker.pick();
			PatrolFleetData custom = new PatrolFleetData(type);

			OptionalFleetData extra = new OptionalFleetData(market);
			extra.fleetType = type.getFleetType();

			RouteData route = RouteManager.getInstance().addRoute(sid, market, Misc.genRandomSeed(), extra, this, custom);
			float patrolDays = 35f + (float) Math.random() * 10f;

			route.addSegment(new RouteSegment(patrolDays, market.getPrimaryEntity()));
		}
	}
	@Override
	public void reportAboutToBeDespawnedByRouteManager(RouteData route) {
	}
	@Override
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
	@Override
	public boolean shouldCancelRouteAfterDelayCheck(RouteData route) {
		return false;
	}
	@Override
	public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {

	}
	@Override
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
	@Override
	public CampaignFleetAPI spawnFleet(RouteData route) {

		PatrolFleetData custom = (PatrolFleetData) route.getCustom();
		PatrolType type = custom.type;

		Random random = route.getRandom();

		float combat = 0f;
		float tanker = 0f;
		float freighter = 0f;
		String fleetType = type.getFleetType();
		switch (type) {
			case FAST:
				combat = Math.round(3f + (float) random.nextFloat() * 2f) * 5f;
				break;
			case COMBAT:
				combat = Math.round(6f + (float) random.nextFloat() * 4f) * 5f;
				tanker = Math.round((float) random.nextFloat()) * 2f;
				break;
			case HEAVY:
				combat = Math.round(10f + (float) random.nextFloat() * 6f) * 5f;
				tanker = Math.round((float) random.nextFloat()) * 4f;
				freighter = Math.round((float) random.nextFloat()) * 4f;
				break;
		}

		FleetParamsV3 params = new FleetParamsV3(
				market,
				null, // loc in hyper; don't need if have market
				"hmi_exec",
				route.getQualityOverride(), // quality override
				fleetType,
				combat, // combatPts
				freighter, // freighterPts
				tanker, // tankerPts
				0f, // transportPts
				0f, // linerPts
				0f, // utilityPts
				0f // qualityMod
		);
		params.timestamp = route.getTimestamp();
		//params.random = random;
		params.random = new Random(); //for easier testing
		params.qualityOverride = 150f;
		params.modeOverride = ShipPickMode.PRIORITY_THEN_ALL;
		params.officerLevelBonus = 10;
		params.officerNumberMult = 1.5f;
		CampaignFleetAPI fleet = FleetFactoryV3.createFleet(params);

		if (fleet == null || fleet.isEmpty()) return null;


		fleet.setFaction(market.getFactionId(), true);
		fleet.setNoFactionInName(false);

		fleet.addEventListener(this);

//		PatrolAssignmentAIV2 ai = new PatrolAssignmentAIV2(fleet, custom);
//		fleet.addScript(ai);

		fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_PATROL_FLEET, true);
		if (type == PatrolType.FAST || type == PatrolType.COMBAT) {
			fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_CUSTOMS_INSPECTOR, true);
		}

		String postId = Ranks.POST_PATROL_COMMANDER;
		String rankId = Ranks.SPACE_COMMANDER;
		switch (type) {
			case FAST:
				rankId = Ranks.SPACE_LIEUTENANT;
				break;
			case COMBAT:
				rankId = Ranks.SPACE_COMMANDER;
				break;
			case HEAVY:
				rankId = Ranks.SPACE_CAPTAIN;
				break;
		}

		fleet.getCommander().setPostId(postId);
		fleet.getCommander().setRankId(rankId);

		market.getContainingLocation().addEntity(fleet);
		fleet.setFacing((float) Math.random() * 360f);
		// this will get overridden by the patrol assignment AI, depending on route-time elapsed etc
		fleet.setLocation(market.getPrimaryEntity().getLocation().x, market.getPrimaryEntity().getLocation().x);

		fleet.addScript(new PatrolAssignmentAIV4(fleet, route));

		fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_IGNORES_OTHER_FLEETS, true, 0.1f);

		//market.getContainingLocation().addEntity(fleet);
		//fleet.setLocation(market.getPrimaryEntity().getLocation().x, market.getPrimaryEntity().getLocation().y);

		if (custom.spawnFP <= 0) {
			custom.spawnFP = fleet.getFleetPoints();
		}

		return fleet;
	}

	public String getRouteSourceId() {
		return getMarket().getId() + "_" + "blackrock_consortium";
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