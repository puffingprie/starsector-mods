package data.missions.uafrandom;

import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;

public class MissionDefinition implements MissionDefinitionPlugin {

	private List ships = new ArrayList();
	private List uaf_ships = new ArrayList();
	private void addEnemy(String variant, int weight){
		for(int i = 0; i< weight; i++){
			uaf_ships.add(variant);
		}
	}
	private void addShip(String variant, int weight) {
		for (int i = 0; i < weight; i++) {
			ships.add(variant);
		}
	}
	
	private void generateFleet(int maxFP, FleetSide side, List ships, MissionDefinitionAPI api) {
		int currFP = 0;
		
		if (side == FleetSide.PLAYER) {
			String [] choices = {
				"onslaught_Elite",
				"astral_Strike",
				"paragon_Elite",
				"odyssey_Balanced",
				"legion_Strike",
				"legion_FS",
				"doom_Strike"
			};
			String flagship = choices[(int) (Math.random() * (float) choices.length)];
			api.addToFleet(side, flagship, FleetMemberType.SHIP, true);
			currFP += api.getFleetPointCost(flagship);
		}
		if(side == FleetSide.ENEMY){
			String [] choices = {
				"uaf_slvv_core_elite",
				"uaf_novaeria_elite"
			};
			String flagship = choices[(int) (Math.random() * (float) choices.length)];
			api.addToFleet(side, flagship, FleetMemberType.SHIP, true);
			currFP += api.getFleetPointCost(flagship);
		}
		
		while (true) {
			int index = (int)(Math.random() * ships.size());
			String id = (String) ships.get(index);
			currFP += api.getFleetPointCost(id);
			if (currFP > maxFP) {
				return;
			}
			
			if (id.endsWith("_wing")) {
				api.addToFleet(side, id, FleetMemberType.FIGHTER_WING, false);
			} else {
				api.addToFleet(side, id, FleetMemberType.SHIP, false);
			}
		}
	}
	
	public void defineMission(MissionDefinitionAPI api) {

		addShip("doom_Strike", 3);
		addShip("shade_Assault", 7);
		addShip("afflictor_Strike", 7);
		addShip("hyperion_Attack", 3);
		addShip("hyperion_Strike", 3);
		addShip("onslaught_Standard", 3);
		addShip("onslaught_Outdated", 3);
		addShip("onslaught_Elite", 1);
		addShip("astral_Elite", 3);
		addShip("astral_Strike", 3);
		addShip("astral_Attack", 3);
		addShip("paragon_Elite", 1);
		addShip("legion_Strike", 1);
		addShip("legion_Assault", 1);
		addShip("legion_Escort", 1);
		addShip("legion_FS", 1);
		addShip("odyssey_Balanced", 2);
		addShip("conquest_Elite", 3);
		addShip("eagle_Assault", 5);
		addShip("falcon_Attack", 5);
		addShip("venture_Balanced", 5);
		addShip("apogee_Balanced", 5);
		addShip("aurora_Balanced", 5);
		addShip("aurora_Balanced", 5);
		addShip("gryphon_FS", 7);
		addShip("gryphon_Standard", 7);
		addShip("mora_Assault", 3);
		addShip("mora_Strike", 3);
		addShip("mora_Support", 3);
		addShip("dominator_Assault", 5);
		addShip("dominator_Support", 5);
		addShip("medusa_Attack", 5);
		addShip("condor_Support", 15);
		addShip("condor_Strike", 15);
		addShip("condor_Attack", 15);
		addShip("enforcer_Assault", 15);
		addShip("enforcer_CS", 15);
		addShip("hammerhead_Balanced", 10);
		addShip("hammerhead_Elite", 5);
		addShip("drover_Strike", 10);
		addShip("sunder_CS", 10);
		addShip("gemini_Standard", 8);
		addShip("buffalo2_FS", 20);
		addShip("lasher_CS", 20);
		addShip("lasher_Standard", 20);
		addShip("hound_Standard", 15);
		addShip("tempest_Attack", 15);
		addShip("brawler_Assault", 15);
		addShip("wolf_CS", 2);
		addShip("hyperion_Strike", 1);
		addShip("vigilance_Standard", 10);
		addShip("vigilance_FS", 15);
		addShip("tempest_Attack", 2);
		addShip("brawler_Assault", 10);

		//add UAF ships
		//add flagships

		//carriers
		addEnemy("uaf_exoria_bomber", 5);
		addEnemy("uaf_exoria_standard", 5);
		addEnemy("uaf_exoria_superiority", 5);
		addEnemy("uaf_exoria_surgical", 5);
		addEnemy("uaf_m_exoria_bomber", 5);
		addEnemy("uaf_m_exoria_standard", 5);
		addEnemy("uaf_m_exoria_strike", 5);
		addEnemy("uaf_m_exoria_superiority", 5);

		addEnemy("uaf_ai_minoria_standard", 5);
		addEnemy("uaf_ai_minoria_strategic", 5);
		addEnemy("uaf_ai_minoria_strike", 5);
		addEnemy("uaf_ai_minoria_superiority", 5);
		addEnemy("uaf_ai_minoria_surgical", 5);
		addEnemy("uaf_m_minoria_bomber", 5);
		addEnemy("uaf_m_minoria_standard", 5);
		addEnemy("uaf_m_minoria_strike", 5);
		addEnemy("uaf_m_minoria_superiorty", 5);
		addEnemy("uaf_minoria_bomber", 5);
		addEnemy("uaf_minoria_standard", 5);
		addEnemy("uaf_minoria_superiority", 5);
		addEnemy("uaf_minoria_surgical", 5);

		//backbone
		addEnemy("uaf_ai_inazuma_standard", 5);
		addEnemy("uaf_ai_inazuma_support", 5);
		addEnemy("uaf_ai_inazuma_suppressor", 5);
		addEnemy("uaf_m_inazuma_assault", 5);
		addEnemy("uaf_m_inazuma_standard", 5);
		addEnemy("uaf_m_inazuma_support", 5);
		addEnemy("uaf_m_inazuma_suppressor", 5);
		addEnemy("uaf_inazuma_assault", 5);
		addEnemy("uaf_inazuma_standard", 5);
		addEnemy("uaf_inazuma_defense", 5);
		addEnemy("uaf_inazuma_suppressor", 5);

		addEnemy("uaf_ai_nakiha_assault", 5);
		addEnemy("uaf_ai_nakiha_escort", 5);
		addEnemy("uaf_ai_nakiha_standard", 5);
		addEnemy("uaf_ai_nakiha_support", 5);
		addEnemy("uaf_ai_nakiha_suppressor", 5);
		addEnemy("uaf_m_nakiha_multirole", 5);
		addEnemy("uaf_m_nakiha_raider", 5);
		addEnemy("uaf_m_nakiha_standard", 5);
		addEnemy("uaf_m_nakiha_suppressor", 5);
		addEnemy("uaf_nakiha_assault", 5);
		addEnemy("uaf_nakiha_multirole", 5);
		addEnemy("uaf_nakiha_standard", 5);
		addEnemy("uaf_nakiha_suppressor", 5);

		//massproduce
		addEnemy("uaf_ai_rama_corvette_assault", 10);
		addEnemy("uaf_ai_rama_corvette_defense", 10);
		addEnemy("uaf_ai_rama_corvette_standard", 10);
		addEnemy("uaf_ai_rama_corvette_suppressor", 10);
		addEnemy("uaf_m_rama_corvette_multirole", 10);
		addEnemy("uaf_m_rama_corvette_raider", 10);
		addEnemy("uaf_m_rama_corvette_standard", 10);
		addEnemy("uaf_m_rama_corvette_suppressor", 10);
		addEnemy("uaf_rama_corvette_standard", 10);
		addEnemy("uaf_rama_corvette_raider", 10);
		addEnemy("uaf_rama_corvette_suppressor", 10);
		
		addEnemy("uaf_ai_lovela_assault", 10);
		addEnemy("uaf_ai_lovela_standard", 10);
		addEnemy("uaf_ai_lovela_support", 10);
		addEnemy("uaf_ai_lovela_suppressor", 10);
		addEnemy("uaf_m_lovela_assault", 10);
		addEnemy("uaf_m_lovela_multirole", 10);
		addEnemy("uaf_m_lovela_standard", 10);
		addEnemy("uaf_m_lovela_suppressor", 10);
		addEnemy("uaf_lovela_assault", 10);
		addEnemy("uaf_lovela_standard", 10);
		addEnemy("uaf_lovela_strike", 10);
		addEnemy("uaf_lovela_suppression", 10);

		addEnemy("uaf_m_perwira_standard", 15);

		// addEnemy("uaf_sentry_standard", 10);
		addEnemy("uaf_m_sentry_prototype", 10);

		addEnemy("uaf_m_machi_apa_transport", 10);
		addEnemy("uaf_machi_apa_transport", 10);

		//flagship candidate
		addEnemy("uaf_ai_rillaru_assault", 3);
		addEnemy("uaf_ai_rillaru_standard", 3);
		addEnemy("uaf_ai_rillaru_suppressor", 3);
		addEnemy("uaf_rillaru_assault", 3);
		addEnemy("uaf_rillaru_defense", 3);
		addEnemy("uaf_rillaru_standard", 3);
		addEnemy("uaf_rillaru_suppression", 3);

		addEnemy("uaf_m_reisen_assault", 3);
		addEnemy("uaf_m_reisen_standard", 3);
		addEnemy("uaf_m_reisen_support", 3);
		addEnemy("uaf_m_reisen_suppressor", 3);
		addEnemy("uaf_reisen_standard", 3);
		addEnemy("uaf_reisen_assault", 3);
		addEnemy("uaf_reisen_suppression", 3);

		addEnemy("uaf_isenoria_prototype_assault", 3);
		addEnemy("uaf_isenoria_prototype_bomber", 3);
		addEnemy("uaf_isenoria_prototype_standard", 3);
		addEnemy("uaf_isenoria_prototype_surgical", 3);

		addEnemy("uaf_ai_shinoria_standard", 3);
		addEnemy("uaf_ai_shinoria_superiority", 3);
		addEnemy("uaf_ai_shinoria_strike", 3);
		addEnemy("uaf_ai_shinoria_suppressor", 3);
		addEnemy("uaf_ai_shinoria_surgical", 3);
		addEnemy("uaf_shinoria_standard", 3);
		addEnemy("uaf_shinoria_strike", 3);
		addEnemy("uaf_shinoria_superiority", 3);
		addEnemy("uaf_shinoria_surgical", 3);

		//supper carrier
		addEnemy("uaf_ai_neuxoria_bomber", 1);
		addEnemy("uaf_ai_neuxoria_standard", 1);
		addEnemy("uaf_ai_neuxoria_strategic", 1);
		addEnemy("uaf_ai_neuxoria_suppressor", 1);
		addEnemy("uaf_ai_neuxoria_superiority", 1);
		addEnemy("uaf_ai_neuxoria_surgical", 1);
		
		addEnemy("uaf_m_zepporia_bomber", 1);
		addEnemy("uaf_m_zepporia_multirole", 1);
		addEnemy("uaf_m_zepporia_standard", 1);
		addEnemy("uaf_m_zepporia_strike", 1);
		addEnemy("uaf_m_zepporia_superiority", 1);

		

		//special
		addEnemy("uaf_m_osela_suv_standard", 1);
		addEnemy("uaf_eyeseau_standard", 1);
		addEnemy("uaf_eyeseau_defense", 1);
		addEnemy("uaf_eyeseau_offensive", 1);
		
		// Set up the fleets so we can add ships and fighter wings to them.
		// In this scenario, the fleets are attacking each other, but
		// in other scenarios, a fleet may be defending or trying to escape
		api.initFleet(FleetSide.PLAYER, "ISS", FleetGoal.ATTACK, false, 5);
		api.initFleet(FleetSide.ENEMY, "UAF", FleetGoal.ATTACK, true, 5);

		// Set a small blurb for each fleet that shows up on the mission detail and
		// mission results screens to identify each side.
		api.setFleetTagline(FleetSide.PLAYER, "Your forces");
		api.setFleetTagline(FleetSide.ENEMY, "Enemy forces");
		
		// These show up as items in the bulleted list under 
		// "Tactical Objectives" on the mission detail screen
		api.addBriefingItem("Defeat all enemy forces");
		
		// Set up the fleets
		generateFleet(100 + (int)((float) Math.random() * 50), FleetSide.PLAYER, ships, api);
		generateFleet(100 + (int)((float) Math.random() * 50), FleetSide.ENEMY, uaf_ships, api);
		
		// Set up the map.
		float width = 24000f;
		float height = 18000f;
		api.initMap((float)-width/2f, (float)width/2f, (float)-height/2f, (float)height/2f);
		
		float minX = -width/2;
		float minY = -height/2;
		
		
		for (int i = 0; i < 50; i++) {
			float x = (float) Math.random() * width - width/2;
			float y = (float) Math.random() * height - height/2;
			float radius = 100f + (float) Math.random() * 400f; 
			api.addNebula(x, y, radius);
		}
		
		// Add objectives
		api.addObjective(minX + width * 0.25f + 2000, minY + height * 0.25f + 2000, "nav_buoy");
		api.addObjective(minX + width * 0.75f - 2000, minY + height * 0.25f + 2000, "comm_relay");
		api.addObjective(minX + width * 0.75f - 2000, minY + height * 0.75f - 2000, "nav_buoy");
		api.addObjective(minX + width * 0.25f + 2000, minY + height * 0.75f - 2000, "comm_relay");
		api.addObjective(minX + width * 0.5f, minY + height * 0.5f, "sensor_array");
		
		String [] planets = {"barren", "terran", "gas_giant", "ice_giant", "cryovolcanic", "frozen", "jungle", "desert", "arid"};
		String planet = planets[(int) (Math.random() * (double) planets.length)];
		float radius = 100f + (float) Math.random() * 150f;
		api.addPlanet(0, 0, radius, planet, 200f, true);
	}

}





