package org.amazigh.foundry.scripts.arktech;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;

public class ASF_NaxdinOnHitEffect2 implements OnHitEffectPlugin {

	public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target,
					  Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
		
		if (target instanceof ShipAPI) {
			float dam = projectile.getDamageAmount() * 0.5f;
			float emp = projectile.getEmpAmount() * 1.5f;
			engine.spawnEmpArcPierceShields(projectile.getSource(), point, target, target,
					DamageType.ENERGY,
					dam, // damage
					emp, // emp
					1000f, // max range
					"tachyon_lance_emp_impact",
					13f, // thickness
					new Color(70,155,130,100), // 130,70,155
					new Color(225,255,255,110));
		}
	}
}
