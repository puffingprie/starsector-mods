package data.missions.ii_almamater;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;

public class MissionDefinition implements MissionDefinitionPlugin {

    static int missionType = 0;

    @Override
    public void defineMission(MissionDefinitionAPI api) {
        // Set up the fleets
        api.initFleet(FleetSide.PLAYER, "IAC", FleetGoal.ATTACK, false); // Imperial Academy

        // Set a small blurb for each fleet that shows up on the mission detail and
        // mission results screens to identify each side.
        switch (missionType) {
            case 0:
                api.initFleet(FleetSide.ENEMY, "HSS", FleetGoal.ATTACK, true);
                api.setFleetTagline(FleetSide.PLAYER, "Imperial Academy Trial - Traditional");
                api.setFleetTagline(FleetSide.ENEMY, "Simulated Hegemony detachment");
                break;
            case 1:
                api.initFleet(FleetSide.ENEMY, "TTS", FleetGoal.ATTACK, true);
                api.setFleetTagline(FleetSide.PLAYER, "Imperial Academy Trial - Fortified");
                api.setFleetTagline(FleetSide.ENEMY, "Simulated Tri-Tachyon security patrol");
                break;
            case 2:
                api.initFleet(FleetSide.ENEMY, "PLS", FleetGoal.ATTACK, true);
                api.setFleetTagline(FleetSide.PLAYER, "Imperial Academy Trial - Standoff");
                api.setFleetTagline(FleetSide.ENEMY, "Simulated Persean League patrol");
                break;
            default:
                break;
        }

        // These show up as items in the bulleted list under
        // "Tactical Objectives" on the mission detail screen
        api.addBriefingItem("Defeat the enemy fleet.");
        api.addBriefingItem("Click on the mission in the list to select a different scenario.");

        FactionAPI imperium = Global.getSettings().createBaseFaction("interstellarimperium");
        FleetMemberAPI member;
        switch (missionType) {
            case 0: {
                // Set up the player's fleet (132 DP)
                member = api.addToFleet(FleetSide.PLAYER, "ii_dominus_bal", FleetMemberType.SHIP, true); // 45
                member.setShipName(imperium.pickRandomShipName().replaceFirst("ISA", "IAC"));
                member = api.addToFleet(FleetSide.PLAYER, "ii_sebastos_sta", FleetMemberType.SHIP, false); // 20
                member.setShipName(imperium.pickRandomShipName().replaceFirst("ISA", "IAC"));
                member = api.addToFleet(FleetSide.PLAYER, "ii_princeps_sta", FleetMemberType.SHIP, false); // 14
                member.setShipName(imperium.pickRandomShipName().replaceFirst("ISA", "IAC"));
                member = api.addToFleet(FleetSide.PLAYER, "ii_legionary_bal", FleetMemberType.SHIP, false); // 14
                member.setShipName(imperium.pickRandomShipName().replaceFirst("ISA", "IAC"));
                member = api.addToFleet(FleetSide.PLAYER, "ii_praetorian_sta", FleetMemberType.SHIP, false); // 10
                member.setShipName(imperium.pickRandomShipName().replaceFirst("ISA", "IAC"));
                member = api.addToFleet(FleetSide.PLAYER, "ii_maximus_sta", FleetMemberType.SHIP, false); // 9
                member.setShipName(imperium.pickRandomShipName().replaceFirst("ISA", "IAC"));
                member = api.addToFleet(FleetSide.PLAYER, "ii_triarius_sta", FleetMemberType.SHIP, false); // 5
                member.setShipName(imperium.pickRandomShipName().replaceFirst("ISA", "IAC"));
                member = api.addToFleet(FleetSide.PLAYER, "ii_decurion_sta", FleetMemberType.SHIP, false); // 5
                member.setShipName(imperium.pickRandomShipName().replaceFirst("ISA", "IAC"));
                member = api.addToFleet(FleetSide.PLAYER, "ii_decurion_sta", FleetMemberType.SHIP, false); // 5
                member.setShipName(imperium.pickRandomShipName().replaceFirst("ISA", "IAC"));
                member = api.addToFleet(FleetSide.PLAYER, "ii_basileus_sta", FleetMemberType.SHIP, false); // 5
                member.setShipName(imperium.pickRandomShipName().replaceFirst("ISA", "IAC"));
                break;
            }
            case 1: {
                // Set up the player's fleet (45 DP)
                member = api.addToFleet(FleetSide.PLAYER, "ii_ixon_str", FleetMemberType.SHIP, true); // 20
                member.setShipName(imperium.pickRandomShipName().replaceFirst("ISA", "IAC"));
                member = api.addToFleet(FleetSide.PLAYER, "ii_praetorian_ass", FleetMemberType.SHIP, false); // 10
                member.setShipName(imperium.pickRandomShipName().replaceFirst("ISA", "IAC"));
                member = api.addToFleet(FleetSide.PLAYER, "ii_triarius_ass", FleetMemberType.SHIP, false); // 5
                member.setShipName(imperium.pickRandomShipName().replaceFirst("ISA", "IAC"));
                member = api.addToFleet(FleetSide.PLAYER, "ii_decurion_ass", FleetMemberType.SHIP, false); // 5
                member.setShipName(imperium.pickRandomShipName().replaceFirst("ISA", "IAC"));
                member = api.addToFleet(FleetSide.PLAYER, "ii_basileus_ass", FleetMemberType.SHIP, false); // 5
                member.setShipName(imperium.pickRandomShipName().replaceFirst("ISA", "IAC"));
                break;
            }
            case 2: {
                // Set up the player's fleet (90 DP)
                member = api.addToFleet(FleetSide.PLAYER, "ii_dictator_art", FleetMemberType.SHIP, true); // 28
                member.setShipName(imperium.pickRandomShipName().replaceFirst("ISA", "IAC"));
                member = api.addToFleet(FleetSide.PLAYER, "ii_ardea_str", FleetMemberType.SHIP, false); // 22
                member.setShipName(imperium.pickRandomShipName().replaceFirst("ISA", "IAC"));
                member = api.addToFleet(FleetSide.PLAYER, "ii_interrex_cs", FleetMemberType.SHIP, false); // 10
                member.setShipName(imperium.pickRandomShipName().replaceFirst("ISA", "IAC"));
                member = api.addToFleet(FleetSide.PLAYER, "ii_praetorian_art", FleetMemberType.SHIP, false); // 10
                member.setShipName(imperium.pickRandomShipName().replaceFirst("ISA", "IAC"));
                member = api.addToFleet(FleetSide.PLAYER, "ii_triarius_cs", FleetMemberType.SHIP, false); // 5
                member.setShipName(imperium.pickRandomShipName().replaceFirst("ISA", "IAC"));
                member = api.addToFleet(FleetSide.PLAYER, "ii_triarius_cs", FleetMemberType.SHIP, false); // 5
                member.setShipName(imperium.pickRandomShipName().replaceFirst("ISA", "IAC"));
                member = api.addToFleet(FleetSide.PLAYER, "ii_basileus_cs", FleetMemberType.SHIP, false); // 5
                member.setShipName(imperium.pickRandomShipName().replaceFirst("ISA", "IAC"));
                member = api.addToFleet(FleetSide.PLAYER, "ii_basileus_cs", FleetMemberType.SHIP, false); // 5
                member.setShipName(imperium.pickRandomShipName().replaceFirst("ISA", "IAC"));
                break;
            }
            default:
                break;
        }

        switch (missionType) {
            case 0: {
                // Set up the enemy fleet (147 [75 x 1.75 = 131] DP)
                FactionAPI hegemony = Global.getSettings().createBaseFaction(Factions.HEGEMONY);
                member = api.addToFleet(FleetSide.ENEMY, "onslaught_Elite", FleetMemberType.SHIP, false); // 40
                member.setShipName(hegemony.pickRandomShipName());
                member = api.addToFleet(FleetSide.ENEMY, "dominator_Assault", FleetMemberType.SHIP, false); // 25
                member.setShipName(hegemony.pickRandomShipName());
                member = api.addToFleet(FleetSide.ENEMY, "eagle_Balanced", FleetMemberType.SHIP, false); // 22
                member.setShipName(hegemony.pickRandomShipName());
                member = api.addToFleet(FleetSide.ENEMY, "condor_Strike", FleetMemberType.SHIP, false); // 10
                member.setShipName(hegemony.pickRandomShipName());
                member = api.addToFleet(FleetSide.ENEMY, "condor_Attack", FleetMemberType.SHIP, false); // 10
                member.setShipName(hegemony.pickRandomShipName());
                member = api.addToFleet(FleetSide.ENEMY, "condor_Support", FleetMemberType.SHIP, false); // 10
                member.setShipName(hegemony.pickRandomShipName());
                member = api.addToFleet(FleetSide.ENEMY, "enforcer_Assault", FleetMemberType.SHIP, false); // 9
                member.setShipName(hegemony.pickRandomShipName());
                member = api.addToFleet(FleetSide.ENEMY, "enforcer_Balanced", FleetMemberType.SHIP, false); // 9
                member.setShipName(hegemony.pickRandomShipName());
                member = api.addToFleet(FleetSide.ENEMY, "lasher_Assault", FleetMemberType.SHIP, false); // 4
                member.setShipName(hegemony.pickRandomShipName());
                member = api.addToFleet(FleetSide.ENEMY, "lasher_CS", FleetMemberType.SHIP, false); // 4
                member.setShipName(hegemony.pickRandomShipName());
                member = api.addToFleet(FleetSide.ENEMY, "lasher_Strike", FleetMemberType.SHIP, false); // 4
                member.setShipName(hegemony.pickRandomShipName());
                break;
            }
            case 1: {
                // Set up the enemy fleet (48 [25 x 1.75 = 44] DP)
                FactionAPI tritachyon = Global.getSettings().createBaseFaction(Factions.TRITACHYON);
                member = api.addToFleet(FleetSide.ENEMY, "drover_Strike", FleetMemberType.SHIP, false); // 12
                member.setShipName(tritachyon.pickRandomShipName());
                member = api.addToFleet(FleetSide.ENEMY, "medusa_Attack", FleetMemberType.SHIP, false); // 12
                member.setShipName(tritachyon.pickRandomShipName());
                member = api.addToFleet(FleetSide.ENEMY, "shrike_Attack", FleetMemberType.SHIP, false); // 8
                member.setShipName(tritachyon.pickRandomShipName());
                member = api.addToFleet(FleetSide.ENEMY, "afflictor_Strike", FleetMemberType.SHIP, false); // 8
                member.setShipName(tritachyon.pickRandomShipName());
                member = api.addToFleet(FleetSide.ENEMY, "tempest_Attack", FleetMemberType.SHIP, false); // 8
                member.setShipName(tritachyon.pickRandomShipName());
                break;
            }
            case 2: {
                // Set up the enemy fleet (96 [45 x 2 = 90] DP)
                FactionAPI persean = Global.getSettings().createBaseFaction(Factions.PERSEAN);
                member = api.addToFleet(FleetSide.ENEMY, "heron_Strike", FleetMemberType.SHIP, false); // 20
                member.setShipName(persean.pickRandomShipName());
                member = api.addToFleet(FleetSide.ENEMY, "gryphon_Standard", FleetMemberType.SHIP, false); // 20
                member.setShipName(persean.pickRandomShipName());
                member = api.addToFleet(FleetSide.ENEMY, "sunder_Assault", FleetMemberType.SHIP, false); // 11
                member.setShipName(persean.pickRandomShipName());
                member = api.addToFleet(FleetSide.ENEMY, "hammerhead_Balanced", FleetMemberType.SHIP, false); // 10
                member.setShipName(persean.pickRandomShipName());
                member = api.addToFleet(FleetSide.ENEMY, "gemini_Standard", FleetMemberType.SHIP, false); // 9
                member.setShipName(persean.pickRandomShipName());
                member = api.addToFleet(FleetSide.ENEMY, "shade_Assault", FleetMemberType.SHIP, false); // 8
                member.setShipName(persean.pickRandomShipName());
                member = api.addToFleet(FleetSide.ENEMY, "wolf_Assault", FleetMemberType.SHIP, false); // 5
                member.setShipName(persean.pickRandomShipName());
                member = api.addToFleet(FleetSide.ENEMY, "wolf_Assault", FleetMemberType.SHIP, false); // 5
                member.setShipName(persean.pickRandomShipName());
                member = api.addToFleet(FleetSide.ENEMY, "brawler_Assault", FleetMemberType.SHIP, false); // 4
                member.setShipName(persean.pickRandomShipName());
                member = api.addToFleet(FleetSide.ENEMY, "brawler_Elite", FleetMemberType.SHIP, false); // 4
                member.setShipName(persean.pickRandomShipName());
                break;
            }
            default:
                break;
        }

        // Set up the map.
        float width = 20000f;
        float height = 24000f;
        int nebulas = 40;
        switch (missionType) {
            case 0:
                width = 20000f;
                height = 24000f;
                nebulas = 40;
                break;
            case 1:
                width = 14000f;
                height = 16000f;
                nebulas = 20;
                break;
            case 2:
                width = 17000f;
                height = 20000f;
                nebulas = 30;
                break;
            default:
                break;
        }
        api.initMap(-width / 2f, width / 2f, -height / 2f, height / 2f);

        float minX = -width / 2;
        float minY = -height / 2;

        api.setNebulaTex("graphics/imperium/terrain/ii_nebula_orange.png");
        api.setNebulaMapTex("graphics/imperium/terrain/ii_nebula_orange_map.png");
        for (int i = 0; i < nebulas; i++) {
            float x = (float) Math.random() * width - width / 2;
            float y = (float) Math.random() * height - height / 2;
            float radius = 400f + (float) Math.random() * 400f;
            api.addNebula(x, y, radius);
        }

        // Add objectives
        switch (missionType) {
            case 0:
                api.addObjective(minX + width * 0.25f, minY + height * 0.25f, "nav_buoy");
                api.addObjective(minX + width * 0.75f, minY + height * 0.25f, "comm_relay");
                api.addObjective(minX + width * 0.75f, minY + height * 0.75f, "nav_buoy");
                api.addObjective(minX + width * 0.25f, minY + height * 0.75f, "comm_relay");
                api.addObjective(minX + width * 0.5f, minY + height * 0.5f, "sensor_array");
                break;
            case 1:
                api.addObjective(minX + width * 0.25f, minY + height * 0.25f, "nav_buoy");
                api.addObjective(minX + width * 0.75f, minY + height * 0.75f, "nav_buoy");
                api.addObjective(minX + width * 0.5f, minY + height * 0.5f, "sensor_array");
                break;
            case 2:
                api.addObjective(minX + width * 0.25f, minY + height * 0.25f, "nav_buoy");
                api.addObjective(minX + width * 0.75f, minY + height * 0.25f, "comm_relay");
                api.addObjective(minX + width * 0.75f, minY + height * 0.75f, "sensor_array");
                api.addObjective(minX + width * 0.25f, minY + height * 0.75f, "comm_relay");
                break;
            default:
                break;
        }

        // Add some planets.  These are defined in data/config/planets.json.
        api.addPlanet(0.3f, 0.1f, 250f, "ii_byzantium_mission", 0f, true);

        missionType++;
        if (missionType > 2) {
            missionType = 0;
        }
    }
}
