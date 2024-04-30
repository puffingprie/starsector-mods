package data.hullmods;

import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;


public class pn_bay extends BaseHullMod {

	public static final int CREW_REQ = 20;
	//public static final int CARGO_REQ = 80;
	
	private static Map mag = new HashMap();
	static {
		mag.put(HullSize.FRIGATE, 75f);
		mag.put(HullSize.DESTROYER, 75f);
		mag.put(HullSize.CRUISER, 75f);
		mag.put(HullSize.CAPITAL_SHIP, 75f);
	}
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getFighterRefitTimeMult().modifyPercent(id, ((Float) mag.get(hullSize)));
		stats.getNumFighterBays().modifyFlat(id, 1f);
		
		stats.getMinCrewMod().modifyFlat(id, CREW_REQ);
		//stats.getCargoMod().modifyFlat(id, -CARGO_REQ);
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + ((Float) mag.get(HullSize.DESTROYER)).intValue() + "%";
		if (index == 1) return "" + ((Float) mag.get(HullSize.CRUISER)).intValue() + "%";
		if (index == 2) return "" + ((Float) mag.get(HullSize.CAPITAL_SHIP)).intValue() + "%";
		if (index == 3) return "" + CREW_REQ;
		return null;
		//if (index == 0) return "" + ((Float) mag.get(hullSize)).intValue();
		//return null;
	}
	

	public boolean isApplicableToShip(ShipAPI ship) {
		//if (ship.getMutableStats().getCargoMod().computeEffective(ship.getHullSpec().getCargo()) < CARGO_REQ) return false;
		
		return ship != null && !ship.isFrigate() && ship.getHullSpec().getFighterBays() <= 0 &&
								!ship.getVariant().hasHullMod(HullMods.PHASE_FIELD);
								//ship.getHullSpec().getShieldType() != ShieldType.PHASE;		
	}
	
	public String getUnapplicableReason(ShipAPI ship) {
		if (ship != null && ship.isFrigate()) return "Can not be installed on a frigate";
		if (ship != null && ship.getHullSpec().getFighterBays() > 0) return "Ship has standard fighter bays";
		return "Can not be installed on a phase ship";
	}
}



