package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;


public class MachinaTargetSystem extends BaseHullMod {

    private final float RANGE_BOOST=100;
    private final float ROF_MALUS=10;
    
    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        ship.getMutableStats().getBallisticWeaponRangeBonus().modifyFlat(id, RANGE_BOOST);
        ship.getMutableStats().getEnergyWeaponRangeBonus().modifyFlat(id, RANGE_BOOST);
        ship.getMutableStats().getBallisticRoFMult().modifyPercent(id,-ROF_MALUS);
        ship.getMutableStats().getEnergyRoFMult().modifyPercent(id,-ROF_MALUS);
      //ship.getMutableStats().getBeamWeaponRangeBonus().modifyFlat(id, RANGE_BOOST);
        
    }
    
    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return " "+ RANGE_BOOST+" "+"su";
        if (index == 1) return " "+ ROF_MALUS+"%";

       
        return null;
    }
    
}