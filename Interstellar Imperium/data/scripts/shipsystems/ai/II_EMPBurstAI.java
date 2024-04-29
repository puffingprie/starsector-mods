package data.scripts.shipsystems.ai;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.everyframe.II_TitanPlugin;
import data.scripts.util.II_Util;
import java.util.List;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

public class II_EMPBurstAI implements ShipSystemAIScript {

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

            float blastRange = II_TitanPlugin.INITIAL_BLAST_RADIUS_BURST;
            float falloffRange = II_TitanPlugin.EXPANSION_RATE_BURST * II_TitanPlugin.EXPANSION_TIME_BURST;
            float decisionTarget = 150f;

            float totalTargetWeight = 0f;

            List<ShipAPI> nearbyTargets = II_Util.getShipsWithinRange(shipLoc, falloffRange);
            for (ShipAPI t : nearbyTargets) {
                if ((t.getOwner() == ship.getOwner()) || !t.isAlive() || t.isPhased() || t.isShuttlePod()) {
                    continue;
                }

                if (t.isFighter() && !t.isWingLeader()) {
                    continue;
                }

                float distance = MathUtils.getDistance(ship, t);
                float weight = 1f - (distance / falloffRange);
                weight *= Math.max(1f, 2f - (distance / blastRange));

                float shipStrength;
                switch (t.getHullSize()) {
                    default:
                    case DEFAULT:
                    case FIGHTER:
                        weight *= 1f;
                        shipStrength = 5f;
                        break;
                    case FRIGATE:
                        weight *= 1.75f;
                        shipStrength = 5f;
                        break;
                    case DESTROYER:
                        weight *= 1.5f;
                        shipStrength = 10f;
                        break;
                    case CRUISER:
                        weight *= 1.25f;
                        shipStrength = 15f;
                        break;
                    case CAPITAL_SHIP:
                        weight *= 1f;
                        shipStrength = 25f;
                        break;
                }

                FleetMemberAPI member = CombatUtils.getFleetMember(t);
                if (member != null) {
                    shipStrength = 0.1f + member.getFleetPointCost();
                }

                weight *= (float) Math.sqrt(shipStrength);
                weight *= (float) Math.sqrt(t.getCollisionRadius());

                totalTargetWeight += weight;
            }

            float decisionLevel = totalTargetWeight;
            decisionLevel *= 1f + ship.getFluxTracker().getFluxLevel();
            decisionLevel *= 1f - ship.getFluxTracker().getFluxLevel();
            decisionLevel *= II_Util.lerp(0.75f, 1.25f, ship.getHullLevel());
            if (flags.hasFlag(AIFlags.BACK_OFF) || flags.hasFlag(AIFlags.BACKING_OFF)) {
                decisionLevel *= 1.25f;
            }
            if (flags.hasFlag(AIFlags.DO_NOT_USE_FLUX)) {
                decisionLevel *= 0.5f;
            }

            if (decisionLevel >= decisionTarget) {
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
