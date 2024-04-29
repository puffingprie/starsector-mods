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
import com.fs.starfarer.api.combat.WeaponGroupAPI;
import data.scripts.util.II_Multi;
import data.scripts.util.II_Util;
import data.scripts.weapons.II_LightsEveryFrame;
import java.util.List;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class II_TitanMIRVAI implements ShipAIPlugin {

    private static final float VELOCITY_DAMPING_FACTOR = 3f;

    private static Vector2f assignedTarget(ShipAPI ship) {
        AssignmentInfo assignment = Global.getCombatEngine().getFleetManager(ship.getOwner()).getTaskManager(
                ship.isAlly()).getAssignmentFor(ship);
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

    private static ShipAPI findBestTarget(ShipAPI ship) {
        ShipAPI largest = null;
        float size, largestSize = 0f;
        List<ShipAPI> enemies = AIUtils.getEnemiesOnMap(ship);
        int enemiesSize = enemies.size();
        for (int i = 0; i < enemiesSize; i++) {
            ShipAPI tmp = enemies.get(i);
            if ((tmp.getOwner() == ship.getOwner()) || tmp.isHulk() || tmp.isShuttlePod() || tmp.isFighter()
                    || tmp.isDrone() || !II_Multi.isRoot(tmp) || (tmp.getOwner() == 100)) {
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

    private final float angleOffset;
    private float armingTimer;
    private float delayTimer;
    private boolean doorsOn;
    private boolean firedNow;
    private float doorTimer;
    private final ShipwideAIFlags flags = new ShipwideAIFlags();
    private final ShipAPI ship;
    private ShipAPI target;

    private final ShipAIConfig config = new ShipAIConfig();

    public II_TitanMIRVAI(ShipAPI ship) {
        this.ship = ship;

        angleOffset = MathUtils.getRandomNumberInRange(-5f, 5f);
        armingTimer = 5f;
        delayTimer = 0f;
        doorTimer = 1f;
        doorsOn = true;
        firedNow = false;
        target = findBestTarget(ship);
    }

    @Override
    public void advance(float amount) {
        if (Global.getCombatEngine().isPaused() || !ship.isAlive()) {
            return;
        }

        if (armingTimer > 0f) {
            armingTimer -= amount;

            for (ShipAPI module : this.ship.getChildModulesCopy()) {
                module.getMutableStats().getDynamic().getMod(II_LightsEveryFrame.LIGHTS_ALPHA_ID).modifyFlat("ii_titanmirv_ai", 1f);
            }
            if (armingTimer <= 0f) {
                ship.setCollisionClass(CollisionClass.SHIP);
            }

            ship.giveCommand(ShipCommand.ACCELERATE, null, 0);
            return;
        }

        if ((float) Math.random() >= 0.95f) {
            target = findBestTarget(ship);
        }

        if (target == null || (target instanceof ShipAPI && target.isHulk()) || (ship.getOwner() == target.getOwner())
                || !Global.getCombatEngine().isEntityInPlay(target)) {
            target = findBestTarget(ship);

            ship.giveCommand(ShipCommand.ACCELERATE, null, 0);
            return;
        }

        Vector2f targetLocation = assignedTarget(ship);
        if (targetLocation == null) {
            targetLocation = target.getLocation();
        }

        if (!doorsOn) {
            if (doorTimer > 0f) {
                doorTimer -= amount;
            }
        }

        if (delayTimer > 0f) {
            delayTimer -= amount;
        }

        float distance = MathUtils.getDistance(ship.getLocation(), targetLocation);
        Vector2f guidedTarget = targetLocation;

        if (guidedTarget == null || ship.getLocation() == null) {
            ship.giveCommand(ShipCommand.ACCELERATE, null, 0);
            return;
        }

        float swingaroundDistance;
        float refireTimer;
        float fireRange;
        float turnRadius = 1000f;
        if (II_Util.getNonDHullId(ship.getHullSpec()).contentEquals("ii_titan_armor")) {
            swingaroundDistance = 2000f;
            refireTimer = 2f;
            fireRange = 2000f;
        } else {
            swingaroundDistance = 3500f;
            refireTimer = 2.5f;
            fireRange = 10000f;
        }

        boolean approachingMapBorder = false;
        if (((ship.getLocation().x >= ((Global.getCombatEngine().getMapWidth() * 0.5f) - ship.getCollisionRadius())) && (Math.abs(MathUtils.getShortestRotation(ship.getFacing(), 0f)) < 45f))
                || ((ship.getLocation().x <= ((Global.getCombatEngine().getMapWidth() * -0.5f) + ship.getCollisionRadius())) && (Math.abs(MathUtils.getShortestRotation(ship.getFacing(), 180f)) < 45f))
                || ((ship.getLocation().y >= ((Global.getCombatEngine().getMapHeight() * 0.5f) - ship.getCollisionRadius())) && (Math.abs(MathUtils.getShortestRotation(ship.getFacing(), 90f)) < 45f))
                || ((ship.getLocation().y <= ((Global.getCombatEngine().getMapHeight() * -0.5f) + ship.getCollisionRadius())) && (Math.abs(MathUtils.getShortestRotation(ship.getFacing(), 270f)) < 45f))) {
            approachingMapBorder = true;
        }

        float targetAngle = MathUtils.clampAngle(VectorUtils.getAngleStrict(ship.getLocation(), guidedTarget) + angleOffset);
        float targetArc = MathUtils.getShortestRotation(ship.getFacing(), VectorUtils.getAngleStrict(ship.getLocation(), guidedTarget));
        if (distance <= (swingaroundDistance + turnRadius)) {
            float targetAngleOffset = -Math.signum(targetArc) * 90f;
            if (approachingMapBorder) {
                targetAngleOffset *= -1f;
            }
            if (distance > swingaroundDistance) {
                targetAngleOffset *= II_Util.lerp(1f, 0f, (distance - swingaroundDistance) / turnRadius);
            } else {
                targetAngleOffset *= II_Util.lerp(1f, 1.5f, Math.min(1f, (swingaroundDistance - distance) / turnRadius));
            }
            targetAngle += targetAngleOffset;
        }

        float angularDistance = MathUtils.getShortestRotation(ship.getFacing(), targetAngle);
        float absAngularDistance = Math.abs(angularDistance);

        if (absAngularDistance <= 45f) {
            ship.giveCommand(ShipCommand.ACCELERATE, null, 0);
        } else {
            ship.giveCommand(ShipCommand.DECELERATE, null, 0);
        }

        if (!doorsOn && (delayTimer <= 0f) && (doorTimer <= 0f)) {
            List<ShipAPI> enemies = AIUtils.getNearbyEnemies(ship, fireRange);
            if (!enemies.isEmpty()) {
                if (!ship.getWeaponGroupsCopy().isEmpty()) {
                    int currGroup = 0;
                    for (WeaponGroupAPI group : ship.getWeaponGroupsCopy()) {
                        if (group == ship.getSelectedGroupAPI()) {
                            break;
                        } else {
                            currGroup++;
                        }
                    }
                    int nextGroup = currGroup + 1;
                    if (nextGroup >= ship.getWeaponGroupsCopy().size()) {
                        nextGroup = 0;
                    }
                    if (firedNow) {
                        ship.giveCommand(ShipCommand.SELECT_GROUP, null, nextGroup);
                        firedNow = false;
                    } else {
                        ship.giveCommand(ShipCommand.FIRE, ship.getMouseTarget(), currGroup);
                        firedNow = true;
                        delayTimer = refireTimer;
                    }
                }
            }
        }

        if (doorsOn) {
            float overallDangerLevel = 0f;
            List<ShipAPI> enemies = AIUtils.getNearbyEnemies(ship, 3000f);
            int enemiesSize = enemies.size();
            for (int i = 0; i < enemiesSize; i++) {
                ShipAPI enemy = enemies.get(i);
                float dangerLevel = 0f;
                if (enemy.isFighter()) {
                    dangerLevel += 0.2f;
                } else if (enemy.isFrigate()) {
                    dangerLevel += 1f;
                } else if (enemy.isDestroyer()) {
                    dangerLevel += 2f;
                } else if (enemy.isCruiser()) {
                    dangerLevel += 3.5f;
                } else if (enemy.isCapital()) {
                    dangerLevel += 5f;
                }

                if (enemy.getShipTarget() == ship) {
                    dangerLevel *= 2.5f;
                }

                overallDangerLevel += dangerLevel;
            }

            float distanceThreshold = fireRange + target.getCollisionRadius() + overallDangerLevel * 50f;
            if (distance <= distanceThreshold || ship.getHullLevel() <= 0.5f) {
                ship.getMutableStats().getDynamic().getMod(II_LightsEveryFrame.LIGHTS_ALPHA_ID).modifyFlat("ii_titanmirv_ai", 1f);
                ship.useSystem();
                if (!ship.getWeaponGroupsCopy().isEmpty()) {
                    ship.giveCommand(ShipCommand.SELECT_GROUP, null, 0);
                }
                doorsOn = false;
            }
        }

        float turnFlipChance = 0f;
        if (Math.abs(angularDistance) < (Math.abs(ship.getAngularVelocity()) * VELOCITY_DAMPING_FACTOR)) {
            turnFlipChance = 1f - (0.5f * (Math.abs(angularDistance) / VELOCITY_DAMPING_FACTOR));
        }

        if (absAngularDistance > 5f && !ship.getTravelDrive().isOn()) {
            if (Math.random() < turnFlipChance) {
                ship.giveCommand(angularDistance > 0f ? ShipCommand.TURN_RIGHT : ShipCommand.TURN_LEFT, null, 0);
            } else {
                ship.giveCommand(angularDistance > 0f ? ShipCommand.TURN_LEFT : ShipCommand.TURN_RIGHT, null, 0);
            }
        }

//        if (AbsAngD < 5) {
//            float MFlightAng = VectorUtils.getAngle(ZERO, ship.getVelocity());
//            float MFlightCC = MathUtils.getShortestRotation(ship.getFacing(), MFlightAng);
//            if (Math.abs(MFlightCC) > 20) {
//                ship.giveCommand(MFlightCC < 0 ? ShipCommand.STRAFE_LEFT : ShipCommand.STRAFE_RIGHT, null, 0);
//            }
//        }
        if (Math.abs(angularDistance) < Math.abs(ship.getAngularVelocity()) * VELOCITY_DAMPING_FACTOR) {
            ship.setAngularVelocity(angularDistance / VELOCITY_DAMPING_FACTOR);
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
