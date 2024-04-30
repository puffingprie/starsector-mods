package data.scripts.ix.weapons;

import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicLensFlare;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

import data.scripts.ix.ShapedExplosionUtil;

public class ThundermarkOnHit implements OnFireEffectPlugin, OnHitEffectPlugin {
	
	private static float EMP_ODDS = 0.25f;
	private static Color CORE_COLOR = new Color(200,200,255,255);
	private static Color EMP_BLUE = new Color(100,100,255,255);
	private static Color FRINGE_COLOR = new Color(0,100,255,200);
	
    public void onFire(DamagingProjectileAPI proj, WeaponAPI weapon, CombatEngineAPI engine) {
		Vector2f projLoc = MathUtils.getPointOnCircumference(proj.getLocation(), 7f, weapon.getCurrAngle());
		ShipAPI ship = weapon.getShip();
		
        engine.addHitParticle(
				projLoc,
				ship.getVelocity(),
				50.0f, //size
				1.0f, //brightness
				0.25f, //duration
				EMP_BLUE);
		
		engine.addHitParticle(
				projLoc,
				ship.getVelocity(),
				20.0f, //size
				2.5f, //brightness
				0.15f, //duration
				CORE_COLOR);
				
		if ((weapon.getBurstFireTimeRemaining() > 0.6f)) {
			float angle = weapon.getCurrAngle() - 180f;
			Vector2f loc = MathUtils.getPointOnCircumference(weapon.getLocation(), 7f, angle);
			Vector2f shipVelocity = weapon.getShip().getVelocity();
			float speed = (float) Math.sqrt(shipVelocity.lengthSquared());
			ShapedExplosionUtil.spawnShapedExplosion(loc, angle, speed, EMP_BLUE, "thundermark");
		}
    }

	public void onHit(DamagingProjectileAPI proj, CombatEntityAPI target, Vector2f point, boolean shieldHit, 
						ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
		
		if (target instanceof ShipAPI) {
			ShipAPI ship = (ShipAPI) target;
			float pierceChance = ship.getHardFluxLevel() - 0.1f;
			pierceChance *= ship.getMutableStats().getDynamic().getValue(Stats.SHIELD_PIERCED_MULT);
			if ((Math.random() < EMP_ODDS) && (!shieldHit || (float) Math.random() < pierceChance)) {
				engine.spawnEmpArcPierceShields(
						proj.getSource(), point, target, target,
						DamageType.ENERGY, 
						proj.getDamageAmount(), // damage
						proj.getEmpAmount(), // emp 
						100000f, // max range 
						"tachyon_lance_emp_impact",
						20f,
						FRINGE_COLOR,
						CORE_COLOR
						);
			}
		}
		
		MagicLensFlare.createSharpFlare(
				engine,
				proj.getSource(),
				point,
				7,		//thickness
				400,	//length
				0,		//angle
				FRINGE_COLOR,	//fringe
				EMP_BLUE	//core
			);
		
		Global.getSoundPlayer().playSound("starfall_mlrs_ix_hit", 1f, 1f, point, new Vector2f());
	}
}