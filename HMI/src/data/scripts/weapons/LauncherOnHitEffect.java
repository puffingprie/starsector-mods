package data.scripts.weapons;

import com.fs.starfarer.api.combat.*;
import org.lwjgl.util.vector.Vector2f;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;

import static com.fs.starfarer.api.combat.DamageType.HIGH_EXPLOSIVE;

public class LauncherOnHitEffect implements OnHitEffectPlugin {

    private static final float EXTRA_DAMAGE = 150f;


    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target,
                      Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        if (target instanceof ShipAPI && !shieldHit) {
            engine.applyDamage (target, point, EXTRA_DAMAGE, HIGH_EXPLOSIVE, 0f, false, false, projectile.getSource());
        }
    }
}
