package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

import java.awt.*;

public class AbyssDashStats extends BaseShipSystemScript {
	private static float SPEED_BONUS = 275f;
	private static float TURN_BONUS = 100f;
	private static final float INCOMING_DAMAGE_MULT = 0.5f;

	private static final Color JITTER_COLOR = new Color(50,15,115,55);
	private static final Color JITTER_UNDER_COLOR = new Color(50,15,115,155);
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		ShipAPI ship = (ShipAPI) stats.getEntity();

		stats.getMaxSpeed().modifyFlat(id, SPEED_BONUS);

		stats.getTurnAcceleration().modifyFlat(id, TURN_BONUS);
		stats.getTurnAcceleration().modifyPercent(id, TURN_BONUS * 5f);
		stats.getMaxTurnRate().modifyFlat(id, 60f);
		stats.getMaxTurnRate().modifyPercent(id, 100f);
		stats.getHullDamageTakenMult().modifyMult(id, 1f - (1f - INCOMING_DAMAGE_MULT) * effectLevel);
		stats.getArmorDamageTakenMult().modifyMult(id, 1f - (1f - INCOMING_DAMAGE_MULT) * effectLevel);
		stats.getShieldDamageTakenMult().modifyMult(id,1f-(1f-INCOMING_DAMAGE_MULT)*effectLevel);
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
			stats.getAcceleration().modifyMult(id, 50f);
			stats.getDeceleration().modifyMult(id, 50f);
		}
		if (state == State.ACTIVE) {
			jitterLevel = 1f;
			jitterRangeBonus = maxRangeBonus;
			stats.getAcceleration().modifyMult(id, 0f);
			stats.getDeceleration().modifyMult(id, 0f);
		}
		if (state == State.OUT) {
			stats.getMaxSpeed().unmodify(id); // to slow down ship to its regular top speed while powering drive down
			/*
			if (MathUtils.getDistance(stats.getEntity().getVelocity(), new Vector2f()) > ship.getEngineController().getMaxSpeedWithoutBoost())
				stats.getEntity().getVelocity().scale(0.95f);

			 */
			jitterRangeBonus = jitterLevel * maxRangeBonus;
			stats.getAcceleration().modifyMult(id, 10f);
			stats.getDeceleration().modifyMult(id, 20f);
			stats.getMaxTurnRate().unmodify(id);
		}
		jitterLevel = (float) Math.sqrt(jitterLevel);
		effectLevel *= effectLevel;

		//ship.setJitter(this, JITTER_COLOR, jitterLevel, 3, 0, 0 + jitterRangeBonus);
		//ship.setJitterUnder(this, JITTER_UNDER_COLOR, jitterLevel, 25, 0f, 7f + jitterRangeBonus);


		//ship.getEngineController().fadeToOtherColor(this, JITTER_COLOR, new Color(0,0,0,0), effectLevel, 0.5f);
		//ship.getEngineController().extendFlame(this, -0.25f, -0.25f, -0.25f);


	}

	public void unapply(MutableShipStatsAPI stats, String id) {


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
		}
		else if (index == 2) {
		return new StatusData(" Damage taken halved", false);
		}
		return null;
	}

}

