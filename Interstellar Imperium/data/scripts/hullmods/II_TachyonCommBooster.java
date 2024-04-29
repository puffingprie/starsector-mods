package data.scripts.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class II_TachyonCommBooster extends BaseHullMod {

    public static final float FIGHTER_RANGE_PCT = 100f;

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getFighterWingRange().modifyPercent(id, FIGHTER_RANGE_PCT);
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
        if (index == 0) {
            return "" + (int) Math.round(FIGHTER_RANGE_PCT) + "%";
        } else {
            return null;
        }
    }
}
