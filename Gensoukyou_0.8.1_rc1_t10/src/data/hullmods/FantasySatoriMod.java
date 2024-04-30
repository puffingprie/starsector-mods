package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class FantasySatoriMod extends BaseHullMod {
    public static final float SENSOR_BONUS = 50f;
    public static final float ECM = 5f;

    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getSensorStrength().modifyMult(id, 1 + SENSOR_BONUS / 100);

        stats.getDynamic().getMod(Stats.ELECTRONIC_WARFARE_FLAT).modifyFlat(id, ECM);

    }

    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) return "" + (int) (SENSOR_BONUS) + "%";
        if (index == 1) return "" + (int) (ECM) + "%";
        return null;
    }

}
