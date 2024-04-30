package data.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.loading.WingRole;
import com.fs.starfarer.api.util.Misc;
import data.utils.FM_Colors;
import data.utils.FM_Misc;
import data.utils.I18nUtil;
import org.dark.shaders.distortion.DistortionShader;
import org.dark.shaders.distortion.WaveDistortion;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicLensFlare;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class FM_bordercontrol extends BaseShipSystemScript {

    public static final float RANGE = 3000f;


    public static final Color EFFECT_COLOR = new Color(123, 101, 255, 183);

    public static final Color FLARE_1 = new Color(104, 146, 255, 205);
    public static final Color FLARE_2 = new Color(129, 156, 234, 205);

    private float JITTER_TIMER = 0;
    public static class TargetData {
        public ShipAPI ship;
        public ShipAPI target;

        public TargetData(ShipAPI ship, ShipAPI target) {

            this.ship = ship;
            this.target = target;

        }
    }

    private WaveDistortion distortion = null;
    private float t = 0;

    private List<ShipAPI> fighters(ShipAPI carrier) {
        List<ShipAPI> result = new ArrayList<>();

        for (ShipAPI fighter : FM_Misc.getFighters(carrier)) {
            if (!fighter.isFighter()) continue;
            if (fighter.getWing() == null) continue;
            if (fighter.getWing().getRole() != WingRole.BOMBER) {
                result.add(fighter);
            }
        }
        return result;
    }

    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {

        ShipAPI ship;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }

        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine == null) return;

        final String targetDataKey = ship.getId() + "_FM_bordercontrol_target_data";

        //Global.getCombatEngine().addFloatingText(ship.getLocation(),state.toString(),10f, Color.WHITE,ship,0f,0f);

        java.lang.Object targetDataObj = Global.getCombatEngine().getCustomData().get(targetDataKey);

        if (effectLevel > 0) {
            ShipAPI target = findTarget(ship);
            Global.getCombatEngine().getCustomData().put(targetDataKey, new FM_bordercontrol.TargetData(ship, target));
            if (effectLevel >= 0.5f && effectLevel < 0.6f) {
                for (ShipAPI fighter_0 : fighters(ship)) {
                    fighter_0.setJitter(fighter_0, EFFECT_COLOR, 4, 6, 3);
                    MagicLensFlare.createSharpFlare(engine, fighter_0, fighter_0.getLocation(), 7, 160, 90, FLARE_1, FLARE_2);
                }
            }

            if (target != null && effectLevel == 1f) {
                //折跃战机
                Vector2f target_loc = target.getLocation();
                for (ShipAPI fighter : fighters(ship)) {
                    Vector2f fighter_loc_n = MathUtils.getRandomPointOnCircumference(target_loc, 200f);
                    fighter.getLocation().set(fighter_loc_n);
                    Global.getSoundPlayer().playSound("system_phase_skimmer", 1f, 1.3f, ship.getLocation(), ship.getVelocity());

                }
            }
        } else if (state == State.IDLE && targetDataObj != null) {
            Global.getCombatEngine().getCustomData().remove(targetDataKey);
        }

        //Global.getCombatEngine().addFloatingText(ship.getLocation(), "TRUE",20,Color.WHITE,ship,1,1);
        //视觉和其他
        if (distortion == null && ship.getSystem().getCooldownRemaining() > 0f) {
            distortion = new WaveDistortion(ship.getLocation(), new Vector2f());
            distortion.setSize(0f);
            distortion.setIntensity(75f);
            distortion.flip(false);
            DistortionShader.addDistortion(distortion);

//            for (int i = 0; i < 7 ;i = i + 1){
//                engine.addNebulaParticle(
//                        ship.getLocation(),
//                        MathUtils.getRandomPointOnCircumference(FM_Misc.ZERO,MathUtils.getRandomNumberInRange(70f,100f)),
//                        MathUtils.getRandomNumberInRange(50f,70f),
//                        3f,
//                        0.2f,
//                        0.2f,
//                        4f,
//                        FLARE_1
//                );
//            }
        }
        if (distortion != null) {
            if (distortion.getIntensity() <= 0) {
                DistortionShader.removeDistortion(distortion);
                if (ship.getSystem().getCooldownRemaining() <= 0) {
                    distortion = null;
                    t = 0f;
                }
            } else {
                t = t + engine.getElapsedInLastFrame();
                distortion.advance(engine.getElapsedInLastFrame());
                distortion.setIntensity(75f * (1 - t * t * 2f));
                distortion.setSize(0 + t * 510f);

//                if (t < 0.4f) {
//                    ship.setJitter(ship, FLARE_2, t * 3.5f, 7, 13f * t);
//                    for (ShipAPI fighter : fighters(ship)) {
//                        fighter.setJitter(ship, FLARE_2, t * 3f, 10, 10f * t);
//                    }
//                } else {
//                    ship.setJitter(ship, FLARE_2, 1.5f - 1.25f * t, 7, 10f * (1 - t));
//                    for (ShipAPI fighter : fighters(ship)) {
//                        fighter.setJitter(ship, FLARE_2, t * 3f, 10, 10f * (1 - t));
//                    }
//                }

                //Global.getCombatEngine().addFloatingText(ship.getLocation(), "TRUE",20,Color.WHITE,ship,1,1);
            }
        }

        if (effectLevel > 0) {
            if (state != State.IN) {
                JITTER_TIMER += Global.getCombatEngine().getElapsedInLastFrame();
            }
            float shipJitterLevel;
            if (state == State.IN) {
                shipJitterLevel = effectLevel;
            } else {
                float durOut = 0.75f;
                shipJitterLevel = Math.max(0, durOut - JITTER_TIMER) / durOut;
            }
            float maxRangeBonus = 20f;
            float jitterRangeBonus = shipJitterLevel * maxRangeBonus;
            if (shipJitterLevel > 0) {
                //ship.setJitterUnder(KEY_SHIP, JITTER_UNDER_COLOR, shipJitterLevel, 21, 0f, 3f + jitterRangeBonus);
                ship.setJitter(ship, FM_Colors.FM_BLUE_FLARE_FRINGE, shipJitterLevel, 4, 4f, 0 + jitterRangeBonus);
            }
        }
    }

    public void unapply(MutableShipStatsAPI stats, String id) {

        JITTER_TIMER = 0f;
    }

    //索敌
    protected ShipAPI findTarget(ShipAPI ship) {
        ShipAPI target = ship.getShipTarget();
        float effect_range = FM_Misc.getSystemRange(ship,RANGE);
        boolean player = ship == Global.getCombatEngine().getPlayerShip();
        if (target != null) {
            float distance = Misc.getDistance(target.getLocation(), ship.getLocation());
            if (distance > FM_Misc.getSystemRange(ship,RANGE)) {
                target = null;
            }
        } else {
            if (player) {
                target = Misc.findClosestShipEnemyOf(ship, ship.getMouseTarget(), ShipAPI.HullSize.FRIGATE, effect_range, true);
            } else {
                Object test = ship.getAIFlags().getCustom(ShipwideAIFlags.AIFlags.MANEUVER_TARGET);
                if (test instanceof ShipAPI) {
                    target = (ShipAPI) test;
                    float distance = Misc.getDistance(ship.getLocation(), target.getLocation());
                    if (distance > effect_range) target = null;
                }
                if (target == null) {
                    target = Misc.findClosestShipEnemyOf(ship, ship.getLocation(), ShipAPI.HullSize.FRIGATE, effect_range, true);
                }
            }

        }
        return target;
    }
    //UI相关

    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (effectLevel > 0) {
            if (index == 0) {
                return new StatusData(I18nUtil.getShipSystemString("FM_BorderControlInfo"), false);
            }
        }

        return null;
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


    @Override
    public boolean isUsable(ShipSystemAPI system, ShipAPI ship) {
        //if (true) return true;
        ShipAPI target = findTarget(ship);
        return target != null && target != ship;
    }
}
