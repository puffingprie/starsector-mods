package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;

public class sv_longrangesensors extends BaseHullMod {
    private static final int SENSOR_STRENGTH = 400;
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getSensorStrength().modifyFlat(id, SENSOR_STRENGTH);
    }
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) return "" + SENSOR_STRENGTH;
        return null;
    }
}