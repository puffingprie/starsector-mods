package data.missions.pn_simulator;

import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;

public class MissionDefinition implements MissionDefinitionPlugin {

	public void defineMission(MissionDefinitionAPI api) {

		// Set up the fleets
		api.initFleet(FleetSide.PLAYER, "P9", FleetGoal.ATTACK, false, 7);
		api.initFleet(FleetSide.ENEMY, "HSS", FleetGoal.ATTACK, true, 5);

		// Set a blurb for each fleet
		api.setFleetTagline(FleetSide.PLAYER, "P9 task force");
		api.setFleetTagline(FleetSide.ENEMY, "Bennys Mercenary Inc");
		
		// These show up as items in the bulleted list under 
		// "Tactical Objectives" on the mission detail screen
		api.addBriefingItem("Use your Samaa-DSI to shut down enemy heavy hitters while you pick apart the other ships");
		api.addBriefingItem("Try not to die too fast");
		api.addBriefingItem("Defeat all enemy forces");
		
		// Set up the player's fleet
		api.addToFleet(FleetSide.PLAYER, "pn_kaala_combat", FleetMemberType.SHIP, "P9 Jolly", true);
		api.addToFleet(FleetSide.PLAYER, "pn_samaa-ebm_frontline", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "pn_aggr_mid", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "pn_samaa-dsi_standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "pn_eyfel_support", FleetMemberType.SHIP, false);		
		api.addToFleet(FleetSide.PLAYER, "pn_thelep_support", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "pn_korlo-skt_elite_support", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "pn_korlo-skt_support", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "pn_snz_escort", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "pn_snz_escort", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "pn_trundler_combat", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "pn_pdcruiser_standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "pn_sl-t3_barrage", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "pn_sl-t3_barrage", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "pn_tick_support", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "pn_cimex_assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "pn_sl-t1_attack", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "pn_sl-t1_attack", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "pn_spore", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "pn_spore-mk2", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "pn_sl-t2_longrange", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "pn_sl-t2_longrange", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "pn_sl-t2_longrange", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "pn_sl-t2_longrange", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "pn_sike-m_offensive", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "pn_sike-m_offensive", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "pn_sike-m_offensive", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "pn_silverfish-mpk_charger", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "pn_rs_kinetic", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "pn_rs_kinetic", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "pn_rs_kinetic", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "pn_rs_kinetic", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "pn_sike_energy", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "pn_sike_energy", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "pn_sike_energy", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "pn_sike_energy", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "pn_louse_standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "pn_mlmv_tug", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "pn_crig_unarmed", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "pn_buffalo_Standard", FleetMemberType.SHIP, false);                
		api.addToFleet(FleetSide.PLAYER, "pn_wolf_Strike", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "pn_wolf_Assault", FleetMemberType.SHIP, false);
                //api.addToFleet(FleetSide.PLAYER, "pn_pdliner", FleetMemberType.SHIP, false);
                
		
		// Set up the enemy fleet

		
		api.addToFleet(FleetSide.ENEMY, "medusa_Attack", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "medusa_Attack", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "sunder_CS", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "wolf_CS", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "wolf_CS", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "tempest_Attack", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "tempest_Attack", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "omen_PD", FleetMemberType.SHIP, false);

		api.addToFleet(FleetSide.ENEMY, "onslaught_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "dominator_Support", FleetMemberType.SHIP, false);
		
		api.addToFleet(FleetSide.ENEMY, "condor_FS", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "condor_FS", FleetMemberType.SHIP, false);

		api.addToFleet(FleetSide.ENEMY, "heron_Strike", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "heron_Attack", FleetMemberType.SHIP, false);
                
		api.addToFleet(FleetSide.ENEMY, "apogee_Balanced", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "apogee_Starting", FleetMemberType.SHIP, false);

		api.addToFleet(FleetSide.ENEMY, "scarab_Starting", FleetMemberType.SHIP, false);
                
		api.addToFleet(FleetSide.ENEMY, "eagle_Assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "eagle_Balanced", FleetMemberType.SHIP, false);

		api.addToFleet(FleetSide.ENEMY, "aurora_Balanced", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "aurora_Assault", FleetMemberType.SHIP, false);                
                
		api.addToFleet(FleetSide.ENEMY, "enforcer_Assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "enforcer_Assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "enforcer_Assault", FleetMemberType.SHIP, false);
		
		api.addToFleet(FleetSide.ENEMY, "lasher_CS", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "lasher_CS", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "lasher_CS", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "lasher_CS", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "lasher_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "lasher_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "lasher_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "lasher_Standard", FleetMemberType.SHIP, false);
		
		// Set up the map.
		float width = 24000f;
		float height = 18000f;
		api.initMap((float)-width/2f, (float)width/2f, (float)-height/2f, (float)height/2f);
		
		float minX = -width/2;
		float minY = -height/2;
		
		for (int i = 0; i < 25; i++) {
			float x = (float) Math.random() * width - width/2;
			float y = (float) Math.random() * height - height/2;
			float radius = 1000f + (float) Math.random() * 1000f; 
			api.addNebula(x, y, radius);
		}
		
		api.addNebula(minX + width * 0.8f - 2000, minY + height * 0.4f, 2000);
		api.addNebula(minX + width * 0.8f - 2000, minY + height * 0.5f, 2000);
		api.addNebula(minX + width * 0.8f - 2000, minY + height * 0.6f, 2000);
		
		api.addObjective(minX + width * 0.15f + 3000, minY + height * 0.3f + 1000, "nav_buoy");
		api.addObjective(minX + width * 0.4f + 1000, minY + height * 0.4f, "sensor_array");
		api.addObjective(minX + width * 0.8f - 2000, minY + height * 0.3f + 1000, "comm_relay");
		
		api.addObjective(minX + width * 0.85f - 3000, minY + height * 0.7f - 1000, "nav_buoy");
		api.addObjective(minX + width * 0.6f - 1000, minY + height * 0.6f, "sensor_array");
		api.addObjective(minX + width * 0.2f + 2000, minY + height * 0.7f - 1000, "comm_relay");
		
		api.addAsteroidField(minX, minY + height * 0.5f, 0, height,
							20f, 70f, 50);
		
		api.addPlanet(0, 0, 350f, "barren", 200f, true);
	}

}






