package data.weapons.onHit;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import data.utils.FM_Colors;
import data.utils.FM_Misc;
import data.utils.I18nUtil;
import org.dark.shaders.distortion.DistortionShader;
import org.dark.shaders.distortion.WaveDistortion;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicLensFlare;

import java.util.List;

public class FM_CaptureWebOnHit implements OnHitEffectPlugin {

    public static final float BUFF_FLAT = 0.25f;
    public static final float TIMER_AMOUNT = 3f;
    public static final float BUFF_CHANCE = 0.25f;
    public static final String buffId = "FM_CaptureWebBuff";

    private BaseEveryFrameCombatPlugin plugin = null;

    public static Object INFO;

    @Override
    public void onHit(final DamagingProjectileAPI projectile, final CombatEntityAPI target, final Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, final CombatEngineAPI engine) {
        if (target == null) return;
        if (!(target instanceof ShipAPI)) return;
        if (projectile.getSource() == null) return;
        if (projectile.getWeapon() == null) return;

        for (int i = 0; i < 20; i = i + 1) {
            engine.addHitParticle(
                    point,
                    MathUtils.getRandomPointInCone(new Vector2f(), MathUtils.getRandomNumberInRange(70f, 120f), projectile.getFacing() + 150f, projectile.getFacing() + 210f),
                    MathUtils.getRandomNumberInRange(4f, 6f),
                    2f,
                    MathUtils.getRandomNumberInRange(2.5f, 3.5f),
                    FM_Colors.FM_PURPLE_RED_CORE
            );
        }

        engine.addHitParticle(
                point,
                new Vector2f(),
                60f,
                255f,
                0.2f,
                FM_Colors.FM_PURPLE_RED_SPRITE
        );
//        engine.addNegativeParticle(
//                point,
//                new Vector2f(),
//                40f,
//                255f,
//                0.15f,
//                FM_Colors.FM_PURPLE_RED_SPRITE
//        );

        WaveDistortion wave = new WaveDistortion(point, FM_Misc.ZERO);
        wave.setSize(50f);
        wave.setIntensity(40f);
        wave.setArcAttenuationWidth(1f);
        wave.flip(false);
        DistortionShader.addDistortion(wave);
        wave.fadeInSize(0.1f);
        wave.fadeOutIntensity(0.4f);

        if (plugin == null && Math.random() <= BUFF_CHANCE) {
            MagicLensFlare.createSharpFlare(engine, (ShipAPI) target, point, 4f, 120f, 0, FM_Colors.FM_PURPLE_RED_SPRITE, FM_Colors.FM_RED_EMP_CORE);
            for (int i = 0; i < 3; i = i + 1) {
                engine.spawnEmpArcVisual(
                        point,
                        target,
                        MathUtils.getRandomPointInCone(point, target.getCollisionRadius(), projectile.getFacing() - 15f, projectile.getFacing() + 15f),
                        target,
                        3f,
                        FM_Colors.FM_PURPLE_RED_SPRITE,
                        FM_Colors.FM_PURPLE_RED_CORE
                );
            }
            plugin = new BaseEveryFrameCombatPlugin() {

                public float TIMER = TIMER_AMOUNT;

                @Override
                public void advance(float amount, List<InputEventAPI> events) {
                    super.advance(amount, events);

                    TIMER = TIMER - amount;
                    ((ShipAPI) target).getMutableStats().getMaxArmorDamageReduction().modifyFlat(buffId, -BUFF_FLAT);
                    float jitterBuff = TIMER / TIMER_AMOUNT;
                    ((ShipAPI) target).setJitterUnder(target, FM_Colors.FM_RED_EMP_FRINGE, 0.6f * jitterBuff, 10, 25f * jitterBuff);
                    ((ShipAPI) target).setCircularJitter(true);
                    if (TIMER <= 0f) {
                        ((ShipAPI) target).getMutableStats().getMaxArmorDamageReduction().unmodify(buffId);
                        engine.removePlugin(plugin);
                    }

                    if (target == engine.getPlayerShip()) {
                        engine.maintainStatusForPlayerShip(INFO, Global.getSettings().getSpriteName("ui", "icon_kinetic"),
                                I18nUtil.getString("misc", "FM_CaptureWeb_T"), I18nUtil.getString("misc", "FM_CaptureWeb_D") + (int) (BUFF_FLAT * 100f) + "%", true);
                    }
                }
            };
            engine.addPlugin(plugin);
        }
    }
}
