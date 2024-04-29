package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

import java.awt.*;

public class AbyssDashStatsOld extends BaseShipSystemScript {
	public static final float MAX_TIME_MULT = 1f;
	public static final float MIN_TIME_MULT = 1f;
	public static final float INCOMING_DAMAGE_MULT = 0.5f;
	public static final float SPEED_BONUS = 350f;
	public static final Color JITTER_COLOR = new Color(50, 15, 115, 55);
	public static final Color JITTER_UNDER_COLOR = new Color(50, 15, 115, 155);


	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		ShipAPI ship = null;
		boolean player = false;
		if (stats.getEntity() instanceof ShipAPI) {
			ship = (ShipAPI) stats.getEntity();
			player = ship == Global.getCombatEngine().getPlayerShip();
			id = id + "_" + ship.getId();
		} else {
			return;
		}
		stats.getMaxSpeed().modifyFlat(id, SPEED_BONUS);
		stats.getAcceleration().modifyFlat(id, 500f);
		stats.getDeceleration().modifyFlat(id, 500f);
		stats.getTurnAcceleration().modifyFlat(id, 10f * effectLevel);
		//stats.getTurnAcceleration().modifyPercent(id, 120f * effectLevel);
		stats.getMaxTurnRate().modifyFlat(id, 5f);
		//stats.getMaxTurnRate().modifyPercent(id, 120f);
		stats.getHullDamageTakenMult().modifyMult(id, 1f - (1f - INCOMING_DAMAGE_MULT) * effectLevel);
		stats.getArmorDamageTakenMult().modifyMult(id, 1f - (1f - INCOMING_DAMAGE_MULT) * effectLevel);
		stats.getShieldDamageTakenMult().modifyMult(id, 1f - (1f - INCOMING_DAMAGE_MULT) * effectLevel);
		stats.getEmpDamageTakenMult().modifyMult(id, 1f - (1f - INCOMING_DAMAGE_MULT) * effectLevel);
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

		Global.getCombatEngine().getTimeMult().unmodify(id);
		stats.getTimeMult().unmodify(id);

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
		float shipTimeMult = 1f + (MAX_TIME_MULT - 1f) * effectLevel;
		if (index == 0) {
			if (state == State.IN) {
				return new StatusData("INITIATING PHASE PROCEDURE", false);
			}
			if (state == State.ACTIVE) {
				return new StatusData("PHASE DASH ACTIVE", false);
			}
			if (state == State.OUT) {
				return new StatusData("PHASE DASH TERMINATED", false);
			}
		} else if (index == 1) {
			return new StatusData("+" + (int) SPEED_BONUS + " top speed", false);
		} else if (index == 2) {
			return new StatusData(" Damage taken halved", false);
		}
		return null;
	}
}


