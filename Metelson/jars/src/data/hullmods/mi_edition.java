package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.StatBonus;
import java.util.HashMap;
import java.util.Map;











public class mi_edition
  extends BaseHullMod
{
  private static final float DISSIPATION_MULT = 1.1F;
  private static Map mag = new HashMap();
  
  static { mag.put(ShipAPI.HullSize.FRIGATE, Float.valueOf(15.0F));
    mag.put(ShipAPI.HullSize.DESTROYER, Float.valueOf(20.0F));
    mag.put(ShipAPI.HullSize.CRUISER, Float.valueOf(25.0F));
    mag.put(ShipAPI.HullSize.CAPITAL_SHIP, Float.valueOf(35.0F));
  }
  

  public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id)
  {
    stats.getArmorBonus().modifyPercent(id, ((Float)mag.get(hullSize)).floatValue());
    stats.getHullBonus().modifyPercent(id, ((Float)mag.get(hullSize)).floatValue());
    

    stats.getFluxDissipation().modifyMult(id, 1.1F);
  }
  



  public String getDescriptionParam(int index, ShipAPI.HullSize hullSize)
  {
    if (index == 0) return "" + ((Float)mag.get(ShipAPI.HullSize.FRIGATE)).intValue() + "%";
    if (index == 1) return "" + ((Float)mag.get(ShipAPI.HullSize.DESTROYER)).intValue() + "%";
    if (index == 2) return "" + ((Float)mag.get(ShipAPI.HullSize.CRUISER)).intValue() + "%";
    if (index == 3) return "" + ((Float)mag.get(ShipAPI.HullSize.CAPITAL_SHIP)).intValue() + "%";
    if (index == 4) return "10" + "%";
    return null;
  }
  
  public mi_edition() {}
}
