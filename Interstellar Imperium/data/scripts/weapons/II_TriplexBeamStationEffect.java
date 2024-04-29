package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.BeamEffectPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class II_TriplexBeamStationEffect implements BeamEffectPlugin {

    private static final Color COLOR1 = new Color(255, 160, 100, 100);
    private static final Color COLOR2 = new Color(255, 170, 150, 100);
    private static final Color COLOR3 = new Color(255, 160, 100, 50);

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

        Vector2f muzzleLocation = beam.getFrom();

        if (firing) {
            if (beam.getBrightness() < level) {
                firing = false;
            }
        } else {
            if (beam.getBrightness() > level) {
                firing = true;
                if (Global.getCombatEngine().getTotalElapsedTime(false) - sinceLast > 0.2f) {
                    //Global.getSoundPlayer().playSound("ii_photonbeam_fire", 1.2f, 1f, muzzleLocation, new Vector2f());
                }

                sinceLast = Global.getCombatEngine().getTotalElapsedTime(false);
                Global.getCombatEngine().addHitParticle(muzzleLocation, beam.getWeapon().getShip().getVelocity(), 125f, 3f, 0.15f, COLOR1);
                Global.getCombatEngine().addHitParticle(muzzleLocation, beam.getWeapon().getShip().getVelocity(), 60f, 3f, 0.4f, COLOR2);
            }
        }
        level = beam.getBrightness();

        interval2.advance(amount);
        if (interval2.intervalElapsed()) {
            Global.getCombatEngine().addHitParticle(muzzleLocation, beam.getWeapon().getShip().getVelocity(),
                    ((float) Math.random() * 60f * level) + 100f, 0.25f * Math.max(0.1f, level), 0.25f * Math.max(0.1f, level),
                    new Color(MathUtils.getRandomNumberInRange(200, 255),
                            MathUtils.getRandomNumberInRange(100, 150),
                            MathUtils.getRandomNumberInRange(50, 100),
                            100));
        }

        interval.advance(amount);
        if (interval.intervalElapsed()) {
            Global.getCombatEngine().spawnExplosion(muzzleLocation, beam.getWeapon().getShip().getVelocity(),
                    COLOR3, (((float) Math.random() * 10f) + 20f) * Math.max(0.1f, level), 0.3f);
        }
        if (beam.didDamageThisFrame()) {
            if (beam.getDamageTarget() != null) {
                Global.getCombatEngine().spawnExplosion(beam.getTo(), ZERO,
                        new Color(MathUtils.getRandomNumberInRange(225, 255),
                                MathUtils.getRandomNumberInRange(50, 100),
                                MathUtils.getRandomNumberInRange(0, 50),
                                255),
                        (((float) Math.random() * 40f) + 40f) * Math.max(0.1f, level), 0.6f * Math.max(0.1f, level));
            }
        }
    }
}
