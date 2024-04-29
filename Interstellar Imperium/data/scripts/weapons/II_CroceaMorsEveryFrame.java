package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.everyframe.II_Trails;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.entities.AnchoredEntity;
import org.lwjgl.util.vector.Vector2f;

public class II_CroceaMorsEveryFrame implements EveryFrameWeaponEffectPlugin {

    private static final Color CHARGEUP_GLOW_COLOR = new Color(255, 220, 100, 100);
    private static final float CHARGEUP_GLOW_SIZE = 150.0f;
    private static final Color CHARGEUP_COLOR_CORE = new Color(255, 255, 255, 100);
    private static final Color CHARGEUP_COLOR_FRINGE = new Color(255, 205, 20, 255);
    private static final float CHARGEUP_DIST_MAX = 202f;
    private static final float CHARGEUP_DIST_MIN = 129f;
    private static final float CHARGEUP_LATERAL_OFFSET = 11f;
    private static final float CHARGEUP_THICKNESS_MAX = 4f;
    private static final float CHARGEUP_THICKNESS_MIN = 2f;
    private static final Color MUZZLE_BANG_COLOR = new Color(255, 205, 20, 40);
    private static final Color MUZZLE_FLASH_COLOR = new Color(255, 220, 100, 150);
    private static final float MUZZLE_FLASH_DURATION = 0.4f;
    private static final float MUZZLE_FLASH_SIZE = 250.0f;
    private static final Vector2f MUZZLE_OFFSET = new Vector2f(27f, 0f);

    private final IntervalUtil interval = new IntervalUtil(0.1f, 0.1f);
    private float lastChargeLevel = 0.0f;
    private float lastCooldownRemaining = 0.0f;
    private boolean shot = false;

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (engine.isPaused()) {
            return;
        }

        II_Trails.createIfNeeded();

        float chargeLevel = weapon.getChargeLevel();
        float cooldownRemaining = weapon.getCooldownRemaining();

        ShipAPI ship = weapon.getShip();
        Vector2f weaponLocation = weapon.getLocation();
        float weaponFacing = weapon.getCurrAngle();
        Vector2f shipVelocity = ship.getVelocity();
        Vector2f offset = new Vector2f(MUZZLE_OFFSET);
        VectorUtils.rotate(offset, weaponFacing, offset);
        Vector2f muzzleLocation = Vector2f.add(weaponLocation, offset, new Vector2f());

        if (chargeLevel > lastChargeLevel || lastCooldownRemaining < cooldownRemaining) {
            if (weapon.isFiring()) {
                interval.advance(amount);
                if (interval.intervalElapsed()) {
                    float distLong = CHARGEUP_DIST_MIN + (chargeLevel * (CHARGEUP_DIST_MAX - CHARGEUP_DIST_MIN));
                    Vector2f point1 = new Vector2f(distLong, CHARGEUP_LATERAL_OFFSET);
                    Vector2f point2 = new Vector2f(distLong, -CHARGEUP_LATERAL_OFFSET);
                    VectorUtils.rotate(point1, ship.getFacing(), point1);
                    VectorUtils.rotate(point2, ship.getFacing(), point2);
                    Vector2f.add(point1, ship.getLocation(), point1);
                    Vector2f.add(point2, ship.getLocation(), point2);
                    if (Math.random() > 0.5) {
                        engine.spawnEmpArc(ship, point1, ship, new AnchoredEntity(ship, point2),
                                DamageType.ENERGY, 0, 0, 0, null,
                                MathUtils.getRandomNumberInRange(CHARGEUP_THICKNESS_MIN, CHARGEUP_THICKNESS_MAX),
                                CHARGEUP_COLOR_FRINGE, CHARGEUP_COLOR_CORE);
                    } else {
                        engine.spawnEmpArc(ship, point2, ship, new AnchoredEntity(ship, point1),
                                DamageType.ENERGY, 0, 0, 0, null,
                                MathUtils.getRandomNumberInRange(CHARGEUP_THICKNESS_MIN, CHARGEUP_THICKNESS_MAX),
                                CHARGEUP_COLOR_FRINGE, CHARGEUP_COLOR_CORE);
                    }

                    engine.addSmoothParticle(muzzleLocation, shipVelocity,
                            CHARGEUP_GLOW_SIZE, 0.5f + chargeLevel, 0.5f * chargeLevel, CHARGEUP_GLOW_COLOR);
                }
            }

            if (!shot && ((lastCooldownRemaining < cooldownRemaining) || ((chargeLevel >= 1f) && (lastChargeLevel < 1f)))) {
                shot = true;
                engine.spawnExplosion(muzzleLocation, shipVelocity,
                        MUZZLE_FLASH_COLOR, MUZZLE_FLASH_SIZE, MUZZLE_FLASH_DURATION);
                engine.addSmoothParticle(muzzleLocation, shipVelocity,
                        MUZZLE_FLASH_SIZE * 3f, 1f, MUZZLE_FLASH_DURATION * 2f, MUZZLE_FLASH_COLOR);
                engine.addHitParticle(muzzleLocation, shipVelocity,
                        MUZZLE_FLASH_SIZE * 4f, 0.25f, 0.05f, MUZZLE_BANG_COLOR);
            } else {
                shot = false;
            }
        }

        lastChargeLevel = chargeLevel;
        lastCooldownRemaining = cooldownRemaining;
    }
}
