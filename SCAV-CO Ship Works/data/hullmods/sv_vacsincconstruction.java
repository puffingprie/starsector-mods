package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.util.Misc;

public class sv_vacsincconstruction extends BaseHullMod {

    public static float WEAPON_ROF = 25;
    public static float FLUX_BOOST = 100;
    public static float CONSTRUCTION_QUALITY = 25;
    public float ENGINE_MOD (MutableShipStatsAPI stats) {

        float totalEffect = 0f;
        if (stats.getVariant().getHullMods().contains("safetyoverrides")){
            totalEffect = totalEffect - 33f;}return totalEffect;
    }
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id)
    {
        stats.getBallisticRoFMult().modifyPercent(id,WEAPON_ROF);
        stats.getEnergyRoFMult().modifyPercent(id,WEAPON_ROF);
        stats.getMissileRoFMult().modifyPercent(id,WEAPON_ROF);
        stats.getZeroFluxSpeedBoost().modifyPercent(id,FLUX_BOOST);
        stats.getAcceleration().modifyPercent(id,FLUX_BOOST);
        stats.getVentRateMult().modifyPercent(id,FLUX_BOOST);
        stats.getMaxArmorDamageReduction().modifyPercent(id,-CONSTRUCTION_QUALITY);
        stats.getEngineHealthBonus().modifyPercent(id,-CONSTRUCTION_QUALITY);
        stats.getWeaponHealthBonus().modifyPercent(id,-CONSTRUCTION_QUALITY);
        float ENGINE_MODED = ENGINE_MOD (stats);
        stats.getMaxSpeed().modifyPercent(id,ENGINE_MODED);
    }
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) return Misc.getRoundedValue(WEAPON_ROF) + "%";
        if (index == 1) return Misc.getRoundedValue(FLUX_BOOST) + "%";
        if (index == 2) return Misc.getRoundedValue(CONSTRUCTION_QUALITY) + "%";
        if (index == 3) return Misc.getRoundedValue(33.0f) + "%";
        return null;
    }
}
