package data.scripts.ai;

import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import java.util.ArrayList;
import java.util.List;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class II_ApocalypseAI3 extends II_BaseMissile {

    private static final float VELOCITY_DAMPING_FACTOR = 0.1f;
    private static final Vector2f ZERO = new Vector2f();

    public II_ApocalypseAI3(MissileAPI missile, ShipAPI launchingShip) {
        super(missile, launchingShip);
    }

    @Override
    public void advance(float amount) {
        if (missile.isFizzling() || missile.isFading()) {
            return;
        }

        missile.giveCommand(ShipCommand.ACCELERATE);

        if (!acquireTarget(amount)) {
            return;
        }

        float distance = MathUtils.getDistance(target.getLocation(), missile.getLocation());
        float acceleration = missile.getAcceleration();
        float maxSpeed = missile.getMaxSpeed();
        float guidance = 0.5f;
        if (missile.getSource() != null) {
            guidance += Math.min(missile.getSource().getMutableStats().getMissileGuidance().getModifiedValue()
                    - missile.getSource().getMutableStats().getMissileGuidance().getBaseValue(), 1f) * 0.5f;
        }
        Vector2f guidedTarget = interceptAdvanced(missile.getLocation(), missile.getVelocity().length(), acceleration,
                maxSpeed, target.getLocation(),
                target.getVelocity());
        if (guidedTarget == null) {
            Vector2f projection = new Vector2f(target.getVelocity());
            float scalar = distance / (missile.getVelocity().length() + 1f);
            projection.scale(scalar);
            guidedTarget = Vector2f.add(target.getLocation(), projection, null);
        }
        Vector2f.sub(guidedTarget, target.getLocation(), guidedTarget);
        guidedTarget.scale(guidance);
        Vector2f.add(guidedTarget, target.getLocation(), guidedTarget);

        float velocityFacing = VectorUtils.getFacing(missile.getVelocity());
        float absoluteDistance = MathUtils.getShortestRotation(velocityFacing, VectorUtils.getAngle(
                missile.getLocation(), guidedTarget));
        float angularDistance = MathUtils.getShortestRotation(missile.getFacing(), VectorUtils.getAngle(
                missile.getLocation(), guidedTarget));
        float compensationDifference = MathUtils.getShortestRotation(angularDistance, absoluteDistance);
        if (Math.abs(compensationDifference) <= 75f) {
            angularDistance += 0.5f * compensationDifference;
        }
        float absDAng = Math.abs(angularDistance);

        missile.giveCommand(angularDistance < 0 ? ShipCommand.TURN_RIGHT : ShipCommand.TURN_LEFT);

        if (absDAng < 5) {
            float MFlightAng = VectorUtils.getAngle(ZERO, missile.getVelocity());
            float MFlightCC = MathUtils.getShortestRotation(missile.getFacing(), MFlightAng);
            if (Math.abs(MFlightCC) > 20) {
                missile.giveCommand(MFlightCC < 0 ? ShipCommand.STRAFE_LEFT : ShipCommand.STRAFE_RIGHT);
            }
        }

        if (absDAng < Math.abs(missile.getAngularVelocity()) * VELOCITY_DAMPING_FACTOR) {
            missile.setAngularVelocity(angularDistance / VELOCITY_DAMPING_FACTOR);
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
