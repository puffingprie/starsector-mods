package data.missions.Metelson;

import com.fs.starfarer.api.combat.BattleCreationContext;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.combat.EscapeRevealPlugin;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;


public class MissionDefinition implements MissionDefinitionPlugin {

    
        @Override
	public void defineMission(MissionDefinitionAPI api) {

		
		// Set up the fleets so we can add ships and fighter wings to them.
		// In this scenario, the fleets are attacking each other, but
		// in other scenarios, a fleet may be defending or trying to escape
		api.initFleet(FleetSide.PLAYER, "MSV", FleetGoal.ATTACK, false, 5);
		api.initFleet(FleetSide.ENEMY, "HSS", FleetGoal.ATTACK, true);

		// Set a small blurb for each fleet that shows up on the mission detail and
		// mission results screens to identify each side.
		api.setFleetTagline(FleetSide.PLAYER, "Metelsons Industries testing fleet");
		api.setFleetTagline(FleetSide.ENEMY, "Hegemony convoy and assisting patrol force");
		
		// These show up as items in the bulleted list under 
		// "Tactical Objectives" on the mission detail screen
		api.addBriefingItem("FOR TEST PURPOSE ONLY!");
		
		// Set up the player's fleet.  Variant names come from the
		// files in data/variants and data/variants/fighters
                
                // FleetMemberAPI addToFleet(FleetSide side, String variantId, FleetMemberType type, #String shipName, boolean isFlagship, #CrewXPLevel level);
                api.addToFleet(FleetSide.PLAYER, "mi_thrudgelmir_standard", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.PLAYER, "mi_drengur_standard", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.PLAYER, "mi_acutor_standard", FleetMemberType.SHIP, false);
				api.addToFleet(FleetSide.PLAYER, "mi_mantarn_standard", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.PLAYER, "mi_gadfar_standard", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.PLAYER, "mi_cringer_standard", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.PLAYER, "mi_grancursor_standard", FleetMemberType.SHIP, false);
				api.addToFleet(FleetSide.PLAYER, "mi_starliner_standard", FleetMemberType.SHIP, false);
				api.addToFleet(FleetSide.PLAYER, "mi_enforcer_standard", FleetMemberType.SHIP, false);
				api.addToFleet(FleetSide.PLAYER, "mi_hammerhead_standard", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.PLAYER, "mi_securi_standard", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.PLAYER, "mi_glima_standard", FleetMemberType.SHIP, false);
				api.addToFleet(FleetSide.PLAYER, "mi_actelh35_standard", FleetMemberType.SHIP, false);
				api.addToFleet(FleetSide.PLAYER, "mi_actelh35a_standard", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.PLAYER, "mi_rastrum_standard", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.PLAYER, "mi_varingur_standard", FleetMemberType.SHIP, false);
				api.addToFleet(FleetSide.PLAYER, "mi_pala_standard", FleetMemberType.SHIP, true);
                api.addToFleet(FleetSide.PLAYER, "mi_farin_standard", FleetMemberType.SHIP, true);
                api.addToFleet(FleetSide.PLAYER, "mi_cavos_standard", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.PLAYER, "mi_roma_standard", FleetMemberType.SHIP, false);
				api.addToFleet(FleetSide.PLAYER, "mi_glima_standard", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.PLAYER, "mi_mite_standard", FleetMemberType.SHIP, false);
				api.addToFleet(FleetSide.PLAYER, "mi_vigilance_standard", FleetMemberType.SHIP, false);
				api.addToFleet(FleetSide.PLAYER, "mi_wolf_standard", FleetMemberType.SHIP, false);
                

                
                


                
               


		// Set up the enemy fleet.
		api.addToFleet(FleetSide.ENEMY, "onslaught_Standard", FleetMemberType.SHIP, "HSS Naga", true);
		api.addToFleet(FleetSide.ENEMY, "onslaught_Outdated", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "onslaught_Outdated", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "dominator_Assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "dominator_Assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "mora_Assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "mora_Strike", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "mora_Strike", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "mora_Support", FleetMemberType.SHIP, false);
		//api.addToFleet(FleetSide.ENEMY, "dominator_Support", FleetMemberType.SHIP, false);
		//api.addToFleet(FleetSide.ENEMY, "dominator_Support", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "condor_Support", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "condor_Support", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "enforcer_Assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "enforcer_Assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "enforcer_CS", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "enforcer_CS", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "hound_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "hound_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "lasher_CS", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "lasher_CS", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "lasher_CS", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "lasher_CS", FleetMemberType.SHIP, false);
		
		
		
		// Set up the map.
		float width = 15000f;
		float height = 15000f;
		api.initMap((float)-width/2f, (float)width/2f, (float)-height/2f, (float)height/2f);
		
		float minX = -width/2;
		float minY = -height/2;
		
		// Add an asteroid field
		api.addAsteroidField(minX, minY + height / 2, 0, 8000f,
							 20f, 70f, 100);
		
		api.addPlanet(0, 0, 50f, "star_yellow", 250f, true);
		
	}
}




