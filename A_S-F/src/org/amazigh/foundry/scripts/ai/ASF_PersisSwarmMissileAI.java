// Based on the MagicMissileAI script By Tartiflette.
// Modifications to make missiles with this script "swarm" outwards, and use "MISSILE_SPREAD" behaviour, also random targeting (has a stronger "swarm" than the ""normal"" swarm AI)
package org.amazigh.foundry.scripts.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.GuidedMissileAI;
import com.fs.starfarer.api.combat.MissileAIPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.util.Misc;

import org.magiclib.util.MagicTargeting;

import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.FastTrig;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class ASF_PersisSwarmMissileAI implements MissileAIPlugin, GuidedMissileAI {
          
    //////////////////////
    //     SETTINGS     //
    //////////////////////
    
    //Angle with the target beyond which the missile turn around without accelerating. Avoid endless circling.
    //  Set to a negative value to disable
    private final float OVERSHOT_ANGLE=40;
    
    //Time to complete a wave in seconds.
    private float WAVE_TIME=1.6f;
    
    //Max angle of the waving in degree (0.7 strength with ECCM). Set to a negative value to avoid all waving.
    private final float WAVE_AMPLITUDE=32;
    
    //Damping of the turn speed when closing on the desired aim. The smaller the snappier.
    private final float DAMPING=0.1f;
    
    //Does the missile try to correct it's velocity vector as fast as possible or just point to the desired direction and drift a bit?
    //  Can create strange results with large waving
    //  Require a projectile with a decent turn rate and around twice that in turn acceleration
    //  Usefull for slow torpedoes with low forward acceleration, or ultra precise anti-fighter missiles.     
    private final boolean OVERSTEER=false;  //REQUIRE NO OVERSHOOT ANGLE!
    
    //Does the missile switch its target if it has been destroyed?
    private final boolean TARGET_SWITCH=true;
    
    //Does the missile find a random target or aways tries to hit the ship's one?    
    /*
     *  NO_RANDOM, 
     * If the launching ship has a valid target within arc, the missile will pursue it.
     * If there is no target, it will check for an unselected cursor target within arc.
     * If there is none, it will pursue its closest valid threat within arc.    
     *
     *  LOCAL_RANDOM, 
     * If the ship has a target, the missile will pick a random valid threat around that one. 
     * If the ship has none, the missile will pursue a random valid threat around the cursor, or itself.
     * Can produce strange behavior if used with a limited search cone.
     * 
     *  FULL_RANDOM, 
     * The missile will always seek a random valid threat within arc around itself.
     * 
     *  IGNORE_SOURCE,
     * The missile will pick the closest target of interest. Useful for custom MIRVs.
     * 
    */
    private final MagicTargeting.targetSeeking seeking = MagicTargeting.targetSeeking.LOCAL_RANDOM;
    
    //Target class priorities
    //set to 0 to ignore that class
    private final int fighters=3;
    private final int frigates=5;
    private final int destroyers=4;
    private final int cruisers=2;
    private final int capitals=1;
    
    //Arc to look for targets into
    //set to 360 or more to ignore
    private final int SEARCH_CONE=360;
    
    //range in which the missile seek a target in game units.
    private final int MAX_SEARCH_RANGE = 750;
    
    //should the missile fall back to the closest enemy when no target is found within the search parameters
    //only used with limited search cones
    private final boolean FAILSAFE = false;
    
    //range under which the missile start to get progressively more precise in game units.
    private float PRECISION_RANGE=360;
    
    //Is the missile lead the target or tailchase it?
    private final boolean LEADING=true;
    
    //Leading loss without ECCM hullmod. The higher, the less accurate the leading calculation will be.
    //   1: perfect leading with and without ECCM
    //   2: half precision without ECCM
    //   3: a third as precise without ECCM. Default
    //   4, 5, 6 etc : 1/4th, 1/5th, 1/6th etc precision.
    private float ECCM=3;   //A VALUE BELOW 1 WILL PREVENT THE MISSILE FROM EVER HITTING ITS TARGET!
    
    
    // how far off from direct aim the "swarm spread" should be, big numbers can result in extremely *weird* behaviour
    private float SWARM_OFFSET = 32f;
    
    //////////////////////
    //    VARIABLES     //
    //////////////////////
    
    //max speed of the missile after modifiers.
    private final float MAX_SPEED;
    //Random starting offset for the waving.
    private final float OFFSET;
    private CombatEngineAPI engine;
    private final MissileAPI MISSILE;
    private CombatEntityAPI target;
    private Vector2f lead = new Vector2f();
    private Vector2f targetPoint = new Vector2f();
    private boolean launch=true;
    private float timer=0, check=0f, swarmBase=0f, swarmTrue=0f;

    //////////////////////
    //  DATA COLLECTING //
    //////////////////////
    
    public ASF_PersisSwarmMissileAI(MissileAPI missile, ShipAPI launchingShip) {
        this.MISSILE = missile;
        MAX_SPEED = missile.getMaxSpeed();
        if (missile.getSource().getVariant().getHullMods().contains("eccm")){
            ECCM=1;
        }
        //calculate the precision range factor
        PRECISION_RANGE=(float)Math.pow((2*PRECISION_RANGE),2);
        OFFSET=(float)(Math.random()*MathUtils.FPI*2);
        
        WAVE_TIME = MathUtils.getRandomNumberInRange(1.4f, 1.8f);
        // slight randomness to wave time, because
        
        swarmBase = MathUtils.getRandomNumberInRange(-SWARM_OFFSET, SWARM_OFFSET);
    }
    
    //////////////////////
    //   MAIN AI LOOP   //
    //////////////////////
    
    @Override
    public void advance(float amount) {
        
        if (engine != Global.getCombatEngine()) {
            this.engine = Global.getCombatEngine();
        }
        
        //skip the AI if the game is paused, the missile is engineless or fading
        if (Global.getCombatEngine().isPaused() || MISSILE.isFading() || MISSILE.isFizzling()) {return;}
        
        //assigning a target if there is none or it got destroyed
        if (target == null
                || (TARGET_SWITCH 
                        && ((target instanceof ShipAPI && !((ShipAPI) target).isAlive())
                                  || !engine.isEntityInPlay(target) || (target instanceof ShipAPI && ((ShipAPI) target).isPhased()))
                   )
                ){
            setTarget(
                    MagicTargeting.pickTarget(
                        MISSILE,
                        seeking,
                        MAX_SEARCH_RANGE,
                        SEARCH_CONE,
                        fighters,
                        frigates, 
                        destroyers,
                        cruisers,
                        capitals, 
                        FAILSAFE
                )
            );
            applySwarmOffset();
            //forced acceleration by default
            MISSILE.giveCommand(ShipCommand.ACCELERATE);
            return;
        }
        
        timer+=amount;
        //finding lead point to aim to        
        if(launch || timer>=check){
            launch=false;
            timer -=check;
            //set the next check time
            check = Math.min(
                    0.25f,
                    Math.max(
                            0.05f,
                            MathUtils.getDistanceSquared(MISSILE.getLocation(), target.getLocation()) / PRECISION_RANGE)
            );
            
            swarmTrue = swarmBase * (Math.min(1f, MathUtils.getDistanceSquared(MISSILE.getLocation(), target.getLocation()) / PRECISION_RANGE) );
            	// scaling down the swarm offset angle when close, to reduce fucky behaviour
            if (MathUtils.getDistanceSquared(MISSILE.getLocation(), target.getLocation()) < PRECISION_RANGE) {
            	swarmTrue *= 0.65f;
            }
            
            
            if(LEADING){
                //best intercepting point
                lead = AIUtils.getBestInterceptPoint(
                        MISSILE.getLocation(),
                        MAX_SPEED*ECCM, //if eccm is intalled the point is accurate, otherwise it's placed closer to the target (almost tailchasing)
                        target.getLocation(),
                        target.getVelocity()
                );                
                //null pointer protection
                if (lead == null) {
                    lead = target.getLocation(); 
                }
            } else {
                lead = target.getLocation();
            }
            
            Vector2f targetPointRotated = VectorUtils.rotate(new Vector2f(targetPoint), target.getFacing());
            Vector2f.add(lead, targetPointRotated, lead);
        }
        
        //best velocity vector angle for interception
        float correctAngle = VectorUtils.getAngle(
                        MISSILE.getLocation(),
                        lead
                );
        
        if (OVERSTEER){
            //velocity angle correction
            float offCourseAngle = MathUtils.getShortestRotation(
                    VectorUtils.getFacing(MISSILE.getVelocity()),
                    correctAngle
                    );

            float correction = MathUtils.getShortestRotation(                
                    correctAngle,
                    VectorUtils.getFacing(MISSILE.getVelocity())+180
                    ) 
                    * 0.5f * //oversteer
                    (float)((FastTrig.sin(MathUtils.FPI/90*(Math.min(Math.abs(offCourseAngle),45))))); //damping when the correction isn't important

            //modified optimal facing to correct the velocity vector angle as soon as possible
            correctAngle = correctAngle+correction;
        }
        
        if(WAVE_AMPLITUDE>0){            
            //waving
            float multiplier=1;
            if(ECCM<=1){
                multiplier=0.7f;
            }
            correctAngle+=multiplier*WAVE_AMPLITUDE*check*Math.cos(OFFSET+MISSILE.getElapsed()*(2*MathUtils.FPI/WAVE_TIME));
        }
        
        //target angle for interception
        float aimAngle = MathUtils.getShortestRotation( MISSILE.getFacing(), correctAngle) + swarmTrue;
        	// adding the swarm offset angle here
        
        if(OVERSHOT_ANGLE<=0 || Math.abs(aimAngle)<OVERSHOT_ANGLE){
            MISSILE.giveCommand(ShipCommand.ACCELERATE);  
        }
        
        if (aimAngle < 0) {
            MISSILE.giveCommand(ShipCommand.TURN_RIGHT);
        } else {
            MISSILE.giveCommand(ShipCommand.TURN_LEFT);
        }  
        
        // Damp angular velocity if the missile aim is getting close to the targeted angle
        if (Math.abs(aimAngle) < Math.abs(MISSILE.getAngularVelocity()) * DAMPING) {
            MISSILE.setAngularVelocity(aimAngle / DAMPING);
        }
    }
    
    //////////////////////
    //    TARGETING     //
    //////////////////////
    
    @Override
    public CombatEntityAPI getTarget() {
        return target;
    }

    @Override
    public void setTarget(CombatEntityAPI target) {
        this.target = target;
    }

    // Taken from Nicke535's homing projectile script, to emulate MISSILE_SPREAD behaviour. 
	//Used for getting a swarm target point, IE a random point offset on the target. Should only be used when target != null
	private void applySwarmOffset() {
		int i = 20; //We don't want to take too much time, even if we get unlucky: only try 20 times
		boolean success = false;
		while (i > 0 && target != null) {
			i--;

			//Get a random position and check if its valid
			Vector2f potPoint = MathUtils.getRandomPointInCircle(target.getLocation(), target.getCollisionRadius());
			if (CollisionUtils.isPointWithinBounds(potPoint, target)) {
				//If the point is valid, convert it to an offset and store it
				potPoint.x -= target.getLocation().x;
				potPoint.y -= target.getLocation().y;
				potPoint = VectorUtils.rotate(potPoint, -target.getFacing());
				targetPoint = new Vector2f(potPoint);
				success = true;
				break;
			}
		}

		//If we didn't find a point in 20 tries, just choose target center
		if (!success) {
			targetPoint = new Vector2f(Misc.ZERO);
		}
	}

public void init(CombatEngineAPI engine) {}
}