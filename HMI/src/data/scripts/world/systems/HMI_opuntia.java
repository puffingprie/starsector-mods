package data.scripts.world.systems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Terrain;
import com.fs.starfarer.api.impl.campaign.procgen.DefenderDataOverride;
import com.fs.starfarer.api.impl.campaign.procgen.NebulaEditor;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.DerelictThemeGenerator;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.BaseSalvageSpecial;
import com.fs.starfarer.api.impl.campaign.terrain.DebrisFieldTerrainPlugin.DebrisFieldParams;
import com.fs.starfarer.api.impl.campaign.terrain.DebrisFieldTerrainPlugin.DebrisFieldSource;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.util.Misc;
import data.campaign.fleets.MessFleetManager;
import data.campaign.fleets.HMIScavFleetRouteManager;
import indevo.utils.helper.Settings;

import java.awt.*;
import java.util.Random;

public class HMI_opuntia implements SectorGeneratorPlugin {
    private static final Random random = new Random();

    public static SectorEntityToken getSectorAccess() {
        return Global.getSector().getStarSystem("Opuntia").getEntityById("hmi_opuntia_star");
    }

    @Override
    public void generate(SectorAPI sector) {

        StarSystemAPI system = sector.createStarSystem("Opuntia");
        LocationAPI hyper = Global.getSector().getHyperspace();
        system.setBackgroundTextureFilename("graphics/backgrounds/hyperspace1.jpg");

        // create the star and generate the hyperspace anchor for this system
        PlanetAPI star = system.initStar("hmi_opuntia_star", // unique id for this star
                "star_red_dwarf", // id in planets.json
                300f, 		  // radius (in pixels at default zoom)
                120, // corona
                3f, // solar wind burn level
                0.7f, // flare probability
                2.0f); // CR loss multiplier, good values are in the range of 1-5
        system.setLightColor(new Color(255, 90, 0)); // light color in entire system, affects all entities
        system.getLocation().set(-31000, -20000);


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

         int count = StarSystemGenerator.random.nextInt(10) + 10;
        for (int i = 0; i < count; i++) {
            float amount = StarSystemGenerator.random.nextFloat() * 200f + 100f;
            float density = StarSystemGenerator.random.nextFloat() * 0.5f + 0.5f;
            DebrisFieldParams params = new DebrisFieldParams(
                              amount / density, // field radius - should not go above 1000 for performance reasons
                              density / 3f, // density, visual - affects number of debris pieces
                              10000000f, // duration in days
                              0f); // days the field will keep generating glowing pieces
            params.source = DebrisFieldSource.BATTLE;
            params.baseSalvageXP = 500; // base XP for scavenging in field
            SectorEntityToken debris = Misc.addDebrisField(system, params, StarSystemGenerator.random);
            debris.setDiscoverable(true);
            debris.setSensorProfile(amount);

            float r = StarSystemGenerator.random.nextFloat();
            if (r < 0.2) {
                debris.setCircularOrbit(star, StarSystemGenerator.random.nextFloat() * 360f,
                                        StarSystemGenerator.random.nextFloat() * 300f + 2000f, 35);
            } else if (r < 0.7) {
                debris.setCircularOrbit(star, StarSystemGenerator.random.nextFloat() * 360f,
                                        StarSystemGenerator.random.nextFloat() * 900f + 3200f, 180);
            } else {
                debris.setCircularOrbit(star, StarSystemGenerator.random.nextFloat() * 360f,
                                        StarSystemGenerator.random.nextFloat() * 1200f + 5500f, 705);
            }
        }
       

		PlanetAPI opun1 = system.addPlanet("opun_1", star, "Parkinsonia", "barren", 0, 100, 2000, 100);
                    Misc.initConditionMarket(opun1);
                    opun1.getMarket().setSurveyLevel(MarketAPI.SurveyLevel.FULL);
                    opun1.getMarket().addCondition(Conditions.NO_ATMOSPHERE);
                    opun1.getMarket().addCondition(Conditions.LOW_GRAVITY);
                    opun1.getMarket().addCondition(Conditions.EXTREME_TECTONIC_ACTIVITY);
                    opun1.getMarket().addCondition(Conditions.VERY_HOT);
					opun1.getMarket().addCondition(Conditions.METEOR_IMPACTS);
        opun1.setCustomDescriptionId("hmi_stripped");

        SectorEntityToken stationDerelict3 = DerelictThemeGenerator.addSalvageEntity(system, Entities.STATION_MINING, Factions.DERELICT);
        stationDerelict3.setId("mess_derelict1");
        stationDerelict3.setCircularOrbit(star, 100, 1700, 65f);
        Misc.setDefenderOverride(stationDerelict3, new DefenderDataOverride("mess", 1f, 20, 60));
        CargoAPI extraStationSalvage3 = Global.getFactory().createCargo(true);
        extraStationSalvage3.addCommodity("mess_nano", 69);
        BaseSalvageSpecial.addExtraSalvage(extraStationSalvage3, stationDerelict3.getMemoryWithoutUpdate(), -1);


        system.addRingBand(star, "misc", "rings_dust0", 256f, 1, Color.white, 256f, 2000, 100f, Terrain.RING, "Parkinsonia Remnant");
					
		PlanetAPI opun2 = system.addPlanet("opun_2", star, "Salvinia", "barren", 200, 100, 3200, 120);
					Misc.initConditionMarket(opun2);
                    opun2.getMarket().setSurveyLevel(MarketAPI.SurveyLevel.FULL);
                    opun2.getMarket().addCondition(Conditions.NO_ATMOSPHERE);
                    opun2.getMarket().addCondition(Conditions.LOW_GRAVITY);
                    opun2.getMarket().addCondition(Conditions.EXTREME_TECTONIC_ACTIVITY);
                    opun2.getMarket().addCondition(Conditions.VERY_COLD);		
					opun2.getMarket().addCondition(Conditions.METEOR_IMPACTS);		
        opun2.setCustomDescriptionId("hmi_stripped");

        SectorEntityToken stationDerelict1 = DerelictThemeGenerator.addSalvageEntity(system, Entities.STATION_RESEARCH, Factions.DERELICT);
        stationDerelict1.setId("mess_derelict1");
        stationDerelict1.setCircularOrbit(star, 355, 3000, 200f);
        Misc.setDefenderOverride(stationDerelict1, new DefenderDataOverride("mess", 1f, 185, 250));
        CargoAPI extraStationSalvage2 = Global.getFactory().createCargo(true);
        extraStationSalvage2.addCommodity("mess_nano", 185);
        BaseSalvageSpecial.addExtraSalvage(extraStationSalvage2, stationDerelict1.getMemoryWithoutUpdate(), -1);

        system.addRingBand(star, "misc", "rings_dust0", 256f, 1, Color.white, 256f, 3200, 100f, Terrain.RING, "Salvinia Remnant");

        PlanetAPI opun3 = system.addPlanet("opun_3", star, "Cylindropuntia", "barren", 180, 150, 5500, 200);
                    Misc.initConditionMarket(opun3);
                    opun3.getMarket().setSurveyLevel(MarketAPI.SurveyLevel.FULL);
                    opun3.getMarket().addCondition(Conditions.THIN_ATMOSPHERE);
                    opun3.getMarket().addCondition(Conditions.COLD);
                    opun3.getMarket().addCondition(Conditions.EXTREME_TECTONIC_ACTIVITY);
					opun3.getMarket().addCondition(Conditions.METEOR_IMPACTS);	
					opun3.getMarket().addCondition(Conditions.ORE_MODERATE);
					opun3.getMarket().addCondition(Conditions.RARE_ORE_SPARSE);
                    opun3.getMarket().addCondition("hmi_mess_swarm_cond");
        opun3.getSpec().setPlanetColor(new Color(157, 157, 239,255));
        opun3.getSpec().setGlowTexture(Global.getSettings().getSpriteName("hab_glows", "banded"));
        opun3.getSpec().setGlowColor(new Color(235,38,8,145));
        opun3.getSpec().setUseReverseLightForGlow(true);
        opun3.getSpec().setAtmosphereThickness(0.5f);
        opun3.getSpec().setCloudRotation( 15f );
        opun3.getSpec().setAtmosphereColor(new Color(216, 212, 241,245));


		system.addRingBand(star, "misc", "rings_dust0", 256f, 0, Color.white, 256f, 5500, 200f, Terrain.RING, "Cylindropuntia Remnant");
		system.addAsteroidBelt(star, 240, 5500, 128, 200, 300, Terrain.ASTEROID_BELT, null);
        system.addRingBand(opun3, "misc", "rings_dust0", 256f, 1, Color.white, 256f, 220, 200f, Terrain.RING, "Cylindropuntia Remnant");
        opun3.setCustomDescriptionId("hmi_infested");
        opun3.addTag("hmi_mess_blocktag");
     
        SectorEntityToken opun_station1 = system.addCustomEntity(
                "mess_station1",
                "The Mess",
                "station_mess",
                "mess");
        opun_station1.setCircularOrbitPointingDown(opun3, 270, 225, 175);
        opun_station1.setCustomDescriptionId("hmi_mess");
		opun_station1.setInteractionImage("illustrations", "abandoned_station3");
        opun_station1.addTag("hmi_messstationtag");

        int maxFleets = 6 + this.random.nextInt(2);
        MessFleetManager MessFleets = new MessFleetManager(
                opun_station1, 1.0f, 3, maxFleets, 15.0f, 5, 25);
        system.addScript(MessFleets);

        HMIScavFleetRouteManager fleets = new HMIScavFleetRouteManager(system);
        system.addScript(fleets);

        SectorEntityToken stationDerelict2 = DerelictThemeGenerator.addSalvageEntity(system, Entities.ORBITAL_HABITAT, Factions.DERELICT);
        stationDerelict2.setId("mess_derelict2");
        stationDerelict2.setCircularOrbit(star, 120, 5500, 200f);
        Misc.setDefenderOverride(stationDerelict2, new DefenderDataOverride("mess", 1f, 50, 100));
        CargoAPI extraStationSalvage = Global.getFactory().createCargo(true);
        extraStationSalvage.addCommodity("mess_nano", 200);
        BaseSalvageSpecial.addExtraSalvage(extraStationSalvage, stationDerelict2.getMemoryWithoutUpdate(), -1);


		PlanetAPI opun4 = system.addPlanet("opun_4", star, "Rubus", "rocky_metallic", 270, 175, 7500, 300);
                    Misc.initConditionMarket(opun4);
                    opun4.getMarket().setSurveyLevel(MarketAPI.SurveyLevel.FULL);
		            opun4.getMarket().addCondition(Conditions.NO_ATMOSPHERE);
                    opun4.getMarket().addCondition(Conditions.COLD);	
					opun4.getMarket().addCondition(Conditions.ORE_MODERATE);
					opun4.getMarket().addCondition(Conditions.RARE_ORE_SPARSE);		
		
		SectorEntityToken opun_station2 = system.addCustomEntity("mess_station2", 
																		"Cactorum",
																		"station_mining00",
																		"HMI");
			
		opun_station2.setCircularOrbitPointingDown(opun4, 180, 600, 120);
		opun_station2.setCustomDescriptionId("hmi_station_cactorum");
        opun_station2.setInteractionImage("illustrations", "industrial_megafacility");

        //JUMP POINT
        JumpPointAPI jumpPoint1 = Global.getFactory().createJumpPoint("opun_jumpPointA", "Cactorum Jump-Point");
        OrbitAPI orbit = Global.getFactory().createCircularOrbit(star, 250, 7600, 300);
        jumpPoint1.setOrbit(orbit);
        jumpPoint1.setRelatedPlanet(opun4);
        jumpPoint1.setStandardWormholeToHyperspaceVisual();
        system.addEntity(jumpPoint1);
            
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