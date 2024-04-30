package data.scripts.world.systems;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.terrain.AsteroidFieldTerrainPlugin.AsteroidFieldParams;
import com.fs.starfarer.api.impl.campaign.terrain.MagneticFieldTerrainPlugin.MagneticFieldParams;
import com.fs.starfarer.api.impl.campaign.procgen.NebulaEditor;
import com.fs.starfarer.api.impl.campaign.procgen.StarAge;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.util.Misc;

public class acs_ganzir implements SectorGeneratorPlugin {

	public void generate(SectorAPI sector) {
		
		StarSystemAPI system = sector.createStarSystem("Ganzir");
		LocationAPI hyper = Global.getSector().getHyperspace();
		
		system.setBackgroundTextureFilename("graphics/backgrounds/background4.jpg");
		
		// create the star and generate the hyperspace anchor for this system
		PlanetAPI ganzir_star = system.initStar("ganzir", // unique id for this star 
											StarTypes.RED_GIANT,  // id in planets.json
										    1100f, 		  // radius (in pixels at default zoom)
										    500); // corona radius, from star edge
		system.setLightColor(new Color(255, 200, 210)); // light color in entire system, affects all entities
		system.getLocation().set(-68000, -40000);
		
		float innerOrbitDistance = StarSystemGenerator.addOrbitingEntities(
                system, //star system variable, used to add entities
                star, //focus object for entities to orbit
                StarAge.AVERAGE, //used by generator to decide which kind of planets to add
                1, //minimum number of entities
                4, //maximum number of entities
                4000, //the radius between the first generated entity and the focus object, in this case the star
                1, //used to assign roman numerals to the generated entities if not given special names
                true //generator will give unique names like "Ordog" instead of "Example Star System III"
        );
		
		// hot asteroid belt
//		system.addAsteroidBelt(ganzir_star, 50, 2200, 100, 30, 40, Terrain.ASTEROID_BELT, null);
//		system.addRingBand(ganzir_star, "misc", "rings_asteroids0", 256f, 3, Color.white, 256f, 13750, 345f, Terrain.ASTEROID_BELT, null);
		
		system.addAsteroidBelt(ganzir_star, 50, 2200, 100, 30, 40, Terrain.ASTEROID_BELT, null);
		system.addRingBand(ganzir_star, "misc", "rings_asteroids0", 256f, 3, Color.white, 256f, 2200, 345f, null, null);
		
		//SectorEntityToken civilianStation = system.addOrbitalStation("new_maxios", star, 0, 3900, 160, "New Maxios", "independent");
		SectorEntityToken ganzirStation = system.addCustomEntity("new_ganzir", "Ganzir Station", "station_side07", "independent");
		civilianStation.setCustomDescriptionId("station_new_maxios");
		civilianStation.setInteractionImage("illustrations", "cargo_loading");
		civilianStation.setCircularOrbitWithSpin(ganzir_star, 0, 3900, 160, 2, 5);
		
		// create a market for ganzirStation - not connected to the rest of the economy to start with
		market = Global.getFactory().createMarket("ganzir_market", ganzirStation.getName(), 0);
		market.setSize(2);
		//market.setFactionId(Factions.PIRATES);
		
		market.setSurveyLevel(SurveyLevel.FULL);
		market.setPrimaryEntity(ganzirStation);
		
		market.setFactionId(ganzirStation.getFaction().getId());
		market.addCondition(Conditions.POPULATION_2);
		//market.addCondition(Conditions.ORBITAL_STATION);
		market.addCondition(Conditions.FREE_PORT);
		
		market.addIndustry(Industries.POPULATION);
		market.addIndustry(Industries.SPACEPORT);
		market.addIndustry(Industries.MINING);

		
		market.addSubmarket(Submarkets.SUBMARKET_OPEN);
		market.addSubmarket(Submarkets.SUBMARKET_BLACK);
		market.addSubmarket(Submarkets.SUBMARKET_STORAGE);
		market.getTariff().modifyFlat("default_tariff", market.getFaction().getTariffFraction());
		
		ganzirStation.setMarket(market);
		//ganzirStation.addScript(new GalatiaMarketScript(market));
		
		market.setEconGroup(market.getId());
		Global.getSector().getEconomy().addMarket(market, true);
		
			
			
		// Asteroid belt.
//		system.addRingBand(ganzir_star, "misc", "rings_asteroids0", 256f, 0, Color.white, 256f, 13750, 345f, Terrain.ASTEROID_BELT, null);
//		system.addAsteroidBelt(ganzir_star, 100, 13750, 200, 330, 360, Terrain.ASTEROID_BELT, "The Cyclopeans");
		system.addAsteroidBelt(ganzir_star, 100, 13750, 200, 330, 360, Terrain.ASTEROID_BELT, "The Cyclopeans");
		system.addRingBand(ganzir_star, "misc", "rings_asteroids0", 256f, 0, Color.white, 256f, 13750, 345f, null, null);
		
		//JumpPointAPI jumpPoint = Global.getFactory().createJumpPoint("ganzir_jump_point_alpha", "Penelope's Star Inner System Jump");
		//OrbitAPI orbit = Global.getFactory().createCircularOrbit(ganzir_star, 0, 800, 45);
		//jumpPoint.setOrbit(orbit);
		//jumpPoint.setRelatedPlanet(ganzir_star);
		//jumpPoint.setStandardWormholeToHyperspaceVisual();
		//system.addEntity(jumpPoint);

		system.autogenerateHyperspaceJumpPoints(true, true);
		
	}
}
