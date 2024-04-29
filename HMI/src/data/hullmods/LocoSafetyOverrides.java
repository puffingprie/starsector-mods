package data.hullmods;

import java.awt.Color;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.util.Misc;

public class LocoSafetyOverrides extends BaseHullMod {

	private static Map speed = new HashMap();
	static {
		speed.put(HullSize.FRIGATE, 50f);
		speed.put(HullSize.DESTROYER, 30f);
		speed.put(HullSize.CRUISER, 20f);
		speed.put(HullSize.CAPITAL_SHIP, 10f);
	}
	

	private static final float PEAK_MULT = 0.33f;
	private static final float FLUX_DISSIPATION_MULT = 2f;

	
	private static final float RANGE_THRESHOLD = 700f;
	private static final float RANGE_MULT = 0.25f;



	private static final Set<String> BLOCKED_HULLMODS = new HashSet<>();
	static
	{
		// These hullmods will automatically be removed
		BLOCKED_HULLMODS.add("safetyoverrides");
	}
	private float check=0;
	private String id, ERROR="IncompatibleHullmodWarning";


	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getMaxSpeed().modifyFlat(id, (Float) speed.get(hullSize));
		stats.getAcceleration().modifyFlat(id, (Float) speed.get(hullSize) * 2f);
		stats.getDeceleration().modifyFlat(id, (Float) speed.get(hullSize) * 2f);
		stats.getZeroFluxMinimumFluxLevel().modifyFlat(id, 2f); // set to two, meaning boost is always on 
		
		stats.getFluxDissipation().modifyMult(id, FLUX_DISSIPATION_MULT);
		
		stats.getPeakCRDuration().modifyMult(id, PEAK_MULT);
		stats.getVentRateMult().modifyMult(id, 0f);
		
		stats.getWeaponRangeThreshold().modifyFlat(id, RANGE_THRESHOLD);
		stats.getWeaponRangeMultPastThreshold().modifyMult(id, RANGE_MULT);
	}

	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {

		if (check > 0) {
			check -= 1;
			if (check < 1) {
				ship.getVariant().removeMod(ERROR);
			}
		}

		for (String tmp : BLOCKED_HULLMODS) {
			if (ship.getVariant().getHullMods().contains(tmp)) {
				ship.getVariant().removeMod(tmp);
				ship.getVariant().addMod(ERROR);
				check = 3;
			}
		}
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + ((Float) speed.get(HullSize.FRIGATE)).intValue();
		if (index == 1) return "" + ((Float) speed.get(HullSize.DESTROYER)).intValue();
		if (index == 2) return "" + ((Float) speed.get(HullSize.CRUISER)).intValue();
		if (index == 3) return Misc.getRoundedValue(FLUX_DISSIPATION_MULT);
		if (index == 4) return "3";
		if (index == 5) return Misc.getRoundedValue(RANGE_THRESHOLD);

		return null;
	}

	@Override
	public boolean isApplicableToShip(ShipAPI ship) {
		if (ship.getVariant().getHullSize() == HullSize.CAPITAL_SHIP) return false;
		if (ship.getVariant().hasHullMod(HullMods.CIVGRADE) && !ship.getVariant().hasHullMod(HullMods.MILITARIZED_SUBSYSTEMS)) return false;
		return true;
	}
	
	public String getUnapplicableReason(ShipAPI ship) {
		if (ship.getVariant().getHullSize() == HullSize.CAPITAL_SHIP) {
			return "Can not be installed on capital ships";
		}
		if (ship.getVariant().hasHullMod(HullMods.CIVGRADE) && !ship.getVariant().hasHullMod(HullMods.MILITARIZED_SUBSYSTEMS)) {
			return "Can not be installed on civilian ships";
		}
		
		return null;
	}
	

	private Color color = new Color(255,100,255,255);
	@Override
	public void advanceInCombat(ShipAPI ship, float amount) {
			ship.getEngineController().fadeToOtherColor(this, color, null, 1f, 0.4f);
			ship.getEngineController().extendFlame(this, 0.25f, 0.25f, 0.25f);
//		}
	}

	

}
