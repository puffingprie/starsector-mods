package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.combat.listeners.DamageListener;

import java.awt.*;

public class FantasyFairyMod extends BaseHullMod {

    public static final float DAMAGE_CEIL = 200f;
    public static final float DAMAGE_BUFF_MULT = 0.33f;

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        super.advanceInCombat(ship, amount);

        if (Global.getCombatEngine() == null) return;
        CombatEngineAPI engine = Global.getCombatEngine();
        if (!engine.getListenerManager().hasListenerOfClass(FantasyFairyModDamageListener.class)) {
            engine.getListenerManager().addListener(new FantasyFairyModDamageListener(engine));
        }

    }

    public static class FantasyFairyModDamageListener implements DamageListener {

        public CombatEngineAPI engine;

        public FantasyFairyModDamageListener(CombatEngineAPI engine) {
            this.engine = engine;
        }

        @Override
        public void reportDamageApplied(Object source, CombatEntityAPI target, ApplyDamageResultAPI result) {
            if (!(target instanceof ShipAPI)) return;
            ShipAPI fighter = (ShipAPI) target;
            if (!fighter.isAlive()) return;
            if (!fighter.isFighter()) return;
            if (!fighter.getVariant().hasHullMod("FantasyFairyMod")) return;
            if (result.isDps()) return;
            float totalDamage = result.getTotalDamageToArmor() + result.getDamageToHull() + result.getDamageToShields();
            if (totalDamage >= DAMAGE_CEIL) {
                result.setDamageToHull(result.getDamageToHull() * DAMAGE_BUFF_MULT);
                result.setTotalDamageToArmor(result.getTotalDamageToArmor() * DAMAGE_BUFF_MULT);
                result.setDamageToShields(result.getDamageToShields() * DAMAGE_BUFF_MULT);

                fighter.setJitterUnder(fighter, Color.RED, 10f, 10, 1f, 2f);
            }
        }
    }

    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {

        if (index == 0) return "" + (int) DAMAGE_CEIL;
        if (index == 1) return "" + (int) (DAMAGE_BUFF_MULT * 100) + "%";

        return null;
    }
}
