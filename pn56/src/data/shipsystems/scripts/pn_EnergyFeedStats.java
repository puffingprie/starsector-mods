package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;

public class pn_EnergyFeedStats extends BaseShipSystemScript {

    public static final float ROF_BONUS = 1f;
    public static final float FLUX_REDUCTION = 50f;
    
    
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {

        float mult = 1f + ROF_BONUS * effectLevel;
        stats.getEnergyRoFMult().modifyMult(id, mult);
        stats.getEnergyWeaponFluxCostMod().modifyMult(id, 1f * (FLUX_REDUCTION * 0.01f));
    }

    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getBallisticRoFMult().unmodify(id);
        stats.getMissileRoFMult().unmodify(id);
        stats.getEnergyRoFMult().unmodify(id);
        stats.getEnergyWeaponFluxCostMod().unmodify(id);
    }

    public StatusData getStatusData(int index, State state, float effectLevel) {
        float mult = 1f + ROF_BONUS * effectLevel;
        float bonusPercent = (int) (mult - 1f) * 100f;
        if (index == 0) {
            return new StatusData("energy rate of fire +" + (int) bonusPercent + "%", false);
        }
        if (index == 1) {
	return new StatusData("energy flux use -" + (int) FLUX_REDUCTION + "%", false);
	}
        return null;
    }
}
