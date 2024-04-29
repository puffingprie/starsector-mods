
package data.shipsystems.scripts.ai;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

import java.util.HashSet;
import java.util.LinkedList;

/**
 * Checks if a target is in a vulnerable state, and activates shipsystem if so
 * @author Nicke535
 */
public class expsp_disruptor_ai implements ShipSystemAIScript {
    //How long does the AI "remember" hull/armor damage an enemy has taken(in seconds)?
    private static final float DAMAGE_REMEMBER_TIME = 1f;

    //How much (in percentage, 0.1f = 10%) hull or armor can a ship lose before this shipsystem considers it a valid target?
    private static final float DAMAGE_ACTIVATION_THRESHOLD = 0.01f;

    //At how much flux (percentage) should the target be considered valid?
    private static final float HIGH_FLUX_LEVEL = 0.9f;

    //How small is the smallest vent time we still want to activate our system against (in seconds)?
    private static final float VENT_WINDOW_TIME = 3f;

    //How often does the script check for activation?
    private IntervalUtil counter = new IntervalUtil(0.08f, 0.12f);


    //--Used in-script to simplify later calls and some other tracking: don't touch--
    private ShipAPI ship;
    private CombatEngineAPI engine;
    private ShipwideAIFlags flags;
    private ShipSystemAPI system;
    private ShipAPI currentTarget = null;
    private LinkedList<DamageRememberer> damageTracker = new LinkedList<>();

    //Initialize some variables for later use
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.flags = flags;
        this.engine = engine;
        this.system = system;
    }

    //Main advance loop
    @SuppressWarnings("unchecked")
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
        //Ticks down remembered damage
        HashSet<DamageRememberer> toRemove = new HashSet<>();
        for (DamageRememberer rem : damageTracker) {
            rem.tick(amount);
            if (rem.timeAgo > DAMAGE_REMEMBER_TIME) {
                toRemove.add(rem);
            }
        }
        damageTracker.removeAll(toRemove);

        //If we have no target, do nothing
        if (target == null) {
            return;
        }

        //Stop here if the system isn't ready to use
        if (!system.getState().equals(ShipSystemAPI.SystemState.IDLE)) {
            return;
        }

        //Main loop for AI
        counter.advance(amount);
        if (counter.intervalElapsed()) {
            if (currentTarget != target) {
                currentTarget = target;
                damageTracker.clear();
            }

            //Check if the target is vulnerable
            //Above certain flux
            if (target.getFluxLevel() >= HIGH_FLUX_LEVEL) {
                activateIfAllyInRange();
            }

            //Target overloaded or venting for a long time
            else if (target.getFluxTracker().isOverloaded()
                    || (target.getFluxTracker().getTimeToVent() >= VENT_WINDOW_TIME && target.getFluxTracker().isVenting())) {
                activateIfAllyInRange();
            }

            //Target has no shields, or is a phase ship that isn't phased
            else if (target.getShield() == null || target.getShield().getType().equals(ShieldAPI.ShieldType.NONE)
                    || (target.getShield().getType().equals(ShieldAPI.ShieldType.PHASE) && !target.isPhased())) {
                activateIfAllyInRange();
            }


            //If the target isn't vulnerable, calculate damage for it: this might still make it count as vulnerable
            float hullThisFrame = target.getHullLevel();
            float armorThisFrame = 0f;
            int numberOfCells = 0;
            for (int ix = 0; ix < (target.getArmorGrid().getLeftOf() + target.getArmorGrid().getRightOf()); ix++) {
                for (int iy = 0; iy < (target.getArmorGrid().getAbove() + target.getArmorGrid().getBelow()); iy++) {
                    armorThisFrame += target.getArmorGrid().getArmorFraction(ix, iy);
                    numberOfCells++;
                }
            }
            armorThisFrame /= (float)numberOfCells;
            damageTracker.addLast(new DamageRememberer(armorThisFrame+hullThisFrame));

            //Check if our combined armor/hull loss is higher than our threshold
            if (damageTracker.getFirst().status - DAMAGE_ACTIVATION_THRESHOLD >= hullThisFrame+armorThisFrame) {
                activateIfAllyInRange();
            }
        }
    }

    private void activateIfAllyInRange() {
        if (MathUtils.getDistance(ship, currentTarget) < getLongestWeaponRange(ship)) {
            activateSystem();
            return;
        }
        for (ShipAPI ally : AIUtils.getAlliesOnMap(ship)) {
            if (MathUtils.getDistance(ally, currentTarget) < getLongestWeaponRange(ally)) {
                activateSystem();
                break;
            }
        }
    }

    //Function for getting the maximum range on a ship: missile weapons don't count
    private float getLongestWeaponRange (ShipAPI target) {
        //Go through all weapons, discard missiles, and return the highest range
        float maxRange = 0f;
        for (WeaponAPI wep : target.getAllWeapons()) {
            if (wep.getType() == WeaponAPI.WeaponType.MISSILE) {
                continue;
            }

            if (maxRange < wep.getRange()) {
                maxRange = wep.getRange();
            }
        }
        return maxRange;
    }

    private void activateSystem() {
        if (!system.isOn()) {
            ship.useSystem();
        }
    }

    //Utility class for storing damage data
    private class DamageRememberer {
        private float status;
        private float timeAgo;
        DamageRememberer (float status) {
            timeAgo = 0f;
            this.status = status;
        }

        private void tick(float amount) {
            this.timeAgo += amount;
        }
    }
}

