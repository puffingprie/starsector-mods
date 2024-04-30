package data.hullmods.ix;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class PointDefenseSmallHandler extends BaseHullMod {
	
	private static String OTHER_MOD = "ix_point_defense_medium";
	private static String OTHER_MOD_HANDLER = "ix_point_defense_medium_handler";
	private static String THIS_MOD = "ix_point_defense_small";
	
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		if (!stats.getVariant().hasHullMod(THIS_MOD)) {
			stats.getVariant().addMod(OTHER_MOD);
			stats.getVariant().addMod(OTHER_MOD_HANDLER);
			stats.getVariant().getHullMods().remove(id);
		}
	}
}