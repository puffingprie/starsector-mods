package data.scripts.vice.shipsystems;

import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

public class Ultracapacitors extends BaseShipSystemScript {

	private static float BONUS_RANGE_PERCENT = 50f;
	private static float FLUX_REDUCTION = 25f;
	private static String EQUALIZER_HULLMOD = "vice_supercruise_engines";
	private static String EQUALIZER_DECO_ID = "vice_ultracapacitors_deco";
	
	private int tick = 0;
	private static int TICKS_PER_FRAME = 5;
	
	public static String DATA_KEY = "ultracapacitors_data_key";
	
	public class UltracapacitorsData {
		Map<String, Float> stats = new HashMap<String, Float>();
	}
	
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		float bonusRangePercent = BONUS_RANGE_PERCENT * effectLevel;
		stats.getEnergyWeaponRangeBonus().modifyPercent(id, bonusRangePercent);
		stats.getEnergyWeaponFluxCostMod().modifyMult(id, 1f - (FLUX_REDUCTION * 0.01f));
		
		ShipAPI ship = null;
		if (stats.getEntity() instanceof ShipAPI) ship = (ShipAPI) stats.getEntity();
		else return;
		
		if (stats.getVariant().hasHullMod(EQUALIZER_HULLMOD)) {
			CombatEngineAPI engine = Global.getCombatEngine();
			String key = DATA_KEY + "_" + ship.getId();
			UltracapacitorsData data = (UltracapacitorsData) engine.getCustomData().get(key);
			if (data == null) {
				data = new UltracapacitorsData();
				engine.getCustomData().put(key, data);
				data.stats.put("tick", 5f);
			}
			applyReactorGlow(ship, data);
		}
	}
	
	public void unapply(MutableShipStatsAPI stats, String id) {
		stats.getEnergyWeaponRangeBonus().unmodify(id);
		stats.getEnergyWeaponFluxCostMod().unmodify(id);
		ShipAPI ship = null;
		if (stats.getEntity() instanceof ShipAPI) ship = (ShipAPI) stats.getEntity();
		else return;
		if (stats.getVariant().hasHullMod(EQUALIZER_HULLMOD)) {
			unapplyReactorGlow(ship);
		}
	}
	
	private void applyReactorGlow(ShipAPI ship, UltracapacitorsData data) {
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
		if (index == 0) {
			return new StatusData("+" + (int) BONUS_RANGE_PERCENT + "% energy weapon range", false);
		}
		if (index == 1) {
			return new StatusData("-" + (int) FLUX_REDUCTION + "% energy weapon flux cost", false);
		}
		return null;
	}
}