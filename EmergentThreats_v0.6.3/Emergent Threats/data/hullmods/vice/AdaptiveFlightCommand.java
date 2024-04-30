package data.hullmods.vice;

import java.awt.Color;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;

import data.scripts.vice.hullmods.RemnantSubsystemsUtil;

public class AdaptiveFlightCommand extends BaseHullMod {

	private float FIGHTER_BAY_COUNT_PENALTY = 50f;
	private float FIGHTER_SPEED_BONUS = 25f;
	private float FIGHTER_REPLACEMENT_BONUS = 50f;
	private int STANDARD_FIGHTER_BAYS_TO_COUNT_AS_CARRIER = 2;
	
	private static String ADAPTIVE_DRONE_BAY = "vice_adaptive_drone_bay";
	private static String GRAPHICS_OVERRIDE_MOD = "vice_converted_bridge";
	private static String NEW_SPRITE = "resplendent_adaptive_flight_command";
	
	//Utility variables
	private RemnantSubsystemsUtil util = new RemnantSubsystemsUtil();
	
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		int builtInOffset = stats.getVariant().getFittedWings().size() - stats.getVariant().getNonBuiltInWings().size();
		stats.getNumFighterBays().modifyMult(id, FIGHTER_BAY_COUNT_PENALTY * 0.01f);
		stats.getNumFighterBays().modifyFlat(id, builtInOffset);
		stats.getFighterRefitTimeMult().modifyMult(id, 1f - FIGHTER_REPLACEMENT_BONUS * 0.01f);
	}

	@Override
	public void applyEffectsToFighterSpawnedByShip(ShipAPI fighter, ShipAPI ship, String id) {
		MutableShipStatsAPI stats = fighter.getMutableStats();
		stats.getMaxSpeed().modifyMult(id, 1f + FIGHTER_SPEED_BONUS * 0.01f);
	}
	
	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		if (ship.getHullSpec().getHullId().equals("vice_resplendent") && !ship.getVariant().hasHullMod(GRAPHICS_OVERRIDE_MOD)) {
			float x = ship.getSpriteAPI().getCenterX();
			float y = ship.getSpriteAPI().getCenterY();
			float alpha = ship.getSpriteAPI().getAlphaMult();
			float angle = ship.getSpriteAPI().getAngle();
			Color color = ship.getSpriteAPI().getColor();
			ship.setSprite("vice_ships", NEW_SPRITE);
			ship.getSpriteAPI().setCenter(x, y);
			ship.getSpriteAPI().setAlphaMult(alpha);
			ship.getSpriteAPI().setAngle(angle);
			ship.getSpriteAPI().setColor(color);	
		}
	}
	
	//workaround for various modded remnant ships lacking the CARRIER hint, 2+ standard bays counts as carrier
	private boolean isCarrier(ShipAPI ship) {
		if (ship.getHullSpec().getHints().contains(ShipHullSpecAPI.ShipTypeHints.CARRIER)) return true;
		int totalFighterBays = (int) ship.getMutableStats().getNumFighterBays().getBaseValue();
		int builtInFighterBays = ship.getHullSpec().getBuiltInWings().size();
		if (totalFighterBays - builtInFighterBays >= STANDARD_FIGHTER_BAYS_TO_COUNT_AS_CARRIER) return true;
		return false;
	}
	
	//checks if the fighter bays to be deleted are unequipped, as "hidden" bays with equipped fighters do not refund Ordance Points
	private boolean baysToDeleteAreEmpty(ShipAPI ship) {
		int totalFighterBays = (int) ship.getMutableStats().getNumFighterBays().getBaseValue();
		int builtInFighterBays = ship.getHullSpec().getBuiltInWings().size();
		int clearFightersFromIndex = (int) Math.ceil((totalFighterBays + builtInFighterBays - 1) * FIGHTER_BAY_COUNT_PENALTY * 0.01f);
		boolean baysAreEmpty = false;
		for (int i = clearFightersFromIndex; i < totalFighterBays; i++) {
			baysAreEmpty = (ship.getVariant().getWingId(i) == null);
			if (!baysAreEmpty) break;
		}
		return baysAreEmpty;
	}
	
	@Override
    public boolean isApplicableToShip(ShipAPI ship) {
		if (ship.getVariant().hasHullMod(ADAPTIVE_DRONE_BAY)) return false;
		return (util.isApplicable(ship) && util.isOnlyRemnantMod(ship) && isCarrier(ship) && baysToDeleteAreEmpty(ship));
	}

	public String getUnapplicableReason(ShipAPI ship) {
		if (ship.getVariant().hasHullMod(ADAPTIVE_DRONE_BAY)) return "Ship does not have standard fighter bays";
		if (!util.isApplicable(ship)) return util.getIncompatibleCauseString("manufacturer");
		if (!util.isOnlyRemnantMod(ship)) return util.getIncompatibleCauseString("modcount");
		if (!isCarrier(ship)) return "Requires minimum 2 free fighter bays";
		if (!baysToDeleteAreEmpty(ship)) return "Fighters are present in bays that will be removed";
		return null;
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) FIGHTER_BAY_COUNT_PENALTY + "%";
		if (index == 1) return "" + (int) FIGHTER_SPEED_BONUS + "%";
		if (index == 2) return "" + (int) FIGHTER_REPLACEMENT_BONUS + "%";
		return null;
	}
}