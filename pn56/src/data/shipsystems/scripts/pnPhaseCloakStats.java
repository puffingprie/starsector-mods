package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;

public class pnPhaseCloakStats extends BaseShipSystemScript {

@Override
public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
                effectLevel = 1f;
		if (state == ShipSystemStatsScript.State.ACTIVE) {
			stats.getMaxSpeed().modifyPercent(id, 500f * effectLevel); // to slow down ship to its regular top speed while powering drive down
			stats.getMaxTurnRate().modifyPercent(id, 200f * effectLevel);
			stats.getDeceleration().modifyPercent(id, 200f * effectLevel);					
		} else {
			stats.getMaxSpeed().modifyFlat(id, 500f * effectLevel);
			stats.getAcceleration().modifyFlat(id, 200f * effectLevel);
			stats.getDeceleration().modifyFlat(id, 200f * effectLevel);
			stats.getTurnAcceleration().modifyFlat(id, 200f * effectLevel);
			stats.getTurnAcceleration().modifyPercent(id, 200f * effectLevel);
			stats.getMaxTurnRate().modifyFlat(id, 200f * effectLevel);
			stats.getMaxTurnRate().modifyPercent(id, 200f * effectLevel);
		}
	}

@Override
	public void unapply(MutableShipStatsAPI stats, String id) {
		stats.getMaxSpeed().unmodify(id);
		stats.getMaxTurnRate().unmodify(id);
		stats.getTurnAcceleration().unmodify(id);
		stats.getAcceleration().unmodify(id);
		stats.getDeceleration().unmodify(id);
	}


    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData("improved speed", false);
        } else if (index == 1) {
            return new StatusData("Increased top speed", false);
        }
        return null;
    }
}
