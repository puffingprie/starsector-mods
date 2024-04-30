package data.hullmods;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import com.fs.starfarer.api.combat.BaseHullMod;

// script credit: Sundog
public class pn_sensors extends BaseHullMod
{
    private static final Set BLOCKED_HULLMODS = new HashSet();
    private static final Random rand = new Random();
    private static final String FACTION_PREFIX = "pn_eyfel";
@SuppressWarnings("unchecked")

	private static final Map mag = new HashMap();
	        static
        {
        // These hullmods will automatically be removed
        // Not as elegant as blocking them in the first place, but
        // this method doesn't require editing every hullmod's script
        BLOCKED_HULLMODS.add("heavyarmor");		
        }
                static 
        {
		mag.put(HullSize.FRIGATE, 200f);
		mag.put(HullSize.DESTROYER, 175f);
		mag.put(HullSize.CRUISER, 150f);
		mag.put(HullSize.CAPITAL_SHIP, 125f);
	}   

	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getSensorStrength().modifyPercent(id, ((Float) mag.get(hullSize)).intValue());
	}
	

	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + ((Float) mag.get(HullSize.FRIGATE)).intValue();
		if (index == 1) return "" + ((Float) mag.get(HullSize.DESTROYER)).intValue();
		if (index == 2) return "" + ((Float) mag.get(HullSize.CRUISER)).intValue();
                if (index == 2) return "" + ((Float) mag.get(HullSize.CAPITAL_SHIP)).intValue();
		return null;
	}
    @Override
    public boolean isApplicableToShip(ShipAPI ship)
    {
        return (ship.getHullSpec().getHullId().startsWith(FACTION_PREFIX));
    }

}
