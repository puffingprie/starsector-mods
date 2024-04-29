package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.util.Misc;

public class sv_maraudingengines extends BaseHullMod {

    public static float SPEED_UP = 100f;
    public static float HULL_DOWN = 25f;


    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getZeroFluxMinimumFluxLevel().modifyFlat(id, 2f);
        stats.getAcceleration().modifyPercent(id,SPEED_UP);
        stats.getDeceleration().modifyPercent(id,SPEED_UP);
        stats.getHullDamageTakenMult().modifyPercent(id,HULL_DOWN);
    }
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) return Misc.getRoundedValue(SPEED_UP) + "%";
        if (index == 1) return Misc.getRoundedValue(HULL_DOWN) + "%";
        return null;
    }
}
