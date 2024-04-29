package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.SoundAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

public class II_TriplexBeamStationEveryFrame implements EveryFrameWeaponEffectPlugin {

    public static final Vector2f MUZZLE_OFFSET_TURRET[] = {
        new Vector2f(7f, 0f),
        new Vector2f(14f, 10f),
        new Vector2f(14f, -10f)
    };
    public static final Vector2f MUZZLE_OFFSET_HARDPOINT[] = {
        new Vector2f(15f, 0f),
        new Vector2f(22f, 10f),
        new Vector2f(22f, -10f)
    };

    private boolean charging = false;
    private boolean cooling = false;
    private boolean firing = false;
    private final IntervalUtil interval = new IntervalUtil(0.015f, 0.015f);
    private float level = 0f;
    private SoundAPI fireSound = null;

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (Global.getCombatEngine().isPaused()) {
            return;
        }

        Vector2f weaponLocation = weapon.getLocation();
        float weaponFacing = weapon.getCurrAngle();
        Vector2f muzzleLocations[] = new Vector2f[3];
        for (int i = 0; i < 3; i++) {
            Vector2f offset;
            if (weapon.getSlot().isHardpoint()) {
                offset = new Vector2f(MUZZLE_OFFSET_HARDPOINT[i]);
            } else {
                offset = new Vector2f(MUZZLE_OFFSET_TURRET[i]);
            }
            VectorUtils.rotate(offset, weaponFacing, offset);
            muzzleLocations[i] = Vector2f.add(weaponLocation, offset, new Vector2f());
        }

        if (charging) {
            if (!firing && (weapon.getChargeLevel() < level)) {
                fireSound.stop();
                charging = false;
                firing = false;
            } else if (firing && (weapon.getChargeLevel() < 1f)) {
                charging = false;
                cooling = true;
                firing = false;
            } else if (weapon.getChargeLevel() < 1f) {
                interval.advance(amount);
                if (interval.intervalElapsed()) {
                    for (int i = 0; i < 3; i++) {
                        Global.getCombatEngine().addHitParticle(muzzleLocations[i], weapon.getShip().getVelocity(),
                                ((float) Math.random() * 75f) + (75f * weapon.getChargeLevel()), weapon.getChargeLevel() * 0.15f, 0.25f,
                                new Color(MathUtils.getRandomNumberInRange(200, 255),
                                        MathUtils.getRandomNumberInRange(100, 150),
                                        MathUtils.getRandomNumberInRange(50, 100), 150));
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
                fireSound = Global.getSoundPlayer().playSound("ii_triplex_fire", 0.9f, 1.2f, muzzleLocations[0], weapon.getShip().getVelocity());
            }
        }
        level = weapon.getChargeLevel();
    }
}
