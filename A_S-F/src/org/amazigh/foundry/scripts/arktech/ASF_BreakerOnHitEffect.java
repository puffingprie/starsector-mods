package org.amazigh.foundry.scripts.arktech;

import java.awt.Color;

import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;

public class ASF_BreakerOnHitEffect implements OnHitEffectPlugin {

	private static final Color COLOR_P = new Color(160,125,105,255);
    private static final Color COLOR_X = new Color(75,60,50,155);
	
	public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target,
					  Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {

		DamagingExplosionSpec blast = new DamagingExplosionSpec(0.2f,
                50f,
                30f,
                projectile.getDamageAmount()/2f,
                projectile.getDamageAmount()/4f,
                CollisionClass.PROJECTILE_FF,
                CollisionClass.PROJECTILE_FIGHTER,
                2f,
                6f,
                0.7f,
                32,
                COLOR_P,
                COLOR_X);
        blast.setDamageType(DamageType.HIGH_EXPLOSIVE);
        blast.setShowGraphic(false);
        
        engine.spawnDamagingExplosion(blast,projectile.getSource(),point,false);
        
        float timerRandom = MathUtils.getRandomNumberInRange(0.2f, 0.3f);
        Vector2f blastPoint = MathUtils.getRandomPointOnCircumference(point, MathUtils.getRandomNumberInRange(5f, 13f));
        
        Vector2f sfxVel = new Vector2f();
		if (target != null) {
			sfxVel.set(target.getVelocity());
		}
        engine.spawnExplosion(blastPoint, sfxVel, COLOR_X, 20f, timerRandom);
        
        
	}
}
