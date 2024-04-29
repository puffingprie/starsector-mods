package data.scripts.world.systems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Terrain;
import com.fs.starfarer.api.impl.campaign.procgen.NebulaEditor;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.SalvageSpecialAssigner;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial;
import com.fs.starfarer.api.impl.campaign.terrain.AsteroidFieldTerrainPlugin.AsteroidFieldParams;
import com.fs.starfarer.api.impl.campaign.terrain.DebrisFieldTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;

public class HMI_mercy implements SectorGeneratorPlugin {

    public static SectorEntityToken getSectorAccess() {
        return Global.getSector().getStarSystem("Mercy").getEntityById("hmi_mercy_star");
    }

    @Override
    public void generate(SectorAPI sector) {

        StarSystemAPI system = sector.createStarSystem("Mercy");
        LocationAPI hyper = Global.getSector().getHyperspace();
        system.setBackgroundTextureFilename("graphics/backgrounds/mercy.jpg");

        // create the star and generate the hyperspace anchor for this system
        PlanetAPI star = system.initStar("hmi_mercy_star", // unique id for this star
                                        "star_yellow", // id in planets.json
										775f,
										500, // extent of corona outside star
										10f, // solar wind burn level
										1f, // flare probability
										1.5f);		// radius (in pixels at default zoom)
	system.setLightColor(new Color(255, 220, 200)); // light color in entire system, affects all entities       
                
	system.getLocation().set(-28000, -23000);
        
        /*
         * addPlanet() parameters:
         * 1. What the planet orbits (orbit is always circular)
         * 2. Name
         * 3. Planet type id in planets.json
         * 4. Starting angle in orbit, i.e. 0 = to the right of the star
         * 5. Planet radius, pixels at default zoom
         * 6. Orbit radius, pixels at default zoom
         * 7. Days it takes to complete an orbit. 1 day = 10 seconds.
         */
        /*
         * addAsteroidBelt() parameters:
         * 1. What the belt orbits
         * 2. Number of asteroids
         * 3. Orbit radius
         * 4. Belt width
         * 6/7. Range of days to complete one orbit. Value picked randomly for each asteroid. 
         */
        /*
         * addRingBand() parameters:
         * 1. What it orbits
         * 2. Category under "graphics" in settings.json
         * 3. Key in category
         * 4. Width of band within the texture
         * 5. Index of band
         * 6. Color to apply to band
         * 7. Width of band (in the game)
         * 8. Orbit radius (of the middle of the band)
         * 9. Orbital period, in days
         */        

		PlanetAPI merc1 = system.addPlanet("merc_1", star, "Cassiel", "barren", 0, 100, 2000, 85);
		Misc.initConditionMarket(merc1);
		merc1.getMarket().setSurveyLevel(MarketAPI.SurveyLevel.FULL);
		merc1.getMarket().addCondition(Conditions.VERY_HOT);
		merc1.getMarket().addCondition(Conditions.TECTONIC_ACTIVITY);
		merc1.getMarket().addCondition(Conditions.IRRADIATED);
		merc1.getMarket().addCondition(Conditions.NO_ATMOSPHERE);
		merc1.getMarket().addCondition(Conditions.ORE_SPARSE);

		//2000
        PlanetAPI merc2 = system.addPlanet("hmi_nuriel", star, "Nuriel", "toxic", 55, 150, 5000, 100);
		merc2.setCustomDescriptionId("hmi_nuriel");
		merc2.setInteractionImage("illustrations", "abandoned_station3");

        //3000
        PlanetAPI merc3 = system.addPlanet("hmi_samiel", star, "Samiel", "haunted", 245, 200, 7500, 200);
        merc3.setCustomDescriptionId("hmi_samiel");
		merc3.setInteractionImage("illustrations", "hmi_samiel");
        system.addRingBand(merc3, "misc", "rings_dust0", 256f, 0, new Color(255,210,180,0), 64f, 1000, 175f, Terrain.RING, "...join...");
        system.addRingBand(merc3, "misc", "rings_dust0", 256f, 0, new Color(255,210,180,0), 64f, 2000, 175f, Terrain.RING, "...make...us...");
        system.addRingBand(merc3, "misc", "rings_dust0", 256f, 0, new Color(255,210,180,0), 64f, 4000, 175f, Terrain.RING, "...help...");

		//4000				
        PlanetAPI merc4 = system.addPlanet("path_soul", star, "Soul", "jungle", 245, 200, 9000, 300);
        merc4.setCustomDescriptionId("path_soul");
		merc4.setInteractionImage("illustrations", "mazalot");

		PlanetAPI merc4B = system.addPlanet("path_soulB", merc4, "Uriel", "barren-bombarded", 235, 70, 750, 60);
		Misc.initConditionMarket(merc4B);
		merc1.getMarket().setSurveyLevel(MarketAPI.SurveyLevel.FULL);
		merc1.getMarket().addCondition(Conditions.NO_ATMOSPHERE);
     
        //JUMP POINT
        JumpPointAPI jumpPoint1 = Global.getFactory().createJumpPoint("merc_jumpPointA", "Mercy Jump-Point");
        OrbitAPI orbit = Global.getFactory().createCircularOrbit(merc4, 270+60, 750, 250);
        jumpPoint1.setOrbit(orbit);
        jumpPoint1.setRelatedPlanet(merc4);
        jumpPoint1.setStandardWormholeToHyperspaceVisual();
        system.addEntity(jumpPoint1);

        
        //RELAY
        SectorEntityToken relay = system.addCustomEntity("merc_relay", // unique id
                null, // name - if null, defaultName from custom_entities.json will be used
                "comm_relay_makeshift", // type of object, defined in custom_entities.json
                "HMI"); // faction
        relay.setCircularOrbit(star, 270-60, 3000, 250);

		//6000
		system.addAsteroidBelt(star, 120, 11000, 600, 300, 350, Terrain.ASTEROID_BELT, "The Choir");
        
        //7000
		SectorEntityToken askonia_gate = system.addCustomEntity("mercy_gate", // unique id
				"Mercy Gate", // name - if null, defaultName from custom_entities.json will be used
				"inactive_gate", // type of object, defined in custom_entities.json
				null); // faction
		askonia_gate.setCircularOrbit(star, 20, 7000, 230);

		PlanetAPI merc5 = system.addPlanet("merc5", star, "Metatron", "gas_giant", 200, 300, 18000, 400);
		Misc.initConditionMarket(merc5);
		merc5.getMarket().setSurveyLevel(MarketAPI.SurveyLevel.FULL);
		merc5.getMarket().addCondition(Conditions.VERY_COLD);
		merc5.getMarket().addCondition(Conditions.HIGH_GRAVITY);
		merc5.getMarket().addCondition(Conditions.VOLATILES_DIFFUSE);
		merc5.getMarket().addCondition(Conditions.EXTREME_WEATHER);
		merc5.getMarket().addCondition(Conditions.DENSE_ATMOSPHERE);

		system.addRingBand(merc5, "misc", "rings_dust0", 256f, 0, Color.lightGray, 256f, 1150, 80f);

		SectorEntityToken nevermore_station1 = system.addCustomEntity("mercy_corpse", "Corpse", "station_side00", Factions.LUDDIC_PATH);
		nevermore_station1.setCircularOrbitPointingDown(merc5, 0, 1150, 30);
		nevermore_station1.setCustomDescriptionId("mercy_corpse");

		PlanetAPI merc5B = system.addPlanet("merc5b", merc5, "Michael", "lava", 20, 70, 1000, 50);

		Misc.initConditionMarket(merc5B);
		merc5B.getMarket().setSurveyLevel(MarketAPI.SurveyLevel.FULL);
		merc5B.getMarket().addCondition(Conditions.VERY_HOT);
		merc5B.getMarket().addCondition(Conditions.HIGH_GRAVITY);
		merc5B.getMarket().addCondition(Conditions.ORE_MODERATE);
		merc5B.getMarket().addCondition(Conditions.THIN_ATMOSPHERE);

		PlanetAPI merc5c = system.addPlanet("merc5c", merc5, "Simon", "cryovolcanic", 175, 75, 1300, 60);

		Misc.initConditionMarket(merc5c);
		merc5c.getMarket().setSurveyLevel(MarketAPI.SurveyLevel.FULL);
		merc5c.getMarket().addCondition(Conditions.VERY_COLD);
		merc5c.getMarket().addCondition(Conditions.LOW_GRAVITY);
		merc5c.getMarket().addCondition(Conditions.ORE_SPARSE);
		merc5c.getMarket().addCondition(Conditions.VOLATILES_TRACE);
		merc5c.getMarket().addCondition(Conditions.EXTREME_TECTONIC_ACTIVITY);

		PlanetAPI merc5d = system.addPlanet("merc5d", merc5, "Daniel", "barren", 235, 100, 1600, 70);

		Misc.initConditionMarket(merc5d);
		merc5d.getMarket().setSurveyLevel(MarketAPI.SurveyLevel.FULL);
		merc5d.getMarket().addCondition(Conditions.VERY_COLD);
		merc5d.getMarket().addCondition(Conditions.ORE_SPARSE);
		merc5d.getMarket().addCondition(Conditions.RARE_ORE_SPARSE);
		merc5d.getMarket().addCondition(Conditions.NO_ATMOSPHERE);

			SectorEntityToken merc5L4 = system.addTerrain(Terrain.ASTEROID_FIELD,
					new AsteroidFieldParams(
						500f, // min radius
						700f, // max radius
						20, // min asteroid count
						30, // max asteroid count
						4f, // min asteroid radius 
						16f, // max asteroid radius
						"Metatron L4 Asteroids")); // null for default name
			
			SectorEntityToken merc5L5 = system.addTerrain(Terrain.ASTEROID_FIELD,
					new AsteroidFieldParams(
						500f, // min radius
						700f, // max radius
						20, // min asteroid count
						30, // max asteroid count
						4f, // min asteroid radius 
						16f, // max asteroid radius
						"Metatron L5 Asteroids")); // null for default name
			
			merc5L4.setCircularOrbit(star, 200f -60f, 18000, 400);
			merc5L5.setCircularOrbit(star, 200f +60f, 18000, 400);

		//FRINGE JUMP POINT
		JumpPointAPI jumpPoint3 = Global.getFactory().createJumpPoint("merc_jumpPointA", "Fringe Jump Point");
		OrbitAPI orbit3 = Global.getFactory().createCircularOrbit(star, 270+60, 20000, 480);
		jumpPoint3.setOrbit(orbit3);
		jumpPoint3.setStandardWormholeToNothingVisual();
		jumpPoint3.setStandardWormholeToHyperspaceVisual();
		system.addEntity(jumpPoint3);




			SectorEntityToken merc6 = system.addCustomEntity(
			"hmi_mercy_station",
			"Abandoned Research Station",
			"station_side06",
			"neutral");

        merc6.setCircularOrbitPointingDown(star, 20, 31000, 900);
        merc6.setCustomDescriptionId("hmi_mercy_station");
        merc6.setInteractionImage("illustrations", "abandoned_station2");
		merc6.addTag("hmi_mercy_stationtag");

		DebrisFieldTerrainPlugin.DebrisFieldParams params2 = new DebrisFieldTerrainPlugin.DebrisFieldParams(
				250f, // field radius - should not go above 1000 for performance reasons
				1.2f, // density, visual - affects number of debris pieces
				10000000f, // duration in days
				0f); // days the field will keep generating glowing pieces
		params2.source = DebrisFieldTerrainPlugin.DebrisFieldSource.MIXED;
		params2.baseSalvageXP = 1000; // base XP for scavenging in field
		SectorEntityToken debrisstation = Misc.addDebrisField(system, params2, StarSystemGenerator.random);
		debrisstation.setSensorProfile(650f);
		debrisstation.setDiscoverable(true);
		debrisstation.setCircularOrbit(star, 20f, 31000, 900f);
		debrisstation.setId("mercy_debrisBelt");

		addDerelict(system, merc6, "aurora_Balanced", ShipRecoverySpecial.ShipCondition.BATTERED, 270f, (Math.random()<0.5));
		addDerelict(system, merc6, "fury_Attack", ShipRecoverySpecial.ShipCondition.BATTERED, 200f, (Math.random()<0.5));
		addDerelict(system, merc6, "scarab_Experimental", ShipRecoverySpecial.ShipCondition.BATTERED, 200f, (Math.random()<0.5));
		addDerelict(system, merc6, "fury_Attack", ShipRecoverySpecial.ShipCondition.BATTERED, 200f, (Math.random()<0.5));
		addDerelict(system, merc6, "shrike_Attack", ShipRecoverySpecial.ShipCondition.BATTERED, 200f, (Math.random()<0.5));
		addDerelict(system, merc6, "wolf_Assault", ShipRecoverySpecial.ShipCondition.BATTERED, 200f, (Math.random()<0.5));
		addDerelict(system, merc6, "wolf_Assault", ShipRecoverySpecial.ShipCondition.BATTERED, 200f, (Math.random()<0.5));
		addDerelict(system, merc6, "shrike_Attack", ShipRecoverySpecial.ShipCondition.BATTERED, 200f, (Math.random()<0.5));
		addDerelict(system, merc6, "wolf_Assault", ShipRecoverySpecial.ShipCondition.BATTERED, 215f, (Math.random()<0.5));
		addDerelict(system, merc6, "tempest_Attack", ShipRecoverySpecial.ShipCondition.BATTERED, 215f, (Math.random()<0.5));

        system.autogenerateHyperspaceJumpPoints(true, false);

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

	protected void addDerelict(StarSystemAPI system,
							   SectorEntityToken focus,
							   String variantId,
							   ShipRecoverySpecial.ShipCondition condition,
							   float orbitRadius,
							   boolean recoverable) {
		DerelictShipEntityPlugin.DerelictShipData params = new DerelictShipEntityPlugin.DerelictShipData(new ShipRecoverySpecial.PerShipData(variantId, condition), true);
		SectorEntityToken ship = BaseThemeGenerator.addSalvageEntity(system, Entities.WRECK, Factions.NEUTRAL, params);
		ship.setDiscoverable(true);

		float orbitDays = orbitRadius / (10f + (float) Math.random() * 5f);
		ship.setCircularOrbit(focus, (float) Math.random() * 360f, orbitRadius, orbitDays);

		if (recoverable) {
			SalvageSpecialAssigner.ShipRecoverySpecialCreator creator = new SalvageSpecialAssigner.ShipRecoverySpecialCreator(null, 0, 0, false, null, null);
			Misc.setSalvageSpecial(ship, creator.createSpecial(ship, null));
		}
	}
}