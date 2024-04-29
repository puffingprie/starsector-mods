package data.scripts.world.systems.haven;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.impl.campaign.procgen.NebulaEditor;
import com.fs.starfarer.api.impl.campaign.procgen.StarAge;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;

public class Skarseld {

	public void generate(SectorAPI sector) {
		StarSystemAPI system = sector.createStarSystem("Skärseld");
		
		system.setBackgroundTextureFilename("graphics/backgrounds/background3.jpg");

		PlanetAPI skarseld_star = system.initStar("skarseld", // unique id for this star
				"star_yellow", 900f, 390);
		
		system.setLightColor(new Color(255,205,205));

		PlanetAPI jord = system.addPlanet("anvil_jord", skarseld_star, "Jord", "jungle", 180, 150, 3300, 360);
		jord.setCustomDescriptionId("anvil_jord");



		JumpPointAPI jumpPoint2 = Global.getFactory().createJumpPoint("skarseld_jump", "Skarseld Inner Jump-point");
		jumpPoint2.setCircularOrbit( system.getEntityById("skarseld"), 120 + 60, 2700, 80);
		system.addEntity(jumpPoint2);


		PlanetAPI fisa = system.addPlanet("anvil_fisa", skarseld_star, "Fisa", "gas_giant", 230, 300, 8400, 250);
		fisa.setCustomDescriptionId("anvil_fisa");
		fisa.getSpec().setPlanetColor(new Color(255,225,170,255));
		fisa.getSpec().setAtmosphereColor(new Color(160,110,45,140));
		fisa.getSpec().setCloudColor(new Color(255,164,96,200));
		fisa.getSpec().setTilt(15);
		fisa.applySpecChanges();


		PlanetAPI pengar = system.addPlanet("anvil_pengar", fisa, "Pengar", "water", 110, 90, 600, 45);
		pengar.setCustomDescriptionId("anvil_pengar");
		pengar.getSpec().setGlowColor(new Color(255,255,255,255));
		pengar.getSpec().setUseReverseLightForGlow(true);
		pengar.applySpecChanges();
		pengar.setInteractionImage("illustrations", "space_bar");
	
		
		SectorEntityToken skarseld_loc1 = system.addCustomEntity(null, null, "sensor_array_makeshift", "keruvim");
		skarseld_loc1.setCircularOrbitPointingDown(skarseld_star, 180-120, 6600, 400);

		SectorEntityToken skarseld_loc2 = system.addCustomEntity(null, null, "comm_relay_makeshift", "keruvim");
		skarseld_loc2.setCircularOrbitPointingDown(skarseld_star, 180+120, 7300, 400);


		system.addAsteroidBelt(skarseld_star,
				400,
				4300,
				700,
				460,
				500);



		float radiusAfter = StarSystemGenerator.addOrbitingEntities(system, skarseld_star, StarAge.AVERAGE,
				2, 4,
				10000,
				5,
				true);

		system.autogenerateHyperspaceJumpPoints(true, true);

		//Getting rid of some hyperspace nebula, just in case
		HyperspaceTerrainPlugin plugin = (HyperspaceTerrainPlugin) Misc.getHyperspaceTerrain().getPlugin();
		NebulaEditor editor = new NebulaEditor(plugin);
		float minRadius = plugin.getTileSize() * 2f;

		float radius = system.getMaxRadiusInHyperspace();
		editor.clearArc(system.getLocation().x, system.getLocation().y, 0, radius + minRadius, 0, 360f);
		editor.clearArc(system.getLocation().x, system.getLocation().y, 0, radius + minRadius, 0, 360f, 0.25f);
	}

}
