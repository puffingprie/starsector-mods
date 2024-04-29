package data.scripts.campaign.econ;

import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.fleets.RouteLocationCalculator;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteSegment;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RouteFleetAssignmentAI;
import data.scripts.campaign.econ.II_ImperialGuardHQ.IGPatrolFleetData;

public class II_DefenderAssignmentAI extends RouteFleetAssignmentAI {

    public II_DefenderAssignmentAI(CampaignFleetAPI fleet, RouteData route) {
        super(fleet, route);
    }

    @Override
    protected void giveInitialAssignments() {
        SectorEntityToken source = route.getMarket().getPrimaryEntity();
        if (source == null) {
            return;
        }

        SectorEntityToken target = null;
        LocationAPI location = source.getContainingLocation();
        for (SectorEntityToken entity : location.getAllEntities()) {
            if (entity.getMemoryWithoutUpdate() != null) {
                if (entity.getMemoryWithoutUpdate().contains(MemFlags.STATION_MARKET)) {
                    if (entity.getMemoryWithoutUpdate().get(MemFlags.STATION_MARKET) == route.getMarket()) {
                        target = entity;
                        break;
                    }
                }
            }
        }
        if (target == null) {
            return;
        }

        RouteSegment current = route.getCurrent();
        if (current == null) {
            return;
        }

        float progress = current.getProgress();
        RouteLocationCalculator.setLocation(fleet, progress, target, target);

        fleet.clearAssignments();

        fleet.addAssignment(FleetAssignment.ORBIT_PASSIVE, target, current.daysMax - current.elapsed, "defending " + target.getName());
        fleet.addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, source, 1000f, "standing down", goNextScript(current));
    }

    @Override
    public void advance(float amount) {
        super.advance(amount);

        RouteSegment current = route.getCurrent();
        if (current == null) {
            return;
        }

        IGPatrolFleetData custom = (IGPatrolFleetData) route.getCustom();
        if ((fleet.getAI().getCurrentAssignment() != null) && (fleet.getAI().getCurrentAssignment().getAssignment() != FleetAssignment.GO_TO_LOCATION_AND_DESPAWN)) {
            int fp = fleet.getFleetPoints();
            if (fp < (custom.spawnFP / 2)) {
                SectorEntityToken source = route.getMarket().getPrimaryEntity();
                fleet.clearAssignments();
                fleet.addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, source, 1000f, "standing down", goNextScript(current));
                return;
            }
        }

        if ((fleet.getAI().getCurrentAssignment() != null) && (fleet.getAI().getCurrentAssignment().getAssignment() == FleetAssignment.ORBIT_PASSIVE)) {
            SectorEntityToken target = fleet.getAI().getCurrentAssignment().getTarget();
            if (target instanceof CampaignFleetAPI) {
                CampaignFleetAPI targetFleet = (CampaignFleetAPI) target;
                if (targetFleet.getBattle() != null) {
                    BattleAPI battle = targetFleet.getBattle();
                    if (!battle.isDone()) {
                        fleet.clearAssignments();
                        fleet.addAssignment(FleetAssignment.ORBIT_AGGRESSIVE, target, 3f, "defending " + target.getName());
                        if ((current.daysMax - current.elapsed) > 3f) {
                            fleet.addAssignment(FleetAssignment.ORBIT_PASSIVE, target, (current.daysMax - current.elapsed) - 3f, "defending " + target.getName());
                        }
                        SectorEntityToken source = route.getMarket().getPrimaryEntity();
                        if (source != null) {
                            fleet.addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, source, 1000f, "standing down", goNextScript(current));
                        }
                    }
                }
            }
        }
    }
}
