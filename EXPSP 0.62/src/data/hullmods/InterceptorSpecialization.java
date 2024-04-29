package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

import java.util.HashMap;
import java.util.Map;

public class InterceptorSpecialization extends BaseHullMod {

	public static final int CREW_REQ = 20;
	//public static final int CARGO_REQ = 80;
	public static final int ALL_FIGHTER_COST_PERCENT = 25;
	
	public static final int INTERCEPTOR_COST_PERCENT = -25;
	
	public static final int BOMBER_COST_PERCENT = 100;
	
	private static Map mag = new HashMap();
	static {
		mag.put(HullSize.FRIGATE, 0f);
		mag.put(HullSize.DESTROYER, 75f);
		mag.put(HullSize.CRUISER, 50f);
		mag.put(HullSize.CAPITAL_SHIP, 25f);
	}
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		//stats.getFighterRefitTimeMult().modifyPercent(id, ((Float) mag.get(hullSize)));
		//stats.getNumFighterBays().modifyFlat(id, 1f);

		//stats.getMinCrewMod().modifyFlat(id, CREW_REQ);
		//stats.getDynamic().getMod(Stats.ALL_FIGHTER_COST_MOD).modifyPercent(id, ALL_FIGHTER_COST_PERCENT);
		stats.getDynamic().getMod(Stats.BOMBER_COST_MOD).modifyPercent(id, BOMBER_COST_PERCENT);
		stats.getDynamic().getMod(Stats.FIGHTER_COST_MOD).modifyPercent(id, ALL_FIGHTER_COST_PERCENT);
		stats.getDynamic().getMod(Stats.INTERCEPTOR_COST_MOD).modifyPercent(id, INTERCEPTOR_COST_PERCENT);
		stats.getDynamic().getMod(Stats.SUPPORT_COST_MOD).modifyPercent(id, ALL_FIGHTER_COST_PERCENT);
		//stats.getCargoMod().modifyFlat(id, -CARGO_REQ);
	}
	
	
	
	public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
		//if (index == 2) return "" + CREW_REQ;
		if (index == 0) return "" + BOMBER_COST_PERCENT + "%";
		if (index == 1) return "" + ALL_FIGHTER_COST_PERCENT + "%";
		if (index == 2) return "" + ALL_FIGHTER_COST_PERCENT + "%";
//		return new DefectiveManufactory().getDescriptionParam(index, hullSize, ship);
//		if (index == 0) return "" + ((Float) mag.get(HullSize.DESTROYER)).intValue() + "%";
//		if (index == 1) return "" + ((Float) mag.get(HullSize.CRUISER)).intValue() + "%";
//		if (index == 2) return "" + ((Float) mag.get(HullSize.CAPITAL_SHIP)).intValue() + "%";
//		if (index == 3) return "" + CREW_REQ;
		return null;
		//if (index == 0) return "" + ((Float) mag.get(hullSize)).intValue();
		//return null;
	}
	
	@Override
	public boolean affectsOPCosts() {
		return true;
	}
}



