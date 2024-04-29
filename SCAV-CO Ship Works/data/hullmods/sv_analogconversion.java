package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.util.Misc;

import java.util.HashMap;
import java.util.Map;

public class sv_analogconversion extends BaseHullMod {
    public static float EMP_DAMAGE_TAKEN = 50;

    private static Map mag = new HashMap();

    static {
        mag.put(ShipAPI.HullSize.FRIGATE, 30f);
        mag.put(ShipAPI.HullSize.DESTROYER, 25f);
        mag.put(ShipAPI.HullSize.CRUISER, 20f);
        mag.put(ShipAPI.HullSize.CAPITAL_SHIP, 15f);
    }
    private static Map mag1 = new HashMap();
    static {
        mag1.put(ShipAPI.HullSize.FRIGATE, 45f);
        mag1.put(ShipAPI.HullSize.DESTROYER, 45f);
        mag1.put(ShipAPI.HullSize.CRUISER,35f);
        mag1.put(ShipAPI.HullSize.CAPITAL_SHIP, 25f);

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
        if (index == 8) return Misc.getRoundedValue(EMP_DAMAGE_TAKEN) + "%";
        return null;
    }

    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {

        stats.getDeceleration().modifyPercent(id, -(Float) mag.get(hullSize));
        stats.getAcceleration().modifyPercent(id, -(Float) mag.get(hullSize));
        stats.getMaxSpeed().modifyPercent(id, -(Float) mag.get(hullSize));
        stats.getTurnAcceleration().modifyPercent(id, -(Float) mag.get(hullSize));
        stats.getMaxTurnRate().modifyPercent(id, -(Float) mag.get(hullSize));
        stats.getAutofireAimAccuracy().modifyPercent(id, -(Float) mag.get(hullSize));
        stats.getRecoilPerShotMult().modifyPercent(id, (Float) mag.get(hullSize));
        stats.getFluxCapacity().modifyPercent(id, -(Float) mag.get(hullSize));
        stats.getFluxDissipation().modifyPercent(id, -(Float) mag.get(hullSize));
        stats.getEnergyWeaponRangeBonus().modifyPercent(id, -(Float) mag.get(hullSize));
        stats.getBallisticWeaponRangeBonus().modifyPercent(id, -(Float) mag.get(hullSize));
        stats.getWeaponTurnRateBonus().modifyPercent(id, -(Float) mag.get(hullSize));
        stats.getShieldDamageTakenMult().modifyPercent(id, (Float) mag.get(hullSize));
        stats.getShieldArcBonus().modifyPercent(id, -(Float) mag.get(hullSize));
        stats.getShieldUnfoldRateMult().modifyPercent(id, -(Float) mag.get(hullSize));
        stats.getShieldUpkeepMult().modifyPercent(id, (Float) mag.get(hullSize));
        stats.getShieldTurnRateMult().modifyPercent(id, (Float) mag.get(hullSize));
        stats.getSuppliesPerMonth().modifyPercent(id, -(Float) mag1.get(hullSize));
        stats.getFuelUseMod().modifyPercent(id, (Float) mag1.get(hullSize));
        stats.getPeakCRDuration().modifyPercent(id, (Float) mag1.get(hullSize));
        stats.getEmpDamageTakenMult().modifyPercent(id, EMP_DAMAGE_TAKEN);
    }

    public boolean isApplicableToShip(ShipAPI ship) {
        return !ship.getVariant().getHullMods().contains("sv_modernization_decision");
    }

    public String getUnapplicableReason(ShipAPI ship) {
        if (ship.getVariant().getHullMods().contains("sv_modernization_decision")) {
            return "Nah";
        }
        return null;
    }
}
