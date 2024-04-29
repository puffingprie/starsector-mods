package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

public class expsp_DefenseFocusStats extends BaseShipSystemScript {
	public static final float ROF_BONUS = 0.5f;
	public static final float FLUX_REDUCTION = 20f;

	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		//stats.getShieldTurnRateMult().modifyMult(id, 1f);
		//stats.getShieldUnfoldRateMult().modifyPercent(id, 2000);

		//stats.getShieldDamageTakenMult().modifyMult(id, 0.1f);
		stats.getShieldDamageTakenMult().modifyMult(id, 1f - .5f * effectLevel);
		//stats.getShieldUpkeepMult().modifyMult(id,4f*effectLevel);
		//stats.getShieldUpkeepMult().modifyMult(id, 0f);
		//stats.getShieldArcBonus().modifyPercent(id,-50f*effectLevel);
		stats.getMaxSpeed().modifyMult(id,0.5f);
		float mult = 1f + ROF_BONUS * effectLevel;
		stats.getBallisticRoFMult().modifyMult(id, 0.8f);
		stats.getEnergyRoFMult().modifyMult(id, 0.8f);
		stats.getDamageToFighters().modifyMult(id,mult);
		stats.getDamageToMissiles().modifyMult(id,mult);
		stats.getBeamWeaponDamageMult().modifyMult(id,0.8f);
		stats.getWeaponTurnRateBonus().modifyMult(id,mult);
		//stats.getEnergyWeaponFluxCostMod().modifyMult(id, 1f - (FLUX_REDUCTION * 0.01f));
		//stats.getBallisticWeaponFluxCostMod().modifyMult(id, 1f - (FLUX_REDUCTION * 0.01f));
		//System.out.println("level: " + effectLevel);
	}
	
	public void unapply(MutableShipStatsAPI stats, String id) {
		//stats.getShieldAbsorptionMult().unmodify(id);
		//stats.getShieldArcBonus().unmodify(id);
		stats.getShieldDamageTakenMult().unmodify(id);
		//stats.getShieldTurnRateMult().unmodify(id);
		//stats.getShieldUnfoldRateMult().unmodify(id);
		//stats.getShieldUpkeepMult().unmodify(id);
		stats.getBallisticRoFMult().unmodify(id);
		stats.getEnergyRoFMult().unmodify(id);
		//stats.getBallisticWeaponFluxCostMod().unmodify(id);
		stats.getBeamWeaponDamageMult().unmodify(id);
		stats.getDamageToMissiles().unmodify(id);
		stats.getDamageToMissiles().unmodify(id);
		stats.getMaxSpeed().unmodify(id);
	}
	
	public StatusData getStatusData(int index, State state, float effectLevel) {
		if (index == 0) {
			return new StatusData("shield damage taken reduced by 50%", false);
		}
		else if (index == 1) {
			return new StatusData("Weapon ROF reduced by "+ 0.2f*100*effectLevel+"%", true);
		} else if (index == 2) {
			return new StatusData("movement speed halved", true);
		}else if (index == 3) {
			return new StatusData("Damage to missiles and fighters x"+((ROF_BONUS+1)*effectLevel*100)+"%", false);

		}else if (index == 4) {
			return new StatusData("Weapon turn rate increased by 50%",false);
		}
		return null;
	}
}
