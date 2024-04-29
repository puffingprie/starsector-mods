package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.everyframe.II_Trails;
import data.scripts.everyframe.II_WeaponScriptPlugin;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

public class II_SolisCannonEveryFrame implements EveryFrameWeaponEffectPlugin {

    private static final float CHARGEUP_PARTICLE_ANGLE_SPREAD = 120f;
    private static final float CHARGEUP_PARTICLE_BRIGHTNESS = 0.33f;
    private static final Color CHARGEUP_PARTICLE_COLOR = new Color(255, 190, 70, 100);
    private static final float CHARGEUP_PARTICLE_COUNT_FACTOR = 4;
    private static final float CHARGEUP_PARTICLE_DISTANCE_MAX = 15f;
    private static final float CHARGEUP_PARTICLE_DISTANCE_MIN = 10f;
    private static final float CHARGEUP_PARTICLE_DURATION = 0.15f;
    private static final float CHARGEUP_PARTICLE_SIZE_MAX = 7f;
    private static final float CHARGEUP_PARTICLE_SIZE_MIN = 4f;
    private static final Color MUZZLE_BANG_COLOR = new Color(255, 190, 40, 40);
    private static final Color MUZZLE_FLASH_COLOR = new Color(255, 210, 70, 165);
    private static final float MUZZLE_FLASH_DURATION = 0.3f;
    private static final float MUZZLE_FLASH_SIZE = 40.0f;
    private static final Vector2f MUZZLE_OFFSET_HARDPOINT_1 = new Vector2f(23.5f, 4f);
    private static final Vector2f MUZZLE_OFFSET_HARDPOINT_2 = new Vector2f(23.5f, -4f);
    private static final Vector2f MUZZLE_OFFSET_TURRET_1 = new Vector2f(20f, 4f);
    private static final Vector2f MUZZLE_OFFSET_TURRET_2 = new Vector2f(20f, -4f);

    private final IntervalUtil interval = new IntervalUtil(0.015f, 0.015f);
    private float lastChargeLevel = 0.0f;
    private float lastCooldownRemaining = 0.0f;
    private boolean shot = false;

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (engine.isPaused()) {
            return;
        }

        II_Trails.createIfNeeded();
        II_WeaponScriptPlugin.createIfNeeded();

        float chargeLevel = weapon.getChargeLevel();
        float cooldownRemaining = weapon.getCooldownRemaining();

        Vector2f weaponLocation = weapon.getLocation();
        ShipAPI ship = weapon.getShip();
        float weaponFacing = weapon.getCurrAngle();
        Vector2f shipVelocity = ship.getVelocity();
        Vector2f offset1, offset2;
        if (weapon.getSlot().isHardpoint()) {
            offset1 = new Vector2f(MUZZLE_OFFSET_HARDPOINT_1);
            offset2 = new Vector2f(MUZZLE_OFFSET_HARDPOINT_2);
        } else {
            offset1 = new Vector2f(MUZZLE_OFFSET_TURRET_1);
            offset2 = new Vector2f(MUZZLE_OFFSET_TURRET_2);
        }
        VectorUtils.rotate(offset1, weaponFacing, offset1);
        VectorUtils.rotate(offset2, weaponFacing, offset2);
        Vector2f muzzleLocation1 = Vector2f.add(weaponLocation, offset1, new Vector2f());
        Vector2f muzzleLocation2 = Vector2f.add(weaponLocation, offset2, new Vector2f());

        if ((chargeLevel > lastChargeLevel) || (lastCooldownRemaining < cooldownRemaining)) {
            if (weapon.isFiring() && ((ship.getFluxTracker().getMaxFlux() - ship.getFluxTracker().getCurrFlux()) >= weapon.getFluxCostToFire())) {
                interval.advance(amount);
                if (interval.intervalElapsed()) {
                    int particleCount = (int) (CHARGEUP_PARTICLE_COUNT_FACTOR * chargeLevel);
                    float distance, size, angle, speed;
                    Vector2f particleVelocity;
                    for (int i = 0; i < particleCount; ++i) {
                        distance = MathUtils.getRandomNumberInRange(CHARGEUP_PARTICLE_DISTANCE_MIN,
                                CHARGEUP_PARTICLE_DISTANCE_MAX);
                        size = MathUtils.getRandomNumberInRange(CHARGEUP_PARTICLE_SIZE_MIN, CHARGEUP_PARTICLE_SIZE_MAX);
                        angle = MathUtils.getRandomNumberInRange(-0.5f * CHARGEUP_PARTICLE_ANGLE_SPREAD, 0.5f
                                * CHARGEUP_PARTICLE_ANGLE_SPREAD);
                        Vector2f spawnLocation1 = MathUtils.getPointOnCircumference(muzzleLocation1, distance,
                                (angle + weaponFacing));
                        Vector2f spawnLocation2 = MathUtils.getPointOnCircumference(muzzleLocation2, distance,
                                (angle + weaponFacing));
                        speed = distance / CHARGEUP_PARTICLE_DURATION;
                        particleVelocity = MathUtils.getPointOnCircumference(shipVelocity, speed, 180.0f + angle
                                + weaponFacing);
                        engine.addHitParticle(spawnLocation1, particleVelocity, size, CHARGEUP_PARTICLE_BRIGHTNESS
                                * weapon.getChargeLevel(),
                                CHARGEUP_PARTICLE_DURATION, CHARGEUP_PARTICLE_COLOR);
                        engine.addHitParticle(spawnLocation2, particleVelocity, size, CHARGEUP_PARTICLE_BRIGHTNESS
                                * weapon.getChargeLevel(),
                                CHARGEUP_PARTICLE_DURATION, CHARGEUP_PARTICLE_COLOR);
                    }
                }
            }

            if (!shot && ((lastCooldownRemaining < cooldownRemaining) || ((chargeLevel >= 1f) && (lastChargeLevel < 1f)))) {
                shot = true;

                engine.spawnExplosion(muzzleLocation1, shipVelocity, MUZZLE_FLASH_COLOR, MUZZLE_FLASH_SIZE,
                        MUZZLE_FLASH_DURATION);
                engine.addSmoothParticle(muzzleLocation1, shipVelocity, MUZZLE_FLASH_SIZE * 3f, 1f,
                        MUZZLE_FLASH_DURATION * 2f, MUZZLE_FLASH_COLOR);
                engine.addHitParticle(muzzleLocation1, shipVelocity, MUZZLE_FLASH_SIZE * 4f, 0.25f, 0.05f,
                        MUZZLE_BANG_COLOR);
                engine.spawnExplosion(muzzleLocation2, shipVelocity, MUZZLE_FLASH_COLOR, MUZZLE_FLASH_SIZE,
                        MUZZLE_FLASH_DURATION);
                engine.addSmoothParticle(muzzleLocation2, shipVelocity, MUZZLE_FLASH_SIZE * 3f, 1f,
                        MUZZLE_FLASH_DURATION * 2f, MUZZLE_FLASH_COLOR);
                engine.addHitParticle(muzzleLocation2, shipVelocity, MUZZLE_FLASH_SIZE * 4f, 0.25f, 0.05f,
                        MUZZLE_BANG_COLOR);
            }
        } else {
            shot = false;
        }

        lastChargeLevel = chargeLevel;
        lastCooldownRemaining = cooldownRemaining;
    }
}
