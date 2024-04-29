package data.scripts.ai;

import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;

public class II_BallistaStage2AI extends II_BaseMissile {

    public II_BallistaStage2AI(MissileAPI missile, ShipAPI launchingShip) {
        super(missile, launchingShip);
    }

    @Override
    public void advance(float amount) {
        if (missile.isFading() || missile.isFizzling()) {
            return;
        }

        if (!acquireTarget(amount)) {
            missile.giveCommand(ShipCommand.ACCELERATE);
            return;
        }

        float angularDistance = MathUtils.getShortestRotation(missile.getFacing(), VectorUtils.getAngleStrict(
                                                              missile.getLocation(), target.getLocation()));

        float absAngD = Math.abs(angularDistance);

        // Point towards target
        if (absAngD > 0.5) {
            // Makes missile fly off target
            missile.giveCommand(angularDistance > 0 ? ShipCommand.TURN_LEFT : ShipCommand.TURN_RIGHT);
        }

        missile.giveCommand(ShipCommand.ACCELERATE);

        if (absAngD < 0.4) {
            missile.setAngularVelocity(0);
        }
    }
}
