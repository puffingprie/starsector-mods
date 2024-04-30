package data.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.Misc;
import data.utils.FM_Misc;
import data.utils.I18nUtil;
import org.dark.shaders.distortion.DistortionShader;
import org.dark.shaders.distortion.WaveDistortion;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicAnim;
import org.magiclib.util.MagicLensFlare;

import java.awt.*;
import java.util.List;

public class FM_GazeOfSpectre extends BaseShipSystemScript {

    public static final float RANGE = 1000;
    public static final Color TEXT_COLOR = new Color(222, 155, 187, 255);

    public static final float SPEED_ACC_AND_DEC_MULT = 0.66f;

    public static final Object SHIP_INFO = new Object();
    public static final Object TARGET_INFO = new Object();

    public static final Color JITTER_TARGET = new Color(203, 73, 114, 205);
    public static final Color JITTER_SHIP = new Color(224, 43, 105, 255);
    public static final Color EMP_CORE = new Color(227, 50, 78, 232);
    public static final Color EMP_FRINE = new Color(167, 13, 66, 233);
    public static final float INTENSITY = 72f;

    private float timer = 0;
    private boolean visualEffect = false;
    private float empTimer = 0;
    private WaveDistortion ripple = null;

    public boolean EFFECT_ON = false;


    @Override
    public void apply(MutableShipStatsAPI stats, final String id, State state, final float effectLevel) {
        ShipAPI ship;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }
        final String targetDataKey = ship.getFleetMemberId() + "_GazeOfSpectre_target_data";
        if (Global.getCombatEngine() == null) return;

        final CombatEngineAPI engine = Global.getCombatEngine();

        EFFECT_ON = true;
        Object targetDataObj = Global.getCombatEngine().getCustomData().get(targetDataKey);

        if (state == State.IN && targetDataObj == null) {
            ShipAPI target = findTarget(ship);
            Global.getCombatEngine().getCustomData().put(targetDataKey, new TargetData(ship, target));
            if (target != null) {
                if (target.getFluxTracker().showFloaty() ||
                        ship == Global.getCombatEngine().getPlayerShip() ||
                        target == Global.getCombatEngine().getPlayerShip()) {
                    target.getFluxTracker().showOverloadFloatyIfNeeded(I18nUtil.getShipSystemString("FM_GazeOfSpectre_FloatText"), TEXT_COLOR, 4f, true);
                }
            }
        } else if (state == State.OUT && targetDataObj != null) {
            Global.getCombatEngine().getCustomData().remove(targetDataKey);
            //debug
            //engine.addFloatingText(ship.getLocation(),"TEST",20f,Color.WHITE,ship,1f,1f);

            targetDataObj = null;
        }
        if (targetDataObj == null || ((TargetData) targetDataObj).target == null) return;
        final TargetData targetData = (TargetData) targetDataObj;

        targetData.effectOfTarget = 1 - SPEED_ACC_AND_DEC_MULT * effectLevel;

        if (targetData.targetEffectPlugin == null) {
            targetData.targetEffectPlugin = new BaseEveryFrameCombatPlugin() {
                @Override
                public void advance(float amount, List<InputEventAPI> events) {
                    if (Global.getCombatEngine().isPaused()) return;
                    if (targetData.target == Global.getCombatEngine().getPlayerShip()) {
                        Global.getCombatEngine().maintainStatusForPlayerShip(TARGET_INFO,
                                targetData.ship.getSystem().getSpecAPI().getIconSpriteName(),
                                targetData.ship.getSystem().getDisplayName(),
                                (int) (SPEED_ACC_AND_DEC_MULT * 100f) + I18nUtil.getShipSystemString("FM_GazeOfSpectre_Target"), true);

                    }

                    if (targetData.ship.isAlive() && EFFECT_ON) {
                        MutableShipStatsAPI statsOfTarget = targetData.target.getMutableStats();

                        statsOfTarget.getMaxSpeed().modifyMult(id, targetData.effectOfTarget);
                        statsOfTarget.getAcceleration().modifyMult(id, targetData.effectOfTarget);
                        statsOfTarget.getDeceleration().modifyMult(id, targetData.effectOfTarget);
                        statsOfTarget.getMaxTurnRate().modifyMult(id, targetData.effectOfTarget);
                        statsOfTarget.getTurnAcceleration().modifyMult(id, targetData.effectOfTarget);


                        //engine.addFloatingText(targetData.target.getLocation(),"" + effectLevel,10f,Color.WHITE,targetData.target,0f,0f);
                    } else {
                        MutableShipStatsAPI statsOfTarget = targetData.target.getMutableStats();
                        statsOfTarget.getMaxSpeed().unmodifyMult(id);
                        statsOfTarget.getAcceleration().unmodifyMult(id);
                        statsOfTarget.getDeceleration().unmodifyMult(id);
                        statsOfTarget.getMaxTurnRate().unmodifyMult(id);
                        statsOfTarget.getTurnAcceleration().unmodifyMult(id);

                        Global.getCombatEngine().removePlugin(targetData.targetEffectPlugin);
                    }
                }
            };
            Global.getCombatEngine().addPlugin(targetData.targetEffectPlugin);
        }

        if (effectLevel > 0 && !visualEffect) {
            if (timer <= 2f) {
                timer = timer + Global.getCombatEngine().getElapsedInLastFrame();
            }
            if (ripple == null) {
                ripple = new WaveDistortion();
                ripple.setArc(0, 360f);
                ripple.setSize(ship.getCollisionRadius() * 0.8f);
                ripple.setIntensity(INTENSITY);

                ripple.flip(false);

                ripple.setLocation(ship.getLocation());

                MagicLensFlare.createSharpFlare(engine, ship, ship.getLocation(), 10f, 400f, 0f, EMP_CORE, TEXT_COLOR);
            } else {
                DistortionShader.addDistortion(ripple);
                ripple.setAutoFadeIntensityTime(0.25f);
                ripple.setVelocity(new Vector2f());
                ripple.setSize(ship.getCollisionRadius() * 0.8f + timer * timer * 60f);
                ripple.setIntensity(INTENSITY - timer * INTENSITY);
            }

        }

        if ((timer >= 2f) && ripple != null) {
            ripple.fadeOutIntensity(0.3f);
            ripple = null;
            visualEffect = true;
        }
        if (visualEffect && timer > 0) {
            timer = timer - Global.getCombatEngine().getElapsedInLastFrame() * 1.25f;
        }

        if (effectLevel > 0) {
            if (effectLevel < 0.25f) {
                targetData.target.setJitterUnder(TARGET_INFO, JITTER_TARGET, effectLevel * 4f, 20, 10f + 4f * effectLevel * 20);
            } else {
                float t = Math.max(0.1f, 1f - effectLevel * 1.33f);
                targetData.target.setJitterUnder(TARGET_INFO, JITTER_TARGET, t + 1f, 20, 10f + t * 10);
            }
            targetData.target.setCircularJitter(true);
            float effectJitter = MagicAnim.normalizeRange(timer, 0, 2f);

            if (timer > 0) {
                ship.setJitterUnder(SHIP_INFO, JITTER_SHIP, effectJitter, 18, 8f);
            }
            Vector2f empArcPointA =
                    FM_Misc.BezierCurvePoint(
                            effectJitter,
                            MathUtils.getRandomPointInCircle(ship.getLocation(), ship.getCollisionRadius() * 0.8f),
                            MathUtils.getRandomPointInCircle(targetData.target.getLocation(), targetData.target.getCollisionRadius() * 0.8f),
                            MathUtils.getRandomPointInCircle(MathUtils.getMidpoint(ship.getLocation(), targetData.target.getLocation()), 15f)
                    );
            Vector2f empArcPointB =
                    FM_Misc.BezierCurvePoint(
                            1 - effectJitter,
                            MathUtils.getRandomPointInCircle(ship.getLocation(), ship.getCollisionRadius() * 0.8f),
                            MathUtils.getRandomPointInCircle(targetData.target.getLocation(), targetData.target.getCollisionRadius() * 0.8f),
                            MathUtils.getRandomPointInCircle(MathUtils.getMidpoint(ship.getLocation(), targetData.target.getLocation()), 15f)
                    );

            empTimer = empTimer - engine.getElapsedInLastFrame();
            if (empTimer <= 0) {
                engine.spawnEmpArcVisual(
                        MathUtils.getRandomPointInCircle(ship.getLocation(), ship.getCollisionRadius() * 0.8f),
                        ship,
                        empArcPointA,
                        targetData.target,
                        20f,
                        EMP_FRINE,
                        EMP_CORE
                );
                engine.spawnEmpArcVisual(
                        MathUtils.getRandomPointInCircle(targetData.target.getLocation(), targetData.target.getCollisionRadius() * 0.8f),
                        targetData.target,
                        empArcPointB,
                        ship,
                        20f,
                        EMP_FRINE,
                        EMP_CORE
                );
                empTimer = 0.3f;
            }
        }


        //debug
        //Global.getCombatEngine().addFloatingText(ship.getLocation(),String.valueOf(timer),10f,Color.WHITE,ship,1f,1f);
        //Global.getCombatEngine().addFloatingText(ship.getLocation(),String.valueOf(targetDataObj),10f,Color.WHITE,ship,1f,1f);
        //Global.getCombatEngine().addFloatingText(ship.getLocation(),String.valueOf(targetData.target),10f,Color.WHITE,ship,1f,1f);
        //Global.getCombatEngine().addFloatingText(ship.getLocation(),String.valueOf(state),10f,Color.WHITE,ship,1f,1f);


    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        timer = 0f;
        ripple = null;
        visualEffect = false;
        empTimer = 0f;
        EFFECT_ON = false;
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
    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (effectLevel > 0) {
            if (index == 0) {
                return new StatusData((int) (100f * (SPEED_ACC_AND_DEC_MULT * effectLevel)) + I18nUtil.getShipSystemString("FM_GazeOfSpectre_Player"), false);
            }
        }
        return null;
    }

    @Override
    public boolean isUsable(ShipSystemAPI system, ShipAPI ship) {
        //if (true) return true;
        ShipAPI target = findTarget(ship);
        return target != null && target != ship;
    }

    protected ShipAPI findTarget(ShipAPI ship) {
        Misc.FindShipFilter filter = new Misc.FindShipFilter() {
            public boolean matches(ShipAPI ship) {
                return !ship.getEngineController().isFlamedOut();
            }
        };

        float range = getSystemRange(ship);
        boolean player = ship == Global.getCombatEngine().getPlayerShip();
        ShipAPI target = ship.getShipTarget();
        if (target != null) {
            float dist = Misc.getDistance(ship.getLocation(), target.getLocation());
            float radSum = ship.getCollisionRadius() + target.getCollisionRadius();
            if (dist > range + radSum) target = null;
        } else {
            if (target == null || target.getOwner() == ship.getOwner()) {
                if (player) {
                    target = Misc.findClosestShipEnemyOf(ship, ship.getMouseTarget(), ShipAPI.HullSize.FRIGATE, range, true, filter);
                } else {
                    Object test = ship.getAIFlags().getCustom(ShipwideAIFlags.AIFlags.MANEUVER_TARGET);
                    if (test instanceof ShipAPI) {
                        target = (ShipAPI) test;
                        float dist = Misc.getDistance(ship.getLocation(), target.getLocation());
                        float radSum = ship.getCollisionRadius() + target.getCollisionRadius();
                        if (dist > range + radSum) target = null;
                    }
                }
            }
            if (target == null) {
                target = Misc.findClosestShipEnemyOf(ship, ship.getLocation(), ShipAPI.HullSize.FIGHTER, range, true, filter);
            }
        }

        return target;
    }


    public float getSystemRange(ShipAPI ship) {
        return ship.getMutableStats().getSystemRangeBonus().computeEffective(
                RANGE
        );
    }

    public static class TargetData {
        public ShipAPI ship;
        public ShipAPI target;
        public EveryFrameCombatPlugin targetEffectPlugin;
        public float effectOfTarget;

        public TargetData(ShipAPI ship, ShipAPI target) {
            this.ship = ship;
            this.target = target;
        }
    }
}
