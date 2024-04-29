package mmm.missions;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithBarEvent;
import com.fs.starfarer.api.impl.campaign.missions.hub.MissionTrigger.TriggerAction;
import com.fs.starfarer.api.impl.campaign.missions.hub.MissionTrigger.TriggerActionContext;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;
import com.fs.starfarer.api.util.WeightedRandomPicker;

import java.awt.Color;
import java.text.MessageFormat;
import java.util.*;

import mmm.Utils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicSettings;

public class DefenseMission extends HubMissionWithBarEvent implements FleetEventListener {
    private static final String MOD_ID = Utils.MOD_ID;
    private static final Logger log = Global.getLogger(DefenseMission.class);
    static {
        if (MagicSettings.getBoolean(MOD_ID, "MmmDebug")) {
            log.setLevel(Level.ALL);
        }
    }
    public enum Stage {
        ACCEPTED,
        IN_SYSTEM,
        COMPLETED,
        FAILED
    }

    // Strings:
    public static final String MISSION_ID = "mmm_dm";

    // Memory flags:
    public static final String MISSION_DATA_KEY = "$mmm_dm_mission_data";
    public static final String DIFFICULTY_KEY = "$mmm_dm_difficulty";

    // Settings:
    // How far to spawn enemy fleets away from the station.
    public static final float SPAWN_DISTANCE_MIN = 5500f;
    public static final float SPAWN_DISTANCE_MAX = 6000f;
    // How many arc does the enemy spawn from?
    public static final int SPAWN_DEGREES = 45;
    // An enemy fleet is considered to be defeated if its fleet strength falls at or below this ratio. Note that
    // MagicLib generates logistic ships at 20% of total
    public static final float FLEET_DEFEAT_RATIO = 0.4f;
    // Beware of overflow if you convert this to float instead of double.
    public static final long MILLISECONDS_PER_DAY = OrbitalMissionBase.MILLISECONDS_PER_DAY;
    // Maximum number of fleets to show in the intel screen.
    public static final int MAX_FLEETS_TO_SHOW = 20;
    // Add this fraction only enemy fleet points as supplies to their cargo
    public static final float PLAYER_FP_TO_SUPPLIES_RATIO = 0.75f;
    // Don't generate multiple fleets if it would result in smaller fleets then this limit, so the enemy fleets have
    // to be larger than 2X this limit for in order for more then 1 enemy fleet to appear.
    public static final int MIN_ENEMY_FP_PER_FLEET = 120;

    // These following settings are loaded from modSettings.json after restart:
    // Chance to add contact after mission success
//    public static final float POTENTIAL_CONTACT_PROB =
//            MagicSettings.getFloat(MOD_ID, "DmPotentialContactProbability");
    // How many days you need to defend the station after arriving in system.
    public static final int MAX_DEFEND_DAYS = MagicSettings.getInteger(MOD_ID, "DmMaxDefendDays");
    // How many price you get per fleet strength.
    public static int CREDIT_REWARD_PER_FP;
    // Minimum credit you get per mission.
    public static int MIN_CREDIT_REWARD;
    // How much the max enemy fleet strength grows after a successful mission (based on current enemy fleet strength).
    public static float DIFFICULTY_GROWTH;
    // Reduce your mission credit by this ratio of station fleet strength; the smaller this number is, the less the
    // station FP matters in reward computation and the more credit you get.
    public static final float STATION_FP_REWARD_REDUCTION =
            MagicSettings.getFloat(MOD_ID, "DmStationFpRewardReduction");
    // Chance that reinforcements will have only 1 faction; chance that the fleets will have N factions is:
    // SINGLE_FACTION_PROB * (1 - SINGLE_FACTION_PROB) ^ (N - 1)
    public static final float SINGLE_FACTION_PROB = MagicSettings.getFloat(MOD_ID, "DmSingleFactionProb");
    // The time granularity for changing invasion decisions. The actual limit is between 80% and 120% of this value.
    public static final int INVASION_TIME_PERIOD_DAYS = MagicSettings.getInteger(MOD_ID, "DmInvasionTimePeriodDays");
    // The first time you visit a market, what's the chance that there's an invasion? If you fail the roll, you
    // need to wait INVASION_TIME_PERIOD_DAYS for the invasion to arrive.
    public static final float INVASION_PROB = MagicSettings.getFloat(MOD_ID, "DmInvasionProb");
    // If true generate enemies from any factions with ships, including hidden factions, like omega and Lion's Guard.
    // Can spoil content and break immersion if set to true. If false also skip factions with no markets.
    public static boolean USE_HIDDEN_FACTIONS;
    // If set allied fleets won't chase after the enemy fleets, making it more likely that they will make it to the
    // station.
    public static boolean ENEMY_IGNORED_BY_OTHER_FLEETS;
    // Blacklist the provided faction ID from enemy fleet reinforcement; has priority over FACTION_WHITELIST.
    public static final Set<String> FACTION_BLACKLIST =
            new HashSet<>(MagicSettings.getList(MOD_ID, "DmReinforcementFactionBlacklist"));
    // Treat the provided provided faction ID as non-hidden (and has markets), allowing it to be used even if
    // USE_HIDDEN_FACTIONS is false.
    public static final Set<String> FACTION_WHITELIST =
            new HashSet<>(MagicSettings.getList(MOD_ID, "DmReinforcementFactionWhitelist"));
    public static final Map<String, Float> FACTION_STRENGTH_RATIO = OrbitalMissionBase.FACTION_STRENGTH_RATIO;

    public MissionData mission_data = null;  // also attached to the Market's memory permanently
    public CampaignFleetAPI station = null;  // The defending station.

    public static class StationDisruptedChecker implements ConditionChecker {
        public CampaignFleetAPI station;
        public Industry industry;
        public StationDisruptedChecker(CampaignFleetAPI station, Industry industry) {
            this.station = station;
            this.industry = industry;
        }
        @Override
        public boolean conditionsMet() {
            return !station.isAlive() || industry.isDisrupted();
        }
    }

    public static class FleetsDefeatedChecker implements ConditionChecker {
        public List<CampaignFleetAPI> fleets;
        public List<Integer> defeat_thresholds;
        public FleetsDefeatedChecker(List<CampaignFleetAPI> fleets, List<Integer> defeat_thresholds) {
            this.fleets = fleets;
            this.defeat_thresholds = defeat_thresholds;
        }
        @Override
        public boolean conditionsMet() {
            for (int i = 0; i < fleets.size(); ++i) {
                CampaignFleetAPI fleet = fleets.get(i);
                if (fleet.isAlive() && getFleetStrength(fleet) > defeat_thresholds.get(i)) {
                    return false;
                }
            }
            return true;
        }
    }

    // A TriggerAction for enabling enemy fleet AI and moving it to the correct position.
    public static class WakeUpEnemyFleetsAction implements TriggerAction {
        public List<CampaignFleetAPI> fleets;
        public CampaignFleetAPI target;
        public Random random;
        public WakeUpEnemyFleetsAction(List<CampaignFleetAPI> fleets, CampaignFleetAPI target, Random random) {
            this.fleets = fleets;
            this.target = target;
            this.random = random;
        }

        @Override
        public void doAction(TriggerActionContext context) {
            String enemy_faction = pickEnemyFaction(target.getFaction());
            log.debug("WakeUpEnemyFleetAction.doAction: " + enemy_faction);
            String actionText = "attacking " + target.getName();
            Vector2f target_loc = target.getLocation();
            float base_angle = (float) random.nextFloat() * 360;
            for (CampaignFleetAPI fleet : fleets) {
                if (enemy_faction != null) {
                    fleet.setFaction(enemy_faction, true);
                }

                // Ensure the fleet is in the right system
//                if (target.getContainingLocation() != fleet.getContainingLocation()) {
                    fleet.getContainingLocation().removeEntity(fleet);
                    target.getContainingLocation().addEntity(fleet);
//                }

                float distance = SPAWN_DISTANCE_MIN + random.nextFloat() * (SPAWN_DISTANCE_MAX - SPAWN_DISTANCE_MIN);
                float angle = base_angle + (float) (Math.random() * SPAWN_DEGREES) % 360;
                float angle_radians = (float) (angle * Math.PI * 2f / 360);
                float x = (float) (Math.cos(angle_radians) * distance) + target_loc.x;
                float y = (float) (Math.sin(angle_radians) * distance) + target_loc.y;
                fleet.setLocation(x, y);

                fleet.clearAssignments();
                // Less likely to be distracted by hostile fleets then ATTACK_LOCATION
                fleet.addAssignment(FleetAssignment.DELIVER_MARINES, target, 1000f, actionText);
                fleet.addAssignment(FleetAssignment.ATTACK_LOCATION, target, 1000f, actionText);
                fleet.setDoNotAdvanceAI(false);
            }
        }
    }

    public static class MissionData {
        public List<CampaignFleetAPI> fleets = new ArrayList<>();
        // If the fleet strength of fleets.get(i) fall to or below defeat_thresholds.get(i), consider it defeated.
        // Therefore, fleets and defeat_thresholds should always have the same size.
        public List<Integer> defeat_thresholds = new ArrayList<>();
        // What ship types are we fighting against rather than what relations are affected, sorted according to the
        // effective strength of the strongest fleet.
        public List<String> reinforcement_factions = new ArrayList<>();
        // The difficulty of this mission, defined as the enemy fleet strength minus station fleet strength.
        public int difficulty = 0;
        // When does the fleet expire and has to be regenerated?
        public long fleet_expiration_ts = Long.MAX_VALUE;

        // Did the last roll succeeds?
        public boolean roll_result = false;
        // When the roll result expires; afterward if the roll was a success then no invasion is allowed for 1 cool down
        // period. If the roll was a fail then the invasion is guaranteed for 2 cool down periods after
        // 2 * (1 / PROB - 1) duration of no invasions.
        public long roll_expires_ts = Long.MIN_VALUE;
        // deprecated; do not use
        public long cool_down_end_ts = Long.MIN_VALUE;
        // Start of the guaranteed invasion time window.
        public long guaranteed_start_ts = Long.MIN_VALUE;

        public void ClearFleets() {
            fleets.clear();
            defeat_thresholds.clear();
            reinforcement_factions.clear();
            difficulty = 0;
        }
        public void ClearAndDespawnFleets(long now) {
            for (CampaignFleetAPI fleet : fleets) {
                fleet.despawn();
            }
            ClearFleets();
            if (now > Long.MIN_VALUE) {
                fleet_expiration_ts = roll_expires_ts > now ? roll_expires_ts : now + 2L * INVASION_TIME_PERIOD_DAYS;
            }
        }

        public static long pickNewTs(long now, float time_periods, Random random) {
            double factor = random == null ? 1.0 : 0.85 + 0.3 * random.nextDouble();
            double delay_days = time_periods * INVASION_TIME_PERIOD_DAYS * factor;
            return now + Math.round(delay_days * MILLISECONDS_PER_DAY);
        }

        // Representing a string representing timestamp ts.
        public static String getDaysFromNowStr(long now, long ts) {
            return OrbitalMissionBase.getDaysFromNowStr(now, ts);
        }

        // Check the roll results, re-roll if necessary.
        public boolean checkRollResultAndReRoll(boolean is_player_owned, Random random) {
            long now = Global.getSector().getClock().getTimestamp();

            boolean has_guaranteed = INVASION_PROB > 0f;
            float guaranteed_periods = 0f;
            long guaranteed_end_ts = Long.MIN_VALUE;
            if (has_guaranteed) {
                guaranteed_periods = Math.min(Math.max(1f / INVASION_PROB - 1f, 0f), 10000f) * 2;
                if (guaranteed_start_ts == 0 || guaranteed_start_ts == Long.MIN_VALUE) {
                    guaranteed_start_ts = pickNewTs(now, guaranteed_periods + 1, random);
                    guaranteed_end_ts = pickNewTs(guaranteed_start_ts, 2, null);
                }
            }

            Boolean result = null;
            Float roll = null;
            if (now < roll_expires_ts) {  // previous roll is still in effect
                result = roll_result;
            } else if (roll_result) {  // previous roll succeeds but has expired
                long no_invasion_ends = pickNewTs(roll_expires_ts, 1, random);
                if (now < no_invasion_ends) {
                    result = false;
                }
            }

            if (result == null) {  // Needs to roll, but also check guaranteed invasion window
                if (has_guaranteed) {
                    guaranteed_end_ts = pickNewTs(guaranteed_start_ts, 2, null);
                    while (now >= guaranteed_end_ts) {
                        guaranteed_start_ts = pickNewTs(guaranteed_end_ts, guaranteed_periods, null);
                        guaranteed_end_ts = pickNewTs(guaranteed_start_ts, 2, null);
                    }
                    if (now >= guaranteed_start_ts) {
                        result = true;
                    }
                }

                if (result == null) {  // Needs to re-roll
                    if (Global.getSettings().isDevMode()) {
                        roll = -1f;
                    } else {
                        roll = random.nextFloat();
                    }
                    // Increased invasion chance for player faction.
                    result = roll_result = roll <= (is_player_owned ? (INVASION_PROB + 1f) / 2 : INVASION_PROB);
                    roll_expires_ts = pickNewTs(now, 1, random);
                    if (result && has_guaranteed) {
                        guaranteed_start_ts = pickNewTs(roll_expires_ts, guaranteed_periods + 1, random);
                    }
                }
            }

            log.debug(MessageFormat.format(
                    "checkRollResultAndReRoll: invasion={0}, roll={1}, roll_result={2}; roll_expires_ts={3}, " +
                            "guaranteed_start_ts={4}, guaranteed_end_ts={5}, fleet_expiration_ts={6}",
                    result, roll, roll_result, getDaysFromNowStr(now, roll_expires_ts),
                    getDaysFromNowStr(now, guaranteed_start_ts), getDaysFromNowStr(now, guaranteed_end_ts),
                    getDaysFromNowStr(now, fleet_expiration_ts)));
            return result;
        }

        // Called on accept to ensure that checkRollResultAndReRoll returns false and no re-rolls.
        public void lockMarket() {
            roll_result = false;
            roll_expires_ts = Long.MAX_VALUE;
//            cool_down_end_ts = Long.MAX_VALUE;
        }

        // Called on endFailureImpl to unlock the market.
        public void unlockMarket(Random random) {
            roll_expires_ts = pickNewTs(Global.getSector().getClock().getTimestamp(), 1, random);
        }
    }

    // Obtains all faction ids, with no duplicates, such that those appearing in preferred appears first in the provided
    // order.
    public static List<String> getAllFactionIds(List<String> preferred) {
        HashSet<String> existing = new HashSet<>();
        ArrayList<String> order = new ArrayList<>();
        if (preferred != null) {
            for (String id : preferred) {
                if (existing.add(id)) {
                    order.add(id);
                }
            }
        }

        for (FactionAPI faction : Global.getSector().getAllFactions()) {
            if (existing.add(faction.getId())) {
                order.add(faction.getId());
            }
        }
        return order;
    }

    // Find a faction for the enemy fleet, and check that the market is not hostile to you.
    public static String pickEnemyFaction(FactionAPI market_faction) {
        // Must be hostile with the enemy faction but not hostile with the defending faction or else you won't be able
        // to join the battle.
        FactionAPI player_faction = Global.getSector().getPlayerFaction();
        if (market_faction.isHostileTo(player_faction)) {
            log.debug("Relationship with " + market_faction.getDisplayName() + " is hostile");
            return null;
        }

        // The enemy faction must also be hostile to both the player and market.
        List<String> faction_ids = getAllFactionIds(
                Arrays.asList(Factions.PIRATES, Factions.REMNANTS, Factions.OMEGA, Factions.LUDDIC_PATH));
        for (String faction_id :faction_ids) {
            if (player_faction.isHostileTo(faction_id) && market_faction.isHostileTo(faction_id)) {
                return faction_id;
            }
        }
        log.error("Cannot find an enemy faction.");
        return null;
    }

    // If the market is eligible for this mission right now, returns the station fleet.
    public static CampaignFleetAPI getStationIfEligible(MarketAPI market, Random random) {
        if (random == null || market == null) return null;  // sanity check

        if (pickEnemyFaction(market.getFaction()) == null) return null;

        // Check to ensure that the market has a station industry and fleet and is functional.
        Industry industry = Misc.getStationIndustry(market);
        if (industry == null) {
//            log.debug(market.getName() + " market has no station");
            return null;
        }
        if (!industry.isFunctional()) {
//            log.debug(market.getName() + " station industry is not functional.");
            return null;
        }

        CampaignFleetAPI fleet = Misc.getStationFleet(market);
        if (fleet == null) {
            log.error(market.getName() + " station fleet is missing.");
            return null;
        }

        // Check invasion timestamps.
        MissionData data = getMissionData(market);
        if (!data.checkRollResultAndReRoll(market.isPlayerOwned(), random)) {
//            log.debug("No invasions at " + market.getName() + " this time.");
            return null;
        }

        return fleet;
    }

    @Override
    public boolean shouldShowAtMarket(MarketAPI market) {
//        log.debug("shouldShowAtMarket called for " + market.getName());
        // Get new random so contact/bar missions are consistent.
        return getStationIfEligible(market, OrbitalMissionBase.getRandom(MISSION_ID, market)) != null;
    }

    @Override
    protected boolean create(MarketAPI createdAt, boolean barEvent) {
        String id = getMissionId();
//        log.debug((barEvent ? "bar" : "contact") + " event; create called with mission_id=" + id);
        if (!id.equals(MISSION_ID)) {
            log.error("Unexpected mission id: " + id);
            return false;
        }

        station = getStationIfEligible(createdAt, getGenRandom());
        if (station == null) return false;  // market not eligible

        if (barEvent) OrbitalMissionBase.createBarGiver(this, createdAt);

        PersonAPI person = getPerson();
        Industry industry = Misc.getStationIndustry(createdAt);
        // sanity check
        if (person == null || person.getMarket() != createdAt || industry == null || station.getStarSystem() == null ||
                createdAt.getStarSystem() == null) {
            return false;
        }

        // Make sure the faction matches
        if (person.getFaction() != createdAt.getFaction()) {
            log.error(MessageFormat.format(
                    "{0} is in {1} faction instead of {2}",
                    person.getNameString(), person.getFaction().getDisplayName(),
                    createdAt.getFaction().getDisplayName()));
            return false;
        }

        // Only the station commander can give out this mission.
        if (!person.getPostId().equals(Ranks.POST_STATION_COMMANDER)) {
            log.error(person.getNameString() + " is not the station commander");
            return false;
        }

        // This line, among other things, prevents the same person from accepting multiple missions, which effectively
        // prevents concurrent invasions on the same market as long as the same person is picked.
        final String KEY = "$mmm_dm_ref";
        if (!setPersonMissionRef(person, KEY)) {
            log.debug(KEY + " already set for " + person.getNameString());
            return false;
        }

        genMissionRewardMultAndQuality();

        // The way MilitaryCustomBounty works is that everytime you leave the bar without accepting the mission, the
        // fleet is removed, and the same fleet is regenerated again the next time you re-entire the bar. We cannot
        // generate the exact same fleet because we use MagicLib to generate the fleet and it doesn't allow us to pass
        // in a PRNG or seed. If we create the fleet on ACCEPT then we cannot show accurate intel in the mission
        // briefing, so we will instead create the fleet but move it away and disable it, then move it back after we're
        // within 1 LY of the system after ACCEPT.
        mission_data = getMissionData(createdAt);
//        if (mission_data == null) return false;  // sanity check

        if (!createEnemyFleets(mission_data, station, getGenRandom())) return false;  // Failed to generate a fleet
        if (mission_data.fleets.isEmpty()) return false;  // sanity check

        // set our starting, success and failure stages
        setStartingStage(Stage.ACCEPTED);
        setSuccessStage(Stage.COMPLETED);
        setFailureStage(Stage.FAILED);

        // Get into the system within eta days, or else you fail the mission.
        CampaignFleetAPI player_fleet = Global.getSector().getPlayerFleet();
        float distance = Misc.getDistance(createdAt.getPrimaryEntity().getLocationInHyperspace(),
                player_fleet.getLocationInHyperspace()) * 1.1f;
        float eta = distance / player_fleet.getTravelSpeed() / Global.getSector().getClock().getSecondsPerDay() + 5f;
        setTimeLimit(Stage.FAILED, eta, createdAt.getStarSystem());

        connectWithEnteredLocation(Stage.ACCEPTED, Stage.IN_SYSTEM, createdAt.getContainingLocation());
        // Once you have entered the system, you win if the station is still alive/not disrupted in 20 days.
        connectWithDaysElapsed(Stage.IN_SYSTEM, Stage.COMPLETED, MAX_DEFEND_DAYS);
        connectWithCustomCondition(Stage.IN_SYSTEM, Stage.FAILED, new StationDisruptedChecker(station, industry));
        // If the enemy fleets are all defeated, you also win; this check should be added after StationDisruptedChecker
        // so that if both happens in the same battle StationDisruptedChecker has priority.
        connectWithCustomCondition(Stage.IN_SYSTEM, Stage.COMPLETED,
                new FleetsDefeatedChecker(mission_data.fleets, mission_data.defeat_thresholds));

        addTag(Tags.INTEL_BOUNTY);
        addTag(Tags.INTEL_MILITARY);

        // mission rewards
        int station_fp = getFleetStrength(station);
        int adjusted_fp = Math.round(station_fp * STATION_FP_REWARD_REDUCTION);
        setCreditRewardApplyRelMult(Math.max(
                MIN_CREDIT_REWARD,
                CREDIT_REWARD_PER_FP * (getFleetStrength(mission_data.fleets) - adjusted_fp)));
        setRepPersonChangesHigh();
        setRepFactionChangesMedium();

        // When we're within 1LY wakes up the enemy fleet AI and move them to SPAWN_DISTANCE around the station.
        beginWithinHyperspaceRangeTrigger(createdAt, 1f, false,
                Stage.ACCEPTED, Stage.IN_SYSTEM);
        triggerCustomAction(new WakeUpEnemyFleetsAction(mission_data.fleets, station, getGenRandom()));
        endTrigger();

        return true;
    }

    protected void makeFleetGoAway(CampaignFleetAPI fleet) {
        log.debug("makeFleetGoAway called");
        if (!fleet.isAlive()) return;

        SectorEntityToken target = Misc.findNearestJumpPoint(fleet);
        if (fleet.getCurrentAssignment() != null && fleet.getCurrentAssignment().getTarget() == target) {
            return;
        }
        fleet.clearAssignments();
        fleet.addAssignment(FleetAssignment.STANDING_DOWN, null,
                0.5f + 0.5f * (float) Math.random());
        String actionText = "leaving system";
        // Less likely to be distracted by hostile fleets then GO_TO_LOCATION_AND_DESPAWN.
        fleet.addAssignment(FleetAssignment.DELIVER_MARINES, target, 1000f, actionText);
        fleet.addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, target, 1000f, actionText);
        makeUnimportant(fleet);
    }


    @Override
    protected void endSuccessImpl(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
        // Update difficulty if successful.
        int difficulty = getOrIncreaseDifficulty(mission_data.difficulty);
        // Assuming defaultPotentialContactProbability is 0.25f, and bar giver has 0 relation before mission,
        // then this results in 50% for non-player faction and 75% for player faction.
        OrbitalMissionBase.addPotentialContact(this, dialog, 0.67f, 0.36f);
        endFailureImpl(dialog, memoryMap);
        log.info("Mission success; new difficulty=" + difficulty);
    }

    @Override
    protected void endFailureImpl(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
        // Mission is done; make the fleet go away then despawn itself
        for (CampaignFleetAPI fleet : mission_data.fleets) {
            makeFleetGoAway(fleet);
            // Note that we cannot do this in reportBattleOccurred since we will run into concurrent list modification
            // error.
            fleet.removeEventListener(this);
        }
        // Note that mission_data is put into the MemoryAPI, so we reuse it for later missions.
        mission_data.ClearFleets();
        mission_data.unlockMarket(getGenRandom());
    }

    protected static String getStationDesc(CampaignFleetAPI station) {
        for (FleetMemberAPI member : station.getFleetData().getMembersListCopy()) {
            if (member.isStation()) return member.getVariant().getFullDesignationWithHullName();
        }
        return "station";
    }

    // showBountyDetail is defined in rules.csv for the mmm_dmShowBounty/mmm_dmShowBountyBar rules.
    @Override
    protected boolean callAction(String action, String ruleId, InteractionDialogAPI dialog, List<Token> params,
                                 Map<String, MemoryAPI> memoryMap) {
        // showBountyDetail is in triggers column in rules.csv
        if ("showBountyDetail".equals(action) && mission_data.fleets != null &&
                !mission_data.fleets.isEmpty()) {
            final int cols = 7;
            final float opad = 10f;
            final float iconSize = (float) 440 / cols;
            final Color h = Misc.getHighlightColor();

            int num_ships = 0;
            ArrayList<ArrayList<FleetMemberAPI>> members_list = new ArrayList<>();
            for (CampaignFleetAPI fleet : mission_data.fleets) {
                ArrayList<FleetMemberAPI> members = new ArrayList<>();
                for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
                    if (!member.isFighterWing()) {
                        ++num_ships;
                        if (members.size() < cols) {
                            members.add(member);
                        }
                    }
                }
                members_list.add(members);
            }

            String s_or = mission_data.fleets.size() > 1 ? "s" : "";
            String num_fleets_str = String.valueOf(mission_data.fleets.size());
            String num_ships_str = String.valueOf(num_ships);
            String enemy_str = String.valueOf(getFleetStrength(mission_data.fleets));
            String player_str = String.valueOf(getPlayerFleetStrength());
            String station_str = String.valueOf(Math.round(station.getEffectiveStrength()));
            Color station_color = station.getFaction().getBaseUIColor();
            String station_desc = getStationDesc(station);

//            updateInteractionData(dialog, memoryMap);
            TextPanelAPI text = dialog.getTextPanel();

            TooltipMakerAPI info = text.beginTooltip();
            info.setParaSmallInsignia();
            info.addPara(Misc.ucFirst(getPerson().getHeOrShe()) + " taps a data pad, and " +
                    "an intel assessment shows up on your tripad.", 0f);
            int num_rows = Math.min(MAX_FLEETS_TO_SHOW, members_list.size());
            for (int row = 0; row < num_rows; ++row) {
                // baseColor here doesn't seems to do anything.
                info.addShipList(cols, 1, iconSize, h, members_list.get(row), opad);
            }

            ArrayList<String> highlights = new ArrayList<>();
            ArrayList<Color> hl_colors = new ArrayList<>();
            StringBuilder ship_desc = new StringBuilder(
                    mission_data.reinforcement_factions.size() > 1 ? "a mixture of " : "");
            for (int i = 0; i < mission_data.reinforcement_factions.size(); ++i) {
                FactionAPI faction = Global.getSector().getFaction(mission_data.reinforcement_factions.get(i));
                String display_name = faction.getDisplayName();
                highlights.add(display_name);
                hl_colors.add(faction.getBaseUIColor());

                if (i == 0) {
                    ship_desc.append(display_name);
                } else if (i == mission_data.reinforcement_factions.size() - 1) {
                    ship_desc.append(" and ").append(display_name);
                } else {
                    ship_desc.append(", ").append(display_name);
                }
            }
            highlights.addAll(Arrays.asList(num_fleets_str, num_ships_str, enemy_str, player_str, station_desc,
                    station_str));
            hl_colors.addAll(Arrays.asList(h, h, h, h, station_color, h));

            String msg =
                    "\"According military intelligence, the enemy fields {0} ships. They should have {1} fleet{2} " +
                    "containing {3} ships with a combined strength of {4} points. For comparison your " +
                    "ships have {5} points, while our {6} has {7} points.\"";
            info.addPara(
                    MessageFormat.format(msg, ship_desc.toString(), num_fleets_str, s_or, num_ships_str, enemy_str,
                            player_str, station_desc, station_str),
                    opad, hl_colors.toArray(new Color[0]), highlights.toArray(new String[0]));

            String days_str = String.valueOf(MAX_DEFEND_DAYS);
            msg = "\"Your mission is to defeat the enemy fleet{0} or protect our {1} for {2} days. You up for it?\"";
            info.addPara(MessageFormat.format(msg, s_or, station_desc, days_str), opad, h, days_str);
            text.addTooltip();
            return true;
        }
        return super.callAction(action, ruleId, dialog, params, memoryMap);
    }

    @Override
    protected void updateInteractionDataImpl() {
        // Used by rules.csv
        set("$mmm_dm_reward", Misc.getDGSCredits(getCreditsReward()));
        if (mission_data.fleets.size() > 1) {
            set("$mmm_dm_fleet_desc_bar", "fairly powerful fleets");
            set("$mmm_dm_fleet_desc_contact", "are fairly powerful fleets");
        } else {
            set("$mmm_dm_fleet_desc_bar", "a fairly powerful fleet");
            set("$mmm_dm_fleet_desc_contact", "is a fairly powerful fleet");
        }
    }

    @Override
    public void accept(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
        if (station == null || mission_data == null || mission_data.fleets.isEmpty()) return;  // sanity check

        mission_data.lockMarket();
        for (CampaignFleetAPI fleet : mission_data.fleets) {
            // Making the fleet important also prevents it from being despawned.
            makeImportant(fleet, "$mmm_dm_target", Stage.ACCEPTED, Stage.IN_SYSTEM);
            fleet.addEventListener(this);

//            fleet.clearAssignments();
//            // Less likely to be distracted by hostile fleets then ATTACK_LOCATION
//            fleet.addAssignment(FleetAssignment.DELIVER_MARINES, station, 1000f, actionText);
//            fleet.addAssignment(FleetAssignment.ATTACK_LOCATION, station, 1000f, actionText);
            // Remove faction impact.
            fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_NO_REP_IMPACT, true);
            fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_LOW_REP_IMPACT, true);

            if (ENEMY_IGNORED_BY_OTHER_FLEETS) {
                setFlag(fleet, MemFlags.FLEET_IGNORED_BY_OTHER_FLEETS, false, Stage.ACCEPTED, Stage.IN_SYSTEM);
            }

            // Adds some extra supplies to the cargo since the enemy sometimes drops very little
            CargoAPI cargo = fleet.getCargo();
            cargo.addSupplies(Math.min(fleet.getFleetPoints() * PLAYER_FP_TO_SUPPLIES_RATIO, cargo.getMaxCapacity()));
        }
        log.info(MessageFormat.format(
                "ACCEPTED: enemy_points={0}, player_points={1}, station_points={2}, credit_rewards={3}" +
                        ", defeat_thresholds=",
                getFleetStrength(mission_data.fleets), getPlayerFleetStrength(), getFleetStrength(station),
                getCreditsReward()) + mission_data.defeat_thresholds.toString());
        super.accept(dialog, memoryMap);
        OrbitalMissionBase.acceptCommon(MISSION_ID, getPerson());
    }

    @Override
    public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {
        int index = mission_data.fleets.indexOf(fleet);
        int fp = getFleetStrength(fleet);
        int defeat_threshold = index >= 0 ? mission_data.defeat_thresholds.get(index) : 0;
        boolean defeated = index >= 0 && fp <= defeat_threshold;
        log.debug(MessageFormat.format(
                "reportBattleOccurred: {0}, defeated={1}, fp={2}, defeat_threshold={3}",
                fleet.getName(), defeated, fp, defeat_threshold));
        if (defeated) {
            makeFleetGoAway(fleet);
        }
    }
    // Needed so subclass is not abstract
    @Override
    public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, CampaignEventListener.FleetDespawnReason reason,
                                               Object param) {}

    // where on the map the intel screen tells us to go
    @Override
    public SectorEntityToken getMapLocation(SectorMapAPI map) {
        return station;
    }

    // mission name on intel screen
    @Override
    public String getBaseName() {
        return station == null ? "" : station.getNameWithFaction() + " Defense";
    }

    // Needed to display intel correctly.
    @Override
    protected void addBulletPoints(TooltipMakerAPI info, ListInfoMode mode) {
        // adapted from BaseHubMission.addBulletPoints
        bullet(info);

        boolean display_reward = false;
        boolean display_steps = false;
        boolean is_finished = isFailed() || isSucceeded();
        // Not really sure what this block does; but it seems to work
        if (getListInfoParam() != null) {
            if (is_finished) {
                addResultBulletsAssumingAlreadyIndented(info, mode);
            } else {
                display_steps = true;
            }
        } else if (getResult() != null) {
            if (mode == ListInfoMode.IN_DESC) addResultBulletsAssumingAlreadyIndented(info, mode);
        } else {
            display_reward = mode == ListInfoMode.IN_DESC;
            display_steps = !is_finished;
        }

        float pad = 3f;
        Color h = Misc.getHighlightColor();
        Color text_color = getBulletColorForMode(mode);

        if (display_reward) {
            info.addPara("%s reward", pad, text_color, h, Misc.getDGSCredits(getCreditsReward()));
            pad = 0f;
        }

        if (display_steps && station != null && timeLimit != null) {  // sanity checks
            Stage stage = (Stage) getCurrentStage();
            // This block is needed because it takes a bit of time to reach the IN_SYSTEM stage even if we start in
            // the same system.
            StarSystemAPI system = Global.getSector().getPlayerFleet().getStarSystem();
            if (system != null && system.equals(station.getStarSystem())) {
                stage = Stage.IN_SYSTEM;
            }

            int arrive_days = Math.max(1, Math.round(timeLimit.days - elapsed));
            int defend_days = 0;
            for (StageConnection connection : connections) {
                if (connection.from == Stage.IN_SYSTEM && connection.checker instanceof DaysElapsedChecker) {
                    float days = ((DaysElapsedChecker) connection.checker).days - getData(Stage.IN_SYSTEM).elapsed;
                    defend_days = Math.max(1, Math.round(days));
                }
            }

            if (stage != Stage.IN_SYSTEM) {
                String s_or = arrive_days > 1 ? "s" : "";
                String d = String.valueOf(arrive_days);
                String name = station.getStarSystem().getNameWithLowercaseTypeShort();
                info.addPara(MessageFormat.format("Arrive at {0} in {1} day{2}.", name, d, s_or),
                        pad, text_color, h, name, d);
                pad = 0f;
            }

            String s_or = defend_days > 1 ? "s" : "";
            String d = String.valueOf(defend_days);
            String fleets_desc = "the enemy fleet";
            if (mission_data.fleets.size() == 2) {
                fleets_desc = "both enemy fleets";
            } else if (mission_data.fleets.size() > 2) {
                fleets_desc = MessageFormat.format("all {0} enemy fleets", mission_data.fleets.size());
            }
            info.addPara(MessageFormat.format(
                    "Defend {0} for {1} day{2} or destroy {3} without leaving the system.",
                            station.getName(), d, s_or, fleets_desc),
                    pad, text_color, h, station.getName(), d);
        }
        unindent(info);
    }

    // Get or set the difficulty, returning 0 if no previous difficulty was set; NOOP if the previous difficulty is
    // already greater.
    public static int getOrIncreaseDifficulty(Integer difficulty) {
        MemoryAPI memory = Global.getSector().getMemoryWithoutUpdate();
        Integer p = (Integer) memory.get(DIFFICULTY_KEY);

        if (difficulty != null && difficulty > 0) {
            int new_difficulty = Math.max(p == null ? 0 : p, difficulty);
            memory.set(DIFFICULTY_KEY, new_difficulty);
            return new_difficulty;
        }
        return p == null ? 0 : p;
    }

    public static int getFleetStrength(CampaignFleetAPI fleet) {
        return OrbitalMissionBase.getFleetStrength(fleet);
    }
    public static int getFleetStrength(List<CampaignFleetAPI> fleets) {
        return OrbitalMissionBase.getFleetStrength(fleets);
    }
    public static int getPlayerFleetStrength() {
        return OrbitalMissionBase.getPlayerFleetStrength();
    }

    public static List<String> pickReinforcementFactions(Random random) {
        // Prioritise vanilla factions
        List<String> faction_ids = getAllFactionIds(Arrays.asList(
                Factions.DERELICT, Factions.DIKTAT, Factions.HEGEMONY, Factions.INDEPENDENT, Factions.KOL,
                Factions.LIONS_GUARD, Factions.LUDDIC_CHURCH, Factions.LUDDIC_PATH, Factions.MERCENARY,
                Factions.OMEGA, Factions.PERSEAN, Factions.PIRATES, Factions.REMNANTS, Factions.TRITACHYON,
                Factions.PLAYER));

        // Add any faction that has ships and has a different ship set from an existing one and isn't in the
        // blacklist. Also skip hidden factions (not shown in intel tab), if the USE_HIDDEN_FACTIONS setting is off,
        // as well as factions without markets.
        HashSet<String> factions_with_markets = new HashSet<>();
        for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
            factions_with_markets.add(market.getFactionId());
        }

        ArrayList<String> candidate_factions = new ArrayList<>();
        // We use hashCode instead of TreeSet<String> to save memory.
        HashSet<Integer> ships_set = new HashSet<>();
        for (String faction_id : faction_ids) {
            if (FACTION_BLACKLIST.contains(faction_id)) continue;
            FactionAPI faction = Global.getSector().getFaction(faction_id);
            if (!USE_HIDDEN_FACTIONS && !FACTION_WHITELIST.contains(faction_id)) {
                if (faction_id.equals(Factions.PLAYER) || !faction.isShowInIntelTab() ||
                        !factions_with_markets.contains(faction.getId())) {
                    continue;
                }
            }

            if (!faction.getKnownShips().isEmpty() && ships_set.add(faction.getKnownShips().hashCode())) {
                candidate_factions.add(faction_id);
            }
        }
        log.debug("eligible reinforcement factions: " + candidate_factions);

        WeightedRandomPicker<String> picker = new WeightedRandomPicker<>(random);
        picker.addAll(candidate_factions);

        ArrayList<String> result = new ArrayList<>();
        while (!picker.isEmpty()) {
            result.add(picker.pickAndRemove());
            if (random.nextFloat() <= SINGLE_FACTION_PROB) break;
        }
        log.debug("pickReinforcementFactions picked: " + result);
        return result;
    }

    // Compute the target strength per enemy fleet.
    public static int computeFleetSize(int target_fp, int player_fp, int station_fp, Random random) {
        int fp_min = Math.max(MIN_ENEMY_FP_PER_FLEET, Math.round(player_fp * 0.6f));
        int fp_max = Math.max(fp_min, (player_fp + station_fp) / 2);
        int fleets_max = Math.max(1, target_fp / fp_min);
        int fleets_min = Math.max(1, Math.min(fleets_max, (int) Math.round(Math.ceil((float) target_fp / fp_max))));
        log.debug(MessageFormat.format(
                "computeFleetSize: target_fp={0}, player_fp={1}, station_fp={2}, fp range: ({3}, {4}) " +
                        "fleets range: ({5}, {6})",
                target_fp, player_fp, station_fp, fp_min, fp_max, fleets_min, fleets_max));
        int num = fleets_min;
        if (fleets_max > fleets_min) {
            num = fleets_min + random.nextInt(target_fp / fp_min - fleets_min);
        }
        SharedData.getData().getPersonBountyEventData().isParticipating("1");
        return target_fp / num;
    }

    // Create or return DefenseMissionData associated with the market.
    public static MissionData getMissionData(MarketAPI market) {
        // We want to save the fleet so the next call to createEnemyFleets does not generate a new one. A class member
        // does not work since every visit to the bar creates a new instance of DefenseMission, while a class static
        // member also does not work since the data won't survive save/load. Instead we will need to use the MemoryAPI
        // and keep the fleet data on the target station.
        MemoryAPI memory = market.getMemoryWithoutUpdate();
        MissionData data = (MissionData) memory.get(MISSION_DATA_KEY);
        if (data == null) {
            data = new MissionData();
            // Use a new Random to avoid save-scumming, since MissionData would already be written the next time.
            memory.set(MISSION_DATA_KEY, data);
        }
        return data;
    }

    // Actually creates a fleet targeting station with possibly multiple attempts.
    public static OrbitalMissionBase.CreateFleetResult createFleet(
            CampaignFleetAPI station, String reinforcement_faction, int attempts, int expected_fp,
            float input_fp_per_output) {
        // Store the fleet as the neutral faction so they don't take over the local comm relay etc.
        OrbitalMissionBase.CreateFleetResult result = OrbitalMissionBase.createFleet(
                station, station.getName() + " Invasion Fleet", Factions.NEUTRAL, reinforcement_faction,
                attempts, expected_fp, input_fp_per_output);

        // Send the fleet away and don't allow it to move.
        if (result.fleet != null) {
            result.fleet.setDoNotAdvanceAI(true);
            result.fleet.setLocation(-35000, -35000);
        }
        return result;
    }

    // Create enemy fleets with the provided strength attacking the target station, but send it away and disable AI.
    // Adds the fleets sorted by fleet strength (largest first).
    public static boolean createEnemyFleets(MissionData data, CampaignFleetAPI station, Random random) {
        // First Check if the existing fleet can be re-used.
        boolean generate_new_fleets = false;
        if (data.fleets.isEmpty()) {
            log.debug("No existing fleets found; generating new fleet");
            generate_new_fleets = true;
        } else if (Global.getSector().getClock().getTimestamp() >= data.fleet_expiration_ts) {
            log.debug("fleet too old; generating new fleet");
            generate_new_fleets = true;
        } else {
            for (CampaignFleetAPI fleet : data.fleets) {
                if (!fleet.isAlive()) {
                    log.debug("existing fleet not alive; generating new fleet");
                    generate_new_fleets = true;
                    break;
                }
            }
        }

        if (!generate_new_fleets) return true;
        long now = Global.getSector().getClock().getTimestamp();
        data.ClearAndDespawnFleets(now);  // Now fleets are empty.

        // Computes expected strength of the new fleet from the strength of the station and the strength of the player.
        int player_fp = getPlayerFleetStrength();
        int station_fp = getFleetStrength(station);
        int difficulty = getOrIncreaseDifficulty(null);
        int min_points = player_fp + station_fp;
        int max_points = station_fp + Math.max(player_fp, Math.round(difficulty * DIFFICULTY_GROWTH));
        int target_fp = min_points + random.nextInt(max_points - min_points + 1);
        int fp_per_fleet = computeFleetSize(target_fp, player_fp, station_fp, random);

        log.debug(MessageFormat.format(
                "createEnemyFleets: player_fp={0}, station_fp={1}, fp_range=({2}, {3}), fp_per_fleet={4}" +
                       ", target_fp={5}, difficulty={6}",
                player_fp, station_fp, min_points, max_points, fp_per_fleet, target_fp, difficulty));

        // Now generates the fleets. Unfortunately the minFP parameter in MagicFleetBuilder is more of a suggestion;
        // it's neither a minimum nor a maximum, so we may get ships with more or less fleet strength then expected.
        List<String> reinforcement_factions = pickReinforcementFactions(random);
        if (reinforcement_factions.isEmpty()) return false;
        HashMap<CampaignFleetAPI, String> fleet_to_faction = new HashMap<>();

        // In general 1 fleet point passed to MagicFleetBuilder will result in greater than 1 output fleet strength.
        float input_fp_per_output = 0.68f;
        Float strength_ratio = FACTION_STRENGTH_RATIO.get(reinforcement_factions.get(0));
        if (strength_ratio != null && strength_ratio > 0f) {
            input_fp_per_output /= strength_ratio;
        }

        int actual_fp = 0;
        int remaining = target_fp;
        int failed = 0;
        for (int i = 0; remaining >= fp_per_fleet * 2 / 3; ++i) {
            // Try not to generate a bunch of large fleets then 1 small one, which would be annoying to hunt down;
            // divide the remaining strength evenly, but no smaller than fp_per_fleet.  Also try extra hard to generate
            // the fleets with the correct sizes if this is the last 2 fleets (2 extra attempts).
            int num = Math.max(1, (int) Math.round(Math.floor((double) remaining / fp_per_fleet)));
            int expected_fp = remaining / num;
            // If we switch factions give it 1 more attempt, since the ratio might have changed.
            int attempts = (i == 0 || reinforcement_factions.size() > 1) ? 2 : 1;
            if (num <= 2) {
                attempts += 2;
            }

            String reinforcement_faction = reinforcement_factions.get(i % reinforcement_factions.size());
            OrbitalMissionBase.CreateFleetResult result = createFleet(station, reinforcement_faction, attempts,
                    expected_fp, input_fp_per_output);
            input_fp_per_output = result.input_fp_per_output;

            if (result.fleet == null) {  // MagicLib failed to find a fleet
                failed += 1;
                if (failed > 3) {  // Give up to avoid infinite loop
                    data.ClearAndDespawnFleets(now);
                    log.error(MessageFormat.format("Failed to create fleet with {0} strength.", expected_fp));
                    return false;
                }
                continue;
            }

            data.fleets.add(result.fleet);
            fleet_to_faction.put(result.fleet, reinforcement_faction);
            actual_fp += getFleetStrength(result.fleet);
            remaining = target_fp - actual_fp;
            failed = 0;
        }

        if (data.fleets.size() > 1) {
            // Reorder the fleets according to strength and rename them.
            Collections.sort(data.fleets, new Comparator<CampaignFleetAPI>() {
                @Override
                public int compare(CampaignFleetAPI o1, CampaignFleetAPI o2) {
                    return Math.round(o1.getEffectiveStrength() - o2.getEffectiveStrength());
                }
            });
            Collections.reverse(data.fleets);
            for (int i = 0; i < data.fleets.size(); ++i) {
                data.fleets.get(i).setName(station.getName() + " Invasion Fleet " + (i + 1));
            }
        }

        StringBuilder buf = new StringBuilder(MessageFormat.format(
                "Created {0} fleets with {1} total strength ({2} target):",
                data.fleets.size(),  actual_fp, target_fp));
        // Update associated values in MissionData
        data.difficulty = Math.max(actual_fp - station_fp, player_fp);
        // Compute defeat threshold and total rewards
        for (CampaignFleetAPI fleet : data.fleets) {
            data.defeat_thresholds.add(Math.round(getFleetStrength(fleet) * FLEET_DEFEAT_RATIO));
            String faction = fleet_to_faction.get(fleet);
            if (!data.reinforcement_factions.contains(faction)) {
                data.reinforcement_factions.add(faction);
            }
            buf.append(MessageFormat.format(
                    " (r={0}, strength={1})", faction, getFleetStrength(fleet)));
        }
        log.info(buf.toString());
        return true;
    }
}