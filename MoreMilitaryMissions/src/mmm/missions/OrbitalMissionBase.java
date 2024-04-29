package mmm.missions;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.DebugFlags;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BarEventManager;
import com.fs.starfarer.api.impl.campaign.intel.contacts.ContactIntel;
import com.fs.starfarer.api.impl.campaign.intel.contacts.ContactIntel.ContactState;
import com.fs.starfarer.api.impl.campaign.missions.hub.BaseHubMission;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithBarEvent;
import com.fs.starfarer.api.impl.campaign.shared.PlayerActivityTracker;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import com.fs.starfarer.api.impl.campaign.submarkets.BaseSubmarketPlugin;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import mmm.Utils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.magiclib.campaign.MagicFleetBuilder;
import org.magiclib.util.MagicSettings;

import java.text.MessageFormat;
import java.util.*;

public abstract class OrbitalMissionBase extends HubMissionWithBarEvent {
    public static final String MOD_ID = Utils.MOD_ID;
    private static final Logger log = Global.getLogger(OrbitalMissionBase.class);
    static {
        if (Utils.DEBUG) {
            log.setLevel(Level.ALL);
        }
    }

    // Memory flags and constants
    public static final String STRENGTH_RATIO_KEY = "$mmm_dm_strength_ratio";
    public static final float TRADE_IMPACT_DAYS = BaseSubmarketPlugin.TRADE_IMPACT_DAYS;

    // Settings
    // Beware of overflow if you convert this to float instead of double.
    public static final long MILLISECONDS_PER_DAY = 86400000L;
    public static final Map<String, Float> FACTION_STRENGTH_RATIO =
            MagicSettings.getFloatMap(MOD_ID, "DmFactionStrengthRatio");
    // Changes the effective fleet strength of the station for enemy strength calculation. This smaller this ratio,
    // the weaker the enemy is initially.
    public static float STATION_EFFECTIVE_FP_RATIO;

    public static boolean isActiveContact(PersonAPI person) {
        ContactIntel intel = ContactIntel.getContactIntel(person);
        return intel != null &&
                (intel.getState() == ContactState.NON_PRIORITY || intel.getState() == ContactState.PRIORITY);
    }

    // Compare 2 person by relationships with player, whether they're in the comm directory or not, and whether they
    // are contacts.
    public static class ComparePersonByRel implements Comparator<PersonAPI> {
        public CommDirectoryAPI dir;
        public ComparePersonByRel(MarketAPI market) {
            dir = market.getCommDirectory();
        }

        @Override
        public int compare(PersonAPI o0, PersonAPI o1) {
            int priority0 = Math.round(1000 * (o0.getRelToPlayer().getRel()));
            int priority1 = Math.round(1000 * (o1.getRelToPlayer().getRel()));

            CommDirectoryEntryAPI entry0 = dir.getEntryForPerson(o0);
            CommDirectoryEntryAPI entry1 = dir.getEntryForPerson(o1);
            if (entry0 != null && !entry0.isHidden()) priority0 += 4000;
            if (entry1 != null && !entry1.isHidden()) priority1 += 4000;

            priority0 += getContactScore(o0);
            priority1 += getContactScore(o1);

            return priority0 - priority1;
        }

        public static int getContactScore(PersonAPI person) {
            ContactIntel intel = ContactIntel.getContactIntel(person);
            return intel == null ? 0 : intel.getState() == ContactState.PRIORITY ?
                    32000 : intel.getState() == ContactState.NON_PRIORITY ? 16000 : 8000;
        }
    }

    // Representing a string representing timestamp ts.
    public static String getDaysFromNowStr(long now, long ts) {
        if (ts == Long.MIN_VALUE) return "-inf";
        if (ts == Long.MAX_VALUE) return "inf";
        return String.format("%.1f days", (ts - now) / (double) MILLISECONDS_PER_DAY);
    }

    // Same as getRoundNumber, but rounds number smaller than 100 to multiplies of 5, and number smaller than 10 to 10.
    public static int myGetRoundNumber(float num) {
        int exp = (int) Math.log10(num);
        if (exp <= 0) {
            return 10;
        }
        int base = exp == 1 ? 5 : (int) Math.round(Math.pow(10, exp - 1));
        return Math.round(num / base) * base;
    }


    // PRNG seed managements:
    //
    // Returns null if missionId is not a person mission.
    private static PersonAPI getPersonOrNullForMissionId(String missionId, PersonAPI person) {
        return MissionInjector.PERSON_MISSION_IDS.contains(missionId) ? person : null;
    }

    private static String getSeedKey(String missionId, PersonAPI person) {
        PersonAPI p = getPersonOrNullForMissionId(missionId, person);
        return p == null ? missionId : missionId + '|' + p.getId();
    }

    // Here only missionId cannot be null.
    private static long getNewSeed(String missionId, MarketAPI market, PersonAPI person) {
        CampaignClockAPI clock = Global.getSector().getClock();
        long mix = BarEventManager.getInstance().getSeed(market == null ? null : market.getPrimaryEntity(),
                getPersonOrNullForMissionId(missionId, person), missionId);
        return Misc.seedUniquifier() + mix + clock.getCycle() * 100L + clock.getMonth();
    }
    private static long getNewSeed(String missionId, MarketAPI market) {
        return getNewSeed(missionId, market, null);
    }

    // Ensure $mmm_mission_seeds is populated for the market and returns it.
    public static Map<String, Long> getSeedMap(MarketAPI market) {
        final String MISSION_SEEDS = "$mmm_mission_seeds";
        MemoryAPI memory = market.getMemoryWithoutUpdate();
        Map<String, Long> mapping = (Map) memory.get(MISSION_SEEDS);
        if (mapping == null) {
            mapping = new HashMap<>();
            memory.set(MISSION_SEEDS, mapping, 30f);
        }
        return mapping;
    }

    // Updates the seed for the mission so that the next mission would have a different seed; should be called on accept.
    public static void updateSeed(String missionId, MarketAPI market, PersonAPI person) {
        Map<String, Long> mapping = getSeedMap(market);
        mapping.put(getSeedKey(missionId, person), getNewSeed(missionId, market, person));
    }

    public static Random getRandom(String missionId, MarketAPI market, PersonAPI person) {
        Map<String, Long> mapping = getSeedMap(market);
        String key = getSeedKey(missionId, person);
        Long seed = mapping.get(key);
        if (seed == null) {
            seed = getNewSeed(missionId, market, person);
            mapping.put(key, seed);
        }
        return new Random(seed);
    }
    public static Random getRandom(String missionId, MarketAPI market) {
        return getRandom(missionId, market, null);
    }

    // Ensure that  $mmm_mission_seeds are populated for the relevant mission ids.
    public static void ensureSeeds(MarketAPI market) {
        Map<String, Long> mapping = getSeedMap(market);
        for (String missionId : MissionInjector.MARKET_MISSION_IDS) {
            if (!mapping.containsKey(missionId)) {
                mapping.put(missionId, getNewSeed(missionId, market));
            }
        }
        for (PersonAPI person : market.getPeopleCopy()) {
            if (!isActiveContact(person)) continue;
            for (String missionId : MissionInjector.PERSON_MISSION_IDS) {
                String seedKey = getSeedKey(missionId, person);
                if (!mapping.containsKey(seedKey)) {
                    mapping.put(seedKey, getNewSeed(missionId, market, person));
                }
            }
        }
    }

    public static long mixSeeds(long seed, long... mixes) {
        for (long mix : mixes) {
            seed += mix * 181783497276652981L;
        }
        return seed;
    }


    public static PersonAPI findStationCommander(MarketAPI market) {
        // Try to get the existing station commander instead of generating duplicates. In fact nothing prevents the
        // game can from generating multiple station commanders in the background, so we prefer the non-hidden ones in
        // the comm directory, preferring contacts. In there are ties, break them by relationship with player.
        HashSet<PersonAPI> candidates = new HashSet<>(market.getPeopleCopy());
        // For some reason the people the comm directory of in Pirate/LP outposts isn't actually in the market. It
        // probably doesn't matter in the base game since you can't do anything with them, but that needs to be fixed
        // in createBarGiver before we can use them as quest giver.
        for (CommDirectoryEntryAPI entry : market.getCommDirectory().getEntriesCopy()) {
            if (entry.getType() == CommDirectoryEntryAPI.EntryType.PERSON &&
                    entry.getEntryData() instanceof PersonAPI) {
                candidates.add((PersonAPI) entry.getEntryData());
            }
        }

        // Remove candidates that don't have the right attributes.
        for (Iterator<PersonAPI> iterator = candidates.iterator(); iterator.hasNext();) {
            PersonAPI person = iterator.next();
            if (!person.getPostId().equals(Ranks.POST_STATION_COMMANDER) ||
                    person.getFaction() != market.getFaction() ||
                    (person.getMarket() != null && person.getMarket() != market)) {
                iterator.remove();
            }
        }

        return candidates.isEmpty() ? null : Collections.max(candidates, new ComparePersonByRel(market));
    }

    public void createBarGiver(MarketAPI createdAt) {
        createBarGiver(this, createdAt);
    }
    public static void createBarGiver(HubMissionWithBarEvent event, MarketAPI createdAt) {
        PersonAPI commander = findStationCommander(createdAt);
        String tag = Factions.PIRATES.equals(createdAt.getFactionId()) ?
                Tags.CONTACT_UNDERWORLD : Tags.CONTACT_MILITARY;
        if (commander != null) {
//            log.debug("Existing station commander " + commander.getNameString() + " found with tags=" +
//                    commander.getTags());
            // Give them military/underworld tag so they work as a military/underworld contact
            commander.addTag(tag);
            if (commander.getMarket() == null) {
                createdAt.addPerson(commander);
            }
            event.makePersonRequired(commander);  // Prevent another mission from removing him.
            event.setPersonOverride(commander);
            return;
        }

        // Adapted from MilitaryCustomBounty. To avoid duplicate station commanders, use it only if the above code
        // fails to find someone.
        event.setGiverPost(Ranks.POST_STATION_COMMANDER);
        event.setGiverTags(tag);
        event.setGiverRank(Ranks.SPACE_CAPTAIN);
        event.setGiverImportance(myPickHighImportance(event.getGenRandom()));
        event.findOrCreateGiver(createdAt, false, false);
        event.setPersonOverride(event.getPerson());
        log.info("Created new station commander " + event.getPerson().getNameString());
    }

    // Add the quest giver of event as contact using the same logic as vanilla ContactIntel.addPotentialContact, then
    // adjust the probability with contactProbFactor if non-player faction, or playerContactProbFactor if player
    // faction. If negative then do not add contact. If 0 then always add contact. Otherwise, adjust the probability by
    // taking the power to the factor (so a 1f factor means vanilla probability).
    public static void addPotentialContact(BaseHubMission event, InteractionDialogAPI dialog, float contactProbFactor,
                                           float playerContactProbFactor, float bonus) {
        // Logic adapted from ContactIntel.addPotentialContact
        PersonAPI contact = event.getPerson();
        MarketAPI market = contact.getMarket();

        if (market == null) return;  // Sanity check
        if (ContactIntel.playerHasIntelItemForContact(contact)) return;
        if (market.getMemoryWithoutUpdate().getBoolean(ContactIntel.NO_CONTACTS_ON_MARKET)) return;
        if (contact.getFaction().getCustomBoolean(Factions.CUSTOM_NO_CONTACTS)) return;

        float factor = contact.getFaction().isPlayerFaction() ? playerContactProbFactor : contactProbFactor;
        if (factor < 0f) return;

        if (factor > 0f) {
            float rel = contact.getRelToPlayer().getRel() + bonus;
            float probability = ContactIntel.DEFAULT_POTENTIAL_CONTACT_PROB + rel / factor;

            final String key = "$potentialContactRollFails";
            MemoryAPI mem = Global.getSector().getMemoryWithoutUpdate();

            float prob = (float) Math.pow(probability, factor);
            float fails = mem.getInt(key);
            prob += ContactIntel.ADD_PER_FAIL * fails;
            float roll = event.getGenRandom().nextFloat();
            log.debug(MessageFormat.format(
                    "addPotentialContact: person={0}, factor={1}, rel={2}, probability={3}, prob={4}, roll={5}, fails={6}, bonus={7}",
                    contact.getNameString(), factor, rel, probability, prob, roll, fails, bonus));

            if (!DebugFlags.ALWAYS_ADD_POTENTIAL_CONTACT && roll >= prob) {
                fails++;
                mem.set(key, fails);
                return;
            }
            mem.set(key, 0);
        }

        contact.removeTag(REMOVE_ON_MISSION_OVER);  // Not sure if this is needed
        // Note that ContactIntel will ensure that the person is added to the market and comm directory.
        Global.getSector().getIntelManager().addIntel(new ContactIntel(contact, market), false,
                dialog != null ? dialog.getTextPanel() : null);
    }

    public static void addPotentialContact(BaseHubMission event, InteractionDialogAPI dialog, float contactProbFactor,
                                           float playerContactProbFactor) {
        addPotentialContact(event, dialog, contactProbFactor, playerContactProbFactor, 0f);
    }

    public void addPotentialContact(InteractionDialogAPI dialog, float factor) {
        addPotentialContact(this, dialog, factor, factor, 0f);
    }

    // Compares 2 fleets by fleet strength difference with the expected value.
    public static class CompareFleetByFpDiff implements Comparator<CampaignFleetAPI> {
        public int expected_fp;
        public CompareFleetByFpDiff(int expected_fp) {
            this.expected_fp = expected_fp;
        }
        @Override
        public int compare(CampaignFleetAPI o1, CampaignFleetAPI o2) {
            return Math.abs(expected_fp - getFleetStrength(o1)) - Math.abs(expected_fp - getFleetStrength(o2));
        }
    }

    public static int getFleetStrength(CampaignFleetAPI fleet) {
        Float strength_ratio = (Float) fleet.getMemoryWithoutUpdate().get(STRENGTH_RATIO_KEY);
        if (strength_ratio == null) {
            strength_ratio = 1f;
        }
        int points = Math.round(Math.max(fleet.getFleetPoints(), fleet.getEffectiveStrength()) * strength_ratio);
        return fleet.isStationMode() ? Math.round(points * STATION_EFFECTIVE_FP_RATIO) : points;
    }
    // Same as above, but also tags the fleet with STRENGTH_RATIO_KEY so it can be applied.
    public static int getFleetStrength(CampaignFleetAPI fleet, String reinforcement_faction) {
        Float strength_ratio = FACTION_STRENGTH_RATIO.get(reinforcement_faction);
        if (strength_ratio != null) {
            fleet.getMemoryWithoutUpdate().set(STRENGTH_RATIO_KEY, strength_ratio);
        }
        return getFleetStrength(fleet);
    }
    public static int getFleetStrength(List<CampaignFleetAPI> fleets) {
        int fp = 0;
        for (CampaignFleetAPI fleet : fleets) {
            fp += getFleetStrength(fleet);
        }
        return fp;
    }

    public static int getPlayerFleetStrength() {
        // When you talk to a contact with stellar network, sometimes the getPlayerFleet() call returns an empty fleet?
        // As a workaround memorize the player fleet strength.
        int strength = getFleetStrength(Global.getSector().getPlayerFleet());
        MemoryAPI memory = Global.getSector().getMemoryWithoutUpdate();
        final String KEY = "$mmm_dm_player_fp";
        if (strength > 0) {
            memory.set(KEY, strength);
        } else {
            Object val = memory.get(KEY);
            if (val != null) strength = (Integer) val;
        }
        return strength;
    }

    public static class CreateFleetResult {
        CampaignFleetAPI fleet;
        // The updated expected strength per input fleet points.
        float input_fp_per_output;
        public CreateFleetResult(CampaignFleetAPI fleet, float input_fp_per_output) {
            this.fleet = fleet;
            this.input_fp_per_output = input_fp_per_output;
        }
    }
    // Creates a fleet targeting target with possibly multiple attempts.
    public static CreateFleetResult createFleet(
            SectorEntityToken target, String fleet_name, String relation_faction, String reinforcement_faction,
                int attempts, int expected_fp, float input_fp_per_output) {
        if (input_fp_per_output == 0f) input_fp_per_output = 0.68f;

        float max_input_fp_per_output = 1.5f;
        Float strength_ratio = FACTION_STRENGTH_RATIO.get(reinforcement_faction);
        if (strength_ratio != null && strength_ratio > 0f) {
            max_input_fp_per_output = Math.max(max_input_fp_per_output, max_input_fp_per_output / strength_ratio);
        }

        ArrayList<CampaignFleetAPI> candidates = new ArrayList<>();
        int threshold = Math.round(expected_fp * 0.15f);
        for (int i = 0; i < attempts; ++i){
            int input_fp = Math.round(expected_fp * input_fp_per_output);

            // Keeping the fleets around as the enemy faction can result in them grabbing the system's comm relay, etc.
            // To prevent this we set them to the neutral faction, and change them to the enemy faction when we wake
            // them up.
            CampaignFleetAPI fleet;
            try {
                fleet = new MagicFleetBuilder()
                        .setFleetName(fleet_name)
                        .setFleetFaction(reinforcement_faction)
                        .setReinforcementFaction(reinforcement_faction)     // controls ship types
                        .setMinFP(input_fp)
                        .setAssignmentTarget(target)
                        .create();
            } catch (Exception e) {
                log.error("MagicFleetBuilder Exception: ", e);
                fleet = null;
            }
            if (fleet == null) {
                log.error("Failed to find " + input_fp + " MinFP fleet for " + reinforcement_faction);
                break;
            }

            // Needed?
            fleet.setFaction(relation_faction);
            fleet.forceSync();

            int output_fp = getFleetStrength(fleet, reinforcement_faction);
            log.debug(MessageFormat.format(
                    "createFleet: reinforcement={0}, expected_fp={1}, input_fp={2}, output_fp={3}({4} points)" +
                            ", input_fp_per_output={5}(max {6}), threshold={7}, attempt {8}/{9}",
                    reinforcement_faction, expected_fp, input_fp, output_fp, fleet.getFleetPoints(),
                    input_fp_per_output, max_input_fp_per_output, threshold, i + 1, attempts));
            input_fp_per_output = Math.min(max_input_fp_per_output, input_fp / (float) output_fp);
            candidates.add(fleet);
            // If we're off by more then 15%, try again with an adjusted input_fp_per_output.
            if (Math.abs(output_fp - expected_fp) <= threshold) break;
        }

        // Now pick the best candidate and de-spawns the rest
        if (candidates.isEmpty()) {
            return new CreateFleetResult(null, input_fp_per_output);
        }
        CampaignFleetAPI chosen = Collections.min(candidates, new CompareFleetByFpDiff(expected_fp));
        candidates.remove(chosen);
        for (CampaignFleetAPI fleet : candidates) {
            fleet.setLocation(-26000, -26000);
            fleet.despawn();
        }
        return new CreateFleetResult(chosen, input_fp_per_output);
    }

    // Returns the first non-null submarket.
    public static SubmarketAPI firstNoneNull(List<SubmarketAPI> submarkets) {
        for (SubmarketAPI submarket : submarkets) {
            if (submarket != null) {
                return submarket;
            }
        }
        return null;
    }

    // Buy or sell the commodities on the market, applying trade mods and adding to or removing from the market stack.
    // Also register the trade to PlayerTradeDataForSubmarket for XP. Here prices can be null, and is only used for
    // sells. Note that we don't use CargoStackAPI because it has a max size.
    public static void applyTradeMods(MarketAPI market, boolean smuggling, boolean sell, boolean limit_effect,
                                      Map<String, Integer> stacks, Map<String, Integer> prices) {
        if (market == null || stacks == null) return;  // Sanity check
//        log.debug("applyTradeMods on market " + market.getName());

        // Apply trade price mods
        for (Map.Entry<String, Integer> entry : stacks.entrySet()) {
            int quantity = sell ? entry.getValue() : -entry.getValue();
            if (limit_effect) {
                // See Misc.affectAvailabilityWithinReason for reference
                CommodityOnMarketAPI com = market.getCommodityData(entry.getKey());
                int units = Misc.computeEconUnitChangeFromTradeModChange(com, quantity);
                int maxUnits = Math.min(3, Math.max(com.getMaxDemand(), com.getMaxSupply()));
                if (Math.abs(units) > maxUnits) {
                    int sign = (int) Math.signum(quantity);
                    quantity = (int) Math.round(com.getQuantityForModValue(maxUnits));
                    quantity *= sign;
                }
            }
            market.getCommodityData(entry.getKey()).addTradeMod("mmm_" + Misc.genUID(), quantity,
                    TRADE_IMPACT_DAYS);
        }

        SubmarketAPI open_market = market.getSubmarket(Submarkets.SUBMARKET_OPEN);
        SubmarketAPI mil_market = market.getSubmarket(Submarkets.GENERIC_MILITARY);
        SubmarketAPI blk_market = market.getSubmarket(Submarkets.SUBMARKET_BLACK);

        ArrayList<SubmarketAPI> legal_markets = new ArrayList<>(Arrays.asList(open_market, mil_market, blk_market));
        ArrayList<SubmarketAPI> illegal_markets = new ArrayList<>(Arrays.asList(mil_market, open_market, blk_market));
        if (smuggling) {
            legal_markets.add(0, blk_market);
            illegal_markets.add(0, blk_market);
        }

        SubmarketAPI legal_market = firstNoneNull(legal_markets);
        SubmarketAPI illegal_market = firstNoneNull(illegal_markets);

        PlayerActivityTracker tracker = SharedData.getData().getPlayerActivityTracker();
        for (Map.Entry<String, Integer> entry : stacks.entrySet()) {
            // Add stack to the most relevant market.
            boolean isIllegal = market.isIllegal(entry.getKey());
            if (sell) {
                // Adding to market
                SubmarketAPI sub_market = isIllegal ? illegal_market : legal_market;
                if (sub_market != null) {
                    sub_market.getCargo().addCommodity(entry.getKey(), entry.getValue());

                    // Register the trade for player XP.
                    CargoStackAPI temp = Global.getFactory().createCargoStack(CargoAPI.CargoItemType.RESOURCES,
                            entry.getKey(), null);
                    temp.setSize(entry.getValue());
                    tracker.getPlayerTradeData(sub_market).addToTrackedPlayerSold(temp,
                            prices == null ? -1 : prices.get(entry.getKey()));
                }
            } else {
                // Removing from market.
                int remaining = entry.getValue();
                for (SubmarketAPI sub_market : (isIllegal ? illegal_markets : legal_markets)) {
                    if (remaining <= 0) break;
                    if (sub_market == null) continue;

                    int diff = Math.min(Math.round(sub_market.getCargo().getCommodityQuantity(entry.getKey())), remaining);
                    sub_market.getCargo().removeCommodity(entry.getKey(), diff);
                    remaining -= diff;

                    // Registers the trade for player XP
                    CargoStackAPI temp = Global.getFactory().createCargoStack(CargoAPI.CargoItemType.RESOURCES,
                            entry.getKey(), null);
                    temp.setSize(diff);
                    tracker.getPlayerTradeData(sub_market).addToTrackedPlayerBought(temp);
                }
            }
        }
    }

    // Set the memory flag to true and extend the expiration if longer
    public static void extendFlag(MemoryAPI memory, String flag, float expiration) {
        memory.set(flag, true, Math.max(memory.getExpire(flag), expiration));
    }

    // Same as vanilla logic but different weights (see BaseHubMission)
    public static PersonImportance myPickImportance(Random random) {
        WeightedRandomPicker<PersonImportance> picker = new WeightedRandomPicker<PersonImportance>(random);
        picker.add(PersonImportance.LOW, 5f);
        picker.add(PersonImportance.MEDIUM, 10f);    // 48%
        picker.add(PersonImportance.HIGH, 5f);       // 24%
        picker.add(PersonImportance.VERY_HIGH, 1f);  // 4.8%
        return picker.pick();
    }
    public static PersonImportance myPickHighImportance(Random random) {
        WeightedRandomPicker<PersonImportance> picker = new WeightedRandomPicker<PersonImportance>(random);
        picker.add(PersonImportance.MEDIUM, 10f);
        picker.add(PersonImportance.HIGH, 10f);      // 43%
        picker.add(PersonImportance.VERY_HIGH, 3f);  // 13%
        return picker.pick();
    }
    public static PersonImportance myPickVeryHighImportance(Random random) {
        WeightedRandomPicker<PersonImportance> picker = new WeightedRandomPicker<PersonImportance>(random);
        picker.add(PersonImportance.HIGH, 2f);
        picker.add(PersonImportance.VERY_HIGH, 1f);  // 33%
        return picker.pick();
    }

    public static void acceptCommon(String missionId, PersonAPI person) {
        updateSeed(missionId, person.getMarket(), person);
    }

    @Override
    public void acceptImpl(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
        super.acceptImpl(dialog, memoryMap);
        acceptCommon(getMissionId(), getPerson());
    }
}