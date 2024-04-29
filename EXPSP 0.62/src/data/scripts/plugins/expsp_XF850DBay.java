package data.scripts.plugins;
/*
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
import com.fs.starfarer.combat.ai.FighterAI;
import com.fs.starfarer.combat.ai.L;
import com.fs.starfarer.combat.entities.Ship;
import com.fs.starfarer.loading.specs.FighterWingSpec;
import com.fs.starfarer.combat.ai.BasicShipAI;
import com.fs.starfarer.api.combat.ShipAIConfig;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.IntervalUtil;

import org.lwjgl.util.vector.Vector2f;

public class expsp_XF850DBay extends BaseEveryFrameCombatPlugin {
    
    private CombatEngineAPI engine;
    
    private final IntervalUtil TIMER1= new IntervalUtil(0.1f, 0.1f);   
    
    private final IntervalUtil interval = new IntervalUtil(0.1f, 0.1f);
    private float timer = 0f;       //Timer
    private float duration = 0.05f; //Duration of the effect
    
    private static final String expsp_XF850DBayShipID = "LTA_Talon_Railfed_Interceptor_Wing";
    private static final String expsp_XF850DProjectileID = "expsp_xf850d_msl";
    
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

            if (DARKSUMMONINGRITUALFROMBEYONDTHEGREYSUN2.getElapsed() >= 0.1f) {
            switch (projectile.getProjectileSpecId()) {
                case LTA_TalonProjectileID: {

                    Vector2f location = new Vector2f(projectile.getLocation());
                    ShipAPI ship = projectile.getSource();
                    float angle = projectile.getFacing();
                    int owner = projectile.getOwner();

                    engine.removeEntity(projectile);
                    CombatFleetManagerAPI FleetManager = engine.getFleetManager(ship.getOwner());
                    FleetManager.setSuppressDeploymentMessages(true);
                    FleetMemberAPI missileMember = Global.getFactory().createFleetMember(FleetMemberType.FIGHTER_WING, LTA_TalonShipID);
                    missileMember.getRepairTracker().setCrashMothballed(false);
                    missileMember.getRepairTracker().setMothballed(false);
                    missileMember.getRepairTracker().setCR(1f);
                    missileMember.setOwner(owner);
                    missileMember.setAlly(ship.isAlly());
                    //ShipAPI fighter;
                    //ShipAIConfig config = new ShipAIConfig();
                    //config.personalityOverride = Personalities.STEADY;
                    //config.backingOffWhileNotVentingAllowed = true;
                    //ship.setShipAI(new BasicShipAI((Ship) ship, config));
                    ShipAPI missile = engine.getFleetManager(owner).spawnFleetMember(missileMember, location, angle, 2.5f);
                    missile.setCRAtDeployment(0.55f);
                    missile.setCollisionClass(CollisionClass.FIGHTER);
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
            TIMER1.advance(amount);
        }
    }
    
    @Override
    public void init(CombatEngineAPI engine) {
        this.engine = engine;
    }
}
*/