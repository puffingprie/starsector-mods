package data.shipsystems.scripts;

import java.awt.Color;
import java.util.*;

import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.input.InputEventAPI;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
import com.fs.starfarer.api.combat.EveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ViewportAPI;

import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.CombatUtils;

public class pn_bounce extends BaseShipSystemScript implements EveryFrameCombatPlugin {
    private final IntervalUtil tracker = new IntervalUtil(0.35f, 0.4f);
    private static CombatEngineAPI activeEngine;
    private ShipSystemAPI system;

    private final static Map reflecting = new HashMap();

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        ShipAPI owner = (ShipAPI) stats.getEntity();
        ShipHullSpecAPI shipHull = owner.getHullSpec();
        String hullName = shipHull.getHullId();
        if (!reflecting.containsKey(owner)) {
            reflecting.put(owner, hullName);
        }
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        reflecting.remove(stats.getEntity());
    }

    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData("repulsing nearby enemy objects", false);
        }
        return null;
    }

    private static Vector2f randomCircularVelocity(float velocity) {
        Vector2f newVel = MathUtils.getRandomPointOnCircumference(null, velocity);
        return newVel;
    }

    @Override
    public void advance(float amount, List events) {
        //activeEngine.addFloatingText(activeEngine.getPlayerShip().getLocation(), "RUNNING REPULSOR", 200f, Color.BLUE, activeEngine.getPlayerShip(), 1f, 0f);
        if (activeEngine.isPaused()) return;
        if (!reflecting.isEmpty()) {
            tracker.advance(amount);

            {
                for (Iterator iter = reflecting.entrySet().iterator(); iter.hasNext(); ) {
                    Map.Entry tmp = (Map.Entry) iter.next();
                    ShipAPI ship = (ShipAPI) tmp.getKey();

                    ShipHullSpecAPI shipHull = ship.getHullSpec();
                    String hullName = shipHull.getHullId();
                    if (hullName != tmp.getValue() || ship.isHulk() || ship.getHullLevel() < 0.01f) {
                        reflecting.remove(ship);
                        return;
                    }

                    //The ship's location, used for the rest of the code.
                    Vector2f shipLoc = ship.getLocation();
                    Vector2f shipVec = ship.getVelocity();


                    //Gets list of nearby ships and projectiles and pushes them out of the radius
                    float repulseRadius = 500;

                    List nearbyEnemies = CombatUtils.getShipsWithinRange(ship.getLocation(), repulseRadius);//List of enemies to push back, smoothly
                    nearbyEnemies.addAll(CombatUtils.getAsteroidsWithinRange(ship.getLocation(), repulseRadius));
                    for (int enemies = 0; enemies < nearbyEnemies.size(); enemies++) {
                        CombatEntityAPI enemy = (CombatEntityAPI) nearbyEnemies.get(enemies);
                        if (enemy == null) {
                            continue;
                        }

                        if (enemy instanceof ShipAPI) {
                            ShipAPI thisEnemy = (ShipAPI) nearbyEnemies.get(enemies);
                            //Don't push phased ships.
                            ShipSystemAPI cloak = thisEnemy.getPhaseCloak();
                            if (cloak != null && cloak.isActive()) {
                                continue;
                            }
                        }
                        if (enemy.getOwner() != ship.getOwner()) {
                            Vector2f thisEnemyLoc = enemy.getLocation();
                            Vector2f pushVec = new Vector2f(thisEnemyLoc.x - shipLoc.x, thisEnemyLoc.y - shipLoc.y);//Get the vector going from A to B
                            float magnitude = (1f / pushVec.lengthSquared());//Get the magnitude of the vector (usually very small)
                            float mass = Math.max(1f, enemy.getMass());
                            magnitude /= mass;
                            pushVec.x *= magnitude * 100000f;
                            pushVec.y *= magnitude * 100000f;
                            Vector2f enemyVel = enemy.getVelocity(); //Get the velocity of the ship, and add the new vector to it.
                            enemyVel.x += pushVec.x;
                            enemyVel.y += pushVec.y;
                        }
                        //Test code here
                        //activeEngine.addFloatingText(thisEnemyLoc, "Repulsed with " + pushVec.x + pushVec.y + " force!", 20f, Color.BLUE, ship, 1f, 0f);
                    }
                    if (tracker.intervalElapsed()) {

                        //Blue radiation from our repulsor pulses out with each use pulse
                        int blueRadius = (int) repulseRadius;
                        for (int i = 0; i < blueRadius / 10; i++) {
                            Vector2f randPoint = MathUtils.getRandomPointInCircle(shipLoc, repulseRadius / 35f);
                            float randSize = MathUtils.getRandomNumberInRange(repulseRadius / 10f, repulseRadius / 5f);
                            Vector2f randVec = randomCircularVelocity(MathUtils.getRandomNumberInRange(repulseRadius / 3f, repulseRadius / 1.5f));
                            Vector2f randVecTwo = randomCircularVelocity(repulseRadius / 2f);
                            randVecTwo.x += shipVec.x;
                            randVecTwo.y += shipVec.y;
                            float randDur = 0.1f + MathUtils.getRandomNumberInRange(0.3f, 2f);
                            float randDurTwo = MathUtils.getRandomNumberInRange(3f, 3.25f);
                            int yelVal = (int) (Math.random() * 128f + 32f);
                            int randTrans = (int) MathUtils.getRandomNumberInRange(64f, 128f);

                            activeEngine.addSmoothParticle(shipLoc, randVecTwo, randSize / 2, 1f, randDurTwo, new Color(0, yelVal + 64, 255, randTrans));
                            activeEngine.addSmoothParticle(randPoint, randVec, randSize, 1f, randDur, new Color(0, yelVal, 255, randTrans));
                        }


                        //Iterates through nearby enemy projectiles and reverses their course, "repulsing" them.
                        //What this really does is replace the projectiles with new clones, then destroys the enemy versions.
                        //In theory, two groups with Repulsors active could ping-pong shots back and forth at one another nearly forever...
                        //In reality, the time intervals don't allow for it.
                        List projectileList = activeEngine.getProjectiles();
                        for (Iterator projectiles = projectileList.iterator(); projectiles.hasNext(); ) {
                            DamagingProjectileAPI thisProj = (DamagingProjectileAPI) projectiles.next();
                            if (thisProj.getOwner() != ship.getOwner() && Math.random() > 0.99f)//If not ours and if luck favors us, then...
                            {
                                //Get the projectile to be reversed's vital stats, including the inverse of its velocity (to send it right back where it came from)

                                Vector2f thisProjLoc = thisProj.getLocation();
                                float distance = MathUtils.getDistance(shipLoc, thisProjLoc);
                                if (distance > repulseRadius) continue;
                                Vector2f thisProjVel = thisProj.getVelocity();
                                thisProjVel.x *= -0.7f;
                                thisProjVel.y *= -0.7f;
                                //Get the projectile's weapon ID, which we can then use to manufacture our clone
                                WeaponAPI weapon = thisProj.getWeapon();
                                if (weapon == null) return;
                                String weaponName = weapon.getId();
                                //Get the inverse angle of the projectile, using the ship and the projectile to generate an angle.  Not an entirely accurate solution, but fast.
                                float thisProjAngleInverse = MathUtils.clampAngle(VectorUtils.getAngle(ship.getLocation(), thisProjLoc) + MathUtils.getRandomNumberInRange(-5f, 5f));

                                //Build our new projectile clone and send it back to the enemy.
                                activeEngine.spawnProjectile(ship, null, weaponName, thisProjLoc, thisProjAngleInverse, thisProjVel);

                                activeEngine.removeEntity(thisProj);//Remove the "reversed" projectile
                            }
                            //activeEngine.addFloatingText(thisProjLoc, "Repulsed with " + pushVec.x + pushVec.y + " force!", 20f, Color.BLUE, ship, 1f, 0f);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void init(CombatEngineAPI engine) {
        activeEngine = engine;
        reflecting.clear();
    }

    @Override
    public void renderInWorldCoords(ViewportAPI viewport) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void renderInUICoords(ViewportAPI viewport) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void processInputPreCoreControls(float amount, List<InputEventAPI> events) {}
}
