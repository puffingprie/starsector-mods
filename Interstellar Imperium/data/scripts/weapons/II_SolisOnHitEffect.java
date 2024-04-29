package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class II_SolisOnHitEffect implements OnHitEffectPlugin {

    private static final int NUM_PARTICLES = 20;
    private static final Color PARTICLE_COLOR = new Color(255, 190, 0, 255);

    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit,
            ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        if (target == null || point == null) {
            return;
        }
        float speed = projectile.getVelocity().length();
        float facing = projectile.getFacing();
        for (int x = 0; x < NUM_PARTICLES; x++) {
            engine.addHitParticle(point, MathUtils.getPointOnCircumference(null,
                    MathUtils.getRandomNumberInRange(speed * 0.025f, speed * 0.10f),
                    MathUtils.getRandomNumberInRange(facing - 180f, facing + 180f)),
                    6f, 0.5f, MathUtils.getRandomNumberInRange(1f, 2f), PARTICLE_COLOR);
            engine.addHitParticle(MathUtils.getPointOnCircumference(point,
                    MathUtils.getRandomNumberInRange(0f, 50f),
                    MathUtils.getRandomNumberInRange(facing - 180f, facing + 180f)),
                    new Vector2f(), 4f, 1f, MathUtils.getRandomNumberInRange(0.5f, 2f), PARTICLE_COLOR);
        }
    }
}
