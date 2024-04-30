package data.scripts.vice.weapons;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.BeamEffectPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipEngineControllerAPI;
import com.fs.starfarer.api.combat.ShipEngineControllerAPI.ShipEngineAPI;
import com.fs.starfarer.api.util.IntervalUtil;

import data.scripts.vice.DistanceUtil;

public class InterdictorOnHit implements BeamEffectPlugin {

	private boolean applied = false;
	private IntervalUtil tracker = new IntervalUtil(1.2f, 1.2f);
	
	public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
		tracker.advance(amount);
		if (applied) return;
		CombatEntityAPI target = beam.getDamageTarget();
		if (target == null) return;
		if (target instanceof ShipAPI && beam.getBrightness() >= 1f) {
			ShipAPI ship = (ShipAPI) target;
			ShipEngineControllerAPI ec = ship.getEngineController();
			float limit = ec.getFlameoutFraction();
			float disabledSoFar = 0f;
			boolean disabledAnEngine = false;
			List<ShipEngineAPI> engines = new ArrayList<ShipEngineAPI>(ec.getShipEngines());
			
			Collections.shuffle(engines);
			for (ShipEngineAPI engine : engines) {
				float contrib = engine.getContribution();
				if (disabledSoFar + contrib <= limit) {
					engine.disable();
					disabledSoFar += contrib;
					disabledAnEngine = true;
				}
			}
			if (!disabledAnEngine) {
				for (ShipEngineAPI engine : engines) {
					if (engine.isDisabled()) continue;
					engine.disable();
					break;
				}
			}
			ec.computeEffectiveStats(ship == Global.getCombatEngine().getPlayerShip());
			applied = true; //once per beam even if random failed to apply, count as applied
		}
	}
}