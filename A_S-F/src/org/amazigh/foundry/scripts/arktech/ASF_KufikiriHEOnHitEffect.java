package org.amazigh.foundry.scripts.arktech;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;

public class ASF_KufikiriHEOnHitEffect implements OnHitEffectPlugin {

	private static final Color COLOR_P = new Color(255,210,150,240);
	private static final Color COLOR_X = new Color(175,70,70,255);
	
	public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target,
					  Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
		
		float blastDamage = projectile.getDamageAmount() * 0.75f;
		
		DamagingExplosionSpec blast = new DamagingExplosionSpec(0.2f,
                40f,
                20f,
                blastDamage,
                blastDamage/2f,
                CollisionClass.PROJECTILE_FF,
                CollisionClass.PROJECTILE_FIGHTER,
                4f,
                5f,
                1f,
                30,
                COLOR_P,
                COLOR_X);
        blast.setDamageType(DamageType.FRAGMENTATION);
		blast.setShowGraphic(true);
		blast.setUseDetailedExplosion(false);
        
        engine.spawnDamagingExplosion(blast,projectile.getSource(),point,false);
		
        Vector2f fxVel = new Vector2f();
		if (target != null) {
			fxVel.set(target.getVelocity());
		}
        
		Global.getSoundPlayer().playSound("system_canister_flak_explosion", 1.25f, 0.75f, point, fxVel); //"explosion_flak", 0.8f, 0.9f
	}
}
