package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.BeamEffectPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.IIModPlugin;
import data.scripts.util.II_AnamorphicFlare;
import data.scripts.util.II_Util;
import java.awt.Color;
import java.util.List;
import org.dark.shaders.distortion.DistortionShader;
import org.dark.shaders.distortion.RippleDistortion;
import org.dark.shaders.light.LightShader;
import org.dark.shaders.light.StandardLight;
import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.entities.AnchoredEntity;
import org.lwjgl.util.vector.Vector2f;

public class II_WaveMotionBeamEffect implements BeamEffectPlugin {

    private static final float AREA_DAMAGE = 1000f;
    private static final float AREA_EFFECT = 400f;
    private static final float AREA_EFFECT_INNER = 200f;

    private static final float CHARGEUP_PARTICLE_ANGLE_SPREAD = 360f;
    private static final float CHARGEUP_PARTICLE_BRIGHTNESS = 0.75f;
    private static final float CHARGEUP_PARTICLE_DISTANCE_MAX = 75f;
    private static final float CHARGEUP_PARTICLE_DISTANCE_MIN = 50f;
    private static final float CHARGEUP_PARTICLE_DURATION = 0.1f;
    private static final float CHARGEUP_PARTICLE_SIZE_MAX = 10f;
    private static final float CHARGEUP_PARTICLE_SIZE_MIN = 5f;

    private static final Color COLOR0 = new Color(50, 150, 255, 50);
    private static final Color COLOR1 = new Color(125, 200, 255);
    private static final Color COLOR2 = new Color(205, 240, 255);
    private static final Color COLOR3 = new Color(50, 200, 255, 150);
    private static final Color COLOR4 = new Color(150, 225, 255, 150);

    private static final Vector2f ZERO = new Vector2f();

    private static void damage(BeamAPI beam, Vector2f point, CombatEngineAPI engine, CombatEntityAPI target) {
        if (point == null) {
            return;
        }

        if (IIModPlugin.hasGraphicsLib) {
            StandardLight light = new StandardLight(point, ZERO, ZERO, null);
            light.setColor(COLOR3);
            light.setSize(AREA_EFFECT * 1.1f);
            light.setIntensity(0.5f);
            light.fadeOut(1.25f);
            LightShader.addLight(light);
        }

        List<ShipAPI> ships = II_Util.getShipsWithinRange(point, AREA_EFFECT);
        List<CombatEntityAPI> targets = II_Util.getAsteroidsWithinRange(point, AREA_EFFECT);
        targets.addAll(II_Util.getMissilesWithinRange(point, AREA_EFFECT));
        targets.addAll(ships);

        II_Util.filterObscuredTargets(target, point, targets, true, true, false);

        for (CombatEntityAPI tgt : targets) {
            float distance = II_Util.getActualDistance(point, tgt, true);
            float reduction = 1f;
            if (distance > AREA_EFFECT_INNER) {
                reduction = (AREA_EFFECT - distance) / (AREA_EFFECT - AREA_EFFECT_INNER);
            }

            if (reduction <= 0f) {
                continue;
            }

            boolean shieldHit = false;
            if (tgt instanceof ShipAPI) {
                ShipAPI ship = (ShipAPI) tgt;
                if (ship.getShield() != null && ship.getShield().isWithinArc(point)) {
                    shieldHit = true;
                }
            }

            if (tgt == null) {
                continue;
            }

            Vector2f damagePoint;
            if (shieldHit) {
                ShipAPI ship = (ShipAPI) tgt;
                damagePoint = MathUtils.getPointOnCircumference(null, ship.getShield().getRadius(),
                        VectorUtils.getAngle(ship.getShield().getLocation(), point));
                Vector2f.add(damagePoint, tgt.getLocation(), damagePoint);
            } else {
                Vector2f projection = VectorUtils.getDirectionalVector(tgt.getLocation(), point);
                projection.scale(tgt.getCollisionRadius());
                Vector2f.add(projection, tgt.getLocation(), projection);
                damagePoint = CollisionUtils.getCollisionPoint(point, projection, tgt);
            }
            if (damagePoint == null) {
                damagePoint = point;
            }

            engine.applyDamage(beam, tgt, damagePoint, AREA_DAMAGE * reduction, DamageType.HIGH_EXPLOSIVE, 0f, false, false, beam.getSource(), false);
        }
    }

    private boolean firing = false;
    private boolean impacted = false;
    private final IntervalUtil interval = new IntervalUtil(0.1f, 0.1f);
    private final IntervalUtil interval2 = new IntervalUtil(0.015f, 0.015f);
    private boolean wasZero = true;
    private float level = 0f;
    private float sinceLast = 0f;
    private float sinceLastImpact = 0f;

    @Override
    public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
        if (engine.isPaused()) {
            return;
        }

        Vector2f origin = new Vector2f(beam.getWeapon().getLocation());
        Vector2f offset = new Vector2f(II_WaveMotionEveryFrame.TURRET_OFFSET, 0f);
        VectorUtils.rotate(offset, beam.getWeapon().getCurrAngle(), offset);
        Vector2f.add(offset, origin, origin);

        float shipFacing = beam.getWeapon().getCurrAngle();
        Vector2f shipVelocity = beam.getSource().getVelocity();

        beam.setHitGlow(Global.getSettings().getSprite("ii_fx", "ii_boss_wmg_hit"));

        if (firing) {
            if (beam.getBrightness() < level) {
                firing = false;
                impacted = false;
            }
        } else {
            if (beam.getBrightness() > level) {
                firing = true;
                if (engine.getTotalElapsedTime(false) - sinceLast > 1f) {
                    Global.getSoundPlayer().playSound("ii_boss_wavemotion_fire", 1f, 1.5f, origin, new Vector2f());
                    engine.addHitParticle(origin, ZERO, 500f, 1f, 0.75f, COLOR0);
                    engine.addHitParticle(origin, ZERO, 200f, 5f, 0.2f, COLOR1);
                    engine.addHitParticle(origin, ZERO, 100f, 5f, 0.5f, COLOR2);
                }

                sinceLast = engine.getTotalElapsedTime(false);
            }
        }
        level = beam.getBrightness();

        if (beam.getBrightness() > 0f) {
            Global.getSoundPlayer().playLoop("ii_boss_wavemotion_loop", beam, 1f, beam.getBrightness(), origin, beam.getWeapon().getShip().getVelocity());
        }

        float dur = beam.getDamage().getDpsDuration();
        if (!wasZero) {
            dur = 0;
        }
        wasZero = beam.getDamage().getDpsDuration() <= 0;
        if (dur > 0f) {
            CombatEntityAPI target = beam.getDamageTarget();
            if ((beam.getBrightness() >= 1f) && ((target instanceof ShipAPI) && (((ShipAPI) target).isFighter() || ((ShipAPI) target).isDrone() || ((ShipAPI) target).isHulk()))) {
                float damageScaler = 2f;
                engine.applyDamage(beam, target, beam.getTo(), beam.getDamage().computeDamageDealt(dur) * damageScaler, DamageType.ENERGY, 0f, false, true, beam.getSource(), false);
            }
        }

        interval2.advance(amount);
        if (interval2.intervalElapsed()) {
            Color color = new Color(II_Util.clamp255(MathUtils.getRandomNumberInRange(75, 125)),
                    II_Util.clamp255(MathUtils.getRandomNumberInRange(175, 200)),
                    II_Util.clamp255(MathUtils.getRandomNumberInRange(225, 255)),
                    II_Util.clamp255((int) (255f * beam.getBrightness())));
            engine.addHitParticle(origin, shipVelocity, MathUtils.getRandomNumberInRange(150f, 400f), 1f, MathUtils.getRandomNumberInRange(0.015f, 0.06f), color);
            Global.getCombatEngine().spawnExplosion(origin, shipVelocity, color,
                    beam.getBrightness() * MathUtils.getRandomNumberInRange(50f, 100f), MathUtils.getRandomNumberInRange(0.015f, 0.06f));

            if (!impacted || (beam.getDamageTarget() != null)) {
                engine.addHitParticle(beam.getTo(), shipVelocity, MathUtils.getRandomNumberInRange(200f, 250f), beam.getBrightness(), MathUtils.getRandomNumberInRange(0.015f, 0.06f), color);
            }

            int fringeBrightness = II_Util.clamp255(MathUtils.getRandomNumberInRange(150, 255));
            beam.setFringeColor(new Color(fringeBrightness, fringeBrightness, fringeBrightness, fringeBrightness));

            float beamLen = MathUtils.getDistance(beam.getFrom(), beam.getTo());
            float beamAngle = VectorUtils.getAngle(beam.getFrom(), beam.getTo());
            if (firing) {
                int count = (int) Math.round(beam.getBrightness() * beamLen / 50f);
                for (int i = 0; i < count; i++) {
                    float distance = MathUtils.getRandomNumberInRange(0f, 8f) * beam.getBrightness();
                    float size = MathUtils.getRandomNumberInRange(4f, 6f) * beam.getBrightness();
                    float angle = beamAngle + ((Math.random() > 0.5) ? 90f : -90f) + MathUtils.getRandomNumberInRange(-30f, 30f);
                    Vector2f spawnLocation = MathUtils.getPointOnCircumference(MathUtils.getRandomPointOnLine(beam.getFrom(), beam.getTo()), distance, angle);
                    float speed = beam.getBrightness() * MathUtils.getRandomNumberInRange(150f, 300f);
                    Vector2f particleVelocity = MathUtils.getPointOnCircumference(shipVelocity, speed, angle);
                    engine.addHitParticle(spawnLocation, particleVelocity, size, 1f, MathUtils.getRandomNumberInRange(0.05f, 0.2f),
                            new Color(II_Util.clamp255(MathUtils.getRandomNumberInRange(50, 75)),
                                    II_Util.clamp255(MathUtils.getRandomNumberInRange(100, 125)),
                                    II_Util.clamp255(MathUtils.getRandomNumberInRange(200, 255))));
                }
            } else {
                int count = (int) Math.round(beam.getBrightness() * beamLen / 20f);
                for (int i = 0; i < count; i++) {
                    float distance = MathUtils.getRandomNumberInRange(0f, 32f) * beam.getBrightness();
                    float size = MathUtils.getRandomNumberInRange(4f, 6f);
                    float angle = beamAngle + ((Math.random() > 0.5) ? 90f : -90f);
                    Vector2f spawnLocation = MathUtils.getPointOnCircumference(MathUtils.getRandomPointOnLine(beam.getFrom(), beam.getTo()), distance, angle);
                    float speed = beam.getBrightness() * MathUtils.getRandomNumberInRange(25f, 50f);
                    Vector2f particleVelocity = MathUtils.getPointOnCircumference(shipVelocity, speed, MathUtils.getRandomNumberInRange(-180f, 180f));
                    engine.addHitParticle(spawnLocation, particleVelocity, size, 1f, MathUtils.getRandomNumberInRange(0.1f, 1.2f),
                            new Color(II_Util.clamp255(MathUtils.getRandomNumberInRange(0, 50)),
                                    II_Util.clamp255(MathUtils.getRandomNumberInRange(75, 150)),
                                    II_Util.clamp255(MathUtils.getRandomNumberInRange(200, 255))));
                }
            }

            float empScale = 4f - 3f * (beam.getBrightness() * beam.getBrightness());
            if ((Math.random() < (0.05 * empScale)) && (beamLen > 50f)) {
                float startDist = Math.max(0f, MathUtils.getRandomNumberInRange(-200f, beamLen - 200f));
                float startAngle = beamAngle + ((Math.random() > 0.5) ? 90f : -90f);
                Vector2f startPoint = MathUtils.getPointOnCircumference(MathUtils.getPoint(beam.getFrom(), startDist, beamAngle),
                        MathUtils.getRandomNumberInRange(0f, 8f), startAngle);
                float endDist = Math.min(beamLen, MathUtils.getRandomNumberInRange(startDist + 200f, Math.min(startDist + 600f, beamLen)));
                float endAngle = beamAngle + ((Math.random() > 0.5) ? 90f : -90f);
                Vector2f endPoint = MathUtils.getPointOnCircumference(MathUtils.getPoint(beam.getFrom(), endDist, beamAngle),
                        MathUtils.getRandomNumberInRange(0f, 8f), endAngle);
                engine.spawnEmpArc(beam.getWeapon().getShip(), startPoint, new AnchoredEntity(beam.getWeapon().getShip(), startPoint), new AnchoredEntity(beam.getWeapon().getShip(), endPoint),
                        DamageType.ENERGY, 0f, 0f, 0f, null, MathUtils.getRandomNumberInRange(10f, 15f) * beam.getBrightness(), COLOR1, COLOR2);
            }

            if (firing) {
                int count = (int) (beam.getBrightness() * 5);
                for (int i = 0; i < count; i++) {
                    float distance = MathUtils.getRandomNumberInRange(CHARGEUP_PARTICLE_DISTANCE_MIN, CHARGEUP_PARTICLE_DISTANCE_MAX) * beam.getBrightness();
                    float size = MathUtils.getRandomNumberInRange(CHARGEUP_PARTICLE_SIZE_MIN, CHARGEUP_PARTICLE_SIZE_MAX);
                    float angle = MathUtils.getRandomNumberInRange(-0.5f * CHARGEUP_PARTICLE_ANGLE_SPREAD, 0.5f * CHARGEUP_PARTICLE_ANGLE_SPREAD);
                    Vector2f spawnLocation = MathUtils.getPointOnCircumference(origin, distance, (angle + shipFacing));
                    float speed = distance / CHARGEUP_PARTICLE_DURATION;
                    Vector2f particleVelocity = MathUtils.getPointOnCircumference(shipVelocity, speed, angle + shipFacing);
                    engine.addHitParticle(spawnLocation, particleVelocity, size,
                            CHARGEUP_PARTICLE_BRIGHTNESS * Math.min(beam.getBrightness() + 0.5f, 1f) * MathUtils.getRandomNumberInRange(0.75f, 1.25f),
                            CHARGEUP_PARTICLE_DURATION, color);
                }
            }
        }

        if (!impacted && firing) {
            if ((beam.getDamageTarget() != null) && (beam.getDamageTarget() instanceof ShipAPI)
                    && !((ShipAPI) beam.getDamageTarget()).isFighter() && !((ShipAPI) beam.getDamageTarget()).isDrone()
                    && ((ShipAPI) beam.getDamageTarget()).isAlive()) {
                impacted = true;
                if ((engine.getTotalElapsedTime(false) - sinceLastImpact) > 4.5f) {
                    Global.getSoundPlayer().playSound("ii_boss_wavemotion_impact", 1f, 1f, beam.getTo(), new Vector2f());
                    engine.spawnExplosion(beam.getTo(), ZERO, COLOR3, 300f, 1.5f);
                    engine.addHitParticle(beam.getTo(), ZERO, 400f, 0.5f, 1.25f, COLOR3);
                    engine.addHitParticle(beam.getTo(), ZERO, 500f, 0.5f, 0.1f, COLOR4);
                    engine.addHitParticle(beam.getTo(), ZERO, 150f, 1f, 0.6f, COLOR4);

                    RippleDistortion ripple = new RippleDistortion(beam.getTo(), ZERO);
                    ripple.setSize(400f);
                    ripple.setIntensity(1200f * 0.1f);
                    ripple.setFrameRate(60f / 0.6f);
                    ripple.fadeInSize(1f);
                    ripple.fadeOutIntensity(1f);
                    DistortionShader.addDistortion(ripple);

                    float angle = VectorUtils.getAngle(beam.getFrom(), beam.getTo()) + 90f;
                    II_AnamorphicFlare.createStripFlare(beam.getSource(), new Vector2f(beam.getTo()), engine, 1f, 40, 1f, 10f,
                            angle, 5f, 5f, COLOR3, COLOR4, true);

                    Vector2f vel = new Vector2f();
                    for (int i = 0; i < 60; i++) {
                        vel.set(((float) Math.random() * 1.5f + 0.35f) * AREA_EFFECT, 0f);
                        VectorUtils.rotate(vel, (float) Math.random() * 360f, vel);
                        engine.addSmoothParticle(beam.getTo(), vel, (float) Math.random() * 2.5f + 2.5f, 1f,
                                (float) Math.random() * 0.5f + 0.5f, COLOR3);
                    }

                    damage(beam, beam.getTo(), engine, beam.getDamageTarget());
                }
                sinceLastImpact = engine.getTotalElapsedTime(false);
            }
        }

        interval.advance(amount);
        if (interval.intervalElapsed()) {
            if (beam.getDamageTarget() != null) {
                Global.getCombatEngine().spawnExplosion(new Vector2f(beam.getTo()), ZERO,
                        new Color(II_Util.clamp255(MathUtils.getRandomNumberInRange(75, 125)),
                                II_Util.clamp255(MathUtils.getRandomNumberInRange(150, 200)),
                                255,
                                50),
                        beam.getBrightness() * MathUtils.getRandomNumberInRange(100f, 200f), 0.2f);
                for (int x = 0; x < 6; x++) {
                    float angle = (float) Math.random() * 360f;
                    engine.addHitParticle(new Vector2f(beam.getTo()),
                            MathUtils.getPointOnCircumference(null, (float) Math.random() * 250f + 250f, angle),
                            10f, 1f, (float) Math.random() * 0.2f + 0.2f,
                            new Color(II_Util.clamp255(MathUtils.getRandomNumberInRange(50, 100)),
                                    II_Util.clamp255(MathUtils.getRandomNumberInRange(50, 100)),
                                    255,
                                    255));
                }
            }
        }
    }
}
