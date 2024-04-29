package data.scripts.world.systems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.ShipRolePick;
import com.fs.starfarer.api.impl.campaign.events.OfficerManagerEvent;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.procgen.*;
import com.fs.starfarer.api.impl.campaign.procgen.themes.DerelictThemeGenerator;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageGenFromSeed;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;
import java.util.Random;

public class HMI_seele implements SectorGeneratorPlugin {

//    public static SectorEntityToken getSectorAccess() {
//        return Global.getSector().getStarSystem("Seele").getEntityById("hmi_seele_star");
//    }
    Random characterSaveSeed = StarSystemGenerator.random;
    Random random = new Random(characterSaveSeed.nextLong());
    public static String HMINIGHTMARE_KEY = "$hmi_nightmarestation";

    public void generate(SectorAPI sector) {


        String[] strings = {"Nerve", "Katsuragi", "Ikari", "Sorhyu", "Longinus"};
        int nameSelector = random.nextInt(strings.length);      // random name selector

        LocationAPI hyper = Global.getSector().getHyperspace();

        Constellation hmi_constellation_Seele = new Constellation(
                Constellation.ConstellationType.NORMAL, StarAge.OLD
        );

        NameGenData data = new NameGenData("null", "null");
        ProcgenUsedNames.NamePick Mansaconstname = new ProcgenUsedNames.NamePick(data, strings[nameSelector], "null");
        hmi_constellation_Seele.setNamePick(Mansaconstname);
        StarSystemAPI system = sector.createStarSystem("Seele");
        StarSystemAPI system2 = sector.createStarSystem("Wille");
        StarSystemAPI system3 = sector.createStarSystem("Leiche");

        hmi_constellation_Seele.getSystems().add(sector.getStarSystem("Seele"));
        hmi_constellation_Seele.getSystems().add(sector.getStarSystem("Wille"));
        hmi_constellation_Seele.getSystems().add(sector.getStarSystem("Leiche"));

        sector.getStarSystem("Seele").setConstellation(hmi_constellation_Seele);
        sector.getStarSystem("Wille").setConstellation(hmi_constellation_Seele);
        sector.getStarSystem("Leiche").setConstellation(hmi_constellation_Seele);

        system.setBackgroundTextureFilename("graphics/backgrounds/mercy.jpg");


        // create the star and generate the hyperspace anchor for this system
        PlanetAPI star = system.initStar("hmi_seele_star", // unique id for this star
                "star_red_dwarf", // id in planets.json
                250f,
                500,
                3f, // solar wind burn level
                0.7f, // flare probability
                2.0f); // CR loss multiplier, good values are in the range of 1-5); // extent of corona outside star)
        system.setLightColor(new Color(255, 90, 0)); // light color in entire system, affects all entities
        system.getLocation().set(-71000, -23000);
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


        //3000
        PlanetAPI seele1 = system.addPlanet("hmi_kiel", star, "Kiel", "irradiated", 245, 200, 4500, 200);

        seele1.getMarket().addCondition(Conditions.VERY_COLD);
        seele1.getMarket().addCondition(Conditions.NO_ATMOSPHERE);
        seele1.getMarket().addCondition(Conditions.DARK);
        seele1.getMarket().addCondition(Conditions.LOW_GRAVITY);
        seele1.getMarket().addCondition(Conditions.IRRADIATED);

        system.addRingBand(seele1, "misc", "rings_dust0", 256f, 0, new Color(0, 0, 0, 0), 64f, 666, 175f, Terrain.RING, "...it...hurts...");
        system.addRingBand(seele1, "misc", "rings_dust0", 256f, 0, new Color(0, 0, 0, 0), 64f, 1288, 175f, Terrain.RING, "...the...pain...");
        system.addRingBand(seele1, "misc", "rings_dust0", 256f, 0, new Color(0, 0, 0, 0), 64f, 1678, 175f, Terrain.RING, "...away...");
        system.addRingBand(seele1, "misc", "rings_dust0", 256f, 0, new Color(0, 0, 0, 0), 64f, 2265, 175f, Terrain.RING, "...get...");
        system.addRingBand(seele1, "misc", "rings_dust0", 256f, 0, new Color(0, 0, 0, 0), 64f, 2525, 175f, Terrain.RING, "...forgot...how...");
        system.addRingBand(seele1, "misc", "rings_dust0", 256f, 0, new Color(0, 0, 0, 0), 64f, 3289, 175f, Terrain.RING, "...help...");

        SectorEntityToken stationNightmare = DerelictThemeGenerator.addSalvageEntity(system, "hmi_station_nightmare", Factions.NEUTRAL);
        stationNightmare.getMemoryWithoutUpdate().set(HMINIGHTMARE_KEY, true);
        stationNightmare.setCircularOrbit(seele1, 100, 600, 65f);
        Misc.setDefenderOverride(stationNightmare, new DefenderDataOverride("hmi_nightmare", 1f, 100, 400, 10));



        //CargoAPI extraStationSalvage = Global.getFactory().createCargo(true);
        //extraStationSalvage.addSpecial(new SpecialItemData("industry_bp", "HMI_mindcontrol"), 1);
        //extraStationSalvage.addSpecial(new SpecialItemData("modspec", "hmi_subliminal"), 1);


        //SalvageEntityGenDataSpec.DropData d = new SalvageEntityGenDataSpec.DropData();
        //d.chances = 5;
        //d.group = "blueprints";
        //stationNightmare.addDropRandom(d);

        //d = new SalvageEntityGenDataSpec.DropData();
        //d.chances = 1;
        //d.group = "rare_tech";
        //stationNightmare.addDropRandom(d);

        //d = new SalvageEntityGenDataSpec.DropData();
        //d.chances = 800;
        //d.group = "basic";
        //stationNightmare.addDropRandom(d);

        //d = new SalvageEntityGenDataSpec.DropData();
        //d.chances = 2;
        //d.group = "any_hullmod_high";
        //stationNightmare.addDropRandom(d);

        //d = new SalvageEntityGenDataSpec.DropData();
        //d.chances = 2;
        //d.group = "blueprints_low";
        //stationNightmare.addDropRandom(d);

        //d = new SalvageEntityGenDataSpec.DropData();
        //d.chances = 2;
        //d.group = "blueprints_guaranteed";
        //stationNightmare.addDropRandom(d);

        float radiusAfter = StarSystemGenerator.addOrbitingEntities(system, star, StarAge.OLD,
                2, 4, // min/max entities to add
                7000, // radius to start adding at
                1, // name offset - next planet will be <system name> <roman numeral of this parameter + 1>
                false);
        //true); // whether to use custom or system-name based names
        system.autogenerateHyperspaceJumpPoints(true, true);
        cleanup(system);

        ///WILLE, SYSTEM2


        system2.setBackgroundTextureFilename("graphics/backgrounds/mercy.jpg");

        // create the star and generate the hyperspace anchor for this system
        PlanetAPI star2 = system2.initStar("hmi_wille_star", // unique id for this star
                "star_red_dwarf", // id in planets.json
                150f, 		  // radius (in pixels at default zoom)
                300, // corona
                4f, // solar wind burn level
                0.7f, // flare probability
                2.0f); // CR loss multiplier, good values are in the range of 1-5
        system2.setLightColor(new Color(225, 100, 100)); // light color in entire system, affects all entities
        system2.getLocation().set(-68500, -26000);

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

        float radiusAfterWille = StarSystemGenerator.addOrbitingEntities(system2, star2, StarAge.OLD,
                1, 2, // min/max entities to add
                7000, // radius to start adding at
                0, // name offset - next planet will be <system name> <roman numeral of this parameter + 1>
                true, // whether to use custom or system-name based names
                false); // whether to allow habitable worlds

        //JUMP POINT
        JumpPointAPI jumpPointWille = Global.getFactory().createJumpPoint("wille_jumpPointA", "Wille Inner Jump-Point");
        OrbitAPI orbitWille = Global.getFactory().createCircularOrbit(star2, 5000, 4000, 300);
        jumpPointWille.setOrbit(orbitWille);
        jumpPointWille.setStandardWormholeToHyperspaceVisual();
        system2.addEntity(jumpPointWille);

        system2.autogenerateHyperspaceJumpPoints(true, true);

        cleanup(system2);

        ///LEICHE, SYSTEM3
        system3.setBackgroundTextureFilename("graphics/backgrounds/mercy.jpg");

        // create the star and generate the hyperspace anchor for this system
        PlanetAPI star3 = system3.initStar("hmi_leiche_star", // unique id for this star
                "star_red_dwarf", // id in planets.json
                225f, 		  // radius (in pixels at default zoom)
                600, // corona
                6f, // solar wind burn level
                0.7f, // flare probability
                2.0f); // CR loss multiplier, good values are in the range of 1-5
        system3.setLightColor(new Color(225, 100, 100)); // light color in entire system, affects all entities
        system3.getLocation().set(-71000, -21700);

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

        float radiusAfterLieche = StarSystemGenerator.addOrbitingEntities(system3, star3, StarAge.OLD,
                1, 3, // min/max entities to add
                6000, // radius to start adding at
                0, // name offset - next planet will be <system name> <roman numeral of this parameter + 1>
                true, // whether to use custom or system-name based names
                false); // whether to allow habitable worlds

        //JUMP POINT
        JumpPointAPI jumpPointLieche = Global.getFactory().createJumpPoint("lieche_jumpPointA", "Leiche Inner Jump-Point");
        OrbitAPI orbitLieche = Global.getFactory().createCircularOrbit(star3, 4000, 4000, 300);
        jumpPointLieche.setOrbit(orbitLieche);
        jumpPointLieche.setStandardWormholeToHyperspaceVisual();
        system3.addEntity(jumpPointLieche);

        system3.autogenerateHyperspaceJumpPoints(true, true);

        cleanup(system3);

    }

    void cleanup(StarSystemAPI system) {
        HyperspaceTerrainPlugin plugin = (HyperspaceTerrainPlugin) Misc.getHyperspaceTerrain().getPlugin();
        NebulaEditor editor = new NebulaEditor(plugin);
        float minRadius = plugin.getTileSize() * 2f;

        float radius = system.getMaxRadiusInHyperspace();
        editor.clearArc(system.getLocation().x, system.getLocation().y, 0, radius + minRadius * 0.5f, 0, 360f);
        editor.clearArc(system.getLocation().x, system.getLocation().y, 0, radius + minRadius, 0, 360f, 0.25f);
    }
}

