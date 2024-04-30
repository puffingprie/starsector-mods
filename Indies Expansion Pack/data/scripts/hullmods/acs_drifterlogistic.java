package data.scripts.hullmods;

// import com.fs.graphics.util.Fader.State;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;

import com.fs.starfarer.api.combat.ShipAPI.HullSize;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;



import com.fs.starfarer.api.campaign.CampaignFleetAPI;


public class acs_drifterlogistic extends BaseHullMod {

    public static final float FUEL_ADDED = 250f;

    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        
        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();

        int fleetNumber = fleet.getFleetData().getNumMembers();

        if (fleetNumber >= 2){
            stats.getFuelMod().modifyFlat(id, FUEL_ADDED);
        } else{

            stats.getFuelMod().unmodify(id);
        };

    }

    public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) FUEL_ADDED + "%";
		return null;
	}
}
