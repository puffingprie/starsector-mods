package data.shipsystems.ai;

import com.fs.starfarer.api.combat.*;
import data.utils.FM_Misc;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

public class FM_RumiaSystem_ai implements ShipSystemAIScript {

    public static final float range = 800f;
    public static final float bound = 15f;
    private ShipAPI ship;
    private CombatEngineAPI engine;


    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.engine = engine;
    }

    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {

        if (ship == null) return;

        if (target == null) return;
        //幅能积累
        //ship.blockCommandForOneFrame(ShipCommand.VENT_FLUX);
        if (!ship.getAIFlags().hasFlag(ShipwideAIFlags.AIFlags.BACK_OFF) &&
                !ship.getAIFlags().hasFlag(ShipwideAIFlags.AIFlags.BACK_OFF_MIN_RANGE) &&
                !ship.getAIFlags().hasFlag(ShipwideAIFlags.AIFlags.BACKING_OFF)) {
            if (ship.getHardFluxLevel() <= 0.25f) {
                if (!ship.isPhased()) {
                    ship.giveCommand(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK, ship.getMouseTarget(), 0);
                } else {
                    ship.blockCommandForOneFrame(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK);
                }
            }
            if (ship.getHardFluxLevel() <= 0.5f) {
                if (ship.getAIFlags().hasFlag(ShipwideAIFlags.AIFlags.HARASS_MOVE_IN)
                        || ship.getAIFlags().hasFlag(ShipwideAIFlags.AIFlags.PURSUING)
                        || FM_Misc.getSpellModState(engine, ship).spellPower <= 0.3f) {
                    if (!ship.isPhased()) {
                        ship.giveCommand(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK, ship.getMouseTarget(), 0);
                    } else {
                        ship.blockCommandForOneFrame(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK);
                    }
                }
            }
        }


        if (ship.getSystem().isCoolingDown()) return;

        if (target.isFighter() || target.isDrone()) return;
        if (FM_Misc.getSpellModState(engine, ship).spellPower < 0.1f) return;

        if (!MathUtils.isWithinRange(target, ship, range)) return;

        float angle = VectorUtils.getAngle(ship.getLocation(), target.getLocation());
        if (Math.abs(ship.getFacing() - angle) < bound) {
            ship.useSystem();
        }
        if (ship.getSystem().isActive()) {
            ship.getAIFlags().setFlag(ShipwideAIFlags.AIFlags.MAINTAINING_STRIKE_RANGE);
        }
    }

}
