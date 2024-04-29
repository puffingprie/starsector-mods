package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import data.scripts.everyframe.II_WeaponScriptPlugin;
import java.awt.Color;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

public class II_PhotonBlasterEveryFrame implements EveryFrameWeaponEffectPlugin {

    private static final Color MUZZLE_FLASH_COLOR = new Color(255, 175, 50, 255);
    private static final float MUZZLE_FLASH_DURATION = 0.05f;
    private static final float MUZZLE_FLASH_SIZE = 30.0f;
    private static final Vector2f MUZZLE_OFFSET_HARDPOINT = new Vector2f(12f, 0f);
    private static final Vector2f MUZZLE_OFFSET_TURRET = new Vector2f(12f, 0f);

    private float lastChargeLevel = 0.0f;
    private float lastCooldownRemaining = 0.0f;
    private boolean shot = false;

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (engine.isPaused()) {
            return;
        }

        II_WeaponScriptPlugin.createIfNeeded();

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

        if ((weapon.getShip().getSystem() != null) && !weapon.getShip().getSystem().isActive()) {
            if (!shot && ((lastCooldownRemaining < cooldownRemaining) || ((chargeLevel >= 1f) && (lastChargeLevel < 1f)))) {
                shot = true;
                engine.spawnExplosion(muzzleLocation, shipVelocity, MUZZLE_FLASH_COLOR, MUZZLE_FLASH_SIZE,
                        MUZZLE_FLASH_DURATION);
                engine.addSmoothParticle(muzzleLocation, shipVelocity, MUZZLE_FLASH_SIZE * 3f, 1f, MUZZLE_FLASH_DURATION
                        * 3f, MUZZLE_FLASH_COLOR);
            } else {
                shot = false;
            }
        } else {
            shot = false;
        }

        lastChargeLevel = chargeLevel;
        lastCooldownRemaining = cooldownRemaining;
    }
}
