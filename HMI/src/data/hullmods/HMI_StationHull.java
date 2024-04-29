package data.hullmods;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.listeners.DamageTakenModifier;
import com.fs.starfarer.api.loading.ProjectileSpawnType;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class HMI_StationHull extends BaseHullMod {

    public static final float SENSOR_MOD = 80f;
    public static final float MANEUVER_MALUS = -15f;
    private static final float RANGE_MULT = 0.90f;
    public static final float REDUCED_EXPLOSION_MULT = 0.2f;
    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getSensorProfile().modifyFlat(id, SENSOR_MOD);
        stats.getZeroFluxSpeedBoost().modifyPercent(id, MANEUVER_MALUS);
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id){

        if (ship.getVariant().getHullMods().contains("frontshield")) {
            ship.getMutableStats().getAcceleration().modifyPercent(id, MANEUVER_MALUS * 2f);
            ship.getMutableStats().getDeceleration().modifyPercent(id, MANEUVER_MALUS);
            ship.getMutableStats().getTurnAcceleration().modifyPercent(id, MANEUVER_MALUS * 2f);
            ship.getMutableStats().getMaxTurnRate().modifyPercent(id, MANEUVER_MALUS);
            }

        if (ship.getVariant().getHullMods().contains("augmentedengines")) {
            ship.getMutableStats().getFuelUseMod().modifyMult(id, 1.5f);
        }

        if (ship.getVariant().getHullMods().contains("unstable_injector")){
            ship.getMutableStats().getBallisticWeaponRangeBonus().modifyMult(id, RANGE_MULT);
            ship.getMutableStats().getEnergyWeaponRangeBonus().modifyMult(id, RANGE_MULT);
        }
        ship.addListener(new LocomotiveDamageListener());
    }

    public static class LocomotiveDamageListener implements DamageTakenModifier {

//Script courtesy of Rubi - much appreciated!

        @Override
        public String modifyDamageTaken(Object param, CombatEntityAPI target, DamageAPI damage, Vector2f point, boolean shieldHit) {
            // checking for ship explosions
            if (param instanceof DamagingProjectileAPI) {
                DamagingProjectileAPI proj = (DamagingProjectileAPI) param;
                // checks if the damage fits the details of a ship explosion
                if (proj.getDamageType().equals(DamageType.HIGH_EXPLOSIVE)
                        && proj.getProjectileSpecId() == null
                        && !proj.getSource().isAlive()
                        && proj.getSpawnType().equals(ProjectileSpawnType.OTHER)
                        && MathUtils.getDistance(proj.getSpawnLocation(), proj.getSource().getLocation()) < 0.5f) {
                    damage.getModifier().modifyMult(this.getClass().getName(), REDUCED_EXPLOSION_MULT);
                }
            }
            return null;
        }
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return "" + "Makeshift Shield Generator";
        if (index == 1) return "" + "decrease manoeuvrability and zero flux speed boost by 15%";
        if (index == 2) return "" + "Augmented Drive Field";
        if (index == 3) return "" + "fuel consumption increasing by 50%";
        if (index == 4) return "" + "Unstable Injector";
        if (index == 5) return "" + (int) Math.round((1f - RANGE_MULT) * 100f) + "%";
        if (index == 6) return "" + "damage caused by ship destruction explosions are reduced by 80%";
        if (index == 7) return "" + (int)Math.round(SENSOR_MOD);

        return null;
    }
}
