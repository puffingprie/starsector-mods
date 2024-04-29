package data.scripts.world.imperium;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CustomCampaignEntityAPI;
import com.fs.starfarer.api.campaign.JumpPointAPI;
import com.fs.starfarer.api.campaign.OrbitAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin.DerelictShipData;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.StarTypes;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.ids.Terrain;
import com.fs.starfarer.api.impl.campaign.procgen.NebulaEditor;
import com.fs.starfarer.api.impl.campaign.procgen.StarAge;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.SalvageSpecialAssigner;
import com.fs.starfarer.api.impl.campaign.terrain.BaseTiledTerrain;
import com.fs.starfarer.api.impl.campaign.terrain.DebrisFieldTerrainPlugin.DebrisFieldParams;
import com.fs.starfarer.api.impl.campaign.terrain.DebrisFieldTerrainPlugin.DebrisFieldSource;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.MagneticFieldTerrainPlugin.MagneticFieldParams;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import org.lazywizard.lazylib.MathUtils;

import static data.scripts.world.imperium.II_Thracia.addMarketplace;
import java.util.Random;

public class II_Corsica {

    private static final WeightedRandomPicker<String> factionPicker = new WeightedRandomPicker<>();

    static {
        factionPicker.add(Factions.HEGEMONY, 5f);
        factionPicker.add(Factions.PIRATES, 3f * 2f);
        factionPicker.add(Factions.TRITACHYON, 5f);
        factionPicker.add("interstellarimperium", 5f * 0.5f);
    }

    public static void addDerelict(StarSystemAPI system, SectorEntityToken focus, String factionId, float orbitRadius,
            Random random) {
        DerelictShipData params = DerelictShipEntityPlugin.createRandom(factionId, null, random);
        if (params != null) {
            CustomCampaignEntityAPI entity = (CustomCampaignEntityAPI) BaseThemeGenerator.addSalvageEntity(system,
                    Entities.WRECK, Factions.NEUTRAL, params);
            entity.setDiscoverable(true);

            entity.getLocation().set(system.getLocation());
            float orbitDays = orbitRadius / (10f + (float) Math.random() * 5f);
            entity.setCircularOrbit(focus, (float) Math.random() * 360f, orbitRadius, orbitDays);
        }
    }

    public void generate(SectorAPI sector) {
        StarSystemAPI system = sector.createStarSystem("Corsica");
        system.getLocation().set(200, 4600);
        system.addTag(Tags.THEME_CORE_POPULATED);
        system.addTag(Tags.THEME_CORE);

        system.setBackgroundTextureFilename("graphics/imperium/backgrounds/ii_corsica.jpg");

        PlanetAPI star = system.initStar("ii_corsica", StarTypes.BLUE_SUPERGIANT, 1400f, 750f, 17, 0.5f, 6f); // 29.1 solar masses (30 total)
        star.getSpec().setAtmosphereThickness(0.5f);
        star.getSpec().setAtmosphereThicknessMin(50f);
        star.applySpecChanges();
        star.setCustomDescriptionId("ii_star_corsica");

        PlanetAPI otherStar
                = system.addPlanet("ii_corsica_beta", star, "Corsica Beta", StarTypes.ORANGE, 125, 300, 1900,
                        -23); // 0.9 solar masses, 0.49 AU
        otherStar.getSpec().setPitch(70f);
        otherStar.getSpec().setRotation(3.5f);
        otherStar.applySpecChanges();
        otherStar.setCustomDescriptionId("ii_star_corsica_beta");
        system.addCorona(star, 500f, 10, 0.25f, 3f);

        system.setSecondary(otherStar);

        system.addRingBand(otherStar, "misc", "ii_accretion", 256f, 3, Color.white, 256, 300, -9.7f); // 0.086 AU
        system.addRingBand(otherStar, "misc", "ii_accretion", 256f, 2, Color.white, 256, 375, -13.5f);
        system.addRingBand(otherStar, "misc", "ii_accretion", 256f, 1, new Color(255, 255, 255, 200), 256, 450, -17.7f);
        system.addRingBand(otherStar, "misc", "ii_accretion", 256f, 0, new Color(255, 255, 255, 150), 256, 525, -22.3f);

        PlanetAPI aleria = system.addPlanet("ii_aleria", star, "Aleria", "lava_minor", 90, 175, 5000, -115); // 1.5 earth masses, 1.43 AU
        aleria.getSpec().setAtmosphereColor(new Color(225, 75, 20, 100));
        aleria.getSpec().setCloudColor(new Color(150, 30, 5, 100));
        aleria.getSpec().setPlanetColor(new Color(255, 255, 255));
        aleria.getSpec().setGlowColor(new Color(255, 255, 255, 150));
        aleria.getSpec().setIconColor(new Color(225, 125, 60));
        aleria.getSpec().setTilt(60);
        aleria.getSpec().setCloudTexture(Global.getSettings().getSpriteName("planets", "ii_aleria_clouds"));
        aleria.getSpec().setTexture(Global.getSettings().getSpriteName("planets", "ii_aleria"));
        aleria.getSpec().setGlowTexture(Global.getSettings().getSpriteName("planets", "ii_aleria_light"));
        aleria.applySpecChanges();
        aleria.setCustomDescriptionId("ii_planet_aleria");

        float cydoniaAngle = 120f;
        PlanetAPI cydonia = system.addPlanet("ii_cydonia", star, "Cydonia", "ii_cydonia", cydoniaAngle, 250, 10000, -390); // 25 earth masses, 3.25 AU
        cydonia.setCustomDescriptionId("ii_planet_cydonia");
        system.addRingBand(cydonia, "misc", "rings_dust0", 256f, 0, new Color(255, 200, 255), 128f, 650, -22); // 0.0065 AU
        system.addRingBand(cydonia, "misc", "rings_dust0", 256f, 1, new Color(255, 200, 255), 256f, 800, -30,
                Terrain.RING, null);

        SectorEntityToken cydoniaField = system.addTerrain(Terrain.MAGNETIC_FIELD,
                new MagneticFieldParams(cydonia.getRadius() + 300f, // terrain effect band width
                        (cydonia.getRadius() + 300f) / 2f, // terrain effect middle radius
                        cydonia, // entity that it's around
                        cydonia.getRadius() + 50f, // visual band start
                        cydonia.getRadius() + 50f + 350f, // visual band end
                        new Color(80, 20, 60, 100), // base color
                        0.25f, // probability to spawn aurora sequence
                        new Color(230, 70, 200),
                        new Color(180, 80, 210),
                        new Color(150, 100, 200),
                        new Color(110, 120, 250),
                        new Color(70, 100, 240),
                        new Color(240, 70, 130),
                        new Color(190, 110, 150)));
        cydoniaField.setCircularOrbit(cydonia, 0, 0, 100);

        SectorEntityToken cydoniaStation = system.addCustomEntity("ii_station_cydonia", "Base Cydonia",
                "ii_station_cydonia", Factions.TRITACHYON);
        cydoniaStation.setCircularOrbitPointingDown(cydonia, 80, 350, -9); // 0.0035 AU
        cydoniaStation.setInteractionImage("illustrations", "cargo_loading");
        cydoniaStation.setCustomDescriptionId("ii_station_cydonia");

//        MarketAPI cydoniaPlanetMarket = Global.getFactory().createMarket("ii_cydonia_conditions", cydonia.getName(), 0);
//        cydoniaPlanetMarket.setPrimaryEntity(cydonia);
//        cydoniaPlanetMarket.setSurveyLevel(SurveyLevel.FULL);
//        cydoniaPlanetMarket.setFactionId(Factions.TRITACHYON);
//        cydoniaPlanetMarket.addCondition(Conditions.DENSE_ATMOSPHERE);
//        cydoniaPlanetMarket.addCondition(Conditions.EXTREME_WEATHER);
//        cydoniaPlanetMarket.addCondition(Conditions.VOLATILES_PLENTIFUL);
//        cydoniaPlanetMarket.setPlanetConditionMarketOnly(true);
//        cydonia.setMarket(cydoniaPlanetMarket);
        float hadesAngle = 25f;
        SectorEntityToken hades
                = system.addCustomEntity("ii_station_hades", "Hades", "station_side05", Factions.PIRATES);
        hades.setCircularOrbitPointingDown(star, hadesAngle, 8000, -220);
        hades.setInteractionImage("illustrations", "abandoned_station3");
        hades.setCustomDescriptionId("ii_station_hades");

        /* Hades debris */
        DebrisFieldParams params = new DebrisFieldParams(
                200f, // field radius - should not go above 1000 for performance reasons
                0.75f, // density, visual - affects number of debris pieces
                10000000f, // duration in days
                0f); // days the field will keep generating glowing pieces
        params.source = DebrisFieldSource.MIXED;
        params.baseSalvageXP = 400; // base XP for scavenging in field
        SectorEntityToken debris = Misc.addDebrisField(system, params, StarSystemGenerator.random);
        SalvageSpecialAssigner.assignSpecialForDebrisField(debris);
        debris.setDiscoverable(true);
        debris.setSensorProfile(100f);
        debris.setCircularOrbit(hades, 60, 30, -250);

        PlanetAPI arafa = system.addPlanet("ii_arafa", star, "Arafa", "gas_giant", 25, 500, 13000, -450);
        arafa.getSpec().setPitch(10f);
        arafa.getSpec().setAtmosphereColor(new Color(230, 170, 50, 100));
        arafa.getSpec().setCloudTexture(Global.getSettings().getSpriteName("planets", "ii_arafa_clouds"));
        arafa.getSpec().setCloudColor(new Color(255, 180, 70, 255));
        arafa.applySpecChanges();
        arafa.setCustomDescriptionId("ii_planet_arafa");
        system.addRingBand(arafa, "misc", "ii_arafa_rings", 1024f, 0, new Color(255, 255, 255, 150), 150f, 900, -20,
                Terrain.RING, null); // 0.0075 AU

        SectorEntityToken arafaHaze = system.addTerrain("ii_atmospherichaze", new BaseTiledTerrain.TileParams(
                " x      "
                + " xxx  x "
                + " xxx  xx"
                + "xx xx xx"
                + " xxxxx  "
                + "xx xxx x"
                + " xxxx xx"
                + "  x  x  ",
                8, 8, // size of the nebula grid, should match above string
                "terrain", "nebula_amber", 4, 4, null));
        arafaHaze.setCircularOrbit(arafa, 0, 0, -40);
        arafaHaze.addTag("radar_nebula");

        PlanetAPI inferi = system.addPlanet("ii_inferi", arafa, "Inferi", "barren-bombarded", 140, 50, 650, -13);
        inferi.getSpec().setPitch(80f);
        inferi.getSpec().setRotation(2.769f);
        inferi.applySpecChanges();
        inferi.setCustomDescriptionId("ii_planet_inferi");

        PlanetAPI carthage = system.addPlanet("ii_carthage", arafa, "Carthage", "desert", 120, 100, 1200, -30); // 1 earth mass
        carthage.getSpec().setGlowTexture(Global.getSettings().getSpriteName("hab_glows", "barren"));
        carthage.getSpec().setUseReverseLightForGlow(true);
        carthage.applySpecChanges();
        carthage.setInteractionImage("illustrations", "desert_moons_ruins");
        carthage.setCustomDescriptionId("ii_planet_carthage");

        /* Battle remnants */
        params = new DebrisFieldParams(
                300f, // field radius - should not go above 1000 for performance reasons
                0.5f, // density, visual - affects number of debris pieces
                10000000f, // duration in days
                0f); // days the field will keep generating glowing pieces
        params.source = DebrisFieldSource.BATTLE;
        params.baseSalvageXP = 250; // base XP for scavenging in field
        debris = Misc.addDebrisField(system, params, StarSystemGenerator.random);
        debris.setDiscoverable(true);
        debris.setSensorProfile(100f);
        debris.setCircularOrbit(carthage, 12, 200, -100);

        SectorEntityToken carthageStation = system.addCustomEntity("ii_station_traian", "Carthage Station",
                "ii_station_carthage", Factions.HEGEMONY);
        carthageStation.setCircularOrbitPointingDown(carthage, 15, 250, -5); // 0.0025 AU
        carthageStation.setInteractionImage("illustrations", "comm_relay");
        carthageStation.setCustomDescriptionId("ii_station_carthage");

        PlanetAPI vetus = system.addPlanet("ii_vetus", arafa, "Vetus", "rocky_ice", 25, 80, 1600, -425);
        vetus.getSpec().setTilt(40f);
        vetus.getSpec().setPlanetColor(new Color(120, 100, 40));
        vetus.applySpecChanges();
        vetus.setCustomDescriptionId("ii_planet_vetus");

        PlanetAPI mortalis = system.addPlanet("ii_mortalis", arafa, "Mortalis", "barren", 200, 90, 2200, -100);
        mortalis.getSpec().setPitch(-80f);
        mortalis.applySpecChanges();
        mortalis.setCustomDescriptionId("ii_planet_mortalis");

        system.addAsteroidBelt(star, 250, 8000, 2500, -240, -220, Terrain.ASTEROID_BELT, "Great Accretion Expanse");
        system.addAsteroidBelt(star, 250, 8500, 1500, -260, -240);
        system.addAsteroidBelt(star, 250, 7500, 1500, -220, -200);
        system.addRingBand(star, "misc", "ii_dusty_ring", 1024f, 0, new Color(255, 255, 255, 200), 2048f, 8000, -230);
        system.addRingBand(star, "misc", "ii_rocky_ring", 1024f, 0, new Color(255, 255, 255, 150), 2048f, 7500, -210);
        system.addRingBand(star, "misc", "ii_rocky_ring", 1024f, 0, new Color(255, 230, 200, 125), 2048f, 8500, -250);

        /* Wrecked stuff in the Great Accretion Expanse, bigger wrecks closer to Hades, none near Cydonia */
        int count = StarSystemGenerator.random.nextInt(10) + 10;
        for (int i = 0; i < count; i++) {
            float angle = StarSystemGenerator.random.nextFloat() * 360f;
            float scale = 1f - (MathUtils.getShortestRotation(angle, hadesAngle) / 180f);
            scale *= MathUtils.getShortestRotation(angle, cydoniaAngle) / 180f;
            if (scale <= 0.25f) {
                continue;
            }

            float amount = StarSystemGenerator.random.nextFloat() * (300f + 200f) * scale;
            float density = StarSystemGenerator.random.nextFloat() * 0.5f + 0.5f;
            params = new DebrisFieldParams(
                    amount / density, // field radius - should not go above 1000 for performance reasons
                    density / 3f, // density, visual - affects number of debris pieces
                    10000000f, // duration in days
                    0f); // days the field will keep generating glowing pieces
            params.source = DebrisFieldSource.MIXED;
            params.baseSalvageXP = 250; // base XP for scavenging in field
            debris = Misc.addDebrisField(system, params, StarSystemGenerator.random);
            debris.setDiscoverable(true);
            debris.setSensorProfile(amount);
            debris.setCircularOrbit(star, angle, StarSystemGenerator.random.nextFloat() * 1000f + 7500f, -230);

            if (StarSystemGenerator.random.nextFloat() > 0.85f) {
                int wreckCount = Math.round(amount / 200f);
                wreckCount += StarSystemGenerator.random.nextInt(2);
                for (int j = 0; j < wreckCount; j++) {
                    addDerelict(system, debris, factionPicker.pick(StarSystemGenerator.random),
                            StarSystemGenerator.random.nextFloat() * 150f + 50f, StarSystemGenerator.random);
                }
            }
        }

        JumpPointAPI jumpPoint = Global.getFactory().createJumpPoint("ii_corsicaGate1", "Corsica Gate Alpha");
        OrbitAPI orbit = Global.getFactory().createCircularOrbit(star, 30, 5000, -115);
        jumpPoint.setRelatedPlanet(aleria);
        jumpPoint.setOrbit(orbit);
        jumpPoint.setStandardWormholeToHyperspaceVisual();
        system.addEntity(jumpPoint);

        JumpPointAPI jumpPoint2 = Global.getFactory().createJumpPoint("ii_corsicaGate2", "Corsica Gate Beta");
        OrbitAPI orbit2 = Global.getFactory().createCircularOrbit(star, 325, 13000, -450);
        jumpPoint2.setRelatedPlanet(carthage);
        jumpPoint2.setOrbit(orbit2);
        jumpPoint2.setStandardWormholeToHyperspaceVisual();
        system.addEntity(jumpPoint2);

        SectorEntityToken relay = system.addCustomEntity("ii_corsica_relay", "Corsica Relay", "comm_relay",
                "interstellarimperium");
        relay.setCircularOrbit(star, 250, 6000, -150);

        SectorEntityToken outerNebula1 = system.addTerrain(Terrain.NEBULA, new BaseTiledTerrain.TileParams(
                "  x  x"
                + "x xx x"
                + "xxxxx "
                + " xxxxx"
                + "xx xx "
                + "     x",
                6, 6, // size of the nebula grid, should match above string
                "terrain", "nebula_blue", 4, 4, null));
        SectorEntityToken outerNebula2 = system.addTerrain(Terrain.NEBULA, new BaseTiledTerrain.TileParams(
                "  x   "
                + " xx x "
                + "x  xxx"
                + " xxxx "
                + "xxxx x"
                + "  x  x",
                6, 6, // size of the nebula grid, should match above string
                "terrain", "nebula_blue", 4, 4, null));
        SectorEntityToken outerNebula3 = system.addTerrain(Terrain.NEBULA, new BaseTiledTerrain.TileParams(
                " xx x "
                + " xx xx"
                + "x xxx "
                + " x  x "
                + "xx x x"
                + "x x xx",
                6, 6, // size of the nebula grid, should match above string
                "terrain", "nebula_blue", 4, 4, null));
        outerNebula1.setCircularOrbit(star, 205, 14500, -500);
        outerNebula2.setCircularOrbit(jumpPoint2, 0, 0, 100);
        outerNebula3.setCircularOrbit(star, 85, 14500, -500);

        SectorEntityToken nav_buoy = system.addCustomEntity(null, null, "nav_buoy", "interstellarimperium");
        nav_buoy.setCircularOrbitPointingDown(star, 60, 3000, 150);

        SectorEntityToken stable_location = system.addCustomEntity(null, null, "stable_location", Factions.NEUTRAL);
        stable_location.setCircularOrbitPointingDown(star, 200, 14000, 1500);

        StarSystemGenerator.addOrbitingEntities(system, star, StarAge.YOUNG,
                2, 2, // min/max entities to add
                15000, // radius to start adding at
                4, // name offset - next planet will be <system name> <roman numeral of this parameter + 1>
                true, // whether to use custom or system-name based names
                false); // habitable allowed

        system.autogenerateHyperspaceJumpPoints(true, true, true);

        MarketAPI aleriaMarket = addMarketplace("interstellarimperium", aleria,
                null,
                "Aleria", 5, // 3 industry limit
                new ArrayList<>(Arrays.asList(
                        Conditions.POPULATION_5,
                        Conditions.HOT,
                        Conditions.THIN_ATMOSPHERE,
                        Conditions.HIGH_GRAVITY,
                        Conditions.ORE_ULTRARICH,
                        Conditions.RARE_ORE_SPARSE)),
                new ArrayList<>(Arrays.asList(
                        new ArrayList<>(Arrays.asList(Industries.POPULATION, Commodities.ALPHA_CORE)),
                        new ArrayList<>(Arrays.asList(Industries.SPACEPORT)),
                        new ArrayList<>(Arrays.asList(Industries.GROUNDDEFENSES)),
                        new ArrayList<>(Arrays.asList(Industries.MILITARYBASE, Commodities.ALPHA_CORE)), // Industry
                        new ArrayList<>(Arrays.asList(Industries.MINING)), // Industry
                        new ArrayList<>(Arrays.asList(Industries.REFINING)), // Industry
                        new ArrayList<>(Arrays.asList("ii_battlestation", Commodities.ALPHA_CORE)))),
                new ArrayList<>(Arrays.asList(
                        Submarkets.SUBMARKET_BLACK,
                        Submarkets.SUBMARKET_OPEN,
                        Submarkets.SUBMARKET_STORAGE,
                        Submarkets.GENERIC_MILITARY)),
                0.3f,
                false
        );

        MarketAPI cydoniaMarket = addMarketplace(Factions.TRITACHYON, cydonia,
                new ArrayList<>(Arrays.asList(cydoniaStation)),
                "Cydonia", 5, // 3 industry limit
                new ArrayList<>(Arrays.asList(
                        Conditions.POPULATION_5,
                        Conditions.HIGH_GRAVITY,
                        Conditions.COLD,
                        Conditions.VOLATILES_ABUNDANT)),
                new ArrayList<>(Arrays.asList(
                        new ArrayList<>(Arrays.asList(Industries.POPULATION)),
                        new ArrayList<>(Arrays.asList(Industries.MEGAPORT)),
                        new ArrayList<>(Arrays.asList(Industries.MINING)), // Industry
                        new ArrayList<>(Arrays.asList(Industries.GROUNDDEFENSES)),
                        new ArrayList<>(Arrays.asList(Industries.FUELPROD)), // Industry
                        new ArrayList<>(Arrays.asList(Industries.MILITARYBASE)), // Industry
                        new ArrayList<>(Arrays.asList(Industries.WAYSTATION)),
                        new ArrayList<>(Arrays.asList(Industries.BATTLESTATION_HIGH)))),
                new ArrayList<>(Arrays.asList(
                        Submarkets.SUBMARKET_BLACK,
                        Submarkets.SUBMARKET_OPEN,
                        Submarkets.SUBMARKET_STORAGE,
                        Submarkets.GENERIC_MILITARY)),
                0.3f,
                false
        );

        MarketAPI carthageMarket = addMarketplace(Factions.HEGEMONY, carthage,
                new ArrayList<>(Arrays.asList(carthageStation)),
                "Carthage", 5, // 3 industry limit
                new ArrayList<>(Arrays.asList(
                        Conditions.POPULATION_5,
                        Conditions.HABITABLE,
                        Conditions.FARMLAND_POOR,
                        Conditions.HOT,
                        Conditions.POOR_LIGHT,
                        Conditions.ORE_SPARSE)),
                new ArrayList<>(Arrays.asList(
                        new ArrayList<>(Arrays.asList(Industries.POPULATION)),
                        new ArrayList<>(Arrays.asList(Industries.SPACEPORT)),
                        new ArrayList<>(Arrays.asList(Industries.FARMING)), // Industry
                        new ArrayList<>(Arrays.asList(Industries.HEAVYBATTERIES)),
                        new ArrayList<>(Arrays.asList(Industries.LIGHTINDUSTRY)), // Industry
                        new ArrayList<>(Arrays.asList(Industries.MILITARYBASE)), // Industry
                        new ArrayList<>(Arrays.asList(Industries.BATTLESTATION)))),
                new ArrayList<>(Arrays.asList(
                        Submarkets.SUBMARKET_BLACK,
                        Submarkets.SUBMARKET_OPEN,
                        Submarkets.GENERIC_MILITARY,
                        Submarkets.SUBMARKET_STORAGE)),
                0.3f,
                false
        );

        MarketAPI hadesMarket = addMarketplace(Factions.PIRATES, hades,
                null,
                "Hades", 3, // 1 industry limit
                new ArrayList<>(Arrays.asList(
                        Conditions.POPULATION_3)),
                new ArrayList<>(Arrays.asList(
                        new ArrayList<>(Arrays.asList(Industries.POPULATION)),
                        new ArrayList<>(Arrays.asList(Industries.SPACEPORT)),
                        new ArrayList<>(Arrays.asList("commerce")), // Industry
                        new ArrayList<>(Arrays.asList(Industries.GROUNDDEFENSES)),
                        new ArrayList<>(Arrays.asList(Industries.ORBITALSTATION)))),
                new ArrayList<>(Arrays.asList(
                        Submarkets.SUBMARKET_BLACK,
                        Submarkets.SUBMARKET_OPEN,
                        Submarkets.SUBMARKET_STORAGE)),
                0.3f,
                true
        );

        HyperspaceTerrainPlugin plugin = (HyperspaceTerrainPlugin) Misc.getHyperspaceTerrain().getPlugin();
        NebulaEditor editor = new NebulaEditor(plugin);

        float minRadius = plugin.getTileSize() * 2f;
        float radius = system.getMaxRadiusInHyperspace();
        editor.clearArc(system.getLocation().x, system.getLocation().y, 0, radius + minRadius * 0.5f, 0, 360f);
        editor.clearArc(system.getLocation().x, system.getLocation().y, 0, radius + minRadius, 0, 360f, 0.25f);
    }
}
