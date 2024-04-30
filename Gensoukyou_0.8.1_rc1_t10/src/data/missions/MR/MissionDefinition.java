package data.missions.MR;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;

public class MissionDefinition implements MissionDefinitionPlugin {

    private final List ships = new ArrayList();
    private final List FM_ships = new ArrayList();
    private final List<String> allIds = Global.getSettings().getAllVariantIds();
    private static int maxDP = 10;
    private static boolean FMPlayer = true;


    private final boolean hasFlagShip = false;

    private void addFMShip(List<String> variants) {
        for (String variant : variants) {

            if (!variant.startsWith("FM_")) continue;
            if (Global.getSettings().getVariant(variant).getHullSpec().getHints().contains(ShipHullSpecAPI.ShipTypeHints.HIDE_IN_CODEX))
                continue;
            ShipAPI.HullSize hullSize = Global.getSettings().getVariant(variant).getHullSize();
            if (hullSize == ShipAPI.HullSize.FIGHTER) continue;
            if (Global.getSettings().getVariant(variant).isStation() ||
                    Global.getSettings().getVariant(variant).isCivilian() ||
                    Global.getSettings().getVariant(variant).isTanker() ||
                    Global.getSettings().getVariant(variant).isLiner() ||
                    Global.getSettings().getVariant(variant).isTransport()) continue;

            if (Global.getSettings().getVariant(variant).getHullSize() == ShipAPI.HullSize.FRIGATE) {
                for (int i = 0; i < MathUtils.getRandomNumberInRange(6, 10); i = i + 1) {
                    FM_ships.add(variant);
                }
            }

            if (Global.getSettings().getVariant(variant).getHullSize() == ShipAPI.HullSize.DESTROYER) {
                for (int i = 0; i < MathUtils.getRandomNumberInRange(4, 8); i = i + 1) {
                    FM_ships.add(variant);
                }
            }

            if (Global.getSettings().getVariant(variant).getHullSize() == ShipAPI.HullSize.CRUISER) {
                for (int i = 0; i < MathUtils.getRandomNumberInRange(2, 6); i = i + 1) {
                    FM_ships.add(variant);
                }
            }

            if (Global.getSettings().getVariant(variant).getHullSize() == ShipAPI.HullSize.CAPITAL_SHIP) {
                for (int i = 0; i < MathUtils.getRandomNumberInRange(1, 2); i = i + 1) {
                    FM_ships.add(variant);
                }
            }
        }
    }

    private void addShip(String variant, int weight) {
        for (int i = 0; i < weight; i++) {
            ships.add(variant);
        }
    }

    private void generateFleet(int maxDP, FleetSide side, List ships, MissionDefinitionAPI api) {
        int currDP = 0;

        while (true) {
            if (ships.isEmpty()) return;
            int index = (int) (Math.random() * ships.size());
            String id = (String) ships.get(index);
            int dp = (int) Global.getSettings().getVariant(id).getHullSpec().getSuppliesToRecover();

            currDP = dp + currDP;
            if (currDP > maxDP) {
                return;
            }

            if (id.endsWith("_wing")) {
                api.addToFleet(side, id, FleetMemberType.FIGHTER_WING, false);
            } else {
                api.addToFleet(side, id, FleetMemberType.SHIP, !hasFlagShip && side == FleetSide.PLAYER);
            }
        }
    }

    public void defineMission(MissionDefinitionAPI api) {

        if (Keyboard.isKeyDown(Keyboard.KEY_1)) {
            maxDP = maxDP + 10;
            if (maxDP >= 300) {
                maxDP = 300;
            }
        } else if (Keyboard.isKeyDown(Keyboard.KEY_2)) {
            maxDP = maxDP - 10;
            if (maxDP <= 10) {
                maxDP = 10;
            }
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_3)) {
            FMPlayer = !FMPlayer;
        }

        addShip("doom_Strike", 5);
        addShip("shade_Assault", 7);
        addShip("afflictor_Strike", 7);
        addShip("hyperion_Attack", 3);
        addShip("hyperion_Strike", 3);
        addShip("onslaught_Standard", 4);
        addShip("onslaught_Outdated", 3);
        addShip("onslaught_Elite", 1);
        addShip("astral_Elite", 3);
        addShip("astral_Strike", 3);
        addShip("astral_Attack", 3);
        addShip("paragon_Elite", 4);
        addShip("legion_Strike", 2);
        addShip("legion_Assault", 3);
        addShip("legion_Escort", 2);
        addShip("legion_FS", 1);
        addShip("odyssey_Balanced", 2);
        addShip("conquest_Elite", 3);
        addShip("eagle_Assault", 5);
        addShip("falcon_Attack", 5);
        addShip("venture_Balanced", 5);
        addShip("apogee_Balanced", 5);
        addShip("aurora_Balanced", 7);
        addShip("aurora_Balanced", 7);
        addShip("gryphon_FS", 7);
        addShip("gryphon_Standard", 7);
        addShip("mora_Assault", 3);
        addShip("mora_Strike", 3);
        addShip("mora_Support", 3);
        addShip("dominator_Assault", 5);
        addShip("dominator_Support", 5);
        addShip("medusa_Attack", 5);
        addShip("condor_Support", 3);
        addShip("condor_Strike", 3);
        addShip("condor_Attack", 3);
        addShip("enforcer_Assault", 4);
        addShip("enforcer_CS", 4);
        addShip("hammerhead_Balanced", 10);
        addShip("hammerhead_Elite", 5);
        addShip("drover_Strike", 10);
        addShip("sunder_CS", 10);
        addShip("gemini_Standard", 8);
        addShip("buffalo2_FS", 1);
        addShip("lasher_CS", 3);
        addShip("lasher_Standard", 3);
        addShip("hound_Standard", 1);
        addShip("tempest_Attack", 20);
        addShip("brawler_Assault", 15);
        addShip("wolf_CS", 20);
        addShip("hyperion_Strike", 5);
        addShip("vigilance_Standard", 10);
        addShip("vigilance_FS", 15);
        addShip("tempest_Attack", 20);
        addShip("brawler_Assault", 10);
//		addShip("piranha_wing", 15);
//		addShip("talon_wing", 20);
//		addShip("broadsword_wing", 10);
//		addShip("mining_drone_wing", 10);
//		addShip("wasp_wing", 10);
//		addShip("xyphos_wing", 10);
//		addShip("longbow_wing", 10);
//		addShip("dagger_wing", 10);
//		addShip("thunder_wing", 5);
//		addShip("gladius_wing", 15);
//		addShip("warthog_wing", 5);

        addFMShip(allIds);


        // Set up the fleets so we can add ships and fighter wings to them.
        // In this scenario, the fleets are attacking each other, but
        // in other scenarios, a fleet may be defending or trying to escape
        if (FMPlayer) {

            api.initFleet(FleetSide.PLAYER, "GMS", FleetGoal.ATTACK, false, 5);
            api.initFleet(FleetSide.ENEMY, "TEST", FleetGoal.ATTACK, true, 5);

            // Set a small blurb for each fleet that shows up on the mission detail and
            // mission results screens to identify each side.
            api.setFleetTagline(FleetSide.PLAYER, "Gensoukyou Manufacture Simulation Fleet" + " || " + maxDP);
            api.setFleetTagline(FleetSide.ENEMY, "Simulation Enemy Fleet" + " || " + maxDP);

            // Set up the fleets
            generateFleet(maxDP, FleetSide.PLAYER, FM_ships, api);
            generateFleet(maxDP, FleetSide.ENEMY, ships, api);
        } else {
            api.initFleet(FleetSide.PLAYER, "TEST", FleetGoal.ATTACK, false, 5);
            api.initFleet(FleetSide.ENEMY, "GMS", FleetGoal.ATTACK, true, 5);

            api.setFleetTagline(FleetSide.PLAYER, "Sector Simulation Fleet" + " || " + maxDP);
            api.setFleetTagline(FleetSide.ENEMY, "Simulated Gensoukyou Manufacture Enemy Fleet" + " || " + maxDP);

            generateFleet(maxDP, FleetSide.PLAYER, ships, api);
            generateFleet(maxDP, FleetSide.ENEMY, FM_ships, api);

        }

        // These show up as items in the bulleted list under
        // "Tactical Objectives" on the mission detail screen
        api.addBriefingItem("Defeat Enemy");
        api.addBriefingItem("Clicking mission while holding number 1 or number 2 will increase size of each side. 1 increase deployment, 2 decrease deployment");
        api.addBriefingItem("Press 3 while clicking the mission to switch factions");

        // Set up the map.
        float width = 24000f;
        float height = 18000f;
        api.initMap(-width / 2f, width / 2f, -height / 2f, height / 2f);

        float minX = -width / 2;
        float minY = -height / 2;


        for (int i = 0; i < 50; i++) {
            float x = (float) Math.random() * width - width / 2;
            float y = (float) Math.random() * height - height / 2;
            float radius = 100f + (float) Math.random() * 400f;
            api.addNebula(x, y, radius);
        }

        // Add objectives
        api.addObjective(minX + width * 0.25f + 2000, minY + height * 0.25f + 2000, "nav_buoy");
        api.addObjective(minX + width * 0.75f - 2000, minY + height * 0.25f + 2000, "comm_relay");
        api.addObjective(minX + width * 0.75f - 2000, minY + height * 0.75f - 2000, "nav_buoy");
        api.addObjective(minX + width * 0.25f + 2000, minY + height * 0.75f - 2000, "comm_relay");
        api.addObjective(minX + width * 0.5f, minY + height * 0.5f, "sensor_array");

        String[] planets = {"barren", "terran", "gas_giant", "ice_giant", "cryovolcanic", "frozen", "jungle", "desert", "arid"};
        String planet = planets[(int) (Math.random() * (double) planets.length)];
        float radius = 100f + (float) Math.random() * 150f;
        api.addPlanet(0, 0, radius, planet, 200f, true);
    }

}





