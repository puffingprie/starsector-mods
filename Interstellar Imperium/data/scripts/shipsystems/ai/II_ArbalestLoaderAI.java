package data.scripts.shipsystems.ai;

import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatAssignmentType;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.CombatFleetManagerAPI.AssignmentInfo;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponGroupAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import data.scripts.hullmods.II_BasePackage;
import data.scripts.shipsystems.II_ArbalestLoaderStats;
import java.util.Collections;
import java.util.List;
import org.lazywizard.lazylib.CollectionUtils;
import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

public class II_ArbalestLoaderAI implements ShipSystemAIScript {

    private static final float MAGNA_FULMEN_RANGE_STANDARD = 1000f;
    private static final float MAGNA_FULMEN_EXTRA_RANGE_STANDARD = 250f;
    private static final float MAGNA_FULMEN_EXTRA_RANGE_ELITE = 325f;
    private static final float FIRING_LINE_ARC_STANDARD = 15f;
    private static final float FIRING_LINE_ARC_ARMOR = 30f;
    private static final float FIRING_LINE_ARC_ARMOR_ACTUAL = 20f;
    private static final float OTHER_TARGET_WEIGHT_THRESHOLD = 4f;
    private static final float TARGET_DESIRE_STANDARD = 0.4f;
    private static final float TARGET_DESIRE_ARMOR = 0.5f;
    private static final float BASE_PROJECTILE_SPEED = 1500f;

    private CombatEngineAPI engine;
    private ShipwideAIFlags flags;
    private ShipAPI ship;
    private ShipSystemAPI system;

    private final IntervalUtil tracker = new IntervalUtil(0.2f, 0.3f);

    private static final boolean DEBUG = false;
    private final Object STATUSKEY1 = new Object();
    private final Object STATUSKEY2 = new Object();
    private float desireShow = 0f;
    private float targetDesireShow = 0f;
    private boolean allyInFiringLineShow = false;
    private boolean enemyInFiringLineShow = false;
    private boolean otherTargetsInFiringLineShow = false;
    private Vector2f aimPointShow = null;
    private boolean stillWantsToUseSystem = false;

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
        if (engine == null) {
            return;
        }

        if (engine.isPaused()) {
            if (DEBUG) {
                displayDebug();
            }
            return;
        }

        int armed = II_ArbalestLoaderStats.getArmed(ship);

        /* Force-fire setup */
        WeaponGroupAPI magnaFulmenGroup = null;
        WeaponAPI magnaFulmen = null;
        for (WeaponAPI weapon : ship.getUsableWeapons()) {
            if (weapon.getId().contentEquals("ii_magna_fulmen")) {
                magnaFulmen = weapon;
                if (!weapon.isDisabled() && !weapon.isPermanentlyDisabled() && !weapon.isFiring()) {
                    magnaFulmenGroup = ship.getWeaponGroupFor(weapon);
                }
                break;
            }
        }
        /* Don't vent in the middle of an enhanced firing cycle! */
        if ((armed > 0) && (magnaFulmen != null) && magnaFulmen.isFiring()) {
            flags.setFlag(AIFlags.DO_NOT_VENT, 1f);
        }

        tracker.advance(amount);
        if (tracker.intervalElapsed()) {
            float FIRING_LINE_ARC = FIRING_LINE_ARC_STANDARD;
            float MAGNA_FULMEN_RANGE = MAGNA_FULMEN_RANGE_STANDARD;
            float MAGNA_FULMEN_EXTRA_RANGE = MAGNA_FULMEN_EXTRA_RANGE_STANDARD;
            float TARGET_DESIRE = TARGET_DESIRE_STANDARD;
            if (ship.getVariant().hasHullMod(II_BasePackage.ARMOR_PACKAGE)) {
                FIRING_LINE_ARC = FIRING_LINE_ARC_ARMOR;
                TARGET_DESIRE = TARGET_DESIRE_ARMOR;
            } else if (ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
                MAGNA_FULMEN_EXTRA_RANGE = MAGNA_FULMEN_EXTRA_RANGE_ELITE;
            }

            boolean allyInFiringLine = false;
            boolean enemyInFiringLine = false;
            boolean otherTargetsInFiringLine = false;
            Vector2f aimPoint = null;
            boolean pointingAtRetreatingEnemy = false;
            float magnaFulmenRange = ship.getMutableStats().getEnergyWeaponRangeBonus().computeEffective(MAGNA_FULMEN_RANGE);
            if (magnaFulmen != null) {
                magnaFulmenRange = magnaFulmen.getRange();
                float magnaFulmenFarRange = magnaFulmenRange + (MAGNA_FULMEN_EXTRA_RANGE * (magnaFulmen.getProjectileSpeed() / BASE_PROJECTILE_SPEED));

                float distanceThreshold = Float.MAX_VALUE;
                float distanceThresholdNear = Float.MAX_VALUE;
                float otherTargetWeight = 0f;
                List<ShipAPI> directTargets = CombatUtils.getShipsWithinRange(ship.getLocation(), magnaFulmenFarRange);
                if (!directTargets.isEmpty()) {
                    Vector2f endpoint = new Vector2f(magnaFulmenFarRange, 0f);
                    VectorUtils.rotate(endpoint, magnaFulmen.getCurrAngle(), endpoint);
                    Vector2f.add(endpoint, ship.getLocation(), endpoint);

                    Collections.sort(directTargets, new CollectionUtils.SortEntitiesByDistance(magnaFulmen.getLocation(), true));
                    for (ShipAPI tmp : directTargets) {
                        if ((tmp != ship) && (tmp.getCollisionClass() != CollisionClass.NONE) && !tmp.isFighter() && !tmp.isDrone()
                                && (tmp.getLocation() != null) && (ship.getLocation() != null)) {
                            float distanceToTmp = MathUtils.getDistance(magnaFulmen.getLocation(), tmp.getLocation());

                            float visibleArc = (float) Math.abs(Math.toDegrees(Math.atan(tmp.getCollisionRadius() / Math.max(1f, distanceToTmp))));
                            if (Misc.isInArc(magnaFulmen.getCurrAngle(), FIRING_LINE_ARC + (2f * visibleArc),
                                    VectorUtils.getAngle(magnaFulmen.getLocation(), tmp.getLocation()))) {
                                if ((tmp.getOwner() == ship.getOwner()) && tmp.isAlive()) {
                                    allyInFiringLine = true;
                                    distanceThreshold = Math.min(distanceThreshold, distanceToTmp + tmp.getCollisionRadius());
                                } else if ((tmp.getOwner() != ship.getOwner()) && tmp.isAlive() && (tmp.getOwner() != 100) && (distanceToTmp <= magnaFulmenRange)) {
                                    if (distanceToTmp <= distanceThreshold) {
                                        pointingAtRetreatingEnemy = false;
                                        if ((tmp.getOwner() == 0) || (tmp.getOwner() == 1)) {
                                            AssignmentInfo assignment = engine.getFleetManager(tmp.getOwner()).getTaskManager(tmp.isAlly()).getAssignmentFor(tmp);
                                            if ((assignment != null) && (assignment.getType() == CombatAssignmentType.RETREAT) && (assignment.getType() == CombatAssignmentType.REPAIR_AND_REFIT)) {
                                                pointingAtRetreatingEnemy = true;
                                            }
                                        }

                                        if (!pointingAtRetreatingEnemy || !ship.getVariant().hasHullMod(II_BasePackage.TARGETING_PACKAGE)) {
                                            enemyInFiringLine = true;
                                        }
                                        distanceThreshold = Math.min(distanceThreshold, distanceToTmp + tmp.getCollisionRadius());
                                    }
                                }
                            }

                            /* Failsafe */
                            if ((Math.abs(magnaFulmen.distanceFromArc(tmp.getLocation())) <= ((FIRING_LINE_ARC / 2f) + visibleArc))
                                    && (tmp.getOwner() == ship.getOwner()) && tmp.isAlive()) {
                                allyInFiringLine = true;
                                distanceThreshold = Math.min(distanceThreshold, distanceToTmp + tmp.getCollisionRadius());
                            }

                            Vector2f collisionPoint = CollisionUtils.getCollisionPoint(ship.getLocation(), endpoint, tmp);
                            if (collisionPoint != null) {
                                if (aimPoint == null) {
                                    aimPoint = collisionPoint;
                                }
                                distanceThreshold = Math.min(distanceThreshold, distanceToTmp + tmp.getCollisionRadius());
                                distanceThresholdNear = Math.min(distanceThreshold, distanceToTmp - tmp.getCollisionRadius());
                            }
                        }

                        if (ship.getVariant().hasHullMod(II_BasePackage.ARMOR_PACKAGE)) {
                            if ((tmp != ship) && (tmp.getCollisionClass() != CollisionClass.NONE)
                                    && (tmp.getLocation() != null) && (ship.getLocation() != null)) {
                                float distanceToTmp = MathUtils.getDistance(magnaFulmen.getLocation(), tmp.getLocation());
                                if (distanceToTmp <= distanceThresholdNear) {
                                    float visibleArc = (float) Math.abs(Math.toDegrees(Math.atan(tmp.getCollisionRadius() / Math.max(1f, distanceToTmp))));
                                    if (Misc.isInArc(magnaFulmen.getCurrAngle(), (FIRING_LINE_ARC_ARMOR_ACTUAL) + (2f * visibleArc),
                                            VectorUtils.getAngle(magnaFulmen.getLocation(), tmp.getLocation()))) {
                                        otherTargetWeight += 1f;
                                    }
                                }
                            }
                        }
                    }
                }

                if (ship.getVariant().hasHullMod(II_BasePackage.ARMOR_PACKAGE)) {
                    List<MissileAPI> missileTargets = CombatUtils.getMissilesWithinRange(ship.getLocation(), magnaFulmenRange);
                    if (!missileTargets.isEmpty()) {
                        for (MissileAPI tmp : missileTargets) {
                            if ((tmp != ship) && (tmp.getCollisionClass() != CollisionClass.NONE) && !tmp.isFlare()
                                    && (tmp.getLocation() != null) && (ship.getLocation() != null)) {
                                /* Stop checking if we already found something nearer */
                                float distanceToTmp = MathUtils.getDistance(magnaFulmen.getLocation(), tmp.getLocation());
                                if (distanceToTmp > distanceThresholdNear) {
                                    break;
                                }

                                float visibleArc = (float) Math.abs(Math.toDegrees(Math.atan(tmp.getCollisionRadius() / Math.max(1f, distanceToTmp))));
                                if (Misc.isInArc(magnaFulmen.getCurrAngle(), FIRING_LINE_ARC_ARMOR_ACTUAL + (2f * visibleArc),
                                        VectorUtils.getAngle(magnaFulmen.getLocation(), tmp.getLocation()))) {
                                    float damageWeight = tmp.getDamageAmount() / 750f;
                                    if (tmp.getDamageType() == DamageType.FRAGMENTATION) {
                                        damageWeight *= 0.25f;
                                    }
                                    damageWeight += tmp.getEmpAmount() / 1500f;
                                    otherTargetWeight += Math.min(1f, damageWeight);
                                }
                            }
                        }
                    }
                }

                if (otherTargetWeight >= OTHER_TARGET_WEIGHT_THRESHOLD) {
                    otherTargetsInFiringLine = true;
                }
            }

            allyInFiringLineShow = allyInFiringLine;
            enemyInFiringLineShow = enemyInFiringLine;
            otherTargetsInFiringLineShow = otherTargetsInFiringLine;
            aimPointShow = aimPoint;

            /* Force-fire! */
            if ((magnaFulmenGroup != null) && (magnaFulmen != null) && (armed > 0) && !allyInFiringLine && ((aimPoint != null) || otherTargetsInFiringLine)
                    && !stillWantsToUseSystem && (!pointingAtRetreatingEnemy || !ship.getVariant().hasHullMod(II_BasePackage.TARGETING_PACKAGE))) {
                int groupNum = 0;
                boolean foundGroup = false;
                for (WeaponGroupAPI group : ship.getWeaponGroupsCopy()) {
                    if (group == magnaFulmenGroup) {
                        foundGroup = true;
                        break;
                    } else {
                        groupNum++;
                    }
                }
                if (foundGroup) {
                    if (ship.getSelectedGroupAPI() != magnaFulmenGroup) {
                        ship.giveCommand(ShipCommand.SELECT_GROUP, null, groupNum);
                    }
                    if (ship.getSelectedGroupAPI() == magnaFulmenGroup) {
                        ship.giveCommand(ShipCommand.FIRE, ship.getMouseTarget(), groupNum);
                    }
                }
            }

            if (ship.getFluxTracker().isOverloadedOrVenting() || !AIUtils.canUseSystemThisFrame(ship)) {
                if (ship.getFluxTracker().isOverloadedOrVenting()) {
                    stillWantsToUseSystem = false;
                }

                desireShow = 0f;
                if (DEBUG) {
                    displayDebug();
                }
                return;
            }

            float potential = II_ArbalestLoaderStats.getPotential(ship);
            if (potential <= 0f) {
                stillWantsToUseSystem = false;

                if (DEBUG) {
                    desireShow = 0f;
                    displayDebug();
                }
                return;
            }

            float fluxRemaining = ship.getFluxTracker().getMaxFlux() - ship.getFluxTracker().getCurrFlux();
            float totalFluxCost = system.getFluxPerUse();
            for (WeaponAPI weapon : ship.getUsableWeapons()) {
                if (weapon.getId().contentEquals("ii_magna_fulmen")) {
                    if (!weapon.isDisabled() && !weapon.isPermanentlyDisabled()) {
                        totalFluxCost += weapon.getFluxCostToFire();
                    }
                    break;
                }
            }

            boolean panic = false;
            if (ship.getHullLevel() <= (1f / 3f)) {
                panic = true;
            }
            if (ship.getCurrentCR() <= 0.2f) {
                panic = true;
            }

            float engageRange = 1000f;
            for (WeaponAPI weapon : ship.getUsableWeapons()) {
                if (weapon.getType() == WeaponAPI.WeaponType.MISSILE) {
                    continue;
                }
                if (weapon.getRange() > engageRange) {
                    engageRange = weapon.getRange();
                }
            }

            boolean activeTimeAlmostGone = false;
            if ((armed > 0) && (II_ArbalestLoaderStats.getActiveTimeLeft(ship) <= 1f)) {
                activeTimeAlmostGone = true;
            }

            AssignmentInfo assignment = engine.getFleetManager(ship.getOwner()).getTaskManager(ship.isAlly()).getAssignmentFor(ship);
            Vector2f targetSpot;
            if ((assignment != null) && (assignment.getTarget() != null) && (assignment.getType() != CombatAssignmentType.AVOID)) {
                targetSpot = assignment.getTarget().getLocation();
            } else {
                targetSpot = null;
            }
            CombatEntityAPI immediateTarget;
            if (flags.getCustom(AIFlags.MANEUVER_TARGET) instanceof CombatEntityAPI) {
                immediateTarget = (CombatEntityAPI) flags.getCustom(AIFlags.MANEUVER_TARGET);
            } else {
                immediateTarget = ship.getShipTarget();
            }

            float desiredRange = 500f;
            if ((assignment != null)
                    && ((assignment.getType() == CombatAssignmentType.ENGAGE)
                    || (assignment.getType() == CombatAssignmentType.HARASS)
                    || (assignment.getType() == CombatAssignmentType.INTERCEPT)
                    || (assignment.getType() == CombatAssignmentType.LIGHT_ESCORT)
                    || (assignment.getType() == CombatAssignmentType.MEDIUM_ESCORT)
                    || (assignment.getType() == CombatAssignmentType.HEAVY_ESCORT)
                    || (assignment.getType() == CombatAssignmentType.STRIKE))) {
                desiredRange = engageRange;
            }

            boolean immediateTargetInRange = false;
            if ((immediateTarget != null) && (MathUtils.getDistance(immediateTarget, ship) < (engageRange - ship.getCollisionRadius()))) {
                immediateTargetInRange = true;
            }

            float desire = potential;

            /* Don't ruin the 0 flux boost! */
            if (flags.hasFlag(AIFlags.RUN_QUICKLY)) {
                if (ship.getFluxTracker().isEngineBoostActive()) {
                    desire *= 0.25f;
                }
            }
            if ((assignment != null) && (assignment.getType() == CombatAssignmentType.RETREAT)) {
                if (ship.getFluxTracker().isEngineBoostActive()) {
                    desire *= 0.25f;
                }
            }
            if ((immediateTarget != null) && !immediateTargetInRange) {
                if (ship.getFluxTracker().isEngineBoostActive()) {
                    desire *= 0.25f;
                }
            }
            if ((targetSpot != null) && (MathUtils.getDistance(targetSpot, ship.getLocation()) >= desiredRange) && !immediateTargetInRange) {
                if (ship.getFluxTracker().isEngineBoostActive()) {
                    desire *= 0.25f;
                }
            }

            if (flags.hasFlag(AIFlags.PURSUING) || flags.hasFlag(AIFlags.DO_NOT_BACK_OFF)) {
                desire *= 1.25f;
            }
            if (flags.hasFlag(AIFlags.BACKING_OFF) || flags.hasFlag(AIFlags.DO_NOT_PURSUE)) {
                desire *= 0.75f;
            }

            if ((immediateTarget == null) || (MathUtils.getDistance(immediateTarget, ship) > magnaFulmenRange)) {
                if (!activeTimeAlmostGone) {
                    desire *= 0.5f;
                } else {
                    desire *= 1.25f;
                }
            }

            if (!flags.hasFlag(AIFlags.SAFE_VENT)) {
                if (panic) {
                    desire *= 2f;
                }

                if (flags.hasFlag(AIFlags.DO_NOT_USE_FLUX)) {
                    desire *= 0.5f;
                }
            }

            if (allyInFiringLine) {
                desire *= 0.5f;
            }
            if (!enemyInFiringLine) {
                if (otherTargetsInFiringLine) {
                    desire *= 0.75f;
                } else {
                    desire *= 0.5f;
                }
            }
            if (((aimPoint == null) || !enemyInFiringLine) && !otherTargetsInFiringLine) {
                desire *= 0.75f;
            }
            if (otherTargetsInFiringLine) {
                desire *= 1.5f;
            }
            if (pointingAtRetreatingEnemy && ship.getVariant().hasHullMod(II_BasePackage.TARGETING_PACKAGE)) {
                if (enemyInFiringLine) {
                    desire *= 0.75f;
                } else {
                    desire *= 0.25f;
                }
            }

            /* In a neutral situation, this is enough to make the AI use the system again just to keep the charge going */
            if (activeTimeAlmostGone) {
                desire *= 1.5f;
                totalFluxCost = system.getFluxPerUse();
            }

            desire *= fluxRemaining / Math.max(1f, fluxRemaining + totalFluxCost);

            desireShow = desire;
            targetDesireShow = TARGET_DESIRE;

            if (desire >= TARGET_DESIRE) {
                ship.useSystem();
                if (ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
                    stillWantsToUseSystem = true;
                }
            } else {
                stillWantsToUseSystem = false;
            }
        }

        if (DEBUG) {
            displayDebug();
        }
    }

    private void displayDebug() {
        if (engine.getPlayerShip() == ship) {
            String targetingString = "";
            if (allyInFiringLineShow) {
                targetingString += " ALLY";
            }
            if (enemyInFiringLineShow) {
                targetingString += " ENEMY";
            }
            if (aimPointShow != null) {
                targetingString += " POINTING";
            }
            if (otherTargetsInFiringLineShow) {
                targetingString += " OTHER";
            }
            engine.maintainStatusForPlayerShip(STATUSKEY2, system.getSpecAPI().getIconSpriteName(),
                    "AI", "Targeting:" + targetingString, allyInFiringLineShow);
        }
        if (engine.getPlayerShip() == ship) {
            engine.maintainStatusForPlayerShip(STATUSKEY1, system.getSpecAPI().getIconSpriteName(),
                    "AI", "Desire: " + Math.round(100f * desireShow) + "/" + Math.round(100f * targetDesireShow), desireShow < targetDesireShow);
        }
    }

    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.flags = flags;
        this.system = system;
        this.engine = engine;
    }
}
