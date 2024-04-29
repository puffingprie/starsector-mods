package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;

public class expsp_passivechronoboost extends BaseHullMod {

	    public static final float TIME_MULT = 20f;

	

	public void advanceInCombat(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
		 ShipAPI ship = null;

		 if (stats.getEntity() instanceof ShipAPI) {
			 ship = (ShipAPI) stats.getEntity();
		 } else {
			 return;
		 }

        if (Global.getCombatEngine().isPaused()) {
            return;
        }


        boolean player = ship == Global.getCombatEngine().getPlayerShip();

        if ( !ship.isAlive() || ship.isPiece() ) {
            return;
        }

        ship.setJitterShields(false);

        if (ship.getSystem() != null) {
            if (!ship.getSystem().isActive() && !ship.getFluxTracker().isOverloadedOrVenting()) {






                    if (player) {
                        ship.getMutableStats().getTimeMult().modifyPercent(id, TIME_MULT);
                        Global.getCombatEngine().getTimeMult().modifyPercent(id, 1f / TIME_MULT);
                        Global.getCombatEngine().maintainStatusForPlayerShip(id, "graphics/icons/hullsys/temporal_shell.png", "Temporal Field", "Timeflow at 110%", false);
                    } else {
                        ship.getMutableStats().getTimeMult().modifyPercent(id, TIME_MULT);
                        Global.getCombatEngine().getTimeMult().unmodify(id);
                    }



                }


            } else {
                ship.getMutableStats().getTimeMult().unmodify(id);
                Global.getCombatEngine().getTimeMult().unmodify(id);
            }
        }

	public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
		if (index == 0) return "" + (int)(TIME_MULT) + " %";
		return null;
	}



}



