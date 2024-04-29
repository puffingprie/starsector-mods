package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import data.scripts.plugins.sv_weaponDamageListener;

public class SV_MODSPEC implements EveryFrameWeaponEffectPlugin {
    
    //These ones are used in-script, so don't touch them!
    private boolean hasFiredThisCharge = false;
    private int currentBarrel = 0;
    private boolean shouldOffsetBarrelExtra = false;

    //Instantiator
    public SV_MODSPEC() {}

    boolean doOnce = true;

    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        //Don't run while paused, or without a weapon
        if (weapon == null || amount <= 0f) {return;}

        if (doOnce) {
            if (!weapon.getShip().hasListenerOfClass(sv_weaponDamageListener.class)){
                weapon.getShip().addListener(new sv_weaponDamageListener());
            }
            doOnce = false;
        }
       }
}