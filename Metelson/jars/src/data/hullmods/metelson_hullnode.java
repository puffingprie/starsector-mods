package data.hullmods;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.ui.Alignment;

public class metelson_hullnode extends BaseHullMod {
  public static final float DAMAGE_REDUCTION = 25.0F;
  public static final float DAMAGE_PENALTY = 30.0F;
  public static final float SMOD_DAMAGE_REDUCTION_BONUS = 10.0F; // Additional reduction for SMod
  private static Map<HullSize, Float> mag = new HashMap<>();

  static {
    mag.put(HullSize.FRIGATE, 50.0F);
    mag.put(HullSize.DESTROYER, 100.0F);
    mag.put(HullSize.CRUISER, 150.0F);
    mag.put(HullSize.CAPITAL_SHIP, 200.0F);
  }

  @Override
  public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
    stats.getArmorBonus().modifyFlat(id, mag.get(hullSize));

    boolean sMod = isSMod(stats);
    float damageTakenMultiplier = 0.75F - (sMod ? SMOD_DAMAGE_REDUCTION_BONUS / 100.0F : 0);  // Apply SMod bonus if present

    stats.getProjectileDamageTakenMult().modifyMult(id, damageTakenMultiplier);
    stats.getEmpDamageTakenMult().modifyMult(id, damageTakenMultiplier);
    stats.getFragmentationDamageTakenMult().modifyMult(id, damageTakenMultiplier);
    stats.getBeamDamageTakenMult().modifyMult(id, sMod ? 1.20F : 1.3F); // Reduce the penalty for beam damage if SMod
  }


  @Override
  public String getDescriptionParam(int index, HullSize hullSize) {
    if (index == 0) return "" + mag.get(HullSize.FRIGATE).intValue();
    if (index == 1) return "" + mag.get(HullSize.DESTROYER).intValue();
    if (index == 2) return "" + mag.get(HullSize.CRUISER).intValue();
    if (index == 3) return "" + mag.get(HullSize.CAPITAL_SHIP).intValue();
    if (index == 4) return "" + (int) DAMAGE_REDUCTION + "%";
    if (index == 5) return "" + (int) DAMAGE_PENALTY + "%";
    return null;
  }

  public String getSModDescriptionParam(int index, HullSize hullSize) {
    if (index == 0) {
      // SMod additional damage reduction bonus
      return "" + (int) SMOD_DAMAGE_REDUCTION_BONUS + "%";
    } else if (index == 1) {
      // SMod reduced beam damage penalty
      // Assuming that 20% is the reduction to the penalty, not the new penalty value.
      // The actual new penalty value, after reduction, would be 1.20 - 1.0, which is 20%.
      return "20%";
    }
    return null;
  }
}

