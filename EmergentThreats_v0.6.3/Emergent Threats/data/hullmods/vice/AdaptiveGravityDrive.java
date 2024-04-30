package data.hullmods.vice;

import java.awt.Color;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

import data.scripts.vice.hullmods.RemnantSubsystemsUtil;

public class AdaptiveGravityDrive extends BaseHullMod {
	
	private static String FLEET_JUMP = "fleet jump";
	private static String LAMPETIA_HULLMOD = "vice_lampetia_remnant";
	private static String RESPLENDENT_HULLMOD = "vice_resplendent_remnant";
	private static String RESPLENDENT_PROTOTYPE_MOD = "vice_resplendent_prototype";
	private static String SHIP_1 = "Lampetia";
	private static String SHIP_2 = "Resplendent";
	
	private static String GRAPHICS_OVERRIDE_MOD = "vice_converted_bridge";
	private static String NEW_SPRITE = "resplendent_adaptive_gravity_drive";
	
	//Utility variables
	private RemnantSubsystemsUtil util = new RemnantSubsystemsUtil();
	
	//dummy hullmod, system switch handled by built-in hullmods
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
	
	@Override
    public boolean isApplicableToShip(ShipAPI ship) {
		return (isValidShip(ship) && util.isOnlyRemnantMod(ship));
	}
	
	private boolean isValidShip(ShipAPI ship) {
		return (ship.getVariant().getHullMods().contains(LAMPETIA_HULLMOD) 
				|| ship.getVariant().getHullMods().contains(RESPLENDENT_HULLMOD)
				|| ship.getVariant().getHullMods().contains(RESPLENDENT_PROTOTYPE_MOD));
	}
	
	public String getUnapplicableReason(ShipAPI ship) {
		if (!isValidShip(ship)) return "Incompatible hull";
		if (!util.isApplicable(ship)) return util.getIncompatibleCauseString("manufacturer");
		if (!util.isOnlyRemnantMod(ship)) return util.getIncompatibleCauseString("modcount");
		return null;
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + FLEET_JUMP;
		if (index == 1) return SHIP_1;
		if (index == 2) return SHIP_2;
		return null;
	}
}