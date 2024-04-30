package data.scripts.ix.ai;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.FighterLaunchBayAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.util.IntervalUtil;

public class RapidAssemblyAI implements ShipSystemAIScript {
    
	private CombatEngineAPI engine;
    private ShipAPI ship;
    private ShipSystemAPI system;
	private static float FLUX_THRESHOLD = 0.5f;
    private IntervalUtil timer = new IntervalUtil (5,5);

    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.system = system;
    }

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {		
        if (engine != Global.getCombatEngine()) this.engine = Global.getCombatEngine();
        if (engine.isPaused() || this.ship.getShipAI() == null || this.ship.getFluxLevel() > FLUX_THRESHOLD) return;
		
        timer.advance(amount);
		
		if (timer.intervalElapsed()) {
			int currentFighters = 0;
			int totalFighters = 0;
			for (FighterLaunchBayAPI bay : ship.getLaunchBaysCopy()) {
				if (bay.getWing() == null) continue;
				currentFighters += bay.getWing().getWingMembers().size();
				totalFighters += bay.getWing().getSpec().getNumFighters();
			}
			if (totalFighters != 0 && (currentFighters * 2 <= totalFighters)) ship.useSystem();
        }
    }
}
