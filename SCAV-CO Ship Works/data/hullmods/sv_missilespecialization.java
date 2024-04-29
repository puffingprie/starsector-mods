package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.util.Misc;

import java.util.HashMap;
import java.util.Map;

public class sv_missilespecialization extends BaseHullMod {

    public static float LOGI = 25;
    private static Map mag = new HashMap();
    static {
        mag.put(ShipAPI.HullSize.FRIGATE, 30f);
        mag.put(ShipAPI.HullSize.DESTROYER, 25f);
        mag.put(ShipAPI.HullSize.CRUISER,20f);
        mag.put(ShipAPI.HullSize.CAPITAL_SHIP, 15f);
    }
    private static Map mag1 = new HashMap();
    static {
        mag1.put(ShipAPI.HullSize.FRIGATE, 40f);
        mag1.put(ShipAPI.HullSize.DESTROYER, 35f);
        mag1.put(ShipAPI.HullSize.CRUISER,30f);
        mag1.put(ShipAPI.HullSize.CAPITAL_SHIP, 25f);
    }

    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getMissileWeaponRangeBonus().modifyPercent(id,(Float) mag.get(hullSize));
        stats.getMissileWeaponDamageMult().modifyPercent(id,(Float) mag.get(hullSize));
        stats.getMissileAccelerationBonus().modifyPercent(id,(Float) mag.get(hullSize));
        stats.getMissileGuidance().modifyPercent(id,(Float) mag.get(hullSize));
        stats.getMissileMaxSpeedBonus().modifyPercent(id,(Float) mag.get(hullSize));
        stats.getMissileRoFMult().modifyPercent(id,(Float) mag.get(hullSize));
        stats.getMissileWeaponFluxCostMod().modifyPercent(id,(Float) mag.get(hullSize));
        stats.getMissileHealthBonus().modifyPercent(id,(Float) mag.get(hullSize));
        stats.getMissileAmmoBonus().modifyPercent(id,(Float) mag.get(hullSize));
        stats.getMissileAmmoRegenMult().modifyPercent(id,(Float) mag.get(hullSize));
        stats.getMissileTurnAccelerationBonus().modifyPercent(id,(Float) mag.get(hullSize));
        stats.getSuppliesPerMonth().modifyPercent(id,-(Float) mag1.get(hullSize));
        stats.getPeakCRDuration().modifyPercent(id,-LOGI);
    }
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) return "" + ((Float) mag.get(ShipAPI.HullSize.FRIGATE)).intValue() + "%";
        if (index == 1) return "" + ((Float) mag.get(ShipAPI.HullSize.DESTROYER)).intValue() + "%";
        if (index == 2) return "" + ((Float) mag.get(ShipAPI.HullSize.CRUISER)).intValue() + "%";
        if (index == 3) return "" + ((Float) mag.get(ShipAPI.HullSize.CAPITAL_SHIP)).intValue() + "%";
        if (index == 4) return "" + ((Float) mag1.get(ShipAPI.HullSize.FRIGATE)).intValue() + "%";
        if (index == 5) return "" + ((Float) mag1.get(ShipAPI.HullSize.DESTROYER)).intValue() + "%";
        if (index == 6) return "" + ((Float) mag1.get(ShipAPI.HullSize.CRUISER)).intValue() + "%";
        if (index == 7) return "" + ((Float) mag1.get(ShipAPI.HullSize.CAPITAL_SHIP)).intValue() + "%";
        if (index == 8) return Misc.getRoundedValue(LOGI) + "%";
        return null;
    }
}
