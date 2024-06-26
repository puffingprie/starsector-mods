package data.scripts.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.listeners.DamageTakenModifier;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.loading.ProjectileSpawnType;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.util.MagicFakeBeam;
import static data.scripts.util.MagicFakeBeam.getShipCollisionPoint;
import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

public class eis_rampagedrive extends BaseShipSystemScript {
    //massy private static Map mass_mult = new HashMap();
    public static Map bugs = new HashMap();
    private static Map wide = new HashMap();
    private static Map damage = new HashMap();
    private static Map SPEED_BOOST = new HashMap();
    private static Map DAMAGE_MULT = new HashMap();
    static {
        /*mass_mult.put(HullSize.FRIGATE, 3f);
        mass_mult.put(HullSize.DESTROYER, 3f);
        mass_mult.put(HullSize.CRUISER, 2f);
        mass_mult.put(HullSize.CAPITAL_SHIP, 2f); massy*/
        bugs.put(HullSize.FRIGATE, 80f);
        bugs.put(HullSize.DESTROYER, 100f);
        bugs.put(HullSize.CRUISER, 160f);
        bugs.put(HullSize.CAPITAL_SHIP, 305f);
        wide.put(HullSize.FRIGATE, 80f);
        wide.put(HullSize.DESTROYER, 136f);
        wide.put(HullSize.CRUISER, 177f);
        wide.put(HullSize.CAPITAL_SHIP, 201f);
        damage.put(HullSize.FRIGATE, 1000f);
        damage.put(HullSize.DESTROYER, 1500f);
        damage.put(HullSize.CRUISER, 2500f);
        damage.put(HullSize.CAPITAL_SHIP, 3500f);
        SPEED_BOOST.put(HullSize.FRIGATE, 250f);
        SPEED_BOOST.put(HullSize.DESTROYER, 275f);
        SPEED_BOOST.put(HullSize.CRUISER, 300f);
        SPEED_BOOST.put(HullSize.CAPITAL_SHIP, 300f);
        DAMAGE_MULT.put(HullSize.FRIGATE, 0.33f);
        DAMAGE_MULT.put(HullSize.DESTROYER, 0.33f);
        DAMAGE_MULT.put(HullSize.CRUISER, 0.5f);
        DAMAGE_MULT.put(HullSize.CAPITAL_SHIP, 0.5f);
    }
    private static final Color color = new Color(255, 135, 240, 0);
    public static final float MASS_MULT = 1.2f;
    //public static final float RANGE = 600f;
    public static final float ROF_MULT = 0.5f;

    private static String poopystinky = Global.getSettings().getString("eis_ironshell", "eis_rampagedrive1");
    private static String poopystinky2 = Global.getSettings().getString("eis_ironshell", "eis_rampagedrive2");
    private static String poopystinky3 = Global.getSettings().getString("eis_ironshell", "eis_rampagedrive3");
    
    private boolean reset = true;
    //private float activeTime = 0f;
    //private float jitterLevel;
    private boolean DidRam = false;
    
    private Float mass = null;

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        CombatEngineAPI engine =  Global.getCombatEngine();
        ShipAPI ship = (ShipAPI) stats.getEntity();
        if (engine.isPaused() || ship == null) {
            return;
        }

        if (mass == null) {
            mass = ship.getMass();
        }
        
        if (reset) {
            reset = false;
            //activeTime = 0f;
            //jitterLevel = 0f;
            DidRam = false;
        }

        ShipAPI target = ship.getShipTarget();
        float turnrate = ship.getMaxTurnRate()*2;

        if (state == State.OUT) {
            stats.getMaxSpeed().unmodify(id); // to slow down ship to its regular top speed while powering drive down
            stats.getBallisticRoFMult().unmodify(id);
            stats.getEnergyRoFMult().unmodify(id);
            ship.setMass(mass);
            //if (ship.hasListenerOfClass(RampageDriveListener.class)) {ship.removeListenerOfClass(RampageDriveListener.class);}
            DidRam = false;
        } else {
            if (ship.getMass() == mass) {
                ship.setMass(mass * MASS_MULT);
                if (!ship.hasListenerOfClass(RampageDriveListener.class)) {ship.addListener(new RampageDriveListener());}
            }
            stats.getMaxSpeed().modifyFlat(id, (Float) SPEED_BOOST.get(ship.getHullSize()));
            stats.getAcceleration().modifyFlat(id, (Float) SPEED_BOOST.get(ship.getHullSize()) * 4);
            stats.getEmpDamageTakenMult().modifyMult(id, (Float) DAMAGE_MULT.get(ship.getHullSize()));
            stats.getArmorDamageTakenMult().modifyMult(id, (Float) DAMAGE_MULT.get(ship.getHullSize()));
            stats.getHullDamageTakenMult().modifyMult(id, (Float) DAMAGE_MULT.get(ship.getHullSize()));

            stats.getBallisticRoFMult().modifyMult(id, ROF_MULT);
            stats.getEnergyRoFMult().modifyMult(id, ROF_MULT);
            if (!DidRam) {
                Vector2f from = ship.getLocation();
                float angle = ship.getFacing();
                Vector2f end = MathUtils.getPoint(from, (Float) bugs.get(ship.getHullSize()), angle);
                List <CombatEntityAPI> entity = CombatUtils.getEntitiesWithinRange(ship.getLocation(), (Float) bugs.get(ship.getHullSize())+25f);
                if (!entity.isEmpty()) {
                    for (CombatEntityAPI e : entity) {
                        if (e.getCollisionClass() == CollisionClass.NONE){continue;}
                        if (e.getOwner() == ship.getOwner()) {continue;}
                        Vector2f col = new Vector2f(1000000,1000000);                  
                        if (e instanceof ShipAPI ){                    
                            if(e!=ship && ((ShipAPI)e).getParentStation()!=e && (e.getCollisionClass()!=CollisionClass.NONE && e.getCollisionClass() != CollisionClass.FIGHTER) && CollisionUtils.getCollides(ship.getLocation(), end, e.getLocation(), e.getCollisionRadius())) {
                                            //&&
                                            //!(e.getCollisionClass()==CollisionClass.FIGHTER && e.getOwner()==ship.getOwner() && !((ShipAPI)e).getEngineController().isFlamedOut())               
                                ShipAPI s = (ShipAPI) e;
                                Vector2f hitPoint = getShipCollisionPoint(from, end, s, angle);
                                if (hitPoint != null ){col = hitPoint;}
                            }
                            if (col.x != 1000000 && MathUtils.getDistanceSquared(from, col) < MathUtils.getDistanceSquared(from, end)) {
                                DidRam = true;
                                MagicFakeBeam.spawnFakeBeam(engine, ship.getLocation(), (Float) bugs.get(ship.getHullSize()), ship.getFacing(), (Float) wide.get(ship.getHullSize()), 0.1f, 0.1f, 25, color, color, (Float) damage.get(ship.getHullSize()), DamageType.HIGH_EXPLOSIVE, 0, ship);
                                Global.getSoundPlayer().playSound("collision_ships", 1f, 0.5f, ship.getLocation(), ship.getVelocity());
                                //engine.addFloatingText(ship.getLocation(), "Yamete!", 25f, Color.WHITE, ship, 1f, 0.5f);
                            }
                        }
                    }
                }
            }
            if (ship.isDirectRetreat() && ship.getSystem().isActive()) {ship.setAngularVelocity(Math.min(turnrate, Math.max(-turnrate, MathUtils.getShortestRotation(ship.getFacing(),ship.getOwner() == 0 ? Global.getCombatEngine().getFleetManager(ship.getOwner()).getGoal() == FleetGoal.ESCAPE ? 90f : 270f : Global.getCombatEngine().getFleetManager(ship.getOwner()).getGoal() == FleetGoal.ESCAPE ? 270f : 90f)*2)));}
            else if ((target != null && target.isAlive() && !target.isAlly()) && ship.getSystem().isActive()) {
                float facing = ship.getFacing();
                if ((target.isFighter() || target.isDrone()) && target.getWing() != null && target.getWing().getSourceShip() != null && target.getWing().getSourceShip().isAlive()) {
                    facing=MathUtils.getShortestRotation(facing,VectorUtils.getAngle(ship.getLocation(), target.getWing().getSourceShip().getLocation()));
                } else {
                    //if ((target.isFighter() || target.isDrone()) && ship.getAIFlags().getCustom(AIFlags.MANEUVER_TARGET) instanceof ShipAPI) {engine.addFloatingText(ship.getLocation(), "My Life For Ava!", 15f, Color.WHITE, ship, 1f, 0.5f);ship.setShipTarget((ShipAPI) ship.getAIFlags().getCustom(AIFlags.MANEUVER_TARGET));}
                facing=MathUtils.getShortestRotation(
                        facing,
                        VectorUtils.getAngle(ship.getLocation(), target.getLocation())
                );}
                if (!target.isFighter() && !target.isDrone()) {
                    ship.setAngularVelocity(Math.min(turnrate, Math.max(-turnrate, facing*5)));
                } else {ship.setAngularVelocity(Math.min(turnrate, Math.max(-turnrate, facing*2)));}
              // activeTime += engine.getElapsedInLastFrame();
                /*if (activeTime <= 3f) {
                    //jitterLevel = Math.max(1 - activeTime/1.0f, 1.0f);
                    stats.getEmpDamageTakenMult().modifyMult(id, (Float) DAMAGE_MULT.get(ship.getHullSize()));
                    stats.getArmorDamageTakenMult().modifyMult(id, (Float) DAMAGE_MULT.get(ship.getHullSize()));
                    stats.getHullDamageTakenMult().modifyMult(id, (Float) DAMAGE_MULT.get(ship.getHullSize()));
                    //ship.setJitter(this, new Color (255,140,60,65), jitterLevel, 1, 0, 5f);
                    //ship.setJitterUnder(this, new Color (255,80,30,135), jitterLevel, 10, 0f, 8f);
                }
                if (activeTime > 3f) {
                    //jitterLevel = 0f;
                    stats.getEmpDamageTakenMult().unmodifyMult(id);
                    stats.getArmorDamageTakenMult().unmodifyMult(id);
                    stats.getHullDamageTakenMult().unmodifyMult(id);
                }*/
            }
        }
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        reset = true;
        ShipAPI ship = (ShipAPI) stats.getEntity();
        if (ship == null) {
            return;
        }

        if (mass == null) {
            mass = ship.getMass();
        }
        if (ship.hasListenerOfClass(RampageDriveListener.class)) {ship.removeListenerOfClass(RampageDriveListener.class);}
        if (ship.getMass() != mass) {
            ship.setMass(mass);
        }
        stats.getMaxSpeed().unmodify(id);
        stats.getAcceleration().unmodify(id);
        stats.getEmpDamageTakenMult().unmodify(id);
        stats.getHullDamageTakenMult().unmodify(id);
        stats.getArmorDamageTakenMult().unmodify(id);

        stats.getBallisticRoFMult().unmodify(id);
        stats.getEnergyRoFMult().unmodify(id);
    }

    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData(poopystinky, false);
        } else if (index == 1) {
            return new StatusData(poopystinky2, true);
        }
        return null;
    }

    @Override
    public String getInfoText(ShipSystemAPI system, ShipAPI ship) {
        if (system.isOutOfAmmo()) return null;
        if (system.getState() != ShipSystemAPI.SystemState.IDLE) return null;

        //ShipAPI target = findTarget(ship);
        /*if (target != null && target != ship) {
            return "TARGET ENGAGED";
        }

        if ((target == null || target == ship) && ship.getShipTarget() != null) {
            return "OUT OF RANGE";
        }*/
        return poopystinky3;
    }

    
    public static class RampageDriveListener implements DamageTakenModifier {

            @Override
            public String modifyDamageTaken(Object param, CombatEntityAPI target, DamageAPI damage, Vector2f point, boolean shieldHit) {
                // checking for ship explosions
                if (param instanceof DamagingProjectileAPI) {
                    DamagingProjectileAPI proj = (DamagingProjectileAPI) param;
                    /*
                     log.info(proj.getDamageType());
                     log.info(proj.getSource());
                     log.info(proj.getSpawnType());
                     log.info(MathUtils.getDistance(proj.getSpawnLocation(), proj.getSource().getLocation()));
                     */
                    // checks if the damage fits the details of a ship explosion
                    if (proj.getDamageType().equals(DamageType.HIGH_EXPLOSIVE)
                            && proj.getProjectileSpecId() == null
                            && !proj.getSource().isAlive()
                            && proj.getSpawnType().equals(ProjectileSpawnType.OTHER)
                            && MathUtils.getDistance(proj.getSpawnLocation(), proj.getSource().getLocation()) < 2f) {
                        //log.info(damage.computeDamageDealt(0f)); //0.5f??
                        damage.getModifier().modifyMult(this.getClass().getName(), 0f);
                        //log.info(damage.computeDamageDealt(0f));
                        //log.info("Reduced explosion damage from " + proj.getSource());
                    }
                }
                return null;
            }
        }
    /*protected ShipAPI findTarget(ShipAPI ship) {
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
    }*/
}
