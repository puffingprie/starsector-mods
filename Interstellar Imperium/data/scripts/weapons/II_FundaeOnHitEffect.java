package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import data.scripts.ai.II_FundaeAI;
import org.lwjgl.util.vector.Vector2f;

public class II_FundaeOnHitEffect implements OnHitEffectPlugin {
    
    public static final float FUNDAE_FRAG_DAMAGE_MULT = 4.375f;

    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit,
            ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        if (target == null || point == null || !(projectile instanceof MissileAPI)) {
            return;
        }
        Global.getCombatEngine().applyDamage(projectile, target, projectile.getLocation(), projectile.getDamageAmount() * FUNDAE_FRAG_DAMAGE_MULT,
                DamageType.FRAGMENTATION, 0f, false, false, projectile.getSource(), false);
        II_FundaeAI.explode((MissileAPI) projectile, target, projectile.getLocation(), Global.getCombatEngine());
    }
}
