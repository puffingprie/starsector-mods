package data.world.systems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.MusicPlayerPluginImpl;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.procgen.StarAge;
import com.fs.starfarer.api.util.Misc;
import data.utils.I18nUtil;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

import static data.world.FMGen.addMarketplace;

public class FM_Gensokyo {
    public void generate(SectorAPI sector) {
        //create a star system
        StarSystemAPI system = sector.createStarSystem("Gensokyo");
        //set its location
        system.getLocation().set(4200f, 10000f);
        //set background image
        system.setBackgroundTextureFilename("graphics/backgrounds/background_test3.jpg");
        //set bgm
        system.getMemoryWithoutUpdate().set(MusicPlayerPluginImpl.MUSIC_SET_MEM_KEY, "FM_starsystem_01");

        //the star
        PlanetAPI gensokyo_star = system.initStar("gensokyo", "star_yellow", 600f, 350f);
        //background light color
        system.setLightColor(new Color(245, 237, 203));

        //make asteroid belt surround it
        system.addAsteroidBelt(gensokyo_star, 100, 2000f, 150f, 180, 360, Terrain.ASTEROID_BELT, I18nUtil.getStarSystemsString("terrain_name_RoadOfReconsideration"));
        system.addRingBand(gensokyo_star, "misc", "rings_dust0", 256f, 1, Color.WHITE, 256f, 2000f, 300f);

        //have some nebula
        SectorEntityToken nebula = Misc.addNebulaFromPNG("graphics/misc/FM_Gensokyo_nebula.png", 0, 0,
                system, "terrain", "nebula_blue", 4, 4, StarAge.AVERAGE);
//        SectorEntityToken nebula = system.addTerrain(Terrain.NEBULA, new BaseTiledTerrain.TileParams(
//                "xxxxxxxx"
//                +"xxx   xx"
//                +"xx     x",8,3,"terrain","nebula",1800,1000,"星云"
//        ));
//
//        nebula.setLocation(0,13000f);
        //a gate
        SectorEntityToken gate = system.addCustomEntity("FM_Gensokyo_gate", // unique id
                I18nUtil.getStarSystemsString("entity_name_gensokyo_gate"), // name - if null, defaultName from custom_entities.json will be used
                Entities.INACTIVE_GATE, // type of object, defined in custom_entities.json
                null); // faction
        gate.setCircularOrbit(gensokyo_star, 240f, 10000f, 1200f);

        //a new planet for people
        PlanetAPI hakurei = system.addPlanet("FM_planet_hakurei", gensokyo_star, I18nUtil.getStarSystemsString("planet_name_hakurei"), "terran", 215, 120f, 4500f, 365f);

        //a new market for planet
        MarketAPI hakureiMarket = addMarketplace("fantasy_manufacturing", hakurei, null
                , hakurei.getName(), 7,
                new ArrayList<>(
                        Arrays.asList(
                                Conditions.POPULATION_7, // population
                                Conditions.HABITABLE,
                                Conditions.FARMLAND_BOUNTIFUL,
                                Conditions.REGIONAL_CAPITAL,
                                Conditions.RUINS_VAST,
                                "FM_PhantomFactory"
                        )),
                new ArrayList<>(
                        Arrays.asList(
                                Submarkets.GENERIC_MILITARY,
                                Submarkets.SUBMARKET_BLACK,
                                Submarkets.SUBMARKET_OPEN,
                                Submarkets.SUBMARKET_STORAGE
                        )),
                new ArrayList<>(
                        Arrays.asList(
                                Industries.POPULATION,
                                Industries.MEGAPORT,
                                Industries.STARFORTRESS_HIGH,
                                Industries.FARMING,
                                Industries.HIGHCOMMAND,
                                Industries.ORBITALWORKS,
                                Industries.WAYSTATION,
                                Industries.TECHMINING,
                                Industries.HEAVYBATTERIES
                        )),
                0.3f,
                false,
                true);
        //make a custom description which is specified in descriptions.csv
        hakurei.setCustomDescriptionId("FM_planet_hakurei");
        hakurei.setInteractionImage("illustrations", "FM_illustration_01");
        //give the orbital works a gamma core
        hakureiMarket.getIndustry(Industries.MEGAPORT).setAICoreId(Commodities.BETA_CORE);
        hakureiMarket.getIndustry(Industries.POPULATION).setAICoreId(Commodities.BETA_CORE);
        hakureiMarket.getIndustry(Industries.HIGHCOMMAND).setAICoreId(Commodities.ALPHA_CORE);
        hakureiMarket.getIndustry(Industries.STARFORTRESS_HIGH).setAICoreId(Commodities.ALPHA_CORE);
        hakureiMarket.getIndustry(Industries.ORBITALWORKS).setAICoreId(Commodities.ALPHA_CORE);
        hakureiMarket.getIndustry(Industries.ORBITALWORKS).setSpecialItem(new SpecialItemData(Items.CORRUPTED_NANOFORGE, null));

        PlanetAPI kirisame = system.addPlanet("FM_planet_kirisame", gensokyo_star, I18nUtil.getStarSystemsString("planet_name_kirisame"), "gas_giant", 200, 300f, 6000f, 400f);
        MarketAPI kirisameMarket = addMarketplace("fantasy_manufacturing", kirisame, null
                , kirisame.getName(), 5,
                new ArrayList<>(
                        Arrays.asList(
                                Conditions.POPULATION_5,
                                Conditions.COLD,
                                Conditions.HIGH_GRAVITY,
                                Conditions.DENSE_ATMOSPHERE,
                                Conditions.VOLATILES_PLENTIFUL,// population
                                "FM_PhantomFactory"

                        )),
                new ArrayList<>(
                        Arrays.asList(

                                Submarkets.SUBMARKET_BLACK,
                                Submarkets.SUBMARKET_OPEN,
                                Submarkets.SUBMARKET_STORAGE
                        )),
                new ArrayList<>(
                        Arrays.asList(
                                Industries.POPULATION,
                                Industries.SPACEPORT,
                                Industries.BATTLESTATION_HIGH,
                                Industries.PATROLHQ,
                                Industries.MINING,
                                Industries.REFINING,
                                Industries.WAYSTATION,
                                Industries.FUELPROD,
                                Industries.GROUNDDEFENSES
                        )),
                0.3f,
                false,
                true);
        kirisame.setCustomDescriptionId("FM_planet_kirisame");
        kirisame.setInteractionImage("illustrations", "FM_illustration_02");
        system.addRingBand(kirisame, "misc", "rings_asteroids0", 256f, 1, Color.white, 256f, 1100, 190f, "ring", null);
        kirisameMarket.getIndustry(Industries.MINING).setAICoreId(Commodities.GAMMA_CORE);
        kirisameMarket.getIndustry(Industries.SPACEPORT).setAICoreId(Commodities.GAMMA_CORE);



        PlanetAPI blazing_hell = system.addPlanet("FM_planet_blazing_hell", gensokyo_star, I18nUtil.getStarSystemsString("planet_name_blazing_hell"), "lava", 270, 70f, 1300f, 100f);
        MarketAPI blazing_hellMarket = addMarketplace("fantasy_manufacturing", blazing_hell, null
                , blazing_hell.getName(), 4,
                new ArrayList<>(
                        Arrays.asList(
                                Conditions.POPULATION_4,
                                Conditions.VERY_HOT,
                                Conditions.RARE_ORE_RICH,
                                Conditions.ORE_MODERATE,
                                Conditions.TOXIC_ATMOSPHERE,
                                "FM_PhantomFactory"

                        )),
                new ArrayList<>(
                        Arrays.asList(

                                Submarkets.SUBMARKET_BLACK,
                                Submarkets.SUBMARKET_OPEN,
                                Submarkets.SUBMARKET_STORAGE
                        )),
                new ArrayList<>(
                        Arrays.asList(
                                Industries.POPULATION,
                                Industries.MEGAPORT,
                                Industries.BATTLESTATION_HIGH,
                                Industries.MILITARYBASE,
                                Industries.MINING,
                                Industries.WAYSTATION,
                                Industries.HEAVYBATTERIES
                        )),
                0.3f,
                false,
                true);
        blazing_hell.setCustomDescriptionId("FM_planet_blazing_hell");

        blazing_hellMarket.getIndustry(Industries.BATTLESTATION_HIGH).setAICoreId(Commodities.ALPHA_CORE);
        blazing_hellMarket.getIndustry(Industries.MILITARYBASE).setAICoreId(Commodities.ALPHA_CORE);
        blazing_hellMarket.getIndustry(Industries.MEGAPORT).setAICoreId(Commodities.BETA_CORE);

        blazing_hellMarket.getIndustry(Industries.MILITARYBASE).setSpecialItem(new SpecialItemData(Items.CRYOARITHMETIC_ENGINE, null));

        PlanetAPI crino = system.addPlanet("FM_planet_crino", gensokyo_star, I18nUtil.getStarSystemsString("planet_name_crino"), "frozen", 330, 150f, 8000f, 1000f);
//        MarketAPI crinoMarket = addMarketplace("fantasy_manufacturing", crino, null
//                , crino.getName(), 4,
//                new ArrayList<>(
//                        Arrays.asList(
//                                Conditions.POPULATION_4,
//                                Conditions.VERY_COLD,
//                                Conditions.THIN_ATMOSPHERE,
//                                Conditions.LOW_GRAVITY
//
//
//                        )),
//                new ArrayList<>(
//                        Arrays.asList(
//
//                                Submarkets.SUBMARKET_OPEN,
//                                Submarkets.SUBMARKET_STORAGE
//                        )),
//                new ArrayList<>(
//                        Arrays.asList(
//                                Industries.POPULATION,
//                                Industries.SPACEPORT,
//                                Industries.STARFORTRESS_HIGH,
//                                Industries.REFINING,
//                                Industries.ORBITALWORKS,
//                                Industries.WAYSTATION,
//                                Industries.HEAVYBATTERIES
//                        )),
//                0.3f,
//                false,
//                true);
        crino.setCustomDescriptionId("FM_planet_crino");
        system.addAsteroidBelt(crino, 30, 400f, 100f, 200f, 250f);
//        crinoMarket.getIndustry(Industries.ORBITALWORKS).setAICoreId(Commodities.ALPHA_CORE);
//        crinoMarket.getIndustry(Industries.ORBITALWORKS).setSpecialItem(new SpecialItemData(Items.CORRUPTED_NANOFORGE,null));
//        // and apply its effects
//        InstallableItemEffect itemEffect = ItemEffectsRepo.ITEM_EFFECTS.get(Items.CORRUPTED_NANOFORGE);
//        itemEffect.apply(crinoMarket.getIndustry(Industries.ORBITALWORKS));
//
//        crinoMarket.getIndustry(Industries.WAYSTATION).setAICoreId(Commodities.GAMMA_CORE);
//        crinoMarket.getIndustry(Industries.STARFORTRESS_HIGH).setAICoreId(Commodities.ALPHA_CORE);


        PlanetAPI inverted_castle = system.addPlanet("FM_planet_inverted_castle", gensokyo_star, I18nUtil.getStarSystemsString("planet_name_inverted_castle"), "barren", 15, 50f, 10000f, 1200f);
        MarketAPI inverted_castleMarket = addMarketplace("luddic_path", inverted_castle, null
                , inverted_castle.getName(), 4,
                new ArrayList<>(
                        Arrays.asList(
                                Conditions.POPULATION_4,
                                Conditions.VERY_COLD,
                                Conditions.NO_ATMOSPHERE,
                                Conditions.DARK,
                                Conditions.RARE_ORE_SPARSE,
                                Conditions.ORE_SPARSE,
                                Conditions.OUTPOST,
                                Conditions.STEALTH_MINEFIELDS,
                                Conditions.FRONTIER
                        )),
                new ArrayList<>(
                        Arrays.asList(

                                Submarkets.SUBMARKET_BLACK,
                                Submarkets.SUBMARKET_OPEN,
                                Submarkets.SUBMARKET_STORAGE
                        )),
                new ArrayList<>(
                        Arrays.asList(
                                Industries.POPULATION,
                                Industries.SPACEPORT,
                                Industries.PATROLHQ,
                                Industries.HEAVYINDUSTRY,
                                Industries.GROUNDDEFENSES
                        )),
                0.3f,
                false,
                true);
        inverted_castle.setCustomDescriptionId("FM_planet_inverted_castle");


        SectorEntityToken relay = system.addCustomEntity("gensokyo_relay", // unique id
                I18nUtil.getStarSystemsString("entity_name_gensokyo_relay"), // name - if null, defaultName from custom_entities.json will be used
                "comm_relay", // type of object, defined in custom_entities.json
                "fantasy_manufacturing"); // faction

        SectorEntityToken nav = system.addCustomEntity("gensokyo_nav",
                I18nUtil.getStarSystemsString("entity_name_gensokyo_nav"),
                "nav_buoy",
                "fantasy_manufacturing");

        SectorEntityToken sensor = system.addCustomEntity("gensokyo_sensor",
                I18nUtil.getStarSystemsString("entity_name_gensokyo_sensor"),
                "sensor_array",
                "fantasy_manufacturing");


        relay.setCircularOrbitPointingDown(system.getEntityById("gensokyo"), 245 - 60, 3000, 200);
        nav.setCircularOrbitPointingDown(system.getEntityById("gensokyo"), 245 - 180, 3000, 200);
        sensor.setCircularOrbitPointingDown(system.getEntityById("gensokyo"), 245 - 300, 3000, 200);

        JumpPointAPI jumppoint = Global.getFactory().createJumpPoint("gensokyo_jump", I18nUtil.getStarSystemsString("jump_name_gensokyo_jump"));
        OrbitAPI the_orbit = Global.getFactory().createCircularOrbit(hakurei, 90, 450f, 200f);

        jumppoint.setOrbit(the_orbit);
        jumppoint.setRelatedPlanet(hakurei);
        jumppoint.setStandardWormholeToHyperspaceVisual();
        system.addEntity(jumppoint);

        // generates hyperspace destinations for in-system jump points
        system.autogenerateHyperspaceJumpPoints(true, true);


    }


}
