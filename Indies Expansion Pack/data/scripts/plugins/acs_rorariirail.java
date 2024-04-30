package data.scripts.plugins;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatFleetManagerAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.IntervalUtil;

import org.lwjgl.util.vector.Vector2f;

public class acs_rorariirail extends BaseEveryFrameCombatPlugin {
    
    private CombatEngineAPI engine;
    
    private final IntervalUtil DUMB = new IntervalUtil(1.5f, 1.5f);   
    
    private final IntervalUtil interval = new IntervalUtil(1.5f, 1.5f);
    private float timer = 0f;       //Timer
    private float duration = 0.05f; //Duration of the effect
    
    private static final String acs_rorariiShipID = "acs_rorariifrig_variant";
    private static final String acs_rorariiProjectileID = "acs_rorariirail_lrm";
    
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
                case acs_rorariiProjectileID: {

                    Vector2f location = new Vector2f(projectile.getLocation());
                    ShipAPI ship = projectile.getSource();
                    float angle = projectile.getFacing();
                    int owner = projectile.getOwner();

                    engine.removeEntity(projectile);
                    CombatFleetManagerAPI FleetManager = engine.getFleetManager(ship.getOwner());
                    FleetManager.setSuppressDeploymentMessages(true);
                    FleetMemberAPI missileMember = Global.getFactory().createFleetMember(FleetMemberType.SHIP, acs_rorariiShipID);
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