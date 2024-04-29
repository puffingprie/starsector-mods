package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.util.Misc;

import java.util.HashMap;
import java.util.Map;

public class sv_modernization_decision extends BaseHullMod {
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
        mag1.put(ShipAPI.HullSize.FRIGATE, 100f);
        mag1.put(ShipAPI.HullSize.DESTROYER, 80f);
        mag1.put(ShipAPI.HullSize.CRUISER,60f);
        mag1.put(ShipAPI.HullSize.CAPITAL_SHIP, 40f);
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

    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {

        stats.getDeceleration().modifyPercent(id,(Float) mag.get(hullSize));
        stats.getAcceleration().modifyPercent(id,(Float) mag.get(hullSize));
        stats.getMaxSpeed().modifyPercent(id,(Float) mag.get(hullSize));
        stats.getTurnAcceleration().modifyPercent(id,(Float) mag.get(hullSize));
        stats.getMaxTurnRate().modifyPercent(id,(Float) mag.get(hullSize));
        stats.getHullBonus().modifyPercent(id,(Float) mag.get(hullSize));
        stats.getArmorBonus().modifyPercent(id,(Float) mag.get(hullSize));
        stats.getWeaponHealthBonus().modifyPercent(id,(Float) mag.get(hullSize));
        stats.getEngineHealthBonus().modifyPercent(id,(Float) mag.get(hullSize));
        stats.getAutofireAimAccuracy().modifyPercent(id,(Float) mag.get(hullSize));
        stats.getRecoilPerShotMult().modifyPercent(id,-(Float) mag.get(hullSize));
        stats.getFluxCapacity().modifyPercent(id,(Float) mag.get(hullSize));
        stats.getFluxDissipation().modifyPercent(id,(Float) mag.get(hullSize));
        stats.getEnergyWeaponRangeBonus().modifyPercent(id,(Float) mag.get(hullSize));
        stats.getBallisticWeaponRangeBonus().modifyPercent(id,(Float) mag.get(hullSize));
        stats.getWeaponTurnRateBonus().modifyPercent(id,(Float) mag.get(hullSize));
        stats.getShieldDamageTakenMult().modifyPercent(id,-(Float) mag.get(hullSize));
        stats.getShieldArcBonus().modifyPercent(id,(Float) mag.get(hullSize));
        stats.getShieldUnfoldRateMult().modifyPercent(id,(Float) mag.get(hullSize));
        stats.getShieldUpkeepMult().modifyPercent(id,-(Float) mag.get(hullSize));
        stats.getShieldTurnRateMult().modifyPercent(id,-(Float) mag.get(hullSize));
        stats.getSuppliesPerMonth().modifyPercent(id,(Float) mag1.get(hullSize));
        stats.getFuelUseMod().modifyPercent(id,-(Float) mag.get(hullSize));
        stats.getPeakCRDuration().modifyPercent(id, -LOGI);
    }
    public boolean isApplicableToShip(ShipAPI ship) {
        return !ship.getVariant().getHullMods().contains("sv_analogconversion");
    }
    public String getUnapplicableReason(ShipAPI ship) {
        if (ship.getVariant().getHullMods().contains("sv_analogconversion")) {
            return "Nope";
        }
        return null;
    }
}

