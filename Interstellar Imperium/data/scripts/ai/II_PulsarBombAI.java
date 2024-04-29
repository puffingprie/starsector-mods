package data.scripts.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import data.scripts.util.II_Util;
import java.util.List;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

public class II_PulsarBombAI extends II_BaseMissile {

    private static final float SPLIT_DEVIATION_DEG = 20f;
    private static final float SPLIT_RANGE = 500f;
    private static final Vector2f ZERO = new Vector2f();
    private final IntervalUtil interval = new IntervalUtil(0.1f, 0.2f);

    private float lifetime;

    public II_PulsarBombAI(MissileAPI missile, ShipAPI launchingShip) {
        super(missile, launchingShip);

        lifetime = 1.5f;
    }

    @SuppressWarnings("SuspiciousNameCombination")
    @Override
    public void advance(float amount) {
        if (missile.isFading()) {
            return;
        }

        interval.advance(amount);

        lifetime -= amount;
        if (interval.intervalElapsed() && lifetime < 0f) {
            float facingAngle = VectorUtils.getFacing(missile.getVelocity());

            boolean split = false;
            float angleToTarget = 0f;
            List<ShipAPI> ships = II_Util.getShipsWithinRange(missile.getLocation(), SPLIT_RANGE);
            for (ShipAPI ship : ships) {
                if (ship.isAlive() && ship.getOwner() != missile.getOwner()) {
                    float baseAngle = VectorUtils.getAngle(missile.getLocation(), ship.getLocation());
                    float distance = MathUtils.getDistance(missile.getLocation(), ship.getLocation());

                    Vector2f widthFromLeft = new Vector2f(ship.getCollisionRadius() * 2f, 0f);
                    VectorUtils.rotate(widthFromLeft, baseAngle + 90f, widthFromLeft);
                    Vector2f.add(widthFromLeft, ship.getLocation(), widthFromLeft);
                    float apparentWidthLeft = Misc.getTargetingRadius(widthFromLeft, ship, false);
                    float widthAngleLeft = (float) Math.toDegrees(Math.atan2(apparentWidthLeft, distance));

                    Vector2f widthFromRight = new Vector2f(ship.getCollisionRadius() * 2f, 0f);
                    VectorUtils.rotate(widthFromRight, baseAngle - 90f, widthFromRight);
                    Vector2f.add(widthFromRight, ship.getLocation(), widthFromRight);
                    float apparentWidthRight = Misc.getTargetingRadius(widthFromRight, ship, false);
                    float widthAngleRight = (float) Math.toDegrees(Math.atan2(apparentWidthRight, distance));

                    float angleDistance = MathUtils.getShortestRotation(facingAngle, baseAngle);
                    if (angleDistance > 0f) {
                        if (Math.abs(angleDistance) <= (SPLIT_DEVIATION_DEG + widthAngleRight)) {
                            split = true;
                            angleToTarget = baseAngle;
                            break;
                        }
                    } else {
                        if (Math.abs(angleDistance) <= (SPLIT_DEVIATION_DEG + widthAngleLeft)) {
                            split = true;
                            angleToTarget = baseAngle;
                            break;
                        }
                    }
                }
            }

            if (split) {
                Global.getSoundPlayer().playSound("ii_pulsarbomb_mirv", 1f, 1f, missile.getLocation(), ZERO);
                Global.getCombatEngine().applyDamage(missile, missile.getLocation(), missile.getHitpoints() * 100f,
                        DamageType.FRAGMENTATION, 0f, false, false, missile, false);
                for (int i = 0; i < 5; i++) {
                    Vector2f spreadVel = MathUtils.getPointOnCircumference(missile.getVelocity(),
                            MathUtils.getRandomNumberInRange(0f, missile.getMaxSpeed() * 0.5f), MathUtils.getRandomNumberInRange(0f, 360f));
                    float facing = angleToTarget + MathUtils.getRandomNumberInRange(-10f, 10f);
                    Vector2f startPos = MathUtils.getPointOnCircumference(missile.getLocation(),
                            MathUtils.getRandomNumberInRange(0f, 8f), MathUtils.getRandomNumberInRange(0f, 360f));
                    DamagingProjectileAPI newProj = (DamagingProjectileAPI) Global.getCombatEngine().spawnProjectile(
                            missile.getSource(), missile.getWeapon(), "ii_pulsarbomb_pulse", startPos, facing, spreadVel);
                    newProj.setFromMissile(true);
                }
            }
        }
    }

    @Override
    public CombatEntityAPI getTarget() {
        return null;
    }
}
