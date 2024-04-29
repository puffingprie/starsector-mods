package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import org.magiclib.util.MagicRender;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

public class HMI_puckleeffect_alt2 implements EveryFrameWeaponEffectPlugin {
    private float refire=0;
    private int firing=1;

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        
        if(engine.isPaused() || weapon.getShip().getOriginalOwner()==-1){return;}
        
        if(MagicRender.screenCheck(0.25f, weapon.getLocation())){
            if(weapon.getChargeLevel()==1) {

                if (weapon.isFiring()) {
                    firing=1; //the weapon is firing, this is to check if the weapon does not cease to fire for more than one frame
                    refire = Math.max(1, refire + 0.1f);
                    for (DamagingProjectileAPI p : CombatUtils.getProjectilesWithinRange(weapon.getLocation(), 200)) {

                        if (p.getWeapon() != weapon) continue;

                        float inverseSpread = weapon.getCurrSpread() / weapon.getSpec().getMaxSpread();
                        float inverseSpread2 = Math.min(inverseSpread, 1f);
                        float inverseSpread3 = 1f - inverseSpread2;

                        float spread = inverseSpread3 * weapon.getSpec().getMaxSpread();
                        float angle = (float) (Math.random() * spread);
                        float angle2 = (angle * 2f) - weapon.getSpec().getMaxSpread();

                        Vector2f projectileVelocity = new Vector2f(p.getVelocity());

                        float facing = weapon.getArcFacing() + angle2;
                        float projVelDir = VectorUtils.getFacing(projectileVelocity);

                        VectorUtils.rotate(projectileVelocity, MathUtils.clampAngle(facing - projVelDir));
                        p.getVelocity().set(projectileVelocity);

                    }
                } else if(firing>0){
                    firing--;
                } else {
                    refire=0; //reset the refire delay
                }
            }
        }
    }
}