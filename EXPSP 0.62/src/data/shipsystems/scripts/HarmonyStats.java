package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class HarmonyStats extends BaseShipSystemScript {

	public static final Object KEY_JITTER = new Object();
	public static final float MAX_SPEED_BUFF = 75;
	public static final float DAMAGE_INCREASE_PERCENT = 20;
	static final float DR_PERCENT = 15;
	public static final float SPEED_INCREASE_PERCENT = 20;
	public static final Color JITTER_UNDER_COLOR = new Color(155,250,250,125);
	public static final Color JITTER_COLOR = new Color(155,250,250,75);

	
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		ShipAPI ship = null;
		stats.getShieldDamageTakenMult().modifyMult(id,1f - 0.01f*DR_PERCENT * effectLevel);
		stats.getArmorDamageTakenMult().modifyMult(id,1f - 0.01f*DR_PERCENT * effectLevel);
		stats.getHullDamageTakenMult().modifyMult(id,1f - 0.01f*DR_PERCENT * effectLevel);
		stats.getMaxSpeed().modifyFlat(id, MAX_SPEED_BUFF * effectLevel);
		stats.getShieldUpkeepMult().modifyMult(id, 0f);
		stats.getEnergyWeaponDamageMult().modifyMult(id,1f - 0.01f*DAMAGE_INCREASE_PERCENT * effectLevel);
		stats.getBallisticWeaponDamageMult().modifyMult(id,1f - 0.01f*DAMAGE_INCREASE_PERCENT * effectLevel);
		stats.getBeamWeaponDamageMult().modifyMult(id,1f - 0.01f*DAMAGE_INCREASE_PERCENT * effectLevel);
		if (stats.getEntity() instanceof ShipAPI) {
			ship = (ShipAPI) stats.getEntity();
		} else {
			return;
		}

		
		if (effectLevel > 0) {
			float jitterLevel = effectLevel;
			float maxRangeBonus = 5f;
			float jitterRangeBonus = jitterLevel * maxRangeBonus;
			for (ShipAPI fighter : getFighters(ship)) {
				if (fighter.isHulk()) continue;
				MutableShipStatsAPI fStats = fighter.getMutableStats();
//				fStats.getBallisticWeaponDamageMult().modifyPercent(id, DAMAGE_INCREASE_PERCENT * effectLevel);
//				fStats.getEnergyWeaponDamageMult().modifyPercent(id, DAMAGE_INCREASE_PERCENT * effectLevel);
//				fStats.getMissileWeaponDamageMult().modifyPercent(id, DAMAGE_INCREASE_PERCENT * effectLevel);


				//fStats.getBallisticWeaponDamageMult().modifyMult(id, 1f + 0.01f * DAMAGE_INCREASE_PERCENT * effectLevel);
				//fStats.getEnergyWeaponDamageMult().modifyMult(id, 1f + 0.01f * DAMAGE_INCREASE_PERCENT * effectLevel);
				//fStats.getMissileWeaponDamageMult().modifyMult(id, 1f + 0.01f * DAMAGE_INCREASE_PERCENT * effectLevel);
				fStats.getMaxSpeed().modifyMult(id, 1f + 0.01f * SPEED_INCREASE_PERCENT * effectLevel);
				fStats.getProjectileDamageTakenMult().modifyMult(id,1f - 0.01f*DR_PERCENT * effectLevel);
				fStats.getBeamDamageTakenMult().modifyMult(id,1f - 0.01f*DR_PERCENT * effectLevel);
				fStats.getArmorDamageTakenMult().modifyMult(id,1f - 0.01f*DR_PERCENT * effectLevel);
				fStats.getEnergyWeaponDamageMult().modifyMult(id,1f - 0.01f*DAMAGE_INCREASE_PERCENT * effectLevel);
				fStats.getBallisticWeaponDamageMult().modifyMult(id,1f - 0.01f*DAMAGE_INCREASE_PERCENT * effectLevel);
				fStats.getBeamWeaponDamageMult().modifyMult(id,1f - 0.01f*DAMAGE_INCREASE_PERCENT * effectLevel);
				fStats.getMissileWeaponDamageMult().modifyMult(id,1f - 0.01f*DAMAGE_INCREASE_PERCENT * effectLevel);
				if (jitterLevel > 0) {
					//fighter.setWeaponGlow(effectLevel, new Color(255,50,0,125), EnumSet.allOf(WeaponType.class));
					fighter.setWeaponGlow(effectLevel, Misc.setAlpha(JITTER_UNDER_COLOR, 50), EnumSet.allOf(WeaponType.class));
					
					fighter.setJitterUnder(KEY_JITTER, JITTER_COLOR, jitterLevel, 4, 0f, jitterRangeBonus);
					fighter.setJitter(KEY_JITTER, JITTER_UNDER_COLOR, jitterLevel, 1, 0f, 0 + jitterRangeBonus * 1f);
					Global.getSoundPlayer().playLoop("system_targeting_feed_loop", ship, 1f, 1f, fighter.getLocation(), fighter.getVelocity());
				}
			}
		}
	}
	
	private List<ShipAPI> getFighters(ShipAPI carrier) {
		List<ShipAPI> result = new ArrayList<ShipAPI>();
		
//		this didn't catch fighters returning for refit		
//		for (FighterLaunchBayAPI bay : carrier.getLaunchBaysCopy()) {
//			if (bay.getWing() == null) continue;
//			result.addAll(bay.getWing().getWingMembers());
//		}
		
		for (ShipAPI ship : Global.getCombatEngine().getShips()) {
			if (!ship.isFighter()) continue;
			if (ship.getWing() == null) continue;
			if (ship.getWing().getSourceShip() == carrier) {
				result.add(ship);
			}
		}
		
		return result;
	}
	
	
	public void unapply(MutableShipStatsAPI stats, String id) {
		ShipAPI ship = null;
		stats.getMaxSpeed().unmodify(id);
		stats.getShieldArcBonus().unmodify(id);
		stats.getShieldDamageTakenMult().unmodify(id);
		stats.getArmorDamageTakenMult().unmodify(id);
		stats.getHullDamageTakenMult().unmodify(id);
		stats.getBeamWeaponDamageMult().unmodify(id);
		stats.getEnergyWeaponDamageMult().unmodify(id);
		stats.getBallisticWeaponDamageMult().unmodify(id);
		stats.getShieldTurnRateMult().unmodify(id);
		stats.getShieldUnfoldRateMult().unmodify(id);
		stats.getShieldUpkeepMult().unmodify(id);
		if (stats.getEntity() instanceof ShipAPI) {
			ship = (ShipAPI) stats.getEntity();
		} else {
			return;
		}
		for (ShipAPI fighter : getFighters(ship)) {
			if (fighter.isHulk()) continue;
			MutableShipStatsAPI fStats = fighter.getMutableStats();
			fStats.getBallisticWeaponDamageMult().unmodify(id);
			fStats.getEnergyWeaponDamageMult().unmodify(id);
			fStats.getBeamWeaponDamageMult().unmodify(id);
			fStats.getMissileWeaponDamageMult().unmodify(id);
			fStats.getMaxSpeed().unmodify(id);
			fStats.getBeamDamageTakenMult().unmodify(id);
			fStats.getArmorDamageTakenMult().unmodify(id);
			fStats.getProjectileDamageTakenMult().unmodify(id);
		}
	}
	
	
	public StatusData getStatusData(int index, State state, float effectLevel) {
		float percent = DAMAGE_INCREASE_PERCENT * effectLevel;
		if (index == 0) {
			//return new StatusData("+" + (int)percent + "% fighter damage", false);
			return new StatusData("" + Misc.getRoundedValueMaxOneAfterDecimal(DAMAGE_INCREASE_PERCENT * effectLevel) + "% Ship and fighter damage buff", false);
		} else if (index == 1) {
			//return new StatusData("+" + (int) damageTakenPercent + "% weapon/engine damage taken", false);
			return new StatusData("Ship and fighter Damage taken x0.85" , false);

		}else if (index==2){
			return new StatusData("Max speed increased by 75" , false);
		}
		return null;
	}

	
}








