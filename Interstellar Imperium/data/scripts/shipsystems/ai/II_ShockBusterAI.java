package data.scripts.shipsystems.ai;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.AIHints;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.hullmods.II_BasePackage;
import data.scripts.shipsystems.II_ShockBusterStats;
import data.scripts.util.II_Util;
import java.util.List;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

public class II_ShockBusterAI implements ShipSystemAIScript {

    private CombatEngineAPI engine;

    private ShipwideAIFlags flags;
    private ShipAPI ship;
    private final IntervalUtil tracker = new IntervalUtil(0.2f, 0.3f);

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
        if (engine == null) {
            return;
        }

        if (engine.isPaused()) {
            return;
        }

        tracker.advance(amount);
        Vector2f shipLoc = ship.getLocation();

        if (tracker.intervalElapsed()) {
            if (!AIUtils.canUseSystemThisFrame(ship)) {
                return;
            }

            float shipRadius = II_Util.effectiveRadius(ship);

            float maxRange = II_ShockBusterStats.MAX_RANGE;
            float falloffRange = II_ShockBusterStats.FALLOFF_RANGE;
            float decisionTarget = 90f;
            float blastArea = shipRadius * II_ShockBusterStats.BLAST_AREA_RADIUS_SCALE + II_ShockBusterStats.BLAST_AREA_FLAT;
            if (ship.getVariant().hasHullMod(II_BasePackage.ARMOR_PACKAGE)) {
                maxRange = II_ShockBusterStats.ARMOR_MAX_RANGE;
                falloffRange = II_ShockBusterStats.ARMOR_FALLOFF_RANGE;
                decisionTarget = 150f;
                blastArea = shipRadius * II_ShockBusterStats.ARMOR_BLAST_AREA_RADIUS_SCALE + II_ShockBusterStats.ARMOR_BLAST_AREA_FLAT;
            } else if (ship.getVariant().hasHullMod(II_BasePackage.TARGETING_PACKAGE)) {
                maxRange = II_ShockBusterStats.TARGETING_MAX_RANGE;
                falloffRange = II_ShockBusterStats.TARGETING_FALLOFF_RANGE;
                decisionTarget = 110f;
            } else if (ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
                maxRange = II_ShockBusterStats.ELITE_MAX_RANGE;
                falloffRange = II_ShockBusterStats.ELITE_FALLOFF_RANGE;
                decisionTarget = 60f;
            } else if (ship.getVariant().hasHullMod("supercomputer")) {
                maxRange = II_ShockBusterStats.STATION_MAX_RANGE;
                falloffRange = II_ShockBusterStats.STATION_FALLOFF_RANGE;
                decisionTarget = 90f;
            }

            maxRange = ship.getMutableStats().getSystemRangeBonus().computeEffective(maxRange);
            falloffRange = ship.getMutableStats().getSystemRangeBonus().computeEffective(falloffRange);
            blastArea = ship.getMutableStats().getSystemRangeBonus().computeEffective(blastArea);

            float flankMaxWeight;
            switch (ship.getHullSize()) {
                default:
                case FRIGATE:
                    flankMaxWeight = 0.25f;
                    decisionTarget *= 0.8f;
                    break;
                case DESTROYER:
                    flankMaxWeight = 0.5f;
                    break;
                case CRUISER:
                    flankMaxWeight = 0.75f;
                    decisionTarget *= 1.15f;
                    break;
                case CAPITAL_SHIP:
                    flankMaxWeight = 1f;
                    decisionTarget *= 1.3f;
                    break;
            }

            if (ship.getVariant().hasHullMod(II_BasePackage.ARMOR_PACKAGE)) {
                flankMaxWeight *= 0.5f;
            }

            ShipAPI bestTarget = null;
            float bestTargetWeight = 0f;
            float totalTargetWeight = 0f;

            float engageRange = 1000f;
            float minWeaponRange = engageRange;
            for (WeaponAPI weapon : ship.getUsableWeapons()) {
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
            for (WeaponAPI weapon : ship.getUsableWeapons()) {
                if (weapon.getType() == WeaponType.MISSILE) {
                    continue;
                }
                if (weapon.hasAIHint(AIHints.PD) && !weapon.hasAIHint(AIHints.PD_ALSO)) {
                    continue;
                }
                float range = weapon.getRange();
                float opCost = Math.max(weapon.getSpec().getOrdnancePointCost(null), 1f);
                eligibleOP += opCost;
                optimalRange += range * opCost;
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

            boolean immediateTargetInRange = false;
            if ((immediateShipTarget != null) && (MathUtils.getDistance(immediateShipTarget, ship) < (engageRange - ship.getCollisionRadius()))) {
                immediateTargetInRange = true;
            }

            boolean immediateTargetWithinOptimalRange = false;
            if (immediateTargetInRange && (MathUtils.getDistance(immediateShipTarget, ship) < (optimalRange - ship.getCollisionRadius()))) {
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

            List<ShipAPI> nearbyTargets = II_Util.getShipsWithinRange(shipLoc, maxRange);
            for (ShipAPI t : nearbyTargets) {
                if ((t.getOwner() == ship.getOwner()) || !t.isAlive() || t.isPhased() || t.isShuttlePod()) {
                    continue;
                }

                if (!II_ShockBusterStats.isTargetValid(ship, t)) {
                    continue;
                }

                float weight = 1f - MathUtils.getDistance(ship, t) / falloffRange;

                float shipStrength;
                switch (t.getHullSize()) {
                    default:
                    case DEFAULT:
                    case FIGHTER:
                        if (ship.getVariant().hasHullMod(II_BasePackage.ARMOR_PACKAGE)) {
                            weight *= 2f;
                        } else {
                            weight *= 1f;
                        }
                        shipStrength = 5f;
                        break;
                    case FRIGATE:
                        weight *= 5f;
                        shipStrength = 5f;
                        break;
                    case DESTROYER:
                        weight *= 2.5f;
                        shipStrength = 10f;
                        break;
                    case CRUISER:
                        if (ship.getVariant().hasHullMod(II_BasePackage.TARGETING_PACKAGE)) {
                            weight *= 2f;
                        } else {
                            weight *= 1.5f;
                        }
                        shipStrength = 15f;
                        break;
                    case CAPITAL_SHIP:
                        if (ship.getVariant().hasHullMod(II_BasePackage.TARGETING_PACKAGE)) {
                            weight *= 1.5f;
                        } else {
                            weight *= 1f;
                        }
                        shipStrength = 25f;
                        break;
                }

                FleetMemberAPI member = CombatUtils.getFleetMember(t);
                if (member != null) {
                    shipStrength = 0.1f + member.getFleetPointCost();
                }

                weight *= (float) Math.sqrt(shipStrength);
                weight *= (float) Math.sqrt(t.getCollisionRadius());

                Vector2f relVel = Vector2f.sub(t.getVelocity(), ship.getVelocity(), new Vector2f());
                float angleToShip = VectorUtils.getAngle(t.getLocation(), ship.getLocation());
                float speedTowardShip = VectorUtils.rotate(relVel, -angleToShip).getX();
                weight *= 1f + speedTowardShip / 600f;
                if (weight < 0f) {
                    weight = 0f;
                }

                float flankAngle = Math.abs(VectorUtils.getAngle(shipLoc, t.getLocation()) - ship.getFacing());
                weight *= 1f + (flankAngle / 180f) * flankMaxWeight;

                if (!ship.isPhased()) {
                    if ((immediateShipTarget == t) || (t.getParentStation() == t)) {
                        /* Avoid pushing immediate target away if it would be counter-productive */
                        if (immediateTargetWithinOptimalRange && (wantsToStandOff || wantsToCloseDistance)) {
                            continue;
                        }
                    } else {
                        /* Avoid distracting self against non-immediate target */
                        if (immediateTargetInRange) {
                            if (immediateTargetWithinOptimalRange) {
                                weight *= 0.5f;
                            } else {
                                weight *= 0.75f;
                            }
                        }
                    }
                }

                if (flags.hasFlag(AIFlags.PHASE_ATTACK_RUN) && !flags.hasFlag(AIFlags.PHASE_ATTACK_RUN_IN_GOOD_SPOT)) {
                    if (!ship.isPhased()) {
                        if (immediateShipTarget == t) {
                            continue;
                        }
                    } else {
                        if (immediateShipTarget == t) {
                            weight *= 3f;
                        }
                    }
                }
                if (flags.hasFlag(AIFlags.PHASE_ATTACK_RUN_IN_GOOD_SPOT)) {
                    if (ship.isPhased()) {
                        continue;
                    }
                }

                if (weight >= bestTargetWeight) {
                    bestTarget = t;
                    bestTargetWeight = weight;
                    if (!ship.getVariant().hasHullMod(II_BasePackage.ARMOR_PACKAGE)) {
                        totalTargetWeight = weight;
                    }
                }

                if (ship.getVariant().hasHullMod(II_BasePackage.ARMOR_PACKAGE)) {
                    totalTargetWeight += weight;
                }
            }

            float missileThreatLevel = 0f;
            List<MissileAPI> allMissiles = CombatUtils.getMissilesWithinRange(ship.getLocation(), blastArea * 0.9f);
            II_Util.filterObscuredTargets(null, ship.getLocation(), allMissiles, false, true, false);
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
                            scale = 1.5f;
                            break;
                        default:
                        case ENERGY:
                            break;
                    }
                    missileThreatLevel += missile.getDamageAmount() * scale;
                }
            }
            boolean missileThreat;
            boolean highMissileThreat;
            boolean ultraHighMissileThreat;
            if (ship.getHullSize() == HullSize.CAPITAL_SHIP) {
                missileThreat = missileThreatLevel >= ship.getHitpoints() * 0.25f;
                highMissileThreat = missileThreatLevel >= ship.getHitpoints() * 0.5f;
                ultraHighMissileThreat = missileThreatLevel >= ship.getHitpoints();
            } else {
                missileThreat = missileThreatLevel >= ship.getHitpoints() * 0.5f;
                highMissileThreat = missileThreatLevel >= ship.getHitpoints();
                ultraHighMissileThreat = missileThreatLevel >= ship.getHitpoints() * 2f;
            }

            if ((bestTarget == null) && !missileThreat) {
                return;
            }

            float decisionLevel = bestTargetWeight + totalTargetWeight;
            if (missileThreat) {
                if (flags.hasFlag(AIFlags.PHASE_ATTACK_RUN) && !flags.hasFlag(AIFlags.PHASE_ATTACK_RUN_IN_GOOD_SPOT) && ship.isPhased()) {
                    decisionLevel += 0f;
                } else if (ship.isPhased()) {
                    decisionLevel += 50f;
                } else {
                    decisionLevel += 100f;
                }
            }
            decisionLevel *= 1f + ship.getFluxTracker().getFluxLevel();
            decisionLevel *= 1f - ship.getFluxTracker().getFluxLevel();
            if (highMissileThreat) {
                if (flags.hasFlag(AIFlags.PHASE_ATTACK_RUN) && !flags.hasFlag(AIFlags.PHASE_ATTACK_RUN_IN_GOOD_SPOT) && ship.isPhased()) {
                    decisionLevel += 10f;
                } else if (ship.isPhased()) {
                    decisionLevel += 30f;
                } else {
                    decisionLevel += 50f;
                }
            }
            decisionLevel *= II_Util.lerp(0.75f, 1.25f, ship.getHullLevel());
            if (ultraHighMissileThreat) {
                if (flags.hasFlag(AIFlags.PHASE_ATTACK_RUN) && !flags.hasFlag(AIFlags.PHASE_ATTACK_RUN_IN_GOOD_SPOT) && ship.isPhased()) {
                    decisionLevel += 40f;
                } else if (ship.isPhased()) {
                    decisionLevel += 70f;
                } else {
                    decisionLevel += 100f;
                }
            }
            if (flags.hasFlag(AIFlags.BACK_OFF) || flags.hasFlag(AIFlags.BACKING_OFF)) {
                decisionLevel *= 1.25f;
            }
            if (flags.hasFlag(AIFlags.DO_NOT_USE_FLUX)) {
                decisionLevel *= 0.5f;
            }

            if (decisionLevel >= decisionTarget) {
                if (!ship.getVariant().hasHullMod(II_BasePackage.ARMOR_PACKAGE)) {
                    boolean player = ship == engine.getPlayerShip();
                    if (player) {
                        if (bestTarget == null) {
                            ship.getMouseTarget().set(new Vector2f(ship.getLocation()));
                        } else {
                            ship.getMouseTarget().set(new Vector2f(bestTarget.getLocation()));
                        }
                    }
                    if (bestTarget == null) {
                        ship.getAIFlags().setFlag(AIFlags.SYSTEM_TARGET_COORDS, 1f, new Vector2f(ship.getLocation()));
                    } else {
                        ship.getAIFlags().setFlag(AIFlags.SYSTEM_TARGET_COORDS, 1f, new Vector2f(bestTarget.getLocation()));
                    }
                }
                ship.useSystem();
            }
        }
    }

    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.flags = flags;
        this.engine = engine;
    }
}
