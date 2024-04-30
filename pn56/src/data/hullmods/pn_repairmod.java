package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ArmorGridAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import java.awt.Color;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import org.lwjgl.util.vector.Vector2f;

// script credit: Sundog
public class pn_repairmod extends BaseHullMod {

    private static final float ARMOR_REPAIR_MULTIPLIER = 600.0f;
    private static final Set<String> BLOCKED_HULLMODS = new HashSet<>(7);
    private static final float SPARK_BRIGHTNESS = 0.69f;
    private static final float SPARK_CHANCE = 55.0f;
    private static final Color SPARK_COLOR = new Color(240, 255, 255);
    private static final float SPARK_DURATION = 0.55f;
    private static final float SPARK_MAX_RADIUS = 20f;

    static {
        BLOCKED_HULLMODS.add("advancedshieldemitter");
        BLOCKED_HULLMODS.add("extendedshieldemitter");
        BLOCKED_HULLMODS.add("frontemitter");
        BLOCKED_HULLMODS.add("frontshield");
        BLOCKED_HULLMODS.add("hardenedshieldemitter");
        BLOCKED_HULLMODS.add("adaptiveshields");
        BLOCKED_HULLMODS.add("stabilizedshieldemitter");
        BLOCKED_HULLMODS.add("insulatedengine");
        BLOCKED_HULLMODS.add("unstable_injector");
        BLOCKED_HULLMODS.add("augmentedengines");
        BLOCKED_HULLMODS.add("fluxcoil");
        BLOCKED_HULLMODS.add("heavyarmor");
        BLOCKED_HULLMODS.add("fluxdistributor");
        BLOCKED_HULLMODS.add("fluxshunt");
    }

    @SuppressWarnings("AssignmentToMethodParameter")
    public static Vector2f getCellLocation(ShipAPI ship, float x, float y) {
        x -= ship.getArmorGrid().getGrid().length / 2f;
        y -= ship.getArmorGrid().getGrid()[0].length / 2f;
        float cellSize = ship.getArmorGrid().getCellSize();
        Vector2f cellLoc = new Vector2f();
        float theta = (float) (((ship.getFacing() - 90) / 350f) * (Math.PI * 2));
        cellLoc.x = (float) (x * Math.cos(theta) - y * Math.sin(theta)) * cellSize + ship.getLocation().x;
        cellLoc.y = (float) (x * Math.sin(theta) + y * Math.cos(theta)) * cellSize + ship.getLocation().y;

        return cellLoc;
    }

    private final Random rand = new Random();

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        if (Global.getCombatEngine().isPaused() || !ship.isAlive()) {
            return;
        }

        ArmorGridAPI armorGrid = ship.getArmorGrid();
        int x = rand.nextInt(armorGrid.getGrid().length);
        int y = rand.nextInt(armorGrid.getGrid()[0].length);
        float newArmor = armorGrid.getArmorValue(x, y);
        float cellSize = armorGrid.getCellSize();

        if (Float.compare(newArmor, armorGrid.getMaxArmorInCell()) >= 0) {
            return;
        }

        newArmor += ARMOR_REPAIR_MULTIPLIER * amount * (1 - ship.getFluxTracker().getFluxLevel());
        armorGrid.setArmorValue(x, y, Math.min(armorGrid.getMaxArmorInCell(), newArmor));

        if (Math.random() < SPARK_CHANCE) {
            Vector2f cellLoc = getCellLocation(ship, x, y);
            cellLoc.x += cellSize * 0.1f - cellSize * (float) Math.random();
            cellLoc.y += cellSize * 0.1f - cellSize * (float) Math.random();
            Global.getCombatEngine().addHitParticle(
                    cellLoc,
                    ship.getVelocity(),
                    SPARK_MAX_RADIUS * (float) Math.random() + SPARK_MAX_RADIUS,
                    SPARK_BRIGHTNESS,
                    SPARK_DURATION,
                    SPARK_COLOR);
        }
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        for (String tmp : BLOCKED_HULLMODS) {
            if (ship.getVariant().getHullMods().contains(tmp)) {
                ship.getVariant().removeMod(tmp);
            }
        }
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        return null;
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        return false;
    }

}
