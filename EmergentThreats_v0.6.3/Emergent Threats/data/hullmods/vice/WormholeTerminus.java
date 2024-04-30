package data.hullmods.vice;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class WormholeTerminus extends BaseHullMod {

	private static float COST_REDUCTION  = 10f;
	private static float ENERGY_WEAPON_FLUX_REDUCTION  = 50f;
	private static float CREW_REDUCTION = 60f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getDynamic().getMod(Stats.LARGE_ENERGY_MOD).modifyFlat(id, -COST_REDUCTION);
		stats.getEnergyWeaponFluxCostMod().modifyMult(id, 1f - ENERGY_WEAPON_FLUX_REDUCTION * 0.01f);
		stats.getMinCrewMod().modifyMult(id, 1f - CREW_REDUCTION * 0.01f);
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) COST_REDUCTION;
		if (index == 1) return "" + (int) ENERGY_WEAPON_FLUX_REDUCTION + "%";
		if (index == 2) return "" + (int) CREW_REDUCTION + "%";
		return null;
	}

	@Override
	public boolean affectsOPCosts() {
		return true;
	}
}

