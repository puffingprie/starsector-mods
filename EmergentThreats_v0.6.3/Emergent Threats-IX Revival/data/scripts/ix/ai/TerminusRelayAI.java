package data.scripts.ix.ai;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;

import data.scripts.ix.shipsystems.TerminusRelayStats;

public class TerminusRelayAI implements ShipSystemAIScript {
    
	private CombatEngineAPI engine;
    private ShipAPI ship;
    private ShipSystemAPI system;
	private static float FLUX_THRESHOLD = 0.75f;
	
    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.system = system;
    }
	
    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {  
        if (engine != Global.getCombatEngine()) this.engine = Global.getCombatEngine();
        if (engine.isPaused() || this.ship.getShipAI() == null || this.ship.getFluxLevel() > FLUX_THRESHOLD) return;
		TerminusRelayStats stats = new TerminusRelayStats();
		if (("READY").equals(stats.getInfoText(system, ship))) ship.useSystem();
    }
}