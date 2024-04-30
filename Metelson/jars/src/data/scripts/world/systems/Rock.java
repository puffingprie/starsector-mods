package data.scripts.world.systems;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.JumpPointAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.procgen.StarAge;
import java.util.ArrayList;
import java.util.List;

public class Rock
{
  public Rock() {}
  
  public void generate(SectorAPI sector)
  {
    StarSystemAPI system = sector.createStarSystem("Rock");
    com.fs.starfarer.api.campaign.LocationAPI hyper = Global.getSector().getHyperspace();
    
    system.setBackgroundTextureFilename("graphics/backgrounds/background4.jpg");
    
    SectorEntityToken rock_nebula = com.fs.starfarer.api.util.Misc.addNebulaFromPNG
        ("data/campaign/terrain/rock_nebula.png", 0.0F, 0.0F, system, "terrain", "nebula_amber", 4, 4, StarAge.ANY);
    


    PlanetAPI rock_star = system.initStar("rock", "star_red_dwarf", 125.0F, 175.0F);
    
//reminder to self:PlanetAPI rock2ab = system.addPlanet("anvil", rock2a, "Anvil", "barren", 30.0F, SIZE.0F, 25.0F, 60.0F);
// Sparkel sparkel

    system.setLightColor(new Color(255, 210, 200));
    rock_star.setCustomDescriptionId("star_red_rock");
    

//buncha' rings around Rock star that act as the "Gotcha" for hiding players as both players and the enties in-system can hide.
    system.addRingBand(rock_star, "misc", "rings_dust0", 256.0F, 2, Color.white, 256.0F, 2000.0F, 80.0F);
    system.addRingBand(rock_star, "misc", "rings_ice0", 256.0F, 3, Color.blue, 256.0F, 1400.0F, 100.0F);
    system.addRingBand(rock_star, "misc", "rings_dust0", 256.0F, 2, Color.white, 256.0F, 1600.0F, 130.0F);
    system.addRingBand(rock_star, "misc", "rings_dust0", 256.0F, 3, Color.blue, 800.0F, 1000.0F, 80.0F);
    system.addRingBand(rock_star, "misc", "rings_dust0", 256.0F, 2, Color.white, 556.0F, 2200.0F, 80.0F);
    system.addRingBand(rock_star, "misc", "rings_ice0", 256.0F, 3, Color.blue, 256.0F, 2400.0F, 100.0F);
    system.addRingBand(rock_star, "misc", "rings_dust0", 256.0F, 2, Color.white, 360.0F, 3500.0F, 130.0F);
    system.addRingBand(rock_star, "misc", "rings_ice0", 256.0F, 3, Color.white, 456.0F, 2800.0F, 80.0F);
    

    system.addAsteroidBelt(rock_star, 300, 3000.0F, 2000.0F, 200.0F, 365.0F);
    
    SectorEntityToken ring = system.addTerrain("ring", new com.fs.starfarer.api.impl.campaign.terrain.BaseRingTerrain.RingParams(3006.0F, 2200.0F, null, "Rock Dust"));
    ring.setCircularOrbit(rock_star, 0.0F, 0.0F, 100.0F);    

    SectorEntityToken gate = system.addCustomEntity("stone_gate", "Stone Gate", "inactive_gate", null);    
    gate.setCircularOrbit(rock_star, 240.0F, 9000.0F, 720.0F);
    


///Make Viymese SOMEWHAT liveable... Might be better as a tundra world instead.
    PlanetAPI rock1 = system.addPlanet("viymese", rock_star, "Viymese", "tundra", 90.0F, 130.0F, 4000.0F, 365.0F);
    rock1.setCustomDescriptionId("planet_viymese");
    
    //Note to self, WTF does Congite mean again?
    PlanetAPI rock2 = system.addPlanet("congite", rock_star, "Congite", "gas_giant", 270.0F, 400.0F, 6000.0F, 365.0F);
    rock2.setCustomDescriptionId("planet_congite");
    


    PlanetAPI rock2a = system.addPlanet("rangus", rock_star, "Rangus", "arid", 270.0F, 130.0F, 4600.0F, 365.0F);
    rock2a.setCustomDescriptionId("planet_rangus");
    rock2a.getSpec().setGlowTexture(Global.getSettings().getSpriteName("hab_glows", "asharu"));
    rock2a.getSpec().setGlowColor(new Color(235, 245, 255, 255));
    rock2a.getSpec().setUseReverseLightForGlow(true);
    rock2a.applySpecChanges();
    rock2a.setInteractionImage("illustrations", "cargo_loading");
       
    
    //Rock2 = congite gas giant!
    system.addRingBand(rock2, "misc", "rings_dust0", 256.0F, 2, Color.red, 256.0F, 1000.0F, 40.0F);
    system.addAsteroidBelt(rock2, 20, 1000.0F, 128.0F, 40.0F, 80.0F);
    
    //Rock2  the Anvil of Metelson Industries! basingall the heavy industry and such
PlanetAPI rock2ab = system.addPlanet("anvil", rock_star, "Anvil", "barren", 100.0F, 20.0F, 2000.0F, 100.0F);
    rock2ab.setCustomDescriptionId("planet_anvil");
    rock2ab.getSpec().setGlowTexture(Global.getSettings().getSpriteName("hab_glows", "aurorae"));
    rock2ab.getSpec().setGlowColor(new Color(100, 255, 255, 153));
    rock2ab.getSpec().setUseReverseLightForGlow(true);
    rock2ab.applySpecChanges();
    rock2ab.setInteractionImage("illustrations", "industrial_megafacility");    //removed for testing
//Tritacyhon station for Rock for a bit of "back n' forth" when Metelson and Triracyon have a spat or so...
    SectorEntityToken rock_station = system.addCustomEntity("rock_station", " Rock Citadel", "station_side02", "metelson");
    rock_station.setCircularOrbitPointingDown(system.getEntityById("rock"), 90.0F, 600.0F, 45.0F);
    rock_station.setCustomDescriptionId("rock_station");
    rock_station.setInteractionImage("illustrations", "hound_hangar");
    

//Yo-ho-diddley-dee, they are the pirates!
PlanetAPI rock3 = system.addPlanet("pebble", rock_star, "Pebble", "barren", 280.0F, 50.0F, 12000.0F, 650.0F);
    rock3.setCustomDescriptionId("planet_Pebble");
    rock3.getSpec().setGlowTexture(Global.getSettings().getSpriteName("hab_glows", "sindria"));
    rock3.getSpec().setGlowColor(new Color(235, 245, 255, 255));
    rock3.getSpec().setUseReverseLightForGlow(true);
    rock3.applySpecChanges();
    rock3.setInteractionImage("illustrations", "pirate_station");    //removed for testing
    


    JumpPointAPI jumpPoint = Global.getFactory().createJumpPoint("congite_passage", "Congite Passage");
    com.fs.starfarer.api.campaign.OrbitAPI orbit = Global.getFactory().createCircularOrbit(rock2a, 0.0F, 2200.0F, 100.0F);
    jumpPoint.setOrbit(orbit);
    jumpPoint.setRelatedPlanet(rock2a);
    jumpPoint.setStandardWormholeToHyperspaceVisual();
    system.addEntity(jumpPoint);
    

    float radiusAfter = com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator.addOrbitingEntities(system, rock_star, StarAge.ANY, 2, 4, 8500.0F, 3, true);

    system.autogenerateHyperspaceJumpPoints(true, true);
    

    SectorEntityToken relay = system.addCustomEntity("rock_relay", "Rock Relay", "comm_relay", "metelson");
    


    relay.setCircularOrbit(system.getEntityById("rock"), 90.0F, 1200.0F, 45.0F);
  }
}
