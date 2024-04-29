package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import data.scripts.util.LE_Util;
import java.awt.Color;

public class LE_LightsEveryFrame implements EveryFrameWeaponEffectPlugin {
    //Code Courtesy of the Titan code from Interstellar Imperium by Dark Revenant
    public static final String LIGHTS_ALPHA_ID = "le_lights_alpha";

    private static final Color COLOR_STANDARD = new Color(255, 0, 0);
    private static final float TRANSITION_TIME = 0.5f;

    private float currAlpha = 0f;

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        boolean animated = weapon.getAnimation() != null;

        if (weapon.getShip() != null) {
            float alpha = 0f;
            if (weapon.getShip().isAlive()) {
                alpha = weapon.getShip().getMutableStats().getDynamic().getValue(LIGHTS_ALPHA_ID, 0f);

                if (alpha > currAlpha) {
                    currAlpha = Math.min(currAlpha + amount / TRANSITION_TIME, alpha);
                } else {
                    currAlpha = Math.max(currAlpha - amount / TRANSITION_TIME, alpha);
                }
            } else {
                currAlpha = alpha;
            }

            Color color;
            switch (weapon.getId()) {
                case "le_dram_lights":
                    color = COLOR_STANDARD;
                    break;
                default:
                    color = new Color(255, 255, 255);
                    break;
            }

            if (animated) {
                if (currAlpha > 0f) {
                    weapon.getAnimation().setFrame(1);
                } else {
                    weapon.getAnimation().setFrame(0);
                }
            }
            Color newColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), LE_Util.clamp255(Math.round(255f * currAlpha)));
            weapon.getSprite().setAdditiveBlend();
            weapon.getSprite().setColor(newColor);
        }
    }
}
