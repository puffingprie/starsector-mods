package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import java.awt.Color;
import org.lwjgl.util.vector.Vector2f;

public class II_GravityCannonEveryFrame implements EveryFrameWeaponEffectPlugin {

    private static final Color COLOR1 = new Color(148, 148, 224, 160);
    private static final Color COLOR2 = new Color(160, 160, 255);

    private static final Vector2f ZERO = new Vector2f();

    private boolean charging = false;
    private final IntervalUtil interval = new IntervalUtil(0.015f, 0.015f);
    private float level = 0f;

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (Global.getCombatEngine().isPaused()) {
            return;
        }

        Vector2f origin = new Vector2f(weapon.getLocation());

        if (charging) {
            if (weapon.getChargeLevel() < level && weapon.getChargeLevel() < 1f) {
                charging = false;
            } else if (weapon.getChargeLevel() < 1f) {
                interval.advance(amount);
                if (interval.intervalElapsed()) {
                    Global.getCombatEngine().addHitParticle(origin, ZERO, (float) Math.random() * 50f + 125f
                            * weapon.getChargeLevel(), 0.2f, 0.3f
                            * weapon.getChargeLevel(),
                            COLOR1);
                    Global.getCombatEngine().addHitParticle(origin, ZERO, (float) Math.random() * 50f + 175f
                            * weapon.getChargeLevel(), 0.2f, 0.3f
                            * weapon.getChargeLevel(),
                            COLOR2);
                }
            }
        } else {
            if (weapon.getChargeLevel() > 0f && weapon.getChargeLevel() > level) {
                charging = true;
                Global.getSoundPlayer().playSound("ii_gravitycannon_charge", 1f, 1f, origin,
                        weapon.getShip().getVelocity());
            }
        }
        level = weapon.getChargeLevel();
    }
}
