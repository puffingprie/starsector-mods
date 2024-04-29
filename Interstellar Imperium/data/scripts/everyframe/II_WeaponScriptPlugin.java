package data.scripts.everyframe;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import data.scripts.hullmods.II_BasePackage;
import data.scripts.shipsystems.II_ArbalestLoaderStats;
import data.scripts.shipsystems.II_MagnumSalvoStats;
import data.scripts.util.II_Util;
import java.awt.Color;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.dark.shaders.distortion.DistortionAPI;
import org.dark.shaders.distortion.DistortionShader;
import org.dark.shaders.distortion.WaveDistortion;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class II_WeaponScriptPlugin extends BaseEveryFrameCombatPlugin {

    private static final String DATA_KEY = "II_WeaponScriptPlugin";

    private static final Color REPEATER_MUZZLE_FLASH_COLOR = new Color(255, 175, 100, 255);
    private static final float REPEATER_MUZZLE_FLASH_DURATION = 0.15f;
    private static final float REPEATER_MUZZLE_FLASH_SIZE = 30.0f;

    private static final float MAGNA_FULMEN_ARMOR_ENHANCED_MAX_SPREAD = 24f;
    private static final float MAGNA_FULMEN_ARMOR_ENHANCED_MIN_SPREAD = 8f;

    private static final Color HEAVY_PULSAR_COLOR = new Color(255, 100, 0, 255);
    private static final Color PHOTON_COLOR = new Color(255, 175, 25, 50);
    private static final Color PULSAR_BOMB_COLOR = new Color(255, 50, 0, 255);
    private static final Color PULSAR_COLOR = new Color(255, 150, 0, 200);
    private static final Color SOLIS_COLOR = new Color(255, 175, 0, 100);
    private static final Vector2f ZERO = new Vector2f();

    private CombatEngineAPI engine;
    private final IntervalUtil interval = new IntervalUtil(0.015f, 0.015f);

    private static ShipAPI findBestTarget(DamagingProjectileAPI proj) {
        ShipAPI source = proj.getSource();
        if ((source != null) && (source.getShipTarget() != null)
                && !source.getShipTarget().isFighter() && !source.getShipTarget().isDrone() && !source.getShipTarget().isShuttlePod()) {
            float angleDif = Math.abs(MathUtils.getShortestRotation(VectorUtils.getAngle(proj.getLocation(), source.getShipTarget().getLocation()), proj.getFacing()));
            if (source.getShipTarget().isAlive() && (angleDif <= (MAGNA_FULMEN_ARMOR_ENHANCED_MAX_SPREAD * 0.5f))) {
                return source.getShipTarget();
            }
        }
        if ((source != null) && (source.getMouseTarget() != null)) {
            float angleDif = Math.abs(MathUtils.getShortestRotation(VectorUtils.getAngle(proj.getLocation(), source.getMouseTarget()), proj.getFacing()));
            if (angleDif <= (MAGNA_FULMEN_ARMOR_ENHANCED_MAX_SPREAD * 0.5f)) {
                ShipAPI largest = null;
                float largestRadius = 0f;

                for (ShipAPI tmp : AIUtils.getEnemiesOnMap(source)) {
                    if (tmp.isFighter() || tmp.isDrone() || tmp.isShuttlePod()) {
                        continue;
                    }

                    float distance = MathUtils.getDistance(tmp, source.getMouseTarget());
                    float targetingRadius = Misc.getTargetingRadius(source.getMouseTarget(), tmp, false);
                    if (((distance <= 100f) || ((angleDif <= (MAGNA_FULMEN_ARMOR_ENHANCED_MAX_SPREAD * 0.25f)) && (distance <= 200f))) && (targetingRadius > largestRadius)) {
                        largest = tmp;
                        largestRadius = targetingRadius;
                    }
                }

                if (largest != null) {
                    return largest;
                }
            }
        }
        return null;
    }

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if (engine == null) {
            return;
        }

        if (engine.isPaused()) {
            return;
        }

        final LocalData localData = (LocalData) engine.getCustomData().get(DATA_KEY);
        final Map<DamagingProjectileAPI, ProjectileInfo> projectiles = localData.projectiles;
        final Set<DamagingProjectileAPI> repeaterShots = localData.repeaterShots;

        List<DamagingProjectileAPI> activeProjectiles = engine.getProjectiles();
        int projectilesSize = activeProjectiles.size();
        for (int i = 0; i < projectilesSize; i++) {
            DamagingProjectileAPI projectile = activeProjectiles.get(i);
            if ((projectile.getProjectileSpecId() == null) || projectile.didDamage()) {
                continue;
            }

            switch (projectile.getProjectileSpecId()) {
                case "ii_flare_base": {
                    ShipAPI source = projectile.getSource();
                    Vector2f sourceVel = null;
                    int type = 0;
                    if (source != null) {
                        sourceVel = source.getVelocity();
                        if (source.getVariant().hasHullMod(II_BasePackage.ARMOR_PACKAGE)) {
                            type = 1;
                        } else if (source.getVariant().hasHullMod(II_BasePackage.TARGETING_PACKAGE)) {
                            type = 2;
                        } else if (source.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
                            type = 3;
                        }
                    }
                    switch (type) {
                        default:
                        case 0:
                            engine.spawnProjectile(source, projectile.getWeapon(), "ii_flares_standard", projectile.getLocation(),
                                    projectile.getFacing() + 30f + MathUtils.getRandomNumberInRange(-15f, 15f), sourceVel);
                            engine.spawnProjectile(source, projectile.getWeapon(), "ii_flares_standard", projectile.getLocation(),
                                    projectile.getFacing() - 30f + MathUtils.getRandomNumberInRange(-15f, 15f), sourceVel);
                            Global.getSoundPlayer().playSound("launch_flare_1", 1f, 1f, projectile.getLocation(), sourceVel);
                            break;
                        case 1:
                            engine.spawnProjectile(source, projectile.getWeapon(), "ii_flares_armor", projectile.getLocation(),
                                    projectile.getFacing() + 45f + MathUtils.getRandomNumberInRange(-5f, 5f), sourceVel);
                            engine.spawnProjectile(source, projectile.getWeapon(), "ii_flares_armor", projectile.getLocation(),
                                    projectile.getFacing() + MathUtils.getRandomNumberInRange(-5f, 5f), sourceVel);
                            engine.spawnProjectile(source, projectile.getWeapon(), "ii_flares_armor", projectile.getLocation(),
                                    projectile.getFacing() - 45f + MathUtils.getRandomNumberInRange(-5f, 5f), sourceVel);
                            Global.getSoundPlayer().playSound("launch_flare_1", 1f, 1f, projectile.getLocation(), sourceVel);
                            break;
                        case 2:
                            engine.spawnProjectile(source, projectile.getWeapon(), "ii_flares_targeting", projectile.getLocation(),
                                    projectile.getFacing() + MathUtils.getRandomNumberInRange(-45f, 45f), sourceVel);
                            Global.getSoundPlayer().playSound("system_flare_launcher_active", 1f, 1f, projectile.getLocation(), sourceVel);
                            break;
                        case 3:
                            engine.spawnProjectile(source, projectile.getWeapon(), "ii_flares_elite", projectile.getLocation(),
                                    projectile.getFacing() + MathUtils.getRandomNumberInRange(-45f, 45f), sourceVel);
                            Global.getSoundPlayer().playSound("system_flare_launcher_active", 1f, 1f, projectile.getLocation(), sourceVel);
                            break;
                    }
                    engine.removeEntity(projectile);
                    break;
                }
                case "ii_armageddon_base": {
                    ShipAPI source = projectile.getSource();
                    boolean replaced = false;
                    Vector2f sourceVel = null;
                    if (source != null) {
                        sourceVel = source.getVelocity();

                        int armed = II_MagnumSalvoStats.getArmed(source);
                        if (armed > 0) {
                            II_MagnumSalvoStats.setArmed(source, armed - 1);

                            int type = 0;
                            if (source.getVariant().hasHullMod(II_BasePackage.ARMOR_PACKAGE)) {
                                type = 1;
                            } else if (source.getVariant().hasHullMod(II_BasePackage.TARGETING_PACKAGE)) {
                                type = 2;
                            } else if (source.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
                                type = 3;
                            }

                            switch (type) {
                                default:
                                case 0:
                                    engine.spawnProjectile(source, projectile.getWeapon(), "ii_armageddon_standard", projectile.getLocation(),
                                            projectile.getFacing(), sourceVel);
                                    engine.spawnExplosion(projectile.getLocation(), sourceVel, II_MagnumSalvoStats.GLOW_COLOR_STANDARD, 75f, 0.1f);
                                    Global.getSoundPlayer().playSound("ii_armageddon_fire", 0.9f, 1.1f, projectile.getLocation(), ZERO);
                                    break;
                                case 1:
                                    engine.spawnProjectile(source, projectile.getWeapon(), "ii_armageddon_armor", projectile.getLocation(),
                                            projectile.getFacing(), sourceVel);
                                    engine.spawnExplosion(projectile.getLocation(), sourceVel, II_MagnumSalvoStats.GLOW_COLOR_ARMOR, 100f, 0.1f);
                                    Global.getSoundPlayer().playSound("ii_armageddon_fire", 0.7f, 1.3f, projectile.getLocation(), ZERO);
                                    break;
                                case 2:
                                    engine.spawnProjectile(source, projectile.getWeapon(), "ii_armageddon_targeting", projectile.getLocation(),
                                            projectile.getFacing(), sourceVel);
                                    engine.spawnExplosion(projectile.getLocation(), sourceVel, II_MagnumSalvoStats.GLOW_COLOR_TARGETING, 75f, 0.1f);
                                    Global.getSoundPlayer().playSound("ii_armageddon_fire", 1.1f, 1.2f, projectile.getLocation(), ZERO);
                                    break;
                                case 3:
                                    engine.spawnProjectile(source, projectile.getWeapon(), "ii_armageddon_elite", projectile.getLocation(),
                                            projectile.getFacing(), sourceVel);
                                    engine.spawnExplosion(projectile.getLocation(), sourceVel, II_MagnumSalvoStats.GLOW_COLOR_ELITE, 75f, 0.1f);
                                    Global.getSoundPlayer().playSound("ii_armageddon_fire", 1.2f, 1.3f, projectile.getLocation(), ZERO);
                                    break;
                            }
                            engine.removeEntity(projectile);
                            replaced = true;
                        }
                    }
                    if (!replaced) {
                        engine.spawnProjectile(source, projectile.getWeapon(), "ii_armageddon_normal", projectile.getLocation(),
                                projectile.getFacing(), sourceVel);
                        Global.getSoundPlayer().playSound("ii_armageddon_fire", 1f, 1f, projectile.getLocation(), ZERO);
                        engine.removeEntity(projectile);
                    }
                    break;
                }
                case "ii_pulsar_shot": {
                    ShipAPI source = projectile.getSource();
                    WeaponAPI sourceWeapon = projectile.getWeapon();
                    if (!repeaterShots.contains(projectile) && (sourceWeapon != null) && sourceWeapon.getId().contentEquals("ii_pulsarrepeater")) {
                        Vector2f srcVel = ZERO;
                        if (source != null) {
                            srcVel = source.getVelocity();
                        }
                        engine.spawnExplosion(projectile.getLocation(), srcVel, REPEATER_MUZZLE_FLASH_COLOR, REPEATER_MUZZLE_FLASH_SIZE, REPEATER_MUZZLE_FLASH_DURATION);
                        repeaterShots.add(projectile);
                    }
                    break;
                }
                case "ii_magna_fulmen_base": {
                    ShipAPI source = projectile.getSource();
                    boolean replaced = false;
                    Vector2f sourceVel = null;
                    if (source != null) {
                        sourceVel = source.getVelocity();

                        int armed = II_ArbalestLoaderStats.getArmed(source);
                        if (armed > 0) {
                            int type = 0;
                            if (source.getVariant().hasHullMod(II_BasePackage.ARMOR_PACKAGE)) {
                                type = 1;
                            } else if (source.getVariant().hasHullMod(II_BasePackage.TARGETING_PACKAGE)) {
                                type = 2;
                            } else if (source.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
                                type = Math.min(6, 2 + armed);
                            }

                            switch (type) {
                                default:
                                case 0:
                                    break;
                                case 1: {
                                    float spreadArc = MAGNA_FULMEN_ARMOR_ENHANCED_MAX_SPREAD;
                                    ShipAPI target = findBestTarget(projectile);
                                    if (target != null) {
                                        Vector2f viewPoint1 = new Vector2f(target.getCollisionRadius() * 2f, 0f);
                                        VectorUtils.rotate(viewPoint1, projectile.getFacing() + 90f);
                                        Vector2f.add(viewPoint1, target.getLocation(), viewPoint1);
                                        float targetingRadius1 = Misc.getTargetingRadius(viewPoint1, target, true);
                                        Vector2f viewPoint2 = new Vector2f(target.getCollisionRadius() * 2f, 0f);
                                        VectorUtils.rotate(viewPoint2, projectile.getFacing() - 90f);
                                        Vector2f.add(viewPoint2, target.getLocation(), viewPoint2);
                                        float targetingRadius2 = Misc.getTargetingRadius(viewPoint2, target, true);

                                        float distanceToTgt = MathUtils.getDistance(projectile.getLocation(), target.getLocation());
                                        float visibleArc1 = (float) Math.abs(Math.toDegrees(Math.atan(targetingRadius1 / Math.max(1f, distanceToTgt))));
                                        float visibleArc2 = (float) Math.abs(Math.toDegrees(Math.atan(targetingRadius2 / Math.max(1f, distanceToTgt))));
                                        spreadArc = Math.max(Math.min(spreadArc, (visibleArc1 + visibleArc2) * 0.8f), MAGNA_FULMEN_ARMOR_ENHANCED_MIN_SPREAD);
                                    }
                                    for (int j = -2; j < 3; j++) {
                                        if (j == 0) {
                                            engine.spawnProjectile(source, projectile.getWeapon(), "ii_magna_fulmen_standard_armor", projectile.getLocation(),
                                                    projectile.getFacing() + (spreadArc * j / 4f), sourceVel);
                                        } else {
                                            engine.spawnProjectile(source, projectile.getWeapon(), "ii_magna_fulmen_enhanced_armor", projectile.getLocation(),
                                                    projectile.getFacing() + (spreadArc * j / 4f), sourceVel);
                                        }
                                    }
                                    engine.removeEntity(projectile);
                                    replaced = true;
                                    break;
                                }
                                case 2:
                                    engine.spawnProjectile(source, projectile.getWeapon(), "ii_magna_fulmen_enhanced_targeting", projectile.getLocation(),
                                            projectile.getFacing(), sourceVel);
                                    engine.removeEntity(projectile);
                                    replaced = true;
                                    break;
                                case 3:
                                    /* Elite I */
                                    engine.spawnProjectile(source, projectile.getWeapon(), "ii_magna_fulmen_enhanced_elite1", projectile.getLocation(),
                                            projectile.getFacing(), sourceVel);
                                    engine.removeEntity(projectile);
                                    replaced = true;
                                    break;
                                case 4:
                                    /* Elite II */
                                    engine.spawnProjectile(source, projectile.getWeapon(), "ii_magna_fulmen_enhanced_elite2", projectile.getLocation(),
                                            projectile.getFacing(), sourceVel);
                                    engine.removeEntity(projectile);
                                    replaced = true;
                                    break;
                                case 5:
                                    /* Elite III */
                                    engine.spawnProjectile(source, projectile.getWeapon(), "ii_magna_fulmen_enhanced_elite3", projectile.getLocation(),
                                            projectile.getFacing(), sourceVel);
                                    engine.removeEntity(projectile);
                                    replaced = true;
                                    break;
                                case 6:
                                    /* Elite IV */
                                    engine.spawnProjectile(source, projectile.getWeapon(), "ii_magna_fulmen_enhanced_elite4", projectile.getLocation(),
                                            projectile.getFacing(), sourceVel);
                                    engine.removeEntity(projectile);
                                    replaced = true;
                                    break;
                            }
                        }
                    }
                    if (!replaced) {
                        if ((source != null) && source.getVariant().hasHullMod(II_BasePackage.ARMOR_PACKAGE)) {
                            engine.spawnProjectile(source, projectile.getWeapon(), "ii_magna_fulmen_standard_armor", projectile.getLocation(),
                                    projectile.getFacing(), sourceVel);
                        } else if ((source != null) && source.getVariant().hasHullMod(II_BasePackage.TARGETING_PACKAGE)) {
                            engine.spawnProjectile(source, projectile.getWeapon(), "ii_magna_fulmen_standard_targeting", projectile.getLocation(),
                                    projectile.getFacing(), sourceVel);
                        } else if ((source != null) && source.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
                            engine.spawnProjectile(source, projectile.getWeapon(), "ii_magna_fulmen_standard_elite", projectile.getLocation(),
                                    projectile.getFacing(), sourceVel);
                        } else {
                            engine.spawnProjectile(source, projectile.getWeapon(), "ii_magna_fulmen_standard", projectile.getLocation(),
                                    projectile.getFacing(), sourceVel);
                        }
                        engine.removeEntity(projectile);
                    }
                    break;
                }
                case "ii_magna_fulmen_enhanced_targeting": {
                    if (!projectiles.containsKey(projectile)) {
                        WaveDistortion wave = new WaveDistortion(projectile.getLocation(), ZERO);
                        wave.setIntensity(10f);
                        wave.setSize(150f);
                        wave.fadeInIntensity(0.2f);
                        wave.flip(true);
                        DistortionShader.addDistortion(wave);
                        projectiles.put(projectile, new ProjectileInfo(wave));
                    }
                    if (Math.random() < (amount * 10)) {
                        Vector2f particleLoc = new Vector2f(MathUtils.getRandomNumberInRange(50f, 100f), 0f);
                        VectorUtils.rotate(particleLoc, MathUtils.getRandomNumberInRange(-180f, 180f));
                        Vector2f.add(projectile.getLocation(), particleLoc, particleLoc);
                        Vector2f particleVel = new Vector2f(MathUtils.getRandomNumberInRange(0f, 50f), 0f);
                        VectorUtils.rotate(particleVel, MathUtils.getRandomNumberInRange(-180f, 180f));
                        Vector2f.add(projectile.getVelocity(), particleVel, particleVel);
                        engine.addSmoothParticle(particleLoc, particleVel, MathUtils.getRandomNumberInRange(10f, 15f),
                                MathUtils.getRandomNumberInRange(0.75f, 1.25f), MathUtils.getRandomNumberInRange(0.4f, 0.7f), II_ArbalestLoaderStats.GLOW_COLOR_TARGETING);

                    }
                    break;
                }
                default:
                    break;
            }
        }

        interval.advance(amount);
        if (interval.intervalElapsed()) {
            Iterator<DamagingProjectileAPI> iter = repeaterShots.iterator();
            while (iter.hasNext()) {
                DamagingProjectileAPI projectile = iter.next();
                if (!engine.isEntityInPlay(projectile)) {
                    iter.remove();
                }
            }

            double particleGenCount = 0.0;

            for (int i = 0; i < projectilesSize; i++) {
                DamagingProjectileAPI projectile = activeProjectiles.get(i);
                if (projectile.getProjectileSpecId() == null || projectile.didDamage() || projectile.isFading()) {
                    continue;
                }

                if (projectile.getProjectileSpecId().contentEquals("ii_photonblaster_shot")) {
                    if (II_Util.isOnscreen(projectile.getLocation(), projectile.getVelocity().length() * 0.15f)) {
                        particleGenCount += 0.65 * 0.15;
                    }
                }

                if (projectile.getProjectileSpecId().contentEquals("ii_solis_shot")) {
                    if (II_Util.isOnscreen(projectile.getLocation(), projectile.getVelocity().length() * 0.05f)) {
                        particleGenCount += 0.3 * 0.05;
                    }
                }

                if (projectile.getProjectileSpecId().contentEquals("ii_pulsar_shot")) {
                    if (II_Util.isOnscreen(projectile.getLocation(),
                            (projectile.getVelocity().length() * 0.1f * 0.75f)
                            + (15f * 0.75f))) {
                        particleGenCount += 0.2 * 0.55;
                    }
                }

                if (projectile.getProjectileSpecId().contentEquals("ii_heavypulsar_shot")) {
                    if (II_Util.isOnscreen(projectile.getLocation(),
                            (projectile.getVelocity().length() * 0.1f * 0.9f)
                            + (30f * 0.9f))) {
                        particleGenCount += 0.3 * 0.7;
                    }
                }

                if (projectile.getProjectileSpecId().contentEquals("ii_pulsarbomb_shot")) {
                    if (II_Util.isOnscreen(projectile.getLocation(),
                            (projectile.getVelocity().length() * 0.1f * 1.05f)
                            + (45f * 1.05f))) {
                        particleGenCount += 0.4 * 0.85;
                    }
                }
            }

            double ratio = 10.0 / Math.max(10.0, particleGenCount);
            for (int i = 0; i < projectilesSize; i++) {
                DamagingProjectileAPI projectile = activeProjectiles.get(i);
                if (projectile.getProjectileSpecId() == null || projectile.didDamage() || projectile.isFading()) {
                    continue;
                }

                if (projectile.getProjectileSpecId().contentEquals("ii_photonblaster_shot")) {
                    if (II_Util.isOnscreen(projectile.getLocation(), projectile.getVelocity().length() * 0.15f)) {
                        if (Math.random() < (0.65 * ratio)) {
                            engine.addHitParticle(projectile.getLocation(), projectile.getVelocity(), 90f, 0.1f, 0.15f,
                                    PHOTON_COLOR);
                        }
                    }
                }

                if (projectile.getProjectileSpecId().contentEquals("ii_solis_shot")) {
                    if (II_Util.isOnscreen(projectile.getLocation(), projectile.getVelocity().length() * 0.05f)) {
                        if (Math.random() < (0.3 * ratio)) {
                            engine.addHitParticle(projectile.getLocation(), projectile.getVelocity(), 75f, 0.2f, 0.05f,
                                    SOLIS_COLOR);
                        }
                    }
                }

                if (projectile.getProjectileSpecId().contentEquals("ii_pulsar_shot")) {
                    if (II_Util.isOnscreen(projectile.getLocation(),
                            (projectile.getVelocity().length() * 0.1f * 0.75f)
                            + (15f * 0.75f))) {
                        if (Math.random() < (0.2 * ratio)) {
                            Vector2f vel = new Vector2f(MathUtils.getRandomNumberInRange(10f, 15f), 0f);
                            VectorUtils.rotate(vel, MathUtils.getRandomNumberInRange(0f, 360f), vel);
                            Vector2f inheritVel = new Vector2f(projectile.getVelocity());
                            inheritVel.scale(MathUtils.getRandomNumberInRange(0f, 0.1f));
                            Vector2f.add(vel, inheritVel, vel);

                            Vector2f pos = new Vector2f(MathUtils.getRandomNumberInRange(0f, 8.75f), 0f);
                            VectorUtils.rotate(pos, MathUtils.getRandomNumberInRange(0f, 360f), pos);
                            Vector2f.add(pos, projectile.getLocation(), pos);

                            engine.addHitParticle(pos, vel, 4f, 1f,
                                    MathUtils.getRandomNumberInRange(0.35f, 0.75f),
                                    PULSAR_COLOR);
                        }
                    }
                }

                if (projectile.getProjectileSpecId().contentEquals("ii_heavypulsar_shot")) {
                    if (II_Util.isOnscreen(projectile.getLocation(),
                            (projectile.getVelocity().length() * 0.1f * 0.9f)
                            + (30f * 0.9f))) {
                        if (Math.random() < (0.3 * ratio)) {
                            Vector2f vel = new Vector2f(MathUtils.getRandomNumberInRange(20f, 30f), 0f);
                            VectorUtils.rotate(vel, MathUtils.getRandomNumberInRange(0f, 360f), vel);
                            Vector2f inheritVel = new Vector2f(projectile.getVelocity());
                            inheritVel.scale(MathUtils.getRandomNumberInRange(0f, 0.1f));
                            Vector2f.add(vel, inheritVel, vel);

                            Vector2f pos = new Vector2f(MathUtils.getRandomNumberInRange(0f, 12.5f), 0f);
                            VectorUtils.rotate(pos, MathUtils.getRandomNumberInRange(0f, 360f), pos);
                            Vector2f.add(pos, projectile.getLocation(), pos);

                            engine.addHitParticle(pos, vel, 4f, 1f,
                                    MathUtils.getRandomNumberInRange(0.5f, 0.9f),
                                    HEAVY_PULSAR_COLOR);
                        }
                    }
                }

                if (projectile.getProjectileSpecId().contentEquals("ii_pulsarbomb_shot")) {
                    if (II_Util.isOnscreen(projectile.getLocation(),
                            (projectile.getVelocity().length() * 0.1f * 1.05f)
                            + (45f * 1.05f))) {
                        if (Math.random() < (0.4 * ratio)) {
                            Vector2f vel = new Vector2f(MathUtils.getRandomNumberInRange(30f, 45f), 0f);
                            VectorUtils.rotate(vel, MathUtils.getRandomNumberInRange(0f, 360f), vel);
                            Vector2f inheritVel = new Vector2f(projectile.getVelocity());
                            inheritVel.scale(MathUtils.getRandomNumberInRange(0f, 0.1f));
                            Vector2f.add(vel, inheritVel, vel);

                            Vector2f pos = new Vector2f(MathUtils.getRandomNumberInRange(0f, 14.25f), 0f);
                            VectorUtils.rotate(pos, MathUtils.getRandomNumberInRange(0f, 360f), pos);
                            Vector2f.add(pos, projectile.getLocation(), pos);

                            engine.addHitParticle(pos, vel, 4f, 1f,
                                    MathUtils.getRandomNumberInRange(0.65f, 1.05f),
                                    PULSAR_BOMB_COLOR);
                        }
                    }
                }
            }
        }

        Iterator<Map.Entry<DamagingProjectileAPI, ProjectileInfo>> iter = projectiles.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<DamagingProjectileAPI, ProjectileInfo> entry = iter.next();
            DamagingProjectileAPI projectile = entry.getKey();
            ProjectileInfo info = entry.getValue();

            if (projectile.didDamage()) {
                if (info.distortion != null) {
                    if (projectile.getProjectileSpecId().contentEquals("ii_magna_fulmen_enhanced_targeting")) {
                        WaveDistortion wave = (WaveDistortion) info.distortion;
                        if (!wave.isFading()) {
                            wave.fadeOutIntensity(0.2f);
                        }
                    }
                }

                iter.remove();
            } else if (!engine.isEntityInPlay(projectile)) {
                if (info.distortion != null) {
                    if (projectile.getProjectileSpecId().contentEquals("ii_magna_fulmen_enhanced_targeting")) {
                        WaveDistortion wave = (WaveDistortion) info.distortion;
                        wave.setLifetime(0f);
                    }
                }

                iter.remove();
            } else if (info.distortion != null) {
                if (projectile.getProjectileSpecId().contentEquals("ii_magna_fulmen_enhanced_targeting")) {
                    WaveDistortion wave = (WaveDistortion) info.distortion;
                    wave.setLocation(projectile.getLocation());
                }
            } else if (projectile.isFading()) {
                if (info.distortion != null) {
                    if (projectile.getProjectileSpecId().contentEquals("ii_magna_fulmen_enhanced_targeting")) {
                        WaveDistortion wave = (WaveDistortion) info.distortion;
                        if (!wave.isFading()) {
                            wave.fadeOutIntensity(0.5f);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void init(CombatEngineAPI engine) {
        this.engine = engine;
    }

    public static void createIfNeeded() {
        if (Global.getCombatEngine() != null) {
            if (!Global.getCombatEngine().getCustomData().containsKey(DATA_KEY)) {
                Global.getCombatEngine().getCustomData().put(DATA_KEY, new LocalData());
                Global.getCombatEngine().addPlugin(new II_WeaponScriptPlugin());
            }
        }
    }

    private static final class LocalData {

        final Map<DamagingProjectileAPI, ProjectileInfo> projectiles = new LinkedHashMap<>(100);
        final Set<DamagingProjectileAPI> repeaterShots = new LinkedHashSet<>();
    }

    private static class ProjectileInfo {

        DistortionAPI distortion;

        ProjectileInfo(DistortionAPI distortion) {
            this.distortion = distortion;
        }
    }
}
