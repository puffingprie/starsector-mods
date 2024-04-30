package data.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import data.utils.FM_Colors;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicFakeBeam;
import org.magiclib.util.MagicLensFlare;

import java.awt.*;

public class FM_rumiasystem_old extends BaseShipSystemScript {

    private boolean EFFECT = false;
    private float TIMER = 0;
    private float JITTER_TIMER = 0f;

    public float RANGE = 600f;
    public float DAMAGE = 225f;

    public static final Color CORE = new Color(180, 233, 255, 223);
    public static final Color FRINGE = new Color(117, 248, 236, 210);


    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {

        if (!(stats.getEntity() instanceof ShipAPI)) {
            return;
        }

        ShipAPI the_ship = (ShipAPI) stats.getEntity();

        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine == null) return;

        //计时
        TIMER = TIMER + engine.getElapsedInLastFrame();
        //engine.addFloatingText(the_ship.getLocation(),String.valueOf(Global.getCombatEngine().getElapsedInLastFrame()),10f,Color.WHITE,the_ship,1f,1f);

        if (TIMER >= 0.34f) {
            EFFECT = !EFFECT;
            TIMER = 0;
        }

        if (EFFECT) {

            //在圆周上生成弹头并沿切线方向射出
            for (int i = 0; i < 360; i = i + 10) {
                Vector2f point = MathUtils.getPoint(the_ship.getLocation(), the_ship.getCollisionRadius() + 50f, i);
                //float direction_1 = i + 30;
                //float direction_2 = i - 30;
                float direction_3 = i - 90;
                float direction_4 = i + 90;
                //engine.spawnProjectile(the_ship,null,"FM_IcicleFall",point,direction_1,null);
                //engine.spawnProjectile(the_ship,null,"FM_IcicleFall",point,direction_2,null);
                engine.spawnProjectile(the_ship, null, "FM_IcicleFall", point, direction_3, null);
                engine.spawnProjectile(the_ship, null, "FM_IcicleFall", point, direction_4, null);


                MagicFakeBeam.spawnAdvancedFakeBeam(engine, MathUtils.getRandomPointInCircle(the_ship.getLocation(), the_ship.getCollisionRadius())
                        , RANGE, MathUtils.getRandomNumberInRange(i - 5, i + 5), 30f, 18f
                        , -0.4f, "FM_trail_2", "FM_trail_2", 128f, 20f,
                        20f, 50f, 0.4f, 0.2f, 50f,
                        CORE,
                        FRINGE,
                        DAMAGE, DamageType.ENERGY, 0f, the_ship);

                Global.getSoundPlayer().playSound("FM_Hailstorm_se", 3f, 1f, the_ship.getLocation(), the_ship.getVelocity());
                Global.getSoundPlayer().playLoop("system_damper_omega_loop", the_ship, 2f, 1f, the_ship.getLocation(), the_ship.getVelocity(), 0f, 0.5f);


            }

            for (int i = 0; i < 360; i = i + 20) {
                MagicLensFlare.createSharpFlare(engine, the_ship, MathUtils.getRandomPointOnCircumference(the_ship.getLocation(), the_ship.getCollisionRadius() + 50f
                ), 4f, 300f, 0f, FRINGE, CORE);
            }

//            for (int i = 0; i < 30; i = i + 1){
//                engine.addNegativeNebulaParticle(
//                        the_ship.getLocation(),
//                        MathUtils.getRandomPointInCircle(new Vector2f(),MathUtils.getRandomNumberInRange(80f,120f)),
//                        30f,
//                        1.5f,
//                        -0.3f,
//                        0.1f,
//                        1f,
//                        FM_ProjectEffect.EFFECT_7
//                );
//            }
            EFFECT = !EFFECT;
        }

        if (effectLevel > 0) {
            if (state != State.IN) {
                JITTER_TIMER += Global.getCombatEngine().getElapsedInLastFrame();
            }
            float shipJitterLevel;
            if (state == State.IN) {
                shipJitterLevel = effectLevel;
            } else {
                float durOut = 0.8f;
                shipJitterLevel = Math.max(0, durOut - JITTER_TIMER) / durOut;
            }
            float maxRangeBonus = 80f;
            float jitterRangeBonus = shipJitterLevel * maxRangeBonus;
            if (shipJitterLevel > 0) {
                //ship.setJitterUnder(KEY_SHIP, JITTER_UNDER_COLOR, shipJitterLevel, 21, 0f, 3f + jitterRangeBonus);
                the_ship.setJitter(the_ship, FM_Colors.FM_BLUE_FLARE_CORE, shipJitterLevel, 10, 4f, 0 + jitterRangeBonus);

            }
        }


    }

    public void unapply(MutableShipStatsAPI stats, String id) {

        TIMER = 0;
        JITTER_TIMER = 0f;
    }

    public StatusData getStatusData(int index, State state, float effectLevel) {
        return null;
    }
}
