package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

import com.fs.starfarer.api.impl.combat.RiftCascadeEffect;
import com.fs.starfarer.api.impl.combat.RiftLanceEffect;
import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;


public class HMI_Mess_VisualEffect extends BaseHullMod {

    private static final Color SPARK_COLOR = new Color(192, 193, 224, 255);
    private static final Color SPARK_COLOR3 = new Color(75, 63, 83, 175);
    private static final float SPARK_DURATION = 0.5f;
    private static final float SPARK_RADIUS = 42f;

    private static final Color SPARK_COLOR2 = new Color(236, 255, 170, 175);
    private static final float SPARK_RADIUS2 = 60f;

    public static int clamp255(int x) {
        return Math.max(0, Math.min(255, x));
    }
    public static Color colorJitter(Color color, float amount) {
        return new Color(clamp255((int) (color.getRed() + (int) (((float) Math.random() - 0.5f) * amount))),
                clamp255((int) (color.getGreen() + (int) (((float) Math.random() - 0.5f) * amount))),
                clamp255((int) (color.getBlue() + (int) (((float) Math.random() - 0.5f) * amount))),
                color.getAlpha());
    }


    public static Vector2f getCellLocation(ShipAPI ship, float x, float y) {
        float xx = x - (ship.getArmorGrid().getGrid().length / 2f);
        float yy = y - (ship.getArmorGrid().getGrid()[0].length / 2f);
        float cellSize = ship.getArmorGrid().getCellSize();
        Vector2f cellLoc = new Vector2f();
        float theta = (float) (((ship.getFacing() - 90f) / 360f) * (Math.PI * 2.0));
        cellLoc.x = (float) (xx * Math.cos(theta) - yy * Math.sin(theta)) * cellSize + ship.getLocation().x;
        cellLoc.y = (float) (xx * Math.sin(theta) + yy * Math.cos(theta)) * cellSize + ship.getLocation().y;

        return cellLoc;
    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        CombatEngineAPI engine = Global.getCombatEngine();
//        Color SPARK_COLOR3 = RiftLanceEffect.getColorForDarkening(RiftCascadeEffect.STANDARD_RIFT_COLOR);


        ArmorGridAPI armorGrid = ship.getArmorGrid();
        for (int x = 0; x < armorGrid.getGrid().length; x++) {
            for (int y = 0; y < armorGrid.getGrid()[0].length; y++) {
                float armorLevel = armorGrid.getArmorValue(x, y);
                float chance = amount * 2 * (1 - (armorLevel / armorGrid.getMaxArmorInCell()));
                float random = (float) Math.random();
                if (Math.random() >= chance) {
                    continue;
                }
                float cellSize = armorGrid.getCellSize();
                Vector2f cellLoc = getCellLocation(ship, x, y);
                cellLoc.x += cellSize * 0.1f - cellSize * (float) Math.random();
                cellLoc.y += cellSize * 0.1f - cellSize * (float) Math.random();
                if (CollisionUtils.isPointWithinBounds(cellLoc, ship)) {
//                    engine.addNegativeParticle(cellLoc, ship.getVelocity(), 0.5f * SPARK_RADIUS2 * random, 1f, SPARK_DURATION,
//                            colorJitter(SPARK_COLOR2, 50f));

                    engine.addSmokeParticle(cellLoc, ship.getVelocity(), 0.5f * SPARK_RADIUS * random, 1f, SPARK_DURATION,
                            colorJitter(SPARK_COLOR, 50f));
                    engine.addSmokeParticle(cellLoc, ship.getVelocity(), 0.5f * SPARK_RADIUS2 * random, 1f, SPARK_DURATION,
                            colorJitter(SPARK_COLOR3, 50f));


//                engine.addNebulaSmoothParticle(cellLoc, ship.getVelocity(), 0.5f * SPARK_RADIUS * (float) Math.random(), 1f, 1f / SPARK_DURATION, 0.5f / SPARK_DURATION, SPARK_DURATION,
//                        colorJitter(color, 50f));
//                engine.addNegativeNebulaParticle(cellLoc, ship.getVelocity(), 0.75f * SPARK_RADIUS2 * random, 2f, 0.5f / SPARK_DURATION, 0.25f / SPARK_DURATION, SPARK_DURATION,
//                            colorJitter(SPARK_COLOR2, 50f));



            }
            }
        }
    }

}
