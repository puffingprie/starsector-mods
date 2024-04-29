package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.util.Misc;

public class sv_scavcosubsytems extends BaseHullMod {

    public float getShieldEfficiencyModifier(MutableShipStatsAPI stats) {
        float totalEffect = 10f;
        if (stats.getVariant().getHullMods().contains("pointdefenseai")) {
            totalEffect = totalEffect + 5f;}
        return totalEffect;
    }
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id)
    {
                stats.getBeamPDWeaponRangeBonus().modifyPercent(id,25f);
                stats.getNonBeamPDWeaponRangeBonus().modifyPercent(id,25f);
                stats.getDamageToMissiles().modifyPercent(id, 25f);
                stats.getDamageToFighters().modifyPercent(id, 25f);
                stats.getDamageToFrigates().modifyPercent(id, 25f);
                stats.getPeakCRDuration().modifyPercent(id, 25f);
                stats.getCRLossPerSecondPercent().modifyPercent(id,25f);
                float shieldEff = getShieldEfficiencyModifier(stats);
                stats.getShieldDamageTakenMult().modifyPercent(id, shieldEff);
    }
        public String getDescriptionParam(int index, ShipAPI.HullSize hullSize, ShipAPI ship) 
    {
        if (index == 0) return Misc.getRoundedValue(25.0F) + "%";
        if (index == 1) return Misc.getRoundedValue(25.0F) + "%";
        if (index == 2) return Misc.getRoundedValue(25.0f) + "%";
        if (index == 3) return Misc.getRoundedValue(25.0f) + "%";
        if (index == 4) return Misc.getRoundedValue(10.0f) + "%";
        if (index == 5) return Misc.getRoundedValue(5.0f) + "%";

        return null;
    }
}