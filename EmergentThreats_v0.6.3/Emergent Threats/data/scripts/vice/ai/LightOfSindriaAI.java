package data.scripts.vice.ai;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.FighterLaunchBayAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.util.IntervalUtil;

//attempt to use system when deployed fighter count is less than half of total fighter count, does not use when first spawned
public class LightOfSindriaAI implements ShipSystemAIScript {
    
	private CombatEngineAPI engine;
    private ShipAPI ship;
    private ShipSystemAPI system;
    private IntervalUtil timer = new IntervalUtil (5f, 6f);
	private static String LG_FIGHTER_WING = "vice_gladius_lg_wing";
	
    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.system = system;
    }

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {  
        
        if (engine != Global.getCombatEngine()) {
            this.engine = Global.getCombatEngine();
        }
        if (engine.isPaused() || ship.getShipAI() == null) {
            return;
        }
		
        timer.advance(amount);
		
		if (timer.intervalElapsed()) {
			int fighterCount = 0;
			int squadronCount = 0;
			for (FighterLaunchBayAPI bay : ship.getLaunchBaysCopy()) {
				if (bay.getWing() == null || !bay.getWing().getWingId().equals(LG_FIGHTER_WING)) continue;
				fighterCount += bay.getWing().getWingMembers().size();
				squadronCount++;
			}
			if (squadronCount != 0 && fighterCount <= 1) ship.useSystem();
        }
    }
}