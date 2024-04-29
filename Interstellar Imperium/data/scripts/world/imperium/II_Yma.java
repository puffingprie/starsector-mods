package data.scripts.world.imperium;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.intel.deciv.DecivTracker;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import data.scripts.IIModPlugin;
import java.util.ArrayList;
import java.util.Arrays;

import static data.scripts.world.imperium.II_Thracia.addMarketplace;

public class II_Yma {

    public void generate(SectorAPI sector) {
        StarSystemAPI system = sector.getStarSystem("Yma");

        SectorEntityToken embassy = system.addCustomEntity("ii_embassy", "Imperial Embassy", "station_side03",
                "interstellarimperium");
        embassy.setCircularOrbitPointingDown(system.getEntityById("huascar"), 0, 300, 100);
        embassy.setInteractionImage("illustrations", "hound_hangar");
        embassy.setCustomDescriptionId("ii_station_embassy");

        MarketAPI embassyMarket = addMarketplace("interstellarimperium", embassy,
                null,
                "Imperial Embassy", 3, // 1 industry limit
                new ArrayList<>(Arrays.asList(
                        Conditions.POPULATION_3,
                        "ii_imperialdoctrine")),
                new ArrayList<>(Arrays.asList(
                        new ArrayList<>(Arrays.asList(Industries.POPULATION)),
                        new ArrayList<>(Arrays.asList(Industries.SPACEPORT)),
                        new ArrayList<>(Arrays.asList(Industries.HEAVYBATTERIES)),
                        new ArrayList<>(Arrays.asList(Industries.WAYSTATION)),
                        new ArrayList<>(Arrays.asList(Industries.PATROLHQ)),
                        new ArrayList<>(Arrays.asList("commerce")), // Industry
                        new ArrayList<>(Arrays.asList("ii_orbitalstation", Commodities.ALPHA_CORE)))),
                new ArrayList<>(Arrays.asList(
                        Submarkets.SUBMARKET_BLACK,
                        Submarkets.SUBMARKET_OPEN,
                        Submarkets.SUBMARKET_STORAGE)),
                0.3f,
                true);

        if (IIModPlugin.isExerelin) {
            embassyMarket.getMemoryWithoutUpdate().set("$nex_colony_growth_limit", 3);
            Misc.makeStoryCritical(embassyMarket, "ii_no_invade");
            system.addScript(new HandleEmbassy(embassyMarket));
        }
    }

    public static class HandleEmbassy implements EveryFrameScript {

        private final MarketAPI embassy;
        private final IntervalUtil interval = new IntervalUtil(1f, 2f);
        private boolean dead = false;

        HandleEmbassy(MarketAPI market) {
            this.embassy = market;
        }

        @Override
        public void advance(float amount) {
            interval.advance(Global.getSector().getClock().convertToDays(amount));
            if (!interval.intervalElapsed()) {
                return;
            }

            if (embassy.isPlanetConditionMarketOnly()) {
                dead = true;
                return;
            }

            if (!embassy.getFactionId().contentEquals("interstellarimperium")) {
                return;
            }

            boolean imperiumAlive = false;
            for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
                if (market.isPlanetConditionMarketOnly()) {
                    continue;
                }
                if (market.getId().contentEquals(embassy.getId())) {
                    continue;
                }
                if (market.getFactionId().contentEquals("interstellarimperium")) {
                    imperiumAlive = true;
                    break;
                }
            }

            if (!imperiumAlive) {
                DecivTracker.decivilize(embassy, true, true);
                dead = true;
                return;
            }

            boolean alliesPresent = false;
            for (MarketAPI market : Global.getSector().getEconomy().getMarkets(embassy.getContainingLocation())) {
                if (market.isPlanetConditionMarketOnly()) {
                    continue;
                }
                if (market.getId().contentEquals(embassy.getId())) {
                    continue;
                }
                if (market.getFactionId().contentEquals(Factions.PERSEAN)) {
                    alliesPresent = true;
                    break;
                }
                FactionAPI faction = market.getFaction();
                if (faction.getRelationshipLevel("interstellarimperium").isAtWorst(RepLevel.FRIENDLY)) {
                    alliesPresent = true;
                    break;
                }
            }

            if (alliesPresent) {
                if (!Misc.isStoryCritical(embassy)) {
                    Misc.makeStoryCritical(embassy, "ii_no_invade");
                }
            } else {
                if (Misc.isStoryCritical(embassy)) {
                    Misc.makeNonStoryCritical(embassy, "ii_no_invade");
                }
            }
        }

        @Override
        public boolean isDone() {
            return dead;
        }

        @Override
        public boolean runWhilePaused() {
            return false;
        }
    }
}
