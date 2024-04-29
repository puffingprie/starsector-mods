package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI;


public class SiegeCore extends BaseHullMod {

    private final float RANGE_BOOST=1.20f;
    private final float FLUX_DISSP_BOOST=200f;
    private static final float BALLISTIC_REGEN = 1.2f;
    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        ship.getMutableStats().getBallisticWeaponRangeBonus().modifyMult(id, RANGE_BOOST);
        ship.getMutableStats().getEnergyWeaponRangeBonus().modifyMult(id, RANGE_BOOST);
        ship.getMutableStats().getFluxDissipation().modifyFlat(id,FLUX_DISSP_BOOST);
        //ship.getMutableStats().getBallisticRoFMult().modifyPercent(id,-ROF_MALUS);
        //ship.getMutableStats().getEnergyRoFMult().modifyPercent(id,-ROF_MALUS);
      //ship.getMutableStats().getBeamWeaponRangeBonus().modifyFlat(id, RANGE_BOOST);
        
    }
    public void advanceInCombat (ShipAPI ship, float amount) {
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine.isPaused() || !ship.isAlive())
            return;
        for (WeaponAPI w : ship.getAllWeapons()) {
            float reloadRate = w.getSpec().getAmmoPerSecond();
            float AdjustedRate = reloadRate * BALLISTIC_REGEN;
            if (w.getType() == WeaponAPI.WeaponType.BALLISTIC && w.usesAmmo() && reloadRate > 0.0F)
                w.getAmmoTracker().setAmmoPerSecond(AdjustedRate);
        }
    }
    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return "20%";
        if (index == 1) return "20%";
        if (index == 2) return " "+ FLUX_DISSP_BOOST+"f/s";
       
        return null;
    }
    
}