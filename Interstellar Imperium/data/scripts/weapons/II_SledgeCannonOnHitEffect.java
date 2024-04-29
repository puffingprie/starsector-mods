package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class II_SledgeCannonOnHitEffect implements OnHitEffectPlugin {

    private static final Color EXPLOSION_COLOR = new Color(100, 100, 240, 200);
    private static final float EXTRA_DAMAGE_CHANCE = 0.5f;
    private static final float MAX_EXTRA_DAMAGE = 60f;
    private static final float MIN_EXTRA_DAMAGE = 60f;
    private static final int NUM_PARTICLES = 20;
    private static final Color PARTICLE_COLOR = new Color(50, 50, 200, 125);
    private static final String SOUND_ID = "ii_sledgecannon_crit";

    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit,
            ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        if (target == null || point == null) {
            return;
        }
        if (!shieldHit) {
            float scale;
            if ((float) Math.random() <= EXTRA_DAMAGE_CHANCE) {
                engine.applyDamage(projectile, target, point, MathUtils.getRandomNumberInRange(MIN_EXTRA_DAMAGE, MAX_EXTRA_DAMAGE),
                        DamageType.HIGH_EXPLOSIVE, 0f, false, false, projectile.getSource(), false);
                engine.spawnExplosion(point, new Vector2f(target.getVelocity().x * 0.45f, target.getVelocity().y * 0.45f), EXPLOSION_COLOR, 60f, 1f);
                Global.getSoundPlayer().playSound(SOUND_ID, 1f, 1f, target.getLocation(), target.getVelocity());
                scale = 1.25f;
            } else {
                scale = 0.75f;
            }
            float speed = projectile.getVelocity().length() * scale;
            float facing = projectile.getFacing();
            for (int x = 0; x < NUM_PARTICLES * scale; x++) {
                engine.addHitParticle(point, MathUtils.getPointOnCircumference(null, MathUtils.getRandomNumberInRange(speed * .025f, speed * .10f),
                        MathUtils.getRandomNumberInRange(facing - 180f, facing + 180f)), 6f, 1f,
                        MathUtils.getRandomNumberInRange(0.5f, 1.5f) * scale, PARTICLE_COLOR);
            }
        }
    }
}
