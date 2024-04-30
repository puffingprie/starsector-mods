package data.hullmods.ix;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import java.util.HashMap;
import java.util.Map;

public class MicroFluxShunt extends BaseHullMod
{
	private static Map<Hullsize, Float> mag = new HashMap<Hullsize, Float>();
  
	static { 
		mag.put(HullSize.FRIGATE, 15f);
		mag.put(HullSize.DESTROYER, 12f);
		mag.put(HullSize.CRUISER, 10f);
		mag.put(HullSize.CAPITAL_SHIP, 8f); 
	}
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) { 
		stats.getHardFluxDissipationFraction().modifyFlat(id, ((Float) mag.get(hullSize)) * 0.01f); 
	}
  
	public String getDescriptionParam(int index, HullSize hullSize)
	{
		if (index == 0) return "" + (Integer) mag.get(HullSize.FRIGATE);
		if (index == 1) return "" + (Integer) mag.get(HullSize.DESTROYER);
		if (index == 2) return "" + (Integer) mag.get(HullSize.CRUISER);
		if (index == 3) return "" + (Integer) mag.get(HullSize.CAPITAL_SHIP);
		return null;
	}
  
  public boolean isApplicableToShip(ShipAPI ship) {
		return (!ship.getVariant().getHullMods().contains("fluxshunt"));
	}
  
  public String getUnapplicableReason(ShipAPI ship) {
		if (ship.getVariant().getHullMods().contains("fluxshunt")) return "This ship already has a flux shunt";
		return null;
	}
}