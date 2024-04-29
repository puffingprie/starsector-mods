package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.BeamEffectPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.util.II_Multi;
import data.scripts.util.II_Util;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class II_GravityCannonEffect implements BeamEffectPlugin {

    private static final Color COLOR1 = new Color(148, 148, 224, 160);
    private static final Color COLOR2 = new Color(160, 160, 255);

    private static final Vector2f ZERO = new Vector2f();

    private final IntervalUtil interval = new IntervalUtil(0.015f, 0.015f);
    private float level = 0f;

    @Override
    public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
        if (Global.getCombatEngine().isPaused()) {
            return;
        }

        Vector2f origin = new Vector2f(beam.getWeapon().getLocation());

        level = beam.getBrightness();

        interval.advance(amount);
        if (interval.intervalElapsed()) {
            Global.getCombatEngine().addHitParticle(origin, ZERO, (float) Math.random() * 25f + 75f, 0.2f, 0.2f * level,
                    COLOR1);
            Global.getCombatEngine().addHitParticle(origin, ZERO, (float) Math.random() * 25f + 125f, 0.2f, 0.2f * level,
                    COLOR2);
        }

        CombatEntityAPI target = beam.getDamageTarget();
        if (target instanceof ShipAPI) {
            target = II_Multi.getRoot((ShipAPI) target);
        }

        if (beam.didDamageThisFrame() && target != null) {
            float force = level * amount * 7000f;
            Vector2f dir = MathUtils.getPoint(new Vector2f(1f, 0f), 1f, beam.getWeapon().getCurrAngle());
            II_Util.applyForce(target, dir, force);
        }
    }
}
