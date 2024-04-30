package data.weapons.onHit;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import data.utils.FM_Colors;
import data.utils.FM_Misc;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class FM_Blade_ac_OnHitEffect implements OnHitEffectPlugin {

    private static final int NUM_PARTICLES = 10;

    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        float speed = projectile.getMoveSpeed();
        float facing = projectile.getFacing();

        engine.addHitParticle(point, FM_Misc.ZERO, 175f, 255f, 0.2f, FM_Colors.FM_BLUE_FLARE_CORE);

        if (shieldHit) {
            for (int x = 0; x < NUM_PARTICLES; x++) {
                engine.addSmoothParticle(point, MathUtils.getRandomPointOnCircumference(null, MathUtils.getRandomNumberInRange(speed * 0.07f, speed * 0.08f)), MathUtils.getRandomNumberInRange(10, 20), 200f, 1.5f, Color.CYAN);
            }
        }

    }
}
