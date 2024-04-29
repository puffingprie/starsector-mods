package data.missions.carrierflightmaiden;

import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;

public class MissionDefinition implements MissionDefinitionPlugin {
    public void defineMission(MissionDefinitionAPI api) {
        // Set up the fleets
		api.initFleet(FleetSide.PLAYER, "UAF", FleetGoal.ATTACK, false, 2);
		api.initFleet(FleetSide.ENEMY, "FAU", FleetGoal.ATTACK, true, 5);

		// Set a blurb for each fleet
		api.setFleetTagline(FleetSide.PLAYER, "This is UAF Virtual Field, battle will commence virtually");
		api.setFleetTagline(FleetSide.ENEMY, "FAU is a virtual fleet used for training");
		
		// These show up as items in the bulleted list under 
		// "Tactical Objectives" on the mission detail screen
		api.addBriefingItem("Defeat the enemy forces");
		api.addBriefingItem("Survive, or be wiped.");
		api.addBriefingItem("Scores will be noted down as a test for new admiralty.");
		
		// Set up the player's fleet
		api.addToFleet(FleetSide.PLAYER, "uaf_novaeria_elite", FleetMemberType.SHIP, "UAF Novaeria", true);
		//exorias
		api.addToFleet(FleetSide.PLAYER, "uaf_exoria_standard", FleetMemberType.SHIP, "UAF Exoria", false);
		api.addToFleet(FleetSide.PLAYER, "uaf_ai_exoria_superiority", FleetMemberType.SHIP, "UAF Exoria AI", false);

		//minorias
		api.addToFleet(FleetSide.PLAYER, "uaf_minoria_standard", FleetMemberType.SHIP, "UAF Minoria", false);
		api.addToFleet(FleetSide.PLAYER, "uaf_m_minoria_standard", FleetMemberType.SHIP, "UAF Minoria M", false);
		api.addToFleet(FleetSide.PLAYER, "uaf_ai_minoria_standard", FleetMemberType.SHIP, "UAF Minoria AI", false);

		//neuxorias
		api.addToFleet(FleetSide.PLAYER, "uaf_ai_neuxoria_standard", FleetMemberType.SHIP, "UAF Neuxoria AI", false);

		//zepporias
		api.addToFleet(FleetSide.PLAYER, "uaf_m_zepporia_standard", FleetMemberType.SHIP, "UAF Zepporia M", false);

		//shinorias
		api.addToFleet(FleetSide.PLAYER, "uaf_m_shinoria_standard", FleetMemberType.SHIP, "UAF Shinoria M", false);

		api.addToFleet(FleetSide.PLAYER, "uaf_shinoria_standard", FleetMemberType.SHIP, "UAF Shinoria", false);

		//machis
		api.addToFleet(FleetSide.PLAYER, "uaf_m_machi_apa_transport", FleetMemberType.SHIP, "Machi-I", false);

		//battlecarrier
		api.addToFleet(FleetSide.PLAYER, "uaf_isenoria_prototype_standard", FleetMemberType.SHIP, "Isenoria", false);

		//sunami
		api.addToFleet(FleetSide.PLAYER, "uaf_sunami_prototype_standard", FleetMemberType.SHIP, "Sunami", false);


		api.addToFleet(FleetSide.ENEMY, "uaf_novaeria_elite", FleetMemberType.SHIP, true);
		
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
		
		api.addPlanet(0, 0, 500f, "aurorian", 300f, true);
    }
}
