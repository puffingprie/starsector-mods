package data.scripts.campaign;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.util.II_Util;
import java.util.List;

public class II_MissileKillScript implements EveryFrameScript {

    private final IntervalUtil interval = new IntervalUtil(1f, 1f);

    @Override
    public void advance(float amount) {
        interval.advance(amount);

        if (interval.intervalElapsed()) {
            List<CampaignFleetAPI> fleets = Global.getSector().getCurrentLocation().getFleets();
            for (CampaignFleetAPI fleet : fleets) {
                List<FleetMemberAPI> members = fleet.getFleetData().getMembersListCopy();
                for (FleetMemberAPI member : members) {
                    String specId = II_Util.getNonDHullId(member.getHullSpec());
                    if (specId.contentEquals("ii_titan") || specId.contentEquals("ii_titan_armor")
                            || specId.contentEquals("ii_titan_targeting") || specId.contentEquals("ii_titan_elite")) {
                        fleet.removeFleetMemberWithDestructionFlash(member);
                    }
                }
            }
        }
    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public boolean runWhilePaused() {
        return false;
    }
}
