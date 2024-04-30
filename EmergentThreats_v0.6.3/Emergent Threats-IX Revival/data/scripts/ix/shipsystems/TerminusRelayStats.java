package data.scripts.ix.shipsystems;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI.SystemState;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

import data.scripts.ix.DistanceUtil;

public class TerminusRelayStats extends BaseShipSystemScript {
	
	private static float RELAY_RANGE = 1000f;
	private static float TARGET_RANGE = 1000f;
	private static float DAMAGE = 300f;
	private static float EMP_DAMAGE = 300f;
	private static boolean IS_READY = false;

	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		if (!IS_READY) return;

		ShipAPI drone0 = null;
		ShipAPI drone1 = null;
		ShipAPI target0 = null;
		ShipAPI target1 = null;
		ShipAPI ship = null;
		
		if (stats.getEntity() instanceof ShipAPI) ship = (ShipAPI) stats.getEntity();
		else return;
		
		List<ShipAPI> drones = getFighters(ship);
		if (drones.isEmpty()) return;
		else {
			drone0 = (ShipAPI) drones.get(0);
			if (drones.size() > 1) drone1 = (ShipAPI) drones.get(1);
		}
		
		if (drone0 != null) target0 = DistanceUtil.getNearestEnemy(drone0, TARGET_RANGE);
		if (drone1 != null) target1 = DistanceUtil.getNearestEnemy(drone1, TARGET_RANGE);
		
		if (drone0 != null && target0 != null) {
			spawnEMP(ship, drone0, true);
			spawnEMP(drone0, target0, false);
			spawnEMP(drone0, target0, true);
			IS_READY = false;
		}
		if (drone1 != null && target1 != null) {
			spawnEMP(ship, drone1, true);
			spawnEMP(drone1, target1, false);
			spawnEMP(drone1, target1, true);
			IS_READY = false;
		}
	}

	private List<ShipAPI> getFighters(ShipAPI carrier) {
		List<ShipAPI> result = new ArrayList<ShipAPI>();
		
		//if Temblor (IX)
		if (carrier.getVariant().hasHullMod("ix_terminus_relay_built_in")) {
			for (ShipAPI ship : Global.getCombatEngine().getShips()) {
				if (!ship.isFighter() || ship.getWing() == null) continue;
				if (ship.getWing().getSourceShip() == carrier) result.add(ship);
			}
		}
		//else Odyssey (IX)
		else {
			ShipAPI mothership = carrier.getParentStation();
			if (mothership == null) return result;
			for (ShipAPI ship : Global.getCombatEngine().getShips()) {
				if (!ship.isFighter() || ship.getWing() == null) continue;
				if (ship.getWing().getSourceShip() == mothership) result.add(ship);
			}
		}
		return result;
	}
	
	private void spawnEMP(ShipAPI ship, ShipAPI target, boolean isDecorative) {
		CombatEngineAPI engine = Global.getCombatEngine();
		float damage = isDecorative ? 0f : DAMAGE;
		float empDamage = isDecorative ? 0f : EMP_DAMAGE;
		float range = isDecorative ? RELAY_RANGE : TARGET_RANGE;
		
		engine.spawnEmpArc(ship,
						ship.getLocation(),
						ship,
						target,
			 			DamageType.ENERGY,
						damage, // damage
						empDamage, // emp damage
						range + 200f, // range with buffer to prevent disconnects
						"", // sound
						20f, // thickness
						new Color(50,255,50,200), // fringe
						new Color(200,255,200,180) // core color
						);	
	}
	
	@Override
	public String getInfoText(ShipSystemAPI system, ShipAPI ship) {
		if (ship == null) return null;
		if (system.isCoolingDown()) return null;
		//if (system.getState() != SystemState.IDLE) return null;
		List<ShipAPI> drones = getFighters(ship);
		if (drones.isEmpty()) return "NO DRONE RELAYS";
		boolean isEnemyInRange = false;
		ShipAPI drone0 = null;
		ShipAPI drone1 = null;
		ShipAPI target0 = null;
		ShipAPI target1 = null;
		for (ShipAPI drone : drones) {
			if (drone != null 
					&& !drone.isHulk() 
					&& drone.getHullSpec().hasTag("terminus_drone")
					&& DistanceUtil.getDistance(ship, drone) < RELAY_RANGE) {
				if (drone0 != null) drone0 = drone;
				else drone1 = drone;
			}
		}
		if (drone0 != null) target0 = DistanceUtil.getNearestEnemy(drone0, TARGET_RANGE);
		if (drone1 != null) target1 = DistanceUtil.getNearestEnemy(drone1, TARGET_RANGE);
		if (target0 == null && target1 == null) return "OUT OF RANGE";
		return "READY";
	}
	
	@Override
	public boolean isUsable(ShipSystemAPI system, ShipAPI ship) {
		if (ship == null) return false;
		//if (system.getState() != SystemState.IDLE) return false;
		if (system.isActive() || system.isCoolingDown()) {
			IS_READY = false;
			return false;
		}
		else if (getInfoText(system, ship).equals("READY")) {
			IS_READY = true;
			return true;
		}
		return false;
	}
	
	public StatusData getStatusData(int index, State state, float effectLevel) {
		return null;
	}
}