package data.weapons.onHit;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicRender;

import java.awt.*;

public class FM_SealOnHitEffect implements OnHitEffectPlugin {


    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {


        if (!shieldHit) {
            engine.spawnExplosion(point, new Vector2f(), Color.BLUE, 64, 1.5f);
        } else {
            MagicRender.battlespace(Global.getSettings().getSprite("fx", "FM_modeffect_3"), point, new Vector2f(),
                    new Vector2f(100, 100), new Vector2f(100, 100), MathUtils.getRandomNumberInRange(0, 360), 0f,
                    new Color(212, 32, 32, 255), true, 1f, 0f, 0f, 0f, 0.1f, 0.05f, 0.05f, 0.5f,
                    CombatEngineLayers.BELOW_SHIPS_LAYER);
        }

//        FM_StarParticle.FM_SPParams SP = new FM_StarParticle.FM_SPParams();
//        SP.fadeOut = 0.6f;
//        SP.radius = 30;
//        SP.thickness = 2f;
//        SP.color = Color.WHITE;
//
//
//        CombatEntityAPI visual = engine.addLayeredRenderingPlugin(new FM_StarParticle(SP));
//        visual.getLocation().set(point);
    }
}
