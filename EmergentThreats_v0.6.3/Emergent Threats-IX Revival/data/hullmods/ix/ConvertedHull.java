package data.hullmods.ix;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class ConvertedHull extends BaseHullMod {

	private static float MIN_CREW = 100f;
	private static float MAX_CREW = 150f;
	//private static float SHIELD_PENALTY = 16.6666f;
	private static float SHIELD_PENALTY = 33.3333f;
	private static int SYSTEM_CHARGES_PENALTY = 1; //text only, value set by ship system in ships.csv
	
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getMinCrewMod().modifyFlat(id, MIN_CREW);
		stats.getMaxCrewMod().modifyFlat(id, MAX_CREW);
		stats.getShieldDamageTakenMult().modifyMult(id, 1f + SHIELD_PENALTY * 0.01f);
	}

	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) MIN_CREW;
		if (index == 1) return "" + (int) MAX_CREW;
		if (index == 2) return "" + (int) Math.ceil(SHIELD_PENALTY) + "%";
		if (index == 3) return "" + SYSTEM_CHARGES_PENALTY;
		return null;
	}
}