package data.scripts.world.systems.haven;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.impl.campaign.ids.Terrain;
import com.fs.starfarer.api.impl.campaign.procgen.NebulaEditor;
import com.fs.starfarer.api.impl.campaign.procgen.StarAge;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.terrain.AsteroidFieldTerrainPlugin.AsteroidFieldParams;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;

public class Aphrodite {

	public void generate(SectorAPI sector) {
		StarSystemAPI system = sector.createStarSystem("Aphrodite");

		system.setBackgroundTextureFilename("graphics/backgrounds/background_love.jpg");

		PlanetAPI aphrodite_star = system.initStar("aphrodite", // unique id for this star
				"star_yellow", // Star type. Vanilla star types can be found in starsector-core/data/campaign/procgen/star_gen_data.csv and starsector-core/data/config/planets.json
				600f, 		  //gfbdfgb radius (in pixels at default zoom)
				350); // corona radius, from star edge

		system.setLightColor(new Color(255,155,81)); // light color in entire system, affects all entities

		PlanetAPI aphrodite_b = system.addPlanet("amor", aphrodite_star, "Amor", "toxic", 270, 140, 3800, 80);
		aphrodite_b.setCustomDescriptionId("anvil_amor");


		// Kumari Aru trojans - L4 leads, L5 follows
		SectorEntityToken edinburghL4 = system.addTerrain(Terrain.ASTEROID_FIELD,
				new AsteroidFieldParams(
						300f, // min radius
						500f, // max radius
						16, // min asteroid count
						24, // max asteroid count
						4f, // min asteroid radius
						16f, // max asteroid radius
						"Aphrodite Asteroids")); // null for default name

		SectorEntityToken edinburghL5 = system.addTerrain(Terrain.ASTEROID_FIELD,
				new AsteroidFieldParams(
						300f, // min radius
						500f, // max radius
						16, // min asteroid count
						24, // max asteroid count
						4f, // min asteroid radius
						16f, // max asteroid radius
						"Aphrodite Asteroids")); // null for default name

		edinburghL4.setCircularOrbit(aphrodite_star, 270 +60, 2800, 80);
		edinburghL5.setCircularOrbit(aphrodite_star, 270 -60, 2800, 80);



		SectorEntityToken aphrodite_loc1 = system.addCustomEntity(null, null, "comm_relay_makeshift", "keruvim");
		aphrodite_loc1.setCircularOrbitPointingDown(aphrodite_star, 270-60, 2800, 80);


		PlanetAPI stirling = system.addPlanet("afecto", aphrodite_star, "Afecto", "desert", 220, 160, 1300, 180);
		stirling.getSpec().setGlowColor(new Color(255, 20, 153,255));
		stirling.getSpec().setUseReverseLightForGlow(true);
		stirling.applySpecChanges();
		stirling.setCustomDescriptionId("anvil_afecto");

		// counter-orbit sensor array

		SectorEntityToken aphrodite_loc3 = system.addCustomEntity(null, null, "nav_buoy_makeshift", "keruvim");
		aphrodite_loc3.setCircularOrbitPointingDown(aphrodite_star, 220-180, 4300, 180);
		;



		// a gate in the Lagrangian of Chalcedon
		SectorEntityToken gate = system.addCustomEntity("aphrodite_gate", // unique id
				"Aphrodite Gate", // name - if null, defaultName from custom_entities.json will be used
				"inactive_gate", // type of object, defined in custom_entities.json
				null); // faction
		gate.setCircularOrbit(aphrodite_star, 220-60, 4300, 180);

		// a jump in the other one: Rama's Bridge :  Jump-point
		JumpPointAPI jumpPoint2 = Global.getFactory().createJumpPoint("aphrodite_jump", "Aphrodite Jump");
		jumpPoint2.setCircularOrbit( system.getEntityById("aphrodite"), 220 + 60, 4300, 180);
		jumpPoint2.setRelatedPlanet(stirling);
		system.addEntity(jumpPoint2);


		PlanetAPI leeds = system.addPlanet("cuidado", aphrodite_star, "Cuidado", "barren", 180, 90, 7300, 210);
		leeds.getSpec().setPlanetColor(new Color(255,250,245,255));
		leeds.getSpec().setPitch(-60f);
		leeds.getSpec().setTilt(10f);
		leeds.applySpecChanges();
		leeds.setCustomDescriptionId("anvil_cuidado");

		//PlanetAPI edinburgh_c = system.addPlanet("peru_aru", edinburgh_star, "Peru Aru", "barren", 270, 100, 4000, 190);
		//PlanetAPI edinburgh_d = system.addPlanet("muthur", edinburgh_star, "Muthur", "frozen", 270, 100, 4800, 240);


		system.addRingBand(aphrodite_star, "misc", "rings_dust0", 256f, 0, Color.white, 256f, 5900, 220f, null, null);
		system.addAsteroidBelt(aphrodite_star, 150, 5900, 128, 200, 240, Terrain.ASTEROID_BELT, null);


		SectorEntityToken aphrodite_loc2 = system.addCustomEntity(null, null, "sensor_array_makeshift", "keruvim");
		aphrodite_loc2.setCircularOrbitPointingDown(aphrodite_star, 180 + 60, 8100, 300);

		float radiusAfter = StarSystemGenerator.addOrbitingEntities(system, aphrodite_star, StarAge.AVERAGE,
				1, 2, // min/max entities to add
				9500, // radius to start adding at
				6, // name offset - next planet will be <system name> <roman numeral of this parameter + 1>
				false); // whether to use custom or system-name based names

		system.autogenerateHyperspaceJumpPoints(true, true);

		HyperspaceTerrainPlugin plugin = (HyperspaceTerrainPlugin) Misc.getHyperspaceTerrain().getPlugin();
		NebulaEditor editor = new NebulaEditor(plugin);
		float minRadius = plugin.getTileSize() * 2f;

		float radius = system.getMaxRadiusInHyperspace();
		editor.clearArc(system.getLocation().x, system.getLocation().y, 0, radius + minRadius, 0, 360f);
		editor.clearArc(system.getLocation().x, system.getLocation().y, 0, radius + minRadius, 0, 360f, 0.25f);

		//Misc.setFullySurveyed(edinburgh_b.getMarket(), null, false);
	}

}
