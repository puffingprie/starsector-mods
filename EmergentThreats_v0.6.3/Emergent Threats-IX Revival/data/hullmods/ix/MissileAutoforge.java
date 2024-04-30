package data.hullmods.ix;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.util.IntervalUtil;

public class MissileAutoforge extends BaseHullMod {
	
	public static String MR_DATA_KEY = "microforge_reload_data_key";
	private static float RELOAD_TIME = 40f;
	private static float RELOAD_PERCENT_SMALL = 50f;
	private static float RELOAD_PERCENT_MEDIUM = 25f;
	private static float RELOAD_PERCENT_LARGE = 10f;
	private static String CONFLICT_MOD_0 = "advancedcore";
	private static String CONFLICT_MOD_1 = "missleracks";
	private static String CONFLICT_MOD_2 = "missile_autoloader";
	
	public static class PeriodicMissileReloadData {
		IntervalUtil interval = new IntervalUtil(RELOAD_TIME, RELOAD_TIME);
	}
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getVariant().getHullMods().remove(CONFLICT_MOD_0);
		stats.getVariant().removeMod(CONFLICT_MOD_1);
		stats.getVariant().removeMod(CONFLICT_MOD_2);
	}
	
	@Override
	public void advanceInCombat(ShipAPI ship, float amount) {
		super.advanceInCombat(ship, amount);

		if (!ship.isAlive()) return;
		
		CombatEngineAPI engine = Global.getCombatEngine();
		
		String key = MR_DATA_KEY + "_" + ship.getId();
		PeriodicMissileReloadData data = (PeriodicMissileReloadData) engine.getCustomData().get(key);
		if (data == null) {
			data = new PeriodicMissileReloadData();
			engine.getCustomData().put(key, data);
		}
		
		data.interval.advance(amount);
		if (data.interval.intervalElapsed()) {
			for (WeaponAPI w : ship.getAllWeapons()) {
				if ((w.getSize() != WeaponSize.SMALL && w.getSize() != WeaponSize.MEDIUM && w.getSize() != WeaponSize.LARGE) 
							|| w.getType() != WeaponType.MISSILE) continue;
				if (w.usesAmmo() && w.getAmmo() < w.getMaxAmmo()) {
					float reloadBonus = 0; 
					if (w.getSize() == WeaponSize.SMALL) reloadBonus = RELOAD_PERCENT_SMALL;
					else if (w.getSize() == WeaponSize.MEDIUM) reloadBonus = RELOAD_PERCENT_MEDIUM;
					else if (w.getSize() == WeaponSize.LARGE) reloadBonus = RELOAD_PERCENT_LARGE;
					int extraAmmo = 1;
					if (w.getMaxAmmo() * reloadBonus * 0.01f > 1f) {
						extraAmmo = (int) (w.getMaxAmmo() * reloadBonus * 0.01f);
					}
					int ammo = w.getAmmo() + extraAmmo;
					if (w.getMaxAmmo() == 1) w.setAmmo(1);
					else if (ammo <= w.getMaxAmmo()) w.setAmmo(ammo);
					else w.setAmmo(w.getMaxAmmo());
				}
			}
		}
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) RELOAD_PERCENT_SMALL + "%";
		if (index == 1) return "" + (int) RELOAD_PERCENT_MEDIUM + "%";
		if (index == 2) return "" + (int) RELOAD_PERCENT_LARGE + "%";
		if (index == 3) return "" + (int) RELOAD_TIME;
		if (index == 4) return "" + 1;
		return null;
	}
}