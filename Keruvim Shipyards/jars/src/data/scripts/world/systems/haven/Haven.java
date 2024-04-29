package data.scripts.world.systems.haven;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.procgen.NebulaEditor;
import com.fs.starfarer.api.impl.campaign.procgen.StarAge;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;
public class Haven {

    public void generate(SectorAPI sector) {
        StarSystemAPI system = sector.createStarSystem("Haven");
        system.setBackgroundTextureFilename("graphics/backgrounds/background2.jpg");
        PlanetAPI haven = system.initStar("anvil_haven", // id of the entity. Useful for later uses in other files (like descriptions.csv) or scripts
                "star_yellow", // Star type. Vanilla star types can be found in starsector-core/data/campaign/procgen/star_gen_data.csv and starsector-core/data/config/planets.json
                900f, // Star radius, dunno how it is measured, but I guess it's in pixels
                300f); // Corona size of the star, maybe in pixels
		system.setLightColor(new Color(255,255,181));

        PlanetAPI crucible = system.addPlanet("anvil_crucible", // id of the entity. Useful for later uses
                haven, // The entity this planet orbiting. That's why stocking entities in variables is important, to reuse them later in this code
                "Crucible", // Name of the planet that will be displayed in game
                "tundra", // Type of planet. Every type of planet can be found in starsector-core/data/campaign/procgen/planet_gen_data.csv or starsector-core/data/config/planets.json
                40, // Beginning orbit angle of the planet. Dunno how it exactly works, but from every test I've made, setting it to 0 is more likely to set the entity almost to the south of what it's orbiting
                190, // Planet radius. Same deal as the star size
                7000, // How far the planet is from the entity it's orbiting, maybe in pixels
                300); // Number of days needed for the planet to do a complete orbit cycle
		crucible.setCustomDescriptionId("anvil_crucible"); // In case you have a custom description ID in decriptions.csv for this planet, put it here between "". I guess if you set up a description for this planet, if the description has the same id as the planet, it may work like with ships and such
        crucible.setInteractionImage("illustrations", "urban01");

        // Enfer volcanic
        // Here, same deal as for Crucible
        PlanetAPI enfer = system.addPlanet("anvil_enfer",
                haven,
                "Enfer",
                "lava",
                210,
                140,
                5000,
                120);
        enfer.setCustomDescriptionId("anvil_enfer");
        // Enfer volcanic

        // A custom entity. There are many types of them and this one is for instance used comm relay
        // As always, stock it in a variable for later uses (such as setting its orbit)
        SectorEntityToken relay = system.addCustomEntity("anvil_haven_relay", // id of the entity. Useful for other uses in other files and scripts
                "Haven Relay", // Name of the entity displayed in game
                "comm_relay", // Entity type. Every entity types can be found in starsector-core/data/config/custom_entities.json
                "pirates"); // id of the faction controlling the entity. If you want it being in the control of no one, use Factions.NEUTRAL
        // Setting the orbit parameters of the entity up
        relay.setCircularOrbit(haven, // Variable stocking the thing you want this entity to orbit
                340, // Orbiting angle
                5000, // How far this entity is from the thing it's orbiting, maybe in pixels
                120); // Number of days needed for the entity to do a complete orbit cycle around what it is orbiting

        // Other custom entity. Same deal as the first one
		SectorEntityToken buoy = system.addCustomEntity("anvil_haven_buoy",
                "Haven Nav Buoy",
                "nav_buoy",
                "keruvim");
        buoy.setCircularOrbit(haven,
                90,
                5000,
                120);

        // Other entity too.
        SectorEntityToken stableloc3 = system.addCustomEntity(null, // You can choose not to give an id to the entity, if so, set null instead of "id"
                null, // You can also choose to give no name for an entity. Mostly recommended for stable locations.
                "stable_location", // Entity type
                Factions.NEUTRAL); // And faction it belongs to
        // And then orbit parameters as seen before
		stableloc3.setCircularOrbitPointingDown(haven,
                183,
                5000,
                120);

		// An asteroid belt creation
        system.addAsteroidBelt(haven, // Variable stocking the entity you want the asteroid belt to encircle
                100, // Number of asteroids in the belt
                6000, // Diameter (I think) of the belt, in pixels (I guess, again)
                300, // How thicc the belt is. The asteroids will spread more the higher the value
                460, // Minimum of days required for the asteroid belt to do an entire cycle
                500); // Maximum days the belt will take to do a cycle
        // Adding a ring band to make the belt feel less empty
        system.addRingBand(haven, // Variable stocking the entity you want the decoration to encircle
                "misc", // I don't know what this is used for
                "rings_asteroids0", // Picture to use for this decoration. Vanilla ones can be found here : starsector-core/graphics/planets
                280f, // How large the ring band is
                1, // Which band of the picture to use. For instance, rings_asteroids0 have 4 bands and here we are using the 1st
                Color.orange, // Color to apply to the ring band
                280f, // Don't know what this is used for so I prefer to use the same value as the one used before the band
                6050, // Diameter of the band in pixels (probably)
                200); // How many days are required for the band to do an entire cycle

        // Inner jump point
        // Jump point definition stocked in a variable for later use if needed
		JumpPointAPI jumpPoint1 = Global.getFactory().createJumpPoint("anvil_haven_inner_jump", // Jump point id
                "Haven Inner System Jump-point"); // Jump point displayed name in game
        // Creating an orbit for the jump point
		OrbitAPI orbit1 = Global.getFactory().createCircularOrbit(haven, // Variable containing the entity to orbit
                170, // Angle of the object it will be assigned too
                2500, // How far the orbit is from the entity, in pixels
                70); // How many days are required to do an entire cycle
        // Assigning the orbit to the jump point
		jumpPoint1.setOrbit(orbit1);
		// Those 2 lines are required
		jumpPoint1.setStandardWormholeToHyperspaceVisual();
		system.addEntity(jumpPoint1);

		// Jump point near Crucible
        // Same deal as the first one
		JumpPointAPI jumpPoint2 = Global.getFactory().createJumpPoint("anvil_haven_crucible_jump", "Crucible Jump-point");
		OrbitAPI orbit2 = Global.getFactory().createCircularOrbit(haven, 60, 7000, 365);
		jumpPoint2.setOrbit(orbit2);
		jumpPoint2.setRelatedPlanet(crucible);
		jumpPoint2.setStandardWormholeToHyperspaceVisual();
		system.addEntity(jumpPoint2);       

		// Automatic generation of entities after a certain radius to fill a bit the system
        float radiusAfter = StarSystemGenerator.addOrbitingEntities(system, haven, StarAge.AVERAGE,
                                                                    3, 5, // min/max entities to add
                                                                    18000, // radius to start adding at
                                                                    5, // name offset - next planet will be <system name> <roman numeral of this parameter + 1>
                                                                    true); // whether to use custom or system-name based names

        system.autogenerateHyperspaceJumpPoints(true, true);
		
		//Getting rid of some hyperspace nebula, just in case
        HyperspaceTerrainPlugin plugin = (HyperspaceTerrainPlugin) Misc.getHyperspaceTerrain().getPlugin();
        NebulaEditor editor = new NebulaEditor(plugin);
        float minRadius = plugin.getTileSize() * 2f;

        float radius = system.getMaxRadiusInHyperspace();
        editor.clearArc(system.getLocation().x, system.getLocation().y, 0, radius + minRadius, 0, 360f);
        editor.clearArc(system.getLocation().x, system.getLocation().y, 0, radius + minRadius, 0, 360f, 0.25f);
    }

}
