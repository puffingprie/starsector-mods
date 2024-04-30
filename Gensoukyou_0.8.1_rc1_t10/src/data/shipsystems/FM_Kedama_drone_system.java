package data.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.plugins.MagicTrailPlugin;

import java.awt.*;

public class FM_Kedama_drone_system extends BaseShipSystemScript {

    private boolean EFFECT = false;
    private float TIMER = 0;
    private float ANGLE = 0;
    private float CONSTANT = 0.15f;
    private final float trail_id = org.magiclib.plugins.MagicTrailPlugin.getUniqueID();

    private static final Color EFFECT_1 = new Color(31, 219, 236, 226);
    private static final Color EFFECT_2 = new Color(202, 231, 246, 226);

    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {

        if (!(stats.getEntity() instanceof ShipAPI)) {
            return;
        }

        ShipAPI ship = (ShipAPI) stats.getEntity();

        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine == null) return;

        //计时
        TIMER = TIMER + engine.getElapsedInLastFrame();


        if (TIMER >= 0.001) {
            EFFECT = !EFFECT;
            TIMER = 0;
            ANGLE = ANGLE + 8f;
        }
        if (EFFECT) {
            float radio = ANGLE * CONSTANT;

            Vector2f set_off = MathUtils.getPoint(ship.getLocation(), radio, ANGLE);

            float facing = ANGLE;

            engine.spawnProjectile(ship, null, "FM_Kedama_system_weapon", set_off, facing, ship.getVelocity());


            for (int i = 0; i < 5; i++) {
                engine.addSmoothParticle(MathUtils.getRandomPointInCircle(set_off, 10f), MathUtils.getRandomPointInCircle(new Vector2f(), 100f), MathUtils.getRandomNumberInRange(5f, 10f),
                        -0.3f, 1f, EFFECT_2);
            }

            //engine.addSmoothParticle(set_off,new Vector2f(),12f,255f,2f,color);

            MagicTrailPlugin.addTrailMemberAdvanced(
                    ship,
                    trail_id,
                    Global.getSettings().getSprite("fx", "base_trail_smooth"),
                    set_off,
                    0,
                    0,
                    ANGLE,
                    0f,
                    0f,
                    60f,
                    180f,
                    EFFECT_1,
                    EFFECT_2,
                    1f,
                    0.2f,
                    0.3f,
                    1f,
                    GL11.GL_BLEND_SRC,
                    GL11.GL_ONE_MINUS_CONSTANT_ALPHA,
                    256f,
                    10,
                    10f,
                    null,
                    null,
                    CombatEngineLayers.BELOW_SHIPS_LAYER,
                    60f
            );

            EFFECT = !EFFECT;
        }


    }

    public void unapply(MutableShipStatsAPI stats, String id) {
        TIMER = 0f;
        ANGLE = 0f;
        CONSTANT = -CONSTANT;
    }

    public StatusData getStatusData(int index, State state, float effectLevel) {
        return null;

    }
}
