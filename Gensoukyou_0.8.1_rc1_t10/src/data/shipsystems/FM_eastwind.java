package data.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.Misc;
import data.utils.I18nUtil;
import data.utils.visual.FM_MisfortuneAbsorbVisual;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.List;
import java.util.*;

public class FM_eastwind extends BaseShipSystemScript {

    public static final float RANGE = 800f;
    public static Object KEY_SHIP = new Object();
    //五角星的端点
    public Map<Integer, Vector2f> NODES = new HashMap<>();
    //五条边
    public Map<Integer, Vector2f> LINES = new HashMap<>();
    //边上的点的集合
    public Map<Vector2f, List<Vector2f>> POINTS = new HashMap<>();
    //每个弹头的飞行方向
    public Map<Vector2f, Float> DIRECTION = new HashMap<>();


    private boolean EFFECT = false;
    private float TIMER_FOR_PROJECT = 0;
    private float FIR_ANGLE = 0;
    private int BULLET_TIME = 0;

    private final FM_MisfortuneAbsorbVisual visualPlugin = null;
    private final FM_MisfortuneAbsorbVisual.FM_MAVParams param = null;
    private float r;

    public float getSystemRange(ShipAPI ship) {
        return ship.getMutableStats().getSystemRangeBonus().computeEffective(
                RANGE
        );
    }


    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {

        if (!(stats.getEntity() instanceof ShipAPI)) {
            return;
        }

        if (Global.getCombatEngine() == null) return;


        ShipAPI the_ship = (ShipAPI) stats.getEntity();

        the_ship.fadeToColor(KEY_SHIP, new Color(75, 75, 75, 255), 0.1f, 0.1f, effectLevel);
        //ship.fadeToColor(KEY_SHIP, new Color(100,100,100,255), 0.1f, 0.1f, effectLevel);
        the_ship.setWeaponGlow(effectLevel, new Color(100, 165, 255, 255), EnumSet.of(WeaponAPI.WeaponType.BALLISTIC, WeaponAPI.WeaponType.ENERGY, WeaponAPI.WeaponType.MISSILE));
        the_ship.getEngineController().fadeToOtherColor(KEY_SHIP, new Color(0, 0, 0, 0), new Color(0, 0, 0, 0), effectLevel, 0.75f * effectLevel);
        //ship.setJitter(KEY_SHIP, new Color(100,165,255,55), effectLevel, 1, 0f, 5f);
        the_ship.setJitterUnder(KEY_SHIP, new Color(100, 165, 255, 255), effectLevel, 15, 0f, 15f);
        //ship.setShowModuleJitterUnder(true);

        float amount = Global.getCombatEngine().getElapsedInLastFrame();
//        if (visualPlugin == null){
//            param = new FM_MisfortuneAbsorbVisual.FM_MAVParams();
//            param.radius = 0f;
//            param.fadeIn = 0.2f;
//            param.fadeIdle = 0.3f;
//            param.fadeOut = 0.8f;
//            param.thickness = 25f;
//            param.color = FM_Colors.FM_GREEN_EMP_FRINGE;
//            param.additive = true;
//            param.loc = the_ship.getLocation();
//            visualPlugin = new FM_MisfortuneAbsorbVisual(param);
//            Global.getCombatEngine().addLayeredRenderingPlugin(visualPlugin);
//        }else {
//            r = r + amount;
//            if (r >= 1.5f){
//                visualPlugin = null;
//                r = 0;
//            }
//            MagicAnim.arbitrarySmooth(r,0,1);
//            param.loc = the_ship.getLocation();
//            param.radius = RANGE * r;
//        }

        //弹道偏转相关
        List<CombatEntityAPI> entities = CombatUtils.getEntitiesWithinRange(the_ship.getLocation(), getSystemRange(the_ship));
        for (CombatEntityAPI entity : entities) {
            Vector2f entity_vel = entity.getVelocity();
            float vel_length = entity_vel.length();

            Vector2f PS = new Vector2f();
            Vector2f.sub(the_ship.getLocation(), entity.getLocation(), PS);

            float facing_1 = VectorUtils.getFacing(PS);
            float facing_2 = VectorUtils.getFacing(entity_vel);

            if (entity instanceof DamagingProjectileAPI
                    && entity.getOwner() != the_ship.getOwner()
                    && effectLevel > 0f
                    && !(entity instanceof MissileAPI)
                    && Math.min(Math.abs(Misc.normalizeAngle(facing_1 - facing_2)), Math.abs(Misc.normalizeAngle(facing_2 - facing_1))) <= 30f) {

                if (facing_2 < 90f && facing_1 > 270f) {
                    VectorUtils.rotate(entity_vel, 60f * amount);
                    entity.setFacing(facing_2 + 60f * amount);
                } else if (facing_2 > 270f && facing_1 < 90f) {
                    VectorUtils.rotate(entity_vel, -60f * amount);
                    entity.setFacing(facing_2 - 60f * amount);
                } else if (facing_1 > facing_2) {
                    VectorUtils.rotate(entity_vel, -60f * amount);
                    entity.setFacing(facing_2 - 60f * amount);
                } else {
                    VectorUtils.rotate(entity_vel, 60f * amount);
                    entity.setFacing(facing_2 + 60f * amount);
                }
            }
        }

//        List<CombatEntityAPI> enemyProjects = CombatUtils.getEntitiesWithinRange(the_ship.getLocation(),RANGE);
//        for (CombatEntityAPI enemyProject : enemyProjects){
//            if (enemyProject.getOwner() == the_ship.getOwner())continue;
//            if (!(enemyProject instanceof DamagingProjectileAPI))continue;
//            if (enemyProject.getVelocity().length() > 50){
//                VectorUtils.resize(enemyProject.getVelocity(),enemyProject.getVelocity().length() - 800f * Global.getCombatEngine().getElapsedInLastFrame(),enemyProject.getVelocity());
//            }
//        }


        //计时
        TIMER_FOR_PROJECT = TIMER_FOR_PROJECT + Global.getCombatEngine().getElapsedInLastFrame();

        if (TIMER_FOR_PROJECT >= 0.33f && BULLET_TIME <= 5) {
            EFFECT = !EFFECT;
            TIMER_FOR_PROJECT = 0;
            FIR_ANGLE = FIR_ANGLE + 27f;
            BULLET_TIME = BULLET_TIME + 1;
        }
        if (EFFECT) {
            //五角星生成
            for (int i = 0; i < 5; i = i + 1) {
                float r = the_ship.getCollisionRadius();
                float direction = the_ship.getFacing();
                Vector2f node = MathUtils.getPoint(the_ship.getLocation(), r, FIR_ANGLE + 72 * i);
                NODES.put(i, node);
            }
            for (int i = 0; i < 5; i = i + 1) {
                Vector2f line = new Vector2f();
                if (i <= 2) {
                    Vector2f.sub(NODES.get(i + 2), NODES.get(i), line);
                } else {
                    Vector2f.sub(NODES.get(i - 3), NODES.get(i), line);
                }
                LINES.put(i, line);
            }

            for (int i = 0; i < 5; i++) {
                Vector2f line = LINES.get(i);
                List<Vector2f> points = new ArrayList<>();
                for (int k = 1; k < 11; k++) {
                    Vector2f point = new Vector2f();
                    VectorUtils.resize(line, line.length() * k / 11, point);

                    Vector2f point_0 = new Vector2f();
                    Vector2f.add(NODES.get(i), point, point_0);
                    points.add(point_0);

                }
                points.add(NODES.get(i));
                POINTS.put(line, points);
            }

            //生成弹头
            CombatEngineAPI engine = Global.getCombatEngine();
            ShipAPI enemy = Misc.findClosestShipEnemyOf(the_ship, the_ship.getLocation(), ShipAPI.HullSize.FRIGATE, getSystemRange(the_ship), true);

            //if ( enemy != null){
            for (int i = 0; i < 5; i++) {
                for (Vector2f point : POINTS.get(LINES.get(i))) {
                    if (i <= 1) {
                        DIRECTION.put(point, VectorUtils.getAngle(point, NODES.get(i + 3)));
                    } else {
                        DIRECTION.put(point, VectorUtils.getAngle(point, NODES.get(i - 2)));
                    }


                }
            }
            //}else {
            //    for (int i = 0; i < 5 ; i++){
            //        for (Vector2f point : POINTS.get(LINES.get(i))){
            //            DIRECTION.put(point,the_ship.getFacing());
            //        }
            //    }

            //}
            for (int i = 0; i < 5; i++) {
                for (Vector2f point : POINTS.get(LINES.get(i))) {
                    engine.spawnProjectile(the_ship, null, "FM_accball", point, DIRECTION.get(point), null);
                    Global.getSoundPlayer().playSound("FM_Nightbugs_expand_1", 2f, 0.25f, point, new Vector2f());
                }
            }

            EFFECT = !EFFECT;
        }


    }

    public void unapply(MutableShipStatsAPI stats, String id) {

        TIMER_FOR_PROJECT = 0;
        FIR_ANGLE = 0;
        BULLET_TIME = 0;

//        r = 0f;
//        param = null;
//        visualPlugin = null;

    }

    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData(I18nUtil.getShipSystemString("FM_EastWindInfo0"), false);
        } else if (index == 1) {
            return new StatusData(I18nUtil.getShipSystemString("FM_EastWindInfo1"), false);
        }
        return null;
    }


}
