package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

public class BombardmentModeStats extends BaseShipSystemScript {
	public static final float ROF_BONUS = 0.2f;
	public static final float FLUX_REDUCTION = 20f;

	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		//stats.getShieldTurnRateMult().modifyMult(id, 1f);
		//stats.getShieldUnfoldRateMult().modifyPercent(id, 2000);

		//stats.getShieldDamageTakenMult().modifyMult(id, 0.1f);
		stats.getShieldDamageTakenMult().modifyMult(id, 1f - .2f * effectLevel);
		stats.getShieldUpkeepMult().modifyMult(id,4f*effectLevel);
		//stats.getShieldUpkeepMult().modifyMult(id, 0f);
		stats.getShieldArcBonus().modifyPercent(id,-50f*effectLevel);
		stats.getMaxSpeed().modifyFlat(id,-50f);
		float mult = 1f + ROF_BONUS * effectLevel;
		stats.getBallisticRoFMult().modifyMult(id, mult);
		stats.getEnergyRoFMult().modifyMult(id, mult);
		stats.getBeamWeaponDamageMult().modifyMult(id,mult);
		stats.getEnergyWeaponFluxCostMod().modifyMult(id, 1f - (FLUX_REDUCTION * 0.01f));
		stats.getBallisticWeaponFluxCostMod().modifyMult(id, 1f - (FLUX_REDUCTION * 0.01f));
		//System.out.println("level: " + effectLevel);
	}
	
	public void unapply(MutableShipStatsAPI stats, String id) {
		//stats.getShieldAbsorptionMult().unmodify(id);
		stats.getShieldArcBonus().unmodify(id);
		stats.getShieldDamageTakenMult().unmodify(id);
		stats.getShieldTurnRateMult().unmodify(id);
		stats.getShieldUnfoldRateMult().unmodify(id);
		stats.getShieldUpkeepMult().unmodify(id);
		stats.getBallisticRoFMult().unmodify(id);
		stats.getEnergyRoFMult().unmodify(id);
		stats.getBallisticWeaponFluxCostMod().unmodify(id);
		stats.getMaxSpeed().unmodify(id);
	}
	
	public StatusData getStatusData(int index, State state, float effectLevel) {
		if (index == 0) {
			return new StatusData("shield damage taken reduced by 20%", false);
		}
	else if (index == 1) {
		return new StatusData("shield upkeep quadrupled", true);
		} else if (index == 2) {
		return new StatusData("movement speed reduced by 50", true);
		}else if (index == 3) {
			return new StatusData("Non-beam, non missile weapon ROF +20%", false);
		}else if (index == 4) {
			return new StatusData("Non-beam, non missile weapon flux cost -20%", false);
		}else if (index == 5) {
			return new StatusData("Beam weapon damage +20%", false);
		}
		return null;
	}
}
