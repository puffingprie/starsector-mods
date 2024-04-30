package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

public class mmstandardfeedstats extends BaseShipSystemScript {

	public static final float ROF_BONUS = 1.4f;
	public static final float FLUX_REDUCTION = 45f;
	public static final float REGEN_BONUS = 40f;
	//public static float RANGE_BONUS = 12f;
	
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		
		float mult = 1f + ROF_BONUS * effectLevel;
		stats.getBallisticRoFMult().modifyMult(id, mult);
		stats.getBallisticWeaponFluxCostMod().modifyMult(id, 1f - (FLUX_REDUCTION * 0.01f));
		stats.getBallisticAmmoRegenMult().modifyPercent(id, REGEN_BONUS);
		//stats.getBallisticWeaponRangeBonus().modifyPercent(id, RANGE_BONUS);

	}
	public void unapply(MutableShipStatsAPI stats, String id) {
		stats.getBallisticRoFMult().unmodify(id);
		stats.getBallisticWeaponFluxCostMod().unmodify(id);
		stats.getBallisticAmmoRegenMult().unmodify(id);
		//stats.getBallisticWeaponRangeBonus().unmodify(id);
	}
	
	public StatusData getStatusData(int index, State state, float effectLevel) {
		float mult = 1f + ROF_BONUS * effectLevel;
		float bonusPercent = (int) ((mult - 1f) * 100f);
		if (index == 0) {
			return new StatusData("ballistic rate of fire +" + (int) bonusPercent + "%", false);
		}
		if (index == 1) {
			return new StatusData("ballistic flux use -" + (int) FLUX_REDUCTION + "%", false);
		}
		if (index == 2) {
			return new StatusData("ballistic ammo regen rate -" + (int) REGEN_BONUS + "%", false);
		}
		//if (index == 2) {
		//	return new StatusData("ballistic range -" + (int) RANGE_BONUS + "%", false);
		//}
		return null;
	}
}
