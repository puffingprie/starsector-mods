package data.hullmods.ix;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class PanopticInterfaceCommandHandler extends BaseHullMod {
	
	private static String COMMAND_MOD_ID = "ix_panoptic_command";
	private static String COMMAND_CORE_ID = "ix_command_core";
	
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		
		if (!stats.getVariant().hasHullMod(COMMAND_MOD_ID) 
					&& stats.getFleetMember() != null 
					&& COMMAND_CORE_ID.equals(stats.getFleetMember().getCaptain().getAICoreId())) {
			stats.getFleetMember().setCaptain(Global.getFactory().createPerson());
		}
	}
}