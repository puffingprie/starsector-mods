package data.hullmods.ix;

import java.util.Iterator;
import java.util.List;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.AIHints;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class PointDefenseSmall extends BaseHullMod {

	private static float DAMAGE_BONUS = 50f;
	private static float TURN_RATE = 100f;
	private static String CONFLICT_MOD = "pointdefenseai";
	private static String CONFLICT_MOD_NAME = "Integrated Point Defense AI";
	
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getDynamic().getMod(Stats.PD_IGNORES_FLARES).modifyFlat(id, 1f);
		stats.getDynamic().getMod(Stats.PD_BEST_TARGET_LEADING).modifyFlat(id, 1f);
		stats.getDamageToMissiles().modifyPercent(id, DAMAGE_BONUS);
		stats.getVariant().getHullMods().remove(CONFLICT_MOD);
	}
	
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		List<WeaponAPI> weapons = ship.getAllWeapons();
		Iterator<WeaponAPI> iter = weapons.iterator();
		while (iter.hasNext()) {
			WeaponAPI weapon = (WeaponAPI)iter.next();
			if (weapon.getSize() == WeaponSize.SMALL
						&& weapon.getType() == WeaponType.ENERGY 
						&& !weapon.hasAIHint(AIHints.STRIKE)) {
				weapon.setPD(true);
				//if (weapon.getTurnRate() < TURN_RATE) weapon.setTurnRateOverride(TURN_RATE);
			}
		}
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return CONFLICT_MOD_NAME;
		return null;
	}
}