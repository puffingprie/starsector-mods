package data.weapons.everyFrame;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import data.utils.FM_Colors;
import data.utils.FM_ProjectEffect;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class FM_DesireAcceleratorEveryFrame implements EveryFrameWeaponEffectPlugin {
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (weapon.getShip() == null) return;
        if (weapon.getChargeLevel() >= 1f && weapon.getCooldownRemaining() <= 0f) {
            Global.getSoundPlayer().playSound("FM_Spark_pd_fire", 1.5f, 1f, weapon.getLocation(), new Vector2f());
            Vector2f firePoint = weapon.getFirePoint(0);
            engine.spawnEmpArcVisual(
                    firePoint,
                    weapon.getShip(),
                    MathUtils.getRandomPointInCircle(firePoint, 60f),
                    weapon.getShip(),
                    3f,
                    FM_ProjectEffect.EFFECT_3,
                    FM_Colors.FM_GREEN_EMP_CORE

            );
            for (int i = 0; i < 3; i = i + 1) {
                engine.addNegativeSwirlyNebulaParticle(
                        firePoint,
                        MathUtils.getRandomPointInCircle(new Vector2f(), MathUtils.getRandomNumberInRange(55f, 65f)),
                        MathUtils.getRandomNumberInRange(30f, 40f),
                        0.6f,
                        0.2f,
                        0.6f,
                        0.8f,
                        FM_Colors.FM_GREEN_EMP_CORE

                );
            }
        }
    }
}
