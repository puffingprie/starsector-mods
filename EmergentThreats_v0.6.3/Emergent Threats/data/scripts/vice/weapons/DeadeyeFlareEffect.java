package data.scripts.vice.weapons;

import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;

import data.scripts.vice.ShapedExplosionUtil;

public class DeadeyeFlareEffect implements OnFireEffectPlugin {

	private static Color FLARE_COLOR = new Color(255,35,0,200);
	
	public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
		float angle = weapon.getCurrAngle() - 180f;
		Vector2f loc = MathUtils.getPointOnCircumference(weapon.getLocation(), 7f, angle);
		Vector2f shipVelocity = weapon.getShip().getVelocity();
		float speed = (float) Math.sqrt(shipVelocity.lengthSquared());
		ShapedExplosionUtil.spawnShapedExplosion(loc, angle, speed, FLARE_COLOR, "deadeye");
	}
}