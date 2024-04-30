package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.MutableStat;

public class HeavyRostorStats extends com.fs.starfarer.api.impl.combat.BaseShipSystemScript
{
  public static final float INCOMING_DAMAGE_MULT = 0.35F;
  public static final float INCOMING_EMP_NERF = 1.0F;
  
  public HeavyRostorStats() {}
  
  public void apply(MutableShipStatsAPI stats, String id, com.fs.starfarer.api.plugins.ShipSystemStatsScript.State state, float effectLevel)
  {
    effectLevel = 1.0F;
    


    stats.getHullDamageTakenMult().modifyMult(id, 0.65F);
    stats.getArmorDamageTakenMult().modifyMult(id, 0.65F);
    stats.getEmpDamageTakenMult().modifyMult(id, 0.65F);
  }
  






  public void unapply(MutableShipStatsAPI stats, String id)
  {
    stats.getMaxSpeed().unmodify(id);
    stats.getMaxTurnRate().unmodify(id);
    stats.getTurnAcceleration().unmodify(id);
    stats.getAcceleration().unmodify(id);
    stats.getDeceleration().unmodify(id);
  }
  

  public com.fs.starfarer.api.plugins.ShipSystemStatsScript.StatusData getStatusData(int index, com.fs.starfarer.api.plugins.ShipSystemStatsScript.State state, float effectLevel)
  {
    if (index == 0) {
      return new com.fs.starfarer.api.plugins.ShipSystemStatsScript.StatusData("All Damage reduced by 65%", false);
    }
    return null;
  }
}
