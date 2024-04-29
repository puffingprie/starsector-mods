package data.missions.expsp_baptism;

import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;

public class MissionDefinition implements MissionDefinitionPlugin {

	public void defineMission(MissionDefinitionAPI api) {

		// Set up the fleets
		api.initFleet(FleetSide.PLAYER, "MVS", FleetGoal.ATTACK, false, 2);
		api.initFleet(FleetSide.ENEMY, "ISS", FleetGoal.ATTACK, true, 3);

		// Set a blurb for each fleet
		api.setFleetTagline(FleetSide.PLAYER, "MVS Luna Dial and Vanguard Fleet");
		api.setFleetTagline(FleetSide.ENEMY, "Mercenary hit-team");
		
		// These show up as items in the bulleted list under 
		// "Tactical Objectives" on the mission detail screen
		api.addBriefingItem("Defeat all enemy forces");
		api.addBriefingItem("Flank the enemy fleet to eliminate the hostile carriers");
		// Set up the player's fleet
		
	
		api.addToFleet(FleetSide.PLAYER, "expsp_ascalon_Elite", FleetMemberType.SHIP, "MVS Luna Dial", true);
		api.addToFleet(FleetSide.PLAYER, "expsp_verteidiger_Outdated", FleetMemberType.SHIP,false );
		
		//api.addToFleet(FleetSide.PLAYER, "expsp_virtue_Standard", FleetMemberType.SHIP, false);
		
		
		
		api.addToFleet(FleetSide.PLAYER, "expsp_kriegsmesser_Strike", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "expsp_kriegsmesser_Strike", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "expsp_puukko_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "expsp_puukko_Standard", FleetMemberType.SHIP, false);
		
		
		
		// Set up the enemy fleet
		
		
		FleetMemberAPI fleetMember;
		api.addToFleet(FleetSide.ENEMY, "fury_Support", FleetMemberType.SHIP,"ISS Sealing Nail", false);
		api.addToFleet(FleetSide.ENEMY, "hyperion_Attack", FleetMemberType.SHIP,"ISS Bright Spark", false);
		api.addToFleet(FleetSide.ENEMY, "medusa_Attack", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "shrike_Attack", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "drover_Strike", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "drover_Strike", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "scarab_Experimental", FleetMemberType.SHIP, false);
		
		api.addToFleet(FleetSide.ENEMY, "tempest_Attack", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "tempest_Attack", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "wolf_Strike", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "wolf_Strike", FleetMemberType.SHIP, false);
		//api.addToFleet(FleetSide.ENEMY, "enforcer_Balanced", FleetMemberType.SHIP, false);
		//enemy fleet

		// Set up the map.
		float width = 24000f;
		float height = 18000f;
		api.initMap((float)-width/2f, (float)width/2f, (float)-height/2f, (float)height/2f);
		
		float minX = -width/2;
		float minY = -height/2;
		
		// All the addXXX methods take a pair of coordinates followed by data for
		// whatever object is being added.
		
		// Add two big nebula clouds
		api.addNebula(minX + width * 0.66f, minY + height * 0.5f, 2000);
		api.addNebula(minX + width * 0.25f, minY + height * 0.6f, 1000);
		api.addNebula(minX + width * 0.25f, minY + height * 0.4f, 1000);
		
		// And a few random ones to spice up the playing field.
		for (int i = 0; i < 5; i++) {
			float x = (float) Math.random() * width - width/2;
			float y = (float) Math.random() * height - height/2;
			float radius = 100f + (float) Math.random() * 400f; 
			api.addNebula(x, y, radius);
		}
		
		// add objectives
		api.addObjective(minX + width * 0.25f + 2000f, minY + height * 0.5f, 
						 "sensor_array");
		api.addObjective(minX + width * 0.75f - 2000f, minY + height * 0.5f,
						 "comm_relay");
		api.addObjective(minX + width * 0.33f + 2000f, minY + height * 0.4f, 
						 "nav_buoy");
		api.addObjective(minX + width * 0.66f - 2000f, minY + height * 0.6f, 
						 "nav_buoy");
		

		api.addAsteroidField(-(minY + height), minY + height, -45, 2000f,
								20f, 70f, 100);
		
		api.addPlanet(0, 0, 400f, "barren", 200f, true);
		api.addRingAsteroids(0,0, 30, 32, 32, 48, 200);
	}

}






