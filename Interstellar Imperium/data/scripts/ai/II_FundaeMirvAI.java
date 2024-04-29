package data.scripts.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import data.scripts.util.II_Util;
import data.scripts.weapons.II_FundaeOnHitEffect;
import java.util.List;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class II_FundaeMirvAI extends II_BaseMissile {

    private static final float PROXIMITY_RANGE = 15f;
    private static final Vector2f ZERO = new Vector2f();

    private float detonate;
    private float noEngines = 0.25f;
    private final float baseofftarget;
    private float offtarget;
    private boolean freeTargeting = false;
    private boolean exploded = false;
    private final IntervalUtil timer = new IntervalUtil(0.1f, 0.2f);
    private final IntervalUtil retargetTimer = new IntervalUtil(0.2f, 0.3f);

    public II_FundaeMirvAI(MissileAPI missile, ShipAPI launchingShip) {
        super(missile, launchingShip);

        this.offtarget = 30f * (0.5f - (float) Math.random());
        this.baseofftarget = 30f * (0.5f - (float) Math.random());

        detonate = (float) Math.random() * 0.5f;

        initialTargetingBehavior();
    }

    private void initialTargetingBehavior() {
        target = null;
        freeTargeting = true;
        setTarget(findBestTarget());
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
    protected ShipAPI findBestTarget() {
        List<ShipAPI> ships = AIUtils.getEnemiesOnMap(missile);
        float range = getRemainingRange() + missile.getMaxSpeed();
        int size = ships.size();
        WeightedRandomPicker<ShipAPI> enemies = new WeightedRandomPicker<>();
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
            if (distance < range) {
                float weight = 1f - (distance / range);
                enemies.add(tmp, weight);
            }
        }
        if (enemies.isEmpty()) {
            return null;
        }
        return enemies.pick();
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
                II_FundaeAI.explode(missile, null, new Vector2f(missile.getLocation()), Global.getCombatEngine());
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
                    II_FundaeAI.explode(missile, target, new Vector2f(missile.getLocation()), Global.getCombatEngine());
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
}
