package data.weapons.onFire;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import data.utils.FM_Colors;
import data.utils.FM_Misc;

public class FM_FireflyFlareOnFIre implements OnFireEffectPlugin {
    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        engine.addHitParticle(
                projectile.getLocation(),
                FM_Misc.ZERO,
                25f,
                255f,
                0.2f,
                FM_Colors.FM_GREEN_EMP_FRINGE
        );
    }
}
