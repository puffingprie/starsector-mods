package data.weapons.onHit;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import data.utils.FM_Colors;
import data.utils.FM_Misc;
import data.utils.FM_ProjectEffect;
import data.utils.visual.FM_DiamondParticle3DTest;
import data.utils.visual.FM_ParticleManager;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class FM_SilverOnHitEffect implements OnHitEffectPlugin {

    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
//        for (int i = 0; i < 12; i++) {
//            engine.addNebulaParticle(
//                    point,
//                    MathUtils.getRandomPointInCircle(new Vector2f(), 50f),
//                    26,
//                    0.8f,
//                    -0.5f,
//                    0.8f,
//                    1f,
//                    FM_ProjectEffect.EFFECT_5,
//                    true
//            );
//        }
        engine.spawnExplosion(point, new Vector2f(), FM_ProjectEffect.EFFECT_1, 50f, 0.2f);
        if (shieldHit){
            engine.addNebulaParticle(point,FM_Misc.ZERO,60f,3f,0.1f,0.1f,0.5f,FM_ProjectEffect.EFFECT_5);
        }
        Vector2f particlesVel = (Vector2f) new Vector2f(projectile.getVelocity()).scale(0.1f);
        FM_DiamondParticle3DTest manager = FM_ParticleManager.getDiamondParticleManager(engine);
        for (int i = 0; i < 20; i = i + 1) {
            manager.addDiamondParticle(
                    point,
                    Vector2f.add(particlesVel, MathUtils.getRandomPointInCircle(FM_Misc.ZERO, 225f), new Vector2f()),
                    MathUtils.getRandomNumberInRange(10f, 14f),
                    0.03f,
                    0.57f,
                    FM_Colors.FM_BLUE_FLARE_CORE,
                    7f,
                    MathUtils.getRandomNumberInRange(0, 360f),
                    MathUtils.getRandomNumberInRange(180f, 540f), MathUtils.getRandomNumberInRange(180f, 540f), Math.random() < 0.5f
            );
        }
    }
}
