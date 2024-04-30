package data.scripts.ix.weapons;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.BeamEffectPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipEngineControllerAPI;
import com.fs.starfarer.api.combat.ShipEngineControllerAPI.ShipEngineAPI;
import com.fs.starfarer.api.util.IntervalUtil;

import data.scripts.ix.DistanceUtil;

public class InterdictorOnHit implements BeamEffectPlugin {

	private boolean IS_APPLIED = false;
	private boolean IS_APPLIED_DECO = false;
	private static int BOLTS = 2;
	private static float INTERVAL_TIME = 0.2f;
	private static int INTERVAL_MAX_COUNT = 6;
	private float TIMER = 0.2f;
	private int INTERVAL_COUNT = 0;
	private IntervalUtil tracker = new IntervalUtil(1.2f, 1.2f);
	
	public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
		if (IS_APPLIED && IS_APPLIED_DECO) return;

		boolean isIconoclast = beam.getSource().getVariant().hasHullMod("ix_point_defense_iris");
		if (!isIconoclast) tracker.advance(amount);
		TIMER += amount;
		
		//Iconoclast built-in weapon on fire effect
		if (isIconoclast && beam.getBrightness() >= 0f && !IS_APPLIED_DECO) {
			if (TIMER >= INTERVAL_TIME && INTERVAL_COUNT < INTERVAL_MAX_COUNT) {
				TIMER = 0f;
				ShipAPI ship = beam.getSource();
				for (int i = 0; i < BOLTS; i++) {
					spawnEmpArc(ship, ship, beam.getFrom(), engine);
					INTERVAL_COUNT++;
				}
			}
			else if (INTERVAL_COUNT == INTERVAL_MAX_COUNT) IS_APPLIED_DECO = true;
		}
		else if (!isIconoclast) IS_APPLIED_DECO = true;
		
		if (IS_APPLIED) return;
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
			IS_APPLIED = true; //once per beam even if random failed to apply, count as applied
		}
	}
	
	private void spawnEmpArc(ShipAPI source, ShipAPI target, Vector2f point, CombatEngineAPI engine) {
		engine.spawnEmpArc(source, //damageSource
					point, //hit location
					source, //CombatEntityAPI point anchor
					source, //CombatEntityAPI target
					DamageType.ENERGY,
					0f, // damage
					0f, // emp damage
					300f, // range
					"", // sound
					26f, // thickness
					new Color(25,100,155,255),
					Color.white);
	}
}