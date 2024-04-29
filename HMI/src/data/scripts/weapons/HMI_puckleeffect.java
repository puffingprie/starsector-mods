package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import org.magiclib.util.MagicRender;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;


//Code courtesy of Tomatopaste - Voracious Code

public class HMI_puckleeffect implements OnFireEffectPlugin {
    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        float maxSpread = weapon.getSpec().getMaxSpread();
        float minSpread = weapon.getSpec().getMinSpread();
        float currSpread = weapon.getCurrSpread();

        float ratio = (currSpread - minSpread) / (maxSpread - minSpread);
        ratio = Math.min(Math.max(0f, ratio), 1f);
        float invRatio = 1f - ratio;

        maxSpread *= 0.5f;
//        float angle = (invRatio * maxSpread) - maxSpread * 0.5f;
        float angle = (invRatio * maxSpread);
        float random = (float) (Math.random()) * 2f - 1f;
        angle *= random;

        float facing = weapon.getCurrAngle() + angle;

//        float projVelDir = VectorUtils.getFacing(projectile.getVelocity());
//        VectorUtils.rotate(projectile.getVelocity(), MathUtils.clampAngle(facing - projVelDir), projectile.getVelocity());
        projectile.setFacing(facing);
    }
}
