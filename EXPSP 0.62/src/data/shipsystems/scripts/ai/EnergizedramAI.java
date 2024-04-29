package data.shipsystems.scripts.ai;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class EnergizedramAI implements ShipSystemAIScript {
    /*
    private IntervalUtil timer = new IntervalUtil(0.3f,0.4f);
    float timeOfNextRefresh = 0;
    private ShipSystemAPI system;
    private ShipAPI ship;
    private ShipAPI victim;
    private float timeOfTargetAquisition;
    boolean retreating = false;

    private float getScore(ShipAPI self, ShipAPI victim) {
        if (!victim.isAlive()) return 0;

        return Math.max(0, (victim.getCollisionRadius() - 0) / MathUtils.getDistance(self, victim));
    }

    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.system = system;
    }

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
        float time = Global.getCombatEngine().getTotalElapsedTime(false);

        if (system.isActive() || timer.intervalElapsed()) {
            WeaponAPI drill = ship.getAllWeapons().get(2);

            // Can we get a better target?
            List<ShipAPI> candidates = WeaponUtils.getEnemiesInArc(drill);

            if (candidates.isEmpty()) timeOfTargetAquisition = time;

            float score = (victim == null || !candidates.contains(victim)) ? 0 : getScore(ship, victim);

            for (Object candidate : candidates) {
                ShipAPI newVictim = (ShipAPI) candidate;
                float newScore = getScore(ship, newVictim);

                if (newScore > 0 && newScore > score) {
                    victim = newVictim;
                    score = newScore;
                    timeOfTargetAquisition = time;
                }
            }


            // Nothing to kill...
            if(victim == null) return;

            boolean wantActive = (!drill.isDisabled() && score > 0 && time - timeOfTargetAquisition > 1)
                    || (system.isActive() && MathUtils.getDistance(ship, victim) < (ship.getCollisionRadius() + victim.getCollisionRadius()) * 2 + 500);

            // Prevent ship from strafing before activation.
            if (!system.isActive() && time != timeOfTargetAquisition) {
                ship.getVelocity().scale(0.9f);
            }

            if (system.isActive() && !wantActive) {
                ship.useSystem(); // Turn off
                victim = null;
            } else if (!system.isActive() && wantActive && ship.getFluxTracker().getFluxLevel() < 0.5f) {
                ship.useSystem(); // Turn on
            } else if (system.isActive()) {
                Vector2f to = victim.getLocation();

                float angleDif = MathUtils.getShortestRotation(ship.getFacing(), VectorUtils.getAngle(ship.getLocation(), to));
                ShipCommand direction = (angleDif > 0) ? ShipCommand.TURN_LEFT : ShipCommand.TURN_RIGHT;
                ship.giveCommand(direction, to, 0);
            }
        }
    }


     */

  private ShipSystemAPI system;
    private ShipAPI ship;
    private CombatEngineAPI engine;
    private ShipwideAIFlags flags;

    private IntervalUtil tracker = new IntervalUtil(0.5F, 1.0F);
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.flags = flags;
        this.engine = engine;
        this.system = system;
    }

    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
        if (!engine.isPaused()) {
            if (!system.isActive()) {
                tracker.advance(amount);
                if (tracker.intervalElapsed()) {
                    if (target == null) {
                        return;
                    }

                    if (ship.getShipTarget() == null) {
                        ship.setShipTarget(target);
                        return;
                    }

                    if (!target.isAlive()) {
                        return;
                    }

                    if (target.isFighter() || target.isDrone()) {
                        return;
                    }

                    if (!MathUtils.isWithinRange(ship, target, 200.0F) && !AIUtils.canUseSystemThisFrame(ship)) {
                        return;
                    }

                    if (ship.getFluxTracker().getFluxLevel() > 0.5F) {
                        return;
                    }

                    if (flags.hasFlag(ShipwideAIFlags.AIFlags.MANEUVER_TARGET) || flags.hasFlag(ShipwideAIFlags.AIFlags.PURSUING) || flags.hasFlag(ShipwideAIFlags.AIFlags.HARASS_MOVE_IN)) {
                        ship.useSystem();
                    }
                }
            }

        }
    }


}