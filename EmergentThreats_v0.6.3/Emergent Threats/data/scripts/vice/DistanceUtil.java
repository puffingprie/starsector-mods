package data.scripts.vice;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;

public class DistanceUtil {
 
	private static String ABYSSAL_HULLMOD = "rat_abyssal_grid";
	private static String ENTROPY_ARRESTER_1 = "vice_adaptive_entropy_arrester";
	private static String ENTROPY_ARRESTER_2 = "ix_entropy_arrester";
	
	public static float getDistance(CombatEntityAPI self, CombatEntityAPI target) {
		if (self == null || target == null) return 100000f; //out of range by default
		
		Vector2f selfLoc = self.getLocation();
		Vector2f targetLoc = target.getLocation();
		
		float x1 = selfLoc.getX();
		float y1 = selfLoc.getY();
		float x2 = targetLoc.getX();
		float y2 = targetLoc.getY();
		
		return (float) Math.sqrt((y2 - y1) * (y2 - y1) + (x2 - x1) * (x2 - x1));		
	}
	
	public static ShipAPI getNearestEnemy(CombatEntityAPI ship, float range) {
		List<ShipAPI> enemies = getAllShipsInRange(ship, range, "enemies");
		ShipAPI result = null;
		for (ShipAPI e : enemies) {
			if (!e.isDrone() && !e.isFighter() && !e.isHulk() && !e.isPhased()) {
				if (result == null) result = e;
				else if (getDistance(ship, e) < getDistance(ship, result)) result = e;
			}
		}
        return result;
    }

	public static ShipAPI getNearestEnemy(CombatEntityAPI ship, float range, boolean isTargetingFighters) {
		List<ShipAPI> enemies = getAllShipsInRange(ship, range, "enemies");
		ShipAPI result = null;
		for (ShipAPI e : enemies) {
			if (!e.isHulk() && !e.isPhased()) {
				if (result == null) result = e;
				else if (getDistance(ship, e) < getDistance(ship, result)) result = e;
			}
		}
        return result;
	}

	public static ShipAPI getNearestNotAbyssal(CombatEntityAPI ship, float range, String iff) {
		List<ShipAPI> ships = getAllShipsInRange(ship, range, iff);
		ShipAPI result = null;
		for (ShipAPI s : ships) {
			if (!s.isDrone() && !s.isFighter() && !s.isHulk() && !s.isPhased()) {
				if (s.getVariant().hasHullMod(ABYSSAL_HULLMOD) 
						|| s.getVariant().hasHullMod(ENTROPY_ARRESTER_1) 
						|| s.getVariant().hasHullMod(ENTROPY_ARRESTER_2)) continue;
				if (result == null) result = s;
				else if (getDistance(ship, s) < getDistance(ship, result)) result = s;
			}
		}
        return result;
    }
	
	public static MissileAPI getNearestMissile(CombatEntityAPI ship, float range) {
		List<MissileAPI> missiles = getAllMissilesInRange(ship, range);		
		missiles.remove(ship);
		MissileAPI result = null;
		for (MissileAPI m : missiles) {
			if (getDistance(ship, m) < getDistance(ship, result)) result = m;
		}
        return result;
    }
	
	public static MissileAPI getNearestMissile(CombatEntityAPI ship, float range, boolean isEnemyOnly) {
		List<MissileAPI> missiles = getAllMissilesInRange(ship, range);		
		missiles.remove(ship);
		MissileAPI result = null;
		for (MissileAPI m : missiles) {
			if (m.getSourceAPI().getOwner() != ship.getOwner() 
					&& getDistance(ship, m) < getDistance(ship, result)) result = m;
		}
        return result;
    }
	
	public static CombatEntityAPI getNearestTargetIncludeMissile (CombatEntityAPI ship, float range) {
		CombatEntityAPI enemy = getNearestEnemy(ship, range, true);
		CombatEntityAPI missile = getNearestMissile(ship, range, true);
		CombatEntityAPI result = null;
		if (enemy != null && missile != null) {
			result = getDistance(ship, missile) <= getDistance(ship, enemy)	? missile : enemy;
			return result;
		}
		else if (enemy != null && missile == null) return enemy;
        else if (enemy == null && missile != null) return missile;
		return result;
    }
	
	public static List<MissileAPI> getAllMissilesInRange(CombatEntityAPI e, float range) {
		List<MissileAPI> missileList = new ArrayList<MissileAPI>();
		List<MissileAPI> missiles = Global.getCombatEngine().getMissiles();
		for (MissileAPI m : missiles) {
			if (getDistance(e, m) < range) missileList.add(m);
		}
		missileList.remove(e);
		return missileList;
	}
	
	//includes fighters but not phased ships
	public static List<ShipAPI> getAllShipsInRange(CombatEntityAPI ship, float range, String iff) {
		List<ShipAPI> shipList = new ArrayList<ShipAPI>();
		List<ShipAPI> ships = Global.getCombatEngine().getShips();
		if (iff.equals("enemies")) {
			for (ShipAPI s : ships) {
				if (getDistance(ship, s) < (range + s.getCollisionRadius()) 
						&& (s.getOwner() != ship.getOwner()) 
						&& !s.isHulk() && !s.isShuttlePod() && !s.isPhased()) shipList.add(s);
			}
		}
		else if (iff.equals("friends")) {
			for (ShipAPI s : ships) {
				if (ship == s) continue;
				if (getDistance(ship, s) < (range + s.getCollisionRadius()) 
						&& (s.getOwner() == ship.getOwner()) 
						&& !s.isHulk() && !s.isShuttlePod() && !s.isPhased()) shipList.add(s);
			}
		}
		else {
			for (ShipAPI s : ships) {
				if (ship == s) continue;
				if (getDistance(ship, s) < (range + s.getCollisionRadius()) 
						&& !s.isHulk() && !s.isShuttlePod() && !s.isPhased()) shipList.add(s);
			}
		}
		return shipList;
	}
}