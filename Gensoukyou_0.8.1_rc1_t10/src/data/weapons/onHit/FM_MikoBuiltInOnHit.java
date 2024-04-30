package data.weapons.onHit;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import data.utils.FM_Colors;
import data.utils.FM_Misc;
import data.utils.visual.FM_DiamondParticle3DTest;
import data.utils.visual.FM_ParticleManager;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class FM_MikoBuiltInOnHit implements OnHitEffectPlugin {

    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        engine.addHitParticle(point, FM_Misc.ZERO, 120f, 255f, 0.2f, FM_Colors.FM_RED_EMP_FRINGE);
        FM_DiamondParticle3DTest manager = FM_ParticleManager.getDiamondParticleManager(engine);
        for (int i = 0; i < 12; i = i + 1) {
            manager.addDiamondParticle(
                    point,
                    MathUtils.getRandomPointOnCircumference(FM_Misc.ZERO, MathUtils.getRandomNumberInRange(0f,275f)),
                    MathUtils.getRandomNumberInRange(10f, 14f),
                    0.03f,
                    0.47f,
                    FM_Colors.FM_TEXT_RED,
                    7f,
                    MathUtils.getRandomNumberInRange(0, 360f),
                    MathUtils.getRandomNumberInRange(180f, 540f), MathUtils.getRandomNumberInRange(180f, 540f), Math.random() < 0.5f
            );
        }

        if (projectile.getSource() == null) return;
        ShipAPI ship = projectile.getSource();
        if (ship.getVariant().hasHullMod("FantasySpellMod")) {
            if (target instanceof ShipAPI && target.getOwner() != ship.getOwner()) {
                FM_Misc.getSpellModState(engine, ship).spellPower = FM_Misc.getSpellModState(engine, ship).spellPower + 0.02f;
            }
        }
    }
}
