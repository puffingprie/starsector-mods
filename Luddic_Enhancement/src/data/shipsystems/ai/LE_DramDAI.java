package data.shipsystems.ai;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;
//Code Courtesy of the Crusher Drive AI from Tahlan by Nia and maybe Tart
public class LE_DramDAI implements ShipSystemAIScript {
    private ShipAPI ship;
    private CombatEngineAPI engine;
    private ShipwideAIFlags flags;
    private ShipSystemAPI system;
    private IntervalUtil tracker = new IntervalUtil(0.5F, 1.0F);

    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.flags = flags;
        this.engine = engine;
        this.system = system;
    }

    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
        if (!engine.isPaused()) {
            if (!system.isActive()) {
                tracker.advance(amount);
                if (tracker.intervalElapsed()) {
                    if (target == null) {
                        return;
                    }

                    if (ship.getShipTarget() == null) {
                        ship.setShipTarget(target);
                        return;
                    }

                    if (!target.isAlive()) {
                        return;
                    }

                    if (target.isFighter() || target.isDrone()) {
                        return;
                    }

                    if (!MathUtils.isWithinRange(ship, target, 1200.0F) && !AIUtils.canUseSystemThisFrame(ship)) {
                        return;
                    }
                    

                    if (flags.hasFlag(AIFlags.MANEUVER_TARGET) || flags.hasFlag(AIFlags.PURSUING) || flags.hasFlag(AIFlags.HARASS_MOVE_IN)) {
                        ship.useSystem();
                    }
                }
            }

        }
    }
}
