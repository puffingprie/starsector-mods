package data.scripts.ix.ai;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI.SystemState;
import com.fs.starfarer.api.combat.ShipwideAIFlags;

import data.scripts.ix.DistanceUtil;

public class InterdictorPulseAI implements ShipSystemAIScript {
    
	private CombatEngineAPI engine;
    private ShipAPI ship;
    private ShipSystemAPI system;
	private static float RANGE = 2000f;
	private static float FLUX_THRESHOLD = 0.90f;
	
    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.system = system;
    }
	
	private static boolean isInRange(ShipAPI ship, ShipAPI target, float range) {
		if (ship == null || target == null) return false;
		return DistanceUtil.getDistance(ship, target) <= range;
	}
	
    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {  
        if (engine != Global.getCombatEngine()) this.engine = Global.getCombatEngine();
        if (engine.isPaused() || ship.getShipAI() == null || ship.getFluxLevel() > FLUX_THRESHOLD) return;
		if (isInRange(ship, target, RANGE) && ship.getSystem().getState() == SystemState.IDLE) ship.useSystem();
		else if (ship.getShipTarget() == null && ship.getSystem().getState() == SystemState.IDLE) {
			ShipAPI enemy = DistanceUtil.getNearestEnemy(ship, RANGE);
			if (enemy == null) return;
			ship.setShipTarget(enemy);
			ship.useSystem();
			return;
		}
    }
}