package data.weapons.onFire;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import data.utils.FM_Colors;
import data.utils.FM_Misc;
import org.lazywizard.lazylib.MathUtils;

public class FM_SealOnFire implements OnFireEffectPlugin {
    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        for (int i = 0; i < 20; i = i + 1) {
            engine.addHitParticle(
                    weapon.getFirePoint(0),
                    MathUtils.getRandomPointInCircle(FM_Misc.ZERO, 100f),
                    MathUtils.getRandomNumberInRange(5f, 7f),
                    255f, 0.5f,
                    FM_Colors.FM_TEXT_RED
            );
        }
    }
}
