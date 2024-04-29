package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI;

public class expsp_stilettoautoforge extends BaseHullMod {

	private static final float MISSILE_RELOAD_RATE = 33f;
        private static final float MISSILE_REGEN = 1.25f;
	 private static final float DAMAGE_REDUCTION = 10f;
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		////stats.getBallisticRoFMult().modifyMult(id, 1f + MISSILE_RELOAD_RATE * 0.01f);
		////stats.getEnergyRoFMult().modifyMult(id, 1f + MISSILE_RELOAD_RATE * 0.01f);
		stats.getMissileWeaponDamageMult().modifyMult(id, 1f - DAMAGE_REDUCTION * 0.01f);
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
		if (index == 0) return "" + (int) 25 + "%";
        if (index == 1) return "" + (int) 10 + "%";
		return null;
	}


}
