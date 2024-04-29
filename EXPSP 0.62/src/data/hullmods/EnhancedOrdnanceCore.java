package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI;

public class EnhancedOrdnanceCore extends BaseHullMod {

	private static final float MISSILE_RELOAD_RATE = 20f;
        private static final float MISSILE_REGEN = 1.4f;
	private static final float SPEED_BOOST=20f;
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		////stats.getBallisticRoFMult().modifyMult(id, 1f + MISSILE_RELOAD_RATE * 0.01f);
		////stats.getEnergyRoFMult().modifyMult(id, 1f + MISSILE_RELOAD_RATE * 0.01f);
		stats.getMissileRoFMult().modifyMult(id, 1f + MISSILE_RELOAD_RATE * 0.01f);
		stats.getMaxSpeed().modifyFlat(id,SPEED_BOOST);
	}

    public void advanceInCombat (ShipAPI ship, float amount) {
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine.isPaused() || !ship.isAlive())
            return;
        for (WeaponAPI w : ship.getAllWeapons()) {
            float reloadRate = w.getSpec().getAmmoPerSecond();
            float AdjustedRate = reloadRate * MISSILE_REGEN;
            if (w.getType() == WeaponAPI.WeaponType.MISSILE && w.usesAmmo() && reloadRate > 0.0F)
                w.getAmmoTracker().setAmmoPerSecond(AdjustedRate);
            }
    }        
        
	public String getDescriptionParam(int index, HullSize hullSize) {
        if (index ==0)  return "20%";
	    if (index ==1)  return "40%";
		if (index ==2) return "20 SU";
		return null;
	}


}
