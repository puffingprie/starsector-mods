package data.hullmods;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.BaseHullMod;

public class mi_PrimitiveHangerBay extends BaseHullMod {

  // Without SMod, the refit speed is 50% slower
  public static final float REFIT_SPEED_PENALTY_MULT = 1.5F;
  // With SMod, the refit speed penalty is removed, making it normal
  public static final float SMOD_REFIT_SPEED_NORMAL_MULT = 1.0F;

  public mi_PrimitiveHangerBay() {}

  @Override
  public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
    boolean sMod = isSMod(stats);
    if (sMod) {
      // SMod installed, so apply normal refit speed
      stats.getFighterRefitTimeMult().modifyMult(id, SMOD_REFIT_SPEED_NORMAL_MULT);
    } else {
      // Apply the penalty to refit speed since SMod is not installed
      stats.getFighterRefitTimeMult().modifyMult(id, REFIT_SPEED_PENALTY_MULT);
    }
  }

  @Override
  public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
    if (index == 0) return "50%"; // Description of refit speed penalty
    return null;
  }

  @Override
  public String getSModDescriptionParam(int index, ShipAPI.HullSize hullSize) {
    if (index == 0) {
      // Return the description indicating no penalty to refit time when SMod is installed
      return "0%";
    }
    return null;
  }

  @Override
  public boolean isApplicableToShip(ShipAPI ship) {
    // Check if the hullmod can be applied to the ship (could be any logic you define)
    return ship.getHullSpec().getHullId().startsWith("mi_");
  }
}
