package data.scripts.campaign.econ;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.ai.FleetAssignmentDataAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.fleets.PatrolAssignmentAIV4;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteData;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.util.CountingMap;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import data.scripts.campaign.econ.II_ImperialGuardHQ.IGPatrolFleetData;
import data.scripts.campaign.econ.II_ImperialGuardHQ.IGPatrolType;
import java.util.List;
import java.util.Random;

public class II_IGPatrolAssignmentAI extends PatrolAssignmentAIV4 {

    public II_IGPatrolAssignmentAI(CampaignFleetAPI fleet, RouteData route) {
        super(fleet, route);
    }

    /* The only difference here is IGPatrolFleetData and IGPatrolType instead of PatrolFleetData and PatrolType */
    @Override
    public SectorEntityToken pickEntityToGuard() {
        Random random = route.getRandom(1);

        IGPatrolFleetData custom = (IGPatrolFleetData) route.getCustom();
        IGPatrolType type = custom.type;

        LocationAPI loc = fleet.getContainingLocation();
        if (loc == null) {
            return null;
        }

        WeightedRandomPicker<SectorEntityToken> picker = new WeightedRandomPicker<>(random);

        CountingMap<SectorEntityToken> existing = new CountingMap<>();
        for (RouteData data : RouteManager.getInstance().getRoutesForSource(route.getSource())) {
            CampaignFleetAPI other = data.getActiveFleet();
            if (other == null) {
                continue;
            }
            FleetAssignmentDataAPI curr = other.getCurrentAssignment();
            if (curr == null || curr.getTarget() == null
                    || curr.getAssignment() != FleetAssignment.PATROL_SYSTEM) {
                continue;
            }
            existing.add(curr.getTarget());
        }

        List<MarketAPI> markets = Misc.getMarketsInLocation(fleet.getContainingLocation());
        int hostileMax = 0;
        int ourMax = 0;
        for (MarketAPI market : markets) {
            if (market.getFaction().isHostileTo(fleet.getFaction())) {
                hostileMax = Math.max(hostileMax, market.getSize());
            } else if (market.getFaction() == fleet.getFaction()) {
                ourMax = Math.max(ourMax, market.getSize());
            }
        }
        boolean inControl = ourMax > hostileMax;

        for (SectorEntityToken entity : loc.getEntitiesWithTag(Tags.OBJECTIVE)) {
            if (entity.getFaction() != fleet.getFaction()) {
                continue;
            }

            float w = 2f;
            for (int i = 0; i < existing.getCount(entity); i++) {
                w *= 0.1f;
            }

            if (type == IGPatrolType.HEAVY) {
                w *= 0.1f;
            }

            picker.add(entity, w);
        }

        // patrol stable locations, will build there
        for (SectorEntityToken entity : loc.getEntitiesWithTag(Tags.STABLE_LOCATION)) {
            float w = 2f;
            for (int i = 0; i < existing.getCount(entity); i++) {
                w *= 0.1f;
            }

            if (type == IGPatrolType.HEAVY) {
                w *= 0.1f;
            }

            picker.add(entity, w);
        }

        if (inControl) {
            for (SectorEntityToken entity : loc.getJumpPoints()) {
                float w = 2f;
                for (int i = 0; i < existing.getCount(entity); i++) {
                    w *= 0.1f;
                }

                if (type == IGPatrolType.HEAVY) {
                    w *= 0.1f;
                }

                picker.add(entity, w);
            }

            if (loc instanceof StarSystemAPI && custom.type == IGPatrolType.HEAVY) {
                StarSystemAPI system = (StarSystemAPI) loc;
                if (system.getHyperspaceAnchor() != null) {
                    float w = 3f;
                    for (int i = 0; i < existing.getCount(system.getHyperspaceAnchor()); i++) {
                        w *= 0.1f;
                    }
                    picker.add(system.getHyperspaceAnchor(), w);
                }
            }
        }

        for (MarketAPI market : markets) {
            if (market.getFaction().isHostileTo(fleet.getFaction())) {
                continue;
            }

            float w = 0f;
            if (market == route.getMarket()) {
                w = 5f;
            } else {
                // defend on-hostile non-military markets; prefer own faction
                //if (!market.hasSubmarket(Submarkets.GENERIC_MILITARY)) {
                if (market.getMemoryWithoutUpdate().getBoolean(MemFlags.MARKET_PATROL)) {
                    if (market.getFaction() != fleet.getFaction()) {
                        w = 0f; // don't patrol near patrolHQ/military markets of another faction
                    } else {
                        w = 4f;
                    }
                }
            }

            for (int i = 0; i < existing.getCount(market.getPrimaryEntity()); i++) {
                w *= 0.1f;
            }
            picker.add(market.getPrimaryEntity(), w);
        }

        if (fleet.getContainingLocation() instanceof StarSystemAPI && type != IGPatrolType.HEAVY) {
            StarSystemAPI system = (StarSystemAPI) fleet.getContainingLocation();
            float w = 1f;
            for (int i = 0; i < existing.getCount(system.getCenter()); i++) {
                w *= 0.1f;
            }
            picker.add(system.getCenter(), w);
        }

        SectorEntityToken target = picker.pick();
        if (target == null) {
            target = route.getMarket().getPrimaryEntity();
        }

        return target;
    }
}
