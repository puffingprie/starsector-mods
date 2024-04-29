package data.missions.keruvim_test;

import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;

public class MissionDefinition implements MissionDefinitionPlugin {

	public void defineMission(MissionDefinitionAPI api) {

		// Set up the fleets so we can add ships and fighter wings to them.
		// In this scenario, the fleets are attacking each other, but
		// in other scenarios, a fleet may be defending or trying to escape
		api.initFleet(FleetSide.PLAYER, "KVSS", FleetGoal.ATTACK, false);
		api.initFleet(FleetSide.ENEMY, "TITS", FleetGoal.ATTACK, true);

//		api.getDefaultCommander(FleetSide.PLAYER).getStats().setSkillLevel(Skills.COORDINATED_MANEUVERS, 3);
//		api.getDefaultCommander(FleetSide.PLAYER).getStats().setSkillLevel(Skills.ELECTRONIC_WARFARE, 3);
		
		// Set a small blurb for each fleet that shows up on the mission detail and
		// mission results screens to identify each side.
		api.setFleetTagline(FleetSide.PLAYER, "Awesome fleet of awesomeness");
		api.setFleetTagline(FleetSide.ENEMY, "Stupid tri-tach Astral with ugly Astral escort");
		

		// These show up as items in the bulleted list under 
		// "Tactical Objectives" on the mission detail screen
		api.addBriefingItem("Take down these Nerds");
		api.addBriefingItem("Test me ships");
		api.addBriefingItem("Got any Grapes?");
		
		// Set up the player's fleet.  Variant names come from the
		// files in data/variants and data/variants/fighters
		api.addToFleet(FleetSide.PLAYER, "keruvim_lucerne_assault", FleetMemberType.SHIP, "Lover", false);
		api.addToFleet(FleetSide.PLAYER, "keruvim_axe_a_support", FleetMemberType.SHIP, "1", false);
		api.addToFleet(FleetSide.PLAYER, "keruvim_axe_b_sniper", FleetMemberType.SHIP, "2", false);
		api.addToFleet(FleetSide.PLAYER, "keruvim_brute_standard", FleetMemberType.SHIP, "4", false);
		api.addToFleet(FleetSide.PLAYER, "keruvim_buccaneer_support", FleetMemberType.SHIP, "5", false);
		api.addToFleet(FleetSide.PLAYER, "keruvim_carrion_standard", FleetMemberType.SHIP, "6", false);
		api.addToFleet(FleetSide.PLAYER, "keruvim_chariot_a_strike", FleetMemberType.SHIP, "7", false);
		api.addToFleet(FleetSide.PLAYER, "keruvim_chariot_b_assault", FleetMemberType.SHIP, "8", false);
		api.addToFleet(FleetSide.PLAYER, "keruvim_cudgel_runner", FleetMemberType.SHIP, "9", false);
		api.addToFleet(FleetSide.PLAYER, "keruvim_culverin_assault", FleetMemberType.SHIP, "10", false);
		api.addToFleet(FleetSide.PLAYER, "keruvim_duchess_assault", FleetMemberType.SHIP, "12", true);
		api.addToFleet(FleetSide.PLAYER, "keruvim_empress_support", FleetMemberType.SHIP, "13", false);
		api.addToFleet(FleetSide.PLAYER, "keruvim_glaive_standard", FleetMemberType.SHIP, "14", false);
		api.addToFleet(FleetSide.PLAYER, "keruvim_flamberge_standard", FleetMemberType.SHIP, "15", false);
		api.addToFleet(FleetSide.PLAYER, "keruvim_shotel_standard", FleetMemberType.SHIP, "16", false);
		api.addToFleet(FleetSide.PLAYER, "keruvim_mortar_support", FleetMemberType.SHIP, "17", false);
		api.addToFleet(FleetSide.PLAYER, "keruvim_rapier_assault", FleetMemberType.SHIP, "18", false);
		api.addToFleet(FleetSide.PLAYER, "keruvim_zweihander_a_standard", FleetMemberType.SHIP, "19", false);
		api.addToFleet(FleetSide.PLAYER, "keruvim_zweihander_b_sniper", FleetMemberType.SHIP, "20", false);
		api.addToFleet(FleetSide.PLAYER, "keruvim_zweihander_p_assault", FleetMemberType.SHIP, "21", false);
		api.addToFleet(FleetSide.PLAYER, "keruvim_bastion_standard", FleetMemberType.SHIP, "22", false);
		api.addToFleet(FleetSide.PLAYER, "keruvim_brawler_xiv_standard", FleetMemberType.SHIP, "23", false);
		api.addToFleet(FleetSide.PLAYER, "keruvim_ranger_standard", FleetMemberType.SHIP, "24", false);
		api.addToFleet(FleetSide.PLAYER, "keruvim_morgenstern_assault", FleetMemberType.SHIP, "26", false);
		api.addToFleet(FleetSide.PLAYER, "keruvim_flail_standard", FleetMemberType.SHIP, "27", false);
		api.addToFleet(FleetSide.PLAYER, "keruvim_stallion_assault", FleetMemberType.SHIP, "28", false);
		api.addToFleet(FleetSide.PLAYER, "keruvim_paragon_p_elite", FleetMemberType.SHIP, "29", false);
		api.addToFleet(FleetSide.PLAYER, "keruvim_catapult_standard", FleetMemberType.SHIP, "30", false);
		api.addToFleet(FleetSide.PLAYER, "keruvim_bouncer_standard", FleetMemberType.SHIP, "31", false);
		api.addToFleet(FleetSide.PLAYER, "keruvim_quarry_standard", FleetMemberType.SHIP, "32", false);
		api.addToFleet(FleetSide.PLAYER, "keruvim_zweistar_assault", FleetMemberType.SHIP, "37", false);



		// Set up the enemy fleet.
		api.addToFleet(FleetSide.ENEMY, "astral_Strike", FleetMemberType.SHIP, "Stupid", false);
		api.addToFleet(FleetSide.ENEMY, "astral_Elite", FleetMemberType.SHIP, "Ugly", false);

		
		
		// Set up the map.
		float width = 12000f;
		float height = 12000f;
		api.initMap((float)-width/2f, (float)width/2f, (float)-height/2f, (float)height/2f);
		
		float minX = -width/2;
		float minY = -height/2;
		
		// Add an asteroid field
		api.addAsteroidField(minX, minY + height / 2, 0, 8000f,
							 20f, 70f, 100);
		
		api.addPlanet(-320, -140, 200f, "tundra", 250f, true);
		
	}

}
