package data.hullmods.vice;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class AbominationInterfacePenalty extends BaseHullMod {
	
	private static String INTERFACE_MOD = "vice_abomination_interface";
	
	//self delete if other modded content removes built-in INTERFACE_MOD
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		if (!stats.getVariant().hasHullMod(INTERFACE_MOD)) stats.getVariant().getHullMods().remove(id);
	}
}