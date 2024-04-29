package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import data.scripts.shipsystems.II_MagnumSalvoStats;
import org.lwjgl.util.vector.Vector2f;

public class II_ArmageddonEliteOnHitEffect implements OnHitEffectPlugin {

    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit,
            ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        if (target instanceof ShipAPI) {
            ShipAPI ship = (ShipAPI) target;
            float pierceChance = (ship.getHardFluxLevel() * 0.5f) - 0.1f;
            pierceChance *= ship.getMutableStats().getDynamic().getValue(Stats.SHIELD_PIERCED_MULT);

            boolean piercedShield = shieldHit && (float) Math.random() < pierceChance;
            if (!shieldHit || piercedShield) {
                float emp = projectile.getEmpAmount();
                float dam = projectile.getDamageAmount();

                engine.spawnEmpArcPierceShields(projectile.getSource(), point, target, target,
                        DamageType.ENERGY,
                        dam,
                        emp, // emp 
                        100000f, // max range 
                        "tachyon_lance_emp_impact",
                        40f, // thickness
                        II_MagnumSalvoStats.JITTER_COLOR_ELITE,
                        II_MagnumSalvoStats.GLOW_COLOR_ELITE
                );
            }
        }
    }
}
