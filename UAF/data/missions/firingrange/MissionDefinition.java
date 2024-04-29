package data.missions.firingrange;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;

public class MissionDefinition implements MissionDefinitionPlugin {
    public void defineMission(MissionDefinitionAPI api) {
        // Set up the fleets
		api.initFleet(FleetSide.PLAYER, "UAF", FleetGoal.ATTACK, false, 10);
		api.initFleet(FleetSide.ENEMY, "FAU", FleetGoal.ATTACK, true, 5);

		// Set a blurb for each fleet
		api.setFleetTagline(FleetSide.PLAYER, "UAF Military");
		
		// These show up as items in the bulleted list under 
		// "Tactical Objectives" on the mission detail screen
		api.addBriefingItem("Defeat the enemy forces");
		api.addBriefingItem("Survive, or be wiped.");
		api.addBriefingItem("Don't let the new Solvernia be wiped");
		
		//fighter
		api.addToFleet(FleetSide.PLAYER, "uaf_wing_lynx_ahft", FleetMemberType.SHIP, "UAF Lynx", true);

		//Cherry
		api.addToFleet(FleetSide.PLAYER, "uaf_cherry_core_elite", FleetMemberType.SHIP, "UAF Cherry", true);

		// Solvy
		api.addToFleet(FleetSide.PLAYER, "uaf_supercap_slv_core_elite", FleetMemberType.SHIP, "UAF Solvernia", true);
		
		//Seniraja
		api.addToFleet(FleetSide.PLAYER, "uaf_seniraja_standard", FleetMemberType.SHIP, "UAF Seniraja", false);
		api.addToFleet(FleetSide.PLAYER, "uaf_m_seniraja_standard", FleetMemberType.SHIP, "UAF Seniraja", false);

		//Procastria
		api.addToFleet(FleetSide.PLAYER, "uaf_procastria_prototype_standard", FleetMemberType.SHIP, "UAF Seniraja", false);

		// Fusoreina
		api.addToFleet(FleetSide.PLAYER, "uaf_m_fusoreina_standard", FleetMemberType.SHIP, "UAF Fusoreina", false);

		// Lion
		api.addToFleet(FleetSide.PLAYER, "uaf_lyora_prototype_standard", FleetMemberType.SHIP, "UAF Lion", false);

		// Seiichi
		api.addToFleet(FleetSide.PLAYER, "uaf_m_seiichi_standard", FleetMemberType.SHIP, "UAF Seiichi", false);

		// Vaharagiri
		api.addToFleet(FleetSide.PLAYER, "uaf_ai_vaharagiri_standard", FleetMemberType.SHIP, "UAF Seiichi", false);

		// Super Nakiha
		api.addToFleet(FleetSide.PLAYER, "uaf_super_nakiha_prototype_ace", FleetMemberType.SHIP, "UAF Super Nakiha", false);

		//Akatsuzuma
		api.addToFleet(FleetSide.PLAYER, "uaf_akatsuzuma_standard", FleetMemberType.SHIP, "UAF Akatsuzuma", false);
		api.addToFleet(FleetSide.PLAYER, "uaf_ai_akatsuzuma_standard", FleetMemberType.SHIP, "UAF Akatsuzuma", false);

		//Novayu		
		api.addToFleet(FleetSide.PLAYER, "uaf_novayu_standard", FleetMemberType.SHIP, "UAF Novayu", false);
		//Junyo
		api.addToFleet(FleetSide.PLAYER, "uaf_junyo_delta_standard", FleetMemberType.SHIP, "UAF Junyo", false);
		//Supolev
		api.addToFleet(FleetSide.PLAYER, "uaf_supolev_alpha_standard", FleetMemberType.SHIP, "UAF Supolev", false);
		//Miyo
		api.addToFleet(FleetSide.PLAYER, "uaf_miyo_juliet_standard", FleetMemberType.SHIP, "UAF Miyo", false);

		//Purcellyra
		api.addToFleet(FleetSide.PLAYER, "uaf_purcellyra_standard", FleetMemberType.SHIP, "Shield-I", false);
		
		//Scinaut
		api.addToFleet(FleetSide.PLAYER, "uaf_scinaut_suv_standard", FleetMemberType.SHIP, "Scinaut-I", false);

		//Rillaru
		api.addToFleet(FleetSide.PLAYER, "uaf_rillaru_standard", FleetMemberType.SHIP, "Sword-I", false);
		api.addToFleet(FleetSide.PLAYER, "uaf_ai_rillaru_standard", FleetMemberType.SHIP, "Beam-II", false);

		//Lilaru
		api.addToFleet(FleetSide.PLAYER, "uaf_lillaru_standard", FleetMemberType.SHIP, "Initiator-I", false);

		//Lilaru AI
		api.addToFleet(FleetSide.PLAYER, "uaf_ai_lillaru_standard", FleetMemberType.SHIP, "Initiator-AI", false);

		//Maisya SP
		api.addToFleet(FleetSide.PLAYER, "uaf_ai_maisya_standard", FleetMemberType.SHIP, "Initiator-AI", false);

		//Reisen
		api.addToFleet(FleetSide.PLAYER, "uaf_m_reisen_standard", FleetMemberType.SHIP, "Brocade II", false);
		api.addToFleet(FleetSide.PLAYER, "uaf_reisen_standard", FleetMemberType.SHIP, "Dreams I", false);

		//Nakiha
		api.addToFleet(FleetSide.PLAYER, "uaf_m_nakiha_standard", FleetMemberType.SHIP, "Greyhound Alpha III", false);
		api.addToFleet(FleetSide.PLAYER, "uaf_ai_nakiha_standard", FleetMemberType.SHIP, "Greyhound Beta III", false);
		api.addToFleet(FleetSide.PLAYER, "uaf_nakiha_standard", FleetMemberType.SHIP, "Greyhound Gamma I", false);

		//Perwira
		api.addToFleet(FleetSide.PLAYER, "uaf_m_perwira_standard", FleetMemberType.SHIP, "Boxer-I", false);
		api.addToFleet(FleetSide.PLAYER, "uaf_ai_perwira_standard", FleetMemberType.SHIP, "Boxer-II", false);
		
		
		//Rama
		api.addToFleet(FleetSide.PLAYER, "uaf_rama_corvette_standard", FleetMemberType.SHIP, "Bee-I", false);
		api.addToFleet(FleetSide.PLAYER, "uaf_ai_rama_corvette_standard", FleetMemberType.SHIP, "/2Bee-III", false);

		//Sentry
		api.addToFleet(FleetSide.PLAYER, "uaf_m_sentry_prototype", FleetMemberType.SHIP, "Arty-I", false);
		api.addToFleet(FleetSide.PLAYER, "uaf_sentry_standard", FleetMemberType.SHIP, "Arty-II", false);

		//Inazuma
		api.addToFleet(FleetSide.PLAYER, "uaf_m_inazuma_standard", FleetMemberType.SHIP, "Archer-II", false);
		api.addToFleet(FleetSide.PLAYER, "uaf_inazuma_standard", FleetMemberType.SHIP, "String-I", false);

		//Lovela
		api.addToFleet(FleetSide.PLAYER, "uaf_lovela_standard", FleetMemberType.SHIP, "Heavy Escort I", false);
		api.addToFleet(FleetSide.PLAYER, "uaf_m_lovela_standard", FleetMemberType.SHIP, "Heavy M Escort I", false);
		
		//logistics
		api.addToFleet(FleetSide.PLAYER, "uaf_tsutsumu_l_standard", FleetMemberType.SHIP, "Panamax", false);
		api.addToFleet(FleetSide.PLAYER, "uaf_sekai_s_standard", FleetMemberType.SHIP, "Golden Nori", false);
		api.addToFleet(FleetSide.PLAYER, "uaf_wo_m_standard", FleetMemberType.SHIP, "Golden Nori", false);
		api.addToFleet(FleetSide.PLAYER, "uaf_osela_suv_standard", FleetMemberType.SHIP, "Explorator I", false);
		api.addToFleet(FleetSide.PLAYER, "uaf_eyeseau_explorer", FleetMemberType.SHIP, "Explorator IV", false);
		api.addToFleet(FleetSide.PLAYER, "uaf_m_nebula_bravo_runner", FleetMemberType.SHIP, "Invincible", false);

		api.defeatOnShipLoss("UAF Solvernia");

		//contest
		api.addToFleet(FleetSide.PLAYER, "uaf_ai_roria_esvernia_anchor", FleetMemberType.SHIP, "Anchor", false);
		api.addToFleet(FleetSide.PLAYER, "uaf_ai_roria_esvernia_bulwark", FleetMemberType.SHIP, "Bulwark", false);
		api.addToFleet(FleetSide.PLAYER, "uaf_ai_roria_esvernia_custom", FleetMemberType.SHIP, "Custom", false);
		api.addToFleet(FleetSide.PLAYER, "uaf_ai_roria_esvernia_ledah", FleetMemberType.SHIP, "Ledah", false);
		api.addToFleet(FleetSide.PLAYER, "uaf_ai_roria_esvernia_prototype_2", FleetMemberType.SHIP, "Prototype", false);
		api.addToFleet(FleetSide.PLAYER, "uaf_ai_roria_esvernia_runner", FleetMemberType.SHIP, "Blockade Runner", false);
		api.addToFleet(FleetSide.PLAYER, "uaf_ai_roria_esvernia_smasher", FleetMemberType.SHIP, "Smasher", false);
		api.addToFleet(FleetSide.PLAYER, "uaf_ai_roria_esvernia_support", FleetMemberType.SHIP, "Support", false);
		api.addToFleet(FleetSide.PLAYER, "uaf_ai_roria_esvernia_zyrith", FleetMemberType.SHIP, "Zyrith", false);
	
		// Solvy
		api.addToFleet(FleetSide.ENEMY, "uaf_slvv_core_elite", FleetMemberType.SHIP, "UAF Solvirnea", true);

		// Set up the map.
		float width = 24000f;
		float height = 18000f;
		api.initMap((float)-width/2f, (float)width/2f, (float)-height/2f, (float)height/2f);
		
		float minX = -width/2;
		float minY = -height/2;
		
		for (int i = 0; i < 15; i++) {
			float x = (float) Math.random() * width - width/2;
			float y = (float) Math.random() * height - height/2;
			float radius = 100f + (float) Math.random() * 900f; 
			api.addNebula(x, y, radius);
		}
		
		api.addNebula(minX + width * 0.8f - 1000, minY + height * 0.4f, 2000);
		api.addNebula(minX + width * 0.8f - 1000, minY + height * 0.5f, 2000);
		api.addNebula(minX + width * 0.8f - 1000, minY + height * 0.6f, 2000);
		
		// api.addObjective(minX + width * 0.8f - 1000, minY + height * 0.4f, "nav_buoy");
		// api.addObjective(minX + width * 0.8f - 1000, minY + height * 0.6f, "nav_buoy");
		// api.addObjective(minX + width * 0.3f + 1000, minY + height * 0.3f, "comm_relay");
		// api.addObjective(minX + width * 0.3f + 1000, minY + height * 0.7f, "comm_relay");
		// api.addObjective(minX + width * 0.5f, minY + height * 0.5f, "sensor_array");
		// api.addObjective(minX + width * 0.2f + 1000, minY + height * 0.5f, "sensor_array");
		
		api.addAsteroidField(minX + width * 0.3f, minY, 90, 3000f,
								20f, 70f, 50);
		
		api.addPlanet(0, 0, 200f, "irradiated", 350f, true);
		
    }
}
