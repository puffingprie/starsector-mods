package data.scripts.shipsystems.ai;

import com.fs.starfarer.api.combat.CombatAssignmentType;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatFleetManagerAPI.AssignmentInfo;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.AIHints;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.hullmods.II_BasePackage;
import java.util.List;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class II_TurbofeederAI implements ShipSystemAIScript {

    private static final float TARGET_DESIRE = 1f;
    private static final float TARGET_DESIRE_ELITE = 1f;

    private CombatEngineAPI engine;
    private ShipwideAIFlags flags;
    private ShipAPI ship;
    private ShipSystemAPI system;

    private final IntervalUtil tracker = new IntervalUtil(0.1f, 0.2f);

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
        if (engine == null) {
            return;
        }

        if (engine.isPaused()) {
            return;
        }

        tracker.advance(amount);

        if (tracker.intervalElapsed()) {
            if (ship.getFluxTracker().isOverloadedOrVenting() || system.isActive() || system.isCoolingDown()) {
                return;
            }

            boolean highFlux = false;
            boolean veryHighFlux = false;
            boolean ultraHighFlux = false;
            if (ship.getFluxLevel() >= 0.7f) {
                highFlux = true;
            }
            if (ship.getHardFluxLevel() >= 0.5f) {
                if (highFlux) {
                    veryHighFlux = true;
                } else {
                    highFlux = true;
                }
            }
            if (ship.getFluxLevel() >= 0.9f) {
                veryHighFlux = true;
            }
            if (ship.getHardFluxLevel() >= 0.7f) {
                if (veryHighFlux) {
                    ultraHighFlux = true;
                } else {
                    veryHighFlux = true;
                }
            }
            if (ship.getHardFluxLevel() >= 0.9f) {
                ultraHighFlux = true;
            }

            float engageRange = 1000f;
            float minWeaponRange = engageRange;
            for (WeaponAPI weapon : ship.getUsableWeapons()) {
                if (weapon.getType() == WeaponAPI.WeaponType.MISSILE) {
                    continue;
                }
                minWeaponRange = Math.min(minWeaponRange, weapon.getRange());
                if (weapon.getRange() > engageRange) {
                    engageRange = weapon.getRange();
                }
            }

            ShipAPI immediateShipTarget;
            float immediateTargetDistance = Float.MAX_VALUE;
            if (flags.getCustom(AIFlags.MANEUVER_TARGET) instanceof ShipAPI) {
                immediateShipTarget = (ShipAPI) flags.getCustom(AIFlags.MANEUVER_TARGET);
            } else {
                immediateShipTarget = ship.getShipTarget();
            }
            if (immediateShipTarget != null) {
                immediateTargetDistance = MathUtils.getDistance(immediateShipTarget, ship);
            }

            List<ShipAPI> nearbyEnemies = AIUtils.getNearbyEnemies(ship, engageRange);
            float nearestEnemyDistance = immediateTargetDistance;
            for (ShipAPI enemy : nearbyEnemies) {
                if (enemy.isFighter() || enemy.isDrone() || enemy.isShuttlePod()) {
                    continue;
                }
                nearestEnemyDistance = Math.min(nearestEnemyDistance, MathUtils.getDistance(enemy, ship));
            }

            float turbofeederEligibleRange = minWeaponRange;
            float turbofeederOptimalRange = 0f;
            float totalTurbofeederEligibleOP = 0f;
            float turbofeederOPThatWouldDirectlyBenefit = 0f;
            float turbofeederOPThatWouldIndirectlyBenefit = 0f;
            float directBenefitFrac = 0f;
            float indirectBenefitFrac = 0f;
            for (WeaponAPI weapon : ship.getUsableWeapons()) {
                if (weapon.getType() == WeaponAPI.WeaponType.MISSILE) {
                    continue;
                }
                if (weapon.isBeam() && !weapon.isBurstBeam()) {
                    continue;
                }
                if (weapon.hasAIHint(AIHints.PD) && !weapon.hasAIHint(AIHints.PD_ALSO)) {
                    continue;
                }
                float range = weapon.getRange();
                if (range > turbofeederEligibleRange) {
                    turbofeederEligibleRange = range;
                }
                float opCost = Math.max(weapon.getSpec().getOrdnancePointCost(null), 1f);
                totalTurbofeederEligibleOP += opCost;
                turbofeederOptimalRange += range * opCost;

                if ((weapon.isDisabled() && !ship.getVariant().hasHullMod(II_BasePackage.ARMOR_PACKAGE)) || weapon.isPermanentlyDisabled()) {
                    continue;
                }
                if ((weapon.getRange() - (ship.getCollisionRadius() * 0.5f)) < nearestEnemyDistance) {
                    continue;
                }
                turbofeederOPThatWouldIndirectlyBenefit += opCost;

                if (immediateShipTarget == null) {
                    continue;
                }
                if ((weapon.getRange() - (ship.getCollisionRadius() * 0.5f)) < immediateTargetDistance) {
                    continue;
                }
                float arcDist = weapon.distanceFromArc(immediateShipTarget.getLocation());
                float targetPerspectiveHalfArc = (float) Math.toDegrees(Math.atan(immediateShipTarget.getCollisionRadius() / (immediateTargetDistance + ship.getCollisionRadius())));
                if (Math.abs(arcDist) > Math.abs(targetPerspectiveHalfArc)) {
                    continue;
                }
                turbofeederOPThatWouldDirectlyBenefit += opCost;
                turbofeederOPThatWouldIndirectlyBenefit -= opCost;
            }
            if (totalTurbofeederEligibleOP >= 1f) {
                turbofeederOptimalRange /= totalTurbofeederEligibleOP;
                directBenefitFrac = turbofeederOPThatWouldDirectlyBenefit / totalTurbofeederEligibleOP;
                indirectBenefitFrac = turbofeederOPThatWouldIndirectlyBenefit / totalTurbofeederEligibleOP;
            } else {
                turbofeederOptimalRange = minWeaponRange;
            }

            boolean immediateTargetInRange = false;
            if (immediateTargetDistance < (engageRange - ship.getCollisionRadius())) {
                immediateTargetInRange = true;
            }

            boolean immediateTargetAtOptimalEngageRange = false;
            if (immediateTargetInRange && (immediateTargetDistance >= ((engageRange * (2f / 3f)) - ship.getCollisionRadius()))) {
                immediateTargetAtOptimalEngageRange = true;
            }

            boolean immediateTargetWithinEligibleRange = false;
            if (immediateTargetInRange && (immediateTargetDistance < (turbofeederEligibleRange - ship.getCollisionRadius()))) {
                immediateTargetWithinEligibleRange = true;
            }

            boolean immediateTargetWithinOptimalRange = false;
            if (immediateTargetInRange && (immediateTargetDistance < (turbofeederOptimalRange - ship.getCollisionRadius()))) {
                immediateTargetWithinOptimalRange = true;
            }

            boolean wantsToRetreat = false;
            if (flags.hasFlag(AIFlags.BACKING_OFF) || flags.hasFlag(AIFlags.RUN_QUICKLY)) {
                wantsToRetreat = true;
            }

            boolean wantsToStandOff = false;
            if (!wantsToRetreat || flags.hasFlag(AIFlags.STANDING_OFF_VS_SHIP_ON_MAP_BORDER) || flags.hasFlag(AIFlags.MAINTAINING_STRIKE_RANGE)) {
                wantsToStandOff = true;
                wantsToRetreat = false;
            }

            boolean wantsToCloseDistance = false;
            if (flags.hasFlag(AIFlags.HARASS_MOVE_IN) || flags.hasFlag(AIFlags.PURSUING) || (!wantsToStandOff && !wantsToRetreat && immediateTargetWithinOptimalRange)) {
                wantsToCloseDistance = true;
                wantsToStandOff = false;
                //wantsToRetreat = false;
            }

            float desire = 0f;
            if (wantsToStandOff && immediateTargetWithinEligibleRange) {
                if (immediateTargetWithinOptimalRange) {
                    if (ultraHighFlux) {
                        desire += 0.75f;
                    } else if (veryHighFlux) {
                        desire += 1f;
                    } else if (highFlux) {
                        desire += 1.25f;
                    } else {
                        desire += 1f;
                    }
                } else {
                    if (ultraHighFlux) {
                        desire += 0.5f;
                    } else if (veryHighFlux) {
                        desire += 0.75f;
                    } else if (highFlux) {
                        desire += 1f;
                    } else {
                        desire += 0.75f;
                    }
                }
                if (immediateTargetAtOptimalEngageRange && ship.getVariant().hasHullMod(II_BasePackage.TARGETING_PACKAGE)) {
                    desire += 0.5f;
                }
            }

            if (wantsToCloseDistance && immediateTargetWithinEligibleRange) {
                if (immediateTargetWithinOptimalRange) {
                    if (ultraHighFlux) {
                        desire += 1f;
                    } else if (veryHighFlux) {
                        desire += 1.25f;
                    } else if (highFlux) {
                        desire += 1.5f;
                    } else {
                        desire += 1.25f;
                    }
                } else {
                    if (ultraHighFlux) {
                        desire += 0.75f;
                    } else if (veryHighFlux) {
                        desire += 1f;
                    } else if (highFlux) {
                        desire += 1.25f;
                    } else {
                        desire += 1f;
                    }
                }
                if (ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
                    desire += 0.5f;
                }
            }

            desire *= directBenefitFrac;
            if (ultraHighFlux) {
                desire += 0.5f * indirectBenefitFrac;
            } else if (veryHighFlux) {
                desire += 0.75f * indirectBenefitFrac;
            } else if (highFlux) {
                desire += 1f * indirectBenefitFrac;
            } else {
                desire += 1.25f * indirectBenefitFrac;
            }

            if (ship.getVariant().hasHullMod(II_BasePackage.ARMOR_PACKAGE)) {
                float damagedWeaponOP = 0f;
                float workingWeaponOP = 0f;
                for (WeaponAPI weapon : ship.getAllWeapons()) {
                    if (weapon.getSpec() == null) {
                        continue;
                    }

                    if (weapon.isDisabled()) {
                        damagedWeaponOP += weapon.getSpec().getOrdnancePointCost(null);
                    } else if (!weapon.isPermanentlyDisabled()) {
                        workingWeaponOP += weapon.getSpec().getOrdnancePointCost(null);
                    }
                }

                float disabledFrac;
                if ((workingWeaponOP + damagedWeaponOP) >= 1f) {
                    disabledFrac = damagedWeaponOP / (workingWeaponOP + damagedWeaponOP);
                } else {
                    disabledFrac = 0f;
                }

                /* We *really* want our guns to fire */
                desire += disabledFrac * 2f;
            }

            /* Drive me closer so I can hit them with my sword! */
            if (ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
                AssignmentInfo assignment = engine.getFleetManager(ship.getOwner()).getTaskManager(ship.isAlly()).getAssignmentFor(ship);
                Vector2f targetSpot;
                if ((assignment != null) && (assignment.getTarget() != null) && (assignment.getType() != CombatAssignmentType.AVOID)) {
                    targetSpot = assignment.getTarget().getLocation();
                } else {
                    targetSpot = null;
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
                if ((targetSpot != null) && (MathUtils.getDistance(targetSpot, ship.getLocation()) >= desiredRange) && !immediateTargetInRange) {
                    if ((immediateShipTarget != null) && (MathUtils.getDistance(immediateShipTarget, targetSpot) <= engageRange)) {
                        desire += 1.5f;
                    } else {
                        desire += 1.25f;
                    }
                }
            }

            if (flags.hasFlag(AIFlags.IN_CRITICAL_DPS_DANGER)) {
                desire += 0.125f;
                if (ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
                    desire += 0.125f;
                }
            }

            if (flags.hasFlag(AIFlags.NEEDS_HELP)) {
                desire += 0.125f;
            }

            if (flags.hasFlag(AIFlags.HAS_INCOMING_DAMAGE)) {
                if (ship.getVariant().hasHullMod(II_BasePackage.ARMOR_PACKAGE)) {
                    desire += 0.25f;
                }
                if (ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
                    desire += 0.125f;
                }
            }

            if ((flags.hasFlag(AIFlags.RUN_QUICKLY) || flags.hasFlag(AIFlags.BACKING_OFF)) && ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
                if (ultraHighFlux) {
                    desire += 1f;
                } else if (veryHighFlux) {
                    desire += 0.75f;
                } else if (highFlux) {
                    desire += 0.5f;
                } else {
                    desire += 0.25f;
                }
                if (flags.hasFlag(AIFlags.RUN_QUICKLY)) {
                    desire += 0.25f;
                }
            } else if (flags.hasFlag(AIFlags.DO_NOT_USE_FLUX) || flags.hasFlag(AIFlags.DO_NOT_AUTOFIRE_NON_ESSENTIAL_GROUPS)) {
                desire -= 0.5f;
            }

            if (flags.hasFlag(AIFlags.TURN_QUICKLY) && ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
                desire += 0.125f;
            }

            float targetDesire;
            if (ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
                targetDesire = TARGET_DESIRE_ELITE;
            } else {
                targetDesire = TARGET_DESIRE;
            }

            if ((int) Math.round(desire * 100f) >= (int) Math.round(targetDesire * 100f)) {
                ship.useSystem();
            }
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
