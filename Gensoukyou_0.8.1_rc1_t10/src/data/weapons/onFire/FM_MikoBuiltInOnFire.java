package data.weapons.onFire;

import com.fs.starfarer.api.combat.*;
import data.hullmods.FantasySpellMod;
import data.utils.FM_Colors;
import data.utils.FM_Misc;

public class FM_MikoBuiltInOnFire implements OnFireEffectPlugin {

    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        if (weapon == null) return;
        if (weapon.getShip() == null) return;
        ShipAPI ship = weapon.getShip();
        FantasySpellMod.SpellModState state = FM_Misc.getSpellModState(engine, ship);
        state.spellPower = state.spellPower - 0.06f;
        engine.addHitParticle(weapon.getFirePoint(0), FM_Misc.ZERO, 150f, 255f, 0.1f, FM_Colors.FM_TEXT_RED);
    }
}
