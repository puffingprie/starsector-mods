package data.scripts.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

import com.fs.starfarer.api.impl.campaign.ids.Stats;

import java.util.HashMap;
import java.util.Map;

public class acs_remotedroneshipcommand extends BaseHullMod {

	private static Map mag = new HashMap();
	static {
		mag.put(HullSize.FRIGATE, 0.5f);
		mag.put(HullSize.DESTROYER, 1f);
		mag.put(HullSize.CRUISER, 1.5f);
		mag.put(HullSize.CAPITAL_SHIP, 2f);
	}
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getDynamic().getMod(Stats.ELECTRONIC_WARFARE_FLAT).modifyFlat(id, (Float) mag.get(hullSize));
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + ((Float) mag.get(HullSize.FRIGATE)).intValue() + "%";
		if (index == 1) return "" + ((Float) mag.get(HullSize.DESTROYER)).intValue() + "%";
		if (index == 2) return "" + ((Float) mag.get(HullSize.CRUISER)).intValue() + "%";
		if (index == 3) return "" + ((Float) mag.get(HullSize.CAPITAL_SHIP)).intValue() + "%";
		return null;
	}


}

// package data.scripts.campaign.ids;

// public class acs_ids {

// 	public static final String ACS_HULLMOD_DRIFTERLOGISTIC = "acs_drifterlogistic_hullmod";

//     public static final String ACS_HULLMOD_REMOTEDRONESHIPCOMMAND = "acs_remotedroneshipcommand_hullmod";

// }