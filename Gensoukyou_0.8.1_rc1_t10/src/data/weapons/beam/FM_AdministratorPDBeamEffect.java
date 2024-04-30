package data.weapons.beam;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.combat.NegativeExplosionVisual;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;
import com.fs.starfarer.api.util.Misc;
import data.utils.FM_Colors;
import data.utils.FM_ProjectEffect;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicFakeBeam;

import java.awt.*;

public class FM_AdministratorPDBeamEffect implements BeamEffectPlugin {

    private boolean EXPLOSION = true;

    @Override
    public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
        Vector2f hit_loc = beam.getTo();
        CombatEntityAPI target = beam.getDamageTarget();
        if (beam.getWeapon() == null) return;
        WeaponAPI weapon = beam.getWeapon();
        if (weapon.getShip() == null || !weapon.getShip().isAlive()) return;
        if (EXPLOSION && (beam.didDamageThisFrame() || beam.getLength() >= weapon.getRange() * 0.95f)) {
            //engine.spawnExplosion(hit_loc,new Vector2f(),EXP_COLOR,30f,0.3f);
            DamagingProjectileAPI explosion = engine.spawnDamagingExplosion(createExplosionSpec(weapon), beam.getSource(), hit_loc);
            explosion.addDamagedAlready(target);

            for (int i = 0; i < 17; i = i + 1) {
                Vector2f beamFrom = MathUtils.getRandomPointOnCircumference(hit_loc, 30f);
                float dir = VectorUtils.getAngle(hit_loc, beamFrom);
                MagicFakeBeam.spawnAdvancedFakeBeam(
                        engine,
                        beamFrom,
                        MathUtils.getRandomNumberInRange(180 * 0.8f, 180 * 1.1f),
                        dir,
                        15f,
                        10f,
                        -0.5f,
                        "base_trail_smooth",
                        "base_trail_smooth",
                        128f,
                        20f,
                        10,
                        50f,
                        0.2f,
                        0.2f,
                        40f,
                        Color.WHITE,
                        FM_ProjectEffect.EFFECT_5,
                        25f,
                        DamageType.ENERGY,
                        0f,
                        weapon.getShip()
                );

            }
            NegativeExplosionVisual.NEParams neEffect = new NegativeExplosionVisual.NEParams();
            neEffect.fadeOut = 0.3f;
            neEffect.radius = 6f;
            neEffect.thickness = 2f;
            neEffect.color = Color.WHITE;
            neEffect.underglow = Misc.setAlpha(FM_Colors.FM_YELLOW_BEAM_FRINGE, 50);

            CombatEntityAPI visual = engine.addLayeredRenderingPlugin(new NegativeExplosionVisual(neEffect));
            visual.getLocation().set(hit_loc);
            Global.getSoundPlayer().playSound("FM_Opposition_expand", 2f, 0.5f, hit_loc, new Vector2f());
            EXPLOSION = false;
        }
    }

    public DamagingExplosionSpec createExplosionSpec(WeaponAPI weapon) {
        float damage = 150f;
        DamagingExplosionSpec spec = new DamagingExplosionSpec(
                0.1f, // duration
                90f, // radius
                75f, // coreRadius
                damage, // maxDamage
                damage / 2f, // minDamage
                CollisionClass.PROJECTILE_FF, // collisionClass
                CollisionClass.PROJECTILE_FIGHTER, // collisionClassByFighter
                3f, // particleSizeMin
                3f, // particleSizeRange
                0.5f, // particleDuration
                30, // particleCount
                Color.WHITE, // particleColor
                FM_Colors.FM_ORANGE_FLARE_CORE  // explosionColor
        );

        spec.setDamageType(DamageType.ENERGY);
        spec.setUseDetailedExplosion(false);
        spec.setSoundSetId("hit_glancing_energy");
        return spec;
    }
}
