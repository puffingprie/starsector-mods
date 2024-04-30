package data.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import data.utils.FM_Colors;
import data.utils.I18nUtil;
import org.dark.shaders.distortion.DistortionShader;
import org.dark.shaders.distortion.WaveDistortion;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicLensFlare;
import org.magiclib.util.MagicRender;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class FM_endlessgallery extends BaseShipSystemScript {

    //    public static final Color FLARE_CORE = new Color(166, 218, 252, 237);
//    public static final Color FLARE_FRINGE = new Color(64, 131, 243, 235);
    public static final Color TARGET_1 = new Color(80, 204, 226, 255);
    public static final Color TARGET_2 = new Color(98, 158, 227, 196);
    public static final float RANGE = 2000f;
    public static final Object KEY_SHIP = new Object();
    private static final float DISTANCE = 1000f;
    private final HashMap<ShipAPI, Vector2f> LOC_CHANGE = new HashMap<>();
    private final List<ShipAPI> ENEMY_SHIPS = new ArrayList<>();
    data.utils.FM_LocalData.FM_Data currdata = data.utils.FM_LocalData.getCurrData();
    Map<ShipAPI, Wave> currEffect = null;
    private boolean BEGIN = true;
    private float TIMER = 0f;
    private boolean VISUAL = false;
    private float JITTER_TIMER = 0f;

    public float getSystemRange(ShipAPI ship) {
        return ship.getMutableStats().getSystemRangeBonus().computeEffective(
                RANGE
        );
    }

    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {

        if (Global.getCombatEngine() == null) return;
        if (currdata == null)return;
        if (currEffect == null){
            currEffect = currdata.elgeffect;
        }
        if (stats.getEntity() instanceof ShipAPI) {
            ShipAPI the_ship = (ShipAPI) stats.getEntity();
            CombatEngineAPI engine = Global.getCombatEngine();
            //标记周边敌舰并获取传送目标
            if (BEGIN) {
                for (ShipAPI ship : CombatUtils.getShipsWithinRange(the_ship.getLocation(), getSystemRange(the_ship))) {
                    if (!ship.isAlive()) continue;
                    if (ship.getOwner() == the_ship.getOwner()) continue;
                    if (ship.isStation() || ship.isStationModule() || ship.isHulk()) continue;
                    ENEMY_SHIPS.add(ship);
                }
                if (!ENEMY_SHIPS.isEmpty()) {
                    for (ShipAPI ship : ENEMY_SHIPS) {
                        if (ship.getOwner() == the_ship.getOwner() || !ship.isAlive()) {
                            ENEMY_SHIPS.remove(ship);
                            continue;
                        }
                        Vector2f p = ship.getLocation();
                        Vector2f o = the_ship.getLocation();
                        Vector2f dir = new Vector2f();
                        Vector2f.sub(p, o, dir);
                        VectorUtils.resize(dir, DISTANCE, dir);
                        Vector2f p_x = new Vector2f();
                        Vector2f.add(p, dir, p_x);
                        LOC_CHANGE.put(ship, p_x);
                    }
                }
                BEGIN = false;
            }
            //自身视觉效果
            TIMER = TIMER + engine.getElapsedInLastFrame();
            if (TIMER >= 0.04f && !VISUAL) {
                for (int i = 0; i < MathUtils.getRandomNumberInRange(1, 4); i = i + 1) {
                    MagicLensFlare.createSharpFlare(
                            engine,
                            the_ship,
                            MathUtils.getRandomPointOnCircumference(the_ship.getLocation(), the_ship.getShieldRadiusEvenIfNoShield()),
                            MathUtils.getRandomNumberInRange(4f, 7f),
                            MathUtils.getRandomNumberInRange(150, 200),
                            0f,
                            FM_Colors.FM_BLUE_FLARE_FRINGE,
                            FM_Colors.FM_BLUE_FLARE_CORE
                    );
                }
                TIMER = 0f;
                VISUAL = true;
            } else if (TIMER >= 0.32f * (1 - effectLevel)) {
                TIMER = 0f;
                VISUAL = false;
            }
            if (effectLevel > 0) {
                if (state != State.IN) {
                    JITTER_TIMER += Global.getCombatEngine().getElapsedInLastFrame();
                }
                float shipJitterLevel;
                if (state == State.IN) {
                    shipJitterLevel = effectLevel;
                } else {
                    float durOut = 0.55f;
                    shipJitterLevel = Math.max(0, durOut - JITTER_TIMER) / durOut;
                }
                float maxRangeBonus = 20f;
                float jitterRangeBonus = shipJitterLevel * maxRangeBonus;
                if (shipJitterLevel > 0) {
                    //ship.setJitterUnder(KEY_SHIP, JITTER_UNDER_COLOR, shipJitterLevel, 21, 0f, 3f + jitterRangeBonus);
                    the_ship.setJitter(KEY_SHIP, FM_Colors.FM_BLUE_FLARE_FRINGE, shipJitterLevel, 4, 4f, 0 + jitterRangeBonus);
                }
            }
            //对方效果等
            if (ENEMY_SHIPS.isEmpty()) return;
            for (ShipAPI ship_in_range : ENEMY_SHIPS) {
                ship_in_range.setJitter(ship_in_range, FM_Colors.FM_BLUE_FLARE_FRINGE, effectLevel * 0.7f, (int) (5 + effectLevel * 5), 60f + 60f * effectLevel * effectLevel);


                //扭曲相关
                if (!currEffect.containsKey(ship_in_range)) {
                    currEffect.put(ship_in_range, new Wave());
                }
                if (currEffect.get(ship_in_range).wave == null) {
                    currEffect.get(ship_in_range).wave = new WaveDistortion(ship_in_range.getLocation(), (Vector2f) ship_in_range.getVelocity().scale(0.05f));
                    currEffect.get(ship_in_range).wave.setSize(ship_in_range.getCollisionRadius() * effectLevel);
                    currEffect.get(ship_in_range).wave.setIntensity(1f);
                    currEffect.get(ship_in_range).wave.setArc(0, 360);
                    currEffect.get(ship_in_range).wave.flip(false);
                    DistortionShader.addDistortion(currEffect.get(ship_in_range).wave);
                }

                if (currEffect.get(ship_in_range).wave != null) {
                    currEffect.get(ship_in_range).wave.setLocation(ship_in_range.getLocation());
                    currEffect.get(ship_in_range).wave.setSize(MathUtils.getRandomNumberInRange(ship_in_range.getCollisionRadius() * 0.9f * effectLevel,
                            ship_in_range.getCollisionRadius() * 1.1f * effectLevel));
                    currEffect.get(ship_in_range).wave.setIntensity(1f + effectLevel * effectLevel * 100f);
                    currEffect.get(ship_in_range).wave.setLifetime(1f);
                    currEffect.get(ship_in_range).wave.setAutoFadeIntensityTime(0.8f);
                    currEffect.get(ship_in_range).wave.setAutoFadeSizeTime(0.8f);
                }
                if (state == State.ACTIVE || the_ship.getFluxTracker().isOverloaded()) {
                    Vector2f size = new Vector2f(ship_in_range.getSpriteAPI().getWidth(), ship_in_range.getSpriteAPI().getHeight());
                    Global.getSoundPlayer().playSound("system_phase_skimmer", 1f, 1.5f, the_ship.getLocation(), the_ship.getVelocity());
                    MagicRender.battlespace(Global.getSettings().getSprite(ship_in_range.getHullSpec().getSpriteName()),
                            currEffect.get(ship_in_range).wave.getLocation(), currEffect.get(ship_in_range).wave.getVelocity(), size,
                            new Vector2f(), ship_in_range.getFacing() - 90f, 0,
                            TARGET_1, true, 60f * (1 - effectLevel), 1f,
                            2f, 1f, 0.05f, 0f, 0.3f, 0.4f,
                            CombatEngineLayers.BELOW_SHIPS_LAYER
                    );
                    for (int i = 0; i < 360; i = i + 10) {
                        engine.addNebulaParticle(MathUtils.getPointOnCircumference(currEffect.get(ship_in_range).wave.getLocation(), ship_in_range.getCollisionRadius() * 0.5f
                                        , i), new Vector2f(), ship_in_range.getCollisionRadius() * 0.2f, 2f, -0.5f, 0f, 1f,
                                TARGET_2, true);
                    }
                    currEffect.get(ship_in_range).wave.fadeOutSize(0.8f);
                    currEffect.get(ship_in_range).wave.fadeOutIntensity(0.8f);
                    currEffect.get(ship_in_range).wave = null;

                    //折跃本身.aya
                    if (!ENEMY_SHIPS.contains(ship_in_range)) continue;
                    if (!LOC_CHANGE.containsKey(ship_in_range)) continue;
                    ship_in_range.getLocation().set(LOC_CHANGE.get(ship_in_range));

                }

            }
        }

    }


    public void unapply(MutableShipStatsAPI stats, String id) {
        VISUAL = false;
        TIMER = 0f;
        JITTER_TIMER = 0f;


        ENEMY_SHIPS.clear();
        LOC_CHANGE.clear();
        if (currEffect != null){
            currEffect.clear();
        }
        BEGIN = true;
    }

    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData(I18nUtil.getShipSystemString("FM_EndLessGallery"), false);
        }
        return null;
    }

    public final static class Wave {
        WaveDistortion wave;

        private Wave() {
            wave = null;
        }
    }

}
