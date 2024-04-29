package data.campaign.procgen;

import java.util.*;

import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.procgen.themes.*;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.BaseSalvageSpecial;
import com.fs.starfarer.api.impl.campaign.world.MoteParticleScript;
import data.campaign.fleets.HMIScavFleetRouteManager;
import data.campaign.fleets.MessFleetManager;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.CoronalTapParticleScript;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin;
import com.fs.starfarer.api.impl.campaign.econ.impl.PlanetaryShield;
import com.fs.starfarer.api.impl.campaign.fleets.DefaultFleetInflater;
import com.fs.starfarer.api.impl.campaign.fleets.DefaultFleetInflaterParams;
import com.fs.starfarer.api.impl.campaign.procgen.Constellation;
import com.fs.starfarer.api.impl.campaign.procgen.DefenderDataOverride;
import com.fs.starfarer.api.impl.campaign.procgen.NameGenData;
import com.fs.starfarer.api.impl.campaign.procgen.PlanetConditionGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.PlanetGenDataSpec;
import com.fs.starfarer.api.impl.campaign.procgen.ProcgenUsedNames;
import com.fs.starfarer.api.impl.campaign.procgen.ProcgenUsedNames.NamePick;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator.StarSystemType;
import com.fs.starfarer.api.impl.campaign.procgen.themes.SalvageSpecialAssigner.ShipRecoverySpecialCreator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.SalvageSpecialAssigner.SpecialCreationContext;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.ShipCondition;
import com.fs.starfarer.api.impl.campaign.terrain.AsteroidFieldTerrainPlugin.AsteroidFieldParams;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;


public class HMIThemeGenerator extends BaseThemeGenerator {

	public String getThemeId() {
		return Themes.MISC;
	}

	@Override
	public float getWeight() {
		return 0f;
	}

	@Override
	public int getOrder() {
		return 1000000;
	}
	public static String HMIBLACKLUDD_SYSTEM_KEY = "$hmi_hmiblackluddSystem";
	public static String HMIBLACKLUDD_STATION_KEY = "$hmi_hmiblackluddStation";
	public static String BLACKLUDDDEFENDER_KEY = "$hmi_hmiblackluddDefender";

	public static String HMI_MYSTERYBLACKHOLESITE_SYSTEM_KEY = "$hmi_hmiblacksiteSystem";
	public static String HMI_MYSTERYBLACKHOLESITE_STATION_KEY = "$hmi_hmiblackmercStation";
	public static String HMI_MYSTERYBLACKHOLEDEFENDER_KEY = "$hmi_hmiblackmercDefender";

	public static String HMI_MYSTERY_MINING_STATION_KEY = "$hmi_hmiMercminingDefender";
	public static String HMI_MYSTERY_REFINING_STATION_KEY = "$hmi_hmiMercrefiningDefender";

	public static String HMI_BLACKSITE_RANDOMSHIPSELECT_KEY = "$hmi_BlackMercShipSelect";

	public static String  HMIRAMPANTMOTHERSHIP_SYSTEM_KEY = "$hmi_hmirampantmotherSystem";
	public static String  HMIRAMPANT_STATION_KEY = "$hmi_hmirampantmotherStation";

	private static Float random_var = null;

	@Override
	public void generateForSector(ThemeGenContext context, float allowedUnusedFraction) {

		addBlackLuddSystem(context);
		addMysteryBlacksiteSystem(context);
		addRampantMothership(context);
//		addLostRemnantSystem(context);
	}
	
	protected void addBlackLuddSystem(ThemeGenContext context) {
		if (DEBUG) System.out.println("Looking for Black Hole LP system");
		
		List<StarSystemAPI> preferred = new ArrayList<StarSystemAPI>();
		List<StarSystemAPI> other = new ArrayList<StarSystemAPI>();
		
		for (Constellation c : context.constellations) {
			for (StarSystemAPI system : c.getSystems()) {
				if (system.hasTag(Tags.THEME_SPECIAL)) continue;

				if (!system.hasBlackHole()) continue;
				
				boolean misc = system.hasTag(Tags.THEME_MISC_SKIP) || system.hasTag(Tags.THEME_MISC);
				if (system.hasTag(Tags.THEME_DERELICT)) misc = false;
				
				boolean nonLargeDerelict = system.hasTag(Tags.THEME_DERELICT) && 
										!system.hasTag(Tags.THEME_DERELICT_MOTHERSHIP) &&
										!system.hasTag(Tags.THEME_DERELICT_CRYOSLEEPER) &&
										!system.hasTag(Tags.THEME_DERELICT_SURVEY_SHIP);
				boolean remnantNoFleets = system.hasTag(Tags.THEME_REMNANT_NO_FLEETS);
				boolean unsafe = system.hasTag(Tags.THEME_UNSAFE);
				if (unsafe || !(misc || nonLargeDerelict || remnantNoFleets)) {
					continue;
				}
				
				int count = 0;
//				for (SectorEntityToken curr : system.getAllEntities()) {
//					if (curr.getId().equals("station_research") || curr.getId().equals("station_research_remnant"))
//					count++;
//				}
				for (PlanetAPI curr : system.getPlanets()) {
					if (curr.isStar()) continue;
					if (curr.isMoon()) continue;
					if (!curr.getMarket().isPlanetConditionMarketOnly()) continue;
					count++;
				}
				
				if (count > 2) {
					preferred.add(system);
				} else {
					other.add(system);
				}
			}
		}
		
		Comparator<StarSystemAPI> comp = new Comparator<StarSystemAPI>() {
			public int compare(StarSystemAPI o1, StarSystemAPI o2) {
				return (int) Math.signum(o2.getLocation().length() - o1.getLocation().length());
			}
		};

		List<StarSystemAPI> sorted = new ArrayList<StarSystemAPI>();
		if (!preferred.isEmpty()) {
			sorted.addAll(preferred);
		} else {
			sorted.addAll(other);
		}
		if (sorted.isEmpty()) {
			if (DEBUG) System.out.println("FAILED TO FIND SUITABLE SYSTEM FOR BLACK HOLE LP");
			return;
		}
		Collections.sort(sorted, comp);
		
		
		// pick from some of the matching systems furthest from core
		WeightedRandomPicker<StarSystemAPI> picker = new WeightedRandomPicker<StarSystemAPI>(random);
		for (int i = 0; i < 20 && i < sorted.size(); i++) {
			//sorted.get(i).addTag(Tags.PK_SYSTEM);
			picker.add(sorted.get(i), 1f);
		}
		
		StarSystemAPI system = picker.pick();
		
		if (DEBUG) System.out.println("Adding Black Hole LP to [" + system.getName() + "] at [" + system.getLocation() + "]");
		setUpBlackHoleLPSystem(system);
		
		
		if (DEBUG) System.out.println("Finished adding Black Hole LP system\n\n\n\n\n");
	}

	protected void setUpBlackHoleLPSystem(StarSystemAPI system) {
		system.addTag(Tags.THEME_SPECIAL);
		system.addTag("HMI_LPBLACKHOLE_SYSTEM");
		system.addTag(Tags.THEME_UNSAFE);
		
		Global.getSector().getPersistentData().put(HMIBLACKLUDD_SYSTEM_KEY, system);
		Global.getSector().getMemoryWithoutUpdate().set(HMIBLACKLUDD_SYSTEM_KEY, system.getId());
		
		// - Grab the Research Station

		SectorEntityToken station = null;
//		for (SectorEntityToken curr : system.getAllEntities()) {
//			if (curr.getId().equals("station_research") || curr.getId().equals("station_research_remnant"))
//				station = curr;
//			break;
//		}

		// In the original code it'd make a research station, but
		// considering we've deliberately sought out a research station specifically,
		// I don't think we really need this.
		//EDIT: Turns out that in general Research Stations can spawn directly on the edge of black holes.
		// So, it's actually better if I just make the station further away each time.
//		if (station == null) {
		List<OrbitGap> gaps = BaseThemeGenerator.findGaps(system.getCenter(), 2000, 20000, 800);
		float orbitRadius = 7000;
		if (!gaps.isEmpty()) {
			orbitRadius = (gaps.get(0).start + gaps.get(0).end) * 0.5f;
		}
		float radius = 500f + 200f * random.nextFloat();
		float area = radius * radius * 3.14f;
		int count = (int) (area / 80000f);
		count *= 2;
		if (count < 10) count = 10;
		if (count > 100) count = 100;
		float angle = random.nextFloat() * 360f;
		float orbitDays = orbitRadius / (20f + random.nextFloat() * 5f);


		station = BaseThemeGenerator.addSalvageEntity(system, Entities.STATION_RESEARCH, Factions.LUDDIC_PATH);
		station.setCircularOrbit(system.getCenter(), angle, orbitRadius, orbitDays);


		if (station == null) {
				if (DEBUG) System.out.println("FAILED TO CREATE BLACK LUDD RESEARCH STATION IN SYSTEM");
				return;
			}
//		}

		station.setName("The Temple Mount");
		station.setFaction(Factions.LUDDIC_PATH);
		station.addTag(Tags.NOT_RANDOM_MISSION_TARGET);
		station.addScript(new MoteParticleScript(station, 0.25f));
		station.setCustomDescriptionId("hmi_blackludd_stationdesc");
		station.setId("black_hole_lp_station_id");
		station.getMemoryWithoutUpdate().set(BLACKLUDDDEFENDER_KEY, true);
		Misc.setDefenderOverride(station, new DefenderDataOverride(Factions.LUDDIC_PATH, 1f, 20, 20, 1));



		int maxFleets = 3 + this.random.nextInt(2);

		BlackLuddStationFleetManager BlackLuddFleets = new BlackLuddStationFleetManager(
				station, 1.0f, 0, maxFleets, 15.0f, 15, 40);
		system.addScript(BlackLuddFleets);

		for (SectorEntityToken curr : system.getEntitiesWithTag(Tags.STABLE_LOCATION)) {
			system.removeEntity(curr);
		}
		for (SectorEntityToken curr : system.getEntitiesWithTag(Tags.OBJECTIVE)) {
			system.removeEntity(curr);
		}

		//Setting up the Mercenary Graveyard - Removed, as it kept getting LP fleets stuck in the hole, and also is a pain in the neck for ship seek quests

//		StarSystemData data = new StarSystemData();
//		WeightedRandomPicker<String> hulls = new WeightedRandomPicker<String>(random);
//		PlanetAPI lp_blackhole = null;
//		for (PlanetAPI curr : system.getPlanets()) {
//			if (!curr.getSpec().isBlackHole()) continue;
//			lp_blackhole = curr;
//			break;
//		}
//		if (lp_blackhole != null) {
//			data = new StarSystemData();
//			WeightedRandomPicker<String> MercShipFactions = new WeightedRandomPicker<String>(random);
//			MercShipFactions.add(Factions.MERCENARY);
//			hulls = new WeightedRandomPicker<String>(random);
//			hulls.add("revenant", 0.25f);
//			hulls.add("phantom", 0.25f);
//			hulls.add("afflictor", 0.75f);
//			hulls.add("shade", 0.75f);
//			hulls.add("buffalo_tritachyon", 1f);
//			hulls.add("buffalo_tritachyon", 0.5f);
//			hulls.add("medusa", 0.5f);
//			hulls.add("dram", 1f);
//			hulls.add("phaeton", 0.75f);
//			hulls.add("hyperion", 0.1f);
//			hulls.add("tempest", 0.5f);
//			hulls.add("shrike", 1f);
//			hulls.add("shrike", 0.5f);
//			hulls.add("wolf", 1f);
//			hulls.add("wolf", 0.5f);

//			addShipGraveyard(data, lp_blackhole, MercShipFactions, hulls);
// 			Makes no sense for this to be here tbh.
//			addDebrisField(data, lp_blackhole, 400f);

//			for (AddedEntity ae : data.generated) {
//				SalvageSpecialAssigner.assignSpecials(ae.entity, true);
//				if (ae.entity.getCustomPlugin() instanceof DerelictShipEntityPlugin) {
//					DerelictShipEntityPlugin plugin = (DerelictShipEntityPlugin) ae.entity.getCustomPlugin();
//					plugin.getData().ship.condition = ShipCondition.WRECKED;
//				}
//			}
//		}

		//Setting up the first Monument

		PlanetAPI lp_monument_planet1 = null;

		for (PlanetAPI curr : system.getPlanets()) {
			if (curr.isStar()) continue;
			if (curr.isMoon()) continue;
			if (!curr.getMarket().isPlanetConditionMarketOnly()) continue;
			if (curr.getCircularOrbitRadius() < 6000) continue;
			lp_monument_planet1 = curr;
			break;
		}

		if (lp_monument_planet1 == null) {
			List<OrbitGap> gaps2 = BaseThemeGenerator.findGaps(system.getCenter(), 6000, 20000, 800);
			float orbitRadius2 = 6000;
			if (!gaps2.isEmpty()) {
				orbitRadius2 = (gaps2.get(0).start + gaps2.get(0).end) * 0.5f;
			}
			float orbitDays2 = orbitRadius2 / (20f + random.nextFloat() * 5f);
			float radius2 = 100f + random.nextFloat() * 50f;
			float angle2 = random.nextFloat() * 360f;
			String type = Planets.BARREN;
			NamePick namePick = ProcgenUsedNames.pickName(NameGenData.TAG_PLANET, null, null);
			String name2 = namePick.nameWithRomanSuffixIfAny;
			lp_monument_planet1 = system.addPlanet(Misc.genUID(), system.getStar(), name2, type, angle2, radius2, orbitRadius2, orbitDays2);
			PlanetConditionGenerator.generateConditionsForPlanet(null, lp_monument_planet1, system.getAge());
			if (lp_monument_planet1 == null) {
				return;
			}
		}


		SectorEntityToken lp_blackhole_monument1 = system.addCustomEntity("lp_blackhole_monument1", "Old Probe", "hmi_blackhole_monument1", Factions.NEUTRAL);
		lp_blackhole_monument1.setCircularOrbitPointingDown(lp_monument_planet1, 0, 500, 40);
		lp_blackhole_monument1.setCustomDescriptionId("lp_blackhole_monument1");
		lp_blackhole_monument1.addTag("hmi_hmiblackluddmonument1");

		//Setting up the Second Monument

		PlanetAPI lp_monument_planet2 = null;

		for (PlanetAPI curr : system.getPlanets()) {
			if (curr.isStar()) continue;
			if (curr.isMoon()) continue;
			if (!curr.getMarket().isPlanetConditionMarketOnly()) continue;
			if (curr.getCircularOrbitRadius() > 6000) continue;
			lp_monument_planet2 = curr;
			break;
		}

		if (lp_monument_planet2 == null) {
			List<OrbitGap> gaps3 = BaseThemeGenerator.findGaps(system.getCenter(), 8000, 20000, 800);
			float orbitRadius3 = 9000;
			if (!gaps3.isEmpty()) {
				orbitRadius3 = (gaps3.get(0).start + gaps3.get(0).end) * 0.5f;
			}
			float orbitDays3 = orbitRadius3 / (20f + random.nextFloat() * 5f);
			float radius3 = 200f + random.nextFloat() * 50f;
			float angle3 = random.nextFloat() * 360f;
			String type = "gas_giant";
			NamePick namePick = ProcgenUsedNames.pickName(NameGenData.TAG_PLANET, null, null);
			String name3 = namePick.nameWithRomanSuffixIfAny;
			lp_monument_planet2 = system.addPlanet(Misc.genUID(), system.getStar(), name3, type, angle3, radius3, orbitRadius3, orbitDays3);
			PlanetConditionGenerator.generateConditionsForPlanet(null, lp_monument_planet2, system.getAge());
			if (lp_monument_planet2 == null) {
				return;
			}
		}


			SectorEntityToken lp_blackhole_monument2 = system.addCustomEntity("lp_blackhole_monument2", "Hot Buoy", "hmi_blackhole_monument2", Factions.NEUTRAL);
			lp_blackhole_monument2.setCircularOrbitPointingDown(lp_monument_planet2, 120, 900, 90);
			lp_blackhole_monument2.setCustomDescriptionId("lp_blackhole_monument2");
			lp_blackhole_monument2.addTag("hmi_hmiblackluddmonument2");



		StarSystemData data2 = new StarSystemData();
		WeightedRandomPicker<String> hulls2 = new WeightedRandomPicker<String>(random);
		// add some random derelicts around a fringe jump-point
		float max = 0f;
		JumpPointAPI fringePoint = null;
		List<JumpPointAPI> points = system.getEntities(JumpPointAPI.class);
		for (JumpPointAPI curr : points) {
			float dist = curr.getCircularOrbitRadius();
			if (dist > max) {
				max = dist;
				fringePoint = curr;
			}
		}
		
		if (fringePoint != null) {
			data2 = new StarSystemData();
			WeightedRandomPicker<String> ScavShipFactions = new WeightedRandomPicker<String>(random);
			ScavShipFactions.add(Factions.SCAVENGERS);
			hulls2 = new WeightedRandomPicker<String>(random);
			hulls2.add("venture", 0.25f);
			hulls2.add("wayfarer", 1f);
			hulls2.add("wayfarer", 1f);
			hulls2.add("buffalo", 1f);
			hulls2.add("dram", 1f);
			hulls2.add("dram", 1f);
			hulls2.add("dram", 1f);
			hulls2.add("phaeton", 0.5f);
			hulls2.add("colossus", 0.5f);
			hulls2.add("apogee", 0.25f);
			hulls2.add("condor", 1f);
			hulls2.add("condor", 1f);
			hulls2.add("shepherd", 1f);
			hulls2.add("shepherd", 1f);
			hulls2.add("wolf", 0.5f);
			hulls2.add("hammerhead", 0.5f);
			addShipGraveyard(data2, fringePoint, ScavShipFactions, hulls2);
			addDebrisField(data2, fringePoint, 400f);
			
			for (AddedEntity ae : data2.generated) {
				SalvageSpecialAssigner.assignSpecials(ae.entity, true);
				if (ae.entity.getCustomPlugin() instanceof DerelictShipEntityPlugin) {
					DerelictShipEntityPlugin plugin = (DerelictShipEntityPlugin) ae.entity.getCustomPlugin();
					plugin.getData().ship.condition = ShipCondition.WRECKED;
				}
			}
		}
	}

	protected void addMysteryBlacksiteSystem(ThemeGenContext context) {
		if (DEBUG) System.out.println("Looking for Black Hole LP system");

		List<StarSystemAPI> preferred = new ArrayList<StarSystemAPI>();
		List<StarSystemAPI> other = new ArrayList<StarSystemAPI>();

		for (Constellation c : context.constellations) {
			for (StarSystemAPI system : c.getSystems()) {
				if (system.hasTag(Tags.THEME_SPECIAL)) continue;

				if (!system.hasBlackHole()) continue;

				boolean misc = system.hasTag(Tags.THEME_MISC_SKIP) || system.hasTag(Tags.THEME_MISC);
				if (system.hasTag(Tags.THEME_DERELICT)) misc = false;
				boolean nonLargeDerelict = system.hasTag(Tags.THEME_DERELICT) &&
						!system.hasTag(Tags.THEME_DERELICT_MOTHERSHIP) &&
						!system.hasTag(Tags.THEME_DERELICT_CRYOSLEEPER) &&
						!system.hasTag(Tags.THEME_DERELICT_SURVEY_SHIP);
				boolean remnantNoFleets = system.hasTag(Tags.THEME_REMNANT_NO_FLEETS);
				boolean unsafe = system.hasTag(Tags.THEME_UNSAFE);
				if (unsafe || !(misc || nonLargeDerelict || remnantNoFleets)) {
					continue;
				}

				int count = 0;
//				for (SectorEntityToken curr : system.getAllEntities()) {
//					if (curr.getId().equals("station_research") || curr.getId().equals("station_research_remnant"))
//					count++;
//				}
				for (PlanetAPI curr : system.getPlanets()) {
					if (curr.isStar()) continue;
					if (curr.isMoon()) continue;
					if (!curr.getMarket().isPlanetConditionMarketOnly()) continue;
					count++;
				}

				if (count > 2) {
					preferred.add(system);
				} else {
					other.add(system);
				}
			}
		}

		Comparator<StarSystemAPI> comp = new Comparator<StarSystemAPI>() {
			public int compare(StarSystemAPI o1, StarSystemAPI o2) {
				return (int) Math.signum(o2.getLocation().length() - o1.getLocation().length());
			}
		};

		List<StarSystemAPI> sorted = new ArrayList<StarSystemAPI>();
		if (!preferred.isEmpty()) {
			sorted.addAll(preferred);
		} else {
			sorted.addAll(other);
		}
		if (sorted.isEmpty()) {
			if (DEBUG) System.out.println("FAILED TO FIND SUITABLE SYSTEM FOR MYSTERY BLACK SITE");
			return;
		}
		Collections.sort(sorted, comp);


		// pick from some of the matching systems furthest from core
		WeightedRandomPicker<StarSystemAPI> picker = new WeightedRandomPicker<StarSystemAPI>(random);
		for (int i = 0; i < 20 && i < sorted.size(); i++) {
			//sorted.get(i).addTag(Tags.PK_SYSTEM);
			picker.add(sorted.get(i), 1f);
		}

		StarSystemAPI system = picker.pick();

		if (DEBUG) System.out.println("Adding Mystery Black Hole Black Site to [" + system.getName() + "] at [" + system.getLocation() + "]");
		setUpBlackHoleMysterySiteSystem(system);


		if (DEBUG) System.out.println("Finished adding Mystery Black Hole Black Site system\n\n\n\n\n");
	}

	public static class randomNumberGetter {

		private static Float randomNumber = null;
		public static Float getRandomNumber() {
			if (randomNumber == null) {
				randomNumber = MathUtils.getRandomNumberInRange(1f, 4f);; //whatver random number thing you wanna use; Courtesy of Ruddy and Atlantic
			}
			return randomNumber;
		}
	}

	protected void setUpBlackHoleMysterySiteSystem(StarSystemAPI system) {

		system.addTag(Tags.THEME_SPECIAL);
		system.addTag("HMI_MYSTERYBLACKHOLESITE_SYSTEM");
		system.addTag(Tags.THEME_UNSAFE);

		Global.getSector().getPersistentData().put(HMI_MYSTERYBLACKHOLESITE_SYSTEM_KEY, system);
		Global.getSector().getMemoryWithoutUpdate().set(HMI_MYSTERYBLACKHOLESITE_SYSTEM_KEY, system.getId());

		//Try and randomly select a ship here, maybe it'll work?

		if (random_var == null) {
			random_var = MathUtils.getRandomNumberInRange(1f, 3f);
		}

//		int shipselect = MathUtils.getRandomNumberInRange(1, 4);
//		Global.getSector().getMemoryWithoutUpdate().set(HMI_BLACKSITE_RANDOMSHIPSELECT_KEY, shipselect);



		//Set up Dockyards hidden in an Asteroid Belt

		SectorEntityToken station2 = null;
		List<OrbitGap> gaps = BaseThemeGenerator.findGaps(system.getCenter(), 2000, 20000, 800);
		float orbitRadius = 7000;
		if (!gaps.isEmpty()) {
			orbitRadius = (gaps.get(0).start + gaps.get(0).end) * 0.5f;
		}
		float radius = 500f + 200f * random.nextFloat();
		float area = radius * radius * 3.14f;
		int count = (int) (area / 80000f);
		count *= 2;
		if (count < 10) count = 10;
		if (count > 100) count = 100;
		float angle = random.nextFloat() * 360f;
		float orbitDays = orbitRadius / (20f + random.nextFloat() * 5f);

		SectorEntityToken field = system.addTerrain(Terrain.ASTEROID_FIELD,
				new AsteroidFieldParams(
						radius, // min radius
						radius + 100f, // max radius
						count, // min asteroid count
						count, // max asteroid count
						4f, // min asteroid radius
						16f, // max asteroid radius
						null)); // null for default name

		field.setCircularOrbit(system.getCenter(), angle, orbitRadius, orbitDays);

		station2 = BaseThemeGenerator.addSalvageEntity(system, Entities.STATION_RESEARCH, Factions.NEUTRAL);
		station2.setCircularOrbit(system.getCenter(), angle, orbitRadius, orbitDays);


		if (station2 == null) {
			if (DEBUG) System.out.println("FAILED TO CREATE MYSTERY DOCKYARDS IN SYSTEM");
			return;
		}

		station2.setName("Deep Space Dockworks");
		station2.setFaction(Factions.MERCENARY);
		station2.addTag(Tags.NOT_RANDOM_MISSION_TARGET);
		station2.setCustomDescriptionId("hmi_mysteryblacksite_stationdesc");
		station2.setId("hmi_mysteryblacksite_station_id");
		station2.addTag(HMI_MYSTERYBLACKHOLESITE_STATION_KEY);
		station2.getMemoryWithoutUpdate().set(HMI_MYSTERYBLACKHOLEDEFENDER_KEY, true);
		Misc.setDefenderOverride(station2, new DefenderDataOverride(Factions.MERCENARY, 1f, 20, 20, 1));
		CargoAPI extraStation0Salvage = Global.getFactory().createCargo(true);
		extraStation0Salvage.addSpecial(new SpecialItemData(Items.CORRUPTED_NANOFORGE, null), 1);;
		extraStation0Salvage.addCommodity(Commodities.METALS, 400f + random.nextInt(801));
		extraStation0Salvage.addCommodity(Commodities.RARE_METALS, 200f + random.nextInt(401));
		extraStation0Salvage.addCommodity(Commodities.HEAVY_MACHINERY, 100f + random.nextInt(101));
		extraStation0Salvage.addCommodity(Commodities.SUPPLIES, 200f + random.nextInt(201));
		extraStation0Salvage.addCommodity(Commodities.FUEL, 200f + random.nextInt(201));
		BaseSalvageSpecial.addExtraSalvage(extraStation0Salvage, station2.getMemoryWithoutUpdate(), -1);

		int maxFleets = 2 + this.random.nextInt(2);

		MysterySiteStationFleetManager BlackMercFleets = new MysterySiteStationFleetManager(
				station2, 1.0f, 0, maxFleets, 25.0f, 8, 16);
		system.addScript(BlackMercFleets);

		//Setting up Mining Station

		PlanetAPI mystery_blacksite_planet1 = null;

		for (PlanetAPI curr : system.getPlanets()) {
			if (curr.isStar()) continue;
			if (curr.isMoon()) continue;
			if (!curr.getMarket().isPlanetConditionMarketOnly()) continue;
			if (curr.getCircularOrbitRadius() < 6000) continue;
			mystery_blacksite_planet1 = curr;
			break;
		}

		if (mystery_blacksite_planet1 == null) {
			List<OrbitGap> gaps2 = BaseThemeGenerator.findGaps(system.getCenter(), 6000, 20000, 800);
			float orbitRadius2 = 6000;
			if (!gaps2.isEmpty()) {
				orbitRadius2 = (gaps2.get(0).start + gaps2.get(0).end) * 0.5f;
			}
			float orbitDays2 = orbitRadius2 / (20f + random.nextFloat() * 5f);
			float radius2 = 100f + random.nextFloat() * 50f;
			float angle2 = random.nextFloat() * 360f;
			String type = "rocky_metallic";
			NamePick namePick = ProcgenUsedNames.pickName(NameGenData.TAG_PLANET, null, null);
			String name2 = namePick.nameWithRomanSuffixIfAny;
			mystery_blacksite_planet1 = system.addPlanet(Misc.genUID(), system.getStar(), name2, type, angle2, radius2, orbitRadius2, orbitDays2);
			PlanetConditionGenerator.generateConditionsForPlanet(null, mystery_blacksite_planet1, system.getAge());
			if (mystery_blacksite_planet1 == null) {
				return;
			}
		}


		SectorEntityToken mystery_blacksite_mining_station = BaseThemeGenerator.addSalvageEntity(system, Entities.STATION_MINING, Factions.NEUTRAL);
		mystery_blacksite_mining_station.setCircularOrbitPointingDown(mystery_blacksite_planet1, 0, 500, 40);
		mystery_blacksite_mining_station.setName("Reactivated Mining Station");
		mystery_blacksite_mining_station.setId("hmi_mysteryblacksite_station_id");
		mystery_blacksite_mining_station.setFaction(Factions.MERCENARY);
		mystery_blacksite_mining_station.addTag(Tags.NOT_RANDOM_MISSION_TARGET);
		mystery_blacksite_mining_station.addTag("hmi_mystery_mining_station");
		mystery_blacksite_mining_station.setCustomDescriptionId("hmi_mystery_mining_station");


		mystery_blacksite_mining_station.addTag(HMI_MYSTERY_MINING_STATION_KEY);
		mystery_blacksite_mining_station.getMemoryWithoutUpdate().set(HMI_MYSTERY_MINING_STATION_KEY, true);
		Misc.setDefenderOverride(mystery_blacksite_mining_station, new DefenderDataOverride(Factions.MERCENARY, 1f, 50, 120, 300));
		CargoAPI extraStation1Salvage = Global.getFactory().createCargo(true);
		extraStation1Salvage.addSpecial(new SpecialItemData(Items.FULLERENE_SPOOL, null), 1);;
		extraStation1Salvage.addCommodity(Commodities.ORE, 200f + random.nextInt(401));
		extraStation1Salvage.addCommodity(Commodities.RARE_ORE, 100f + random.nextInt(201));
		extraStation1Salvage.addCommodity(Commodities.METALS, 50f + random.nextInt(51));
		extraStation1Salvage.addCommodity(Commodities.HEAVY_MACHINERY, 50f + random.nextInt(51));
		extraStation1Salvage.addCommodity(Commodities.FUEL, 50f + random.nextInt(51));
		extraStation1Salvage.addCommodity(Commodities.SUPPLIES, 100f + random.nextInt(101));
		BaseSalvageSpecial.addExtraSalvage(extraStation1Salvage, mystery_blacksite_mining_station.getMemoryWithoutUpdate(), -1);

		//Setting up Refining Station

		PlanetAPI mystery_blacksite_planet2 = null;

		for (PlanetAPI curr : system.getPlanets()) {
			if (curr.isStar()) continue;
			if (curr.isMoon()) continue;
			if (!curr.getMarket().isPlanetConditionMarketOnly()) continue;
			if (curr.getCircularOrbitRadius() > 6000) continue;
			mystery_blacksite_planet2 = curr;
			break;
		}

		if (mystery_blacksite_planet2 == null) {
			List<OrbitGap> gaps3 = BaseThemeGenerator.findGaps(system.getCenter(), 8000, 20000, 800);
			float orbitRadius3 = 9000;
			if (!gaps3.isEmpty()) {
				orbitRadius3 = (gaps3.get(0).start + gaps3.get(0).end) * 0.5f;
			}
			float orbitDays3 = orbitRadius3 / (20f + random.nextFloat() * 5f);
			float radius3 = 80f + random.nextFloat() * 50f;
			float angle3 = random.nextFloat() * 360f;
			String type = "rocky_ice";
			NamePick namePick = ProcgenUsedNames.pickName(NameGenData.TAG_PLANET, null, null);
			String name3 = namePick.nameWithRomanSuffixIfAny;
			mystery_blacksite_planet2 = system.addPlanet(Misc.genUID(), system.getStar(), name3, type, angle3, radius3, orbitRadius3, orbitDays3);
			PlanetConditionGenerator.generateConditionsForPlanet(null, mystery_blacksite_planet2, system.getAge());

			if (mystery_blacksite_planet2 == null) {
				return;
			}
		}


		SectorEntityToken mystery_blacksite_refining_station = BaseThemeGenerator.addSalvageEntity(system, Entities.STATION_MINING, Factions.NEUTRAL);
		mystery_blacksite_refining_station.setCircularOrbitPointingDown(mystery_blacksite_planet2, 120, 900, 90);

		mystery_blacksite_refining_station.setName("Makeshift Refinery");
		mystery_blacksite_refining_station.setFaction(Factions.MERCENARY);
		mystery_blacksite_refining_station.addTag(Tags.NOT_RANDOM_MISSION_TARGET);
		mystery_blacksite_refining_station.setCustomDescriptionId("hmi_mystery_refining_station");
		mystery_blacksite_refining_station.setId("mystery_blacksite_refining_station_id");
		mystery_blacksite_refining_station.addTag("mystery_blacksite_refining_station");
		mystery_blacksite_refining_station.addTag("hmi_mystery_refining_station");

		mystery_blacksite_refining_station.addTag(HMI_MYSTERY_REFINING_STATION_KEY);
		mystery_blacksite_refining_station.getMemoryWithoutUpdate().set(HMI_MYSTERY_REFINING_STATION_KEY, true);
		Misc.setDefenderOverride(mystery_blacksite_refining_station, new DefenderDataOverride(Factions.MERCENARY, 1f, 50, 120, 300));
		CargoAPI extraStation2Salvage = Global.getFactory().createCargo(true);
		extraStation2Salvage.addSpecial(new SpecialItemData(Items.CATALYTIC_CORE, null), 1);;
		extraStation2Salvage.addCommodity(Commodities.ORE, 50f + random.nextInt(51));
		extraStation2Salvage.addCommodity(Commodities.RARE_ORE, 50f + random.nextInt(51));
		extraStation2Salvage.addCommodity(Commodities.METALS, 200f + random.nextInt(400));
		extraStation2Salvage.addCommodity(Commodities.RARE_METALS, 100f + random.nextInt(200));
		extraStation2Salvage.addCommodity(Commodities.HEAVY_MACHINERY, 50f + random.nextInt(51));
		extraStation2Salvage.addCommodity(Commodities.SUPPLIES, 50f + random.nextInt(51));
		extraStation2Salvage.addCommodity(Commodities.FUEL, 50f + random.nextInt(51));
		BaseSalvageSpecial.addExtraSalvage(extraStation2Salvage, mystery_blacksite_refining_station.getMemoryWithoutUpdate(), -1);

		StarSystemData data = new StarSystemData();
		WeightedRandomPicker<String> hulls = new WeightedRandomPicker<String>(random);

		// add some random derelicts around jump-points
		float max = 0f;
		float min = 0f;
		JumpPointAPI fringePoint = null;
		JumpPointAPI innerPoint = null;
		List<JumpPointAPI> points = system.getEntities(JumpPointAPI.class);
		for (JumpPointAPI curr : points) {
			float dist = curr.getCircularOrbitRadius();
			if (dist > max) {
				max = dist;
				fringePoint = curr;
			}
			if (dist < min) {
				min = dist;
				innerPoint = curr;
			}
		}

		if (fringePoint != null) {
			data = new StarSystemData();
			WeightedRandomPicker<String> ScavShipFactions = new WeightedRandomPicker<String>(random);
			ScavShipFactions.add(Factions.SCAVENGERS);
			ScavShipFactions.add(Factions.HEGEMONY);
			hulls = new WeightedRandomPicker<String>(random);
			hulls.add("venture", 0.25f);
			hulls.add("wayfarer", 1f);
			hulls.add("wayfarer", 1f);
			hulls.add("buffalo_hegemony", 1f);
			hulls.add("dram", 1f);
			hulls.add("dram", 1f);
			hulls.add("dram", 1f);
			hulls.add("phaeton", 0.5f);
			hulls.add("colossus", 0.5f);
			hulls.add("venture", 0.25f);
			hulls.add("condor", 1f);
			hulls.add("condor", 1f);
			hulls.add("shepherd", 1f);
			hulls.add("shepherd", 1f);
			hulls.add("lasher", 0.5f);
			hulls.add("enforcer", 0.5f);
			addShipGraveyard(data, fringePoint, ScavShipFactions, hulls);
			addDebrisField(data, fringePoint, 400f);

			for (AddedEntity ae : data.generated) {
				SalvageSpecialAssigner.assignSpecials(ae.entity, true);
				if (ae.entity.getCustomPlugin() instanceof DerelictShipEntityPlugin) {
					DerelictShipEntityPlugin plugin = (DerelictShipEntityPlugin) ae.entity.getCustomPlugin();
					plugin.getData().ship.condition = ShipCondition.WRECKED;
				}
			}


			if (innerPoint != null) {
				data = new StarSystemData();
				WeightedRandomPicker<String> ScavShipFactions2 = new WeightedRandomPicker<String>(random);
				ScavShipFactions2.add(Factions.SCAVENGERS);
				ScavShipFactions2.add(Factions.PERSEAN);
				hulls = new WeightedRandomPicker<String>(random);
				hulls.add("apogee", 0.25f);
				hulls.add("wayfarer", 1f);
				hulls.add("mercury", 1f);
				hulls.add("buffalo", 1f);
				hulls.add("dram", 1f);
				hulls.add("dram", 1f);
				hulls.add("dram", 1f);
				hulls.add("phaeton", 0.5f);
				hulls.add("colossus", 0.5f);
				hulls.add("apogee", 0.25f);
				hulls.add("vigilance", 1f);
				hulls.add("vigilance", 1f);
				hulls.add("shepherd", 1f);
				hulls.add("shepherd", 1f);
				hulls.add("sunder", 0.5f);
				hulls.add("falcon", 0.5f);
				addShipGraveyard(data, fringePoint, ScavShipFactions2, hulls);
				addDebrisField(data, fringePoint, 400f);

				for (AddedEntity ae : data.generated) {
					SalvageSpecialAssigner.assignSpecials(ae.entity, true);
					if (ae.entity.getCustomPlugin() instanceof DerelictShipEntityPlugin) {
						DerelictShipEntityPlugin plugin = (DerelictShipEntityPlugin) ae.entity.getCustomPlugin();
						plugin.getData().ship.condition = ShipCondition.WRECKED;
					}
				}
			}

		}
	}

	protected void addRampantMothership(ThemeGenContext context) {
		if (DEBUG) System.out.println("Looking for Rampant Mothership system");

		List<StarSystemAPI> preferred = new ArrayList<StarSystemAPI>();
		List<StarSystemAPI> other = new ArrayList<StarSystemAPI>();

		for (Constellation c : context.constellations) {
			for (StarSystemAPI system : c.getSystems()) {
				if (system.hasTag(Tags.THEME_SPECIAL)) continue;

				if (system.hasPulsar()) continue;
				if (system.hasBlackHole()) continue;
				if (!system.hasTag(Tags.THEME_DERELICT)) continue;

				boolean misc = system.hasTag(Tags.THEME_MISC_SKIP) || system.hasTag(Tags.THEME_MISC);
				if (system.hasTag(Tags.THEME_DERELICT)) misc = false;

				boolean nonLargeDerelict = system.hasTag(Tags.THEME_DERELICT) &&
						!system.hasTag(Tags.THEME_DERELICT_MOTHERSHIP) &&
						!system.hasTag(Tags.THEME_DERELICT_CRYOSLEEPER);
				boolean unsafe = system.hasTag(Tags.THEME_UNSAFE);
				if (unsafe || !(misc || nonLargeDerelict)) {
					continue;
				}

				int count = 0;
//				for (SectorEntityToken curr : system.getAllEntities()) {
//					if (curr.getId().equals("station_research") || curr.getId().equals("station_research_remnant"))
//					count++;
//				}
				for (PlanetAPI curr : system.getPlanets()) {
					if (curr.isStar()) continue;
					if (curr.isMoon()) continue;
					if (curr.isGasGiant()) continue;
					if (!curr.getMarket().isPlanetConditionMarketOnly()) continue;
					if (curr.getCircularOrbitRadius() < 6000) continue;
					if (curr.hasTag(Tags.NOT_RANDOM_MISSION_TARGET)) continue;
					count++;
				}

				if (count > 0) {
					preferred.add(system);
				} else {
					other.add(system);
				}
			}
		}

		Comparator<StarSystemAPI> comp = new Comparator<StarSystemAPI>() {
			public int compare(StarSystemAPI o1, StarSystemAPI o2) {
				return (int) Math.signum(o2.getLocation().length() - o1.getLocation().length());
			}
		};

		List<StarSystemAPI> sorted = new ArrayList<StarSystemAPI>();
		if (!preferred.isEmpty()) {
			sorted.addAll(preferred);
		} else {
			sorted.addAll(other);
		}
		if (sorted.isEmpty()) {
			if (DEBUG) System.out.println("FAILED TO FIND SUITABLE SYSTEM FOR RAMPANT MOTHERSHIP LP");
			return;
		}
		Collections.sort(sorted, comp);


		// pick from some of the matching systems furthest from core
		WeightedRandomPicker<StarSystemAPI> picker = new WeightedRandomPicker<StarSystemAPI>(random);
		for (int i = 0; i < 20 && i < sorted.size(); i++) {
			//sorted.get(i).addTag(Tags.PK_SYSTEM);
			picker.add(sorted.get(i), 1f);
		}

		StarSystemAPI system = picker.pick();

		if (DEBUG) System.out.println("Adding Rampant Mothership to [" + system.getName() + "] at [" + system.getLocation() + "]");
		setUpRampantMothershipSystem(system);


		if (DEBUG) System.out.println("Finished adding Black Hole LP system\n\n\n\n\n");
	}

	protected void setUpRampantMothershipSystem(StarSystemAPI system) {
		system.addTag(Tags.THEME_SPECIAL);
		system.addTag("HMI_RAMPANTMOTHERSHIP_SYSTEM");
		system.addTag(Tags.THEME_UNSAFE);

		Global.getSector().getPersistentData().put(HMIRAMPANTMOTHERSHIP_SYSTEM_KEY, system);
		Global.getSector().getMemoryWithoutUpdate().set(HMIRAMPANTMOTHERSHIP_SYSTEM_KEY, system.getId());

		SectorEntityToken station = null;

		List<OrbitGap> gaps = BaseThemeGenerator.findGaps(system.getCenter(), 2000, 20000, 800);
		float orbitRadius = 7000;
		if (!gaps.isEmpty()) {
			orbitRadius = (gaps.get(0).start + gaps.get(0).end) * 0.5f;
		}
		float radius = 500f + 200f * random.nextFloat();
		float area = radius * radius * 3.14f;
		int count = (int) (area / 80000f);
		count *= 2;
		if (count < 10) count = 10;
		if (count > 100) count = 100;
		float angle = random.nextFloat() * 360f;
		float orbitDays = orbitRadius / (20f + random.nextFloat() * 5f);


		station = BaseThemeGenerator.addSalvageEntity(system, "derelict_rampant_mothership", Factions.DERELICT);
		station.setCircularOrbit(system.getCenter(), angle, orbitRadius, orbitDays);




		if (station == null) {
			if (DEBUG) System.out.println("FAILED TO CREATE RAMPANT MOTHERSHIP STATION IN SYSTEM");
			return;
		}

		station.setName("Rampant Domain Mothership");
		station.setFaction(Factions.DERELICT);
		station.addTag(Tags.NOT_RANDOM_MISSION_TARGET);
		station.setCustomDescriptionId("hmi_derelict_rampant_mothership_stationdesc");
		station.setId("derelict_rampant_mothership_station_id");
		station.addTag(HMIRAMPANT_STATION_KEY);

		int maxFleets = 4 + this.random.nextInt(2);
		HMIRAMPANTStationFleetManager RampantFleets = new HMIRAMPANTStationFleetManager(
				station, 1.0f, 0, maxFleets, 25.0f, 10, 14);
		system.addScript(RampantFleets);

		for (SectorEntityToken curr : system.getEntitiesWithTag(Tags.STABLE_LOCATION)) {
			system.removeEntity(curr);
		}
		for (SectorEntityToken curr : system.getEntitiesWithTag(Tags.OBJECTIVE)) {
			system.removeEntity(curr);
		}

		//Setting up the Mercenary Graveyard
	}
}
















