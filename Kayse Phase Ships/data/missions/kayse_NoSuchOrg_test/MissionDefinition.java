package data.missions.kayse_NoSuchOrg_test;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.ids.StarTypes;
import com.fs.starfarer.api.loading.VariantSource;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;
import com.fs.starfarer.api.combat.ShipAPI;

public class MissionDefinition implements MissionDefinitionPlugin {

	public void defineMission(MissionDefinitionAPI api) {
		//Original Mission from ScalarTech Solutions:
		//ScalarTech Solutions Mod by Nia Tahl is licensed under a
		//Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License.
		//Any derivative work must also comply with Starsector's EULA

		// Set up the fleets so we can add ships and fighter wings to them.
		// In this scenario, the fleets are attacking each other, but
		// in other scenarios, a fleet may be defending or trying to escape
		api.initFleet(FleetSide.PLAYER, "NSO", FleetGoal.ATTACK, false);
		api.initFleet(FleetSide.ENEMY, "ISS", FleetGoal.ATTACK, true);

		
		// Set a small blurb for each fleet that shows up on the mission detail and
		// mission results screens to identify each side.
		api.setFleetTagline(FleetSide.PLAYER, "'No Such Organization' Task Force");
		api.setFleetTagline(FleetSide.ENEMY, "Victims");
		
		// These show up as items in the bulleted list under 
		// "Tactical Objectives" on the mission detail screen
		api.addBriefingItem("PHASEOPS fleet exercise: Destroy all OPFOR.");
		
		// Set up the player's fleet.
		api.addToFleet(FleetSide.PLAYER, "kayse_dullahan_variant", FleetMemberType.SHIP, true);
		api.addToFleet(FleetSide.PLAYER, "kayse_azrael_elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "kayse_banshee_variant", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "kayse_deathknight_beam", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "kayse_ghost_strike", FleetMemberType.SHIP, false);		
		api.addToFleet(FleetSide.PLAYER, "kayse_harbinger_NSO_variant", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "kayse_haunt_escort", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "kayse_necromancer_support", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "kayse_ghoul_variant", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "kayse_grim_variant", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "kayse_grime_variant", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "kayse_pyre_variant", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "kayse_wight_variant", FleetMemberType.SHIP, false);
		
		// Set up the enemy fleet.
		for (int i = 0; i < 5; i++){
			api.addToFleet(FleetSide.ENEMY, "buffalo2_FS", FleetMemberType.SHIP, false);
		}
		
		/* api.addToFleet(FleetSide.ENEMY, "buffalo2_FS", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "buffalo2_FS", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "buffalo2_FS", FleetMemberType.SHIP, false); */

		
		// Set up the map.
		float width = 10000f;
		float height = 10000f;
		api.initMap((float)-width/2f, (float)width/2f, (float)-height/2f, (float)height/2f);
		
		float minX = -width/2;
		float minY = -height/2;
		
		// Add an asteroid field
		api.addAsteroidField(minX, minY + height / 2, 0, 8000f,
							 20f, 70f, 100);
		
	}
}
