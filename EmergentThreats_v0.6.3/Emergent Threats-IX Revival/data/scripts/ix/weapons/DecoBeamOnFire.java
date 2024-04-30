package data.scripts.ix.weapons;

import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.BeamEffectPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.ShipAPI;

public class DecoBeamOnFire implements BeamEffectPlugin {

	public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
		ShipAPI proj = beam.getSource();
		engine.applyDamage(proj, proj.getLocation(), 0f, DamageType.ENERGY, 10000f, false, false, proj);
	}
}