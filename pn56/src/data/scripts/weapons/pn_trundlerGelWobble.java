package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

public class pn_trundlerGelWobble implements EveryFrameWeaponEffectPlugin {
    
    // the power of the leap, determines if the ship can leap in a completely different direction as it was travelling before.
    private final int LEAP_FORCE = 1000;
    // the mass multiplier, determines the amount of damage dealt by the collision
    private final int COLLISION_MULTIPLIER = 10;
    
    // the amount of wobblelling, determines the amplitude of the deformation
    private final float WOBBLE_INTENSITY = 0.01f;
    // the speed of wobblelling, determines the frequency of the deformation
    private final float WOBBLE_FREQUENCY = 1;
    // the instability of the wobblelling, determine the speed of the frequency changes
    private final float WOBBLE_REACTIVITY = 0.01f;
    
    private ShipAPI ship;
    private float height;
    private float width;
    private float timer = 0;
    private float freq = 1;
    private float scale = 1;
    private float maxSpeed;
    private float shipMass;
    
    private boolean runOnce = false;
    private boolean leap=false;    
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        
        if (engine.isPaused() || !weapon.getShip().isAlive()) {
            return;
        }
        
        //data collection
        if (!runOnce){         
            ship = weapon.getShip();
            height = ship.getSpriteAPI().getHeight();
            width = ship.getSpriteAPI().getWidth();
            maxSpeed = ship.getMutableStats().getMaxSpeed().getBaseValue();
            shipMass = ship.getMass();
            runOnce = true;
        }
        
        //WOBBLE TECHNOLOGY
        //the frequency increase as the ship accelerate
        if (    ship.getEngineController().isAccelerating() ||
                ship.getEngineController().isAcceleratingBackwards() ||
                ship.getEngineController().isDecelerating() ||
                ship.getEngineController().isStrafingLeft() ||
                ship.getEngineController().isStrafingRight()
                ) {
            freq +=3*WOBBLE_REACTIVITY;
            if (freq>3*WOBBLE_FREQUENCY){freq=3*WOBBLE_FREQUENCY;}            
        } else {
            //and decrease back to normal when idle or coasting
            freq -= WOBBLE_REACTIVITY;
            if (freq<WOBBLE_FREQUENCY){freq=WOBBLE_FREQUENCY;}
        }
        
        //the amplitude rise as the ship goes faster
        scale = (float) (2*WOBBLE_INTENSITY + WOBBLE_INTENSITY * (ship.getVelocity().length()/maxSpeed));        
        timer +=amount*freq;
        
        //calculate the deformation
        float waveX = height * (1 + ((float)Math.sin(timer)*scale));
        float waveY = width * (1 + ((float)Math.sin(timer-4.5)*scale));
        
        //apply the deformation
        weapon.getSprite().setSize(waveY, waveX);
        weapon.getSprite().setCenter(waveY/2, waveX/2);
        
        //LEAP SYSTEM
        //leap once when the system activate
        if (!leap && ship.getSystem().isStateActive()){
            leap=true;
            //choose the direction of the leap
            Vector2f direction;             
            //if the ship has a valid target, leap towards it, leading the target
            if (!ship.isRetreating() && ship.getShipTarget()!=null && ship.getShipTarget().getOwner()!=ship.getOwner() && ship.getShipTarget().isAlive()){
                Vector2f lead = AIUtils.getBestInterceptPoint(ship.getLocation(), 600, ship.getShipTarget().getLocation(), new Vector2f(ship.getShipTarget().getVelocity().x*0.75f,ship.getShipTarget().getVelocity().y*0.75f));
                if (lead != null){
                    direction = VectorUtils.getDirectionalVector(ship.getLocation(), lead);
                } else {
                    direction = VectorUtils.getDirectionalVector(ship.getLocation(), ship.getShipTarget().getLocation());
                }
            } else {
                //else, just leap in the curent direction
                direction=ship.getVelocity();
            }
            //apply leap and increase mass for more damage
            CombatUtils.applyForce(ship, direction, LEAP_FORCE);
            ship.setMass(shipMass*COLLISION_MULTIPLIER);
            
        } else if (leap && !ship.getSystem().isActive()){
            //when the system shut down, reset the mass and leap check
            leap=false;
            ship.setMass(shipMass);
        }  
    }
}