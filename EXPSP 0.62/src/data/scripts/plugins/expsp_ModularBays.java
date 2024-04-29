package data.scripts.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lwjgl.util.vector.Vector2f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class expsp_ModularBays extends BaseEveryFrameCombatPlugin {
    
    private CombatEngineAPI engine;
    
    private final IntervalUtil DUMB = new IntervalUtil(1.5f, 1.5f);   
    
    private final IntervalUtil interval = new IntervalUtil(1.5f, 1.5f);
    private float timer = 0f;       //Timer
    private float duration = 0.05f; //Duration of the effect
    
    private static final String LTA_PortableBayTalonShipID = "LTA_PortableBay_Talon";
    private static final String LTA_PortableBayTalonProjectileID = "LTA_portablebaytalonspec";
    
    private static final String LTA_PortableBayPiranhaShipID = "LTA_PortableBay_Piranha";
    private static final String LTA_PortableBayPiranhaProjectileID = "LTA_portablebaypiranhaspec";

    private static final String LTA_PortableBayDiademPShipID = "LTA_PortableBay_DiademP";
    private static final String LTA_PortableBayDiademPProjectileID = "LTA_portablebaydiadempspec";
    
    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if (engine == null) {
            return;
        }
        if (engine.isPaused()) {
            return;
        }        
                                                                                              
        List<DamagingProjectileAPI> projectiles = engine.getProjectiles();
        List<DamagingProjectileAPI> projectiles_copy = new ArrayList(projectiles);

        Iterator<DamagingProjectileAPI> iter = projectiles_copy.iterator();        
        while (iter.hasNext()) {
            DamagingProjectileAPI projectile = iter.next();
            
            if (projectile.getProjectileSpecId() == null) {
                continue;
            }

            if (DUMB.getElapsed() >= 1.5f) {
            switch (projectile.getProjectileSpecId()) {
                case LTA_PortableBayTalonProjectileID: {

                    Vector2f location = new Vector2f(projectile.getLocation());
                    ShipAPI ship = projectile.getSource();
                    float angle = projectile.getFacing();
                    int owner = projectile.getOwner();

                    engine.removeEntity(projectile);
                    CombatFleetManagerAPI FleetManager = engine.getFleetManager(ship.getOwner());
                    FleetManager.setSuppressDeploymentMessages(true);
                    FleetMemberAPI missileMember = Global.getFactory().createFleetMember(FleetMemberType.SHIP, LTA_PortableBayTalonShipID);
                    missileMember.getRepairTracker().setCrashMothballed(false);
                    missileMember.getRepairTracker().setMothballed(false);
                    missileMember.getRepairTracker().setCR(1f);
                    missileMember.setOwner(owner);
                    missileMember.setAlly(ship.isAlly());
                    ShipAPI missile = engine.getFleetManager(owner).spawnFleetMember(missileMember, location, angle, 2.5f);
                    missile.setCollisionClass(CollisionClass.SHIP);
                    missile.getVelocity().set(ship.getVelocity());
                    missile.setAngularVelocity(ship.getAngularVelocity());
                                        
                    interval.advance(amount);
                    timer += amount;

                    if (timer>=duration) {
                        timer-=timer;
                        FleetManager.setSuppressDeploymentMessages(false);
                    }
                }
                break;
                
                case LTA_PortableBayPiranhaProjectileID: {

                    Vector2f location = new Vector2f(projectile.getLocation());
                    ShipAPI ship = projectile.getSource();
                    float angle = projectile.getFacing();
                    int owner = projectile.getOwner();

                    engine.removeEntity(projectile);
                    CombatFleetManagerAPI FleetManager = engine.getFleetManager(ship.getOwner());
                    FleetManager.setSuppressDeploymentMessages(true);
                    FleetMemberAPI missileMember = Global.getFactory().createFleetMember(FleetMemberType.SHIP, LTA_PortableBayPiranhaShipID);
                    missileMember.getRepairTracker().setCrashMothballed(false);
                    missileMember.getRepairTracker().setMothballed(false);
                    missileMember.getRepairTracker().setCR(1f);
                    missileMember.setOwner(owner);
                    missileMember.setAlly(ship.isAlly());
                    ShipAPI missile = engine.getFleetManager(owner).spawnFleetMember(missileMember, location, angle, 2.5f);
                    missile.setCollisionClass(CollisionClass.SHIP);
                    missile.getVelocity().set(ship.getVelocity());
                    missile.setAngularVelocity(ship.getAngularVelocity());
                                        
                    interval.advance(amount);
                    timer += amount;

                    if (timer>=duration) {
                        timer-=timer;
                        FleetManager.setSuppressDeploymentMessages(false);
                    }
                }
                break;
                
                case LTA_PortableBayDiademPProjectileID: {

                    Vector2f location = new Vector2f(projectile.getLocation());
                    ShipAPI ship = projectile.getSource();
                    float angle = projectile.getFacing();
                    int owner = projectile.getOwner();

                    engine.removeEntity(projectile);
                    CombatFleetManagerAPI FleetManager = engine.getFleetManager(ship.getOwner());
                    FleetManager.setSuppressDeploymentMessages(true);
                    FleetMemberAPI missileMember = Global.getFactory().createFleetMember(FleetMemberType.SHIP, LTA_PortableBayDiademPShipID);
                    missileMember.getRepairTracker().setCrashMothballed(false);
                    missileMember.getRepairTracker().setMothballed(false);
                    missileMember.getRepairTracker().setCR(1f);
                    missileMember.setOwner(owner);
                    missileMember.setAlly(ship.isAlly());
                    ShipAPI missile = engine.getFleetManager(owner).spawnFleetMember(missileMember, location, angle, 2.5f);
                    missile.setCollisionClass(CollisionClass.SHIP);
                    missile.getVelocity().set(ship.getVelocity());
                    missile.setAngularVelocity(ship.getAngularVelocity());
                                        
                    interval.advance(amount);
                    timer += amount;

                    if (timer>=duration) {
                        timer-=timer;
                        FleetManager.setSuppressDeploymentMessages(false);
                    }
                }
                break;

                default:
                }
            }
            DUMB.advance(amount);
        }
    }
    
    @Override
    public void init(CombatEngineAPI engine) {
        this.engine = engine;
    }
}