package data.hullmods.ix;

import java.util.List;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.DModManager;
import com.fs.starfarer.api.loading.HullModSpecAPI;

public class DModHandler extends BaseHullMod {

	//workaround for nex invasion support strike fleets spawning with d-mods
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		clearDModsFromStrikeFleetShip(stats);
	}
	
	public static void clearDModsFromStrikeFleetShip(MutableShipStatsAPI stats) {
		if (stats.getVariant().hasDMods()) {
			try {
				String fleetName = stats.getFleetMember().getFleetData().getFleet().getNameWithFaction();
				if (fleetName.toLowerCase().startsWith("ix b") && fleetName.toLowerCase().endsWith("strike fleet")) {
					List<HullModSpecAPI> dMods = DModManager.getModsWithTags("dmod");
					for (HullModSpecAPI mod : dMods) {
						stats.getVariant().getHullMods().remove(mod.getId());
					}
				}
			}
			catch (Exception e) {}
		}
	}
}