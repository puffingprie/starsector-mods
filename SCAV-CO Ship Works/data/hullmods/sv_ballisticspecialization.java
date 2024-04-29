package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.util.Misc;

import java.util.HashMap;
import java.util.Map;

public class sv_ballisticspecialization extends BaseHullMod {
    public static float LOGI = 25;
    private static Map mag = new HashMap();
    static {
        mag.put(ShipAPI.HullSize.FRIGATE, 25f);
        mag.put(ShipAPI.HullSize.DESTROYER, 20f);
        mag.put(ShipAPI.HullSize.CRUISER,15f);
        mag.put(ShipAPI.HullSize.CAPITAL_SHIP, 10f);
    }
    private static Map mag1 = new HashMap();
    static {
        mag1.put(ShipAPI.HullSize.FRIGATE, 55f);
        mag1.put(ShipAPI.HullSize.DESTROYER, 50f);
        mag1.put(ShipAPI.HullSize.CRUISER,45f);
        mag1.put(ShipAPI.HullSize.CAPITAL_SHIP, 40f);
    }
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getBallisticWeaponRangeBonus().modifyPercent(id, (Float) mag.get(hullSize));
        stats.getBallisticProjectileSpeedMult().modifyPercent(id, (Float) mag.get(hullSize));
        stats.getBallisticWeaponDamageMult().modifyPercent(id, (Float) mag.get(hullSize));
        stats.getBallisticRoFMult().modifyPercent(id, (Float) mag.get(hullSize));
        stats.getBallisticWeaponFluxCostMod().modifyPercent(id, -(Float) mag.get(hullSize));
        stats.getBallisticAmmoBonus().modifyPercent(id, (Float) mag.get(hullSize));
        stats.getBallisticAmmoRegenMult().modifyPercent(id, (Float) mag.get(hullSize));
        stats.getSuppliesPerMonth().modifyPercent(id, (Float) mag1.get(hullSize));
        stats.getPeakCRDuration().modifyPercent(id, -LOGI);
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
