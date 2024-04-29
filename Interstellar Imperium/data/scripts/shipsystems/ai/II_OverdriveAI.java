package data.scripts.shipsystems.ai;

import com.fs.starfarer.api.combat.CombatAssignmentType;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.CombatFleetManagerAPI.AssignmentInfo;
import com.fs.starfarer.api.combat.ShipAIConfig;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.AIHints;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import data.scripts.hullmods.II_BasePackage;
import data.scripts.shipsystems.II_OverdriveStats;
import data.scripts.util.II_Util;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

public class II_OverdriveAI implements ShipSystemAIScript {

    private static final float HYSTERESIS_TIME_ACTIVE = 2f;
    private static final float HYSTERESIS_TIME = 4f;
    private static final float RE_EVAL_TIME = 1f;

    private CombatEngineAPI engine;
    private ShipwideAIFlags flags;
    private ShipAPI ship;
    private ShipSystemAPI system;
    private float maxDesire = 0f;

    private static final boolean DEBUG = false;
    private final Object STATUSKEY1 = new Object();
    private final Object STATUSKEY2 = new Object();
    private final Object STATUSKEY3 = new Object();
    private final Object STATUSKEY4 = new Object();
    private final Object STATUSKEY5 = new Object();
    private float desireShow = 0f;
    private float unfilteredDesire = 0f;
    private float targetDesireShow = 0f;
    private float reEvalTimer = 0f;
    private boolean forceTimidAI = false;
    private boolean forceAggressiveAI = false;
    private final ShipAIConfig savedConfig = new ShipAIConfig();

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
        if (engine == null) {
            return;
        }

        if (engine.isPaused()) {
            if (DEBUG) {
                if (engine.getPlayerShip() == ship) {
                    engine.maintainStatusForPlayerShip(STATUSKEY1, system.getSpecAPI().getIconSpriteName(),
                            "AI", "Desire: " + Math.round(100f * desireShow) + "/" + Math.round(100f * targetDesireShow), desireShow < targetDesireShow);
                    engine.maintainStatusForPlayerShip(STATUSKEY2, system.getSpecAPI().getIconSpriteName(),
                            "AI", "Desire (no filt): " + Math.round(100f * unfilteredDesire), desireShow < targetDesireShow);
                    engine.maintainStatusForPlayerShip(STATUSKEY3, system.getSpecAPI().getIconSpriteName(),
                            "AI", system.isActive() ? "Active" : "Inactive", !system.isActive());
                    engine.maintainStatusForPlayerShip(STATUSKEY4, system.getSpecAPI().getIconSpriteName(),
                            "AI", II_OverdriveStats.isUsable(ship, system) ? "Usable" : "Unusable", !II_OverdriveStats.isUsable(ship, system));
                    engine.maintainStatusForPlayerShip(STATUSKEY5, system.getSpecAPI().getIconSpriteName(),
                            "AI", ship.getFluxTracker().isOverloadedOrVenting() ? "Overload/vent" : "Normal", ship.getFluxTracker().isOverloadedOrVenting());
                }
            }
            return;
        }

        float desire = 0f;

        float gauge = II_OverdriveStats.getGauge(ship);
        float overLevel = 1f; // 1f to 2f (standard/targeting) or 3f (elite)
        boolean overloadDanger = false;
        if (!ship.getVariant().hasHullMod(II_BasePackage.ARMOR_PACKAGE)) {
            overLevel = II_OverdriveStats.getOverlevel(ship);

            /* Shift values lower so we don't wiggle at a particular value */
            float gaugeTimeLeft = gauge * II_OverdriveStats.GAUGE_DRAIN_TIME;
            if (system.isActive()) {
                if (gaugeTimeLeft < 0.25f) {
                    overloadDanger = true;
                }
            } else {
                if (gaugeTimeLeft < 0.75f) {
                    overloadDanger = true;
                }
            }
        }

        boolean lowGauge;
        boolean highGauge;
        if (system.isActive()) {
            /* Shift values lower so we don't wiggle at a particular value */
            lowGauge = gauge < 0.25f;
            highGauge = gauge >= 0.5f;
        } else {
            lowGauge = gauge < 0.5f;
            highGauge = gauge >= 0.75f;
        }

        boolean highFlux = false;
        boolean veryHighFlux = false;
        boolean ultraHighFlux = false;
        float trueFluxLevel = ship.getFluxLevel();
        float trueHardFluxLevel = ship.getHardFluxLevel();
        if (system.isActive()) {
            trueFluxLevel *= II_Util.lerp(1f, II_OverdriveStats.CAPACITY_MULT, system.getEffectLevel() * overLevel);
            trueHardFluxLevel *= II_Util.lerp(1f, II_OverdriveStats.CAPACITY_MULT, system.getEffectLevel() * overLevel);
        }
        if (trueFluxLevel >= 0.7f) {
            highFlux = true;
        }
        if (trueHardFluxLevel >= 0.5f) {
            if (highFlux) {
                veryHighFlux = true;
            } else {
                highFlux = true;
            }
        }
        if (trueFluxLevel >= 0.9f) {
            veryHighFlux = true;
        }
        if (trueHardFluxLevel >= 0.7f) {
            if (veryHighFlux) {
                ultraHighFlux = true;
            } else {
                veryHighFlux = true;
            }
        }
        if (trueFluxLevel >= 1.1f) {
            ultraHighFlux = true;
        }
        if (trueHardFluxLevel >= 0.9f) {
            ultraHighFlux = true;
        }

        boolean panic = false;
        if (ship.getHullLevel() <= (1f / 3f)) {
            panic = true;
        }
        if (ship.getCurrentCR() <= 0.2f) {
            panic = true;
        }

        float engageRange = 1000f;
        float minWeaponRange = engageRange;
        for (WeaponAPI weapon : ship.getAllWeapons()) {
            if (weapon.isPermanentlyDisabled()) {
                continue;
            }
            if (weapon.getType() == WeaponType.MISSILE) {
                continue;
            }
            minWeaponRange = Math.min(minWeaponRange, weapon.getRange());
            if (weapon.getRange() > engageRange) {
                engageRange = weapon.getRange();
            }
        }
        float optimalRange = 0f;
        float eligibleOP = 0f;
        for (WeaponAPI weapon : ship.getAllWeapons()) {
            if (weapon.isPermanentlyDisabled()) {
                continue;
            }
            if (weapon.getType() == WeaponType.MISSILE) {
                continue;
            }
            if (weapon.hasAIHint(AIHints.PD) && !weapon.hasAIHint(AIHints.PD_ALSO)) {
                continue;
            }
            float opCost = Math.max(weapon.getSpec().getOrdnancePointCost(null), 1f);
            eligibleOP += opCost;
            optimalRange += weapon.getRange() * opCost;
        }
        if (eligibleOP >= 1f) {
            optimalRange /= eligibleOP;
        } else {
            optimalRange = minWeaponRange;
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
        ShipAPI immediateShipTarget = null;
        if (immediateTarget instanceof ShipAPI) {
            immediateShipTarget = (ShipAPI) immediateTarget;
        }
        boolean immediateTargetNearDangerousDeath = false;
        if ((immediateShipTarget != null) && !immediateShipTarget.isFighter() && !immediateShipTarget.isFrigate() && (immediateShipTarget.getHullLevel() <= 0.2f)) {
            immediateTargetNearDangerousDeath = true;
        }
        float tooCloseRange;
        if (immediateTargetNearDangerousDeath) {
            tooCloseRange = (optimalRange / 2f) + ship.getCollisionRadius();
        } else {
            tooCloseRange = (optimalRange / 3f) + ship.getCollisionRadius();
        }
        boolean immediateTargetTooClose = false;
        if ((immediateShipTarget != null) && !immediateShipTarget.isFighter() && (MathUtils.getDistance(immediateShipTarget, ship) < (tooCloseRange - ship.getCollisionRadius()))) {
            immediateTargetTooClose = true;
        }
        boolean immediateTargetTooFar = false;
        if ((immediateShipTarget != null) && !immediateShipTarget.isFighter() && (MathUtils.getDistance(immediateShipTarget, ship) >= (engageRange - ship.getCollisionRadius()))) {
            immediateTargetTooFar = true;
        }
        float goodRange;
        if (immediateTargetNearDangerousDeath) {
            goodRange = (optimalRange / 1.25f) + ship.getCollisionRadius();
        } else {
            goodRange = (optimalRange / 1.5f) + ship.getCollisionRadius();
        }
        boolean immediateTargetAtGoodRange = false;
        if (!immediateTargetTooFar && (immediateShipTarget != null) && !immediateShipTarget.isFighter() && (MathUtils.getDistance(immediateShipTarget, ship) >= (goodRange - ship.getCollisionRadius()))) {
            immediateTargetAtGoodRange = true;
        }
        boolean smallCraftTarget = false;
        if ((immediateShipTarget != null) && immediateShipTarget.isFighter()) {
            smallCraftTarget = true;
        }
        boolean collisionDanger = false;
        if (immediateTargetNearDangerousDeath && (immediateShipTarget != null) && (MathUtils.getDistance(immediateShipTarget, ship) < (optimalRange / 4f))) {
            collisionDanger = true;
        }

        /* If we need to GTFO, the system is necessary for survival - but less useful if we're running out of gauge */
        if (flags.hasFlag(AIFlags.RUN_QUICKLY)) {
            if (lowGauge && !panic) {
                if (ship.getVariant().hasHullMod(II_BasePackage.TARGETING_PACKAGE)) {
                    desire += 0.5f;
                } else {
                    desire += 0.75f;
                }
            } else {
                if (ship.getVariant().hasHullMod(II_BasePackage.TARGETING_PACKAGE)) {
                    desire += 0.75f;
                } else {
                    desire += 1.5f;
                }
            }
        } else if (flags.hasFlag(AIFlags.BACKING_OFF)) {
            if (lowGauge && !panic) {
                desire += 0.5f;
            } else {
                if (ship.getVariant().hasHullMod(II_BasePackage.TARGETING_PACKAGE)) {
                    desire += 0.5f;
                } else {
                    desire += 1f;
                }
            }
        }

        /* If we want to stay put, less likely to need the system - especially if gauge is low */
        if (flags.hasFlag(AIFlags.DO_NOT_PURSUE)
                || (flags.hasFlag(AIFlags.DO_NOT_BACK_OFF) && !flags.hasFlag(AIFlags.PURSUING))) {
            if (lowGauge) {
                desire -= 0.5f;
            } else {
                desire -= 0.25f;
            }
        }

        /* If we're pursuing, more likely to need the system - unless gauge is low */
        if (flags.hasFlag(AIFlags.PURSUING)) {
            if (!lowGauge) {
                desire += 0.25f;
            }
        }

        /* If we're moving in, we probably need the system */
        if (flags.hasFlag(AIFlags.HARASS_MOVE_IN)) {
            if (ship.getVariant().hasHullMod(II_BasePackage.TARGETING_PACKAGE)) {
                desire += 0.25f;
            } else {
                desire += 0.5f;
            }
        }

        /* "Oh shit!" - dont overload us, though */
        if (flags.hasFlag(AIFlags.NEEDS_HELP)) {
            if (overloadDanger && !panic) {
                desire += 1f;
            } else {
                desire += 2f;
            }
        }

        /* If we need to tank some shit *right now*, this system can save our lives. Likewise, if we're outclassed,
               the system will come in handy -- but let's not waste it too soon, nor screw ourselves by overloading. */
        if (flags.hasFlag(AIFlags.HAS_INCOMING_DAMAGE) && flags.hasFlag(AIFlags.IN_CRITICAL_DPS_DANGER)) {
            if (veryHighFlux) {
                if (panic) {
                    desire += 2f;
                } else if (!overloadDanger) {
                    desire += 1.25f;
                }
            } else if (highFlux) {
                if (panic) {
                    desire += 1f;
                } else if (!overloadDanger) {
                    desire += 0.5f;
                }
            } else {
                if (panic) {
                    desire += 0.5f;
                } else if (!overloadDanger) {
                    desire += 0.25f;
                }
            }
        } else if (flags.hasFlag(AIFlags.HAS_INCOMING_DAMAGE) || flags.hasFlag(AIFlags.IN_CRITICAL_DPS_DANGER)) {
            if (veryHighFlux) {
                if (panic) {
                    desire += 1.25f;
                } else if (!overloadDanger) {
                    desire += 0.75f;
                }
            } else if (highFlux) {
                if (panic) {
                    desire += 0.5f;
                } else if (!overloadDanger) {
                    desire += 0.25f;
                }
            } else {
                if (panic) {
                    desire += 0.25f;
                }
            }
        }

        /* If we need to spin around quickly, we probably need the system (regardless of gauge; this can save us!) */
        if (!ship.getVariant().hasHullMod(II_BasePackage.TARGETING_PACKAGE)) {
            if (flags.hasFlag(AIFlags.TURN_QUICKLY)) {
                desire += 0.75f;
            }
        }

        boolean immediateTargetInRange = false;
        if ((immediateTarget != null) && (MathUtils.getDistance(immediateTarget, ship) < (engageRange - ship.getCollisionRadius()))) {
            immediateTargetInRange = true;
        }

        /* If our center of attention is too far away and we're ready to go, consider using the system */
        if (!ship.getVariant().hasHullMod(II_BasePackage.TARGETING_PACKAGE)) {
            if ((immediateTarget != null) && !immediateTargetInRange) {
                if (!highFlux && highGauge && !panic) {
                    desire += 0.5f;
                }
            }
        }

        /* If we're ordered to go after a specific target, that target is out of range, and we're ready to go, consider using the system */
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
            if (!ship.getVariant().hasHullMod(II_BasePackage.TARGETING_PACKAGE)) {
                if ((immediateTarget != null) && (MathUtils.getDistance(immediateTarget, targetSpot) <= engageRange)) {
                    if (!highFlux && highGauge && !panic) {
                        desire += 0.5f; // Adds to the other 0.5
                        if (system.isActive()) {
                            desire += 0.25f; // Discourage feathering
                        }
                    }
                } else if (immediateTarget != null) {
                    if (!highFlux && highGauge && !panic) {
                        desire += 0.25f; // Adds to the other 0.5
                        if (system.isActive()) {
                            desire += 0.25f; // Discourage feathering
                        }
                    }
                } else {
                    if (!highFlux && highGauge && !panic) {
                        desire += 0.75f;
                        if (system.isActive()) {
                            desire += 0.5f; // Discourage feathering
                        }
                    }
                }
            }
        } else if (flags.hasFlag(AIFlags.SAFE_VENT)) {
            /* If we're this safe and don't have anything better to do, don't bother using the system */
            desire -= 0.5f;
        }

        /* If ordered to retreat, make sure it gets done */
        if (!ship.getVariant().hasHullMod(II_BasePackage.TARGETING_PACKAGE)) {
            if ((assignment != null) && (assignment.getType() == CombatAssignmentType.RETREAT)) {
                if (!overloadDanger || panic) {
                    desire += 1.5f;
                }
            }
        }

        if (ship.getVariant().hasHullMod(II_BasePackage.TARGETING_PACKAGE)) {
            if (!flags.hasFlag(AIFlags.BACKING_OFF) && immediateTargetInRange) {
                if (!overloadDanger || panic) {
                    desire += 0.5f;
                }
            }
        }

        /* Generally want the system more at high flux.
               At very high flux, this system is a safety net.
               At ultra high flux, we can't really function without this system.
               Important detail: when the system is active and at low gauge, we really need to be careful not to let go
               of it, lest we get stuck with high flux! Except for Elite, which can activate while redlined. */
        if (ultraHighFlux) {
            desire *= 1.3f;
            if (system.isActive() && (overLevel > 1f) && !ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
                desire += 1.5f * ((float) Math.sqrt(overLevel - 1f) + 1f);
            } else {
                desire += 1f;
            }
        } else if (veryHighFlux) {
            desire *= 1.2f;
            if (system.isActive() && (overLevel > 1f) && !ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
                desire += 0.75f * ((float) Math.sqrt(overLevel - 1f) + 1f);
            } else {
                desire += 0.5f;
            }
        } else if (highFlux) {
            desire *= 1.1f;
            if (system.isActive() && (overLevel > 1f) && !ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
                desire += 0.375f * ((float) Math.sqrt(overLevel - 1f) + 1f);
            } else {
                desire += 0.25f;
            }
        }

        if (desire > maxDesire) {
            maxDesire = desire;
        } else {
            if (system.isActive()) {
                maxDesire -= (amount / HYSTERESIS_TIME_ACTIVE) * (maxDesire - desire);
            } else {
                maxDesire -= (amount / HYSTERESIS_TIME) * (maxDesire - desire);
            }
        }

        float overLevelFactor = overLevel;
        if (ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
            overLevelFactor = (float) Math.sqrt(overLevel);
        }

        float targetDesire;
        if (system.isActive()) {
            float overloadDangerFactor;
            if (overloadDanger) {
                overloadDangerFactor = 3f;
            } else {
                overloadDangerFactor = 1f;
            }
            if (panic) {
                targetDesire = 0.25f + ((1f - gauge) * overLevelFactor * overloadDangerFactor);
            } else {
                targetDesire = 0.5f + ((1f - gauge) * overLevelFactor * overloadDangerFactor);
            }
            /* Fewer things to increase desire; need lower target for Targeting or station module */
            if (ship.getVariant().hasHullMod(II_BasePackage.TARGETING_PACKAGE)) {
                targetDesire *= 0.75f;
            }
            if (ship.isStationModule()) {
                targetDesire *= 2f / 3f;
            }
        } else {
            float overloadDangerFactor;
            if (overloadDanger) {
                overloadDangerFactor = 2f;
            } else {
                overloadDangerFactor = 1f;
            }
            if (panic) {
                targetDesire = 0.375f + ((1f - gauge) * overLevelFactor * overloadDangerFactor);
            } else {
                targetDesire = 0.75f + ((1f - gauge) * overLevelFactor * overloadDangerFactor);
            }
            /* Fewer things to increase desire; need lower target for Targeting or station module */
            if (ship.getVariant().hasHullMod(II_BasePackage.TARGETING_PACKAGE)) {
                targetDesire *= 0.75f;
            }
            if (ship.isStationModule()) {
                targetDesire *= 2f / 3f;
            }
        }

        if (!ship.getFluxTracker().isOverloadedOrVenting() && II_OverdriveStats.isUsable(ship, system)) {
            if (system.isActive()) {
                if (maxDesire < targetDesire) {
                    ship.useSystem();
                }
            } else {
                if (maxDesire >= targetDesire) {
                    ship.useSystem();
                }
            }
        }

        if (reEvalTimer > 0f) {
            reEvalTimer -= amount;
        }

        if (collisionDanger || (lowGauge && (veryHighFlux || (highFlux && (flags.hasFlag(AIFlags.NEEDS_HELP) || panic))))) {
            if (forceAggressiveAI) {
                forceAggressiveAI = false;
                reEvalTimer = 0f;
                restoreAIConfig(ship);
            }

            flags.setFlag(AIFlags.BACK_OFF, 0.2f);
            flags.setFlag(AIFlags.DO_NOT_PURSUE, 0.2f);
            flags.setFlag(AIFlags.DO_NOT_USE_FLUX, 0.2f);
            flags.setFlag(AIFlags.DO_NOT_AUTOFIRE_NON_ESSENTIAL_GROUPS, 0.2f);
            flags.unsetFlag(AIFlags.DO_NOT_BACK_OFF);

            if ((reEvalTimer <= 0f) && !forceTimidAI) {
                forceTimidAI = true;
                reEvalTimer = RE_EVAL_TIME;
                saveAIConfig(ship);
                ship.getShipAI().getConfig().backingOffWhileNotVentingAllowed = true;
                ship.getShipAI().getConfig().personalityOverride = II_Util.getLessAggressivePersonality(CombatUtils.getFleetMember(ship), ship);
                ship.getShipAI().forceCircumstanceEvaluation();
                ship.getShipAI().cancelCurrentManeuver();
            }
        } else {
            if ((reEvalTimer <= 0f) && forceTimidAI) {
                forceTimidAI = false;
                reEvalTimer = RE_EVAL_TIME;
                flags.unsetFlag(AIFlags.BACK_OFF);
                flags.unsetFlag(AIFlags.DO_NOT_PURSUE);
                flags.unsetFlag(AIFlags.DO_NOT_USE_FLUX);
                flags.unsetFlag(AIFlags.DO_NOT_AUTOFIRE_NON_ESSENTIAL_GROUPS);
                restoreAIConfig(ship);
                ship.getShipAI().forceCircumstanceEvaluation();
            }
        }

        if (!collisionDanger && highGauge && !highFlux && !panic) {
            if (forceTimidAI) {
                forceTimidAI = false;
                reEvalTimer = 0f;
                restoreAIConfig(ship);
            }

            if (collisionDanger || immediateTargetTooClose) {
                flags.setFlag(AIFlags.BACK_OFF, 0.2f);
                flags.unsetFlag(AIFlags.DO_NOT_BACK_OFF);
            } else {
                if (immediateTargetAtGoodRange || immediateTargetTooFar || smallCraftTarget) {
                    flags.setFlag(AIFlags.DO_NOT_BACK_OFF, 0.2f);
                    flags.unsetFlag(AIFlags.BACK_OFF);
                    if ((!immediateTargetTooFar || smallCraftTarget) && forceAggressiveAI) {
                        ship.getShipAI().getConfig().backingOffWhileNotVentingAllowed = false;
                    }
                }
            }
            flags.unsetFlag(AIFlags.DO_NOT_PURSUE);
            flags.unsetFlag(AIFlags.DO_NOT_USE_FLUX);
            flags.unsetFlag(AIFlags.DO_NOT_AUTOFIRE_NON_ESSENTIAL_GROUPS);

            if ((reEvalTimer <= 0f) && !forceAggressiveAI) {
                forceAggressiveAI = true;
                reEvalTimer = RE_EVAL_TIME;
                saveAIConfig(ship);
                if ((immediateTargetAtGoodRange && !immediateTargetTooFar) || smallCraftTarget) {
                    ship.getShipAI().getConfig().backingOffWhileNotVentingAllowed = false;
                }
                ship.getShipAI().getConfig().personalityOverride = II_Util.getMoreAggressivePersonality(CombatUtils.getFleetMember(ship), ship);
                ship.getShipAI().forceCircumstanceEvaluation();
            }
        } else {
            if ((reEvalTimer <= 0f) && forceAggressiveAI) {
                forceAggressiveAI = false;
                reEvalTimer = RE_EVAL_TIME;
                flags.unsetFlag(AIFlags.DO_NOT_BACK_OFF);
                restoreAIConfig(ship);
                ship.getShipAI().forceCircumstanceEvaluation();
                ship.getShipAI().cancelCurrentManeuver();
            }
        }

        if (DEBUG) {
            desireShow = maxDesire;
            unfilteredDesire = desire;
            targetDesireShow = targetDesire;

            if (engine.getPlayerShip() == ship) {
                engine.maintainStatusForPlayerShip(STATUSKEY1, system.getSpecAPI().getIconSpriteName(),
                        "AI", "Desire: " + Math.round(100f * desireShow) + "/" + Math.round(100f * targetDesireShow), desireShow < targetDesireShow);
                engine.maintainStatusForPlayerShip(STATUSKEY2, system.getSpecAPI().getIconSpriteName(),
                        "AI", "Desire (no filt): " + Math.round(100f * unfilteredDesire), desireShow < targetDesireShow);
                engine.maintainStatusForPlayerShip(STATUSKEY3, system.getSpecAPI().getIconSpriteName(),
                        "AI", system.isActive() ? "Active" : "Inactive", !system.isActive());
                engine.maintainStatusForPlayerShip(STATUSKEY4, system.getSpecAPI().getIconSpriteName(),
                        "AI", II_OverdriveStats.isUsable(ship, system) ? "Usable" : "Unusable", !II_OverdriveStats.isUsable(ship, system));
                engine.maintainStatusForPlayerShip(STATUSKEY5, system.getSpecAPI().getIconSpriteName(),
                        "AI", ship.getFluxTracker().isOverloadedOrVenting() ? "Overload/vent" : "Normal", ship.getFluxTracker().isOverloadedOrVenting());
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

    private void saveAIConfig(ShipAPI ship) {
        if (ship.getShipAI().getConfig() != null) {
            savedConfig.backingOffWhileNotVentingAllowed = ship.getShipAI().getConfig().backingOffWhileNotVentingAllowed;
            savedConfig.personalityOverride = ship.getShipAI().getConfig().personalityOverride;
        }
    }

    private void restoreAIConfig(ShipAPI ship) {
        if (ship.getShipAI().getConfig() != null) {
            ship.getShipAI().getConfig().backingOffWhileNotVentingAllowed = savedConfig.backingOffWhileNotVentingAllowed;
            ship.getShipAI().getConfig().personalityOverride = savedConfig.personalityOverride;
        }
    }
}
