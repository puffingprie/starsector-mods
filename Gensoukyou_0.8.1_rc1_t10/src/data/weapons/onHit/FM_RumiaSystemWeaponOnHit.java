package data.weapons.onHit;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.util.Misc;
import data.utils.FM_Colors;
import data.utils.FM_Misc;
import data.utils.visual.FM_CrossExplosion;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class FM_RumiaSystemWeaponOnHit implements OnHitEffectPlugin {
    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        FM_CrossExplosion.FM_CEParams param = new FM_CrossExplosion.FM_CEParams();
        float effectMult = projectile.getDamage().getModifier().getMult();
        param.color = FM_Colors.FM_RED_EMP_FRINGE;
        param.coreColor = Color.BLACK;
        param.fadeIn = 0.1f;
        param.fadeOut = 0.2f;
        param.fadeIdle = 0f;
        param.additive = false;
        param.radius = 46f + 4f * effectMult;
        param.loc = point;
        param.vel = new Vector2f();
        param.thickness = param.radius;
        param.facing = MathUtils.getRandomNumberInRange(0, 360);
        engine.addLayeredRenderingPlugin(new FM_CrossExplosion(param));
        engine.addHitParticle(point, FM_Misc.ZERO, 275f + 25f * effectMult, 1f, 0.2f, Misc.scaleAlpha(Color.RED, 0.2f));
//        engine.spawnExplosion(
//                point,
//                FM_Misc.ZERO,
//                FM_Colors.FM_DARK_CORE_RED,
//                200f,
//                0.8f
//        );
        for (int i = 0; i < 15; i = i + 1) {
            engine.addNegativeNebulaParticle(
                    point,
                    MathUtils.getRandomPointInCircle(FM_Misc.ZERO, MathUtils.getRandomNumberInRange(75f, 125f)),
                    MathUtils.getRandomNumberInRange(27f + 3f * effectMult, 36f + 4f * effectMult),
                    1f,
                    0.1f,
                    0.7f,
                    MathUtils.getRandomNumberInRange(0.9f, 1.1f),
                    FM_Colors.FM_BLUE_FLARE_CORE
            );
        }

        if (target instanceof ShipAPI && target.getOwner() != projectile.getOwner() && projectile.getSource() != null) {
            ShipAPI source = projectile.getSource();
            if (source.getHitpoints() < source.getMaxHitpoints()) {
                float hpHeal = Math.min(damageResult.getDamageToShields(),
                        source.getMaxHitpoints() - source.getHitpoints());
                source.setHitpoints(source.getHitpoints() + hpHeal);

            }
        }
    }
}
