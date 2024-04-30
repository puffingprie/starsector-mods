package data.scripts.ix.shipsystems;

//import org.lwjgl.util.vector.Vector2f;

//import com.fs.starfarer.api.Global;
//import com.fs.starfarer.api.combat.CombatEngineAPI;
//import com.fs.starfarer.api.combat.MissileAPI;
//import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
//import com.fs.starfarer.api.combat.ShipAPI.HullSize;
//import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI.SystemState;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

import data.scripts.ix.DistanceUtil;

public class InterdictorPulseTargeting extends BaseShipSystemScript {
	
	private static float RANGE = 2000f;
	
	public StatusData getStatusData(int index, State state, float effectLevel) {
		return null;
	}

	@Override
	public String getInfoText(ShipSystemAPI system, ShipAPI ship) {
		if (system.isOutOfAmmo()) return null;
		if (system.getState() != SystemState.IDLE) return null;
		if (ship == null) return null;
		ShipAPI target = ship.getShipTarget();
		if (target == null) return "NO TARGET";
		if (target.getOwner() == ship.getOwner() 
					|| target.getOwner() == 100 
					|| target.getOwner() == ship.getOwner()
					|| target.isFighter()
					|| target.isPhased()) return "INVALID TARGET";
		if (DistanceUtil.getDistance(ship, target) > RANGE) return "OUT OF RANGE";
		return "READY";
	}
	
	@Override
	public boolean isUsable(ShipSystemAPI system, ShipAPI ship) {
		if (system.isActive()) return true;
		if (system.isOutOfAmmo()) return false;
		if (ship == null) return false;
		ShipAPI target = ship.getShipTarget();
		if (target == null) return false;
		if (target.getOwner() == ship.getOwner() 
					|| target.getOwner() == 100 
					|| target.getOwner() == ship.getOwner()
					|| target.isFighter()
					|| target.isPhased()) return false;
		return (DistanceUtil.getDistance(ship, target) <= RANGE);
	}
}