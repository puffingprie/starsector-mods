package data.hullmods;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.util.Misc;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class mi_MiRepairTracker implements EveryFrameScript {

    public static final String HULLMOD_ID = "mi_MiRepairGantry";
    public static final String STAT_MOD_ID = "mi_MiRepairGantry_repair_bonus";
    public static final float REPAIR_RATE_BONUS = 15f;
    public static final float SCAN_RANGE = 4000f; // hullmod won't work for NPC fleets more than this distance from player fleet
    static final Set<ShipAPI.HullSize> EXCLUDE = new HashSet<>();

    static {
        EXCLUDE.add(ShipAPI.HullSize.FIGHTER);
        EXCLUDE.add(ShipAPI.HullSize.FRIGATE);
    }
    
    public static final String KEY = "$mi_MiRepairTracker";
    
    public mi_MiRepairTracker() {
        Global.getSector().getMemoryWithoutUpdate().set(KEY, this);
    }

    public static mi_MiRepairTracker getInstance() {
        Object test = Global.getSector().getMemoryWithoutUpdate().get(KEY);
        return (mi_MiRepairTracker) test;
    }
    
    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public boolean runWhilePaused() {
        return false;
    }

    @Override
    public void advance(float amount) {
        for (CampaignFleetAPI fleet : Misc.getNearbyFleets(Global.getSector().getPlayerFleet(), SCAN_RANGE)) {
            updateStats(fleet);
        }
    }
    
    
    public void updateStats(CampaignFleetAPI fleet) {
        List<FleetMemberAPI> rigs = new ArrayList<>();
        List<FleetMemberAPI> members = fleet.getFleetData().getMembersListCopy();
        for (FleetMemberAPI member : members) {
            if (member.isMothballed() || !member.canBeDeployedForCombat()) {
                continue;
            }
            for (String modId : member.getVariant().getHullMods()) {
                if (HULLMOD_ID.equals(modId)) {
                    rigs.add(member);
                }
            }
        }

        int index = 0;
        List<FleetMemberAPI> assisted = new ArrayList<>();
        for (FleetMemberAPI rig : rigs) {
            for (int i = index; i < members.size(); i++) {
                FleetMemberAPI member = members.get(i);
                if (rig == member) {
                    continue;
                }
                if (!member.canBeRepaired()) {
                    continue;
                }
                if (!member.needsRepairs()) {
                    continue;
                }
                if (EXCLUDE.contains(member.getHullSpec().getHullSize())) {
                    continue;
                }

                member.getStats().getRepairRatePercentPerDay().modifyPercent(STAT_MOD_ID, REPAIR_RATE_BONUS);
                index = i + 1;
                assisted.add(member);

                break;
            }
        }

        for (FleetMemberAPI member : members) {
            if (!assisted.contains(member)) {
                member.getStats().getRepairRatePercentPerDay().unmodify(STAT_MOD_ID);
            }
        }
    }
}
