package data.scripts.ix.shipsystems;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

public class ImmanenceEngineStats extends BaseShipSystemScript {
	private static float TIME_MULT = 2f;
	private static float INCOMING_MULT = 0.5f;
	private static String EQUALIZER_ID = "equalizer_ix";
	private static String EQUALIZER_D_ID = "equalizer_ix_default_D";
	private static String EQUALIZER_DECO_ID = "immanence_engine_deco_ix";
	
	private int tick = 0;
	private static int TICKS_PER_FRAME = 15;
	
	private static Color JITTER_COLOR = new Color(100,255,100,155);
	private static Color SHIELD_INNER_COLOR_ACTIVE = new Color(100,225,100,75);
	private static Color SHIELD_INNER_HIGH_TECH = new Color(125,125,255,75);
	
	public static String DATA_KEY = "immanence_engine_data_key";

	public class ImmanenceEngineData {
		Map<String, Float> stats = new HashMap<String, Float>();
	}
	
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		ShipAPI ship = null;
		if (stats.getEntity() instanceof ShipAPI) ship = (ShipAPI) stats.getEntity();
		else return;
		applyEffect(ship, id, state, effectLevel);
		String hullId = ship.getHullSpec().getHullId();
		if (hullId.equals(EQUALIZER_ID) || hullId.equals(EQUALIZER_D_ID)) {
			CombatEngineAPI engine = Global.getCombatEngine();
			String key = DATA_KEY + "_" + ship.getId();
			ImmanenceEngineData data = (ImmanenceEngineData) engine.getCustomData().get(key);
			if (data == null) {
				data = new ImmanenceEngineData();
				engine.getCustomData().put(key, data);
				data.stats.put("tick", 0f);
			}
			applyReactorGlow(ship, data);
		}
	}
	
	public void unapply(MutableShipStatsAPI stats, String id) {
		ShipAPI ship = null;
		if (stats.getEntity() instanceof ShipAPI) ship = (ShipAPI) stats.getEntity();
		else return;
		unapplyEffect(ship, id);
		String hullId = ship.getHullSpec().getHullId();
		if (hullId.equals(EQUALIZER_ID) || hullId.equals(EQUALIZER_D_ID)) {
			unapplyReactorGlow(ship);
		}
	}
	
	private void applyEffect(ShipAPI ship, String id, State state, float effectLevel) {
		float jitterLevel = effectLevel;
		float jitterRangeBonus = 0;
		float maxRangeBonus = 10f;
		
		if (state == State.IN) {
			jitterLevel = effectLevel;
			if (jitterLevel > 1) jitterLevel = 1f;
			jitterRangeBonus = jitterLevel * maxRangeBonus;
		} 
		else if (state == State.ACTIVE) {
			jitterLevel = 1f;
			jitterRangeBonus = maxRangeBonus;
		} 
		else if (state == State.OUT) jitterRangeBonus = jitterLevel * maxRangeBonus;
		
		jitterLevel = (float) Math.sqrt(jitterLevel);
		effectLevel *= effectLevel;
		ship.setJitterUnder(this, JITTER_COLOR, jitterLevel, 25, 0f, 7f + jitterRangeBonus);
		float shipTimeMult = 1f + (TIME_MULT - 1f) * effectLevel;
		
		if (ship == Global.getCombatEngine().getPlayerShip()) {
			Global.getCombatEngine().getTimeMult().modifyMult(id, 1f / shipTimeMult);
		} 
		else Global.getCombatEngine().getTimeMult().unmodify(id);

		MutableShipStatsAPI stat = ship.getMutableStats();
		stat.getTimeMult().modifyMult(id, shipTimeMult);
		stat.getShieldDamageTakenMult().modifyMult(id, 1f - INCOMING_MULT * effectLevel);
		stat.getArmorDamageTakenMult().modifyMult(id, 1f - INCOMING_MULT * effectLevel);
		stat.getHullDamageTakenMult().modifyMult(id, 1f - INCOMING_MULT * effectLevel);
		if (ship.getShield() != null) ship.getShield().setInnerColor(SHIELD_INNER_COLOR_ACTIVE);
		ship.getEngineController().fadeToOtherColor(this, JITTER_COLOR, new Color(0,0,0,0), effectLevel, 0.5f);
	}

	private void unapplyEffect(ShipAPI ship, String id) {
		Global.getCombatEngine().getTimeMult().unmodify(id);
		MutableShipStatsAPI stat = ship.getMutableStats();
		stat.getTimeMult().unmodify(id);
		stat.getShieldDamageTakenMult().unmodify(id);
		stat.getArmorDamageTakenMult().unmodify(id);
		stat.getHullDamageTakenMult().unmodify(id);
		if (ship.getShield() != null) ship.getShield().setInnerColor(SHIELD_INNER_HIGH_TECH);
	}
	
	private void applyReactorGlow(ShipAPI ship, ImmanenceEngineData data) {
		WeaponAPI weapon = null;
		float frameTick = (Float) data.stats.get("tick");
		for (WeaponAPI w : ship.getAllWeapons()) {
			if (w.getId().equals(EQUALIZER_DECO_ID)) weapon = w;
		}
		if (weapon != null) {
			int frame = weapon.getAnimation().getFrame();
			frameTick++;
			if (frameTick > TICKS_PER_FRAME) {
				if (frame != weapon.getAnimation().getNumFrames() - 1) frame++;
				data.stats.put("tick", 0f);
			}
			else data.stats.put("tick", frameTick);
			weapon.getAnimation().setFrame(frame);
		}
	}

	private void unapplyReactorGlow(ShipAPI ship) {
		WeaponAPI weapon = null;
		for (WeaponAPI w : ship.getAllWeapons()) {
			if (w.getId().equals(EQUALIZER_DECO_ID)) weapon = w;
		}
		if (weapon != null) {
			weapon.getAnimation().setFrame(0);
		}
	}
	
	public StatusData getStatusData(int index, State state, float effectLevel) {
		float shipTimeMult = 1f + (TIME_MULT - 1f) * effectLevel;
		if (index == 0) return new StatusData("time flow altered", false);
		return null;
	}
}