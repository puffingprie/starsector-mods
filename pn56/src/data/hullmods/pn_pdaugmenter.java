package data.hullmods;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.BaseHullMod;


public class pn_pdaugmenter extends BaseHullMod {

    public static final float BALLISTIC_RANGE_BONUS = 300f;
    public static final float BONUS_PERCENT = 30f;
    public static final float NEG_PERCENT = 25f;

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getEnergyWeaponDamageMult().modifyPercent(id, -NEG_PERCENT);
        stats.getProjectileSpeedMult().modifyPercent(id, BONUS_PERCENT);
        stats.getBallisticWeaponRangeBonus().modifyFlat(id, BALLISTIC_RANGE_BONUS);
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        // Allows any ship with a pn_ hull id  
        return ship.getHullSpec().getHullId().startsWith("pn_sl-t1p");
    }
}
