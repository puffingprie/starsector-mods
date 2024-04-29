package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import org.magiclib.util.MagicRender;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;


//Code courtesy of Tomatopaste

public class HMI_puckleeffect_alt implements EveryFrameWeaponEffectPlugin {
    private int firing = 1;

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {

        if (engine.isPaused() || weapon.getShip().getOriginalOwner() == -1) {
            return;
        }

        if (MagicRender.screenCheck(0.25f, weapon.getLocation())) {
            if (weapon.getChargeLevel() == 1) {

                if (weapon.isFiring()) {
                    firing = 1; //the weapon is firing, this is to check if the weapon does not cease to fire for more than one frame

                    float maxSpread = weapon.getSpec().getMaxSpread();
                    float minSpread = weapon.getSpec().getMinSpread();
                    float currSpread = weapon.getCurrSpread();

                    float ratio = (currSpread - minSpread) / (maxSpread - minSpread);
                    ratio = Math.min(Math.max(0f, ratio), 1f);
                    float invRatio = 1f - ratio;

                    maxSpread *= 0.5f;
//                    float angle = (invRatio * maxSpread) - maxSpread * 0.5f;
                    float angle = (invRatio * maxSpread);
                    float random = (float) (Math.random()) * 2f - 1f;
                    angle *= random;

                    float facing = weapon.getCurrAngle() + angle;

                    //float angle = (float) ((Math.random()-0.5) * 2 * spread);
                    //angle = (angle * 2f) - weapon.getSpec().getMaxSpread();

                    Vector2f loc = MathUtils.getRandomPointInCircle(weapon.getLocation(), 5);
                    //float facing = weapon.getArcFacing() + angle;

                    engine.spawnProjectile(weapon.getShip(), weapon, "krandlpucklegun_perfect", loc, facing, weapon.getShip().getVelocity());
                    //engine.spawnProjectile(weapon.getShip(), weapon, "krandlpucklegun", loc, weapon.getArcFacing() * angle, weapon.getShip().getVelocity());
                    }
                } else if (firing > 0) {
                    firing--;
                }
            }
        }
    }
