package data.scripts.vice.shipsystems;

import java.awt.Color;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

public class PhaseOvercharge extends BaseShipSystemScript {

	public static float DAMAGE_BONUS_PERCENT = 100f;
	public static Object KEY_SHIP = new Object();
	
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		if (stats.getEntity() instanceof ShipAPI) {
			ShipAPI ship = (ShipAPI) stats.getEntity();
			ship.fadeToColor(KEY_SHIP, new Color(75,75,75,255), 1f, 0.1f, effectLevel);
		}
		float bonusPercent = DAMAGE_BONUS_PERCENT * effectLevel;
		stats.getEnergyWeaponDamageMult().modifyPercent(id, bonusPercent);
	}
	
	public void unapply(MutableShipStatsAPI stats, String id) {
		stats.getEnergyWeaponDamageMult().unmodify(id);
	}
	
	public StatusData getStatusData(int index, State state, float effectLevel) {
		if (index == 0) {
			String bonus = "+" + (int) DAMAGE_BONUS_PERCENT + "% energy weapon damage";
			return new StatusData(bonus, false);	
		}
		return null;
	}
}