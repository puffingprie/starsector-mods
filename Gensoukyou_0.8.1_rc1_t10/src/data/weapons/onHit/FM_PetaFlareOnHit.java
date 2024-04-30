package data.weapons.onHit;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import data.utils.FM_Colors;
import org.lwjgl.util.vector.Vector2f;

public class FM_PetaFlareOnHit implements OnHitEffectPlugin {

    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {

        engine.spawnExplosion(
                point,
                (Vector2f) projectile.getVelocity().scale(0.1f),
                FM_Colors.FM_ORANGE_FLARE_CORE,
                192f,
                0.8f
        );

        Global.getSoundPlayer().playSound("hit_heavy_energy", 1f, 0.45f, point, new Vector2f());
    }
}
