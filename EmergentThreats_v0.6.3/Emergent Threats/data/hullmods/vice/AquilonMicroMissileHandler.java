package data.hullmods.vice;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class AquilonMicroMissileHandler extends BaseHullMod {
	
	//active flares -> micro-missiles -> spatial charges
	private static String NEXT_MOD = "vice_aquilon_sc";
	private static String NEXT_MOD_HANDLER = "vice_aquilon_sc_handler";
	private static String THIS_MOD = "vice_aquilon_mm";
	private static String CONFLICT_MOD = "vice_aquilon_fl";
	private static String CONFLICT_MOD_HANDLER = "vice_aquilon_fl_handler";
	
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