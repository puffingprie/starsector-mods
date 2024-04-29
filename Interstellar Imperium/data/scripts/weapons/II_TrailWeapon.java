package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import data.scripts.everyframe.II_Trails;
import data.scripts.everyframe.II_WeaponScriptPlugin;

public class II_TrailWeapon implements OnFireEffectPlugin {

    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        II_Trails.createIfNeeded();
        II_WeaponScriptPlugin.createIfNeeded();
    }
}
