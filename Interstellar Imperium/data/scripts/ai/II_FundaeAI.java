package data.scripts.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.util.II_Util;
import data.scripts.weapons.II_FundaeOnHitEffect;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.dark.shaders.light.LightShader;
import org.dark.shaders.light.StandardLight;
import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

public class II_FundaeAI extends II_BaseMissile {

    private static final float AREA_EFFECT = 50f;
    private static final float AREA_EFFECT_INNER = 15f;
    private static final Color EXPLOSION_COLOR = new Color(255, 200, 100, 150);
    private static final Color FLASH_COLOR = new Color(50, 150, 255, 255);
    private static final int NUM_PARTICLES = 15;
    private static final Color PARTICLE_COLOR = new Color(255, 200, 100, 150);
    private static final float PROXIMITY_RANGE = 15f;
    private static final String SOUND_FILE = "ii_fundae_explosion";
    private static final Vector2f ZERO = new Vector2f();

    private float detonate;
    private float noEngines = 0.25f;
    private final float baseofftarget;
    private float offtarget;
    private boolean freeTargeting = false;
    private boolean exploded = false;
    private final IntervalUtil timer = new IntervalUtil(0.1f, 0.2f);
    private final IntervalUtil retargetTimer = new IntervalUtil(0.2f, 0.3f);

    public II_FundaeAI(MissileAPI missile, ShipAPI launchingShip, boolean useShipTarget) {
        super(missile, launchingShip);

        this.offtarget = 30f * (0.5f - (float) Math.random());
        this.baseofftarget = 30f * (0.5f - (float) Math.random());

        detonate = (float) Math.random() * 0.5f;

        initialTargetingBehavior(launchingShip, useShipTarget);
    }

    private void initialTargetingBehavior(ShipAPI launchingShip, boolean useShipTarget) {
        target = null;
        if (useShipTarget) {
            assignMissileToShipTarget(launchingShip);
        }

        if (target == null) {
            freeTargeting = true;
            setTarget(findBestTarget());
        }
    }

    @Override
    protected CombatEntityAPI getMouseTarget(ShipAPI launchingShip) {
        return null;
    }

    @Override
    protected boolean acquireTarget(float amount) {
        if (target instanceof MissileAPI) {
            if (((MissileAPI) target).isFlare()) {
                freeTargeting = false;
            }
        }
        if (target instanceof ShipAPI) {
            if ((((ShipAPI) target).getVariant() != null) && ((ShipAPI) target).getVariant().hasHullMod("ii_attraction_matrix")) {
                freeTargeting = false;
            }
        }

        retargetTimer.advance(amount);
        if (!isTargetValid(target) || (freeTargeting && retargetTimer.intervalElapsed())) {
            freeTargeting = true;
            setTarget(findBestTarget());
            if (target == null) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected CombatEntityAPI findBestTarget() {
        ShipAPI closest = null;
        float range = getRemainingRange() + missile.getMaxSpeed() * 2f;
        float closestDistance = getRemainingRange() + missile.getMaxSpeed() * 2f;
        List<ShipAPI> ships = Global.getCombatEngine().getShips();
        int size = ships.size();
        for (int i = 0; i < size; i++) {
            ShipAPI tmp = ships.get(i);
            float mod = range * MathUtils.getRandomNumberInRange(-0.15f, 0.15f);
            if (!tmp.isFighter() && !tmp.isDrone()) {
                mod += range * 0.5f;
            }
            if (!isTargetValid(tmp) || (tmp.getOwner() == 100)) {
                continue;
            }
            float distance = MathUtils.getDistance(tmp, missile.getLocation()) + mod;
            if (distance < closestDistance) {
                closest = tmp;
                closestDistance = distance;
            }
        }
        return closest;
    }

    @Override
    public void advance(float amount) {
        if (exploded) {
            return;
        }

        noEngines -= amount;

        if (missile.isFizzling() || missile.isFading()) {
            detonate -= amount;
            if (detonate <= 0f) {
                explode(missile, null, new Vector2f(missile.getLocation()), Global.getCombatEngine());
                //Global.getCombatEngine().removeEntity(missile);
                Global.getCombatEngine().applyDamage(missile, missile.getLocation(), missile.getHitpoints() * 2f, DamageType.FRAGMENTATION, 0f, false, false,
                        missile, false);
                exploded = true;
                return;
            }
        }

        if (target instanceof ShipAPI) {
            ShipAPI ship = (ShipAPI) target;
            if (ship.isFighter() || ship.isDrone()) {
                float distance = II_Util.getActualDistance(missile.getLocation(), target, true);

                if ((distance <= PROXIMITY_RANGE) && (target.getCollisionClass() != CollisionClass.NONE)) {
                    Global.getCombatEngine().applyDamage(missile, target, missile.getLocation(), missile.getDamageAmount(), DamageType.HIGH_EXPLOSIVE, 0f, false, false,
                            missile.getSource(), false);
                    Global.getCombatEngine().applyDamage(missile, target, missile.getLocation(), missile.getDamageAmount() * II_FundaeOnHitEffect.FUNDAE_FRAG_DAMAGE_MULT,
                            DamageType.FRAGMENTATION, 0f, false, false, missile.getSource(), false);
                    explode(missile, target, new Vector2f(missile.getLocation()), Global.getCombatEngine());
                    //Global.getCombatEngine().removeEntity(missile);
                    Global.getCombatEngine().applyDamage(missile, missile.getLocation(), missile.getHitpoints() * 2f, DamageType.FRAGMENTATION, 0f, false, false,
                            missile, false);
                    exploded = true;
                    return;
                }
            }
        }

        if (missile.isFizzling() || missile.isFading() || (noEngines > 0f)) {
            return;
        }

        if (!acquireTarget(amount)) {
            missile.giveCommand(ShipCommand.ACCELERATE);
            return;
        }

        float angularDistance = MathUtils.getShortestRotation(missile.getFacing(), VectorUtils.getAngle(
                missile.getLocation(), target.getLocation()));

        if (timer.intervalElapsed()) {
            offtarget = (offtarget > 0 ? offtarget - 1 : offtarget + 1);
        }

        float distance = MathUtils.getDistance(target.getLocation(), missile.getLocation());
        float offtargetby = (0f + (offtarget + (baseofftarget * target.getCollisionRadius() / 75f)));

        // Make it slightly more accurate
        if (distance <= target.getCollisionRadius() * 2f) {
            offtargetby *= (distance - target.getCollisionRadius() * 1.5f) / target.getCollisionRadius() + 0.5f;
        }

        float AbsAngD = Math.abs(angularDistance - offtargetby);

        // Point towards target
        if (AbsAngD > 0.5) {
            // Makes missile fly off target
            missile.giveCommand(angularDistance > offtargetby ? ShipCommand.TURN_LEFT : ShipCommand.TURN_RIGHT);
        }

        // Course correction
        if (AbsAngD < 5) {
            float MFlightAng = VectorUtils.getAngle(ZERO, missile.getVelocity());
            float MFlightCC = MathUtils.getShortestRotation(missile.getFacing(), MFlightAng);
            if (Math.abs(MFlightCC) > 20) {
                missile.giveCommand(MFlightCC < 0 ? ShipCommand.STRAFE_LEFT : ShipCommand.STRAFE_RIGHT);
            }
        }

        missile.giveCommand(ShipCommand.ACCELERATE);

        if (AbsAngD < 0.4) {
            missile.setAngularVelocity(0);
        }
    }

    public static void explode(MissileAPI missile, CombatEntityAPI target, Vector2f point, CombatEngineAPI engine) {
        if (point == null) {
            return;
        }

        engine.spawnExplosion(point, ZERO, EXPLOSION_COLOR, 50f, 0.5f);
        engine.addSmoothParticle(point, ZERO, 200f, 0.5f, 0.1f, FLASH_COLOR);
        engine.addHitParticle(point, ZERO, 100f, 0.5f, 0.25f, FLASH_COLOR);
        for (int x = 0; x < NUM_PARTICLES; x++) {
            engine.addHitParticle(point,
                    MathUtils.getPointOnCircumference(null, MathUtils.getRandomNumberInRange(50f, 150f), (float) Math.random() * 360f),
                    5f, 1f, MathUtils.getRandomNumberInRange(0.3f, 0.6f), PARTICLE_COLOR);
        }

        StandardLight light = new StandardLight(point, ZERO, ZERO, null);
        light.setColor(EXPLOSION_COLOR);
        light.setSize(AREA_EFFECT * 1.5f);
        light.setIntensity(0.2f);
        light.fadeOut(0.25f);
        LightShader.addLight(light);

        List<CombatEntityAPI> targets = new ArrayList<>(10);
        targets.addAll(II_Util.getMissilesWithinRange(missile.getLocation(), AREA_EFFECT));
        targets.addAll(II_Util.getShipsWithinRange(missile.getLocation(), AREA_EFFECT));
        targets.addAll(II_Util.getAsteroidsWithinRange(missile.getLocation(), AREA_EFFECT));
        targets.remove(target);

        Iterator<CombatEntityAPI> iter = targets.iterator();
        while (iter.hasNext()) {
            CombatEntityAPI tgt = iter.next();
            if (tgt.getCollisionClass() == CollisionClass.NONE) {
                iter.remove();
                continue;
            }

            if (tgt.getOwner() == missile.getOwner()) {
                iter.remove();
                continue;
            }

            if (tgt instanceof ShipAPI) {
                ShipAPI shp = (ShipAPI) tgt;
                if (!shp.isFighter() && !shp.isDrone()) {
                    continue;
                }
            }

            boolean remove = false;
            for (CombatEntityAPI t : targets) {
                if (t.getShield() != null && t != tgt) {
                    if (t.getShield().isWithinArc(tgt.getLocation()) && t.getShield().isOn()
                            && MathUtils.getDistance(tgt.getLocation(), t.getShield().getLocation())
                            <= t.getShield().getRadius()) {
                        remove = true;
                    }
                }
            }

            if (remove) {
                iter.remove();
            }
        }

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
            if (tgt.getShield() != null && tgt.getShield().isWithinArc(point)) {
                shieldHit = true;
            }

            Vector2f damagePoint;
            if (shieldHit) {
                damagePoint = MathUtils.getPointOnCircumference(null, tgt.getShield().getRadius(), VectorUtils.getAngle(
                        tgt.getShield().getLocation(), point));
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
            engine.applyDamage(missile, tgt, damagePoint, missile.getDamageAmount() * II_FundaeOnHitEffect.FUNDAE_FRAG_DAMAGE_MULT * reduction,
                    DamageType.FRAGMENTATION, 0f, false, false, missile.getSource(), false);
        }

        Global.getSoundPlayer().playSound(SOUND_FILE, 1f, 1f, point, ZERO);
    }
}
