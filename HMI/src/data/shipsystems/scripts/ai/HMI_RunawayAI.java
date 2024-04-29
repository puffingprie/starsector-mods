package data.shipsystems.scripts.ai;

import com.fs.starfarer.api.combat.CombatAssignmentType;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatFleetManagerAPI.AssignmentInfo;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import java.util.List;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

public class HMI_RunawayAI implements ShipSystemAIScript {

    private CombatEngineAPI engine;
    private ShipwideAIFlags flags;
    private ShipAPI ship;

    private final IntervalUtil tracker = new IntervalUtil(0.75f, 1.5f);

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
        if (engine.isPaused()) {
            return;
        }

        tracker.advance(amount);

        if (tracker.intervalElapsed()) {
            if (!AIUtils.canUseSystemThisFrame(ship)) {
                return;
            }

            float decisionLevel = 0f;

            float allyLevel = 0f;
            float enemyLevel = 0f;
            List<ShipAPI> nearbyShips = CombatUtils.getShipsWithinRange(ship.getLocation(), 3000f);
            for (ShipAPI s : nearbyShips) {
                if (!s.isAlive() || s.isDrone() || s.isFighter() || s.isShuttlePod() || s == ship) {
                    continue;
                }

                float targetArc = MathUtils.getShortestRotation(ship.getFacing(), VectorUtils.getAngle(
                                                                ship.getLocation(), s.getLocation()));
                if (Math.abs(targetArc) > 60f) {
                    continue;
                }

                float weight = Math.min(1f, 1.4f - MathUtils.getDistance(ship, s) / 3000f);
                float shipStrength;
                switch (s.getHullSize()) {
                    default:
                    case FRIGATE:
                        shipStrength = 5f;
                        break;
                    case DESTROYER:
                        shipStrength = 10f;
                        break;
                    case CRUISER:
                        shipStrength = 15f;
                        break;
                    case CAPITAL_SHIP:
                        shipStrength = 25f;
                        break;
                }

                FleetMemberAPI member = CombatUtils.getFleetMember(s);
                if (member != null) {
                    shipStrength = 0.1f + member.getFleetPointCost();
                }

                weight *= shipStrength;

                if (s.getOwner() == ship.getOwner()) {
                    allyLevel += weight;
                } else {
                    enemyLevel += weight;
                }
            }

            FleetMemberAPI member = CombatUtils.getFleetMember(ship);
            if (member != null) {
                allyLevel += 0.1f + member.getFleetPointCost();
            } else {
                allyLevel += 30f;
            }

            AssignmentInfo assignment =
                           engine.getFleetManager(ship.getOwner()).getTaskManager(ship.isAlly()).getAssignmentFor(ship);
            Vector2f targetSpot;
            if (assignment != null && assignment.getTarget() != null) {
                targetSpot = assignment.getTarget().getLocation();
            } else {
                targetSpot = null;
            }

            if (assignment != null && assignment.getType() == CombatAssignmentType.RETREAT) {
                float retreatDirection = (ship.getOwner() == 0) ? 270f : 90f;
                if (Math.abs(MathUtils.getShortestRotation(ship.getFacing(), retreatDirection)) <= 60f) {
                    decisionLevel += 15f;
                } else if (Math.abs(MathUtils.getShortestRotation(ship.getFacing(), retreatDirection)) > 90f) {
                    decisionLevel -= 15f;
                }
            }

            if (flags.hasFlag(AIFlags.RUN_QUICKLY)) {
                decisionLevel += 7.5f;
            } else if (flags.hasFlag(AIFlags.PURSUING)) {
                decisionLevel += 10f;
            } else if (targetSpot != null && MathUtils.getDistance(ship, targetSpot) >= 2000f) {
                decisionLevel += 12f;
            } else if (target != null) {
                if (target.getFluxTracker().getMaxFlux() - target.getFluxTracker().getCurrFlux() + 4000f <
                        ship.getFluxTracker().getMaxFlux() - ship.getFluxTracker().getCurrFlux()) {
                    decisionLevel += 8f;
                }
            }

            if (flags.hasFlag(AIFlags.TURN_QUICKLY)) {
                decisionLevel += 5f;
            } else if (flags.hasFlag(AIFlags.NEEDS_HELP)) {
                decisionLevel += 5f;
            }

            if (flags.hasFlag(AIFlags.BACK_OFF) || flags.hasFlag(AIFlags.BACK_OFF_MIN_RANGE) ||
                    flags.hasFlag(AIFlags.BACKING_OFF) || flags.hasFlag(AIFlags.DO_NOT_USE_FLUX)) {
                decisionLevel *= 0f;
            }

            allyLevel *= 0.5f;
            if (enemyLevel < allyLevel) {
                enemyLevel = allyLevel;
            }
            decisionLevel *= allyLevel / enemyLevel;

            if (decisionLevel >= 10f) {
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
