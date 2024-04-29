package data.scripts.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipEngineControllerAPI.ShipEngineAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.hullmods.II_BasePackage;
import data.scripts.util.II_Util;
import java.awt.Color;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.entities.AnchoredEntity;
import org.lwjgl.util.vector.Vector2f;

public class II_OverdriveStats extends BaseShipSystemScript {

    public static final float GAUGE_DRAIN_TIME = 12f;
    public static final float GAUGE_REGEN_TIME = 28f;
    public static final float COOLDOWN_MIN = 0.25f;
    public static final float COOLDOWN_MAX = 1f;
    public static final float CAPACITY_MULT = 1.5f;
    public static final float DISSIPATION_MULT = 2f;
    public static final float ROF_BONUS = 50f;
    public static final float SPEED_BONUS = 25f;
    public static final Map<HullSize, Float> SPEED_FLAT_BONUS = new HashMap<>();
    public static final float ACCEL_BONUS = 50f;
    public static final float ACCEL_FLAT_BONUS = 30f;
    public static final float DECEL_BONUS = 50f;
    public static final float DECEL_FLAT_BONUS = 30f;
    public static final float TURN_ACCEL_BONUS = 200f;
    public static final float MAX_TURN_BONUS = 50f;
    public static final Map<HullSize, Float> MAX_TURN_FLAT_BONUS = new HashMap<>();
    public static final float AUTOAIM_BONUS = 1f / 3f;
    public static final float RECOIL_MULT = 2f / 3f;
    public static final float OVERLOAD_DUR = GAUGE_DRAIN_TIME * 0.5f;
    public static final float CR_LOSS_MULT = 3f;
    public static final float OVER_GAUGE_LEVEL = 1f / 3f;
    public static final float MAX_OVERLEVEL = 2f;
    public static final float ARMOR_GAUGE_REGEN_TIME = GAUGE_REGEN_TIME / 1.5f;
    public static final float ARMOR_OVER_GAUGE_LEVEL = 0.2f;
    public static final float ARMOR_CR_LOSS_MULT = 2f;
    public static final float TARGETING_GAUGE_DRAIN_TIME = GAUGE_DRAIN_TIME * (4f / 3f);
    public static final float TARGETING_OVERLOAD_DUR = TARGETING_GAUGE_DRAIN_TIME * 0.5f;
    public static final float TARGETING_ROF_BONUS = 100f;
    public static final float TARGETING_PROJ_SPEED_MULT = 2f;
    public static final float TARGETING_TURRET_TURN_MULT = 2f;
    public static final float ELITE_OVERLOAD_DUR = GAUGE_DRAIN_TIME;
    public static final float ELITE_OVER_GAUGE_LEVEL = 0.5f;
    public static final float ELITE_MAX_OVERLEVEL = 3f;
    public static final float ELITE_OVERLEVEL_CR_LOSS_MULT = 2f;

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

    private static final Color ENGINE_COLOR_STANDARD = new Color(255, 125, 50, 220);
    private static final Color CONTRAIL_COLOR_STANDARD = new Color(80, 60, 40, 100);
    private static final Color OVERLOAD_COLOR_STANDARD = new Color(205, 50, 10);
    private static final Color ENGINE_COLOR_ARMOR = new Color(205, 150, 100, 200);
    private static final Color CONTRAIL_COLOR_ARMOR = new Color(80, 75, 40, 90);
    private static final Color OVERLOAD_COLOR_ARMOR = new Color(205, 150, 50);
    private static final Color ENGINE_COLOR_TARGETING = new Color(50, 220, 255, 220);
    private static final Color CONTRAIL_COLOR_TARGETING = new Color(40, 70, 80, 100);
    private static final Color OVERLOAD_COLOR_TARGETING = new Color(10, 110, 205);
    private static final Color ENGINE_COLOR_ELITE = new Color(145, 50, 255, 220);
    private static final Color CONTRAIL_COLOR_ELITE = new Color(60, 40, 80, 100);
    private static final Color OVERLOAD_COLOR_ELITE = new Color(100, 10, 205);

    private static final Vector2f ZERO = new Vector2f();
    private static final String DATA_KEY_ID = "II_OverdriveStats";
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
    private boolean unbugify = false;
    private final IntervalUtil interval = new IntervalUtil(TICK_TIME, TICK_TIME);

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        final ShipAPI ship = (ShipAPI) stats.getEntity();
        if (ship == null) {
            return;
        }

        Object data = Global.getCombatEngine().getCustomData().get(DATA_KEY_ID + "_" + ship.getId());
        OverdriveData odData = null;
        if (data instanceof OverdriveData) {
            odData = (OverdriveData) data;
        }
        if ((odData == null) || (STATEKEY != odData.stateKey)) {
            odData = new OverdriveData(STATEKEY);
            Global.getCombatEngine().getCustomData().put(DATA_KEY_ID + "_" + ship.getId(), odData);
            odData.gauge = 1f;
            totalPeakTimeLoss = 0f;
        }

        float shipRadius = II_Util.effectiveRadius(ship);
        float amount = Global.getCombatEngine().getElapsedInLastFrame();
        float objectiveAmount = amount * Global.getCombatEngine().getTimeMult().getModifiedValue();
        if (Global.getCombatEngine().isPaused()) {
            amount = 0f;
            objectiveAmount = 0f;
        }

        Color ENGINE_COLOR = ENGINE_COLOR_STANDARD;
        Color CONTRAIL_COLOR = CONTRAIL_COLOR_STANDARD;
        Color OVERLOAD_COLOR = OVERLOAD_COLOR_STANDARD;
        float gaugeDrainTime = GAUGE_DRAIN_TIME;
        float gaugeRegenTime = GAUGE_REGEN_TIME;
        float overGaugeLevel = OVER_GAUGE_LEVEL;
        float maxOverlevel = MAX_OVERLEVEL;
        float afterimageIntensity = 1f;
        float sparkIntensity = 1f;
        float pitchShift = 1.2f;
        float volumeShift = 0.8f;
        tempArmor = false;
        tempTargeting = false;
        tempElite = false;
        if (ship.getVariant().hasHullMod(II_BasePackage.ARMOR_PACKAGE)) {
            ENGINE_COLOR = ENGINE_COLOR_ARMOR;
            CONTRAIL_COLOR = CONTRAIL_COLOR_ARMOR;
            OVERLOAD_COLOR = OVERLOAD_COLOR_ARMOR;
            gaugeRegenTime = ARMOR_GAUGE_REGEN_TIME;
            tempArmor = true;
        } else if (ship.getVariant().hasHullMod(II_BasePackage.TARGETING_PACKAGE)) {
            ENGINE_COLOR = ENGINE_COLOR_TARGETING;
            CONTRAIL_COLOR = CONTRAIL_COLOR_TARGETING;
            OVERLOAD_COLOR = OVERLOAD_COLOR_TARGETING;
            afterimageIntensity = 1.5f;
            sparkIntensity = 2f;
            pitchShift = 1f;
            volumeShift = 1f;
            gaugeDrainTime = TARGETING_GAUGE_DRAIN_TIME;
            tempTargeting = true;
        } else if (ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
            ENGINE_COLOR = ENGINE_COLOR_ELITE;
            CONTRAIL_COLOR = CONTRAIL_COLOR_ELITE;
            OVERLOAD_COLOR = OVERLOAD_COLOR_ELITE;
            overGaugeLevel = ELITE_OVER_GAUGE_LEVEL;
            maxOverlevel = ELITE_MAX_OVERLEVEL;
            tempElite = true;
        }

        gaugeDrainTime = stats.getSystemUsesBonus().computeEffective(gaugeDrainTime);
        gaugeRegenTime = 1f / stats.getSystemRegenBonus().computeEffective(1f / gaugeRegenTime);

        float effectLevelSquared = effectLevel * effectLevel;
        float effectOverlevel = 1f;
        if ((odData.gauge < overGaugeLevel) && !ship.getVariant().hasHullMod(II_BasePackage.ARMOR_PACKAGE)) {
            effectOverlevel = 1f / II_Util.lerp(1f / maxOverlevel, 1f, Math.max(0f, odData.gauge) / overGaugeLevel);
        }
        float effectOverlevelSquared = effectOverlevel * effectOverlevel;

        if (!ship.getFluxTracker().isOverloaded()) {
            shutdown = false;
        }

        ship.getSystem().setCooldown(Math.max(0f, II_Util.lerp(COOLDOWN_MAX * (float) Math.sqrt(effectOverlevel), COOLDOWN_MIN, odData.gauge)));

        if (state == State.IN) {
            deactivated = false;
            if (!activated) {
                Global.getSoundPlayer().playSound("ii_overdrive_activate", 1f * effectOverlevel * pitchShift, 1f * effectOverlevel * volumeShift, ship.getLocation(), ZERO);
                activated = true;
            }
        } else {
            activated = false;
        }

        if (state == State.OUT) {
            if (!deactivated) {
                Global.getSoundPlayer().playSound("ii_overdrive_deactivate", 1f * effectOverlevel * pitchShift, 1f * effectOverlevel * volumeShift, ship.getLocation(), ZERO);
                deactivated = true;
            }
        }

        if ((state == State.COOLDOWN) || (state == State.IDLE) || (odData.gauge < 0f) || shutdown) {
            boolean systemOff = true;
            if ((odData.gauge < 0f) && ((state == State.IN) || (state == State.ACTIVE) || (state == State.OUT))
                    && !ship.getFluxTracker().isOverloaded()) {
                if (ship.getVariant().hasHullMod(II_BasePackage.ARMOR_PACKAGE)) {
                    systemOff = false;

                    if (state != State.OUT) {
                        ship.getSystem().deactivate();
                        ship.getSystem().setCooldownRemaining(ship.getSystem().getCooldownRemaining());

                        ship.setWeaponGlow(0f, ENGINE_COLOR, EnumSet.of(WeaponAPI.WeaponType.BALLISTIC, WeaponAPI.WeaponType.MISSILE, WeaponAPI.WeaponType.ENERGY));
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
                    deactivated = true;

                    ship.setOverloadColor(OVERLOAD_COLOR);
                    if (ship.getVariant().hasHullMod(II_BasePackage.TARGETING_PACKAGE)) {
                        ship.getFluxTracker().beginOverloadWithTotalBaseDuration(TARGETING_OVERLOAD_DUR);
                    } else if (ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
                        ship.getFluxTracker().beginOverloadWithTotalBaseDuration(ELITE_OVERLOAD_DUR);
                    } else {
                        ship.getFluxTracker().beginOverloadWithTotalBaseDuration(OVERLOAD_DUR);
                    }

                    if (ship.getFluxTracker().showFloaty() || (ship == Global.getCombatEngine().getPlayerShip())) {
                        ship.getFluxTracker().showOverloadFloatyIfNeeded("Emergency Shutdown!", OVERLOAD_COLOR, 4f, true);
                    }
                    Global.getSoundPlayer().playSound("ii_overdrive_failure", 1f * pitchShift, 1f * effectOverlevel * volumeShift, ship.getLocation(), ZERO);

                    ship.setWeaponGlow(0f, ENGINE_COLOR, EnumSet.of(WeaponAPI.WeaponType.BALLISTIC, WeaponAPI.WeaponType.MISSILE, WeaponAPI.WeaponType.ENERGY));

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
                                0f, 0f, shipRadius, null, thickness, OVERLOAD_COLOR, coreColor);
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

                odData.gauge = 0f;
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
                stats.getProjectileSpeedMult().unmodify(id);
                stats.getWeaponTurnRateBonus().unmodify(id);
                stats.getAutofireAimAccuracy().unmodify(id);
                stats.getMaxRecoilMult().unmodify(id);
                stats.getRecoilPerShotMult().unmodify(id);
                stats.getCRLossPerSecondPercent().unmodify(id);

                /* Ham-fisted attempt to get rid of that FUCKING glow */
                ship.setWeaponGlow(0f, ENGINE_COLOR, EnumSet.of(WeaponAPI.WeaponType.BALLISTIC, WeaponAPI.WeaponType.MISSILE, WeaponAPI.WeaponType.ENERGY));

                if (ship.controlsLocked()) {
                    odData.gauge = 0f;
                } else {
                    odData.gauge += (objectiveAmount / gaugeRegenTime) * effectOverlevel;
                    if (odData.gauge > 1f) {
                        odData.gauge = 1f;
                    }
                }

                tempGauge = odData.gauge;
                return;
            }
        }

        /* WTF? */
        if (effectLevel <= 0f) {
            tempGauge = odData.gauge;
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
                stats.getMaxSpeed().modifyPercent(id, SPEED_BONUS * effectOverlevel);
                stats.getMaxSpeed().modifyFlat(id, SPEED_FLAT_BONUS.get(ship.getHullSize()) * effectOverlevel);
                stats.getMaxTurnRate().modifyPercent(id, MAX_TURN_BONUS * effectOverlevel);
                stats.getMaxTurnRate().modifyFlat(id, MAX_TURN_FLAT_BONUS.get(ship.getHullSize()) * effectOverlevel);
            }

            odData.gauge -= objectiveAmount / gaugeDrainTime;
        }

        stats.getFluxCapacity().modifyMult(id, II_Util.lerp(1f, CAPACITY_MULT, effectLevel * effectOverlevel));
        stats.getFluxDissipation().modifyMult(id, II_Util.lerp(1f, DISSIPATION_MULT, effectLevel * effectOverlevel));
        if (ship.getVariant().hasHullMod(II_BasePackage.TARGETING_PACKAGE)) {
            stats.getBallisticRoFMult().modifyPercent(id, TARGETING_ROF_BONUS * effectLevel * effectOverlevel);
            stats.getMissileRoFMult().modifyPercent(id, TARGETING_ROF_BONUS * effectLevel * effectOverlevel);
            stats.getEnergyRoFMult().modifyPercent(id, TARGETING_ROF_BONUS * effectLevel * effectOverlevel);
            stats.getProjectileSpeedMult().modifyMult(id, II_Util.lerp(1f, TARGETING_PROJ_SPEED_MULT, effectLevel * effectOverlevel));
            stats.getWeaponTurnRateBonus().modifyPercent(id, II_Util.lerp(1f, TARGETING_TURRET_TURN_MULT, effectLevel * effectOverlevel));
        } else {
            stats.getBallisticRoFMult().modifyPercent(id, ROF_BONUS * effectLevel * effectOverlevel);
            stats.getMissileRoFMult().modifyPercent(id, ROF_BONUS * effectLevel * effectOverlevel);
            stats.getEnergyRoFMult().modifyPercent(id, ROF_BONUS * effectLevel * effectOverlevel);
            stats.getAcceleration().modifyPercent(id, ACCEL_BONUS * effectLevelSquared * effectOverlevel);
            stats.getAcceleration().modifyFlat(id, ACCEL_FLAT_BONUS * effectLevelSquared * effectOverlevel);
            stats.getDeceleration().modifyPercent(id, DECEL_BONUS * effectLevelSquared * effectOverlevel);
            stats.getDeceleration().modifyFlat(id, DECEL_FLAT_BONUS * effectLevelSquared * effectOverlevel);
            stats.getTurnAcceleration().modifyPercent(id, TURN_ACCEL_BONUS * effectLevelSquared * effectOverlevel);
        }
        stats.getAutofireAimAccuracy().modifyFlat(id, AUTOAIM_BONUS * effectLevel * effectOverlevel);
        stats.getMaxRecoilMult().modifyMult(id, II_Util.lerp(1f, RECOIL_MULT, effectLevel * effectOverlevel));
        stats.getRecoilPerShotMult().modifyMult(id, II_Util.lerp(1f, RECOIL_MULT, effectLevel * effectOverlevel));

        if (ship.getVariant().hasHullMod(II_BasePackage.ARMOR_PACKAGE)) {
            totalPeakTimeLoss += (ARMOR_CR_LOSS_MULT - 1f) * effectLevel * effectOverlevelSquared * objectiveAmount;
            stats.getCRLossPerSecondPercent().modifyMult(id, II_Util.lerp(1f, ARMOR_CR_LOSS_MULT, effectLevel * effectOverlevel));
            stats.getPeakCRDuration().modifyFlat(id, -totalPeakTimeLoss / ship.getMutableStats().getPeakCRDuration().getMult());
        } else if (ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
            if (effectOverlevelSquared > 1f) {
                totalPeakTimeLoss += (ELITE_OVERLEVEL_CR_LOSS_MULT - 1f) * effectLevel * (effectOverlevelSquared - 1f) * objectiveAmount;
                stats.getCRLossPerSecondPercent().modifyMult(id, II_Util.lerp(1f, ELITE_OVERLEVEL_CR_LOSS_MULT, effectLevel * (effectOverlevel - 1f)));
                stats.getPeakCRDuration().modifyFlat(id, -totalPeakTimeLoss / ship.getMutableStats().getPeakCRDuration().getMult());
            }
        } else {
            totalPeakTimeLoss += (CR_LOSS_MULT - 1f) * effectLevel * effectOverlevelSquared * objectiveAmount;
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

        Color glowColor = new Color(II_Util.clamp255(Math.round((3f * OVERLOAD_COLOR.getRed() + 255f) / 4f)),
                II_Util.clamp255(Math.round((3f * OVERLOAD_COLOR.getGreen() + 255f) / 4f)),
                II_Util.clamp255(Math.round((3f * OVERLOAD_COLOR.getBlue() + 255f) / 4f)),
                255);
        ship.setWeaponGlow(effectLevel, glowColor, EnumSet.of(WeaponAPI.WeaponType.BALLISTIC, WeaponAPI.WeaponType.MISSILE, WeaponAPI.WeaponType.ENERGY));

        if (!Global.getCombatEngine().isPaused()) {
            Global.getSoundPlayer().playLoop("ii_overdrive_loop", ship, 1f * effectOverlevel * pitchShift, 1f * effectOverlevel * effectLevel * volumeShift, ship.getLocation(), ZERO);
        }

        interval.advance(amount);
        if (interval.intervalElapsed()) {
            if (ship.getVariant().hasHullMod(II_BasePackage.TARGETING_PACKAGE)) {
                float randRange = (float) shipRadius * 0.25f * afterimageIntensity;
                float randSpeed = (float) shipRadius * 1f * afterimageIntensity;
                float randAngle = MathUtils.getRandomNumberInRange(0f, 360f);
                float randRadiusFrac = (float) (Math.random() + Math.random());
                randRadiusFrac = (randRadiusFrac > 1f ? 2f - randRadiusFrac : randRadiusFrac);
                Vector2f randLoc = MathUtils.getPointOnCircumference(ZERO, randRange * randRadiusFrac, randAngle);
                Vector2f randVel = MathUtils.getRandomPointInCircle(ZERO, randSpeed * MathUtils.getRandomNumberInRange(0.5f, 1f));
                Color afterimageColor = new Color(OVERLOAD_COLOR.getRed(), OVERLOAD_COLOR.getGreen(), OVERLOAD_COLOR.getBlue(),
                        II_Util.clamp255(Math.round(0.1f * afterimageIntensity * effectLevel * (float) Math.sqrt(effectOverlevel) * CONTRAIL_COLOR.getAlpha())));
                ship.addAfterimage(afterimageColor, randLoc.x, randLoc.y, randVel.x, randVel.y,
                        randRange, 0f, 0.1f, 0.3f * effectLevel, true, false, false);
                Color afterimageColor2 = new Color(OVERLOAD_COLOR.getRed(), OVERLOAD_COLOR.getGreen(), OVERLOAD_COLOR.getBlue(),
                        II_Util.clamp255(Math.round(0.05f * afterimageIntensity * effectLevel * (float) Math.sqrt(effectOverlevel) * CONTRAIL_COLOR.getAlpha())));
                ship.addAfterimage(afterimageColor2, randLoc.x, randLoc.y, randVel.x, randVel.y,
                        randRange, 0f, 0.1f, 0.3f * effectLevel, true, false, true);
            } else {
                float randRange = (float) shipRadius * 0.2f * afterimageIntensity;
                float randSpeed = (float) shipRadius * 0.5f * afterimageIntensity;
                float randAngle = MathUtils.getRandomNumberInRange(0f, 360f);
                float randRadiusFrac = (float) (Math.random() + Math.random());
                randRadiusFrac = (randRadiusFrac > 1f ? 2f - randRadiusFrac : randRadiusFrac);
                Vector2f randLoc = MathUtils.getPointOnCircumference(ZERO, randRange * randRadiusFrac, randAngle);
                Vector2f randVel = MathUtils.getRandomPointInCircle(ZERO, randSpeed * MathUtils.getRandomNumberInRange(0.5f, 1f));
                Color afterimageColor = new Color(OVERLOAD_COLOR.getRed(), OVERLOAD_COLOR.getGreen(), OVERLOAD_COLOR.getBlue(),
                        II_Util.clamp255(Math.round(0.1f * afterimageIntensity * effectLevel * (float) Math.sqrt(effectOverlevel) * CONTRAIL_COLOR.getAlpha())));
                ship.addAfterimage(afterimageColor, randLoc.x, randLoc.y, randVel.x, randVel.y,
                        randRange, 0f, 0.1f, 0.3f * effectLevel, true, false, false);

                randRange = (float) Math.sqrt(shipRadius) * 0.75f * afterimageIntensity;
                randLoc = MathUtils.getRandomPointInCircle(ZERO, randRange);
                Color afterimageColor3 = new Color(OVERLOAD_COLOR.getRed(), OVERLOAD_COLOR.getGreen(), OVERLOAD_COLOR.getBlue(),
                        II_Util.clamp255(Math.round(0.15f * afterimageIntensity * effectLevel * (float) Math.sqrt(effectOverlevel) * CONTRAIL_COLOR.getAlpha())));
                ship.addAfterimage(afterimageColor3, randLoc.x, randLoc.y, -ship.getVelocity().x, -ship.getVelocity().y,
                        randRange, 0f, 0.1f, 0.7f * effectLevel, true, false, false);
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
                            0f, 0f, sparkLen * 2f, null, thickness, OVERLOAD_COLOR, coreColor);
                }
            }
        }

        tempGauge = odData.gauge;
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
        float overGaugeLevel = OVER_GAUGE_LEVEL;
        boolean isArmor = false;
        boolean isElite = false;
        if ((ship != null) && ship.getVariant().hasHullMod(II_BasePackage.ARMOR_PACKAGE)) {
            overGaugeLevel = ARMOR_OVER_GAUGE_LEVEL;
            isArmor = true;
        }
        if ((ship != null) && ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
            overGaugeLevel = ELITE_OVER_GAUGE_LEVEL;
            isElite = true;
        }
        float gauge = getGauge(ship);

        int displayGauge = Math.round(100f * Math.max(0f, gauge));
        if (shutdown) {
            return "SHUTDOWN";
        }
        if ((gauge < overGaugeLevel) && system.isOn() && !isArmor) {
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
            return "" + displayGauge + "% - ACTIVE";
        }
        return "" + displayGauge + "% - READY";
    }

    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        float overGaugeLevel = OVER_GAUGE_LEVEL;
        float maxOverlevel = MAX_OVERLEVEL;
        if (tempElite) {
            overGaugeLevel = ELITE_OVER_GAUGE_LEVEL;
            maxOverlevel = ELITE_MAX_OVERLEVEL;
        }

        float effectOverlevel = 1f;
        if ((tempGauge < overGaugeLevel) && !tempArmor) {
            effectOverlevel = 1f / II_Util.lerp(1f / maxOverlevel, 1f, Math.max(0f, tempGauge) / overGaugeLevel);
        }
        float effectOverlevelSquared = effectOverlevel * effectOverlevel;

        switch (index) {
            case 0:
                if ((state == State.IN) || (state == State.ACTIVE) || (state == State.OUT)) {
                    return new StatusData("flux capacity +" + Math.round((II_Util.lerp(1f, CAPACITY_MULT, effectLevel * effectOverlevel) - 1f) * 100f) + "%", false);
                }
                break;
            case 1:
                if ((state == State.IN) || (state == State.ACTIVE) || (state == State.OUT)) {
                    return new StatusData("flux dissipation +" + Math.round((II_Util.lerp(1f, DISSIPATION_MULT, effectLevel * effectOverlevel) - 1f) * 100f) + "%", false);
                }
                break;
            case 2:
                if (tempTargeting) {
                    if ((state == State.IN) || (state == State.ACTIVE) || (state == State.OUT)) {
                        return new StatusData("rate of fire +" + Math.round(TARGETING_ROF_BONUS * effectLevel * effectOverlevel) + "%", false);
                    }
                } else {
                    if ((state == State.IN) || (state == State.ACTIVE) || (state == State.OUT)) {
                        return new StatusData("rate of fire +" + Math.round(ROF_BONUS * effectLevel * effectOverlevel) + "%", false);
                    }
                }
                break;
            case 3:
                if (tempTargeting) {
                    if ((state == State.IN) || (state == State.ACTIVE) || (state == State.OUT)) {
                        return new StatusData("projectile speed +" + Math.round((II_Util.lerp(1f, TARGETING_PROJ_SPEED_MULT, effectLevel * effectOverlevel) - 1f) * 100f) + "%", false);
                    }
                } else {
                    if ((state == State.IN) || (state == State.ACTIVE) || (state == State.OUT)) {
                        return new StatusData("improved maneuverability", false);
                    }
                }
                break;
            case 4:
                if (tempTargeting) {
                    if ((state == State.IN) || (state == State.ACTIVE) || (state == State.OUT)) {
                        return new StatusData("increased weapon turn rate", false);
                    }
                } else {
                    if ((state == State.IN) || (state == State.ACTIVE)) {
                        return new StatusData("increased engine power", false);
                    }
                }
                break;
            case 5:
                if ((state == State.IN) || (state == State.ACTIVE) || (state == State.OUT)) {
                    return new StatusData("weapon accuracy +" + Math.round((1f - II_Util.lerp(1f, RECOIL_MULT, effectLevel * effectOverlevel)) * 100f) + "%", false);
                }
                break;
            case 6:
                if (tempArmor) {
                    if ((state == State.IN) || (state == State.ACTIVE) || (state == State.OUT)) {
                        return new StatusData("CR degradation +" + Math.round((II_Util.lerp(1f, ARMOR_CR_LOSS_MULT, effectLevel * effectOverlevelSquared) - 1f) * 100f) + "%", true);
                    }
                } else if (tempElite) {
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
        if (data instanceof OverdriveData) {
            OverdriveData odData = (OverdriveData) data;

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

        float overGaugeLevel = OVER_GAUGE_LEVEL;
        float maxOverlevel = MAX_OVERLEVEL;
        if (ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
            overGaugeLevel = ELITE_OVER_GAUGE_LEVEL;
            maxOverlevel = ELITE_MAX_OVERLEVEL;
        }
        float gauge = getGauge(ship);

        float effectOverlevel = 1f;
        if ((gauge < overGaugeLevel) && (gauge >= 0f) && !ship.getVariant().hasHullMod(II_BasePackage.ARMOR_PACKAGE)) {
            effectOverlevel = 1f / II_Util.lerp(1f / maxOverlevel, 1f, gauge / overGaugeLevel);
        }
        return effectOverlevel;
    }

    public static boolean isUsable(ShipAPI ship, ShipSystemAPI system) {
        if ((ship == null) || (system == null)) {
            return false;
        }

        float overGaugeLevel = OVER_GAUGE_LEVEL;
        boolean isElite = false;
        if (ship.getVariant().hasHullMod(II_BasePackage.ARMOR_PACKAGE)) {
            overGaugeLevel = ARMOR_OVER_GAUGE_LEVEL;
        }
        if (ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
            overGaugeLevel = ELITE_OVER_GAUGE_LEVEL;
            isElite = true;
        }
        float gauge = getGauge(ship);

        return !((gauge < overGaugeLevel) && !system.isActive() && !isElite);
    }

    private static class OverdriveData {

        final Object stateKey;
        float gauge;

        OverdriveData(Object stateKey) {
            this.stateKey = stateKey;
        }
    }
}
