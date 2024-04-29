package data.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.SoundAPI;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.everyframe.LE_Dram_Missile_Plugin;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;

public class LE_Explode_Phaeton extends BaseShipSystemScript {
//Code Courtesy of the Titan code from Interstellar Imperium by Dark Revenant, and the Crusher Drive System from Tahlan by Nia and maybe Tart


    private static final Color JITTER_COLOR = new Color(255, 204, 0, 50);

    public static final float RANGE = 900f;
    public static final float SPEED_BOOST = 160f;

    private boolean exploded = false;
    private final IntervalUtil interval = new IntervalUtil(0.2f, 0.2f);
    private SoundAPI sound = null;
    private boolean started = false;

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        ShipAPI ship = (ShipAPI) stats.getEntity();
        if (ship == null) {
            return;
        }

        if (Global.getCombatEngine().isPaused()) {
            return;
        }

        ShipAPI target = ship.getShipTarget();
        float turnrate = ship.getMaxTurnRate()*4;

        //       if(target!=null && ship.getSystem().isActive()){
        //system's aiming effect
//            float facing = ship.getFacing();
//            facing=MathUtils.getShortestRotation(
//                    facing,
//                    VectorUtils.getAngle(ship.getLocation(), target.getLocation())
//            );
//            ship.setAngularVelocity(Math.min(turnrate, Math.max(-turnrate, facing*5)));
//        }

        interval.advance(Global.getCombatEngine().getElapsedInLastFrame());
        ship.setJitter(stats.getEntity(), JITTER_COLOR, effectLevel, 5 + Math.round(effectLevel * 5f), effectLevel * 5f, 10f + (effectLevel * 20f));


        if (!started) {
            started = true;

            float distanceToHead = MathUtils.getDistance(stats.getEntity(),
                    Global.getCombatEngine().getViewport().getCenter());
            float refDist = 1500f;
            float vol = refDist / Math.max(refDist, distanceToHead);
            sound = Global.getSoundPlayer().playUISound("le_might_explode_charge", 1f, vol);
            stats.getEntity().setCollisionClass(CollisionClass.SHIP);
        }


        if ((state == State.IN && !exploded)) {
            stats.getMaxSpeed().modifyFlat(id, SPEED_BOOST);
            stats.getAcceleration().modifyFlat(id, SPEED_BOOST * 5);
            if(target!=null && ship.getSystem().isActive()) {
                //system's aiming effect
                float facing = ship.getFacing();
                facing = MathUtils.getShortestRotation(
                        facing,
                        VectorUtils.getAngle(ship.getLocation(), target.getLocation())
                );
                ship.setAngularVelocity(Math.min(turnrate, Math.max(-turnrate, facing * 5)));
            }

            if (interval.intervalElapsed() && ((Math.random() * Math.random()) > (stats.getEntity().getHullLevel() * 2f))) {
                if (sound != null) {
                    sound.stop();
                }
                exploded = true;
                {
                    LE_Dram_Missile_Plugin.explodePhaeton(ship, effectLevel);
                }
            }
        }

        if ((state == State.ACTIVE) && !exploded) {
            exploded = true;{
                LE_Dram_Missile_Plugin.explodePhaeton(ship, 1f);
            }
        }
    }

    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 1) {
            return new StatusData("Arming flux core detonator", false);
        }
        return null;
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        exploded = false;
        started = false;
        if (sound != null) {
            sound.stop();
        }

        ShipAPI ship = (ShipAPI) stats.getEntity();
        if (ship == null) {
            return;
        }
        stats.getMaxSpeed().unmodify(id);
        stats.getAcceleration().unmodify(id);
    }
    protected ShipAPI findTarget(ShipAPI ship) {
        ShipAPI target = ship.getShipTarget();
        if(
                target!=null
                        &&
                        (!target.isDrone()||!target.isFighter())
                        &&
                        MathUtils.isWithinRange(ship, target, RANGE)
                        &&
                        target.getOwner()!=ship.getOwner()
        ){
            return target;
        } else {
            return null;
        }
    }

    @Override
    public boolean isUsable(ShipSystemAPI system, ShipAPI ship) {
        ShipAPI target = findTarget(ship);
        return target != null && target != ship;
    }

}
