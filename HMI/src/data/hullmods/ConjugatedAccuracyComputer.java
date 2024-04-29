package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

import java.awt.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ConjugatedAccuracyComputer extends BaseHullMod {

	private static Map ConjSpeed = new HashMap();
	static {
		ConjSpeed.put(HullSize.FRIGATE, 30f);
		ConjSpeed.put(HullSize.DESTROYER, 20f);
		ConjSpeed.put(HullSize.CRUISER, 10f);
		ConjSpeed.put(HullSize.CAPITAL_SHIP, 5f);
	}
	
	
	private static final float OVERLOAD_DUR = 50f;
	private static final float VENT_NERF = 33f;
	private static final float LOW_SPEED_MOD = 0f;
	private static final float RANGE_BOOST = 60f;

	private static final Set<String> BLOCKED_HULLMODS = new HashSet<>();
    static
    {
        // These hullmods will automatically be removed
        BLOCKED_HULLMODS.add("safetyoverrides");
		BLOCKED_HULLMODS.add("dedicated_targeting_core");
		BLOCKED_HULLMODS.add("targetingunit");
		BLOCKED_HULLMODS.add("advancedoptics");
    }
    private float check=0;
    private String ID, ERROR="IncompatibleHullmodWarning";
	private Color color = new Color(100,255,255,255);

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getMaxSpeed().modifyPercent(id, -(Float) ConjSpeed.get(hullSize));
		stats.getAcceleration().modifyPercent(id, -(Float) ConjSpeed.get(hullSize) * 0.5f);
		stats.getDeceleration().modifyPercent(id, -(Float) ConjSpeed.get(hullSize));
		stats.getZeroFluxSpeedBoost().modifyMult(id, LOW_SPEED_MOD);
	    stats.getFluxDissipation().modifyMult(id, 1f - VENT_NERF * 0.01f);	
		stats.getOverloadTimeMod().modifyPercent(id, OVERLOAD_DUR);
		stats.getVentRateMult().modifyMult(id, 0f);
		stats.getBallisticWeaponRangeBonus().modifyMult(id, 1f + RANGE_BOOST * 0.01f);
		stats.getEnergyWeaponRangeBonus().modifyMult(id, 1f + RANGE_BOOST * 0.01f);
		ID=id;
	}

	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id){

		for (String tmp : BLOCKED_HULLMODS) {
			if (ship.getVariant().getHullMods().contains(tmp)) {
				ship.getVariant().removeMod(tmp);
				ship.getVariant().addMod(ERROR);
			}
		}
	}

	@Override
	public void advanceInCombat(ShipAPI ship, float amount) {
		ship.getEngineController().fadeToOtherColor(this, color, null, 1f, 0.4f);
		ship.getEngineController().extendFlame(this, 0.25f, 0.25f, 0.25f);
	}

	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) RANGE_BOOST + "%";		
		if (index == 1) return "" + ((Float) ConjSpeed.get(HullSize.FRIGATE)).intValue() + "%";
		if (index == 2) return "" + ((Float) ConjSpeed.get(HullSize.DESTROYER)).intValue() + "%";
		if (index == 3) return "" + ((Float) ConjSpeed.get(HullSize.CRUISER)).intValue() + "%";
		if (index == 4) return "" + ((Float) ConjSpeed.get(HullSize.CAPITAL_SHIP)).intValue() + "%";
		if (index == 5) return "" + (int) VENT_NERF + "%";
		if (index == 6) return "" + (int) OVERLOAD_DUR + "%";
		return null;
	}
	
}
