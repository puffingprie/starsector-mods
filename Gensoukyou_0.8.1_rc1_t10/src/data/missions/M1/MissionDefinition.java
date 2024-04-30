package data.missions.M1;

import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;

public class MissionDefinition implements MissionDefinitionPlugin {

    public void defineMission(MissionDefinitionAPI api) {

        // Set up the fleets so we can add ships and fighter wings to them.
        // In this scenario, the fleets are attacking each other, but
        // in other scenarios, a fleet may be defending or trying to escape
        api.initFleet(FleetSide.PLAYER, "GMS", FleetGoal.ATTACK, false, 5);
        api.initFleet(FleetSide.ENEMY, "", FleetGoal.ATTACK, true);

        // Set a small blurb for each fleet that shows up on the mission detail and
        // mission results screens to identify each side.
        api.setFleetTagline(FleetSide.PLAYER, "Small Gift for Amanojaku");
        api.setFleetTagline(FleetSide.ENEMY, "Big Surprise for Amanojaku");

        // These show up as items in the bulleted list under
        // "Tactical Objectives" on the mission detail screen
        api.addBriefingItem("Fight for another days...");
        api.addBriefingItem("Let the blood rain!");
        api.addBriefingItem("Your ship wont last long against coordinated attack.");

        // Set up the player's fleet.  Variant names come from the
        // files in data/variants and data/variants/fighters

        // Set up the player's fleet.  Variant names come from the
        // files in data/variants and data/variants/fighters
        //api.addToFleet(FleetSide.PLAYER, "afflictor_Strike", FleetMemberType.SHIP, "ISS Black Star", true, CrewXPLevel.VETERAN);
        //api.addToFleet(FleetSide.PLAYER, "station_small_Standard", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "FM_Miko_Standard", FleetMemberType.SHIP, "GMS Gift", true);


        //fleetMember = api.addToFleet(FleetSide.PLAYER, "buffalo_tritachyon_Standard", FleetMemberType.SHIP, false);
        //fleetMember = api.addToFleet(FleetSide.PLAYER, "falcon_CS", FleetMemberType.SHIP, false);

//		api.addToFleet(FleetSide.PLAYER, "mining_drone_wing", FleetMemberType.FIGHTER_WING, false);
//		api.addToFleet(FleetSide.PLAYER, "mining_drone_wing", FleetMemberType.FIGHTER_WING, false);

//		api.addToFleet(FleetSide.PLAYER, "enforcer_Assault", FleetMemberType.SHIP, "ISS Hamatsu", true);
//		api.addToFleet(FleetSide.PLAYER, "medusa_PD", FleetMemberType.SHIP, false);
//		api.addToFleet(FleetSide.PLAYER, "omen_PD", FleetMemberType.SHIP, false);
//		api.addToFleet(FleetSide.PLAYER, "hyperion_Attack", FleetMemberType.SHIP, false);
//		api.addToFleet(FleetSide.PLAYER, "wolf_CS", FleetMemberType.SHIP, false);
//		api.addToFleet(FleetSide.PLAYER, "medusa_Attack", FleetMemberType.SHIP, false);
//		api.addToFleet(FleetSide.PLAYER, "tempest_Attack", FleetMemberType.SHIP, false);

        //api.addToFleet(FleetSide.PLAYER, "mining_drone_wing", FleetMemberType.FIGHTER_WING, false);
        //api.addToFleet(FleetSide.PLAYER, "mining_drone_wing", FleetMemberType.FIGHTER_WING, false);
        //api.addToFleet(FleetSide.PLAYER, "mining_drone_wing", FleetMemberType.FIGHTER_WING, false);
        //api.addToFleet(FleetSide.PLAYER, "longbow_wing", FleetMemberType.FIGHTER_WING, false);
        //api.addToFleet(FleetSide.PLAYER, "longbow_wing", FleetMemberType.FIGHTER_WING, false);
        //api.addToFleet(FleetSide.PLAYER, "longbow_wing", FleetMemberType.FIGHTER_WING, false);
        //api.addToFleet(FleetSide.PLAYER, "longbow_wing", FleetMemberType.FIGHTER_WING, false);

        // Mark both ships as essential - losing either one results
        // in mission failure. Could also be set on an enemy ship,
        // in which case destroying it would result in a win.


        // Set up the enemy fleet.
        // It's got more ships than the player's, but they're not as strong.
        //api.addToFleet(FleetSide.ENEMY, "station_small_Standard", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
        api.addToFleet(FleetSide.ENEMY, "prometheus2_Standard", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
        api.addToFleet(FleetSide.ENEMY, "eradicator_Overdriven", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
        api.addToFleet(FleetSide.ENEMY, "eradicator_Overdriven", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
        api.addToFleet(FleetSide.ENEMY, "condor_Attack", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
        api.addToFleet(FleetSide.ENEMY, "condor_Attack", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
        api.addToFleet(FleetSide.ENEMY, "enforcer_Overdriven", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
        api.addToFleet(FleetSide.ENEMY, "enforcer_Overdriven", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
        api.addToFleet(FleetSide.ENEMY, "hound_luddic_path_Attack", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
        api.addToFleet(FleetSide.ENEMY, "hound_luddic_path_Attack", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
        api.addToFleet(FleetSide.ENEMY, "brawler_pather_Raider", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);


//		api.addToFleet(FleetSide.ENEMY, "piranha_wing", FleetMemberType.FIGHTER_WING, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
//		api.addToFleet(FleetSide.ENEMY, "talon_wing", FleetMemberType.FIGHTER_WING, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
//		api.addToFleet(FleetSide.ENEMY, "talon_wing", FleetMemberType.FIGHTER_WING, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);


        //api.addToFleet(FleetSide.ENEMY, "hound_Standard", FleetMemberType.SHIP, false);
        //api.addToFleet(FleetSide.ENEMY, "broadsword_wing", FleetMemberType.FIGHTER_WING, false);

//		api.addToFleet(FleetSide.ENEMY, "mining_drone_wing", FleetMemberType.FIGHTER_WING, false).getCaptain().setPersonality("suicidal");
//		api.addToFleet(FleetSide.ENEMY, "mining_drone_wing", FleetMemberType.FIGHTER_WING, false).getCaptain().setPersonality("suicidal");
//		api.addToFleet(FleetSide.ENEMY, "mining_drone_wing", FleetMemberType.FIGHTER_WING, false).getCaptain().setPersonality("suicidal");
//		api.addToFleet(FleetSide.ENEMY, "mining_drone_wing", FleetMemberType.FIGHTER_WING, false).getCaptain().setPersonality("suicidal");


        //api.addToFleet(FleetSide.ENEMY, "talon_wing", FleetMemberType.FIGHTER_WING, false).getCaptain().setPersonality("suicidal");

        api.defeatOnShipLoss("FMS Reimu");

        // Set up the map.
        float width = 24000f;
        float height = 14000f;
        api.initMap(-width / 2f, width / 2f, -height / 2f, height / 2f);

        float minX = -width / 2;
        float minY = -height / 2;

        // All the addXXX methods take a pair of coordinates followed by data for
        // whatever object is being added.

        // Add two big nebula clouds
        api.addNebula(minX + width * 0.75f, minY + height * 0.5f, 2000);
        api.addNebula(minX + width * 0.25f, minY + height * 0.5f, 1000);

        // And a few random ones to spice up the playing field.
        // A similar approach can be used to randomize everything
        // else, including fleet composition.
        for (int i = 0; i < 5; i++) {
            float x = (float) Math.random() * width - width / 2;
            float y = (float) Math.random() * height - height / 2;
            float radius = 100f + (float) Math.random() * 400f;
            api.addNebula(x, y, radius);
        }

        // Add objectives. These can be captured by each side
        // and provide stat bonuses and extra command points to
        // bring in reinforcements.
        // Reinforcements only matter for large fleets - in this
        // case, assuming a 100 command point battle size,
        // both fleets will be able to deploy fully right away.


        // Add an asteroid field going diagonally across the
        // battlefield, 2000 pixels wide, with a maximum of
        // 100 asteroids in it.
        // 20-70 is the range of asteroid speeds.
        api.addAsteroidField(minY, minY, 45, 2000f,
                20f, 70f, 100);

        // Add some planets.  These are defined in data/config/planets.json.
        api.addPlanet(512, 380, 200f, "lava", 600f, true);
    }

}






