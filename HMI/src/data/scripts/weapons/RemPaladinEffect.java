package data.scripts.weapons;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.AsteroidAPI;
import com.fs.starfarer.api.combat.*;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lazywizard.lazylib.combat.entities.SimpleEntity;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.loading.DamagingExplosionSpec;
import com.fs.starfarer.api.util.Misc;

public class RemPaladinEffect implements BeamEffectPlugin {

	private boolean done = false;
	public static Color LIGHTNING_CORE_COLOR = new Color(219, 255, 254, 200);
	public static Color LIGHTNING_FRINGE_COLOR = new Color(19, 227, 255, 175);

	public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
		if (done) return;
		
		CombatEntityAPI target = beam.getDamageTarget();
		ShipAPI ship = beam.getWeapon().getShip();
		boolean first = beam.getWeapon().getBeams().indexOf(beam) == 0;
		if (target != null && beam.getBrightness() >= 1f && first) {
			Vector2f point = beam.getTo();
			float maxDist = 0f;
			for (BeamAPI curr : beam.getWeapon().getBeams()) {
				maxDist = Math.max(maxDist, Misc.getDistance(point, curr.getTo()));
			}
			if (maxDist < 15f) {
				DamagingProjectileAPI e = engine.spawnDamagingExplosion(createExplosionSpec(), beam.getSource(), point);
				e.addDamagedAlready(target);
				List<CombatEntityAPI> validTargets = new ArrayList<CombatEntityAPI>();
				for (CombatEntityAPI entityToTest : CombatUtils.getEntitiesWithinRange(point, 500f)) {
					if (entityToTest instanceof ShipAPI || entityToTest instanceof AsteroidAPI || entityToTest instanceof MissileAPI) {
						//Phased targets, and targets with no collision, are ignored
						if (entityToTest instanceof ShipAPI) {
							if (((ShipAPI) entityToTest).isPhased() || ship.getOriginalOwner() != target.getOwner()) {
								continue;
							}
						}
						if (entityToTest instanceof MissileAPI) {
							if (((MissileAPI)entityToTest).getOwner() != target.getOwner()) {
								continue;
							}
						}
						if (entityToTest.getCollisionClass().equals(CollisionClass.NONE)) {
							continue;
						}
						validTargets.add(entityToTest);
					}
				}
				if (validTargets.isEmpty()) {
					validTargets.add(new SimpleEntity(MathUtils.getRandomPointInCircle(point, 500f)));
				}
				CombatEntityAPI target2 = null;
				for (int i = 0; i < 4; i++) {
					if (validTargets.isEmpty()) {
						target2 = new SimpleEntity(MathUtils.getRandomPointInCircle(point, 500f));
					} else {
						target2 = validTargets.get(MathUtils.getRandomNumberInRange(0, validTargets.size() - 1));
					}
					engine.spawnEmpArc(beam.getSource(), point, target2, target2,
							DamageType.ENERGY, //Damage type
							MathUtils.getRandomNumberInRange(0.8f, 1.2f) * 200f, //Damage
							MathUtils.getRandomNumberInRange(0.8f, 1.2f) * 300f, //Emp
							100000f, //Max range
							"tachyon_lance_emp_impact", //Impact sound
							10f, // thickness of the lightning bolt
							LIGHTNING_CORE_COLOR, //Central color
							LIGHTNING_FRINGE_COLOR //Fringe Color);
					);
				}
				done = true;
			}
		}
	}

	public DamagingExplosionSpec createExplosionSpec() {
		float damage = 200f;
		DamagingExplosionSpec spec = new DamagingExplosionSpec(
				0.1f, // duration
				200f, // radius
				100f, // coreRadius
				damage, // maxDamage
				damage / 2f, // minDamage
				CollisionClass.PROJECTILE_FF, // collisionClass
				CollisionClass.PROJECTILE_FIGHTER, // collisionClassByFighter
				3f, // particleSizeMin
				3f, // particleSizeRange
				0.5f, // particleDuration
				150, // particleCount
				new Color(145, 245, 255,100), // particleColor
				new Color(145, 245, 255, 50)  // explosionColor
		);

		spec.setDamageType(DamageType.FRAGMENTATION);
		spec.setUseDetailedExplosion(false);
		spec.setSoundSetId("explosion_guardian");
		return spec;
	}
}




