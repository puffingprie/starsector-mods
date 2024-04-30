package data.hullmods;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.BaseHullMod;
import java.awt.Color;

public class Kayse_PhaseLogistics extends BaseHullMod {
    
    public static final float SUPPLY_USE_MULT = 0.9f;//10% discount on phase ships
    public static final float PROFILE_MULT = 0.8f;//20% Sensor Profile improvement on non-phase ships

	
    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        if ( stats.getVariant().hasHullMod("phasefield")){
            stats.getSuppliesPerMonth().modifyMult(id, SUPPLY_USE_MULT);
            stats.getSuppliesToRecover().modifyMult(id, SUPPLY_USE_MULT);
        }else{
            stats.getSensorProfile().modifyMult(id, PROFILE_MULT);
        }
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return "" + (int) Math.round((1f - SUPPLY_USE_MULT) * 100f) + "%";
        if (index == 1) return "" + (int) Math.round((1f - PROFILE_MULT) * 100f) + "%";
        return null;
    }
    
    @Override
    public Color getBorderColor() {
        return new Color(70,72,140, 255);
    }

    @Override
    public Color getNameColor() {
        //return new Color(13,158,255,255);
        return new Color(70,72,140,255);
    }
}