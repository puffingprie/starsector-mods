package data.scripts.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;


public class acs_railfedfighter extends BaseHullMod {

    public static final float CRCHANGES = 55f;
    public boolean APPLIED = false;

    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {}
    public void advanceInCombat(ShipAPI ship, float amount) {
        if(!ship.isAlive()) return;
        if(!APPLIED){
          APPLIED = true;
          ship.setCRAtDeployment(CRCHANGES/100); //override the current value
        }
    }

    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return "" + (int) ((1f - CRCHANGES) * 100f) + "%";
        return null;
    }
}
