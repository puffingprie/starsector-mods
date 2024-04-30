package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;

public class SCY_armorChild extends BaseHullMod {

  private final String id = "SCY_childModule";

  @Override
  public void advanceInCombat(ShipAPI ship, float amount) {

    if (!ship.isAlive()) return;
    ship.setDrone(true);
  }

  @Override
  public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
    if(!ship.hasListenerOfClass(SCY_modularArmor.ExplosionOcclusionRaycast.class)) ship.addListener(new SCY_modularArmor.ExplosionOcclusionRaycast());
  }

  @Override
  public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
    return null;
  }
}
