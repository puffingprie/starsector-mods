package mmm.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.missions.hub.BaseHubMissionCreator;
import com.fs.starfarer.api.impl.campaign.missions.hub.BaseMissionHub;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMission;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.loading.PersonMissionSpec;
import com.fs.starfarer.api.util.Misc;
import mmm.Utils;
import mmm.missions.MissionInjector;
import mmm.missions.MmmProcurementMission;
import mmm.missions.OrbitalMissionBase;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

// For contact person missions, we need to insert the missions between BaseMissionHub.prepare and listMissions calls,
// so we overwrite the contactMHOpenText rule in rules.csv to execute this command. To avoid crowding out other
// missions, we use 0 value as frequency in person_missions.csv so that BaseMissionHub.prepare will not pick them.
public class MMM_MissionHubCMD extends BaseCommandPlugin {
    private static final Logger log = Global.getLogger(MMM_MissionHubCMD.class);
    static {
        if (Utils.DEBUG) {
            log.setLevel(Level.ALL);
        }
    }

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String,
            MemoryAPI> memoryMap) {
        log.debug(MessageFormat.format("execute ruleId={0}, params={1}, this={2}", ruleId, params, this));
        // Sanity checks
        if (dialog.getInteractionTarget() == null || dialog.getInteractionTarget().getActivePerson() == null) {
            return false;
        }
        PersonAPI person = dialog.getInteractionTarget().getActivePerson();
        BaseMissionHub hub = (BaseMissionHub) BaseMissionHub.get(person);
        if (hub == null || person.getMarket() == null) return false;  // Sanity check

        List<HubMission> offered = hub.getOfferedMissions();
        if (offered == null) return false;  // sanity check

        // BarCMD.showOptions calls ImportantPeopleAPI.excludeFromGetPerson to exclude contacts from bar missions,
        // so we never have to worry that a bar mission will pick an existing contact.
        for (PersonMissionSpec spec : BaseMissionHub.getMissionsForPerson(person)) {
            if (!MissionInjector.ALL_MISSION_IDS.contains(spec.getMissionId())) continue;
            if (MissionInjector.PERSON_MISSION_IDS.contains(spec.getMissionId()) &&
                    MmmProcurementMission.IsVanillaLogic(person)) {
                continue;
            }

            HubMission mission = spec.createMission();
            if (mission == null) continue;  // Sanity check
            mission.setHub(hub);  // This also sets the mission.getPerson() result
            mission.setCreator(new BaseHubMissionCreator(spec));
            // This isn't really needed since each mission is supposed to set its own random.
            mission.setGenRandom(OrbitalMissionBase.getRandom(spec.getMissionId(), person.getMarket(), person));
            mission.createAndAbortIfFailed(person.getMarket(), false);
            if (!mission.isMissionCreationAborted()) {
                offered.add(mission);
                mission.updateInteractionData(dialog, memoryMap);
            }
        }

        // Update $mh_firstInlineBlurb and $mh_count so the correct rules.csv line is used.
        if (!offered.isEmpty()) {
            MemoryAPI pMem = person.getMemoryWithoutUpdate();
            pMem.set("$mh_firstInlineBlurb", offered.get(0).getBlurbText(), 0);
            pMem.set("$mh_count", offered.size(), 0);
        }

        return true;
    }
}
