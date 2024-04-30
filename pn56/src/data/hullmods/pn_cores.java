package data.hullmods;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.BaseHullMod;


//Script made by Tartiflette and modified by Zudgemud


public class pn_cores extends BaseHullMod {
    
    public static final float DISSIPATION_BONUS = 10f;
    //public static final float CORONA_EFFECT_REDUCTION = 0.25f;
	
        
    
    private static final Set<String> BLOCKED_HULLMODS = new HashSet<>();
    static
    {
        // These hullmods will automatically be removed
        // Not as elegant as blocking them in the first place, but
        // this method doesn't require editing every hullmod's script  
        BLOCKED_HULLMODS.add("extendedshieldemitter");
        BLOCKED_HULLMODS.add("frontshieldemitter");
        BLOCKED_HULLMODS.add("safetyoverrides");
    }
    
    private final boolean runOnce=false;
    private final float maxRange=0;

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getVentRateMult().modifyMult(id,0.6f);
        stats.getHardFluxDissipationFraction().modifyFlat(id,0.15f);
	//stats.getDynamic().getStat(Stats.CORONA_RESISTANCE).modifyMult(id, CORONA_EFFECT_REDUCTION);
    }
        
    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id)
    {
        for (String tmp : BLOCKED_HULLMODS)
        {
            if (ship.getVariant().getHullMods().contains(tmp))
            {
                ship.getVariant().removeMod(tmp);
            }
        }
    }
	
        
    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        // Allows any ship with a pn hull id  
        return ship.getHullSpec().getHullId().startsWith("pn_");
    }
    
    //MORE VENTING AI
    

}
