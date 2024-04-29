package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
import com.fs.starfarer.api.util.IntervalUtil;

public class HMI_LiminaReconstructionStats extends BaseHullMod {

    private static final float HULL_REPAIR_MULTIPLIER = 600.0f;

    protected Object RECONKEY = new Object();
	
    private final IntervalUtil interval = new IntervalUtil(0.033f, 0.033f);

    private static final float SUPPLY_MALUS = 50f;
    private static final float VENT_RATE_BONUS = 50f;

    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getSuppliesPerMonth().modifyPercent(id, SUPPLY_MALUS);
        stats.getVentRateMult().modifyPercent(id, VENT_RATE_BONUS);
    }

    public void advanceInCombat(ShipAPI ship, float amount) {
        CombatEngineAPI engine = Global.getCombatEngine();
        ShipAPI playerShip = Global.getCombatEngine().getPlayerShip();
        String id = "HMI_RECON";
        float effect_level = (ship.getFluxTracker().getFluxLevel());
		float hull_level = (ship.getHitpoints() / ship.getMaxHitpoints());

        if(ship==playerShip){
            Global.getCombatEngine().maintainStatusForPlayerShip(
                    "RECONKEY",
                    "graphics/icons/hullsys/temporal_shell.png",
                    "Nanite Reconstruction Active",
                    "Hull integrity at " + Math.round(hull_level*100) + "%",
                    false);
        }

        interval.advance(engine.getElapsedInLastFrame());
        if (!ship.getFluxTracker().isOverloaded() && ship.getFluxTracker().isVenting()) {
		        if (interval.intervalElapsed()) {
				ship.setHitpoints(Math.min(ship.getHitpoints() + interval.getIntervalDuration() * effect_level * HULL_REPAIR_MULTIPLIER, ship.getMaxHitpoints()));
				}
        }


    }



    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) return "" + "reconstructs hull when actively venting"; // + Strings.X;
        if (index == 1) return "" + "repaired"; // + Strings.X;
        if (index == 2) return "" + "current flux level"; // + Strings.X;
        if (index == 3) return "" + (int)(VENT_RATE_BONUS) + "%"; // + Strings.X;
        if (index == 4) return "" + (int)(SUPPLY_MALUS) + "%"; // + Strings.X;
        return null;
    }

}