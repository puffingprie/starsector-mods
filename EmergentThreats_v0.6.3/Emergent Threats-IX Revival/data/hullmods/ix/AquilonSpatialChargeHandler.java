package data.hullmods.ix;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class AquilonSpatialChargeHandler extends BaseHullMod {
	
	//active flares -> starfall rockets -> spatial charges
	private static String NEXT_MOD = "ix_aquilon_fl";
	private static String NEXT_MOD_HANDLER = "ix_aquilon_fl_handler";
	private static String THIS_MOD = "ix_aquilon_sc";
	private static String CONFLICT_MOD = "ix_aquilon_sr";
	private static String CONFLICT_MOD_HANDLER = "ix_aquilon_sr_handler";
	
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		if (!stats.getVariant().hasHullMod(THIS_MOD)) {
			stats.getVariant().getHullMods().remove(CONFLICT_MOD_HANDLER);
			stats.getVariant().getHullMods().remove(CONFLICT_MOD);
			stats.getVariant().addMod(NEXT_MOD);
			stats.getVariant().addMod(NEXT_MOD_HANDLER);
			stats.getVariant().getHullMods().remove(id);
		}
	}
}