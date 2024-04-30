package data.weapons.beam;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.util.Misc;
import data.utils.FM_Misc;
import data.utils.visual.FM_ParticleManager;
import data.utils.visual.FM_StarParticle;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class FM_SparkBeamEffect implements BeamEffectPlugin {

    private static final Color PARTICLES_COLOR = new Color(143, 227, 229, 255);

    private float timer = 0f;

    public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
        CombatEntityAPI target = beam.getDamageTarget();
        Vector2f hit_loc = beam.getTo();
        float R = beam.getWidth() * 6f;
        float PARTICLE_NUMBER = beam.getWidth() * 0.5f;

        if (target instanceof ShipAPI) {

            timer = timer + amount;
            if (timer >= 0.1f) {
                engine.addHitParticle(beam.getTo(), FM_Misc.ZERO, beam.getWidth() * beam.getBrightness() * 5f, 255f, 0.2f, PARTICLES_COLOR);
                timer = 0f;
            }

            FM_StarParticle visual = FM_ParticleManager.getStarParticleManager(engine);

            visual.addStarParticle(
                    hit_loc,
                    MathUtils.getRandomPointOnCircumference(new Vector2f(), MathUtils.getRandomNumberInRange(60f,80f)),
                    MathUtils.getRandomNumberInRange(6f, 8f),
                    0.1f,
                    MathUtils.getRandomNumberInRange(0.6f,0.9f),
                    Misc.scaleAlpha(PARTICLES_COLOR, 0.7f * beam.getBrightness()),
                    6f,
                    0f,
                    MathUtils.getRandomNumberInRange(360f, 720f),
                    Math.random() < 0.5f
            );

            for (int i = 0; i < PARTICLE_NUMBER * beam.getBrightness(); i++) {

                engine.addHitParticle(hit_loc, MathUtils.getRandomPointOnCircumference(null, MathUtils.getRandomNumberInRange(R + 50f,R + 80f)), MathUtils.getRandomNumberInRange(2, 5), 30f, MathUtils.getRandomNumberInRange(0.6f,0.9f), PARTICLES_COLOR);
            }

        }
    }
}
