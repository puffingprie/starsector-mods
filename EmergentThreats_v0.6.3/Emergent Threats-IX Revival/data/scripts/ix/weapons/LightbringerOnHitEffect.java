package data.scripts.ix.weapons;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;

public class LightbringerOnHitEffect implements OnHitEffectPlugin {

	public static float DAMAGE_BONUS = 10f;

	public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target,
					  Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
		if (target instanceof MissileAPI || (target instanceof ShipAPI && ((ShipAPI) target).isFighter())) {
			engine.applyDamage(target, point, DAMAGE_BONUS, DamageType.ENERGY, 0, false, false, projectile.getSource());	
		}
	}
}