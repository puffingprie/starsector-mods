package data.scripts.ix.weapons;

import java.awt.Color;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

import data.scripts.ix.DistanceUtil;
import data.scripts.ix.ShapedExplosionUtil;

public class VampyrShotOnFire implements OnFireEffectPlugin {

	private static float BOLT_BASE_DAMAGE = 1000f;
	private static float BOLT_BASE_EMP = 1000f;
	private static float BOLT_RANGE = 1200f;
	private static int BOLT_COUNT = 3;
	
	public void onFire(DamagingProjectileAPI proj, WeaponAPI weapon, CombatEngineAPI engine) {
		proj.setDamageAmount(0); //damage dealt from spawnEMP only
		ShipAPI thisShip = proj.getSource();
		//target is projectile target, if no target get closes ship, if no unphased ships nearby
		//get closest phased ship for deco bolts, if still no ship set target to self to avoid null
		CombatEntityAPI target = proj.getDamageTarget();
		boolean isOnlyPhaseInRange = false;
		if (target == null) target = DistanceUtil.getNearestEnemy(proj, BOLT_RANGE);
		if (target == null) {
			isOnlyPhaseInRange = true;
			target = DistanceUtil.getNearestPhaseEnemy(proj, BOLT_RANGE);
		}
		if (target == null) target = proj;
			
		float missileDamageMult = thisShip.getMutableStats().getMissileWeaponDamageMult().getMult();
		float damage = BOLT_BASE_DAMAGE * missileDamageMult;
		float empDamage = BOLT_BASE_EMP * missileDamageMult;
		boolean[] isPierce = { false, false, false };
		proj.getVelocity().set(0f, 0f);
		Vector2f spawnPoint = new Vector2f(proj.getLocation());
		
		if (target instanceof ShipAPI && !isOnlyPhaseInRange) {
			ShipAPI hitShip = (ShipAPI) target;
			float pierceChance = hitShip.getHardFluxLevel() - 0.1f;
			pierceChance *= hitShip.getMutableStats().getDynamic().getValue(Stats.SHIELD_PIERCED_MULT);
			for (int i = 0; i < BOLT_COUNT; i++) {
				if (Math.random() < pierceChance) isPierce[i] = true;
				else isPierce[i] = false;
				spawnEMP(thisShip, spawnPoint, proj, hitShip, damage, empDamage, engine, isPierce[i]);
			}
		}
		
		//fire deco bolts in a narrow cone pointed at nearest phased enemy if only phased ships in range
		else if (target instanceof ShipAPI && isOnlyPhaseInRange) {
			float spawnX = spawnPoint.getX();
			float spawnY = spawnPoint.getY();
			float targetX = target.getLocation().getX();
			float targetY = target.getLocation().getY();
			float angleDiff = 0.2f;
			float angle = (float) Math.atan2(targetY - spawnY, targetX - spawnX) - angleDiff;
			float distance = 400f;
			for (int i = 0; i < BOLT_COUNT; i++) {
				float x = spawnX + (float) (distance * Math.cos(angle));
				float y = spawnY + (float) (distance * Math.sin(angle));
				Vector2f endPoint = new Vector2f(x, y);
				spawnEMPDeco(spawnPoint, endPoint, proj, engine);
				angle += angleDiff; //radians
			}
		}
		
		ShapedExplosionUtil.spawnShapedExplosion(
				proj.getLocation(), 
				proj.getFacing(), 
				-200f,
				new Color(50,255,50,200),
				false);
		
		Global.getSoundPlayer().playSound("vampyr_ix_emp", 1f, 1f, proj.getLocation(), new Vector2f());
		engine.applyDamage(proj, proj.getLocation(), 10000f, DamageType.ENERGY, 0, false, false, proj);
	}
	
	private void spawnEMP (ShipAPI ship, Vector2f point, CombatEntityAPI source, CombatEntityAPI target, float damage, float empDamage, CombatEngineAPI engine, boolean isPierce) {
	
		if (isPierce) {
			engine.spawnEmpArcPierceShields(
								ship, point, source, target,
								DamageType.ENERGY, 
								damage, 
								empDamage,
								10000f, // max range 
								"",
								20f, // thickness
								new Color(50,255,50,200), // fringe
								new Color(200,255,200,180) // core color
								);
		}
		else {
			engine.spawnEmpArc(
								ship, point, source, target,
								DamageType.ENERGY, 
								damage, 
								empDamage,
								10000f, // max range 
								"",
								20f, // thickness
								new Color(50,255,50,200), // fringe
								new Color(200,255,200,180) // core color
								);
		}
	}
	
	private void spawnEMPDeco (Vector2f from, Vector2f to, CombatEntityAPI anchor, CombatEngineAPI engine) {
		engine.spawnEmpArcVisual(
								from, 
								anchor, //from anchor
								to, 
								anchor, //to anchor 
								20f, // thickness
								new Color(50,255,50,200), // fringe
								new Color(200,255,200,180) // core color
								);
	}
}