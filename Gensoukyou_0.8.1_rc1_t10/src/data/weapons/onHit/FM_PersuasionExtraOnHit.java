package data.weapons.onHit;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.util.Misc;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class FM_PersuasionExtraOnHit implements OnHitEffectPlugin {

    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        if (projectile instanceof MissileAPI) {

            engine.addHitParticle(
                    point,
                    new Vector2f(),
                    40f,
                    1f,
                    1f,
                    Misc.interpolateColor(Color.WHITE, ((MissileAPI) projectile).getSpriteAPI().getColor(), 0.5f)
            );
        }
    }
}
