package data.scripts.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.everyframe.II_BlockedHullmodDisplayScript;
import java.util.HashSet;
import java.util.Set;

public class II_ReinforcedEmplacements extends BaseHullMod {

    public static final float HEALTH_BONUS = 150f;
    public static final float RANGE_BONUS = 20f;
    public static final float TURN_PENALTY = 50f;

    private static final float PARA_PAD = 10f;

    private static final Set<String> BLOCKED_HULLMODS = new HashSet<>(1);

    static {
        BLOCKED_HULLMODS.add("diableavionics_mount");
    }

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getBallisticWeaponRangeBonus().modifyPercent(id, RANGE_BONUS);
        stats.getEnergyWeaponRangeBonus().modifyPercent(id, RANGE_BONUS);
        stats.getBeamWeaponRangeBonus().modifyPercent(id, -RANGE_BONUS);
        stats.getWeaponHealthBonus().modifyPercent(id, HEALTH_BONUS);
        stats.getWeaponTurnRateBonus().modifyPercent(id, -TURN_PENALTY);
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        for (String tmp : BLOCKED_HULLMODS) {
            if (ship.getVariant().getNonBuiltInHullmods().contains(tmp) && !ship.getVariant().getSMods().contains(tmp)) {
                ship.getVariant().removeMod(tmp);
                II_BlockedHullmodDisplayScript.showBlocked(ship);
            }
        }
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) {
            return "" + (int) HEALTH_BONUS + "%";
        }
        if (index == 1) {
            return "" + (int) RANGE_BONUS + "%";
        }
        if (index == 2) {
            return "" + (int) TURN_PENALTY + "%";
        }
        return null;
    }

    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        HullModSpecAPI dampenedMount = Global.getSettings().getHullModSpec("diableavionics_mount");
        if (dampenedMount != null) {
            LabelAPI label = tooltip.addPara("Incompatible with " + dampenedMount.getDisplayName() + ".", PARA_PAD);
            label.setHighlightColor(Misc.getNegativeHighlightColor());
            label.setHighlight(dampenedMount.getDisplayName());
        }
    }
}
