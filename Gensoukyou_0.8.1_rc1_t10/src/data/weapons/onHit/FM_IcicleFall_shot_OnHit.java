package data.weapons.onHit;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import data.utils.FM_Colors;
import data.utils.FM_ProjectEffect;
import org.lwjgl.util.vector.Vector2f;

public class FM_IcicleFall_shot_OnHit implements OnHitEffectPlugin {


    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        engine.addNebulaParticle(
                point,
                (Vector2f) projectile.getVelocity().scale(0.1f),
                50f,
                2f,
                0.25f,
                0.1f,
                0.8f,
                FM_Colors.FM_BLUE_FLARE_CORE
        );
        engine.spawnExplosion(point, new Vector2f(), FM_ProjectEffect.CYAN_X, 50f, 0.4f);
    }
}
