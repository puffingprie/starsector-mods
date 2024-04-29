package data.missions.uafvirtualfield;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;

public class MissionDefinition implements MissionDefinitionPlugin {
    @Override
    public void defineMission(MissionDefinitionAPI api) {
        // Set up the fleets
//        Global.getSoundPlayer().playCustomMusic(0, 0, "northern_cross", true);
		api.initFleet(FleetSide.PLAYER, "UAF", FleetGoal.ATTACK, false, 2);
		api.initFleet(FleetSide.ENEMY, "FAU", FleetGoal.ATTACK, true, 5);

		// Set a blurb for each fleet
		api.setFleetTagline(FleetSide.PLAYER, "This is UAF Virtual Field, battle will commence virtually");
		api.setFleetTagline(FleetSide.ENEMY, "FAU is a virtual fleet used for training");
		
		// These show up as items in the bulleted list under 
		// "Tactical Objectives" on the mission detail screen
//		api.addBriefingItem("At least 25% of your forces must escape");
//		api.addBriefingItem("Ships ordered to retreat will escape along the top edge of the map");
		api.addBriefingItem("Defeat the enemy forces");
		api.addBriefingItem("Survive, or be wiped.");
		api.addBriefingItem("Scores will be noted down as a test for new admiralty.");
		
		// Set up the player's fleet
                api.addToFleet(FleetSide.PLAYER, "uaf_m_nebula_bravo_military", FleetMemberType.SHIP, "UAF Nebula", false);
                api.addToFleet(FleetSide.PLAYER, "uaf_wing_lynx_ahft", FleetMemberType.SHIP, "UAF Lynx", false);
                api.addToFleet(FleetSide.PLAYER, "uaf_solverniaiv_military", FleetMemberType.SHIP, "UAF Solvernia", true);
                
                api.addToFleet(FleetSide.PLAYER, "uaf_reisen_standard", FleetMemberType.SHIP, "UAF Reisen", false);
                api.addToFleet(FleetSide.PLAYER, "uaf_m_reisen_military", FleetMemberType.SHIP, "UAF Reisen", false);

                api.addToFleet(FleetSide.PLAYER, "uaf_exoria_standard", FleetMemberType.SHIP, "UAF Exoria", false);
                api.addToFleet(FleetSide.PLAYER, "uaf_m_exoria_military", FleetMemberType.SHIP, "UAF Exoria", false);

                api.addToFleet(FleetSide.PLAYER, "uaf_minoria_standard", FleetMemberType.SHIP, "UAF Minoria", false);
                api.addToFleet(FleetSide.PLAYER, "uaf_m_minoria_military", FleetMemberType.SHIP, "UAF Minoria", false);
                api.addToFleet(FleetSide.PLAYER, "uaf_ai_minoria_ai", FleetMemberType.SHIP, "UAF Minoria", false);

                api.addToFleet(FleetSide.PLAYER, "uaf_nakiha_standard", FleetMemberType.SHIP, "UAF Nakiha", false);
                api.addToFleet(FleetSide.PLAYER, "uaf_m_nakiha_military", FleetMemberType.SHIP, "UAF Nakiha", false);
                api.addToFleet(FleetSide.PLAYER, "uaf_ai_nakiha_ai", FleetMemberType.SHIP, "UAF Nakiha", false);

                api.addToFleet(FleetSide.PLAYER, "uaf_ai_neuxoria_ai", FleetMemberType.SHIP, "UAF Neuxoria", false);
                
                api.addToFleet(FleetSide.PLAYER, "uaf_m_perwira_military", FleetMemberType.SHIP, "UAF Perwira", false);
                
                
                //wings
                // api.addToFleet(FleetSide.PLAYER, "uaf_wing_aria", FleetMemberType.FIGHTER_WING, false);
                // api.addToFleet(FleetSide.PLAYER, "uaf_wing_fisoyuz", FleetMemberType.FIGHTER_WING, false);
                // api.addToFleet(FleetSide.PLAYER, "uaf_wing_mitsurei", FleetMemberType.FIGHTER_WING, false);
                // api.addToFleet(FleetSide.PLAYER, "uaf_wing_exoscar", FleetMemberType.FIGHTER_WING, false);
                // api.addToFleet(FleetSide.PLAYER, "uaf_wing_sakurai", FleetMemberType.FIGHTER_WING, false);
                // api.addToFleet(FleetSide.PLAYER, "uaf_wing_minersky", FleetMemberType.FIGHTER_WING, false);
                
		//api.addToFleet(FleetSide.PLAYER, "apogee_Balanced", FleetMemberType.SHIP, "ISS Van Rijn", true);
//		api.addToFleet(FleetSide.PLAYER, "aurora_Assault_Support", FleetMemberType.SHIP, "ISS Athena", true);
//		api.addToFleet(FleetSide.PLAYER, "venture_Balanced", FleetMemberType.SHIP, "ISS Enki", false);
//		api.addToFleet(FleetSide.PLAYER, "falcon_Attack", FleetMemberType.SHIP, false);
//		api.addToFleet(FleetSide.PLAYER, "heron_Attack", FleetMemberType.SHIP, false);
		//api.addToFleet(FleetSide.PLAYER, "medusa_Attack", FleetMemberType.SHIP, false, CrewXPLevel.VETERAN);
//		api.addToFleet(FleetSide.PLAYER, "tempest_Attack", FleetMemberType.SHIP, false);
		//api.addToFleet(FleetSide.PLAYER, "wolf_CS", FleetMemberType.SHIP, false);
		//api.addToFleet(FleetSide.PLAYER, "tempest_Attack", FleetMemberType.SHIP, false, CrewXPLevel.VETERAN);
		//api.addToFleet(FleetSide.PLAYER, "dagger_wing", FleetMemberType.FIGHTER_WING, false);
		//api.addToFleet(FleetSide.PLAYER, "xyphos_wing", FleetMemberType.FIGHTER_WING, false);
		//api.addToFleet(FleetSide.PLAYER, "wasp_wing", FleetMemberType.FIGHTER_WING, false);
		//api.addToFleet(FleetSide.PLAYER, "broadsword_wing", FleetMemberType.FIGHTER_WING, false);
		//api.addToFleet(FleetSide.PLAYER, "wasp_wing", FleetMemberType.FIGHTER_WING, false);
		
		// Mark flagship as essential
//		api.defeatOnShipLoss("ISS Enki");
		
		/*
		api.addToFleet(FleetSide.PLAYER, "paragon_Elite", FleetMemberType.SHIP, "ISS Aesir", true, CrewXPLevel.REGULAR);
		api.addToFleet(FleetSide.PLAYER, "paragon_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "odyssey_Balanced", FleetMemberType.SHIP, false, CrewXPLevel.REGULAR);
		api.addToFleet(FleetSide.PLAYER, "odyssey_Balanced", FleetMemberType.SHIP, false, CrewXPLevel.REGULAR);
		api.addToFleet(FleetSide.PLAYER, "wasp_wing", FleetMemberType.FIGHTER_WING, false);
		api.addToFleet(FleetSide.PLAYER, "wasp_wing", FleetMemberType.FIGHTER_WING, false);
		api.addToFleet(FleetSide.PLAYER, "wasp_wing", FleetMemberType.FIGHTER_WING, false);
		api.addToFleet(FleetSide.PLAYER, "wasp_wing", FleetMemberType.FIGHTER_WING, false);
		*/
		// Mark flagship as essential
		//api.defeatOnShipLoss("Aesir");
		
		// Set up the enemy fleet
		api.addToFleet(FleetSide.ENEMY, "onslaught_Elite", FleetMemberType.SHIP, "HSS Phoenix", false);
		api.addToFleet(FleetSide.ENEMY, "condor_Strike", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "condor_Attack", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "condor_Attack", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "condor_Support", FleetMemberType.SHIP, false);
		//api.addToFleet(FleetSide.ENEMY, "dominator_Support", FleetMemberType.SHIP, false, CrewXPLevel.VETERAN);
		api.addToFleet(FleetSide.ENEMY, "gryphon_Standard", FleetMemberType.SHIP, false);
		//api.addToFleet(FleetSide.ENEMY, "enforcer_CS", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "enforcer_Assault", FleetMemberType.SHIP, false);
//		api.addToFleet(FleetSide.ENEMY, "enforcer_Balanced", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "enforcer_Balanced", FleetMemberType.SHIP, false);
		
		api.addToFleet(FleetSide.ENEMY, "hound_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "hound_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "lasher_CS", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "lasher_CS", FleetMemberType.SHIP, false);
		
		
		// Set up the map.
		float width = 24000f;
		float height = 18000f;
		api.initMap((float)-width/2f, (float)width/2f, (float)-height/2f, (float)height/2f);
		
		float minX = -width/2;
		float minY = -height/2;
		
		// All the addXXX methods take a pair of coordinates followed by data for
		// whatever object is being added.
		
		// And a few random ones to spice up the playing field.
		// A similar approach can be used to randomize everything
		// else, including fleet composition.
		for (int i = 0; i < 25; i++) {
			float x = (float) Math.random() * width - width/2;
			float y = (float) Math.random() * height - height/2;
			float radius = 400f + (float) Math.random() * 1000f; 
			api.addNebula(x, y, radius);
		}
		
		// Add objectives. These can be captured by each side
		// and provide stat bonuses and extra command points to
		// bring in reinforcements.
		// Reinforcements only matter for large fleets - in this
		// case, assuming a 100 command point battle size,
		// both fleets will be able to deploy fully right away.
		api.addObjective(minX + width * 0.2f + 400 + 3000, minY + height * 0.2f + 400 + 2000, "sensor_array");
		api.addObjective(minX + width * 0.4f + 2000, minY + height * 0.7f, "sensor_array");
		api.addObjective(minX + width * 0.75f - 2000, minY + height * 0.7f, "comm_relay");
		api.addObjective(minX + width * 0.2f + 3000, minY + height * 0.5f, "nav_buoy");
		api.addObjective(minX + width * 0.85f - 3000, minY + height * 0.4f, "nav_buoy");
		
		//api.addPlanet(0, 0, 500f, "ice_giant", 300f, true);
    }
}
