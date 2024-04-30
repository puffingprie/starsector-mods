package data.weapons.onFire;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import data.utils.FM_Colors;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicLensFlare;

public class FM_PetaFlareOnFire implements OnFireEffectPlugin {


    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        MagicLensFlare.createSharpFlare(
                engine,
                weapon.getShip(),
                weapon.getFirePoint(0),
                7f,
                500,
                0,
                FM_Colors.FM_ORANGE_FLARE_FRINGE,
                FM_Colors.FM_ORANGE_FLARE_CORE

        );

        for (int i = 0; i < 10; i = i + 1) {
            engine.addNebulaParticle(
                    weapon.getFirePoint(0),
                    MathUtils.getPointOnCircumference(new Vector2f(), 40f, MathUtils.getRandomNumberInRange(0, 360)),
                    MathUtils.getRandomNumberInRange(50f, 60f),
                    0.3f,
                    -0.25f,
                    0.5f,
                    2f,
                    FM_Colors.FM_ORANGE_FLARE_FRINGE,
                    false
            );
        }


    }
}
