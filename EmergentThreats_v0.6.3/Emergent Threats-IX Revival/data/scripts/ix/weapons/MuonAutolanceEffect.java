package data.scripts.ix.weapons;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.BeamEffectPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.util.IntervalUtil;

public class MuonAutolanceEffect implements BeamEffectPlugin {

	private IntervalUtil fireInterval = new IntervalUtil(0.7f, 1.0f);
	private boolean wasZero = true;
	
	public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
		CombatEntityAPI target = beam.getDamageTarget();
		if (target instanceof ShipAPI && beam.getBrightness() >= 1f) {
			if (target.getShield() != null && target.getShield().isWithinArc(beam.getTo())) return;
			float dur = beam.getDamage().getDpsDuration();
			if (!wasZero) dur = 0;
			wasZero = beam.getDamage().getDpsDuration() <= 0;
			fireInterval.advance(dur);
			
			if (fireInterval.intervalElapsed()) {
				Vector2f point = beam.getRayEndPrevFrame();
				float emp = beam.getDamage().getFluxComponent() * 0.5f;
				float dam = beam.getDamage().getDamage() * 0.5f;
				engine.spawnEmpArc(beam.getSource(), point, beam.getDamageTarget(), beam.getDamageTarget(),DamageType.ENERGY, dam, emp, 100000f, "tachyon_lance_emp_impact", beam.getWidth(), beam.getFringeColor(), beam.getCoreColor());
			}
		}
	}
}
