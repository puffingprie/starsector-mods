package data.scripts.weapons.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.AutofireAIPlugin;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.shipsystems.II_LuxFinisStats;
import data.scripts.util.II_Util;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.lazywizard.lazylib.CollectionUtils;
import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

public class II_LightspearAI implements AutofireAIPlugin {

    private boolean shouldFire = false;
    private CombatEntityAPI target = null;
    private final WeaponAPI weapon;

    public II_LightspearAI(WeaponAPI weapon) {
        this.weapon = weapon;
    }

    @Override
    public void advance(float amount) {
        float firingRange = weapon.getRange();
        shouldFire = false;
        target = null;

        if (weapon.getAmmo() <= 0) {
            return;
        }

        ShipAPI ship = weapon.getShip();
        if ((ship == null) || !ship.isAlive()) {
            return;
        }

        float gauge = II_LuxFinisStats.getGauge(ship);
        boolean lowGauge = gauge < 0.35f;
        boolean player = (ship == Global.getCombatEngine().getPlayerShip()) && (ship.getShipAI() == null);

        ShipAPI bestTarget;
        if (!player && (ship.getAIFlags() != null) && ship.getAIFlags().getCustom(AIFlags.MANEUVER_TARGET) instanceof ShipAPI) {
            bestTarget = (ShipAPI) ship.getAIFlags().getCustom(AIFlags.MANEUVER_TARGET);
        } else {
            bestTarget = ship.getShipTarget();
        }
        if ((bestTarget != null) && (bestTarget.isFighter() || bestTarget.isDrone())) {
            bestTarget = null;
        }
        if ((bestTarget != null) && (bestTarget.getCollisionClass() != CollisionClass.SHIP) && (bestTarget.getCollisionClass() != CollisionClass.FIGHTER)) {
            bestTarget = null;
        }
        if ((bestTarget != null) && ((bestTarget.getOwner() == ship.getOwner()) || (bestTarget.getOwner() == 100))) {
            bestTarget = null;
        }
        if ((bestTarget != null) && !bestTarget.isAlive()) {
            bestTarget = null;
        }

        Vector2f beamProjection = new Vector2f(firingRange, 0f);
        VectorUtils.rotate(beamProjection, weapon.getCurrAngle(), beamProjection);
        Vector2f.add(beamProjection, weapon.getLocation(), beamProjection);

        if (player || (bestTarget == null)) {
            List<ShipAPI> potentialTargets = II_Util.getShipsWithinRange(weapon.getLocation(), firingRange * 1.25f);
            Iterator<ShipAPI> iter = potentialTargets.iterator();
            while (iter.hasNext()) {
                ShipAPI potentialTarget = iter.next();
                if (potentialTarget.isFighter() || potentialTarget.isDrone()) {
                    iter.remove();
                    continue;
                }
                if ((potentialTarget.getCollisionClass() != CollisionClass.SHIP) && (potentialTarget.getCollisionClass() != CollisionClass.FIGHTER)) {
                    iter.remove();
                    continue;
                }
                if (((potentialTarget.getOwner() == ship.getOwner()) || (potentialTarget.getOwner() == 100))) {
                    iter.remove();
                    continue;
                }
                if (!potentialTarget.isAlive()) {
                    iter.remove();
                    continue;
                }
                if (weapon.getSlot().isHardpoint() && !CollisionUtils.getCollides(weapon.getLocation(), beamProjection, potentialTarget.getLocation(), potentialTarget.getCollisionRadius())) {
                    iter.remove();
                }
                if (!weapon.getSlot().isHardpoint() && !Misc.isInArc(weapon.getArcFacing(), weapon.getArc(), weapon.getLocation(), potentialTarget.getLocation())) {
                    iter.remove();
                }
            }

            if (potentialTargets.isEmpty()) {
                bestTarget = null;
            } else {
                List<ShipAPI> destroyerOrLargerTargets = new ArrayList(potentialTargets);
                iter = destroyerOrLargerTargets.iterator();
                while (iter.hasNext()) {
                    ShipAPI potentialTarget = iter.next();
                    if (potentialTarget.isFrigate()) {
                        iter.remove();
                    }

                }
                List<ShipAPI> cruiserOrLargerTargets = new ArrayList(destroyerOrLargerTargets);
                iter = cruiserOrLargerTargets.iterator();
                while (iter.hasNext()) {
                    ShipAPI potentialTarget = iter.next();
                    if (potentialTarget.isDestroyer()) {
                        iter.remove();
                    }

                }
                List<ShipAPI> capitalTargets = new ArrayList(cruiserOrLargerTargets);
                iter = capitalTargets.iterator();
                while (iter.hasNext()) {
                    ShipAPI potentialTarget = iter.next();
                    if (potentialTarget.isCapital()) {
                        iter.remove();
                    }

                }
                if (!capitalTargets.isEmpty()) {
                    Collections.sort(capitalTargets, new CollectionUtils.SortEntitiesByDistance(ship.getLocation()));
                    bestTarget = capitalTargets.get(0);
                }
                if (!cruiserOrLargerTargets.isEmpty()) {
                    Collections.sort(cruiserOrLargerTargets, new CollectionUtils.SortEntitiesByDistance(ship.getLocation()));
                    bestTarget = cruiserOrLargerTargets.get(0);
                }
                if (!destroyerOrLargerTargets.isEmpty()) {
                    Collections.sort(destroyerOrLargerTargets, new CollectionUtils.SortEntitiesByDistance(ship.getLocation()));
                    bestTarget = destroyerOrLargerTargets.get(0);
                }
                if (!potentialTargets.isEmpty()) {
                    Collections.sort(potentialTargets, new CollectionUtils.SortEntitiesByDistance(ship.getLocation()));
                    bestTarget = potentialTargets.get(0);
                }
            }
        }

        target = bestTarget;
        if ((bestTarget != null) && !lowGauge) {
            if (CollisionUtils.getCollides(weapon.getLocation(), beamProjection, bestTarget.getLocation(), bestTarget.getCollisionRadius())) {
                float actualDist = II_Util.getActualDistance(weapon.getLocation(), bestTarget, false);
                if (actualDist <= firingRange) {
                    shouldFire = true;
                }
            }
        }
    }

    @Override
    public void forceOff() {
        shouldFire = false;
    }

    @Override
    public Vector2f getTarget() {
        if (target == null) {
            return null;
        } else {
            return target.getLocation();
        }
    }

    @Override
    public ShipAPI getTargetShip() {
        if (target instanceof ShipAPI) {
            return (ShipAPI) target;
        }
        return null;
    }

    @Override
    public WeaponAPI getWeapon() {
        return weapon;
    }

    @Override
    public boolean shouldFire() {
        return shouldFire;
    }

    @Override
    public MissileAPI getTargetMissile() {
        return null;
    }
}
