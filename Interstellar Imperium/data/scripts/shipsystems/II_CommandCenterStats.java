package data.scripts.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import data.scripts.hullmods.II_BasePackage;
import data.scripts.util.II_Util;
import java.awt.Color;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class II_CommandCenterStats extends BaseShipSystemScript {

    private static final Color REFIT_COLOR_STANDARD = new Color(255, 150, 50, 50);
    private static final Color REFIT_JITTER_COLOR_STANDARD = new Color(255, 150, 50, 100);
    private static final Color REFIT_COLOR_ARMOR = new Color(255, 200, 50, 75);
    private static final Color REFIT_UNDER_COLOR_ARMOR = new Color(255, 200, 50, 100);
    private static final Color REFIT_JITTER_COLOR_ARMOR = new Color(255, 200, 50, 140);

    private static final Color SPEED_COLOR_STANDARD = new Color(10, 50, 255, 120);
    private static final Color SPEED_UNDER_COLOR_STANDARD = new Color(10, 50, 255, 60);
    private static final Color SPEED_ENGINE_COLOR_STANDARD = new Color(100, 150, 255, 180);
    private static final Color SPEED_CONTRAIL_COLOR_STANDARD = new Color(40, 50, 80, 60);
    private static final Color SPEED_JITTER_COLOR_STANDARD = new Color(10, 50, 255, 160);
    private static final Color SPEED_COLOR_TARGETING = new Color(50, 100, 255, 120);
    private static final Color SPEED_UNDER_COLOR_TARGETING = new Color(50, 100, 255, 60);
    private static final Color SPEED_ENGINE_COLOR_TARGETING = new Color(100, 175, 255, 200);
    private static final Color SPEED_CONTRAIL_COLOR_TARGETING = new Color(45, 60, 80, 70);
    private static final Color SPEED_JITTER_COLOR_TARGETING = new Color(50, 100, 255, 190);

    private static final Color ATTACK_COLOR_STANDARD = new Color(255, 50, 10, 50);
    private static final Color ATTACK_UNDER_COLOR_STANDARD = new Color(255, 50, 10, 100);
    private static final Color ATTACK_JITTER_COLOR_STANDARD = new Color(255, 50, 10, 110);
    private static final Color ATTACK_COLOR_ELITE = new Color(255, 50, 255, 40);
    private static final Color ATTACK_UNDER_COLOR_ELITE = new Color(255, 50, 255, 90);
    private static final Color ATTACK_JITTER_COLOR_ELITE = new Color(255, 50, 255, 130);

    private static final float REFIT_RATE_DECREASE = 25f;
    private static final float REFIT_RATE_INCREASE = 25f;
    private static final float REFIT_TIME_DECREASE = 25f;
    private static final float SPEED_MANEUVERABILITY_INCREASE = 50f;
    private static final float SPEED_SPEED_INCREASE = 25f;
    private static final float ATTACK_DAMAGE_INCREASE = (1f / 3f) * 100f;
    private static final float ARMOR_REFIT_DAMAGE_REDUCTION = 25f;
    private static final float TARGETING_SPEED_FLAT_RANGE_BONUS = 250f;
    private static final float TARGETING_SPEED_PROJECTILE_SPEED_INCREASE = 50f;
    private static final float ELITE_ATTACK_ROF_INCREASE = 25f;
    private static final float ELITE_ATTACK_FLUX_USE_REDUCTION = 20f;

    private static final Object REFIT_KEY = new Object();
    private static final Object SPEED_KEY = new Object();
    private static final Object ATTACK_KEY = new Object();

    private static final float RAMP_UP_TIME = 0.5f;

    private static final Map<ShipAPI, CCMode> currMode = new WeakHashMap<>();

    public static CCMode getMode(ShipAPI ship) {
        return currMode.get(ship);
    }

    private static List<ShipAPI> getFighters(ShipAPI carrier) {
        List<ShipAPI> result = new ArrayList<>(30);

        for (ShipAPI ship : Global.getCombatEngine().getShips()) {
            if (!ship.isFighter()) {
                continue;
            }
            if (ship.getWing() == null) {
                continue;
            }
            if (ship.getWing().getSourceShip() == carrier) {
                result.add(ship);
            }
        }

        return result;
    }

    private boolean changed = false;
    private float level = 0f;
    private CCMode mode = CCMode.REFIT;
    private final IntervalUtil particleInterval = new IntervalUtil(0.1f, 0.15f);
    private boolean isVentOrOverload = false;
    private boolean isArmor = false;
    private boolean isTargeting = false;
    private boolean isElite = false;
    private boolean refitApplied = false;
    private boolean speedApplied = false;
    private boolean attackApplied = false;

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        ShipAPI ship = (ShipAPI) stats.getEntity();
        if (ship == null) {
            return;
        }

        isVentOrOverload = ship.getFluxTracker().isOverloadedOrVenting();

        if (!ship.isAlive()) {
            unapplyAll(stats, id);
            return;
        }

        float objectiveAmount = Global.getCombatEngine().getElapsedInLastFrame();
        objectiveAmount *= Global.getCombatEngine().getTimeMult().getModifiedValue();
        if (Global.getCombatEngine().isPaused()) {
            objectiveAmount = 0f;
        }

        if (state == State.IN || state == State.ACTIVE || state == State.OUT) {
            if (!changed) {
                changed = true;
                switch (mode) {
                    default:
                    case REFIT:
                        mode = CCMode.SPEED;
                        break;
                    case SPEED:
                        mode = CCMode.ATTACK;
                        break;
                    case ATTACK:
                        mode = CCMode.REFIT;
                        break;
                }
                level = 0f;
                if (ship == Global.getCombatEngine().getPlayerShip()) {
                    Global.getSoundPlayer().playUISound(mode.soundId, 1f, 1f);
                } else {
                    Global.getSoundPlayer().playSound(mode.otherSoundId, 1f, 0.5f, ship.getLocation(), ship.getVelocity());
                }
            }
        } else {
            changed = false;
        }

        Color JITTER_COLOR;
        float underIntensity = 1f;
        float idleIntensity = 0f;
        isArmor = false;
        isTargeting = false;
        isElite = false;
        switch (mode) {
            default:
            case REFIT:
                JITTER_COLOR = REFIT_JITTER_COLOR_STANDARD;
                break;
            case SPEED:
                JITTER_COLOR = SPEED_JITTER_COLOR_STANDARD;
                break;
            case ATTACK:
                JITTER_COLOR = ATTACK_JITTER_COLOR_STANDARD;
                break;
        }
        if (ship.getVariant().hasHullMod(II_BasePackage.ARMOR_PACKAGE) && (mode == CCMode.REFIT)) {
            JITTER_COLOR = REFIT_JITTER_COLOR_ARMOR;
            underIntensity = 1.25f;
            idleIntensity = 1f;
            isArmor = true;
        } else if (ship.getVariant().hasHullMod(II_BasePackage.TARGETING_PACKAGE) && (mode == CCMode.SPEED)) {
            JITTER_COLOR = SPEED_JITTER_COLOR_TARGETING;
            underIntensity = 1.25f;
            idleIntensity = 1f;
            isTargeting = true;
        } else if (ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE) && (mode == CCMode.ATTACK)) {
            JITTER_COLOR = ATTACK_JITTER_COLOR_ELITE;
            underIntensity = 1.25f;
            idleIntensity = 1f;
            isElite = true;
        }

        if ((state == State.IDLE) && !ship.getFluxTracker().isOverloadedOrVenting() && (ship.getCurrentCR() > 0f)) {
            level = Math.min(1f, level + (objectiveAmount / RAMP_UP_TIME));

            switch (mode) {
                default:
                case REFIT:
                    applyRefit(stats, id);
                    unapplySpeed(stats, id);
                    unapplyAttack(stats, id);
                    break;
                case SPEED:
                    applySpeed(stats, id);
                    unapplyRefit(stats, id);
                    unapplyAttack(stats, id);
                    break;
                case ATTACK:
                    applyAttack(stats, id);
                    unapplyRefit(stats, id);
                    unapplySpeed(stats, id);
                    break;
            }
        } else {
            level = 0f;

            unapplyAll(stats, id);
        }

        if ((state == State.IN) || (state == State.ACTIVE) || (state == State.OUT)) {
            float jitterUnderIntensity = 0.75f * underIntensity * effectLevel;
            ship.setJitterUnder(this,
                    new Color(JITTER_COLOR.getRed(), JITTER_COLOR.getGreen(), JITTER_COLOR.getBlue(),
                            II_Util.clamp255(Math.round(JITTER_COLOR.getAlpha() * jitterUnderIntensity))),
                    1f, Math.round(15 * underIntensity), 0f, 15f * underIntensity * (float) Math.sqrt(jitterUnderIntensity));
        } else if ((idleIntensity > 0f) && (level > 0f)) {
            float jitterUnderIntensity = 0.4f * idleIntensity * level;
            ship.setJitterUnder(this,
                    new Color(JITTER_COLOR.getRed(), JITTER_COLOR.getGreen(), JITTER_COLOR.getBlue(),
                            II_Util.clamp255(Math.round(JITTER_COLOR.getAlpha() * jitterUnderIntensity))),
                    1f, Math.round(10 * idleIntensity), 0f, 15f * idleIntensity * (float) Math.sqrt(jitterUnderIntensity));
        }

        currMode.put(ship, mode);
    }

    @Override
    public String getInfoText(ShipSystemAPI system, ShipAPI ship) {
        return mode.name;
    }

    @Override
    public boolean isUsable(ShipSystemAPI system, ShipAPI ship) {
        if (ship != null) {
            if (ship.getFluxTracker().isOverloadedOrVenting()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        if ((state == State.IDLE) && !isVentOrOverload) {
            switch (mode) {
                default:
                case REFIT:
                    switch (index) {
                        case 0:
                            return new StatusData("+" + Math.round(REFIT_RATE_INCREASE * level) + "% replacement rate recovery", false);
                        case 1:
                            return new StatusData("-" + Math.round(REFIT_RATE_DECREASE * level) + "% replacement rate drain", false);
                        case 2:
                            return new StatusData("-" + Math.round(REFIT_TIME_DECREASE * level) + "% fighter refit time", false);
                        case 3:
                            if (isArmor) {
                                return new StatusData("-" + Math.round(ARMOR_REFIT_DAMAGE_REDUCTION * level) + "% fighter damage taken", false);
                            }
                        default:
                            break;
                    }
                    return null;
                case SPEED:
                    switch (index) {
                        case 0:
                            return new StatusData("+" + Math.round(SPEED_SPEED_INCREASE * level) + "% fighter speed", false);
                        case 1:
                            return new StatusData("+" + Math.round(SPEED_MANEUVERABILITY_INCREASE * level) + "% fighter maneuverability", false);
                        case 2:
                            if (isTargeting) {
                                return new StatusData("+" + Math.round(TARGETING_SPEED_FLAT_RANGE_BONUS * level) + " fighter projectile range", false);
                            }
                        default:
                            break;
                    }
                    return null;
                case ATTACK:
                    switch (index) {
                        case 0:
                            return new StatusData("+" + Math.round(ATTACK_DAMAGE_INCREASE * level) + "% fighter damage", false);
                        case 1:
                            if (isElite) {
                                return new StatusData("+" + Math.round(ELITE_ATTACK_ROF_INCREASE * level) + "% fighter rate of fire", false);
                            }
                        case 2:
                            if (isElite) {
                                return new StatusData("-" + Math.round(ELITE_ATTACK_FLUX_USE_REDUCTION * level) + "% fighter flux use", false);
                            }
                        default:
                            break;
                    }
                    return null;
            }
        }

        return null;
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        unapplyAll(stats, id);
    }

    private void applyRefit(MutableShipStatsAPI stats, String id) {
        ShipAPI ship = (ShipAPI) stats.getEntity();
        if (ship == null) {
            return;
        }

        stats.getFighterRefitTimeMult().modifyMult(id, 1f - ((REFIT_TIME_DECREASE * level) / 100f));
        stats.getDynamic().getStat(Stats.REPLACEMENT_RATE_INCREASE_MULT).modifyPercent(id, REFIT_RATE_INCREASE * level);
        stats.getDynamic().getStat(Stats.REPLACEMENT_RATE_DECREASE_MULT).modifyMult(id, 1f - ((REFIT_RATE_DECREASE * level) / 100f));

        if (Global.getCombatEngine().isPaused()) {
            return;
        }

        Color REFIT_COLOR = REFIT_COLOR_STANDARD;
        float effectScale = 1f;
        if (ship.getVariant().hasHullMod(II_BasePackage.ARMOR_PACKAGE)) {
            REFIT_COLOR = REFIT_COLOR_ARMOR;
            effectScale = 1.25f;
        }

        float amount = Global.getCombatEngine().getElapsedInLastFrame();
        particleInterval.advance(amount);
        if (particleInterval.intervalElapsed()) {
            for (WeaponSlotAPI slot : ship.getHullSpec().getAllWeaponSlotsCopy()) {
                if (slot.getWeaponType() != WeaponType.LAUNCH_BAY) {
                    continue;
                }

                if (Math.random() > 0.33) {
                    continue;
                }

                Vector2f loc = slot.computePosition(ship);
                if (loc == null) {
                    continue;
                }

                float size = MathUtils.getRandomNumberInRange(60f, 85f) * effectScale;
                float brightness = MathUtils.getRandomNumberInRange(0.75f, 1f) * level * effectScale;
                float duration = MathUtils.getRandomNumberInRange(0.8f, 1.2f);
                Global.getCombatEngine().addSmoothParticle(loc, ship.getVelocity(), size, brightness, duration, REFIT_COLOR);
            }
        }

        if (ship.getVariant().hasHullMod(II_BasePackage.ARMOR_PACKAGE)) {
            float jitterLevel = level;
            float jitterRangeBonus = jitterLevel * 4f * effectScale;
            float jitterUnderRangeBonus = jitterLevel * 5.5f * effectScale;
            for (ShipAPI fighter : getFighters(ship)) {
                if (fighter.isHulk()) {
                    continue;
                }
                MutableShipStatsAPI fStats = fighter.getMutableStats();
                fStats.getHullDamageTakenMult().modifyMult(id, 1f - ((ARMOR_REFIT_DAMAGE_REDUCTION * level) / 100f));
                fStats.getArmorDamageTakenMult().modifyMult(id, 1f - ((ARMOR_REFIT_DAMAGE_REDUCTION * level) / 100f));
                fStats.getShieldDamageTakenMult().modifyMult(id, 1f - ((ARMOR_REFIT_DAMAGE_REDUCTION * level) / 100f));

                fighter.setJitterUnder(REFIT_KEY, REFIT_UNDER_COLOR_ARMOR, jitterLevel, Math.round(5 * effectScale), 0f, jitterUnderRangeBonus);
                fighter.setJitter(REFIT_KEY, REFIT_COLOR, jitterLevel, Math.round(1 * effectScale), 0f, jitterRangeBonus);
            }
        }

        refitApplied = true;
    }

    private void applySpeed(MutableShipStatsAPI stats, String id) {
        ShipAPI ship = (ShipAPI) stats.getEntity();
        if (ship == null) {
            return;
        }

        Color SPEED_COLOR = SPEED_COLOR_STANDARD;
        Color SPEED_UNDER_COLOR = SPEED_UNDER_COLOR_STANDARD;
        Color SPEED_ENGINE_COLOR = SPEED_ENGINE_COLOR_STANDARD;
        Color SPEED_CONTRAIL_COLOR = SPEED_CONTRAIL_COLOR_STANDARD;
        float effectScale = 1f;
        if (ship.getVariant().hasHullMod(II_BasePackage.TARGETING_PACKAGE)) {
            SPEED_COLOR = SPEED_COLOR_TARGETING;
            SPEED_UNDER_COLOR = SPEED_UNDER_COLOR_TARGETING;
            SPEED_ENGINE_COLOR = SPEED_ENGINE_COLOR_TARGETING;
            SPEED_CONTRAIL_COLOR = SPEED_CONTRAIL_COLOR_TARGETING;
            effectScale = 1.25f;
        }

        float jitterLevel = level;
        float jitterRangeBonus = jitterLevel * 7f * effectScale;
        for (ShipAPI fighter : getFighters(ship)) {
            if (fighter.isHulk()) {
                continue;
            }
            MutableShipStatsAPI fStats = fighter.getMutableStats();
            fStats.getMaxSpeed().modifyPercent(id, SPEED_SPEED_INCREASE * level);
            fStats.getMaxTurnRate().modifyPercent(id, SPEED_MANEUVERABILITY_INCREASE * level);
            fStats.getAcceleration().modifyPercent(id, SPEED_MANEUVERABILITY_INCREASE * level);
            fStats.getDeceleration().modifyPercent(id, SPEED_MANEUVERABILITY_INCREASE * level);
            fStats.getTurnAcceleration().modifyPercent(id, SPEED_MANEUVERABILITY_INCREASE * level);
            if (ship.getVariant().hasHullMod(II_BasePackage.TARGETING_PACKAGE)) {
                fStats.getBallisticWeaponRangeBonus().modifyFlat(id, TARGETING_SPEED_FLAT_RANGE_BONUS * level);
                fStats.getEnergyWeaponRangeBonus().modifyFlat(id, TARGETING_SPEED_FLAT_RANGE_BONUS * level);
                fStats.getBeamWeaponRangeBonus().modifyFlat(id, -TARGETING_SPEED_FLAT_RANGE_BONUS * level);
                fStats.getProjectileSpeedMult().modifyPercent(id, TARGETING_SPEED_PROJECTILE_SPEED_INCREASE * level);
            }

            fighter.getEngineController().fadeToOtherColor(SPEED_KEY, SPEED_ENGINE_COLOR, SPEED_CONTRAIL_COLOR, level * effectScale, 0.5f);
            fighter.getEngineController().extendFlame(SPEED_KEY, 0.25f * effectScale, 0.25f * effectScale, 0.1f * effectScale);
            fighter.setJitterUnder(SPEED_KEY, SPEED_COLOR, jitterLevel, Math.round(5 * effectScale), 0f, jitterRangeBonus);
            fighter.setJitter(SPEED_KEY, SPEED_UNDER_COLOR, jitterLevel, Math.round(2 * effectScale), 0f, jitterRangeBonus);
        }

        speedApplied = true;
    }

    private void applyAttack(MutableShipStatsAPI stats, String id) {
        ShipAPI ship = (ShipAPI) stats.getEntity();
        if (ship == null) {
            return;
        }

        Color ATTACK_COLOR = ATTACK_COLOR_STANDARD;
        Color ATTACK_UNDER_COLOR = ATTACK_UNDER_COLOR_STANDARD;
        float effectScale = 1f;
        if (ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
            ATTACK_COLOR = ATTACK_COLOR_ELITE;
            ATTACK_UNDER_COLOR = ATTACK_UNDER_COLOR_ELITE;
            effectScale = 1.25f;
        }

        float jitterLevel = level;
        float jitterRangeBonus = jitterLevel * 5f * effectScale;
        for (ShipAPI fighter : getFighters(ship)) {
            if (fighter.isHulk()) {
                continue;
            }
            MutableShipStatsAPI fStats = fighter.getMutableStats();
            fStats.getBallisticWeaponDamageMult().modifyMult(id, 1f + ((ATTACK_DAMAGE_INCREASE * level) / 100f));
            fStats.getEnergyWeaponDamageMult().modifyMult(id, 1f + ((ATTACK_DAMAGE_INCREASE * level) / 100f));
            fStats.getMissileWeaponDamageMult().modifyMult(id, 1f + ((ATTACK_DAMAGE_INCREASE * level) / 100f));
            if (ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
                fStats.getBallisticRoFMult().modifyMult(id, 1f + ((ELITE_ATTACK_ROF_INCREASE * level) / 100f));
                fStats.getEnergyRoFMult().modifyMult(id, 1f + ((ELITE_ATTACK_ROF_INCREASE * level) / 100f));
                fStats.getMissileRoFMult().modifyMult(id, 1f + ((ELITE_ATTACK_ROF_INCREASE * level) / 100f));
                fStats.getBallisticWeaponFluxCostMod().modifyMult(id, 1f - ((ELITE_ATTACK_FLUX_USE_REDUCTION * level) / 100f));
                fStats.getEnergyWeaponFluxCostMod().modifyMult(id, 1f - ((ELITE_ATTACK_FLUX_USE_REDUCTION * level) / 100f));
                fStats.getMissileWeaponFluxCostMod().modifyMult(id, 1f - ((ELITE_ATTACK_FLUX_USE_REDUCTION * level) / 100f));
            }

            fighter.setWeaponGlow(level, Misc.setAlpha(ATTACK_COLOR, Math.min(255, Math.round(ATTACK_COLOR.getAlpha() * 2f))), EnumSet.allOf(WeaponType.class));
            fighter.setJitterUnder(ATTACK_KEY, ATTACK_COLOR, jitterLevel, Math.round(5 * effectScale), 0f, jitterRangeBonus);
            fighter.setJitter(ATTACK_KEY, ATTACK_UNDER_COLOR, jitterLevel, Math.round(2 * effectScale), 0f, jitterRangeBonus);
        }

        attackApplied = true;
    }

    private void unapplyRefit(MutableShipStatsAPI stats, String id) {
        ShipAPI ship = (ShipAPI) stats.getEntity();
        if (ship == null) {
            return;
        }

        if (refitApplied) {
            stats.getFighterRefitTimeMult().unmodify(id);
            stats.getDynamic().getStat(Stats.REPLACEMENT_RATE_INCREASE_MULT).unmodify(id);
            stats.getDynamic().getStat(Stats.REPLACEMENT_RATE_DECREASE_MULT).unmodify(id);

            for (ShipAPI fighter : getFighters(ship)) {
                if (fighter.isHulk()) {
                    continue;
                }
                MutableShipStatsAPI fStats = fighter.getMutableStats();
                fStats.getHullDamageTakenMult().unmodify(id);
                fStats.getArmorDamageTakenMult().unmodify(id);
                fStats.getShieldDamageTakenMult().unmodify(id);
            }
        }

        refitApplied = false;
    }

    private void unapplySpeed(MutableShipStatsAPI stats, String id) {
        ShipAPI ship = (ShipAPI) stats.getEntity();
        if (ship == null) {
            return;
        }

        if (speedApplied) {
            for (ShipAPI fighter : getFighters(ship)) {
                if (fighter.isHulk()) {
                    continue;
                }
                MutableShipStatsAPI fStats = fighter.getMutableStats();
                fStats.getMaxSpeed().unmodify(id);
                fStats.getMaxTurnRate().unmodify(id);
                fStats.getAcceleration().unmodify(id);
                fStats.getDeceleration().unmodify(id);
                fStats.getTurnAcceleration().unmodify(id);
                fStats.getBallisticWeaponRangeBonus().unmodify(id);
                fStats.getEnergyWeaponRangeBonus().unmodify(id);
                fStats.getBeamWeaponRangeBonus().unmodify(id);
                fStats.getProjectileSpeedMult().unmodify(id);
            }
        }

        speedApplied = false;
    }

    private void unapplyAttack(MutableShipStatsAPI stats, String id) {
        ShipAPI ship = (ShipAPI) stats.getEntity();
        if (ship == null) {
            return;
        }

        if (attackApplied) {
            for (ShipAPI fighter : getFighters(ship)) {
                if (fighter.isHulk()) {
                    continue;
                }
                MutableShipStatsAPI fStats = fighter.getMutableStats();
                fStats.getBallisticWeaponDamageMult().unmodify(id);
                fStats.getEnergyWeaponDamageMult().unmodify(id);
                fStats.getMissileWeaponDamageMult().unmodify(id);
                fStats.getBallisticRoFMult().unmodify(id);
                fStats.getEnergyRoFMult().unmodify(id);
                fStats.getMissileRoFMult().unmodify(id);
                fStats.getBallisticWeaponFluxCostMod().unmodify(id);
                fStats.getEnergyWeaponFluxCostMod().unmodify(id);
                fStats.getMissileWeaponFluxCostMod().unmodify(id);
            }
        }

        attackApplied = false;
    }

    private void unapplyAll(MutableShipStatsAPI stats, String id) {
        ShipAPI ship = (ShipAPI) stats.getEntity();
        if (ship == null) {
            return;
        }

        for (ShipAPI fighter : getFighters(ship)) {
            if (refitApplied) {
                fighter.setJitterUnder(REFIT_KEY, REFIT_COLOR_STANDARD, 0f, 5, 0f, 0f);
                fighter.setJitter(REFIT_KEY, REFIT_UNDER_COLOR_ARMOR, 0f, 2, 0f, 0f);
            }
            if (speedApplied) {
                fighter.setJitterUnder(SPEED_KEY, SPEED_COLOR_STANDARD, 0f, 5, 0f, 0f);
                fighter.setJitter(SPEED_KEY, SPEED_UNDER_COLOR_STANDARD, 0f, 2, 0f, 0f);
            }
            if (attackApplied) {
                fighter.setWeaponGlow(0f, Misc.setAlpha(ATTACK_UNDER_COLOR_STANDARD, 255), EnumSet.allOf(WeaponType.class));
                fighter.setJitterUnder(ATTACK_KEY, ATTACK_COLOR_STANDARD, 0f, 5, 0f, 0f);
                fighter.setJitter(ATTACK_KEY, ATTACK_UNDER_COLOR_STANDARD, 0f, 2, 0f, 0f);
            }
        }

        unapplyRefit(stats, id);
        unapplySpeed(stats, id);
        unapplyAttack(stats, id);
    }

    public static enum CCMode {

        REFIT("Refit", "ii_commandcenter_ui", "ii_commandcenter_other"),
        SPEED("Speed", "ii_commandcenter_ui", "ii_commandcenter_other"),
        ATTACK("Attack", "ii_commandcenter_ui", "ii_commandcenter_other");

        final String name;
        final String soundId;
        final String otherSoundId;

        private CCMode(String name, String soundId, String otherSoundId) {
            this.name = name;
            this.soundId = soundId;
            this.otherSoundId = otherSoundId;
        }
    }
}
