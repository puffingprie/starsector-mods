package data.scripts.world.tolp;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.JumpPointAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
//import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
//import com.fs.starfarer.api.impl.campaign.ids.Industries;
//import com.fs.starfarer.api.impl.campaign.ids.Items;
//import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.ids.Terrain;
import org.lwjgl.util.vector.Vector2f;
//import com.fs.starfarer.api.util.Misc;
//import data.scripts.world.AddMarketplace;
//
//import java.util.ArrayList;
//import java.util.Arrays;

public class Tolp {
    	public void generate(SectorAPI sector) {
		
		StarSystemAPI system = sector.createStarSystem("Tolp");
		LocationAPI hyper = Global.getSector().getHyperspace();
		
		system.setBackgroundTextureFilename("graphics/backgrounds/tolpbg.jpg");
		
		// create the star and generate the hyperspace anchor for this system
		PlanetAPI tolp_star = system.initStar("tolp_star", // unique id for this star 
                                                    "star_red_giant",  // id in planets.json
                                                    400f,
                                                    800, // extent of corona outside star
                                                    10f, // solar wind burn level
                                                    1f, // flare probability
                                                    4f); // CR loss multiplier, good values are in the range of 1-5
		
		system.setLightColor(new Color(255, 240, 220)); // light color in entire system, affects all entities
                
                system.getLocation().set(new Vector2f(13000,-15000));
                
                
                ///////////////////////////////////////////////////////////////////////////////////////////////////////////
                ///////////////////////////////////////////////////////////////////////////////////////////////////////////
                ///////////////////////////////////////////////////////////////////////////////////////////////////////////
                
                
                //Tolp I, a blob of lava
                PlanetAPI tolp1 = system.addPlanet("tolpI", tolp_star, "Tolp I", "rocky_metallic", 90, 100, 1600, 60);
                tolp1.setCustomDescriptionId("planet_tolpI");
                                        //tolp1.getMarket().addCondition(Conditions.RARE_ORE_ABUNDANT);
                                        //tolp1.getMarket().addCondition(Conditions.ORE_ABUNDANT);                                        
                                        //tolp1.getMarket().addCondition(Conditions.VERY_HOT);                                        
                                        //tolp1.getMarket().addCondition(Conditions.NO_ATMOSPHERE);                                        
                                        
                    // Add fixed conditions to Tolp I.
                    //Misc.initConditionMarket(tolp1);
                    //tolp1.getMarket().addCondition(Conditions.NO_ATMOSPHERE);
                    //tolp1.getMarket().addCondition(Conditions.LOW_GRAVITY);
                    //tolp1.getMarket().addCondition(Conditions.VERY_HOT);
                    
                // Inner magnetic field.
                //SectorEntityToken tolp_field1 = system.addTerrain(Terrain.MAGNETIC_FIELD,
		//	new MagneticFieldTerrainPlugin.MagneticFieldParams(400f, // terrain effect band width 
		//	1050, // terrain effect middle radius
		//	tolp_star, // entity that it's around
		//	850f, // visual band start
		//	1250f, // visual band end
		//	new Color(50, 30, 100, 45), // base color
		//	0.3f, // probability to spawn aurora sequence, checked once/day when no aurora in progress
		//	new Color(50, 20, 110, 130),
		//	new Color(150, 30, 120, 150), 
		//	new Color(200, 50, 130, 190),
		//	new Color(250, 70, 150, 240),
		//	new Color(200, 80, 130, 255),
		//	new Color(75, 0, 160), 
		//	new Color(127, 0, 255)
		//	));
                //tolp_field1.setCircularOrbit(tolp_star, 0, 0, 120);

                ///////////////////////////////////////////////////////////////////////////////////////////////////////////
                    
                    
                // Tolp II, an irradiated glowball ---------------
                PlanetAPI tolp2 = system.addPlanet("tolpII", tolp_star, "Tolp II", "irradiated", 110, 120, 2400, 110);
		tolp2.getSpec().setPitch(-15f);
		tolp2.getSpec().setTilt(15f);
		tolp2.applySpecChanges();
                tolp2.setCustomDescriptionId("planet_tolpII");
                                // Add fixed conditions to Tolp II.
//                                    Misc.initConditionMarket(tolp2);
                                        tolp2.getMarket().addCondition(Conditions.RUINS_VAST);
                                        tolp2.getMarket().addCondition(Conditions.IRRADIATED);
                                        tolp2.getMarket().addCondition(Conditions.VERY_HOT);
                                        tolp2.getMarket().addCondition(Conditions.VOLATILES_PLENTIFUL);
                                        tolp2.getMarket().addCondition(Conditions.RARE_ORE_ABUNDANT);
                                        tolp2.getMarket().addCondition(Conditions.POLLUTION);
                                        tolp2.getMarket().addCondition(Conditions.EXTREME_WEATHER);

                ///////////////////////////////////////////////////////////////////////////////////////////////////////////
                
                // Tolp III, a mineral rich mining outpost ---------------
                PlanetAPI tolp3 = system.addPlanet("tolpIII", tolp_star, "Tolp III", "desert", 140, 200, 3100, 200);
		tolp3.getSpec().setPitch(5f);
		tolp3.getSpec().setTilt(1f);
		tolp3.applySpecChanges();
                tolp3.setCustomDescriptionId("planet_tolpIII");

//                // adds the marketplace to Tolp III ---------------
//                MarketAPI tolp3Market = AddMarketplace.addMarketplace("pn_colony", tolp3, null,
//                        "Tolp III", // name of the market
//                        5, // size of the market (from the JSON)
//                        new ArrayList<>(
//                                Arrays.asList( // list of market conditions from martinique.json
//                                        Conditions.FRONTIER,
//                                        Conditions.RARE_ORE_ABUNDANT,
//                                        Conditions.ORE_ABUNDANT,
//                                        Conditions.THIN_ATMOSPHERE, 
//                                        Conditions.POPULATION_5)),
//                        new ArrayList<>
//                                (Arrays.asList( // list of industries
//                                        Industries.MINING,
//                                        Industries.REFINING,
//                                        Industries.SPACEPORT,
//                                        Industries.LIGHTINDUSTRY,
//                                        Industries.POPULATION)),
//                        new ArrayList<>(
//                                Arrays.asList( // which submarkets to generate
//                                        Submarkets.SUBMARKET_BLACK,
//                                        Submarkets.SUBMARKET_OPEN)),
//                        0.3f); // tariff amount

                tolp3.setCustomDescriptionId("planet_tolpIII");
                
                // Tolp jump point in L5 ---------------
                JumpPointAPI jumpPoint2 = Global.getFactory().createJumpPoint("tolp_inner_jump", "Tolp Bridge");
		jumpPoint2.setCircularOrbit( tolp_star, 185, 4500, 200);
		jumpPoint2.setRelatedPlanet(tolp3);
		system.addEntity(jumpPoint2);
                
                ///////////////////////////////////////////////////////////////////////////////////////////////////////////
                
                // Tolp IV, The nexus of the P9 faction ---------------
                PlanetAPI tolp4 = system.addPlanet("tolpIV", tolp_star, "Tolp IV", "tundra", 120, 180, 4000, 110);
		tolp4.getSpec().setPitch(-15f);
		tolp4.getSpec().setTilt(15f);
		tolp4.applySpecChanges();
                tolp4.setCustomDescriptionId("planet_tolpIV");
		tolp4.getSpec().setGlowTexture(Global.getSettings().getSpriteName("hab_glows", "volturn"));

                // adds the marketplace to Tolp IV ---------------
//                MarketAPI tolp4Market = AddMarketplace.addMarketplace("pn_colony", tolp4, null,
//                        "Tolp IV", // name of the market
//                        7, // size of the market (from the JSON)
//                        new ArrayList<>(
//                                Arrays.asList( // list of market conditions from martinique.json
//                                        Conditions.FARMLAND_POOR,
//                                        Conditions.RARE_ORE_SPARSE,
//                                        Conditions.ORE_SPARSE,
//                                        Conditions.ORGANICS_COMMON,
//                                        Conditions.FARMLAND_POOR,
//                                        Conditions.COLD,
//                                        Conditions.HABITABLE, 
//                                        Conditions.EXTREME_WEATHER,
//                                        Conditions.VOLATILES_DIFFUSE,
//                                        Conditions.IRRADIATED,
//                                        Conditions.POPULATION_7)),
//                        new ArrayList<>
//                                (Arrays.asList( // list of industries
//                                        Industries.BATTLESTATION_HIGH,
//                                        Industries.MILITARYBASE,
//                                        Industries.MINING,
//                                        Industries.FUELPROD,
//                                        Industries.WAYSTATION,
//                                        Industries.SPACEPORT,
//                                        Industries.FARMING,
//                                        Industries.HEAVYBATTERIES,
//                                        Industries.POPULATION)),
//                        new ArrayList<>(
//                                Arrays.asList( // which submarkets to generate
//                                        Submarkets.GENERIC_MILITARY,
//                                        Submarkets.SUBMARKET_BLACK,
//                                        Submarkets.SUBMARKET_OPEN,
//                                        Submarkets.SUBMARKET_STORAGE)),
//                        0.3f); // tariff amount
//                        tolp4Market.addIndustry(Industries.HEAVYINDUSTRY, new ArrayList<>(Arrays.asList(Items.CORRUPTED_NANOFORGE)));

                tolp4.setCustomDescriptionId("planet_tolpIV");
                

                
                ///////////////////////////////////////////////////////////////////////////////////////////////////////////
                    
                    
                // Tolp V, Boring gas giant---------------
                PlanetAPI tolp5 = system.addPlanet("tolpV", tolp_star, "Tolp V", "gas_giant", 180, 250, 5900, 340);
		tolp5.getSpec().setPitch(3f);
		tolp5.getSpec().setTilt(-2f);
		tolp5.applySpecChanges();
                tolp5.setCustomDescriptionId("planet_tolpV");
                
                
                // Rings    
                system.addRingBand(tolp5, "misc", "rings_dust0", 256f, 2, Color.white, 256f, 450, 80f);
		system.addRingBand(tolp5, "misc", "rings_asteroids0", 256f, 3, Color.white, 256f, 500, 100f);
		system.addRingBand(tolp5, "misc", "rings_dust0", 256f, 2, Color.white, 256f, 600, 130f);
		system.addRingBand(tolp5, "misc", "rings_asteroids0", 256f, 3, Color.white, 256f, 650, 80f);

                ///////////////////////////////////////////////////////////////////////////////////////////////////////////
                    
                    
                // Tolp VI, Boring gas giant with a pirate base---------------
                PlanetAPI tolp6 = system.addPlanet("tolpVI", tolp_star, "Tolp VI", "gas_giant", 230, 170, 7000, 550);
		tolp6.getSpec().setPitch(-1f);
		tolp6.getSpec().setTilt(-5f);
		tolp6.applySpecChanges();
                tolp6.setCustomDescriptionId("planet_tolpVI");

                // Complex  43, Abedisi rules here ---------------
                SectorEntityToken tolppiratestation = system.addCustomEntity("tolp_piratestation", "Complex  43", "station_side06", "pirates");
                tolppiratestation.setCircularOrbitPointingDown(system.getEntityById("tolpVI"), 90, 450, 45);

                // add the marketplace to Meridian Station ---------------
//                MarketAPI tolppiratestationMarket = AddMarketplace.addMarketplace("pirates", tolppiratestation, null,
//                        "Complex  43", // name of the market
//                        5, // size of the market (from the JSON)
//                        new ArrayList<>(
//                        Arrays.asList( // list of market conditions from nikolaev.json
//                                Conditions.FREE_PORT,
//                                Conditions.VICE_DEMAND,
//                                Conditions.STEALTH_MINEFIELDS,
//                                //Conditions.VOLATILES_DEPOT,
//                                //Conditions.ORBITAL_STATION,
//                                Conditions.POPULATION_5)),
//                        new ArrayList<>
//                        (Arrays.asList( // list of industries
//                                Industries.BATTLESTATION,
//                                //Industries.HEAVYINDUSTRY,
//                                Industries.WAYSTATION,
//                                Industries.SPACEPORT,
//                                Industries.POPULATION)),
//                        new ArrayList<>(
//                        Arrays.asList( // which submarkets to generate
//                                Submarkets.SUBMARKET_BLACK,
//                                Submarkets.SUBMARKET_OPEN,
//                                Submarkets.SUBMARKET_STORAGE)),
//                0.3f); // tariff amount
//                tolppiratestationMarket.addIndustry(Industries.HEAVYINDUSTRY, new ArrayList<>(Arrays.asList(Items.CORRUPTED_NANOFORGE)));


                tolppiratestation.setCustomDescriptionId("tolp_piratestation");  
                
                
                //SectorEntityToken tolppiratestation = system.addCustomEntity("tolp_piratestation", "Complex  43", "station_side04", "pirates");
		//tolppiratestation.setCircularOrbitPointingDown(system.getEntityById("tolpVI"), 90, 450, 45);		
		//tolppiratestation.setCustomDescriptionId("tolp_piratestation");

                ///////////////////////////////////////////////////////////////////////////////////////////////////////////
                ///////////////////////////////////////////////////////////////////////////////////////////////////////////
                ///////////////////////////////////////////////////////////////////////////////////////////////////////////
                ///////////////////////////////////////////////////////////////////////////////////////////////////////////
                ///////////////////////////////////////////////////////////////////////////////////////////////////////////
                ///////////////////////////////////////////////////////////////////////////////////////////////////////////
                
         
                // Asteroids
		system.addAsteroidBelt(tolp_star, 90, 7965, 500, 150, 300, Terrain.ASTEROID_BELT,  "Those Annoying Pebbles");
		system.addRingBand(tolp_star, "misc", "rings_dust0", 256f, 3, Color.white, 256f, 7930, 305f, null, null);
		system.addRingBand(tolp_star, "misc", "rings_asteroids0", 256f, 3, Color.white, 256f, 8010, 295f, null, null);

                
                // Tolp Relay at L3 ---------------
                SectorEntityToken tolprelay = system.addCustomEntity("tolp_relay", // unique id
                        "Herr Nils", // name - if null, defaultName from custom_entities.json will be used
                        "comm_relay", // type of object, defined in custom_entities.json
                        "pn_colony"); // faction
                tolprelay.setCircularOrbitPointingDown(tolp_star, 120, 8000, 240);
                
                
                system.autogenerateHyperspaceJumpPoints(true, true, true); 
                
                
        }
}
