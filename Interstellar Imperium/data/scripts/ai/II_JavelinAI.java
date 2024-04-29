package data.scripts.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.util.IntervalUtil;
import java.awt.Color;
import java.util.List;
import org.lazywizard.lazylib.FastTrig;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

public class II_JavelinAI extends II_BaseMissile {

    private static final float ANTI_CLUMP_RANGE = 125f;
    private static final float DEAD_TIME_RATIO = 22f;
    private static final float ENGINE_DEAD_TIME_FACTOR_MAX = 1.05f;
    private static final float ENGINE_DEAD_TIME_FACTOR_MIN = 0.95f;
    private static final Color FLARE_COLOR = new Color(255, 140, 100);
    private static final float LEAD_GUIDANCE_FACTOR = 0.6f;
    private static final float LEAD_GUIDANCE_FACTOR_FROM_ECCM = 0.4f;
    private static final float VELOCITY_DAMPING_FACTOR = 0.15f;
    private static final float WEAVE_DISTANCE_MAX = 1500f;
    private static final float WEAVE_SINE_A_AMPLITUDE = 30f; // degrees offset
    private static final float WEAVE_SINE_A_PERIOD = 4f;
    private static final float WEAVE_SINE_B_AMPLITUDE = 20f; // degrees offset
    private static final float WEAVE_SINE_B_PERIOD = 1.5f;

    private final IntervalUtil antiClumpInterval = new IntervalUtil(0.1f, 0.25f);
    private boolean aspectLocked = false;
    private float engineDeadTime;
    private float nearestJavelinAngle = 180f;
    private float nearestJavelinDistance = Float.MAX_VALUE;
    private float timeAccum = 0f;
    private final float weaveSineAPhase;
    private final float weaveSineBPhase;

    public II_JavelinAI(MissileAPI missile, ShipAPI launchingShip) {
        super(missile, launchingShip);
        missile.setEmpResistance(missile.getEmpResistance() + 2);

        weaveSineAPhase = (float) (Math.random() * Math.PI * 2.0);
        weaveSineBPhase = (float) (Math.random() * Math.PI * 2.0);

        float deadTimeScale = missile.getMaxFlightTime() / DEAD_TIME_RATIO;
        engineDeadTime = MathUtils.getRandomNumberInRange(ENGINE_DEAD_TIME_FACTOR_MIN, ENGINE_DEAD_TIME_FACTOR_MAX)
                * deadTimeScale;
    }

    @Override
    public void advance(float amount) {
        boolean noEngines;
        noEngines = missile.isFizzling() || missile.isFading();

        float maxSpeed = missile.getMaxSpeed();

        if (engineDeadTime > 0f) {
            engineDeadTime -= amount;
            if (engineDeadTime <= 0f && aspectLocked) {
                Vector2f offset = new Vector2f(-14f, 0f);
                VectorUtils.rotate(offset, missile.getFacing(), offset);
                Vector2f.add(offset, missile.getLocation(), offset);
                Global.getCombatEngine().addHitParticle(offset, missile.getVelocity(), 125f, 0.5f, 0.25f, FLARE_COLOR);
            } else {
                if (engineDeadTime <= 0f) {
                    engineDeadTime = 0.01f;
                }
                if (missile.getVelocity().length() > maxSpeed * 0.5f) {
                    missile.giveCommand(ShipCommand.DECELERATE);
                }
            }
        }

        timeAccum += amount;

        if (!acquireTarget(amount)) {
            if (engineDeadTime <= 0f && !noEngines) {
                missile.giveCommand(ShipCommand.ACCELERATE);
            }
            return;
        }

        float distance = MathUtils.getDistance(target.getLocation(), missile.getLocation());
        float guidance = LEAD_GUIDANCE_FACTOR;
        if (missile.getSource() != null) {
            guidance += Math.min(missile.getSource().getMutableStats().getMissileGuidance().getModifiedValue()
                    - missile.getSource().getMutableStats().getMissileGuidance().getBaseValue(), 1f)
                    * LEAD_GUIDANCE_FACTOR_FROM_ECCM;
        }
        Vector2f guidedTarget = intercept(missile.getLocation(), maxSpeed, target.getLocation(), target.getVelocity());
        if (guidedTarget == null) {
            Vector2f projection = new Vector2f(target.getVelocity());
            float scalar = distance / maxSpeed;
            projection.scale(scalar);
            guidedTarget = Vector2f.add(target.getLocation(), projection, null);
        }
        Vector2f.sub(guidedTarget, target.getLocation(), guidedTarget);
        guidedTarget.scale(guidance);
        Vector2f.add(guidedTarget, target.getLocation(), guidedTarget);

        float weaveFactor = Math.max(0.5f, Math.min(1f, 0.5f + 0.5f * distance / WEAVE_DISTANCE_MAX));

        float weaveSineA = WEAVE_SINE_A_AMPLITUDE * (float) FastTrig.sin((2.0 * Math.PI * timeAccum
                / WEAVE_SINE_A_PERIOD) + weaveSineAPhase);
        float weaveSineB = WEAVE_SINE_B_AMPLITUDE * (float) FastTrig.sin((2.0 * Math.PI * timeAccum
                / WEAVE_SINE_B_PERIOD) + weaveSineBPhase);
        float weaveOffset = (weaveSineA + weaveSineB) * weaveFactor;

        if (antiClumpInterval.intervalElapsed()) {
            nearestJavelinDistance = Float.MAX_VALUE;
            nearestJavelinAngle = 180f;
            List<MissileAPI> nearbyJavelins = CombatUtils.getMissilesWithinRange(missile.getLocation(), ANTI_CLUMP_RANGE);
            for (MissileAPI nearbyJavelin : nearbyJavelins) {
                if (nearbyJavelin == missile) {
                    continue;
                }

                if ((nearbyJavelin.getProjectileSpecId() != null) && (missile.getProjectileSpecId() != null)
                        && nearbyJavelin.getProjectileSpecId().contentEquals(missile.getProjectileSpecId())) {
                    float javelinDistance = MathUtils.getDistance(missile.getLocation(), nearbyJavelin.getLocation());
                    if (javelinDistance < nearestJavelinDistance) {
                        nearestJavelinDistance = javelinDistance;
                        nearestJavelinAngle = VectorUtils.getAngleStrict(missile.getLocation(), nearbyJavelin.getLocation());
                    }
                }
            }
        }

        float angularDistance;
        if (aspectLocked) {
            angularDistance = MathUtils.getShortestRotation(missile.getFacing(),
                    MathUtils.clampAngle(VectorUtils.getAngleStrict(missile.getLocation(), guidedTarget) + weaveOffset));
        } else {
            angularDistance = MathUtils.getShortestRotation(missile.getFacing(),
                    MathUtils.clampAngle(VectorUtils.getAngleStrict(missile.getLocation(), guidedTarget)));
        }

        float nearestJavelinAngularDistance = MathUtils.getShortestRotation(missile.getFacing(), nearestJavelinAngle);
        if ((nearestJavelinDistance <= ANTI_CLUMP_RANGE) && (Math.abs(nearestJavelinAngularDistance) <= 135f)) {
            if (nearestJavelinAngularDistance <= 0f) {
                angularDistance += 0.75f * (1f - nearestJavelinDistance / ANTI_CLUMP_RANGE) * (135f + nearestJavelinAngularDistance);
            } else {
                angularDistance += 0.75f * (1f - nearestJavelinDistance / ANTI_CLUMP_RANGE) * (-135f + nearestJavelinAngularDistance);
            }
        }

        float absDAng = Math.abs(angularDistance);

        if (!noEngines) {
            missile.giveCommand(angularDistance < 0 ? ShipCommand.TURN_RIGHT : ShipCommand.TURN_LEFT);
        }

//        float aimAllowance = Math.max(80f * weaveFactor, 20f) * Math.min(Math.max(distance / 300f, 0.25f), 1f);
//        if (engineDeadTime > 0f) {
//            aimAllowance *= 0.75f;
//        }
//        aimAllowance = Math.max(aimAllowance, 40f);
//        if (aspectLocked && absDAng > aimAllowance) {
//            aspectLocked = false;
//        }
        if (!aspectLocked && absDAng <= 25f) {
            aspectLocked = true;
        }

        if (aspectLocked || missile.getVelocity().length() <= maxSpeed * 0.4f) {
            if (engineDeadTime <= 0f && !noEngines) {
                missile.giveCommand(ShipCommand.ACCELERATE);
            }
        }

        if (absDAng < Math.abs(missile.getAngularVelocity()) * VELOCITY_DAMPING_FACTOR) {
            missile.setAngularVelocity(angularDistance / VELOCITY_DAMPING_FACTOR);
        }
    }

    @Override
    protected boolean acquireTarget(float amount) {
        if (!isTargetValidAlternate(target)) {
            if (target instanceof ShipAPI) {
                ShipAPI ship = (ShipAPI) target;
                if (ship.isPhased() && ship.isAlive()) {
                    return false;
                }
            }
            setTarget(findBestTarget());
            if (target == null) {
                setTarget(findBestTargetAlternate());
            }
            if (target == null) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected ShipAPI findBestTarget() {
        ShipAPI best = null;
        float weight, bestWeight = 0f;
        List<ShipAPI> ships = AIUtils.getEnemiesOnMap(missile);
        float maxDistance = getRemainingRange() + missile.getMaxSpeed() * 2f;
        int size = ships.size();
        for (int i = 0; i < size; i++) {
            ShipAPI tmp = ships.get(i);
            float mod;
            if (!isTargetValid(tmp)) {
                mod = 0f;
            } else {
                switch (tmp.getHullSize()) {
                    case FIGHTER:
                        mod = 0.1f;
                        break;
                    case FRIGATE:
                        mod = 3f;
                        break;
                    case DESTROYER:
                        mod = 8f;
                        break;
                    case CRUISER:
                        mod = 12f;
                        break;
                    case CAPITAL_SHIP:
                        mod = 15f;
                        break;
                    default:
                        mod = 0f;
                        break;
                }
            }
            float distance = MathUtils.getDistance(tmp, missile.getLocation());
            if (distance > maxDistance) {
                continue;
            }
            weight = (2500f / Math.max(distance, 250f)) * mod;
            if (weight > bestWeight) {
                best = tmp;
                bestWeight = weight;
            }
        }
        return best;
    }

    protected ShipAPI findBestTargetAlternate() {
        ShipAPI best = null;
        float weight, bestWeight = 0f;
        List<ShipAPI> ships = AIUtils.getEnemiesOnMap(missile);
        float maxDistance = getRange() + missile.getMaxSpeed() * 2f;
        int size = ships.size();
        for (int i = 0; i < size; i++) {
            ShipAPI tmp = ships.get(i);
            float mod;
            if (!isTargetValidAlternate(tmp)) {
                mod = 0f;
            } else {
                switch (tmp.getHullSize()) {
                    case FIGHTER:
                        mod = 0.1f;
                        break;
                    case FRIGATE:
                        mod = 3f;
                        break;
                    case DESTROYER:
                        mod = 8f;
                        break;
                    case CRUISER:
                        mod = 12f;
                        break;
                    case CAPITAL_SHIP:
                        mod = 15f;
                        break;
                    default:
                        mod = 0f;
                        break;
                }
            }
            float distance = MathUtils.getDistance(tmp, missile.getLocation());
            if (distance > maxDistance) {
                continue;
            }
            weight = (2500f / Math.max(distance, 250f)) * mod;
            if (weight > bestWeight) {
                best = tmp;
                bestWeight = weight;
            }
        }
        return best;
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
