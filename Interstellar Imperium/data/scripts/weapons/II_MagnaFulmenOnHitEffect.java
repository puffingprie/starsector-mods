package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatAsteroidAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.loading.ProjectileSpawnType;
import data.scripts.shipsystems.II_ArbalestLoaderStats;
import data.scripts.util.II_Multi;
import data.scripts.util.II_Util;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.dark.shaders.distortion.DistortionShader;
import org.dark.shaders.distortion.RippleDistortion;
import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

public class II_MagnaFulmenOnHitEffect implements OnHitEffectPlugin {

    private static final Color EXPLOSION_COLOR_STANDARD = new Color(255, 175, 100);
    private static final Color EXPLOSION_COLOR_ARMOR = new Color(255, 215, 100);
    private static final Color EXPLOSION_COLOR_STANDARD_TARGETING = new Color(100, 175, 255, 200);
    private static final Color EXPLOSION_COLOR_ENHANCED_TARGETING = new Color(100, 175, 255, 150);
    private static final Color EXPLOSION_COLOR_ELITE = new Color(205, 100, 255);
    private static final Color EMP_CORE_COLOR_ELITE = new Color(220, 150, 255);

    private static final Vector2f ZERO = new Vector2f();

    private static void explode(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, CombatEngineAPI engine,
            float areaDamage, float areaEffect, float areaEffectInner) {
        if (point == null) {
            return;
        }

        List<ShipAPI> shipTargets = CombatUtils.getShipsWithinRange(point, areaEffect);
        List<MissileAPI> missileTargets = CombatUtils.getMissilesWithinRange(point, areaEffect);
        if (target instanceof ShipAPI) {
            ShipAPI ship = (ShipAPI) target;
            shipTargets.remove(ship);
        }

        II_Util.filterObscuredTargets(target, point, shipTargets, true, true, false);
        II_Util.filterObscuredTargets(target, point, missileTargets, false, true, false);

        for (ShipAPI tgt : shipTargets) {
            float distance = II_Util.getActualDistance(point, tgt, true);
            float reduction = 1f;
            if (distance > areaEffectInner) {
                reduction = (areaEffect - distance) / (areaEffect - areaEffectInner);
            }
            if (tgt.getOwner() == projectile.getOwner()) {
                reduction *= 0.0f;
            }

            if (reduction <= 0f) {
                continue;
            }

            boolean shieldHit = false;
            if ((tgt.getShield() != null) && tgt.getShield().isWithinArc(point)) {
                shieldHit = true;
            }

            Vector2f damagePoint;
            if (shieldHit) {
                damagePoint = MathUtils.getPointOnCircumference(null, tgt.getShield().getRadius(), VectorUtils.getAngle(tgt.getShield().getLocation(), point));
                Vector2f.add(damagePoint, tgt.getLocation(), damagePoint);
            } else {
                Vector2f projection = VectorUtils.getDirectionalVector(point, tgt.getLocation());
                projection.scale(tgt.getCollisionRadius());
                Vector2f.add(projection, tgt.getLocation(), projection);
                damagePoint = CollisionUtils.getCollisionPoint(point, projection, tgt);
            }
            if (damagePoint == null) {
                damagePoint = point;
            }
            engine.applyDamage(projectile, tgt, damagePoint, areaDamage * reduction, DamageType.FRAGMENTATION, 0f, false, false, projectile.getSource(), false);
        }

        for (MissileAPI tgt : missileTargets) {
            float distance = II_Util.getActualDistance(point, tgt, true);
            float reduction = 1f;
            if (distance > areaEffectInner) {
                reduction = (areaEffect - distance) / (areaEffect - areaEffectInner);
            }
            if (tgt.getOwner() == projectile.getOwner()) {
                reduction *= 0.0f;
            }

            if (reduction <= 0f) {
                continue;
            }

            engine.applyDamage(projectile, tgt, point, areaDamage * reduction, DamageType.FRAGMENTATION, 0f, false, false, projectile.getSource(), false);
        }
    }

    private static void repulse(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point,
            float force, float areaEffect, float areaEffectInner) {
        if (point == null) {
            return;
        }

        List<CombatEntityAPI> entityTargets = CombatUtils.getEntitiesWithinRange(point, areaEffect);
        entityTargets.remove(projectile);
        if (target != null) {
            entityTargets.remove(target);
        }

        II_Util.filterObscuredTargets(target, point, entityTargets, true, true, false);

        List<ShipAPI> toRemove = new ArrayList<>();
        Iterator<CombatEntityAPI> iter = entityTargets.iterator();
        while (iter.hasNext()) {
            CombatEntityAPI entity = iter.next();
            if (entity instanceof DamagingProjectileAPI) {
                DamagingProjectileAPI tgtProj = (DamagingProjectileAPI) entity;
                if ((tgtProj.getProjectileSpecId() != null)
                        && (tgtProj.getProjectileSpecId().contentEquals("ii_magna_fulmen_standard_targeting")
                        || tgtProj.getProjectileSpecId().contentEquals("ii_magna_fulmen_enhanced_targeting"))) {
                    iter.remove();
                }
            }
            if (entity instanceof ShipAPI) {
                ShipAPI ship = (ShipAPI) entity;
                List<ShipAPI> children = II_Multi.getChildren(ship);
                if ((children != null) && !children.isEmpty()) {
                    toRemove.addAll(children);
                }
            }
        }

        entityTargets.removeAll(toRemove);

        for (CombatEntityAPI tgt : entityTargets) {
            float distance = II_Util.getActualDistance(point, tgt, true);
            float reduction = 1f;
            if (distance > areaEffectInner) {
                reduction = (areaEffect - distance) / (areaEffect - areaEffectInner);
            }
            if (tgt.getOwner() == projectile.getOwner()) {
                if (tgt instanceof DamagingProjectileAPI) {
                    reduction *= 0f;
                } else {
                    reduction *= 0.25f;
                }
            }

            if (reduction <= 0f) {
                continue;
            }

            /* Special case for rays */
            if (tgt instanceof DamagingProjectileAPI) {
                DamagingProjectileAPI tgtProj = (DamagingProjectileAPI) tgt;
                if (tgtProj.getSpawnType() == ProjectileSpawnType.BALLISTIC_AS_BEAM) {
                    tgtProj.setFacing(VectorUtils.getAngle(point, tgtProj.getLocation()));
                    continue;
                }
            }

            CombatEntityAPI forceTarget = tgt;
            if (tgt instanceof ShipAPI) {
                forceTarget = II_Multi.getRoot((ShipAPI) tgt);
            }
            Vector2f dir = VectorUtils.getDirectionalVector(point, tgt.getLocation());
            II_Util.applyForce(forceTarget, dir, force * reduction);
        }
    }

    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit,
            ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        if (target instanceof ShipAPI || target instanceof CombatAsteroidAPI) {
            Color EXPLOSION_COLOR;
            Color BANG_COLOR;
            Color PARTICLE_COLOR;
            float EXPLOSION_SIZE;
            float BANG_SIZE;
            float BANG_BRIGHTNESS;
            int NUM_PARTICLES;
            float PARTICLE_SPEED_MULT;
            float EXPLOSION_DURATION;
            float AREA_DAMAGE = 1000f;
            float AREA_EFFECT = 250f;
            float AREA_EFFECT_INNER = 200f;
            float SOUND_PITCH = 1f;
            float SOUND_VOLUME = 0.85f;
            float AUX_SOUND_PITCH = 1f;
            float AUX_SOUND_VOLUME = 1f;
            switch (projectile.getProjectileSpecId()) {
                case "ii_magna_fulmen_standard_armor":
                    EXPLOSION_COLOR = EXPLOSION_COLOR_ARMOR;
                    BANG_COLOR = II_ArbalestLoaderStats.GLOW_COLOR_ARMOR;
                    PARTICLE_COLOR = II_ArbalestLoaderStats.GLOW_COLOR_ARMOR;
                    EXPLOSION_SIZE = 300f;
                    BANG_SIZE = 900f;
                    BANG_BRIGHTNESS = 0.25f;
                    NUM_PARTICLES = 150;
                    PARTICLE_SPEED_MULT = 1.5f;
                    EXPLOSION_DURATION = 0.8f;
                    SOUND_PITCH = 1f;
                    SOUND_VOLUME = 0.75f;
                    break;
                case "ii_magna_fulmen_enhanced_armor":
                    EXPLOSION_COLOR = EXPLOSION_COLOR_ARMOR;
                    BANG_COLOR = II_ArbalestLoaderStats.GLOW_COLOR_ARMOR;
                    PARTICLE_COLOR = II_ArbalestLoaderStats.GLOW_COLOR_ARMOR;
                    EXPLOSION_SIZE = 150f;
                    BANG_SIZE = 450f;
                    BANG_BRIGHTNESS = 0.25f;
                    NUM_PARTICLES = 50;
                    PARTICLE_SPEED_MULT = 1f;
                    EXPLOSION_DURATION = 0.5f;
                    AREA_DAMAGE = 500f;
                    AREA_EFFECT = 200f;
                    AREA_EFFECT_INNER = 150f;
                    SOUND_PITCH = 1.2f;
                    SOUND_VOLUME = 0.45f;
                    AUX_SOUND_PITCH = 1.2f;
                    AUX_SOUND_VOLUME = 0.6f;
                    break;
                case "ii_magna_fulmen_standard_targeting":
                    EXPLOSION_COLOR = EXPLOSION_COLOR_STANDARD_TARGETING;
                    BANG_COLOR = II_ArbalestLoaderStats.GLOW_COLOR_TARGETING;
                    PARTICLE_COLOR = II_ArbalestLoaderStats.GLOW_COLOR_TARGETING;
                    EXPLOSION_SIZE = 200f;
                    BANG_SIZE = 400f;
                    BANG_BRIGHTNESS = 0.3f;
                    NUM_PARTICLES = 75;
                    PARTICLE_SPEED_MULT = 1f;
                    EXPLOSION_DURATION = 0.7f;
                    SOUND_PITCH = 1f;
                    SOUND_VOLUME = 0.7f;
                    AUX_SOUND_PITCH = 1f;
                    AUX_SOUND_VOLUME = 0.6f;
                    break;
                case "ii_magna_fulmen_enhanced_targeting":
                    EXPLOSION_COLOR = EXPLOSION_COLOR_ENHANCED_TARGETING;
                    BANG_COLOR = II_ArbalestLoaderStats.GLOW_COLOR_TARGETING;
                    PARTICLE_COLOR = II_ArbalestLoaderStats.GLOW_COLOR_TARGETING;
                    EXPLOSION_SIZE = 400f;
                    BANG_SIZE = 1600f;
                    BANG_BRIGHTNESS = 0.5f;
                    NUM_PARTICLES = 200;
                    PARTICLE_SPEED_MULT = 3f;
                    EXPLOSION_DURATION = 1f;
                    AREA_EFFECT = 400f;
                    AREA_EFFECT_INNER = 100f;
                    SOUND_PITCH = 0.85f;
                    SOUND_VOLUME = 0.8f;
                    AUX_SOUND_PITCH = 1f;
                    AUX_SOUND_VOLUME = 0.9f;
                    break;
                case "ii_magna_fulmen_standard_elite":
                    EXPLOSION_COLOR = EXPLOSION_COLOR_ELITE;
                    BANG_COLOR = II_ArbalestLoaderStats.GLOW_COLOR_ELITE;
                    PARTICLE_COLOR = II_ArbalestLoaderStats.GLOW_COLOR_ELITE;
                    EXPLOSION_SIZE = 200f;
                    BANG_SIZE = 400f;
                    BANG_BRIGHTNESS = 0.25f;
                    NUM_PARTICLES = 75;
                    PARTICLE_SPEED_MULT = 1.25f;
                    EXPLOSION_DURATION = 0.7f;
                    SOUND_PITCH = 1f;
                    SOUND_VOLUME = 0.8f;
                    AUX_SOUND_PITCH = 1f;
                    AUX_SOUND_VOLUME = 0.6f;
                    break;
                case "ii_magna_fulmen_enhanced_elite1":
                    EXPLOSION_COLOR = EXPLOSION_COLOR_ELITE;
                    BANG_COLOR = II_ArbalestLoaderStats.GLOW_COLOR_ELITE;
                    PARTICLE_COLOR = II_ArbalestLoaderStats.GLOW_COLOR_ELITE;
                    EXPLOSION_SIZE = 250f;
                    BANG_SIZE = 600f;
                    BANG_BRIGHTNESS = 0.3f;
                    NUM_PARTICLES = 100;
                    PARTICLE_SPEED_MULT = 1.5f;
                    EXPLOSION_DURATION = 0.8f;
                    SOUND_PITCH = 0.95f;
                    SOUND_VOLUME = 0.85f;
                    AUX_SOUND_PITCH = 1f;
                    AUX_SOUND_VOLUME = 0.7f;
                    break;
                case "ii_magna_fulmen_enhanced_elite2":
                    EXPLOSION_COLOR = EXPLOSION_COLOR_ELITE;
                    BANG_COLOR = II_ArbalestLoaderStats.GLOW_COLOR_ELITE;
                    PARTICLE_COLOR = II_ArbalestLoaderStats.GLOW_COLOR_ELITE;
                    EXPLOSION_SIZE = 300f;
                    BANG_SIZE = 800f;
                    BANG_BRIGHTNESS = 0.35f;
                    NUM_PARTICLES = 125;
                    PARTICLE_SPEED_MULT = 1.75f;
                    EXPLOSION_DURATION = 0.9f;
                    SOUND_PITCH = 0.9f;
                    SOUND_VOLUME = 0.9f;
                    AUX_SOUND_PITCH = 1f;
                    AUX_SOUND_VOLUME = 0.8f;
                    break;
                case "ii_magna_fulmen_enhanced_elite3":
                    EXPLOSION_COLOR = EXPLOSION_COLOR_ELITE;
                    BANG_COLOR = II_ArbalestLoaderStats.GLOW_COLOR_ELITE;
                    PARTICLE_COLOR = II_ArbalestLoaderStats.GLOW_COLOR_ELITE;
                    EXPLOSION_SIZE = 350f;
                    BANG_SIZE = 1000f;
                    BANG_BRIGHTNESS = 0.4f;
                    NUM_PARTICLES = 150;
                    PARTICLE_SPEED_MULT = 2f;
                    EXPLOSION_DURATION = 1f;
                    SOUND_PITCH = 0.85f;
                    SOUND_VOLUME = 0.95f;
                    AUX_SOUND_PITCH = 1f;
                    AUX_SOUND_VOLUME = 0.9f;
                    break;
                case "ii_magna_fulmen_enhanced_elite4":
                    EXPLOSION_COLOR = EXPLOSION_COLOR_ELITE;
                    BANG_COLOR = II_ArbalestLoaderStats.GLOW_COLOR_ELITE;
                    PARTICLE_COLOR = II_ArbalestLoaderStats.GLOW_COLOR_ELITE;
                    EXPLOSION_SIZE = 400f;
                    BANG_SIZE = 1200f;
                    BANG_BRIGHTNESS = 0.45f;
                    NUM_PARTICLES = 175;
                    PARTICLE_SPEED_MULT = 2.25f;
                    EXPLOSION_DURATION = 1.1f;
                    SOUND_PITCH = 0.8f;
                    SOUND_VOLUME = 1f;
                    AUX_SOUND_PITCH = 1f;
                    AUX_SOUND_VOLUME = 1f;
                    break;
                case "ii_magna_fulmen_standard":
                default:
                    EXPLOSION_COLOR = EXPLOSION_COLOR_STANDARD;
                    BANG_COLOR = II_ArbalestLoaderStats.GLOW_COLOR_STANDARD;
                    PARTICLE_COLOR = II_ArbalestLoaderStats.GLOW_COLOR_STANDARD;
                    EXPLOSION_SIZE = 200f;
                    BANG_SIZE = 400f;
                    BANG_BRIGHTNESS = 0.25f;
                    NUM_PARTICLES = 75;
                    PARTICLE_SPEED_MULT = 1f;
                    EXPLOSION_DURATION = 0.7f;
                    break;
            }
            BANG_COLOR = new Color(BANG_COLOR.getRed(), BANG_COLOR.getGreen(), BANG_COLOR.getBlue(),
                    II_Util.clamp255(Math.round(BANG_COLOR.getAlpha() * 0.15f)));

            float fade = projectile.getDamageAmount() / projectile.getBaseDamageAmount();
            float fadeSqrt = (float) Math.sqrt(fade);

            Global.getSoundPlayer().playSound("ii_magnafulmen_impact", SOUND_PITCH, SOUND_VOLUME * fade, point, ZERO);

            if (projectile.getSource() != null) {
                AREA_DAMAGE *= projectile.getSource().getMutableStats().getEnergyWeaponDamageMult().getModifiedValue();
            }

            float speed = projectile.getVelocity().length();
            for (int x = 0; x < (NUM_PARTICLES * fade); x++) {
                engine.addHitParticle(point,
                        MathUtils.getPointOnCircumference(null,
                                MathUtils.getRandomNumberInRange(0.025f, 0.125f) * speed * fadeSqrt * PARTICLE_SPEED_MULT,
                                MathUtils.getRandomNumberInRange(0f, 360f)),
                        MathUtils.getRandomNumberInRange(7f, 10f), 1f, MathUtils.getRandomNumberInRange(0.75f, 1.25f), PARTICLE_COLOR);
            }
            engine.spawnExplosion(point, new Vector2f(target.getVelocity().x * 0.45f, target.getVelocity().y * 0.45f), EXPLOSION_COLOR, EXPLOSION_SIZE * fadeSqrt, EXPLOSION_DURATION);
            engine.addHitParticle(point, new Vector2f(target.getVelocity().x * 0.45f, target.getVelocity().y * 0.45f), BANG_SIZE * fadeSqrt, BANG_BRIGHTNESS * fade, 0.05f, BANG_COLOR);

            switch (projectile.getProjectileSpecId()) {
                case "ii_magna_fulmen_standard_armor":
                case "ii_magna_fulmen_enhanced_armor": {
                    RippleDistortion ripple = new RippleDistortion(point, new Vector2f(target.getVelocity().x * 0.45f, target.getVelocity().y * 0.45f));
                    ripple.setSize(AREA_EFFECT * fadeSqrt * 1.1f);
                    ripple.setIntensity(AREA_EFFECT * fadeSqrt * 1.1f * 0.05f);
                    ripple.setFrameRate(RippleDistortion.FRAMES / (EXPLOSION_DURATION * 0.5f));
                    ripple.fadeInSize(EXPLOSION_DURATION * 0.5f);
                    ripple.fadeOutIntensity(EXPLOSION_DURATION * 0.5f);
                    DistortionShader.addDistortion(ripple);

                    Global.getSoundPlayer().playSound("ii_magnafulmen_armor_pop", AUX_SOUND_PITCH, AUX_SOUND_VOLUME * fade, point, ZERO);
                    explode(projectile, target, point, engine, AREA_DAMAGE * fade, AREA_EFFECT * fadeSqrt, AREA_EFFECT_INNER * fadeSqrt);

                    /* Extra damage for the enhanced version, since it does more frag than energy; we need to make it
                       at least as dangerous to be hit directly as to be splashed. */
                    if (projectile.getProjectileSpecId().contentEquals("ii_magna_fulmen_enhanced_armor")) {
                        engine.applyDamage(projectile, target, point, AREA_DAMAGE * fade / 2f, DamageType.FRAGMENTATION, 0f, false, false, projectile.getSource(), false);
                    }
                    break;
                }
                case "ii_magna_fulmen_standard_targeting": {
                    RippleDistortion ripple = new RippleDistortion(point, new Vector2f(target.getVelocity().x * 0.45f, target.getVelocity().y * 0.45f));
                    ripple.setSize(EXPLOSION_SIZE * fadeSqrt * 0.75f);
                    ripple.setIntensity(EXPLOSION_SIZE * fadeSqrt * 0.75f * 0.1f);
                    ripple.setCurrentFrame(RippleDistortion.FRAMES - 1);
                    ripple.setFrameRate(RippleDistortion.FRAMES / (EXPLOSION_DURATION * -0.5f));
                    ripple.fadeOutSize(EXPLOSION_DURATION * 0.5f);
                    ripple.fadeInIntensity(EXPLOSION_DURATION * 0.25f);
                    ripple.setLifetime(EXPLOSION_DURATION * 0.25f);
                    ripple.setAutoFadeIntensityTime(EXPLOSION_DURATION * 0.25f);
                    DistortionShader.addDistortion(ripple);

                    Global.getSoundPlayer().playSound("ii_magnafulmen_targeting_knockback", AUX_SOUND_PITCH, AUX_SOUND_VOLUME * fade, point, ZERO);
                    II_Util.applyForce(target, projectile.getVelocity(), 1500f * fade);
                    break;
                }
                case "ii_magna_fulmen_enhanced_targeting": {
                    RippleDistortion ripple = new RippleDistortion(point, new Vector2f(target.getVelocity().x * 0.45f, target.getVelocity().y * 0.45f));
                    ripple.setSize(AREA_EFFECT * fadeSqrt * 1.1f);
                    ripple.setIntensity(AREA_EFFECT * fadeSqrt * 1.1f * 0.15f);
                    ripple.setFrameRate(RippleDistortion.FRAMES / (EXPLOSION_DURATION * 0.65f));
                    ripple.fadeInSize(EXPLOSION_DURATION * 0.65f);
                    ripple.fadeOutIntensity(EXPLOSION_DURATION * 0.65f);
                    DistortionShader.addDistortion(ripple);

                    Global.getSoundPlayer().playSound("ii_magnafulmen_targeting_blast", AUX_SOUND_PITCH, AUX_SOUND_VOLUME * fade, point, ZERO);
                    II_Util.applyForce(target, projectile.getVelocity(), 3000f * fade);

                    repulse(projectile, target, point, 1500f * fade, AREA_EFFECT * fadeSqrt, AREA_EFFECT_INNER * fadeSqrt);
                    engine.addSmoothParticle(point, new Vector2f(target.getVelocity().x * 0.45f, target.getVelocity().y * 0.45f), BANG_SIZE * fadeSqrt / 2f, 1f, EXPLOSION_DURATION * 1.25f, PARTICLE_COLOR);
                    break;
                }
                case "ii_magna_fulmen_standard_elite":
                case "ii_magna_fulmen_enhanced_elite1":
                case "ii_magna_fulmen_enhanced_elite2":
                case "ii_magna_fulmen_enhanced_elite3":
                case "ii_magna_fulmen_enhanced_elite4": {
                    if (target instanceof ShipAPI) {
                        ShipAPI ship = (ShipAPI) target;

                        int numEMP;
                        float empThickness;
                        switch (projectile.getProjectileSpecId()) {
                            default:
                            case "ii_magna_fulmen_standard_elite":
                                numEMP = 4;
                                empThickness = 20f;
                                break;

                            case "ii_magna_fulmen_enhanced_elite1":
                                numEMP = 5;
                                empThickness = 22.5f;
                                break;

                            case "ii_magna_fulmen_enhanced_elite2":
                                numEMP = 6;
                                empThickness = 25f;
                                break;

                            case "ii_magna_fulmen_enhanced_elite3":
                                numEMP = 7;
                                empThickness = 27.5f;
                                break;

                            case "ii_magna_fulmen_enhanced_elite4":
                                numEMP = 8;
                                empThickness = 30f;
                                break;
                        }

                        boolean playSound = false;
                        float emp = projectile.getEmpAmount() / numEMP;
                        float dmg = 0.5f * projectile.getDamageAmount() / numEMP;
                        for (int x = 0; x < Math.round(numEMP * fade); x++) {
                            float pierceChance = ((ShipAPI) target).getHardFluxLevel() - 0.1f;
                            pierceChance *= ship.getMutableStats().getDynamic().getValue(Stats.SHIELD_PIERCED_MULT);

                            boolean piercedShield = shieldHit && (float) Math.random() < pierceChance;
                            if (!shieldHit || piercedShield) {
                                playSound = true;
                                engine.spawnEmpArcPierceShields(projectile.getSource(), point, ship, ship,
                                        DamageType.ENERGY, dmg, emp, 100000f, null, empThickness, PARTICLE_COLOR, EMP_CORE_COLOR_ELITE);
                            }
                        }

                        if (playSound) {
                            Global.getSoundPlayer().playSound("ii_magnafulmen_elite_zap", AUX_SOUND_PITCH, AUX_SOUND_VOLUME * fade, point, ZERO);
                        }
                    }
                    break;
                }
                default:
                    break;
            }
        }
    }
}
