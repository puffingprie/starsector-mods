package data.scripts.world.systems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.procgen.NebulaEditor;
import com.fs.starfarer.api.impl.campaign.terrain.AsteroidFieldTerrainPlugin.AsteroidFieldParams;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.util.Misc;
import data.campaign.econ.luddenhance_industries;
import data.scripts.world.AddMarketplace;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

import static com.fs.starfarer.api.impl.campaign.ids.Factions.LUDDIC_CHURCH;


public class luddenhance_alexandretta implements SectorGeneratorPlugin {
    public static SectorEntityToken getSectorAccess() {
        return Global.getSector().getStarSystem("Alexandretta").getEntityById("alexandretta_star");
    }
	@Override
	public void generate(SectorAPI sector) {
		StarSystemAPI system = sector.createStarSystem("Alexandretta");
		LocationAPI hyper = Global.getSector().getHyperspace();
		system.setBackgroundTextureFilename("graphics/backgrounds/background6.jpg");
		
		// create the star and generate the hyperspace anchor for this system
		PlanetAPI star = system.initStar("alexandretta_star",
										 "star_white", // id in planets.json
										700f, // radius (in pixels at default zoom)
										800, // corona radius, from star edge
										4f, // solar wind burn level
										0.5f, // flare probability
										1.4f); // cr loss mult
		system.setLightColor(new Color(255, 255, 255)); // light color in entire system, affects all entities
		system.getLocation().set(-8920, -12100);
		
		
        system.addRingBand(star, "misc", "rings_dust0", 256f, 0, new Color(255,210,180,0), 512f, 1500, 175f, Terrain.RING, "The Hydra");
		
        PlanetAPI alexandretta1 = system.addPlanet("equus", star, "Equus", "lava", 90, 55, 2100, 100);
		    alexandretta1.getMarket().addCondition(Conditions.EXTREME_TECTONIC_ACTIVITY);
			alexandretta1.getMarket().addCondition(Conditions.TOXIC_ATMOSPHERE);
            alexandretta1.getMarket().addCondition(Conditions.VERY_HOT);
            alexandretta1.getMarket().addCondition(Conditions.ORE_MODERATE);

		PlanetAPI alexandretta1a = system.addPlanet("campus", star, "Campus", "barren", 233, 45, 1200, 300);
		alexandretta1a.getMarket().addCondition(Conditions.TECTONIC_ACTIVITY);
		alexandretta1a.getMarket().addCondition(Conditions.HOT);
		alexandretta1a.getMarket().addCondition(Conditions.NO_ATMOSPHERE);
		alexandretta1a.getMarket().addCondition(Conditions.RARE_ORE_SPARSE);

		PlanetAPI alexandretta1c = system.addPlanet("artaxerxes", star, "Artaxerxes", "irradiated", 359, 85, 2500, 365);
		alexandretta1c.getSpec().setPlanetColor(new Color(220,245,255,255));
		alexandretta1c.getSpec().setAtmosphereColor(new Color(150,120,100,250));
		alexandretta1c.getSpec().setCloudColor(new Color(150,120,120,150));
		alexandretta1c.setCustomDescriptionId("planet_artaxerxes");

		alexandretta1c.getMarket().addCondition(Conditions.RUINS_EXTENSIVE);
		alexandretta1c.getMarket().getFirstCondition(Conditions.RUINS_EXTENSIVE).setSurveyed(true);
		alexandretta1c.getMarket().addCondition(Conditions.TECTONIC_ACTIVITY);
		alexandretta1c.getMarket().addCondition(Conditions.TOXIC_ATMOSPHERE);
		alexandretta1c.getMarket().addCondition(Conditions.EXTREME_WEATHER);
		alexandretta1c.getMarket().addCondition(Conditions.RARE_ORE_SPARSE);
		alexandretta1c.getMarket().addCondition(Conditions.ORE_SPARSE);
		alexandretta1c.getMarket().addCondition(Conditions.IRRADIATED);




		system.addAsteroidBelt(star, 100, 4000, 256, 80, 160, Terrain.ASTEROID_BELT, "The Birds");
		system.addRingBand(star, "misc", "rings_dust0", 256f, 0, new Color(255,210,180,0), 512f, 4000, 175f, Terrain.RING, "The Birds");
		
		PlanetAPI alexandretta2 = system.addPlanet("alexandretta2", star, "Geryon", "gas_giant", 240, 300, 9650, 600);
		alexandretta2.getSpec().setAtmosphereColor(new Color(255,245,200,220));
		alexandretta2.getSpec().setPlanetColor(new Color(245,250,255,255));
		alexandretta2.applySpecChanges();
		alexandretta2.getSpec().setGlowTexture(Global.getSettings().getSpriteName("hab_glows", "aurorae"));
		alexandretta2.getSpec().setGlowColor(new Color(245,50,20,100));
		alexandretta2.getSpec().setUseReverseLightForGlow(true);
		alexandretta2.getSpec().setAtmosphereThickness(0.5f);
		alexandretta2.getSpec().setCloudRotation( 10f );
		alexandretta2.getSpec().setPitch(20);
		alexandretta2.getSpec().setAtmosphereThicknessMin(80);
		alexandretta2.getSpec().setAtmosphereThickness(0.30f);
		alexandretta2.getSpec().setAtmosphereColor(new Color(255,150,50,205));
		
		Misc.initConditionMarket(alexandretta2);
        alexandretta2.getMarket().addCondition(Conditions.HIGH_GRAVITY);
        alexandretta2.getMarket().addCondition(Conditions.VOLATILES_ABUNDANT);
		alexandretta2.getMarket().addCondition(Conditions.EXTREME_WEATHER);
        alexandretta2.getMarket().getFirstCondition(Conditions.VOLATILES_ABUNDANT).setSurveyed(true);
		

		SectorEntityToken station1 = system.addCustomEntity("hera", "Hera Station", "station_side02", "luddic_church");
		station1.setCircularOrbitPointingDown(system.getEntityById("alexandretta2"), 45, 500, 50);
		station1.setInteractionImage("illustrations", "orbital");
		station1.setCustomDescriptionId("hegemony_hera");

		AddMarketplace.addMarketplace (Factions.HEGEMONY, station1,
				null,
				"Hera Station", 3,
				new ArrayList<>
						(Arrays.asList(
								Conditions.POPULATION_3,
                      		Conditions.URBANIZED_POLITY)),
              new ArrayList<>
                        (Arrays.asList( // list of industries
                                Industries.BATTLESTATION,
                                Industries.SPACEPORT,
								Industries.MILITARYBASE,
								Industries.SPACEPORT,
                                Industries.POPULATION)),
                new ArrayList<>(
                        Arrays.asList( // which submarkets to generate
                                Submarkets.SUBMARKET_BLACK,
                                Submarkets.GENERIC_MILITARY,
                                Submarkets.SUBMARKET_OPEN,
								Submarkets.SUBMARKET_STORAGE)),
				0.3f
		);
					
		
			PlanetAPI alexandretta2a = system.addPlanet("lyon", alexandretta2, "Lyon", "lava_minor", 0, 60, 800, 18);
			alexandretta2a.getMarket().addCondition(Conditions.EXTREME_TECTONIC_ACTIVITY);
			alexandretta2a.getMarket().addCondition(Conditions.TOXIC_ATMOSPHERE);
			alexandretta2a.getMarket().addCondition(Conditions.VERY_HOT);
			alexandretta2a.getMarket().addCondition(Conditions.ORE_SPARSE);
			alexandretta2a.getMarket().addCondition(Conditions.RARE_ORE_SPARSE);

			PlanetAPI alexandretta2b = system.addPlanet("taurus", alexandretta2, "Taurus", "cryovolcanic", 30, 75, 925, 60);
			alexandretta2b.getMarket().addCondition(Conditions.COLD);
			alexandretta1.getMarket().addCondition(Conditions.THIN_ATMOSPHERE);
			alexandretta2b.getMarket().addCondition(Conditions.TECTONIC_ACTIVITY);
			alexandretta2b.getMarket().addCondition(Conditions.ORE_SPARSE);
			alexandretta2b.getMarket().addCondition(Conditions.VOLATILES_TRACE);

			PlanetAPI alexandretta2c = system.addPlanet("hippolyta", alexandretta2, "Hippolyta", "rocky_unstable", 235, 45, 1600, 100);
			alexandretta2b.getMarket().addCondition(Conditions.COLD);
			alexandretta2b.getMarket().addCondition(Conditions.TECTONIC_ACTIVITY);
			alexandretta2b.getMarket().addCondition(Conditions.ORE_SPARSE);
			alexandretta1.getMarket().addCondition(Conditions.NO_ATMOSPHERE);

			system.addAsteroidBelt(alexandretta2, 60, 1300, 100, 15, 25, Terrain.ASTEROID_BELT, "The Apples");
			
			SectorEntityToken alexandretta2L4 = system.addTerrain(Terrain.ASTEROID_FIELD,
					new AsteroidFieldParams(
						300f, // min radius
						500f, // max radius
						10, // min asteroid count
						15, // max asteroid count
						4f, // min asteroid radius 
						12f, // max asteroid radius
						"Phoenix Asteroids")); // null for default name
			
			SectorEntityToken alexandretta2L5 = system.addTerrain(Terrain.ASTEROID_FIELD,
					new AsteroidFieldParams(
						300f, // min radius
						500f, // max radius
						10, // min asteroid count
						15, // max asteroid count
						4f, // min asteroid radius 
						12f, // max asteroid radius
						"Roc Asteroids")); // null for default name
			
			alexandretta2L4.setCircularOrbit(star, 240 + 60, 4200, 600);
			alexandretta2L5.setCircularOrbit(star, 240 - 60, 4200, 600);

			
		PlanetAPI alexandretta3 = system.addPlanet("cerbrus", star, "Cerberus", "tundra", 100, 100, 5000, 900);
		alexandretta3.setCustomDescriptionId("luddicchurch_cerebrus");
		if (!Global.getSettings().getModManager().isModEnabled("tahlan")) {
			MarketAPI alexandretta3Market = AddMarketplace.addMarketplace(LUDDIC_CHURCH, alexandretta3,
					null,
					"Cerberus", 5,
					new ArrayList<>
							(Arrays.asList(Conditions.POPULATION_5,
									Conditions.RUINS_EXTENSIVE,
									Conditions.VERY_COLD,
									Conditions.ORE_MODERATE,
									Conditions.RARE_ORE_SPARSE)),
					new ArrayList<>
							(Arrays.asList( // list of industries
									Industries.SPACEPORT,
									Industries.MILITARYBASE,
									Industries.HEAVYBATTERIES,
									luddenhance_industries.REFIT,
									Industries.POPULATION)),
					new ArrayList<>(
							Arrays.asList( // which submarkets to generate
									Submarkets.SUBMARKET_BLACK,
									Submarkets.GENERIC_MILITARY,
									Submarkets.SUBMARKET_OPEN,
									Submarkets.SUBMARKET_STORAGE)),
					0.3f
			);
		}
		if (Global.getSettings().getModManager().isModEnabled("tahlan")) {
			MarketAPI alexandretta3Market = AddMarketplace.addMarketplace (LUDDIC_CHURCH, alexandretta3,
					null,
					"Cerberus", 6,
					new ArrayList<>
							(Arrays.asList(Conditions.POPULATION_6,
									Conditions.RUINS_EXTENSIVE,
									Conditions.VERY_COLD,
									Conditions.ORE_MODERATE,
									Conditions.RARE_ORE_SPARSE)),
					new ArrayList<>
							(Arrays.asList( // list of industries
									Industries.SPACEPORT,
									Industries.MILITARYBASE,
									Industries.HEAVYBATTERIES,
									Industries.BATTLESTATION,
									luddenhance_industries.REFIT,
									Industries.POPULATION)),
					new ArrayList<>(
							Arrays.asList( // which submarkets to generate
									Submarkets.SUBMARKET_BLACK,
									Submarkets.GENERIC_MILITARY,
									Submarkets.SUBMARKET_OPEN,
									Submarkets.SUBMARKET_STORAGE)),
					0.3f
			);
		}

		SectorEntityToken alexandratta_buoy = system.addCustomEntity(
				"alexandratta_buoy",
				"Alexandretta Nav Buoy",
				"nav_buoy", LUDDIC_CHURCH);
		alexandratta_buoy.setCircularOrbitPointingDown(system.getEntityById("alexandretta_star"), 180, 2600, 600);

		SectorEntityToken alexandratta_relay = system.addCustomEntity("alexandratta_relay", // unique id
				"Alexandretta Relay", // name - if null, defaultName from custom_entities.json will be used
				"comm_relay", // type of object, defined in custom_entities.json
				LUDDIC_CHURCH); // faction
		alexandratta_relay.setCircularOrbitPointingDown(system.getEntityById("alexandretta_star"), 300, 2800, 600);

		SectorEntityToken gate = system.addCustomEntity("alexandratta_gate", // unique id
				"Alexandretta Gate", // name - if null, defaultName from custom_entities.json will be used
				"inactive_gate", // type of object, defined in custom_entities.json
				null); // faction

		gate.setCircularOrbit(system.getEntityById("alexandretta_star"), 60, 6000, 365);

		JumpPointAPI jumpPoint = Global.getFactory().createJumpPoint("alexandretta_jump_point1", "Alexandretta Jump-point");
		OrbitAPI orbit = Global.getFactory().createCircularOrbit(gate, 0, 300, 20);
		jumpPoint.setOrbit(orbit);
		jumpPoint.setRelatedPlanet(alexandretta3);
		jumpPoint.setStandardWormholeToHyperspaceVisual();
		system.addEntity(jumpPoint);

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
