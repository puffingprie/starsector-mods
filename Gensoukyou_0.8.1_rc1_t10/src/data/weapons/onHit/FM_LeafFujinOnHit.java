package data.weapons.onHit;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import data.utils.FM_Colors;
import org.lwjgl.util.vector.Vector2f;

public class FM_LeafFujinOnHit implements OnHitEffectPlugin {

    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        if (!(target instanceof ShipAPI)) return;
        if (!shieldHit) return;
        float empChanceBase;
        ShipAPI ship = (ShipAPI) target;
        float fluxLevel = ship.getFluxTracker().getFluxLevel();
        empChanceBase = fluxLevel / 0.75f;
        if (empChanceBase >= 1f) {
            empChanceBase = 1f;
        }
        if (Math.random() <= empChanceBase * 0.25f) {
            engine.spawnEmpArcPierceShields(
                    projectile.getSource(),
                    point,
                    ship,
                    ship,
                    DamageType.ENERGY,
                    projectile.getDamageAmount(),
                    projectile.getEmpAmount(),
                    10000f,
                    "tachyon_lance_emp_impact",
                    10f,
                    FM_Colors.FM_GREEN_EMP_FRINGE,
                    FM_Colors.FM_GREEN_EMP_CORE
            );
        }
    }
}
