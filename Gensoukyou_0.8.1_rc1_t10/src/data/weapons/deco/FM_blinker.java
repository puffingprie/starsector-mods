package data.weapons.deco;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;

public class FM_blinker implements EveryFrameWeaponEffectPlugin {

    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {

        if (weapon.getShip() != null && weapon.getShip().isAlive()) {
            weapon.getAnimation().setAlphaMult(1f);
            weapon.getAnimation().play();
        } else {
            weapon.getAnimation().pause();
            weapon.getAnimation().setAlphaMult(0f);
        }
    }
}
