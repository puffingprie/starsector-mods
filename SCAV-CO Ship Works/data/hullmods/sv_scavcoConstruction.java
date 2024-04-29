package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.util.Misc;


public class sv_scavcoConstruction extends BaseHullMod {
    public static float DMOD_EFFECT_MULT = 0.25F;
    public static float DMOD_AVOID_CHANCE = 0.20F;


    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id)
    {
        stats.getDynamic().getStat("dmod_effect_mult").modifyMult(id, DMOD_EFFECT_MULT);
        stats.getDynamic().getMod("dmod_acquire_prob_mod").modifyMult(id, 1.0F - DMOD_AVOID_CHANCE * 0.01F);
        stats.getDynamic().getMod("individual_ship_recovery_mod").modifyFlat(id, 200.0F);
		stats.getMinCrewMod().modifyMult(id, 0.90F);
        stats.getSuppliesPerMonth().modifyMult(id, 0.90F);
        stats.getFuelUseMod().modifyMult(id, 0.90F);
        stats.getBaseCRRecoveryRatePercentPerDay().modifyPercent(id, 100.0F);
        stats.getCRLossPerSecondPercent().modifyPercent(id,25f);
        stats.getRepairRatePercentPerDay().modifyPercent(id, 100.0F);
    }

        public String getDescriptionParam(int index, ShipAPI.HullSize hullSize, ShipAPI ship) {
        if (index == 0) return Misc.getRoundedValue(75.0F) + "%";
        if (index == 1) return Misc.getRoundedValue(20.0F) + "%";
        if (index == 2) return Misc.getRoundedValue(10.0f) + "%";
        if (index == 3) return Misc.getRoundedValue (100.0F) + "%";
        return null;
    }
}