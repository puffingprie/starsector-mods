package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;



public class pnsilverfishPhaseCloakStats extends BaseShipSystemScript {
            	public static final float INCOMING_DAMAGE_MULT = 0.8f;

public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
                effectLevel = 1f;
		if (state == ShipSystemStatsScript.State.ACTIVE) {
			stats.getMaxSpeed().modifyPercent(id, 500f * effectLevel); // to slow down ship to its regular top speed while powering drive down
			stats.getMaxTurnRate().modifyPercent(id, 500f * effectLevel);
			stats.getDeceleration().modifyPercent(id, 500f * effectLevel);					
		} else {
			stats.getMaxSpeed().modifyFlat(id, 300f * effectLevel);
			stats.getAcceleration().modifyFlat(id, 450f * effectLevel);
			stats.getDeceleration().modifyFlat(id, 120f * effectLevel);
			stats.getTurnAcceleration().modifyFlat(id, 90f * effectLevel);
			stats.getTurnAcceleration().modifyPercent(id, 200f * effectLevel);
			stats.getMaxTurnRate().modifyFlat(id, 40f * effectLevel);
			stats.getMaxTurnRate().modifyPercent(id, 100f * effectLevel);
                
		stats.getHullDamageTakenMult().modifyMult(id, 1f - (1f - INCOMING_DAMAGE_MULT) * effectLevel);
		stats.getArmorDamageTakenMult().modifyMult(id, 1f - (1f - INCOMING_DAMAGE_MULT ) * effectLevel);
		stats.getEmpDamageTakenMult().modifyMult(id, 1f - (1f - INCOMING_DAMAGE_MULT) * effectLevel);
		}
	}
	public void unapply(MutableShipStatsAPI stats, String id) {
		stats.getMaxSpeed().unmodify(id);
		stats.getMaxTurnRate().unmodify(id);
		stats.getTurnAcceleration().unmodify(id);
		stats.getAcceleration().unmodify(id);
		stats.getDeceleration().unmodify(id);
                
                stats.getHullDamageTakenMult().unmodify(id);
		stats.getArmorDamageTakenMult().unmodify(id);
		stats.getEmpDamageTakenMult().unmodify(id);
	}

        
        
	public StatusData getStatusData(int index, State state, float effectLevel) {

		if (index == 0) {
			return new StatusData("improved maneuverability", false);
		} else if (index == 1) {
			return new StatusData("increased top speed", false);
		}
		return null;
                
                
                
	}
}
