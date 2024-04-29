package data.scripts.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.GuidedMissileAI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import java.awt.Color;
import java.util.List;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class II_BallistaAI extends II_BaseMissile {

    private static final float FINAL_APPROACH_DISTANCE = 1000f;
    private static final float FINAL_APPROACH_SPEED = 400f;
    private static final Color FLARE_COLOR = new Color(148, 148, 224);
    private static final Vector2f ZERO = new Vector2f();

    private final float baseofftarget;
    private float boostTimer = 2f;

    public II_BallistaAI(MissileAPI missile, ShipAPI launchingShip) {
        super(missile, launchingShip);

        this.baseofftarget = 20f * (0.5f - (float) Math.random());
    }

    @Override
    public void advance(float amount) {
        if (missile.isFading() || missile.isFizzling()) {
            return;
        }

        if (!acquireTarget(amount)) {
            missile.giveCommand(ShipCommand.ACCELERATE);
            return;
        }

        float angularDistance = MathUtils.getShortestRotation(missile.getFacing(), VectorUtils.getAngleStrict(
                missile.getLocation(), target.getLocation()));

        float distance = MathUtils.getDistance(target.getLocation(), missile.getLocation());
        float offtargetby = baseofftarget;

        // Make it slightly more accurate
        if (distance <= target.getCollisionRadius() * 2f) {
            offtargetby *= (distance - target.getCollisionRadius() * 1.5f) / target.getCollisionRadius() + 0.5f;
        }

        float maxSpeed = FINAL_APPROACH_SPEED;
        if (missile.getSource() != null) {
            missile.getSource().getMutableStats().getMissileMaxSpeedBonus().computeEffective(maxSpeed);
        }
        Vector2f guidedTarget = intercept(missile.getLocation(), maxSpeed, target.getLocation(),
                target.getVelocity());
        if (guidedTarget == null) {
            Vector2f projection = new Vector2f(target.getVelocity());
            float scalar = distance / missile.getMaxSpeed();
            projection.scale(scalar);
            guidedTarget = Vector2f.add(target.getLocation(), projection, null);
        }

        float velocityFacing = VectorUtils.getFacing(missile.getVelocity());
        float guidedAngle = MathUtils.getShortestRotation(velocityFacing, VectorUtils.getAngleStrict(
                missile.getLocation(), guidedTarget));
        float facingDelta = MathUtils.getShortestRotation(velocityFacing, missile.getFacing());

        boostTimer -= amount;
        if ((Math.abs(guidedAngle) <= 15f) && (Math.abs(facingDelta) <= 10f) && (boostTimer <= 0f)
                && ((distance - target.getCollisionRadius()) <= FINAL_APPROACH_DISTANCE)) {
            MissileAPI newMissile = (MissileAPI) Global.getCombatEngine().spawnProjectile(launchingShip,
                    missile.getWeapon(),
                    "ii_ballista_stage2",
                    missile.getLocation(),
                    missile.getFacing(),
                    missile.getVelocity());
            newMissile.setAngularVelocity(missile.getAngularVelocity());
            newMissile.setFromMissile(true);
            newMissile.setEmpResistance(missile.getEmpResistance());

            float damageToDeal = missile.getMaxHitpoints() - missile.getHitpoints();
            if (damageToDeal > 0f) {
                Global.getCombatEngine().applyDamage(newMissile, missile.getLocation(), damageToDeal,
                        DamageType.FRAGMENTATION, 0f, true, false, missile.getSource(), false);
            }

            ((GuidedMissileAI) newMissile.getMissileAI()).setTarget(target);
            Vector2f offset = new Vector2f(-7.5f, 0f);
            VectorUtils.rotate(offset, missile.getFacing(), offset);
            Vector2f.add(offset, missile.getLocation(), offset);
            Global.getSoundPlayer().playSound("ii_ballista_boost", 1f, 1f, missile.getLocation(), missile.getVelocity());
            Global.getCombatEngine().addHitParticle(offset, missile.getVelocity(), 125f, 0.5f, 0.25f, FLARE_COLOR);
            Global.getCombatEngine().removeEntity(missile);
            return;
        }

        float absAngD = Math.abs(angularDistance - offtargetby);

        // Point towards target
        if (absAngD > 0.5) {
            // Makes missile fly off target
            missile.giveCommand(angularDistance > offtargetby ? ShipCommand.TURN_LEFT : ShipCommand.TURN_RIGHT);
        }

        // Course correction
        if (absAngD < 5) {
            float MFlightAng = VectorUtils.getAngleStrict(ZERO, missile.getVelocity());
            float MFlightCC = MathUtils.getShortestRotation(missile.getFacing(), MFlightAng);
            if (Math.abs(MFlightCC) > 20) {
                missile.giveCommand(MFlightCC < 0 ? ShipCommand.STRAFE_LEFT : ShipCommand.STRAFE_RIGHT);
            }
        }

        missile.giveCommand(ShipCommand.ACCELERATE);

        if (absAngD < 0.4) {
            missile.setAngularVelocity(0);
        }
    }

    @Override
    protected boolean acquireTarget(float amount) {
        if (!isTargetValidAlternate(target)) {
            if (target instanceof ShipAPI) {
                ShipAPI ship = (ShipAPI) target;
                if (ship.isPhased() && ship.isAlive()) {
                    return false;
                }
            }
            setTarget(findBestTarget());
            if (target == null) {
                setTarget(findBestTargetAlternate());
            }
            if (target == null) {
                return false;
            }
        }
        return true;
    }

    protected ShipAPI findBestTargetAlternate() {
        ShipAPI closest = null;
        float range = getRemainingRange();
        float distance, closestDistance = getRemainingRange() + missile.getMaxSpeed() * 2f;
        List<ShipAPI> ships = AIUtils.getEnemiesOnMap(missile);
        int size = ships.size();
        for (int i = 0; i < size; i++) {
            ShipAPI tmp = ships.get(i);
            float mod = 0f;
            if (tmp.isFighter() || tmp.isDrone()) {
                mod = range / 2f;
            }
            if (!isTargetValidAlternate(tmp)) {
                mod = range;
            }
            distance = MathUtils.getDistance(tmp, missile.getLocation()) + mod;
            if (distance < closestDistance) {
                closest = tmp;
                closestDistance = distance;
            }
        }
        return closest;
    }

    @Override
    protected boolean isTargetValid(CombatEntityAPI target) {
        if (target instanceof ShipAPI) {
            ShipAPI ship = (ShipAPI) target;
            if (ship.isFighter() || ship.isDrone()) {
                return false;
            }
        }
        return super.isTargetValid(target);
    }

    protected boolean isTargetValidAlternate(CombatEntityAPI target) {
        return super.isTargetValid(target);
    }
}
