package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.util.Misc;

public class sv_highvolumefirespecialization extends BaseHullMod {
    public static final float COST_REDUCTION  = 50f;
    public static final float FLUXCOST_AND_DAMAGE_REDUCTION = 30f;

    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getDynamic().getMod(Stats.SMALL_BALLISTIC_MOD).modifyPercent(id, -COST_REDUCTION);
        stats.getDynamic().getMod(Stats.SMALL_ENERGY_MOD).modifyPercent(id, -COST_REDUCTION);
        stats.getBallisticWeaponFluxCostMod().modifyPercent(id,-FLUXCOST_AND_DAMAGE_REDUCTION);
        stats.getBeamWeaponFluxCostMult().modifyPercent(id,-FLUXCOST_AND_DAMAGE_REDUCTION);
        stats.getEnergyWeaponFluxCostMod().modifyPercent(id,-FLUXCOST_AND_DAMAGE_REDUCTION);
        stats.getBallisticWeaponDamageMult().modifyPercent(id,-FLUXCOST_AND_DAMAGE_REDUCTION);
        stats.getBeamWeaponFluxCostMult().modifyPercent(id,-FLUXCOST_AND_DAMAGE_REDUCTION);
        stats.getEnergyWeaponDamageMult().modifyPercent(id,-FLUXCOST_AND_DAMAGE_REDUCTION);
    }
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) return Misc.getRoundedValue(COST_REDUCTION) + "%";
        if (index == 1) return Misc.getRoundedValue(FLUXCOST_AND_DAMAGE_REDUCTION) + "%";
        return null;
    }
    public boolean affectsOPCosts(){return true;}
}
