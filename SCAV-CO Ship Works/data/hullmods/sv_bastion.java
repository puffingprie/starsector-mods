package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.util.Misc;

public class sv_bastion extends BaseHullMod {

    public static float SHIELD_UP = 100f;
    public static float SHIELD_UNFOLD = 1000f;
    public static float SHIELD_NORM = 50F;

    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getShieldUpkeepMult().modifyPercent(id,-SHIELD_UP);
        stats.getHighExplosiveShieldDamageTakenMult().modifyPercent(id,SHIELD_UP);
        stats.getKineticShieldDamageTakenMult().modifyPercent(id,-SHIELD_NORM);
        stats.getShieldUnfoldRateMult().modifyPercent(id,SHIELD_UNFOLD);
    }
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) return Misc.getRoundedValue(SHIELD_UNFOLD) + "%";
        return null;
    }
}
