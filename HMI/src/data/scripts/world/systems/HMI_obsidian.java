package data.scripts.world.systems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.procgen.DefenderDataOverride;
import com.fs.starfarer.api.impl.campaign.procgen.NebulaEditor;
import com.fs.starfarer.api.impl.campaign.procgen.themes.DerelictThemeGenerator;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.util.Misc;
import data.campaign.fleets.MessFleetManager;

import java.awt.*;
import java.util.Random;

public class HMI_obsidian implements SectorGeneratorPlugin {

	private static final Random random = new Random();
    public static SectorEntityToken getSectorAccess() {
        return Global.getSector().getStarSystem("Obsidian").getEntityById("obsidian_star");
    }

	@Override
	public void generate(SectorAPI sector) {
		StarSystemAPI system = sector.createStarSystem("Obsidian");
		LocationAPI hyper = Global.getSector().getHyperspace();		
		system.setBackgroundTextureFilename("graphics/backgrounds/background2.jpg");

		PlanetAPI star = system.initStar("obsidian_star", // unique id for this star
										 StarTypes.RED_GIANT, // id in planets.json
										 800f,		// radius (in pixels at default zoom)
										 400,
							5f, // solar wind burn level
							0.2f, // flare probability
							4.0f); // CR loss multiplier, good values are in the range of 1-5
		
		system.setLightColor(new Color(225, 100, 100)); // light color in entire system, affects all entities
		system.getLocation().set(-27000, -27000);
		

        PlanetAPI obsidian1 = system.addPlanet("feldspar", star, "Feldspar", "lava", 245, 85, 1600, 200);

        Misc.initConditionMarket(obsidian1);
		obsidian1.getMarket().setSurveyLevel(MarketAPI.SurveyLevel.FULL);
		obsidian1.getMarket().addCondition(Conditions.VERY_HOT);
		obsidian1.getMarket().addCondition(Conditions.EXTREME_TECTONIC_ACTIVITY);
		obsidian1.getMarket().addCondition(Conditions.RARE_ORE_MODERATE);
		obsidian1.getMarket().addCondition(Conditions.ORE_MODERATE);
		obsidian1.getMarket().addCondition(Conditions.TOXIC_ATMOSPHERE);


        PlanetAPI obsidian2 = system.addPlanet("pyrite", star, "Pyrite", "rocky_metallic", 120, 125, 2000, 150);
		obsidian2.getSpec().setGlowTexture(Global.getSettings().getSpriteName("hab_glows", "asharu"));
		obsidian2.getSpec().setGlowColor(new Color(255,245,235,255));
		obsidian2.getSpec().setUseReverseLightForGlow(true);
		obsidian2.getSpec().setAtmosphereThicknessMin(25);
		obsidian2.getSpec().setAtmosphereThickness(0.2f);
		obsidian2.getSpec().setAtmosphereColor( new Color(80,90,100,120) );
		obsidian2.applySpecChanges();
        obsidian2.setCustomDescriptionId("hmi_pyrite");
		obsidian2.setInteractionImage("illustrations", "facility_explosion");		

        PlanetAPI obsidian3 = system.addPlanet("cinnibar", star, "Cinnibar", "lava", 80, 65, 2600, 365);
        Misc.initConditionMarket(obsidian3);
		obsidian3.getMarket().setSurveyLevel(MarketAPI.SurveyLevel.FULL);
		obsidian3.getMarket().addCondition(Conditions.HOT);
		obsidian3.getMarket().addCondition(Conditions.TECTONIC_ACTIVITY);
		obsidian3.getMarket().addCondition(Conditions.RARE_ORE_SPARSE);
		obsidian3.getMarket().addCondition(Conditions.ORE_ULTRARICH);
		obsidian3.getMarket().addCondition(Conditions.TOXIC_ATMOSPHERE);

        PlanetAPI obsidian4 = system.addPlanet("bauxite", star, "Bauxite", "barren-bombarded", 210, 95, 3500, 365);
		obsidian4.setCustomDescriptionId("hmi_bauxite");
		obsidian4.setInteractionImage("illustrations", "abandoned_station");

		system.addAsteroidBelt(star, 200, 3300, 512, 150, 325, Terrain.ASTEROID_BELT, "The Shards");
		system.addAsteroidBelt(star, 200, 3700, 512, 150, 400, Terrain.ASTEROID_BELT, "The Shards");
		system.addRingBand(star, "misc", "rings_dust0", 256f, 0, Color.white, 512f, 3200, 80f);
		system.addRingBand(star, "misc", "rings_dust0", 256f, 1, Color.white, 512f, 3400, 100f);
		system.addRingBand(star, "misc", "rings_dust0", 256f, 1, Color.white, 512f, 3600, 100f);

		PlanetAPI obsidian5 = system.addPlanet("bornite", star, "Bornite", "barren", 120, 200, 5850, 100);
		obsidian5.setCustomDescriptionId("hmi_bornite");
		obsidian5.setInteractionImage("illustrations", "abandoned_station");

		PlanetAPI obsidian6 = system.addPlanet("galena", star, "Galena", "hmi_crystalline", 80, 165, 9000, 480);
        obsidian6.setCustomDescriptionId("hmi_galena");
		obsidian6.setInteractionImage("illustrations", "crystalline");

		system.addAsteroidBelt(star, 100, 12300, 256, 150, 325, Terrain.ASTEROID_BELT, "The Shredders");
		system.addAsteroidBelt(star, 100, 12700, 256, 150, 400, Terrain.ASTEROID_BELT, "The Shredders");
		system.addRingBand(star, "misc", "rings_dust0", 256f, 0, Color.white, 256f, 12200, 80f);
		system.addRingBand(star, "misc", "rings_dust0", 256f, 1, Color.white, 512f, 12400, 100f);
		system.addRingBand(star, "misc", "rings_dust0", 256f, 1, Color.white, 256f, 12600, 100f);


		PlanetAPI obsidian7 = system.addPlanet("anthracite", star, "Anthracite", "barren-bombarded", 0, 125, 14000, 520);
		obsidian7.setCustomDescriptionId("hmi_anthracite");
		obsidian7.setInteractionImage("illustrations", "mine");


		SectorEntityToken scrap1 = DerelictThemeGenerator.addSalvageEntity(system, Entities.EQUIPMENT_CACHE, Factions.DERELICT);
		scrap1.setId("fang_scrap1");
		scrap1.setCircularOrbit(star, 200, 8400, 200);
		Misc.setDefenderOverride(scrap1, new DefenderDataOverride(Factions.DERELICT, 0, 0, 0));
		scrap1.setDiscoverable(Boolean.TRUE);

		SectorEntityToken scrap2 = DerelictThemeGenerator.addSalvageEntity(system, Entities.SUPPLY_CACHE_SMALL, Factions.DERELICT);
		scrap2.setId("fang_scrap2");
		scrap2.setCircularOrbit(star, 340, 8200, 200);
		Misc.setDefenderOverride(scrap2, new DefenderDataOverride(Factions.DERELICT, 0, 0, 0));
		scrap2.setDiscoverable(Boolean.TRUE);

		SectorEntityToken scrap3 = DerelictThemeGenerator.addSalvageEntity(system, Entities.SUPPLY_CACHE, Factions.DERELICT);
		scrap3.setId("fang_scrap3");
		scrap3.setCircularOrbit(star, 90, 8600, 200);
		Misc.setDefenderOverride(scrap3, new DefenderDataOverride(Factions.DERELICT, 0, 0, 0));
		scrap3.setDiscoverable(Boolean.TRUE);

		JumpPointAPI jumpPoint = Global.getFactory().createJumpPoint("obsidian_jump_point1", "Obsidian Inner Jump-point");
		OrbitAPI orbit = Global.getFactory().createCircularOrbit(star, 0, 1400, 100);
		jumpPoint.setOrbit(orbit);
		orbit.setEntity(jumpPoint);
		jumpPoint.setRelatedPlanet(obsidian1);
		jumpPoint.setStandardWormholeToHyperspaceVisual();
		system.addEntity(jumpPoint);


		JumpPointAPI jumpPointOuter = Global.getFactory().createJumpPoint("obsidian_jump_point2", "Obsidian Outer Jump-point");
		OrbitAPI orbit_outer = Global.getFactory().createCircularOrbit(obsidian7, 0, 800, 60);
		jumpPointOuter.setOrbit(orbit_outer);
		orbit_outer.setEntity(jumpPointOuter);
		jumpPointOuter.setRelatedPlanet(obsidian7);
		jumpPointOuter.setStandardWormholeToHyperspaceVisual();
		system.addEntity(jumpPointOuter);

		system.autogenerateHyperspaceJumpPoints(true, true);
		orbit.setEntity(jumpPoint);
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
}
