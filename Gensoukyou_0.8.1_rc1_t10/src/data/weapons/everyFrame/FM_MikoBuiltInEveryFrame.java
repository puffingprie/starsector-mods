package data.weapons.everyFrame;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import data.hullmods.FantasySpellMod;
import data.utils.FM_Misc;

public class FM_MikoBuiltInEveryFrame implements EveryFrameWeaponEffectPlugin {

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (weapon.getShip() == null) return;
        ShipAPI ship = weapon.getShip();
        FantasySpellMod.SpellModState state = FM_Misc.getSpellModState(engine, ship);
        if (state.spellPower < 0.06f) {
            weapon.setRemainingCooldownTo(2f);
        }
//        if (ship.getShipAI() != null){
//            if (state.spellPower <= 0.25f){
//                ship.getAIFlags().setFlag(ShipwideAIFlags.AIFlags.BACKING_OFF);
//                ship.getAIFlags().setFlag(ShipwideAIFlags.AIFlags.BACK_OFF_MIN_RANGE);
////                ShipAPI enemyShip = Misc.findClosestShipEnemyOf(ship,ship.getLocation(), ShipAPI.HullSize.FRIGATE,weapon.getRange(),true);
////                Vector2f bestFire = MathUtils.getPoint(ship.getLocation(),weapon.getRange(),ship.getFacing());
////                if (enemyShip != null){
////                    bestFire = AIUtils.getBestInterceptPoint(weapon.getFirePoint(0), weapon.getProjectileSpeed(), enemyShip.getLocation(),enemyShip.getVelocity());
////                }
//
////                if (ship.getAIFlags().hasFlag(ShipwideAIFlags.AIFlags.BACK_OFF) || ship.getAIFlags().hasFlag(ShipwideAIFlags.AIFlags.IN_CRITICAL_DPS_DANGER)){
////                    weapon.setRemainingCooldownTo(5f);
////                }
//            }else {
//                ship.getAIFlags().unsetFlag(ShipwideAIFlags.AIFlags.BACKING_OFF);
//                ship.getAIFlags().unsetFlag(ShipwideAIFlags.AIFlags.BACK_OFF_MIN_RANGE);
//                if (state.spellPower >= 0.6f){
//                    ship.getAIFlags().setFlag(ShipwideAIFlags.AIFlags.HARASS_MOVE_IN);
//                    ship.getAIFlags().setFlag(ShipwideAIFlags.AIFlags.PURSUING);
//                }
//            }
//        }
    }
}
