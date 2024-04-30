package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.listeners.WeaponBaseRangeModifier;

public class FantasyMiracleMod extends BaseHullMod {

    public static final float RANGE_BUFF = 100f;
    public static final float PROJECT_SPEED_BONUS = 10f;

    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        super.applyEffectsBeforeShipCreation(hullSize, stats, id);
        stats.getEnergyProjectileSpeedMult().modifyPercent(id, PROJECT_SPEED_BONUS);
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        super.applyEffectsAfterShipCreation(ship, id);
        ship.addListener(new FantasyEnergyBaseRangeBonus(RANGE_BUFF));

    }


    public static class FantasyEnergyBaseRangeBonus implements WeaponBaseRangeModifier {
        public float baseBonus;

        public FantasyEnergyBaseRangeBonus(float baseBonus) {
            this.baseBonus = baseBonus;
        }

        public float getWeaponBaseRangePercentMod(ShipAPI ship, WeaponAPI weapon) {
            return 0;
        }

        public float getWeaponBaseRangeMultMod(ShipAPI ship, WeaponAPI weapon) {
            return 1f;
        }

        public float getWeaponBaseRangeFlatMod(ShipAPI ship, WeaponAPI weapon) {
            if (weapon.getSlot() == null || weapon.getSlot().getWeaponType() != WeaponAPI.WeaponType.ENERGY) {
                return 0f;
            }
            return baseBonus;
        }
    }

    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) return "" + (int) RANGE_BUFF;
        if (index == 1) return "" + (int) PROJECT_SPEED_BONUS + "%";
        return null;
    }
}
