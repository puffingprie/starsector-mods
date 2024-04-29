package mmm.missions;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCampaignEventListener;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.intel.bar.PortsideBarData;
import com.fs.starfarer.api.impl.campaign.intel.bar.PortsideBarEvent;
import mmm.Utils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.util.*;

public class MissionInjector extends BaseCampaignEventListener {
    private static final Logger log = Global.getLogger(MissionInjector.class);
    static {
        if (Utils.DEBUG) {
            log.setLevel(Level.ALL);
        }
    }

    // Strings
    // Missions that does not depend on the quest giver, and so only requires a different seed per market.
    public static final List<String> MARKET_MISSION_IDS =
            Arrays.asList(DefenseMission.MISSION_ID, RepairMission.MISSION_ID, EscortMission.MISSION_ID);
    // Missions that does depend on the quest giver, and thus requires a different seed per person. Note that
    // MmmProcurementMission is not used for bar missions, only contact.
    public static final List<String> PERSON_MISSION_IDS = Arrays.asList(MmmProcurementMission.MISSION_ID,
            MmmCheapCommodityMission.MISSION_ID);
    public static final List<String> ALL_MISSION_IDS = new ArrayList<>(MARKET_MISSION_IDS);
    static {
        ALL_MISSION_IDS.addAll(PERSON_MISSION_IDS);
    }

    public MissionInjector() {
        super(false);  // Don't add permanently
    }

    public static boolean marketIsValid(MarketAPI market) {
        // Some sanity check
        return market != null && market.getStarSystem() != null && market.getPrimaryEntity() != null &&
                market.getFaction() != null && !market.getFactionId().equals(Factions.NEUTRAL) &&
                !market.getMemoryWithoutUpdate().getBoolean("$noBar");
    }

    @Override
    public void reportPlayerOpenedMarket(MarketAPI market) {
        if (!marketIsValid(market)) return;  // Some sanity check

        // Ensure $mmm_mission_seeds is populated for every mission/person for the market.
        OrbitalMissionBase.ensureSeeds(market);

        // BarCMD calls BarEventManager.notifyWasInteractedWith, which removes the event from PortsideBarData,
        // so we need to add them back here.
        PortsideBarData data = PortsideBarData.getInstance();
        ArrayList<String> missing_event_ids = new ArrayList<>(MARKET_MISSION_IDS);
        for (PortsideBarEvent event : data.getEvents()) {
            missing_event_ids.remove(event.getBarEventId());
        }

        for (String event_id : missing_event_ids) {
//                log.debug("Added " + event_id + " to PortsideBarData in " + market.getName());
            data.addEvent(new MmmHubMissionBarEventWrapper(event_id));
        }

        // Ensure the mission id is in $BarCMD_shownEvents if the key exists; otherwise BarCMD would skip them
        final String KEY = "$BarCMD_shownEvents";
        MemoryAPI market_memory = market.getMemoryWithoutUpdate();
        if (market_memory.contains(KEY)) {
            List<String> stored_event_ids = (List<String>) market_memory.get(KEY);
            missing_event_ids = new ArrayList<>(MARKET_MISSION_IDS);
            missing_event_ids.removeAll(stored_event_ids);
            stored_event_ids.addAll(missing_event_ids);
//                for (String event_id : missing_event_ids) {
//                    log.debug("Added " + event_id + " to $BarCMD_shownEvents in " + market.getName());
//                }
        }

//        log.debug("reportPlayerOpenedMarket; getSeedMap=" + OrbitalMissionBase.getSeedMap(market));
    }
}
