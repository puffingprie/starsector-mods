package data.missions.expsp_mvstest;

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
		api.initFleet(FleetSide.ENEMY, "", FleetGoal.ATTACK, true, 3);

		// Set a blurb for each fleet
		api.setFleetTagline(FleetSide.PLAYER, "Simulated MVS fleet assets");
		api.setFleetTagline(FleetSide.ENEMY, "Target practice freighters");
		
		// These show up as items in the bulleted list under 
		// "Tactical Objectives" on the mission detail screen
		api.addBriefingItem("Defeat all enemy forces");
		
		// Set up the player's fleet
		api.addToFleet(FleetSide.PLAYER, "expsp_cainhurst_Lancer", FleetMemberType.SHIP, "MVS Scion of Blood", false);
		api.addToFleet(FleetSide.PLAYER, "expsp_dragonhunter_Standard", FleetMemberType.SHIP, "MVS St. George's Blade", false);
		api.addToFleet(FleetSide.PLAYER, "expsp_abysswalker_Lancer", FleetMemberType.SHIP, "MVS Devotion and Sacrifice", false);
		api.addToFleet(FleetSide.PLAYER, "expsp_dominion_Elite", FleetMemberType.SHIP, "MVS Annihilation of Gomorrah", false);
		api.addToFleet(FleetSide.PLAYER, "expsp_dominion_belka_Custom", FleetMemberType.SHIP, "Sword of Annihilation", false);
		api.addToFleet(FleetSide.PLAYER, "expsp_asciislaught_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "expsp_testament_Standard", FleetMemberType.SHIP, "MVS Redemption's Light", false);
		api.addToFleet(FleetSide.PLAYER, "expsp_helena_mil_Assault", FleetMemberType.SHIP, "MVS Rebirth in Fire", false);
		
		api.addToFleet(FleetSide.PLAYER, "expsp_anker_Lancer", FleetMemberType.SHIP, "MVS Child of Mischmetal",false);
		api.addToFleet(FleetSide.PLAYER, "expsp_scholarmvs_Elite", FleetMemberType.SHIP,"MVS Cycles of Guilt", false);
		api.addToFleet(FleetSide.PLAYER, "expsp_searchlight_Beamer", FleetMemberType.SHIP, "MVS Embraced by the Flame", false);
		api.addToFleet(FleetSide.PLAYER, "expsp_principality_Strike", FleetMemberType.SHIP, "MVS Vex Ad Gloriae", false);
		api.addToFleet(FleetSide.PLAYER, "expsp_kiel_Standard", FleetMemberType.SHIP, "MVS Cold Front",false);
		api.addToFleet(FleetSide.PLAYER, "expsp_815_Standard", FleetMemberType.SHIP, "MVS Occurrence Border" ,false);
		api.addToFleet(FleetSide.PLAYER, "expsp_raphael_Standard", FleetMemberType.SHIP, "MVS Coldlight" ,false);
	
		//api.addToFleet(FleetSide.PLAYER, "expsp_hybridca_Standard", FleetMemberType.SHIP, false);
		//api.addToFleet(FleetSide.PLAYER, "expsp_eaglemvs_Beamer", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "expsp_falcon_mvs_ew_Support", FleetMemberType.SHIP,"MVS Enchanter", false);
		api.addToFleet(FleetSide.PLAYER, "expsp_helena_Expedition", FleetMemberType.SHIP, "MVS Rebirth in Iron" , false);
		api.addToFleet(FleetSide.PLAYER, "expsp_chrysostom_Support", FleetMemberType.SHIP, "MVS Abrogation",false);
		api.addToFleet(FleetSide.PLAYER, "expsp_provisioner_Standard", FleetMemberType.SHIP, "MVS Glory in Humility" , false);
		api.addToFleet(FleetSide.PLAYER, "expsp_florian_Combat", FleetMemberType.SHIP, "MVS Fuel For the Fire" , false);
		
		api.addToFleet(FleetSide.PLAYER, "expsp_cipher_Standard", FleetMemberType.SHIP,"MVS Brainfreeze", false);
		api.addToFleet(FleetSide.PLAYER, "expsp_hetzer_Standard", FleetMemberType.SHIP, "MVS Fire by Night", false);
		api.addToFleet(FleetSide.PLAYER, "expsp_sammlerin_Militarized", FleetMemberType.SHIP,"MVS Faithful and Trustworthy", false);
		api.addToFleet(FleetSide.PLAYER, "expsp_verteidiger_Standard", FleetMemberType.SHIP, "MVS Adamantine Will",  false);
		api.addToFleet(FleetSide.PLAYER, "expsp_lauretana_Standard", FleetMemberType.SHIP, "MVS Hyacinth Girl",false);
		api.addToFleet(FleetSide.PLAYER, "expsp_sunder_mvs_Siege", FleetMemberType.SHIP, "MVS Inundation", false);
		api.addToFleet(FleetSide.PLAYER, "expsp_intervention_Standard", FleetMemberType.SHIP, "MVS Freischutz", false);
		api.addToFleet(FleetSide.PLAYER, "expsp_scharfrichter_Executor", FleetMemberType.SHIP, "MVS Justitia", false);
		api.addToFleet(FleetSide.PLAYER, "expsp_tzaphkiel_Cyclebreaker", FleetMemberType.SHIP, "MVS Eye Facing the Fear",false);
		api.addToFleet(FleetSide.PLAYER, "expsp_virtue_Standard", FleetMemberType.SHIP, "MVS Blessed Are the Peacemakers", false);
		
		api.addToFleet(FleetSide.PLAYER, "expsp_ascalon_Elite", FleetMemberType.SHIP, "MVS Luna Dial",false);
		api.addToFleet(FleetSide.PLAYER, "expsp_shashka_Strike", FleetMemberType.SHIP, "MVS Resonance Of War", false);
		api.addToFleet(FleetSide.PLAYER, "expsp_scytale_Standard", FleetMemberType.SHIP,"MVS Styx", false);
		api.addToFleet(FleetSide.PLAYER, "expsp_testudo_Support", FleetMemberType.SHIP, "Duty and Faith",false);
		api.addToFleet(FleetSide.PLAYER, "expsp_stiletto_Closequarters", FleetMemberType.SHIP, "MVS The Mind Electric", false);
		api.addToFleet(FleetSide.PLAYER, "expsp_kriegsmesser_Strike", FleetMemberType.SHIP, "MVS Wages of Sin", false);
		api.addToFleet(FleetSide.PLAYER, "expsp_malakh_Standard", FleetMemberType.SHIP, "MVS Oberon", false);
		api.addToFleet(FleetSide.PLAYER, "expsp_kiteskipper_Standard", FleetMemberType.SHIP, "MVS Inundation", false);
		api.addToFleet(FleetSide.PLAYER, "expsp_kurierin_Standard", FleetMemberType.SHIP, "MVS Handful of Dust",false);

		api.addToFleet(FleetSide.PLAYER, "expsp_puukko_Standard", FleetMemberType.SHIP, "MVS Glatisant",false);
		//api.addToFleet(FleetSide.PLAYER, "expsp_korsettes_Assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "expsp_kris_Lancer", FleetMemberType.SHIP, "MVS Oncoming Storm", false);
		
		api.addToFleet(FleetSide.PLAYER, "expsp_asdf1_frig_Strike", FleetMemberType.SHIP,"MVS Garmr I" ,false);
		api.addToFleet(FleetSide.PLAYER, "expsp_thunderer_frig_Strike", FleetMemberType.SHIP,"MVS Brickily Constructed" ,false);
		api.addToFleet(FleetSide.PLAYER, "expsp_su30_frig_Strike", FleetMemberType.SHIP, "MVS Sol Invictus" ,false);
		api.addToFleet(FleetSide.PLAYER, "expsp_mirage_frig_Strike", FleetMemberType.SHIP, "MVS Rigauex" ,false);
		api.addToFleet(FleetSide.PLAYER, "expsp_executor_Assault", FleetMemberType.SHIP,"MVS Dragon" ,false);
		
		// Set up the enemy fleet
		
		
		FleetMemberAPI fleetMember;
		api.addToFleet(FleetSide.ENEMY, "atlas_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "atlas_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "colossus_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "colossus_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "buffalo_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "buffalo_Standard", FleetMemberType.SHIP, false);
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






