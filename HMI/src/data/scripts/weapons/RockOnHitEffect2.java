package data.scripts.weapons;

import com.fs.starfarer.api.combat.*;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;

import static com.fs.starfarer.api.combat.DamageType.FRAGMENTATION;

public class RockOnHitEffect2 implements OnHitEffectPlugin {

    private static final float MAX_EXTRA_DAMAGE = 75f;
    private static final float MIN_EXTRA_DAMAGE = 25f;

    @Override

    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target,
                      Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        if (target instanceof ShipAPI) {
            engine.applyDamage(target, point, MathUtils.getRandomNumberInRange(MIN_EXTRA_DAMAGE, MAX_EXTRA_DAMAGE), FRAGMENTATION, 0f, false, false, projectile.getSource());
        }
        if (target instanceof MissileAPI){
            engine.applyDamage(target, point, MathUtils.getRandomNumberInRange(MIN_EXTRA_DAMAGE, MAX_EXTRA_DAMAGE), FRAGMENTATION, 0f, false, false, projectile.getSource());
        }
    }
}
