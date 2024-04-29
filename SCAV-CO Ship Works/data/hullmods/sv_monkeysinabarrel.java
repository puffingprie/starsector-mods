package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.util.Misc;

public class sv_monkeysinabarrel extends BaseHullMod {

    public static float WEAPON_ROF = 40;
    public static float HULL_BONUS = 50;
    public static float HULL_BONUS2 = 400;
    public static float HULL_BONUS3 = 25;


    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getBallisticRoFMult().modifyPercent(id,-WEAPON_ROF);
        stats.getHullBonus().modifyPercent(id,HULL_BONUS3);
        stats.getCrewLossMult().modifyPercent(id,-HULL_BONUS);
        stats.getHullDamageTakenMult().modifyPercent(id,-HULL_BONUS);
        stats.getEmpDamageTakenMult().modifyPercent(id,-HULL_BONUS);
        stats.getWeaponHealthBonus().modifyPercent(id,HULL_BONUS2);
        stats.getEngineHealthBonus().modifyPercent(id,HULL_BONUS2);
        stats.getCombatEngineRepairTimeMult().modifyPercent(id,HULL_BONUS);
        stats.getCombatWeaponRepairTimeMult().modifyPercent(id,HULL_BONUS);
    }
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) return Misc.getRoundedValue(WEAPON_ROF) + "%";
        if (index == 1) return Misc.getRoundedValue(HULL_BONUS3) + "%";
        if (index == 2) return Misc.getRoundedValue(HULL_BONUS) + "%";
        if (index == 3) return Misc.getRoundedValue(HULL_BONUS) + "%";
        if (index == 4) return Misc.getRoundedValue(HULL_BONUS) + "%";
        if (index == 5) return Misc.getRoundedValue(HULL_BONUS2) + "%";
        return null;
    }
}
