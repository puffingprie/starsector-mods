package data.scripts.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipEngineControllerAPI.ShipEngineAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.hullmods.II_BasePackage;
import data.scripts.util.II_Util;
import data.scripts.weapons.II_LightspearBeamEffect;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lazywizard.lazylib.combat.entities.AnchoredEntity;
import org.lwjgl.util.vector.Vector2f;

public class II_LuxFinisStats extends BaseShipSystemScript {

    public static final float PITCH_SHIFT = 0f;
    public static final float GAUGE_DRAIN_TIME = 16f;
    public static final float GAUGE_REGEN_TIME = 28f;
    public static final float COOLDOWN_MIN = 0.25f;
    public static final float COOLDOWN_MAX = 1f;
    public static final float CAPACITY_MULT = 1.25f;
    public static final float DISSIPATION_MULT = 1.25f;
    public static final float SPEED_BONUS = 25f;
    public static final Map<HullSize, Float> SPEED_FLAT_BONUS = new HashMap<>();
    public static final float ACCEL_BONUS = 50f;
    public static final float ACCEL_FLAT_BONUS = 30f;
    public static final float DECEL_BONUS = 50f;
    public static final float DECEL_FLAT_BONUS = 30f;
    public static final float TURN_ACCEL_BONUS = 200f;
    public static final float MAX_TURN_BONUS = 50f;
    public static final float DAMAGE_REDUCTION_MULT = 1f / 3f;
    public static final Map<HullSize, Float> MAX_TURN_FLAT_BONUS = new HashMap<>();
    public static final float BLAST_AREA_RADIUS_SCALE = 2f;
    public static final float BLAST_AREA_FLAT = 100f;
    public static final float BLAST_DAMAGE = 300f;
    public static final float BLAST_MAX_DAMAGE = BLAST_DAMAGE * 3f;
    public static final float BLAST_EMP = 300f;
    public static final int BLAST_MAX_FIGHTER_TARGETS = 3;
    public static final int BLAST_MAX_MISSILE_TARGETS = 3;
    public static final float BLAST_INTERVAL = 0.5f;
    public static final float CR_LOSS_MULT = 3f;
    public static final float ACTIVATE_THRESHOLD = 1f / 3f;
    public static final float ARMOR_DAMAGE_REDUCTION_MULT = 0.1f;
    public static final float TARGETING_GAUGE_DRAIN_TIME = GAUGE_DRAIN_TIME * (4f / 3f);
    public static final float TARGETING_PITCH_SHIFT = -0.1f;
    public static final float TARGETING_CAPACITY_MULT = 1.5f;
    public static final float TARGETING_DISSIPATION_MULT = 1.5f;
    public static final float TARGETING_ROF_BONUS = 50f;
    public static final float ELITE_OVERLOAD_DUR = GAUGE_DRAIN_TIME;
    public static final float ELITE_OVER_GAUGE_LEVEL = 0.5f;
    public static final float ELITE_MAX_OVERLEVEL = 2f;
    public static final float ELITE_OVERLEVEL_CR_LOSS_MULT = 2f;

    public static final float LIGHTSPEAR_FIRE_TIME = 5f;

    static {
        SPEED_FLAT_BONUS.put(HullSize.FRIGATE, 50f);
        SPEED_FLAT_BONUS.put(HullSize.DESTROYER, 40f);
        SPEED_FLAT_BONUS.put(HullSize.CRUISER, 35f);
        SPEED_FLAT_BONUS.put(HullSize.CAPITAL_SHIP, 30f);

        MAX_TURN_FLAT_BONUS.put(HullSize.FRIGATE, 50f);
        MAX_TURN_FLAT_BONUS.put(HullSize.DESTROYER, 40f);
        MAX_TURN_FLAT_BONUS.put(HullSize.CRUISER, 30f);
        MAX_TURN_FLAT_BONUS.put(HullSize.CAPITAL_SHIP, 20f);
    }

    private static final float TRAVERSE_SPEED = 270f;
    private static final float TICK_TIME = 0.015f;
    private static final Map<HullSize, Float> EXTEND_TIME = new HashMap<>();
    private static final Map<HullSize, Float> BASE_SPARK_CHANCE_PER_TICK = new HashMap<>();
    private static final Map<HullSize, Integer> SPARKS_ON_OVERLOAD = new HashMap<>();

    static {
        EXTEND_TIME.put(HullSize.FRIGATE, 0.1f);
        EXTEND_TIME.put(HullSize.DESTROYER, 0.125f);
        EXTEND_TIME.put(HullSize.CRUISER, 0.15f);
        EXTEND_TIME.put(HullSize.CAPITAL_SHIP, 0.175f);

        BASE_SPARK_CHANCE_PER_TICK.put(HullSize.FRIGATE, TICK_TIME * 3f);
        BASE_SPARK_CHANCE_PER_TICK.put(HullSize.DESTROYER, TICK_TIME * 3.5f);
        BASE_SPARK_CHANCE_PER_TICK.put(HullSize.CRUISER, TICK_TIME * 4f);
        BASE_SPARK_CHANCE_PER_TICK.put(HullSize.CAPITAL_SHIP, TICK_TIME * 4.5f);

        SPARKS_ON_OVERLOAD.put(HullSize.FRIGATE, 4);
        SPARKS_ON_OVERLOAD.put(HullSize.DESTROYER, 6);
        SPARKS_ON_OVERLOAD.put(HullSize.CRUISER, 8);
        SPARKS_ON_OVERLOAD.put(HullSize.CAPITAL_SHIP, 10);
    }

    public static final Color ENGINE_COLOR_STANDARD = new Color(255, 125, 75, 210);
    public static final Color CONTRAIL_COLOR_STANDARD = new Color(80, 65, 40, 100);
    public static final Color JITTER_COLOR_STANDARD = new Color(205, 100, 50);
    public static final Color GLOW_COLOR_STANDARD = new Color(255, 150, 75);
    public static final Color FRINGE_COLOR_STANDARD = new Color(255, 125, 50);
    public static final Color ENGINE_COLOR_ARMOR = new Color(205, 150, 100, 200);
    public static final Color CONTRAIL_COLOR_ARMOR = new Color(80, 75, 40, 90);
    public static final Color JITTER_COLOR_ARMOR = new Color(205, 150, 50);
    public static final Color GLOW_COLOR_ARMOR = new Color(255, 225, 100);
    public static final Color FRINGE_COLOR_ARMOR = new Color(255, 200, 50);
    public static final Color ENGINE_COLOR_TARGETING = new Color(50, 220, 255, 220);
    public static final Color CONTRAIL_COLOR_TARGETING = new Color(40, 70, 80, 100);
    public static final Color JITTER_COLOR_TARGETING = new Color(10, 110, 205);
    public static final Color GLOW_COLOR_TARGETING = new Color(100, 200, 255);
    public static final Color FRINGE_COLOR_TARGETING = new Color(50, 200, 255);
    public static final Color ENGINE_COLOR_ELITE = new Color(175, 50, 255, 220);
    public static final Color CONTRAIL_COLOR_ELITE = new Color(70, 40, 80, 100);
    public static final Color JITTER_COLOR_ELITE = new Color(140, 10, 205);
    public static final Color GLOW_COLOR_ELITE = new Color(200, 100, 255);
    public static final Color FRINGE_COLOR_ELITE = new Color(175, 50, 255);

    private static final Vector2f ZERO = new Vector2f();
    private static final String DATA_KEY_ID = "II_LuxFinisStats";
    private final Object STATUSKEY1 = new Object();
    private final Object STATEKEY = new Object();
    private final Object ENGINEKEY1 = new Object();
    private final Object ENGINEKEY2 = new Object();

    private final Map<Integer, Float> engState = new HashMap<>();

    private boolean activated = false;
    private boolean deactivated = false;
    private boolean shutdown = false;
    private boolean tempArmor = false;
    private boolean tempTargeting = false;
    private boolean tempElite = false;
    private float totalPeakTimeLoss = 0f;
    private float tempGauge = 0f;
    private boolean tempLightspearFiring = false;
    private boolean tempLightspearOutTransition = false;
    private float tempLightspearChargeLevel = 0f;
    private boolean unbugify = false;
    private final IntervalUtil interval = new IntervalUtil(TICK_TIME, TICK_TIME);
    private final IntervalUtil interval2 = new IntervalUtil(BLAST_INTERVAL, BLAST_INTERVAL);

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        final ShipAPI ship = (ShipAPI) stats.getEntity();
        if (ship == null) {
            return;
        }

        Object data = Global.getCombatEngine().getCustomData().get(DATA_KEY_ID + "_" + ship.getId());
        LuxFinisData lfData = null;
        if (data instanceof LuxFinisData) {
            lfData = (LuxFinisData) data;
        }
        if ((lfData == null) || (STATEKEY != lfData.stateKey)) {
            lfData = new LuxFinisData(STATEKEY);
            Global.getCombatEngine().getCustomData().put(DATA_KEY_ID + "_" + ship.getId(), lfData);
            lfData.gauge = 1f;
            totalPeakTimeLoss = 0f;
        }

        float shipRadius = II_Util.effectiveRadius(ship);
        float amount = Global.getCombatEngine().getElapsedInLastFrame();
        float objectiveAmount = amount * Global.getCombatEngine().getTimeMult().getModifiedValue();
        if (Global.getCombatEngine().isPaused()) {
            amount = 0f;
            objectiveAmount = 0f;
        }

        ArrayList<WeaponAPI> blasters = new ArrayList<>(2);
        ArrayList<WeaponAPI> photonBeams = new ArrayList<>(3);
        WeaponAPI lightspear = null;
        for (WeaponAPI shipWeapon : ship.getAllWeapons()) {
            if (shipWeapon.getId().startsWith("ii_photonblaster") && !shipWeapon.isDisabled() && !shipWeapon.isPermanentlyDisabled()) {
                blasters.add(shipWeapon);
            } else if (shipWeapon.getId().startsWith("ii_photonbeam") && !shipWeapon.isDisabled() && !shipWeapon.isPermanentlyDisabled()) {
                photonBeams.add(shipWeapon);
            } else if (shipWeapon.getId().startsWith("ii_lightspear")) {
                lightspear = shipWeapon;
            }
        }
        if ((Global.getCombatEngine().getPlayerShip() == ship)
                && ((lightspear == null) || lightspear.getId().contentEquals("ii_lightspear"))) {
            if (blasters.size() < 2) {
                float loss;
                if (ship.getVariant().hasHullMod(II_BasePackage.ARMOR_PACKAGE)) {
                    loss = (2 - blasters.size()) * II_LightspearBeamEffect.LIGHTSPEAR_ATTEN_PER_DISABLED_PB_ARMOR;
                } else {
                    loss = (2 - blasters.size()) * II_LightspearBeamEffect.LIGHTSPEAR_ATTEN_PER_DISABLED_PB;
                }
                Global.getCombatEngine().maintainStatusForPlayerShip(STATUSKEY1, "graphics/icons/tactical/broken.png",
                        "Lightspear", "Lightspear output -" + (int) Math.round(loss * 100f) + "%", true);
            }
        }

        Color ENGINE_COLOR = ENGINE_COLOR_STANDARD;
        Color CONTRAIL_COLOR = CONTRAIL_COLOR_STANDARD;
        Color JITTER_COLOR = JITTER_COLOR_STANDARD;
        Color GLOW_COLOR = GLOW_COLOR_STANDARD;
        Color FRINGE_COLOR = FRINGE_COLOR_STANDARD;
        float gaugeDrainTime = GAUGE_DRAIN_TIME;
        float gaugeRegenTime = GAUGE_REGEN_TIME;
        float overGaugeLevel = 0f;
        float maxOverlevel = 1f;
        float afterimageIntensity = 1f;
        float jitterIntensity = 0.5f;
        float jitterOverIntensity = 0f;
        float sparkIntensity = 0f;
        float pitchShift = 1f;
        float volumeShift = 1f;
        tempArmor = false;
        tempTargeting = false;
        tempElite = false;
        tempLightspearFiring = false;
        tempLightspearOutTransition = false;
        if (ship.getVariant().hasHullMod(II_BasePackage.ARMOR_PACKAGE)) {
            ENGINE_COLOR = ENGINE_COLOR_ARMOR;
            CONTRAIL_COLOR = CONTRAIL_COLOR_ARMOR;
            JITTER_COLOR = JITTER_COLOR_ARMOR;
            GLOW_COLOR = GLOW_COLOR_ARMOR;
            FRINGE_COLOR = FRINGE_COLOR_ARMOR;
            tempArmor = true;
        } else if (ship.getVariant().hasHullMod(II_BasePackage.TARGETING_PACKAGE)) {
            ENGINE_COLOR = ENGINE_COLOR_TARGETING;
            CONTRAIL_COLOR = CONTRAIL_COLOR_TARGETING;
            JITTER_COLOR = JITTER_COLOR_TARGETING;
            GLOW_COLOR = GLOW_COLOR_TARGETING;
            FRINGE_COLOR = FRINGE_COLOR_TARGETING;
            sparkIntensity = 2f;
            pitchShift = 0.9f;
            volumeShift = 1.1f;
            gaugeDrainTime = TARGETING_GAUGE_DRAIN_TIME;
            tempTargeting = true;
        } else if (ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
            ENGINE_COLOR = ENGINE_COLOR_ELITE;
            CONTRAIL_COLOR = CONTRAIL_COLOR_ELITE;
            JITTER_COLOR = JITTER_COLOR_ELITE;
            GLOW_COLOR = GLOW_COLOR_ELITE;
            FRINGE_COLOR = FRINGE_COLOR_ELITE;
            overGaugeLevel = ELITE_OVER_GAUGE_LEVEL;
            maxOverlevel = ELITE_MAX_OVERLEVEL;
            tempElite = true;
        }

        gaugeDrainTime = stats.getSystemUsesBonus().computeEffective(gaugeDrainTime);
        gaugeRegenTime = 1f / stats.getSystemRegenBonus().computeEffective(1f / gaugeRegenTime);

        float effectLevelSquared = effectLevel * effectLevel;
        float effectOverlevel = 1f;
        if ((lfData.gauge < overGaugeLevel) && ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
            effectOverlevel = 1f / II_Util.lerp(1f / maxOverlevel, 1f, Math.max(0f, lfData.gauge) / overGaugeLevel);
        }
        float effectOverlevelSquared = effectOverlevel * effectOverlevel;
        if (effectOverlevel > 1f) {
            sparkIntensity += effectOverlevel - 0.5f;
        }

        if (!ship.getFluxTracker().isOverloaded()) {
            shutdown = false;
        }

        ship.getSystem().setCooldown(Math.max(0f, II_Util.lerp(COOLDOWN_MAX * (float) Math.sqrt(effectOverlevel), COOLDOWN_MIN, lfData.gauge)));

        if (state == State.OUT) {
            for (WeaponAPI blaster : blasters) {
                float currAngle = blaster.getCurrAngle();
                float desiredAngle = ship.getFacing();

                desiredAngle = MathUtils.clampAngle(desiredAngle);
                float angularDistance = MathUtils.getShortestRotation(currAngle, desiredAngle);
                float horizonDistance = MathUtils.getShortestRotation(currAngle, ship.getFacing());
                float newAngle;
                if (blaster.getId().contentEquals("ii_photonblaster_left")) {
                    if (horizonDistance > 0) {
                        if (angularDistance < 0) {
                            newAngle = currAngle + Math.max(angularDistance, -TRAVERSE_SPEED * objectiveAmount);
                        } else {
                            newAngle = currAngle + Math.min(angularDistance, TRAVERSE_SPEED * objectiveAmount);
                        }
                        blaster.setCurrAngle(newAngle);
                    }
                } else {
                    if (horizonDistance < 0) {
                        if (angularDistance < 0) {
                            newAngle = currAngle + Math.max(angularDistance, -TRAVERSE_SPEED * objectiveAmount);
                        } else {
                            newAngle = currAngle + Math.min(angularDistance, TRAVERSE_SPEED * objectiveAmount);
                        }
                        blaster.setCurrAngle(newAngle);
                    }
                }
            }
            for (WeaponAPI photonBeam : photonBeams) {
                photonBeam.setAmmo(photonBeam.getMaxAmmo());

                float currAngle = photonBeam.getCurrAngle();
                float desiredAngle;
                switch (photonBeam.getSlot().getId()) {
                    case "WS0002":
                        desiredAngle = ship.getFacing() + 50f;
                        break;
                    default:
                    case "WS0001":
                        desiredAngle = ship.getFacing();
                        break;
                    case "WS0003":
                        desiredAngle = ship.getFacing() - 50f;
                        break;
                }

                desiredAngle = MathUtils.clampAngle(desiredAngle);
                float angularDistance = MathUtils.getShortestRotation(currAngle, desiredAngle);
                float newAngle;
                if (angularDistance < 0) {
                    newAngle = currAngle + Math.max(angularDistance, -TRAVERSE_SPEED * objectiveAmount);
                } else {
                    newAngle = currAngle + Math.min(angularDistance, TRAVERSE_SPEED * objectiveAmount);
                }
                photonBeam.setCurrAngle(newAngle);
            }
        }

        tempLightspearChargeLevel = 0f;
        if ((lightspear != null) && lightspear.isFiring() && (lightspear.getCooldownRemaining() <= 0f)) {
            tempLightspearFiring = true;
            tempLightspearChargeLevel = lightspear.getChargeLevel();
        }
        if (lightspear != null) {
            if (!lightspear.getBeams().isEmpty()) {
                BeamAPI beam = lightspear.getBeams().get(0);
                if (beam.getBrightness() > 0f) {
                    tempLightspearOutTransition = true;
                    tempLightspearChargeLevel = beam.getBrightness();
                }
            }
        }

        if (((state == State.IN) || (state == State.ACTIVE)) && tempLightspearFiring) {
            ship.getAIFlags().setFlag(AIFlags.DO_NOT_VENT);
            ship.getAIFlags().removeFlag(AIFlags.OK_TO_CANCEL_SYSTEM_USE_TO_VENT);
        }

        if (state == State.IN) {
            deactivated = false;
            if (!activated) {
                Global.getSoundPlayer().playSound("ii_luxfinis_activate", 1f * effectOverlevel * pitchShift, 1f * effectOverlevel * volumeShift, ship.getLocation(), ZERO);
                activated = true;
            }
        } else {
            activated = false;
        }

        if (state == State.OUT) {
            if (!deactivated) {
                Global.getSoundPlayer().playSound("ii_luxfinis_deactivate", 1f * effectOverlevel * pitchShift, 1f * effectOverlevel * volumeShift, ship.getLocation(), ZERO);
                deactivated = true;
            }
        }

        if ((state == State.IN) || (state == State.ACTIVE) || tempLightspearFiring) {
            for (WeaponAPI blaster : blasters) {
                blaster.setRemainingCooldownTo(blaster.getCooldown());

                float currAngle = blaster.getCurrAngle();
                float desiredAngle;
                if (blaster.getId().contentEquals("ii_photonblaster_left")) {
                    desiredAngle = ship.getFacing() - 90f;
                } else {
                    desiredAngle = ship.getFacing() + 90f;
                }

                desiredAngle = MathUtils.clampAngle(desiredAngle);
                float angularDistance = MathUtils.getShortestRotation(currAngle, desiredAngle);
                float horizonDistance = MathUtils.getShortestRotation(currAngle, ship.getFacing());
                float newAngle;
                if (blaster.getId().contentEquals("ii_photonblaster_left")) {
                    if (horizonDistance < 0) {
                        newAngle = currAngle - Math.min(Math.abs(angularDistance), TRAVERSE_SPEED * objectiveAmount);
                    } else {
                        if (angularDistance < 0) {
                            newAngle = currAngle + Math.max(angularDistance, -TRAVERSE_SPEED * objectiveAmount);
                        } else {
                            newAngle = currAngle + Math.min(angularDistance, TRAVERSE_SPEED * objectiveAmount);
                        }
                    }
                } else {
                    if (horizonDistance > 0) {
                        newAngle = currAngle + Math.min(Math.abs(angularDistance), TRAVERSE_SPEED * objectiveAmount);
                    } else {
                        if (angularDistance < 0) {
                            newAngle = currAngle + Math.max(angularDistance, -TRAVERSE_SPEED * objectiveAmount);
                        } else {
                            newAngle = currAngle + Math.min(angularDistance, TRAVERSE_SPEED * objectiveAmount);
                        }
                    }
                }
                blaster.setCurrAngle(newAngle);
            }
            for (WeaponAPI photonBeam : photonBeams) {
                photonBeam.setAmmo(0);
                photonBeam.setRemainingCooldownTo(photonBeam.getCooldown());

                float currAngle = photonBeam.getCurrAngle();
                float desiredAngle;
                switch (photonBeam.getSlot().getId()) {
                    case "WS0002":
                        desiredAngle = ship.getFacing() + 50f;
                        break;
                    default:
                    case "WS0001":
                        desiredAngle = ship.getFacing();
                        break;
                    case "WS0003":
                        desiredAngle = ship.getFacing() - 50f;
                        break;
                }

                desiredAngle = MathUtils.clampAngle(desiredAngle);
                float angularDistance = MathUtils.getShortestRotation(currAngle, desiredAngle);
                float newAngle;
                if (angularDistance < 0) {
                    newAngle = currAngle + Math.max(angularDistance, -TRAVERSE_SPEED * objectiveAmount);
                } else {
                    newAngle = currAngle + Math.min(angularDistance, TRAVERSE_SPEED * objectiveAmount);
                }
                photonBeam.setCurrAngle(newAngle);
            }
        }

        if ((state == State.COOLDOWN) || (state == State.IDLE) || (lfData.gauge < 0f) || shutdown) {
            boolean systemOff = true;
            if ((lfData.gauge < 0f) && ((state == State.IN) || (state == State.ACTIVE) || (state == State.OUT))
                    && !ship.getFluxTracker().isOverloaded()) {
                if (!ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
                    systemOff = false;

                    if (state != State.OUT) {
                        ship.getSystem().deactivate();
                        ship.getSystem().setCooldownRemaining(ship.getSystem().getCooldownRemaining());

                        if (ship.getVariant().hasHullMod(II_BasePackage.TARGETING_PACKAGE)) {
                            ship.setWeaponGlow(0f, ENGINE_COLOR, EnumSet.of(WeaponAPI.WeaponType.BALLISTIC, WeaponAPI.WeaponType.MISSILE, WeaponAPI.WeaponType.ENERGY));
                        }
                    }

                    List<ShipEngineAPI> engList = ship.getEngineController().getShipEngines();
                    for (int i = 0; i < engList.size(); i++) {
                        ShipEngineAPI eng = engList.get(i);
                        if (eng.isSystemActivated()) {
                            Float currLevel = engState.get(i);
                            if (currLevel == null) {
                                currLevel = 0f;
                            }
                            if (currLevel > 0f) {
                                currLevel = Math.max(0f, currLevel - (objectiveAmount / EXTEND_TIME.get(ship.getHullSize())));
                            }
                            if (ship.getEngineController().isFlamedOut()) {
                                currLevel = 0f;
                            }
                            if (ship.getVariant().hasHullMod(II_BasePackage.TARGETING_PACKAGE)) {
                                currLevel = 0f;
                            }
                            engState.put(i, currLevel);
                            ship.getEngineController().setFlameLevel(eng.getEngineSlot(), currLevel);
                        }
                    }
                } else {
                    shutdown = true;

                    ship.setOverloadColor(JITTER_COLOR);
                    ship.getFluxTracker().beginOverloadWithTotalBaseDuration(ELITE_OVERLOAD_DUR);

                    ship.getFluxTracker().playOverloadSound();
                    if (ship.getFluxTracker().showFloaty() || (ship == Global.getCombatEngine().getPlayerShip())) {
                        ship.getFluxTracker().showOverloadFloatyIfNeeded("Emergency Shutdown!", JITTER_COLOR, 4f, true);
                    }

                    if (ship.getVariant().hasHullMod(II_BasePackage.TARGETING_PACKAGE)) {
                        ship.setWeaponGlow(0f, ENGINE_COLOR, EnumSet.of(WeaponAPI.WeaponType.BALLISTIC, WeaponAPI.WeaponType.MISSILE, WeaponAPI.WeaponType.ENERGY));
                    }

                    List<ShipEngineAPI> engList = ship.getEngineController().getShipEngines();
                    for (int i = 0; i < engList.size(); i++) {
                        ShipEngineAPI eng = engList.get(i);
                        if (eng.isSystemActivated()) {
                            engState.put(i, 0f);
                            ship.getEngineController().setFlameLevel(eng.getEngineSlot(), 0f);
                        }
                    }

                    for (int i = 0; i < SPARKS_ON_OVERLOAD.get(ship.getHullSize()); i++) {
                        Vector2f targetPoint = MathUtils.getRandomPointInCircle(ship.getLocation(), (shipRadius * 0.75f + 15f) * effectOverlevel * sparkIntensity);
                        Vector2f anchorPoint = MathUtils.getRandomPointInCircle(ship.getLocation(), shipRadius);
                        AnchoredEntity anchor = new AnchoredEntity(ship, anchorPoint);
                        float thickness = (float) Math.sqrt(((shipRadius * 0.025f + 5f) * effectOverlevel * sparkIntensity) * MathUtils.getRandomNumberInRange(0.75f, 1.25f)) * 3f;
                        Color coreColor = new Color(ENGINE_COLOR.getRed(), ENGINE_COLOR.getGreen(), ENGINE_COLOR.getBlue(), 255);
                        Global.getCombatEngine().spawnEmpArcPierceShields(ship, targetPoint, anchor, anchor, DamageType.ENERGY,
                                0f, 0f, shipRadius, null, thickness, JITTER_COLOR, coreColor);
                    }

                    Global.getCombatEngine().addPlugin(new BaseEveryFrameCombatPlugin() {
                        @Override
                        public void advance(float amount, List<InputEventAPI> events) {
                            if (!ship.getFluxTracker().isOverloadedOrVenting()) {
                                ship.resetOverloadColor();
                                Global.getCombatEngine().removePlugin(this);
                            }
                        }
                    });
                }

                lfData.gauge = 0f;
            }

            if (systemOff) {
                stats.getMaxSpeed().unmodify(id);
                stats.getMaxTurnRate().unmodify(id);
                stats.getFluxCapacity().unmodify(id);
                stats.getFluxDissipation().unmodify(id);
                stats.getBallisticRoFMult().unmodify(id);
                stats.getMissileRoFMult().unmodify(id);
                stats.getEnergyRoFMult().unmodify(id);
                stats.getAcceleration().unmodify(id);
                stats.getDeceleration().unmodify(id);
                stats.getTurnAcceleration().unmodify(id);
                stats.getHullDamageTakenMult().unmodify(id);
                stats.getArmorDamageTakenMult().unmodify(id);
                stats.getEmpDamageTakenMult().unmodify(id);
                stats.getCRLossPerSecondPercent().unmodify(id);

                if (ship.getVariant().hasHullMod(II_BasePackage.TARGETING_PACKAGE)) {
                    /* Ham-fisted attempt to get rid of that FUCKING glow */
                    ship.setWeaponGlow(0f, ENGINE_COLOR, EnumSet.of(WeaponAPI.WeaponType.BALLISTIC, WeaponAPI.WeaponType.MISSILE, WeaponAPI.WeaponType.ENERGY));
                }

                if (ship.controlsLocked()) {
                    lfData.gauge = 0f;
                } else {
                    lfData.gauge += (objectiveAmount * stats.getTimeMult().getModifiedValue() / gaugeRegenTime) * effectOverlevel;
                    if (lfData.gauge > 1f) {
                        lfData.gauge = 1f;
                    }
                }

                tempGauge = lfData.gauge;
                return;
            }
        }

        /* WTF? */
        if (effectLevel <= 0f) {
            tempGauge = lfData.gauge;
            if (unbugify) {
                ship.getSystem().deactivate();
            } else {
                unbugify = true;
            }
            return;
        } else {
            unbugify = false;
        }

        if (state == State.OUT) {
            stats.getMaxSpeed().unmodify(id);
            stats.getMaxTurnRate().unmodify(id);

            /* No gauge draining during out phase */
        } else {
            if (!ship.getVariant().hasHullMod(II_BasePackage.TARGETING_PACKAGE)) {
                if (!tempLightspearFiring) {
                    stats.getMaxSpeed().modifyPercent(id, SPEED_BONUS * effectOverlevel * (1f - tempLightspearChargeLevel));
                    stats.getMaxSpeed().modifyFlat(id, SPEED_FLAT_BONUS.get(ship.getHullSize()) * effectOverlevel * (1f - tempLightspearChargeLevel));
                    stats.getMaxTurnRate().modifyPercent(id, MAX_TURN_BONUS * effectOverlevel * (1f - tempLightspearChargeLevel));
                    stats.getMaxTurnRate().modifyFlat(id, MAX_TURN_FLAT_BONUS.get(ship.getHullSize()) * effectOverlevel * (1f - tempLightspearChargeLevel));
                } else {
                    stats.getMaxSpeed().unmodify(id);
                    stats.getMaxTurnRate().unmodify(id);
                }
            }

            lfData.gauge -= objectiveAmount * stats.getTimeMult().getModifiedValue() / gaugeDrainTime;
        }

        if (tempLightspearFiring || tempLightspearOutTransition) {
            float damageReductionMult;
            float jitterMult;
            if (ship.getVariant().hasHullMod(II_BasePackage.ARMOR_PACKAGE)) {
                damageReductionMult = ARMOR_DAMAGE_REDUCTION_MULT;
                jitterMult = 5f;
            } else {
                damageReductionMult = DAMAGE_REDUCTION_MULT;
                jitterMult = 4f;
            }
            damageReductionMult = II_Util.lerp(1f, damageReductionMult, Math.max(0f, Math.min(1f, (float) Math.sqrt(tempLightspearChargeLevel) * effectLevel * effectOverlevel)));
            jitterIntensity *= II_Util.lerp(1f, jitterMult, (float) Math.sqrt(tempLightspearChargeLevel) * effectLevel * effectOverlevel);
            jitterOverIntensity = II_Util.lerp(0f, jitterMult, (float) Math.sqrt(tempLightspearChargeLevel) * effectLevel * effectOverlevel);
            stats.getHullDamageTakenMult().modifyMult(id, damageReductionMult);
            stats.getArmorDamageTakenMult().modifyMult(id, damageReductionMult);
            stats.getEmpDamageTakenMult().modifyMult(id, damageReductionMult);
        } else {
            stats.getHullDamageTakenMult().unmodify(id);
            stats.getArmorDamageTakenMult().unmodify(id);
            stats.getEmpDamageTakenMult().unmodify(id);
        }

        if (ship.getVariant().hasHullMod(II_BasePackage.TARGETING_PACKAGE)) {
            stats.getFluxCapacity().modifyMult(id, II_Util.lerp(1f, TARGETING_CAPACITY_MULT, effectLevel * effectOverlevel));
            stats.getFluxDissipation().modifyMult(id, II_Util.lerp(1f, TARGETING_DISSIPATION_MULT, effectLevel * effectOverlevel));
            stats.getBallisticRoFMult().modifyPercent(id, TARGETING_ROF_BONUS * effectLevel * effectOverlevel);
            stats.getMissileRoFMult().modifyPercent(id, TARGETING_ROF_BONUS * effectLevel * effectOverlevel);
            stats.getEnergyRoFMult().modifyPercent(id, TARGETING_ROF_BONUS * effectLevel * effectOverlevel);
        } else {
            stats.getFluxCapacity().modifyMult(id, II_Util.lerp(1f, CAPACITY_MULT, effectLevel * effectOverlevel));
            stats.getFluxDissipation().modifyMult(id, II_Util.lerp(1f, DISSIPATION_MULT, effectLevel * effectOverlevel));

            if (!tempLightspearFiring) {
                stats.getAcceleration().modifyPercent(id, ACCEL_BONUS * effectLevelSquared * effectOverlevel * (1f - tempLightspearChargeLevel));
                stats.getAcceleration().modifyFlat(id, ACCEL_FLAT_BONUS * effectLevelSquared * effectOverlevel * (1f - tempLightspearChargeLevel));
                stats.getDeceleration().modifyPercent(id, DECEL_BONUS * effectLevelSquared * effectOverlevel * (1f - tempLightspearChargeLevel));
                stats.getDeceleration().modifyFlat(id, DECEL_FLAT_BONUS * effectLevelSquared * effectOverlevel * (1f - tempLightspearChargeLevel));
                stats.getTurnAcceleration().modifyPercent(id, TURN_ACCEL_BONUS * effectLevelSquared * effectOverlevel * (1f - tempLightspearChargeLevel));
            } else {
                stats.getAcceleration().unmodify(id);
                stats.getDeceleration().unmodify(id);
                stats.getTurnAcceleration().unmodify(id);
            }
        }

        if (ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
            if (effectOverlevelSquared > 1f) {
                totalPeakTimeLoss += (ELITE_OVERLEVEL_CR_LOSS_MULT - 1f) * effectLevel * (effectOverlevelSquared - 1f) * objectiveAmount * stats.getTimeMult().getModifiedValue();
                stats.getCRLossPerSecondPercent().modifyMult(id, II_Util.lerp(1f, ELITE_OVERLEVEL_CR_LOSS_MULT, effectLevel * (effectOverlevel - 1f)));
                stats.getPeakCRDuration().modifyFlat(id, -totalPeakTimeLoss / ship.getMutableStats().getPeakCRDuration().getMult());
            }
        } else {
            totalPeakTimeLoss += (CR_LOSS_MULT - 1f) * effectLevel * effectOverlevelSquared * objectiveAmount * stats.getTimeMult().getModifiedValue();
            stats.getCRLossPerSecondPercent().modifyMult(id, II_Util.lerp(1f, CR_LOSS_MULT, effectLevel * effectOverlevel));
            stats.getPeakCRDuration().modifyFlat(id, -totalPeakTimeLoss / ship.getMutableStats().getPeakCRDuration().getMult());
        }

        if (!ship.getVariant().hasHullMod(II_BasePackage.TARGETING_PACKAGE)) {
            if (ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
                ship.getEngineController().fadeToOtherColor(ENGINEKEY1, ENGINE_COLOR, CONTRAIL_COLOR, (float) Math.sqrt(effectOverlevel), effectLevel);
            } else {
                ship.getEngineController().fadeToOtherColor(ENGINEKEY1, ENGINE_COLOR, CONTRAIL_COLOR, effectLevel * (float) Math.sqrt(effectOverlevel), 1f);
            }
            ship.getEngineController().extendFlame(ENGINEKEY2, 0.25f * effectLevel * (float) Math.sqrt(effectOverlevel),
                    0.25f * effectLevel * (float) Math.sqrt(effectOverlevel), 0.25f * effectLevel * (float) Math.sqrt(effectOverlevel));
        }

        if (effectLevel > 0f) {
            Vector2f offset = new Vector2f(MathUtils.getRandomNumberInRange(-65f, -30f), 0f);
            VectorUtils.rotate(offset, ship.getFacing(), offset);
            Vector2f centerLocation = Vector2f.add(ship.getLocation(), offset, new Vector2f());

            interval2.advance(objectiveAmount * stats.getTimeMult().getModifiedValue());
            if (interval2.intervalElapsed()) {
                float blastArea = shipRadius * BLAST_AREA_RADIUS_SCALE + (BLAST_AREA_FLAT * effectLevel * effectOverlevel);
                float blastDamage = BLAST_DAMAGE * effectLevel * effectOverlevel;
                float maxBlastDamage = BLAST_MAX_DAMAGE * effectLevel * effectOverlevel;
                float blastEMP = BLAST_EMP * effectLevel * effectOverlevel;

                float totalDamage = 0f;
                int fighterTargets = 0;
                List<ShipAPI> nearbyEnemies = CombatUtils.getShipsWithinRange(centerLocation, blastArea);
                for (ShipAPI thisEnemy : nearbyEnemies) {
                    if ((thisEnemy == ship) || !(thisEnemy.isFighter() || thisEnemy.isDrone()) || (thisEnemy.getOwner() == ship.getOwner()) || !thisEnemy.isAlive()) {
                        continue;
                    }

                    float contribution = 1f;

                    float falloff = 1f - MathUtils.getDistance(ship, thisEnemy) / blastArea;
                    if (thisEnemy.getCollisionClass() == CollisionClass.NONE) {
                        continue;
                    } else {
                        totalDamage += blastDamage * falloff * contribution;
                    }

                    for (int i = 0; i <= (int) (blastDamage * falloff / 125f); i++) {
                        totalDamage += blastDamage * falloff * 0.25f * contribution;
                    }
                    fighterTargets++;
                    if (fighterTargets >= BLAST_MAX_FIGHTER_TARGETS) {
                        break;
                    }
                }

                int missileTargets = 0;
                List<MissileAPI> allMissiles = CombatUtils.getMissilesWithinRange(centerLocation, blastArea);
                Collections.shuffle(allMissiles);
                for (MissileAPI missile : allMissiles) {
                    if (missile.getOwner() != ship.getOwner()) {
                        float falloff = 1f - MathUtils.getDistance(ship, missile) / blastArea;
                        if (missile.getCollisionClass() == CollisionClass.NONE) {
                            continue;
                        }

                        missileTargets++;
                        totalDamage += Math.min(missile.getHitpoints(), blastDamage * 0.5f * falloff);
                        if (missileTargets >= BLAST_MAX_MISSILE_TARGETS) {
                            break;
                        }
                    }
                }

                if (totalDamage > 0f) {
                    Global.getSoundPlayer().playSound("ii_active_armor_spark", 1f, Math.max(0.25f, Math.min(1f, (float) Math.sqrt(totalDamage / maxBlastDamage))),
                            centerLocation, ZERO);
                }

                float attenuation = 1f;
                if (totalDamage > maxBlastDamage) {
                    attenuation *= maxBlastDamage / totalDamage;
                }

                Color coreColor = new Color(ENGINE_COLOR.getRed(), ENGINE_COLOR.getGreen(), ENGINE_COLOR.getBlue(), 255);
                fighterTargets = 0;
                for (ShipAPI thisEnemy : nearbyEnemies) {
                    if ((thisEnemy == ship) || !(thisEnemy.isFighter() || thisEnemy.isDrone()) || (thisEnemy.getOwner() == ship.getOwner()) || !thisEnemy.isAlive()) {
                        continue;
                    }

                    Vector2f projection = VectorUtils.getDirectionalVector(centerLocation, thisEnemy.getLocation());
                    projection.scale(thisEnemy.getCollisionRadius());
                    Vector2f.add(projection, thisEnemy.getLocation(), projection);
                    Vector2f damagePoint = CollisionUtils.getCollisionPoint(centerLocation, projection, thisEnemy);
                    if (damagePoint == null) {
                        damagePoint = centerLocation;
                    }
                    float falloff = 1f - MathUtils.getDistance(ship, thisEnemy) / blastArea;
                    if (thisEnemy.getOwner() == ship.getOwner()) {
                        falloff *= 0.25f;
                    }
                    if (thisEnemy.getCollisionClass() == CollisionClass.NONE) {
                        continue;
                    } else {
                        Global.getCombatEngine().applyDamage(thisEnemy, damagePoint, blastDamage * falloff * attenuation,
                                DamageType.ENERGY, blastEMP * falloff * attenuation, false, false, ship, false);
                    }

                    ShipAPI empTarget = thisEnemy;
                    for (int i = 0; i <= Math.round(attenuation * blastDamage * falloff / 125f); i++) {
                        Global.getCombatEngine().spawnEmpArc(ship, centerLocation, empTarget, empTarget, DamageType.ENERGY,
                                blastDamage * falloff * 0.25f, blastEMP * falloff * 0.25f, 10000f, null,
                                (float) Math.sqrt(blastDamage), FRINGE_COLOR, coreColor);
                    }
                    fighterTargets++;
                    if (fighterTargets >= BLAST_MAX_FIGHTER_TARGETS) {
                        break;
                    }
                }

                missileTargets = 0;
                for (MissileAPI missile : allMissiles) {
                    if (missile.getOwner() != ship.getOwner()) {
                        float falloff = 1f - MathUtils.getDistance(ship, missile) / blastArea;
                        if (missile.getCollisionClass() == CollisionClass.NONE) {
                            continue;
                        }

                        missileTargets++;
                        MissileAPI empTarget = missile;
                        Global.getCombatEngine().spawnEmpArc(ship, centerLocation, ship, empTarget, DamageType.ENERGY,
                                blastDamage * falloff * 0.5f * attenuation, blastEMP * falloff * 0.5f * attenuation,
                                10000f, null, (float) Math.sqrt(blastDamage * falloff * attenuation), FRINGE_COLOR, coreColor);
                        if (missileTargets >= BLAST_MAX_MISSILE_TARGETS) {
                            break;
                        }
                    }
                }
            }
        }

        /* Unweighted direction calculation for visual purposes - 0 degrees is forward */
        Vector2f direction = new Vector2f();
        float visualDir = 0f;
        boolean maneuvering = true;
        boolean cwTurn = false;
        boolean ccwTurn = false;
        if (ship.getEngineController().isAccelerating()) {
            direction.y += 1f;
        } else if (ship.getEngineController().isAcceleratingBackwards()) {
            direction.y -= 1f;
        }
        if (ship.getEngineController().isStrafingLeft()) {
            direction.x -= 1f;
        } else if (ship.getEngineController().isStrafingRight()) {
            direction.x += 1f;
        }
        if (direction.length() > 0f) {
            visualDir = MathUtils.clampAngle(VectorUtils.getFacing(direction) - 90f);
        } else if (ship.getEngineController().isDecelerating() && (ship.getVelocity().length() > 0f)) {
            visualDir = MathUtils.clampAngle(VectorUtils.getFacing(ship.getVelocity()) + 180f - ship.getFacing());
        } else {
            maneuvering = false;
        }
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
                engineScaleMap.put(i, getSystemEngineScale(ship, eng, visualDir, maneuvering, cwTurn, ccwTurn, null));
            }
        }
        for (int i = 0; i < engList.size(); i++) {
            ShipEngineAPI eng = engList.get(i);
            if (eng.isSystemActivated()) {
                float targetLevel = getSystemEngineScale(ship, eng, visualDir, maneuvering, cwTurn, ccwTurn, engineScaleMap);
                if (state == State.OUT) {
                    targetLevel *= effectLevel;
                }
                if (tempLightspearFiring) {
                    targetLevel = 0f;
                }
                if (tempLightspearOutTransition) {
                    targetLevel *= 1f - tempLightspearChargeLevel;
                }
                targetLevel = Math.max(0f, targetLevel);
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
                if (ship.getVariant().hasHullMod(II_BasePackage.TARGETING_PACKAGE)) {
                    currLevel = 0f;
                }
                engState.put(i, currLevel);
                ship.getEngineController().setFlameLevel(eng.getEngineSlot(), currLevel);
            }
        }

        if (!ship.getVariant().hasHullMod(II_BasePackage.TARGETING_PACKAGE)) {
            if (state == State.IN) {
                ship.getEngineController().getExtendLengthFraction().advance(objectiveAmount * 4f);
                ship.getEngineController().getExtendWidthFraction().advance(objectiveAmount * 4f);
                ship.getEngineController().getExtendGlowFraction().advance(objectiveAmount * 4f);
            } else if (state == State.ACTIVE) {
                ship.getEngineController().getExtendLengthFraction().advance(objectiveAmount);
                ship.getEngineController().getExtendWidthFraction().advance(objectiveAmount);
                ship.getEngineController().getExtendGlowFraction().advance(objectiveAmount);
            }
        }

        if (ship.getVariant().hasHullMod(II_BasePackage.TARGETING_PACKAGE)) {
            Color glowColor = new Color(II_Util.clamp255(Math.round((3f * JITTER_COLOR.getRed() + 255f) / 4f)),
                    II_Util.clamp255(Math.round((3f * JITTER_COLOR.getGreen() + 255f) / 4f)),
                    II_Util.clamp255(Math.round((3f * JITTER_COLOR.getBlue() + 255f) / 4f)),
                    255);
            ship.setWeaponGlow(effectLevel, glowColor, EnumSet.of(WeaponAPI.WeaponType.BALLISTIC, WeaponAPI.WeaponType.MISSILE, WeaponAPI.WeaponType.ENERGY));
        }

        if (!Global.getCombatEngine().isPaused()) {
            Global.getSoundPlayer().playLoop("ii_luxfinis_loop", ship, 1f * effectOverlevel * pitchShift, 1f * effectOverlevel * effectLevel * volumeShift, ship.getLocation(), ZERO);
        }

        interval.advance(amount);
        if (interval.intervalElapsed()) {
            if (((state == State.IN) || (state == State.ACTIVE)) && !(tempLightspearFiring && (tempLightspearChargeLevel >= 1f)) && !ship.isPhased()) {
                for (WeaponAPI blaster : blasters) {
                    Color glowColor = new Color(GLOW_COLOR.getRed(), GLOW_COLOR.getGreen(), GLOW_COLOR.getBlue(),
                            II_Util.clamp255(Math.round(interval.getIntervalDuration() * GLOW_COLOR.getAlpha())));
                    Global.getCombatEngine().addHitParticle(blaster.getLocation(), ship.getVelocity(),
                            (float) Math.random() * 25f + 50f, 0.15f, 0.15f, glowColor);
                }
                for (WeaponAPI photonBeam : photonBeams) {
                    Color glowColor = new Color(GLOW_COLOR.getRed(), GLOW_COLOR.getGreen(), GLOW_COLOR.getBlue(),
                            II_Util.clamp255(Math.round(interval.getIntervalDuration() * GLOW_COLOR.getAlpha())));
                    Global.getCombatEngine().addHitParticle(photonBeam.getLocation(), ship.getVelocity(),
                            (float) Math.random() * 25f + 50f, 0.15f, 0.15f, glowColor);
                }
            }

            if (ship.getVariant().hasHullMod(II_BasePackage.TARGETING_PACKAGE)) {
                float randRange = (float) shipRadius * 0.25f * afterimageIntensity;
                float randSpeed = (float) shipRadius * 1f * afterimageIntensity;
                float randAngle = MathUtils.getRandomNumberInRange(0f, 360f);
                float randRadiusFrac = (float) (Math.random() + Math.random());
                randRadiusFrac = (randRadiusFrac > 1f ? 2f - randRadiusFrac : randRadiusFrac);
                Vector2f randLoc = MathUtils.getPointOnCircumference(ZERO, randRange * randRadiusFrac, randAngle);
                Vector2f randVel = MathUtils.getRandomPointInCircle(ZERO, randSpeed * MathUtils.getRandomNumberInRange(0.5f, 1f));
                Color afterimageColor = new Color(JITTER_COLOR.getRed(), JITTER_COLOR.getGreen(), JITTER_COLOR.getBlue(),
                        II_Util.clamp255(Math.round(0.1f * afterimageIntensity * effectLevel * (float) Math.sqrt(effectOverlevel) * CONTRAIL_COLOR.getAlpha())));
                ship.addAfterimage(afterimageColor, randLoc.x, randLoc.y, randVel.x, randVel.y,
                        randRange, 0f, 0.1f, 0.2f * effectLevel, true, false, false);
                Color afterimageColor2 = new Color(JITTER_COLOR.getRed(), JITTER_COLOR.getGreen(), JITTER_COLOR.getBlue(),
                        II_Util.clamp255(Math.round(0.05f * afterimageIntensity * effectLevel * (float) Math.sqrt(effectOverlevel) * CONTRAIL_COLOR.getAlpha())));
                ship.addAfterimage(afterimageColor2, randLoc.x, randLoc.y, randVel.x, randVel.y,
                        randRange, 0f, 0.1f, 0.2f * effectLevel, true, false, true);

                Color jitterColor = new Color(JITTER_COLOR.getRed(), JITTER_COLOR.getGreen(), JITTER_COLOR.getBlue(),
                        II_Util.clamp255(Math.round(0.4f * jitterIntensity * effectLevel * (float) Math.sqrt(effectOverlevel) * JITTER_COLOR.getAlpha())));
                ship.setJitterUnder(this, jitterColor, 1f, 10, 1f, 4f * jitterIntensity);
            } else {
                float randRange = (float) shipRadius * 0.2f * afterimageIntensity;
                float randSpeed = (float) shipRadius * 0.5f * afterimageIntensity;
                float randAngle = MathUtils.getRandomNumberInRange(0f, 360f);
                float randRadiusFrac = (float) (Math.random() + Math.random());
                randRadiusFrac = (randRadiusFrac > 1f ? 2f - randRadiusFrac : randRadiusFrac);
                Vector2f randLoc = MathUtils.getPointOnCircumference(ZERO, randRange * randRadiusFrac, randAngle);
                Vector2f randVel = MathUtils.getRandomPointInCircle(ZERO, randSpeed * MathUtils.getRandomNumberInRange(0.5f, 1f));
                Color afterimageColor = new Color(JITTER_COLOR.getRed(), JITTER_COLOR.getGreen(), JITTER_COLOR.getBlue(),
                        II_Util.clamp255(Math.round(0.1f * afterimageIntensity * effectLevel * (float) Math.sqrt(effectOverlevel) * CONTRAIL_COLOR.getAlpha())));
                ship.addAfterimage(afterimageColor, randLoc.x, randLoc.y, randVel.x, randVel.y,
                        randRange, 0f, 0.1f, 0.2f * effectLevel, true, false, false);

                randRange = (float) Math.sqrt(shipRadius) * 0.75f * afterimageIntensity;
                randLoc = MathUtils.getRandomPointInCircle(ZERO, randRange);
                Color afterimageColor3 = new Color(JITTER_COLOR.getRed(), JITTER_COLOR.getGreen(), JITTER_COLOR.getBlue(),
                        II_Util.clamp255(Math.round(0.15f * afterimageIntensity * effectLevel * (float) Math.sqrt(effectOverlevel) * CONTRAIL_COLOR.getAlpha())));
                ship.addAfterimage(afterimageColor3, randLoc.x, randLoc.y, -ship.getVelocity().x, -ship.getVelocity().y,
                        randRange, 0f, 0.1f, 0.5f * effectLevel, true, false, false);

                Color jitterColor = new Color(JITTER_COLOR.getRed(), JITTER_COLOR.getGreen(), JITTER_COLOR.getBlue(),
                        II_Util.clamp255(Math.round(0.4f * jitterIntensity * effectLevel * (float) Math.sqrt(effectOverlevel) * JITTER_COLOR.getAlpha())));
                ship.setJitterUnder(this, jitterColor, 1f, 10, 1f, 4f * jitterIntensity);
            }

            if (effectOverlevel > 1f) {
                float randRange = (float) Math.sqrt(shipRadius) * (float) Math.sqrt(effectOverlevel);
                Vector2f randLoc = MathUtils.getRandomPointOnCircumference(ZERO, randRange);
                Color afterimageColor = new Color(ENGINE_COLOR.getRed(), ENGINE_COLOR.getGreen(), ENGINE_COLOR.getBlue(),
                        II_Util.clamp255(Math.round(0.75f * effectLevel * (effectOverlevel - 1f) * CONTRAIL_COLOR.getAlpha())));
                ship.addAfterimage(afterimageColor, randLoc.x, randLoc.y, 0f, 0f,
                        randRange, 0f, 0f, 0.15f, true, false, true);
            }

            if (Math.random() < (BASE_SPARK_CHANCE_PER_TICK.get(ship.getHullSize())) * effectLevelSquared * effectOverlevelSquared * sparkIntensity) {
                float targetAngle = (float) Math.random() * 360f;
                Vector2f targetPointPre = MathUtils.getPointOnCircumference(ship.getLocation(), shipRadius * 2f, targetAngle);
                Vector2f anchorPoint = CollisionUtils.getCollisionPoint(targetPointPre, ship.getLocation(), ship);
                if (anchorPoint != null) {
                    float sparkLen = (shipRadius * 0.05f + 10f) * MathUtils.getRandomNumberInRange(0.75f, 1.25f) * sparkIntensity * (float) Math.sqrt(effectOverlevel);
                    Vector2f targetPoint = MathUtils.getPointOnCircumference(ship.getLocation(),
                            MathUtils.getDistance(ship.getLocation(), anchorPoint) + sparkLen, targetAngle);
                    AnchoredEntity anchor = new AnchoredEntity(ship, anchorPoint);
                    float thickness = (float) Math.sqrt(sparkLen) * 3f;
                    Color coreColor = new Color(ENGINE_COLOR.getRed(), ENGINE_COLOR.getGreen(), ENGINE_COLOR.getBlue(), 255);
                    Global.getCombatEngine().spawnEmpArcPierceShields(ship, targetPoint, anchor, anchor, DamageType.ENERGY,
                            0f, 0f, sparkLen * 2f, null, thickness, FRINGE_COLOR, coreColor);
                }
            }
        }

        if (jitterOverIntensity > 0f) {
            if (ship.getVariant().hasHullMod(II_BasePackage.TARGETING_PACKAGE)) {
                Color jitterOverColor = new Color(JITTER_COLOR.getRed(), JITTER_COLOR.getGreen(), JITTER_COLOR.getBlue(),
                        II_Util.clamp255(Math.round(0.075f * jitterOverIntensity * effectLevel * (float) Math.sqrt(effectOverlevel) * JITTER_COLOR.getAlpha())));
                ship.setJitter(this, jitterOverColor, 1f, 2, 0f, 3f * jitterOverIntensity);
            } else {
                Color jitterOverColor = new Color(JITTER_COLOR.getRed(), JITTER_COLOR.getGreen(), JITTER_COLOR.getBlue(),
                        II_Util.clamp255(Math.round(0.075f * jitterOverIntensity * effectLevel * (float) Math.sqrt(effectOverlevel) * JITTER_COLOR.getAlpha())));
                ship.setJitter(this, jitterOverColor, 1f, 2, 0f, 3f * jitterOverIntensity);
            }
        }

        tempGauge = lfData.gauge;
    }

    @Override
    public boolean isUsable(ShipSystemAPI system, ShipAPI ship) {
        if (shutdown) {
            return false;
        }
        return isUsable(ship, system);
    }

    @Override
    public String getInfoText(ShipSystemAPI system, ShipAPI ship) {
        float overGaugeLevel = 0f;
        float lowGaugeLevel = LIGHTSPEAR_FIRE_TIME / GAUGE_DRAIN_TIME;
        boolean isElite = false;
        if ((ship != null) && ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
            overGaugeLevel = ELITE_OVER_GAUGE_LEVEL;
            isElite = true;
        }
        if ((ship != null) && ship.getVariant().hasHullMod(II_BasePackage.TARGETING_PACKAGE)) {
            lowGaugeLevel = LIGHTSPEAR_FIRE_TIME / TARGETING_GAUGE_DRAIN_TIME;
        }
        float gauge = getGauge(ship);

        int displayGauge = Math.round(100f * Math.max(0f, gauge));
        if (shutdown) {
            return "SHUTDOWN";
        }
        if ((gauge < overGaugeLevel) && system.isOn() && isElite) {
            long count200ms = (long) Math.floor(Global.getCombatEngine().getTotalElapsedTime(true) / 0.2f);
            if (count200ms % 2L == 0L) {
                return "" + displayGauge + "% - ALERT!";
            } else {
                return "" + displayGauge + "% - ";
            }
        }
        if (((gauge < overGaugeLevel) && !system.isOn() && !isElite) || system.isCoolingDown()) {
            return "" + displayGauge + "%";
        }
        if (system.isActive()) {
            if ((gauge < lowGaugeLevel) && !isElite) {
                return "" + displayGauge + "% - ACTIVE (LOW)";
            }
            return "" + displayGauge + "% - ACTIVE";
        }
        return "" + displayGauge + "% - READY";
    }

    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        float overGaugeLevel = 0f;
        float maxOverlevel = 1f;
        float damageReductionMult = DAMAGE_REDUCTION_MULT;
        float capacityMult = CAPACITY_MULT;
        float dissipationMult = DISSIPATION_MULT;
        if (tempArmor) {
            damageReductionMult = ARMOR_DAMAGE_REDUCTION_MULT;
        }
        if (tempTargeting) {
            capacityMult = TARGETING_CAPACITY_MULT;
            dissipationMult = TARGETING_DISSIPATION_MULT;
        }
        if (tempElite) {
            overGaugeLevel = ELITE_OVER_GAUGE_LEVEL;
            maxOverlevel = ELITE_MAX_OVERLEVEL;
        }

        float effectOverlevel = 1f;
        if ((tempGauge < overGaugeLevel) && tempElite) {
            effectOverlevel = 1f / II_Util.lerp(1f / maxOverlevel, 1f, Math.max(0f, tempGauge) / overGaugeLevel);
        }
        float effectOverlevelSquared = effectOverlevel * effectOverlevel;
        damageReductionMult = II_Util.lerp(1f, damageReductionMult, (float) Math.sqrt(tempLightspearChargeLevel) * effectLevel * effectOverlevel);

        switch (index) {
            case 0:
                if ((state == State.IN) || (state == State.ACTIVE) || (state == State.OUT)) {
                    return new StatusData("flux capacity +" + Math.round((II_Util.lerp(1f, capacityMult, effectLevel * effectOverlevel) - 1f) * 100f) + "%", false);
                }
                break;
            case 1:
                if ((state == State.IN) || (state == State.ACTIVE) || (state == State.OUT)) {
                    return new StatusData("flux dissipation +" + Math.round((II_Util.lerp(1f, dissipationMult, effectLevel * effectOverlevel) - 1f) * 100f) + "%", false);
                }
                break;
            case 2:
                if (tempTargeting) {
                    if ((state == State.IN) || (state == State.ACTIVE) || (state == State.OUT)) {
                        return new StatusData("rate of fire +" + Math.round(TARGETING_ROF_BONUS * effectLevel * effectOverlevel) + "%", false);
                    }
                } else if (!tempLightspearFiring) {
                    if ((state == State.IN) || (state == State.ACTIVE) || (state == State.OUT)) {
                        return new StatusData("improved maneuverability", false);
                    }
                }
                break;
            case 3:
                if (!tempTargeting && !tempLightspearFiring) {
                    if ((state == State.IN) || (state == State.ACTIVE)) {
                        return new StatusData("increased engine power", false);
                    }
                }
                break;
            case 4:
                if (tempLightspearFiring || tempLightspearOutTransition) {
                    if ((state == State.IN) || (state == State.ACTIVE) || (state == State.OUT)) {
                        return new StatusData("" + Math.round((1f - damageReductionMult) * 100f) + "% less damage taken", false);
                    }
                }
                break;
            case 5:
                if (tempElite) {
                    if (effectOverlevelSquared > 1f) {
                        if ((state == State.IN) || (state == State.ACTIVE) || (state == State.OUT)) {
                            return new StatusData("CR degradation +" + Math.round((II_Util.lerp(1f, ELITE_OVERLEVEL_CR_LOSS_MULT, effectLevel * (effectOverlevelSquared - 1f)) - 1f) * 100f) + "%", true);
                        }
                    }
                } else {
                    if ((state == State.IN) || (state == State.ACTIVE) || (state == State.OUT)) {
                        return new StatusData("CR degradation +" + Math.round((II_Util.lerp(1f, CR_LOSS_MULT, effectLevel * effectOverlevelSquared) - 1f) * 100f) + "%", true);
                    }
                }
                break;
            default:
                break;
        }
        return null;
    }

    private static float getSystemEngineScale(ShipAPI ship, ShipEngineAPI engine, float direction, boolean maneuvering, boolean cwTurn, boolean ccwTurn, Map<Integer, Float> engineScaleMap) {
        float target = 0f;

        Vector2f engineRelLocation = new Vector2f(engine.getLocation());
        Vector2f.sub(engineRelLocation, ship.getLocation(), engineRelLocation); // Example -- (20, 20) ship facing forwards, engine on upper right quadrant
        engineRelLocation.normalise(engineRelLocation); // (0.7071, 0.7071)
        VectorUtils.rotate(engineRelLocation, -ship.getFacing(), engineRelLocation); // (0.7071, -0.7071) - engine past centerline (x) on right side (y)
        Vector2f engineAngleVector = VectorUtils.rotate(new Vector2f(1f, 0f), engine.getEngineSlot().getAngle()); // 270 degrees into (0, -1)
        float torque = VectorUtils.getCrossProduct(engineRelLocation, engineAngleVector); // 0.7071*-1 - -0.7071*0 = -0.7071 (70.71% strength CCW torque)

        if ((Math.abs(MathUtils.getShortestRotation(engine.getEngineSlot().getAngle(), direction)) > 100f) && maneuvering) {
            target = 1f;
        } else {
            if ((torque <= -0.4f) && ccwTurn) {
                target = 1f;
            } else if ((torque >= 0.4f) && cwTurn) {
                target = 1f;
            }
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

    public static float getGauge(ShipAPI ship) {
        if ((ship == null) || (ship.getSystem() == null)) {
            return 0f;
        }

        Object data = Global.getCombatEngine().getCustomData().get(DATA_KEY_ID + "_" + ship.getId());
        if (data instanceof LuxFinisData) {
            LuxFinisData odData = (LuxFinisData) data;

            return odData.gauge;
        } else {
            return 0f;
        }
    }

    /* Returns the same regardless of whether the system is on or not */
    public static float getOverlevel(ShipAPI ship) {
        if ((ship == null) || (ship.getSystem() == null)) {
            return 0f;
        }

        float overGaugeLevel = 0f;
        float maxOverlevel = 1f;
        if (ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
            overGaugeLevel = ELITE_OVER_GAUGE_LEVEL;
            maxOverlevel = ELITE_MAX_OVERLEVEL;
        }
        float gauge = getGauge(ship);

        float effectOverlevel = 1f;
        if ((gauge < overGaugeLevel) && (gauge >= 0f) && ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
            effectOverlevel = 1f / II_Util.lerp(1f / maxOverlevel, 1f, gauge / overGaugeLevel);
        }
        return effectOverlevel;
    }

    public static boolean isUsable(ShipAPI ship, ShipSystemAPI system) {
        if ((ship == null) || (system == null)) {
            return false;
        }
        if (ship.isPhased()) {
            return false;
        }

        float activateThreshold = ACTIVATE_THRESHOLD;
        boolean isElite = false;
        if (ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
            isElite = true;
        }
        float gauge = getGauge(ship);

        return !((gauge < activateThreshold) && !system.isActive() && !isElite);
    }

    private static class LuxFinisData {

        final Object stateKey;
        float gauge;

        LuxFinisData(Object stateKey) {
            this.stateKey = stateKey;
        }
    }
}
