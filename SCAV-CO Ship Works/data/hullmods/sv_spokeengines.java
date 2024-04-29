package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.util.Misc;

public class sv_spokeengines extends BaseHullMod {

    public float HullDebuff(MutableShipStatsAPI stats) {

        float totalEffect = 0f;
        if (stats.getVariant().getHullMods().contains("safetyoverrides")){
            totalEffect = totalEffect - 20f;}
        if (stats.getVariant().getHullMods().contains("unstable_injector")) {
            totalEffect = totalEffect - 20f;}return totalEffect;
    }
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id)
    {
      stats.getEngineHealthBonus().modifyPercent(id,-50f);
      stats.getCombatEngineRepairTimeMult().modifyPercent(id,200f);
      stats.getMaxSpeed().modifyPercent(id,25f);
      stats.getZeroFluxSpeedBoost().modifyPercent(id,-100);
      float HullEff = HullDebuff(stats);
      stats.getPeakCRDuration().modifyPercent(id,HullEff);
    }
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize, ShipAPI ship)
    {
        if (index == 0) return Misc.getRoundedValue(50.0F) + "%";
        if (index == 1) return Misc.getRoundedValue(200.0f) + "%";
        if (index == 2) return Misc.getRoundedValue(25.0f) + "%";
        if (index == 3) return Misc.getRoundedValue(20.0f) + "%";
        return null;
    }
}
