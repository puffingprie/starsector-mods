package data.scripts.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import java.awt.Color;

public class II_CommissionedCrew extends BaseHullMod {

    private static final float CASUALTIES_MULT = 0.75f;
    private static final float GROUND_STRENGTH_PER_CREW = 0.1f;

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getCrewLossMult().modifyMult(id, CASUALTIES_MULT);

        if (stats.getVariant() != null) {
            float minCrewBase = stats.getVariant().getHullSpec().getMinCrew();
            stats.getDynamic().getMod(Stats.FLEET_GROUND_SUPPORT).modifyFlat(id, Math.round(GROUND_STRENGTH_PER_CREW * minCrewBase));
        }
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
//        if (!Misc.getCommissionFactionId().contentEquals("interstellarimperium")) {
//            ship.getVariant().removeMod("ii_commissioned_crew");
//        }
        if (ship.getVariant().hasHullMod("CHM_commission")) {
            ship.getVariant().removeMod("CHM_commission");
        }
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
        if (index == 0) {
            return "" + Math.round((1f - CASUALTIES_MULT) * 100f) + "%";
        }
        if (index == 1) {
            String groundBonus = "unknown";
            if (ship != null) {
                float minCrewBase = ship.getHullSpec().getMinCrew();
                groundBonus = "" + Math.round(GROUND_STRENGTH_PER_CREW * minCrewBase);
            }
            return groundBonus;
        }
        if (index == 2) {
            return "" + Math.round(GROUND_STRENGTH_PER_CREW * 100f) + "%";
        }
        return null;
    }

    @Override
    public Color getBorderColor() {
        return new Color(147, 102, 50, 0);
    }

    @Override
    public Color getNameColor() {
        return new Color(255, 205, 120, 255);
    }
}
