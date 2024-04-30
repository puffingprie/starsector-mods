package data.weapons.onHit;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import data.weapons.everyFrame.FM_PersuasionEveryFrame;
import org.lwjgl.util.vector.Vector2f;

public class FM_PersuasionOnHit implements OnHitEffectPlugin {

    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        if (projectile.getWeapon() == null) return;
        if (projectile.getWeapon().getEffectPlugin() instanceof FM_PersuasionEveryFrame) {
            ((FM_PersuasionEveryFrame) projectile.getWeapon().getEffectPlugin()).HIT_NUM = ((FM_PersuasionEveryFrame) projectile.getWeapon().getEffectPlugin()).HIT_NUM + 1;
        }
    }
}
