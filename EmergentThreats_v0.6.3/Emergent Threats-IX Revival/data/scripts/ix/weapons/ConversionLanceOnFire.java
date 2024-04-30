package data.scripts.ix.weapons;

import java.awt.Color;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicRender;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.BeamEffectPlugin;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;
import com.fs.starfarer.api.util.IntervalUtil;

public class ConversionLanceOnFire implements BeamEffectPlugin {

	private static float EXPLOSION_DAMAGE = 1200f;
	private boolean applied = false;
	private static Color oldColor = new Color(170,50,50,255);
	private static Color innerColor = new Color(170,50,50,255);
	private static Color outerColor = new Color(255,200,0,150);
	private IntervalUtil tracker = new IntervalUtil(1.8f, 1.8f);
	
	public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
		if (applied) return;
		tracker.advance(amount);
		
		//on hit explosion effect
		if (tracker.intervalElapsed()) {
			Vector2f point = beam.getRayEndPrevFrame();
			engine.spawnDamagingExplosion(createExplosionSpec(), beam.getSource(), point);
			renderExplosion(beam);
			applied = true;
		}
	}
	
	private void renderExplosion(BeamAPI beam) {
		MagicRender.battlespace(
			Global.getSettings().getSprite("fx", "ix_conversion_explosion"), //sprite
			beam.getRayEndPrevFrame(), 	//loc
			new Vector2f(), 			//vel
			new Vector2f(100,100),		//size
			new Vector2f(600,600), 		//growth
			beam.getWeapon().getCurrAngle() + 90f, //angle
			0f,							//spin
			outerColor,	 				//color
			true,						//additive
			0f,							//fadein
			0.2f,						//full
			0.2f);						//fadeout
	}
	
	private DamagingExplosionSpec createExplosionSpec() {
		float damage = EXPLOSION_DAMAGE;
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
				innerColor, // particleColor
				innerColor  // explosionColor
		);

		spec.setDamageType(DamageType.HIGH_EXPLOSIVE);
		spec.setUseDetailedExplosion(false);
		spec.setSoundSetId("conversion_lance_ix_hit");
		return spec;		
	}
}