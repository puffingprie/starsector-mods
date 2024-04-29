package data.scripts.world.systems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Terrain;
import com.fs.starfarer.api.impl.campaign.procgen.NebulaEditor;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;

public class HMI_hazard implements SectorGeneratorPlugin {

	    public static SectorEntityToken getSectorAccess() {
        return Global.getSector().getStarSystem("Hazard").getEntityById("hazard_star");
    }
	@Override
	public void generate(SectorAPI sector) {

		StarSystemAPI system = sector.createStarSystem("Hazard");
		LocationAPI hyper = Global.getSector().getHyperspace();
		system.setBackgroundTextureFilename("graphics/backgrounds/background1.jpg");

		// create the star and generate the hyperspace anchor for this system
		PlanetAPI star = system.initStar("hazard_star", // unique id for this star
				"star_yellow",  // id in planets.json
				200f,          // radius (in pixels at default zoom)
				750); // corona radius, from star edge
		system.setLightColor(new Color(245, 250, 255)); // light color in entire system, affects all entities
		system.getLocation().set(-20000, -20000);

		//Nevermore Station
		SectorEntityToken nevermore_station1 = system.addCustomEntity("nevermore_station", "Nevermore Station", "station_side02", "HMI");
		nevermore_station1.setCircularOrbitPointingDown(star, 0, 1500, 30);
		nevermore_station1.setCustomDescriptionId("nevermore_station");

		system.addAsteroidBelt(star, 250, 2450, 256, 150, 250, Terrain.ASTEROID_BELT, null);
		system.addRingBand(star, "misc", "rings_dust0", 256f, 0, Color.blue, 256f, 2450, 80f);
		system.addRingBand(star, "misc", "rings_dust0", 256f, 0, Color.cyan, 128f, 2300, 80f);
		system.addRingBand(star, "misc", "rings_dust0", 256f, 1, Color.darkGray, 512f, 2550, 80f);
		system.addRingBand(star, "misc", "rings_dust0", 256f, 1, Color.white, 512f, 2350, 80f);


		//Fuyutsuki and Gemima
		PlanetAPI hazard1 = system.addPlanet("fuyutsuki", star, "Fuyutsuki", "tundra", 30, 175, 3000, 350);
		hazard1.getSpec().setGlowTexture(Global.getSettings().getSpriteName("hab_glows", "volturn"));
		hazard1.getSpec().setGlowColor(new Color(255, 255, 255, 255));
		hazard1.getSpec().setUseReverseLightForGlow(true);
		hazard1.applySpecChanges();
		hazard1.setCustomDescriptionId("planet_fuyutsuki");
		hazard1.setInteractionImage("illustrations", "eochu_bres");

		PlanetAPI hazard1a = system.addPlanet("tyrol", hazard1, "Tyrol", "barren-bombarded", 40, 35, 750, 40);

		SectorEntityToken fuyutsuki_station = system.addCustomEntity("fuyutsuki_station", "Gemima Starport", "station_side03", "HMI");
		fuyutsuki_station.setCircularOrbitPointingDown(hazard1, 90, 450, 25);
		fuyutsuki_station.addTag("hmi_fuyutsuki_station");
		fuyutsuki_station.setCustomDescriptionId("fuyutsuki_station_desc");
		fuyutsuki_station.setInteractionImage("illustrations", "orbital");

		//JUMP POINT
		JumpPointAPI jumpPoint1 = Global.getFactory().createJumpPoint("hazard_jumpPointA", "Fuyutsuki Jump-Point");
		OrbitAPI orbit = Global.getFactory().createCircularOrbit(star, 50, 3000, 350);
		jumpPoint1.setOrbit(orbit);
		jumpPoint1.setRelatedPlanet(hazard1);
		jumpPoint1.setStandardWormholeToHyperspaceVisual();
		system.addEntity(jumpPoint1);

		//Chan Huan
		PlanetAPI hazard2 = system.addPlanet("chan_huan", star, "Chan Huan", "gas_giant", 200, 300, 6000, 350);
		system.addRingBand(hazard2, "misc", "rings_ice0", 256f, 2, Color.white, 256f, 1000, 21f, Terrain.RING, "Chandela's Wedding Ring");

		Misc.initConditionMarket(hazard2);
		hazard2.getMarket().setSurveyLevel(MarketAPI.SurveyLevel.FULL);
		hazard2.getMarket().addCondition(Conditions.COLD);
		hazard2.getMarket().addCondition(Conditions.HIGH_GRAVITY);
		hazard2.getMarket().addCondition(Conditions.VOLATILES_TRACE);
		hazard2.getMarket().addCondition(Conditions.EXTREME_WEATHER);
		hazard2.getMarket().addCondition(Conditions.DENSE_ATMOSPHERE);

		SectorEntityToken hazard2a = system.addPlanet("timms", hazard2, "Timms", "jungle", 20, 70, 750, 150);
		hazard2a.setCustomDescriptionId("planet_timms");
		hazard2a.setInteractionImage("illustrations", "cargo_loading");


		PlanetAPI hazard2b = system.addPlanet("hazard2b", hazard2, "Clarkson", "barren-bombarded", 320, 70, 1400, 75);

		Misc.initConditionMarket(hazard2b);
		hazard2b.getMarket().setSurveyLevel(MarketAPI.SurveyLevel.FULL);
		hazard2b.getMarket().addCondition(Conditions.COLD);
		hazard2b.getMarket().addCondition(Conditions.NO_ATMOSPHERE);
		hazard2b.getMarket().addCondition(Conditions.ORE_SPARSE);
		hazard2b.getMarket().addCondition(Conditions.TECTONIC_ACTIVITY);

		PlanetAPI hazard2c = system.addPlanet("hazard2c", hazard2, "Thelma", "barren", 210, 70, 1200, 140);

		hazard2c.getMarket().setSurveyLevel(MarketAPI.SurveyLevel.FULL);
		hazard2c.getMarket().addCondition(Conditions.VERY_COLD);
		hazard2c.getMarket().addCondition(Conditions.LOW_GRAVITY);
		hazard2c.getMarket().addCondition(Conditions.RARE_ORE_SPARSE);
		hazard2c.getMarket().addCondition(Conditions.THIN_ATMOSPHERE);

		PlanetAPI hazard3 = system.addPlanet("chumundar", star, "Chumundar", "ice_giant", 200, 450, 12000, 900);

		Misc.initConditionMarket(hazard3);
		hazard3.getMarket().setSurveyLevel(MarketAPI.SurveyLevel.FULL);
		hazard3.getMarket().addCondition(Conditions.VERY_COLD);
		hazard3.getMarket().addCondition(Conditions.HIGH_GRAVITY);
		hazard3.getMarket().addCondition(Conditions.VOLATILES_ABUNDANT);
		hazard3.getMarket().addCondition(Conditions.DENSE_ATMOSPHERE);



		// Relay
		SectorEntityToken hazard_relay = system.addCustomEntity("hazard_relay", // unique id
				"Hazard Relay", // name - if null, defaultName from custom_entities.json will be used
				"comm_relay", // type of object, defined in custom_entities.json
				"HMI"); // faction
		hazard_relay.setCircularOrbitPointingDown(star, 300, 4500, 700);

		SectorEntityToken hazard_buoy = system.addCustomEntity("hazard_buoy", // unique id
				"Hazard Nav Buoy", // name - if null, defaultName from custom_entities.json will be used
				"nav_buoy_makeshift", // type of object, defined in custom_entities.json
				"HMI"); // faction
		hazard_buoy.setCircularOrbitPointingDown(star, 120, 4500, 700);

		SectorEntityToken hazard_sensor = system.addCustomEntity("hazard_sensor", // unique id
				"Hazard Sensor Array", // name - if null, defaultName from custom_entities.json will be used
				"sensor_array_makeshift", // type of object, defined in custom_entities.json
				"HMI"); // faction
		hazard_sensor.setCircularOrbitPointingDown(star, 60, 8000, 1000);

		// jump point Chan Huan
		JumpPointAPI jumpPoint = Global.getFactory().createJumpPoint("hazard_jumpPointB", "Timms Jump-point");
		OrbitAPI orbitouter = Global.getFactory().createCircularOrbit(hazard2a, 270, 2000, 350);
		jumpPoint.setOrbit(orbitouter);
		jumpPoint.setStandardWormholeToHyperspaceVisual();
		system.addEntity(jumpPoint);

		// generates hyperspace destinations for in-system jump points
		system.autogenerateHyperspaceJumpPoints(true, true);
		cleanup(system);
	}

	void cleanup(StarSystemAPI system){
		HyperspaceTerrainPlugin plugin = (HyperspaceTerrainPlugin) Misc.getHyperspaceTerrain().getPlugin();
		NebulaEditor editor = new NebulaEditor(plugin);
		float minRadius = plugin.getTileSize() * 2f;

		float radius = system.getMaxRadiusInHyperspace();
		editor.clearArc(system.getLocation().x, system.getLocation().y, 0, radius + minRadius * 0.5f, 0, 360f);
		editor.clearArc(system.getLocation().x, system.getLocation().y, 0, radius + minRadius, 0, 360f, 0.25f);
	}
}
