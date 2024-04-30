package data.weapons.onFire;

import com.fs.starfarer.api.combat.*;
import org.lazywizard.lazylib.VectorUtils;

public class FM_PetalOnFire implements OnFireEffectPlugin {

    private int missileCycle = 0;

    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        if (weapon == null) return;
        if (projectile.getSource() == null) return;
        if (!(projectile instanceof MissileAPI)) return;
        if (missileCycle < 5) {
            MissileAPI missile = (MissileAPI) projectile;
            float facing = weapon.getCurrAngle() - 7.5f + 3f * missileCycle;
            missile.setFacing(facing);

            VectorUtils.rotate(missile.getVelocity(), facing - weapon.getCurrAngle(), missile.getVelocity());

        } else if (missileCycle < 10) {
            final MissileAPI missile = (MissileAPI) engine.spawnProjectile(projectile.getSource(), weapon, "FM_Petal_S_ke",
                    projectile.getLocation(), projectile.getFacing(), projectile.getSource().getVelocity());
            engine.removeEntity(projectile);
            float facing = weapon.getCurrAngle() + 6f - 2.4f * (missileCycle - 5);
            if (missile == null) return;
            missile.getDamage().setFluxComponent(0f);
            missile.setFacing(facing);
            VectorUtils.rotate(missile.getVelocity(), facing - weapon.getCurrAngle(), missile.getVelocity());
            //missile.getEngineController().getFlameColorShifter().setBase(FM_Colors.FM_GREEN_EMP_FRINGE);

        } else if (missileCycle < 15) {
            final MissileAPI missile = (MissileAPI) engine.spawnProjectile(projectile.getSource(), weapon, "FM_Petal_S_he",
                    projectile.getLocation(), projectile.getFacing(), projectile.getSource().getVelocity());

            engine.removeEntity(projectile);
            float facing = weapon.getCurrAngle() - 4.5f + 1.8f * (missileCycle - 10);
            if (missile == null) return;
            missile.getDamage().setFluxComponent(0f);
            missile.setFacing(facing);
            VectorUtils.rotate(missile.getVelocity(), facing - weapon.getCurrAngle(), missile.getVelocity());
            //missile.getEngineController().getFlameColorShifter().setBase(FM_Colors.FM_RED_EMP_FRINGE);

        } else if (missileCycle < 20) {
            final MissileAPI missile = (MissileAPI) engine.spawnProjectile(projectile.getSource(), weapon, "FM_Petal_S_fr",
                    projectile.getLocation(), projectile.getFacing(), projectile.getSource().getVelocity());

            engine.removeEntity(projectile);
            if (missile == null) return;
            missile.getDamage().setFluxComponent(0f);
            float facing = weapon.getCurrAngle() + 3f - 1.2f * (missileCycle - 15);
            missile.setFacing(facing);
            VectorUtils.rotate(missile.getVelocity(), facing - weapon.getCurrAngle(), missile.getVelocity());

        }

        missileCycle = missileCycle + 1;
        if (missileCycle >= 20) {
            missileCycle = 0;
        }
    }
}
