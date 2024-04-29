package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class TechminedHull extends BaseHullMod {

	public static final float DEGRADE_INCREASE_PERCENT = 100f;
	public static final float CORONA_PERCENT = 0.50f;
	public static final float REPAIR_RATE_BONUS = 66f;
	public static final float REPAIR_RATE_BONUS2 = 200f;
	public static final float CR_RECOVERY_BONUS = 50f;

	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getCRLossPerSecondPercent().modifyPercent(id, DEGRADE_INCREASE_PERCENT);

		stats.getBaseCRRecoveryRatePercentPerDay().modifyMult(id, (0.01f * CR_RECOVERY_BONUS));
		stats.getRepairRatePercentPerDay().modifyPercent(id, (-REPAIR_RATE_BONUS));

		stats.getDynamic().getStat(Stats.CORONA_EFFECT_MULT).modifyMult(id, (1f + CORONA_PERCENT));
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) DEGRADE_INCREASE_PERCENT + "%";
		if (index == 1) return "" + (int) Math.round(REPAIR_RATE_BONUS2) + "%";
		if (index == 2) return "" + (int) Math.round((1f + CORONA_PERCENT) * 100f) + "%";
		return null;
	}

}
