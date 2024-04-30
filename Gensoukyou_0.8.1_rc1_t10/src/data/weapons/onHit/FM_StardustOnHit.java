package data.weapons.onHit;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import data.utils.FM_Colors;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicLensFlare;

public class FM_StardustOnHit implements OnHitEffectPlugin {
    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        if (shieldHit) return;
        if (projectile.getSource() == null) return;
//        for (int i = 0; i < 24; i = i + 1){
//            engine.addHitParticle(
//                    point,
//                    MathUtils.getRandomPointInCircle(FM_Misc.ZERO,225f),
//                    MathUtils.getRandomNumberInRange(4f,10f),
//                    1f,
//                    0.57f,
//                    FM_Colors.FM_BLUE_FLARE_CORE
//            );
//        }
        MagicLensFlare.createSharpFlare(
                engine,
                projectile.getSource(),
                point,
                7f, 200f, 0f, FM_Colors.FM_BLUE_FLARE_FRINGE, FM_Colors.FM_BLUE_FLARE_CORE
        );
        //engine.spawnExplosion(point,FM_Misc.ZERO,FM_Colors.FM_GREEN_EMP_CORE,110f,0.35f);
        //engine.addHitParticle(point, FM_Misc.ZERO,130f,255f,0.25f, FM_Colors.FM_GREEN_EMP_CORE);

    }
}
