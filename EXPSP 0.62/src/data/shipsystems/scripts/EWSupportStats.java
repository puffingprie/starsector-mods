package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
import org.lazywizard.lazylib.MathUtils;

import java.util.HashMap;
import java.util.Map;


public class EWSupportStats extends BaseShipSystemScript {

    private CombatEngineAPI engine;
    //public static final float SYSTEM_RANGE = 1750f;
    private static final float RANGE = 1500f;
    private final static float SPEED_BOOST = 15f;
    private static final float SENSOR_BOOST = 15f;
    private static final float MISSILE_BOOST = 50f;
    private static final String statID="Stats";
    //Creates a hashmap that keeps track of what ships are receiving the benefits.
    private static final Map<ShipAPI, ShipAPI> receiving = new HashMap<>();

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        if (engine != Global.getCombatEngine()) {
            engine = Global.getCombatEngine();
            receiving.clear();
        }

        ShipAPI host_ship = (ShipAPI) stats.getEntity();



        if (effectLevel > 0) {
            for (ShipAPI ship : engine.getShips()) {
                if (ship.isHulk() || ship.isFighter() || host_ship.getOwner() != ship.getOwner()) {
                    continue;
                }

                if (state ==  ShipSystemStatsScript.State.OUT) {
                    /*
                    ship.getMutableStats().getMaxSpeed().modifyFlat(id, effectLevel);
                    ship.getMutableStats().getBallisticWeaponRangeBonus().modifyPercent(id, effectLevel);
                    ship.getMutableStats().getMissileWeaponRangeBonus().modifyPercent(id, effectLevel);
                    ship.getMutableStats().getBeamWeaponRangeBonus().modifyPercent(id, effectLevel);
                    ship.getMutableStats().getEnergyWeaponRangeBonus().modifyPercent(id, effectLevel);
                    ship.getMutableStats().getMissileMaxSpeedBonus().modifyPercent(id, effectLevel);

                     */
                    stats.getMaxSpeed().unmodify(id);
                }  else if ((MathUtils.getDistance(ship, host_ship) <= (RANGE))){
                    ship.getMutableStats().getMaxSpeed().modifyFlat(id, effectLevel*SPEED_BOOST);
                    ship.getMutableStats().getBallisticWeaponRangeBonus().modifyPercent(id, effectLevel * SENSOR_BOOST);
                    ship.getMutableStats().getBeamWeaponRangeBonus().modifyPercent(id, effectLevel * SENSOR_BOOST);
                    ship.getMutableStats().getEnergyWeaponRangeBonus().modifyPercent(id, effectLevel * SENSOR_BOOST);
                    ship.getMutableStats().getMissileWeaponRangeBonus().modifyPercent(id, effectLevel * SENSOR_BOOST);
                    ship.getMutableStats().getMissileMaxSpeedBonus().modifyPercent(id, effectLevel * MISSILE_BOOST);
                }else{
                    ship.getMutableStats().getMaxSpeed().unmodify(id);
                    ship.getMutableStats().getBallisticWeaponRangeBonus().unmodify(id);
                    ship.getMutableStats().getEnergyWeaponRangeBonus().unmodify(id);
                    ship.getMutableStats().getBeamWeaponRangeBonus().unmodify(id);
                    ship.getMutableStats().getMissileWeaponRangeBonus().unmodify(id);
                    ship.getMutableStats().getMissileMaxSpeedBonus().unmodify(id);
                }
            }
        }
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        //same objects as before.
        ShipAPI host_ship = (ShipAPI) stats.getEntity();

        if (engine != Global.getCombatEngine()) {
            engine = Global.getCombatEngine();
            receiving.clear();
        }
        //stats.getSightRadiusMod().unmodify();

        for (ShipAPI ship : engine.getShips()) {
            if (ship.isHulk() || ship.isFighter() || host_ship.getOwner() != ship.getOwner()) {
                continue;
            }

            ship.getMutableStats().getMaxSpeed().unmodify(id);
            ship.getMutableStats().getBallisticWeaponRangeBonus().unmodify(id);
            ship.getMutableStats().getEnergyWeaponRangeBonus().unmodify(id);
            ship.getMutableStats().getBeamWeaponRangeBonus().unmodify(id);
            ship.getMutableStats().getMissileWeaponRangeBonus().unmodify(id);
            ship.getMutableStats().getMissileMaxSpeedBonus().unmodify(id);
        }
    }

    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData("Weapon range and missile speed increased for nearby ships", false);
        } else if (index == 1) {
            return new StatusData("Max speed increased by for nearby ships", false);
        }
        return null;
    }


}