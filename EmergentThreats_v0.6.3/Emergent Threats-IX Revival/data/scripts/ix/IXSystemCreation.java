package data.scripts.ix;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import lunalib.lunaSettings.LunaSettings;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CustomCampaignEntityAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
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
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.util.Misc;

import data.scripts.util.MagicCampaign;

public class IXSystemCreation {

    public static void generate(SectorAPI sector) {

        //make the system itself!
        StarSystemAPI system = sector.createStarSystem("Zorya");
        LocationAPI hyper = Global.getSector().getHyperspace();

        system.setBackgroundTextureFilename("graphics/backgrounds/background4.jpg");

        //create the star
        PlanetAPI star = system.initStar("ix_zorya", // unique id for this star
                StarTypes.RED_GIANT,  // id in planets.json
                1100f, // radius (in pixels at default zoom)
                600); // corona radius, from star edge
        system.setLightColor(new Color(255, 200, 210)); // light color in entire system, affects all entities

        //get rid of the hyperspace around the star
        HyperspaceTerrainPlugin plugin = (HyperspaceTerrainPlugin) Misc.getHyperspaceTerrain().getPlugin();
        NebulaEditor editor = new NebulaEditor(plugin);

        float minRadius = plugin.getTileSize() * 4f;
        float radius = system.getMaxRadiusInHyperspace();
        editor.clearArc(system.getLocation().x, system.getLocation().y, 0, radius + minRadius * 0.5f, 0, 360f);
        editor.clearArc(system.getLocation().x, system.getLocation().y, 0, radius + minRadius, 0, 360f, 0.25f);

        //add the important planet,
        PlanetAPI planet1 = system.addPlanet("ix_zorya_piorun", star, "Piorun", "terran", 235, 100, 3000, 100);
        planet1.setFaction("ix_battlegroup");
		planet1.setCustomDescriptionId("ix_zorya_piorun");
		planet1.setInteractionImage("illustrations", "ix_piorun_illus");
		
        MarketAPI market = Global.getFactory().createMarket("ix_piorun_market", planet1.getName(), 6);
        market.setFactionId("ix_battlegroup");
        market.setSurveyLevel(MarketAPI.SurveyLevel.FULL);
        market.setPrimaryEntity(planet1);
		market.addCondition(Conditions.HOT);
        market.addCondition(Conditions.ORE_MODERATE);
        market.addCondition(Conditions.RARE_ORE_MODERATE);
		market.addCondition(Conditions.ORGANICS_ABUNDANT);
        market.addCondition(Conditions.VOLATILES_TRACE);
        market.addCondition(Conditions.FARMLAND_POOR);
        market.addCondition(Conditions.HABITABLE);
        market.addCondition(Conditions.POPULATION_6);

        market.addIndustry(Industries.POPULATION, new ArrayList<Commodities>(Arrays.asList(Commodities.GAMMA_CORE)));
		market.addIndustry(Industries.FARMING);
		market.addIndustry(Industries.MINING, new ArrayList<Commodities>(Arrays.asList(Commodities.ALPHA_CORE)));
		market.addIndustry(Industries.REFINING);
        market.addIndustry(Industries.HEAVYBATTERIES);
        market.addIndustry(Industries.MEGAPORT);
		market.addIndustry(Industries.WAYSTATION);
        market.addIndustry(Industries.MILITARYBASE);
        market.addIndustry(Industries.STARFORTRESS_HIGH);

		market.addSubmarket(Submarkets.SUBMARKET_OPEN);
		market.addSubmarket(Submarkets.GENERIC_MILITARY);
        market.addSubmarket(Submarkets.SUBMARKET_STORAGE);
        market.getTariff().modifyFlat("default_tariff", market.getFaction().getTariffFraction());

        planet1.setMarket(market);
        Global.getSector().getEconomy().addMarket(market, true);

		//add independent starbase
        SectorEntityToken starbase = system.addCustomEntity("ix_zorya_vertex", "Vertex Station", "station_hightech3", "ix_battlegroup");
		
		//heliosynchronous orbit for station, spire always pointed towards sun
		starbase.setCircularOrbitPointingDown(star, 235, 2500, 100);
		starbase.setCustomDescriptionId("ix_zorya_vertex");
		starbase.setInteractionImage("illustrations", "ix_vertex_illus");
		
        MarketAPI market_starbase = Global.getFactory().createMarket("ix_vertex_market", starbase.getName(), 5);
        market_starbase.setFactionId("ix_battlegroup");

        market_starbase.setSurveyLevel(MarketAPI.SurveyLevel.FULL);
        market_starbase.setPrimaryEntity(starbase);

        market_starbase.addCondition(Conditions.POPULATION_5);
		market_starbase.addCondition(Conditions.VERY_HOT);
		
		ArrayList<String> listHQ = new ArrayList<String>();
		listHQ.add(Items.CRYOARITHMETIC_ENGINE);
		listHQ.add(Commodities.ALPHA_CORE);
		
        market_starbase.addIndustry(Industries.POPULATION, new ArrayList<Commodities>(Arrays.asList(Commodities.GAMMA_CORE)));
        market_starbase.addIndustry(Industries.HEAVYBATTERIES);
        market_starbase.addIndustry(Industries.MEGAPORT);
		market_starbase.addIndustry("ix_panopticon");
		market_starbase.addIndustry(Industries.HIGHCOMMAND, listHQ);
        market_starbase.addIndustry(Industries.WAYSTATION);
        market_starbase.addIndustry(Industries.ORBITALWORKS, new ArrayList<Items>(Arrays.asList(Items.PRISTINE_NANOFORGE)));
        market_starbase.addIndustry(Industries.FUELPROD, new ArrayList<Items>(Arrays.asList(Items.SYNCHROTRON)));
        market_starbase.addIndustry(Industries.STARFORTRESS_HIGH);
		
		market_starbase.addSubmarket(Submarkets.SUBMARKET_OPEN);
		market_starbase.addSubmarket(Submarkets.GENERIC_MILITARY);
		market_starbase.addSubmarket("IX_honor_guard_market"); 
        market_starbase.addSubmarket(Submarkets.SUBMARKET_STORAGE);
        market_starbase.getTariff().modifyFlat("default_tariff", market_starbase.getFaction().getTariffFraction());

        starbase.setMarket(market_starbase);
        Global.getSector().getEconomy().addMarket(market_starbase, true);

        //water world
        PlanetAPI planet2 = system.addPlanet("ix_zorya_rusalka", star, "Rusalka", "water", 70, 180, 6000, 150);
        planet2.setFaction("ix_battlegroup");
		planet2.setCustomDescriptionId("ix_zorya_rusalka");
		planet2.setInteractionImage("illustrations", "ix_rusalka_illus");
		
        MarketAPI market2 = Global.getFactory().createMarket("ix_rusalka_market", planet2.getName(), 5);
        market2.setFactionId("ix_battlegroup");
        market2.setSurveyLevel(MarketAPI.SurveyLevel.FULL);
        market2.setPrimaryEntity(planet2);

		market2.addCondition("ix_algae");
		market2.addCondition(Conditions.EXTREME_WEATHER);
        market2.addCondition(Conditions.WATER_SURFACE);
        market2.addCondition(Conditions.HABITABLE);
        market2.addCondition(Conditions.POPULATION_5);

        market2.addIndustry(Industries.POPULATION, new ArrayList<Commodities>(Arrays.asList(Commodities.GAMMA_CORE)));
        market2.addIndustry(Industries.COMMERCE);
        market2.addIndustry(Industries.HEAVYBATTERIES);
        market2.addIndustry(Industries.MEGAPORT);
		market2.addIndustry(Industries.WAYSTATION);
        market2.addIndustry(Industries.MILITARYBASE);
        market2.addIndustry(Industries.LIGHTINDUSTRY, new ArrayList<Commodities>(Arrays.asList(Commodities.ALPHA_CORE)));
        market2.addIndustry(Industries.STARFORTRESS_HIGH);
		
		market2.setFreePort(true);
		
		market2.addSubmarket(Submarkets.SUBMARKET_OPEN);
		market2.addSubmarket(Submarkets.GENERIC_MILITARY);
        market2.addSubmarket(Submarkets.SUBMARKET_STORAGE);
        market2.getTariff().modifyFlat("default_tariff", sector.getFaction("pirates").getTariffFraction());

        planet2.setMarket(market2);
        Global.getSector().getEconomy().addMarket(market2, true);
		
		//lawless pirate world Scorn
		if (!LunaSettings.getBoolean("EmergentThreats_IX_Revival", "ix_marzanna_enabled")) {
			PlanetAPI planet3 = system.addPlanet("ix_zorya_scorn", star, "Scorn", "tundra", 140, 165, 8200, 180);
			planet3.setFaction("pirates");
			planet3.setCustomDescriptionId("ix_zorya_scorn");
			planet3.setInteractionImage("illustrations", "ix_scorn_illus");
			
			MarketAPI market3 = Global.getFactory().createMarket("ix_scorn_market", planet3.getName(), 3);
			market3.setFactionId("pirates");
			market3.setSurveyLevel(MarketAPI.SurveyLevel.FULL);
			market3.setPrimaryEntity(planet3);

			market3.addCondition(Conditions.ORE_MODERATE);
			market3.addCondition(Conditions.ORGANICS_COMMON);
			market3.addCondition(Conditions.COLD);
			market3.addCondition(Conditions.IRRADIATED);
			market3.addCondition(Conditions.RUINS_VAST);
			market3.addCondition(Conditions.DECIVILIZED);
			market3.addCondition("ix_killsats");
			market3.addCondition(Conditions.POPULATION_3);

			market3.addIndustry(Industries.POPULATION);
			market3.addIndustry(Industries.SPACEPORT);
			try { 
				market3.addIndustry("BOGGLED_DOMAIN_ARCHAEOLOGY");
			}
			catch (Exception e) {
				market3.addIndustry(Industries.TECHMINING);
			} 
		
			market3.setFreePort(true);
		
			market3.addSubmarket(Submarkets.SUBMARKET_OPEN);
			market3.addSubmarket(Submarkets.SUBMARKET_BLACK);
			market3.addSubmarket(Submarkets.SUBMARKET_STORAGE);
			market3.getTariff().modifyFlat("default_tariff", market3.getFaction().getTariffFraction());

			planet3.setMarket(market3);
			Global.getSector().getEconomy().addMarket(market3, true);
		}
		
		//conquered IX world Marzanna
		else {
			PlanetAPI planet3 = system.addPlanet("ix_zorya_marzanna", star, "Marzanna", "tundra", 140, 165, 8200, 180);
			planet3.setFaction("ix_marzanna");
			planet3.setCustomDescriptionId("ix_zorya_marzanna");
			planet3.setInteractionImage("illustrations", "ix_marzanna_illus");
			
			MarketAPI market3 = Global.getFactory().createMarket("ix_marzanna_market", planet3.getName(), 5);
			market3.setFactionId("ix_battlegroup");
			market3.setSurveyLevel(MarketAPI.SurveyLevel.FULL);
			market3.setPrimaryEntity(planet3);

			market3.addCondition(Conditions.ORE_MODERATE);
			market3.addCondition(Conditions.ORGANICS_COMMON);
			market3.addCondition(Conditions.COLD);
			market3.addCondition(Conditions.POLLUTION);
			market3.addCondition(Conditions.FARMLAND_POOR);
			market3.addCondition(Conditions.POPULATION_5);
			market3.addCondition("ix_cartel_activity");

			market3.addIndustry(Industries.POPULATION);
			market3.addIndustry(Industries.SPACEPORT);
			
			market3.addIndustry(Industries.FARMING);
			market3.addIndustry(Industries.HEAVYINDUSTRY, new ArrayList<Items>(Arrays.asList(Items.CORRUPTED_NANOFORGE)));
			market3.addIndustry(Industries.GROUNDDEFENSES);
			market3.addIndustry("ix_marzanna_base");
			market3.addIndustry(Industries.BATTLESTATION_HIGH);
			
			market3.addSubmarket(Submarkets.SUBMARKET_OPEN);
			market3.addSubmarket(Submarkets.SUBMARKET_BLACK);
			market3.addSubmarket(Submarkets.SUBMARKET_STORAGE);
			market3.getTariff().modifyFlat("default_tariff", market_starbase.getFaction().getTariffFraction());

			planet3.setMarket(market3);
			Global.getSector().getEconomy().addMarket(market3, true);
		}
		
        //add rings
        system.addRingBand(star, "misc", "rings_dust0", 256f, 1, Color.white, 256f, 4500, 305f, null, null);
        system.addRingBand(planet1, "misc", "rings_dust0", 256f, 1, Color.white, 256f, 500, 305f, null, null);
        system.addRingBand(star, "misc", "rings_asteroids0", 256f, 1, Color.white, 256f, 4600, 295f, null, null);
		system.addAsteroidBelt(star, 40, 4600, 100, 30, 40, Terrain.ASTEROID_BELT, "The White Wall");

        //fill rest of system with random planetary bodies
        float radiusAfter = StarSystemGenerator.addOrbitingEntities(system, star, StarAge.AVERAGE,
                2, 4, // min/max entities to add
                9500, // radius to start adding at
                3, // name offset - next planet will be <system name> <roman numeral of this parameter + 1>
                true); // whether to use custom or system-name based names

        StarSystemGenerator.addSystemwideNebula(system, StarAge.AVERAGE);

        SectorEntityToken loc1 = system.addCustomEntity(null,null, "comm_relay",Factions.NEUTRAL);
        loc1.setCircularOrbitPointingDown(star, 50 + 60, 4000, 320);

        SectorEntityToken loc2 = system.addCustomEntity(null,null, "sensor_array",Factions.NEUTRAL);
        loc2.setCircularOrbitPointingDown(star, 50 + 130, 7000, 175);

        SectorEntityToken loc3 = system.addCustomEntity(null,null, "nav_buoy",Factions.NEUTRAL);
        loc3.setCircularOrbitPointingDown(star, 50 + 90, 5500, 155);
		
		//jeff's memorial
		SectorEntityToken jeff = MagicCampaign.createDerelict(
					"scarab_ix_custom",
					ShipRecoverySpecial.ShipCondition.PRISTINE,
					true,
					1000,
					true,
					star,
					70,                 
					4600,                 
					180);
		jeff.addTag(Tags.NEUTRINO_LOW);
		jeff.setCustomDescriptionId("ix_scarab_wreck");
		jeff.setSensorProfile(25f);
		
        //autogenerate jump points
        system.autogenerateHyperspaceJumpPoints(true, true);
		int node = system.getAutogeneratedJumpPointsInHyper().size() - 1;
		SectorEntityToken anchor = (SectorEntityToken) system.getAutogeneratedJumpPointsInHyper().get(node);
        CustomCampaignEntityAPI beacon = Global.getSector().getHyperspace().addCustomEntity("ix_zorya_beacon", null, "ix_warning_beacon", "ix_battlegroup");
        beacon.setCircularOrbitPointingDown(anchor, 180, 150, 365f);
		Color color1 = new Color(255,255,255,255);
		Color color2 = new Color(0,255,0,255);
        Misc.setWarningBeaconColors(beacon, color1, color2);
    }
}