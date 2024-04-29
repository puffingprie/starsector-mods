package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class keruvim_pride_bulkheads extends BaseHullMod {

	public static final float CASUALTY_REDUCTION = 25f;


	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getCrewLossMult().modifyMult(id, 1f - CASUALTY_REDUCTION * 0.1f);
		stats.getDynamic().getMod(Stats.INDIVIDUAL_SHIP_RECOVERY_MOD).modifyFlat(id, 500f);
		stats.getBreakProb().modifyMult(id, 0f);
	}



	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) CASUALTY_REDUCTION + "%";
		return null;
	}

	public boolean isApplicableToShip(ShipAPI ship) {
		return ship != null && (ship.getHullSpec().getNoCRLossTime() < 10000 || ship.getHullSpec().getCRLossPerSecond() > 0);
	}


}



