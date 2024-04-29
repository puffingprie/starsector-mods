package mmm.missions;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.events.nearby.NearbyEventsEvent;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.intel.misc.DistressCallIntel;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import mmm.Utils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.magiclib.util.MagicSettings;

import java.text.MessageFormat;
import java.util.*;

public class VipMission extends OrbitalMissionBase {
    private static final Logger log = Global.getLogger(VipMission.class);
    static {
        if (Utils.DEBUG) {
            log.setLevel(Level.ALL);
        }
    }

    public enum Stage {
        ACCEPTED,
        COMPLETED
    }

    // Memory flags
    public static final String SALVAGE_SEED = "$mmm_vip_salvageSeed";

    // Settings
    public static float MISSION_PROB;
    public static final float DISTRESS_CALL_FREQ = MagicSettings.getFloat(MOD_ID, "VipDistressCallFreq");
    public static final int CREDIT_REWARD = MagicSettings.getInteger(MOD_ID, "VipCreditReward");
    public static final int PERSON_REL_REWARD = MagicSettings.getInteger(MOD_ID, "VipPersonRelReward");
    public static final int REL_REWARD = Math.max(PERSON_REL_REWARD, MagicSettings.getInteger(MOD_ID, "VipRelReward"));

    MarketAPI market = null;

    // Class for adding triggers for VipMission
    public static class TriggerInjector implements EveryFrameScript {
        protected float days = 0f;

        @Override
        public boolean isDone() { return false; }
        @Override
        public boolean runWhilePaused() { return false; }

        @Override
        public void advance(float amount) {
            if (DISTRESS_CALL_FREQ > 1f) {
                for (CampaignEventListener listener : Global.getSector().getAllListeners()) {
                    if (listener instanceof NearbyEventsEvent) {
                        ((NearbyEventsEvent) listener).advance(amount * (DISTRESS_CALL_FREQ - 1f));
                    }
                }
            }

            days -= Global.getSector().getClock().convertToDays(amount);
            if (days > 0f) return;
            days = 1f;

            final String REPEAT_TIMEOUT_FLAG = "$mmm_vip_repeated_timeout";
            final String DERELICT_PICKED = "$mmm_vip_derelict_picked";

            WeightedRandomPicker<CustomCampaignEntityAPI> picker = new WeightedRandomPicker<>();
            for (IntelInfoPlugin curr : Global.getSector().getIntelManager().getIntel(DistressCallIntel.class)) {
                if (!(curr instanceof DistressCallIntel)) continue;
                DistressCallIntel intel = (DistressCallIntel) curr;
                SectorEntityToken entity = intel.getMapLocation(null);
                if (entity == null) continue;
                LocationAPI location = entity.getContainingLocation();
                if (!(location instanceof StarSystemAPI)) continue;

                // No point in checking the same system if they're in timeout.
                StarSystemAPI system = (StarSystemAPI) location;
                MemoryAPI memory = system.getMemoryWithoutUpdate();
                if (memory.contains(REPEAT_TIMEOUT_FLAG)) continue;
                memory.set(REPEAT_TIMEOUT_FLAG, true, NearbyEventsEvent.DISTRESS_REPEAT_TIMEOUT);

                // Same logic as in NearbyEventsEvent.generateDistressDerelictShip
                SectorEntityToken jump_point = Misc.getDistressJumpPoint(system);
                if (jump_point == null) continue;
                float max_radius = Math.max(300, jump_point.getCircularOrbitRadius() * 0.33f);

                for (CustomCampaignEntityAPI derelict : system.getCustomEntitiesWithTag(Tags.SALVAGEABLE)) {
                    if (!derelict.getCustomEntityType().equals(Entities.WRECK)) continue;
                    float dist = Misc.getDistance(derelict, jump_point);
                    if (dist <= max_radius && Misc.getSalvageSpecial(derelict) != null &&
                            !derelict.getMemoryWithoutUpdate().contains(DERELICT_PICKED)) {
//                        log.debug(MessageFormat.format(
//                                "Found candidate derelict: name={0}, system={1}, near={2}, dist={3}, max={4}",
//                                derelict.getName(), system.getName(), jump_point.getName(), dist, max_radius));
                        picker.add(derelict);
                    }
                }
            }

            if (picker.isEmpty()) return;

            // Now we add the trigger for the mission by setting the DERELICT_PICKED flag. We also save the salvage seed
            // to global memory so the VipMission can make use of it.
            CustomCampaignEntityAPI picked = picker.pick();
            long salvage_seed = Misc.getSalvageSeed(picked);
            Global.getSector().getMemoryWithoutUpdate().set(SALVAGE_SEED, salvage_seed);

            boolean has_mission = Global.getSettings().isDevMode() || Misc.random.nextFloat() <= MISSION_PROB;
            log.debug(MessageFormat.format("Picked derelict: name={0}, system={1}, loc={2}, has_mission={3}, $salvageSeed={4}",
                    picked.getName(), picked.getName(), picked.getLocation(), has_mission, salvage_seed));
            picked.getMemoryWithoutUpdate().set(DERELICT_PICKED, has_mission);
        }
    }

    // Find an appropriate market and create the VIP.
    public boolean createVip() {
        HashSet<FactionAPI> valid_factions = new HashSet<>();
        for (FactionAPI faction : Global.getSector().getAllFactions()) {
            if (!faction.isPlayerFaction() && faction.isShowInIntelTab() && !faction.getRelToPlayer().isHostile()) {
                valid_factions.add(faction);
            }
        }

        // First pick faction weighted by SQRT of size to give smaller factions a chance.
        HashMap<FactionAPI, Integer> faction_weights = new HashMap<>();
        HashMap<FactionAPI, List<MarketAPI>> markets_map = new HashMap<>();
        for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
            FactionAPI faction = market.getFaction();
            if (market.isHidden() || market.getStarSystem() == null || !valid_factions.contains(faction)) {
                continue;
            }

            List<MarketAPI> list = markets_map.get(faction);
            if (list == null) {
                list = new ArrayList<>();
                markets_map.put(faction, list);
            }
            list.add(market);

            if (faction_weights.containsKey(faction)) {
                faction_weights.put(faction, faction_weights.get(faction) + market.getSize());
            } else {
                faction_weights.put(faction, market.getSize());
            }
        }
        if (faction_weights.isEmpty() || markets_map.isEmpty()) return false;  // Sanity check

        // To prevent save scum, obtain PRNG from the fleet's salvage seed.
        long seed = Global.getSector().getMemoryWithoutUpdate().getLong(SALVAGE_SEED);
        Random random = seed == 0 ? getGenRandom() : new Random(seed);

        WeightedRandomPicker<FactionAPI> faction_picker = new WeightedRandomPicker<>(random);
        for (Map.Entry<FactionAPI, Integer> entry : faction_weights.entrySet()) {
            faction_picker.add(entry.getKey(), (float) Math.sqrt(entry.getValue()));
        }
        FactionAPI faction = faction_picker.pick();
        log.debug(MessageFormat.format("findVip: faction picked={0}, weights={1}",
                faction.getDisplayName(), faction_weights));

        // Now pick market from faction, weighted by 2.59th power of size to prioritize larger markets
        WeightedRandomPicker<MarketAPI> market_picker = new WeightedRandomPicker<>(random);
        for (MarketAPI market : markets_map.get(faction)) {
            market_picker.add(market, (float) Math.pow(market.getSize(), 2.59));
        }

        market = market_picker.pick();
        log.info(MessageFormat.format("findVip: market picked={0}, system={1}, faction={2}",
                market.getName(), market.getStarSystem().getName(), faction.getDisplayName()));

        // Now determine person post, tags, importance, and voice.
        WeightedRandomPicker<PersonImportance> importance_picker = new WeightedRandomPicker<>(random);
        importance_picker.add(PersonImportance.HIGH, 2f);
        importance_picker.add(PersonImportance.VERY_HIGH, 1f);
        PersonImportance importance = importance_picker.pick();

        String post = null;
        String voice = null;
        ArrayList<String> tags = new ArrayList<>();

        if (faction.getId().equals(Factions.PIRATES)) {
            if (importance == PersonImportance.VERY_HIGH) {
                post = Ranks.POST_ARMS_DEALER;
            } else {
                post = pickOne(Ranks.POST_ARMS_DEALER, Ranks.POST_CRIMINAL);
            }
            tags.add(Tags.CONTACT_UNDERWORLD);
        } else {
            tags.add(Tags.CONTACT_TRADE);
            if (faction.getId().equals(Factions.PERSEAN) || faction.getId().equals(Factions.HEGEMONY)) {
                if (random.nextFloat() < 0.67f) {
                    post = Ranks.POST_ARISTOCRAT;
                    voice = Voices.ARISTO;
                    tags.add(Tags.CONTACT_MILITARY);
                }
            } else if (faction.getId().equals(Factions.TRITACHYON)) {
                if (importance == PersonImportance.VERY_HIGH) {
                    post = Ranks.POST_SENIOR_EXECUTIVE;
                } else {
                    post = pickOne(Ranks.POST_EXECUTIVE, Ranks.POST_SENIOR_EXECUTIVE);
                }
                voice = Voices.BUSINESS;
                tags.add(Tags.CONTACT_MILITARY);
            }

            if (post == null) {
                if (importance == PersonImportance.VERY_HIGH) {
                    post = pickOne(Ranks.POST_SENIOR_EXECUTIVE, Ranks.POST_ENTREPRENEUR);
                } else {
                    post = pickOne(Ranks.POST_EXECUTIVE, Ranks.POST_SENIOR_EXECUTIVE, Ranks.POST_ENTREPRENEUR);
                }
                voice = Voices.BUSINESS;
            }
        }

        // Now create a person.
        // Adapted from BaseHubMission.findOrCreateGiver
        setGiverRank(Ranks.CITIZEN);
        setGiverPost(post);
        setGiverImportance(importance);
        setGiverTags(tags.toArray(new String[0]));
        if (voice != null) {
            setGiverVoice(voice);
        }
        createGiver(market, true, true);
        if (getPerson() == null) {
            log.error(MessageFormat.format("Failed to create VIP with importance={0}, post={1}", importance, post));
            return false;
        }

        log.info(MessageFormat.format("Created VIP {0}, importance={1}, post={2}", getPerson(), importance, post));
        return true;
    }

    @Override
    protected boolean create(MarketAPI createdAt, boolean barEvent) {
        // createdAt is null since the derelict won't have one set.
//        log.debug("create called with barEvent=" + barEvent);
        if (!getMissionId().equals("mmm_vip")) {
            log.error("Unexpected mission id: " + getMissionId());
            return false;
        }

        // TODO: Disallow save-scum.

        if (!createVip()) return false;

        setStartingStage(Stage.ACCEPTED);
        setSuccessStage(Stage.COMPLETED);
        setName("VIP transport");
        setNoRepChanges();

        PersonAPI person = getPerson();
        makeImportant(person, "$mmm_vip_important", Stage.ACCEPTED);

        final String KEY = "$mmm_vip_ref";
        if (!setPersonMissionRef(person, KEY)) {
            log.error(KEY + " already set for " + person.getNameString());
            return false;
        }
        Global.getSector().getMemoryWithoutUpdate().set("$mmm_vip_person", person, 0f);

        return true;
    }

    @Override
    protected void updateInteractionDataImpl() {
        List<String> luddic_factions = Arrays.asList(Factions.LUDDIC_PATH, Factions.LUDDIC_CHURCH);
        setPersonTokens(interactionMemory);
        set("$mmm_vip_is_luddic", luddic_factions.contains(getPerson().getFaction().getId()));
        set("$mmm_vip_market", market.getName());
        set("$mmm_vip_credits", Misc.getDGSCredits(CREDIT_REWARD));
        set("$mmm_vip_person_rel_reward", PERSON_REL_REWARD);
        set("$mmm_vip_rel_reward", REL_REWARD);
    }

    @Override
    public void acceptImpl(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
        // super.acceptImpl(dialog, memoryMap);
        PersonAPI person = getPerson();
        person.incrWantsToContactReasons();
        person.setContactWeight(1000f);
    }

    @Override
    protected boolean callAction(String action, String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        if (action.equals("endSuccess")) {
            setCurrentStage(Stage.COMPLETED, dialog, memoryMap);
            return true;
        }
        return super.callAction(action, ruleId, dialog, params, memoryMap);
    }

    @Override
    protected void endSuccessImpl(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
        PersonAPI person = getPerson();
        person.removeTag(REMOVE_ON_MISSION_OVER);
        person.decrWantsToContactReasons();
        addPotentialContact(dialog, 0f);
    }

    // Display intel correctly.
    @Override
    public SectorEntityToken getMapLocation(SectorMapAPI map) {
        return market.getPrimaryEntity();
    }

    @Override
    public String getNextStepText() {
        return MessageFormat.format("Transport {0} to {1}.", getPerson().getNameString(), market.getName());
    }

    @Override
    public void addDescriptionForNonEndStage(TooltipMakerAPI info, float width, float height) {
        info.addPara(getNextStepText(), 3f, Misc.getHighlightColor(), market.getName());
    }
}
