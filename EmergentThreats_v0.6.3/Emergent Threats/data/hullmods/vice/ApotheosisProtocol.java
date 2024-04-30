package data.hullmods.vice;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;

public class ApotheosisProtocol extends BaseHullMod {
		
	private static int MIN_OP_REMAINING = 20;
	private static int APOTHEOSIS_BAY_ID = 0;
	
	private static String CABAL_MILSPEC_HULLMOD = "vice_odyssey_milspec_cabal";
	private static String APOTHESIS_WING_ID = "vice_disruptor_drone_wing";
	private static String SHIP_HULL_ID = "uw_odyssey_cabal";
	private static String D_SHIP_HULL_ID = "uw_odyssey_cabal_default_D";
	private static String APOTHEOSIS = "Apotheosis";
	private static String WARNING = "Warning";
	
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		
	}
	
	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		ShipVariantAPI variant = ship.getVariant();
		if (!variant.hasHullMod(CABAL_MILSPEC_HULLMOD)) variant.addMod(CABAL_MILSPEC_HULLMOD);
		ship.getVariant().setWingId(APOTHEOSIS_BAY_ID, APOTHESIS_WING_ID);
		variant.getHullMods().remove(id);
	}
	
	private int getRemainingOP(ShipAPI ship) {
		int unusedOP = 0;
		try {
			unusedOP = ship.getVariant().getUnusedOP(Global.getSector().getCharacterData().getPerson().getFleetCommanderStats()); 
		}
		catch (Exception e) {
			unusedOP = ship.getVariant().getUnusedOP(Global.getFactory().createPerson().getFleetCommanderStats());
		}
		return unusedOP;	
	}
	
	private boolean isCorrectHull(ShipAPI ship) {
		String id = ship.getVariant().getHullSpec().getHullId();
		return (id.equals(SHIP_HULL_ID) || id.equals(D_SHIP_HULL_ID));
	}
	
	@Override
    public boolean isApplicableToShip(ShipAPI ship) {
		if (getRemainingOP(ship) < MIN_OP_REMAINING) return false;
		if (ship.getVariant().getWingId(APOTHEOSIS_BAY_ID) != null) return false;
		return isCorrectHull(ship);
	}
	
	public String getUnapplicableReason(ShipAPI ship) {
		if (!isCorrectHull(ship)) return "Incompatible hull";
		if (getRemainingOP(ship) < MIN_OP_REMAINING) return "Insufficient ordnance points remaining";
		if (ship.getVariant().getWingId(APOTHEOSIS_BAY_ID) != null) return "Bay 1 is occupied";
		return null;
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return APOTHEOSIS;
		if (index == 1) return WARNING;
		return null;
	}
}