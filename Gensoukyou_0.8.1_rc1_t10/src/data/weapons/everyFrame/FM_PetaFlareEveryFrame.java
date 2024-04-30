package data.weapons.everyFrame;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import data.utils.FM_Colors;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicAnim;

public class FM_PetaFlareEveryFrame implements EveryFrameWeaponEffectPlugin {

    public static final float RADIUS = 160f;


    private float TIMER = 0f;

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {

        if (weapon.getShip() == null || !weapon.getShip().isAlive()) return;

        if (weapon.getChargeLevel() <= 1f && weapon.isFiring() && weapon.getCooldownRemaining() <= 0) {

            TIMER = TIMER + amount;
            if (TIMER > 0.2f) {
                for (int i = 0; i < 7 * weapon.getChargeLevel(); i = i + 1) {
                    engine.spawnProjectile(
                            weapon.getShip(),
                            weapon,
                            "flarelauncher1",
                            weapon.getFirePoint(0),
                            MathUtils.getRandomNumberInRange(weapon.getCurrAngle() - 60f, weapon.getCurrAngle() + 60f),
                            new Vector2f()
                    );
                }
                TIMER = 0f;
            }
            if (!weapon.getSlot().isHidden()) {
                weapon.getAnimation().play();
            }
            float r = MagicAnim.smooth(weapon.getChargeLevel()) * RADIUS;
            for (int i = 0; i < 10; i = i + 1) {

                Vector2f li = MathUtils.getRandomPointInCircle(weapon.getFirePoint(0), r * (1 - weapon.getChargeLevel()));
                Vector2f vi = (Vector2f) VectorUtils.getDirectionalVector(li, weapon.getFirePoint(0)).scale(120f * (1 - weapon.getChargeLevel()));
                engine.addHitParticle(
                        li,
                        vi,
                        MathUtils.getRandomNumberInRange(4f, 8f),
                        1f,
                        0.3f,
                        FM_Colors.FM_ORANGE_FLARE_FRINGE
                );
            }
        }

        if (weapon.getCooldownRemaining() > 0f) {
            TIMER = 0;
            if (!weapon.getSlot().isHidden()) {
                weapon.getAnimation().pause();
                weapon.getAnimation().setFrame(0);
            }
        }
    }
}
