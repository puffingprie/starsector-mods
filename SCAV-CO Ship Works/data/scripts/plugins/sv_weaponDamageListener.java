package data.scripts.plugins;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.DamageDealtModifier;
import org.lwjgl.util.vector.Vector2f;

public class sv_weaponDamageListener implements DamageDealtModifier {
			
    @Override
    public String modifyDamageDealt(Object param, CombatEntityAPI target, DamageAPI damage, Vector2f point, boolean shieldHit) {

        if (param instanceof DamagingProjectileAPI) {
            if (((DamagingProjectileAPI) param).getProjectileSpecId() != null && ((DamagingProjectileAPI) param).getProjectileSpecId().equals("sv_flyswatter_shot") && shieldHit) {
                            damage.setSoftFlux(true);
            }
        }
        return null;
    }
}