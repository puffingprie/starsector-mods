package data.scripts.world.nosuchorg.stars;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.JumpPointAPI;
import com.fs.starfarer.api.campaign.OrbitAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin;

import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.StarTypes;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Terrain;
import com.fs.starfarer.api.impl.campaign.procgen.NebulaEditor;
import com.fs.starfarer.api.impl.campaign.procgen.StarAge;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.SalvageSpecialAssigner;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.ShipCondition;
import com.fs.starfarer.api.impl.campaign.terrain.DebrisFieldTerrainPlugin.DebrisFieldParams;
import com.fs.starfarer.api.impl.campaign.terrain.DebrisFieldTerrainPlugin.DebrisFieldSource;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.util.Misc;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import data.scripts.world.nosuchorg.addMarketplace;
import java.util.Random;

public class nusquam {
    
    private final Random rand = new Random();

    public void generate(SectorAPI sector) {
        
        StarSystemAPI system = sector.createStarSystem("Nusquam");
        system.getLocation().set(6660, 6660);
        system.setBackgroundTextureFilename("graphics/backgrounds/hyperspace1.jpg");

        PlanetAPI Nusquam_sun = system.initStar("Nusquam", StarTypes.WHITE_DWARF, 550f, 500f); // 0.9 solar masses
        Nusquam_sun.setLightColorOverrideIfStar(Color.pink);
//        
//        // PLANETS 
//        
//
        PlanetAPI tombstone_planet = system.addPlanet("nso_tombstone", Nusquam_sun, "Tombstone", "irradiated", 300, 180, 6000, 180); // 0.0025 AU
        //PlanetAPI addPlanet(String id, SectorEntityToken focus, String name, String type, float angle, float radius, float orbitRadius, float orbitDays);
        tombstone_planet.setCustomDescriptionId("nso_tombstone_description");
        tombstone_planet.getSpec().setRotation(5f); // 5 degrees/second = 7.2 days/revolution
        tombstone_planet.getSpec().setGlowTexture(Global.getSettings().getSpriteName("hab_glows", "sindria"));
        tombstone_planet.getSpec().setGlowColor(new Color(255, 0, 255, 255));
        tombstone_planet.applySpecChanges();
        
        //addMarketplace(String factionID, SectorEntityToken primaryEntity, ArrayList<SectorEntityToken> connectedEntities, String name, 
        //int size, ArrayList<String> marketConditions, ArrayList<String> Industries, ArrayList<String> submarkets, float tariff)
        MarketAPI tombstone_Market = addMarketplace.addMarketplace("no_such_org", tombstone_planet, 
                null, "Tombstone", 5, 
                new ArrayList<>(Arrays.asList(Conditions.RUINS_SCATTERED, Conditions.FARMLAND_POOR, Conditions.ORGANICS_TRACE, Conditions.ORE_MODERATE, Conditions.IRRADIATED, Conditions.STEALTH_MINEFIELDS, Conditions.SOLAR_ARRAY, Conditions.FREE_PORT, Conditions.MILD_CLIMATE)), 
                new ArrayList<>(Arrays.asList(Industries.MILITARYBASE, Industries.SPACEPORT, Industries.POPULATION, Industries.FARMING, Industries.BATTLESTATION_HIGH, Industries.GROUNDDEFENSES)), 
                new ArrayList<>(Arrays.asList(Submarkets.SUBMARKET_STORAGE, Submarkets.SUBMARKET_BLACK, Submarkets.GENERIC_MILITARY, Submarkets.SUBMARKET_OPEN)),
                0.3f);
        
        tombstone_Market.addIndustry(Industries.ORBITALWORKS,new ArrayList<String>(Arrays.asList(Items.CORRUPTED_NANOFORGE)));
//        tombstone_Market.getIndustry(Industries.HIGHCOMMAND).setAICoreId(Commodities.ALPHA_CORE);
//        tombstone_Market.getIndustry(Industries.STARFORTRESS_HIGH).setAICoreId(Commodities.ALPHA_CORE);

        
        //Adding DebrisField
        DebrisFieldParams params = new DebrisFieldParams(//DebrisField code thanks to Histidine!
                500f, // field radius - should not go above 1000 for performance reasons
                -1f, // density, visual - affects number of debris pieces
                10000000f, // duration in days 
                0f); // days the field will keep generating glowing pieces
        params.source = DebrisFieldSource.MIXED;
        params.baseSalvageXP = 250; // base XP for scavenging in field
        SectorEntityToken debrisNextToTombstone = Misc.addDebrisField(system, params, StarSystemGenerator.random);
        debrisNextToTombstone.setSensorProfile(null);  // optional I think - Hist.
        debrisNextToTombstone.setDiscoverable(null);  // ditto
        debrisNextToTombstone.setCircularOrbit(Nusquam_sun, 345f, 6000f, 180f);
        //void setCircularOrbit(SectorEntityToken focus, float angle, float orbitRadius, float orbitDays);
        debrisNextToTombstone.setId("nso_debrisNextToTombstone");  // if you need to reference this specific debris field elsewhere later
        
        //NSO ships to recover
        addOneOfTwoDerelicts(system, debrisNextToTombstone, "kayse_deathknight_beam", "kayse_banshee_variant", ShipCondition.AVERAGE, 100, true, "no_such_org");
        addOneOfTwoDerelicts(system, debrisNextToTombstone, "kayse_pyre_variant", "kayse_wight_variant", ShipCondition.GOOD, 400, true, "no_such_org");
        addOneOfTwoDerelicts(system, debrisNextToTombstone, "kayse_ghoul_variant", "kayse_grim_variant", ShipCondition.GOOD, 300, true, "no_such_org");
        //Hegemony ships (mostly for flavor, but you could spend a Story Point if you really want)
        addOneOfTwoDerelicts(system, debrisNextToTombstone, "onslaught_Standard", "eagle_Balanced", ShipCondition.BATTERED, 200, false, Factions.HEGEMONY);
        addOneOfTwoDerelicts(system, debrisNextToTombstone, "enforcer_Balanced", "buffalo_hegemony_Standard", ShipCondition.BATTERED, 350, false, Factions.HEGEMONY);

        PlanetAPI boneyard_gas_planet = system.addPlanet("nso_boneyard_gas", Nusquam_sun, "Ossum", "ice_giant", 170, 320, 8000, 240); // 0.0025 AU

        PlanetAPI boneyard_planet = system.addPlanet("nso_boneyard", boneyard_gas_planet, "Boneyard", "tundra", 300, 180, 1200, 24); // 0.0025 AU
        boneyard_planet.setCustomDescriptionId("nso_crypt_description");
        boneyard_planet.getSpec().setRotation(5f); // 5 degrees/second = 7.2 days/revolution
//        boneyard_planet.getSpec().setGlowTexture(Global.getSettings().getSpriteName("hab_glows", "sindria"));
        boneyard_planet.getSpec().setGlowColor(new Color(255, 0, 255, 255));
        boneyard_planet.applySpecChanges();
        
        //addMarketplace(String factionID, SectorEntityToken primaryEntity, ArrayList<SectorEntityToken> connectedEntities, String name, 
        //int size, ArrayList<String> marketConditions, ArrayList<String> Industries, ArrayList<String> submarkets, float tariff)
        MarketAPI boneyard_Market = addMarketplace.addMarketplace("no_such_org", boneyard_planet, 
                null, "Boneyard", 4, 
                new ArrayList<>(Arrays.asList(Conditions.RUINS_SCATTERED, Conditions.ORE_ABUNDANT, Conditions.RARE_ORE_MODERATE, Conditions.COLD, Conditions.STEALTH_MINEFIELDS, Conditions.FREE_PORT)), 
                new ArrayList<>(Arrays.asList(Industries.PATROLHQ, Industries.SPACEPORT, Industries.POPULATION, Industries.MINING, Industries.REFINING, Industries.ORBITALSTATION_HIGH, Industries.GROUNDDEFENSES)), 
                new ArrayList<>(Arrays.asList(Submarkets.SUBMARKET_STORAGE, Submarkets.SUBMARKET_BLACK, Submarkets.GENERIC_MILITARY, Submarkets.SUBMARKET_OPEN)),
                0.3f);
        

//        // STABLE LOCATIONS AND RELAYS
//        
        SectorEntityToken relay = system.addCustomEntity("nso_relay", "Ossum Relay", "comm_relay",
                                                         "no_such_org");
                relay.setCircularOrbit(Nusquam_sun, 220, 3500, 215);
        
        SectorEntityToken stableloc2 = system.addCustomEntity(null,null, "stable_location", Factions.NEUTRAL); 
		stableloc2.setCircularOrbitPointingDown(Nusquam_sun, 75, 4000, 215f);
                
        SectorEntityToken stableloc3 = system.addCustomEntity(null,null, "stable_location", Factions.NEUTRAL); 
		stableloc3.setCircularOrbitPointingDown(Nusquam_sun, 310, 3800, 215f);

//        // DECORATIONS 
//        
//
        //system.addAsteroidBelt(Nusquam_sun, 120, 5000, 128, 440, 470);
        system.addAsteroidBelt(Nusquam_sun, 1500, 5000, 200, 300, 600, Terrain.ASTEROID_BELT, "The Ring");

        
//        // JUMP POINTS 
//
//        JumpPointAPI jumpPoint = Global.getFactory().createJumpPoint("Boneyard_jp", "Boneyard Jump Point");
//        OrbitAPI orbit = Global.getFactory().createCircularOrbit(boneyard_gas_planet, 90, 550, 25);
//        jumpPoint.setOrbit(orbit);
//        jumpPoint.setRelatedPlanet(boneyard_gas_planet);
//        jumpPoint.setStandardWormholeToHyperspaceVisual();
//        system.addEntity(jumpPoint);
        
        JumpPointAPI jumpPoint2 = Global.getFactory().createJumpPoint("tombstone_jp", "Tombstone Portal");
        OrbitAPI orbit2 = Global.getFactory().createCircularOrbit(tombstone_planet, 100, 800, 25);
        //void setCircularOrbit(SectorEntityToken focus, float angle, float orbitRadius, float orbitDays);
        jumpPoint2.setOrbit(orbit2);
        jumpPoint2.setRelatedPlanet(tombstone_planet);
        jumpPoint2.setStandardWormholeToHyperspaceVisual();
        system.addEntity(jumpPoint2);
        
        // PROCGEN
        

        float radiusAfter = StarSystemGenerator.addOrbitingEntities(system, Nusquam_sun, StarAge.AVERAGE,
                                                                    4, 6, // min/max entities to add
                                                                    10000, // radius to start adding at
                                                                    2, // name offset - next planet will be <system name> <roman numeral of this parameter + 1>
                                                                    true); // whether to use custom or system-name based names

        system.autogenerateHyperspaceJumpPoints(true, true); //begone evil clouds
        HyperspaceTerrainPlugin plugin = (HyperspaceTerrainPlugin) Misc.getHyperspaceTerrain().getPlugin();
        NebulaEditor editor = new NebulaEditor(plugin);
        float minRadius = plugin.getTileSize() * 2f;

        float radius = system.getMaxRadiusInHyperspace();
        editor.clearArc(system.getLocation().x, system.getLocation().y, 0, radius + minRadius, 0, 360f);
        editor.clearArc(system.getLocation().x, system.getLocation().y, 0, radius + minRadius, 0, 360f, 0.25f);
    }
    
    protected void addOneOfTwoDerelicts(StarSystemAPI system, SectorEntityToken focus, String variantId, String variantId2,
                               ShipRecoverySpecial.ShipCondition condition, float orbitRadius, boolean recoverable) {
        if (rand.nextBoolean()){
            addDerelict(system, focus, variantId, condition, orbitRadius, recoverable, Factions.NEUTRAL);
        }else{
            addDerelict(system, focus, variantId2, condition, orbitRadius, recoverable, Factions.NEUTRAL);
        }
    }
    
    protected void addOneOfTwoDerelicts(StarSystemAPI system, SectorEntityToken focus, String variantId, String variantId2,
                               ShipRecoverySpecial.ShipCondition condition, float orbitRadius, boolean recoverable, String Fact) {
        if (rand.nextBoolean()){
            addDerelict(system, focus, variantId, condition, orbitRadius, recoverable, Fact);
        }else{
            addDerelict(system, focus, variantId2, condition, orbitRadius, recoverable, Fact);
        }
    }
    
    //Shorthand for adding derelicts, thanks to Tartiflette
    protected void addDerelict(StarSystemAPI system, SectorEntityToken focus, String variantId,
                               ShipRecoverySpecial.ShipCondition condition, float orbitRadius, boolean recoverable, String Fact) {
        DerelictShipEntityPlugin.DerelictShipData params = new DerelictShipEntityPlugin.DerelictShipData(new ShipRecoverySpecial.PerShipData(variantId, condition, Fact, 0), false);
        //PerShipData(String variantId, ShipCondition condition, String factionIdForShipName, float sModProb)
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
