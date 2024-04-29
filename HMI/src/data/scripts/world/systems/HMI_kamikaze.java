package data.scripts.world.systems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.procgen.DefenderDataOverride;
import com.fs.starfarer.api.impl.campaign.procgen.NebulaEditor;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.DerelictThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.SalvageSpecialAssigner;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.MagneticFieldTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.StarCoronaTerrainPlugin;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.lazylib.MathUtils;

import java.awt.*;

public class HMI_kamikaze implements SectorGeneratorPlugin {

    public static SectorEntityToken getSectorAccess() {
        return Global.getSector().getStarSystem("Kamikaze").getEntityById("kamikaze_star");
    }

	@Override
	public void generate(SectorAPI sector) {
		StarSystemAPI system = sector.createStarSystem("Kamikaze");
		LocationAPI hyper = Global.getSector().getHyperspace();
		system.setBackgroundTextureFilename("graphics/backgrounds/background6.jpg");

		PlanetAPI star = system.initStar("kamikaze_star",
				 						 StarTypes.NEUTRON_STAR, // id in planets.json
										 250f, 		// radius (in pixels at default zoom)
										 2000,
										20f, // solar wind burn level
										1f, // flare probability
										6.0f); // CR loss multiplier, good values are in the range of 1-5); // corona radius, from star edge

		system.setLightColor(new Color(255, 255, 255)); // light color in entire system, affects all entities
		system.getLocation().set(-21500, -25000);


		SectorEntityToken kamikazeHorizon = system.addTerrain(Terrain.PULSAR_BEAM,
				new StarCoronaTerrainPlugin.CoronaParams(27500,
						12500,
						star,
						2f,
						0f,
						2f)
		);
		kamikazeHorizon.setCircularOrbit(star, 0, 0, 15);

		SectorEntityToken kamikaze2Horizon = system.addTerrain(Terrain.PULSAR_BEAM,
				new StarCoronaTerrainPlugin.CoronaParams(27500,
						12500,
						star,
						2f,
						0f,
						2f)
		);
		kamikaze2Horizon.setCircularOrbit(star, 50, 0, 15);

		SectorEntityToken kamikaze3Horizon = system.addTerrain(Terrain.PULSAR_BEAM,
				new StarCoronaTerrainPlugin.CoronaParams(27500,
						12500,
						star,
						2f,
						0f,
						2f)
		);
		kamikaze3Horizon.setCircularOrbit(star, 100, 0, 15);

		SectorEntityToken kamikaze4Horizon = system.addTerrain(Terrain.PULSAR_BEAM,
				new StarCoronaTerrainPlugin.CoronaParams(27500,
						12500,
						star,
						2f,
						0f,
						2f)
		);
		kamikaze4Horizon.setCircularOrbit(star, 150, 0, 15);

		SectorEntityToken kamikaze5Horizon = system.addTerrain(Terrain.PULSAR_BEAM,
				new StarCoronaTerrainPlugin.CoronaParams(27500,
						12500,
						star,
						2f,
						0f,
						2f)
		);
		kamikaze5Horizon.setCircularOrbit(star, 200, 0, 15);

		SectorEntityToken kamikaze6Horizon = system.addTerrain(Terrain.PULSAR_BEAM,
				new StarCoronaTerrainPlugin.CoronaParams(27500,
						12500,
						star,
						2f,
						0f,
						2f)
		);
		kamikaze6Horizon.setCircularOrbit(star, 250, 0, 15);

        PlanetAPI kamikaze1 = system.addPlanet("eye", star, "Storm's Eye", "irradiated", 90, 25, 2000, 50);

		MarketAPI kamikaze1PlanetMarket = Global.getFactory().createMarket("kamikaze1_conditions", kamikaze1.getName(), 0);
		kamikaze1PlanetMarket.setPrimaryEntity(kamikaze1);
		kamikaze1PlanetMarket.setSurveyLevel(MarketAPI.SurveyLevel.FULL);
		kamikaze1PlanetMarket.addCondition(Conditions.TECTONIC_ACTIVITY);
		kamikaze1PlanetMarket.addCondition(Conditions.HOT);
		kamikaze1PlanetMarket.addCondition(Conditions.IRRADIATED);
		kamikaze1PlanetMarket.addCondition(Conditions.NO_ATMOSPHERE);
		kamikaze1PlanetMarket.addCondition(Conditions.RUINS_EXTENSIVE);

		kamikaze1PlanetMarket.setPlanetConditionMarketOnly(true);
		kamikaze1.setMarket(kamikaze1PlanetMarket);

		SectorEntityToken kamikaze_basefield = system.addTerrain(Terrain.MAGNETIC_FIELD,
				new MagneticFieldTerrainPlugin.MagneticFieldParams(200f, // terrain effect band width
						2250f, // terrain effect middle radius
						star, // entity that it's around
						2000f, // visual band start
						2500f, // visual band end
						new Color(50, 20, 100, 50), // base color
						1f, // probability to spawn aurora sequence, checked once/day when no aurora in progress
						new Color(50, 20, 110, 130),
						new Color(150, 30, 120, 150),
						new Color(200, 50, 130, 190),
						new Color(250, 70, 150, 240),
						new Color(200, 80, 130, 255),
						new Color(75, 0, 160),
						new Color(127, 0, 255)
				));
		kamikaze_basefield.setCircularOrbit(star, 0, 0, 100);

		SectorEntityToken kamikaze_basefield2 = system.addTerrain(Terrain.MAGNETIC_FIELD,
				new MagneticFieldTerrainPlugin.MagneticFieldParams(500f, // terrain effect band width
						5250f, // terrain effect middle radius
						star, // entity that it's around
						5000f, // visual band start
						5500f, // visual band end
						new Color(50, 20, 100, 50), // base color
						1f, // probability to spawn aurora sequence, checked once/day when no aurora in progress
						new Color(50, 20, 110, 130),
						new Color(150, 30, 120, 150),
						new Color(200, 50, 130, 190),
						new Color(250, 70, 150, 240),
						new Color(200, 80, 130, 255),
						new Color(75, 0, 160),
						new Color(127, 0, 255)
				));
		kamikaze_basefield2.setCircularOrbit(star, 0, 0, 100);

		addDerelict(system, kamikaze1, "tempest_Attack", ShipRecoverySpecial.ShipCondition.BATTERED, 270f,  (Math.random()<0.6));
		addDerelict(system, kamikaze1, "scarab_Starting", ShipRecoverySpecial.ShipCondition.BATTERED, 270f,  (Math.random()<0.6));
		addDerelict(system, kamikaze1, "scarab_Starting", ShipRecoverySpecial.ShipCondition.BATTERED, 270f,  (Math.random()<0.6));
		addDerelict(system, kamikaze1, "medusa_Attack", ShipRecoverySpecial.ShipCondition.BATTERED, 125f, (Math.random()<0.6));
		addDerelict(system, kamikaze1, "medusa_Attack", ShipRecoverySpecial.ShipCondition.BATTERED, 125f, (Math.random()<0.6));
		addDerelict(system, kamikaze1, "aurora_Balanced", ShipRecoverySpecial.ShipCondition.BATTERED, 85f, (Math.random()<0.6));
		addDerelict(system, kamikaze1, "onslaught_xiv_Elite", ShipRecoverySpecial.ShipCondition.BATTERED, 175f, (Math.random()<0.6));
		addDerelict(system, kamikaze1, "dominator_XIV_Elite", ShipRecoverySpecial.ShipCondition.BATTERED, 100f, (Math.random()<0.6));
		addDerelict(system, kamikaze1, "dominator_XIV_Elite", ShipRecoverySpecial.ShipCondition.BATTERED, 100f, (Math.random()<0.6));
		addDerelict(system, kamikaze1, "champion_Elite", ShipRecoverySpecial.ShipCondition.BATTERED, 250f, (Math.random()<0.6));
		addDerelict(system, kamikaze1, "enforcer_XIV_Elite", ShipRecoverySpecial.ShipCondition.BATTERED, 250f, (Math.random()<0.6));
		addDerelict(system, kamikaze1, "enforcer_XIV_Elite", ShipRecoverySpecial.ShipCondition.BATTERED, 250f, (Math.random()<0.6));

		SectorEntityToken scrap1 = DerelictThemeGenerator.addSalvageEntity(system, Entities.EQUIPMENT_CACHE, Factions.DERELICT);
		scrap1.setId("kamikaze_scrap1");
		scrap1.setCircularOrbit(kamikaze1, 88, 420, 50);
		Misc.setDefenderOverride(scrap1, new DefenderDataOverride(Factions.DERELICT, 0, 0, 0));
		scrap1.setDiscoverable(Boolean.TRUE);

		SectorEntityToken scrap2 = DerelictThemeGenerator.addSalvageEntity(system, Entities.WEAPONS_CACHE, Factions.DERELICT);
		scrap2.setId("kamikaze_scrap2");
		scrap2.setCircularOrbit(kamikaze1, 92, 500, 50);
		Misc.setDefenderOverride(scrap2, new DefenderDataOverride(Factions.DERELICT, 0, 0, 0));
		scrap2.setDiscoverable(Boolean.TRUE);

		SectorEntityToken scrap3 = DerelictThemeGenerator.addSalvageEntity(system, Entities.WEAPONS_CACHE, Factions.DERELICT);
		scrap3.setId("kamikaze_scrap3");
		scrap3.setCircularOrbit(kamikaze1, 90, 50, 50);
		Misc.setDefenderOverride(scrap3, new DefenderDataOverride(Factions.DERELICT, 0, 0, 0));
		scrap3.setDiscoverable(Boolean.TRUE);


		PlanetAPI kamikaze2 = system.addPlanet("hurricane", star, "Hurricane", "gas_giant", 240, 500, 15000, 600);
		kamikaze2.getSpec().setPlanetColor(new Color(127, 0, 255, 255));
		kamikaze2.getSpec().setGlowTexture(Global.getSettings().getSpriteName("hab_glows", "aurorae"));
		kamikaze2.getSpec().setGlowColor(new Color(50, 20, 100, 50));
		kamikaze2.getSpec().setUseReverseLightForGlow(true);
		kamikaze2.getSpec().setAtmosphereThickness(0.5f);
		kamikaze2.getSpec().setCloudRotation( 10f );
		kamikaze2.getSpec().setPitch(20);
		kamikaze2.getSpec().setAtmosphereThicknessMin(80);
		kamikaze2.getSpec().setAtmosphereThickness(0.30f);
		kamikaze2.getSpec().setAtmosphereColor(new Color(250, 70, 150, 240));
		kamikaze2.applySpecChanges();

		MarketAPI kamikaze2PlanetMarket = Global.getFactory().createMarket("kamikaze2_conditions", kamikaze2.getName(), 0);
		kamikaze2PlanetMarket.setPrimaryEntity(kamikaze2);
		kamikaze2PlanetMarket.setSurveyLevel(MarketAPI.SurveyLevel.FULL);
		kamikaze2PlanetMarket.addCondition(Conditions.HIGH_GRAVITY);
		kamikaze2PlanetMarket.addCondition(Conditions.EXTREME_WEATHER);
		kamikaze2PlanetMarket.addCondition(Conditions.IRRADIATED);
		kamikaze2PlanetMarket.addCondition(Conditions.DENSE_ATMOSPHERE);
		kamikaze2PlanetMarket.addCondition(Conditions.VOLATILES_ABUNDANT);

		kamikaze2PlanetMarket.setPlanetConditionMarketOnly(true);
		kamikaze2.setMarket(kamikaze2PlanetMarket);

		SectorEntityToken kamikaze2_field = system.addTerrain(Terrain.MAGNETIC_FIELD,
				new MagneticFieldTerrainPlugin.MagneticFieldParams(200f, // terrain effect band width
						750, // terrain effect middle radius
						kamikaze2, // entity that it's around
						500f, // visual band start
						1000f, // visual band end
						new Color(50, 30, 100, 30), // base color
						1f, // probability to spawn aurora sequence, checked once/day when no aurora in progress
						new Color(50, 20, 110, 130),
						new Color(150, 30, 120, 150),
						new Color(200, 50, 130, 190),
						new Color(250, 70, 150, 240),
						new Color(200, 80, 130, 255),
						new Color(75, 0, 160),
						new Color(127, 0, 255)
				));
		kamikaze2_field.setCircularOrbit(kamikaze2, 0, 0, 100);

		SectorEntityToken kamikazestation1 = system.addCustomEntity("hurricane_station", "Fuyutsuki's Folly", "station_side02", "HMI");
		kamikazestation1.setCircularOrbitPointingDown(system.getEntityById("hurricane"), 240, 1100, 600);
		kamikazestation1.setInteractionImage("illustrations", "orbital");
		kamikazestation1.setCustomDescriptionId("hmi_hurricane_station");

		JumpPointAPI jumpPoint = Global.getFactory().createJumpPoint("kamikaze2_jump_point1", "Inner Jump-point");
		OrbitAPI orbit = Global.getFactory().createCircularOrbit(kamikaze2, 240, 3000, 600);
		jumpPoint.setOrbit(orbit);
		jumpPoint.setRelatedPlanet(kamikaze2);
		jumpPoint.setStandardWormholeToHyperspaceVisual();
		system.addEntity(jumpPoint);
			
		PlanetAPI kamikaze3 = system.addPlanet("typhoon", star, "Typhoon", "rocky_metallic", 120, 180, 20000, 700);

		MarketAPI kamikaze3PlanetMarket = Global.getFactory().createMarket("kamikaze3_conditions", kamikaze3.getName(), 0);
		kamikaze3PlanetMarket.setPrimaryEntity(kamikaze3);
		kamikaze3PlanetMarket.setSurveyLevel(MarketAPI.SurveyLevel.FULL);
		kamikaze3PlanetMarket.addCondition(Conditions.IRRADIATED);
		kamikaze3PlanetMarket.addCondition(Conditions.VERY_COLD);
		kamikaze3PlanetMarket.addCondition(Conditions.NO_ATMOSPHERE);
		kamikaze3PlanetMarket.addCondition(Conditions.EXTREME_TECTONIC_ACTIVITY);
		kamikaze3PlanetMarket.addCondition(Conditions.ORE_MODERATE);
		kamikaze3PlanetMarket.addCondition(Conditions.RARE_ORE_SPARSE);

		kamikaze3PlanetMarket.setPlanetConditionMarketOnly(true);
		kamikaze3.setMarket(kamikaze3PlanetMarket);

		SectorEntityToken kamikaze3_field = system.addTerrain(Terrain.MAGNETIC_FIELD,
				new MagneticFieldTerrainPlugin.MagneticFieldParams(200f, // terrain effect band width
						180f, // terrain effect middle radius
						kamikaze3, // entity that it's around
						110f, // visual band start
						260f, // visual band end
						new Color(50, 20, 100, 50), // base color
						1f, // probability to spawn aurora sequence, checked once/day when no aurora in progress
						new Color(50, 20, 110, 130),
						new Color(150, 30, 120, 150),
						new Color(200, 50, 130, 190),
						new Color(250, 70, 150, 240),
						new Color(200, 80, 130, 255),
						new Color(75, 0, 160),
						new Color(127, 0, 255)
				));
		kamikaze3_field.setCircularOrbit(kamikaze1, 0, 0, 100);

		SectorEntityToken kamikazestation3 = system.addCustomEntity("typhoon_station", "Shaded Cove", "station_side06", "pirates");
		kamikazestation3.setCircularOrbitPointingDown(system.getEntityById("typhoon"), 120, 1100, 700);
		kamikazestation3.setInteractionImage("illustrations", "pirate_station");
		kamikazestation3.setCustomDescriptionId("hmi_typhoon_station");

		PlanetAPI kamikaze4 = system.addPlanet("tornado", star, "Cyclone", "cryovolcanic", 60, 300, 24000, 1200);

		MarketAPI kamikaze4PlanetMarket = Global.getFactory().createMarket("kamikaze4_conditions", kamikaze4.getName(), 0);
		kamikaze4PlanetMarket.setPrimaryEntity(kamikaze4);
		kamikaze4PlanetMarket.setSurveyLevel(MarketAPI.SurveyLevel.FULL);
		kamikaze4PlanetMarket.addCondition(Conditions.IRRADIATED);
		kamikaze4PlanetMarket.addCondition(Conditions.VERY_COLD);
		kamikaze3PlanetMarket.addCondition(Conditions.EXTREME_TECTONIC_ACTIVITY);
		kamikaze4PlanetMarket.addCondition(Conditions.NO_ATMOSPHERE);
		kamikaze4PlanetMarket.addCondition(Conditions.VOLATILES_DIFFUSE);
		kamikaze4PlanetMarket.addCondition(Conditions.ORE_SPARSE);

		kamikaze4PlanetMarket.setPlanetConditionMarketOnly(true);
		kamikaze4.setMarket(kamikaze4PlanetMarket);

		SectorEntityToken kamikaze4_field = system.addTerrain(Terrain.MAGNETIC_FIELD,
				new MagneticFieldTerrainPlugin.MagneticFieldParams(200f, // terrain effect band width
						500f, // terrain effect middle radius
						kamikaze4, // entity that it's around
						300f, // visual band start
						700f, // visual band end
						new Color(50, 20, 100, 50), // base color
						1f, // probability to spawn aurora sequence, checked once/day when no aurora in progress
						new Color(50, 20, 110, 130),
						new Color(150, 30, 120, 150),
						new Color(200, 50, 130, 190),
						new Color(250, 70, 150, 240),
						new Color(200, 80, 130, 255),
						new Color(75, 0, 160),
						new Color(127, 0, 255)
				));
		kamikaze4_field.setCircularOrbit(kamikaze1, 0, 0, 100);

		SectorEntityToken kamikazestation4 = system.addCustomEntity("tornado_station", "Port Rascal", "station_side06", "pirates");
		kamikazestation4.setCircularOrbitPointingDown(system.getEntityById("tornado"), 60, 600, 1200);
		kamikazestation4.setInteractionImage("illustrations", "pirate_station");
		kamikazestation4.setCustomDescriptionId("hmi_tornado_station");



		JumpPointAPI jumpPoint1 = Global.getFactory().createJumpPoint("kamikaze4_jump_point1", "Outer Jump-point");
		OrbitAPI orbit1 = Global.getFactory().createCircularOrbit(kamikaze4, 60, 3000, 1200);
		jumpPoint1.setOrbit(orbit1);
		jumpPoint1.setStandardWormholeToHyperspaceVisual();
		system.addEntity(jumpPoint1);

		system.autogenerateHyperspaceJumpPoints(true, true);
		cleanup(system);
	}

	void cleanup(StarSystemAPI system){
		HyperspaceTerrainPlugin plugin = (HyperspaceTerrainPlugin) Misc.getHyperspaceTerrain().getPlugin();
		NebulaEditor editor = new NebulaEditor(plugin);
		float minRadius = plugin.getTileSize() * 2f;

		float radius = system.getMaxRadiusInHyperspace();
		editor.clearArc(system.getLocation().x, system.getLocation().y, 0, radius + minRadius * 0.5f, 0, 360f);
		editor.clearArc(system.getLocation().x, system.getLocation().y, 0, radius + minRadius, 0, 360f, 0.25f);
	}

	protected void addDerelict(StarSystemAPI system,
							   SectorEntityToken focus,
							   String variantId,
							   ShipRecoverySpecial.ShipCondition condition,
							   float orbitRadius,
							   boolean recoverable) {
		DerelictShipEntityPlugin.DerelictShipData params = new DerelictShipEntityPlugin.DerelictShipData(new ShipRecoverySpecial.PerShipData(variantId, condition), true);
		SectorEntityToken ship = BaseThemeGenerator.addSalvageEntity(system, Entities.WRECK, Factions.NEUTRAL, params);
		ship.setDiscoverable(true);

		float orbitDays = 50f;
		ship.setCircularOrbit(focus, (float) MathUtils.getRandomNumberInRange(-2,2) + 90f, orbitRadius, orbitDays);

		if (recoverable) {
			SalvageSpecialAssigner.ShipRecoverySpecialCreator creator = new SalvageSpecialAssigner.ShipRecoverySpecialCreator(null, 0, 0, false, null, null);
			Misc.setSalvageSpecial(ship, creator.createSpecial(ship, null));
		}
	}
	
}
