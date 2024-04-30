package data.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.Misc;
import data.utils.FM_Colors;
import data.utils.I18nUtil;
import data.utils.visual.FM_ParticleManager;
import data.utils.visual.FM_StarParticle;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class FM_BlazingStar extends BaseShipSystemScript {

    public static final float VEL_BUFF = 350f;
    public static final Color AFTERIMAGE = new Color(60, 212, 250, 101);
    public static final float TURN_BUFF = 2f;

    private boolean APPLY_FORCE = false;

    private int EFFECT_NUMBER = 60;

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        if (!(stats.getEntity() instanceof ShipAPI)) {
            return;
        }
        if (Global.getCombatEngine() == null) return;
        ShipAPI ship = (ShipAPI) stats.getEntity();

//        FM_StarParticle visual = (FM_StarParticle) Global.getCombatEngine().getCustomData().get(FM_StarParticle.FM_StarParticleString);
//        if (visual == null || !Global.getCombatEngine().getCustomData().containsKey(FM_StarParticle.FM_StarParticleString)){
//            visual = new FM_StarParticle();
//            Global.getCombatEngine().getCustomData().put(FM_StarParticle.FM_StarParticleString,visual);
//            Global.getCombatEngine().addLayeredRenderingPlugin(visual);
//        }

        stats.getTurnAcceleration().modifyMult(id, TURN_BUFF);
        stats.getMaxTurnRate().modifyMult(id, TURN_BUFF);

        if (!APPLY_FORCE) {
            Vector2f velOrigin = ship.getVelocity();
            Vector2f velEx = MathUtils.getPoint(new Vector2f(), VEL_BUFF, VectorUtils.getFacing(velOrigin));
            Vector2f.add(velEx, velOrigin, ship.getVelocity());
            APPLY_FORCE = true;
        }


        if (EFFECT_NUMBER > 0) {
            Vector2f particleloc = MathUtils.getRandomPointInCone(ship.getLocation(), 70f, ship.getFacing() - 150f, ship.getFacing() - 210f);
            Vector2f particlevel = new Vector2f(-ship.getVelocity().x * 0.25f, -ship.getVelocity().y * 0.25f);
            //视觉使用之前的检测
            FM_StarParticle visual = FM_ParticleManager.getStarParticleManager(Global.getCombatEngine());
            visual.addStarParticle(
                    particleloc,
                    particlevel,
                    MathUtils.getRandomNumberInRange(6f, 8f),
                    0.1f,
                    1.5f,
                    Misc.scaleAlpha(Color.WHITE, 0.7f),
                    6f,
                    0f,
                    MathUtils.getRandomNumberInRange(360f, 720f),
                    Math.random() < 0.5f
            );

            Global.getCombatEngine().addNebulaParticle(
                    particleloc,
                    particlevel,
                    100f,
                    1.3f,
                    -0.2f,
                    0.8f,
                    2f,
                    Misc.scaleAlpha(FM_Colors.FM_BLUE_FLARE_FRINGE, 0.3f),
                    true
            );

//            LightAPI light = new StandardLight(particleloc,particlevel,new Vector2f(),visualEntity,1f,30f);
//            light.getColor().set(255, 255, 255);
//            LightShader.addLight(light);
//            Global.getCombatEngine().addHitParticle(
//                    MathUtils.getRandomPointInCone(ship.getLocation(),70f,ship.getFacing() - 150f, ship.getFacing() - 210f),
//                    new Vector2f(-ship.getVelocity().x * 0.25f,-ship.getVelocity().y * 0.25f),
//                    12f,
//                    255f,0.2f,
//                    1f,
//                    Misc.scaleAlpha(FM_Colors.FM_BLUE_FLARE_CORE,0.8f)
//            );
            EFFECT_NUMBER = EFFECT_NUMBER - 1;
        }

        ship.addAfterimage(
                AFTERIMAGE,
                0,
                0,
                -ship.getVelocity().x * 0.25f,
                -ship.getVelocity().y * 0.25f,
                10f * effectLevel,
                0f,
                0.1f,
                0.4f,
                true,
                true,
                true
        );

    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        APPLY_FORCE = false;
        EFFECT_NUMBER = 30;
        stats.getTurnAcceleration().unmodifyMult(id);
        stats.getMaxTurnRate().unmodifyMult(id);
    }

    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData(I18nUtil.getShipSystemString("FM_BlazingStarInfo"), false);
        }
        return null;
    }
}
