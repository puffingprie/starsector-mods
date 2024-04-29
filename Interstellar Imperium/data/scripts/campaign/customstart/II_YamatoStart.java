package data.scripts.campaign.customstart;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.Script;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.CharacterCreationData;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.rulecmd.FireBest;
import com.fs.starfarer.api.impl.campaign.rulecmd.newgame.NGCAddStartingShipsByFleetType;
import data.scripts.campaign.intel.SWP_IBBIntel.FamousBountyStage;
import data.scripts.campaign.intel.SWP_IBBTracker;
import exerelin.campaign.ExerelinSetupData;
import exerelin.campaign.PlayerFactionStore;
import exerelin.campaign.customstart.CustomStart;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class II_YamatoStart extends CustomStart {

    protected List<String> ships = new ArrayList<>(Arrays.asList(new String[]{
        "ii_boss_dominus_starter"
    }));

    @Override
    public void execute(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
        ExerelinSetupData.getInstance().freeStart = true;
        PlayerFactionStore.setPlayerFactionIdNGC(Factions.PLAYER);

        CharacterCreationData data = (CharacterCreationData) memoryMap.get(MemKeys.LOCAL).get("$characterData");

        NGCAddStartingShipsByFleetType.generateFleetFromVariantIds(dialog, data, null, ships);
        NGCAddStartingShipsByFleetType.addStartingDModScript(memoryMap.get(MemKeys.LOCAL));

        FireBest.fire(null, dialog, memoryMap, "ExerelinNGCStep4");

        data.addScript(new Script() {
            @Override
            public void run() {
                Global.getSector().addScript(new EveryFrameScript() {

                    private boolean done = false;

                    @Override
                    public boolean isDone() {
                        return done;
                    }

                    @Override
                    public boolean runWhilePaused() {
                        return true;
                    }

                    @Override
                    public void advance(float amount) {
                        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
                        for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
                            if (member.getShipName().contentEquals("Yamato")) {
                                done = true;
                                break;
                            }

                            member.setShipName("Yamato");
                        }
                    }

                });

                SWP_IBBTracker.getTracker().reportStageCompleted(FamousBountyStage.STAGE_YAMATO);
            }
        });
    }
}
