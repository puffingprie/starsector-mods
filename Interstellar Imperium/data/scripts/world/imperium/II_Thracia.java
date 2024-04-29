package data.scripts.world.imperium;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.JumpPointAPI;
import com.fs.starfarer.api.campaign.OrbitAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.EconomyAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.impl.campaign.ids.StarTypes;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.ids.Terrain;
import com.fs.starfarer.api.impl.campaign.procgen.NebulaEditor;
import com.fs.starfarer.api.impl.campaign.procgen.StarAge;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.terrain.AsteroidFieldTerrainPlugin.AsteroidFieldParams;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.MagneticFieldTerrainPlugin.MagneticFieldParams;
import com.fs.starfarer.api.util.Misc;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class II_Thracia {

    private static final Random random = new Random();

    public static MarketAPI addMarketplace(String factionID, SectorEntityToken primaryEntity,
            ArrayList<SectorEntityToken> connectedEntities, String name, int size,
            ArrayList<String> conditionList, ArrayList<ArrayList<String>> industryList, ArrayList<String> submarkets,
            float tarrif, boolean freePort) {
        EconomyAPI globalEconomy = Global.getSector().getEconomy();
        String planetID = primaryEntity.getId();
        String marketID = planetID/* + "_market"*/;

        MarketAPI newMarket = Global.getFactory().createMarket(marketID, name, size);
        newMarket.setFactionId(factionID);
        newMarket.setPrimaryEntity(primaryEntity);
        newMarket.getTariff().modifyFlat("generator", tarrif);
        newMarket.getLocationInHyperspace().set(primaryEntity.getLocationInHyperspace());

        if (null != submarkets) {
            for (String market : submarkets) {
                newMarket.addSubmarket(market);
            }
        }

        for (String condition : conditionList) {
            newMarket.addCondition(condition);
        }

        for (ArrayList<String> industryWithParam : industryList) {
            String industry = industryWithParam.get(0);
            if (industryWithParam.size() == 1) {
                newMarket.addIndustry(industry);
            } else {
                newMarket.addIndustry(industry, industryWithParam.subList(1, industryWithParam.size()));
            }
        }

        if (null != connectedEntities) {
            for (SectorEntityToken entity : connectedEntities) {
                newMarket.getConnectedEntities().add(entity);
            }
        }

        newMarket.setFreePort(freePort);
        globalEconomy.addMarket(newMarket, true);
        primaryEntity.setMarket(newMarket);
        primaryEntity.setFaction(factionID);

        if (null != connectedEntities) {
            for (SectorEntityToken entity : connectedEntities) {
                entity.setMarket(newMarket);
                entity.setFaction(factionID);
            }
        }

        return newMarket;
    }

    public static SectorEntityToken getBabylon() {
        if (Global.getSector().getStarSystem("Thracia") == null) {
            return null;
        }
        return Global.getSector().getStarSystem("Thracia").getEntityById("ii_babylon");
    }

    public static SectorEntityToken getThracia() {
        if (Global.getSector().getStarSystem("Thracia") == null) {
            return null;
        }
        return Global.getSector().getStarSystem("Thracia").getEntityById("ii_thracia");
    }

    private static void initFactionRelationships(SectorAPI sector) {
        FactionAPI imperium = sector.getFaction("interstellarimperium");

        imperium.setRelationship(Factions.DIKTAT, RepLevel.FRIENDLY);
        imperium.setRelationship(Factions.HEGEMONY, RepLevel.VENGEFUL);
        imperium.setRelationship(Factions.LUDDIC_CHURCH, RepLevel.HOSTILE);
        imperium.setRelationship(Factions.LUDDIC_PATH, RepLevel.VENGEFUL);
        imperium.setRelationship(Factions.PIRATES, RepLevel.HOSTILE);
        imperium.setRelationship(Factions.INDEPENDENT, RepLevel.NEUTRAL);
        imperium.setRelationship(Factions.PERSEAN, RepLevel.WELCOMING);
        imperium.setRelationship(Factions.REMNANTS, RepLevel.HOSTILE);
        imperium.setRelationship(Factions.TRITACHYON, RepLevel.NEUTRAL);
        imperium.setRelationship("blackrock_driveyards", RepLevel.NEUTRAL);
        imperium.setRelationship("shadow_industry", RepLevel.INHOSPITABLE);
        imperium.setRelationship("exigency", RepLevel.HOSTILE);
        imperium.setRelationship("exipirated", RepLevel.HOSTILE);
        imperium.setRelationship("templars", RepLevel.HOSTILE);
        imperium.setRelationship("junk_pirates", RepLevel.HOSTILE);
        imperium.setRelationship("pack", RepLevel.HOSTILE);
        imperium.setRelationship("syndicate_asp", RepLevel.SUSPICIOUS);
        imperium.setRelationship("tiandong", RepLevel.FAVORABLE);
        imperium.setRelationship("sylphon", RepLevel.FAVORABLE);
        imperium.setRelationship("unitedpamed", RepLevel.NEUTRAL);
        imperium.setRelationship("xhanempire", RepLevel.HOSTILE);
        imperium.setRelationship("scalartech", RepLevel.NEUTRAL);
        imperium.setRelationship("tahlan_legioinfernalis", RepLevel.HOSTILE);
        imperium.setRelationship("SCY", RepLevel.SUSPICIOUS);
        imperium.setRelationship("roider", RepLevel.NEUTRAL);
        imperium.setRelationship("prv", RepLevel.NEUTRAL);
        imperium.setRelationship("rb", RepLevel.HOSTILE);
        imperium.setRelationship("pearson_exotronics", RepLevel.SUSPICIOUS);
        imperium.setRelationship("ora", RepLevel.SUSPICIOUS);
        imperium.setRelationship("mayasura", RepLevel.WELCOMING);
        imperium.setRelationship("al_ars", RepLevel.FAVORABLE);
        imperium.setRelationship("kadur_remnant", RepLevel.NEUTRAL);
        imperium.setRelationship("HMI", RepLevel.SUSPICIOUS);
        imperium.setRelationship("star_federation", RepLevel.INHOSPITABLE);
        imperium.setRelationship("diableavionics", RepLevel.HOSTILE);
        imperium.setRelationship("dassault_mikoyan", RepLevel.NEUTRAL);
    }

    public void generate(SectorAPI sector) {
        random.setSeed(sector.getSeedString().hashCode());

        StarSystemAPI system = sector.createStarSystem("Thracia");
        system.getLocation().set(-7200, 6600);
        system.addTag(Tags.THEME_CORE_POPULATED);
        system.addTag(Tags.THEME_CORE);

        system.setBackgroundTextureFilename("graphics/imperium/backgrounds/ii_thracia.png");

        Misc.addNebulaFromPNG("data/campaign/terrain/ii_thracia_nebula.png",
                0, 0, // center of nebula
                system, // location to add to
                "terrain", "ii_nebula_orange", // "nebula_blue", // texture to use, uses xxx_map for map
                4, 4, StarAge.AVERAGE); // number of cells in texture

        PlanetAPI star = system.initStar("ii_thracia", StarTypes.YELLOW, 800f, 400f); // 0.75 solar masses

        PlanetAPI byzantium = system.addPlanet("ii_byzantium", star, "Byzantium", "terran", 45, 140, 4000, 300); // 1 earth mass, 0.8 AU
        byzantium.getSpec().setPlanetColor(new Color(255, 255, 255));
        byzantium.getSpec().setAtmosphereColor(new Color(200, 190, 180, 200));
        byzantium.getSpec().setCloudColor(new Color(150, 140, 130, 200));
        byzantium.getSpec().setTilt(15);
        byzantium.getSpec().setTexture(Global.getSettings().getSpriteName("planets", "ii_byzantium"));
        byzantium.applySpecChanges();
        byzantium.setCustomDescriptionId("ii_planet_byzantium");

        SectorEntityToken babylon = system.addCustomEntity("ii_babylon", "Babylon", "ii_station_babylon",
                "interstellarimperium");
        babylon.setCircularOrbitPointingDown(byzantium, 45, 400, 19); // 0.002 AU

        PlanetAPI perinthus = system.addPlanet("ii_perinthus", star, "Perinthus", "desert", 0, 150, 3000, 150);
        perinthus.getSpec().setTilt(10f);
        perinthus.getSpec().setCloudColor(new Color(240, 230, 170, 140));
        perinthus.getSpec().setAtmosphereThickness(0.15f);
        perinthus.applySpecChanges();
        perinthus.setCustomDescriptionId("ii_planet_perinthus");

        PlanetAPI serdica = system.addPlanet("ii_serdica", star, "Serdica", "ice_giant", 75, 250, 10000, 1100); // 20 earth masses
        serdica.getSpec().setTilt(-20f);
        serdica.getSpec().setCloudColor(new Color(60, 160, 210, 170));
        serdica.applySpecChanges();
        system.addRingBand(serdica, "misc", "rings_dust0", 256f, 2, Color.white, 256f, 750, -11);
        system.addRingBand(serdica, "misc", "rings_dust0", 256f, 3, Color.white, 256f, 900, -14.5f, Terrain.RING,
                "Serdica Ring Minor");
        system.addRingBand(serdica, "misc", "rings_dust0", 256f, 0, Color.white, 256f, 1250, -23.5f);
        system.addRingBand(serdica, "misc", "rings_dust0", 256f, 1, Color.white, 256f, 1200, -22, Terrain.RING,
                "Serdica Ring Major");
        serdica.setCustomDescriptionId("ii_planet_serdica");

        PlanetAPI hadrian = system.addPlanet("ii_hadrian", serdica, "Hadrian", "cryovolcanic", 60, 60, 400, -4.5f); // 0.002 AU
        hadrian.getSpec().setPitch(20f);
        hadrian.applySpecChanges();
        hadrian.setCustomDescriptionId("ii_planet_hadrian");

        PlanetAPI traian = system.addPlanet("ii_traian", serdica, "Traian", "frozen", 120, 100, 1350, -26); // 0.75 earth masses
        traian.getSpec().setTilt(70f);
        traian.applySpecChanges();
        traian.setCustomDescriptionId("ii_planet_traian");

        SectorEntityToken traianStation = system.addCustomEntity("ii_station_traian", "Traian Refinery",
                "station_side07", Factions.INDEPENDENT);
        traianStation.setCircularOrbitPointingDown(traian, 15, 300, -15); // 0.0015 AU
        traianStation.setInteractionImage("illustrations", "hound_hangar");
        traianStation.setCustomDescriptionId("ii_station_traian");

        system.addAsteroidBelt(star, 350, 9000, 500, 950, 1050, Terrain.ASTEROID_BELT, "Cirrus Belt");

        PlanetAPI cassus = system.addPlanet("ii_cassus", star, "Cassus", "ii_cobalt", 260, 70, 14000, 1500);
        cassus.setCustomDescriptionId("ii_planet_cassus");

        SectorEntityToken cassusField = system.addTerrain(Terrain.MAGNETIC_FIELD,
                new MagneticFieldParams(cassus.getRadius() + 165f, // terrain effect band width
                        (cassus.getRadius() + 165f) / 2f, // terrain effect middle radius
                        cassus, // entity that it's around
                        cassus.getRadius() - 15f, // visual band start
                        cassus.getRadius() - 15f + 165f, // visual band end
                        new Color(0, 70, 170, 75), // base color
                        1f, // probability to spawn aurora sequence, checked once/day when no aurora in progress
                        new Color(100, 50, 245),
                        new Color(130, 110, 235),
                        new Color(170, 155, 210),
                        new Color(100, 130, 190),
                        new Color(195, 220, 230),
                        new Color(140, 210, 235),
                        new Color(30, 190, 220),
                        new Color(80, 110, 225)));
        cassusField.setCircularOrbit(cassus, 0, 0, 100);

        SectorEntityToken thraciaGate = system.addCustomEntity("ii_thracia_gate", // unique id
                "Thracia Gate", // name - if null, defaultName from custom_entities.json will be used
                "inactive_gate", // type of object, defined in custom_entities.json
                null); // faction
        thraciaGate.setCircularOrbit(star, 90, 6000, 475);

        SectorEntityToken roidField1 = system.addTerrain(Terrain.ASTEROID_FIELD,
                new AsteroidFieldParams(
                        500f, // min radius
                        700f, // max radius
                        20, // min asteroid count
                        30, // max asteroid count
                        4f, // min asteroid radius
                        12f, // max asteroid radius
                        "Serdican L4 Asteroids")); // null for default name
        SectorEntityToken roidField2 = system.addTerrain(Terrain.ASTEROID_FIELD,
                new AsteroidFieldParams(
                        500f, // min radius
                        700f, // max radius
                        20, // min asteroid count
                        30, // max asteroid count
                        4f, // min asteroid radius
                        12f, // max asteroid radius
                        "Serdican L5 Asteroids")); // null for default name
        SectorEntityToken roidField3 = system.addTerrain(Terrain.ASTEROID_FIELD,
                new AsteroidFieldParams(
                        300f, // min radius
                        500f, // max radius
                        10, // min asteroid count
                        15, // max asteroid count
                        6f, // min asteroid radius
                        18f, // max asteroid radius
                        "Perinthus Obruta")); // null for default name
        SectorEntityToken roidField4 = system.addTerrain(Terrain.ASTEROID_FIELD,
                new AsteroidFieldParams(
                        400f, // min radius
                        600f, // max radius
                        15, // min asteroid count
                        25, // max asteroid count
                        5f, // min asteroid radius
                        15f, // max asteroid radius
                        "Cassian L4 Asteroids")); // null for default name
        SectorEntityToken roidField5 = system.addTerrain(Terrain.ASTEROID_FIELD,
                new AsteroidFieldParams(
                        400f, // min radius
                        600f, // max radius
                        15, // min asteroid count
                        25, // max asteroid count
                        5f, // min asteroid radius
                        15f, // max asteroid radius
                        "Cassian L5 Asteroids")); // null for default name
        roidField1.setCircularOrbit(star, 135, 10000, 1100);
        roidField2.setCircularOrbit(star, 15, 10000, 1100);
        roidField3.setCircularOrbit(perinthus, 240, 50, 50);
        roidField4.setCircularOrbit(star, 320, 14000, 1500);
        roidField5.setCircularOrbit(star, 200, 14000, 1500);

        JumpPointAPI jumpPoint = Global.getFactory().createJumpPoint("ii_thraciaGate", "Jump-point Alpha");
        OrbitAPI orbit = Global.getFactory().createCircularOrbit(star, 345, 4000, 300);
        jumpPoint.setRelatedPlanet(byzantium);
        jumpPoint.setOrbit(orbit);
        jumpPoint.setStandardWormholeToHyperspaceVisual();
        system.addEntity(jumpPoint);

        SectorEntityToken relay = system.addCustomEntity("ii_babylon_relay", "Babylon Relay", "comm_relay",
                "interstellarimperium");
        relay.setCircularOrbit(star, 105, 4000, 300);

        SectorEntityToken nav_buoy = system.addCustomEntity(null, null, "nav_buoy", "interstellarimperium");
        nav_buoy.setCircularOrbitPointingDown(star, 60, 3000, 150);

        SectorEntityToken stable_location = system.addCustomEntity(null, null, "stable_location", Factions.NEUTRAL);
        stable_location.setCircularOrbitPointingDown(star, 200, 14000, 1500);

        StarSystemGenerator.addOrbitingEntities(system, star, StarAge.AVERAGE,
                1, 1, // min/max entities to add
                15000, // radius to start adding at
                4, // name offset - next planet will be <system name> <roman numeral of this parameter + 1>
                true, // whether to use custom or system-name based names
                false); // habitable allowed

        system.autogenerateHyperspaceJumpPoints(true, true, true);

        MarketAPI babylonMarket = addMarketplace("interstellarimperium", byzantium,
                new ArrayList<>(Arrays.asList(babylon)),
                "Byzantium", 7, // 4 industry limit
                new ArrayList<>(Arrays.asList(
                        Conditions.POPULATION_7,
                        "ii_imperialdoctrine",
                        "ii_vineyards",
                        Conditions.HABITABLE,
                        Conditions.TECTONIC_ACTIVITY,
                        Conditions.ORE_SPARSE,
                        Conditions.FARMLAND_ADEQUATE,
                        Conditions.ORGANICS_COMMON)),
                new ArrayList<>(Arrays.asList(
                        new ArrayList<>(Arrays.asList(Industries.POPULATION)),
                        new ArrayList<>(Arrays.asList(Industries.MEGAPORT)),
                        new ArrayList<>(Arrays.asList(Industries.HEAVYBATTERIES, Commodities.ALPHA_CORE)),
                        new ArrayList<>(Arrays.asList(Industries.WAYSTATION)),
                        new ArrayList<>(Arrays.asList(Industries.HIGHCOMMAND, Commodities.ALPHA_CORE)), // Industry
                        new ArrayList<>(Arrays.asList(Industries.LIGHTINDUSTRY)), // Industry
                        new ArrayList<>(Arrays.asList(Industries.FARMING)), // Industry
                        new ArrayList<>(Arrays.asList("ii_interstellarbazaar", Commodities.ALPHA_CORE)), // Industry
                        new ArrayList<>(Arrays.asList("ii_imperialguard", Commodities.ALPHA_CORE)),
                        new ArrayList<>(Arrays.asList("ii_stellacastellum", Commodities.ALPHA_CORE)))),
                new ArrayList<>(Arrays.asList(
                        "ii_ebay",
                        Submarkets.SUBMARKET_BLACK,
                        Submarkets.SUBMARKET_OPEN,
                        Submarkets.SUBMARKET_STORAGE)),
                0.3f,
                false
        );

        addMarketplace(Factions.PIRATES, perinthus,
                null,
                "Perinthus", 5, // 3 industry limit
                new ArrayList<>(Arrays.asList(
                        Conditions.POPULATION_5,
                        Conditions.HABITABLE,
                        Conditions.RUINS_SCATTERED,
                        Conditions.HOT,
                        Conditions.EXTREME_WEATHER,
                        Conditions.ORE_ABUNDANT)),
                new ArrayList<>(Arrays.asList(
                        new ArrayList<>(Arrays.asList(Industries.POPULATION)),
                        new ArrayList<>(Arrays.asList(Industries.SPACEPORT)),
                        new ArrayList<>(Arrays.asList(Industries.MINING)), // Industry
                        new ArrayList<>(Arrays.asList(Industries.LIGHTINDUSTRY)), // Industry
                        new ArrayList<>(Arrays.asList(Industries.TECHMINING)), // Industry
                        new ArrayList<>(Arrays.asList(Industries.GROUNDDEFENSES)))),
                new ArrayList<>(Arrays.asList(
                        Submarkets.SUBMARKET_BLACK,
                        Submarkets.SUBMARKET_OPEN,
                        Submarkets.SUBMARKET_STORAGE)
                ),
                0.3f,
                true
        );

        addMarketplace("interstellarimperium", hadrian,
                null,
                "Hadrian", 4, // 2 industry limit
                new ArrayList<>(Arrays.asList(
                        Conditions.POPULATION_4,
                        "ii_imperialdoctrine",
                        Conditions.COLD,
                        Conditions.VOLATILES_PLENTIFUL,
                        Conditions.POOR_LIGHT,
                        Conditions.ORE_SPARSE,
                        Conditions.LOW_GRAVITY)),
                new ArrayList<>(Arrays.asList(
                        new ArrayList<>(Arrays.asList(Industries.POPULATION)),
                        new ArrayList<>(Arrays.asList(Industries.SPACEPORT)),
                        new ArrayList<>(Arrays.asList(Industries.MINING, Commodities.ALPHA_CORE)), // Industry
                        new ArrayList<>(Arrays.asList(Industries.HEAVYBATTERIES, Commodities.ALPHA_CORE)), // No free alphas for you shitbags
                        new ArrayList<>(Arrays.asList(Industries.MILITARYBASE)))), // Industry
                new ArrayList<>(Arrays.asList(
                        Submarkets.SUBMARKET_BLACK,
                        Submarkets.SUBMARKET_OPEN,
                        Submarkets.SUBMARKET_STORAGE)),
                0.3f,
                false
        );

        addMarketplace(Factions.INDEPENDENT, traian,
                new ArrayList<>(Arrays.asList(traianStation)),
                "Traian", 3, // 1 industry limit
                new ArrayList<>(Arrays.asList(
                        Conditions.POPULATION_4,
                        Conditions.POOR_LIGHT,
                        Conditions.COLD,
                        Conditions.VOLATILES_TRACE,
                        Conditions.NO_ATMOSPHERE)),
                new ArrayList<>(Arrays.asList(
                        new ArrayList<>(Arrays.asList(Industries.POPULATION)),
                        new ArrayList<>(Arrays.asList(Industries.SPACEPORT)),
                        new ArrayList<>(Arrays.asList(Industries.WAYSTATION)),
                        new ArrayList<>(Arrays.asList(Industries.FUELPROD, Items.SYNCHROTRON)), // Industry
                        new ArrayList<>(Arrays.asList(Industries.STARFORTRESS_HIGH)))), // No free synchrotron for you shitbags
                new ArrayList<>(Arrays.asList(
                        Submarkets.SUBMARKET_BLACK,
                        Submarkets.SUBMARKET_OPEN,
                        Submarkets.SUBMARKET_STORAGE)),
                0.3f,
                false
        );

        system.addScript(new Demilitarize(babylonMarket));

        initFactionRelationships(sector);

        HyperspaceTerrainPlugin plugin = (HyperspaceTerrainPlugin) Misc.getHyperspaceTerrain().getPlugin();
        NebulaEditor editor = new NebulaEditor(plugin);

        float minRadius = plugin.getTileSize() * 2f;
        float radius = system.getMaxRadiusInHyperspace();
        editor.clearArc(system.getLocation().x, system.getLocation().y, 0, radius + minRadius * 0.5f, 0, 360f);
        editor.clearArc(system.getLocation().x, system.getLocation().y, 0, radius + minRadius, 0, 360f, 0.25f);
    }

    public static class Demilitarize implements EveryFrameScript {

        private final MarketAPI market;

        Demilitarize(MarketAPI market) {
            this.market = market;
        }

        @Override
        public void advance(float amount) {
            if (market.hasSubmarket(Submarkets.GENERIC_MILITARY)) {
                market.removeSubmarket(Submarkets.GENERIC_MILITARY);
            }
        }

        @Override
        public boolean isDone() {
            return false;
        }

        @Override
        public boolean runWhilePaused() {
            return false;
        }
    }
}
