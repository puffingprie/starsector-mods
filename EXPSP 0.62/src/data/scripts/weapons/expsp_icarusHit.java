package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.impl.combat.DisintegratorEffect;
import com.fs.starfarer.api.util.Misc;
import org.lwjgl.util.vector.Vector2f;

import static com.fs.starfarer.api.combat.DamageType.*;

public class expsp_icarusHit implements OnHitEffectPlugin {

    private static final float EXTRA_DAMAGE = 80;
    public static float ARMOUR_DAMAGE = 20;

    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target,
                      Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        if (target instanceof ShipAPI && ((ShipAPI) target).isFighter()) {
            engine.applyDamage(target, point, EXTRA_DAMAGE, FRAGMENTATION, 20f, false, false, projectile.getSource());
        }
        if(target instanceof  MissileAPI){
            engine.applyDamage(target, point, EXTRA_DAMAGE, FRAGMENTATION, 20f, false, false, projectile.getSource());
        }
        if (!shieldHit && target instanceof ShipAPI&& ((ShipAPI) target).isFighter()) {
            dealArmorDamage(projectile, (ShipAPI) target, point);
        }
    }
        public static void dealArmorDamage(DamagingProjectileAPI projectile, ShipAPI target, Vector2f point) {
            CombatEngineAPI engine = Global.getCombatEngine();

            ArmorGridAPI grid = target.getArmorGrid();
            int[] cell = grid.getCellAtLocation(point);
            if (cell == null) return;

            int gridWidth = grid.getGrid().length;
            int gridHeight = grid.getGrid()[0].length;

            float damageTypeMult = DisintegratorEffect.getDamageTypeMult(projectile.getSource(), target);

            float damageDealt = 0f;
            for (int i = -2; i <= 2; i++) {
                for (int j = -2; j <= 2; j++) {
                    if ((i == 2 || i == -2) && (j == 2 || j == -2)) continue; // skip corners

                    int cx = cell[0] + i;
                    int cy = cell[1] + j;

                    if (cx < 0 || cx >= gridWidth || cy < 0 || cy >= gridHeight) continue;

                    float damMult = 1/30f;
                    if (i == 0 && j == 0) {
                        damMult = 1/15f;
                    } else if (i <= 1 && i >= -1 && j <= 1 && j >= -1) { // S hits
                        damMult = 1/15f;
                    } else { // T hits
                        damMult = 1/30f;
                    }

                    float armorInCell = grid.getArmorValue(cx, cy);
                    float damage = ARMOUR_DAMAGE * damMult * damageTypeMult;
                    damage = Math.min(damage, armorInCell);
                    if (damage <= 0) continue;

                    target.getArmorGrid().setArmorValue(cx, cy, Math.max(0, armorInCell - damage));
                    damageDealt += damage;
                }
            }

            if (damageDealt > 0) {
                if (Misc.shouldShowDamageFloaty(projectile.getSource(), target)) {
                    engine.addFloatingDamageText(point, damageDealt, Misc.FLOATY_ARMOR_DAMAGE_COLOR, target, projectile.getSource());
                }
                target.syncWithArmorGridState();
            }
        }
}
