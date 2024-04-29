package data.scripts.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatAssignmentType;
import com.fs.starfarer.api.combat.CombatFleetManagerAPI.AssignmentInfo;
import com.fs.starfarer.api.combat.DeployedFleetMemberAPI;
import com.fs.starfarer.api.combat.ShipAIConfig;
import com.fs.starfarer.api.combat.ShipAIPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import data.scripts.util.LE_Multi;
import java.util.List;

import data.scripts.weapons.LE_LightsEveryFrame;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class LE_MightAI implements ShipAIPlugin {
    //Code Courtesy of the Titan code from Interstellar Imperium by Dark Revenant
    private static final float DISTANCE_TO_ARM = 800f;
    private static final float VELOCITY_DAMPING_FACTOR = 1f;

    private static Vector2f assignedTarget(ShipAPI ship) {
        AssignmentInfo assignment = Global.getCombatEngine().getFleetManager(ship.getOwner()).getTaskManager(ship.isAlly()).getAssignmentFor(ship);
        if (assignment == null) {
            return null;
        }
        if (assignment.getType() == CombatAssignmentType.ENGAGE
                || assignment.getType() == CombatAssignmentType.HARASS
                || assignment.getType() == CombatAssignmentType.INTERCEPT
                || assignment.getType() == CombatAssignmentType.STRIKE
                || assignment.getType() == CombatAssignmentType.AVOID) {
            return assignment.getTarget().getLocation();
        } else {
            DeployedFleetMemberAPI dfm = Global.getCombatEngine().getFleetManager(ship.getOwner()).getDeployedFleetMember(ship);
            if (dfm != null) {
                Global.getCombatEngine().getFleetManager(ship.getOwner()).getTaskManager(ship.isAlly()).orderSearchAndDestroy(dfm, false);
            }
            return null;
        }
    }

    private static Vector2f assignedVelocity(ShipAPI ship) {
        AssignmentInfo assignment = Global.getCombatEngine().getFleetManager(ship.getOwner()).getTaskManager(ship.isAlly()).getAssignmentFor(ship);
        if (assignment == null) {
            return null;
        }
        if (assignment.getType() == CombatAssignmentType.ENGAGE
                || assignment.getType() == CombatAssignmentType.HARASS
                || assignment.getType() == CombatAssignmentType.INTERCEPT
                || assignment.getType() == CombatAssignmentType.STRIKE
                || assignment.getType() == CombatAssignmentType.AVOID) {
            return assignment.getTarget().getVelocity();
        } else {
            DeployedFleetMemberAPI dfm = Global.getCombatEngine().getFleetManager(ship.getOwner()).getDeployedFleetMember(ship);
            if (dfm != null) {
                Global.getCombatEngine().getFleetManager(ship.getOwner()).getTaskManager(ship.isAlly()).orderSearchAndDestroy(dfm, false);
            }
            return null;
        }
    }

    private static ShipAPI findBestTarget(ShipAPI ship) {
        ShipAPI largest = null;
        float size, largestSize = 0f;
        List<ShipAPI> enemies = AIUtils.getEnemiesOnMap(ship);
        int enemiesSize = enemies.size();
        for (int i = 0; i < enemiesSize; i++) {
            ShipAPI tmp = enemies.get(i);
            if (tmp.getOwner() == ship.getOwner() || tmp.isHulk() || tmp.isShuttlePod() || tmp.isFighter()
                    || tmp.isDrone() || !LE_Multi.isRoot(tmp)) {
                continue;
            }
            size = tmp.getCollisionRadius();
            if (size > largestSize) {
                largest = tmp;
                largestSize = size;
            }
        }
        return largest;
    }

    private static Vector2f intercept(Vector2f point, float speed, float acceleration, float maxspeed, Vector2f target,
            Vector2f targetVel) {
        Vector2f difference = new Vector2f(target.x - point.x, target.y - point.y);

        float s = speed;
        float a = acceleration / 2f;
        float b = speed;
        float c = difference.length();
        Vector2f solutionSet = quad(a, b, c);
        if (solutionSet != null) {
            float t = Math.min(solutionSet.x, solutionSet.y);
            if (t < 0) {
                t = Math.max(solutionSet.x, solutionSet.y);
            }
            if (t > 0) {
                s = acceleration * t;
                s = s / 2f + speed;
                s = Math.min(s, maxspeed);
            }
        }

        a = targetVel.x * targetVel.x + targetVel.y * targetVel.y - s * s;
        b = 2 * (targetVel.x * difference.x + targetVel.y * difference.y);
        c = difference.x * difference.x + difference.y * difference.y;

        solutionSet = quad(a, b, c);

        Vector2f intercept = null;
        if (solutionSet != null) {
            float bestFit = Math.min(solutionSet.x, solutionSet.y);
            if (bestFit < 0) {
                bestFit = Math.max(solutionSet.x, solutionSet.y);
            }
            if (bestFit > 0) {
                intercept = new Vector2f(target.x + targetVel.x * bestFit, target.y + targetVel.y * bestFit);
            }
        }

        return intercept;
    }

    private static Vector2f quad(float a, float b, float c) {
        Vector2f solution = null;
        if (Float.compare(Math.abs(a), 0) == 0) {
            if (Float.compare(Math.abs(b), 0) == 0) {
                solution = (Float.compare(Math.abs(c), 0) == 0) ? new Vector2f(0, 0) : null;
            } else {
                solution = new Vector2f(-c / b, -c / b);
            }
        } else {
            float d = b * b - 4 * a * c;
            if (d >= 0) {
                d = (float) Math.sqrt(d);
                float e = 2 * a;
                solution = new Vector2f((-b - d) / e, (-b + d) / e);
            }
        }
        return solution;
    }

    private boolean armed;
    private boolean armedSdw;
    private float armingTimer;
    private final ShipwideAIFlags flags = new ShipwideAIFlags();
    private final ShipAPI ship;
    private ShipAPI target;

    private final ShipAIConfig config = new ShipAIConfig();

    public LE_MightAI(ShipAPI ship) {
        this.ship = ship;

        armingTimer = 5f;
        armed = false;
        armedSdw = false;
        target = findBestTarget(ship);
    }

    @Override
    public void advance(float amount) {
        if (Global.getCombatEngine().isPaused() || !ship.isAlive()) {
            return;
        }

        ship.giveCommand(ShipCommand.ACCELERATE, null, 0);

        if (armingTimer > 0f) {
            armingTimer -= amount;
            if (armingTimer <= 0f) {
                ship.setCollisionClass(CollisionClass.SHIP);
                ship.getMutableStats().getDynamic().getMod(LE_LightsEveryFrame.LIGHTS_ALPHA_ID).modifyFlat("ii_titan_ai", 1f);
            }

            return;
        }

        if ((float) Math.random() >= 0.95f) {
            target = findBestTarget(ship);
        }

        if (target == null || (target instanceof ShipAPI && target.isHulk()) || (ship.getOwner() == target.getOwner())
                || !Global.getCombatEngine().isEntityInPlay(target)) {
            target = findBestTarget(ship);
            return;
        }

        Vector2f targetLocation = assignedTarget(ship);
        Vector2f targetVelocity = assignedVelocity(ship);
        if (targetLocation == null || targetVelocity == null) {
            targetLocation = target.getLocation();
            targetVelocity = target.getVelocity();
        }

        float distance = MathUtils.getDistance(ship.getLocation(), targetLocation);
        float acceleration = ship.getMutableStats().getAcceleration().getModifiedValue();
        float maxSpeed = ship.getMutableStats().getMaxSpeed().getModifiedValue();
        Vector2f guidedTarget = intercept(ship.getLocation(), ship.getVelocity().length(), acceleration, maxSpeed,
                targetLocation, targetVelocity);
        if (guidedTarget == null) {
            Vector2f projection = new Vector2f(targetVelocity);
            float scalar = distance / 110f;
            projection.scale(scalar);
            guidedTarget = Vector2f.add(targetLocation, projection, null);
        }

        if (guidedTarget == null || ship.getLocation() == null) {
            return;
        }

        float velocityFacing = VectorUtils.getFacing(ship.getVelocity());
        float absoluteDistance = MathUtils.getShortestRotation(velocityFacing, VectorUtils.getAngleStrict(ship.getLocation(),
                guidedTarget));
        float angularDistance = MathUtils.getShortestRotation(ship.getFacing(), VectorUtils.getAngleStrict(ship.getLocation(),
                guidedTarget));
        float compensationDifference = MathUtils.getShortestRotation(angularDistance, absoluteDistance);
        if (Math.abs(compensationDifference) <= 75f) {
            angularDistance += 0.5f * compensationDifference;
        }
        float absAngularDistance = Math.abs(angularDistance);

        if (!armed) {
            float overallDangerLevel = 0f;
            List<ShipAPI> enemies = AIUtils.getNearbyEnemies(ship, 2000f);
            int enemiesSize = enemies.size();
            for (int i = 0; i < enemiesSize; i++) {
                ShipAPI enemy = enemies.get(i);
                float dangerLevel = 0f;
                if (enemy.isFighter()) {
                    dangerLevel += 0.1f;
                } else if (enemy.isFrigate()) {
                    dangerLevel += 1f;
                } else if (enemy.isDestroyer()) {
                    dangerLevel += 2f;
                } else if (enemy.isCruiser()) {
                    dangerLevel += 3.5f;
                } else if (enemy.isCapital()) {
                    dangerLevel += 5f;
                }

                overallDangerLevel += dangerLevel;
            }

            float distanceThreshold = DISTANCE_TO_ARM + target.getCollisionRadius() + overallDangerLevel * 20f;
            if (distance <= distanceThreshold || (ship.getHullLevel() <= 0.5f)) {
                armed = true;
            }
        }

        float turnFlipChance = 0f;
        if (Math.abs(angularDistance) < (Math.abs(ship.getAngularVelocity()) * VELOCITY_DAMPING_FACTOR)) {
            turnFlipChance = 1f - (0.5f * (Math.abs(angularDistance) / VELOCITY_DAMPING_FACTOR));
        }

        if (absAngularDistance > 2f && !ship.getTravelDrive().isOn()) {
            if (Math.random() < turnFlipChance) {
                ship.giveCommand(angularDistance > 0f ? ShipCommand.TURN_RIGHT : ShipCommand.TURN_LEFT, null, 0);
            } else {
                ship.giveCommand(angularDistance > 0f ? ShipCommand.TURN_LEFT : ShipCommand.TURN_RIGHT, null, 0);
            }
        }

//        if (AbsAngD < 5) {
//            float MFlightAng = VectorUtils.getAngleStrict(ZERO, ship.getVelocity());
//            float MFlightCC = MathUtils.getShortestRotation(ship.getFacing(), MFlightAng);
//            if (Math.abs(MFlightCC) > 20) {
//                ship.giveCommand(MFlightCC < 0 ? ShipCommand.STRAFE_LEFT : ShipCommand.STRAFE_RIGHT, null, 0);
//            }
//        }
//
        if (Math.abs(angularDistance) < Math.abs(ship.getAngularVelocity()) * VELOCITY_DAMPING_FACTOR) {
            ship.setAngularVelocity(angularDistance / VELOCITY_DAMPING_FACTOR);
        }
        if (armed && !armedSdw) {
            armedSdw = armed;
            ship.giveCommand(ShipCommand.USE_SYSTEM, null, 0);
        }
    }

    @Override
    public void cancelCurrentManeuver() {
    }

    @Override
    public void forceCircumstanceEvaluation() {
        target = findBestTarget(ship);
    }

    @Override
    public ShipwideAIFlags getAIFlags() {
        return flags;
    }

    @Override
    public void setDoNotFireDelay(float amount) {
    }

    @Override
    public boolean needsRefit() {
        return false;
    }

    @Override
    public ShipAIConfig getConfig() {
        return config;
    }
}
