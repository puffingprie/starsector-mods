package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import org.magiclib.util.MagicRender;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

public class Autocannon_Rem_Fire implements EveryFrameWeaponEffectPlugin {



    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {

        if(engine.isPaused() || weapon.getShip().getOriginalOwner()==-1){return;}

        if(MagicRender.screenCheck(0.25f, weapon.getLocation())){
            if(weapon.getChargeLevel()==1){

                float fluxBoost=0.5f*weapon.getShip().getFluxLevel();

                for(DamagingProjectileAPI p : CombatUtils.getProjectilesWithinRange(weapon.getLocation(), 200)){

                    if(p.getWeapon()!=weapon)continue;

                    p.getVelocity().scale(MathUtils.getRandomNumberInRange(0.9f, 1.05f));

                    //extra damage with hard flux levels
                    p.setDamageAmount(p.getBaseDamageAmount()*(1+fluxBoost));

                }
            }
        }
    }
}