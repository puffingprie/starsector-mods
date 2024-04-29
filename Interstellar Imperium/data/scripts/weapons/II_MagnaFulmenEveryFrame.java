package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.everyframe.II_Trails;
import data.scripts.everyframe.II_WeaponScriptPlugin;
import data.scripts.hullmods.II_BasePackage;
import data.scripts.shipsystems.II_ArbalestLoaderStats;
import data.scripts.util.II_Util;
import java.awt.Color;
import org.dark.shaders.distortion.DistortionShader;
import org.dark.shaders.distortion.WaveDistortion;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.entities.AnchoredEntity;
import org.lwjgl.util.vector.Vector2f;

public class II_MagnaFulmenEveryFrame implements EveryFrameWeaponEffectPlugin {

    private static final float CHARGEUP_THICKNESS_MAX_STANDARD = 6f;
    private static final float CHARGEUP_THICKNESS_MIN_STANDARD = 4f;
    private static final float CHARGEUP_THICKNESS_MAX_ENHANCED = 8f;
    private static final float CHARGEUP_THICKNESS_MIN_ENHANCED = 6f;
    private static final float CHARGEUP_THICKNESS_MAX_ENHANCED_ELITE1 = 8f;
    private static final float CHARGEUP_THICKNESS_MIN_ENHANCED_ELITE1 = 5f;
    private static final float CHARGEUP_THICKNESS_MAX_ENHANCED_ELITE2 = 10f;
    private static final float CHARGEUP_THICKNESS_MIN_ENHANCED_ELITE2 = 6f;
    private static final float CHARGEUP_THICKNESS_MAX_ENHANCED_ELITE3 = 12f;
    private static final float CHARGEUP_THICKNESS_MIN_ENHANCED_ELITE3 = 7f;
    private static final float CHARGEUP_THICKNESS_MAX_ENHANCED_ELITE4 = 14f;
    private static final float CHARGEUP_THICKNESS_MIN_ENHANCED_ELITE4 = 8f;
    private static final Color CHARGEUP_COLOR_CORE_STANDARD = new Color(255, 255, 255, 100);
    private static final Color CHARGEUP_COLOR_FRINGE_STANDARD = new Color(255, 155, 100, 255);
    private static final Color CHARGEUP_COLOR_FRINGE_ARMOR = new Color(255, 190, 100, 255);
    private static final Color CHARGEUP_COLOR_FRINGE_TARGETING = new Color(100, 200, 255, 255);
    private static final Color CHARGEUP_COLOR_FRINGE_ELITE = new Color(180, 100, 255, 255);

    private static final float CHARGEUP_GLOW_SIZE_STANDARD = 100.0f;
    private static final float CHARGEUP_GLOW_SIZE_ENHANCED = 150.0f;
    private static final float CHARGEUP_GLOW_SIZE_ENHANCED_ELITE1 = 150.0f;
    private static final float CHARGEUP_GLOW_SIZE_ENHANCED_ELITE2 = 175.0f;
    private static final float CHARGEUP_GLOW_SIZE_ENHANCED_ELITE3 = 200.0f;
    private static final float CHARGEUP_GLOW_SIZE_ENHANCED_ELITE4 = 225.0f;

    private static final float MUZZLE_FLASH_DURATION_STANDARD = 0.3f;
    private static final float MUZZLE_FLASH_SIZE_STANDARD = 150.0f;
    private static final float MUZZLE_FLASH_DURATION_ENHANCED = 0.5f;
    private static final float MUZZLE_FLASH_SIZE_ENHANCED = 250.0f;
    private static final float MUZZLE_FLASH_DURATION_ENHANCED_ELITE1 = 0.4f;
    private static final float MUZZLE_FLASH_SIZE_ENHANCED_ELITE1 = 250.0f;
    private static final float MUZZLE_FLASH_DURATION_ENHANCED_ELITE2 = 0.45f;
    private static final float MUZZLE_FLASH_SIZE_ENHANCED_ELITE2 = 300.0f;
    private static final float MUZZLE_FLASH_DURATION_ENHANCED_ELITE3 = 0.5f;
    private static final float MUZZLE_FLASH_SIZE_ENHANCED_ELITE3 = 350.0f;
    private static final float MUZZLE_FLASH_DURATION_ENHANCED_ELITE4 = 0.55f;
    private static final float MUZZLE_FLASH_SIZE_ENHANCED_ELITE4 = 400.0f;
    private static final Color MUZZLE_FLASH_COLOR_STANDARD = new Color(255, 175, 100, 150);
    private static final Color MUZZLE_FLASH_COLOR_ARMOR = new Color(255, 215, 100, 150);
    private static final Color MUZZLE_FLASH_COLOR_TARGETING = new Color(100, 175, 255, 150);
    private static final Color MUZZLE_FLASH_COLOR_ELITE = new Color(205, 100, 255, 150);

    private static final float MUZZLE_PARTICLE_SIZE_MIN = 4.0f;
    private static final float MUZZLE_PARTICLE_SIZE_MAX = 8.0f;
    private static final float MUZZLE_PARTICLE_DURATION_MIN = 0.5f;
    private static final float MUZZLE_PARTICLE_DURATION_MAX = 1.25f;

    private static final String FIRING_SOUND_STANDARD = "ii_magnafulmen_fire";
    private static final float SOUND_VOLUME_STANDARD = 0.9f;
    private static final float SOUND_PITCH_STANDARD = 1f;
    private static final String FIRING_SOUND_ENHANCED_ARMOR = "ii_magnafulmen_fire";
    private static final float SOUND_VOLUME_ENHANCED_ARMOR = 0.5f;
    private static final float SOUND_PITCH_ENHANCED_ARMOR = 1.1f;
    private static final String FIRING_SOUND2_ENHANCED_ARMOR = "ii_magnafulmen_bigfire";
    private static final float SOUND_VOLUME2_ENHANCED_ARMOR = 1.0f;
    private static final float SOUND_PITCH2_ENHANCED_ARMOR = 1.1f;
    private static final String FIRING_SOUND_ENHANCED_TARGETING = "ii_magnafulmen_fire";
    private static final float SOUND_VOLUME_ENHANCED_TARGETING = 0.5f;
    private static final float SOUND_PITCH_ENHANCED_TARGETING = 0.9f;
    private static final String FIRING_SOUND2_ENHANCED_TARGETING = "ii_magnafulmen_bigfire";
    private static final float SOUND_VOLUME2_ENHANCED_TARGETING = 1.0f;
    private static final float SOUND_PITCH2_ENHANCED_TARGETING = 0.9f;
    private static final String FIRING_SOUND_ENHANCED_ELITE1 = "ii_magnafulmen_fire";
    private static final float SOUND_VOLUME_ENHANCED_ELITE1 = 1f;
    private static final float SOUND_PITCH_ENHANCED_ELITE1 = 1f;
    private static final String FIRING_SOUND2_ENHANCED_ELITE1 = "ii_magnafulmen_bigfire";
    private static final float SOUND_VOLUME2_ENHANCED_ELITE1 = 0.5f;
    private static final float SOUND_PITCH2_ENHANCED_ELITE1 = 1f;
    private static final String FIRING_SOUND_ENHANCED_ELITE2 = "ii_magnafulmen_fire";
    private static final float SOUND_VOLUME_ENHANCED_ELITE2 = 1f;
    private static final float SOUND_PITCH_ENHANCED_ELITE2 = 1.1f;
    private static final String FIRING_SOUND2_ENHANCED_ELITE2 = "ii_magnafulmen_bigfire";
    private static final float SOUND_VOLUME2_ENHANCED_ELITE2 = 0.75f;
    private static final float SOUND_PITCH2_ENHANCED_ELITE2 = 0.9f;
    private static final String FIRING_SOUND_ENHANCED_ELITE3 = "ii_magnafulmen_fire";
    private static final float SOUND_VOLUME_ENHANCED_ELITE3 = 1f;
    private static final float SOUND_PITCH_ENHANCED_ELITE3 = 1.2f;
    private static final String FIRING_SOUND2_ENHANCED_ELITE3 = "ii_magnafulmen_bigfire";
    private static final float SOUND_VOLUME2_ENHANCED_ELITE3 = 1f;
    private static final float SOUND_PITCH2_ENHANCED_ELITE3 = 0.8f;
    private static final String FIRING_SOUND_ENHANCED_ELITE4 = "ii_magnafulmen_fire";
    private static final float SOUND_VOLUME_ENHANCED_ELITE4 = 1f;
    private static final float SOUND_PITCH_ENHANCED_ELITE4 = 1.3f;
    private static final String FIRING_SOUND2_ENHANCED_ELITE4 = "ii_magnafulmen_bigfire";
    private static final float SOUND_VOLUME2_ENHANCED_ELITE4 = 1.25f;
    private static final float SOUND_PITCH2_ENHANCED_ELITE4 = 0.7f;

    private static final Vector2f MUZZLE_OFFSET = new Vector2f(30.5f, 0f);
    private static final Vector2f LEFT_PRONG_OFFSET = new Vector2f(9f, 98f);
    private static final Vector2f RIGHT_PRONG_OFFSET = new Vector2f(9f, -98f);
    private static final Vector2f CATCH_OFFSET = new Vector2f(-75f, 0f);

    private static final Vector2f ZERO = new Vector2f();

    private final IntervalUtil interval = new IntervalUtil(0.075f, 0.1f);
    private final IntervalUtil interval2 = new IntervalUtil(0.1f, 0.15f);
    private float lastChargeLevel = 0.0f;
    private float lastCooldownRemaining = 0.0f;
    private boolean shot = false;
    private boolean startedFiring = false;
    private WaveDistortion wave = null;

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (engine.isPaused()) {
            return;
        }

        II_Trails.createIfNeeded();
        II_WeaponScriptPlugin.createIfNeeded();

        float chargeLevel = weapon.getChargeLevel();
        float cooldownRemaining = weapon.getCooldownRemaining();

        ShipAPI ship = weapon.getShip();
        Vector2f weaponLocation = weapon.getLocation();
        float weaponFacing = weapon.getCurrAngle();
        Vector2f shipVelocity = ship.getVelocity();

        int armed = II_ArbalestLoaderStats.getArmed(ship);

        Vector2f muzzleOffset = new Vector2f(MUZZLE_OFFSET);
        VectorUtils.rotate(muzzleOffset, weaponFacing, muzzleOffset);
        Vector2f muzzleLocation = Vector2f.add(weaponLocation, muzzleOffset, new Vector2f());

        Vector2f leftProngOffset = new Vector2f(LEFT_PRONG_OFFSET);
        VectorUtils.rotate(leftProngOffset, ship.getFacing(), leftProngOffset);
        Vector2f leftProngLocation = Vector2f.add(ship.getLocation(), leftProngOffset, new Vector2f());

        Vector2f rightProngOffset = new Vector2f(RIGHT_PRONG_OFFSET);
        VectorUtils.rotate(rightProngOffset, ship.getFacing(), rightProngOffset);
        Vector2f rightProngLocation = Vector2f.add(ship.getLocation(), rightProngOffset, new Vector2f());

        Vector2f catchOffset = new Vector2f(CATCH_OFFSET);
        VectorUtils.rotate(catchOffset, ship.getFacing(), catchOffset);
        Vector2f catchLocation = Vector2f.add(ship.getLocation(), catchOffset, new Vector2f());

        Color CHARGEUP_GLOW_COLOR = II_ArbalestLoaderStats.JITTER_COLOR_STANDARD;
        Color MUZZLE_BANG_COLOR = II_ArbalestLoaderStats.GLOW_COLOR_STANDARD;
        Color MUZZLE_PARTICLE_COLOR = II_ArbalestLoaderStats.GLOW_COLOR_STANDARD;
        Color CHARGEUP_COLOR_CORE = CHARGEUP_COLOR_CORE_STANDARD;
        Color CHARGEUP_COLOR_FRINGE = CHARGEUP_COLOR_FRINGE_STANDARD;
        Color MUZZLE_FLASH_COLOR = MUZZLE_FLASH_COLOR_STANDARD;
        String FIRING_SOUND = FIRING_SOUND_STANDARD;
        float SOUND_VOLUME = SOUND_VOLUME_STANDARD;
        float SOUND_PITCH = SOUND_PITCH_STANDARD;
        String FIRING_SOUND2 = null;
        float SOUND_VOLUME2 = 0f;
        float SOUND_PITCH2 = 0f;
        float CHARGEUP_THICKNESS_MAX = CHARGEUP_THICKNESS_MAX_STANDARD;
        float CHARGEUP_THICKNESS_MIN = CHARGEUP_THICKNESS_MIN_STANDARD;
        float CHARGEUP_GLOW_SIZE = CHARGEUP_GLOW_SIZE_STANDARD;
        float MUZZLE_FLASH_DURATION = MUZZLE_FLASH_DURATION_STANDARD;
        float MUZZLE_FLASH_SIZE = MUZZLE_FLASH_SIZE_STANDARD;
        float MUZZLE_PARTICLE_SPEED_MULT = 1f;
        float MUZZLE_PARTICLE_ARC_MULT = 1f;
        int MUZZLE_PARTICLE_COUNT = 50;
        if (ship.getVariant().hasHullMod(II_BasePackage.ARMOR_PACKAGE)) {
            CHARGEUP_GLOW_COLOR = II_ArbalestLoaderStats.JITTER_COLOR_ARMOR;
            MUZZLE_BANG_COLOR = II_ArbalestLoaderStats.GLOW_COLOR_ARMOR;
            MUZZLE_PARTICLE_COLOR = II_ArbalestLoaderStats.GLOW_COLOR_ARMOR;
            CHARGEUP_COLOR_FRINGE = CHARGEUP_COLOR_FRINGE_ARMOR;
            MUZZLE_FLASH_COLOR = MUZZLE_FLASH_COLOR_ARMOR;
            if (armed > 0) {
                FIRING_SOUND = FIRING_SOUND_ENHANCED_ARMOR;
                SOUND_VOLUME = SOUND_VOLUME_ENHANCED_ARMOR;
                SOUND_PITCH = SOUND_PITCH_ENHANCED_ARMOR;
                FIRING_SOUND2 = FIRING_SOUND2_ENHANCED_ARMOR;
                SOUND_VOLUME2 = SOUND_VOLUME2_ENHANCED_ARMOR;
                SOUND_PITCH2 = SOUND_PITCH2_ENHANCED_ARMOR;
                CHARGEUP_THICKNESS_MAX = CHARGEUP_THICKNESS_MAX_ENHANCED;
                CHARGEUP_THICKNESS_MIN = CHARGEUP_THICKNESS_MIN_ENHANCED;
                CHARGEUP_GLOW_SIZE = CHARGEUP_GLOW_SIZE_ENHANCED;
                MUZZLE_FLASH_DURATION = MUZZLE_FLASH_DURATION_ENHANCED;
                MUZZLE_FLASH_SIZE = MUZZLE_FLASH_SIZE_ENHANCED;
                MUZZLE_PARTICLE_ARC_MULT = 3f;
                MUZZLE_PARTICLE_COUNT = 150;
            }
        } else if (ship.getVariant().hasHullMod(II_BasePackage.TARGETING_PACKAGE)) {
            CHARGEUP_GLOW_COLOR = II_ArbalestLoaderStats.JITTER_COLOR_TARGETING;
            MUZZLE_BANG_COLOR = II_ArbalestLoaderStats.GLOW_COLOR_TARGETING;
            MUZZLE_PARTICLE_COLOR = II_ArbalestLoaderStats.GLOW_COLOR_TARGETING;
            CHARGEUP_COLOR_FRINGE = CHARGEUP_COLOR_FRINGE_TARGETING;
            MUZZLE_FLASH_COLOR = MUZZLE_FLASH_COLOR_TARGETING;
            MUZZLE_PARTICLE_SPEED_MULT = 0.75f;
            if (armed > 0) {
                FIRING_SOUND = FIRING_SOUND_ENHANCED_TARGETING;
                SOUND_VOLUME = SOUND_VOLUME_ENHANCED_TARGETING;
                SOUND_PITCH = SOUND_PITCH_ENHANCED_TARGETING;
                FIRING_SOUND2 = FIRING_SOUND2_ENHANCED_TARGETING;
                SOUND_VOLUME2 = SOUND_VOLUME2_ENHANCED_TARGETING;
                SOUND_PITCH2 = SOUND_PITCH2_ENHANCED_TARGETING;
                CHARGEUP_THICKNESS_MAX = CHARGEUP_THICKNESS_MAX_ENHANCED;
                CHARGEUP_THICKNESS_MIN = CHARGEUP_THICKNESS_MIN_ENHANCED;
                CHARGEUP_GLOW_SIZE = CHARGEUP_GLOW_SIZE_ENHANCED;
                MUZZLE_FLASH_DURATION = MUZZLE_FLASH_DURATION_ENHANCED;
                MUZZLE_FLASH_SIZE = MUZZLE_FLASH_SIZE_ENHANCED;
                MUZZLE_PARTICLE_COUNT = 75;
            }
        } else if (ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
            CHARGEUP_GLOW_COLOR = II_ArbalestLoaderStats.JITTER_COLOR_ELITE;
            MUZZLE_BANG_COLOR = II_ArbalestLoaderStats.GLOW_COLOR_ELITE;
            MUZZLE_PARTICLE_COLOR = II_ArbalestLoaderStats.GLOW_COLOR_ELITE;
            CHARGEUP_COLOR_FRINGE = CHARGEUP_COLOR_FRINGE_ELITE;
            MUZZLE_FLASH_COLOR = MUZZLE_FLASH_COLOR_ELITE;
            switch (armed) {
                case 1:
                    FIRING_SOUND = FIRING_SOUND_ENHANCED_ELITE1;
                    SOUND_VOLUME = SOUND_VOLUME_ENHANCED_ELITE1;
                    SOUND_PITCH = SOUND_PITCH_ENHANCED_ELITE1;
                    FIRING_SOUND2 = FIRING_SOUND2_ENHANCED_ELITE1;
                    SOUND_VOLUME2 = SOUND_VOLUME2_ENHANCED_ELITE1;
                    SOUND_PITCH2 = SOUND_PITCH2_ENHANCED_ELITE1;
                    CHARGEUP_THICKNESS_MAX = CHARGEUP_THICKNESS_MAX_ENHANCED_ELITE1;
                    CHARGEUP_THICKNESS_MIN = CHARGEUP_THICKNESS_MIN_ENHANCED_ELITE1;
                    CHARGEUP_GLOW_SIZE = CHARGEUP_GLOW_SIZE_ENHANCED_ELITE1;
                    MUZZLE_FLASH_DURATION = MUZZLE_FLASH_DURATION_ENHANCED_ELITE1;
                    MUZZLE_FLASH_SIZE = MUZZLE_FLASH_SIZE_ENHANCED_ELITE1;
                    MUZZLE_PARTICLE_SPEED_MULT = 1.2f;
                    MUZZLE_PARTICLE_COUNT = 60;
                    break;
                case 2:
                    FIRING_SOUND = FIRING_SOUND_ENHANCED_ELITE2;
                    SOUND_VOLUME = SOUND_VOLUME_ENHANCED_ELITE2;
                    SOUND_PITCH = SOUND_PITCH_ENHANCED_ELITE2;
                    FIRING_SOUND2 = FIRING_SOUND2_ENHANCED_ELITE2;
                    SOUND_VOLUME2 = SOUND_VOLUME2_ENHANCED_ELITE2;
                    SOUND_PITCH2 = SOUND_PITCH2_ENHANCED_ELITE2;
                    CHARGEUP_THICKNESS_MAX = CHARGEUP_THICKNESS_MAX_ENHANCED_ELITE2;
                    CHARGEUP_THICKNESS_MIN = CHARGEUP_THICKNESS_MIN_ENHANCED_ELITE2;
                    CHARGEUP_GLOW_SIZE = CHARGEUP_GLOW_SIZE_ENHANCED_ELITE2;
                    MUZZLE_FLASH_DURATION = MUZZLE_FLASH_DURATION_ENHANCED_ELITE2;
                    MUZZLE_FLASH_SIZE = MUZZLE_FLASH_SIZE_ENHANCED_ELITE2;
                    MUZZLE_PARTICLE_SPEED_MULT = 1.4f;
                    MUZZLE_PARTICLE_COUNT = 70;
                    break;
                case 3:
                    FIRING_SOUND = FIRING_SOUND_ENHANCED_ELITE3;
                    SOUND_VOLUME = SOUND_VOLUME_ENHANCED_ELITE3;
                    SOUND_PITCH = SOUND_PITCH_ENHANCED_ELITE3;
                    FIRING_SOUND2 = FIRING_SOUND2_ENHANCED_ELITE3;
                    SOUND_VOLUME2 = SOUND_VOLUME2_ENHANCED_ELITE3;
                    SOUND_PITCH2 = SOUND_PITCH2_ENHANCED_ELITE3;
                    CHARGEUP_THICKNESS_MAX = CHARGEUP_THICKNESS_MAX_ENHANCED_ELITE3;
                    CHARGEUP_THICKNESS_MIN = CHARGEUP_THICKNESS_MIN_ENHANCED_ELITE3;
                    CHARGEUP_GLOW_SIZE = CHARGEUP_GLOW_SIZE_ENHANCED_ELITE3;
                    MUZZLE_FLASH_DURATION = MUZZLE_FLASH_DURATION_ENHANCED_ELITE3;
                    MUZZLE_FLASH_SIZE = MUZZLE_FLASH_SIZE_ENHANCED_ELITE3;
                    MUZZLE_PARTICLE_SPEED_MULT = 1.6f;
                    MUZZLE_PARTICLE_COUNT = 80;
                    break;
                case 4:
                    FIRING_SOUND = FIRING_SOUND_ENHANCED_ELITE4;
                    SOUND_VOLUME = SOUND_VOLUME_ENHANCED_ELITE4;
                    SOUND_PITCH = SOUND_PITCH_ENHANCED_ELITE4;
                    FIRING_SOUND2 = FIRING_SOUND2_ENHANCED_ELITE4;
                    SOUND_VOLUME2 = SOUND_VOLUME2_ENHANCED_ELITE4;
                    SOUND_PITCH2 = SOUND_PITCH2_ENHANCED_ELITE4;
                    CHARGEUP_THICKNESS_MAX = CHARGEUP_THICKNESS_MAX_ENHANCED_ELITE4;
                    CHARGEUP_THICKNESS_MIN = CHARGEUP_THICKNESS_MIN_ENHANCED_ELITE4;
                    CHARGEUP_GLOW_SIZE = CHARGEUP_GLOW_SIZE_ENHANCED_ELITE4;
                    MUZZLE_FLASH_DURATION = MUZZLE_FLASH_DURATION_ENHANCED_ELITE4;
                    MUZZLE_FLASH_SIZE = MUZZLE_FLASH_SIZE_ENHANCED_ELITE4;
                    MUZZLE_PARTICLE_SPEED_MULT = 1.8f;
                    MUZZLE_PARTICLE_COUNT = 90;
                    break;
                default:
                    break;
            }
        }
        CHARGEUP_GLOW_COLOR = new Color(CHARGEUP_GLOW_COLOR.getRed(), CHARGEUP_GLOW_COLOR.getGreen(), CHARGEUP_GLOW_COLOR.getBlue(),
                II_Util.clamp255(Math.round(CHARGEUP_GLOW_COLOR.getAlpha() * 0.75f)));
        MUZZLE_BANG_COLOR = new Color(MUZZLE_BANG_COLOR.getRed(), MUZZLE_BANG_COLOR.getGreen(), MUZZLE_BANG_COLOR.getBlue(),
                II_Util.clamp255(Math.round(MUZZLE_BANG_COLOR.getAlpha() * 0.15f)));

        if ((chargeLevel > lastChargeLevel) || (lastCooldownRemaining < cooldownRemaining)) {
            if (weapon.isFiring() && ((ship.getFluxTracker().getMaxFlux() - ship.getFluxTracker().getCurrFlux()) >= weapon.getFluxCostToFire())) {
                if (!startedFiring) {
                    startedFiring = true;

                    if (ship.getVariant().hasHullMod(II_BasePackage.TARGETING_PACKAGE) && (armed > 0)) {
                        wave = new WaveDistortion(muzzleLocation, ZERO);
                        wave.setSize(125f);
                        wave.setIntensity(125f * 0.1f);
                        wave.flip(true);
                        wave.fadeOutSize(0.2f);
                        wave.fadeInIntensity(0.2f);
                        //wave.setLifetime(0.2f);
                        DistortionShader.addDistortion(wave);
                    }
                }

                interval.advance(amount);
                if (interval.intervalElapsed()) {
                    engine.spawnEmpArc(ship, leftProngLocation, ship, new AnchoredEntity(ship, catchLocation),
                            DamageType.ENERGY, 0, 0, 0, null,
                            MathUtils.getRandomNumberInRange(CHARGEUP_THICKNESS_MIN, CHARGEUP_THICKNESS_MAX),
                            CHARGEUP_COLOR_FRINGE, CHARGEUP_COLOR_CORE);
                    engine.spawnEmpArc(ship, rightProngLocation, ship, new AnchoredEntity(ship, catchLocation),
                            DamageType.ENERGY, 0, 0, 0, null,
                            MathUtils.getRandomNumberInRange(CHARGEUP_THICKNESS_MIN, CHARGEUP_THICKNESS_MAX),
                            CHARGEUP_COLOR_FRINGE, CHARGEUP_COLOR_CORE);

                    engine.addSmoothParticle(catchLocation, shipVelocity,
                            CHARGEUP_GLOW_SIZE, 0.5f, 0.1f, CHARGEUP_GLOW_COLOR);
                    engine.addSmoothParticle(muzzleLocation, shipVelocity,
                            CHARGEUP_GLOW_SIZE, 1f, 0.1f, CHARGEUP_GLOW_COLOR);
                }

                if (wave != null) {
                    wave.setLocation(muzzleLocation);
                }
            }

            if (!shot && ((lastCooldownRemaining < cooldownRemaining) || ((chargeLevel >= 1f) && (lastChargeLevel < 1f)))) {
                startedFiring = false;
                wave = null;
                shot = true;

                engine.spawnExplosion(muzzleLocation, shipVelocity,
                        MUZZLE_FLASH_COLOR, MUZZLE_FLASH_SIZE, MUZZLE_FLASH_DURATION);
                engine.addSmoothParticle(muzzleLocation, shipVelocity,
                        MUZZLE_FLASH_SIZE * 3f, 1f, MUZZLE_FLASH_DURATION * 2f, MUZZLE_FLASH_COLOR);
                engine.addHitParticle(muzzleLocation, shipVelocity,
                        MUZZLE_FLASH_SIZE * 4f, 0.25f, 0.05f, MUZZLE_BANG_COLOR);

                if (FIRING_SOUND != null) {
                    Global.getSoundPlayer().playSound(FIRING_SOUND, SOUND_PITCH, SOUND_VOLUME, muzzleLocation, ZERO);
                }
                if (FIRING_SOUND2 != null) {
                    Global.getSoundPlayer().playSound(FIRING_SOUND2, SOUND_PITCH2, SOUND_VOLUME2, muzzleLocation, ZERO);
                }
                if (armed > 0) {
                    II_ArbalestLoaderStats.setArmed(weapon.getShip(), 0);
                }

                for (int i = 0; i < MUZZLE_PARTICLE_COUNT; i++) {
                    Vector2f particleVel = new Vector2f(MathUtils.getRandomNumberInRange(50f, 300f) * MUZZLE_PARTICLE_SPEED_MULT, 0f);
                    VectorUtils.rotate(particleVel, weaponFacing + MathUtils.getRandomNumberInRange(-10f, 10f) * MUZZLE_PARTICLE_ARC_MULT);
                    Vector2f.add(shipVelocity, particleVel, particleVel);
                    engine.addHitParticle(muzzleLocation, particleVel, MathUtils.getRandomNumberInRange(MUZZLE_PARTICLE_SIZE_MIN, MUZZLE_PARTICLE_SIZE_MAX),
                            MathUtils.getRandomNumberInRange(0.75f, 1.25f), MathUtils.getRandomNumberInRange(MUZZLE_PARTICLE_DURATION_MIN, MUZZLE_PARTICLE_DURATION_MAX), MUZZLE_PARTICLE_COLOR);
                }

                if (ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE) && (armed > 0)) {
                    for (int i = 0; i < 3 * armed; i++) {
                        float targetAngle = (float) Math.random() * 360f;
                        float sparkLen = 30f * MathUtils.getRandomNumberInRange(0.75f, 1.25f) * (armed + 2);
                        Vector2f targetPoint = MathUtils.getPointOnCircumference(muzzleLocation, sparkLen, targetAngle);
                        float thickness = (float) Math.sqrt(sparkLen) * 3.5f;
                        Global.getCombatEngine().spawnEmpArcPierceShields(ship, targetPoint, ship, new AnchoredEntity(ship, muzzleLocation),
                                DamageType.ENERGY, 0f, 0f, sparkLen * 2f, null, thickness, MUZZLE_PARTICLE_COLOR, CHARGEUP_COLOR_FRINGE_ELITE);
                    }
                }

                for (int i = 0; i < 2; i++) {
                    engine.spawnEmpArc(ship, catchLocation, ship, new AnchoredEntity(ship, muzzleLocation),
                            DamageType.ENERGY, 0, 0, 0, null,
                            MathUtils.getRandomNumberInRange(CHARGEUP_THICKNESS_MIN * 2, CHARGEUP_THICKNESS_MAX * 2),
                            CHARGEUP_COLOR_FRINGE, CHARGEUP_COLOR_CORE);
                }
            }
        } else {
            shot = false;

            if (armed > 0) {
                interval2.advance(amount * armed);
                if (interval2.intervalElapsed()) {
                    engine.addSmoothParticle(catchLocation, shipVelocity, CHARGEUP_GLOW_SIZE, 1f,
                            MathUtils.getRandomNumberInRange(0.1f - (0.02f * armed), 0.1f + (0.02f * armed)), CHARGEUP_GLOW_COLOR);

                    if (ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE) && (Math.random() < 0.1)) {
                        float targetAngle = (float) Math.random() * 360f;
                        float sparkLen = 25f * MathUtils.getRandomNumberInRange(0.75f, 1.25f) * armed;
                        Vector2f targetPoint = MathUtils.getPointOnCircumference(catchLocation, sparkLen, targetAngle);
                        AnchoredEntity anchor = new AnchoredEntity(ship, catchLocation);
                        float thickness = (float) Math.sqrt(sparkLen) * 3f;
                        Global.getCombatEngine().spawnEmpArcPierceShields(ship, targetPoint, anchor, anchor, DamageType.ENERGY,
                                0f, 0f, sparkLen * 2f, null, thickness, MUZZLE_PARTICLE_COLOR, CHARGEUP_COLOR_FRINGE_ELITE);
                    }
                }
            }
        }

        lastChargeLevel = chargeLevel;
        lastCooldownRemaining = cooldownRemaining;
    }
}
