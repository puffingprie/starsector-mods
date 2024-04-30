package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import java.awt.Color;
import java.util.List;
import org.dark.shaders.distortion.DistortionShader;
import org.dark.shaders.distortion.WaveDistortion;
import org.dark.shaders.light.LightShader;
import org.dark.shaders.light.StandardLight;
import org.lazywizard.lazylib.FastTrig;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

// This whole system is wholly ripped from Cycerins BRDY mod
public class pn_pulse extends BaseShipSystemScript {

    private static final String CHARGEUP_SOUND = "pn_pulse_charge";
    private static final float DAMAGE_MOD_VS_CAPITAL = 0.15f;
    private static final float DAMAGE_MOD_VS_CRUISER = 0.15f;
    private static final float DAMAGE_MOD_VS_DESTROYER = 0.15f;
    private static final float DAMAGE_MOD_VS_FRIGATE = 0.15f;
    private static final float DAMAGE_MOD_VS_FIGHTER = 0.15f;
    private static final float DAMAGE_MOD_VS_MISSILE = 0.15f;

    // Distortion constants
    private static final float DISTORTION_BLAST_RADIUS = 300f;
    private static final float DISTORTION_CHARGE_RADIUS = 60f;

    // Explosion effect constants
    private static final Color EXPLOSION_COLOR = new Color(102, 10, 102);
    private static final float EXPLOSION_DAMAGE_AMOUNT = 200f;
    private static final DamageType EXPLOSION_DAMAGE_TYPE = DamageType.ENERGY;
    private static final float EXPLOSION_DAMAGE_VS_ALLIES_MODIFIER = .11f;
    private static final float EXPLOSION_EMP_DAMAGE_AMOUNT = 2000f;
    private static final float EXPLOSION_EMP_VS_ALLIES_MODIFIER = .05f;
    private static final float EXPLOSION_FORCE_VS_ALLIES_MODIFIER = .5f;
    private static final float EXPLOSION_PUSH_RADIUS = 500f;
    private static final String EXPLOSION_SOUND = "pn_pulse_burst";
    private static final float EXPLOSION_VISUAL_RADIUS = 350f;
    private static final float FORCE_VS_ASTEROID = 100f;
    private static final float FORCE_VS_CAPITAL = 15f;
    private static final float FORCE_VS_CRUISER = 25f;
    private static final float FORCE_VS_DESTROYER = 35f;
    private static final float FORCE_VS_FRIGATE = 45f;
    private static final float FORCE_VS_FIGHTER = 100f;
    private static final float FORCE_VS_MISSILE = 100f;

    // "Inhale" effect constants
    private static final int MAX_PARTICLES_PER_FRAME = 9; // Based on charge level
    private static final Color PARTICLE_COLOR = new Color(51, 0, 51);
    private static final float PARTICLE_OPACITY = 0.0f;
    private static final float PARTICLE_RADIUS = 230f;
    private static final float PARTICLE_SIZE = 1f;

    private static final Vector2f ZERO = new Vector2f();

    // Local variables, don't touch these
    private boolean isActive = false;
    private StandardLight light;
    private WaveDistortion wave;

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        // instanceof also acts as a null check
        if (!(stats.getEntity() instanceof ShipAPI)) {
            return;
        }

        ShipAPI ship = (ShipAPI) stats.getEntity();
        // Chargeup, show particle inhalation effect
        if (state == State.IN) {
            Vector2f loc = new Vector2f(ship.getLocation());
            loc.x -= 8f * FastTrig.cos(ship.getFacing() * Math.PI / 180f);
            loc.y -= 8f * FastTrig.sin(ship.getFacing() * Math.PI / 180f);

            // Everything in this block is only done once per chargeup
            if (!isActive) {
                isActive = true;
                Global.getSoundPlayer().playSound(CHARGEUP_SOUND, 1f, 1f, ship.getLocation(), ship.getVelocity());

                light = new StandardLight(loc, ZERO, ZERO, null);
                light.setIntensity(1f);
                light.setSize(EXPLOSION_VISUAL_RADIUS);
                light.setColor(PARTICLE_COLOR);
                light.fadeIn(1.05f);
                light.setLifetime(0.01f);
                light.setAutoFadeOutTime(0.017f);
                LightShader.addLight(light);

                wave = new WaveDistortion(loc, ZERO);
                wave.setSize(DISTORTION_CHARGE_RADIUS);
                wave.setIntensity(DISTORTION_CHARGE_RADIUS / 6f);
                wave.fadeInSize(1.05f);
                wave.fadeInIntensity(1.05f);
                wave.setLifetime(0f);
                wave.setAutoFadeSizeTime(-0.3f);
                wave.setAutoFadeIntensityTime(0.17f);
                DistortionShader.addDistortion(wave);
            } else {
                light.setLocation(loc);
                wave.setLocation(loc);
            }

            // Exact amount per second doesn't matter since it's purely decorative
            Vector2f particlePos, particleVel;
            int numParticlesThisFrame = Math.round(effectLevel * MAX_PARTICLES_PER_FRAME);
            for (int x = 0; x < numParticlesThisFrame; x++) {
                particlePos = MathUtils.getRandomPointOnCircumference(ship.getLocation(), PARTICLE_RADIUS);
                particleVel = Vector2f.sub(ship.getLocation(), particlePos, null);
                Global.getCombatEngine().addSmokeParticle(particlePos, particleVel, PARTICLE_SIZE, PARTICLE_OPACITY, 1f,
                                                          PARTICLE_COLOR);
            }
        } // Cooldown, explode once system is finished
        else if (state == State.OUT) {
            // Everything in this section is only done once per cooldown
            if (isActive) {
                CombatEngineAPI engine = Global.getCombatEngine();
                engine.spawnExplosion(ship.getLocation(), ship.getVelocity(), EXPLOSION_COLOR, EXPLOSION_VISUAL_RADIUS,
                                      0.21f);
                engine.spawnExplosion(ship.getLocation(), ship.getVelocity(), EXPLOSION_COLOR, EXPLOSION_VISUAL_RADIUS /
                                      2f, 0.19f);

                Vector2f loc = new Vector2f(ship.getLocation());
                loc.x -= 8f * FastTrig.cos(ship.getFacing() * Math.PI / 180f);
                loc.y -= 8f * FastTrig.sin(ship.getFacing() * Math.PI / 180f);

                light = new StandardLight();
                light.setLocation(loc);
                light.setIntensity(1.5f);
                light.setSize(EXPLOSION_VISUAL_RADIUS * 2f);
                light.setColor(EXPLOSION_COLOR);
                light.fadeOut(1f);
                LightShader.addLight(light);

                wave = new WaveDistortion();
                wave.setLocation(loc);
                wave.setSize(DISTORTION_BLAST_RADIUS);
                wave.setIntensity(DISTORTION_BLAST_RADIUS * 0.075f);
                wave.fadeInSize(0.8f);
                wave.fadeOutIntensity(1.2f);
                wave.setSize(DISTORTION_BLAST_RADIUS * 0.25f);
                DistortionShader.addDistortion(wave);

                Global.getSoundPlayer().playSound(EXPLOSION_SOUND, 1f, 1f, ship.getLocation(), ship.getVelocity());

                //The part below here was added to make it hit missiles instead, thanks to "Tim, IRS Mommy" from discord for the help in pointing out what to do
                
                MissileAPI missilevictim;
                Vector2f dir;
                float force, damage, emp, mod;
                List<CombatEntityAPI> entities = CombatUtils.getEntitiesWithinRange(ship.getLocation(),
                                                                                    EXPLOSION_PUSH_RADIUS);
                int size = entities.size();
                for (int i = 0; i < size; i++) {
                    CombatEntityAPI tmp = entities.get(i);
                    if (tmp == ship) {
                        continue;
                    }

                    mod = 1f - (MathUtils.getDistance(ship, tmp) / EXPLOSION_PUSH_RADIUS);
                    force = FORCE_VS_ASTEROID * mod;
                    damage = EXPLOSION_DAMAGE_AMOUNT * mod;
                    emp = EXPLOSION_EMP_DAMAGE_AMOUNT * mod;

                    
                    if (tmp instanceof MissileAPI) {
                        missilevictim = (MissileAPI) tmp;
                    
                        force = FORCE_VS_MISSILE * mod;
                        damage /= DAMAGE_MOD_VS_MISSILE;
                        
                    }
                    
                    
                    if (tmp instanceof MissileAPI) {
                        missilevictim = (MissileAPI) tmp;


                        {
                            MissileAPI empTarget = missilevictim;
                            for (int x = 0; x < 5; x++) {
                                engine.spawnEmpArc(ship, MathUtils.getRandomPointInCircle(missilevictim.getLocation(),
                                                                                          missilevictim.getCollisionRadius()),
                                                   empTarget,
                                                   empTarget, EXPLOSION_DAMAGE_TYPE, damage / 10, emp / 5,
                                                   EXPLOSION_PUSH_RADIUS, null, 2f, EXPLOSION_COLOR,
                                                   EXPLOSION_COLOR);
                            }
                        }
                    }

                    dir = VectorUtils.getDirectionalVector(ship.getLocation(), tmp.getLocation());
                    dir.scale(force);

                    Vector2f.add(tmp.getVelocity(), dir, tmp.getVelocity());
                }
                
                //up to here was added to make it hit missiles too
                
                
                
                ShipAPI victim;
                //Vector2f dir;
                //float force, damage, emp, mod;
                //List<CombatEntityAPI> entities = CombatUtils.getEntitiesWithinRange(ship.getLocation(),
                //                                                                    EXPLOSION_PUSH_RADIUS);
                //int size = entities.size();
                for (int i = 0; i < size; i++) {
                    CombatEntityAPI tmp = entities.get(i);
                    if (tmp == ship) {
                        continue;
                    }

                    mod = 1f - (MathUtils.getDistance(ship, tmp) / EXPLOSION_PUSH_RADIUS);
                    force = FORCE_VS_ASTEROID * mod;
                    damage = EXPLOSION_DAMAGE_AMOUNT * mod;
                    emp = EXPLOSION_EMP_DAMAGE_AMOUNT * mod;

                    
                    
                    if (tmp instanceof ShipAPI) {
                        victim = (ShipAPI) tmp;

                        // Modify push strength based on ship class
                        if (victim.getHullSize() == ShipAPI.HullSize.FIGHTER) {
                            force = FORCE_VS_FIGHTER * mod;
                            damage /= DAMAGE_MOD_VS_FIGHTER;
                        } else if (victim.getHullSize() == ShipAPI.HullSize.FRIGATE) {
                            force = FORCE_VS_FRIGATE * mod;
                            damage /= DAMAGE_MOD_VS_FRIGATE;
                        } else if (victim.getHullSize() == ShipAPI.HullSize.DESTROYER) {
                            force = FORCE_VS_DESTROYER * mod;
                            damage /= DAMAGE_MOD_VS_DESTROYER;
                        } else if (victim.getHullSize() == ShipAPI.HullSize.CRUISER) {
                            force = FORCE_VS_CRUISER * mod;
                            damage /= DAMAGE_MOD_VS_CRUISER;
                        } else if (victim.getHullSize() == ShipAPI.HullSize.CAPITAL_SHIP) {
                            force = FORCE_VS_CAPITAL * mod;
                            damage /= DAMAGE_MOD_VS_CAPITAL;
                        }

                        if (victim.getOwner() == ship.getOwner()) {
                            damage *= EXPLOSION_DAMAGE_VS_ALLIES_MODIFIER;
                            emp *= EXPLOSION_EMP_VS_ALLIES_MODIFIER;
                            force *= EXPLOSION_FORCE_VS_ALLIES_MODIFIER;
                        }


                        {
                            ShipAPI empTarget = victim;
                            for (int x = 0; x < 5; x++) {
                                engine.spawnEmpArc(ship, MathUtils.getRandomPointInCircle(victim.getLocation(),
                                                                                          victim.getCollisionRadius()),
                                                   empTarget,
                                                   empTarget, EXPLOSION_DAMAGE_TYPE, damage / 10, emp / 5,
                                                   EXPLOSION_PUSH_RADIUS, null, 2f, EXPLOSION_COLOR,
                                                   EXPLOSION_COLOR);
                            }
                        }
                    }

                    dir = VectorUtils.getDirectionalVector(ship.getLocation(), tmp.getLocation());
                    dir.scale(force);

                    Vector2f.add(tmp.getVelocity(), dir, tmp.getVelocity());
                }

                isActive = false;
            }
        }
    }

    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (state == State.IN) {
            if (index == 0) {
                return new StatusData("Charging EM Pulse", false);
            }
        }

        return null;
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
    }
}
