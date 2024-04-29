package data.missions.ii_doomsday;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;
import java.util.List;
import org.lwjgl.util.vector.Vector2f;

public class MissionDefinition implements MissionDefinitionPlugin {

    @Override
    public void defineMission(MissionDefinitionAPI api) {
        // Set up the fleets
        api.initFleet(FleetSide.PLAYER, "ISA", FleetGoal.ATTACK, false);
        api.initFleet(FleetSide.ENEMY, "HSS", FleetGoal.ATTACK, true);

        // Set a small blurb for each fleet that shows up on the mission detail and
        // mission results screens to identify each side.
        api.setFleetTagline(FleetSide.PLAYER, "5th Imperial Fleet special operations unit");
        api.setFleetTagline(FleetSide.ENEMY, "Samsara station with Eventide defense fleet");

        // These show up as items in the bulleted list under
        // "Tactical Objectives" on the mission detail screen
        api.addBriefingItem("Defeat the enemy fleet.");
        api.addBriefingItem("The ISA Victoria is especially tough and can usually shrug off a Titan blast wave.");
        api.addBriefingItem("The Olympus-class is still a viable combat ship even after its payload is launched.");

        // Set up the player's fleet (150 DP)
        api.addToFleet(FleetSide.PLAYER, "ii_caesar_ass", FleetMemberType.SHIP, "ISA Victoria", true); // 50
        api.addToFleet(FleetSide.PLAYER, "ii_olympus_pb", FleetMemberType.SHIP, "ISA Annihilatio", false); // 50
        api.addToFleet(FleetSide.PLAYER, "ii_olympus_fb", FleetMemberType.SHIP, "ISA Contritio", false); // 50

        // Set up the enemy fleet (164 DP)
        FactionAPI hegemony = Global.getSettings().createBaseFaction(Factions.HEGEMONY);
        FleetMemberAPI member;
        api.addToFleet(FleetSide.ENEMY, "station1_Standard", FleetMemberType.SHIP, "Samsara Station", false); // 50

        //member = api.addToFleet(FleetSide.ENEMY, "onslaught_xiv_Elite", FleetMemberType.SHIP, false); // 40+7
        //member.setShipName(hegemony.pickRandomShipName());
        member = api.addToFleet(FleetSide.ENEMY, "dominator_Assault", FleetMemberType.SHIP, false); // 25
        member.setShipName(hegemony.pickRandomShipName());
        member = api.addToFleet(FleetSide.ENEMY, "dominator_Assault", FleetMemberType.SHIP, false); // 25
        member.setShipName(hegemony.pickRandomShipName());
        //member = api.addToFleet(FleetSide.ENEMY, "mora_Support", FleetMemberType.SHIP, false); // 20
        //member.setShipName(hegemony.pickRandomShipName());
        //member = api.addToFleet(FleetSide.ENEMY, "mora_Strike", FleetMemberType.SHIP, false); // 20
        //member.setShipName(hegemony.pickRandomShipName());

        member = api.addToFleet(FleetSide.ENEMY, "enforcer_Elite", FleetMemberType.SHIP, false); // 9
        member.setShipName(hegemony.pickRandomShipName());
        member = api.addToFleet(FleetSide.ENEMY, "enforcer_Elite", FleetMemberType.SHIP, false); // 9
        member.setShipName(hegemony.pickRandomShipName());
        member = api.addToFleet(FleetSide.ENEMY, "enforcer_Assault", FleetMemberType.SHIP, false); // 9
        member.setShipName(hegemony.pickRandomShipName());
        member = api.addToFleet(FleetSide.ENEMY, "enforcer_Assault", FleetMemberType.SHIP, false); // 9
        member.setShipName(hegemony.pickRandomShipName());

        member = api.addToFleet(FleetSide.ENEMY, "lasher_Strike", FleetMemberType.SHIP, false); // 4
        member.setShipName(hegemony.pickRandomShipName());
        member = api.addToFleet(FleetSide.ENEMY, "lasher_Assault", FleetMemberType.SHIP, false); // 4
        member.setShipName(hegemony.pickRandomShipName());
        member = api.addToFleet(FleetSide.ENEMY, "lasher_Assault", FleetMemberType.SHIP, false); // 4
        member.setShipName(hegemony.pickRandomShipName());
        member = api.addToFleet(FleetSide.ENEMY, "lasher_Standard", FleetMemberType.SHIP, false); // 4
        member.setShipName(hegemony.pickRandomShipName());
        member = api.addToFleet(FleetSide.ENEMY, "lasher_Standard", FleetMemberType.SHIP, false); // 4
        member.setShipName(hegemony.pickRandomShipName());
        member = api.addToFleet(FleetSide.ENEMY, "hound_hegemony_Standard", FleetMemberType.SHIP, false); // 3+1
        member.setShipName(hegemony.pickRandomShipName());
        member = api.addToFleet(FleetSide.ENEMY, "hound_hegemony_Standard", FleetMemberType.SHIP, false); // 3+1
        member.setShipName(hegemony.pickRandomShipName());
        //member = api.addToFleet(FleetSide.ENEMY, "kite_hegemony_Interceptor", FleetMemberType.SHIP, false); // 2+1
        //member.setShipName(hegemony.pickRandomShipName());
        //member = api.addToFleet(FleetSide.ENEMY, "kite_hegemony_Interceptor", FleetMemberType.SHIP, false); // 2+1
        //member.setShipName(hegemony.pickRandomShipName());

        // Set up the map.
        float width = 14000f;
        float height = 14000f;
        api.initMap(-width / 2f, width / 2f, -height / 2f, height / 2f);

        // Add an asteroid field going diagonally across the
        // battlefield, 2000 pixels wide, with a maximum of
        // 100 asteroids in it.
        // 20-70 is the range of asteroid speeds.
        api.addAsteroidField(0f, 0f, (float) Math.random() * 360f, width, 20f, 70f, 100);

        // Add some planets.  These are defined in data/config/planets.json.
        api.addPlanet(0.3f, 0.1f, 250f, "ii_eventide_mission", 0f, true);

        api.addPlugin(new Plugin());
    }

    private final static class Plugin extends BaseEveryFrameCombatPlugin {

        @Override
        public void advance(float amount, List<InputEventAPI> events) {
        }

        @Override
        public void init(CombatEngineAPI engine) {
            float width = 14000f;
            float height = 14000f;

            engine.getContext().setStandoffRange(8000f);

            for (FleetMemberAPI member : engine.getFleetManager(FleetSide.ENEMY).getReservesCopy()) {
                if (member.getHullId().contentEquals("station2")) {
                    engine.getFleetManager(FleetSide.ENEMY).spawnFleetMember(member, new Vector2f(0f, height / 6f), 270f, 0f);
                    break;
                }
            }
        }
    };
}
