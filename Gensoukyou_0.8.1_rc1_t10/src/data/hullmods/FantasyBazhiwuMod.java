package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;

public class FantasyBazhiwuMod extends BaseHullMod {

    public static final float WEAPON_RECOIL_DEBUFF = 0.5f;

    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize,
                                               MutableShipStatsAPI stats, String id) {

        stats.getMaxRecoilMult().modifyFlat(id, WEAPON_RECOIL_DEBUFF);
        stats.getRecoilPerShotMult().modifyFlat(id, WEAPON_RECOIL_DEBUFF);


    }

    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) return "" + (int) (WEAPON_RECOIL_DEBUFF * 100f) + "%";
        return null;
    }


}
