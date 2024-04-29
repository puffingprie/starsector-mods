package data.scripts.world.imperium;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignTerrainAPI;
import com.fs.starfarer.api.campaign.JumpPointAPI;
import com.fs.starfarer.api.campaign.JumpPointAPI.JumpDestination;
import com.fs.starfarer.api.campaign.OrbitAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.impl.campaign.ids.StarTypes;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.ids.Terrain;
import com.fs.starfarer.api.impl.campaign.procgen.DefenderDataOverride;
import com.fs.starfarer.api.impl.campaign.procgen.NebulaEditor;
import com.fs.starfarer.api.impl.campaign.procgen.StarAge;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.DerelictThemeGenerator;
import com.fs.starfarer.api.impl.campaign.terrain.BaseTiledTerrain;
import com.fs.starfarer.api.impl.campaign.terrain.DebrisFieldTerrainPlugin.DebrisFieldParams;
import com.fs.starfarer.api.impl.campaign.terrain.DebrisFieldTerrainPlugin.DebrisFieldSource;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.MagneticFieldTerrainPlugin.MagneticFieldParams;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import data.scripts.campaign.terrain.II_ChargedNebulaTerrainPlugin;
import static data.scripts.world.imperium.II_Thracia.addMarketplace;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import javax.imageio.ImageIO;
import org.lwjgl.util.vector.Vector2f;

public class II_Ex_Vis {

    private static final WeightedRandomPicker<String> factionPicker = new WeightedRandomPicker<>();

    static {
        factionPicker.add(Factions.LUDDIC_CHURCH, 4f);
        factionPicker.add(Factions.INDEPENDENT, 6f);
        factionPicker.add("interstellarimperium", 6f * 0.5f);
    }

    public static SectorEntityToken getNovaCapitalis() {
        if (Global.getSector().getStarSystem("Ex Vis") == null) {
            return null;
        }
        return Global.getSector().getStarSystem("Ex Vis").getEntityById("ii_nova_capitalis");
    }

    public static void removePNGFromNebula(String image, CampaignTerrainAPI terrain) {
        BufferedImage img;
        try {
            img = ImageIO.read(Global.getSettings().openStream(image));
        } catch (IOException ex) {
            return;
        }

        int w = img.getWidth();
        int h = img.getHeight();
        Raster data = img.getData();

        int chunkWidth = w;
        int chunkHeight = h;

        int[][] tiles = ((BaseTiledTerrain) terrain.getPlugin()).getTiles();

        StringBuilder string = new StringBuilder(chunkHeight * chunkWidth);
        for (int y = chunkHeight - 1; y >= 0; y--) {
            for (int x = 0; x < chunkWidth; x++) {
                int[] pixel = data.getPixel(x, h - y - 1, (int[]) null);
                int total = pixel[0] + pixel[1] + pixel[2];
                if (total > 0) {
                    string.append("x");
                } else {
                    string.append(" ");
                }
            }
        }

        for (int i = 0; i < tiles.length; i++) {
            for (int j = 0; j < tiles[0].length; j++) {
                int index = i + (tiles[0].length - j - 1) * tiles.length;
                char c = string.charAt(index);
                if (Character.isWhitespace(c)) {
                    tiles[i][j] = -1;
                }
            }
        }
    }

    public static CampaignTerrainAPI getChargedNebulaTerrain() {
        if (Global.getSector().getStarSystem("Ex Vis") == null) {
            return null;
        }
        for (CampaignTerrainAPI curr : Global.getSector().getStarSystem("Ex Vis").getTerrainCopy()) {
            if (curr.getPlugin() instanceof II_ChargedNebulaTerrainPlugin) {
                return curr;
            }
        }
        return null;
    }

    public void generate(SectorAPI sector) {
        StarSystemAPI system = sector.createStarSystem("Ex Vis");
        system.getLocation().set(-18500, 5800);
        system.addTag(Tags.THEME_CORE_POPULATED);
        system.addTag(Tags.THEME_CORE);

        system.setBackgroundTextureFilename("graphics/imperium/backgrounds/ii_ex_vis.png");

        SectorEntityToken chargedNebula = Misc.addNebulaFromPNG("data/campaign/terrain/ii_ex_vis_charged_nebula.png",
                0, 0,
                system,
                "terrain", "ii_chargednebula",
                4, 4, "ii_chargednebula", StarAge.OLD);
        chargedNebula.addTag("radar_nebula");

        Misc.addNebulaFromPNG("data/campaign/terrain/ii_ex_vis_nebula.png",
                0, 0, // center of nebula
                system, // location to add to
                "terrain", "ii_ex_vis_nebula",
                4, 4, StarAge.OLD); // number of cells in texture

        SectorEntityToken barycenter = system.addCustomEntity("ii_barycenter", null, "ii_barycenter", null);

        SectorEntityToken exVisGate = system.addCustomEntity("ii_ex_vis_gate", // unique id
                "Ex Vis Gate", // name - if null, defaultName from custom_entities.json will be used
                "ii_ex_vis_gate", // type of object, defined in custom_entities.json
                null); // faction
        exVisGate.getLocation().set(0f, 0f);
        exVisGate.setCustomDescriptionId("ii_ex_vis_gate");

        MagneticFieldParams mparams = new MagneticFieldParams(1000f, // terrain effect band width
                1500f, // terrain effect middle radius
                exVisGate, // entity that it's around
                1000f, // visual band start
                2000f, // visual band end
                new Color(85, 10, 140, 100),
                1f,
                new Color(100, 70, 255),
                new Color(230, 75, 255),
                new Color(255, 75, 130));
        mparams.name = "Powerful Magnetic Field";
        SectorEntityToken exVisIonField = system.addTerrain(Terrain.MAGNETIC_FIELD, mparams);
        exVisIonField.setCircularOrbit(exVisGate, 0, 0, 150);

//        TachyonFieldParams tachParams = new TachyonFieldParams(24000f, // terrain effect band width
//                                                               14000f, // terrain effect middle radius
//                                                               exVisGate // entity that it's around
//                   );
//        SectorEntityToken exVisTachyonField = system.addTerrain("ii_tachyonfield", tachParams);
//        exVisTachyonField.setCircularOrbit(exVisGate, 0, 0, 150);
        system.addAsteroidBelt(exVisGate, 500, 3600, 1200, -150, -130, Terrain.ASTEROID_BELT, "Intermediary Field");
        system.addRingBand(exVisGate, "misc", "ii_rocky_ring", 1024f, 0, new Color(255, 220, 220, 200), 1700f, 3600,
                -140);

        SectorEntityToken vortexLabel = system.addCustomEntity("ii_vortex_label", null, "ii_vortex_label", null);
        vortexLabel.getLocation().set(0f, 3000f);

        SectorEntityToken barycenter2 = system.addCustomEntity("ii_barycenter2", null, "ii_barycenter", null);
        barycenter2.setCircularOrbit(barycenter, 50, 7250, 800);

        SectorEntityToken barycenter3 = system.addCustomEntity("ii_barycenter3", null, "ii_barycenter", null);
        barycenter3.setCircularOrbit(barycenter, 230, 15000, 800);

        PlanetAPI star1 = system.initStar("ii_ex_vis", StarTypes.YELLOW, 800f, 500f, 10, 0.5f, 3f);
        star1.setName("Ex Vis");
        star1.getSpec().setTexture(Global.getSettings().getSpriteName("stars", "ii_yellow_star"));
        star1.getSpec().setPlanetColor(new Color(255, 255, 255));
        star1.getSpec().setCloudTexture(Global.getSettings().getSpriteName("stars", "ii_star_clouds"));
        star1.getSpec().setCloudRotation(4f);
        star1.getSpec().setCloudColor(new Color(255, 255, 255));
        star1.applySpecChanges();
        star1.setCircularOrbit(barycenter2, 0, 1000, 30);

        PlanetAPI star2 = system.addPlanet("ii_ex_vis_secundo", barycenter2, "Ex Vis Secundo", StarTypes.ORANGE, 140,
                650, 1750, 30);
        star2.getSpec().setTexture(Global.getSettings().getSpriteName("stars", "ii_orange_star"));
        star2.getSpec().setPlanetColor(new Color(255, 255, 255));
        star2.getSpec().setCloudTexture(Global.getSettings().getSpriteName("stars", "ii_star_clouds"));
        star2.getSpec().setCloudRotation(2f);
        star2.getSpec().setCloudColor(new Color(255, 125, 90));
        star2.applySpecChanges();
        system.addCorona(star2, 400f, 10, 0.25f, 3f);

        PlanetAPI star3 = system.addPlanet("ii_ex_vis_tertius", barycenter3, "Ex Vis Tertius", StarTypes.RED_DWARF, 270,
                500, 1200, 110);
        star3.getSpec().setTexture(Global.getSettings().getSpriteName("stars", "ii_red_star"));
        star3.getSpec().setPlanetColor(new Color(255, 255, 255));
        star3.getSpec().setCloudTexture(Global.getSettings().getSpriteName("stars", "ii_star_clouds"));
        star3.getSpec().setCloudRotation(6f);
        star3.getSpec().setCloudColor(new Color(255, 200, 160));
        star3.applySpecChanges();
        system.addCorona(star3, 350f, 5, 0.3f, 2f);

        PlanetAPI star4
                = system.addPlanet("ii_ex_vis_quartus", barycenter3, "Ex Vis Quartus", StarTypes.WHITE_DWARF, 90, 400,
                        3000, 110);
        star4.getSpec().setTexture(Global.getSettings().getSpriteName("stars", "ii_white_star"));
        star4.getSpec().setPlanetColor(new Color(255, 255, 255));
        star4.applySpecChanges();
        system.addCorona(star4, 300f, 5, 0.1f, 2f);

        system.setCenter(barycenter);
        system.setSecondary(star1);
        system.setTertiary(star2);

        PlanetAPI novaCapitalis = system.addPlanet("ii_nova_capitalis", barycenter, "Nova Capitalis", "ii_auric", 120,
                100, 500, -20);
        novaCapitalis.getSpec().setGlowTexture(Global.getSettings().getSpriteName("hab_glows", "sindria"));
        novaCapitalis.getSpec().setUseReverseLightForGlow(true);
        novaCapitalis.applySpecChanges();
        novaCapitalis.setCustomDescriptionId("ii_planet_nova_capitalis");

        /* Wrecked stuff in the literal hellhole of terrain outside Nova Capitalis */
        int count = StarSystemGenerator.random.nextInt(5) + 5;
        for (int i = 0; i < count; i++) {
            float amount = StarSystemGenerator.random.nextFloat() * 200f + 100f;
            float density = StarSystemGenerator.random.nextFloat() * 0.5f + 0.75f;
            DebrisFieldParams dparams = new DebrisFieldParams(
                    amount / density, // field radius - should not go above 1000 for performance reasons
                    density / 3f, // density, visual - affects number of debris pieces
                    10000000f, // duration in days
                    0f); // days the field will keep generating glowing pieces
            dparams.source = DebrisFieldSource.MIXED;
            dparams.baseSalvageXP = 500; // base XP for scavenging in field
            SectorEntityToken debris = Misc.addDebrisField(system, dparams, StarSystemGenerator.random);
            debris.setDiscoverable(true);
            debris.setSensorProfile(amount);
            debris.setCircularOrbit(barycenter, StarSystemGenerator.random.nextFloat() * 360f,
                    StarSystemGenerator.random.nextFloat() * 1000f + 3000f, 6000);

            if (StarSystemGenerator.random.nextFloat() > 0.8f) {
                int wreckCount = Math.round(amount / 200f);
                wreckCount += StarSystemGenerator.random.nextInt(2);
                for (int j = 0; j < wreckCount; j++) {
                    II_Corsica.addDerelict(system, debris, factionPicker.pick(StarSystemGenerator.random),
                            StarSystemGenerator.random.nextFloat() * 100f + 40f, StarSystemGenerator.random);
                }
            }
        }

        PlanetAPI sepulchrum = system.addPlanet("ii_sepulchrum", barycenter2, "Sepulchrum", "ii_irradiated-bombarded",
                190, 140, 3750, 100);
        sepulchrum.setCustomDescriptionId("ii_planet_sepulchrum");

        PlanetAPI remotum = system.addPlanet("ii_remotum", barycenter2, "Remotum", "rocky_ice", 350, 80, 12000, 1300);
        remotum.getSpec().setAtmosphereThickness(0);
        remotum.getSpec().setRotation(-3f);
        remotum.getSpec().setPitch(-12f);
        remotum.applySpecChanges();
        remotum.setCustomDescriptionId("ii_planet_remotum");

        PlanetAPI pulvis = system.addPlanet("ii_pulvis", barycenter3, "Pulvis", "desert", 220, 175, 5000, 300);
        pulvis.getSpec().setTexture(Global.getSettings().getSpriteName("planets", "ii_pulvis"));
        pulvis.getSpec().setCloudTexture(Global.getSettings().getSpriteName("planets", "ii_pulvis_clouds"));
        pulvis.getSpec().setAtmosphereColor(new Color(170, 170, 140, 200));
        pulvis.getSpec().setRotation(-1f);
        pulvis.getSpec().setPitch(33f);
        pulvis.applySpecChanges();
        pulvis.setCustomDescriptionId("ii_planet_pulvis");

        PlanetAPI labes = system.addPlanet("ii_labes", pulvis, "Labes", "toxic", 45, 70, 350, 15);
        labes.getSpec().setTexture(Global.getSettings().getSpriteName("planets", "ii_labes"));
        labes.getSpec().setRotation(6f);
        labes.getSpec().setTilt(15f);
        labes.applySpecChanges();
        labes.setCustomDescriptionId("ii_planet_labes");

        system.addRingBand(pulvis, "misc", "rings_ice0", 256f, 1, Color.white, 128f, 500, 25f, Terrain.RING, null);

        PlanetAPI saltusDivinus
                = system.addPlanet("ii_saltus_divinus", pulvis, "Saltus Divinus", "jungle", 300, 60, 800, 50);
        saltusDivinus.getSpec().setAtmosphereColor(new Color(60, 70, 140, 255));
        saltusDivinus.getSpec().setAtmosphereThickness(0.5f);
        saltusDivinus.getSpec().setCloudTexture(
                Global.getSettings().getSpriteName("planets", "ii_saltus_divinus_clouds"));
        saltusDivinus.getSpec().setRotation(3f);
        saltusDivinus.getSpec().setTilt(5f);
        saltusDivinus.applySpecChanges();
        saltusDivinus.setCustomDescriptionId("ii_planet_saltus_divinus");
        saltusDivinus.setInteractionImage("illustrations", "luddic_shrine");

        SectorEntityToken dualityResearchStation = system.addCustomEntity("ii_duality_station",
                "Duality Research Station", "station_side04",
                "neutral");
        dualityResearchStation.setCircularOrbitPointingDown(barycenter3, 0, 1, -55);
        Misc.setAbandonedStationMarket("ii_duality_station", dualityResearchStation);
        dualityResearchStation.setInteractionImage("illustrations", "orbital_construction");
        dualityResearchStation.setCustomDescriptionId("ii_station_duality");

        SectorEntityToken dualityShade1 = system.addCustomEntity("ii_duality_shade1", null, "stellar_shade", "neutral");
        dualityShade1.setCircularOrbitPointingDown(barycenter3, 90, 150, 110);
        dualityShade1.setCustomDescriptionId("stellar_shade");

        SectorEntityToken dualityShade2 = system.addCustomEntity("ii_duality_shade2", null, "stellar_shade", "neutral");
        dualityShade2.setCircularOrbitPointingDown(barycenter3, 270, 150, 110);
        dualityShade2.setCustomDescriptionId("stellar_shade");

        /* Duality's sister station - now defunct */
        SectorEntityToken researchStation = DerelictThemeGenerator.addSalvageEntity(system, Entities.STATION_RESEARCH,
                Factions.DERELICT);
        researchStation.setCircularOrbit(barycenter2, 125, 1, 20);
        Misc.setDefenderOverride(researchStation, new DefenderDataOverride(Factions.DERELICT, 1f, 6, 10));
        researchStation.setSensorProfile(100f);
        researchStation.setDiscoverable(true);

        SectorEntityToken relay = system.addCustomEntity("ii_ex_vis_relay", "Imperial Relay", "comm_relay",
                "interstellarimperium");
        relay.setCircularOrbit(barycenter2, 250, 3750, 100);

        SectorEntityToken bary2_location = system.addCustomEntity(null, null, "stable_location", Factions.NEUTRAL);
        bary2_location.setCircularOrbitPointingDown(barycenter3, 280, 5000, 300);

        SectorEntityToken bary_sensor = system.addCustomEntity(null, null, "sensor_array", "interstellarimperium");
        bary_sensor.setCircularOrbitPointingDown(barycenter, 180, 500, -20);

        JumpPointAPI pointCaelestis = Global.getFactory().createJumpPoint("ii_ex_vis_point_caelestis",
                "Jump-point Caelestis");
        OrbitAPI orbit = Global.getFactory().createCircularOrbit(barycenter3, 160, 5000, 300);
        pointCaelestis.setOrbit(orbit);
        pointCaelestis.setRelatedPlanet(saltusDivinus);
        pointCaelestis.setStandardWormholeToHyperspaceVisual();
        system.addEntity(pointCaelestis);

        system.autogenerateHyperspaceJumpPoints(true, true, true);

        for (SectorEntityToken point : system.getEntitiesWithTag(Tags.JUMP_POINT)) {
            if (point != pointCaelestis) {
                orbit = Global.getFactory().createCircularOrbit(barycenter, 290, 15000, 800);
                point.setOrbit(orbit);
            }
        }

        JumpPointAPI pointInfernalis = Global.getFactory().createJumpPoint("ii_ex_vis_point_infernalis",
                "Jump-point Infernalis");
        pointInfernalis.setStandardWormholeToStarOrPlanetVisual(novaCapitalis);
        JumpDestination dest = new JumpDestination(exVisGate, "Point Infernalis");
        dest.setMinDistFromToken(300f);
        dest.setMaxDistFromToken(600f);
        pointInfernalis.addDestination(dest);
        sector.getHyperspace().addEntity(pointInfernalis);
        pointInfernalis.getLocation().set(system.getLocation());
        sector.getHyperspace().addScript(new AnchorToStarSystem(pointInfernalis, barycenter, star1));

        JumpPointAPI pointOrbital = Global.getFactory().createJumpPoint("ii_ex_vis_orbital", "Ex Vis Orbital");
        orbit = Global.getFactory().createCircularOrbit(barycenter, 150, 4750, -170);
        pointOrbital.setOrbit(orbit);
        pointOrbital.setStandardWormholeToHyperspaceVisual();
        dest = new JumpDestination(pointInfernalis, "hyperspace");
        dest.setMinDistFromToken(300f);
        dest.setMaxDistFromToken(400f);
        pointOrbital.addDestination(dest);
        system.addEntity(pointOrbital);

        addMarketplace("interstellarimperium", novaCapitalis,
                null,
                "Nova Capitalis", 6, // 3 industry limit
                new ArrayList<>(Arrays.asList(
                        Conditions.POPULATION_6,
                        "ii_imperialdoctrine",
                        "ii_hostile_terrain",
                        Conditions.POOR_LIGHT,
                        Conditions.TOXIC_ATMOSPHERE,
                        Conditions.ORE_ABUNDANT,
                        Conditions.RARE_ORE_ULTRARICH)),
                new ArrayList<>(Arrays.asList(
                        new ArrayList<>(Arrays.asList(Industries.POPULATION)),
                        new ArrayList<>(Arrays.asList(Industries.MEGAPORT, Commodities.ALPHA_CORE)),
                        new ArrayList<>(Arrays.asList(Industries.HEAVYBATTERIES)),
                        new ArrayList<>(Arrays.asList(Industries.MILITARYBASE)), // Industry
                        new ArrayList<>(Arrays.asList(Industries.PLANETARYSHIELD, Commodities.ALPHA_CORE)),
                        new ArrayList<>(Arrays.asList(Industries.ORBITALWORKS, Items.PRISTINE_NANOFORGE, Commodities.ALPHA_CORE)), // Industry
                        new ArrayList<>(Arrays.asList(Industries.MINING)), // Industry
                        new ArrayList<>(Arrays.asList("ii_starfortress", Commodities.ALPHA_CORE)))),
                new ArrayList<>(Arrays.asList(
                        Submarkets.SUBMARKET_BLACK,
                        Submarkets.SUBMARKET_OPEN,
                        Submarkets.SUBMARKET_STORAGE,
                        Submarkets.GENERIC_MILITARY)),
                0.3f,
                false
        );

        addMarketplace(Factions.LUDDIC_CHURCH, saltusDivinus,
                null,
                "Saltus Divinus", 5, // 3 industry limit
                new ArrayList<>(Arrays.asList(
                        Conditions.POPULATION_5,
                        Conditions.HABITABLE,
                        Conditions.HOT,
                        Conditions.FARMLAND_RICH,
                        Conditions.ORGANICS_ABUNDANT,
                        Conditions.ORE_ABUNDANT,
                        Conditions.RUINS_WIDESPREAD,
                        Conditions.LUDDIC_MAJORITY)),
                new ArrayList<>(Arrays.asList(
                        new ArrayList<>(Arrays.asList(Industries.POPULATION)),
                        new ArrayList<>(Arrays.asList(Industries.SPACEPORT)),
                        new ArrayList<>(Arrays.asList(Industries.FARMING)), // Industry
                        new ArrayList<>(Arrays.asList(Industries.HEAVYBATTERIES)),
                        new ArrayList<>(Arrays.asList(Industries.STARFORTRESS)),
                        new ArrayList<>(Arrays.asList(Industries.MILITARYBASE)))), // Industry
                new ArrayList<>(Arrays.asList(
                        Submarkets.SUBMARKET_BLACK,
                        Submarkets.SUBMARKET_OPEN,
                        Submarkets.SUBMARKET_STORAGE,
                        Submarkets.GENERIC_MILITARY)),
                0.3f,
                false
        );

        HyperspaceTerrainPlugin plugin = (HyperspaceTerrainPlugin) Misc.getHyperspaceTerrain().getPlugin();
        NebulaEditor editor = new NebulaEditor(plugin);

        float minRadius = plugin.getTileSize() * 2f;
        float radius = system.getMaxRadiusInHyperspace();
        editor.clearArc(system.getLocation().x, system.getLocation().y, 0, radius + minRadius * 0.5f, 0, 360f);
        editor.clearArc(system.getLocation().x, system.getLocation().y, 0, radius + minRadius, 0, 360f, 0.25f);
    }

    public static class AnchorToStarSystem implements EveryFrameScript {

        private final SectorEntityToken baseEntity;
        private final SectorEntityToken entity;
        private final SectorEntityToken relativeEntity;
        private final StarSystemAPI system;
        private final Vector2f temp = new Vector2f();

        AnchorToStarSystem(SectorEntityToken entity, SectorEntityToken relativeEntity, SectorEntityToken baseEntity) {
            this.entity = entity;
            this.relativeEntity = relativeEntity;
            this.baseEntity = baseEntity;
            system = (StarSystemAPI) baseEntity.getContainingLocation();
        }

        @Override
        public void advance(float amount) {
            Vector2f diff = Vector2f.sub(relativeEntity.getLocation(), baseEntity.getLocation(), temp);
            diff.scale(0.05f);
            Vector2f.add(diff, system.getLocation(), diff);
            entity.getLocation().set(diff);
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
