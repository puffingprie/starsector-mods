package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import data.scripts.hullmods.II_BasePackage;
import data.scripts.util.II_Util;
import java.awt.Color;

public class II_BlinkerEveryFrame implements EveryFrameWeaponEffectPlugin {

    public static final Color COLOR_TARGETING = new Color(255, 255, 255);
    public static final Color COLOR_OVERLOAD = new Color(255, 0, 0);

    public double t = 0.0;
    public boolean first = true;

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (weapon.getShip() != null) {
            double blinkPeriod;
            double blinkOffset;
            double blinkThreshold;
            boolean additive;
            int frame;

            Color color;
            switch (weapon.getId()) {
//                case "ii_targeting_blinker1":
//                    color = COLOR_TARGETING;
//                    blinkPeriod = 1.6;
//                    blinkOffset = -0.2;
//                    blinkThreshold = Math.sin((-0.2 / blinkPeriod) * 2 * Math.PI);
//                    break;
//                case "ii_targeting_blinker2":
//                    color = COLOR_TARGETING;
//                    blinkPeriod = 1.6;
//                    blinkOffset = -0.4;
//                    blinkThreshold = 0.0;
//                    break;
//                case "ii_targeting_blinker3":
//                    color = COLOR_TARGETING;
//                    blinkPeriod = 1.6;
//                    blinkOffset = -0.6;
//                    blinkThreshold = Math.sin((0.2 / blinkPeriod) * 2 * Math.PI);
//                    break;
//                case "ii_targeting_blinker1":
//                    color = COLOR_TARGETING;
//                    blinkPeriod = 1.0;
//                    blinkOffset = 0.0;
//                    blinkThreshold = 0.0;
//                    break;
//                case "ii_targeting_blinker2":
//                    color = COLOR_TARGETING;
//                    blinkPeriod = 1.0;
//                    blinkOffset = -0.1;
//                    blinkThreshold = 0.0;
//                    break;
//                case "ii_targeting_blinker3":
//                    color = COLOR_TARGETING;
//                    blinkPeriod = 1.0;
//                    blinkOffset = -0.2;
//                    blinkThreshold = 0.0;
//                    break;
                case "ii_targeting_blinker1":
                    color = COLOR_TARGETING;
                    blinkPeriod = 1.0;
                    blinkOffset = -0.15;
                    blinkThreshold = Math.sin((-0.15 / blinkPeriod) * 2 * Math.PI);
                    frame = 2;
                    additive = false;
                    break;
                case "ii_targeting_blinker2":
                    color = COLOR_TARGETING;
                    blinkPeriod = 1.0;
                    blinkOffset = -0.1;
                    blinkThreshold = Math.sin((-0.1 / blinkPeriod) * 2 * Math.PI);
                    frame = 2;
                    additive = false;
                    break;
                case "ii_targeting_blinker3":
                    color = COLOR_TARGETING;
                    blinkPeriod = 1.0;
                    blinkOffset = -0.05;
                    blinkThreshold = Math.sin((-0.05 / blinkPeriod) * 2 * Math.PI);
                    frame = 2;
                    additive = false;
                    break;
                case "ii_targeting_blinker4":
                    color = COLOR_TARGETING;
                    blinkPeriod = 1.0;
                    blinkOffset = 0.0;
                    blinkThreshold = 0.0;
                    frame = 2;
                    additive = false;
                    break;
                default:
                    color = COLOR_TARGETING;
                    blinkPeriod = 1.0;
                    blinkOffset = 0.0;
                    blinkThreshold = 0.0;
                    frame = 1;
                    additive = true;
                    break;
            }

            float alpha = 1f;

            if (!engine.isPaused()) {
                if (first) {
                    first = false;
                } else {
                    t += amount / blinkPeriod;
                }
            }

            double x = (t + (blinkOffset / blinkPeriod)) * 2 * Math.PI;
            float y = (float) Math.sin(x % (2 * Math.PI));

            if (y < blinkThreshold) {
                alpha = 0f;
            }

            if (weapon.getShip().getFluxTracker().isOverloaded()) {
                alpha = (float) Math.random();
                color = COLOR_OVERLOAD;
                frame = 1;
                additive = true;
            } else if (weapon.getShip().getFluxTracker().isVenting()) {
                alpha = 1f;
            }

            if (weapon.getShip().getVariant().hasHullMod(II_BasePackage.TARGETING_PACKAGE) && weapon.getShip().isAlive()) {
                if (alpha <= 0f) {
                    weapon.getAnimation().setFrame(0);
                } else {
                    weapon.getAnimation().setFrame(frame);
                }
                Color newColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), II_Util.clamp255(Math.round(255f * alpha)));
                if (additive) {
                    weapon.getSprite().setAdditiveBlend();
                } else {
                    weapon.getSprite().setNormalBlend();
                }
                weapon.getSprite().setColor(newColor);
            } else {
                weapon.getAnimation().setFrame(0);
            }
            weapon.setCurrAngle(0f);
        }
    }
}
