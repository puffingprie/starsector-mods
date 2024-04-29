package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import java.awt.Color;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

public class II_ERParticleCannonEveryFrame implements EveryFrameWeaponEffectPlugin {

    private static final Color MUZZLE_FLASH_COLOR = new Color(255, 150, 50, 75);
    private static final float MUZZLE_FLASH_DURATION = 0.1f;
    private static final float MUZZLE_FLASH_SIZE = 10.0f;
    private static final Vector2f MUZZLE_OFFSET_HARDPOINT = new Vector2f(14.75f, 0f);
    private static final Vector2f MUZZLE_OFFSET_TURRET = new Vector2f(14f, 0f);

    private float lastChargeLevel = 0.0f;
    private float lastCooldownRemaining = 0.0f;
    private boolean shot = false;

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (engine.isPaused()) {
            return;
        }

        float chargeLevel = weapon.getChargeLevel();
        float cooldownRemaining = weapon.getCooldownRemaining();

        Vector2f weaponLocation = weapon.getLocation();
        ShipAPI ship = weapon.getShip();
        float weaponFacing = weapon.getCurrAngle();
        Vector2f shipVelocity = ship.getVelocity();
        Vector2f offset;
        if (weapon.getSlot().isHardpoint()) {
            offset = new Vector2f(MUZZLE_OFFSET_HARDPOINT);
        } else {
            offset = new Vector2f(MUZZLE_OFFSET_TURRET);
        }
        VectorUtils.rotate(offset, weaponFacing, offset);
        Vector2f muzzleLocation = Vector2f.add(weaponLocation, offset, new Vector2f());

        if (!shot && ((lastCooldownRemaining < cooldownRemaining) || ((chargeLevel >= 1f) && (lastChargeLevel < 1f)))) {
            shot = true;
            engine.spawnExplosion(muzzleLocation, shipVelocity, MUZZLE_FLASH_COLOR, MUZZLE_FLASH_SIZE, MUZZLE_FLASH_DURATION);
        } else {
            shot = false;
        }

        lastChargeLevel = chargeLevel;
        lastCooldownRemaining = cooldownRemaining;
    }
}
