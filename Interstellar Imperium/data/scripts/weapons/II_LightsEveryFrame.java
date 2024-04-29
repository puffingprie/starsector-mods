package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import data.scripts.util.II_Util;
import java.awt.Color;

public class II_LightsEveryFrame implements EveryFrameWeaponEffectPlugin {

    public static final String LIGHTS_ALPHA_ID = "ii_lights_alpha";

    private static final Color COLOR_STANDARD = new Color(255, 0, 0);
    private static final Color COLOR_ARMOR = new Color(255, 255, 0);
    private static final Color COLOR_TARGETING = new Color(0, 145, 255);
    private static final Color COLOR_ELITE = new Color(255, 0, 255);
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
                case "ii_titan_lights":
                    color = COLOR_STANDARD;
                    break;
                case "ii_titan_armor_lights":
                    color = COLOR_ARMOR;
                    break;
                case "ii_titan_targeting_lights":
                case "ii_titan_targeting_door_lights":
                    color = COLOR_TARGETING;
                    break;
                case "ii_titan_elite_lights":
                case "ii_boss_titanx_glow":
                    color = COLOR_ELITE;
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
            Color newColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), II_Util.clamp255(Math.round(255f * currAlpha)));
            weapon.getSprite().setAdditiveBlend();
            weapon.getSprite().setColor(newColor);
        }
    }
}
