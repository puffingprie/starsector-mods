package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

import java.util.HashMap;
import java.util.Map;


public class expsp_siegeretrofitpackage extends BaseHullMod {
    private static Map mag = new HashMap();
    static {
        mag.put(HullSize.FIGHTER, 100f);
        mag.put(HullSize.FRIGATE, 5f);
        mag.put(HullSize.DESTROYER, 100f);
        mag.put(HullSize.CRUISER, 150f);
        mag.put(HullSize.CAPITAL_SHIP, 200f);
    }



    private final float RANGE_BOOST=1.20f;

    private static final float BALLISTIC_REGEN = 1.1f;
    private static final float DAMAGEMULT = 1.5f;
    private static final float SPEEDLOSS=0.1f;
    private static final float PROJVELOCITY=1.2f;
    private static final float ROFMULT=0.9f;
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getBallisticWeaponRangeBonus().modifyFlat(id, (Float) mag.get(hullSize));
        stats.getEnergyWeaponRangeBonus().modifyFlat(id, (Float) mag.get(hullSize));
        stats.getEngineDamageTakenMult().modifyMult(id,DAMAGEMULT);
        stats.getMaxSpeed().modifyMult(id,(1-SPEEDLOSS));
        stats.getArmorBonus().modifyFlat(id,100);
        stats.getBallisticRoFMult().modifyMult(id,ROFMULT);
        stats.getEnergyRoFMult().modifyMult(id,ROFMULT);
        stats.getProjectileSpeedMult().modifyMult(id,PROJVELOCITY);
        //ship.getMutableStats().getFluxDissipation().modifyFlat(id,FLUX_DISSP_BOOST);
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
            if (w.getType() == WeaponAPI.WeaponType.ENERGY && w.usesAmmo() && reloadRate > 0.0F)
                w.getAmmoTracker().setAmmoPerSecond(AdjustedRate);
        }
    }

    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return "" + ((Float) mag.get(HullSize.FRIGATE)).intValue();
        if (index == 1) return "" + ((Float) mag.get(HullSize.DESTROYER)).intValue();
        if (index == 2) return "" + ((Float) mag.get(HullSize.CRUISER)).intValue();
        if (index == 3) return "" + ((Float) mag.get(HullSize.CAPITAL_SHIP)).intValue();
        if (index==4) return  ""+ (10)+ "%";
        if (index==5) return  ""+ (20)+ "%";
        if (index==6) return  ""+ (100);
        if (index==7) return  ""+ (50)+ "%";
        if (index==8) return  ""+ (10)+ "%";
        return null;
    }

    
}