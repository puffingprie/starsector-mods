package data.weapons.beam;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class FM_DollsLanceBeamEffect implements BeamEffectPlugin {
    public static final Color EXP_COLOR = new Color(255, 50, 50, 216);

    private boolean EXPLOSION = true;

    @Override
    public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
        Vector2f hit_loc = beam.getTo();
        CombatEntityAPI target = beam.getDamageTarget();
        if (EXPLOSION && target != null) {
            engine.spawnExplosion(hit_loc, new Vector2f(), EXP_COLOR, 30f, 0.3f);
            DamagingProjectileAPI explosion = engine.spawnDamagingExplosion(createExplosionSpec(), beam.getSource(), hit_loc);
            explosion.addDamagedAlready(target);

            EXPLOSION = false;
        }
    }

    public DamagingExplosionSpec createExplosionSpec() {
        float damage = 50f;
        DamagingExplosionSpec spec = new DamagingExplosionSpec(
                0.1f, // duration
                30f, // radius
                30f, // coreRadius
                damage, // maxDamage
                damage / 2f, // minDamage
                CollisionClass.PROJECTILE_FF, // collisionClass
                CollisionClass.PROJECTILE_FIGHTER, // collisionClassByFighter
                3f, // particleSizeMin
                3f, // particleSizeRange
                0.5f, // particleDuration
                30, // particleCount
                Color.WHITE, // particleColor
                EXP_COLOR  // explosionColor
        );

        spec.setDamageType(DamageType.FRAGMENTATION);
        spec.setUseDetailedExplosion(false);
        spec.setSoundSetId("hit_glancing_energy");
        return spec;
    }

}
