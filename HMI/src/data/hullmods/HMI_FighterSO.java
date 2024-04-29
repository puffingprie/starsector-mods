package data.hullmods;

import java.awt.Color;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class HMI_FighterSO extends BaseHullMod
{
    private static final float SPEED_BONUS = 75f;
    private static final float FLUX_DISSIPATION_MULT = 2f;
    private static final float RANGE_THRESHOLD = 450f;
    private static final float RANGE_MULT = 0.25f;

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id)
    {
        //stats.getMaxSpeed().modifyFlat(id, SPEED_BONUS);	// Doesn't show calculated speed in refit screen - buffed in CSV entry instead
        stats.getAcceleration().modifyFlat(id, SPEED_BONUS);
        stats.getDeceleration().modifyFlat(id, SPEED_BONUS);
        //stats.getZeroFluxMinimumFluxLevel().modifyFlat(id, 2f); // set to two, meaning boost is always on 

        stats.getFluxDissipation().modifyMult(id, FLUX_DISSIPATION_MULT);

        //stats.getVentRateMult().modifyMult(id, 0f);
        stats.getWeaponRangeThreshold().modifyFlat(id, RANGE_THRESHOLD);
        stats.getWeaponRangeMultPastThreshold().modifyMult(id, RANGE_MULT);

    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize)
    {
        return null;
    }

    private final Color color = new Color(255, 100, 255, 255);

    @Override
    public void advanceInCombat(ShipAPI ship, float amount)
    {
        //ship.getFluxTracker().setHardFlux(ship.getFluxTracker().getCurrFlux());
//		if (ship.getEngineController().isAccelerating() || 
//				ship.getEngineController().isAcceleratingBackwards() ||
//				ship.getEngineController().isDecelerating() ||
//				ship.getEngineController().isTurningLeft() ||
//				ship.getEngineController().isTurningRight() ||
//				ship.getEngineController().isStrafingLeft() ||
//				ship.getEngineController().isStrafingRight()) {
        ship.getEngineController().fadeToOtherColor(this, color, null, 1f, 0.4f);
        ship.getEngineController().extendFlame(this, 0.25f, 0.25f, 0.25f);
//		}
    }
}
