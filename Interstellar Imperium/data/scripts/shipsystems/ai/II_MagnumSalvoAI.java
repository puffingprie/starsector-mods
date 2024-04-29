package data.scripts.shipsystems.ai;

import com.fs.starfarer.api.combat.CombatAssignmentType;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatFleetManagerAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponGroupAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.shipsystems.II_MagnumSalvoStats;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class II_MagnumSalvoAI implements ShipSystemAIScript {

    private static final float ARMAGEDDON_RANGE = 1750f;
    private static final float TARGET_DESIRE = 0.4f;

    private CombatEngineAPI engine;
    private ShipwideAIFlags flags;
    private ShipAPI ship;
    private ShipSystemAPI system;

    private final IntervalUtil tracker = new IntervalUtil(0.2f, 0.3f);

    private static final boolean DEBUG = false;
    private final Object STATUSKEY1 = new Object();
    private float desireShow = 0f;
    private float targetDesireShow = 0f;

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
                }
            }
            return;
        }

        /* Force-fire! */
        WeaponGroupAPI armageddonGroup = null;
        if (II_MagnumSalvoStats.getArmed(ship) > 0) {
            for (WeaponAPI weapon : ship.getUsableWeapons()) {
                if (weapon.getId().startsWith("ii_armageddon")) {
                    if (!weapon.isDisabled() && !weapon.isPermanentlyDisabled() && !weapon.isFiring()) {
                        armageddonGroup = ship.getWeaponGroupFor(weapon);
                        break;
                    }
                }
            }
        }
        /* Don't vent in the middle of a salvo! */
        if (armageddonGroup != null) {
            flags.setFlag(AIFlags.DO_NOT_VENT, 1f);
        }

        tracker.advance(amount);
        if (tracker.intervalElapsed()) {
            ShipAPI immediateShipTarget;
            if (flags.getCustom(AIFlags.MANEUVER_TARGET) instanceof ShipAPI) {
                immediateShipTarget = (ShipAPI) flags.getCustom(AIFlags.MANEUVER_TARGET);
            } else {
                immediateShipTarget = ship.getShipTarget();
            }

            if ((armageddonGroup != null)
                    && ((immediateShipTarget == null) || (MathUtils.getDistance(immediateShipTarget, ship) <= ARMAGEDDON_RANGE))) {
                int groupNum = 0;
                boolean foundGroup = false;
                for (WeaponGroupAPI group : ship.getWeaponGroupsCopy()) {
                    if (group == armageddonGroup) {
                        foundGroup = true;
                        break;
                    } else {
                        groupNum++;
                    }
                }
                if (foundGroup) {
                    if (ship.getSelectedGroupAPI() != armageddonGroup) {
                        ship.giveCommand(ShipCommand.SELECT_GROUP, null, groupNum);
                    }
                    if (ship.getSelectedGroupAPI() == armageddonGroup) {
                        ship.giveCommand(ShipCommand.FIRE, ship.getMouseTarget(), groupNum);
                    }
                }
            }

            if (ship.getFluxTracker().isOverloadedOrVenting() || !AIUtils.canUseSystemThisFrame(ship)) {
                if (DEBUG) {
                    if (engine.getPlayerShip() == ship) {
                        engine.maintainStatusForPlayerShip(STATUSKEY1, system.getSpecAPI().getIconSpriteName(),
                                "AI", "Desire: " + Math.round(100f * desireShow) + "/" + Math.round(100f * targetDesireShow), desireShow < targetDesireShow);
                    }
                }
                return;
            }

            float potential = II_MagnumSalvoStats.getPotential(ship);
            if (potential <= 0f) {
                if (DEBUG) {
                    if (engine.getPlayerShip() == ship) {
                        engine.maintainStatusForPlayerShip(STATUSKEY1, system.getSpecAPI().getIconSpriteName(),
                                "AI", "Desire: " + Math.round(100f * desireShow) + "/" + Math.round(100f * targetDesireShow), desireShow < targetDesireShow);
                    }
                }
                return;
            }

            float fluxRemaining = ship.getFluxTracker().getMaxFlux() - ship.getFluxTracker().getCurrFlux();
            float totalFluxCost = system.getFluxPerUse();
            for (WeaponAPI weapon : ship.getUsableWeapons()) {
                if (weapon.getId().startsWith("ii_armageddon")) {
                    if (!weapon.isDisabled() && !weapon.isPermanentlyDisabled()) {
                        totalFluxCost += weapon.getFluxCostToFire();
                    }
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
            engageRange = Math.min(engageRange, ARMAGEDDON_RANGE);

            /* Red "panic!" flags that should make using the system too risky or impractical */
            if (flags.hasFlag(AIFlags.DO_NOT_USE_FLUX)) {
                if (DEBUG) {
                    if (engine.getPlayerShip() == ship) {
                        engine.maintainStatusForPlayerShip(STATUSKEY1, system.getSpecAPI().getIconSpriteName(),
                                "AI", "Desire: " + Math.round(100f * desireShow) + "/" + Math.round(100f * targetDesireShow), desireShow < targetDesireShow);
                    }
                }
                return;
            }

            CombatFleetManagerAPI.AssignmentInfo assignment = engine.getFleetManager(ship.getOwner()).getTaskManager(ship.isAlly()).getAssignmentFor(ship);
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

            boolean immediateTargetInRange = false;
            if ((immediateShipTarget != null) && (MathUtils.getDistance(immediateShipTarget, ship) < (engageRange - ship.getCollisionRadius()))) {
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
            if ((immediateShipTarget != null) && !immediateTargetInRange) {
                if (ship.getFluxTracker().isEngineBoostActive()) {
                    desire *= 0.25f;
                }
            }
            if ((targetSpot != null) && (MathUtils.getDistance(targetSpot, ship.getLocation()) >= desiredRange) && !immediateTargetInRange) {
                if (ship.getFluxTracker().isEngineBoostActive()) {
                    desire *= 0.25f;
                }
            }

            if (flags.hasFlag(AIFlags.PURSUING) || flags.hasFlag(AIFlags.BACKING_OFF)) {
                desire *= 1.25f;
            }
            if (flags.hasFlag(AIFlags.DO_NOT_BACK_OFF) || flags.hasFlag(AIFlags.DO_NOT_PURSUE)) {
                desire *= 0.75f;
            }
            if (flags.hasFlag(AIFlags.NEEDS_HELP)) {
                desire *= 1.5f;
            }

            if ((immediateShipTarget == null) || (MathUtils.getDistance(immediateShipTarget, ship) > ARMAGEDDON_RANGE)) {
                desire *= 0.25f;
            }

            if (!flags.hasFlag(AIFlags.SAFE_VENT)) {
                if (panic) {
                    desire *= 2f;
                }
            }

            desire *= (float) Math.pow(fluxRemaining / Math.max(1f, fluxRemaining + totalFluxCost), 2.0);

            desireShow = desire;
            targetDesireShow = TARGET_DESIRE;

            if (desire >= TARGET_DESIRE) {
                ship.useSystem();
            }
        }

        if (DEBUG) {
            if (engine.getPlayerShip() == ship) {
                engine.maintainStatusForPlayerShip(STATUSKEY1, system.getSpecAPI().getIconSpriteName(),
                        "AI", "Desire: " + Math.round(100f * desireShow) + "/" + Math.round(100f * targetDesireShow), desireShow < targetDesireShow);
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
