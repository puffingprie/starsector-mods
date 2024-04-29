package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class HMICommissionedCrews extends BaseHullMod {

	public static final float CASUALTY_REDUCTION = 25f;
	public static final float REPAIR_BONUS = 20f;
	private static final float SUPPLY_USE_MULT = 5f;
	private final String CREW="CHM_commission";

	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getCrewLossMult().modifyMult(id, 1f - CASUALTY_REDUCTION * 0.01f);
		stats.getDynamic().getStat(Stats.FIGHTER_CREW_LOSS_MULT).modifyMult(id, 1f - CASUALTY_REDUCTION * 0.01f);

		stats.getSuppliesToRecover().modifyPercent(id, 1f - REPAIR_BONUS* 0.01f);

		stats.getFuelUseMod().modifyMult(id, 1f - SUPPLY_USE_MULT* 0.01f);
		stats.getSuppliesPerMonth().modifyMult(id, 1f - SUPPLY_USE_MULT* 0.01f);

	}

	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id){
		if (ship.getVariant().getHullMods().contains(CREW)) {
			ship.getVariant().removeMod(CREW);
		}
	}
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) SUPPLY_USE_MULT + "%";
		if (index == 1) return "" + (int) REPAIR_BONUS + "%";
		if (index == 2) return "" + (int) CASUALTY_REDUCTION+ "%";
		return null;
	}


}
