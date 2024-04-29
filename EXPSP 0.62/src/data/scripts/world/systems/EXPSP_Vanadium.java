package data.scripts.world.systems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.procgen.StarAge;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

public class EXPSP_Vanadium {

	public void generate(SectorAPI sector) {
		StarSystemAPI system = sector.createStarSystem("Vanadium");
		system.getLocation().set(12700, -800);

		
		system.setBackgroundTextureFilename("graphics/backgrounds/background4.jpg");
		
		//system.getMemoryWithoutUpdate().set(MusicPlayerPluginImpl.MUSIC_SET_MEM_KEY, "music_title");
		
		// create the star and generate the hyperspace anchor for this system
		PlanetAPI star = system.initStar("vanadium", // unique id for star
										 StarTypes.ORANGE, // id in planets.json
										 750f,		// radius (in pixels at default zoom)
										 750); // corona radius, from star edge

		
		//system.setLightColor(new Color(255, 147, 0)); // light color in entire system, affects all entities
		//star.setCustomDescriptionId("star_white");
		float innerOrbitDistance = StarSystemGenerator.addOrbitingEntities(
				system, //star system variable, used to add entities
				star, //focus object for entities to orbit
				StarAge.AVERAGE, //used by generator to decide which kind of planets to add
				0, //minimum number of entities
				2, //maximum number of entities
				5000, //the radius between the first generated entity and the focus object, in this case the star
				1, //used to assign roman numerals to the generated entities if not given special names
				true //generator will give unique names like "Ordog" instead of "Example Star System III"
		);
		
		PlanetAPI gunmetal = system.addPlanet("gunmetal", star, "Gunmetal", "arid", 90, 120, 1900, 120);
		//arcadia1.setCustomDescriptionId("planet_nomios");
		gunmetal.setCustomDescriptionId("planet_gunmetal");
			SectorEntityToken gunmetal_location = system.addCustomEntity(null,null, "stable_location",Factions.NEUTRAL);
			gunmetal_location.setCircularOrbitPointingDown( star, 90 + 60, innerOrbitDistance+2000, 100);
		MarketAPI gunmetalMarketplace = EXPSP_AddMarketplace.addMarketplace( //A Market is separate to a Planet, and contains data about population, industries and conditions. This is a method from the other script in this mod, that will assign all marketplace conditions to the planet in one go, making it simple and easy
				"independent", //Factions.INDEPENDENT references the id String of the Independent faction, so it is the same as writing "independent", but neater. This determines the Faction associated with this market
				gunmetal, //the PlanetAPI variable that this market will be assigned to
				null, //some mods and vanilla will have additional floating space stations or other entities, that when accessed, will open this marketplace. We don't have any associated entities for this method to add, so we leave null
				"Gunmetal", //Display name of market
				5, //population size
				new ArrayList<>(Arrays.asList( //List of conditions for this method to iterate through and add to the market
						Conditions.POPULATION_5,
						Conditions.FARMLAND_ADEQUATE,
						Conditions.HOT,
						Conditions.HABITABLE
				)),
				new ArrayList<>(Arrays.asList( //list of submarkets for this method to iterate through and add to the market. if a military base industry was added to this market, it would be consistent to add a military submarket too
						Submarkets.SUBMARKET_OPEN, //add a default open market
						Submarkets.GENERIC_MILITARY,
						Submarkets.SUBMARKET_STORAGE, //add a player storage market
						Submarkets.SUBMARKET_BLACK //add a black market
				)),
				new ArrayList<>(Arrays.asList( //list of industries for this method to iterate through and add to the market
						Industries.POPULATION, //population industry is required for weirdness to not happen
						Industries.SPACEPORT, //same with spaceport
						Industries.ORBITALSTATION_MID,
						Industries.FARMING,
						Industries.MILITARYBASE,
						Industries.REFINING,
						Industries.WAYSTATION,

						Industries.GROUNDDEFENSES

				)),
				true, //if true, the planet will have visual junk orbiting and will play an ambient chatter audio track when the player is nearby
				false//used by the method to make a market hidden like a pirate base, not recommended for generating markets in a core world

		);


		//system.addRingBand(arcadia2, "misc", "rings_asteroids0", 256f, 0, new Color(170,210,255,255), 256f, 800, 40f, Terrain.RING, null);
		//system.addAsteroidBelt(arcadia2, 20, 1000, 128, 40, 80, Terrain.ASTEROID_BELT, null);
		
			// lagrangian point of Syrinx
			SectorEntityToken relay = system.addCustomEntity("vanadium_relay", // unique id
					 "Vanadium Relay", // name - if null, defaultName from custom_entities.json will be used
					 "comm_relay_makeshift", // type of object, defined in custom_entities.json
					 "MVS"); // faction
			relay.setCircularOrbitPointingDown( system.getEntityById("vanadium"), 180 + 60, 6000, 200);
			
			// lagrangian point of Syrinx
			JumpPointAPI jumpPoint = Global.getFactory().createJumpPoint("vanadium_passage","Vanadium Passage");
			OrbitAPI orbit = Global.getFactory().createCircularOrbit(star, 180 - 60, 6000, 200);
			jumpPoint.setOrbit(orbit);	
			jumpPoint.setRelatedPlanet(gunmetal);
			jumpPoint.setStandardWormholeToHyperspaceVisual();
			system.addEntity(jumpPoint);
		
//		SectorEntityToken arc_station = system.addOrbitalStation("arcadia_station", arcadia2, 45, 750, 30, "Citadel Arcadia", "hegemony");
//		arc_station.setCustomDescriptionId("station_arcadia"); 
		
		SectorEntityToken vanadium_station = system.addCustomEntity("vanadium_station", "Fort Ivar", "station_side02", "MVS");
		vanadium_station.setCircularOrbitPointingDown(system.getEntityById("gunmetal"), 45, 630, 30);
		//arc_station.setCustomDescriptionId("station_arcadia");
		vanadium_station.setInteractionImage("illustrations", "hound_hangar");
		vanadium_station.setCustomDescriptionId("planet_ivar");
		MarketAPI ivarMarketplace = EXPSP_AddMarketplace.addMarketplace( //A Market is separate to a Planet, and contains data about population, industries and conditions. This is a method from the other script in this mod, that will assign all marketplace conditions to the planet in one go, making it simple and easy
				"MVS", //Factions.INDEPENDENT references the id String of the Independent faction, so it is the same as writing "independent", but neater. This determines the Faction associated with this market
				vanadium_station, //the PlanetAPI variable that this market will be assigned to
				null, //some mods and vanilla will have additional floating space stations or other entities, that when accessed, will open this marketplace. We don't have any associated entities for this method to add, so we leave null
				"Fort Ivar", //Display name of market
				5, //population size
				new ArrayList<>(Arrays.asList( //List of conditions for this method to iterate through and add to the market
						Conditions.POPULATION_5,
						Conditions.OUTPOST
				)),
				new ArrayList<>(Arrays.asList( //list of submarkets for this method to iterate through and add to the market. if a military base industry was added to this market, it would be consistent to add a military submarket too
						Submarkets.SUBMARKET_OPEN, //add a default open market
						Submarkets.GENERIC_MILITARY,
						Submarkets.SUBMARKET_STORAGE, //add a player storage market
						Submarkets.SUBMARKET_BLACK //add a black market
				)),
				new ArrayList<>(Arrays.asList( //list of industries for this method to iterate through and add to the market
						Industries.POPULATION, //population industry is required for weirdness to not happen
						Industries.SPACEPORT, //same with spaceport
						Industries.BATTLESTATION_HIGH,
						Industries.REFINING,
						Industries.MILITARYBASE,
						Industries.FUELPROD,
						Industries.WAYSTATION,

						Industries.HEAVYBATTERIES

				)),

				true, //if true, the planet will have visual junk orbiting and will play an ambient chatter audio track when the player is nearby
				false//used by the method to make a market hidden like a pirate base, not recommended for generating markets in a core world

		);
		ivarMarketplace.getIndustry(Industries.FUELPROD).setSpecialItem(new SpecialItemData(Items.SYNCHROTRON, null));
		float radiusAfter = StarSystemGenerator.addOrbitingEntities(system, star, StarAge.AVERAGE,
				3, 5, // min/max entities to add
				9400, // radius to start adding at 
				2, // name offset - next planet will be <system name> <roman numeral of this parameter + 1>
				true, // whether to use custom or system-name based names
				false); // whether to allow habitable worlds

		// being cheeky here.


		system.autogenerateHyperspaceJumpPoints(true, true);
		
		//system.addScript(new IndependentTraderSpawnPoint(sector, hyper, 1, 10, hyper.createToken(-6000, 2000), station));
	}
		
	
}
