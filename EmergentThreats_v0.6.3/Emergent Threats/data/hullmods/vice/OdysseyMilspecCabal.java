package data.hullmods.vice;

import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;

public class OdysseyMilspecCabal extends BaseHullMod {

	private static float EXTRA_BAYS = 1f;
	private static float SHIELD_BONUS = 20f;
	private static float CR_INCREASE = 11.1f;
	
	private static String DRONE_WING_ID = "vice_disruptor_drone_wing";
	private static String DRONE_VARIANT = "vice_emp_disruptor_drone";
	private static String DRONE_MOD = "vice_disruptor_drone";
	private static String SENSORS = "hiressensors";
	//private static String DRONE_BAY = "secondary drone bay";
	private static String MAIN_HANGAR = "enlarged main hangar";
	private static String DISRUPTOR_DRONE = "EMP Disruptor Drone";
	private static int DRONE_OP_COST = 20;
	
	@Override	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		ShipVariantAPI var = stats.getVariant();
		ShipHullSpecAPI spec = var.getHullSpec();
		
		var.getHullMods().remove(SENSORS);
		var.addPermaMod(DRONE_MOD, false);
		var.addPermaMod(id, false);
		
		stats.getNumFighterBays().modifyFlat(id, EXTRA_BAYS);
		stats.getShieldDamageTakenMult().modifyMult(id, 1f - SHIELD_BONUS * 0.01f);
		stats.getDynamic().getMod("deployment_points_mod").modifyMult(id, 1f + CR_INCREASE * 0.01f);
		stats.getSuppliesToRecover().modifyMult(id, 1f + CR_INCREASE * 0.01f);
		stats.getSuppliesPerMonth().modifyMult(id, 1f + CR_INCREASE * 0.01f);
		
		int fighterBays = stats.getNumFighterBays().getModifiedInt();
		
		//Apotheosis always equipped at bay 0, which is skipped here
		for (int i = 1; i < fighterBays; i++) {
			if (var.getWingId(i) != null && var.getWingId(i).equals(DRONE_WING_ID)) {
				var.setWingId(i, null);
			}
		}
	}
	
	@Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		try {
			CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
			for (CargoStackAPI s : cargo.getStacksCopy()) {
				if (s.isFighterWingStack() && s.getFighterWingSpecIfWing().getVariantId().equals(DRONE_VARIANT)) cargo.removeStack(s);
			}
		}
		catch (Exception e) {}
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		//if (index == 0) return DRONE_BAY;
		if (index == 0) return MAIN_HANGAR;
		if (index == 1) return DISRUPTOR_DRONE;
		if (index == 2) return "" + (int) SHIELD_BONUS + "%";
		if (index == 3) return "" + (int) CR_INCREASE + "%";
		return null;
	}
}

