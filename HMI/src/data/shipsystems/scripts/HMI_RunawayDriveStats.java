package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipEngineControllerAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;

import java.util.List;

import static java.awt.Color.RED;

public class HMI_RunawayDriveStats extends BaseShipSystemScript {

	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        ShipAPI ship = (ShipAPI) stats.getEntity();
        float chargeLevel = Math.min(effectLevel * 2f, 1f);

		if (state == ShipSystemStatsScript.State.IN){

		    stats.getAcceleration().modifyMult(id, 1f - chargeLevel);
            stats.getDeceleration().modifyMult(id, 1f - chargeLevel);
            stats.getTurnAcceleration().modifyMult(id, 1f - chargeLevel);


			
		} else if (state == State.ACTIVE) {	
			stats.getAcceleration().modifyMult(id, 1f);
            stats.getAcceleration().modifyFlat(id, effectLevel * 75f);
            stats.getAcceleration().modifyPercent(id, effectLevel * 75f);
            stats.getMaxSpeed().modifyFlat(id, effectLevel * 100f);
            stats.getMaxSpeed().modifyPercent(id, effectLevel * 100f);
            stats.getDeceleration().modifyMult(id, 0f);
            stats.getTurnAcceleration().modifyMult(id, 1f);
            stats.getTurnAcceleration().modifyFlat(id, effectLevel * 20f);
            stats.getTurnAcceleration().modifyPercent(id, effectLevel * 50f);
            stats.getMaxTurnRate().modifyFlat(id, effectLevel * 10f);
            stats.getMaxTurnRate().modifyPercent(id, effectLevel * 35f);
			
		} else if (state == ShipSystemStatsScript.State.OUT) {
                stats.getMaxSpeed().unmodify(id);
                stats.getMaxTurnRate().unmodify(id);
                stats.getDeceleration().modifyMult(id, 1f);
                stats.getDeceleration().modifyFlat(id, 150f);
                stats.getAcceleration().modifyFlat(id, effectLevel * 100f);
                stats.getAcceleration().modifyPercent(id, effectLevel * 100f);
                stats.getTurnAcceleration().modifyFlat(id, effectLevel * 40f);
                stats.getTurnAcceleration().modifyPercent(id, effectLevel * 100f);
                stats.getMaxTurnRate().modifyFlat(id, effectLevel * 15f);
                stats.getMaxTurnRate().modifyPercent(id, effectLevel * 50f);
		}
	}
	public void unapply(MutableShipStatsAPI stats, String id) {
		ShipAPI ship = (ShipAPI) stats.getEntity();
        stats.getAcceleration().unmodify(id);
        stats.getMaxSpeed().unmodify(id);
        stats.getDeceleration().unmodify(id);
        stats.getTurnAcceleration().unmodify(id);
        stats.getMaxTurnRate().unmodify(id);
	}
	
	public StatusData getStatusData(int index, State state, float effectLevel) {
		if (index == 0 && state == State.IN) {
            return new StatusData("Engine powering up", true);
        } else if (index == 0 && state != State.IN) {
            return new StatusData("Increased engine power", false);
        }
        return null;
	}
}
