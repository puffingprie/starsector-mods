package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class HMI_Monjeau_Jets extends BaseHullMod {

    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return "Slip Jet system";
        if (index == 1) return "briefly improves speed and agility";
        if (index == 2) return "two charges";
        if (index == 3) return "one charge every three seconds";
        return null;

    }
}
