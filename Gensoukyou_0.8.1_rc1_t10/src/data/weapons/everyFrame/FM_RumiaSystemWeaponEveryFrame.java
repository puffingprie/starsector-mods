package data.weapons.everyFrame;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import data.utils.FM_Colors;
import org.lazywizard.lazylib.MathUtils;

public class FM_RumiaSystemWeaponEveryFrame implements EveryFrameWeaponEffectPlugin {

    private float TIMER = 0f;

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (weapon.getShip() == null || !weapon.getShip().isAlive()) return;
        weapon.setRemainingCooldownTo(10f);
        weapon.disable(true);
        if (weapon.getChargeLevel() <= 1f && weapon.isFiring() && weapon.getCooldownRemaining() <= 0) {
            TIMER = TIMER + engine.getElapsedInLastFrame();
            if (TIMER >= 0.1f) {
                engine.spawnEmpArcVisual(
                        weapon.getLocation(),
                        weapon.getShip(),
                        MathUtils.getRandomPointInCircle(weapon.getLocation(), weapon.getShip().getCollisionRadius()),
                        weapon.getShip(),
                        3f,
                        FM_Colors.FM_RED_EMP_FRINGE,
                        FM_Colors.FM_RED_EMP_CORE
                );
                TIMER = 0f;
            }
        } else {
            TIMER = 0f;
        }
    }
}
