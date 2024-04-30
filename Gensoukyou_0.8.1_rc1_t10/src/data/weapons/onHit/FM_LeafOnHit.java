package data.weapons.onHit;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import data.utils.FM_Colors;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

import java.util.List;

public class FM_LeafOnHit implements OnHitEffectPlugin {

    public static final float EMP_ARC_RANGE = 150f;


    private boolean EMP_ON = true;

    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        if (((target instanceof ShipAPI && ((ShipAPI) target).isFighter())
                || target instanceof MissileAPI)
                && target.getOwner() != projectile.getOwner()
                && !shieldHit) {
            List<CombatEntityAPI> empArcTarget = CombatUtils.getEntitiesWithinRange(target.getLocation(), EMP_ARC_RANGE);
            if (empArcTarget.isEmpty()) return;

            if (EMP_ON) {

                for (CombatEntityAPI entity : empArcTarget) {
                    if (entity.isExpired()) continue;
                    if (entity.getOwner() == projectile.getOwner()) continue;
                    if (!EMP_ON) continue;
                    if (entity == target) continue;
                    if ((entity instanceof MissileAPI || entity instanceof ShipAPI)) {
                        if (projectile.getSource() == null) continue;
                        if (projectile.getWeapon() == null) continue;
                        EMP_ON = false;
                        engine.spawnEmpArc(projectile.getSource(), point, entity, entity,
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
        }
    }
}
