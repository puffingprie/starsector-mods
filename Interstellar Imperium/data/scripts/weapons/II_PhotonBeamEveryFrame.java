package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

public class II_PhotonBeamEveryFrame implements EveryFrameWeaponEffectPlugin {

    public static final float TURRET_OFFSET = 16f;
    public static final float HARDPOINT_OFFSET = 16f;

    private static final Vector2f ZERO = new Vector2f();

    private boolean charging = false;
    private boolean cooling = false;
    private boolean firing = false;
    private final IntervalUtil interval = new IntervalUtil(0.015f, 0.015f);
    private float level = 0f;

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (Global.getCombatEngine().isPaused()) {
            return;
        }

        Vector2f origin = new Vector2f(weapon.getLocation());
        Vector2f offset;
        if (weapon.getSlot().isHardpoint()) {
            offset = new Vector2f(HARDPOINT_OFFSET, 0f);
        } else {
            offset = new Vector2f(TURRET_OFFSET, 0f);
        }
        VectorUtils.rotate(offset, weapon.getCurrAngle(), offset);
        Vector2f.add(offset, origin, origin);

        if (charging) {
            if (firing && (weapon.getChargeLevel() < 1f)) {
                charging = false;
                cooling = true;
                firing = false;
            } else if (weapon.getChargeLevel() < 1f) {
                interval.advance(amount);
                if (interval.intervalElapsed()) {
                    Global.getCombatEngine().addHitParticle(origin, ZERO, (float) Math.random() * 115f + 115f
                            * weapon.getChargeLevel(), weapon.getChargeLevel()
                            * 0.3f, 0.2f,
                            new Color(MathUtils.getRandomNumberInRange(200, 255),
                                    MathUtils.getRandomNumberInRange(150, 200),
                                    MathUtils.getRandomNumberInRange(50, 150), 255));
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
                Global.getSoundPlayer().playSound("ii_photonbeam_charge", 1f, 1f, origin, weapon.getShip().getVelocity());
            }
        }
        level = weapon.getChargeLevel();
    }
}
