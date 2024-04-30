package data.scripts.vice.shipsystems;

import com.fs.starfarer.api.combat.FighterLaunchBayAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;

public class LightOfSindria extends BaseShipSystemScript {
	
	private static String LG_FIGHTER_WING = "vice_gladius_lg_wing";
	
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		ShipAPI ship = null;
		if (stats.getEntity() instanceof ShipAPI) {
			ship = (ShipAPI) stats.getEntity();
		}
		else return;
		
		if (effectLevel == 1) {
			for (FighterLaunchBayAPI bay : ship.getLaunchBaysCopy()) {
				if (bay.getWing() == null || !bay.getWing().getWingId().equals(LG_FIGHTER_WING)) continue;
				bay.setFastReplacements(bay.getNumLost());
			}
		}
	}
		
	public void unapply(MutableShipStatsAPI stats, String id) {

	}
		
	public StatusData getStatusData(int index, State state, float effectLevel) {

		return null;
	}

	@Override
	public boolean isUsable(ShipSystemAPI system, ShipAPI ship) {
		return true;
	}
}