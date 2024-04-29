package data.scripts.ai;

import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.util.IntervalUtil;
import java.util.ArrayList;
import java.util.List;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class II_ApocalypseAI2 extends II_BaseMissile {

    private static final Vector2f ZERO = new Vector2f();

    private final float baseofftarget;
    private float offtarget;
    private final IntervalUtil timer = new IntervalUtil(0.1f, 0.2f);

    public II_ApocalypseAI2(MissileAPI missile, ShipAPI launchingShip) {
        super(missile, launchingShip);

        this.offtarget = 30f * (0.5f - (float) Math.random());
        this.baseofftarget = 30f * (0.5f - (float) Math.random());
    }

    @Override
    public void advance(float amount) {
        timer.advance(amount);

        if (missile.isFading() || missile.isFizzling()) {
            return;
        }

        if (!acquireTarget(amount)) {
            missile.giveCommand(ShipCommand.ACCELERATE);
            return;
        }

        float angularDistance = MathUtils.getShortestRotation(missile.getFacing(), VectorUtils.getAngle(
                                                              missile.getLocation(), target.getLocation()));

        if (timer.intervalElapsed()) {
            offtarget = (offtarget > 0 ? offtarget - 1 : offtarget + 1);
        }

        float distance = MathUtils.getDistance(target.getLocation(), missile.getLocation());
        float offtargetby = (0f + (offtarget + (baseofftarget * target.getCollisionRadius() / 75f)));

        // Make it slightly more accurate
        if (distance <= target.getCollisionRadius() * 2f) {
            offtargetby *= (distance - target.getCollisionRadius() * 1.5f) / target.getCollisionRadius() + 0.5f;
        }

        float AbsAngD = Math.abs(angularDistance - offtargetby);

        // Point towards target
        if (AbsAngD > 0.5) {
            // Makes missile fly off target
            missile.giveCommand(angularDistance > offtargetby ? ShipCommand.TURN_LEFT : ShipCommand.TURN_RIGHT);
        }

        // Course correction
        if (AbsAngD < 5) {
            float MFlightAng = VectorUtils.getAngle(ZERO, missile.getVelocity());
            float MFlightCC = MathUtils.getShortestRotation(missile.getFacing(), MFlightAng);
            if (Math.abs(MFlightCC) > 20) {
                missile.giveCommand(MFlightCC < 0 ? ShipCommand.STRAFE_LEFT : ShipCommand.STRAFE_RIGHT);
            }
        }

        missile.giveCommand(ShipCommand.ACCELERATE);

        if (AbsAngD < 0.4) {
            missile.setAngularVelocity(0);
        }
    }

    @Override
    protected ShipAPI findBestTarget() {
        List<ShipAPI> ships = AIUtils.getEnemiesOnMap(missile);
        int size = ships.size();
        List<ShipAPI> enemies = new ArrayList<>(size / 2);
        for (int i = 0; i < size; i++) {
            ShipAPI tmp = ships.get(i);
            if (!isTargetValid(tmp)) {
                continue;
            }
            enemies.add(tmp);
        }
        if (enemies.isEmpty()) {
            return null;
        }
        return enemies.get((int) (Math.random() * enemies.size()));
    }

    @Override
    protected boolean isTargetValid(CombatEntityAPI target) {
        if (target instanceof ShipAPI) {
            ShipAPI ship = (ShipAPI) target;
            if (ship.isFighter() || ship.isDrone() || (ship.getOwner() == 100)) {
                return false;
            }
        }
        return super.isTargetValid(target);
    }

    @Override
    protected boolean acquireTarget(float amount) {
        if (!isTargetValidAlternate(target)) {
            if (target instanceof ShipAPI) {
                ShipAPI ship = (ShipAPI) target;
                if (ship.isPhased() && ship.isAlive()) {
                    return false;
                }
            }
            setTarget(findBestTarget());
            if (target == null) {
                return false;
            }
        }
        return true;
    }

    protected boolean isTargetValidAlternate(CombatEntityAPI target) {
        return super.isTargetValid(target);
    }
}
