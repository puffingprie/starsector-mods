package data.hullmods.vice;

import java.awt.Color;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class ExperimentalHull extends BaseHullMod {
	
	private static String OVERRIDE_MOD = "vice_standard_plating";
	
	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		
		if (ship.getHullSpec().getHullId().equals("vice_equalizer_ex")) {
			if (ship.getVariant().hasHullMod(OVERRIDE_MOD)) applySkin(ship, "equalizer_ex_classic");
			else applySkin(ship, "equalizer_ex");
		}
		
		else if (ship.getHullSpec().getHullId().equals("vice_flamebreaker_ex")) {
			if (ship.getVariant().hasHullMod(OVERRIDE_MOD)) applySkin(ship, "flamebreaker_ex_classic");
			else applySkin(ship, "flamebreaker_ex");
		}

		else if (ship.getHullSpec().getHullId().equals("vice_hyperion_ex")) {
			if (ship.getVariant().hasHullMod(OVERRIDE_MOD)) applySkin(ship, "hyperion_ex_classic");
			else applySkin(ship, "hyperion_ex");
		}
		
		else if (ship.getHullSpec().getHullId().equals("vice_incursion_ex")) {
			if (ship.getVariant().hasHullMod(OVERRIDE_MOD)) applySkin(ship, "incursion_ex_classic");
			else applySkin(ship, "incursion_ex");
		}
	
		else if (ship.getHullSpec().getHullId().equals("vice_iris_ex")) {
			if (ship.getVariant().hasHullMod(OVERRIDE_MOD)) applySkin(ship, "iris_ex_classic");
			else applySkin(ship, "iris_ex");
		}
	
		else if (ship.getHullSpec().getHullId().equals("vice_mandrake_ex")) {
			if (ship.getVariant().hasHullMod(OVERRIDE_MOD)) applySkin(ship, "mandrake_ex_classic");
			else applySkin(ship, "mandrake_ex");
		}
	
		else if (ship.getHullSpec().getHullId().equals("vice_tigershark_ex")) {
			if (ship.getVariant().hasHullMod(OVERRIDE_MOD)) applySkin(ship, "tigershark_ex_classic");
			else applySkin(ship, "tigershark_ex");
		}
	}
	
	private void applySkin(ShipAPI ship, String spriteString) {
			float x = ship.getSpriteAPI().getCenterX();
			float y = ship.getSpriteAPI().getCenterY();
			float alpha = ship.getSpriteAPI().getAlphaMult();
			float angle = ship.getSpriteAPI().getAngle();
			Color color = ship.getSpriteAPI().getColor();
			ship.setSprite("vice_ships", spriteString);
			ship.getSpriteAPI().setCenter(x, y);
			ship.getSpriteAPI().setAlphaMult(alpha);
			ship.getSpriteAPI().setAngle(angle);
			ship.getSpriteAPI().setColor(color);	
	}
}