package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;

public class pn_leapStats extends BaseShipSystemScript {

    private final int DAMAGE_REDUCTION = 90;

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        stats.getArmorDamageTakenMult().modifyPercent(id, -DAMAGE_REDUCTION * effectLevel);
        stats.getHullDamageTakenMult().modifyPercent(id, -DAMAGE_REDUCTION * effectLevel);
        stats.getEmpDamageTakenMult().modifyPercent(id, -DAMAGE_REDUCTION * effectLevel);
        stats.getMaxSpeed().modifyFlat(id, 600 * effectLevel);
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getArmorDamageTakenMult().unmodify(id);
        stats.getHullDamageTakenMult().unmodify(id);
        stats.getEmpDamageTakenMult().unmodify(id);
        stats.getMaxSpeed().unmodify(id);
    }

    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData((int) Math.round(DAMAGE_REDUCTION * effectLevel) + "% damage reduction", false);
        }
        return null;
    }
}