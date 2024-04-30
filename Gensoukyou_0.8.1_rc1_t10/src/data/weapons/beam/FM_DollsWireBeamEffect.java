package data.weapons.beam;

import com.fs.starfarer.api.combat.*;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class FM_DollsWireBeamEffect implements BeamEffectPlugin {

    public static final float EMP_ARC = 0.33f;

    private float TIMER = 0f;

    @Override
    public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
        CombatEntityAPI target = beam.getDamageTarget();
        if (target instanceof ShipAPI) {
            boolean hitShield = target.getShield() != null && target.getShield().isWithinArc(beam.getTo());
            if (hitShield) return;
            TIMER = TIMER + engine.getElapsedInLastFrame();
            if (TIMER >= 0.2f) {
                float random = MathUtils.getRandomNumberInRange(0f, 1f);
                if (random <= EMP_ARC) {
                    Vector2f point = beam.getRayEndPrevFrame();
                    float emp = beam.getDamage().getFluxComponent();
                    float dam = beam.getDamage().getDamage();
                    engine.spawnEmpArcPierceShields(
                            beam.getSource(), point, beam.getDamageTarget(), beam.getDamageTarget(),
                            DamageType.ENERGY,
                            dam, // damage
                            emp, // emp
                            100000f, // max range
                            "tachyon_lance_emp_impact",
                            beam.getWidth() + 9f,
                            beam.getFringeColor(),
                            beam.getCoreColor()
                    );
                }

                TIMER = 0f;

            }
        }
    }
}
