package data.scripts.shipsystems.ai;

import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
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
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.hullmods.II_BasePackage;
import data.scripts.shipsystems.II_CelerityDriveStats;
import data.scripts.util.II_Util;
import java.util.List;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

public class II_CelerityDriveAI implements ShipSystemAIScript {

    private static final float TARGET_DESIRE = 0.9f;
    private static final float TARGET_DESIRE_ELITE = 0.75f;

    private CombatEngineAPI engine;
    private ShipwideAIFlags flags;
    private ShipAPI ship;
    private ShipSystemAPI system;

    private final IntervalUtil tracker = new IntervalUtil(0.2f, 0.3f);
    private boolean resetAI = false;
    private final ShipAIConfig savedConfig = new ShipAIConfig();

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
            if (ship.getFluxTracker().isOverloadedOrVenting() || system.isActive() || system.isCoolingDown() || ship.isPhased()) {
                return;
            }

            float shipRadius = II_Util.effectiveRadius(ship);

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

            boolean phaseCooldown = false;
            boolean phaseCooldownShort = false;
            if (ship.getPhaseCloak() != null) {
                if (ship.getPhaseCloak().isCoolingDown()) {
                    phaseCooldown = true;
                    if (ship.getPhaseCloak().getCooldownRemaining() < (ship.getPhaseCloak().getCooldown() / 2f)) {
                        phaseCooldownShort = true;
                    }
                } else if (ship.getPhaseCloak().isChargedown()) {
                    phaseCooldown = true;
                }
            }

            float fluxRemaining = ship.getFluxTracker().getMaxFlux() - ship.getFluxTracker().getCurrFlux();
            if ((ship.getSystem().getFluxPerUse() > fluxRemaining) && !ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
                return;
            }

            float blastArea = shipRadius * II_CelerityDriveStats.BLAST_AREA_RADIUS_SCALE + II_CelerityDriveStats.BLAST_AREA_FLAT;
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
            boolean missileThreat = missileThreatLevel >= ship.getHitpoints() * 0.5f;
            boolean highMissileThreat = missileThreatLevel >= ship.getHitpoints();
            boolean ultraHighMissileThreat = missileThreatLevel >= ship.getHitpoints() * 2f;

            float desire = 0f;
            if (!flags.hasFlag(AIFlags.BACKING_OFF)) {
                if (ultraHighFlux) {
                } else if (veryHighFlux) {
                    desire += 0.25f;
                } else if (highFlux) {
                    desire += 0.5f;
                } else {
                    desire += 0.75f;
                }
            }

            if (flags.hasFlag(AIFlags.PHASE_ATTACK_RUN_FROM_BEHIND_DIST_CRITICAL)) {
                if (ultraHighFlux) {
                    desire += 0.25f;
                } else if (veryHighFlux) {
                    desire += 0.5f;
                } else if (highFlux) {
                    desire += 0.75f;
                } else {
                    desire += 1f;
                }
            }

            if (flags.hasFlag(AIFlags.PHASE_ATTACK_RUN_IN_GOOD_SPOT)) {
                if (ultraHighFlux) {
                    desire += 0.5f;
                } else if (veryHighFlux) {
                    desire += 1f;
                } else if (highFlux) {
                    desire += 1.5f;
                } else {
                    desire += 2f;
                }
            }

            if (flags.hasFlag(AIFlags.MAINTAINING_STRIKE_RANGE)) {
                desire += 0.25f;
            }

            desire *= fluxRemaining / Math.max(1f, fluxRemaining + system.getFluxPerUse());

            if (flags.hasFlag(AIFlags.IN_CRITICAL_DPS_DANGER) || ultraHighMissileThreat) {
                if (phaseCooldown && !phaseCooldownShort) {
                    if (highMissileThreat) {
                        desire += 1.25f;
                    } else if (ultraHighMissileThreat) {
                        desire += 1f;
                    } else if (missileThreat) {
                        desire += 0.75f;
                    } else {
                        desire += 0.5f;
                    }
                    if (ship.getVariant().hasHullMod(II_BasePackage.ARMOR_PACKAGE)) {
                        desire += 0.5f;
                    }
                } else {
                    if (ultraHighMissileThreat) {
                        desire += 1.25f;
                    } else if (highMissileThreat) {
                        desire += 1f;
                    } else if (missileThreat) {
                        desire += 0.5f;
                    } else {
                        desire -= 1f;
                    }
                    if (ship.getVariant().hasHullMod(II_BasePackage.ARMOR_PACKAGE)) {
                        desire += 0.5f;
                    }
                }
            }

            if (flags.hasFlag(AIFlags.NEEDS_HELP)) {
                if (phaseCooldown && !phaseCooldownShort) {
                    if (ship.getVariant().hasHullMod(II_BasePackage.ARMOR_PACKAGE)) {
                        desire += 0.75f;
                    } else {
                        desire += 0.5f;
                    }
                } else {
                    if (!ship.getVariant().hasHullMod(II_BasePackage.ARMOR_PACKAGE) && !missileThreat) {
                        desire -= 0.5f;
                    }
                }
            }

            if (flags.hasFlag(AIFlags.HAS_INCOMING_DAMAGE) || ultraHighMissileThreat) {
                if (phaseCooldown && !phaseCooldownShort) {
                    if (missileThreat) {
                        desire += 0.25f;
                    }
                    if (ship.getVariant().hasHullMod(II_BasePackage.ARMOR_PACKAGE)) {
                        desire += 0.25f;
                    }
                } else {
                    if (highMissileThreat) {
                        desire += 0.25f;
                    }
                    if (ship.getVariant().hasHullMod(II_BasePackage.ARMOR_PACKAGE)) {
                        desire += 0.25f;
                    }
                }
            }

            if (flags.hasFlag(AIFlags.DO_NOT_PURSUE) && !missileThreat) {
                desire -= 0.25f;
            }

            if (flags.hasFlag(AIFlags.DO_NOT_USE_FLUX) && !highMissileThreat) {
                desire -= 0.25f;
            }

            if (flags.hasFlag(AIFlags.RUN_QUICKLY) && !highMissileThreat && !ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
                desire -= 0.25f;
            }

            if (flags.hasFlag(AIFlags.TURN_QUICKLY) && !missileThreat && !ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
                desire -= 0.25f;
            }

            float targetDesire;
            if (ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
                targetDesire = TARGET_DESIRE_ELITE;
            } else {
                targetDesire = TARGET_DESIRE;
            }

            if (desire >= targetDesire) {
                ship.useSystem();
            }
        }

        if (system.isActive()) {
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

            List<ShipAPI> collisionTargets = CombatUtils.getShipsWithinRange(ship.getLocation(), ship.getCollisionRadius());
            boolean collisionDanger = false;
            for (ShipAPI collisionTarget : collisionTargets) {
                if ((ship != collisionTarget) && (collisionTarget.getCollisionClass() == CollisionClass.SHIP)) {
                    collisionDanger = true;
                }
            }
            if (immediateTargetNearDangerousDeath && (immediateShipTarget != null) && (MathUtils.getDistance(immediateShipTarget, ship) < (optimalRange / 4f))) {
                collisionDanger = true;
            }

            if (collisionDanger || immediateTargetTooClose) {
                flags.setFlag(AIFlags.BACK_OFF, 0.2f);
                flags.unsetFlag(AIFlags.DO_NOT_BACK_OFF);
            } else {
                if (immediateTargetAtGoodRange || immediateTargetTooFar || smallCraftTarget) {
                    flags.setFlag(AIFlags.DO_NOT_BACK_OFF, 0.2f);
                    flags.unsetFlag(AIFlags.BACK_OFF);
                    if ((!immediateTargetTooFar || smallCraftTarget) && resetAI) {
                        ship.getShipAI().getConfig().alwaysStrafeOffensively = true;
                        ship.getShipAI().getConfig().backingOffWhileNotVentingAllowed = false;
                    }
                }
            }
            flags.setFlag(AIFlags.OK_TO_CANCEL_SYSTEM_USE_TO_VENT, 0.2f);
            flags.setFlag(AIFlags.DO_NOT_USE_SHIELDS, 0.2f);
            flags.unsetFlag(AIFlags.DO_NOT_PURSUE);
            flags.unsetFlag(AIFlags.DO_NOT_USE_FLUX);
            flags.unsetFlag(AIFlags.DO_NOT_AUTOFIRE_NON_ESSENTIAL_GROUPS);

            if (!resetAI) {
                resetAI = true;
                saveAIConfig(ship);
                if ((immediateTargetAtGoodRange && !immediateTargetTooFar) || smallCraftTarget) {
                    ship.getShipAI().getConfig().alwaysStrafeOffensively = true;
                    ship.getShipAI().getConfig().turnToFaceWithUndamagedArmor = false;
                }
                ship.getShipAI().getConfig().personalityOverride = Personalities.RECKLESS;
                ship.getShipAI().forceCircumstanceEvaluation();
                ship.getShipAI().cancelCurrentManeuver();
            }

            WeaponGroupAPI patellaGroup = null;
            for (WeaponAPI weapon : ship.getUsableWeapons()) {
                if (weapon.getId().contentEquals("ii_patella")) {
                    if (!weapon.isDisabled() && !weapon.isPermanentlyDisabled() && !weapon.isFiring()) {
                        patellaGroup = ship.getWeaponGroupFor(weapon);
                        break;
                    }
                }
            }

            if (patellaGroup != null) {
                int groupNum = 0;
                boolean foundGroup = false;
                for (WeaponGroupAPI group : ship.getWeaponGroupsCopy()) {
                    if (group == patellaGroup) {
                        foundGroup = true;
                        break;
                    } else {
                        groupNum++;
                    }
                }
                if (foundGroup) {
                    if (!patellaGroup.isAutofiring()) {
                        ship.giveCommand(ShipCommand.TOGGLE_AUTOFIRE, null, groupNum);
                    }
                }
            }
        } else {
            if (resetAI) {
                resetAI = false;
                flags.unsetFlag(AIFlags.DO_NOT_BACK_OFF);
                flags.unsetFlag(AIFlags.OK_TO_CANCEL_SYSTEM_USE_TO_VENT);
                restoreAIConfig(ship);
                ship.getShipAI().forceCircumstanceEvaluation();
                ship.getShipAI().cancelCurrentManeuver();
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
            savedConfig.turnToFaceWithUndamagedArmor = ship.getShipAI().getConfig().turnToFaceWithUndamagedArmor;
            savedConfig.personalityOverride = ship.getShipAI().getConfig().personalityOverride;
        }
    }

    private void restoreAIConfig(ShipAPI ship) {
        if (ship.getShipAI().getConfig() != null) {
            ship.getShipAI().getConfig().alwaysStrafeOffensively = savedConfig.alwaysStrafeOffensively;
            ship.getShipAI().getConfig().turnToFaceWithUndamagedArmor = savedConfig.turnToFaceWithUndamagedArmor;
            ship.getShipAI().getConfig().personalityOverride = savedConfig.personalityOverride;
        }
    }
}
