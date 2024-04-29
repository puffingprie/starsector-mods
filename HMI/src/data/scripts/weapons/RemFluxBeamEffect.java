package data.scripts.weapons;

import com.fs.starfarer.api.combat.*;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.util.IntervalUtil;

public class RemFluxBeamEffect implements BeamEffectPlugin {

	private IntervalUtil fireInterval = new IntervalUtil(0.25f, 1.75f);
	private boolean wasZero = true;
	private float DAMAGE_MULTIPLIER = 2f;
	
	
	public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
		CombatEntityAPI target = beam.getDamageTarget();
		if (target instanceof ShipAPI && beam.getBrightness() >= 1f) {
			float dur = beam.getDamage().getDpsDuration();
			// needed because when the ship is in fast-time, dpsDuration will not be reset every frame as it should be
			if (!wasZero) dur = 0;
			wasZero = beam.getDamage().getDpsDuration() <= 0;
			fireInterval.advance(dur);

			if (fireInterval.intervalElapsed()) {
				float FLUX_LEVEL = (beam.getSource().getFluxTracker().getFluxLevel())* DAMAGE_MULTIPLIER + 1f;
				float DAMAGE = beam.getDamage().getBaseDamage();

				if (beam.getDamageTarget() != null){
					engine.applyDamage(beam.getDamageTarget(), beam.getTo(), DAMAGE * FLUX_LEVEL, DamageType.FRAGMENTATION, 0f, false, false, beam.getSource());
				}
			}
		}
//			Global.getSoundPlayer().playLoop("system_emp_emitter_loop", 
//											 beam.getDamageTarget(), 1.5f, beam.getBrightness() * 0.5f,
//											 beam.getTo(), new Vector2f());
	}
}
