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

import data.scripts.ix.DistanceUtil;
import org.magiclib.util.MagicLensFlare;

public class SpatialChargeShotOnFire implements OnFireEffectPlugin {

	private static float BOLT_BASE_DAMAGE = 150f;
	private static float BOLT_BASE_EMP = 300f;
	private static float BOLT_RANGE = 600f;

	public void onFire(DamagingProjectileAPI proj, WeaponAPI weapon, CombatEngineAPI engine) {
		ShipAPI thisShip = proj.getSource();
		float missileDamageMult = thisShip.getMutableStats().getMissileWeaponDamageMult().getMult();
		float damage = BOLT_BASE_DAMAGE * missileDamageMult;
		float emp = BOLT_BASE_EMP * missileDamageMult;
		proj.getVelocity().set(0f, 0f);
		Vector2f spawnPoint = new Vector2f(proj.getLocation());
			
		CombatEntityAPI hitShip = DistanceUtil.getNearestEnemy(proj, BOLT_RANGE);
		spawnFlare(engine, proj, spawnPoint);
		if (hitShip != null) spawnEMP(thisShip, spawnPoint, proj, hitShip, damage, emp, engine);
		engine.applyDamage(proj, proj.getLocation(), 10000f, DamageType.ENERGY, 0, false, false, proj);
	}
	
	private void spawnEMP(ShipAPI ship, Vector2f point, CombatEntityAPI source, CombatEntityAPI target, float damage, float emp, CombatEngineAPI engine) {
		
		engine.spawnEmpArc(
							ship, point, source, target,
							DamageType.ENERGY, 
							damage, 
							emp,
							10000f, // max range 
							"",
							20f, // thickness
							new Color(50,50,255,200), // fringe
							new Color(200,200,255,180) // core color
							);
	}
	
	private void spawnFlare(CombatEngineAPI engine, DamagingProjectileAPI proj, Vector2f point) {
		MagicLensFlare.createSharpFlare(
        engine,
        proj.getSource(),
        point,
		15,		//thickness
        400,	//length
        0,		//angle
        new Color(50,50,255),	//fringe
        new Color(100,100,255)	//core
        );
		Global.getSoundPlayer().playSound("mote_attractor_impact_damage", 1f, 1f, point, new Vector2f());
	}
}