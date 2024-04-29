package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.BeamEffectPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import static data.scripts.weapons.II_PhotonBeamEveryFrame.HARDPOINT_OFFSET;
import static data.scripts.weapons.II_PhotonBeamEveryFrame.TURRET_OFFSET;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

public class II_PhotonBeamEffect implements BeamEffectPlugin {

    private static final Color COLOR1 = new Color(255, 225, 125);
    private static final Color COLOR2 = new Color(255, 225, 175);
    private static final float EJECT_OFFSET = -7f;

    private static final Vector2f ZERO = new Vector2f();

    private boolean firing = false;
    private final IntervalUtil interval = new IntervalUtil(0.1f, 0.1f);
    private final IntervalUtil interval2 = new IntervalUtil(0.015f, 0.015f);
    private float level = 0f;
    private float sinceLast = 0f;

    @Override
    public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
        if (Global.getCombatEngine().isPaused()) {
            return;
        }

        Vector2f origin = new Vector2f(beam.getWeapon().getLocation());
        Vector2f offset;
        if (beam.getWeapon().getSlot().isHardpoint()) {
            offset = new Vector2f(HARDPOINT_OFFSET, 0f);
        } else {
            offset = new Vector2f(TURRET_OFFSET, 0f);
        }
        VectorUtils.rotate(offset, beam.getWeapon().getCurrAngle(), offset);
        Vector2f.add(offset, origin, origin);

        if (firing) {
            if (beam.getBrightness() < level) {
                firing = false;
                if (Global.getCombatEngine().getTotalElapsedTime(false) - sinceLast > 0.3f) {
                    float dir = beam.getWeapon().getCurrAngle();
                    if (Math.random() < 0.5) {
                        dir -= 90f;
                    } else {
                        dir += 90f;
                    }
                    if (dir < 0f) {
                        dir += 360f;
                    }

                    Vector2f origin2 = new Vector2f(beam.getWeapon().getLocation());
                    if (beam.getWeapon().getSlot().isHardpoint()) {
                        offset = new Vector2f(HARDPOINT_OFFSET + EJECT_OFFSET, 0f);
                    } else {
                        offset = new Vector2f(TURRET_OFFSET + EJECT_OFFSET, 0f);
                    }
                    VectorUtils.rotate(offset, beam.getWeapon().getCurrAngle(), offset);
                    Vector2f.add(offset, origin2, origin2);
                    Global.getCombatEngine().spawnProjectile(beam.getSource(), beam.getWeapon(), "ii_photonbeam_core", origin2,
                            dir, beam.getSource().getVelocity());
                    Global.getSoundPlayer().playSound("ii_photonbeam_eject", 1.0f, 1.0f, origin2, new Vector2f());
                }
            }
        } else {
            if (beam.getBrightness() > level) {
                firing = true;
                if (Global.getCombatEngine().getTotalElapsedTime(false) - sinceLast > 0.3f) {
                    Global.getSoundPlayer().playSound("ii_photonbeam_fire", 1f, 1.5f, origin, new Vector2f());
                }

                sinceLast = Global.getCombatEngine().getTotalElapsedTime(false);
                Global.getCombatEngine().addHitParticle(origin, ZERO, 150f, 5f, 0.2f, COLOR1);
                Global.getCombatEngine().addHitParticle(origin, ZERO, 75f, 5f, 0.5f, COLOR2);
            }
        }
        level = beam.getBrightness();

        interval2.advance(amount);
        if (interval2.intervalElapsed()) {
            Global.getCombatEngine().addHitParticle(origin, ZERO, (float) Math.random() * 75f + 75f, 0.2f, 0.2f,
                    new Color(MathUtils.getRandomNumberInRange(200, 255),
                            MathUtils.getRandomNumberInRange(150, 200),
                            MathUtils.getRandomNumberInRange(50, 150),
                            255));
        }

        interval.advance(amount);
        if (interval.intervalElapsed()) {
            if (beam.getDamageTarget() != null) {
                Global.getCombatEngine().spawnExplosion(beam.getTo(), ZERO,
                        new Color(MathUtils.getRandomNumberInRange(225, 255),
                                MathUtils.getRandomNumberInRange(100, 150),
                                MathUtils.getRandomNumberInRange(0, 100),
                                255),
                        (float) Math.random() * 50f + 50f, 0.75f);
            }
        }
    }
}
