package data.weapons.weaponAI;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import data.hullmods.FantasySpellMod;
import data.utils.FM_Misc;
import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

import java.util.List;

public class FM_MikoBuiltInFireAI implements AutofireAIPlugin {

    private final WeaponAPI weapon;
    private ShipAPI targetShip;
    private ShipAPI ship;
    private Vector2f targetLoc;
    private float spellPower;
    private boolean clearMagazine = false;

    public FM_MikoBuiltInFireAI(WeaponAPI weapon) {
        this.weapon = weapon;
    }

    @Override
    public void advance(float amount) {
        if (Global.getCombatEngine() == null) return;
        CombatEngineAPI engine = Global.getCombatEngine();
        if (weapon.getShip() == null) return;
        this.ship = weapon.getShip();
        if (!ship.getVariant().hasHullMod("FantasySpellMod")) return;
        //engine.addHitParticle(targetLoc,new Vector2f(),20f,255f,1f, Color.CYAN);
        FantasySpellMod.SpellModState state = FM_Misc.getSpellModState(engine, ship);
        this.spellPower = state.spellPower;
//        if (spellPower <= 0.2f || !clearMagazine){
//            ship.getAIFlags().setFlag(ShipwideAIFlags.AIFlags.HARASS_MOVE_IN);
//        }

    }

    @Override
    public boolean shouldFire() {
        if (ship.getAIFlags().hasFlag(ShipwideAIFlags.AIFlags.BACK_OFF) ||
                ship.getAIFlags().hasFlag(ShipwideAIFlags.AIFlags.BACK_OFF_MIN_RANGE) ||
                ship.getAIFlags().hasFlag(ShipwideAIFlags.AIFlags.BACKING_OFF) ||
                ship.getAIFlags().hasFlag(ShipwideAIFlags.AIFlags.NEEDS_HELP)) {
            if (spellPower < 0.2f) {
                return false;
            }
        }

        if (!clearMagazine) {
            if (spellPower < 0.45f) {
                return false;
            }
        } else if (spellPower <= 0.05f) {
            clearMagazine = false;
        }
        if (spellPower >= 0.45f) {
            clearMagazine = true;
        }

        List<ShipAPI> enemies = AIUtils.getEnemiesOnMap(ship);
        for (ShipAPI enemy : enemies) {
            if (MathUtils.isWithinRange(enemy.getLocation(), weapon.getFirePoint(0), weapon.getRange() + enemy.getCollisionRadius())) {
                Vector2f intercept = enemy.getLocation();
//                Vector2f intercept = AIUtils.getBestInterceptPoint(weapon.getFirePoint(0),weapon.getProjectileSpeed(),enemy.getLocation(),enemy.getVelocity());
//                if (intercept == null){
//                    intercept = enemy.getLocation();
//                }
//                Vector2f line = Vector2f.sub(intercept, weapon.getFirePoint(0), null);
//                float diff = MathUtils.getShortestRotation(weapon.getCurrAngle(), VectorUtils.getFacing(line));
                List<ShipAPI> friendlyShips = AIUtils.getNearbyAllies(ship, weapon.getRange());
                for (ShipAPI friendlyShip : friendlyShips) {
                    if (CollisionUtils.getCollides(weapon.getFirePoint(0),
                            MathUtils.getPoint(weapon.getFirePoint(0),
                                    weapon.getRange(), weapon.getCurrAngle()),
                            friendlyShip.getLocation(), friendlyShip.getCollisionRadius())) {
                        return false;
                    }
                }
                boolean fire = CollisionUtils.getCollides(weapon.getFirePoint(0),
                        MathUtils.getPoint(weapon.getFirePoint(0),
                                weapon.getRange(), weapon.getCurrAngle()),
                        intercept, enemy.getCollisionRadius());
                if (fire) {
                    return fire;
                }
            }
        }
        return false;
    }

    @Override
    public void forceOff() {

    }

    @Override
    public Vector2f getTarget() {
        return null;
    }

    @Override
    public ShipAPI getTargetShip() {
        return null;
    }

    @Override
    public WeaponAPI getWeapon() {
        return this.weapon;
    }

    @Override
    public MissileAPI getTargetMissile() {
        return null;
    }
}
