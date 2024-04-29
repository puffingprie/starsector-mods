package data.scripts.weapons;

import com.fs.starfarer.api.AnimationAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import data.scripts.everyframe.II_Trails;
import java.awt.Color;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

public class II_TelumCannonEveryFrame implements EveryFrameWeaponEffectPlugin {

    private static final Color MUZZLE_FLASH_COLOR = new Color(180, 180, 255, 255);
    private static final float MUZZLE_FLASH_DURATION = 0.15f;
    private static final float MUZZLE_FLASH_SIZE = 20.0f;
    private static final Vector2f MUZZLE_OFFSET_HARDPOINT = new Vector2f(31.5f, 0f);
    private static final Vector2f MUZZLE_OFFSET_TURRET = new Vector2f(29.5f, 0f);

    private static final float FRAME_RATE = 30f;
    private static final float MIN_FRAME_RATE = 15f;

    private float lastChargeLevel = 0.0f;
    private float lastCooldownRemaining = 0.0f;

    private float charge = 0f;
    private float cooldown = 0f;
    private float delay = 1f / MIN_FRAME_RATE;
    private int frame = 0;
    private int startFrame = 0;
    private float timer = 0f;

    private boolean firing = false;
    private boolean spinning = false;

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (engine.isPaused()) {
            return;
        }

        II_Trails.createIfNeeded();

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

        if ((lastCooldownRemaining < cooldownRemaining) || (chargeLevel >= 1f && lastChargeLevel < 1f)) {
            engine.spawnExplosion(muzzleLocation, shipVelocity, MUZZLE_FLASH_COLOR, MUZZLE_FLASH_SIZE, MUZZLE_FLASH_DURATION);
            engine.addSmoothParticle(muzzleLocation, shipVelocity, MUZZLE_FLASH_SIZE * 3f, 1f, MUZZLE_FLASH_DURATION * 2f, MUZZLE_FLASH_COLOR);
        }

        lastChargeLevel = chargeLevel;
        lastCooldownRemaining = cooldownRemaining;

        if (weapon.getSlot().isHidden()) {
            return;
        }

        AnimationAPI theAnim = weapon.getAnimation();

        float spinDown = 0.004f;
        float mult = ship.getMutableStats().getBallisticRoFMult().getModifiedValue();

        float minDelay = 1f / (FRAME_RATE * mult);
        float maxDelay = 1f / MIN_FRAME_RATE;
        int maxFrame = theAnim.getNumFrames();

        if (weapon.getChargeLevel() > 0f && (weapon.getChargeLevel() > charge || weapon.getChargeLevel() >= 1f)) {
            if (!firing) {
                if (frame == 0 || frame > 12) {
                    startFrame = 0;
                } else if (frame <= 6) {
                    startFrame = 6;
                } else {
                    startFrame = 12;
                }
            }
            cooldown = weapon.getCooldownRemaining();
            delay = minDelay;
            firing = true;
            spinning = true;
        } else {
            firing = false;
        }

        if (firing) {
            if (weapon.getCooldownRemaining() > cooldown) {
                if (frame == 0 || frame > 12) {
                    startFrame = 0;
                } else if (frame <= 6) {
                    startFrame = 6;
                } else {
                    startFrame = 12;
                }
            }

            float x = 1f - (weapon.getCooldownRemaining() / weapon.getCooldown());
            frame = startFrame + (int) Math.floor(x * 6);
            if (frame == maxFrame) {
                frame = 0;
            }
        } else {
            timer += amount;
            while (timer >= delay) {
                timer -= delay;

                if (delay >= maxDelay) {
                    delay = maxDelay;
                    spinning = false;
                } else {
                    delay += spinDown;
                }

                if (spinning || (frame != 0 && frame != 6 && frame != 12)) {
                    frame++;
                    if (frame == maxFrame) {
                        frame = 0;
                    }
                }
            }
        }

        theAnim.setFrameRate(0f);
        theAnim.setFrame(frame);

        charge = weapon.getChargeLevel();
        cooldown = weapon.getCooldownRemaining();
    }
}
