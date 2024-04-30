package data.weapons.test;

import com.fs.starfarer.api.combat.*;
import org.dark.shaders.distortion.DistortionShader;
import org.dark.shaders.distortion.WaveDistortion;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicLensFlare;

import java.awt.*;
import java.util.List;

public class FM_BBA_built_in_everyfarme implements EveryFrameWeaponEffectPlugin {

    public float TIMER = 0;
    public float ANG = 0;
    public float DIR = 0;

    public float SIZE = 0f;
    public float intensity = 75f;

    private WaveDistortion wave = null;


    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {

        List<DamagingProjectileAPI> projects = engine.getProjectiles();

        float break_range = weapon.getRange() * 0.4f;
        float time = break_range / weapon.getProjectileSpeed();

        for (DamagingProjectileAPI project : projects) {
            if (project.getWeapon() == weapon && project.getProjectileSpecId().equals("FM_wave_particle_proj")) {

                if (project.getElapsed() >= time) {

                    if (wave == null) {
                        wave = new WaveDistortion();
                        wave.setLocation(project.getLocation());
                        wave.setSize(project.getCollisionRadius());
                        wave.setIntensity(50f);
                        wave.setArc(0, 360);
                        wave.flip(false);

                        DistortionShader.addDistortion(wave);
                        engine.spawnExplosion(project.getLocation(), new Vector2f(), Color.BLUE, 70f, 4f);
                    }

                    if (wave != null) {
                        if (SIZE == 0f) {
                            SIZE = project.getCollisionRadius();
                        } else {
                            SIZE = SIZE + amount * 20f;
                        }


                        float r = MathUtils.getRandomNumberInRange(SIZE, 1.3f * SIZE);
                        wave.setSize(r);


                        intensity = intensity - 10 * amount;
                        wave.setIntensity(intensity);


                    }


                    project.getVelocity().set(0, 0);

                    Vector2f proj_loc = project.getLocation();

                    ShipAPI the_ship = weapon.getShip();

                    TIMER = TIMER + amount;
                    if (TIMER >= 0.1f) {
                        engine.spawnProjectile(the_ship, weapon, "FM_Blade_ac", proj_loc, project.getFacing() + DIR, null);
                        engine.spawnProjectile(the_ship, weapon, "FM_Blade_ac", proj_loc, project.getFacing() + 60 + DIR, null);
                        engine.spawnProjectile(the_ship, weapon, "FM_Blade_ac", proj_loc, project.getFacing() + 120 + DIR, null);
                        engine.spawnProjectile(the_ship, weapon, "FM_Blade_ac", proj_loc, project.getFacing() + 180 + DIR, null);
                        engine.spawnProjectile(the_ship, weapon, "FM_Blade_ac", proj_loc, project.getFacing() + 240 + DIR, null);
                        engine.spawnProjectile(the_ship, weapon, "FM_Blade_ac", proj_loc, project.getFacing() + 300 + DIR, null);
                        ANG = ANG + 1.5f;
                        DIR = ANG + DIR;
                        TIMER = 0;

                        if (project.isFading()) {
                            MagicLensFlare.createSharpFlare(engine, weapon.getShip(), project.getLocation(), 7f, 150f, 0f,
                                    new Color(145, 100, 253, 218)
                                    , new Color(153, 221, 243, 255));
                        }

                    }

                }


                if (project.getElapsed() > time + 5f) {
                    if (wave != null) {
                        wave.fadeOutSize(0.3f);
                        wave.fadeOutIntensity(0.3f);
                        SIZE = 0f;
                        intensity = 50f;
                        wave = null;

                    }
                    engine.removeEntity(project);
                    engine.spawnExplosion(project.getLocation(), project.getVelocity(), Color.BLUE, 20f, 1f);


                }

            }
        }


    }
}
