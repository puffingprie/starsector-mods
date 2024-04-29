package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.hullmods.DefectiveManufactory;

public class HMI_MoraDebuff extends BaseHullMod {

	private static final float REFIT_MALUS = 50f;

	
		
	public void applyEffectsToFighterSpawnedByShip(ShipAPI fighter, ShipAPI ship, String id) {
		float effect = ship.getMutableStats().getDynamic().getValue(Stats.DMOD_EFFECT_MULT);

		MutableShipStatsAPI stats = fighter.getMutableStats();
		stats.getFighterRefitTimeMult().modifyPercent(id, REFIT_MALUS * effect);
		fighter.setHeavyDHullOverlay();
	}
	public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
		return new DefectiveManufactory().getDescriptionParam(index, hullSize, ship);
	}
	
	public boolean isApplicableToShip(ShipAPI ship) {
		return true;
	}
	
	public String getUnapplicableReason(ShipAPI ship) {
		return null;
	}
}



