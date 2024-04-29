package data.scripts.campaign.econ;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI.ShipPickMode;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.fleets.DefaultFleetInflaterParams;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.OptionalFleetData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteFleetSpawner;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteSegment;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD.RaidDangerLevel;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import data.scripts.campaign.II_IGFleetInflater;
import java.util.Random;

public class II_ImperialGuardHQ extends BaseIndustry implements RouteFleetSpawner, FleetEventListener {

    public static final float DEFENDER_SPAWN_WEIGHT_PER_DAY = 0.1f;
    public static final float DEFENDER_RESPAWN_TIMER_MINIMUM = 7f;

    protected float defenderSpawnWeight = 10f;

    @Override
    public boolean isHidden() {
        return !market.getFactionId().equals("interstellarimperium");
    }

    @Override
    public boolean isFunctional() {
        return super.isFunctional() && market.getFactionId().equals("interstellarimperium");
    }

    @Override
    public void apply() {
        super.apply(true);

        int size = market.getSize();

        demand(Commodities.SUPPLIES, size - 1);
        demand(Commodities.FUEL, size - 1);
        demand(Commodities.SHIPS, size - 1);

        supply(Commodities.CREW, size);

        demand(Commodities.HAND_WEAPONS, size);
        supply(Commodities.MARINES, size);

        Pair<String, Integer> deficit = getMaxDeficit(Commodities.HAND_WEAPONS);
        applyDeficitToProduction(1, deficit, Commodities.MARINES);

        modifyStabilityWithBaseMod();

        MemoryAPI memory = market.getMemoryWithoutUpdate();
        Misc.setFlagWithReason(memory, MemFlags.MARKET_PATROL, getModId(), true, -1);
        Misc.setFlagWithReason(memory, MemFlags.MARKET_MILITARY, getModId(), true, -1);

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

        unmodifyStabilityWithBaseMod();
    }

    @Override
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
    public String getNameForModifier() {
        if (getSpec().getName().contains("HQ")) {
            return getSpec().getName();
        }
        return Misc.ucFirst(getSpec().getName());
    }

    @Override
    protected Pair<String, Integer> getStabilityAffectingDeficit() {
        return getMaxDeficit(Commodities.SUPPLIES, Commodities.FUEL, Commodities.SHIPS, Commodities.HAND_WEAPONS);
    }

    @Override
    public String getCurrentImage() {
        return super.getCurrentImage();
    }

    @Override
    public boolean isDemandLegal(CommodityOnMarketAPI com) {
        return true;
    }

    @Override
    public boolean isSupplyLegal(CommodityOnMarketAPI com) {
        return true;
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

        if (Global.getSector().getEconomy().isSimMode()) {
            return;
        }

        if (!isFunctional()) {
            return;
        }

        float days = Global.getSector().getClock().convertToDays(amount);
        defenderSpawnWeight += days * DEFENDER_SPAWN_WEIGHT_PER_DAY;
        if (defenderSpawnWeight >= 10f) {
            defenderSpawnWeight = 10f;
        }

        float spawnRate = 1f;
        float rateMult = market.getStats().getDynamic().getStat(Stats.COMBAT_FLEET_SPAWN_RATE_MULT).getModifiedValue();
        spawnRate *= rateMult;

        float extraTime = 0f;
        if (returningPatrolValue > 0) {
            // apply "returned patrols" to spawn rate, at a maximum rate of 1 interval per day
            float interval = tracker.getIntervalDuration();
            extraTime = interval * days;
            returningPatrolValue -= days;
            if (returningPatrolValue < 0) {
                returningPatrolValue = 0;
            }
        }
        tracker.advance(days * spawnRate + extraTime);

        if (tracker.intervalElapsed()) {
            String sid = getRouteSourceId();

            int light = getCount(IGPatrolType.FAST);
            int medium = getCount(IGPatrolType.COMBAT);
            int heavy = getCount(IGPatrolType.HEAVY);
            int defender = getCount(IGPatrolType.DEFENDER);

            int maxLight = 2;
            int maxMedium = 2;
            int maxHeavy = 2;
            int maxDefender = 1;

            WeightedRandomPicker<IGPatrolType> picker = new WeightedRandomPicker<>();
            picker.add(IGPatrolType.HEAVY, maxHeavy - heavy);
            picker.add(IGPatrolType.COMBAT, maxMedium - medium);
            picker.add(IGPatrolType.FAST, maxLight - light);
            if (Math.random() < defenderSpawnWeight) {
                picker.add(IGPatrolType.DEFENDER, (maxDefender - defender) * defenderSpawnWeight);
            }

            if (picker.isEmpty()) {
                return;
            }

            IGPatrolType type = picker.pick();
            IGPatrolFleetData custom = new IGPatrolFleetData(type);

            OptionalFleetData extra = new OptionalFleetData(market);
            extra.fleetType = type.getFleetType();

            RouteData route = RouteManager.getInstance().addRoute(sid, market, Misc.genRandomSeed(), extra, this, custom);
            float patrolDays = 35f + (float) Math.random() * 10f;
            if (type == IGPatrolType.DEFENDER) {
                patrolDays *= 3f;
            }

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

    public int getCount(IGPatrolType... types) {
        int count = 0;
        for (RouteData data : RouteManager.getInstance().getRoutesForSource(getRouteSourceId())) {
            if (data.getCustom() instanceof IGPatrolFleetData) {
                IGPatrolFleetData custom = (IGPatrolFleetData) data.getCustom();
                for (IGPatrolType type : types) {
                    if (type == custom.type) {
                        count++;
                        break;
                    }
                }
            }
        }
        return count;
    }

    public int getMaxPatrols(IGPatrolType type) {
        if (type == IGPatrolType.FAST) {
            return (int) market.getStats().getDynamic().getMod(Stats.PATROL_NUM_LIGHT_MOD).computeEffective(0);
        }
        if (type == IGPatrolType.COMBAT) {
            return (int) market.getStats().getDynamic().getMod(Stats.PATROL_NUM_MEDIUM_MOD).computeEffective(0);
        }
        if (type == IGPatrolType.HEAVY) {
            return (int) market.getStats().getDynamic().getMod(Stats.PATROL_NUM_HEAVY_MOD).computeEffective(0);
        }
        if (type == IGPatrolType.DEFENDER) {
            return 1;
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
        if (!isFunctional()) {
            return;
        }

        if (reason == FleetDespawnReason.REACHED_DESTINATION) {
            RouteData route = RouteManager.getInstance().getRoute(getRouteSourceId(), fleet);
            if (route.getCustom() instanceof IGPatrolFleetData) {
                IGPatrolFleetData custom = (IGPatrolFleetData) route.getCustom();
                if (custom.spawnFP > 0) {
                    float fraction = fleet.getFleetPoints() / custom.spawnFP;
                    returningPatrolValue += fraction;
                }
            }
        }

        if ((reason == FleetDespawnReason.DESTROYED_BY_BATTLE) || (reason == FleetDespawnReason.NO_MEMBERS)) {
            RouteData route = RouteManager.getInstance().getRoute(getRouteSourceId(), fleet);
            if (route.getCustom() instanceof IGPatrolFleetData) {
                IGPatrolFleetData custom = (IGPatrolFleetData) route.getCustom();
                if (custom.type == IGPatrolType.DEFENDER) {
                    defenderSpawnWeight = -1f * DEFENDER_RESPAWN_TIMER_MINIMUM * DEFENDER_SPAWN_WEIGHT_PER_DAY;
                }
            }
        }
    }

    @Override
    public CampaignFleetAPI spawnFleet(RouteData route) {
        IGPatrolFleetData custom = (IGPatrolFleetData) route.getCustom();
        IGPatrolType type = custom.type;

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
                combat = Math.round(6f + (float) random.nextFloat() * 3f) * 5f;
                tanker = Math.round((float) random.nextFloat()) * 5f;
                break;
            case HEAVY:
                combat = Math.round(10f + (float) random.nextFloat() * 5f) * 5f;
                tanker = Math.round((float) random.nextFloat()) * 10f;
                freighter = Math.round((float) random.nextFloat()) * 10f;
                break;
            case DEFENDER:
                combat = 20 * 5f;
                break;
        }

        FleetParamsV3 params = new FleetParamsV3(
                market,
                null, // loc in hyper; don't need if have market
                "ii_imperial_guard",
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
        params.random = random;
        params.modeOverride = ShipPickMode.PRIORITY_THEN_ALL;
        CampaignFleetAPI fleet = FleetFactoryV3.createFleet(params);

        if (fleet == null || fleet.isEmpty()) {
            return null;
        }

        fleet.setFaction(market.getFactionId(), true);
        fleet.setNoFactionInName(true);

        fleet.addEventListener(this);

        if (type == IGPatrolType.DEFENDER) {
            fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_FIGHT_TO_THE_LAST, true);
            fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_PATROL_ALLOW_TOFF, true);
            fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_SAW_PLAYER_WITH_TRANSPONDER_ON, true);
        }

        fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_PATROL_FLEET, true);
        fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_IGNORES_OTHER_FLEETS, true, 0.3f);

        if (type == IGPatrolType.FAST || type == IGPatrolType.COMBAT) {
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
            case DEFENDER:
                rankId = Ranks.SPACE_ADMIRAL;
                break;
        }

        fleet.getCommander().setPostId(postId);
        fleet.getCommander().setRankId(rankId);

        market.getContainingLocation().addEntity(fleet);
        fleet.setFacing((float) Math.random() * 360f);
        // this will get overridden by the patrol assignment AI, depending on route-time elapsed etc
        fleet.setLocation(market.getPrimaryEntity().getLocation().x, market.getPrimaryEntity().getLocation().y);

        DefaultFleetInflaterParams p = new DefaultFleetInflaterParams();
        p.quality = 5f;
        p.mode = ShipPickMode.PRIORITY_THEN_ALL;
        p.factionId = "ii_imperial_guard";

        II_IGFleetInflater inflater = new II_IGFleetInflater(p);
        fleet.setInflater(inflater);

        if (type == IGPatrolType.DEFENDER) {
            fleet.setName("Imperial Defense Fleet");
            fleet.addScript(new II_DefenderAssignmentAI(fleet, route));
        } else {
            fleet.addScript(new II_IGPatrolAssignmentAI(fleet, route));
        }

        if (custom.spawnFP <= 0) {
            custom.spawnFP = fleet.getFleetPoints();
        }

        return fleet;
    }

    public String getRouteSourceId() {
        return getMarket().getId() + "_" + "ii_imperial_guard";
    }

    @Override
    public boolean isAvailableToBuild() {
        return false;
    }

    @Override
    public boolean showWhenUnavailable() {
        return false;
    }

    public static enum IGPatrolType {
        FAST(FleetTypes.PATROL_SMALL),
        COMBAT(FleetTypes.PATROL_MEDIUM),
        HEAVY(FleetTypes.PATROL_LARGE),
        DEFENDER(FleetTypes.PATROL_LARGE);

        private final String fleetType;

        private IGPatrolType(String fleetType) {
            this.fleetType = fleetType;
        }

        public String getFleetType() {
            return fleetType;
        }
    }

    public static class IGPatrolFleetData {

        public IGPatrolType type;
        public int spawnFP;

        public IGPatrolFleetData(IGPatrolType type) {
            this.type = type;
        }
    }

    @Override
    public boolean canImprove() {
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
