package data.scripts.world.systems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.BattleCreationContext;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.*;
import com.fs.starfarer.api.impl.campaign.events.OfficerManagerEvent;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.procgen.*;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.FleetEncounterContext;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl.BaseFIDDelegate;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl.FIDConfig;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl.FIDConfigGen;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RemnantSeededFleetManager;
import com.fs.starfarer.api.impl.campaign.procgen.themes.SalvageSpecialAssigner;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial;
import com.fs.starfarer.api.impl.campaign.terrain.DebrisFieldTerrainPlugin.DebrisFieldParams;
import com.fs.starfarer.api.impl.campaign.terrain.DebrisFieldTerrainPlugin.DebrisFieldSource;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.util.Misc;
import data.campaign.procgen.MessRemnStationFleetManager;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;
import java.awt.*;
import java.util.List;
import java.util.Random;

import static com.fs.starfarer.api.impl.campaign.intel.PersonBountyIntel.getSharedData;
import static data.campaign.ids.hmi_Tags.*;
import static data.scripts.world.HMI_Utils.MESS_REMNANT_STATION;
import static data.scripts.world.HMI_Utils.level;

public class HMI_mansa implements SectorGeneratorPlugin {

    Random characterSaveSeed = StarSystemGenerator.random;
    Random random = new Random(characterSaveSeed.nextLong());
    public static String DEFEATED_MESS_STATION_KEY = "$defeatedMessStation";


    public static class MessStationFIDConfig implements FleetInteractionDialogPluginImpl.FIDConfigGen {
        public FleetInteractionDialogPluginImpl.FIDConfig createConfig() {
            FIDConfig config = new FIDConfig();

//			config.alwaysAttackVsAttack = true;
//			config.leaveAlwaysAvailable = true;
//			config.showFleetAttitude = false;
            config.showTransponderStatus = false;
            config.showEngageText = false;
            config.alwaysPursue = true;
            config.dismissOnLeave = false;
            //config.lootCredits = false;
            config.withSalvage = false;
            //config.showVictoryText = false;
            config.printXPToDialog = true;

            config.noSalvageLeaveOptionText = "Continue";
//			config.postLootLeaveOptionText = "Continue";
//			config.postLootLeaveHasShortcut = false;

            config.delegate = new BaseFIDDelegate() {
                public void postPlayerSalvageGeneration(InteractionDialogAPI dialog, FleetEncounterContext context, CargoAPI salvage) {
                    new RemnantSeededFleetManager.RemnantFleetInteractionConfigGen().createConfig().delegate.
                            postPlayerSalvageGeneration(dialog, context, salvage);
                }
                public void notifyLeave(InteractionDialogAPI dialog) {

                    SectorEntityToken other = dialog.getInteractionTarget();
                    if (!(other instanceof CampaignFleetAPI)) {
                        dialog.dismiss();
                        return;
                    }
                    CampaignFleetAPI fleet = (CampaignFleetAPI) other;

                    if (!fleet.isEmpty()) {
                        dialog.dismiss();
                        return;
                    }

                    Global.getSector().getMemoryWithoutUpdate().set(DEFEATED_MESS_STATION_KEY, true);

                    ShipRecoverySpecial.PerShipData ship = new ShipRecoverySpecial.PerShipData("hmi_limina_std", ShipRecoverySpecial.ShipCondition.WRECKED, 0f);
                    ship.shipName = "Iteration 180612";
                    DerelictShipEntityPlugin.DerelictShipData params = new DerelictShipEntityPlugin.DerelictShipData(ship, false);
                    CustomCampaignEntityAPI entity = (CustomCampaignEntityAPI) BaseThemeGenerator.addSalvageEntity(
                            fleet.getContainingLocation(),
                            Entities.WRECK, Factions.NEUTRAL, params);
                    Misc.makeImportant(entity, "limina");
                    entity.getMemoryWithoutUpdate().set("$limina", true);

                    entity.getLocation().x = fleet.getLocation().x + (50f - (float) Math.random() * 100f);
                    entity.getLocation().y = fleet.getLocation().y + (50f - (float) Math.random() * 100f);

                    ShipRecoverySpecial.ShipRecoverySpecialData data = new ShipRecoverySpecial.ShipRecoverySpecialData(null);
                    data.notNowOptionExits = true;
                    data.noDescriptionText = true;
                    DerelictShipEntityPlugin dsep = (DerelictShipEntityPlugin) entity.getCustomPlugin();
                    ShipRecoverySpecial.PerShipData copy = (ShipRecoverySpecial.PerShipData) dsep.getData().ship.clone();
                    copy.variant = Global.getSettings().getVariant(copy.variantId).clone();
                    copy.variantId = null;
                    copy.variant.addTag(Tags.SHIP_CAN_NOT_SCUTTLE);
//                  copy.variant.addTag(Tags.SHIP_UNIQUE_SIGNATURE);
                    data.addShip(copy);

                    Misc.setSalvageSpecial(entity, data);

                    dialog.setInteractionTarget(entity);
                    RuleBasedInteractionDialogPluginImpl plugin = new RuleBasedInteractionDialogPluginImpl("MessStationDestroyed");
                    dialog.setPlugin(plugin);
                    plugin.init(dialog);
                }

                public void battleContextCreated(InteractionDialogAPI dialog, BattleCreationContext bcc) {
                    bcc.aiRetreatAllowed = false;
                    bcc.objectivesAllowed = false;
                    bcc.fightToTheLast = true;
                    bcc.enemyDeployAll = true;
                }
            };
            return config;
        }
    }



    @Override
    public void generate(SectorAPI sector) {


        String[] strings = {"Mali", "Sungala", "Malinké", "Fulani", "Mandinka"};
        int nameSelector = random.nextInt(strings.length);      // random name selector

        LocationAPI hyper = Global.getSector().getHyperspace();

        Constellation hmi_constellation_Mansa = new Constellation(
                Constellation.ConstellationType.NORMAL, StarAge.OLD
        );

        NameGenData data = new NameGenData("null", "null");
        ProcgenUsedNames.NamePick Mansaconstname = new ProcgenUsedNames.NamePick(data, strings[nameSelector], "null");
        hmi_constellation_Mansa.setNamePick(Mansaconstname);

        StarSystemAPI system = sector.createStarSystem("Mansa");
        StarSystemAPI system2 = sector.createStarSystem("Mari");
        StarSystemAPI system3 = sector.createStarSystem("Sundiata");

        hmi_constellation_Mansa.getSystems().add(sector.getStarSystem("Mansa"));
        hmi_constellation_Mansa.getSystems().add(sector.getStarSystem("Mari"));
        hmi_constellation_Mansa.getSystems().add(sector.getStarSystem("Sundiata"));

        sector.getStarSystem("Mansa").setConstellation(hmi_constellation_Mansa);
        sector.getStarSystem("Mari").setConstellation(hmi_constellation_Mansa);
        sector.getStarSystem("Sundiata").setConstellation(hmi_constellation_Mansa);


        system.setBackgroundTextureFilename("graphics/backgrounds/mercy.jpg");

                // create the star and generate the hyperspace anchor for this system
        PlanetAPI star = system.initStar("hmi_mansa_star", // unique id for this star
                "star_yellow", // id in planets.json
                450f, 		  // radius (in pixels at default zoom)
                120, // corona
                3f, // solar wind burn level
                0.7f, // flare probability
                2.0f); // CR loss multiplier, good values are in the range of 1-5
        system.setLightColor(new Color(255, 220, 0)); // light color in entire system, affects all entities
        system.getLocation().set(-42000, 42000);
        system.addTag(Tags.THEME_HIDDEN);
        system.addTag(Tags.THEME_UNSAFE);

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
       

		PlanetAPI mansa1 = system.addPlanet("mansa_1", star, "Ouali", "rocky_metallic", 0, 100, 2000, 100);
					Misc.initConditionMarket(mansa1);
                    mansa1.getMarket().addCondition(Conditions.NO_ATMOSPHERE);
                    mansa1.getMarket().addCondition(Conditions.TECTONIC_ACTIVITY);
					mansa1.getMarket().addCondition(Conditions.ORE_MODERATE);
					mansa1.getMarket().addCondition(Conditions.RARE_ORE_MODERATE);
                    mansa1.getMarket().addCondition(Conditions.VERY_HOT);

		PlanetAPI mansa2 = system.addPlanet("mansa2", star, "Khalifa", "barren", 200, 100, 3200, 120);
					Misc.initConditionMarket(mansa2);
                    mansa2.getMarket().addCondition(Conditions.NO_ATMOSPHERE);
                    mansa2.getMarket().addCondition(Conditions.LOW_GRAVITY);
                    mansa2.getMarket().addCondition(Conditions.EXTREME_TECTONIC_ACTIVITY);
                    mansa2.getMarket().addCondition(Conditions.VERY_COLD);		
					mansa2.getMarket().addCondition(Conditions.METEOR_IMPACTS);		
        mansa2.setCustomDescriptionId("hmi_stripped");

        system.addRingBand(star, "misc", "rings_dust0", 256f, 0, Color.white, 256f, 3200, 200f, Terrain.RING, "Khalifa Remnant");
        system.addAsteroidBelt(star, 240, 3200, 128, 200, 300, Terrain.ASTEROID_BELT, null);
        system.addRingBand(mansa2, "misc", "rings_dust0", 256f, 1, Color.white, 256f, 220, 200f, Terrain.RING, "Khalifa Remnant");
					
				// Spawn The Mess Station. Laughs maniacally as laptop users roll in fury.
                FleetMemberAPI member1 = Global.getFactory().createFleetMember(FleetMemberType.SHIP, MESS_REMNANT_STATION);
                member1.getRepairTracker().setCR(member1.getRepairTracker().getMaxCR());
                CampaignFleetAPI fleet1 = FleetFactoryV3.createEmptyFleet("mess_remnant", "battlestation", null);

                //float radius_fleet1 = radius_star + radius_station + radius_variation * (float) Math.random();

                fleet1.getFleetData().addFleetMember(member1);
                fleet1.getMemoryWithoutUpdate().set("$cfai_makeAggressive", Boolean.valueOf(true));
                fleet1.getMemoryWithoutUpdate().set("$cfai_noJump", Boolean.valueOf(true));
                fleet1.getMemoryWithoutUpdate().set("$cfai_makeAllowDisengage", Boolean.valueOf(true));
                fleet1.setStationMode(Boolean.valueOf(true));
                addMessRemnantStationInteractionConfig(fleet1);
                system.addEntity(fleet1);

                fleet1.clearAbilities();
                fleet1.addAbility("transponder");
                fleet1.getAbility("transponder").activate();
                fleet1.getDetectedRangeMod().modifyFlat("gen", 1000.0F);
                fleet1.setCircularOrbitWithSpin(
                        mansa2, // focus
                60,// angle
                250, // orbitRadius, was radius_fleet1, useful if random location in system
                24f, // orbitDays
                7f, // minSpin
                12f // maxSpin
                );
                fleet1.setAI(null);
                PersonAPI commander1 = OfficerManagerEvent.createOfficer(
                Global.getSector().getFaction("mess_remnant"), level, true);
                commander1.getStats().setSkillLevel("gunnery_implants", 3.0F);
                fleet1.setCommander(commander1);
                fleet1.getFlagship().setCaptain(commander1);
                fleet1.getCargo().addCommodity(Commodities.ALPHA_CORE, 5f);
                fleet1.getCargo().addCommodity(Commodities.BETA_CORE, 10f);
                fleet1.getCargo().addCommodity(Commodities.GAMMA_CORE, 20f);
                fleet1.getCargo().addCommodity("mess_nano", 400f);
                fleet1.getCargo().addCommodity(Commodities.HEAVY_MACHINERY, 800f);
                fleet1.getCargo().addCommodity(Commodities.RARE_METALS, 500f);
                fleet1.getCargo().addCommodity(Commodities.METALS, 1000f);
                fleet1.addTag("mess_remnant_station");
                fleet1.getMemoryWithoutUpdate().set(MemFlags.FLEET_INTERACTION_DIALOG_CONFIG_OVERRIDE_GEN,
                new MessStationFIDConfig());

                int maxFleets = 7 + this.random.nextInt(2);

                MessRemnStationFleetManager MessRemnFleets = new MessRemnStationFleetManager(
                fleet1, 1.0f, 0, maxFleets, 10.0f, 30, 60);
                system.addScript(MessRemnFleets);


        PlanetAPI mansa3 = system.addPlanet("mansa3", star, "Sakoura", "lava", 180, 150, 6500, 200);
					Misc.initConditionMarket(mansa3);
                    mansa3.getMarket().addCondition(Conditions.TOXIC_ATMOSPHERE);
                    mansa3.getMarket().addCondition(Conditions.EXTREME_TECTONIC_ACTIVITY);
					mansa3.getMarket().addCondition(Conditions.ORE_SPARSE);
					mansa3.getMarket().addCondition(Conditions.RARE_ORE_SPARSE);
                    mansa3.getMarket().addCondition(Conditions.VERY_HOT);
	
        //JUMP POINT
        JumpPointAPI jumpPoint1 = Global.getFactory().createJumpPoint("mansa_jumpPointA", "Mansa Inner Jump-Point");
        OrbitAPI orbit = Global.getFactory().createCircularOrbit(star, 250, 5000, 300);
        jumpPoint1.setOrbit(orbit);
        jumpPoint1.setRelatedPlanet(mansa3);
        jumpPoint1.setStandardWormholeToHyperspaceVisual();
        system.addEntity(jumpPoint1);

        system.addAsteroidBelt(star, 100, 8000, 256, 150, 250, Terrain.ASTEROID_BELT, null);
        system.addAsteroidBelt(star, 100, 8400, 256, 150, 250, Terrain.ASTEROID_BELT, null);
        system.addRingBand(star, "misc", "rings_dust0", 256f, 0, Color.gray, 256f, 8000, 80f);
        system.addRingBand(star, "misc", "rings_dust0", 256f, 0, Color.gray, 256f, 8400, 80f);

        PlanetAPI mansa4 = system.addPlanet("mansa4", star, "Abubakari", "barren-bombarded", 180, 150, 10000, 200f);
					Misc.initConditionMarket(mansa4);
                    mansa4.getMarket().addCondition(Conditions.NO_ATMOSPHERE);
                    mansa4.getMarket().addCondition(Conditions.METEOR_IMPACTS);
					mansa4.getMarket().addCondition(Conditions.ORE_RICH);
					mansa4.getMarket().addCondition(Conditions.RARE_ORE_RICH);
                    mansa4.getMarket().addCondition(Conditions.VERY_COLD);
					mansa4.getMarket().addCondition(Conditions.DARK);
		
		PlanetAPI mansa5 = system.addPlanet("mansa5", star, "Keita", "ice_giant", 180, 150, 12000, 900f);
					Misc.initConditionMarket(mansa5);
                    mansa5.getMarket().addCondition(Conditions.EXTREME_WEATHER);
                    mansa5.getMarket().addCondition(Conditions.METEOR_IMPACTS);
					mansa5.getMarket().addCondition(Conditions.VERY_COLD);
					mansa5.getMarket().addCondition(Conditions.VOLATILES_TRACE);
					mansa5.getMarket().addCondition(Conditions.DARK);

        system.addRingBand(mansa5, "misc", "rings_ice0", 256f, 0, Color.white, 256f, 800, 100f);
        system.addRingBand(mansa5, "misc", "rings_ice0", 256f, 2, Color.white, 256f, 1000, 140f);
        system.addAsteroidBelt(mansa5, 20, 300, 256, 150, 250, Terrain.ASTEROID_BELT, null);
		system.addRingBand(mansa5, "misc", "rings_special0", 256f, 1, new Color(225,215,255,200), 128f, 380, 30f, Terrain.RING, "Keita's Disk");

        system.autogenerateHyperspaceJumpPoints(true, true);

        SectorEntityToken anchor = system.getHyperspaceAnchor();
        CustomCampaignEntityAPI beacon = Global.getSector().getHyperspace().addCustomEntity("messrem_beacon", "Warning Beacon", "HMI_RemMess_beacon", Factions.NEUTRAL);
        beacon.setCircularOrbitPointingDown(anchor, 100, 300, 65f);
        Color glowColor = new Color(250,55,0,255);
        Color pingColor = new Color(250,55,0,255);
        Misc.setWarningBeaconColors(beacon, glowColor, pingColor);


        cleanup(system);


///    MARI, SYSTEM2

        system2.setBackgroundTextureFilename("graphics/backgrounds/background2.jpg");

        // create the star and generate the hyperspace anchor for this system
        PlanetAPI star2 = system2.initStar("hmi_mari_star", // unique id for this star
                "star_orange", // id in planets.json
                300f, 		  // radius (in pixels at default zoom)
                100, // corona
                3f, // solar wind burn level
                0.7f, // flare probability
                2.0f); // CR loss multiplier, good values are in the range of 1-5
        system2.setLightColor(new Color(255, 230, 220)); // light color in entire system, affects all entities
        system2.getLocation().set(-44000, 42000);

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

        float radiusAfterMari = StarSystemGenerator.addOrbitingEntities(system2, star2, StarAge.OLD,
                2, 5, // min/max entities to add
                5000, // radius to start adding at
                0, // name offset - next planet will be <system name> <roman numeral of this parameter + 1>
                true, // whether to use custom or system-name based names
                false); // whether to allow habitable worlds

        //JUMP POINT
        JumpPointAPI jumpPointMari = Global.getFactory().createJumpPoint("musa_jumpPointA", "Musa Inner Jump-Point");
        OrbitAPI orbitMari = Global.getFactory().createCircularOrbit(star2, 3000, 4000, 300);
        jumpPointMari.setOrbit(orbitMari);
        jumpPointMari.setStandardWormholeToHyperspaceVisual();
        system2.addEntity(jumpPointMari);

        system2.autogenerateHyperspaceJumpPoints(true, true);

        cleanup(system2);

///     SUNDIATA, SYSTEM3


        system3.setBackgroundTextureFilename("graphics/backgrounds/background2.jpg");

        // create the star and generate the hyperspace anchor for this system
        PlanetAPI star3 = system3.initStar("hmi_sundiata_star", // unique id for this star
                "star_orange", // id in planets.json
                600f, 		  // radius (in pixels at default zoom)
                700, // corona
                10f, // solar wind burn level
                0.7f, // flare probability
                3.0f); // CR loss multiplier, good values are in the range of 1-5
        system3.setLightColor(new Color(255, 230, 220)); // light color in entire system, affects all entities
        system3.getLocation().set(-40500, 42800);

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

        float radiusAfterSundiata = StarSystemGenerator.addOrbitingEntities(system3, star3, StarAge.OLD,
                1, 3, // min/max entities to add
                6000, // radius to start adding at
                0, // name offset - next planet will be <system name> <roman numeral of this parameter + 1>
                true, // whether to use custom or system-name based names
                false); // whether to allow habitable worlds

        //JUMP POINT
        JumpPointAPI jumpPointSundiata = Global.getFactory().createJumpPoint("sundiata_jumpPointA", "Sundiata Inner Jump-Point");
        OrbitAPI orbitSundiata = Global.getFactory().createCircularOrbit(star3, 4000, 4000, 300);
        jumpPointSundiata.setOrbit(orbitSundiata);
        jumpPointSundiata.setStandardWormholeToHyperspaceVisual();
        system3.addEntity(jumpPointSundiata);

        system3.autogenerateHyperspaceJumpPoints(true, true);

        cleanup(system3);
    }
    
    void cleanup(StarSystemAPI system){
        HyperspaceTerrainPlugin plugin = (HyperspaceTerrainPlugin) Misc.getHyperspaceTerrain().getPlugin();
		NebulaEditor editor = new NebulaEditor(plugin);        
        float minRadius = plugin.getTileSize() * 2f;
        
        float radius = system.getMaxRadiusInHyperspace();
        editor.clearArc(system.getLocation().x, system.getLocation().y, 0, radius + minRadius * 0.5f, 0, 360f);
        editor.clearArc(system.getLocation().x, system.getLocation().y, 0, radius + minRadius, 0, 360f, 0.25f);	     
		}

    public static void addMessRemnantStationInteractionConfig(CampaignFleetAPI fleet) {
        fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_INTERACTION_DIALOG_CONFIG_OVERRIDE_GEN,
                new MessRemnantStationInteractionConfigGen());
    }


    public int getOrder() {
        return 1500;
    }


    public static class MessRemnantStationInteractionConfigGen implements FleetInteractionDialogPluginImpl.FIDConfigGen {
        public FleetInteractionDialogPluginImpl.FIDConfig createConfig() {
            FleetInteractionDialogPluginImpl.FIDConfig config = new FleetInteractionDialogPluginImpl.FIDConfig();

            config.alwaysAttackVsAttack = true;
            config.leaveAlwaysAvailable = true;
            config.showFleetAttitude = false;
            config.showTransponderStatus = false;
            config.showEngageText = false;

            config.delegate = new FleetInteractionDialogPluginImpl.BaseFIDDelegate() {
                public void postPlayerSalvageGeneration(InteractionDialogAPI dialog, FleetEncounterContext context, CargoAPI salvage) {
                }
                public void notifyLeave(InteractionDialogAPI dialog) {
                }
                public void battleContextCreated(InteractionDialogAPI dialog, BattleCreationContext bcc) {
                    bcc.aiRetreatAllowed = false;
                    bcc.objectivesAllowed = false;
                }
            };
            return config;
        }
    }

}