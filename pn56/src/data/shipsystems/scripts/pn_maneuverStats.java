package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;

public class pn_maneuverStats extends BaseShipSystemScript {

    public static final float ROF_BONUS = 3f;
    public static final float FLUX_REDUCTION = 50f;
    
        public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {

        float mult = 1f + ROF_BONUS * effectLevel;
        stats.getBallisticRoFMult().modifyMult(id, mult);
        
        if (state == ShipSystemStatsScript.State.OUT) {
            stats.getMaxSpeed().unmodify(id); // to slow down ship to its regular top speed while powering drive down
            stats.getMaxTurnRate().unmodify(id);
        } else {
            stats.getMaxSpeed().modifyFlat(id, 95f);
            stats.getAcceleration().modifyPercent(id, 300f * effectLevel);
            stats.getDeceleration().modifyPercent(id, 300f * effectLevel);
            stats.getTurnAcceleration().modifyFlat(id, 50f * effectLevel);
            stats.getTurnAcceleration().modifyPercent(id, 300f * effectLevel);
            stats.getMaxTurnRate().modifyFlat(id, 15f);
            stats.getMaxTurnRate().modifyPercent(id, 100f);
        }
    }

    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getMaxSpeed().unmodify(id);
        stats.getMaxTurnRate().unmodify(id);
        stats.getTurnAcceleration().unmodify(id);
        stats.getAcceleration().unmodify(id);
        stats.getDeceleration().unmodify(id);
        
        stats.getBallisticRoFMult().unmodify(id);
        stats.getMissileRoFMult().unmodify(id);
        stats.getEnergyRoFMult().unmodify(id);
    }

    public StatusData getStatusData(int index, State state, float effectLevel) {
        float mult = 1f + ROF_BONUS * effectLevel;
        float bonusPercent = (int) (mult - 1f) * 100f;
        if (index == 0) {
            return new StatusData("energy rate of fire +" + (int) bonusPercent + "%", false);
        }
        if (index == 0) {
            return new StatusData("improved maneuverability", false);
        } else if (index == 1) {
            return new StatusData("+50 top speed", false);
        }
        return null;
    }

}
