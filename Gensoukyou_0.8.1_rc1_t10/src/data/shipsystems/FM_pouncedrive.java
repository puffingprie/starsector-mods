package data.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipEngineControllerAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import data.utils.I18nUtil;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.List;

public class FM_pouncedrive extends BaseShipSystemScript {

    public static final float SPEED_BONUS = 150f;
    public static final float ACC_BONUS = 100f;


    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {


        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine == null) return;


        if (stats.getEntity() instanceof ShipAPI) {

            ShipAPI the_ship = (ShipAPI) stats.getEntity();

            List<ShipEngineControllerAPI.ShipEngineAPI> ship_engines = the_ship.getEngineController().getShipEngines();
            for (ShipEngineControllerAPI.ShipEngineAPI ship_engine : ship_engines) {
                Vector2f engine_loc = ship_engine.getLocation();
                float engine_direction = ship_engine.getEngineSlot().getAngle();

                Vector2f particle_vel = MathUtils.getRandomPointInCone(null, ship_engine.getEngineSlot().getLength(),
                        engine_direction - 10f, engine_direction + 10f);
                Color engine_color = ship_engine.getEngineColor();


                engine.addHitParticle(engine_loc, particle_vel, 3f, 100f, 1, engine_color);
            }

            if (state == State.OUT) {
                stats.getMaxSpeed().unmodify(id);
            } else {
                stats.getMaxSpeed().modifyFlat(id, SPEED_BONUS);
                stats.getAcceleration().modifyPercent(id, ACC_BONUS);
                stats.getDeceleration().modifyPercent(id, ACC_BONUS);
                stats.getMaxTurnRate().modifyPercent(id, ACC_BONUS);
                stats.getTurnAcceleration().modifyPercent(id, ACC_BONUS);
            }


        }
    }

    public void unapply(MutableShipStatsAPI stats, String id) {

        stats.getMaxSpeed().unmodify(id);
        stats.getAcceleration().unmodify(id);
        stats.getDeceleration().unmodify(id);
        stats.getMaxTurnRate().unmodify(id);
        stats.getTurnAcceleration().unmodify(id);

    }

    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData(I18nUtil.getShipSystemString("FM_PounceDriveInfo"), false);
        }
        return null;
    }
}
