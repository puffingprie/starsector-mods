package data.scripts.vice.ai;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI.SystemState;
import com.fs.starfarer.api.combat.ShipwideAIFlags;

import data.scripts.vice.DistanceUtil;

public class MicroMissileAI implements ShipSystemAIScript {
    
	private CombatEngineAPI engine;
    private ShipAPI ship;
    private ShipSystemAPI system;
    
	private static float RANGE = 1200f;
	
    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.system = system;
    }
	
    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {  
        if (engine != Global.getCombatEngine()) this.engine = Global.getCombatEngine();
        if (engine.isPaused() || this.ship.getShipAI() == null) return;
		if (this.ship.getSystem().getState() == SystemState.IDLE) {
			ShipAPI enemy = DistanceUtil.getNearestEnemy(this.ship, RANGE, true);
			if (enemy != null) ship.useSystem();
			return;
		}
    }
}