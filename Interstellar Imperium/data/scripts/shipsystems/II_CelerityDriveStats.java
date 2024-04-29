package data.scripts.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.SoundAPI;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.hullmods.II_BasePackage;
import data.scripts.util.II_Util;
import java.awt.Color;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.dark.shaders.distortion.DistortionShader;
import org.dark.shaders.distortion.RippleDistortion;
import org.dark.shaders.distortion.WaveDistortion;
import org.dark.shaders.post.PostProcessShader;
import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lazywizard.lazylib.combat.entities.SimpleEntity;
import org.lwjgl.util.vector.Vector2f;

public class II_CelerityDriveStats extends BaseShipSystemScript {

    public static final float TIME_MULT_IN_START = 0.5f;
    public static final float TIME_MULT_IN_END = 0.2f;
    public static final float TIME_MULT_ACTIVE_START = 5.5f;
    public static final float TIME_MULT_ACTIVE_END = 1f;
    public static final float BLAST_AREA_RADIUS_SCALE = 2f;
    public static final float BLAST_AREA_FLAT = 250f;
    public static final float BLAST_DAMAGE = 100f;
    public static final float BLAST_MAX_DAMAGE = BLAST_DAMAGE * 2f;
    public static final float BLAST_EMP = 100f;
    public static final int BLAST_MAX_MISSILE_TARGETS = 3;
    public static final float BLAST_INTERVAL_MIN = 0.3f;
    public static final float BLAST_INTERVAL_MAX = 0.4f;

    public static final Color JITTER_COLOR_STANDARD = new Color(255, 100, 10, 100);
    public static final Color JITTER_UNDER_COLOR_STANDARD = new Color(255, 50, 10, 200);
    public static final Color AFTERIMAGE_COLOR_STANDARD = new Color(255, 150, 10);
    public static final Color JITTER_COLOR_ARMOR = new Color(255, 235, 10, 100);
    public static final Color JITTER_UNDER_COLOR_ARMOR = new Color(255, 215, 10, 200);
    public static final Color AFTERIMAGE_COLOR_ARMOR = new Color(255, 255, 10);
    public static final Color JITTER_COLOR_TARGETING = new Color(10, 135, 255, 100);
    public static final Color JITTER_UNDER_COLOR_TARGETING = new Color(10, 215, 255, 200);
    public static final Color AFTERIMAGE_COLOR_TARGETING = new Color(10, 100, 255);
    public static final Color JITTER_COLOR_ELITE = new Color(215, 10, 255, 100);
    public static final Color JITTER_UNDER_COLOR_ELITE = new Color(255, 10, 225, 200);
    public static final Color AFTERIMAGE_COLOR_ELITE = new Color(135, 10, 255);

    public static final Map<HullSize, Float> ARMOR_DAMAGE_REDUCTION = new HashMap<>();
    public static final float TARGETING_WEAPON_FLUX_MULT = 1f / 3f;
    public static final float ELITE_IN_OVERRIDE = 0.5f;
    public static final float ELITE_OUT_OVERRIDE = 20f / 3f;
    public static final float ELITE_COOLDOWN_OVERRIDE = 5f;

    static {
        ARMOR_DAMAGE_REDUCTION.put(HullSize.FRIGATE, 0.33f);
        ARMOR_DAMAGE_REDUCTION.put(HullSize.DESTROYER, 0.33f);
        ARMOR_DAMAGE_REDUCTION.put(HullSize.CRUISER, 0.5f);
        ARMOR_DAMAGE_REDUCTION.put(HullSize.CAPITAL_SHIP, 0.5f);
    }

    private final Object STATUSKEY1 = new Object();
    private static final Vector2f ZERO = new Vector2f();

    private final IntervalUtil interval = new IntervalUtil(0.1f, 0.1f);
    private final IntervalUtil interval2 = new IntervalUtil(BLAST_INTERVAL_MIN, BLAST_INTERVAL_MAX);
    private boolean started = false;
    private boolean fired = false;
    private WaveDistortion wave = null;
    private SoundAPI activateSound = null;

    private static final String postProcessKey = "ii_celerity_pp";

    /* TODO: Custom AI that tries not to commit suicide, acts more aggressively while active */
    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        if (!(stats.getEntity() instanceof ShipAPI)) {
            return;
        }
        ShipAPI ship = (ShipAPI) stats.getEntity();

        Color JITTER_COLOR = JITTER_COLOR_STANDARD;
        Color JITTER_UNDER_COLOR = JITTER_UNDER_COLOR_STANDARD;
        Color AFTERIMAGE_COLOR = AFTERIMAGE_COLOR_STANDARD;
        float afterImageSpread = 0.5f;
        float afterImageJitter = 0.5f;
        float afterImageDuration = 0.8f;
        float afterImageIntensity = 0.2f;
        float jitterIntensity = 1f;
        float jitterUnderIntensity = 0.5f;
        if (ship.getVariant().hasHullMod(II_BasePackage.ARMOR_PACKAGE)) {
            JITTER_COLOR = JITTER_COLOR_ARMOR;
            JITTER_UNDER_COLOR = JITTER_UNDER_COLOR_ARMOR;
            AFTERIMAGE_COLOR = AFTERIMAGE_COLOR_ARMOR;
            jitterUnderIntensity = 1f;
        } else if (ship.getVariant().hasHullMod(II_BasePackage.TARGETING_PACKAGE)) {
            JITTER_COLOR = JITTER_COLOR_TARGETING;
            JITTER_UNDER_COLOR = JITTER_UNDER_COLOR_TARGETING;
            AFTERIMAGE_COLOR = AFTERIMAGE_COLOR_TARGETING;
            jitterIntensity = 2f;
        } else if (ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
            JITTER_COLOR = JITTER_COLOR_ELITE;
            JITTER_UNDER_COLOR = JITTER_UNDER_COLOR_ELITE;
            AFTERIMAGE_COLOR = AFTERIMAGE_COLOR_ELITE;
        }

        float amount = Global.getCombatEngine().getElapsedInLastFrame();
        float objectiveAmount = amount * Global.getCombatEngine().getTimeMult().getModifiedValue();
        if (Global.getCombatEngine().isPaused()) {
            amount = 0f;
            objectiveAmount = 0f;
        }

        float shipRadius = II_Util.effectiveRadius(ship);
        Vector2f offset = new Vector2f(-6f, 0f);
        VectorUtils.rotate(offset, ship.getFacing(), offset);
        Vector2f centerLocation = Vector2f.add(ship.getLocation(), offset, new Vector2f());

        switch (state) {
            case IN: {
                float startSize = shipRadius * 0.5f;
                float endSize = shipRadius * 0.75f;
                if (!started) {
                    Global.getCombatEngine().spawnExplosion(centerLocation, ZERO, JITTER_UNDER_COLOR, shipRadius * 2f, 0.1f);

                    int numSparks = Math.round(shipRadius * 0.1f);
                    for (int i = 0; i < numSparks; i++) {
                        Vector2f point = MathUtils.getRandomPointOnCircumference(centerLocation, MathUtils.getRandomNumberInRange(startSize * 2f, endSize * 2f));
                        Global.getCombatEngine().spawnEmpArc(ship, point, ship, ship,
                                DamageType.ENERGY, 0f, 0f, 1000f, null, 10f, AFTERIMAGE_COLOR, JITTER_UNDER_COLOR);
                    }

                    float lifetime = 3f;
                    if (ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
                        lifetime = 1.5f;
                    }

                    wave = new WaveDistortion(centerLocation, ZERO);
                    wave.setSize(endSize);
                    wave.setIntensity(endSize * 0.2f);
                    wave.flip(true);
                    wave.fadeInSize(lifetime * endSize / (endSize - startSize));
                    wave.fadeInIntensity(lifetime);
                    wave.setSize(startSize);
                    wave.setLifetime(0f);
                    wave.setAutoFadeIntensityTime(0.1f);
                    wave.setAutoFadeSizeTime(0.1f);
                    DistortionShader.addDistortion(wave);

                    started = true;
                }

                float shipTimeMult = II_Util.lerp(TIME_MULT_IN_START, TIME_MULT_IN_END, effectLevel);

                float damperEffectLevel = effectLevel;
                if (ship.getVariant().hasHullMod(II_BasePackage.ARMOR_PACKAGE)) {
                    damperEffectLevel = II_Util.lerp(0f, 1f, Math.min(1f, effectLevel * 3f / 0.25f));
                    float mult = II_Util.lerp(1f, ARMOR_DAMAGE_REDUCTION.get(ship.getHullSize()), damperEffectLevel);
                    stats.getArmorDamageTakenMult().modifyMult(id, mult);
                    stats.getHullDamageTakenMult().modifyMult(id, mult);
                    if (Global.getCombatEngine().getPlayerShip() == ship) {
                        Global.getCombatEngine().maintainStatusForPlayerShip(STATUSKEY1, ship.getSystem().getSpecAPI().getIconSpriteName(),
                                ship.getSystem().getDisplayName(), "hull/armor damage taken -" + (int) Math.round((1f - mult) * 100f) + "%", false);
                    }

                    Global.getSoundPlayer().playLoop("ii_celerity_armor_loop", ship, shipTimeMult / TIME_MULT_IN_START, damperEffectLevel, centerLocation, ship.getVelocity());
                } else {
                    jitterIntensity *= 0.75f;
                    jitterUnderIntensity *= 0.75f;
                }

                if (wave != null) {
                    wave.setLocation(centerLocation);
                }
                Global.getSoundPlayer().playLoop("ii_celerity_activate_loop", this, shipTimeMult / TIME_MULT_IN_START, 1f, centerLocation, ZERO);
                stats.getTimeMult().modifyMult(id, shipTimeMult);

                float realEffectLevel = II_Util.lerp(0.5f, 1f, effectLevel);
                Color jitterColor = new Color(JITTER_COLOR.getRed(), JITTER_COLOR.getGreen(), JITTER_COLOR.getBlue(),
                        II_Util.clamp255(Math.round(jitterIntensity * realEffectLevel * JITTER_COLOR.getAlpha())));
                Color jitterUnderColor = new Color(JITTER_UNDER_COLOR.getRed(), JITTER_UNDER_COLOR.getGreen(), JITTER_UNDER_COLOR.getBlue(),
                        II_Util.clamp255(Math.round(jitterUnderIntensity * damperEffectLevel * JITTER_UNDER_COLOR.getAlpha())));
                ship.setJitter(this, jitterColor, 1f, 1, 0f, jitterIntensity * 10f * realEffectLevel);
                ship.setJitterUnder(this, jitterUnderColor, 1f, 10, 0f, 7f + jitterUnderIntensity * (10f * damperEffectLevel));

                interval.advance(amount * stats.getTimeMult().getModifiedValue());
                if (interval.intervalElapsed()) {
                    float randRange = (float) Math.sqrt(shipRadius);

                    Vector2f randLoc = MathUtils.getRandomPointInCircle(ZERO, randRange * afterImageSpread);
                    Vector2f vel = new Vector2f(ship.getVelocity());
                    vel.scale(-1f);

                    Color afterImageColor = new Color(AFTERIMAGE_COLOR.getRed(), AFTERIMAGE_COLOR.getGreen(), AFTERIMAGE_COLOR.getBlue(),
                            II_Util.clamp255(Math.round(effectLevel * afterImageIntensity * AFTERIMAGE_COLOR.getAlpha())));
                    ship.addAfterimage(afterImageColor, randLoc.x, randLoc.y, vel.x, vel.y, randRange * afterImageJitter,
                            0.05f * afterImageDuration, 0.2f * afterImageDuration * shipTimeMult, 0.05f * afterImageDuration * shipTimeMult, true, false, false);

                    for (int i = 0; i < 2; i++) {
                        float particleSpawnAngle = MathUtils.getRandomNumberInRange(0f, 360f);
                        float particleSpawnDist = II_Util.lerp(startSize * 3f, endSize * 3f, effectLevel);
                        float particleDuration = II_Util.lerp(0.5f, 0.3f, effectLevel);
                        Vector2f particlePoint = MathUtils.getPointOnCircumference(centerLocation, particleSpawnDist, particleSpawnAngle);
                        Vector2f particleVel = new Vector2f((-particleSpawnDist * 2f / 3f) / particleDuration, 0f);
                        VectorUtils.rotate(particleVel, particleSpawnAngle, particleVel);
                        Global.getCombatEngine().addHitParticle(particlePoint, particleVel, 5f, 1f, particleDuration, AFTERIMAGE_COLOR);
                    }
                }

                interval2.advance(objectiveAmount * stats.getTimeMult().getModifiedValue());
                if (interval2.intervalElapsed()) {
                    float blastArea = shipRadius * BLAST_AREA_RADIUS_SCALE + BLAST_AREA_FLAT;
                    float blastDamage = BLAST_DAMAGE;
                    float maxBlastDamage = BLAST_MAX_DAMAGE;
                    float blastEMP = BLAST_EMP;

                    float totalDamage = 0f;
                    List<ShipAPI> nearbyEnemies = CombatUtils.getShipsWithinRange(centerLocation, blastArea);
                    for (ShipAPI thisEnemy : nearbyEnemies) {
                        if ((thisEnemy == ship) || ((thisEnemy.isFighter() || thisEnemy.isDrone()) && (thisEnemy.getOwner() == ship.getOwner()))) {
                            continue;
                        }

                        float contribution = 1f;
                        if (thisEnemy.isFighter() || thisEnemy.isDrone()) {
                            contribution *= 0.25f;
                        }
                        if (thisEnemy.getOwner() == ship.getOwner()) {
                            contribution *= 0.5f;
                        }

                        float falloff = 1f - MathUtils.getDistance(ship, thisEnemy) / blastArea;
                        if (thisEnemy.getOwner() == ship.getOwner()) {
                            falloff *= 0.25f;
                        }
                        if (thisEnemy.getCollisionClass() == CollisionClass.NONE) {
                            continue;
                        } else {
                            totalDamage += blastDamage * falloff * contribution;
                        }

                        for (int i = 0; i <= (int) (blastDamage * falloff / 125f); i++) {
                            totalDamage += blastDamage * falloff * 0.25f * contribution;
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
                    for (ShipAPI thisEnemy : nearbyEnemies) {
                        if ((thisEnemy == ship) || ((thisEnemy.isFighter() || thisEnemy.isDrone()) && (thisEnemy.getOwner() == ship.getOwner()))) {
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
                                    (float) Math.sqrt(blastDamage), AFTERIMAGE_COLOR, JITTER_UNDER_COLOR);
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
                                    10000f, null, (float) Math.sqrt(blastDamage * falloff * attenuation), AFTERIMAGE_COLOR, JITTER_UNDER_COLOR);
                            if (missileTargets >= BLAST_MAX_MISSILE_TARGETS) {
                                break;
                            }
                        }
                    }
                }
                break;
            }
            case ACTIVE:
            case OUT: {
                if ((ship.getPhaseCloak() != null) && ship.isPhased()) {
                    ship.getPhaseCloak().deactivate();
                }

                if (!fired) {
                    if (Global.getCombatEngine().getPlayerShip() == ship) {
                        activateSound = Global.getSoundPlayer().playSound("ii_celerity_start_long", 0.5f, 1f, centerLocation, ZERO);
                    } else {
                        activateSound = Global.getSoundPlayer().playSound("ii_celerity_start", 1f, 0.8f, centerLocation, ZERO);
                    }
                    Global.getCombatEngine().spawnExplosion(centerLocation, ZERO, JITTER_COLOR, shipRadius * 4f, 0.2f);

                    float startSize = shipRadius * 1.5f;
                    float endSize = (shipRadius * 2f) + 400f;
                    RippleDistortion ripple = new RippleDistortion(centerLocation, ZERO);
                    ripple.setSize(endSize);
                    ripple.setIntensity(endSize * 0.05f);
                    ripple.setFrameRate(60f / 0.3f);
                    ripple.fadeInSize(0.3f * endSize / (endSize - startSize));
                    ripple.fadeOutIntensity(0.3f);
                    ripple.setSize(startSize);
                    DistortionShader.addDistortion(ripple);

                    int numParticles = Math.round(shipRadius);
                    for (int i = 0; i < numParticles; i++) {
                        float particleSpawnAngle = MathUtils.getRandomNumberInRange(0f, 360f);
                        float particleDuration = MathUtils.getRandomNumberInRange(0.5f, 1f);
                        Vector2f particlePoint = MathUtils.getPointOnCircumference(centerLocation, shipRadius, particleSpawnAngle);
                        Vector2f particleVel = new Vector2f(shipRadius * 4f, 0f);
                        VectorUtils.rotate(particleVel, particleSpawnAngle, particleVel);
                        Global.getCombatEngine().addHitParticle(particlePoint, particleVel, 10f, 1f, particleDuration, AFTERIMAGE_COLOR);
                    }

                    int numSparks = Math.round(shipRadius * 0.2f);
                    for (int i = 0; i < numSparks; i++) {
                        Vector2f point = MathUtils.getRandomPointOnCircumference(centerLocation, MathUtils.getRandomNumberInRange(shipRadius * 2f, shipRadius * 4f));
                        Vector2f point2 = MathUtils.getRandomPointOnCircumference(centerLocation, shipRadius);
                        CombatEntityAPI entity = new SimpleEntity(point2);
                        Global.getCombatEngine().spawnEmpArc(ship, point, entity, entity,
                                DamageType.ENERGY, 0f, 0f, 1000f, null, 15f, AFTERIMAGE_COLOR, JITTER_UNDER_COLOR);
                    }

                    fired = true;
                }

                float effectSqrt = (float) Math.sqrt(effectLevel);
                float shipTimeMult = II_Util.lerp(TIME_MULT_ACTIVE_END, TIME_MULT_ACTIVE_START, effectSqrt);
                stats.getTimeMult().modifyMult(id, shipTimeMult);
                stats.getVentRateMult().modifyMult(id, Math.min(1f, 2f / shipTimeMult));
                String globalId = id + "_" + ship.getId();
                if (Global.getCombatEngine().getPlayerShip() == ship) {
                    PostProcessShader.setNoise(false, II_Util.lerp(0f, 0.5f, effectSqrt));
                    PostProcessShader.setSaturation(false, II_Util.lerp(1f, 0f, Math.max(0f, Math.min(1f, (effectSqrt - 0.75f) * 8f))));
                    PostProcessShader.setLightness(false, II_Util.lerp(1f, 1.5f, effectSqrt));
                    Global.getCombatEngine().getTimeMult().modifyMult(globalId, 1f / shipTimeMult);
                    Global.getCombatEngine().getCustomData().put(postProcessKey, new Object());
                    if (activateSound != null) {
                        activateSound.setPitch(II_Util.lerp(2.5f, 0.5f, effectSqrt));
                        activateSound.setVolume(II_Util.lerp(0f, 1f, effectSqrt));
                        activateSound.setLocation(centerLocation.x, centerLocation.y);
                    }
                    Global.getSoundPlayer().playLoop("ii_celerity_loop", this, TIME_MULT_ACTIVE_START / shipTimeMult,
                            II_Util.lerp(0.5f, effectSqrt, Math.min(1f, 4f * (1f - effectLevel))), centerLocation, ZERO);
                } else {
                    Global.getCombatEngine().getTimeMult().unmodify(globalId);
                    if (Global.getCombatEngine().getCustomData().containsKey(postProcessKey)) {
                        Global.getCombatEngine().getCustomData().remove(postProcessKey);
                        PostProcessShader.resetDefaults();
                    }
                    if (activateSound != null) {
                        activateSound.setVolume(II_Util.lerp(0f, 1f, effectSqrt));
                        activateSound.setLocation(centerLocation.x, centerLocation.y);
                    }
                    Global.getSoundPlayer().playLoop("ii_celerity_loop", this, 2f, effectSqrt * 0.5f, centerLocation, ZERO);
                }

                if (ship.getVariant().hasHullMod(II_BasePackage.TARGETING_PACKAGE)) {
                    float mult = II_Util.lerp(1f, TARGETING_WEAPON_FLUX_MULT, effectSqrt);
                    stats.getBallisticWeaponFluxCostMod().modifyMult(id, mult);
                    stats.getEnergyWeaponFluxCostMod().modifyMult(id, mult);
                    stats.getMissileWeaponFluxCostMod().modifyMult(id, mult);
                    if (Global.getCombatEngine().getPlayerShip() == ship) {
                        Global.getCombatEngine().maintainStatusForPlayerShip(STATUSKEY1, ship.getSystem().getSpecAPI().getIconSpriteName(),
                                ship.getSystem().getDisplayName(), "flux use -" + (int) Math.round((1f - mult) * 100f) + "%", false);
                    }
                }

                Color jitterColor = new Color(JITTER_COLOR.getRed(), JITTER_COLOR.getGreen(), JITTER_COLOR.getBlue(),
                        II_Util.clamp255(Math.round(jitterIntensity * effectSqrt * JITTER_COLOR.getAlpha())));
                Color jitterUnderColor = new Color(JITTER_UNDER_COLOR.getRed(), JITTER_UNDER_COLOR.getGreen(), JITTER_UNDER_COLOR.getBlue(),
                        II_Util.clamp255(Math.round(jitterUnderIntensity * effectSqrt * JITTER_UNDER_COLOR.getAlpha())));
                ship.setJitter(this, jitterColor, 1f, 1, 5f, jitterIntensity * (8f * effectSqrt));
                ship.setJitterUnder(this, jitterUnderColor, 1f, 10, 5f, jitterUnderIntensity * (12f * effectSqrt));

                interval.advance(amount);
                if (interval.intervalElapsed()) {
                    float randRange = (float) Math.sqrt(shipRadius);

                    Vector2f randLoc = MathUtils.getRandomPointInCircle(ZERO, randRange * afterImageSpread);
                    Vector2f vel = new Vector2f(ship.getVelocity());
                    vel.scale(-1f);

                    Color afterImageColor = new Color(AFTERIMAGE_COLOR.getRed(), AFTERIMAGE_COLOR.getGreen(), AFTERIMAGE_COLOR.getBlue(),
                            II_Util.clamp255(Math.round(effectSqrt * afterImageIntensity * AFTERIMAGE_COLOR.getAlpha())));
                    ship.addAfterimage(afterImageColor, randLoc.x, randLoc.y, vel.x, vel.y, randRange * afterImageJitter,
                            0.05f * afterImageDuration, 0.2f * afterImageDuration * shipTimeMult, 0.05f * afterImageDuration * shipTimeMult, true, false, false);
                }
                break;
            }
            default:
                break;
        }
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        if (!(stats.getEntity() instanceof ShipAPI)) {
            return;
        }
        ShipAPI ship = (ShipAPI) stats.getEntity();

        stats.getTimeMult().unmodify(id);
        stats.getVentRateMult().unmodify(id);
        stats.getArmorDamageTakenMult().unmodify(id);
        stats.getHullDamageTakenMult().unmodify(id);
        stats.getBallisticWeaponFluxCostMod().unmodify(id);
        stats.getEnergyWeaponFluxCostMod().unmodify(id);
        stats.getMissileWeaponFluxCostMod().unmodify(id);
        String globalId = id + "_" + ship.getId();
        Global.getCombatEngine().getTimeMult().unmodify(globalId);
        if (Global.getCombatEngine().getCustomData().containsKey(postProcessKey)) {
            Global.getCombatEngine().getCustomData().remove(postProcessKey);
            PostProcessShader.resetDefaults();
        }

        started = false;
        fired = false;
        if (wave != null) {
            DistortionShader.removeDistortion(wave);
            wave = null;
        }
        if (activateSound != null) {
            activateSound.stop();
            activateSound = null;
        }
    }

    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        switch (state) {
            case IN: {
                if (index == 0) {
                    return new StatusData("time flow altered", true);
                }
                break;
            }
            case ACTIVE:
            case OUT: {
                if (index == 0) {
                    return new StatusData("time flow altered", false);
                }
                if (index == 1) {
                    return new StatusData("cloak disabled", true);
                }
                break;
            }
            default:
                break;
        }
        return null;
    }

    @Override
    public boolean isUsable(ShipSystemAPI system, ShipAPI ship) {
        if ((ship != null) && (system != null) && ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
            system.setCooldown(ELITE_COOLDOWN_OVERRIDE);
        }

        return true;
    }

    @Override
    public float getInOverride(ShipAPI ship) {
        if ((ship != null) && ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
            return ELITE_IN_OVERRIDE;
        }
        return -1;
    }

    @Override
    public float getOutOverride(ShipAPI ship) {
        if ((ship != null) && ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
            return ELITE_OUT_OVERRIDE;
        }
        return -1;
    }
}
