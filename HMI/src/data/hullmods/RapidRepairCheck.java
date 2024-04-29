package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShieldAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShieldAPI.ShieldType;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class RapidRepairCheck extends BaseHullMod {
	private float repairMult = 0.5f;
	private float EMPMult = 0.5f;


	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		MutableShipStatsAPI stats = ship.getMutableStats();
		ShieldAPI shield = ship.getShield();
		if (shield == null) {
			stats.getCombatEngineRepairTimeMult().modifyMult(id, repairMult);
			stats.getCombatWeaponRepairTimeMult().modifyMult(id, repairMult);
			stats.getEngineDamageTakenMult().modifyMult(id, EMPMult);
			stats.getWeaponDamageTakenMult().modifyMult(id, EMPMult);
		}
	}

	public String getDescriptionParam ( int index, HullSize hullSize){
			if (index == 0) return "" + (int)Math.round(repairMult * 100f) + "%";
			if (index == 1) return "" + + (int)Math.round(EMPMult * 100f) + "%";
			if (index == 2) return "" + "On installation of a Shield Generator, the Rapid Repair system is deactivated";


//		if (index == 1) return "" + ((Float) mag.get(HullSize.FRIGATE)).intValue();
//		if (index == 2) return "" + ((Float) mag.get(HullSize.DESTROYER)).intValue();
//		if (index == 3) return "" + ((Float) mag.get(HullSize.CRUISER)).intValue();
//		if (index == 4) return "" + ((Float) mag.get(HullSize.CAPITAL_SHIP)).intValue();

			return null;
		}
	}

