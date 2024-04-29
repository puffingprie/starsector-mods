package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.util.II_Util;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

public class II_WaveMotionEveryFrame implements EveryFrameWeaponEffectPlugin {

    private static final float CHARGEUP_PARTICLE_ANGLE_SPREAD = 360f;
    private static final float CHARGEUP_PARTICLE_BRIGHTNESS = 0.75f;
    private static final float CHARGEUP_PARTICLE_DISTANCE_MAX = 175f;
    private static final float CHARGEUP_PARTICLE_DISTANCE_MIN = 150f;
    private static final float CHARGEUP_PARTICLE_DURATION = 0.2f;
    private static final float CHARGEUP_PARTICLE_SIZE_MAX = 5f;
    private static final float CHARGEUP_PARTICLE_SIZE_MIN = 4f;

    public static final float TURRET_OFFSET = 31.5f;

    private static final Vector2f ZERO = new Vector2f();

    private boolean charging = false;
    private boolean cooling = false;
    private boolean firing = false;
    private final IntervalUtil interval = new IntervalUtil(0.015f, 0.015f);
    private float level = 0f;

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (engine.isPaused()) {
            return;
        }

        Vector2f origin = new Vector2f(weapon.getLocation());
        Vector2f offset = new Vector2f(TURRET_OFFSET, 0f);
        VectorUtils.rotate(offset, weapon.getCurrAngle(), offset);
        Vector2f.add(offset, origin, origin);
        ShipAPI ship = weapon.getShip();
        float shipFacing = weapon.getCurrAngle();
        Vector2f shipVelocity = ship.getVelocity();

        if (charging) {
            if (firing && (weapon.getChargeLevel() < 1f)) {
                charging = false;
                cooling = true;
                firing = false;
                Global.getSoundPlayer().playSound("ii_boss_wavemotion_end", 1f, 1f, origin, weapon.getShip().getVelocity());
            } else if (weapon.getChargeLevel() < 1f) {
                interval.advance(amount);
                if (interval.intervalElapsed()) {
                    float radius = 50f + (weapon.getChargeLevel() * weapon.getChargeLevel()
                            * MathUtils.getRandomNumberInRange(50f, 150f));
                    Color color = new Color(II_Util.clamp255(MathUtils.getRandomNumberInRange(75, 125)),
                            II_Util.clamp255(MathUtils.getRandomNumberInRange(175, 200)),
                            II_Util.clamp255(MathUtils.getRandomNumberInRange(225, 255)), 255);
                    engine.addHitParticle(origin, ZERO, radius, 0.1f + weapon.getChargeLevel() * 0.3f, 0.2f, color);

                    int count = 10 + (int) (weapon.getChargeLevel() * 25);
                    for (int i = 0; i < count; i++) {
                        float distance = MathUtils.getRandomNumberInRange(CHARGEUP_PARTICLE_DISTANCE_MIN,
                                CHARGEUP_PARTICLE_DISTANCE_MAX)
                                * weapon.getChargeLevel();
                        float size = MathUtils.getRandomNumberInRange(CHARGEUP_PARTICLE_SIZE_MIN, CHARGEUP_PARTICLE_SIZE_MAX);
                        float angle = MathUtils.getRandomNumberInRange(-0.5f * CHARGEUP_PARTICLE_ANGLE_SPREAD, 0.5f
                                * CHARGEUP_PARTICLE_ANGLE_SPREAD);
                        float speed = 0.75f * distance / CHARGEUP_PARTICLE_DURATION;
                        Vector2f particleVelocity = MathUtils.getPointOnCircumference(shipVelocity, speed, angle + shipFacing
                                + 180f);
                        engine.addHitParticle(origin, particleVelocity, size, CHARGEUP_PARTICLE_BRIGHTNESS * Math.min(
                                weapon.getChargeLevel() + 0.5f, 1f)
                                * MathUtils.getRandomNumberInRange(0.75f, 1.25f), CHARGEUP_PARTICLE_DURATION,
                                color);
                    }
                }
            } else {
                firing = true;
            }
        } else {
            if (cooling) {
                if (weapon.getChargeLevel() <= 0f) {
                    cooling = false;
                }
            } else if (weapon.getChargeLevel() > level) {
                charging = true;
                Global.getSoundPlayer().playSound("ii_boss_wavemotion_charge", 1f, 1f, origin, weapon.getShip().getVelocity());
            }
        }
        level = weapon.getChargeLevel();
    }
}
