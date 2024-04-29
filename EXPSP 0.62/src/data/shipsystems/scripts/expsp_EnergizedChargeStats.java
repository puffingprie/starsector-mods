package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;

import java.awt.*;

public class expsp_EnergizedChargeStats extends BaseShipSystemScript {
	public static final float INCOMING_DAMAGE_MULT = 0.8f;
	private Float mass = null;
	public static final float SPEED_BONUS = 350f;
	public static final float MASS_MULT = 2;
	public static final Color JITTER_COLOR = new Color(255, 144, 0, 220);
	public static final Color JITTER_UNDER_COLOR = new Color(255, 138, 0, 128);
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		ShipAPI ship = null;


		boolean player = false;
		if (stats.getEntity() instanceof ShipAPI) {
			ship = (ShipAPI) stats.getEntity();
			player = ship == Global.getCombatEngine().getPlayerShip();
			id = id + "_" + ship.getId();
			if (mass==null) {
				mass = ship.getMass();
			}
		} else {
			return;
		}
		stats.getMaxSpeed().modifyFlat(id, SPEED_BONUS);
		stats.getAcceleration().modifyFlat(id, SPEED_BONUS*1.5f);
		stats.getDeceleration().modifyFlat(id, SPEED_BONUS*1.5f);
		//stats.getTurnAcceleration().modifyFlat(id, 10f * effectLevel);
		//stats.getTurnAcceleration().modifyPercent(id, 120f * effectLevel);
		//stats.getMaxTurnRate().modifyFlat(id, 5f);
		//stats.getMaxTurnRate().modifyPercent(id, 120f);
		stats.getHullDamageTakenMult().modifyMult(id,  INCOMING_DAMAGE_MULT * effectLevel);
		stats.getArmorDamageTakenMult().modifyMult(id, INCOMING_DAMAGE_MULT* effectLevel);
		stats.getShieldDamageTakenMult().modifyMult(id,  INCOMING_DAMAGE_MULT * effectLevel);
		stats.getEmpDamageTakenMult().modifyMult(id, INCOMING_DAMAGE_MULT* effectLevel);
		ship.setMass(mass*MASS_MULT );
		float jitterLevel = effectLevel;
		float jitterRangeBonus = 0;
		float maxRangeBonus = 10f;
		if (state == State.IN) {
			jitterLevel = effectLevel / (1f / ship.getSystem().getChargeUpDur());
			if (jitterLevel > 1) {
				jitterLevel = 1f;
			}
			jitterRangeBonus = jitterLevel * maxRangeBonus;
		} else if (state == State.ACTIVE) {
			jitterLevel = 1f;
			jitterRangeBonus = maxRangeBonus;
		} else if (state == State.OUT) {
			jitterRangeBonus = jitterLevel * maxRangeBonus;
			stats.getMaxSpeed().unmodify(id);
			stats.getMaxTurnRate().unmodify(id);
			ship.setMass(mass);
		}
		jitterLevel = (float) Math.sqrt(jitterLevel);
		effectLevel *= effectLevel;

		ship.setJitter(this, JITTER_COLOR, jitterLevel, 3, 0, 0 + jitterRangeBonus);
		ship.setJitterUnder(this, JITTER_UNDER_COLOR, jitterLevel, 25, 0f, 7f + jitterRangeBonus);


		ship.getEngineController().fadeToOtherColor(this, JITTER_COLOR, new Color(0, 0, 0, 0), effectLevel, 0.5f);
		ship.getEngineController().extendFlame(this, -0.25f, -0.25f, -0.25f);
	}


	public void unapply(MutableShipStatsAPI stats, String id) {
		ShipAPI ship = null;
		boolean player = false;
		if (stats.getEntity() instanceof ShipAPI) {
			ship = (ShipAPI) stats.getEntity();
			player = ship == Global.getCombatEngine().getPlayerShip();
			id = id + "_" + ship.getId();
		} else {
			return;
		}



		stats.getMaxSpeed().unmodify(id);
		stats.getMaxTurnRate().unmodify(id);
		stats.getTurnAcceleration().unmodify(id);
		stats.getAcceleration().unmodify(id);
		stats.getDeceleration().unmodify(id);
		stats.getHullDamageTakenMult().unmodify(id);
		stats.getArmorDamageTakenMult().unmodify(id);
		stats.getEmpDamageTakenMult().unmodify(id);
		stats.getShieldDamageTakenMult().unmodify(id);
	}
	public StatusData getStatusData(int index, State state, float effectLevel) {
		if (index == 0) {
			return new StatusData("increased engine power", false);
		}
		else if (index ==1){
			return new StatusData("Incoming damage reduced by 20%", false);
		}else if (index ==2){
			return new StatusData("Increased mass by 100%", false);
		}
		return null;
	}
}
	
