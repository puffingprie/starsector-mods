package data.weapons.onHit;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import data.utils.FM_Colors;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class FM_WolfsFangOnHit implements OnHitEffectPlugin {

    public static final float EMP_ARC_P = 0.33f;

    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        if (shieldHit) {
            if (!(target instanceof ShipAPI)) return;
            if (!((ShipAPI) target).isAlive()) return;
            if (projectile.getSource() == null) return;
            float p = MathUtils.getRandomNumberInRange(0f, 1f);
            if (p <= EMP_ARC_P) {
                engine.spawnEmpArcPierceShields(
                        projectile.getSource(),
                        point,
                        target,
                        target,
                        DamageType.FRAGMENTATION,
                        projectile.getDamageAmount(),
                        projectile.getEmpAmount(),
                        10000f,
                        "tachyon_lance_emp_impact",
                        14f,
                        FM_Colors.FM_RED_EMP_FRINGE,
                        FM_Colors.FM_RED_EMP_CORE

                );

            }
        }
    }
}
