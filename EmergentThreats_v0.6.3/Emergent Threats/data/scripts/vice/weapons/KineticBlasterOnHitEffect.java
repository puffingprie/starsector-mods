package data.scripts.vice.weapons;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.util.Misc;

public class KineticBlasterOnHitEffect implements OnHitEffectPlugin {

	public static float DAMAGE_BONUS = 50f;

	public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target,
					  Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
		if (shieldHit && target instanceof ShipAPI) {
			engine.applyDamage(target, point, DAMAGE_BONUS, DamageType.KINETIC, 0, false, false, projectile.getSource());			
			engine.addFloatingDamageText(point, DAMAGE_BONUS, Misc.FLOATY_SHIELD_DAMAGE_COLOR, target, projectile.getSource());
		}
	}
}