package data.scripts.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.hullmods.II_BasePackage;
import data.scripts.util.II_Util;
import java.awt.Color;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.entities.AnchoredEntity;
import org.lwjgl.util.vector.Vector2f;

public class II_TurbofeederStats extends BaseShipSystemScript {

    public static final float ROF_BONUS = 50f;
    public static final float FLUX_MULT = 0.5f;
    public static final float ARMOR_WEAPON_RESIST_MULT = 0.25f;
    public static final float TARGETING_PROJ_SPEED_MULT = 2f;
    public static final float ELITE_SPEED_BONUS = 50f;
    public static final float ELITE_ACCEL_BONUS = 75f;
    public static final float ELITE_DECEL_BONUS = 50f;
    public static final float ELITE_TURN_ACCEL_BONUS = 25f;
    public static final float ELITE_MAX_TURN_BONUS = 25f;

    public static final Map<HullSize, Float> ELITE_SPEED_FLAT_BONUS = new HashMap<>();

    static {
        ELITE_SPEED_FLAT_BONUS.put(HullSize.FRIGATE, 50f);
        ELITE_SPEED_FLAT_BONUS.put(HullSize.DESTROYER, 40f);
        ELITE_SPEED_FLAT_BONUS.put(HullSize.CRUISER, 35f);
        ELITE_SPEED_FLAT_BONUS.put(HullSize.CAPITAL_SHIP, 30f);
    }

    private static final Color GLOW_COLOR_STANDARD = new Color(255, 125, 75);
    private static final Color AFTERIMAGE_COLOR_STANDARD = new Color(255, 100, 50, 100);
    private static final Color GLOW_COLOR_ARMOR = new Color(255, 175, 100);
    private static final Color AFTERIMAGE_COLOR_ARMOR = new Color(255, 225, 75, 100);
    private static final Color GLOW_COLOR_TARGETING = new Color(75, 150, 255);
    private static final Color AFTERIMAGE_COLOR_TARGETING = new Color(75, 125, 255, 125);
    private static final Color GLOW_COLOR_ELITE = new Color(200, 100, 255);
    private static final Color AFTERIMAGE_COLOR_ELITE = new Color(175, 75, 255, 50);

    private final IntervalUtil interval = new IntervalUtil(0.015f, 0.015f);
    private final Object STATUSKEY1 = new Object();
    private final Object ENGINEKEY1 = new Object();
    private final Object ENGINEKEY2 = new Object();

    private static final Vector2f ZERO = new Vector2f();

    private boolean started = false;
    private boolean ended = false;

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        ShipAPI ship = (ShipAPI) stats.getEntity();
        if (ship == null) {
            return;
        }

        Color GLOW_COLOR = GLOW_COLOR_STANDARD;
        Color AFTERIMAGE_COLOR = AFTERIMAGE_COLOR_STANDARD;
        float afterImageSpread = 0.2f;
        float afterImageJitter = 0.8f;
        float afterImageDuration = 1f;
        if (ship.getVariant().hasHullMod(II_BasePackage.ARMOR_PACKAGE)) {
            GLOW_COLOR = GLOW_COLOR_ARMOR;
            AFTERIMAGE_COLOR = AFTERIMAGE_COLOR_ARMOR;
        } else if (ship.getVariant().hasHullMod(II_BasePackage.TARGETING_PACKAGE)) {
            GLOW_COLOR = GLOW_COLOR_TARGETING;
            AFTERIMAGE_COLOR = AFTERIMAGE_COLOR_TARGETING;
            afterImageSpread = 0.4f;
            afterImageJitter = 1f;
            afterImageDuration = 1.2f;
        } else if (ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
            GLOW_COLOR = GLOW_COLOR_ELITE;
            AFTERIMAGE_COLOR = AFTERIMAGE_COLOR_ELITE;
            afterImageSpread = 0.4f;
            afterImageJitter = 0.4f;
            afterImageDuration = 0.4f;
        }

        float amount = Global.getCombatEngine().getElapsedInLastFrame();
        if (Global.getCombatEngine().isPaused()) {
            amount = 0f;
        }

        if (ship.getVariant().hasHullMod(II_BasePackage.ARMOR_PACKAGE)) {
            float resistMult = II_Util.lerp(1f, ARMOR_WEAPON_RESIST_MULT, effectLevel);
            stats.getWeaponDamageTakenMult().modifyMult(id, resistMult);
            if (Global.getCombatEngine().getPlayerShip() == ship) {
                Global.getCombatEngine().maintainStatusForPlayerShip(STATUSKEY1, ship.getSystem().getSpecAPI().getIconSpriteName(),
                        ship.getSystem().getDisplayName(), "weapon damage taken -" + Math.round((1f - resistMult) * 100f) + "%", false);
            }
        }

        if (ship.getVariant().hasHullMod(II_BasePackage.TARGETING_PACKAGE)) {
            float projMult = II_Util.lerp(1f, TARGETING_PROJ_SPEED_MULT, effectLevel);
            stats.getProjectileSpeedMult().modifyMult(id, projMult);
            if (Global.getCombatEngine().getPlayerShip() == ship) {
                Global.getCombatEngine().maintainStatusForPlayerShip(STATUSKEY1, ship.getSystem().getSpecAPI().getIconSpriteName(),
                        ship.getSystem().getDisplayName(), "projectile speed +" + Math.round((projMult - 1f) * 100f) + "%", false);
            }
        }

        if (ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
            if (state == State.OUT) {
                stats.getMaxSpeed().unmodify(id);
                stats.getMaxTurnRate().unmodify(id);
            } else {
                stats.getMaxSpeed().modifyFlat(id, ELITE_SPEED_FLAT_BONUS.get(ship.getHullSize()));
                stats.getMaxSpeed().modifyPercent(id, ELITE_SPEED_BONUS);
                stats.getAcceleration().modifyPercent(id, ELITE_ACCEL_BONUS * effectLevel);
                stats.getDeceleration().modifyPercent(id, ELITE_DECEL_BONUS * effectLevel);
                stats.getTurnAcceleration().modifyPercent(id, ELITE_TURN_ACCEL_BONUS * effectLevel);
                stats.getMaxTurnRate().modifyPercent(id, ELITE_MAX_TURN_BONUS);
                if (Global.getCombatEngine().getPlayerShip() == ship) {
                    Global.getCombatEngine().maintainStatusForPlayerShip(STATUSKEY1, ship.getSystem().getSpecAPI().getIconSpriteName(),
                            ship.getSystem().getDisplayName(), "improved engine performance", false);
                }

                Color contrailColor = new Color(AFTERIMAGE_COLOR.getRed(), AFTERIMAGE_COLOR.getGreen(), AFTERIMAGE_COLOR.getBlue(),
                        II_Util.clamp255(Math.round(1.25f * AFTERIMAGE_COLOR.getAlpha())));
                ship.getEngineController().fadeToOtherColor(ENGINEKEY1, GLOW_COLOR, contrailColor, 1f, 2f);
                ship.getEngineController().extendFlame(ENGINEKEY2, 0.5f, 0.75f, 0.5f);
            }
        }

        stats.getBallisticRoFMult().modifyPercent(id, ROF_BONUS * effectLevel);
        stats.getMissileRoFMult().modifyPercent(id, ROF_BONUS * effectLevel);
        stats.getEnergyRoFMult().modifyPercent(id, ROF_BONUS * effectLevel);
        stats.getBallisticWeaponFluxCostMod().modifyMult(id, II_Util.lerp(1f, FLUX_MULT, effectLevel));
        stats.getMissileWeaponFluxCostMod().modifyMult(id, II_Util.lerp(1f, FLUX_MULT, effectLevel));
        stats.getEnergyWeaponFluxCostMod().modifyMult(id, II_Util.lerp(1f, FLUX_MULT, effectLevel));
        stats.getBeamWeaponFluxCostMult().modifyMult(id, 1f / II_Util.lerp(1f, FLUX_MULT, effectLevel));

        ship.setWeaponGlow(effectLevel, GLOW_COLOR, EnumSet.of(WeaponType.BALLISTIC, WeaponType.MISSILE, WeaponType.ENERGY));

        interval.advance(amount);
        if (interval.intervalElapsed()) {
            float shipRadius = II_Util.effectiveRadius(ship);
            float randRange = (float) Math.sqrt(shipRadius);

            Vector2f randLoc = MathUtils.getRandomPointInCircle(ZERO, randRange * afterImageSpread);
            Vector2f vel = new Vector2f(ship.getVelocity());
            if (ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
                vel.scale(-1f);
            } else {
                vel.scale(0f);
            }

            Color afterImageColor = new Color(AFTERIMAGE_COLOR.getRed(), AFTERIMAGE_COLOR.getGreen(), AFTERIMAGE_COLOR.getBlue(),
                    II_Util.clamp255(Math.round(effectLevel * AFTERIMAGE_COLOR.getAlpha())));
            ship.addAfterimage(afterImageColor, randLoc.x, randLoc.y, vel.x, vel.y, randRange * afterImageJitter,
                    0.05f * afterImageDuration, 0.2f * afterImageDuration, 0.05f * afterImageDuration, true, false, false);
        }

        switch (state) {
            case IN: {
                if (!started) {
                    if (ship.getVariant().hasHullMod(II_BasePackage.ARMOR_PACKAGE)) {
                        WeaponSize largestSizeRepaired = null;
                        float numRepaired = 0f;
                        float numRepairable = 0f;

                        for (WeaponAPI weapon : ship.getAllWeapons()) {
                            if (weapon.isPermanentlyDisabled() || weapon.isDecorative() || weapon.getSlot().isDecorative()
                                    || weapon.getSlot().isHidden() || weapon.getSlot().isSystemSlot() || weapon.getSlot().isStationModule()) {
                                continue;
                            } else {
                                numRepairable += 1f;
                            }

                            float intensity = 0f;
                            float radius;
                            int numSparks;
                            switch (weapon.getSize()) {
                                case SMALL:
                                    radius = 30f;
                                    numSparks = 1;
                                    break;
                                default:
                                case MEDIUM:
                                    radius = 60f;
                                    numSparks = 2;
                                    break;
                                case LARGE:
                                    radius = 120f;
                                    numSparks = 4;
                                    break;
                            }

                            if (weapon.isDisabled()) {
                                weapon.repair();
                                intensity = 1f;
                                numRepaired += 1f;
                                if (largestSizeRepaired == null) {
                                    largestSizeRepaired = weapon.getSize();
                                } else if ((weapon.getSize() == WeaponSize.MEDIUM) && (largestSizeRepaired == WeaponSize.SMALL)) {
                                    largestSizeRepaired = WeaponSize.MEDIUM;
                                } else if ((weapon.getSize() == WeaponSize.LARGE) && (largestSizeRepaired != WeaponSize.LARGE)) {
                                    largestSizeRepaired = WeaponSize.LARGE;
                                }

                                for (int i = 0; i < numSparks; i++) {
                                    Vector2f point = MathUtils.getRandomPointOnCircumference(weapon.getLocation(), radius);
                                    AnchoredEntity anchor = new AnchoredEntity(ship, weapon.getLocation());
                                    float thickness = (float) Math.sqrt(radius);
                                    Global.getCombatEngine().spawnEmpArcPierceShields(ship, point, anchor, anchor, DamageType.ENERGY,
                                            0f, 0f, radius, null, thickness, AFTERIMAGE_COLOR, GLOW_COLOR);
                                }
                            } else if (weapon.getCurrHealth() < weapon.getMaxHealth()) {
                                intensity = Math.min(1f, (weapon.getMaxHealth() - weapon.getCurrHealth()) / Math.max(1f, weapon.getMaxHealth()));
                                weapon.setCurrHealth(weapon.getMaxHealth());
                                numRepaired += intensity / 2f;
                            }

                            if (intensity > 0f) {
                                Color flashColor = new Color(GLOW_COLOR.getRed(), GLOW_COLOR.getGreen(), GLOW_COLOR.getBlue(),
                                        II_Util.clamp255(Math.round(intensity * GLOW_COLOR.getAlpha())));
                                float duration = (float) Math.sqrt(radius) * 0.1f;
                                Global.getCombatEngine().addHitParticle(weapon.getLocation(), ship.getVelocity(), radius * 1.5f, 1f, duration, flashColor);
                            }
                        }

                        float fracRepaired = (float) numRepaired / Math.max(1f, numRepairable);

                        if (largestSizeRepaired != null) {
                            switch (largestSizeRepaired) {
                                case SMALL:
                                    Global.getSoundPlayer().playSound("disabled_small_crit", 1.5f, 1f + fracRepaired, ship.getLocation(), ship.getVelocity());
                                    break;
                                default:
                                case MEDIUM:
                                    Global.getSoundPlayer().playSound("disabled_medium_crit", 1.5f, 1f + fracRepaired, ship.getLocation(), ship.getVelocity());
                                    break;
                                case LARGE:
                                    Global.getSoundPlayer().playSound("disabled_large_crit", 1.5f, 1f + fracRepaired, ship.getLocation(), ship.getVelocity());
                                    break;
                            }
                        }

                        if (fracRepaired >= 0.25f) {
                            ship.syncWeaponDecalsWithArmorDamage();
                        }
                    }

                    started = true;
                }
                break;
            }
            case ACTIVE: {
                if (!Global.getCombatEngine().isPaused()) {
                    switch (ship.getHullSize()) {
                        case FIGHTER:
                            Global.getSoundPlayer().playLoop("ii_infernium_turbo_loop", ship, 1.25f, 0.4f, ship.getLocation(), ZERO);
                            break;
                        case FRIGATE:
                            Global.getSoundPlayer().playLoop("ii_infernium_turbo_loop", ship, 1.1f, 0.7f, ship.getLocation(), ZERO);
                            break;
                        default:
                        case DEFAULT:
                        case DESTROYER:
                            Global.getSoundPlayer().playLoop("ii_infernium_turbo_loop", ship, 1f, 0.8f, ship.getLocation(), ZERO);
                            break;
                        case CRUISER:
                            Global.getSoundPlayer().playLoop("ii_infernium_turbo_loop", ship, 0.95f, 0.9f, ship.getLocation(), ZERO);
                            break;
                        case CAPITAL_SHIP:
                            Global.getSoundPlayer().playLoop("ii_infernium_turbo_loop", ship, 0.9f, 1f, ship.getLocation(), ZERO);
                            break;
                    }
                }
                break;
            }
            case OUT: {
                if (!ended) {
                    switch (ship.getHullSize()) {
                        case FIGHTER:
                            Global.getSoundPlayer().playSound("ii_infernium_turbo_off", 1.52f, 0.4f, ship.getLocation(), ZERO);
                            break;
                        case FRIGATE:
                            Global.getSoundPlayer().playSound("ii_infernium_turbo_off", 1.1f, 0.7f, ship.getLocation(), ZERO);
                            break;
                        default:
                        case DEFAULT:
                        case DESTROYER:
                            Global.getSoundPlayer().playSound("ii_infernium_turbo_off", 1f, 0.8f, ship.getLocation(), ZERO);
                            break;
                        case CRUISER:
                            Global.getSoundPlayer().playSound("ii_infernium_turbo_off", 0.95f, 0.9f, ship.getLocation(), ZERO);
                            break;
                        case CAPITAL_SHIP:
                            Global.getSoundPlayer().playSound("ii_infernium_turbo_off", 0.9f, 1f, ship.getLocation(), ZERO);
                            break;
                    }

                    ended = true;
                }
                break;
            }
            default:
                break;
        }
    }

    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        switch (index) {
            case 0:
                return new StatusData("rate of fire +" + Math.round(ROF_BONUS * effectLevel) + "%", false);
            case 1:
                return new StatusData("flux use (non-beam) -" + Math.round((1f - II_Util.lerp(1f, FLUX_MULT, effectLevel)) * 100f) + "%", false);
            default:
                break;
        }
        return null;
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        ShipAPI ship = (ShipAPI) stats.getEntity();
        if (ship == null) {
            return;
        }

        started = false;
        ended = false;

        stats.getWeaponDamageTakenMult().unmodify(id);

        stats.getProjectileSpeedMult().unmodify(id);

        stats.getMaxSpeed().unmodify(id);
        stats.getAcceleration().unmodify(id);
        stats.getDeceleration().unmodify(id);
        stats.getTurnAcceleration().unmodify(id);
        stats.getMaxTurnRate().unmodify(id);

        stats.getBallisticRoFMult().unmodify(id);
        stats.getMissileRoFMult().unmodify(id);
        stats.getEnergyRoFMult().unmodify(id);
        stats.getBallisticWeaponFluxCostMod().unmodify(id);
        stats.getMissileWeaponFluxCostMod().unmodify(id);
        stats.getEnergyWeaponFluxCostMod().unmodify(id);
        stats.getBeamWeaponFluxCostMult().unmodify(id);

        ship.setWeaponGlow(0f, GLOW_COLOR_STANDARD, EnumSet.of(WeaponType.BALLISTIC, WeaponType.MISSILE, WeaponType.ENERGY));
    }
}
