package data.scripts.ix.weapons;

import java.awt.Color;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.BeamEffectPlugin;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.NegativeExplosionVisual.NEParams;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

import data.scripts.ix.ShapedExplosionUtil;

public class SBEOnFire implements BeamEffectPlugin {

	private boolean appliedRift = false;
	private boolean appliedSmoke = false;
	private Color pc = new Color(110,255,85,255);
	private Color underColor = new Color(25,100,25,100);
	private IntervalUtil tracker = new IntervalUtil(0.8f, 0.8f);
	
	public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
		if (appliedRift) return;
		tracker.advance(amount);
		
		//on fire smoke effect
		if (beam.getBrightness() >= 0f && !appliedSmoke) {
			Vector2f origin = beam.getFrom();
			float beamAngle = Misc.getAngleInDegrees(origin, beam.getTo());
			Vector2f shipVelocity = beam.getSource().getVelocity();
			float shipSpeed = (float) Math.sqrt(shipVelocity.lengthSquared());
			ShapedExplosionUtil.spawnShapedExplosion(origin, beamAngle, shipSpeed, pc, false);
			appliedSmoke = true;
		}
		
		//on hit rift effect
		if (tracker.intervalElapsed()) {
			CombatEntityAPI target = beam.getDamageTarget();
			Vector2f point = beam.getRayEndPrevFrame();
			NEParams p = ShapedExplosionUtil.createStandardRiftParams(pc, underColor, 25f);
			p.fadeOut = 1.2f;
			p.hitGlowSizeMult = 0.2f;
			ShapedExplosionUtil.spawnStandardRift(point, p);
			DamagingProjectileAPI e = engine.spawnDamagingExplosion(createExplosionSpec(), beam.getSource(), point);
			appliedRift = true;
		}
	}
	
	private DamagingExplosionSpec createExplosionSpec() {
		float damage = 1000f;
		DamagingExplosionSpec spec = new DamagingExplosionSpec(
				0.1f, // duration
				150f, // radius
				150f, // coreRadius
				damage, // maxDamage
				damage, // minDamage
				CollisionClass.PROJECTILE_FF, // collisionClass
				CollisionClass.PROJECTILE_FIGHTER, // collisionClassByFighter
				0, // particleSizeMin
				0, // particleSizeRange
				0, // particleDuration
				0, // particleCount
				pc, // particleColor
				pc  // explosionColor
		);

		spec.setDamageType(DamageType.ENERGY);
		spec.setUseDetailedExplosion(false);
		spec.setSoundSetId("sbe_ix_rift");
		return spec;		
	}
}