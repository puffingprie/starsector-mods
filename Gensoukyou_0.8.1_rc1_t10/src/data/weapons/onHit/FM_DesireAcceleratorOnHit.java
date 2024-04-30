package data.weapons.onHit;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import data.utils.FM_Colors;
import data.utils.FM_Misc;
import org.lwjgl.util.vector.Vector2f;

public class FM_DesireAcceleratorOnHit implements OnHitEffectPlugin {
    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {

        float emp = projectile.getEmpAmount();
        float dam = projectile.getDamageAmount();

        engine.addHitParticle(point, FM_Misc.ZERO, 125f, 255f, 0.2f, FM_Colors.FM_BLUE_FLARE_CORE);

        if (shieldHit && target instanceof ShipAPI) {
            if (Math.random() * 3f <= ((ShipAPI) target).getFluxTracker().getHardFlux() / ((ShipAPI) target).getMaxFlux()) {
                engine.spawnEmpArcPierceShields(
                        projectile.getSource(),
                        point,
                        target,
                        target,
                        DamageType.ENERGY,
                        dam,
                        emp,
                        100000f,
                        "tachyon_lance_emp_impact",
                        20f,
                        FM_Colors.FM_BLUE_FLARE_FRINGE,
                        FM_Colors.FM_BLUE_FLARE_CORE
                );
            }
        } else if (Math.random() > 0.67f && target instanceof ShipAPI) {
            engine.spawnEmpArc(projectile.getSource(), point, target, target,
                    DamageType.ENERGY,
                    dam,
                    emp, // emp
                    100000f, // max range
                    "tachyon_lance_emp_impact",
                    20f, // thickness
                    FM_Colors.FM_BLUE_FLARE_FRINGE,
                    FM_Colors.FM_BLUE_FLARE_CORE
            );

            //engine.spawnProjectile(null, null, "plasma", point, 0, new Vector2f(0, 0));
        }
    }
}
