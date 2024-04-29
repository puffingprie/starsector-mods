package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import data.scripts.hullmods.II_BasePackage;
import data.scripts.util.II_Util;
import java.awt.Color;

public class II_EliteLightsEveryFrame implements EveryFrameWeaponEffectPlugin {

    public static final Color COLOR_ELITE = new Color(205, 65, 255);
    public static final Color COLOR_OVERLOAD = new Color(255, 0, 0);

    public double t = 0.0;
    public boolean first = true;

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (weapon.getShip() != null) {
            double pulsePeriod;
            double pulseOffset;
            double pulseAmplitude;
            double pulseBias;
            Color color;
//            switch (weapon.getId()) {
//                case "ii_elite_lights_praetorian":
//                case "ii_elite_lights_interrex":
            color = COLOR_ELITE;
            pulsePeriod = (double) II_Util.lerp(4f, 0.5f, weapon.getShip().getFluxLevel());
            pulseOffset = 0.0;
            pulseAmplitude = (double) II_Util.lerp(0.4f, 0.0f, weapon.getShip().getFluxLevel());
            pulseBias = (double) II_Util.lerp(0.6f, 1.0f, weapon.getShip().getFluxLevel());
//                    break;
//                default:
//                    color = new Color(255, 255, 255);
//                    pulsePeriod = 1.0;
//                    pulseOffset = 0.0;
//                    pulseAmplitude = 1.0;
//                    pulseBias = 0.0;
//                    break;
//            }

            float alpha = 1f;

            if (!engine.isPaused()) {
                if (first) {
                    first = false;
                } else {
                    t += amount / pulsePeriod;
                }
            }

            double x = (t + (pulseOffset / pulsePeriod)) * 2 * Math.PI;
            float y = (float) (pulseBias + (pulseAmplitude * Math.sin(x % (2 * Math.PI))));

            alpha *= y;
            alpha *= II_Util.lerp(1f, (float) Math.random(), weapon.getShip().getHardFluxLevel());

            if (weapon.getShip().getFluxTracker().isOverloaded()) {
                alpha = (float) Math.random();
                color = COLOR_OVERLOAD;
            } else if (weapon.getShip().getFluxTracker().isVenting()) {
                alpha = 1f;
            }

            if (weapon.getShip().getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE) && weapon.getShip().isAlive()) {
                if (alpha <= 0f) {
                    weapon.getAnimation().setFrame(0);
                } else {
                    weapon.getAnimation().setFrame(1);
                }
                Color newColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), II_Util.clamp255(Math.round(255f * alpha)));
                weapon.getSprite().setAdditiveBlend();
                weapon.getSprite().setColor(newColor);
            } else {
                weapon.getAnimation().setFrame(0);
            }
        }
    }
}
