package mmm.missions;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionBarEventWrapper;
import com.fs.starfarer.api.loading.PersonMissionSpec;
import mmm.Utils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

// Same as HubMissionBarEventWrapper, but returns true for isAlwaysShow, does not lock itself on a market, and does not
// use BarEventSpec.
public class MmmHubMissionBarEventWrapper extends HubMissionBarEventWrapper {
    private static final Logger log = Global.getLogger(MmmHubMissionBarEventWrapper.class);
    static {
        if (Utils.DEBUG) {
            log.setLevel(Level.ALL);
        }
    }

//    protected int cycle_month = -1;

    public MmmHubMissionBarEventWrapper(String specId) {
        // Note that we don't actually use super.spec
        super(specId);
    }

    // Adapted from HubMissionBarEventWrapper; does not check shownAt, and uses our own PRNG logic. Missions needs
    // to update their own PRNG seed by calling OrbitalMissionBase.updateSeed
    @Override
    public boolean shouldShowAtMarket(MarketAPI market) {
        abortMission();

        // Since we don't know the quest giver here, person missions will need to set their own PRNG in their
        // shouldShowAtMarket/create if they need it. Note that MmmProcurementMission is only used for contact missions,
        // so it is not supported here.
        genRandom = OrbitalMissionBase.getRandom(specId, market);
        switch (specId) {
            case DefenseMission.MISSION_ID:
                mission = new DefenseMission();
                break;
            case RepairMission.MISSION_ID:
                mission = new RepairMission();
                break;
            case EscortMission.MISSION_ID:
                mission = new EscortMission();
                break;
            default:
                log.error("Unimplemented specId=" + specId);
                return false;
        }
        mission.setMissionId(specId);
        mission.setGenRandom(genRandom);

        PersonMissionSpec spec = Global.getSettings().getMissionSpec(specId);
        if (spec != null && spec.getIcon() != null) {
            mission.setIconName(spec.getIcon());
        }

        return mission.shouldShowAtMarket(market);
    }

    @Override
    public boolean isAlwaysShow() {
        return true;
    }
}
