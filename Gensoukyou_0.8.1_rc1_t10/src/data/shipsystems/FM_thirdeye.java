package data.shipsystems;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

import java.util.List;

//目前未使用
public class FM_thirdeye extends BaseShipSystemScript {

    public static final float LOWER_MOTILITY = 25f;
    public static final float SIGHT_RANGE_BONUS = 125f;
    public static final float RANGE_BONUS = 50f;
    public static final float DAMAGE_BONUS = 50f;

    public List<WeaponAPI> WEAPONS = null;

    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {

        stats.getAcceleration().modifyMult(id, LOWER_MOTILITY / 100 * effectLevel);
        stats.getDeceleration().modifyMult(id, LOWER_MOTILITY / 100 * effectLevel);
        stats.getTurnAcceleration().modifyMult(id, LOWER_MOTILITY / 100 * effectLevel);
        stats.getMaxTurnRate().modifyMult(id, LOWER_MOTILITY / 100 * effectLevel);

        stats.getEnergyWeaponRangeBonus().modifyMult(id, 1 + RANGE_BONUS / 100 * effectLevel);
        stats.getEnergyWeaponDamageMult().modifyMult(id, 1 + DAMAGE_BONUS / 100 * effectLevel);

        stats.getSightRadiusMod().modifyMult(id, 1 + SIGHT_RANGE_BONUS / 100 * effectLevel);


        for (WeaponAPI weapon : WEAPONS) {
            if (!weapon.getSize().equals(WeaponAPI.WeaponSize.LARGE) && state == State.ACTIVE) {
                weapon.disable(false);
                stats.getCombatWeaponRepairTimeMult().modifyMult(id, 4f);
            }

            if (state == State.OUT) {
                weapon.repair();
            }
        }


    }

    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getAcceleration().unmodify(id);
        stats.getDeceleration().unmodify(id);
        stats.getTurnAcceleration().unmodify(id);
        stats.getMaxTurnRate().unmodify(id);

        stats.getEnergyWeaponRangeBonus().unmodify(id);

        stats.getSightRadiusMod().unmodify(id);
        stats.getCombatWeaponRepairTimeMult().unmodify(id);

        if (stats.getEntity() instanceof ShipAPI) {
            WEAPONS = ((ShipAPI) stats.getEntity()).getAllWeapons();
        }

    }

    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData("禁用所有非大型武器", true);
        } else if (index == 1) {
            return new StatusData("能量武器射程提升" + RANGE_BONUS + "%", false);
        } else if (index == 2) {
            return new StatusData("机动性降低", true);
        } else if (index == 3) {
            return new StatusData("战斗视野提升" + SIGHT_RANGE_BONUS + "%", false);
        } else if (index == 4) {
            return new StatusData("能量武器造成伤害" + DAMAGE_BONUS + "%", false);
        }
        return null;
    }
}
