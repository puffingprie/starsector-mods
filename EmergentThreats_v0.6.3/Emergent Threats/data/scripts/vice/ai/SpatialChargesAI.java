package data.scripts.vice.ai;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI.SystemState;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.Misc;

import data.scripts.vice.DistanceUtil;

public class SpatialChargesAI implements ShipSystemAIScript {
    
	private CombatEngineAPI engine;
	private ShipAPI ship;
	private WeaponAPI weapon;
	private static float RANGE = 1500f;
	private static String AIM_GUIDE = "vice_hellfire_targeting";
	
	@Override
	public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
		this.engine = engine;
		this.ship = ship;
		for (WeaponAPI w : ship.getAllWeapons()) {
			if (w.getId().equals(AIM_GUIDE)) weapon = w;
		}
	}
	
	@Override
	public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
		if (engine != Global.getCombatEngine()) engine = Global.getCombatEngine();
		if (engine.isPaused() || ship.getShipAI() == null) return;
		if (ship.getSystem().getState() == SystemState.IDLE) {
			CombatEntityAPI enemy = DistanceUtil.getNearestEnemy(ship, RANGE);
			if (enemy != null && weapon != null) {
				if (weapon.distanceFromArc(enemy.getLocation()) <= 0) ship.useSystem();
			}
			return;
		}
	}
}