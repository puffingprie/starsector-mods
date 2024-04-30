package data.scripts.ix.weapons;

import java.awt.Color;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.BeamEffectPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.util.Misc;

import data.scripts.ix.ShapedExplosionUtil;

public class OrthrusOnFire implements BeamEffectPlugin {

	private boolean applied = false;
	private float torpSpeed = 100;
	private Color pc = new Color(125,125,225,255);
	
	public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
		if (applied) return;
		
		CombatEntityAPI target = beam.getDamageTarget();
		if (beam.getBrightness() >= 0f) {
			Vector2f origin = beam.getFrom();
			float beamAngle = Misc.getAngleInDegrees(origin, beam.getTo());
			engine.addSmoothParticle(origin, new Vector2f(), 220, 1.0f, 1.3f, pc);
			ShapedExplosionUtil.spawnShapedExplosion(origin, beamAngle, torpSpeed, pc, true);
			applied = true; //apply once on firing
		}
	}
}
