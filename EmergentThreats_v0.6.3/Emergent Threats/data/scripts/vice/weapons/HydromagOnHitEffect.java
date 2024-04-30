package data.scripts.vice.weapons;

import java.awt.Color;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.ArmorGridAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;

public class HydromagOnHitEffect implements OnHitEffectPlugin {

	private static float CR_PENALTY = 0.01f;

	public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target,
					  Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
						  
		engine.addHitParticle(point, new Vector2f(), 250, 0.1f, 1f, Color.green);
        engine.addSmoothParticle(point, new Vector2f(), 350, 2f, 0.25f, Color.white);
		
		if (!shieldHit && target instanceof ShipAPI) {
			ShipAPI ship = (ShipAPI) target;
			if (!applyDegradeCheck(ship, point)) return;
			ship.setCurrentCR(ship.getCurrentCR() - CR_PENALTY);
		}
	}
		
	//Checks % of armor remaining in area. Low armor increases odds of apply effect.
	private boolean applyDegradeCheck(ShipAPI ship, Vector2f point) {
		ArmorGridAPI grid = ship.getArmorGrid();
		int[] cell = grid.getCellAtLocation(point);
		if (cell == null) return true;
		float armorPercentRemaining = ship.getArmorGrid().getArmorFraction(cell[0], cell[1]);
		return (armorPercentRemaining <= Math.random());
	}
}
