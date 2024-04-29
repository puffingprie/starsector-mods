package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

public class TimeFocusStats extends BaseShipSystemScript {

	public static final float DAMAGE_BONUS_PERCENT = 600f;
	public static final float BEAM_FLUX_BONUS_PERCENT = 200f;

	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		
		float bonusPercent = DAMAGE_BONUS_PERCENT * effectLevel;
		float beambonusPercent = BEAM_FLUX_BONUS_PERCENT * effectLevel;

		stats.getBeamWeaponDamageMult().modifyPercent(id, beambonusPercent);
		stats.getBeamWeaponFluxCostMult().modifyPercent(id, beambonusPercent);
		stats.getEnergyRoFMult().modifyPercent(id, bonusPercent);
		stats.getFluxDissipation().modifyPercent(id, bonusPercent);
	}
	public void unapply(MutableShipStatsAPI stats, String id) {
		stats.getBeamWeaponDamageMult().unmodify(id);
		stats.getBeamWeaponFluxCostMult().unmodify(id);
		stats.getEnergyRoFMult().unmodify(id);
		stats.getFluxDissipation().unmodify(id);
	}
	
	public StatusData getStatusData(int index, State state, float effectLevel) {
		float bonusPercent = DAMAGE_BONUS_PERCENT * effectLevel;
		if (index == 0) {
			return new StatusData("+" + (int) bonusPercent + "% time dilation" , false);
		} else if (index == 1) {
			//return new StatusData("+" + (int) damageTakenPercent + "% weapon/engine damage taken", false);
			return null;
		} else if (index == 2) {
			//return new StatusData("shield damage taken +" + (int) damageTakenPercent + "%", true);
			return null;
		}
		return null;
	}
}
