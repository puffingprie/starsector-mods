//当前未使用
package data.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class FM_nightbugsystem extends BaseShipSystemScript {

    private boolean EFFECT = false;
    private float TIMER = 0;
    private float ANGLE = 0;

    public static final float SPEED_DEBUFF = 0.01f;


    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        if (!(stats.getEntity() instanceof ShipAPI)) {
            return;
        }

        stats.getMaxSpeed().modifyMult(id, SPEED_DEBUFF);

        ShipAPI the_ship = (ShipAPI) stats.getEntity();

        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine == null) return;


        //计时
        TIMER = TIMER + 1;

        if (TIMER >= 10) {
            EFFECT = !EFFECT;
            TIMER = 0;
            ANGLE = ANGLE + 5f;
        }

        if (EFFECT) {
            for (float i = 0; i < 360; i = i + 60f) {

                Vector2f point = MathUtils.getPoint(the_ship.getLocation(), the_ship.getCollisionRadius(), i + ANGLE);

                engine.spawnProjectile(the_ship, null, "FM_decball", point, i + ANGLE, null);


            }

            EFFECT = !EFFECT;
        }


    }

    public void unapply(MutableShipStatsAPI stats, String id) {
        TIMER = 0f;
        ANGLE = 0f;

    }

    public StatusData getStatusData(int index, State state, float effectLevel) {
        return null;
    }


}
