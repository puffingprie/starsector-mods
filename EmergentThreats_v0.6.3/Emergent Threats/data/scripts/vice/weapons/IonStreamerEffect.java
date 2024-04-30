package data.scripts.vice.weapons;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.BeamEffectPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class IonStreamerEffect implements BeamEffectPlugin {

	private boolean applied = false;
	private static float pierceChance = 0.3333f; //3 beam salvo, each beam has 33% base chance to apply EMP arc
	
	public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
		if (applied) return;
		CombatEntityAPI target = beam.getDamageTarget();
		if (target instanceof ShipAPI && beam.getBrightness() >= 1f) {
			boolean hitShield = target.getShield() != null && target.getShield().isWithinArc(beam.getTo());
			if (Math.random() < pierceChance) applyEMP(target, engine, beam, hitShield);
			applied = true; //each beam only makes apply check once even if it did not generate EMP arc
		}
	}
	
	private void applyEMP(CombatEntityAPI target, CombatEngineAPI engine, BeamAPI beam, boolean hitShield) {
		ShipAPI ship = (ShipAPI) target;
		float pierceChance = ((ShipAPI)target).getHardFluxLevel() - 0.1f;
		pierceChance *= ship.getMutableStats().getDynamic().getValue(Stats.SHIELD_PIERCED_MULT);
		boolean piercedShield = (float) Math.random() < pierceChance;
		if (!hitShield || piercedShield) {
			Vector2f point = beam.getRayEndPrevFrame();
			float emp = beam.getDamage().getFluxComponent() * 1f;
			float dam = beam.getDamage().getDamage() * 1f;
			engine.spawnEmpArcPierceShields(beam.getSource(), point, beam.getDamageTarget(), beam.getDamageTarget(),
					DamageType.ENERGY, dam, emp, 100000f, "tachyon_lance_emp_impact", beam.getWidth() + 9f, 
					beam.getFringeColor(), beam.getCoreColor());
		}
	}
}
