package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.BeamEffectPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.shipsystems.II_LuxFinisStats;
import data.scripts.util.II_Util;
import java.awt.Color;
import java.util.ArrayList;
import org.dark.shaders.light.LightShader;
import org.dark.shaders.light.StandardLight;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.entities.AnchoredEntity;
import org.lwjgl.util.vector.Vector2f;

public class II_LightspearStationBeamEffect implements BeamEffectPlugin {

    public static final float LIGHTSPEAR_ATTEN_PER_DISABLED_PB = 0.15f;

    private static final Color FLASH_COLOR = new Color(255, 255, 255, 50);

    private static final float ENDLIGHT_INTENSITY = 0.65f;
    private static final float ENDLIGHT_SIZE = 650f;
    private static final float BEAMLIGHT_INTENSITY = 1.3f;
    private static final float BEAMLIGHT_SIZE = 215f;
    private static final float FULL_WIDTH = 51.2f;

    private static final Vector2f ZERO = new Vector2f();

    private boolean firing = false;
    private final IntervalUtil interval = new IntervalUtil(0.1f, 0.1f);
    private final IntervalUtil interval2 = new IntervalUtil(0.015f, 0.015f);
    private float level = 0f;
    private float sinceLast = 0f;
    private StandardLight endLight = null;
    private StandardLight beamLight = null;

    @Override
    public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
        if (engine.isPaused()) {
            return;
        }

        ShipAPI ship = beam.getWeapon().getShip();
        Color GLOW_COLOR = II_LuxFinisStats.GLOW_COLOR_STANDARD;
        Color FRINGE_COLOR = II_LuxFinisStats.FRINGE_COLOR_STANDARD;

        Vector2f origin = new Vector2f(beam.getWeapon().getLocation());
        Vector2f offset;
        if (beam.getWeapon().getSlot().isHardpoint()) {
            offset = new Vector2f(5.75f, 0f);
        } else {
            offset = new Vector2f(1f, 0f);
        }
        VectorUtils.rotate(offset, beam.getWeapon().getCurrAngle(), offset);
        Vector2f.add(offset, origin, origin);

        ArrayList<WeaponAPI> photonBeams = new ArrayList<>(3);
        for (WeaponAPI shipWeapon : ship.getAllWeapons()) {
            if (shipWeapon.getId().startsWith("ii_photonbeam")
                    && !shipWeapon.isDisabled() && !shipWeapon.isPermanentlyDisabled()) {
                photonBeams.add(shipWeapon);
            }
        }
        float scale = (beam.getBrightness() * 0.6f) + 0.4f;
        float loss = (3 - photonBeams.size()) * LIGHTSPEAR_ATTEN_PER_DISABLED_PB * beam.getBrightness();
        scale -= loss;
        float overlevel = II_LuxFinisStats.getOverlevel(ship);
        scale *= (float) Math.sqrt(overlevel);

        if (beam.getWeapon().isDisabled()) {
            scale = 0.001f;
        }
        beam.setWidth(FULL_WIDTH * scale);
        beam.setCoreColor(new Color(255, 255, 255, II_Util.clamp255((int) (255 * Math.sqrt(scale)))));
        beam.setFringeColor(new Color(FRINGE_COLOR.getRed(), FRINGE_COLOR.getGreen(), FRINGE_COLOR.getBlue(),
                II_Util.clamp255((int) (FRINGE_COLOR.getAlpha() * Math.sqrt(scale)))));
        if (firing) {
            Global.getSoundPlayer().playLoop("ii_lightspear_loop", ship, 1f * overlevel, scale, origin, new Vector2f());
        } else {
            Global.getSoundPlayer().playLoop("ii_lightspear_loop", ship, 1f * overlevel, 0f, origin, new Vector2f());
        }

        if (beam.getBrightness() > 0f) {
            if (endLight == null) {
                endLight = new StandardLight(beam.getTo(), ZERO, beam, true);
                LightShader.addLight(endLight);
            }
            endLight.setIntensity(ENDLIGHT_INTENSITY * beam.getBrightness() * scale);
            endLight.setSize(ENDLIGHT_SIZE * beam.getBrightness() * scale);
            endLight.setColor(GLOW_COLOR.getRed() / 255f, GLOW_COLOR.getGreen() / 255f, GLOW_COLOR.getBlue() / 255f);

            if (beamLight == null) {
                beamLight = new StandardLight(beam.getFrom(), beam.getTo(), ZERO, ZERO, beam);
                LightShader.addLight(beamLight);
            }
            beamLight.setIntensity(BEAMLIGHT_INTENSITY * beam.getBrightness() * scale);
            beamLight.setSize(BEAMLIGHT_SIZE * beam.getBrightness() * scale);
            beamLight.setColor(FRINGE_COLOR.getRed() / 255f, FRINGE_COLOR.getGreen() / 255f, FRINGE_COLOR.getBlue() / 255f);
        } else {
            if (endLight != null) {
                LightShader.removeLight(endLight);
                endLight = null;
            }

            if (beamLight != null) {
                LightShader.removeLight(beamLight);
                beamLight = null;
            }
        }

        if (firing) {
            if (beam.getBrightness() < level) {
                firing = false;
                if (engine.getTotalElapsedTime(false) - sinceLast > 0.3f) {
                    Global.getSoundPlayer().playSound("ii_lightspear_deactivate", 1f, scale, origin, new Vector2f());
                }
            }
        } else {
            if (beam.getBrightness() > level) {
                firing = true;
                if (engine.getTotalElapsedTime(false) - sinceLast > 0.3f) {
                    engine.addHitParticle(origin, ZERO, 300f * scale, 1f, 0.25f, FRINGE_COLOR);
                    engine.addSmoothParticle(origin, ZERO, 1000f * scale, 1f, 0.1f, FLASH_COLOR);
                }
                sinceLast = engine.getTotalElapsedTime(false);
            }
        }
        level = beam.getBrightness();

        interval2.advance(amount);
        if (interval2.intervalElapsed()) {
            engine.addHitParticle(origin, ZERO, scale * ((float) Math.random() * 125f + 50f),
                    amount * 20f * (1f + (float) Math.random()), 0.1f, GLOW_COLOR);

            if (Math.random() < 0.65f * scale) {
                Vector2f point = MathUtils.getPointOnCircumference(origin, (float) Math.sqrt(Math.random()) * scale
                        * 100f, (float) Math.random() * 360f);
                Vector2f dir = new Vector2f(400f, 0f);
                VectorUtils.rotate(dir, beam.getWeapon().getCurrAngle(), dir);
                engine.addSmoothParticle(point, dir, (float) Math.sqrt(scale) * 10f,
                        0.5f * scale, 0.4f, FRINGE_COLOR);
            }
        }

        interval.advance(amount);
        if (interval.intervalElapsed()) {
            for (WeaponAPI photonBeam : photonBeams) {
                if (Math.random() < 0.5 * scale) {
                    Vector2f point = new Vector2f(origin);
                    Vector2f dir = new Vector2f(MathUtils.getRandomNumberInRange(0f, 150f * overlevel), 0f);
                    VectorUtils.rotate(dir, beam.getWeapon().getCurrAngle(), dir);
                    Vector2f.add(point, dir, point);
                    engine.spawnEmpArc(ship, photonBeam.getLocation(), ship, new AnchoredEntity(ship, point),
                            DamageType.ENERGY,
                            0, 0, 0, null, 15f, FRINGE_COLOR, GLOW_COLOR);
                }
            }

            if (beam.getDamageTarget() != null) {
                engine.addHitParticle(beam.getTo(), ZERO, scale * ((float) Math.random() * 125f + 125f), 0.5f, 0.3f, FRINGE_COLOR);
            }
        }
    }
}
