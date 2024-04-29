package mmm.missions;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.campaign.econ.*;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.StatBonus;
import com.fs.starfarer.api.impl.campaign.econ.ShippingDisruption;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.fleets.EconomyFleetAssignmentAI.CargoQuantityData;
import com.fs.starfarer.api.impl.campaign.fleets.EconomyFleetAssignmentAI.EconomyRouteData;
import com.fs.starfarer.api.impl.campaign.fleets.EconomyFleetRouteManager;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteSegment;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.intel.FactionHostilityIntel;
import com.fs.starfarer.api.impl.campaign.intel.contacts.ContactIntel;
import com.fs.starfarer.api.impl.campaign.missions.hub.MissionTrigger.TriggerAction;
import com.fs.starfarer.api.impl.campaign.missions.hub.MissionTrigger.TriggerActionContext;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity;
import com.fs.starfarer.api.impl.campaign.rulecmd.ShowRemainingCapacity;
import com.fs.starfarer.api.impl.campaign.submarkets.BaseSubmarketPlugin;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import mmm.Utils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicSettings;

import java.awt.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.List;

public class EscortMission extends OrbitalMissionBase implements FleetEventListener {
    private static final Logger log = Global.getLogger(EscortMission.class);
    static {
        if (Utils.DEBUG) {
            log.setLevel(Level.ALL);
        }
    }

    public enum Stage {
        ACCEPTED,
        DEPARTURE,  // ROUTE_TRAVEL_DST
        DST_WAIT,   // ROUTE_DST_LOAD/UNLOAD
        RETURN,     // ROUTE_TRAVEL_SRC
        SRC_WAIT,   // ROUTE_SRC_UNLOAD (pending cargo)
        COMPLETED,  // ROUTE_SRC_UNLOAD
        FAILED,
        FAILED_NO_PENALTY,  // If route is cancelled
        ROUTE_CANCELLED
    }

    public static final String MISSION_ID = "mmm_em";

    // Memory flags
    public static final String REF_KEY = "$mmm_em_ref";
    public static final String FAIL_FLAG = "$mmm_em_fail";
    public static final String HAS_COMMODITIES = "$mmm_em_has_commodities";
    public static final String NEEDS_COMMODITIES = "$mmm_em_needs_commodities";
    public static final String CHEAP_COM_TIMEOUT_PREFIX = MmmProcurementMission.CHEAP_COM_TIMEOUT_PREFIX;

    public static final float TRADE_IMPACT_DAYS = ShippingDisruption.ACCESS_LOSS_DURATION;

    // If you are not seen by the trade fleet for this many days, you fail the mission.
    public static final int MAX_DAYS_LAST_SEEN = MagicSettings.getInteger(MOD_ID, "EmMaxDaysLastSeen");
    // For each LY between source and destination market, how much extra credit you receive for the mission as a
    // ratio of the base payments, including effects of CREDITS_PER_PLAYER_FP.
    public static final float CREDIT_RATIO_PER_LY = MagicSettings.getFloat(MOD_ID, "EmCreditRatioPerLY");
    // Maximum pirate fleet strength as a ratio of the convoy strength.
    public static final float MAX_PIRATE_FP_RATIO = MagicSettings.getFloat(MOD_ID, "EmMaxPirateFpRatio");
    // Chance that each leg of the journey will spawn a pirate; only applicable if the systems are more than
    // PIRATE_SPAWN_LY apart.
    public static float PIRATE_SPAWN_CHANCE;
    // Spawns pirate fleets when the convoy is this close to the closest jump point in hyperspace in LY.
    // If the 2 markets are closer than this then no pirates will spawn.
    public static final float PIRATE_SPAWN_LY = MagicSettings.getFloat(MOD_ID, "EmPirateSpawnLY");
    // Extra price per player fleet strength on routes longer than PIRATE_SPAWN_LY apart, up to base credits.
    public static final int CREDITS_PER_PLAYER_FP = MagicSettings.getInteger(MOD_ID, "EmCreditsPerPlayerFp");
    // Min/max amount of your transport capacity that the mission can ask you to use to carry additional commodities.
    public static final float MIN_TRANSPORT_RATIO = MagicSettings.getFloat(MOD_ID, "EmMinTransportRatio");
    public static final float MAX_TRANSPORT_RATIO = MagicSettings.getFloat(MOD_ID, "EmMaxTransportRatio");
    // Reduces the effective strength of the convoy for pirate fleet size computation. The smaller this ratio, the
    // weaker the pirate.
    public static float CONVOY_STR_RATIO;

    public RouteData route = null;
    // Note that this is only set on failure.
    public String stage_description = null;
    public int fleet_size = 0;  // Non-fighter ship counts of the mercantile convoy.
    // We save the cargo/fuel/personnelCap here since EconomyFleetRouteManager.reportBattleOccurred zero's it out.
    public float cargoCap = 0f;
    public float fuelCap = 0f;
    public float personnelCap = 0f;
    DeliveryData delivery_data = null;
    DeliveryData return_data = null;
    public PersonAPI agent = null;
    public CampaignFleetAPI pirate_fleet = null;
    public PlayerLastSeenTooOld last_seen_checker = null;

    public static class DeliveryData {
        // Sorted by quantity (larger first).
        private List<Pair<String, Integer>> sorted_commodities;
        // Deprecated; do not use; this seems to cause serialization issues
        List<Map.Entry<String, Integer>> commodities = null;
        // Prices you're paid for the commodities, not including escrow
        Map<String, Integer> prices;
        public int escrow;
        // Commodities the player will be asked to carry.
        Map<String, Integer> stacks = null;

        public DeliveryData(Map<String, Integer> commodities, Map<String, Integer> prices, int escrow) {
            this.escrow = escrow;
            this.prices = prices;
            this.stacks = commodities;
//            this.commodities = new ArrayList<>(commodities.entrySet());
            this.sorted_commodities = new ArrayList<>();
            for (Map.Entry<String, Integer> entry : commodities.entrySet()) {
                sorted_commodities.add(new Pair<String, Integer>(entry.getKey(), entry.getValue()));
            }
            Collections.sort(this.sorted_commodities, new Comparator<Pair<String, Integer>>() {
                @Override
                public int compare(Pair<String, Integer> o1, Pair<String, Integer> o2) {
                    return o2.two - o1.two;
                }
            });
        }

        public List<Pair<String, Integer>> getSorted() {
            if (sorted_commodities == null && commodities != null) {
                sorted_commodities = new ArrayList<>();
                for (Map.Entry<String, Integer> entry : commodities) {
                    sorted_commodities.add(new Pair<String, Integer>(entry.getKey(), entry.getValue()));
                }
            }
            return sorted_commodities;
        }

        // Add commodities to player cargo.
        public void addToPlayer(TextPanelAPI text) {
            for (Pair<String, Integer> entry : getSorted()) {
                Global.getSector().getPlayerFleet().getCargo().addCommodity(entry.one, entry.two);
                if (text != null) {
                    AddRemoveCommodity.addCommodityGainText(entry.one, entry.two, text);
                }
            }
        }

        // Removes commodities from player cargo.
        public void removeFromPlayer(TextPanelAPI text) {
            for (Pair<String, Integer> entry : getSorted()) {
                Global.getSector().getPlayerFleet().getCargo().removeCommodity(entry.one, entry.two);
                if (text != null) {
                    AddRemoveCommodity.addCommodityLossText(entry.one, entry.two, text);
                }
            }
        }

        // Converts commodities to a list of strings.
        public List<String> toHighlights() {
            ArrayList<String> result = new ArrayList<>();
            for (Pair<String, Integer> entry : getSorted()) {
                result.add(entry.two.toString());
            }
            return result;
        }

        public int totalPrice() {
            int total_price = 0;
            for (int price : prices.values()) {
                total_price += price;
            }
            return total_price;
        }

        @Override
        public String toString() {
            ArrayList<String> result = new ArrayList<>();
            for (Pair<String, Integer> entry : getSorted()) {
                CommoditySpecAPI comm_spec = Global.getSettings().getCommoditySpec(entry.one);
                String pattern = entry.one.equals(Commodities.MARINES) ? "{0} {1}" : "{0} units of {1}";
                result.add(MessageFormat.format(pattern, entry.two.toString(), comm_spec.getLowerCaseName()));
            }
            return Misc.getAndJoined(result);
        }
    }

    // We cancel a route if it has been removed from the RouteManager, or if if the source market became hostile
    // with the destination market.
    public class RouteCancelledChecker implements ConditionChecker {
        @Override
        public boolean conditionsMet() {
            if (getCurrentStage() == Stage.SRC_WAIT) return false;

            // If we already have a failure reason, don't change the failure reason.
            CampaignFleetAPI fleet = route.getActiveFleet();
            if (fleet != null && fleet.getMemoryWithoutUpdate().getBoolean(FAIL_FLAG)) return false;

            // Check for conflict between the two factions
            EconomyRouteData econ_route_data = (EconomyRouteData) route.getCustom();
            if (areMarketsHostile(econ_route_data.from, econ_route_data.to)) {
                // Unless the stage is RETURN and the fleet is already in the same system as source market.

                if (getCurrentStage() != Stage.RETURN || fleet == null ||
                        fleet.getContainingLocation() != econ_route_data.from.getContainingLocation()) {
                    log.info(getFleetTypeName() + " route cancelled - faction hostility");
                    return true;
                }
            }

            // trade route removed from RouteManager
            for (RouteData curr : RouteManager.getInstance().getRoutesForSource(EconomyFleetRouteManager.SOURCE_ID)) {
                if (curr == route) return false;
            }
            log.info(getFleetTypeName() + " route cancelled.");
            return true;
        }
    }

    // Conditions on when the trade fleet has not seen the player for too long on a travel stage.
    public class PlayerLastSeenTooOld implements ConditionChecker {
        public long last_seen_ts = Global.getSector().getClock().getTimestamp();
//        public long last_print_ts = Long.MIN_VALUE;  // Not used

        // If current route is a travel segment, returns when the player was last seen by the fleet if the player fleet
        // is not visible. Otherwise, returns now and update last_seen_ts;
        public long getAndUpdateLastSeen(long now) {
            CampaignFleetAPI fleet = route.getActiveFleet();
            if (route.getCurrent() != null && route.getCurrent().isTravel() && fleet != null &&
                    fleet.getVisibilityLevelOfPlayerFleet() == SectorEntityToken.VisibilityLevel.NONE) {
                // Not seeing the player.
                return last_seen_ts;
            }
            last_seen_ts = now;
            return now;
        }

        @Override
        public boolean conditionsMet() {
            long now = Global.getSector().getClock().getTimestamp();
            if (now - getAndUpdateLastSeen(now) > MAX_DAYS_LAST_SEEN * MILLISECONDS_PER_DAY) {
                if (stage_description == null) {
                    stage_description = "Player fleet missing for too long";
                }
                return true;
            }
            return false;
        }
    }

    // Check that the current route segment is segment_id or greater, assumes that route segment ids are in increasing
    // order.
    public class RouteSegmentChecker implements ConditionChecker {
        public int segment_id;
        public RouteSegmentChecker(int segment_id) { this.segment_id = segment_id; }
        @Override
        public boolean conditionsMet() { return route.getCurrentSegmentId() >= segment_id; }
    }

    public class AdjustFleetAction implements TriggerAction {
        @Override
        public void doAction(TriggerActionContext context) {
            CampaignFleetAPI fleet = route.getActiveFleet();
            if (fleet == null) return;  // Sanity check
            log.debug("AdjustFleetAction.doAction called on " + fleet.getNameWithFaction());

            cargoCap = fleet.getCargo().getMaxCapacity();
            fuelCap = fleet.getCargo().getMaxFuel();
            personnelCap = fleet.getCargo().getMaxPersonnel();

            fleet.addEventListener(EscortMission.this);
            makeImportant(fleet, "$mmm_em_trade_fleet",
                    Stage.ACCEPTED, Stage.DEPARTURE, Stage.DST_WAIT, Stage.RETURN);
            setEntityMissionRef(fleet, REF_KEY);

            // Not sure if FLEET_IGNORES_OTHER_FLEETS actually does much
            for (String flag : Arrays.asList(MemFlags.MEMORY_KEY_SAW_PLAYER_WITH_TRANSPONDER_ON,
                    MemFlags.MEMORY_KEY_ALLOW_PLAYER_BATTLE_JOIN_TOFF, MemFlags.FLEET_IGNORES_OTHER_FLEETS)) {
                Misc.setFlagWithReason(fleet.getMemoryWithoutUpdate(), flag, getReason(), true, -1f);
            }
            if (route.getFactionId().equals(Factions.PIRATES)) {
                fleet.getMemoryWithoutUpdate().set("$mmm_em_underworld", true);
            }

            // If the trade fleet loses enough cargo to cause shipping loss, you fail.
            setStageOnMemoryFlag(Stage.FAILED, fleet, FAIL_FLAG);
            fleet_size = fleet.getFleetSizeCount();

            log.debug(MessageFormat.format("Mercantile convoy: fleet_str={0}, player_str={1}, fleet_size={2}",
                    getFleetStrength(fleet), getPlayerFleetStrength(), fleet_size));
        }
    }

    // Adjust open market's cargo and price based on the trade fleet's cargo, as if the cargo was sold
    // by player.
    public class ModifyMarketAction implements TriggerAction {
        @Override
        public void doAction(TriggerActionContext context) {
            EconomyRouteData econ_route_data = (EconomyRouteData) route.getCustom();
            if (route.getActiveFleet() == null) return;  // Sanity checks.

            // Cargo disappears during the load phase
            if (!route.getCurrentSegmentId().equals(EconomyFleetRouteManager.ROUTE_DST_UNLOAD) &&
                    !route.getCurrentSegmentId().equals(EconomyFleetRouteManager.ROUTE_SRC_UNLOAD)) {
                return;
            }

            MarketAPI market = route.getCurrentSegmentId().equals(EconomyFleetRouteManager.ROUTE_SRC_UNLOAD) ?
                    econ_route_data.from : econ_route_data.to;

            // Note that crew and marines don't get put into the trade fleet's cargo so there won't be trade mods.
            HashMap<String, Integer> stacks = new HashMap<>();
            for (CargoStackAPI stack : route.getActiveFleet().getCargo().getStacksCopy()) {
                stacks.put(stack.getCommodityId(), Math.round(stack.getSize()));

                // Delivery count as a proCom mission in terms of locking out cheapCom missions, so you can't get
                // cheapCom from contact immediately after doing an escort.
                market.getMemoryWithoutUpdate().set(CHEAP_COM_TIMEOUT_PREFIX + stack.getCommodityId(),true,
                        TRADE_IMPACT_DAYS);
            }
            applyTradeMods(market, false, true, true, stacks, null);
        }
    }

    // Checks that the pirate fleet is ready to be spawned and spawns it near the destination market.
    public class PirateFleetSpawner implements ConditionChecker, TriggerAction {
        public int player_fp = getPlayerFleetStrength();
        public ArrayList<Vector2f> spawn_locations = new ArrayList<>();
        public Vector2f closest_location = null;

        public PirateFleetSpawner(Stage stage) {
            EconomyRouteData econ_route_data = (EconomyRouteData) route.getCustom();
            MarketAPI market = stage == Stage.DEPARTURE ? econ_route_data.to : econ_route_data.from;
            List<?> jump_points = market.getPrimaryEntity().getContainingLocation().getEntities(JumpPointAPI.class);
            for (Object curr : jump_points) {
                if (curr instanceof JumpPointAPI) {
                    JumpPointAPI jp = (JumpPointAPI) curr;
                    if (!jp.isGasGiantAnchor() && !jp.isStarAnchor()) {
                        spawn_locations.add(Misc.getSystemJumpPointHyperExitLocation(jp));
                    }
                }
            }
            if (spawn_locations.isEmpty()) {
                spawn_locations.add(market.getLocationInHyperspace());
            }
        }

        @Override
        public boolean conditionsMet() {
            CampaignFleetAPI fleet = route.getActiveFleet();
            if (fleet == null || !fleet.isInHyperspace()) return false;

            Vector2f fleet_loc = fleet.getLocationInHyperspace();
            float min_dist_ly = Float.MAX_VALUE;
            for (Vector2f location : spawn_locations) {
                float distance_ly = Misc.getDistanceLY(location, fleet_loc);
                if (distance_ly < min_dist_ly) {
                    min_dist_ly = distance_ly;
                    closest_location = location;
                }
            }
            return min_dist_ly < PIRATE_SPAWN_LY;
        }

        @Override
        public void doAction(TriggerActionContext context) {
            if (route.getActiveFleet() == null || closest_location == null) return;  // Sanity check
            EconomyRouteData econ_route_data = (EconomyRouteData) route.getCustom();
            MarketAPI market = getCurrentStage() == Stage.DEPARTURE ? econ_route_data.to : econ_route_data.from;

            float convoy_fp = route.getActiveFleet().getEffectiveStrength() * CONVOY_STR_RATIO;
            int enemy_fp = Math.round(Math.min(convoy_fp * MAX_PIRATE_FP_RATIO, player_fp + convoy_fp));
            // Since MagicFleetBuilder crash with NPE if we try to spawn it in hyperspace, we spawn it at target system
            // first then move it to hyperspace.
            CreateFleetResult result = createFleet(market.getPrimaryEntity(), "Convoy Raider",
                    Factions.PIRATES, Factions.PIRATES, 3, enemy_fp, .77f);
            int enemy_str = result.fleet != null ? getFleetStrength(result.fleet) : 0;
            log.debug(MessageFormat.format(
                    "PirateFleetSpawner: success={0}, enemy_fp={1}, enemy_str={2}, convoy_fp={3}, player_fp={4}, spawn={5}",
                    result.fleet != null, enemy_fp, enemy_str, convoy_fp, player_fp, closest_location));

            if (result.fleet == null) return;

            pirate_fleet = result.fleet;
            pirate_fleet.getContainingLocation().removeEntity(pirate_fleet);
            Global.getSector().getHyperspace().addEntity(pirate_fleet);
            pirate_fleet.setLocation(closest_location.x, closest_location.y);
            pirate_fleet.clearAssignments();
            pirate_fleet.addAssignment(FleetAssignment.INTERCEPT, route.getActiveFleet(), 30f);
            // Not sure if these 2 flags does anything.
            setFlag(pirate_fleet, MemFlags.MEMORY_KEY_ALLOW_LONG_PURSUIT, false);
            setFlag(pirate_fleet, MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, false);
            // Make sure the pirate fleet can catch up to the convoy.
            modifyBurnSpeed(pirate_fleet, route.getActiveFleet(), 1);
        }
    }

    // Compute sustained burn speed without nav_buoy bonus.
    public static float computeSustainedBurn(CampaignFleetAPI fleet) {
        final List<String> ignore_sources = Arrays.asList(MISSION_ID, "nav_buoy", "emergency_burn_ability_mod");
        float burn = 2 * fleet.getFleetData().getMinBurnLevelUnmodified();
        ArrayList<String> strs = new ArrayList<>();
        for (MutableStat.StatMod mod : fleet.getStats().getFleetwideMaxBurnMod().getFlatBonuses().values()) {
            strs.add(MessageFormat.format("({0}, {1}, {2}})",
                    mod.getSource(), mod.getValue(), mod.getDesc()));
            if (!ignore_sources.contains(mod.getSource())) {
                burn += mod.getValue();
            }
        }
        log.debug(MessageFormat.format(
                "computeSustainedBurn: {0} fleet, sustained_burn={1}, base={2}, bonus={3}; {4}",
                fleet.getName(), burn, fleet.getFleetData().getMinBurnLevelUnmodified(),
                fleet.getStats().getFleetwideMaxBurnMod().getFlatBonus(), strs.toString()));
        return burn;
    }

    // Improve the burn speed of the provided fleet so that their sustained burn speed is at least the sustained burn
    // speed of the target_fleet modified by modifier.
    public static void modifyBurnSpeed(CampaignFleetAPI fleet, CampaignFleetAPI target_fleet, float modifier) {
        float target_burn = computeSustainedBurn(target_fleet);
        float fleet_burn = computeSustainedBurn(fleet);
        int bonus = Math.max(0, Math.round(target_burn + modifier - fleet_burn));
        if (bonus > 0) {
            StatBonus stats_bonus =  fleet.getStats().getFleetwideMaxBurnMod();
            float old_bonus = stats_bonus.getFlatBonus();
            stats_bonus.modifyFlat(MISSION_ID, bonus, MISSION_ID);
            float new_bonus = stats_bonus.getFlatBonus();
            log.info(MessageFormat.format(
                    "improveBurnSpeed: added {0} bonus burn to {1}; target_burn={2}, fleet_burn={3}, old={4}, new={5}",
                    bonus, fleet.getFullName(), target_burn, fleet_burn, old_bonus, new_bonus));
        }
    }

    // Returns whether the two markets are hostile with each other.
    public static boolean areMarketsHostile(MarketAPI market0, MarketAPI market1) {
        return market0.getFaction().getRelationshipLevel(market1.getFaction()).isAtBest(RepLevel.HOSTILE);
    }

    // Returns how many non-hidden factions that this faction is hostile to.
    public static int getHostileFactionsCount(FactionAPI faction) {
        int count = 0;
        for (FactionAPI curr : Global.getSector().getAllFactions()) {
            if (curr.isShowInIntelTab() && curr.isHostileTo(faction)) {
                ++count;
            }
        }
        return count;
    }

    // Returns the current person that needs a delivery, or null if there's no such person.
    PersonAPI getNeedsDeliveryPerson() {
        for (PersonAPI person : Arrays.asList(agent, getPerson())) {
            if (person != null && person.getMemoryWithoutUpdate().getBoolean(NEEDS_COMMODITIES)) {
                return person;
            }
        }
        return null;
    }

    // If the current stage have time limit set, returns the remaining time. Otherwise, for non-travel stages return the
    // time to the next travel stage, and for ROUTE_CANCELLED returns the expected time to the market that needs a
    // delivery.
    public int getTimeLimitDays() {
        if (timeLimit != null) {
            return Math.max(1, Math.round(timeLimit.days - elapsed));
        }
        int max_segment_id = EconomyFleetRouteManager.ROUTE_SRC_UNLOAD;
        if (getCurrentStage() == Stage.ROUTE_CANCELLED) {
            if (agent != null && agent == getNeedsDeliveryPerson()) {
                max_segment_id = EconomyFleetRouteManager.ROUTE_DST_LOAD;
            }
        }

        float days = route.getDelay();  // This is always 0 once the route starts.
        int current_id = route.getCurrentSegmentId();
        for (RouteSegment segment : route.getSegments()) {
            if (segment.getId() < current_id) continue;
            if (segment.getId() > max_segment_id) break;
            if (getCurrentStage() != Stage.ROUTE_CANCELLED && segment.isTravel()) break;
            days += segment.daysMax - segment.elapsed;
        }
        return Math.max(5, Math.round(days));
    }

    // We do this instead of calling makeImportant because it doesn't support multiple importance flags.
    void makePersonImportant(PersonAPI person, String flag) {
        // Sanity checks
        if (person == null || flag == null) return;
        MemoryAPI memory = person.getMemoryWithoutUpdate();
        if (memory.contains(flag)) return;

        if (!memory.contains(MemFlags.ENTITY_MISSION_IMPORTANT)) {
            memory.set(MemFlags.ENTITY_MISSION_IMPORTANT, true);
        }
        memory.set(flag, true);
        memory.addRequired(MemFlags.ENTITY_MISSION_IMPORTANT, flag);
    }

    // Do various adjustments at the start of each stage.
    @Override
    public void setCurrentStage(Object next, InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
        Object current_stage = getCurrentStage();
        super.setCurrentStage(next, dialog, memoryMap);
        if (current_stage == next) return;
        log.debug(MessageFormat.format("setCurrentStage from {0} to {1}", current_stage, next));

        // Sets time limit.
        if (next == Stage.SRC_WAIT) {
            setTimeLimit(Stage.COMPLETED, getTimeLimitDays() + elapsed, null);
        } else if (next == Stage.ROUTE_CANCELLED) {
            // Same logic as RouteManager.RouteSegment
            setTimeLimit(Stage.FAILED_NO_PENALTY, getTimeLimitDays() + elapsed, null);
//        } else if (next == Stage.DEPARTURE) {
//            setTimeLimit(Stage.FAILED, getTimeLimitDays() + elapsed, null, Stage.DST_WAIT);
//        } else if (next == Stage.RETURN) {
//            setTimeLimit(Stage.FAILED, getTimeLimitDays() + elapsed, null, Stage.SRC_WAIT);
        }

        // If we reached a travel stage, but we're not even in the same system, then we fail. We also fail if the
        // fleet hasn't spawned yet, since this can happen we're too far.
        if (next == Stage.DEPARTURE || next == Stage.RETURN) {
            CampaignFleetAPI fleet = route.getActiveFleet();
            if (fleet == null ||
                    fleet.getContainingLocation() != Global.getSector().getPlayerFleet().getContainingLocation()) {
                stage_description = "Player fleet late";
                setCurrentStage(Stage.FAILED, dialog, memoryMap);
                return;
            }
            setFlag(fleet, "$mmm_em_is_travel", false, getCurrentStage());
            // We need to adjust burn so that the trade fleet is not too slow.
            modifyBurnSpeed(fleet, Global.getSector().getPlayerFleet(), -1);
        }

        // Adjust importance and HAS_COMMODITIES/NEEDS_COMMODITIES flags. We do this instead of relying on
        // makeImportant, makeUnimportant because they don't support multiple flags for the same person.
        if (agent != null && next == Stage.RETURN) {
            // The agent no longer has/needs commodities after RETURN.
            MemoryAPI memory = agent.getMemoryWithoutUpdate();
            memory.unset(HAS_COMMODITIES);
            memory.unset(NEEDS_COMMODITIES);
        }

        if (next == Stage.ACCEPTED) {
            // Remove HAS_COMMODITIES in case delivery was not accepted.
            getPerson().getMemoryWithoutUpdate().unset(HAS_COMMODITIES);
        }

        // Once the route is cancelled, has the fleet return to source and de-spawn, and we no longer care about it.
        if (next == Stage.ROUTE_CANCELLED) {
            CampaignFleetAPI fleet = route.getActiveFleet();
            if (fleet != null) {
                fleet.removeEventListener(this);
                Misc.giveStandardReturnToSourceAssignments(fleet, true);
            }
        }

        for (PersonAPI person : Arrays.asList(agent, getPerson())) {
            if (person == null) continue;
            MemoryAPI memory = person.getMemoryWithoutUpdate();
            // Cancel any delivery not yet accepted.
            if (next == Stage.ROUTE_CANCELLED) {
                memory.unset(HAS_COMMODITIES);
            }

            String market_name = person.getMarket() != null ? person.getMarket().getName() : null;
            log.debug(MessageFormat.format("Person {0} on {1} {2}={3}, {4}={5}, is_important={6}, is_agent={7}",
                    person.getNameString(), market_name, NEEDS_COMMODITIES,
                    memory.getBoolean(NEEDS_COMMODITIES), HAS_COMMODITIES, memory.getBoolean(HAS_COMMODITIES),
                    memory.getBoolean(MemFlags.ENTITY_MISSION_IMPORTANT), person == agent));
        }
    }

    @Override
    public void abort() {
        super.abort();
        for (PersonAPI person : Arrays.asList(agent, getPerson())) {
            if (person == null) continue;
            MemoryAPI memory = person.getMemoryWithoutUpdate();
            memory.unset(HAS_COMMODITIES);
            memory.unset(NEEDS_COMMODITIES);
        }
    }

    // Returns all eligible trade route for any mission
    public static List<RouteData> findEligibleTradeRoutes() {
        ArrayList<RouteData> result = new ArrayList<>();

        HashSet<RouteData> existing = new HashSet<>();
        for (IntelInfoPlugin plugin : Global.getSector().getIntelManager().getIntel(EscortMission.class)) {
            EscortMission mission = (EscortMission) plugin;
            if (mission != null) existing.add(mission.route);
        }

        for (RouteData route : RouteManager.getInstance().getRoutesForSource(EconomyFleetRouteManager.SOURCE_ID)) {
            EconomyRouteData econ_route_data = (EconomyRouteData) route.getCustom();
            if (route.getCurrentSegmentId() > EconomyFleetRouteManager.ROUTE_SRC_LOAD ||
                    route.getFactionId() == null || econ_route_data == null || econ_route_data.from == null ||
                    econ_route_data.to == null || econ_route_data.smuggling || econ_route_data.size <= 3 ||
                    econ_route_data.from.getPrimaryEntity() == null || econ_route_data.to.getPrimaryEntity() == null) {
                continue;
            }

            // Must be neutral or better with the faction.
            if (Global.getSector().getFaction(route.getFactionId()).getRelToPlayer().isAtBest(RepLevel.SUSPICIOUS)) {
                continue;
            }

            // Don't allow multiple missions to trigger on the same trade route.
            if (existing.contains(route)) continue;

            // Ignore if the two markets are hostile
            if (areMarketsHostile(econ_route_data.from, econ_route_data.to)) continue;

            result.add(route);
        }
        return result;
    }

    // Returns an eligible trade route for this mission, or null if the mission cannot be created.
    public static RouteData findEligibleTradeRoute(MarketAPI market, String faction_id, Random random) {
        WeightedRandomPicker<RouteData> picker = new WeightedRandomPicker<>(random);
        for (RouteData route : findEligibleTradeRoutes()) {
            if (route.getMarket() != market) continue;
            if (!(route.getCustom() instanceof EconomyRouteData)) continue;
            // Ensure they have the right faction for contact missions.
            if (faction_id != null && !faction_id.equals(route.getFactionId())) continue;
            // Must be neutral or better with the faction.
            picker.add(route);
        }
        return picker.pick();
    }

    // Adapted from ProcurementMission.
    public void createBarGiver(MarketAPI createdAt, String faction_id) {
        List<String> posts;
        if (faction_id.equals(Factions.PIRATES)) {
            setGiverTags(Tags.CONTACT_UNDERWORLD);
            posts = Arrays.asList(Ranks.POST_SMUGGLER, Ranks.POST_GANGSTER, Ranks.POST_FENCE, Ranks.POST_CRIMINAL);
        } else {
            setGiverTags(Tags.CONTACT_TRADE);
            posts = Arrays.asList(Ranks.POST_TRADER, Ranks.POST_COMMODITIES_AGENT, Ranks.POST_MERCHANT,
                    Ranks.POST_INVESTOR, Ranks.POST_EXECUTIVE, Ranks.POST_SENIOR_EXECUTIVE, Ranks.POST_PORTMASTER,
                    Ranks.POST_ADMINISTRATOR);
        }

        // If we already have a contact with the right post and faction, just use that person.
        ArrayList<PersonAPI> candidates = new ArrayList<>();
        for (PersonAPI person : createdAt.getPeopleCopy()) {
            if (posts.contains(person.getPostId()) && person.getFaction() == createdAt.getFaction() &&
                    ContactIntel.playerHasContact(person, false)) {
                candidates.add(person);
            }
        }
        if (!candidates.isEmpty()) {
            PersonAPI person = Collections.max(candidates, new ComparePersonByRel(createdAt));
            makePersonRequired(person);
            setPersonOverride(person);
            return;
        }

        // If not call findOrCreateGiver to find or create the person.
        String post = pickOne(posts);
        PersonImportance importance;
        if (post.equals(Ranks.POST_SENIOR_EXECUTIVE) || post.equals(Ranks.POST_ADMINISTRATOR)) {
            importance = myPickVeryHighImportance(getGenRandom());
        } else if (post.equals(Ranks.POST_EXECUTIVE) || post.equals(Ranks.POST_PORTMASTER) || post.equals(Ranks.POST_SMUGGLER)) {
            importance = myPickHighImportance(getGenRandom());
        } else {
            importance = myPickImportance(getGenRandom());
        }
        setGiverImportance(importance);
        setGiverFaction(faction_id);
        setGiverRank(Ranks.CITIZEN);
        setGiverPost(post);
        findOrCreateGiver(createdAt, false, false);
    }

    @Override
    public boolean shouldShowAtMarket(MarketAPI market) {
        // Get new random so contact/bar missions are consistent.
        return findEligibleTradeRoute(market, null, getRandom(MISSION_ID, market)) != null;
    }

    @Override
    protected boolean create(MarketAPI createdAt, boolean barEvent) {
        String id = getMissionId();
//        log.debug((isBarEvent() ? "bar" : "contact") + " event; create called with mission_id=" + id);
        if (!id.equals(MISSION_ID)) {
            log.error("Unexpected mission id: " + id);
            return false;
        }

        if (barEvent) {
            route = findEligibleTradeRoute(createdAt, null, getGenRandom());
            if (route == null) return false;
            createBarGiver(createdAt, route.getFactionId());
//            setGiverIsPotentialContactOnSuccess();
        } else {
            PersonAPI person = getPerson();
            if (person == null || person.getFaction() == null) return false;  // Sanity check
            route = findEligibleTradeRoute(createdAt, person.getFaction().getId(), getGenRandom());
            if (route == null) {
//                log.debug("No eligible trade routes for person " + person.getNameString());
                return false;
            }
        }

        PersonAPI person = getPerson();
        if (person == null || person.getMarket() != createdAt || person.getFaction() == null) return false;  // sanity check

        if (!setPersonMissionRef(person, REF_KEY)) {
            log.info(REF_KEY + " already set for " + person.getNameString());
            return false;
        }

        // Compute what commodities we want to carry.
        delivery_data = computeDeliveries(false);
        return_data = computeDeliveries(true);

        EconomyRouteData econ_route_data = (EconomyRouteData) route.getCustom();
        if (delivery_data != null || return_data != null) {
            // Create trader on destination market if there's any delivery that the player can make.
            if (person.getFaction().getId().equals(Factions.PIRATES)) {
                agent = findOrCreateCriminalTrader(econ_route_data.to, true);
            } else {
                agent = findOrCreateTrader(person.getFaction().getId(), econ_route_data.to,
                        true);
            }
            if (agent.getFaction() == null) return false;  // Sanity check
            if (!setPersonMissionRef(agent, REF_KEY)) {
                log.info(REF_KEY + " already set for " + agent.getNameString());
                return false;
            }
            ensurePersonIsInCommDirectory(econ_route_data.to, agent);
            if (return_data != null) {
                ensurePersonIsInCommDirectory(createdAt, getPerson());
            }
        }

        // Set icon; same logic as TradeFleetDepartureIntel
        boolean large = econ_route_data.size >= 6;
        boolean valuable = false;
        for (CargoQuantityData curr : econ_route_data.cargoDeliver) {
            CommoditySpecAPI c = curr.getCommodity();
            if (c.getBasePrice() >= 100 && !c.isPersonnel()) {
                valuable = true;
            }
        }
        if (valuable) {
            setIconName("intel", "tradeFleet_valuable");
        } else if (large) {
            setIconName("intel", "tradeFleet_large");
        } else {
            setIconName("intel", "tradeFleet_other");
        }

        // Note that HAS_COMMODITIES is unset in acceptCargo, while NEEDS_COMMODITIES are set in acceptCargo and unset
        // in unloadCargo.
//        boolean is_valuable = false;
        if (delivery_data != null) {
            makePersonImportant(person, HAS_COMMODITIES);
        }
        if (return_data != null) {
            makePersonImportant(agent, HAS_COMMODITIES);
        }

        setStartingStage(Stage.ACCEPTED);
        setSuccessStage(Stage.COMPLETED);
        setFailureStage(Stage.FAILED);
        addNoPenaltyFailureStages(Stage.FAILED_NO_PENALTY);

        addTag(Tags.INTEL_TRADE);

        // Set rewards
        setRepPersonChangesMedium();
        setRepFactionChangesLow();

        genMissionRewardMultAndQuality();
        FactionAPI faction = createdAt.getFaction();
        if (faction.getCustom().optBoolean(Factions.CUSTOM_ENGAGES_IN_HOSTILITIES)) {
            setCreditReward(CreditReward.HIGH);
        } else if (getHostileFactionsCount(faction) >= 5) {  // pirates/path
            setCreditReward(CreditReward.VERY_HIGH);
        } else {  // independent
            setCreditReward(CreditReward.AVERAGE);
        }

        int base_credits = getCreditsReward();
        if (econ_route_data.to.getStarSystem() == createdAt.getStarSystem()) {
            // Reduce rewards if in the same system.
            setCreditReward(myGetRoundNumber(base_credits * 0.66f));
            return true;
        }

        // Increase reward based on distance in LY
        float distance_ly = Misc.getDistanceLY(createdAt.getLocationInHyperspace(),
                econ_route_data.to.getLocationInHyperspace());
        int fp_credits = 0;
        ArrayList<Stage> pirate_stages = new ArrayList<>();
        if (distance_ly >= PIRATE_SPAWN_LY) {
            // Add more credit if pirates can spawn.
            fp_credits = Math.min(getPlayerFleetStrength() * CREDITS_PER_PLAYER_FP, base_credits);

            // Spawning pirates is pointless if they're not hostile to the convoy.
            if (faction.isAtBest(Factions.PIRATES, RepLevel.HOSTILE)) {
                for (Stage stage : Arrays.asList(Stage.DEPARTURE, Stage.RETURN)) {
                    if (getGenRandom().nextFloat() >= PIRATE_SPAWN_CHANCE) continue;
                    pirate_stages.add(stage);
                    PirateFleetSpawner spawner = new PirateFleetSpawner(stage);
                    beginCustomTrigger(spawner, stage);
                    triggerCustomAction(spawner);
                    endTrigger();
                }
            }
        }
        log.debug("Will spawn pirate fleet in " + pirate_stages);

        int ly_credits = Math.round(distance_ly * CREDIT_RATIO_PER_LY * (base_credits + fp_credits));
        int credits = myGetRoundNumber(base_credits + ly_credits + fp_credits);
        setCreditReward(credits);
        log.info(MessageFormat.format(
                "setCreditReward: credits={0}, base_credits={1}, fp_credits={2}, ly_credits={3}",
                credits, base_credits, fp_credits, ly_credits));
        return true;
    }

    @Override
    public void acceptImpl(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
        super.acceptImpl(dialog, memoryMap);
//        EconomyRouteData econ_route_data = (EconomyRouteData) route.getCustom();

        // Adjust the fleet so it has the right property.
        ConditionChecker fleet_active_checker = new ConditionChecker() {
            @Override public boolean conditionsMet() {
                return route.getActiveFleet() != null;
            }};
        beginCustomTrigger(fleet_active_checker, Stage.ACCEPTED, Stage.DEPARTURE);
        triggerCustomAction(new AdjustFleetAction());
        endTrigger();

        // If the player gets too far away for too long, you fail the mission.
        last_seen_checker = new PlayerLastSeenTooOld();
        setStageOnCustomCondition(Stage.FAILED, last_seen_checker);

        // If the route is cancelled, cancel the mission with no penalty.
        setStageOnCustomCondition(Stage.ROUTE_CANCELLED, new RouteCancelledChecker());
        // Move from ROUTE_CANCELLED to FAILED_NO_PENALTY once all outstanding deliveries has been made.
        ConditionChecker delivery_done = new ConditionChecker() {
            @Override
            public boolean conditionsMet() {
                return getNeedsDeliveryPerson() == null;
            }
        };
        connectWithCustomCondition(Stage.ROUTE_CANCELLED, Stage.FAILED_NO_PENALTY, delivery_done);

        // Go from ACCEPTED to DEPARTURE on ROUTE_TRAVEL_DST
        connectWithCustomCondition(Stage.ACCEPTED, Stage.DEPARTURE,
                new RouteSegmentChecker(EconomyFleetRouteManager.ROUTE_TRAVEL_DST));

        // Go from DEPARTURE to DST_WAIT on ROUTE_DST_UNLOAD
        connectWithCustomCondition(Stage.DEPARTURE, Stage.DST_WAIT,
                new RouteSegmentChecker(EconomyFleetRouteManager.ROUTE_DST_UNLOAD));

        // Go from DST_WAIT to RETURN on ROUTE_TRAVEL_SRC.
        connectWithCustomCondition(Stage.DST_WAIT, Stage.RETURN,
                new RouteSegmentChecker(EconomyFleetRouteManager.ROUTE_TRAVEL_SRC));

        // Go from RETURN to SRC_WAIT on ROUTE_SRC_UNLOAD.
        connectWithCustomCondition(Stage.RETURN, Stage.SRC_WAIT,
                new RouteSegmentChecker(EconomyFleetRouteManager.ROUTE_SRC_UNLOAD));

        // Go from SRC_WAIT to COMPLETE once NEEDS_COMMODITIES is no longer set on quest giver.
        connectWithCustomCondition(Stage.SRC_WAIT, Stage.COMPLETED, delivery_done);

        // Adjust destination market on ROUTE_DST_UNLOAD and ROUTE_SRC_UNLOAD.
        ModifyMarketAction modify_market = new ModifyMarketAction();
        beginStageTrigger(Stage.DST_WAIT);
        triggerCustomAction(modify_market);
        endTrigger();

        beginStageTrigger(Stage.SRC_WAIT, Stage.COMPLETED);
        triggerCustomAction(modify_market);
        endTrigger();
    }

    @Override
    protected void endSuccessImpl(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
        super.endSuccessImpl(dialog, memoryMap);
        // 40% non-player, 50% player
        addPotentialContact(this, dialog, 0.79f, 0.67f);
    }

    // Returns the relevant DeliveryData associated with the person based on flags.
    public DeliveryData getDeliveryData(PersonAPI person) {
        MemoryAPI memory = person.getMemoryWithoutUpdate();
        if (person == getPerson()) {
            return memory.contains(NEEDS_COMMODITIES) ?
                    return_data : memory.contains(HAS_COMMODITIES) ? delivery_data : null;
        }
        if (person == agent) {
            return memory.contains(NEEDS_COMMODITIES) ?
                    delivery_data : memory.contains(HAS_COMMODITIES) ? return_data : null;
        }

        log.error("Talking to unexpected person: " + person.getNameString());
        return null;
    }

    public DeliveryData computeDeliveries(boolean is_return) {
        EconomyRouteData econ_route_data = (EconomyRouteData) route.getCustom();

        CampaignFleetAPI player_fleet = Global.getSector().getPlayerFleet();
        CargoAPI cargo = player_fleet.getCargo();

        // Index 0: fuel, Index 1: personnel, Index 2: cargos
        // For personnel remove skeleton crew from capacity calculation.
        float[] limits = {cargo.getMaxFuel(),
                cargo.getMaxPersonnel() - player_fleet.getFleetData().getMinCrew(),
                cargo.getMaxCapacity()};
        HashMap<String, Float>[] commodities_by_type = new HashMap[limits.length];
        for (int i = 0; i < commodities_by_type.length; ++i) {
            commodities_by_type[i] = new HashMap<>();
            limits[i] *= MIN_TRANSPORT_RATIO + (MAX_TRANSPORT_RATIO - MIN_TRANSPORT_RATIO) * getGenRandom().nextFloat();
        }

        List<CargoQuantityData> cargo_quantity_data;
        MarketAPI buy_market, sell_market;
        if (is_return) {
            cargo_quantity_data = econ_route_data.cargoReturn;
            buy_market = econ_route_data.to;
            sell_market = econ_route_data.from;
        } else {
            cargo_quantity_data = econ_route_data.cargoDeliver;
            buy_market = econ_route_data.from;
            sell_market = econ_route_data.to;
        }
        if (cargo_quantity_data == null || cargo_quantity_data.isEmpty()) return null;

        // Logic adapted from EconomyRouteData.getCargoList
        ArrayList<CargoQuantityData> sorted = new ArrayList<>(cargo_quantity_data);
        Collections.sort(sorted, new Comparator<CargoQuantityData>() {
            public int compare(CargoQuantityData o1, CargoQuantityData o2) {
                if (o1.getCommodity().isPersonnel() && !o2.getCommodity().isPersonnel()) {
                    return 1;
                }
                if (o2.getCommodity().isPersonnel() && !o1.getCommodity().isPersonnel()) {
                    return -1;
                }
                return o2.units - o1.units;
            }
        });
        int size = 0;
        for (CargoQuantityData curr : sorted) {
            if (size >= 4) break;

            CommoditySpecAPI spec = curr.getCommodity();
            if (spec.isMeta()) continue;
            if (spec.isNonEcon()) continue;

            ++size;
            if (spec.getId().equals(Commodities.SHIPS)) continue;

            // Logic adapted from EconomyFleetAssignmentAI.updateCargo; note that BaseIndustry.getSizeMult
            // currently returns 1f.
            float quantity = BaseIndustry.getSizeMult(curr.units) * spec.getEconUnit();
//            log.debug(MessageFormat.format("commodity: {0}, quantity={1}, units={2}, multiplier={3}, econ_units={4}",
//                    spec.getId(), quantity, curr.units, BaseIndustry.getSizeMult(curr.units), spec.getEconUnit()));
            int index = spec.isFuel() ? 0 : spec.isPersonnel() ? 1 : 2;
            commodities_by_type[index].put(spec.getId(), quantity);
        }

        // Normalize cargos to be within player's fuel/personnel/cargo limits.
        HashMap<String, Integer> commodities = new HashMap<>();
        int[] totals_by_type = {0, 0, 0};
        for (int i = 0; i < commodities_by_type.length; ++i) {
            float total_size = 0f;
            for (Map.Entry<String, Float> entry : commodities_by_type[i].entrySet()) {
                CommoditySpecAPI spec = Global.getSettings().getCommoditySpec(entry.getKey());
                total_size += Math.max(1f, spec.getCargoSpace()) * entry.getValue();
            }
            float ratio = Math.min(1f, limits[i] / total_size);

            for (Map.Entry<String, Float> entry : commodities_by_type[i].entrySet()) {
                int rounded = Math.max(10, myGetRoundNumber(entry.getValue() * ratio));
//                log.debug(MessageFormat.format("total_size={0}, ratio={1}, rounded={2}", total_size, ratio, rounded));
                commodities.put(entry.getKey(), rounded);
                totals_by_type[i] += rounded;
            }
        }
//        log.debug(MessageFormat.format("limits={0}, commodities_by_type={1}, commodities={2}",
//                Arrays.toString(limits), Arrays.toString(commodities_by_type), commodities));

        if (commodities.isEmpty()) return null;

        // Now we compute how much credit we get paid for delivery and escrow. The escrow is just the cost to
        // acquire the materials on the buy market plus tariff (30%). The reward, however, is the same logic adapted
        // from DeliveryBarEvent (but we skip all the logic for adjusting quantity since that is already computed).
        float distLY = Misc.getDistanceLY(econ_route_data.from.getLocationInHyperspace(),
                econ_route_data.to.getLocationInHyperspace());
        float score = Math.min(distLY, 10f);
        for (MarketAPI market : Arrays.asList(econ_route_data.from, econ_route_data.to)) {
            if (market.hasCondition(Conditions.PIRATE_ACTIVITY)) score += 10f;
            if (market.hasCondition(Conditions.PATHER_CELLS)) score += 5f;
        }
        float mult = Math.max(score / 30f, 0.75f);

        int total_buy_price = 0;
        int total_price = 0;
        Map<String, Integer> prices = new HashMap<>();
        StringBuilder buf = new StringBuilder();
        for (Map.Entry<String, Integer> entry : commodities.entrySet()) {
            float base = buy_market.getSupplyPrice(entry.getKey(), 1, true);
            int quantity = entry.getValue();
            if (quantity * base < 4000) {
                base = Math.min(100, 4000 / quantity);
            }

            float minBase = 100f - 50f * Math.min(1f, quantity / 500f);
            base = Math.max(base, (base + minBase) * 0.75f);
            // delivery_reward should be close to what DeliveryBarEvent gives you for the same commodity.
            int delivery_reward = Math.max(Math.round(base * mult * quantity), 4000);

            int buy_price = Math.round(buy_market.getSupplyPrice(entry.getKey(), entry.getValue(), true));
            int sell_price = Math.round(sell_market.getDemandPrice(entry.getKey(), entry.getValue(), true));
            total_buy_price += buy_price;
            int price = myGetRoundNumber(Math.max(sell_price - buy_price, delivery_reward));
            total_price += price;
            prices.put(entry.getKey(), price);
            buf.append(MessageFormat.format(
                    " ({0}, qty={1}, buy_price={2}, sell_price={3}, base={4}, delivery_reward={5}, price={6})",
                    entry.getKey(), quantity, buy_price, sell_price, base, delivery_reward, price));
        }

        int escrow = myGetRoundNumber(total_buy_price * 1.3f);

        log.debug(MessageFormat.format(
                "computeDeliveries: return={0}, total_price={1}, escrow={2}, fuel={3}, personnel={4}" +
                        ", cargo_qty={5}, distLY={6}, score={7}, mult={8}; ",
                is_return, total_price, escrow, totals_by_type[0], totals_by_type[1], totals_by_type[2], distLY, score,
                mult) + buf);
        return new DeliveryData(commodities, prices, escrow);
    }

    protected void showCargoOffer(TooltipMakerAPI info, DeliveryData data, float pad) {
        if (data == null) return;
        String escrow_str = Misc.getDGSCredits(data.escrow);
        String reward_str = Misc.getDGSCredits(data.totalPrice());

        List<String> highlights = data.toHighlights();
        String transport_desc = data.toString();
        highlights.add(reward_str);
        highlights.add(escrow_str);

        String pattern = "\"If you''re interested in making more money, we would like you to carry {0}." +
                " You will be paid an additional {1}, but you will have to transfer {2} to an escrow account" +
                ", which will be returned to you on delivery.\"";
        info.addPara(MessageFormat.format(pattern, transport_desc, reward_str, escrow_str),
                pad, Misc.getHighlightColor(), highlights.toArray(new String[0]));
    }

    protected void showRemainingCapacityAndMap(InteractionDialogAPI dialog, MarketAPI market, DeliveryData data) {
        if (data == null) return;

        ArrayList<String> commodity_ids = new ArrayList<>();  // First commodity id per type.
        boolean has_commodity = false;
        for (Pair<String, Integer> entry : data.getSorted()) {
            CommoditySpecAPI comm_spec = Global.getSettings().getCommoditySpec(entry.one);
            if (comm_spec.isFuel() || comm_spec.isPersonnel()) {
                commodity_ids.add(entry.one);
            } else if (!has_commodity) {
                has_commodity = true;
                commodity_ids.add(entry.one);
            }
        }

        ShowRemainingCapacity plugin = new ShowRemainingCapacity();
        for (String id : commodity_ids) {
//            plugin.execute(null, dialog, Arrays.asList(new Token(id, TokenType.LITERAL)), null);
            plugin.execute(null, dialog,
                    Collections.singletonList(new Misc.Token(id, Misc.TokenType.LITERAL)), null);
        }

        Set<String> tags = Collections.singleton(Tags.INTEL_MISSIONS);
        // Icon should be commodity agent at destination
        dialog.getVisualPanel().showMapMarker(market.getPrimaryEntity(),
                "Destination: " + market.getName(), market.getFaction().getBaseUIColor(),
                true, agent != null ? agent.getPortraitSprite() : null, null, tags);
    }

    @Override
    protected boolean callAction(String action, String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params,
                                 Map<String, MemoryAPI> memoryMap) {
        if ("showDetail".equals(action)) {
            EconomyRouteData econ_route_data = (EconomyRouteData) route.getCustom();
            final float pad = 10f;

            TextPanelAPI text = dialog.getTextPanel();
            TooltipMakerAPI info = text.beginTooltip();
            info.setParaSmallInsignia();

            Color dest_color = econ_route_data.to.getFaction().getBaseUIColor();
            Color src_color = econ_route_data.from.getFaction().getBaseUIColor();
            String dest_faction_prefix = econ_route_data.to.getFaction().getPersonNamePrefix();
            String src_faction_name = econ_route_data.from.getFaction().getDisplayName();
            String destination = econ_route_data.to.getName();
            String cargo_delivery = EconomyRouteData.getCargoList(econ_route_data.cargoDeliver);
            String cargo_return = EconomyRouteData.getCargoList(econ_route_data.cargoReturn);

            String pattern;
            if (!econ_route_data.cargoDeliver.isEmpty() && !econ_route_data.cargoReturn.isEmpty()) {
                pattern = "\"We will be carrying {0} on our trip, and on our return trip we will be carrying {1}.";
            } else if (!econ_route_data.cargoDeliver.isEmpty()) {
                pattern = "\"We will be carrying {0} on our trip.";
            } else {
                pattern = "\"We will be returning from our trip with {1}.";
            }
            pattern += " You will be paid if our convoy makes it back safely without losing significant amounts of" +
                    " transport capacity.\"";

            info.addPara(MessageFormat.format(pattern, cargo_delivery, cargo_return), 0f);

            if (agent != null) {
                if (delivery_data != null) {
                    showCargoOffer(info, delivery_data, 10f);
                } else {
                    String trader_name = agent.getNameString();
                    Color[] hl_colors = {agent.getFaction().getBaseUIColor(), dest_color};
                    String[] highlights = {trader_name, destination};
                    pattern = "In addition, we would like to make use of your fleet''s transport capacity to carry" +
                            " additional commodities. If you''re interested, please contact {0} once you''ve reached {1}.";
                    info.addPara(MessageFormat.format(pattern, trader_name, destination), pad, hl_colors, highlights);
                }
            }

            info.addPara("\"What do you think?\"", pad);

            // Find factions at war with source market.
            TreeSet<String> hostilities = new TreeSet<>();
            for (IntelInfoPlugin plugin : Global.getSector().getIntelManager().getIntel(FactionHostilityIntel.class)) {
                FactionHostilityIntel intel = (FactionHostilityIntel) plugin;
                if (intel == null) continue;
                if (intel.getOne() == econ_route_data.from.getFaction()) {
                    hostilities.add(intel.getTwo().getId());
                } else if (intel.getTwo() == econ_route_data.from.getFaction()) {
                    hostilities.add(intel.getOne().getId());
                }
            }

            ArrayList<Color> hl_colors = new ArrayList<>(Arrays.asList(dest_color, dest_color));
            ArrayList<String> highlights = new ArrayList<>(Arrays.asList(destination, dest_faction_prefix));
            String hostility_desc = "";
            if (hostilities.isEmpty()) {
                pattern = "You recall that {0} is under {1} control.";
            } else {
                pattern = "You recall that {0} is under {1} control, and that {2} is at war with {3}.";
                hl_colors.add(src_color);
                highlights.add(src_faction_name);

                ArrayList<String> names_with_article = new ArrayList<>();
                for (String faction_id : hostilities) {
                    FactionAPI faction = Global.getSector().getFaction(faction_id);
                    hl_colors.add(faction.getBaseUIColor());
                    highlights.add(faction.getDisplayNameWithArticleWithoutArticle());
                    names_with_article.add(faction.getDisplayNameWithArticle());
                }
                hostility_desc = Misc.getAndJoined(names_with_article);
            }
            info.addPara(
                    MessageFormat.format(pattern, destination, dest_faction_prefix, src_faction_name, hostility_desc),
                    pad, hl_colors.toArray(new Color[0]), highlights.toArray(new String[0]));

            text.addTooltip();

            showRemainingCapacityAndMap(dialog, econ_route_data.to, delivery_data);
            return true;
        }

        if ("showCargoOffer".equals(action)) {
            // Called when player contacts the trader while the trader has commodities for transport (HAS_COMMODITIES).
            TextPanelAPI text = dialog.getTextPanel();
            TooltipMakerAPI info = text.beginTooltip();
            info.setParaSmallInsignia();
            showCargoOffer(info, return_data, 0f);
            text.addTooltip();
            showRemainingCapacityAndMap(dialog, route.getMarket(), return_data);
            return true;
        }

        if ("acceptCargo".equals(action)) {
            // Called when player contacts the agent or quest giver to accept the cargo (HAS_COMMODITIES).
            PersonAPI person = dialog.getInteractionTarget().getActivePerson();
            if (agent == null) {
                log.error("No agent at dst market.");
                return false;
            }
            if (person != agent && person != getPerson()) {
                log.error("Talking to unexpected person: " + person.getNameString());
                return false;
            }

            DeliveryData data = getDeliveryData(person);
            if (data == null) {  // Sanity check
                log.error("NULL data.");
                return false;
            }

            // If this is the quest giver the flag will be unset again in setCurrentStage but that's fine.
            person.getMemoryWithoutUpdate().unset(HAS_COMMODITIES);
            makePersonImportant(person == getPerson() ? agent : getPerson(), NEEDS_COMMODITIES);

            data.addToPlayer(dialog.getTextPanel());

            return true;
        }

        if ("unloadCargo".equals(action)) {
            // Called when player contacts the trader or quest giver while having cargo to unload.
            PersonAPI person = dialog.getInteractionTarget().getActivePerson();
            if (agent == null) {
                log.error("No agent at dst market.");
                return false;
            }
            if (person != agent && person != getPerson()) {
                log.error("Talking to unexpected person: " + person.getNameString());
                return false;
            }

            DeliveryData data = getDeliveryData(person);
            if (data == null) {  // Sanity check
                log.error("NULL data.");
                return false;
            }
            person.getMemoryWithoutUpdate().unset(NEEDS_COMMODITIES);

            data.removeFromPlayer(dialog.getTextPanel());
            if (data.stacks == null) return true;  // Only possible with mission from earlier version.

            applyTradeMods(dialog.getInteractionTarget().getMarket(),  false, true, true,
                    data.stacks, data.prices);

            // Delivery count as a proCom mission in terms of locking out cheapCom missions, so you can't get
            // cheapCom from contact immediately after doing an escort.
            MemoryAPI memory = person.getMarket().getMemoryWithoutUpdate();
            for (String commodityId : data.stacks.keySet()) {
                memory.set(CHEAP_COM_TIMEOUT_PREFIX + commodityId, true, TRADE_IMPACT_DAYS);
            }

            return true;
        }

        return super.callAction(action, ruleId, dialog, params, memoryMap);
    }

    @Override
    public void updateInteractionData(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
        super.updateInteractionData(dialog, memoryMap);
        // Used by rules.csv
        set("$mmm_em_credits", Misc.getDGSCredits(getCreditsReward()));
        int days = getTimeLimitDays();
        set("$mmm_em_delay_days", String.valueOf(days));
        set("$mmm_em_day_or_days", getDayOrDays(days));
        EconomyRouteData econ_route_data = (EconomyRouteData) route.getCustom();
        set("$mmm_em_from_market", econ_route_data.from.getName());
        set("$mmm_em_from_market_color", econ_route_data.from.getFaction().getBaseUIColor());
        set("$mmm_em_to_market", econ_route_data.to.getName());
        set("$mmm_em_to_market_color", econ_route_data.to.getFaction().getBaseUIColor());
        set("$mmm_em_fleet_name", getFleetTypeName().toLowerCase());
        if (route.getFactionId().equals(Factions.PIRATES)) {
            set("$mmm_em_underworld", true);
        }
        set("$mmm_em_quest_giver", getPerson().getNameString());
        if (agent != null) {
            set("$mmm_em_agent", agent.getNameString());
        }

        PersonAPI person = getCurrentStage() == null ? getPerson() : dialog.getInteractionTarget().getActivePerson();
        if (person == null) {
            log.error("No interaction target person?");
            return;
        }
        if (person != agent && person != getPerson()) {
            log.error("Talking to unexpected person: " + person.getNameString());
            return;
        }

        // Computes $mmm_em_escrow, $mmm_em_price and $mmm_em_has_enough, which can change depending on who and when we
        // talk to them.
        DeliveryData data = getDeliveryData(person);
        if (data != null) {
            set("$mmm_em_escrow", data.escrow);
            set("$mmm_em_delivery_reward", data.totalPrice());
            set("$mmm_em_transport_desc", data.toString());
            boolean enough = true;
            for (Pair<String, Integer> entry : data.getSorted()) {
                if (!playerHasEnough(entry.one, entry.two)) {
                    enough = false;
                    break;
                }
            }
            set("$mmm_em_has_enough", enough);
        }
    }

    @Override
    public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {
        if (isEnding()) return;
        if (route.getCurrentSegmentId() >= EconomyFleetRouteManager.ROUTE_SRC_UNLOAD) return;
        if (fleet != route.getActiveFleet()) return;  // Sanity check

        int previous_size = fleet_size;
        fleet_size = fleet.getFleetPoints();
        if (!fleet.isAlive()) {
            stage_description = getFleetTypeName() + " destroyed.";
        } else if (!battle.isPlayerInvolved() && previous_size > fleet_size) {
            log.info(MessageFormat.format("Player is not involved in battle; previous_size={0}, fleet_size={1}",
                    previous_size, fleet_size));
            stage_description = getFleetTypeName() + " lost ships while player is missing from battle.";
        } else {
            // You also fail if the trade fleet lose enough carrying capacity to cause shipment loss;
            // logic adapted from EconomyFleetRouteManager.reportBattleOccurred.
            EconomyRouteData econ_route_data = (EconomyRouteData) route.getCustom();
            CargoAPI cargo = fleet.getCargo();

            float lost_cargo_ratio = 1f - cargo.getMaxCapacity() / cargoCap;
            float lost_fuel_ratio = 1f - cargo.getMaxFuel() / fuelCap;
            float lost_personnel_ratio = 1f - cargo.getMaxPersonnel() / personnelCap;

            final float lossFraction = 0.34f;
            boolean lost_cargo = cargoCap * lossFraction > cargo.getMaxCapacity();
            boolean lost_fuel = fuelCap * lossFraction > cargo.getMaxFuel();
            boolean lost_personnel = personnelCap * lossFraction > cargo.getMaxPersonnel();

            ArrayList<CargoQuantityData> cargo_list = new ArrayList<>(econ_route_data.cargoReturn);
            // Note that the cargo disappears during ROUTE_DST_LOAD.
            if (route.getCurrentSegmentId() < EconomyFleetRouteManager.ROUTE_DST_LOAD) {
                cargo_list.addAll(econ_route_data.cargoDeliver);
            }

            TreeSet<String> lost_commodities = new TreeSet<>();
            float max_lost_ratio = 0f;
            for (CargoQuantityData cargo_data : cargo_list) {
                if (cargo_data.units <= 0) continue;  // Sanity check
                CommoditySpecAPI comm_spec = Global.getSettings().getCommoditySpec(cargo_data.cargo);
                if (comm_spec.isFuel() ? lost_fuel : comm_spec.isPersonnel() ? lost_personnel : lost_cargo) {
                    lost_commodities.add(cargo_data.cargo);
                    float lost_ratio = comm_spec.isFuel() ? lost_fuel_ratio : comm_spec.isPersonnel() ?
                            lost_personnel_ratio : lost_cargo_ratio;
                    max_lost_ratio = Math.max(max_lost_ratio, lost_ratio);
                }

                if (comm_spec.isFuel() && lost_fuel) {
                    lost_commodities.add(cargo_data.cargo);
                } else if (comm_spec.isPersonnel() && lost_personnel) {
                    lost_commodities.add(cargo_data.cargo);
                } else if (lost_cargo) {
                    lost_commodities.add(cargo_data.cargo);
                }
            }

            boolean lost = !lost_commodities.isEmpty();
            log.info(MessageFormat.format(
                    "reportBattleOccurred: lost={0}, cargo:({1}, {2}, {3}), fuel:({4}, {5}, {6})" +
                            ", personnel:({7}, {8}, {9}), lost_commodities={10}",
                    lost, cargoCap, cargo.getMaxCapacity(), lost_cargo,
                    fuelCap, cargo.getMaxFuel(), lost_fuel,
                    personnelCap, cargo.getMaxPersonnel(), lost_personnel, lost_commodities));
            if (lost) {
                stage_description = MessageFormat.format("{0} lost too much transport capacity ({1}%).",
                        getFleetTypeName(), Math.round(max_lost_ratio * 100));
            } else {
                return;
            }
        }

        // We set a memory flag here instead of calling setCurrentStage to avoid concurrent list modification error.
        setFlag(fleet, FAIL_FLAG, true);
        log.info(stage_description);
    }

    // Needed so the class isn't abstract.
    @Override
    public void reportFleetDespawnedToListener(CampaignFleetAPI fleet,
                                               CampaignEventListener.FleetDespawnReason reason, Object param) {
        if (fleet != null) {
            log.info("Fleet " + fleet.getNameWithFaction() + " de-spawned; reason=" + reason);
        }
    }

    @Override
    protected void notifyEnding() {
        super.notifyEnding();
        if (route.getActiveFleet() != null) {
            route.getActiveFleet().removeEventListener(this);
        }
    }

    // where on the map the intel screen tells us to go
    @Override
    public SectorEntityToken getMapLocation(SectorMapAPI map) {
        if (getCurrentStage() == Stage.ROUTE_CANCELLED) {
            for (PersonAPI person : Arrays.asList(agent, getPerson())) {
                if (person == null) continue;
                if (person.getMemoryWithoutUpdate().getBoolean(NEEDS_COMMODITIES) && person.getMarket() != null) {
                    return person.getMarket().getPrimaryEntity();
                }
            }
        }
        return route.getActiveFleet() == null ? route.getMarket().getPrimaryEntity() : route.getActiveFleet();
    }

    transient protected String fleet_type_name = null;
    public String getFleetTypeName() {
        if (fleet_type_name != null) return fleet_type_name;
        EconomyRouteData data = (EconomyRouteData) route.getCustom();
        String typeId = EconomyFleetRouteManager.getFleetTypeIdForTier(data.size, data.smuggling);
        fleet_type_name = Global.getSector().getFaction(route.getFactionId()).getFleetTypeName(typeId);
        if (fleet_type_name == null || fleet_type_name.equals("Trader")) {
            fleet_type_name = "Trade Fleet";
        }
        return fleet_type_name;
    }

    // mission name on intel screen
    @Override
    public String getBaseName() { return getFleetTypeName() + " Escort"; }

    @Override
    public String getStageDescriptionText() {
        if (getCurrentStage() == Stage.FAILED_NO_PENALTY || getCurrentStage() == Stage.ROUTE_CANCELLED) {
            EconomyRouteData econ_route_data = (EconomyRouteData) route.getCustom();
            return areMarketsHostile(econ_route_data.from, econ_route_data.to) ?
                    "Route cancelled - faction hostility." : "Route cancelled.";
        }
        return stage_description;
    }

    @Override
    public void addDescriptionForCurrentStage(TooltipMakerAPI info, float width, float height) {
        if (stage_description != null) {
            info.addPara(stage_description, Misc.getNegativeHighlightColor(), 10f);
            return;
        }
        super.addDescriptionForCurrentStage(info, width, height);
    }

    @Override
    public String getPostfixForState() {
        return currentStage == Stage.FAILED_NO_PENALTY ? " - Route Cancelled" : super.getPostfixForState();
    }

    // Needed to display intel correctly.
    @Override
    protected void addBulletPoints(TooltipMakerAPI info, ListInfoMode mode) {
        if (getListInfoParam() != null && isFailed() && stage_description != null) {
            info.addPara(stage_description, Misc.getNegativeHighlightColor(), 4f);
        }

        // adapted from BaseHubMission.addBulletPoints
        bullet(info);

        boolean display_reward = false;
        int steps = 0;  // 0, 1, or 2
        boolean is_finished = isFailed() || isSucceeded();
        if (getListInfoParam() != null) {  // is update.
            if (is_finished) {
                addResultBulletsAssumingAlreadyIndented(info, mode);
            } else {
                steps = 1;
            }
        } else if (getResult() != null) {
            if (mode == ListInfoMode.IN_DESC) addResultBulletsAssumingAlreadyIndented(info, mode);
        } else {
            display_reward = mode == ListInfoMode.IN_DESC && getCurrentStage() != Stage.ROUTE_CANCELLED;
            steps = is_finished ? 0 : mode == ListInfoMode.IN_DESC ? 2 : 1;
        }

        float pad = 3f;
        Color h = Misc.getHighlightColor();
        Color text_color = getBulletColorForMode(mode);

        if (display_reward) {
            info.addPara("%s reward", pad, text_color, h, Misc.getDGSCredits(getCreditsReward()));
            pad = 0f;
        }

        EconomyRouteData econ_route_data = (EconomyRouteData) route.getCustom();
        String from_market_name = econ_route_data.from.getName();
        String to_market_name = econ_route_data.to.getName();
        Object current_stage = getCurrentStage();
        if (steps == 1) {  // Stage update
            int days = getTimeLimitDays();
            String days_str = String.valueOf(days);
            String day_or_days = getDayOrDays(days);
            if (current_stage == Stage.ACCEPTED || current_stage == Stage.DST_WAIT) {
                info.addPara(MessageFormat.format("Wait for fleet departure in {0} {1}", days_str, day_or_days),
                        pad, text_color, h, days_str);
            } else if (current_stage == Stage.DEPARTURE || current_stage == Stage.RETURN) {
                String fleet_name = route.getActiveFleet() != null ?
                        route.getActiveFleet().getFullName() : getFleetTypeName();
                String back_or_no = current_stage == Stage.RETURN ? " back" : "";
                String dest_name = current_stage == Stage.RETURN ? econ_route_data.from.getName() : to_market_name;
                info.addPara(MessageFormat.format("Escort {0}{1} to {2}", fleet_name, back_or_no, dest_name),
                        pad, text_color, h, dest_name);

            } else if (current_stage == Stage.ROUTE_CANCELLED) {
                info.addPara(getStageDescriptionText(), pad);
            }

            if (Arrays.asList(Stage.DST_WAIT, Stage.SRC_WAIT, Stage.ROUTE_CANCELLED).contains(current_stage)) {
                for (PersonAPI person : Arrays.asList(agent, getPerson())) {
                    if (person == null) continue;
                    MemoryAPI memory = person.getMemoryWithoutUpdate();
                    String pattern = null;
                    if (memory.getBoolean(NEEDS_COMMODITIES)) {
                        pattern = "Talk to {0} to unload commodities within {1} {2}.";
                    } else if (memory.getBoolean(HAS_COMMODITIES) && current_stage != Stage.ROUTE_CANCELLED) {
                        pattern = "Optional: talk to {0} to acquire commodities within {1} {2}.";
                    }
                    if (pattern != null) {
                        info.addPara(MessageFormat.format(pattern, person.getNameString(), days_str, day_or_days),
                                0f, text_color, h, person.getNameString(), days_str);
                        break;
                    }
                }
            }
        } else if (steps > 1) {  // RHS intel
            if (current_stage == Stage.ACCEPTED || current_stage == Stage.DEPARTURE) {
                info.addPara(MessageFormat.format("Escort fleet to {0} and back.", to_market_name),
                        pad, text_color, h, to_market_name);
            } else if (current_stage == Stage.DST_WAIT || current_stage == Stage.RETURN) {
                info.addPara(MessageFormat.format("Escort fleet back to {0}.", from_market_name),
                        pad, text_color, h, from_market_name);
            }

            if (current_stage != Stage.SRC_WAIT && current_stage != Stage.ROUTE_CANCELLED) {
                info.addPara("Participate in all fleet engagements.", text_color, 0f);
                String last_seen_str = "";
                if (last_seen_checker != null) {
                    long now = Global.getSector().getClock().getTimestamp();
                    long last_seen_ts = last_seen_checker.getAndUpdateLastSeen(now);
                    if (last_seen_ts < now) {
                        last_seen_str = MessageFormat.format(" (last seen {0} ago)",
                                getDaysFromNowStr(last_seen_ts, now));
                    }
                }
                info.addPara(MessageFormat.format(
                        "Do not leave the convoy''s sensor range for more than {0} days{1}.",
                                MAX_DAYS_LAST_SEEN, last_seen_str),
                        text_color, 0f);
            }

            for (PersonAPI person : Arrays.asList(agent, getPerson())) {
                if (person != null) {
                    MemoryAPI memory = person.getMemoryWithoutUpdate();
                    String market_name = person.getMarket().getName();
                    DeliveryData data = getDeliveryData(person);
                    if (memory.getBoolean(NEEDS_COMMODITIES) && data != null) {
                        String reward = Misc.getDGSCredits(data.totalPrice());
                        String escrow = Misc.getDGSCredits(data.escrow);
                        info.addPara(MessageFormat.format(
                                "Talk to {0} once you have reached {1} to unload commodities ({2} reward, {3} escrow).",
                                        person.getNameString(), market_name, reward, escrow),
                                0f, text_color, h, person.getNameString(), market_name, reward, escrow);
                        info.addPara("Manifest: " + data, 0f, text_color, h,
                                data.toHighlights().toArray(new String[0]));
                    } else if (memory.getBoolean(HAS_COMMODITIES) && current_stage != Stage.ROUTE_CANCELLED) {
                        info.addPara(MessageFormat.format(
                                "Optional: talk to {0} once you have reached {1} to acquire commodities.",
                                        person.getNameString(), market_name),
                                0f, text_color, h, person.getNameString(), market_name);
                    }
                }
            }
        }
        unindent(info);
    }
}