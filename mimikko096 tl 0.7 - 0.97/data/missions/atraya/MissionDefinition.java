package data.missions.atraya;

import java.util.List;

import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.ids.StarTypes;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;

public class MissionDefinition implements MissionDefinitionPlugin {

	public void defineMission(MissionDefinitionAPI api) {

		// Set up the fleets so we can add ships and fighter wings to them.
		// In this scenario, the fleets are attacking each other, but
		// in other scenarios, a fleet may be defending or trying to escape
		api.initFleet(FleetSide.PLAYER, "ATRAYA", FleetGoal.ATTACK, false);
		api.initFleet(FleetSide.ENEMY, "TTS", FleetGoal.ATTACK, true);

		// Set a small blurb for each fleet that shows up on the mission detail and
		// mission results screens to identify each side.
		api.setFleetTagline(FleetSide.PLAYER, "ATRAYA Glory");
		api.setFleetTagline(FleetSide.ENEMY, "[Redacted] era Tri-Tachyon and Hegemony Intervention Force");
		
		// These show up as items in the bulleted list under 
		// "Tactical Objectives" on the mission detail screen
		api.addBriefingItem("Display ATRAYA's Glory");
		api.addBriefingItem(" ATRAYA Ens must sruvive");
		
		// Set up the player's fleet.  Variant names come from the
		// files in data/variants and data/variants/fighters

		api.addToFleet(FleetSide.PLAYER, "mimikko_kasutariri_radiant_1_variant", FleetMemberType.SHIP, "ATRAYA Ens", true);
		api.addToFleet(FleetSide.PLAYER, "wolf_atraya_stand", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "wolf_atraya_stand", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "wolf_atraya_stand", FleetMemberType.SHIP, false);



		// Set up the enemy fleet.
		api.addToFleet(FleetSide.ENEMY, "onslaught_Outdated", FleetMemberType.SHIP, "HSS Bismar", false);
		api.addToFleet(FleetSide.ENEMY, "onslaught_Outdated", FleetMemberType.SHIP, "HSS Yamato", false);
		api.addToFleet(FleetSide.ENEMY, "onslaught_Outdated", FleetMemberType.SHIP, "HSS Musasi", false);
		api.addToFleet(FleetSide.ENEMY, "dominator_XIV_Elite", FleetMemberType.SHIP, "HSS Mogami", false);
		api.addToFleet(FleetSide.ENEMY, "dominator_XIV_Elite", FleetMemberType.SHIP, "HSS Atago", false);
		api.addToFleet(FleetSide.ENEMY, "enforcer_d_pirates_Strike", FleetMemberType.SHIP, "HSS Asashio", false);
		api.addToFleet(FleetSide.ENEMY, "enforcer_d_pirates_Strike", FleetMemberType.SHIP, "HSS Kagero", false);

		api.addToFleet(FleetSide.ENEMY, "doom_Strike", FleetMemberType.SHIP, "TTS Buffalo", false);
		api.addToFleet(FleetSide.ENEMY, "doom_Strike", FleetMemberType.SHIP, "TTS BDes Moines", false);
		api.addToFleet(FleetSide.ENEMY, "doom_Strike", FleetMemberType.SHIP, "TTS BDes Moines", false);
		api.addToFleet(FleetSide.ENEMY, "astral_Attack", FleetMemberType.SHIP, "TTS Franklin", false);
		api.addToFleet(FleetSide.ENEMY, "medusa_Attack", FleetMemberType.SHIP, "TTS Sims", false);
		api.addToFleet(FleetSide.ENEMY, "medusa_Attack", FleetMemberType.SHIP, "TTS Fletcher", false);
		//api.addToFleet(FleetSide.ENEMY, "broadsword_wing", FleetMemberType.FIGHTER_WING, false, CrewXPLevel.VETERAN);
		//api.addToFleet(FleetSide.ENEMY, "broadsword_wing", FleetMemberType.FIGHTER_WING, false, CrewXPLevel.VETERAN);
		
		api.defeatOnShipLoss("ATRAYA Ens");
		
		// Set up the map.
		float width = 24000f;
		float height = 18000f;
		api.initMap((float)-width/2f, (float)width/2f, (float)-height/2f, (float)height/2f);
		
		float minX = -width/2;
		float minY = -height/2;
		
		// All the addXXX methods take a pair of coordinates followed by data for
		// whatever object is being added.
		
		// Add nebula clouds
		api.addNebula(minX + width * 0.4f, minY + height * 0.5f, 1000);
		api.addNebula(minX + width * 0.5f, minY + height * 0.5f, 1200);
		api.addNebula(minX + width * 0.6f, minY + height * 0.5f, 1400);
		
		// And a few random ones to spice up the playing field.
		// A similar approach can be used to randomize everything
		// else, including fleet composition.
		for (int i = 0; i < 5; i++) {
			float x = (float) Math.random() * width - width/2;
			float y = (float) Math.random() * height - height/2;
			float radius = 100f + (float) Math.random() * 400f; 
			api.addNebula(x, y, radius);
		}
		
		// Add objectives. These can be captured by each side
		// and provide stat bonuses and extra command points to
		// bring in reinforcements.
		// Reinforcements only matter for large fleets - in this
		// case, assuming a 100 command point battle size,
		// both fleets will be able to deploy fully right away.
		api.addObjective(minX + width * 0.5f, minY + height * 0.5f, 
						 "sensor_array");
		api.addObjective(minX + width * 0.2f + 3000, minY + height * 0.25f + 2000, 
						 "nav_buoy");
		api.addObjective(minX + width * 0.8f - 3000, minY + height * 0.75f - 2000, 
						 "nav_buoy");
		
		// Add an asteroid field
		api.addAsteroidField(minX, minY + height / 2, 0, 8000f,
							 20f, 70f, 100);
		
		api.addPlugin(new BaseEveryFrameCombatPlugin() {
			public void init(CombatEngineAPI engine) {
				engine.getContext().setStandoffRange(10000f);
			}
			public void advance(float amount, List events) {
			}
		});
		
		
		api.addPlanet(0, 0, 50f, StarTypes.RED_GIANT, 250f, true);
	}

}
