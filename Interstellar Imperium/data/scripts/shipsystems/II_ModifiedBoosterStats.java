package data.scripts.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.ShipEngineControllerAPI.ShipEngineAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import data.scripts.util.II_Util;
import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

public class II_ModifiedBoosterStats extends BaseShipSystemScript {

    public static final float MAX_TURN_BONUS = 250f;
    public static final float TURN_ACCEL_BONUS = 200f;
    public static final float INSTANT_BOOST_FLAT = 300f;
    public static final float INSTANT_BOOST_MULT = 5f;
    public static final Map<HullSize, Float> SPEED_FALLOFF_PER_SEC = new HashMap<>();
    public static final Map<HullSize, Float> FORWARD_PENALTY = new HashMap<>(); // from base 0.75
    public static final Map<HullSize, Float> REVERSE_PENALTY = new HashMap<>(); // from base 0.75
    public static final Map<HullSize, Float> IN_OVERRIDE = new HashMap<>();
    public static final Map<HullSize, Float> ACTIVE_OVERRIDE = new HashMap<>();
    public static final Map<HullSize, Float> OUT_OVERRIDE = new HashMap<>();
    public static final Map<HullSize, Integer> USES_OVERRIDE = new HashMap<>();
    public static final Map<HullSize, Float> REGEN_OVERRIDE = new HashMap<>();

    private static final Map<HullSize, Float> EXTEND_TIME = new HashMap<>();
    private static final Map<HullSize, Float> MAX_FRAC_OUT = new HashMap<>();
    private static final Map<HullSize, Float> BOOST_MULT = new HashMap<>();

    static {
        SPEED_FALLOFF_PER_SEC.put(HullSize.FRIGATE, 0.525f);
        SPEED_FALLOFF_PER_SEC.put(HullSize.DESTROYER, 0.425f);
        SPEED_FALLOFF_PER_SEC.put(HullSize.CRUISER, 0.35f);
        SPEED_FALLOFF_PER_SEC.put(HullSize.CAPITAL_SHIP, 0.3f); // base boost distance 306, time 1.68 (yamato 55 speed, 25 decel)

        FORWARD_PENALTY.put(HullSize.FRIGATE, 0.25f);
        FORWARD_PENALTY.put(HullSize.DESTROYER, 0.2f);
        FORWARD_PENALTY.put(HullSize.CRUISER, 0.175f);
        FORWARD_PENALTY.put(HullSize.CAPITAL_SHIP, 0.15f); // boost distance 170, time 1.38

        REVERSE_PENALTY.put(HullSize.FRIGATE, 0.4f);
        REVERSE_PENALTY.put(HullSize.DESTROYER, 0.35f);
        REVERSE_PENALTY.put(HullSize.CRUISER, 0.325f);
        REVERSE_PENALTY.put(HullSize.CAPITAL_SHIP, 0.3f); // boost distance 115, time 1.20

        IN_OVERRIDE.put(HullSize.FRIGATE, 0.2f);
        IN_OVERRIDE.put(HullSize.DESTROYER, 0.2f);
        IN_OVERRIDE.put(HullSize.CRUISER, 0.2f);
        IN_OVERRIDE.put(HullSize.CAPITAL_SHIP, 0.2f);

        ACTIVE_OVERRIDE.put(HullSize.FRIGATE, 0.2f);
        ACTIVE_OVERRIDE.put(HullSize.DESTROYER, 0.2f);
        ACTIVE_OVERRIDE.put(HullSize.CRUISER, 0.2f);
        ACTIVE_OVERRIDE.put(HullSize.CAPITAL_SHIP, 0.2f);

        OUT_OVERRIDE.put(HullSize.FRIGATE, 1.25f);
        OUT_OVERRIDE.put(HullSize.DESTROYER, 1.5f);
        OUT_OVERRIDE.put(HullSize.CRUISER, 1.75f);
        OUT_OVERRIDE.put(HullSize.CAPITAL_SHIP, 2f);

        USES_OVERRIDE.put(HullSize.FRIGATE, 4);
        USES_OVERRIDE.put(HullSize.DESTROYER, 3);
        USES_OVERRIDE.put(HullSize.CRUISER, 3);
        USES_OVERRIDE.put(HullSize.CAPITAL_SHIP, 3);

        REGEN_OVERRIDE.put(HullSize.FRIGATE, 0.175f);
        REGEN_OVERRIDE.put(HullSize.DESTROYER, 0.15f);
        REGEN_OVERRIDE.put(HullSize.CRUISER, 0.125f);
        REGEN_OVERRIDE.put(HullSize.CAPITAL_SHIP, 0.1f);

        EXTEND_TIME.put(HullSize.FRIGATE, 0.1f);
        EXTEND_TIME.put(HullSize.DESTROYER, 0.1f);
        EXTEND_TIME.put(HullSize.CRUISER, 0.1f);
        EXTEND_TIME.put(HullSize.CAPITAL_SHIP, 0.1f);

        MAX_FRAC_OUT.put(HullSize.FRIGATE, 0.15f / OUT_OVERRIDE.get(HullSize.FRIGATE));
        MAX_FRAC_OUT.put(HullSize.DESTROYER, 0.15f / OUT_OVERRIDE.get(HullSize.DESTROYER));
        MAX_FRAC_OUT.put(HullSize.CRUISER, 0.15f / OUT_OVERRIDE.get(HullSize.CRUISER));
        MAX_FRAC_OUT.put(HullSize.CAPITAL_SHIP, 0.15f / OUT_OVERRIDE.get(HullSize.CAPITAL_SHIP));

        BOOST_MULT.put(HullSize.FRIGATE, 1f);
        BOOST_MULT.put(HullSize.DESTROYER, 2f);
        BOOST_MULT.put(HullSize.CRUISER, 3f);
        BOOST_MULT.put(HullSize.CAPITAL_SHIP, 4f);
    }

    private static final Vector2f ZERO = new Vector2f();
    private final Object ENGINEKEY = new Object();

    private final Map<Integer, Float> engState = new HashMap<>();

    private boolean started = false;
    private boolean ended = false;
    private final IntervalUtil interval = new IntervalUtil(0.015f, 0.015f);
    private float boostScale = 0.75f;
    private float boostVisualDir = 0f;
    private boolean boostForward = false;

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        ShipAPI ship = (ShipAPI) stats.getEntity();
        if (ship == null) {
            return;
        }

        float shipRadius = II_Util.effectiveRadius(ship);
        float amount = Global.getCombatEngine().getElapsedInLastFrame();
        float objectiveAmount = amount * Global.getCombatEngine().getTimeMult().getModifiedValue();
        if (Global.getCombatEngine().isPaused()) {
            amount = 0f;
            objectiveAmount = 0f;
        }

        Color ENGINE_COLOR = ship.getEngineController().getShipEngines().get(0).getEngineColor();
        Color CONTRAIL_COLOR = ship.getEngineController().getShipEngines().get(0).getContrailColor();
        Color BOOST_COLOR = new Color(
                II_Util.clamp255((ENGINE_COLOR.getRed() + CONTRAIL_COLOR.getRed() + 50) / 2),
                II_Util.clamp255((ENGINE_COLOR.getGreen() + CONTRAIL_COLOR.getGreen() + 50) / 2),
                II_Util.clamp255((ENGINE_COLOR.getBlue() + CONTRAIL_COLOR.getBlue() + 50) / 2),
                II_Util.clamp255((ENGINE_COLOR.getAlpha() + CONTRAIL_COLOR.getAlpha() + 50) / 2));

        ship.getEngineController().extendFlame(ENGINEKEY, 0f, 1f * effectLevel, 3f * effectLevel);

        if (!ended) {
            /* Unweighted direction calculation for visual purposes - 0 degrees is forward */
            Vector2f direction = new Vector2f();
            if (ship.getEngineController().isAccelerating()) {
                direction.y += 1f;
            } else if (ship.getEngineController().isAcceleratingBackwards() || ship.getEngineController().isDecelerating()) {
                direction.y -= 1f;
            }
            if (ship.getEngineController().isStrafingLeft()) {
                direction.x -= 1f;
            } else if (ship.getEngineController().isStrafingRight()) {
                direction.x += 1f;
            }
            if (direction.length() <= 0f) {
                direction.y = 1f;
            }
            boostVisualDir = MathUtils.clampAngle(VectorUtils.getFacing(direction) - 90f);
        }

        if (state == State.IN) {
            if (!started) {
                Global.getSoundPlayer().playSound("ii_impulse_booster_activate", 1f, 1f, ship.getLocation(), ZERO);

                started = true;
            }

            List<ShipEngineAPI> engList = ship.getEngineController().getShipEngines();
            for (int i = 0; i < engList.size(); i++) {
                ShipEngineAPI eng = engList.get(i);
                if (eng.isSystemActivated()) {
                    float targetLevel = getSystemEngineScale(eng, boostVisualDir) * 0.4f;
                    Float currLevel = engState.get(i);
                    if (currLevel == null) {
                        currLevel = 0f;
                    }
                    if (currLevel > targetLevel) {
                        currLevel = Math.max(targetLevel, currLevel - (objectiveAmount / EXTEND_TIME.get(ship.getHullSize())));
                    } else {
                        currLevel = Math.min(targetLevel, currLevel + (objectiveAmount / EXTEND_TIME.get(ship.getHullSize())));
                    }
                    engState.put(i, currLevel);
                    ship.getEngineController().setFlameLevel(eng.getEngineSlot(), currLevel);
                }
            }
        }

        if (state == State.OUT) {
            /* Black magic to counteract the effects of maneuvering penalties/bonuses on the effectiveness of this system */
            float decelMult = Math.max(0.5f, Math.min(2f, stats.getDeceleration().getModifiedValue() / stats.getDeceleration().getBaseValue()));
            float adjFalloffPerSec = SPEED_FALLOFF_PER_SEC.get(ship.getHullSize()) * (float) Math.pow(decelMult, 0.5);
            float maxDecelPenalty = 1f / decelMult;

            stats.getDeceleration().modifyMult(id, II_Util.lerp(1f, maxDecelPenalty, effectLevel));
            stats.getMaxTurnRate().modifyPercent(id, MAX_TURN_BONUS * effectLevel);
            stats.getTurnAcceleration().modifyPercent(id, TURN_ACCEL_BONUS * effectLevel);

            if (boostForward) {
                ship.giveCommand(ShipCommand.ACCELERATE, null, 0);
                ship.blockCommandForOneFrame(ShipCommand.ACCELERATE_BACKWARDS);
                ship.blockCommandForOneFrame(ShipCommand.DECELERATE);
            } else {
                ship.blockCommandForOneFrame(ShipCommand.ACCELERATE);
            }

            /* Quickly unapply the instant repair buff */
            stats.getCombatEngineRepairTimeMult().unmodify(id);

            if (amount > 0f) {
                ship.getVelocity().scale((float) Math.pow(adjFalloffPerSec, amount));
            }

            interval.advance(amount);
            if (interval.intervalElapsed()) {
                float randRange = (float) Math.sqrt(shipRadius) * 0.5f * boostScale;
                Vector2f randLoc = MathUtils.getRandomPointInCircle(ZERO, randRange);
                Color afterimageColor = new Color(CONTRAIL_COLOR.getRed(), CONTRAIL_COLOR.getGreen(), CONTRAIL_COLOR.getBlue(),
                        II_Util.clamp255(Math.round(0.15f * CONTRAIL_COLOR.getAlpha())));
                ship.addAfterimage(afterimageColor, randLoc.x, randLoc.y, -ship.getVelocity().x, -ship.getVelocity().y,
                        randRange, 0f, 0.1f, 0.5f, true, false, false);
            }
        } else if (state == State.ACTIVE) {
            stats.getMaxTurnRate().modifyPercent(id, MAX_TURN_BONUS * effectLevel);
            stats.getTurnAcceleration().modifyPercent(id, TURN_ACCEL_BONUS * effectLevel);

            ship.getEngineController().getExtendLengthFraction().advance(objectiveAmount * 2f);
            ship.getEngineController().getExtendWidthFraction().advance(objectiveAmount * 2f);
            ship.getEngineController().getExtendGlowFraction().advance(objectiveAmount * 2f);
        }

        if (state != State.IN) {
            boolean cwTurn = false;
            boolean ccwTurn = false;
            if (ship.getEngineController().isTurningRight()) {
                cwTurn = true;
            }
            if (ship.getEngineController().isTurningLeft()) {
                ccwTurn = true;
            }

            List<ShipEngineAPI> engList = ship.getEngineController().getShipEngines();
            Map<Integer, Float> engineScaleMap = new HashMap<>();
            for (int i = 0; i < engList.size(); i++) {
                ShipEngineAPI eng = engList.get(i);
                if (eng.isSystemActivated()) {
                    float scale = getSystemEngineScale(eng, boostVisualDir);
                    scale = Math.max(scale, getSystemEngineScaleOut(ship, eng, cwTurn, ccwTurn, null));
                    engineScaleMap.put(i, scale);
                }
            }
            for (int i = 0; i < engList.size(); i++) {
                ShipEngineAPI eng = engList.get(i);
                if (eng.isSystemActivated()) {
                    float targetLevel = getSystemEngineScale(eng, boostVisualDir) * effectLevel;
                    if (state == State.OUT) {
                        if (targetLevel >= (1f - MAX_FRAC_OUT.get(ship.getHullSize()))) {
                            targetLevel = 1f;
                        } else {
                            targetLevel = targetLevel / (1f - MAX_FRAC_OUT.get(ship.getHullSize()));
                        }
                    }
                    targetLevel = Math.max(targetLevel, Math.min(getSystemEngineScaleOut(ship, eng, cwTurn, ccwTurn, engineScaleMap) * effectLevel, (1f - MAX_FRAC_OUT.get(ship.getHullSize()))));
                    Float currLevel = engState.get(i);
                    if (currLevel == null) {
                        currLevel = 0f;
                    }
                    if (currLevel > targetLevel) {
                        currLevel = Math.max(targetLevel, currLevel - (objectiveAmount / EXTEND_TIME.get(ship.getHullSize())));
                    } else {
                        currLevel = Math.min(targetLevel, currLevel + (objectiveAmount / EXTEND_TIME.get(ship.getHullSize())));
                    }
                    if (ship.getEngineController().isFlamedOut()) {
                        currLevel = 0f;
                    }
                    engState.put(i, currLevel);
                    ship.getEngineController().setFlameLevel(eng.getEngineSlot(), currLevel);
                }
            }
        }

        if (state == State.OUT) {
            if (!ended) {
                Vector2f direction = new Vector2f();
                boostForward = false;
                boostScale = 0.75f;
                if (ship.getEngineController().isAccelerating()) {
                    direction.y += 0.75f - FORWARD_PENALTY.get(ship.getHullSize());
                    boostScale -= FORWARD_PENALTY.get(ship.getHullSize());
                    boostForward = true;
                } else if (ship.getEngineController().isAcceleratingBackwards() || ship.getEngineController().isDecelerating()) {
                    direction.y -= 0.75f - REVERSE_PENALTY.get(ship.getHullSize());
                    boostScale -= REVERSE_PENALTY.get(ship.getHullSize());
                }
                if (ship.getEngineController().isStrafingLeft()) {
                    direction.x -= 1f;
                    boostScale += 0.25f;
                    boostForward = false;
                } else if (ship.getEngineController().isStrafingRight()) {
                    direction.x += 1f;
                    boostScale += 0.25f;
                    boostForward = false;
                }
                if (direction.length() <= 0f) {
                    direction.y = 0.75f - FORWARD_PENALTY.get(ship.getHullSize());
                    boostScale -= FORWARD_PENALTY.get(ship.getHullSize());
                }
                Misc.normalise(direction);
                VectorUtils.rotate(direction, ship.getFacing() - 90f, direction);
                direction.scale(((ship.getMaxSpeedWithoutBoost() * INSTANT_BOOST_MULT) + INSTANT_BOOST_FLAT) * boostScale);
                Vector2f.add(ship.getVelocity(), direction, ship.getVelocity());
                ended = true;

                float duration = (float) Math.sqrt(shipRadius) / 25f;
                ship.getEngineController().getExtendLengthFraction().advance(1f);
                ship.getEngineController().getExtendWidthFraction().advance(1f);
                ship.getEngineController().getExtendGlowFraction().advance(1f);
                for (ShipEngineAPI eng : ship.getEngineController().getShipEngines()) {
                    float level = 1f;
                    if (eng.isSystemActivated()) {
                        level = getSystemEngineScale(eng, boostVisualDir);
                    }
                    if ((eng.isActive() || eng.isSystemActivated()) && (level > 0f) && eng.getStyleId().contentEquals("IMPERIUM")) {
                        Color bigBoostColor = new Color(
                                II_Util.clamp255(Math.round(0.1f * ENGINE_COLOR.getRed())),
                                II_Util.clamp255(Math.round(0.1f * ENGINE_COLOR.getGreen())),
                                II_Util.clamp255(Math.round(0.1f * ENGINE_COLOR.getBlue())),
                                II_Util.clamp255(Math.round(0.3f * ENGINE_COLOR.getAlpha() * level)));
                        Color boostColor = new Color(BOOST_COLOR.getRed(), BOOST_COLOR.getGreen(), BOOST_COLOR.getBlue(),
                                II_Util.clamp255(Math.round(BOOST_COLOR.getAlpha() * level)));
                        Global.getCombatEngine().spawnExplosion(eng.getLocation(), ZERO, bigBoostColor,
                                BOOST_MULT.get(ship.getHullSize()) * 4f * boostScale * eng.getEngineSlot().getWidth(), duration);
                        Global.getCombatEngine().spawnExplosion(eng.getLocation(), ZERO, boostColor,
                                BOOST_MULT.get(ship.getHullSize()) * 2f * boostScale * eng.getEngineSlot().getWidth(), 0.15f);
                    }
                }

                float soundScale = (float) Math.sqrt(boostScale);
                switch (ship.getHullSize()) {
                    case FRIGATE:
                        Global.getSoundPlayer().playSound("ii_impulse_booster_boom", 1f, 1f * soundScale, ship.getLocation(), ZERO);
                        break;
                    default:
                    case DESTROYER:
                        Global.getSoundPlayer().playSound("ii_impulse_booster_boom", 0.9f, 1.1f * soundScale, ship.getLocation(), ZERO);
                        break;
                    case CRUISER:
                        Global.getSoundPlayer().playSound("ii_impulse_booster_boom", 0.8f, 1.2f * soundScale, ship.getLocation(), ZERO);
                        break;
                    case CAPITAL_SHIP:
                        Global.getSoundPlayer().playSound("ii_impulse_booster_boom", 0.7f, 1.3f * soundScale, ship.getLocation(), ZERO);
                        break;
                }
            }
        }
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        ShipAPI ship = (ShipAPI) stats.getEntity();
        if (ship == null) {
            return;
        }

        started = false;
        ended = false;
        boostScale = 0.75f;
        boostVisualDir = 0f;
        boostForward = false;
        engState.clear();

        stats.getMaxTurnRate().unmodify(id);
        stats.getDeceleration().unmodify(id);
        stats.getTurnAcceleration().unmodify(id);
    }

    @Override
    public boolean isUsable(ShipSystemAPI system, ShipAPI ship) {
        if (ship != null) {
            if (ship.getEngineController().isFlamedOut()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String getInfoText(ShipSystemAPI system, ShipAPI ship) {
        if (ship != null) {
            if (ship.getEngineController().isFlamedOut()) {
                return "FLAMED OUT";
            }
        }
        return null;
    }

    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        switch (index) {
            case 0:
                if ((state == State.ACTIVE) || (state == State.OUT)) {
                    return new StatusData("improved maneuverability", false);
                }
                break;
            default:
                break;
        }
        return null;
    }

    @Override
    public float getInOverride(ShipAPI ship) {
        if (ship != null) {
            return IN_OVERRIDE.get(ship.getHullSize());
        }
        return -1;
    }

    @Override
    public float getActiveOverride(ShipAPI ship) {
        if (ship != null) {
            return ACTIVE_OVERRIDE.get(ship.getHullSize());
        }
        return -1;
    }

    @Override
    public float getOutOverride(ShipAPI ship) {
        if (ship != null) {
            return OUT_OVERRIDE.get(ship.getHullSize());
        }
        return -1;
    }

    @Override
    public int getUsesOverride(ShipAPI ship) {
        if (ship != null) {
            return USES_OVERRIDE.get(ship.getHullSize());
        }
        return -1;
    }

    @Override
    public float getRegenOverride(ShipAPI ship) {
        if (ship != null) {
            return REGEN_OVERRIDE.get(ship.getHullSize());
        }
        return -1;
    }

    private static float getSystemEngineScale(ShipEngineAPI engine, float direction) {
        float engAngle = engine.getEngineSlot().getAngle();
        if (Math.abs(MathUtils.getShortestRotation(engAngle, direction)) > 100f) {
            return 1f;
        } else {
            return 0f;
        }
    }

    private static float getSystemEngineScaleOut(ShipAPI ship, ShipEngineAPI engine, boolean cwTurn, boolean ccwTurn, Map<Integer, Float> engineScaleMap) {
        float target = 0f;

        Vector2f engineRelLocation = new Vector2f(engine.getLocation());
        Vector2f.sub(engineRelLocation, ship.getLocation(), engineRelLocation); // Example -- (20, 20) ship facing forwards, engine on upper right quadrant
        engineRelLocation.normalise(engineRelLocation); // (0.7071, 0.7071)
        VectorUtils.rotate(engineRelLocation, -ship.getFacing(), engineRelLocation); // (0.7071, -0.7071) - engine past centerline (x) on right side (y)
        Vector2f engineAngleVector = VectorUtils.rotate(new Vector2f(1f, 0f), engine.getEngineSlot().getAngle()); // 270 degrees into (0, -1)
        float torque = VectorUtils.getCrossProduct(engineRelLocation, engineAngleVector); // 0.7071*-1 - -0.7071*0 = -0.7071 (70.71% strength CCW torque)

        if ((torque <= -0.4f) && ccwTurn) {
            target = 1f;
        } else if ((torque >= 0.4f) && cwTurn) {
            target = 1f;
        }

        /* Engines that are firing directly against each other should shut off */
        if (engineScaleMap != null) {
            List<ShipEngineAPI> engineList = ship.getEngineController().getShipEngines();
            for (int i = 0; i < engineList.size(); i++) {
                ShipEngineAPI otherEngine = engineList.get(i);
                if (otherEngine.isSystemActivated() && (engineScaleMap.get(i) >= 0.5f)) {
                    Vector2f otherEngineRelLocation = new Vector2f(otherEngine.getLocation());
                    Vector2f.sub(otherEngineRelLocation, ship.getLocation(), otherEngineRelLocation); // Example -- (20, 20) ship facing forwards, engine on upper right quadrant
                    otherEngineRelLocation.normalise(otherEngineRelLocation); // (0.7071, 0.7071)
                    VectorUtils.rotate(otherEngineRelLocation, -ship.getFacing(), otherEngineRelLocation); // (0.7071, -0.7071) - engine past centerline (x) on right side (y)
                    Vector2f otherEngineAngleVector = VectorUtils.rotate(new Vector2f(1f, 0f), otherEngine.getEngineSlot().getAngle()); // 270 degrees into (0, -1)

                    float otherTorque = VectorUtils.getCrossProduct(otherEngineRelLocation, otherEngineAngleVector); // 0.7071*-1 - -0.7071*0 = -0.7071 (70.71% strength CCW torque)
                    if ((Math.abs(MathUtils.getShortestRotation(engine.getEngineSlot().getAngle(), otherEngine.getEngineSlot().getAngle())) > 155f)
                            && (Math.abs(torque + otherTorque) <= 0.2f)) {
                        target = 0f;
                        break;
                    }
                }
            }
        }

        return target;
    }
}
