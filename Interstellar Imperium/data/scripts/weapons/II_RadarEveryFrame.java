package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;

public class II_RadarEveryFrame implements EveryFrameWeaponEffectPlugin {

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (engine.isPaused() || (amount <= 0.00001f)) {
            return;
        }

        if (!weapon.getShip().isAlive()) {
            return;
        }

        float currAngle = weapon.getCurrAngle();
        float rotationSpeed;
        if (weapon.getShip().getShipTarget() != null) {
            float targetAngle = VectorUtils.getAngle(weapon.getLocation(), weapon.getShip().getShipTarget().getLocation());
            float deltaAngle = MathUtils.getShortestRotation(currAngle, targetAngle);
            float rotationSpeedLimit = Math.abs(deltaAngle) / amount;
            float maxRotationSpeed = 50f;
            float rotationSpeedFalloffStart = 25f;
            rotationSpeed = Math.min(maxRotationSpeed * Math.min(1f, Math.abs(deltaAngle) / rotationSpeedFalloffStart), rotationSpeedLimit) * Math.signum(deltaAngle);
        } else {
            rotationSpeed = -10f;
        }

        float newAngle = currAngle + (rotationSpeed * amount);
        weapon.setCurrAngle(newAngle);
    }
}
