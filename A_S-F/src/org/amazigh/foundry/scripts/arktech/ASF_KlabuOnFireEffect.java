package org.amazigh.foundry.scripts.arktech;

import java.awt.Color;

import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;

public class ASF_KlabuOnFireEffect implements OnFireEffectPlugin {
    
    private static final Color FLASH_COLOR = new Color(170,80,30,220);
    
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {

            ShipAPI ship = weapon.getShip();
            Vector2f ship_velocity = ship.getVelocity();
            Vector2f proj_location = projectile.getLocation();
            engine.spawnExplosion(proj_location, ship_velocity, FLASH_COLOR, 11f, 0.35f);
            
            engine.addHitParticle(proj_location, ship_velocity, 75f, 1f, 0.1f, FLASH_COLOR.brighter());
            
        	for (int i=0; i < 11; i++) {
    			float angle1 = projectile.getFacing() + MathUtils.getRandomNumberInRange(-3f, 3f);
                Vector2f smokeVel = MathUtils.getPointOnCircumference(ship.getVelocity(), i * 3f, angle1);
                
                engine.addNebulaParticle(proj_location, smokeVel,
                		MathUtils.getRandomNumberInRange(11f, 23f),
                		1.6f, //endsizemult
                		0.1f, //rampUpFraction
                		0.3f, //fullBrightnessFraction
                		MathUtils.getRandomNumberInRange(1.8f, 2.6f), //totalDuration
                		new Color(55,45,40,111),
                		true);
                
                for (int j=0; j < 4; j++) {

        			float angle2 = projectile.getFacing() + MathUtils.getRandomNumberInRange(-36f, 36f);
                    Vector2f sparkVel = MathUtils.getPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(11f, 121f), angle2);

                    engine.addSmoothParticle(MathUtils.getRandomPointInCircle(proj_location, 3f),
                    		sparkVel,
            				MathUtils.getRandomNumberInRange(3f, 6f), //size
            				1f, //brightness
            				MathUtils.getRandomNumberInRange(0.4f, 0.6f), //duration
            				FLASH_COLOR);
                	}
        	}
        	
    }
  }