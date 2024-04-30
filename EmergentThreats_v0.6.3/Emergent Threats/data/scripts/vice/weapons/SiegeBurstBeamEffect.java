package data.scripts.vice.weapons;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ArmorGridAPI;
import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.BeamEffectPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.util.Misc;

public class SiegeBurstBeamEffect implements BeamEffectPlugin {

	private static float LARGE_SHIELD_DAMAGE = 50f;
	private static float MEDIUM_SHIELD_DAMAGE = 15f;
	
	private boolean applied = false;
	
	public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
		if (applied) return;
		
		CombatEntityAPI target = beam.getDamageTarget();
		if (target instanceof ShipAPI && beam.getBrightness() >= 1f) {
			boolean hitShield = target.getShield() != null && target.getShield().isWithinArc(beam.getTo());
			if (!hitShield) {
				float bonusDamage = beam.getWeapon().getSize() == WeaponSize.LARGE ? LARGE_SHIELD_DAMAGE : MEDIUM_SHIELD_DAMAGE;
				Vector2f point = beam.getTo();
				dealArmorDamage(beam, (ShipAPI) target, point, bonusDamage);
				//removed due to visual bug with adaptive temporal shell
				/**
				if (bonusDamage == LARGE_SHIELD_DAMAGE) {
					engine.addHitParticle(point, new Vector2f(), 150, 0.1f, 1f, Color.red);
					engine.addSmoothParticle(point, new Vector2f(), 250, 2f, 0.25f, Color.white);
				}
				**/
				applied = true;
			}
		}
	}
	public static void dealArmorDamage(BeamAPI beam, ShipAPI target, Vector2f point, float bonusDamage) {
		CombatEngineAPI engine = Global.getCombatEngine();

		ArmorGridAPI grid = target.getArmorGrid();
		int[] cell = grid.getCellAtLocation(point);
		if (cell == null) return;
		
		int gridWidth = grid.getGrid().length;
		int gridHeight = grid.getGrid()[0].length;
		
		float damageDealt = 0f;
		for (int i = -2; i <= 2; i++) {
			for (int j = -2; j <= 2; j++) {
				if ((i == 2 || i == -2) && (j == 2 || j == -2)) continue; // skip corners
				
				int cx = cell[0] + i;
				int cy = cell[1] + j;
				
				if (cx < 0 || cx >= gridWidth || cy < 0 || cy >= gridHeight) continue;
				
				float damMult = 1/30f;
				if (i == 0 && j == 0) {
					damMult = 1/15f;
				} else if (i <= 1 && i >= -1 && j <= 1 && j >= -1) { // S hits
					damMult = 1/15f;
				} else { // T hits
					damMult = 1/30f;
				}
				
				float armorInCell = grid.getArmorValue(cx, cy);
				float damage = bonusDamage * damMult;
				damage = Math.min(damage, armorInCell);
				if (damage <= 0) continue;
				
				target.getArmorGrid().setArmorValue(cx, cy, Math.max(0, armorInCell - damage));
				damageDealt += damage;
			}
		}
		
		if (damageDealt > 0) {
			engine.addFloatingDamageText(point, bonusDamage, Misc.FLOATY_ARMOR_DAMAGE_COLOR, target, beam.getSource());
			target.syncWithArmorGridState();
		}
	}
}


