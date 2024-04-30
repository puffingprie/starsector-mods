package data.weapons.everyFrame;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.Misc;
import data.utils.FM_Colors;
import org.lazywizard.lazylib.MathUtils;

public class FM_LeafFujinEveryFrame implements EveryFrameWeaponEffectPlugin {

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (weapon.getChargeLevel() >= 1f && weapon.getShip().isAlive()) {
            engine.spawnEmpArcVisual(
                    MathUtils.getRandomPointInCircle(weapon.getLocation(), MathUtils.getRandomNumberInRange(45, 65)),
                    weapon.getShip(),
                    weapon.getLocation(),
                    weapon.getShip(),
                    2f,
                    Misc.setAlpha(FM_Colors.FM_GREEN_EMP_FRINGE, 35),
                    Misc.setAlpha(FM_Colors.FM_GREEN_EMP_CORE, 75)

            );
        }
    }
}
