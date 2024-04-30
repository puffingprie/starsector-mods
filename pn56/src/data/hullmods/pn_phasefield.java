package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.BaseHullMod;


@SuppressWarnings("unchecked")
public class pn_phasefield extends BaseHullMod {

	private static final float PROFILE_DECREASE = 50f;
	private static final String FACTION_PREFIX = "pn_eyfel";
        
        @Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getSensorProfile().modifyPercent(id, -PROFILE_DECREASE);
	}
	
        @Override
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) PROFILE_DECREASE;
		return null;
	}
    @Override
    public boolean isApplicableToShip(ShipAPI ship)
    {
        return (ship.getHullSpec().getHullId().startsWith(FACTION_PREFIX));
    }

}
