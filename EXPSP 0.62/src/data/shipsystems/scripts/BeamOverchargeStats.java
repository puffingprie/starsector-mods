package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

public class BeamOverchargeStats extends BaseShipSystemScript {

	public static final float FLUX_REDUCTION = 50f;
	private static final float DAMAGE_BONUS_PERCENT = 30f;

	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		
		float bonusPercent = DAMAGE_BONUS_PERCENT * effectLevel;
		float fluxBonus= FLUX_REDUCTION*effectLevel;
		stats.getBeamWeaponDamageMult().modifyPercent(id, bonusPercent);
		stats.getEnergyWeaponDamageMult().modifyPercent(id, bonusPercent/2);
		stats.getBeamWeaponFluxCostMult().modifyMult(id, 1f - (FLUX_REDUCTION * 0.01f));
		stats.getEnergyWeaponFluxCostMod().modifyMult(id, 1f - (FLUX_REDUCTION * 0.005f));
		stats.getBallisticWeaponFluxCostMod().modifyMult(id, 1f - (FLUX_REDUCTION * 0.005f));
		stats.getBallisticWeaponDamageMult().modifyPercent(id, bonusPercent/2);
		stats.getMaxSpeed().modifyFlat(id,25f);
		//float damageTakenPercent = EXTRA_DAMAGE_TAKEN_PERCENT * effectLevel;
//		stats.getArmorDamageTakenMult().modifyPercent(id, damageTakenPercent);
//		stats.getHullDamageTakenMult().modifyPercent(id, damageTakenPercent);
//		stats.getShieldDamageTakenMult().modifyPercent(id, damageTakenPercent);
		//stats.getWeaponDamageTakenMult().modifyPercent(id, damageTakenPercent);
		//stats.getEngineDamageTakenMult().modifyPercent(id, damageTakenPercent);
		
		//stats.getBeamWeaponFluxCostMult().modifyMult(id, 10f);
	}
	public void unapply(MutableShipStatsAPI stats, String id) {
		stats.getEnergyWeaponDamageMult().unmodify(id);
		stats.getEnergyWeaponFluxCostMod().unmodify(id);
		stats.getBeamWeaponDamageMult().unmodify(id);
		stats.getBeamWeaponFluxCostMult().unmodify(id);
		stats.getBallisticWeaponDamageMult().unmodify(id);
		stats.getBallisticWeaponFluxCostMod().unmodify(id);
		stats.getMaxSpeed().unmodify(id);
//		stats.getEnergyWeaponRangeBonus().unmodify(id);
//		stats.getArmorDamageTakenMult().unmodify(id);
//		stats.getHullDamageTakenMult().unmodify(id);
//		stats.getShieldDamageTakenMult().unmodify(id);
//		stats.getWeaponDamageTakenMult().unmodify(id);
//		stats.getEngineDamageTakenMult().unmodify(id);
	}
	
	public StatusData getStatusData(int index, State state, float effectLevel) {
		float bonusPercent = DAMAGE_BONUS_PERCENT * effectLevel;
		//float damageTakenPercent = EXTRA_DAMAGE_TAKEN_PERCENT * effectLevel;
		if (index == 0) {
			return new StatusData("+" + (int) bonusPercent + "% Beam Weapon Damage, Other weapon damage increased by half" , false);
		} else if (index == 1) {
			return new StatusData("Beam Weapon flux cost -50%, Other weapons flux cost -25%", false);
		} else if (index == 2) {
			return new StatusData("+25 Speed", false);

		}
		return null;
	}
}
