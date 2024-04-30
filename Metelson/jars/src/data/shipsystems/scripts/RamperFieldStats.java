package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;

public class RamperFieldStats extends com.fs.starfarer.api.impl.combat.BaseShipSystemScript
{
  public static final float INCOMING_EMP_NERF = 0.9F;  
  
  public RamperFieldStats() {}
  
  public void apply(MutableShipStatsAPI stats, String id, ShipSystemStatsScript.State state, float effectLevel)
  {
    effectLevel = 1.0F;
    
    stats.getArmorDamageTakenMult().modifyMult(id, 0.6F);
    stats.getHullDamageTakenMult().modifyMult(id, 0.6F);
    stats.getMaxSpeed().modifyMult(id, 1.2F);
  }
  


  public void unapply(MutableShipStatsAPI stats, String id)
  {
    stats.getHullDamageTakenMult().unmodify(id);
    stats.getArmorDamageTakenMult().unmodify(id);
    stats.getEmpDamageTakenMult().unmodify(id);
    
    stats.getMaxSpeed().unmodify(id);
    stats.getMaxTurnRate().unmodify(id);
    stats.getTurnAcceleration().unmodify(id);
    stats.getAcceleration().unmodify(id);
    stats.getDeceleration().unmodify(id);
  }
  

  public com.fs.starfarer.api.plugins.ShipSystemStatsScript.StatusData getStatusData(int index, ShipSystemStatsScript.State state, float effectLevel)
  {
    if (index == 0) {
      return new com.fs.starfarer.api.plugins.ShipSystemStatsScript.StatusData("Hull and Armor damage lowered by 40%", false);
    }
    

    return null;
  }
}
