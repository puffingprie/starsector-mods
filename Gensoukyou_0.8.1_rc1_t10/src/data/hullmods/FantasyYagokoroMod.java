package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import org.magiclib.util.MagicIncompatibleHullmods;


public class FantasyYagokoroMod extends BaseHullMod {

    public static final float RANGE_BONUS = 80f;
    public static final float RATE_INCREASE_MODIFIER = 25f;

    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {

        stats.getDynamic().getStat(Stats.REPLACEMENT_RATE_INCREASE_MULT).modifyPercent(id, RATE_INCREASE_MODIFIER);

        stats.getEnergyWeaponRangeBonus().modifyPercent(id, RANGE_BONUS);
        stats.getBallisticWeaponRangeBonus().modifyPercent(id, RANGE_BONUS);

        if (stats.getVariant().getHullMods().contains("expanded_deck_crew")) {
            MagicIncompatibleHullmods.removeHullmodWithWarning(stats.getVariant(), "expanded_deck_crew", "FantasyYagokoroMod");
        }
        if (stats.getVariant().getHullMods().contains("dedicated_targeting_core")) {
            MagicIncompatibleHullmods.removeHullmodWithWarning(stats.getVariant(), "dedicated_targeting_core", "FantasyYagokoroMod");
        }
        if (stats.getVariant().getHullMods().contains("advancedcore")) {
            MagicIncompatibleHullmods.removeHullmodWithWarning(stats.getVariant(), "advancedcore", "FantasyYagokoroMod");
        }
        if (stats.getVariant().getHullMods().contains("targetingunit")) {
            MagicIncompatibleHullmods.removeHullmodWithWarning(stats.getVariant(), "targetingunit", "FantasyYagokoroMod");
        }
        if (stats.getVariant().getHullMods().contains("FantasySageMod")) {
            MagicIncompatibleHullmods.removeHullmodWithWarning(stats.getVariant(), "FantasySageMod", "FantasyYagokoroMod");
        }
    }

    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) return "" + (int) RANGE_BONUS + "%";
        if (index == 1) return "" + (int) RATE_INCREASE_MODIFIER + "%";

        return null;
    }

}
