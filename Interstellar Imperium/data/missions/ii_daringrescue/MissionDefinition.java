package data.missions.ii_daringrescue;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.BattleCreationContext;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.impl.combat.EscapeRevealPlugin;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;
import java.util.List;

public class MissionDefinition implements MissionDefinitionPlugin {

    @Override
    public void defineMission(MissionDefinitionAPI api) {
        // Set up the fleets
        api.initFleet(FleetSide.PLAYER, "IG", FleetGoal.ESCAPE, false, 4);
        api.initFleet(FleetSide.ENEMY, "", FleetGoal.ATTACK, true, 5);

        // Set a small blurb for each fleet that shows up on the mission detail and
        // mission results screens to identify each side.
        api.setFleetTagline(FleetSide.PLAYER, "Imperium relief convoy with Imperial Guard escorts");
        api.setFleetTagline(FleetSide.ENEMY, "Hegemony-sponsored Luddic Path raiders");

        // These show up as items in the bulleted list under
        // "Tactical Objectives" on the mission detail screen
        api.addBriefingItem("At least 25% of the Imperial forces must escape");
        api.addBriefingItem("The elite escorts are powerful, but quickly run out of Combat Readiness");

        /* VIP and escorts (109 DP) */
        FactionAPI imperialGuard = Global.getSettings().createBaseFaction("ii_imperial_guard");
        FleetMemberAPI member;
        api.addToFleet(FleetSide.PLAYER, "ii_adamas_eli", FleetMemberType.SHIP, "IG Clementia Omnis", true); // 35
        member = api.addToFleet(FleetSide.PLAYER, "ii_legionary_eli", FleetMemberType.SHIP, false); // 14
        member.getCaptain().setPersonality(Personalities.AGGRESSIVE);
        member.setShipName(imperialGuard.pickRandomShipName());
        member = api.addToFleet(FleetSide.PLAYER, "ii_legionary_eli", FleetMemberType.SHIP, false); // 14
        member.getCaptain().setPersonality(Personalities.AGGRESSIVE);
        member.setShipName(imperialGuard.pickRandomShipName());
        member = api.addToFleet(FleetSide.PLAYER, "ii_decurion_eli", FleetMemberType.SHIP, false); // 5
        member.getCaptain().setPersonality(Personalities.AGGRESSIVE);
        member.setShipName(imperialGuard.pickRandomShipName());
        member = api.addToFleet(FleetSide.PLAYER, "ii_decurion_eli", FleetMemberType.SHIP, false); // 5
        member.getCaptain().setPersonality(Personalities.AGGRESSIVE);
        member.setShipName(imperialGuard.pickRandomShipName());
        member = api.addToFleet(FleetSide.PLAYER, "ii_decurion_eli", FleetMemberType.SHIP, false); // 5
        member.getCaptain().setPersonality(Personalities.AGGRESSIVE);
        member.setShipName(imperialGuard.pickRandomShipName());
        member = api.addToFleet(FleetSide.PLAYER, "ii_decurion_eli", FleetMemberType.SHIP, false); // 5
        member.getCaptain().setPersonality(Personalities.AGGRESSIVE);
        member.setShipName(imperialGuard.pickRandomShipName());

        /* Convoy (helpless) */
        FactionAPI imperium = Global.getSettings().createBaseFaction("interstellarimperium");
        member = api.addToFleet(FleetSide.PLAYER, "starliner_Standard", FleetMemberType.SHIP, false); // 10 * 0.5
        member.getCaptain().setPersonality(Personalities.TIMID);
        member.setShipName(imperium.pickRandomShipName());
        member = api.addToFleet(FleetSide.PLAYER, "tarsus_Standard", FleetMemberType.SHIP, false); // 3 * 0.5
        member.getCaptain().setPersonality(Personalities.TIMID);
        member.setShipName(imperium.pickRandomShipName());
        member = api.addToFleet(FleetSide.PLAYER, "tarsus_Standard", FleetMemberType.SHIP, false); // 3 * 0.5
        member.getCaptain().setPersonality(Personalities.TIMID);
        member.setShipName(imperium.pickRandomShipName());
        member = api.addToFleet(FleetSide.PLAYER, "tarsus_Standard", FleetMemberType.SHIP, false); // 3 * 0.5
        member.getCaptain().setPersonality(Personalities.TIMID);
        member.setShipName(imperium.pickRandomShipName());
        member = api.addToFleet(FleetSide.PLAYER, "tarsus_Standard", FleetMemberType.SHIP, false); // 3 * 0.5
        member.getCaptain().setPersonality(Personalities.TIMID);
        member.setShipName(imperium.pickRandomShipName());
        member = api.addToFleet(FleetSide.PLAYER, "phaeton_Standard", FleetMemberType.SHIP, false); // 3 * 0.5
        member.getCaptain().setPersonality(Personalities.TIMID);
        member.setShipName(imperium.pickRandomShipName());
        member = api.addToFleet(FleetSide.PLAYER, "phaeton_Standard", FleetMemberType.SHIP, false); // 3 * 0.5
        member.getCaptain().setPersonality(Personalities.TIMID);
        member.setShipName(imperium.pickRandomShipName());
        member = api.addToFleet(FleetSide.PLAYER, "ii_carrum_sta", FleetMemberType.SHIP, false); // 3 * 0.5
        member.getCaptain().setPersonality(Personalities.TIMID);
        member.setShipName(imperium.pickRandomShipName());
        member = api.addToFleet(FleetSide.PLAYER, "ii_carrum_sta", FleetMemberType.SHIP, false); // 3 * 0.5
        member.getCaptain().setPersonality(Personalities.TIMID);
        member.setShipName(imperium.pickRandomShipName());
        member = api.addToFleet(FleetSide.PLAYER, "ii_carrum_sta", FleetMemberType.SHIP, false); // 3 * 0.5
        member.getCaptain().setPersonality(Personalities.TIMID);
        member.setShipName(imperium.pickRandomShipName());
        member = api.addToFleet(FleetSide.PLAYER, "ii_carrum_sta", FleetMemberType.SHIP, false); // 3 * 0.5
        member.getCaptain().setPersonality(Personalities.TIMID);
        member.setShipName(imperium.pickRandomShipName());
        member = api.addToFleet(FleetSide.PLAYER, "ii_carrum_per", FleetMemberType.SHIP, false); // 3 * 0.5
        member.getCaptain().setPersonality(Personalities.TIMID);
        member.setShipName(imperium.pickRandomShipName());
        member = api.addToFleet(FleetSide.PLAYER, "ii_carrum_per", FleetMemberType.SHIP, false); // 3 * 0.5
        member.getCaptain().setPersonality(Personalities.TIMID);
        member.setShipName(imperium.pickRandomShipName());
        member = api.addToFleet(FleetSide.PLAYER, "ii_carrum_fue", FleetMemberType.SHIP, false); // 3 * 0.5
        member.getCaptain().setPersonality(Personalities.TIMID);
        member.setShipName(imperium.pickRandomShipName());
        member = api.addToFleet(FleetSide.PLAYER, "ii_carrum_fue", FleetMemberType.SHIP, false); // 3 * 0.5
        member.getCaptain().setPersonality(Personalities.TIMID);
        member.setShipName(imperium.pickRandomShipName());

        /* Main attack force (135 DP) */
        FactionAPI luddicPath = Global.getSettings().createBaseFaction(Factions.LUDDIC_PATH);
        member = api.addToFleet(FleetSide.ENEMY, "prometheus2_Standard", FleetMemberType.SHIP, false); // 32
        member.setShipName(luddicPath.pickRandomShipName());
        member = api.addToFleet(FleetSide.ENEMY, "colossus2_Pather", FleetMemberType.SHIP, false); // 9
        member.setShipName(luddicPath.pickRandomShipName());
        member = api.addToFleet(FleetSide.ENEMY, "enforcer_Overdriven", FleetMemberType.SHIP, false); // 9
        member.setShipName(luddicPath.pickRandomShipName());
        member = api.addToFleet(FleetSide.ENEMY, "enforcer_Overdriven", FleetMemberType.SHIP, false); // 9
        member.setShipName(luddicPath.pickRandomShipName());
        member = api.addToFleet(FleetSide.ENEMY, "hammerhead_Overdriven", FleetMemberType.SHIP, false); // 10
        member.setShipName(luddicPath.pickRandomShipName());
        member = api.addToFleet(FleetSide.ENEMY, "hammerhead_Overdriven", FleetMemberType.SHIP, false); // 10
        member.setShipName(luddicPath.pickRandomShipName());
        member = api.addToFleet(FleetSide.ENEMY, "sunder_Overdriven", FleetMemberType.SHIP, false); // 11
        member.setShipName(luddicPath.pickRandomShipName());
        member = api.addToFleet(FleetSide.ENEMY, "sunder_Overdriven", FleetMemberType.SHIP, false); // 11
        member.setShipName(luddicPath.pickRandomShipName());

        /* Flanks */
        member = api.addToFleet(FleetSide.ENEMY, "brawler_pather_Raider", FleetMemberType.SHIP, false); // 4
        member.setShipName(luddicPath.pickRandomShipName());
        member = api.addToFleet(FleetSide.ENEMY, "brawler_pather_Raider", FleetMemberType.SHIP, false); // 4
        member.setShipName(luddicPath.pickRandomShipName());
        member = api.addToFleet(FleetSide.ENEMY, "cerberus_luddic_path_Attack", FleetMemberType.SHIP, false); // 4
        member.setShipName(luddicPath.pickRandomShipName());
        member = api.addToFleet(FleetSide.ENEMY, "cerberus_luddic_path_Attack", FleetMemberType.SHIP, false); // 4
        member.setShipName(luddicPath.pickRandomShipName());
        member = api.addToFleet(FleetSide.ENEMY, "hound_luddic_path_Attack", FleetMemberType.SHIP, false); // 3
        member.setShipName(luddicPath.pickRandomShipName());
        member = api.addToFleet(FleetSide.ENEMY, "hound_luddic_path_Attack", FleetMemberType.SHIP, false); // 3
        member.setShipName(luddicPath.pickRandomShipName());
        member = api.addToFleet(FleetSide.ENEMY, "kite_luddic_path_Raider", FleetMemberType.SHIP, false); // 2
        member.setShipName(luddicPath.pickRandomShipName());
        member = api.addToFleet(FleetSide.ENEMY, "kite_luddic_path_Strike", FleetMemberType.SHIP, false); // 2
        member.setShipName(luddicPath.pickRandomShipName());
        member = api.addToFleet(FleetSide.ENEMY, "lasher_luddic_path_Raider", FleetMemberType.SHIP, false); // 4
        member.setShipName(luddicPath.pickRandomShipName());
        member = api.addToFleet(FleetSide.ENEMY, "lasher_luddic_path_Raider", FleetMemberType.SHIP, false); // 4
        member.setShipName(luddicPath.pickRandomShipName());

        // Set up the map.
        float width = 15000f;
        float height = 30000f;
        api.initMap(-width / 2f, width / 2f, -height / 2f, height / 2f);

        /* Asteroid belts */
        api.addAsteroidField(0f, (height / 2f) * 0.05f, 20f, 4000f, 7.5f, 12.5f, 150);
        api.addRingAsteroids(0f, (height / 2f) * 0.06f, 20f, 8000f, 5f, 15f, 600);

        api.addAsteroidField(0f, (height / 2f) * -0.5f, 10f, 6000f, 5f, 7.5f, 300);

        api.addAsteroidField(0f, (height / 2f) * 0.6f, 30f, 5000f, 10f, 20f, 100);

        // Add objectives
        api.addObjective(0f, (height / 2f) * -0.75f, "nav_buoy");
        api.addNebula(0f, (height / 2f) * 0.05f, 3500f);
        api.addObjective(0f, (height / 2f) * 0.6f, "nav_buoy");
        api.addNebula(0f, (height / 2f) * 0.6f, 3000f);
        api.addObjective((width / 2f) * -0.65f, (height / 2f) * 0.4f, "sensor_array");
        api.addObjective((width / 2f) * 0.4f, (height / 2f) * -0.25f, "comm_relay");

        api.addPlanet(-0.6f, 0.4f, 75f, "lava", 0f, true);

        BattleCreationContext context = new BattleCreationContext(null, null, null, null);
        context.setInitialEscapeRange(7000f);
        api.addPlugin(new EscapeRevealPlugin(context));
        api.addPlugin(new Plugin());
    }

    private final static class Plugin extends BaseEveryFrameCombatPlugin {

        @Override
        public void advance(float amount, List<InputEventAPI> events) {
        }

        @Override
        public void init(CombatEngineAPI engine) {
            engine.getContext().setInitialEscapeRange(7000f);
            engine.getContext().setFlankDeploymentDistance(18000f);
        }
    };
}
