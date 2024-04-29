package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;


public class sv_MagOnHitEffect implements OnHitEffectPlugin {

    private static final Color EXPLOSION_COLOR = new Color(55f / 255f, 255f / 255f, 0f / 55f, 120f / 255f);
    private static final float EXPLOSION_SIZE = 100f;
    private static final float EXPLOSION_DURATION = 2f;

    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        if (!(target instanceof ShipAPI)) {return;}
        float bonusDamage = projectile.getDamageAmount()*1;
        if (projectile.didDamage()) {
            Global.getCombatEngine().applyDamage(target, point, bonusDamage, DamageType.HIGH_EXPLOSIVE, bonusDamage, false, false, null, false);
        }

        Global.getCombatEngine().spawnExplosion(point, new Vector2f(0f, 0f), EXPLOSION_COLOR, EXPLOSION_SIZE, EXPLOSION_DURATION);
    }
}