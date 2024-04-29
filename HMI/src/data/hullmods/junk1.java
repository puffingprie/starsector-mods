package data.hullmods;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class junk1 extends junker {

    private static final float JUNKERBONUS = 0.9f;
    private static final float JUNKERMALUSSPEED = 0.1f;
    private static final float JUNKERMALUSQUALITY = 0.05f;


    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getSuppliesToRecover().modifyMult(id, JUNKERBONUS);
    }

    public void applyEffectsToFighterSpawnedByShip(ShipAPI fighter, ShipAPI ship, String id) {
        float effect = ship.getMutableStats().getDynamic().getValue(Stats.DMOD_EFFECT_MULT);

        MutableShipStatsAPI stats = fighter.getMutableStats();

        stats.getMaxSpeed().modifyMult(id, 1f - JUNKERMALUSSPEED * effect);
        stats.getArmorDamageTakenMult().modifyPercent(id, (1 + JUNKERMALUSQUALITY) * 100f * effect);
        stats.getShieldDamageTakenMult().modifyPercent(id, (1 + JUNKERMALUSQUALITY) * 100f * effect);
        stats.getHullDamageTakenMult().modifyPercent(id, (1 + JUNKERMALUSQUALITY) * 100f * effect);

        stats.getMissileWeaponDamageMult().modifyPercent(id, JUNKERMALUSQUALITY * 100f * (1 - effect));
        stats.getBallisticWeaponDamageMult().modifyPercent(id, JUNKERMALUSQUALITY * 100f * (1 - effect));
        stats.getEnergyWeaponDamageMult().modifyPercent(id, JUNKERMALUSQUALITY * 100f * (1 - effect));

        fighter.setHeavyDHullOverlay();
    }


    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) return "" + "2/5/10/20";
        if (index == 1) return "" + (int)Math.round((1f - JUNKERBONUS) * 100f) + "%";
        if (index == 2) return "" + (int)Math.round((JUNKERMALUSQUALITY) * 100f) + "%";
        if (index == 3) return "" + (int)Math.round((JUNKERMALUSQUALITY) * 100f) + "%";
        return null;
    }

    @Override
    public boolean affectsOPCosts() {
        return true;
    }
}