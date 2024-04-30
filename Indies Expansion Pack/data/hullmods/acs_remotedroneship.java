package data.scripts.hullmods;

// import com.fs.graphics.util.Fader.State;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import java.util.HashMap;
import java.util.Map;


import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;


import com.fs.starfarer.api.combat.CombatEngineAPI;

import com.fs.starfarer.api.util.IntervalUtil;

import java.awt.Color;
import com.fs.starfarer.api.combat.ShipCommand;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.combat.entities.Ship;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
//import data.scripts.campaign.ids.acs_ids;
import com.fs.starfarer.api.combat.CombatFleetManagerAPI;
import com.fs.starfarer.api.combat.DeployedFleetMemberAPI;

public class acs_remotedroneship extends BaseHullMod {

	//public static final float UPKEEP_REDUCTION = 100f;
    //public static final float FUEL_ADDED = 250f;
    public static final float MIN_CR = 0.1f;
    public static final float UPKEEP_REDUCTION = 100f;
    public static final float PEAK_CR_DEGRADATION = 90f;
    public static final float MAX_CR_PENALTY = 90f;
    public static final String ACS_HULLMOD_REMOTEDRONESHIPCOMMAND = "acs_remotedroneshipcommand";

    public void applyEffectsBeforeShipCreation(MutableShipStatsAPI stats, String id){

        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();

        for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {

            if (member.isMothballed()) continue;
			if (member.getRepairTracker().getCR() < MIN_CR) continue;

            if (member.getVariant().hasHullMod(ACS_HULLMOD_REMOTEDRONESHIPCOMMAND)) {
                stats.getMinCrewMod().modifyMult(id,1f - UPKEEP_REDUCTION * 0.01f);
                
            } else{
                stats.getMaxCombatReadiness().modifyMult(id,1f - MAX_CR_PENALTY * 0.01f);
            }
        }

    };

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {

        super.advanceInCombat(ship, amount);

        amount = (float) 1f;
        if (!ship.isAlive() || ship.getCurrentCR() == 0f) {return;}

        String id = ship.getId();
        MutableShipStatsAPI stats = ship.getMutableStats();

        CombatEngineAPI engine = Global.getCombatEngine();
        //String key = "acs_remotedroneship" + "_" + ship.getId();

		//if (engine == null) return;
		
		CombatFleetManagerAPI manager = engine.getFleetManager(ship.getOriginalOwner());
		if (manager == null) return;
		
		// DeployedFleetMemberAPI member = manager.getDeployedFleetMember(ship);
		// if (member == null) return;

        for (FleetMemberAPI members : manager.getDeployedCopy()) {

            //if (members.isMothballed()) continue;
			//if (members.getRepairTracker().getCR() < MIN_CR) continue;
            if (members.isAlly()) continue;

            if (!members.getVariant().hasHullMod(ACS_HULLMOD_REMOTEDRONESHIPCOMMAND)) {
                stats.getCRLossPerSecondPercent().modifyFlat(id, PEAK_CR_DEGRADATION);
            } else {

                stats.getCRLossPerSecondPercent().unmodify(id);

            }

            // if (members.getVariant().hasHullMod(acs_ids.ACS_HULLMOD_REMOTEDRONESHIPCOMMAND)) {
            //     stats.getPeakCRDuration().unmodify(id);
            // }
        }


        // if (ship.isAlive()) {
            
        // }

        
    }

    public String getDescriptionParam(int index, HullSize hullSize) {

        if (index == 0) return "Droneship Operation Center";
		if (index == 1) return "" + (int) UPKEEP_REDUCTION + "%";
        if (index == 2) return "" + (int) PEAK_CR_DEGRADATION + "%";
        if (index == 3) return "" + (int) MAX_CR_PENALTY + "%";
        
		return null;
	}
}
