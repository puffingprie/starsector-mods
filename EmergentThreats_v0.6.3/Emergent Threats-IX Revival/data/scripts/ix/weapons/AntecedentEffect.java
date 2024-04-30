package data.scripts.ix.weapons;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;

import data.scripts.ix.DistanceUtil;
import org.lazywizard.lazylib.CollisionUtils;
import org.magiclib.util.MagicRender;

public class AntecedentEffect implements OnFireEffectPlugin, OnHitEffectPlugin {

	//proximity range set in behaviorSpec splitRange
	private static Color EXPLOSION_COLOR = new Color(50,150,50,255);
	private static float EXPLOSION_RADIUS = 400f;
	private static float EXPLOSION_DAMAGE = 4000f; //x2, modified by onFire
	private static String WARHEAD_ID = "antecedent_warhead";
	
	public void onFire(DamagingProjectileAPI proj, WeaponAPI weapon, CombatEngineAPI engine) {
		proj.setDamageAmount(0); //damage dealt from explosion
		MissileAPI missile = (MissileAPI) proj;
		missile.setEmpResistance(2);
		
		//for MIRV warhead
		if (proj.getProjectileSpecId().equals(WARHEAD_ID)) {
			ShipAPI thisShip = proj.getSource();
			float missileDamageMult = thisShip.getMutableStats().getMissileWeaponDamageMult().getMult();
			float damage = EXPLOSION_DAMAGE * missileDamageMult;
			spawnExplosions(proj, proj.getLocation(), damage, engine);
			renderShockwave(proj.getLocation());
			engine.applyDamage(proj, proj.getLocation(), 10000f, DamageType.ENERGY, 0, false, false, proj);
		}
	}
	
	public void onHit(DamagingProjectileAPI proj, CombatEntityAPI target, Vector2f point, boolean shieldHit, 
						ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
		ShipAPI thisShip = proj.getSource();
		float missileDamageMult = thisShip.getMutableStats().getMissileWeaponDamageMult().getMult();
		float damage = EXPLOSION_DAMAGE * missileDamageMult;
		spawnExplosions(proj, point, damage, engine);
		renderShockwave(point);
	}
	
	private void spawnExplosions(DamagingProjectileAPI proj, Vector2f point, float damage, CombatEngineAPI engine) {
		ShipAPI thisShip = proj.getSource();
		DamageType damageType = proj.getDamage().getType();

		DamagingExplosionSpec spec = new DamagingExplosionSpec(
					0.1f,
					EXPLOSION_RADIUS,
					EXPLOSION_RADIUS,
					damage,
					damage,
					CollisionClass.PROJECTILE_FF,
					CollisionClass.PROJECTILE_FF,
					0f,
					0f,
					0f,
					0,
					new Color(0,0,0,0),
					new Color(0,0,0,0));
		
		engine.spawnDamagingExplosion(spec, thisShip, point);
		engine.spawnDamagingExplosion(spec, thisShip, point);
		engine.spawnExplosion(point, new Vector2f(), EXPLOSION_COLOR, EXPLOSION_RADIUS * 3f, 0.7f);
        engine.addHitParticle(point, new Vector2f(), EXPLOSION_RADIUS * 5f, 2f, 1f, Color.red);
		Global.getSoundPlayer().playSound("antecedent_ix_explode", 1f, 1f, point, new Vector2f());

		//this ship plus deployed fighters
		List<ShipAPI> self = new ArrayList<ShipAPI>();
		self.add(thisShip);
		for (ShipAPI ship : Global.getCombatEngine().getShips()) {
			if (!ship.isFighter() || ship.getWing() == null) continue;
			if (ship.getWing().getSourceShip() == thisShip) self.add(ship);
		}
		
		for (ShipAPI ship : self) {
			if (DistanceUtil.getDistance(proj, ship) < EXPLOSION_RADIUS) {
				Vector2f impactSite = null;
				if (ship == thisShip) impactSite = CollisionUtils.getNearestPointOnBounds(point, thisShip);
				else impactSite = ship.getLocation();
				engine.applyDamage(ship, impactSite, damage * 0.5f, damageType, 0, false, false, proj);
			}
		}
	}
	
	private void renderShockwave(Vector2f point) {
		MagicRender.battlespace(
			Global.getSettings().getSprite("fx", "ix_antecedent_explosion"), //sprite
			point,					 	//loc
			new Vector2f(), 			//vel
			new Vector2f(500,500),		//size
			new Vector2f(900,900),		//growth
			0f, 						//angle
			0f,							//spin
			Color.red, 					//color
			true,						//additive
			0.0f,						//fadein
			0.3f,						//full
			0.3f);						//fadeout
			
		MagicRender.battlespace(
			Global.getSettings().getSprite("fx", "ix_antecedent_explosion"), //sprite
			point,					 	//loc
			new Vector2f(), 			//vel
			new Vector2f(500,500),		//size
			new Vector2f(900,900),		//growth
			0f, 						//angle
			0f,							//spin
			EXPLOSION_COLOR,			//color
			true,						//additive
			0.0f,						//fadein
			0.5f,						//full
			0.4f);						//fadeout
	}
}