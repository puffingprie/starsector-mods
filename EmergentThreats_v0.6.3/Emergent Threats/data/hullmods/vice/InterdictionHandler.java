package data.hullmods.vice;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class InterdictionHandler extends BaseHullMod {

	private static String INTERDICTOR_HULLMOD = "vice_interdiction_array";
	private static String SYSTEM_INTERDICTOR = "vice_interdictor";
	private static String SYSTEM_FORTRESS = "fortressshield";
	private static String SYSTEM_ZEUS = "swp_boss_zeusshield";
	private static String ZEUS_ID = "swp_boss_paragon";
	
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		if (stats.getVariant().hasHullMod("ehm_base") 
				&& !stats.getVariant().getHullSpec().getShipSystemId().equals(SYSTEM_INTERDICTOR)
				&& !stats.getVariant().getHullSpec().getShipSystemId().equals(SYSTEM_FORTRESS)
				&& !stats.getVariant().getHullSpec().getShipSystemId().equals(SYSTEM_ZEUS)) return;
		if (stats.getVariant().getHullSpec().getHullId().equals(ZEUS_ID)) applyZeus(stats);
		else {
			String system = stats.getVariant().hasHullMod(INTERDICTOR_HULLMOD) ? SYSTEM_INTERDICTOR : SYSTEM_FORTRESS;
			stats.getVariant().getHullSpec().setShipSystemId(system);
		}
	}
	
	public void applyZeus(MutableShipStatsAPI stats) {
		String system = stats.getVariant().hasHullMod(INTERDICTOR_HULLMOD) ? SYSTEM_INTERDICTOR : SYSTEM_ZEUS;
		stats.getVariant().getHullSpec().setShipSystemId(system);
	}
}