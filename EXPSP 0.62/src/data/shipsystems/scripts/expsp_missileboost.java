package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

public class expsp_missileboost extends BaseShipSystemScript {
	public static final float SPEED_BONUS = 0.2f;
	public static final float DAMAGE_BONUS_PERCENT = 1.1f;
	public static final float EXTRA_FLUX = 75f;
	public static final float TURN_MALUS = 0.5f;
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		float mult = 1f + SPEED_BONUS * effectLevel;
		float bonusPercent = DAMAGE_BONUS_PERCENT * effectLevel;
		float malus= 1f-TURN_MALUS*effectLevel;
		stats.getMissileMaxSpeedBonus().modifyMult(id, mult);
		stats.getMissileAccelerationBonus().modifyMult(id,mult);
		stats.getMissileWeaponDamageMult().modifyMult(id, bonusPercent);
		stats.getMissileTurnAccelerationBonus().modifyMult(id,malus);
		stats.getMissileGuidance().modifyMult(id,malus);
		stats.getMissileWeaponFluxCostMod().modifyFlat(id,EXTRA_FLUX);

		//stats.getBallisticWeaponFluxCostMod().modifyMult(id, 1f - (FLUX_REDUCTION * 0.01f));
		//stats.getEnergyWeaponRangeBonus().modifyPercent(id, bonusPercent);
		
		//float damageTakenPercent = EXTRA_DAMAGE_TAKEN_PERCENT * effectLevel;
//		stats.getArmorDamageTakenMult().modifyPercent(id, damageTakenPercent);
//		stats.getHullDamageTakenMult().modifyPercent(id, damageTakenPercent);
//		stats.getShieldDamageTakenMult().modifyPercent(id, damageTakenPercent);
		//stats.getWeaponDamageTakenMult().modifyPercent(id, damageTakenPercent);
		//stats.getEngineDamageTakenMult().modifyPercent(id, damageTakenPercent);
		
		//stats.getBeamWeaponFluxCostMult().modifyMult(id, 10f);
	}
	public void unapply(MutableShipStatsAPI stats, String id) {
		stats.getMissileMaxSpeedBonus().unmodify(id);
		stats.getMissileGuidance().unmodify(id);
		stats.getMissileAccelerationBonus().unmodify(id);
	stats.getMissileTurnAccelerationBonus().unmodify(id);
		stats.getMissileWeaponFluxCostMod().unmodify(id);
		stats.getMissileWeaponDamageMult().unmodify(id);
//		stats.getShieldDamageTakenMult().unmodify(id);
//		stats.getWeaponDamageTakenMult().unmodify(id);
//		stats.getEngineDamageTakenMult().unmodify(id);
	}
	
	public StatusData getStatusData(int index, State state, float effectLevel) {
		float bonusPercent = DAMAGE_BONUS_PERCENT * effectLevel;
		float boostPercent = SPEED_BONUS* effectLevel;
		float malusPercent= TURN_MALUS*effectLevel;
		if (index == 0) {
			return new StatusData("+" + (int) 20 + "% missile max speed and acceleration" , false);
		} else if (index == 1) {

			return new StatusData("+" + (int) 10 + "% missile weapon damage" , false);
		} else if (index == 2) {
			return new StatusData(75+ " extra flux per missile fired" , true);

		}else if (index==3){
			return new StatusData("-" + (int) malusPercent + "% reduced missile turn acceleration and guidance" , true);
		}
		return null;
	}
}
