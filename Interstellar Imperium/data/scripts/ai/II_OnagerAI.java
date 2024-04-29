package data.scripts.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import data.scripts.util.II_Util;
import java.awt.Color;
import java.util.List;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class II_OnagerAI extends II_BaseMissile {

    private static final Color MIRV_SMOKE = new Color(150, 150, 110, 90);
    private static final float FIGHTER_MIRV_DISTANCE = 800f;
    private static final float TARGET_MIRV_DISTANCE = 800f;
    private static final int MAX_MIRVS = 36;
    private static final float TIME_BETWEEN_MIRVS = 0.05f;
    private static final float VELOCITY_DAMPING_FACTOR = 0.45f;
    private static final Vector2f ZERO = new Vector2f();

    private boolean aspectLocked = true;
    private boolean clearTarget = false;
    private float retargetTimer = 1f;
    private float timeLive = 0f;
    private final float minTimeToMirv = 1f;
    private float mirvCooldown = 0f;
    private int mirvIndex = 0;

    private final IntervalUtil interval = new IntervalUtil(0.2f, 0.3f);

    public II_OnagerAI(MissileAPI missile, ShipAPI launchingShip) {
        super(missile, launchingShip);
        missile.setEmpResistance(missile.getEmpResistance() + 4);
    }

    public void mirv(MissileAPI missile) {
        Vector2f location;
        switch (mirvIndex) {
            default:
            case 0:
            case 24:
                location = new Vector2f(6, 4);
                break;
            case 1:
                location = new Vector2f(6, 1);
                break;
            case 2:
            case 25:
                location = new Vector2f(6, -2);
                break;
            case 3:
                location = new Vector2f(6, -5);
                break;
            case 4:
            case 26:
                location = new Vector2f(-6, 4);
                break;
            case 5:
                location = new Vector2f(-6, 1);
                break;
            case 6:
            case 27:
                location = new Vector2f(-6, -2);
                break;
            case 7:
                location = new Vector2f(-6, -5);
                break;
            case 8:
            case 28:
                location = new Vector2f(4, 4);
                break;
            case 9:
                location = new Vector2f(4, 1);
                break;
            case 10:
            case 29:
                location = new Vector2f(4, -2);
                break;
            case 11:
                location = new Vector2f(4, -5);
                break;
            case 12:
            case 30:
                location = new Vector2f(-4, 4);
                break;
            case 13:
                location = new Vector2f(-4, 1);
                break;
            case 14:
            case 31:
                location = new Vector2f(-4, -2);
                break;
            case 15:
                location = new Vector2f(-4, -5);
                break;
            case 16:
            case 32:
                location = new Vector2f(2, 4);
                break;
            case 17:
                location = new Vector2f(2, 1);
                break;
            case 18:
            case 33:
                location = new Vector2f(2, -2);
                break;
            case 19:
                location = new Vector2f(2, -5);
                break;
            case 20:
            case 34:
                location = new Vector2f(-2, 4);
                break;
            case 21:
                location = new Vector2f(-2, 1);
                break;
            case 22:
            case 35:
                location = new Vector2f(-2, -2);
                break;
            case 23:
                location = new Vector2f(-2, -5);
                break;
        }
        VectorUtils.rotate(location, missile.getFacing());
        Vector2f.add(missile.getLocation(), location, location);

        Global.getCombatEngine().addSmokeParticle(location, missile.getVelocity(), MathUtils.getRandomNumberInRange(12.5f, 20f), 1f, 0.8f, MIRV_SMOKE);
        Global.getSoundPlayer().playSound("ii_fundae_fire", 1.1f, 0.7f, location, missile.getVelocity());

        float angle = VectorUtils.getAngle(missile.getLocation(), location);
        MissileAPI newMissile = (MissileAPI) Global.getCombatEngine().spawnProjectile(
                missile.getSource(), missile.getWeapon(), "ii_fundae_onagersub", location, angle, missile.getVelocity());
        newMissile.setFromMissile(true);

        mirvIndex++;
        mirvCooldown = TIME_BETWEEN_MIRVS;
    }

    @Override
    public void advance(float amount) {
        mirvCooldown -= amount;

        interval.advance(amount);
        if (interval.intervalElapsed()) {
            boolean detonate = false;
            List<ShipAPI> ships = II_Util.getShipsWithinRange(missile.getLocation(), missile.getCollisionRadius());
            for (ShipAPI ship : ships) {
                if ((ship.getOwner() != missile.getOwner()) && ship.isAlive()) {
                    if (!ship.isFighter() && !ship.isDrone() && !ship.isShuttlePod()) {
                        detonate = true;
                        break;
                    }
                }
            }
            if (mirvIndex >= MAX_MIRVS) {
                detonate = true;
            }
            if (detonate) {
                missile.setArmingTime(0f);
            } else {
                missile.setArmingTime(30f);
            }

            if (!clearTarget) {
                ships = II_Util.getShipsWithinRange(missile.getLocation(), FIGHTER_MIRV_DISTANCE + missile.getCollisionRadius());
                for (ShipAPI ship : ships) {
                    if ((ship.getOwner() != missile.getOwner()) && ship.isAlive()) {
                        if ((ship.isFighter() || ship.isDrone()) && !ship.isShuttlePod()) {
                            float distance = MathUtils.getDistance(missile, ship);
                            if (distance <= FIGHTER_MIRV_DISTANCE) {
                                clearTarget = true;
                                break;
                            }
                        }
                    }
                }
            }
        }

        if (missile.isFizzling() || missile.isFading()) {
            if (target == null) {
                return;
            }
            if ((mirvCooldown <= 0f) && (mirvIndex < MAX_MIRVS)) {
                mirv(missile);
            }
            return;
        }

        timeLive += amount;

        if (!acquireTarget(amount)) {
            missile.giveCommand(ShipCommand.ACCELERATE);
            return;
        }

        float distance = MathUtils.getDistance(target.getLocation(), missile.getLocation());
        float acceleration = missile.getAcceleration();
        float maxSpeed = missile.getMaxSpeed();

        Vector2f calculationVelocity = new Vector2f(missile.getVelocity());
        if (calculationVelocity.length() <= (maxSpeed * 0.5f)) {
            if (calculationVelocity.length() <= (maxSpeed * 0.25f)) {
                calculationVelocity.set(maxSpeed * 0.5f, 0f);
                VectorUtils.rotate(calculationVelocity, missile.getFacing(), calculationVelocity);
            } else {
                calculationVelocity.scale((maxSpeed * 0.5f) / calculationVelocity.length());
            }
        }

        Vector2f guidedTarget = interceptAdvanced(missile.getLocation(), calculationVelocity.length(), acceleration,
                maxSpeed, target.getLocation(), target.getVelocity());
        if (guidedTarget == null) {
            Vector2f projection = new Vector2f(target.getVelocity());
            float scalar = distance / (calculationVelocity.length() + 1f);
            projection.scale(scalar);
            guidedTarget = Vector2f.add(target.getLocation(), projection, null);
        }

        if (!clearTarget) {
            float targetingRadius = Misc.getTargetingRadius(missile.getLocation(), target, true);
            float targetingDistance = distance - targetingRadius;
            if (targetingDistance <= TARGET_MIRV_DISTANCE) {
                clearTarget = true;
            }
        }

        if (clearTarget && (timeLive >= minTimeToMirv) && (mirvCooldown <= 0f) && (mirvIndex < MAX_MIRVS)) {
            mirv(missile);
        }

        float velocityFacing = VectorUtils.getFacing(calculationVelocity);
        float absoluteDistance = MathUtils.getShortestRotation(velocityFacing,
                VectorUtils.getAngleStrict(missile.getLocation(), guidedTarget));
        float angularDistance = MathUtils.getShortestRotation(missile.getFacing(),
                VectorUtils.getAngleStrict(missile.getLocation(), guidedTarget));
        float compensationDifference = MathUtils.getShortestRotation(angularDistance, absoluteDistance);
        if (Math.abs(compensationDifference) <= 75f) {
            angularDistance += 0.5f * compensationDifference;
        }
        float absDAng = Math.abs(angularDistance);

        if (aspectLocked && (absDAng > 75f)) {
            aspectLocked = false;
        }

        if (!aspectLocked && (absDAng <= 30f)) {
            aspectLocked = true;
        }

        missile.giveCommand((angularDistance < 0) ? ShipCommand.TURN_RIGHT : ShipCommand.TURN_LEFT);
        float turnRadius = missile.getMaxSpeed() * (360f / missile.getMaxTurnRate()) / (2f * (float) Math.PI);
        if (aspectLocked || (distance > (2.5f * turnRadius))) {
            missile.giveCommand(ShipCommand.ACCELERATE);
        }
        if (absDAng < 5) {
            float MFlightAng = VectorUtils.getAngleStrict(ZERO, calculationVelocity);
            float MFlightCC = MathUtils.getShortestRotation(missile.getFacing(), MFlightAng);
            if (Math.abs(MFlightCC) > 20) {
                missile.giveCommand(MFlightCC < 0 ? ShipCommand.STRAFE_LEFT : ShipCommand.STRAFE_RIGHT);
            }
        }

        if (absDAng < (Math.abs(missile.getAngularVelocity()) * VELOCITY_DAMPING_FACTOR)) {
            missile.setAngularVelocity(angularDistance / VELOCITY_DAMPING_FACTOR);
        }
    }

    @Override
    protected boolean acquireTarget(float amount) {
        if (!isTargetValidAlternate(target)) {
            if (retargetTimer > 0f) {
                retargetTimer -= amount;
                return false;
            } else {
                retargetTimer = 1f;
            }
            setTarget(findBestTarget());
            if (target == null) {
                setTarget(findBestTargetAlternate());
            }
            if (target == null) {
                return false;
            }
        } else {
            retargetTimer = 1f;
            if (!isTargetValidAlternate(target)) {
                CombatEntityAPI newTarget = findBestTarget();
                if (newTarget != null) {
                    target = newTarget;
                }
            }
        }
        return true;
    }

    protected ShipAPI findBestTargetAlternate() {
        ShipAPI closest = null;
        float range = getRemainingRange();
        float distance, closestDistance = getRemainingRange() + missile.getMaxSpeed() * 2f;
        List<ShipAPI> ships = AIUtils.getEnemiesOnMap(missile);
        int size = ships.size();
        for (int i = 0; i < size; i++) {
            ShipAPI tmp = ships.get(i);
            float mod = 0f;
            if (tmp.isFighter() || tmp.isDrone()) {
                mod = range / 2f;
            }
            if (!isTargetValid(tmp)) {
                mod = range;
            }
            distance = MathUtils.getDistance(tmp, missile.getLocation()) + mod;
            if (distance < closestDistance) {
                closest = tmp;
                closestDistance = distance;
            }
        }
        return closest;
    }

    @Override
    protected boolean isTargetValid(CombatEntityAPI target) {
        if (target instanceof ShipAPI) {
            ShipAPI ship = (ShipAPI) target;
            if (ship.isFighter() || ship.isDrone()) {
                return false;
            }
        }
        return super.isTargetValid(target);
    }

    protected boolean isTargetValidAlternate(CombatEntityAPI target) {
        return super.isTargetValid(target);
    }
}
