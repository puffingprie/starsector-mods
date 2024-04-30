package data.hullmods.vice;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;

import data.scripts.vice.hullmods.RemnantSubsystemsUtil;

public class AdaptiveDroneBay extends BaseHullMod {

	private static float EXTRA_BAYS = 1f;
	private static String CONVERTED_HANGAR = "converted_hangar";
	private static String VAST_HANGAR = "vast_hangar";
	private static String DRONE_BAY = "combat drone bay";
	
	//exception shipIds always qualify
	private static String MIMESIS = "vice_mimesis";
	private static String MIMESIS_D = "vice_mimesis_default_D";
	
	//Utility variables
	private RemnantSubsystemsUtil util = new RemnantSubsystemsUtil();
	
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		float bays = EXTRA_BAYS;
		if (stats.getVariant().hasHullMod(VAST_HANGAR)) bays++;
		stats.getNumFighterBays().modifyFlat(id, bays);
	}
	
	@Override
    public boolean isApplicableToShip(ShipAPI ship) {
		if (ship.isFrigate()) return false;
		if (ship.getHullSpec().getHullId().equals(MIMESIS) || ship.getHullSpec().getHullId().equals(MIMESIS_D)) return true;
		if (ship.getVariant().hasHullMod("ix_odyssey_retrofit")) return false;
		if (ship.getVariant().hasHullMod("vice_shipwide_integration")) return false;
		if (ship.getHullSpec().getFighterBays() > 0) return false;
		if (ship.getVariant().hasHullMod("converted_hangar")) return false;
		if (ship.getVariant().hasHullMod("converted_fighterbay")) return false;
		if (ship.getVariant().hasHullMod("phasefield")) return false;

		return (util.isApplicable(ship) && util.isOnlyRemnantMod(ship));
	}

	public String getUnapplicableReason(ShipAPI ship) {
		if (ship.getVariant().hasHullMod("vice_shipwide_integration")) return util.getIncompatibleCauseString("module");
		if (ship.isFrigate()) return "Cannot be installed on frigate";
		if (ship.getVariant().hasHullMod("ix_odyssey_retrofit")) return "Incompatible hull, install Terminus Relay to enable drones";
		if (ship.getHullSpec().getFighterBays() > 0) return "Ship has standard fighter bays";
		if (ship.getVariant().hasHullMod("converted_hangar")) return "Ship has a converted hangar";
		if (ship.getVariant().hasHullMod("converted_fighterbay")) return "Ship has converted fighter bays";
		if (ship.getVariant().hasHullMod("phasefield")) return "Cannot be installed on phase ship";
		if (!util.isApplicable(ship)) return util.getIncompatibleCauseString("manufacturer");
		if (!util.isOnlyRemnantMod(ship)) return util.getIncompatibleCauseString("modcount");
		return null;
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return DRONE_BAY;
		return null;
	}
}