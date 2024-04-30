package data.hullmods;

import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
//import com.fs.starfarer.api.impl.hullmods.BaseLogisticsHullMod;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipVariantAPI;

public class Kayse_PhaseCarrier extends BaseHullMod {
    
    float allWingsPhase = 1.0f;
    float mixedWings = 2.0f;

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        if (allWingsArePhase(stats.getVariant()) ){
            //AllWingsArePhase is true
            stats.getFighterRefitTimeMult().modifyMult(id, allWingsPhase, "Phase Carrier bonus");
        }else{
            //AllWingsArePhase is false
            stats.getFighterRefitTimeMult().modifyMult(id, mixedWings, "Phase Carrier bonus");
            
        }
        //stats.getDynamic().getMod(Stats.FLEET_GROUND_SUPPORT).modifyFlat(id, mag.get(hullSize));
        //stats.getDynamic().getStat(Stats.PHASE_TIME_BONUS_MULT).modifyMult(id, timeBonusEffect);
        //stats.getDynamic().getStat(Stats.PHASE_TIME_BONUS_MULT).modifyMult(id, (float) (1 - timeBonusEffect*.01));
        //stats.getDynamic().getStat(Stats.PHASE_CLOAK_SPEED_MOD).modifyMult(id, cloakSpeedEffect);
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        if (! ship.getVariant().hasHullMod("phasefield"))
            return false;
        return super.isApplicableToShip(ship);
    }

    @Override
    public String getUnapplicableReason(ShipAPI ship) {
        if (! ship.getVariant().hasHullMod("phasefield"))
            return "Requires Phase Field";
        return super.getUnapplicableReason(ship);
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return "" + (int) Math.round((allWingsPhase - 1f) * 100f) + "%";//Gets slowdown precentage
        if (index == 1) return "" + (int) Math.round((mixedWings - 1f) * 100f) + "%";//Gets slowdown precentage 
        return null;
    }
    
    private boolean allWingsArePhase(ShipVariantAPI variant){
        for (String wing : variant.getWings()){
            if (!wing.startsWith("kayse_")){
                return false;
            }
        }
        return true;
    }
}




