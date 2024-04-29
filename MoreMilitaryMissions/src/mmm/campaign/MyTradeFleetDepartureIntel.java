package mmm.campaign;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.campaign.comm.IntelManagerAPI;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteData;
import com.fs.starfarer.api.impl.campaign.intel.misc.TradeFleetDepartureIntel;
import mmm.missions.EscortMission;

import java.util.ArrayList;
import java.util.HashSet;

// Same as TradeFleetDepartureIntel, but adds intel unconditionally to the IntelManagerAPI and does not check location.
public class MyTradeFleetDepartureIntel extends TradeFleetDepartureIntel {
    // Periodically scans for trade departures and ensure that they are added to intel.
    public static class Injector implements EveryFrameScript {
        protected float days = 0f;
        @Override
        public boolean isDone() { return false; }
        @Override
        public boolean runWhilePaused() { return false; }

        @Override
        public void advance(float amount) {
            days -= Global.getSector().getClock().convertToDays(amount);
            if (days > 0f) return;

            IntelManagerAPI manager = Global.getSector().getIntelManager();
            if (!manager.isPlayerInRangeOfCommRelay()) return;

            days = 1f;

            HashSet<RouteData> eligible_routes = new HashSet<>();
            for (RouteData route : EscortMission.findEligibleTradeRoutes()) {
                if (route.getDelay() <= 0f) continue;
                eligible_routes.add(route);
            }

            // If the route is already in intel, nothing to do.
            for (IntelInfoPlugin plugin : manager.getIntel(TradeFleetDepartureIntel.class)) {
                if (plugin instanceof TradeFleetDepartureIntel) {
                    TradeFleetDepartureIntel intel = (TradeFleetDepartureIntel) plugin;
                    eligible_routes.remove(intel.getRoute());
                }
            }

            // Since we're in comm relay range, force add queued intel regardless of distance.
            ArrayList<IntelInfoPlugin> queued = new ArrayList<>(manager.getCommQueue(TradeFleetDepartureIntel.class));
            for (IntelInfoPlugin plugin : queued) {
                if (plugin instanceof TradeFleetDepartureIntel) {
                    TradeFleetDepartureIntel intel = (TradeFleetDepartureIntel) plugin;
                    if (eligible_routes.remove(intel.getRoute())) {
                        intel.setForceAddNextFrame(true);
                    }
                }
            }

            // Now add all eligible routes not already in manager.
            for (RouteData route : eligible_routes) {
                new MyTradeFleetDepartureIntel(route);
            }
        }
    }

    @Override
    public boolean canMakeVisibleToPlayer(boolean playerInRelayRange) {
        return playerInRelayRange && !isEnding();
    }

    public MyTradeFleetDepartureIntel(RouteManager.RouteData route) {
        super(route);
        IntelManagerAPI manager = Global.getSector().getIntelManager();
        // super(route) might have already queued the intel; queueing it again would be NOOP.
        manager.queueIntel(this);
        setForceAddNextFrame(true);
    }
}
