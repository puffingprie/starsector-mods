package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import data.scripts.hullmods.II_BasePackage;
import data.scripts.shipsystems.II_ArbalestLoaderStats;
import data.scripts.util.II_Util;
import java.awt.Color;

public class II_MagnaFulmenBarEveryFrame implements EveryFrameWeaponEffectPlugin {

    public double t = 0.0;
    public boolean first = true;

    private static final Color COLOR_ERROR = new Color(255, 0, 0);

    private static final int NUM_FRAMES = 11;

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (weapon.getShip() != null) {
            Color color = II_ArbalestLoaderStats.GLOW_COLOR_STANDARD;
            if (weapon.getShip().getVariant().hasHullMod(II_BasePackage.ARMOR_PACKAGE)) {
                color = II_ArbalestLoaderStats.GLOW_COLOR_ARMOR;
            } else if (weapon.getShip().getVariant().hasHullMod(II_BasePackage.TARGETING_PACKAGE)) {
                color = II_ArbalestLoaderStats.GLOW_COLOR_TARGETING;
            } else if (weapon.getShip().getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
                color = II_ArbalestLoaderStats.GLOW_COLOR_ELITE;
            }

            WeaponAPI magnaFulmen = null;
            for (WeaponAPI shipWeapon : weapon.getShip().getAllWeapons()) {
                if (shipWeapon.getId().contentEquals("ii_magna_fulmen") && !shipWeapon.isPermanentlyDisabled()) {
                    magnaFulmen = shipWeapon;
                    break;
                }
            }

            float alpha = 1f;
            float progress = 0f;
            double pulsePeriod = 1.5;

            if (magnaFulmen == null) {
                alpha = 0f;
            } else if (weapon.getShip().getFluxTracker().isOverloaded()) {
                alpha = (float) Math.random();
                progress = (float) Math.random();
                color = COLOR_ERROR;
            } else if (magnaFulmen.isDisabled()) {
                alpha = 0.5f;
                progress = 1f;
                color = COLOR_ERROR;
            } else {
                progress = 1f - (magnaFulmen.getCooldownRemaining() / magnaFulmen.getCooldown());

                int armed = II_ArbalestLoaderStats.getArmed(weapon.getShip());
                double x = t * 2 * Math.PI;
                float y = (float) (0.1 + (0.1 * (1 + armed) * Math.sin(x % (2 * Math.PI))));

                if (armed > 0) {
                    pulsePeriod /= 1.25 * armed;
                }
                if (progress >= 1f) {
                    y *= 1.5f;
                    pulsePeriod /= 1.5;
                }

                y += 1f;

                color = new Color(II_Util.clamp255(Math.round(color.getRed() * y)), II_Util.clamp255(Math.round(color.getGreen() * y)), II_Util.clamp255(Math.round(color.getBlue() * y)));
            }

            if (!engine.isPaused()) {
                if (first) {
                    first = false;
                } else {
                    t += amount / pulsePeriod;
                }
            }

            if (weapon.getShip().isAlive()) {
                if (alpha <= 0f) {
                    weapon.getAnimation().setFrame(0);
                } else {
                    weapon.getAnimation().setFrame(Math.max(0, Math.min(NUM_FRAMES - 1, (int) Math.floor(progress * NUM_FRAMES))));
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
