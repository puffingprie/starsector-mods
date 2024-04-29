package data.scripts.ai;

import com.fs.starfarer.api.Global;
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

public class II_ApocalypseAI1 extends II_BaseMissile {

    private static final float MAX_TIME_UNTIL_BEGINS_WEAVING = 0.5f;

    private float flightTime;
    private final float flopOffset;
    private final float flopPeriod;
    private final float maxAngleOffTarget;
    private float timeUntilActive;
    private boolean turnRight;

    public II_ApocalypseAI1(MissileAPI missile, ShipAPI launchingShip) {
        super(missile, launchingShip);

        this.turnRight = (Math.random() > .5);
        this.timeUntilActive = (float) Math.random() * MAX_TIME_UNTIL_BEGINS_WEAVING;
        this.flightTime = 0f;
        this.flopPeriod = (float) Math.random() + 1f;
        this.flopOffset = (float) Math.random() * (float) Math.PI;
        this.maxAngleOffTarget = (float) Math.random() * 30f + 30f;
    }

    @Override
    public void advance(float amount) {
        if (Global.getCombatEngine().isPaused()) {
            return;
        }

        flightTime += amount;
        if (missile.isFading() || missile.isFizzling()) {
            return;
        }

        // This missile should always be accelerating
        missile.giveCommand(ShipCommand.ACCELERATE);

        // Don't start weaving for a while after launch (de-synchs missiles a bit)
        if (timeUntilActive > 0f) {
            timeUntilActive -= amount;
            return;
        }

        if (!acquireTarget(amount)) {
            return;
        }

        // Head towards the target, with target leading
        float distance = MathUtils.getDistance(target.getLocation(), missile.getLocation());
        float guidance = 0.5f;
        if (missile.getSource() != null) {
            guidance += Math.min(missile.getSource().getMutableStats().getMissileGuidance().getModifiedValue() -
            missile.getSource().getMutableStats().getMissileGuidance().getBaseValue(), 1f) * 0.5f;
        }
        Vector2f guidedTarget = intercept(missile.getLocation(), missile.getVelocity().length(), target.getLocation(),
                                          target.getVelocity());
        if (guidedTarget == null) {
            // If the target is unreachable, try to lead anyway
            Vector2f projection = new Vector2f(target.getVelocity());
            float scalar = distance / (missile.getVelocity().length() + 1f);
            projection.scale(scalar);
            guidedTarget = Vector2f.add(target.getLocation(), projection, null);
        }
        Vector2f.sub(guidedTarget, target.getLocation(), guidedTarget);
        guidedTarget.scale(guidance);
        Vector2f.add(guidedTarget, target.getLocation(), guidedTarget);

        float angularDistance = MathUtils.getShortestRotation(missile.getFacing(), VectorUtils.getAngle(
                                                              missile.getLocation(), guidedTarget));

        // Since this is a rapid-fire weapon, using a boolean for faster math
        turnRight = angularDistance < maxAngleOffTarget * (float) Math.sin((Math.PI / flopPeriod) * flightTime +
        flopOffset);

        //Damp angular velocity if we're getting close to the target angle
        if (target instanceof ShipAPI == true) {
            if (Math.abs(angularDistance) < Math.abs(missile.getAngularVelocity()) && distance <
                    target.getCollisionRadius() + 150f &&
                    ((ShipAPI) target).isFighter() || ((ShipAPI) target).isDrone()) {
                missile.setAngularVelocity(angularDistance);
            }
        } else {
            if (Math.abs(angularDistance) < Math.abs(missile.getAngularVelocity()) && distance <
                    target.getCollisionRadius() + 150f) {
                missile.setAngularVelocity(angularDistance);
            }
        }

        // Travel in a sort of sine wave pattern
        missile.giveCommand((turnRight ? ShipCommand.TURN_RIGHT : ShipCommand.TURN_LEFT));
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
