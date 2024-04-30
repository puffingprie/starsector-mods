package data.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.Misc;
import data.utils.FM_Colors;
import data.utils.I18nUtil;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicLensFlare;
import org.magiclib.util.MagicRender;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FM_reisensystem extends BaseShipSystemScript {

    //public static final float ANGLE = 20f;
    public List<WeaponAPI> WEAPONS = null;

    public Map<ShipAPI, Vector2f> OLD_LOC = new HashMap<>();
    public Map<ShipAPI, Float> OLD_FACING = new HashMap<>();

    public boolean TP = false;

    public float RANGE = 1000f;

    public static final Color TEXT_COLOR = new Color(253, 69, 69, 255);
    public static final Color JITTER_UNDER = new Color(232, 59, 59, 255);
    public static final Color IMAGE = new Color(193, 39, 39, 124);

    private float JITTER_TIMER = 0f;
    private float SYSTEM_TIMER = 0f;

    public static class TargetData {
        public ShipAPI ship;
        public ShipAPI target;

        public TargetData(ShipAPI ship, ShipAPI target) {

            this.ship = ship;
            this.target = target;

        }
    }

    public float getSystemRange(ShipAPI ship) {
        return ship.getMutableStats().getSystemRangeBonus().computeEffective(
                RANGE
        );
    }


    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {

        if (!(stats.getEntity() instanceof ShipAPI)) {
            return;
        }
        if (WEAPONS == null) {
            return;
        }
        if (Global.getCombatEngine() == null) return;
        SYSTEM_TIMER = SYSTEM_TIMER + Global.getCombatEngine().getElapsedInLastFrame();
        //简单的旋转和复原动画

//        for (WeaponAPI weapon : WEAPONS) {
//            if (weapon.getType() == WeaponAPI.WeaponType.DECORATIVE) {
//                String weapon_id = weapon.getId();
//                if (weapon_id.equals("FM_Reisen_part_l") && weapon.getCurrAngle() >= Misc.normalizeAngle(the_ship.getFacing() - ANGLE) && state == State.IN) {
//                    weapon.setCurrAngle(weapon.getCurrAngle() - 0.5f);
//
//                }
//                if (weapon_id.equals("FM_Reisen_part_r") && weapon.getCurrAngle() <= Misc.normalizeAngle(the_ship.getFacing() + ANGLE) && state == State.IN) {
//                    weapon.setCurrAngle(weapon.getCurrAngle() + 0.5f);
//                }
//                if (weapon_id.equals("FM_Reisen_part_l") && weapon.getCurrAngle() < the_ship.getFacing() && state == State.OUT) {
//                    weapon.setCurrAngle(weapon.getCurrAngle() + 0.5f);
//                }
//                if (weapon_id.equals("FM_Reisen_part_r") && weapon.getCurrAngle() > the_ship.getFacing() && state == State.OUT) {
//                    weapon.setCurrAngle(weapon.getCurrAngle() - 0.5f);
//                }
//            }
//        }

        //效果相关
        ShipAPI ship;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }
        CombatEngineAPI engine = Global.getCombatEngine();
        final String targetDataKey = ship.getFleetMemberId() + "_FM_reisensystem_target_data";
        java.lang.Object targetDataObj = Global.getCombatEngine().getCustomData().get(targetDataKey);
        TargetData targetDataObjN = (TargetData) targetDataObj;
        if (state == State.IN && targetDataObjN == null) {
            ShipAPI target = findTarget(ship);
            engine.getCustomData().put(targetDataKey, new TargetData(ship, target));
            if (target != null) {
                if (target.getFluxTracker().showFloaty() ||
                        ship == Global.getCombatEngine().getPlayerShip() ||
                        target == Global.getCombatEngine().getPlayerShip()) {
                    target.getFluxTracker().showOverloadFloatyIfNeeded(I18nUtil.getShipSystemString("FM_ReisenSystem_FloatText"), TEXT_COLOR, 20f, true);
                }
            }
        }
        if (targetDataObjN == null || (targetDataObjN).target == null) {
            return;
        }
        //初始位置和返回
        SpriteAPI sprite = Global.getSettings().getSprite(ship.getHullSpec().getSpriteName());
        if (state == State.IN) {
            OLD_LOC.put(ship, new Vector2f(ship.getLocation()));
            OLD_FACING.put(ship, ship.getFacing());
        }

        if (state == State.OUT) {
            ship.getLocation().set(OLD_LOC.get(ship));
            ship.setFacing(OLD_FACING.get(ship));
            ship.getVelocity().set(new Vector2f());

            Global.getCombatEngine().getCustomData().remove(targetDataKey);
            MagicLensFlare.createSharpFlare(engine, ship, OLD_LOC.get(ship), 7f, 200f, 0f, FM_Colors.FM_RED_EMP_FRINGE, FM_Colors.FM_RED_EMP_CORE);
            for (int i = 0; i < 10; i = i + 1) {
                engine.addNebulaParticle(MathUtils.getRandomPointInCircle(OLD_LOC.get(ship), 100f),
                        MathUtils.getRandomPointInCircle(new Vector2f(), 60f),
                        70f,
                        2f,
                        -2f,
                        0.4f,
                        2f,
                        JITTER_UNDER,
                        true);
            }

        }


        //闪现至背后
        Vector2f target_loc = targetDataObjN.target.getLocation();
        float target_facing = targetDataObjN.target.getFacing();
        float target_radius = targetDataObjN.target.getCollisionRadius();
        if (effectLevel == 1 && targetDataObjN.target != null && !TP) {
            Vector2f new_loc = MathUtils.getPoint(target_loc, target_radius * 1.5f, target_facing + 180f);
            ship.getLocation().set(new_loc);
            ship.setFacing(VectorUtils.getAngle(new_loc, target_loc));
            ship.getVelocity().set(new Vector2f());
            TP = true;

            Vector2f size = new Vector2f(ship.getSpriteAPI().getWidth(), ship.getSpriteAPI().getHeight());

            for (int i = 0; i < 10; i++) {
                MagicRender.battlespace(sprite, OLD_LOC.get(ship), new Vector2f(), size, new Vector2f(), OLD_FACING.get(ship) - 90f, 0, IMAGE, true, 20f, 5f, 10f
                        , 1f, 0.4f, 0f, ship.getSystem().getChargeActiveDur(), 2f, CombatEngineLayers.ABOVE_SHIPS_LAYER);
            }

        }

        //engine.addFloatingText(ship.getLocation(),""+ship.getSystem().getCooldownRemaining(),10f,Color.WHITE,ship,0f,0f);


        if (effectLevel > 0) {
            if (state != State.IN) {
                JITTER_TIMER += Global.getCombatEngine().getElapsedInLastFrame();
            }
            float shipJitterLevel;
            if (state == State.IN) {
                shipJitterLevel = effectLevel;
            } else {
                float durOut = 0.8f;
                shipJitterLevel = Math.max(0.1f, durOut - JITTER_TIMER) / durOut;
            }

            float maxRangeBonus = 30f;
            float jitterRangeBonus = shipJitterLevel * maxRangeBonus;

            if (shipJitterLevel > 0) {
                //ship.setJitterUnder(KEY_SHIP, JITTER_UNDER_COLOR, shipJitterLevel, 21, 0f, 3f + jitterRangeBonus);
                ship.setJitter(ship, JITTER_UNDER, shipJitterLevel, 8, 6f, 0 + jitterRangeBonus);
            }

            if (SYSTEM_TIMER >= ship.getSystem().getChargeUpDur() + ship.getSystem().getChargeActiveDur() - 1f) {
                float t = 1f - (ship.getSystem().getChargeUpDur() + ship.getSystem().getChargeActiveDur() - SYSTEM_TIMER);
                //debug
                //engine.addFloatingText(ship.getLocation(),"" + t,10f,Color.WHITE,ship,1f,1f);
                ship.setJitter(ship, JITTER_UNDER, t, 10, 15f + t * t * 35f);
            }

            //AI行为调整
            if (ship.getAI() != null && TP && SYSTEM_TIMER >= ship.getSystem().getChargeUpDur() + ship.getSystem().getChargeActiveDur() - 5f) {
                if (ship.getAIFlags().hasFlag(ShipwideAIFlags.AIFlags.NEEDS_HELP) ||
                        ship.getAIFlags().hasFlag(ShipwideAIFlags.AIFlags.HAS_INCOMING_DAMAGE)) {
                    ship.useSystem();
                }
            }


        }


    }

    public void unapply(MutableShipStatsAPI stats, String id) {

        if (!(stats.getEntity() instanceof ShipAPI)) {
            return;
        }

        WEAPONS = ((ShipAPI) stats.getEntity()).getAllWeapons();
        ShipAPI ship = (ShipAPI) stats.getEntity();
        OLD_LOC.remove(ship);
        OLD_FACING.remove(ship);
        TP = false;

        JITTER_TIMER = 0f;
        SYSTEM_TIMER = 0f;


    }

    public StatusData getStatusData(int index, State state, float effectLevel) {
        return null;
    }

    //索敌
    protected ShipAPI findTarget(ShipAPI ship) {
        ShipAPI target = ship.getShipTarget();
        float effect_range = getSystemRange(ship);
        boolean player = ship == Global.getCombatEngine().getPlayerShip();
        if (target != null) {
            float distance = Misc.getDistance(target.getLocation(), ship.getLocation());
            //Global.getCombatEngine().addFloatingText(ship.getLocation(), target.toString(),10f,Color.WHITE,ship,0f,0f);
            if (distance > getSystemRange(ship) || target.isStation() || target.isStationModule() || !target.isAlive() || target.isFighter()) {
                target = null;
            }
        } else {
            if (player) {
                target = Misc.findClosestShipEnemyOf(ship, ship.getMouseTarget(), ShipAPI.HullSize.FRIGATE, effect_range, true);
                if (target != null) {
                    float distance = Misc.getDistance(target.getLocation(), ship.getLocation());
                    //Global.getCombatEngine().addFloatingText(ship.getLocation(), target.toString(),10f,Color.WHITE,ship,0f,0f);
                    if (distance > getSystemRange(ship) || target.isStation() || target.isStationModule() || !target.isAlive()) {
                        target = null;
                    }
                }
            } else {
                Object test = ship.getAIFlags().getCustom(ShipwideAIFlags.AIFlags.MANEUVER_TARGET);
                if (test instanceof ShipAPI) {
                    target = (ShipAPI) test;
                    float distance = Misc.getDistance(ship.getLocation(), target.getLocation());
                    if (distance > effect_range || target.isStation() || target.isStationModule() || !target.isAlive())
                        target = null;
                }
                if (target == null) {
                    target = Misc.findClosestShipEnemyOf(ship, ship.getLocation(), ShipAPI.HullSize.FRIGATE, effect_range, true);
                }
            }
        }

        return target;
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
        return target != null && target != ship && !target.isStation() && !target.isStationModule();


    }

}
