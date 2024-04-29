package org.amazigh.foundry.scripts.supe;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;
import org.jetbrains.annotations.NotNull;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ASF_chillblainProjScript extends BaseEveryFrameCombatPlugin {

	private static final float TIMER = 5f; // The detonation time
	private static final float RADIUS = 1200; // The aoe radius

	private DamagingProjectileAPI proj; // The projectile itself
	private float timeCounter, fxCounter1, fxCounter2, arcCounter; // Keep track of time stuff
	
	private static Map<HullSize, Float> hullDamMult = new HashMap<HullSize, Float>();
	static {
		hullDamMult.put(HullSize.FIGHTER, 0.8f);
		hullDamMult.put(HullSize.FRIGATE, 0.9f);
		hullDamMult.put(HullSize.DESTROYER, 1f);
		hullDamMult.put(HullSize.CRUISER, 1.15f);
		hullDamMult.put(HullSize.CAPITAL_SHIP, 1.35f);
		hullDamMult.put(HullSize.DEFAULT, 1f);
	}
	
	private static Map<HullSize, Float> hullDamFlat = new HashMap<HullSize, Float>();
	static {
		hullDamFlat.put(HullSize.FIGHTER, 50f);
		hullDamFlat.put(HullSize.FRIGATE, 70f);
		hullDamFlat.put(HullSize.DESTROYER, 100f);
		hullDamFlat.put(HullSize.CRUISER, 150f);
		hullDamFlat.put(HullSize.CAPITAL_SHIP, 250f);
		hullDamFlat.put(HullSize.DEFAULT, 100f);
	}
	
	public ASF_chillblainProjScript(@NotNull DamagingProjectileAPI proj) {
		this.proj = proj;
	}
	
	//Main advance method
	@Override
	public void advance(float amount, List<InputEventAPI> events) {
		//Sanity checks
		if (Global.getCombatEngine() == null) {
			return;
		}
		CombatEngineAPI engine = Global.getCombatEngine();
		if (engine.isPaused()) {
			amount = 0f;
		}
		//Checks if our script should be removed from the combat engine
		if (proj == null || proj.didDamage() || proj.isFading() || !engine.isEntityInPlay(proj)) {
			engine.removePlugin(this);
			return;
		}
			
		Vector2f projLoc = proj.getLocation();
		Vector2f projVel = proj.getVelocity();
		ShipAPI source = proj.getSource();
		
		//Ticks up our time counter, and when it hits TIMER, D E T O N A T E
		timeCounter+=amount;
		if (timeCounter > TIMER) {

			// sum VFX
			for (int pulse=0; pulse < 400; pulse++) {
	            float angle1 = MathUtils.getRandomNumberInRange(0f, 360f);
	            float randScale1 = MathUtils.getRandomNumberInRange(0.3f*RADIUS, 1.8f*RADIUS);
	            
	            Vector2f pulseLoc = MathUtils.getRandomPointInCircle(projLoc, 50f);
	            Vector2f pulseVel = MathUtils.getPointOnCircumference(projVel, randScale1, angle1);

	            engine.addSmoothParticle(pulseLoc,
	            		pulseVel,
	            		MathUtils.getRandomNumberInRange(7f, 21f), //size
	            		1.0f, //brightness
	            		MathUtils.getRandomNumberInRange(0.45f, 0.65f), //duration
	            		new Color(75,175,250,255));
	        }
			engine.addSmoothParticle(projLoc,
            		projVel,
            		640f, //size
            		1.0f, //brightness
            		1f, //duration
            		new Color(90,180,250,255));
			for (int pulse1=0; pulse1 < 12; pulse1++) {
				
				engine.addNebulaParticle(MathUtils.getRandomPointOnCircumference(projLoc, 30f),
						MathUtils.getRandomPointOnCircumference(projVel, MathUtils.getRandomNumberInRange(10f, 80f)),
						MathUtils.getRandomNumberInRange(100f, 140f),
						MathUtils.getRandomNumberInRange(1.2f, 1.6f),
						0.4f,
						0.6f,
						MathUtils.getRandomNumberInRange(0.8f, 1.2f),
						new Color(40,140,250,200));
				
				engine.addSwirlyNebulaParticle(MathUtils.getRandomPointOnCircumference(projLoc, 30f),
						MathUtils.getRandomPointOnCircumference(projVel, MathUtils.getRandomNumberInRange(10f, 80f)),
						MathUtils.getRandomNumberInRange(100f, 140f),
						MathUtils.getRandomNumberInRange(1.2f, 1.6f),
						0.4f,
						0.6f,
						MathUtils.getRandomNumberInRange(0.8f, 1.2f),
						new Color(75,175,250,225),
						false);
				for (int pulse2=0; pulse2 < 2; pulse2++) {
					engine.spawnEmpArcVisual(MathUtils.getRandomPointOnCircumference(projLoc, MathUtils.getRandomNumberInRange(1f, 30f)),
							proj,
							MathUtils.getRandomPointOnCircumference(projLoc, MathUtils.getRandomNumberInRange(RADIUS * 0.1f, RADIUS * 0.4f)),
							proj,
							13f,
							new Color(50,100,255,255),
							new Color(0,0,0,255));
				}
			}
			
			
			// A R C S
				// and
			// E X P L O S I O N
			for (ShipAPI target_ship : engine.getShips()) {
				// check if the ship is a valid target
				if (target_ship.isHulk()) {
					continue;
				}
				
				float tag_radius = target_ship.getCollisionRadius();
				float tag_mass = target_ship.getMass();
				
				// check if the ship is NOT phased and in range, if yes to both, then it's valid
				if ((MathUtils.getDistanceSquared(projLoc, target_ship.getLocation()) <= (RADIUS * RADIUS) + (tag_radius * tag_radius) + 100f)) {
					
					Vector2f tag_vel = target_ship.getVelocity();
					
					// we sneakily increase the targets flux, IT'S BULLYING TIME! (soft rather than hard, so we don't overload things that shouldn't be overloadable)
					target_ship.getFluxTracker().increaseFlux((tag_mass * 0.5f) + (tag_radius * 0.5f), false);
					
					if (!target_ship.isPhased() ) {
						
						float FF_mult = 1f;
						if (target_ship.getOwner() == source.getOwner()) {
							FF_mult = 0.5f;
						}
						
						if (target_ship.isStationModule()) {
							FF_mult *= hullDamMult.get(target_ship.getParentStation().getHullSize());
						}
						
						// NOW THAT'S A LOT OF DAMAGE (for big ships : 5k+ mass)
						FF_mult *= (Math.max(5000f, tag_mass) / 5000f);
						
						float arcValue = (tag_radius/2) + (tag_mass/6);
						
						for (int i=0; i < arcValue; i += 30) {
							Vector2f arcSource = MathUtils.getRandomPointInCircle(target_ship.getLocation(), tag_radius);
							
							EmpArcEntityAPI arc = engine.spawnEmpArc(source,
									arcSource,
									target_ship,
									target_ship,
									DamageType.ENERGY,
									(tag_mass / 10f) * FF_mult * hullDamMult.get(target_ship.getHullSize()),
									(tag_radius / 10f) * FF_mult * hullDamMult.get(target_ship.getHullSize()),
									tag_radius * 2.5f,
									"tachyon_lance_emp_impact",
									15f, // thickness
									new Color(50,100,255,255),
									new Color(0,0,0,255));
							
							Vector2f arcEnd = arc.getTargetLocation();
							
							for (int j1=0; j1 < 2; j1++) {
								
								Vector2f offsetVel0 = MathUtils.getRandomPointOnCircumference(tag_vel, MathUtils.getRandomNumberInRange(0f, 30f));
					            Vector2f offsetVel01 = MathUtils.getRandomPointOnCircumference(tag_vel, MathUtils.getRandomNumberInRange(0f, 30f));
								
								engine.addNebulaParticle(arcSource,
										offsetVel0,
										MathUtils.getRandomNumberInRange(25f, 30f),
										MathUtils.getRandomNumberInRange(1.6f, 1.9f),
										0.4f,
										0.3f,
										MathUtils.getRandomNumberInRange(0.4f, 0.6f),
										new Color(40,140,250,225)); 
								engine.addSwirlyNebulaParticle(arcSource,
										offsetVel01,
										MathUtils.getRandomNumberInRange(25f, 30f),
										MathUtils.getRandomNumberInRange(1.6f, 1.9f),
										0.4f,
										0.3f,
										MathUtils.getRandomNumberInRange(0.4f, 0.6f),
										new Color(40,140,250,225), false);
								
								Vector2f offsetVel02 = MathUtils.getRandomPointOnCircumference(tag_vel, MathUtils.getRandomNumberInRange(0f, 30f));
					            Vector2f offsetVel03 = MathUtils.getRandomPointOnCircumference(tag_vel, MathUtils.getRandomNumberInRange(0f, 30f));
								
								engine.addNebulaParticle(arcEnd,
										offsetVel02,
										MathUtils.getRandomNumberInRange(25f, 30f),
										MathUtils.getRandomNumberInRange(1.6f, 1.9f),
										0.4f,
										0.3f,
										MathUtils.getRandomNumberInRange(0.4f, 0.6f),
										new Color(40,140,250,225)); 
								engine.addSwirlyNebulaParticle(arcEnd,
										offsetVel03,
										MathUtils.getRandomNumberInRange(25f, 30f),
										MathUtils.getRandomNumberInRange(1.6f, 1.9f),
										0.4f,
										0.3f,
										MathUtils.getRandomNumberInRange(0.4f, 0.6f),
										new Color(40,140,250,225), false);
							}
						}
					}
					
					float blastDamage = (((tag_mass * 0.2f) + tag_radius) * hullDamMult.get(target_ship.getHullSize())) + hullDamFlat.get(target_ship.getHullSize());
					float radMod = 1f;
					
					if (target_ship.isStationModule() || target_ship.isShipWithModules()) {
						blastDamage *= 0.25f;
						radMod *= 0.5f;
					}
					
					// lets really try and stop stations from doing a fucky-wucky oopsie-woopsie
					if (target_ship.isStation()) {
						blastDamage *= 0.1f;
						radMod *= 0.5f;
					}
					
					if (target_ship.getOwner() == source.getOwner()) {
						blastDamage *= 0.5f;
					}
					
					// "clown ships get clown damage"
					blastDamage *= (Math.max(5000f, tag_mass) / 5000f);
					
					DamagingExplosionSpec blast = new DamagingExplosionSpec(0.2f,
							tag_radius * 0.8f * radMod,
							tag_radius * 0.3f * radMod,
			                blastDamage,
			                blastDamage * 0.4f,
			                CollisionClass.PROJECTILE_FF,
			                CollisionClass.PROJECTILE_FIGHTER,
			                5f,
			                5f,
			                1f,
			                120,
			                new Color(50,100,225,255),
			                new Color(70,175,255,200));
			        blast.setDamageType(DamageType.ENERGY);
			        blast.setShowGraphic(true);
			        blast.setDetailedExplosionFlashColorCore(new Color(60,120,240,255));
			        blast.setDetailedExplosionFlashColorFringe(new Color(50,100,225,125));
			        blast.setUseDetailedExplosion(true);
			        blast.setDetailedExplosionRadius(tag_radius * 1.2f * radMod);
			        blast.setDetailedExplosionFlashRadius(tag_radius * 2.5f * radMod);
			        blast.setDetailedExplosionFlashDuration(0.6f);
			        
			        engine.spawnDamagingExplosion(blast,source,target_ship.getLocation(),true);
					
					for (int j0=0; j0 < tag_radius; j0+=20) {
						Vector2f offsetVel0 = MathUtils.getRandomPointOnCircumference(tag_vel, MathUtils.getRandomNumberInRange(0f, 50f));
			            Vector2f offsetVel01 = MathUtils.getRandomPointOnCircumference(tag_vel, MathUtils.getRandomNumberInRange(0f, 50f));
			            Vector2f point0 = MathUtils.getRandomPointOnCircumference(target_ship.getLocation(), MathUtils.getRandomNumberInRange(0f, tag_radius * 0.3f * radMod));
			            Vector2f point01 = MathUtils.getRandomPointOnCircumference(target_ship.getLocation(), MathUtils.getRandomNumberInRange(0f, tag_radius * 0.3f * radMod));
			            
						engine.addNebulaParticle(point0,
								offsetVel0,
								MathUtils.getRandomNumberInRange(35f, 50f),
								MathUtils.getRandomNumberInRange(1.6f, 1.9f),
								0.4f,
								0.3f,
								MathUtils.getRandomNumberInRange(0.4f, 0.6f),
								new Color(40,140,250,225)); 
						engine.addSwirlyNebulaParticle(point01,
								offsetVel01,
								MathUtils.getRandomNumberInRange(35f, 50f),
								MathUtils.getRandomNumberInRange(1.6f, 1.9f),
								0.4f,
								0.3f,
								MathUtils.getRandomNumberInRange(0.4f, 0.6f),
								new Color(40,140,250,225), false);
					}
				}
			}
			
			
			// sound
			Global.getSoundPlayer().playSound("hit_heavy_energy", 0.8f, 1.5f, projLoc, projVel);
			Global.getSoundPlayer().playSound("tachyon_lance_emp_impact", 1.0f, 1.25f, projLoc, projVel);			
			
			engine.removeEntity(proj);
		}
		
		
		fxCounter1+=amount;
		fxCounter2+=amount;
		arcCounter += (amount * MathUtils.getRandomNumberInRange(0.5f, 0.6f + (timeCounter * 0.3f)));
		if (fxCounter1 > 0.5f) {
			
			// nebular core
            engine.addSmoothParticle(projLoc,
            		proj.getVelocity(),
            		320f, //size
            		1.0f, //brightness
            		MathUtils.getRandomNumberInRange(0.8f, 1.2f), //duration
            		new Color(40,140,250,230));
            
			for (int i=0; i < 3; i++) {
				
				Vector2f offsetLoc0 = MathUtils.getRandomPointOnCircumference(projLoc, 8f * timeCounter);
				Vector2f offsetLoc01 = MathUtils.getRandomPointOnCircumference(projLoc, 8f * timeCounter);
				
				Vector2f offsetVel0 = MathUtils.getRandomPointOnCircumference(projVel, MathUtils.getRandomNumberInRange(0f, 20f * timeCounter));
	            Vector2f offsetVel01 = MathUtils.getRandomPointOnCircumference(projVel, MathUtils.getRandomNumberInRange(0f, 20f * timeCounter));
				
				engine.addNebulaParticle(offsetLoc0,
						offsetVel0,
						MathUtils.getRandomNumberInRange(100f, 140f),
						MathUtils.getRandomNumberInRange(0.4f, 0.75f),
						0.4f,
						0.3f,
						MathUtils.getRandomNumberInRange(0.8f, 1.2f),
						new Color(40,140,250,200));
				
				engine.addSwirlyNebulaParticle(offsetLoc01,
						offsetVel01,
						MathUtils.getRandomNumberInRange(100f, 140f),
						MathUtils.getRandomNumberInRange(0.4f, 0.75f),
						0.4f,
						0.3f,
						MathUtils.getRandomNumberInRange(0.8f, 1.2f),
						new Color(50,150,250,200),
						false);
			}
			fxCounter1 -= 0.5f;
		}
		
		if (arcCounter > 0.2f) {
			// AAAAAAAAAAAAAAAAAAAAA(rcs)
			for (ShipAPI target_ship : engine.getShips()) {
				// check if the ship is a valid target
				if (target_ship.isHulk()) {
					continue;
				}
				
				float tag_radius = target_ship.getCollisionRadius();
				float tag_mass = target_ship.getMass();
				
				// check if the ship is NOT phased and in range, if yes to both, then it's valid
				if ((MathUtils.getDistanceSquared(projLoc, target_ship.getLocation()) <= (RADIUS * RADIUS) + (tag_radius * tag_radius) + 100f) && (!target_ship.isPhased())) {
					
					float FF_mult = 1f;
					if (target_ship.getOwner() == source.getOwner()) {
						FF_mult = 0.5f;
					}
					
					float arcChance = 0.6f;
					float arcCount = 1f;
					
					if (tag_mass < 100f) {
						arcChance = 0.92f;
					} else if (tag_mass < 500f) {
						arcChance = 0.8f;
					} else if (tag_mass > 2500f) {
						arcChance = 0.3f;
						arcCount = MathUtils.getRandomNumberInRange(0.9f, 3f);
						// NOW THAT'S A LOT OF DAMAGE (for big ships : 5k+ mass)
						FF_mult *= (Math.max(5000f, tag_mass) / 5000f);
					} else if (tag_mass > 1500f) {
						arcChance = 0.4f;
						arcCount = MathUtils.getRandomNumberInRange(0.9f, 2f);
					}
					
					if (Math.random() > arcChance) {
						for (int aarc=0; aarc < arcCount; aarc++) {
							
							Vector2f arcSauce = MathUtils.getRandomPointInCircle(projLoc, 30f);
							
							engine.spawnEmpArc(source,
									arcSauce,
									proj,
									target_ship,
									DamageType.ENERGY,
									(tag_mass / 12.5f) * FF_mult * hullDamMult.get(target_ship.getHullSize()),
									(tag_radius / 12.5f) * FF_mult * hullDamMult.get(target_ship.getHullSize()),
									RADIUS + (tag_radius * 2f),
									"tachyon_lance_emp_impact",
									12f, // thickness
									new Color(50,100,255,255),
									new Color(0,0,0,255));
						}
					}
				}
			}
			arcCounter -= 0.2f;
		}
		
		if (fxCounter2 > 0.05f) {	
			// inwards particles
			for (int i2=0; i2 < 150; i2++) {
	            float angle1 = MathUtils.getRandomNumberInRange(0f, 360f);
	            float randScale1 = MathUtils.getRandomNumberInRange(0.25f*RADIUS, RADIUS);
	            
	            Vector2f offsetVel1 = MathUtils.getPointOnCircumference(projVel, randScale1 * 1.2f, angle1);
	            Vector2f point1 = MathUtils.getPointOnCircumference(projLoc, randScale1, angle1 + 180f);

	            engine.addSmoothParticle(point1,
	            		offsetVel1,
	            		MathUtils.getRandomNumberInRange(5f, 11f), //size
	            		1.0f, //brightness
	            		MathUtils.getRandomNumberInRange(1f, 1.2f), //duration
	            		new Color(40,140,250,225));
	        }
			
			// "ring" particles
			for (int i3=0; i3 < 40; i3++) {
	            float inRing = RADIUS -24f;
	            
	            float angle1 = MathUtils.getRandomNumberInRange(0f, 360f);
	            Vector2f offsetVel1 = MathUtils.getPointOnCircumference(projVel, MathUtils.getRandomNumberInRange(40f, 75f), angle1 + MathUtils.getRandomNumberInRange(85f, 115f));
	            Vector2f point1 = MathUtils.getPointOnCircumference(projLoc, inRing, angle1);

	            engine.addSmoothParticle(point1,
	            		offsetVel1,
	            		MathUtils.getRandomNumberInRange(6f, 14f), //size
	            		0.8f, //brightness
	            		MathUtils.getRandomNumberInRange(0.15f, 0.35f), //duration
	            		new Color(75,175,250,200));
	            
	            float angle2 = MathUtils.getRandomNumberInRange(0f, 360f);
	            Vector2f offsetVel2 = MathUtils.getPointOnCircumference(projVel, MathUtils.getRandomNumberInRange(40f, 75f), angle2 - MathUtils.getRandomNumberInRange(85f, 115f));
	            Vector2f point2 = MathUtils.getPointOnCircumference(projLoc, inRing, angle2);
	            
	            engine.addSmoothParticle(point2,
	            		offsetVel2,
	            		MathUtils.getRandomNumberInRange(6f, 14f), //size
	            		0.8f, //brightness
	            		MathUtils.getRandomNumberInRange(0.15f, 0.35f), //duration
	            		new Color(75,175,250,200));
	        }
			
			fxCounter2 -= 0.05f;
		}
	}
}