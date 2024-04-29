package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import data.scripts.util.II_Util;
import java.awt.Color;

public class II_ClypeusLightsEveryFrame implements EveryFrameWeaponEffectPlugin {

    public static final String LIGHTS_ALPHA_ID = "ii_lights_alpha";

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (weapon.getShip() != null) {
            float alpha = 0f;
            if (weapon.getShip().isAlive()) {
                if (weapon.getShip().getFluxTracker().isOverloaded()) {
                    alpha = Math.min(weapon.getShip().getFluxTracker().getOverloadTimeRemaining(), 1f);
                } else {
                    alpha = weapon.getShip().getFluxTracker().getFluxLevel();
                }
            }

            Color newColor = new Color(255, 255, 255, II_Util.clamp255(Math.round(255f * alpha)));
            weapon.getSprite().setColor(newColor);
        }
    }
}
