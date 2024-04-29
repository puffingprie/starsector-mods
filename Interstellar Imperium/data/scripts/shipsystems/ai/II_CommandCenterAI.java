package data.scripts.shipsystems.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.FighterWingAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.loading.WingRole;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.hullmods.II_BasePackage;
import data.scripts.shipsystems.II_CommandCenterStats;
import data.scripts.shipsystems.II_CommandCenterStats.CCMode;
import java.util.ArrayList;
import java.util.List;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class II_CommandCenterAI implements ShipSystemAIScript {

    private static final float REGROUP_SPEED_THRESHOLD_STANDARD = 1f / 3f;
    private static final float REGROUP_REFIT_LIMIT_STANDARD = 2f / 3f;
    private static final float REGROUP_ATTACK_THRESHOLD_STANDARD = 0.5f;
    private static final float ENGAGE_REFIT_LIMIT_STANDARD = 1f / 3f;
    private static final float ENGAGE_ATTACK_THRESHOLD_STANDARD = 0.5f;
    private static final float ATTACK_RUN_TIMER_STANDARD = 2f;

    private static final float REGROUP_SPEED_THRESHOLD_ARMOR = 0.5f;
    private static final float REGROUP_REFIT_LIMIT_ARMOR = 0.75f;
    private static final float REGROUP_ATTACK_THRESHOLD_ARMOR = 2f / 3f;
    private static final float ENGAGE_REFIT_LIMIT_ARMOR = 0.5f;
    private static final float ENGAGE_ATTACK_THRESHOLD_ARMOR = 2f / 3f;

    private static final float REGROUP_SPEED_THRESHOLD_TARGETING = 0.2f;
    private static final float REGROUP_REFIT_LIMIT_TARGETING = 0.5f;
    private static final float REGROUP_ATTACK_THRESHOLD_TARGETING = 0.5f;
    private static final float ENGAGE_REFIT_LIMIT_TARGETING = 0.25f;
    private static final float ENGAGE_ATTACK_THRESHOLD_TARGETING = 2f / 3f;

    private static final float REGROUP_REFIT_LIMIT_ELITE = 0.5f;
    private static final float REGROUP_ATTACK_THRESHOLD_ELITE = 1f / 3f;
    private static final float ENGAGE_REFIT_LIMIT_ELITE = 0.25f;
    private static final float ENGAGE_ATTACK_THRESHOLD_ELITE = 1f / 3f;
    private static final float ATTACK_RUN_TIMER_ELITE = 2.5f;

    private static List<ShipAPI> getFighters(ShipAPI carrier) {
        List<ShipAPI> result = new ArrayList<>(30);

        for (ShipAPI ship : Global.getCombatEngine().getShips()) {
            if (!ship.isFighter()) {
                continue;
            }
            if (ship.getWing() == null) {
                continue;
            }
            if (ship.getWing().getSourceShip() == carrier) {
                result.add(ship);
            }
        }

        return result;
    }

    private CombatEngineAPI engine;
    private ShipwideAIFlags flags;
    private ShipAPI ship;

    private final IntervalUtil tracker = new IntervalUtil(0.1f, 0.2f);

    private float attackRunTimer = 0f;

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
        if (engine == null) {
            return;
        }

        if (engine.isPaused()) {
            return;
        }

        if (attackRunTimer > 0f) {
            attackRunTimer -= amount;
        }

        tracker.advance(amount);

        if (tracker.intervalElapsed()) {
            if (ship.getFluxTracker().isOverloadedOrVenting()) {
                return;
            }

            float REGROUP_SPEED_THRESHOLD = REGROUP_SPEED_THRESHOLD_STANDARD;
            float REGROUP_REFIT_LIMIT = REGROUP_REFIT_LIMIT_STANDARD;
            float REGROUP_ATTACK_THRESHOLD = REGROUP_ATTACK_THRESHOLD_STANDARD;
            float ENGAGE_REFIT_LIMIT = ENGAGE_REFIT_LIMIT_STANDARD;
            float ENGAGE_ATTACK_THRESHOLD = ENGAGE_ATTACK_THRESHOLD_STANDARD;
            float ATTACK_RUN_TIMER = ATTACK_RUN_TIMER_STANDARD;
            if (ship.getVariant().hasHullMod(II_BasePackage.ARMOR_PACKAGE)) {
                REGROUP_SPEED_THRESHOLD = REGROUP_SPEED_THRESHOLD_ARMOR;
                REGROUP_REFIT_LIMIT = REGROUP_REFIT_LIMIT_ARMOR;
                REGROUP_ATTACK_THRESHOLD = REGROUP_ATTACK_THRESHOLD_ARMOR;
                ENGAGE_REFIT_LIMIT = ENGAGE_REFIT_LIMIT_ARMOR;
                ENGAGE_ATTACK_THRESHOLD = ENGAGE_ATTACK_THRESHOLD_ARMOR;
            } else if (ship.getVariant().hasHullMod(II_BasePackage.TARGETING_PACKAGE)) {
                REGROUP_SPEED_THRESHOLD = REGROUP_SPEED_THRESHOLD_TARGETING;
                REGROUP_REFIT_LIMIT = REGROUP_REFIT_LIMIT_TARGETING;
                REGROUP_ATTACK_THRESHOLD = REGROUP_ATTACK_THRESHOLD_TARGETING;
                ENGAGE_REFIT_LIMIT = ENGAGE_REFIT_LIMIT_TARGETING;
                ENGAGE_ATTACK_THRESHOLD = ENGAGE_ATTACK_THRESHOLD_TARGETING;
            } else if (ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
                REGROUP_REFIT_LIMIT = REGROUP_REFIT_LIMIT_ELITE;
                REGROUP_ATTACK_THRESHOLD = REGROUP_ATTACK_THRESHOLD_ELITE;
                ENGAGE_REFIT_LIMIT = ENGAGE_REFIT_LIMIT_ELITE;
                ENGAGE_ATTACK_THRESHOLD = ENGAGE_ATTACK_THRESHOLD_ELITE;
                ATTACK_RUN_TIMER = ATTACK_RUN_TIMER_ELITE;
            }

            ShipAPI immediateShipTarget;
            if (flags.getCustom(ShipwideAIFlags.AIFlags.CARRIER_FIGHTER_TARGET) instanceof ShipAPI) {
                immediateShipTarget = (ShipAPI) flags.getCustom(ShipwideAIFlags.AIFlags.CARRIER_FIGHTER_TARGET);
            } else if (flags.getCustom(ShipwideAIFlags.AIFlags.MANEUVER_TARGET) instanceof ShipAPI) {
                immediateShipTarget = (ShipAPI) flags.getCustom(ShipwideAIFlags.AIFlags.MANEUVER_TARGET);
            } else {
                immediateShipTarget = ship.getShipTarget();
            }

            float engageRange = 1000f;
            for (WeaponAPI weapon : ship.getUsableWeapons()) {
                if (weapon.getType() == WeaponType.MISSILE) {
                    continue;
                }
                if (weapon.getRange() > engageRange) {
                    engageRange = weapon.getRange();
                }
            }

            boolean carrierNearTarget = (immediateShipTarget != null) && (MathUtils.getDistance(ship, immediateShipTarget) <= engageRange);

            float fightersActive = 0f;
            float fightersNearCarrier = 0f;
            float fightersNearTarget = 0f;
            float nonBomberFightersNearTarget = 0f;
            float fightersNotNearTarget = 0f;
            float nonBomberFightersNotNearTarget = 0f;
            for (ShipAPI fighter : getFighters(ship)) {
                float fighterEngageRange = 500f;
                for (WeaponAPI weapon : fighter.getUsableWeapons()) {
                    if (weapon.getType() == WeaponType.MISSILE) {
                        continue;
                    }
                    if (weapon.getRange() > fighterEngageRange) {
                        fighterEngageRange = weapon.getRange();
                    }
                }

                float targetNearnessThreshold;
                if (fighterEngageRange >= (fighter.getWing().getSpec().getAttackRunRange() + 500f)) {
                    targetNearnessThreshold = fighterEngageRange;
                } else {
                    targetNearnessThreshold = (fighter.getWing().getSpec().getAttackRunRange() + 500f + fighterEngageRange) * 0.5f;
                }
                float carrierNearnessThreshold = 750f;

                if (fighter.isAlive() && engine.isEntityInPlay(fighter) && !fighter.isLanding()) {
                    fightersActive += (fighter.getWing().getSpec().getOpCost(null) + 20f) / (float) fighter.getWing().getSpec().getNumFighters();
                }
                if (MathUtils.getDistance(fighter, ship) <= carrierNearnessThreshold) {
                    fightersNearCarrier += (fighter.getWing().getSpec().getOpCost(null) + 20f) / (float) fighter.getWing().getSpec().getNumFighters();
                }
                if ((immediateShipTarget != null) && (MathUtils.getDistance(fighter, immediateShipTarget) <= targetNearnessThreshold)) {
                    fightersNearTarget += (fighter.getWing().getSpec().getOpCost(null) + 20f) / (float) fighter.getWing().getSpec().getNumFighters();
                    if (fighter.getWing().getRole() != WingRole.BOMBER) {
                        nonBomberFightersNearTarget += (fighter.getWing().getSpec().getOpCost(null) + 20f) / (float) fighter.getWing().getSpec().getNumFighters();
                    }
                } else if ((immediateShipTarget != null) && (MathUtils.getDistance(fighter, immediateShipTarget) > targetNearnessThreshold)
                        && (MathUtils.getDistance(fighter, immediateShipTarget) <= fighter.getWing().getRange())) {
                    fightersNotNearTarget += (fighter.getWing().getSpec().getOpCost(null) + 20f) / (float) fighter.getWing().getSpec().getNumFighters();
                    if (fighter.getWing().getRole() != WingRole.BOMBER) {
                        nonBomberFightersNotNearTarget += (fighter.getWing().getSpec().getOpCost(null) + 20f) / (float) fighter.getWing().getSpec().getNumFighters();
                    }
                }
            }

            float totalFighters = 0f;
            for (FighterWingAPI wing : ship.getAllWings()) {
                totalFighters += wing.getSpec().getOpCost(null) + 20f;
            }

            CCMode desiredMode;
            if (totalFighters > 0) {
                if (ship.isPullBackFighters()) {
                    float fightersNotNearCarrier = fightersActive - fightersNearCarrier;
                    float nonBomberFightersThatCanReachTarget = nonBomberFightersNearTarget + nonBomberFightersNotNearTarget;
                    if (fightersNotNearCarrier > (totalFighters * REGROUP_SPEED_THRESHOLD)) {
                        desiredMode = CCMode.SPEED;
                    } else if ((!carrierNearTarget || (fightersActive < (totalFighters * REGROUP_REFIT_LIMIT)))
                            && (nonBomberFightersNearTarget <= (nonBomberFightersThatCanReachTarget * REGROUP_ATTACK_THRESHOLD))) {
                        desiredMode = CCMode.REFIT;
                    } else {
                        desiredMode = CCMode.ATTACK;
                    }
                } else {
                    float fightersThatCanReachTarget = fightersNotNearTarget + fightersNearTarget;
                    if (fightersActive < (totalFighters * ENGAGE_REFIT_LIMIT)) {
                        desiredMode = CCMode.REFIT;
                    } else if (fightersNearTarget > (fightersThatCanReachTarget * ENGAGE_ATTACK_THRESHOLD)) {
                        desiredMode = CCMode.ATTACK;
                        attackRunTimer = ATTACK_RUN_TIMER;
                    } else {
                        desiredMode = CCMode.SPEED;
                    }
                }
            } else {
                desiredMode = CCMode.REFIT;
                attackRunTimer = 0f;
            }

            if ((desiredMode != II_CommandCenterStats.getMode(ship))
                    && ((attackRunTimer <= 0f) || (desiredMode == CCMode.ATTACK))
                    && AIUtils.canUseSystemThisFrame(ship)) {
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
