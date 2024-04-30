package data.weapons.onFire;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import data.utils.FM_Colors;
import data.utils.FM_Misc;
import org.dark.shaders.distortion.DistortionShader;
import org.dark.shaders.distortion.WaveDistortion;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class FM_RumiaSystemWeaponOnFire implements OnFireEffectPlugin {

    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        Vector2f loc = projectile.getLocation();
        for (int i = 0; i < 10; i = i + 1) {
//            engine.addNegativeParticle(
//                    projectile.getLocation(),
//                    MathUtils.getRandomPointInCircle(FM_Misc.ZERO,70f),
//                    MathUtils.getRandomNumberInRange(6f,9f),
//                    0.5f,
//                    MathUtils.getRandomNumberInRange(1f,1.5f),
//                    FM_Colors.FM_RED_EMP_FRINGE
//            );
            engine.addNegativeNebulaParticle(
                    weapon.getFirePoint(0),
                    MathUtils.getPointOnCircumference(new Vector2f(), 90f, MathUtils.getRandomNumberInRange(0, 360)),
                    MathUtils.getRandomNumberInRange(50f, 60f),
                    0.3f,
                    -0.25f,
                    0.5f,
                    2f,
                    FM_Colors.FM_GREEN_EMP_CORE
            );
        }
        engine.addHitParticle(loc, FM_Misc.ZERO, weapon.getShip().getCollisionRadius() * 3f, 1f, 0.5f, FM_Colors.FM_RED_EMP_FRINGE);
        //engine.addHitParticle(point, vel, size * 3.0f, 1f, dur, p.color);
        engine.addHitParticle(loc, FM_Misc.ZERO, weapon.getShip().getCollisionRadius() * 1.5f, 1f, 0.5f, Color.white);
        //engine.addHitParticle(point, vel, coreSize * 1f, 1f, dur, Color.white);

        WaveDistortion wave = new WaveDistortion(loc, FM_Misc.ZERO);
        wave.fadeInSize(0.2f);
        wave.setIntensity(50f);
        wave.setArc(0, 360);
        wave.fadeOutIntensity(0.5f);
        wave.setLifetime(0.7f);
        DistortionShader.addDistortion(wave);
    }
}
