package data.shipsystems;


import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import data.utils.FM_Misc;
import data.utils.I18nUtil;
import data.utils.visual.FM_MisfortuneAbsorbVisual;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class FM_misfortuneabsorb extends BaseShipSystemScript {

    public static final float RANGE = 800f;

    public static final float DAMAGE_DECREASE = 30f;
    public static final float DAMAGE_INCREASE = 30f;
    public static final Color SHIP_COLOR = new Color(9, 173, 88, 191);
    public static final Color VISYAL_COLOR = new Color(108, 255, 163, 208);
    public static final Color PARTICLES_COLOR = new Color(162, 255, 197, 237);

    private FM_MisfortuneAbsorbVisual.FM_MAVParams visual = null;
    private CombatEntityAPI entityForVisual = null;
//    private boolean FULL_CHARGE = false;

    private float timerForParticles = 0f;

    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {

        if (!(stats.getEntity() instanceof ShipAPI)) {
            return;
        }
        if (Global.getCombatEngine() == null) return;
        ShipAPI the_ship = (ShipAPI) stats.getEntity();
        String spec_id = id + the_ship.getId();

        stats.getHullDamageTakenMult().modifyMult(id, 1 + DAMAGE_INCREASE / 100 * effectLevel);
        stats.getArmorDamageTakenMult().modifyMult(id, 1 + DAMAGE_INCREASE / 100 * effectLevel);
        stats.getShieldDamageTakenMult().modifyMult(id, 1 + DAMAGE_INCREASE / 100 * effectLevel);

        out:
        for (ShipAPI ship_in_range : AIUtils.getAlliesOnMap(the_ship)) {

            float range = MathUtils.getDistance(the_ship, ship_in_range);
            if (range <= FM_Misc.getSystemRange(the_ship,RANGE)) {
                //是否有对应的buff
                for (String key : ship_in_range.getMutableStats().getHullDamageTakenMult().getMultMods().keySet()) {
                    if (key.startsWith(id) && !key.contentEquals(spec_id)) {
                        continue out;
                    }
                }
                //没有就把buff加上
                ship_in_range.getMutableStats().getHullDamageTakenMult().modifyMult(spec_id, 1 - DAMAGE_DECREASE / 100 * effectLevel);
                ship_in_range.getMutableStats().getArmorDamageTakenMult().modifyMult(spec_id, 1 - DAMAGE_DECREASE / 100 * effectLevel);
                ship_in_range.getMutableStats().getShieldDamageTakenMult().modifyMult(spec_id, 1 - DAMAGE_DECREASE / 100 * effectLevel);

                ship_in_range.setJitterUnder(ship_in_range, SHIP_COLOR, 2f * effectLevel, 25, 4);
            } else {
                ship_in_range.getMutableStats().getHullDamageTakenMult().unmodifyMult(spec_id);
                ship_in_range.getMutableStats().getArmorDamageTakenMult().unmodifyMult(spec_id);
                ship_in_range.getMutableStats().getShieldDamageTakenMult().unmodifyMult(spec_id);
            }
        }

        //作用范围效果
        if (visual == null) {
            visual = new FM_MisfortuneAbsorbVisual.FM_MAVParams();
            visual.additive = true;
            visual.fadeIn = the_ship.getSystem().getSpecAPI().getIn();
            visual.fadeIdle = the_ship.getSystem().getSpecAPI().getActive();
            visual.fadeOut = the_ship.getSystem().getSpecAPI().getOut();
            visual.radius = FM_Misc.getSystemRange(the_ship,RANGE);
            visual.thickness = 30f;
            visual.color = VISYAL_COLOR;

            entityForVisual = Global.getCombatEngine().addLayeredRenderingPlugin(new FM_MisfortuneAbsorbVisual(visual,the_ship));
        }
        if (visual != null && entityForVisual != null) {
            visual.loc.set(the_ship.getLocation());
        }
//        if (effectLevel == 1f && !FULL_CHARGE){
//            WaveDistortion wave = new WaveDistortion(the_ship.getLocation(),the_ship.getVelocity());
//            wave.setArc(0,360);
//            wave.fadeInSize(0.1f);
//            wave.fadeOutIntensity(0.4f);
//            DistortionShader.addDistortion(wave);
//            FULL_CHARGE = true;
//        }
        timerForParticles = timerForParticles - Global.getCombatEngine().getElapsedInLastFrame();
        if (timerForParticles <= 0) {
            for (int i = 0; i < 15; i = i + 1) {
                Global.getCombatEngine().addHitParticle(
                        MathUtils.getRandomPointInCircle(the_ship.getLocation(), FM_Misc.getSystemRange(the_ship,RANGE)),
                        MathUtils.getRandomPointInCircle(new Vector2f(), MathUtils.getRandomNumberInRange(20f, 30f)),
                        MathUtils.getRandomNumberInRange(16f, 24f),
                        100f,
                        0.5f,
                        2.5f,
                        PARTICLES_COLOR);
            }
            timerForParticles = 2.5f;
        }
    }

    public void unapply(MutableShipStatsAPI stats, String id) {

        if (!(stats.getEntity() instanceof ShipAPI)) {
            return;
        }
        if (Global.getCombatEngine() == null) return;

        ShipAPI the_ship = (ShipAPI) stats.getEntity();
        String spec_id = id + the_ship.getId();

        for (ShipAPI ship_in_range : AIUtils.getAlliesOnMap(the_ship)) {
            ship_in_range.getMutableStats().getHullDamageTakenMult().unmodifyMult(spec_id);
            ship_in_range.getMutableStats().getArmorDamageTakenMult().unmodifyMult(spec_id);
            ship_in_range.getMutableStats().getShieldDamageTakenMult().unmodifyMult(spec_id);
        }

        timerForParticles = 0f;

        visual = null;
        entityForVisual = null;
//        FULL_CHARGE = false;

    }

    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData(I18nUtil.getShipSystemString("FM_MisfortuneAbsorbInfo0") + (int) (DAMAGE_DECREASE * effectLevel) + "%", false);
        } else if (index == 1) {
            return new StatusData(I18nUtil.getShipSystemString("FM_MisfortuneAbsorbInfo1") + (int) (DAMAGE_INCREASE * effectLevel) + "%", true);
        }
        return null;
    }
}
