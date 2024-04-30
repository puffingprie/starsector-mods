package data.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.Misc;
import data.utils.FM_Colors;
import data.utils.visual.FM_MaskAndGlow;
import data.utils.visual.FM_ParticleManager;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicLensFlare;
import org.magiclib.util.MagicRender;

import java.awt.*;

public class FM_LoupgarouSystem extends BaseShipSystemScript {

    public static final float RANGE = 1300f;
    public static final float NUMBER_OF_MISSILE = 14f;

    //private boolean missileLaunch = false;

    private float JITTER_TIMER = 0f;

    private float timer = 0f;
    private int missiles = 0;


    private boolean test = false;

    public float getSystemRange(ShipAPI ship) {
        return ship.getMutableStats().getSystemRangeBonus().computeEffective(
                RANGE
        );
    }

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        super.apply(stats, id, state, effectLevel);

        if (!(stats.getEntity() instanceof ShipAPI)) return;
        if (Global.getCombatEngine() == null) return;

        ShipAPI ship = (ShipAPI) stats.getEntity();
        ShipAPI target = findTarget(ship);
        CombatEngineAPI engine = Global.getCombatEngine();

        timer = timer + engine.getElapsedInLastFrame();

        if (target != null && timer >= 0.05f && missiles <= NUMBER_OF_MISSILE) {
            Vector2f targetloc = target.getLocation();
            float radius = target.getCollisionRadius() * 1.6f;
            Vector2f sizeOfImage = new Vector2f(48f, 48f);
            Global.getSoundPlayer().playSound("FM_Nightbugs_expand_1", 1.5f, 0.5f, ship.getLocation(), new Vector2f());

            Vector2f missileloc = MathUtils.getRandomPointOnCircumference(targetloc, MathUtils.getRandomNumberInRange(radius - 50f, radius + 250f));
            MagicRender.battlespace(
                    Global.getSettings().getSprite("fx", "FM_modeffect_6"),
                    missileloc,
                    new Vector2f(),
                    sizeOfImage,
                    new Vector2f(),
                    0f,
                    0f,
                    Color.WHITE,
                    true,
                    0.1f,
                    0.4f,
                    0.2f
            );
            MagicLensFlare.createSharpFlare(
                    engine,
                    ship,
                    missileloc,
                    14f,
                    35f,
                    0,
                    Misc.scaleAlpha(FM_Colors.FM_RED_EMP_FRINGE, 0.6f),
                    FM_Colors.FM_RED_EMP_CORE
            );
            engine.spawnProjectile(
                    ship,
                    null,
                    "FM_WolfsFang",
                    missileloc,
                    VectorUtils.getAngle(missileloc, targetloc),
                    new Vector2f()
            );

            timer = timer - 0.05f;
            missiles = missiles + 1;

//            for (int i = 0; i <= NUMBER_OF_MISSILE; i = i +1){
//                Vector2f missileloc = MathUtils.getRandomPointOnCircumference(targetloc,MathUtils.getRandomNumberInRange(radius - 50f,radius + 250f));
//                MagicRender.battlespace(
//                        Global.getSettings().getSprite("fx", "FM_modeffect_6"),
//                        missileloc,
//                        new Vector2f(),
//                        sizeOfImage,
//                        new Vector2f(),
//                        0f,
//                        0f,
//                        Color.WHITE,
//                        true,
//                        0.1f,
//                        0.4f,
//                        0.2f
//                );
//                MagicLensFlare.createSharpFlare(
//                        engine,
//                        ship,
//                        missileloc,
//                        14f,
//                        35f,
//                        0,
//                        Misc.scaleAlpha(FM_Colors.FM_RED_EMP_FRINGE,0.6f),
//                        FM_Colors.FM_RED_EMP_CORE
//                );
//                engine.spawnProjectile(
//                        ship,
//                        null,
//                        "FM_WolfsFang",
//                        missileloc,
//                        VectorUtils.getAngle(missileloc,targetloc),
//                        new Vector2f()
//                );
//            }
            //missileLaunch = true;
        }

        if (effectLevel > 0) {
            if (state != State.IN) {
                JITTER_TIMER += Global.getCombatEngine().getElapsedInLastFrame();
            }
            float shipJitterLevel;
            if (state == State.IN) {
                shipJitterLevel = effectLevel;
            } else {
                float durOut = 0.4f;
                shipJitterLevel = Math.max(0, durOut - JITTER_TIMER) / durOut;
            }
            float maxRangeBonus = 30f;
            float jitterRangeBonus = shipJitterLevel * maxRangeBonus;
            if (shipJitterLevel > 0) {
                //ship.setJitterUnder(KEY_SHIP, JITTER_UNDER_COLOR, shipJitterLevel, 21, 0f, 3f + jitterRangeBonus);
                ship.setJitter(ship, FM_Colors.FM_RED_EXPLOSION, shipJitterLevel, 4, 4f, 0 + jitterRangeBonus);
            }
        }


//        if (!test){
//            FM_MaskAndGlow manager = FM_ParticleManager.getMaskAndGlowTestManager(Global.getCombatEngine());
//            FM_MaskAndGlow.FM_HullGlowParam param = manager.addMaskAndFlow(
//                    ship.getLocation(),ship,
//                    0.4f,1.6f,0.5f,
//                    Misc.scaleAlpha(Color.WHITE,0.7f),
//                    new Color(255, 53, 53, 215),
//                    200f,
//                    20f,
//                    ship.getFacing(),
//                    1f,
//                    Global.getSettings().getSprite("fx","FM_modeffect_3"),
//                    Global.getSettings().getSprite("fx","FM_maskTest")
//            );
//            manager.addParamToRender(param);
//            manager.setLocation(param,ship.getLocation());
//            test = true;
//        }



    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        super.unapply(stats, id);
        //missileLaunch = false;
        timer = 0f;
        JITTER_TIMER = 0f;
        missiles = 0;

        test = false;

    }

    @Override
    public boolean isUsable(ShipSystemAPI system, ShipAPI ship) {
        //if (true) return true;
        ShipAPI target = findTarget(ship);
        return target != null && target != ship;
    }


    @Override
    public String getInfoText(ShipSystemAPI system, ShipAPI ship) {
        if (system.isOutOfAmmo()) return null;
        if (system.getState() != ShipSystemAPI.SystemState.IDLE) return null;

        ShipAPI target = findTarget(ship);
        if (target != null && target != ship) {
            return "READY";
        }
        if ((target == null) && ship.getShipTarget() != null) {
            return "OUT OF RANGE";
        }
        return "NO TARGET";
    }

    protected ShipAPI findTarget(ShipAPI ship) {

        float range = getSystemRange(ship);

        boolean player = ship == Global.getCombatEngine().getPlayerShip();
        ShipAPI target = ship.getShipTarget();
        if (target != null) {
            float dist = Misc.getDistance(ship.getLocation(), target.getLocation());
            float radSum = ship.getCollisionRadius() + target.getCollisionRadius();
            if (dist > range + radSum) target = null;
        } else {
            if (player) {
                target = Misc.findClosestShipEnemyOf(ship, ship.getMouseTarget(), ShipAPI.HullSize.FRIGATE, range, true);
            } else {
                Object test = ship.getAIFlags().getCustom(ShipwideAIFlags.AIFlags.MANEUVER_TARGET);
                if (test instanceof ShipAPI && ((ShipAPI) test).getOwner() != ship.getOwner()) {
                    target = (ShipAPI) test;
                    float dist = Misc.getDistance(ship.getLocation(), target.getLocation());
                    float radSum = ship.getCollisionRadius() + target.getCollisionRadius();
                    if (dist > range + radSum) target = null;
                }
            }
            if (target == null) {
                target = Misc.findClosestShipEnemyOf(ship, ship.getLocation(), ShipAPI.HullSize.FRIGATE, range, true);
            }
        }
        return target;
    }


}
