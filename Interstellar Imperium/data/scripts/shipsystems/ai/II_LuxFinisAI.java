package data.scripts.shipsystems.ai;

import com.fs.starfarer.api.combat.CombatAssignmentType;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatFleetManagerAPI.AssignmentInfo;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAIConfig;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.AIHints;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.combat.WeaponGroupAPI;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import data.scripts.hullmods.II_BasePackage;
import data.scripts.shipsystems.II_LuxFinisStats;
import data.scripts.util.II_Util;
import java.util.ArrayList;
import java.util.List;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

public class II_LuxFinisAI implements ShipSystemAIScript {

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
    private boolean resetAI = false;
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
                            "AI", II_LuxFinisStats.isUsable(ship, system) ? "Usable" : "Unusable", !II_LuxFinisStats.isUsable(ship, system));
                    engine.maintainStatusForPlayerShip(STATUSKEY5, system.getSpecAPI().getIconSpriteName(),
                            "AI", ship.getFluxTracker().isOverloadedOrVenting() ? "Overload/vent" : "Normal", ship.getFluxTracker().isOverloadedOrVenting());
                }
            }
            return;
        }

        float desire = 0f;

        float gauge = II_LuxFinisStats.getGauge(ship);
        float overLevel = 1f; // 1f to 2f (elite)
        boolean overloadDanger = false;
        if (ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
            overLevel = II_LuxFinisStats.getOverlevel(ship);

            /* Shift values lower so we don't wiggle at a particular value */
            float gaugeTimeLeft = gauge * II_LuxFinisStats.GAUGE_DRAIN_TIME;
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
            lowGauge = gauge < 0.35f;
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
            if (ship.getVariant().hasHullMod(II_BasePackage.TARGETING_PACKAGE)) {
                trueFluxLevel *= II_Util.lerp(1f, II_LuxFinisStats.TARGETING_CAPACITY_MULT, system.getEffectLevel() * overLevel);
                trueHardFluxLevel *= II_Util.lerp(1f, II_LuxFinisStats.TARGETING_CAPACITY_MULT, system.getEffectLevel() * overLevel);
            } else {
                trueFluxLevel *= II_Util.lerp(1f, II_LuxFinisStats.CAPACITY_MULT, system.getEffectLevel() * overLevel);
                trueHardFluxLevel *= II_Util.lerp(1f, II_LuxFinisStats.CAPACITY_MULT, system.getEffectLevel() * overLevel);
            }
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

        AssignmentInfo assignment = engine.getFleetManager(ship.getOwner()).getTaskManager(ship.isAlly()).getAssignmentFor(ship);
        Vector2f targetSpot;
        if ((assignment != null) && (assignment.getTarget() != null) && (assignment.getType() != CombatAssignmentType.AVOID)) {
            targetSpot = assignment.getTarget().getLocation();
        } else {
            targetSpot = null;
        }

        ArrayList<WeaponAPI> blasters = new ArrayList<>(2);
        ArrayList<WeaponAPI> photonBeams = new ArrayList<>(3);
        WeaponAPI lightspear = null;
        for (WeaponAPI shipWeapon : ship.getAllWeapons()) {
            if (shipWeapon.getId().startsWith("ii_photonblaster") && !shipWeapon.isDisabled() && !shipWeapon.isPermanentlyDisabled()) {
                blasters.add(shipWeapon);
            } else if (shipWeapon.getId().startsWith("ii_photonbeam") && !shipWeapon.isDisabled() && !shipWeapon.isPermanentlyDisabled()) {
                photonBeams.add(shipWeapon);
            } else if (shipWeapon.getId().startsWith("ii_lightspear")) {
                lightspear = shipWeapon;
            }
        }
        float lightspearScale;
        if ((lightspear == null) || lightspear.getId().contentEquals("ii_lightspear")) {
            if (ship.getVariant().hasHullMod(II_BasePackage.ARMOR_PACKAGE)) {
                lightspearScale = (blasters.size() * 0.3f) + 0.4f;
            } else {
                lightspearScale = (blasters.size() * 0.15f) + 0.7f;
            }
        } else {
            lightspearScale = (photonBeams.size() * 0.15f) + 0.55f;
        }
        lightspearScale *= (float) Math.sqrt(overLevel);

        float shipRadius = II_Util.effectiveRadius(ship);
        float blastArea = shipRadius * II_LuxFinisStats.BLAST_AREA_RADIUS_SCALE + II_LuxFinisStats.BLAST_AREA_FLAT;
        float missileThreatLevel = 0f;
        List<MissileAPI> allMissiles = CombatUtils.getMissilesWithinRange(ship.getLocation(), blastArea);
        for (MissileAPI missile : allMissiles) {
            if (missile.getOwner() != ship.getOwner()) {
                float scale = 1f;
                switch (missile.getDamageType()) {
                    case FRAGMENTATION:
                        scale = 0.5f;
                        break;
                    case KINETIC:
                        scale = 0.75f;
                        break;
                    case HIGH_EXPLOSIVE:
                        scale = 2f;
                        break;
                    default:
                    case ENERGY:
                        break;
                }
                missileThreatLevel += missile.getDamageAmount() * scale;
            }
        }
        boolean missileThreat = missileThreatLevel >= (ship.getHitpoints() * 0.5f);
        boolean highMissileThreat = missileThreatLevel >= ship.getHitpoints();
        boolean ultraHighMissileThreat = missileThreatLevel >= (ship.getHitpoints() * 2f);

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
            if ((lightspear != null) && lightspear.isFiring() && (lightspear.getChargeLevel() > 0f)) {
                if (weapon != lightspear) {
                    continue;
                }
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

        ShipAPI immediateShipTarget;
        if (flags.getCustom(AIFlags.MANEUVER_TARGET) instanceof ShipAPI) {
            immediateShipTarget = (ShipAPI) flags.getCustom(AIFlags.MANEUVER_TARGET);
        } else {
            immediateShipTarget = ship.getShipTarget();
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
        float tooFarRange = Math.max(engageRange, optimalRange * 4f / 3f) + ship.getCollisionRadius();
        boolean immediateTargetTooFar = false;
        if ((immediateShipTarget != null) && !immediateShipTarget.isFighter() && (MathUtils.getDistance(immediateShipTarget, ship) >= (tooFarRange - ship.getCollisionRadius()))) {
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

        /* If we're pursuing, more likely to need the system - unless gauge is low */
        if (flags.hasFlag(AIFlags.PURSUING)) {
            if (!lowGauge) {
                desire += 0.5f;
            }
        }

        /* If we're moving in, we probably need the system */
        if (flags.hasFlag(AIFlags.HARASS_MOVE_IN)) {
            if (ship.getVariant().hasHullMod(II_BasePackage.TARGETING_PACKAGE)) {
                desire += 0.25f;
            } else {
                desire += 0.75f;
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

        /* Use the system if we're in a good spot for it */
        if (flags.hasFlag(AIFlags.PHASE_ATTACK_RUN_FROM_BEHIND_DIST_CRITICAL)) {
            desire += 0.5f;
        }
        if (flags.hasFlag(AIFlags.PHASE_ATTACK_RUN_IN_GOOD_SPOT)) {
            desire += 1f;
        }
        if (flags.hasFlag(AIFlags.PHASE_BRAWLER_DUMPING_FLUX)) {
            desire -= 1f;
        }

        /* If we need to phase through or tank some shit *right now*, this system can save our lives. Likewise, if
               we're outclassed, the system will come in handy -- but let's not waste it too soon, nor screw ourselves
               by overloading. */
        if ((flags.hasFlag(AIFlags.HAS_INCOMING_DAMAGE) && flags.hasFlag(AIFlags.IN_CRITICAL_DPS_DANGER)) || ultraHighMissileThreat) {
            if (ship.getVariant().hasHullMod(II_BasePackage.ARMOR_PACKAGE)) {
                if (veryHighFlux) {
                    if (panic) {
                        desire += 2f;
                    } else if (!overloadDanger) {
                        desire += 1.5f;
                    }
                } else if (highFlux) {
                    if (panic) {
                        desire += 1.5f;
                    } else if (!overloadDanger) {
                        desire += 1f;
                    }
                } else {
                    if (panic) {
                        desire += 1f;
                    } else if (!overloadDanger) {
                        desire += 0.75f;
                    }
                }
            } else {
                if (veryHighFlux) {
                    if (panic) {
                        desire += 1.5f;
                    } else if (!overloadDanger) {
                        desire += 1f;
                    }
                } else if (highFlux) {
                    if (panic) {
                        desire += 1f;
                    } else if (!overloadDanger) {
                        desire += 0.75f;
                    }
                } else {
                    if (panic) {
                        desire += 0.75f;
                    } else if (!overloadDanger) {
                        desire += 0.5f;
                    }
                }
            }
        } else if (flags.hasFlag(AIFlags.HAS_INCOMING_DAMAGE) || flags.hasFlag(AIFlags.IN_CRITICAL_DPS_DANGER) || ultraHighMissileThreat) {
            if (ship.getVariant().hasHullMod(II_BasePackage.ARMOR_PACKAGE)) {
                if (veryHighFlux) {
                    if (panic) {
                        desire += 1.5f;
                    } else if (!overloadDanger) {
                        desire += 1f;
                    }
                } else if (highFlux) {
                    if (panic) {
                        desire += 1f;
                    } else if (!overloadDanger) {
                        desire += 0.75f;
                    }
                } else {
                    if (panic) {
                        desire += 0.75f;
                    } else if (!overloadDanger) {
                        desire += 0.5f;
                    }
                }
            } else {
                if (veryHighFlux) {
                    if (panic) {
                        desire += 1f;
                    } else if (!overloadDanger) {
                        desire += 0.75f;
                    }
                } else if (highFlux) {
                    if (panic) {
                        desire += 0.75f;
                    } else if (!overloadDanger) {
                        desire += 0.5f;
                    }
                } else {
                    if (panic) {
                        desire += 0.5f;
                    } else if (!overloadDanger) {
                        desire += 0.35f;
                    }
                }
            }
        } else if (highMissileThreat) {
            if (veryHighFlux) {
                if (panic) {
                    desire += 0.75f;
                } else if (!overloadDanger) {
                    desire += 0.5f;
                }
            } else if (highFlux) {
                if (panic) {
                    desire += 0.5f;
                } else if (!overloadDanger) {
                    desire += 0.35f;
                }
            } else {
                if (panic) {
                    desire += 0.35f;
                } else if (!overloadDanger) {
                    desire += 0.25f;
                }
            }
        } else if (missileThreat) {
            if (veryHighFlux) {
                if (panic) {
                    desire += 0.5f;
                } else if (!overloadDanger) {
                    desire += 0.35f;
                }
            } else if (highFlux) {
                if (panic) {
                    desire += 0.35f;
                } else if (!overloadDanger) {
                    desire += 0.25f;
                }
            } else {
                if (panic) {
                    desire += 0.25f;
                } else if (!overloadDanger) {
                    desire += 0.15f;
                }
            }
        }

        /* Targeting removes the speed/maneuverability improvements */
        if (!ship.getVariant().hasHullMod(II_BasePackage.TARGETING_PACKAGE)) {
            /* If we need to spin around quickly, we probably need the system (regardless of gauge; this can save us!) */
            if (flags.hasFlag(AIFlags.TURN_QUICKLY)) {
                desire += 0.75f;
            }

            boolean immediateTargetInRange = false;
            if ((immediateShipTarget != null) && (MathUtils.getDistance(immediateShipTarget, ship) < (engageRange - ship.getCollisionRadius()))) {
                immediateTargetInRange = true;
            }

            /* If our center of attention is too far away and we're ready to go, consider using the system */
            if ((immediateShipTarget != null) && !immediateTargetInRange) {
                if (!highFlux && highGauge && !panic) {
                    desire += 0.5f;
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
                if ((immediateShipTarget != null) && (MathUtils.getDistance(immediateShipTarget, targetSpot) <= engageRange)) {
                    if (!highFlux && highGauge && !panic) {
                        desire += 0.5f; // Adds to the other 0.5
                    }
                } else if (immediateShipTarget != null) {
                    if (!highFlux && highGauge && !panic) {
                        desire += 0.25f; // Adds to the other 0.5
                    }
                } else {
                    if (!highFlux && highGauge && !panic) {
                        desire += 0.75f;
                    }
                }
            } else if (flags.hasFlag(AIFlags.SAFE_VENT)) {
                /* If we're this safe and don't have anything better to do, don't bother using the system */
                desire -= 0.5f;
            }

            /* If ordered to retreat, make sure it gets done */
            if ((assignment != null) && (assignment.getType() == CombatAssignmentType.RETREAT)) {
                if (!overloadDanger || panic) {
                    desire += 1.5f;
                }
            }
        } else {
            if (flags.hasFlag(AIFlags.MAINTAINING_STRIKE_RANGE)) {
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
            desire += 0.3f;
            desire *= 1.3f;
            if (system.isActive() && (overLevel > 1f) && !ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
                desire += 1.5f * ((float) Math.sqrt(overLevel - 1f) + 1f);
            } else {
                desire += 1f;
            }
        } else if (veryHighFlux) {
            desire += 0.2f;
            desire *= 1.2f;
            if (system.isActive() && (overLevel > 1f) && !ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
                desire += 0.75f * ((float) Math.sqrt(overLevel - 1f) + 1f);
            } else {
                desire += 0.5f;
            }
        } else if (highFlux) {
            desire += 0.1f;
            desire *= 1.1f;
            if (system.isActive() && (overLevel > 1f) && !ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
                desire += 0.375f * ((float) Math.sqrt(overLevel - 1f) + 1f);
            } else {
                desire += 0.25f;
            }
        }

        if ((lightspear != null) && !lightspear.isFiring() && (lightspear.getCooldownRemaining() <= 0f)
                && (immediateShipTarget != null) && !immediateTargetTooFar && !smallCraftTarget) {
            desire += lightspearScale;
        }

        boolean overrideAI = false;
        if (lightspear != null) {
            if (system.isActive()) {
                WeaponGroupAPI lightspearGroup = null;
                if (!lightspear.isDisabled() && !lightspear.isPermanentlyDisabled() && !lightspear.isFiring()) {
                    lightspearGroup = ship.getWeaponGroupFor(lightspear);
                }

                if (lightspearGroup != null) {
                    int groupNum = 0;
                    boolean foundGroup = false;
                    for (WeaponGroupAPI group : ship.getWeaponGroupsCopy()) {
                        if (group == lightspearGroup) {
                            foundGroup = true;
                            break;
                        } else {
                            groupNum++;
                        }
                    }
                    if (foundGroup) {
                        if ((lightspearGroup.isAutofiring() && lowGauge) || (!lightspearGroup.isAutofiring() && !lowGauge)) {
                            ship.giveCommand(ShipCommand.TOGGLE_AUTOFIRE, null, groupNum);
                        }
                    }
                }

                if (lowGauge) {
                    lightspear.setForceNoFireOneFrame(true);
                }

                if (lightspear.isFiring()) {
                    desire += 3f + (3f * lightspearScale);

                    if (lightspearScale >= 0.5f) {
                        if (forceAggressiveAI) {
                            forceAggressiveAI = false;
                            reEvalTimer = 0f;
                            restoreAIConfig(ship);
                        }
                        if (forceTimidAI) {
                            forceTimidAI = false;
                            reEvalTimer = 0f;
                            restoreAIConfig(ship);
                        }

                        overrideAI = true;
                        if (collisionDanger || immediateTargetTooClose) {
                            flags.setFlag(AIFlags.BACK_OFF, 0.2f);
                            flags.unsetFlag(AIFlags.DO_NOT_BACK_OFF);
                        } else {
                            if (immediateTargetAtGoodRange || immediateTargetTooFar) {
                                flags.setFlag(AIFlags.DO_NOT_BACK_OFF, 0.2f);
                                flags.unsetFlag(AIFlags.BACK_OFF);
                                if (!immediateTargetTooFar && resetAI) {
                                    ship.getShipAI().getConfig().alwaysStrafeOffensively = true;
                                    ship.getShipAI().getConfig().backingOffWhileNotVentingAllowed = false;
                                }
                            }
                        }
                        flags.setFlag(AIFlags.DO_NOT_USE_SHIELDS, 0.2f);
                        ship.blockCommandForOneFrame(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK);
                        flags.unsetFlag(AIFlags.TURN_QUICKLY);
                        flags.unsetFlag(AIFlags.DO_NOT_PURSUE);
                        flags.unsetFlag(AIFlags.DO_NOT_USE_FLUX);
                        flags.unsetFlag(AIFlags.DO_NOT_AUTOFIRE_NON_ESSENTIAL_GROUPS);
                        flags.unsetFlag(AIFlags.PHASE_BRAWLER_DUMPING_FLUX);
                        flags.unsetFlag(AIFlags.STAY_PHASED);

                        if (!resetAI) {
                            resetAI = true;
                            saveAIConfig(ship);
                            if (immediateTargetAtGoodRange && !immediateTargetTooFar) {
                                ship.getShipAI().getConfig().alwaysStrafeOffensively = true;
                                ship.getShipAI().getConfig().backingOffWhileNotVentingAllowed = false;
                            }
                            ship.getShipAI().getConfig().turnToFaceWithUndamagedArmor = false;
                            ship.getShipAI().getConfig().personalityOverride = Personalities.RECKLESS;
                            ship.getShipAI().forceCircumstanceEvaluation();
                            ship.getShipAI().cancelCurrentManeuver();
                        }
                    }
                }
            }
        }

        if (!overrideAI) {
            if (resetAI) {
                resetAI = false;
                flags.unsetFlag(AIFlags.DO_NOT_BACK_OFF);
                flags.unsetFlag(AIFlags.DO_NOT_USE_SHIELDS);
                restoreAIConfig(ship);
                ship.getShipAI().forceCircumstanceEvaluation();
                ship.getShipAI().cancelCurrentManeuver();
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
            if (ship.getVariant().hasHullMod(II_BasePackage.TARGETING_PACKAGE) || ship.isStationModule()) {
                targetDesire *= 0.5f;
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
            if (ship.getVariant().hasHullMod(II_BasePackage.TARGETING_PACKAGE) || ship.isStationModule()) {
                targetDesire *= 2f / 3f;
            }
        }

        if (!ship.getFluxTracker().isOverloadedOrVenting() && II_LuxFinisStats.isUsable(ship, system)) {
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

        if (!overrideAI && !resetAI) {
            if (collisionDanger || (lowGauge && (veryHighFlux || (highFlux && (flags.hasFlag(AIFlags.NEEDS_HELP) || panic))))) {
                if (collisionDanger || (!flags.hasFlag(AIFlags.PHASE_ATTACK_RUN) && !flags.hasFlag(AIFlags.PHASE_BRAWLER_DUMPING_FLUX))) {
                    if (forceAggressiveAI) {
                        forceAggressiveAI = false;
                        reEvalTimer = 0f;
                        restoreAIConfig(ship);
                    }

                    flags.setFlag(AIFlags.BACK_OFF, 0.2f);
                    flags.setFlag(AIFlags.DO_NOT_PURSUE, 0.2f);
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
                }
            } else {
                if ((reEvalTimer <= 0f) && forceTimidAI) {
                    forceTimidAI = false;
                    reEvalTimer = RE_EVAL_TIME;
                    flags.unsetFlag(AIFlags.BACK_OFF);
                    flags.unsetFlag(AIFlags.DO_NOT_PURSUE);
                    restoreAIConfig(ship);
                    ship.getShipAI().forceCircumstanceEvaluation();
                }
            }

            if (!collisionDanger && highGauge && !highFlux && !flags.hasFlag(AIFlags.NEEDS_HELP) && !panic) {
                if (!flags.hasFlag(AIFlags.PHASE_ATTACK_RUN) && !flags.hasFlag(AIFlags.PHASE_BRAWLER_DUMPING_FLUX)) {
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
                                ship.getShipAI().getConfig().alwaysStrafeOffensively = true;
                            }
                        }
                    }
                    flags.unsetFlag(AIFlags.DO_NOT_PURSUE);

                    if ((reEvalTimer <= 0f) && !forceAggressiveAI) {
                        forceAggressiveAI = true;
                        reEvalTimer = RE_EVAL_TIME;
                        saveAIConfig(ship);
                        if ((immediateTargetAtGoodRange && !immediateTargetTooFar) || smallCraftTarget) {
                            ship.getShipAI().getConfig().alwaysStrafeOffensively = true;
                        }
                        ship.getShipAI().getConfig().personalityOverride = II_Util.getMoreAggressivePersonality(CombatUtils.getFleetMember(ship), ship);
                        ship.getShipAI().forceCircumstanceEvaluation();
                    }
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
                        "AI", II_LuxFinisStats.isUsable(ship, system) ? "Usable" : "Unusable", !II_LuxFinisStats.isUsable(ship, system));
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
            savedConfig.alwaysStrafeOffensively = ship.getShipAI().getConfig().alwaysStrafeOffensively;
            savedConfig.backingOffWhileNotVentingAllowed = ship.getShipAI().getConfig().backingOffWhileNotVentingAllowed;
            savedConfig.turnToFaceWithUndamagedArmor = ship.getShipAI().getConfig().turnToFaceWithUndamagedArmor;
            savedConfig.personalityOverride = ship.getShipAI().getConfig().personalityOverride;
        }
    }

    private void restoreAIConfig(ShipAPI ship) {
        if (ship.getShipAI().getConfig() != null) {
            ship.getShipAI().getConfig().alwaysStrafeOffensively = savedConfig.alwaysStrafeOffensively;
            ship.getShipAI().getConfig().backingOffWhileNotVentingAllowed = savedConfig.backingOffWhileNotVentingAllowed;
            ship.getShipAI().getConfig().turnToFaceWithUndamagedArmor = savedConfig.turnToFaceWithUndamagedArmor;
            ship.getShipAI().getConfig().personalityOverride = savedConfig.personalityOverride;
        }
    }
}
