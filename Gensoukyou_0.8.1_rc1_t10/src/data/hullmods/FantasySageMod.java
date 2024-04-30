package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.utils.I18nUtil;
import org.magiclib.util.MagicIncompatibleHullmods;

import java.util.HashMap;
import java.util.Map;

public class FantasySageMod extends BaseHullMod {

    public static final float VITAL_STRIKE_FF = 15f;
    public static final float VITAL_STRIKE_DD = 5f;


    private static final Map<ShipAPI.HullSize, Float> mag = new HashMap();

    static {
        mag.put(ShipAPI.HullSize.FIGHTER, 0f);
        mag.put(ShipAPI.HullSize.FRIGATE, 8f);
        mag.put(ShipAPI.HullSize.DESTROYER, 15f);
        mag.put(ShipAPI.HullSize.CRUISER, 30f);
        mag.put(ShipAPI.HullSize.CAPITAL_SHIP, 45f);
    }

    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getDamageToFrigates().modifyPercent(id, VITAL_STRIKE_FF);
        stats.getDamageToDestroyers().modifyPercent(id, VITAL_STRIKE_DD);

        stats.getBallisticWeaponRangeBonus().modifyPercent(id, mag.get(hullSize));
        stats.getEnergyWeaponRangeBonus().modifyPercent(id, mag.get(hullSize));

        if (stats.getVariant().getHullMods().contains("dedicated_targeting_core")) {
            MagicIncompatibleHullmods.removeHullmodWithWarning(stats.getVariant(), "dedicated_targeting_core", "FantasySageMod");
        }
        if (stats.getVariant().getHullMods().contains("advancedcore")) {
            MagicIncompatibleHullmods.removeHullmodWithWarning(stats.getVariant(), "advancedcore", "FantasySageMod");
        }
        if (stats.getVariant().getHullMods().contains("targetingunit")) {
            MagicIncompatibleHullmods.removeHullmodWithWarning(stats.getVariant(), "targetingunit", "FantasySageMod");
        }


    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        return ship.getVariant().getHullMods().contains(FantasyBasicMod.FANTASYBASICMOD)
                        && !ship.getVariant().getHullMods().contains("dedicated_targeting_core")
                        && !ship.getVariant().getHullMods().contains("advancedcore")
                        && !ship.getVariant().getHullMods().contains("targetingunit")
                ;
    }

    public String getUnapplicableReason(ShipAPI ship) {

        if (!ship.getVariant().getHullMods().contains(FantasyBasicMod.FANTASYBASICMOD)) {
            return I18nUtil.getHullModString("FM_HullModRequireBasicMod");
        }
        return null;
    }

    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        tooltip.addSpacer(10f);
        tooltip.addSectionHeading(I18nUtil.getHullModString("FM_DescriptionAndEvaluation"), Alignment.TMID, 4f);
        tooltip.addPara(I18nUtil.getHullModString("FantasySageMod_DAE_0"), Misc.getTextColor(), 4f);
        tooltip.addSpacer(10f);
        tooltip.addPara(I18nUtil.getHullModString("FantasySageMod_DAE_1"), Misc.getGrayColor(), 4f);
    }


    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {

        if (index == 0) return mag.get(ShipAPI.HullSize.FRIGATE).intValue() + "%";
        if (index == 1) return mag.get(ShipAPI.HullSize.DESTROYER).intValue() + "%";
        if (index == 2) return mag.get(ShipAPI.HullSize.CRUISER).intValue() + "%";
        if (index == 3) return mag.get(ShipAPI.HullSize.CAPITAL_SHIP).intValue() + "%";
        if (index == 4) return (int) VITAL_STRIKE_FF + "%";
        if (index == 5) return (int) VITAL_STRIKE_DD + "%";


        return null;
    }
}
