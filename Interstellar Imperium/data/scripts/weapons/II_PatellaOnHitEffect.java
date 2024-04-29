package data.scripts.weapons;

import java.awt.Color;
import org.lwjgl.util.vector.Vector2f;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class II_PatellaOnHitEffect implements OnHitEffectPlugin {

    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit,
            ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        if (target instanceof ShipAPI) {
            float emp = projectile.getEmpAmount();
            float dam = projectile.getDamageAmount();

            float pierceChance = ((ShipAPI) target).getHardFluxLevel() - 0.1f;
            pierceChance *= ((ShipAPI) target).getMutableStats().getDynamic().getValue(Stats.SHIELD_PIERCED_MULT);
            boolean piercedShield = shieldHit && ((float) Math.random() < pierceChance);

            if (piercedShield || !shieldHit) {
                engine.spawnEmpArcPierceShields(projectile.getSource(), point, target, target,
                        DamageType.ENERGY,
                        dam,
                        emp, // emp 
                        100000f, // max range 
                        "tachyon_lance_emp_impact",
                        20f, // thickness
                        new Color(25, 150, 255, 255),
                        new Color(255, 255, 255, 255)
                );
            }
        }
    }
}
