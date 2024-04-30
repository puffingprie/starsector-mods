package data.scripts.ix.weapons;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.BeamEffectPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class MuonOnArmorHitEffect implements BeamEffectPlugin {

	private boolean applied = false;
	private static float empChance = 0.3333f; //3 beam salvo, each has 33% base chance to apply EMP arc
	
	public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
		if (applied) return;
		CombatEntityAPI target = beam.getDamageTarget();
		WeaponSize size = beam.getWeapon().getSize();
		if (target instanceof ShipAPI && beam.getBrightness() >= 1f) {
			boolean hitShield = target.getShield() != null && target.getShield().isWithinArc(beam.getTo());
			if ((!hitShield) && (size != WeaponSize.SMALL || Math.random() < empChance)) {
				applyEMP(target, engine, beam);
				if (size != WeaponSize.SMALL) applyEMP(target, engine, beam);
			}
			applied = true; //each beam only makes apply check once even if it did not generate EMP arc
		}
	}
	
	private void applyEMP(CombatEntityAPI target, CombatEngineAPI engine, BeamAPI beam) {
		Vector2f point = beam.getRayEndPrevFrame();
		float dMult = beam.getWeapon().getSize() == WeaponSize.SMALL ? 1f : 0.25f;
		float dam = beam.getDamage().getDamage() * dMult;
		float emp = beam.getDamage().getFluxComponent() * 1f;
		engine.spawnEmpArc(beam.getSource(), point, beam.getDamageTarget(), beam.getDamageTarget(),DamageType.ENERGY, dam, emp, 100000f, "tachyon_lance_emp_impact", beam.getWidth(), beam.getFringeColor(), beam.getCoreColor());
	}
}
