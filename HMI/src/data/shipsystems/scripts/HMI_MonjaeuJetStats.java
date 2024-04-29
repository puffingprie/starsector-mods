package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipEngineControllerAPI.ShipEngineAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;

public class HMI_MonjaeuJetStats extends BaseShipSystemScript {

    protected Object STATUSKEY1 = new Object();
    protected Object STATUSKEY2 = new Object();

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {

        if (state == ShipSystemStatsScript.State.OUT) {
            stats.getMaxSpeed().modifyPercent(id, 50f * effectLevel); // to slow down ship to its regular top speed while powering drive down
            stats.getMaxTurnRate().modifyPercent(id, 100f * effectLevel);
            stats.getDeceleration().modifyPercent(id, 100f * effectLevel);
            stats.getTurnAcceleration().modifyPercent(id, 100f * effectLevel);
            stats.getMaxTurnRate().modifyPercent(id, 75f * effectLevel);
        } else {
            stats.getMaxSpeed().modifyFlat(id, 150f * effectLevel);
            stats.getMaxSpeed().modifyPercent(id, 10f * effectLevel);
            stats.getAcceleration().modifyFlat(id, 250f * effectLevel);
            stats.getDeceleration().modifyFlat(id, 250f * effectLevel);
            stats.getTurnAcceleration().modifyFlat(id, 110f * effectLevel);
            stats.getTurnAcceleration().modifyPercent(id, 400f * effectLevel);
            stats.getMaxTurnRate().modifyFlat(id, 80f * effectLevel);
            stats.getMaxTurnRate().modifyPercent(id, 150f * effectLevel);
        }
        if (stats.getEntity() instanceof ShipAPI) {
            ShipAPI ship = (ShipAPI) stats.getEntity();
            String key = ship.getId() + "_" + id;
            Object test = Global.getCombatEngine().getCustomData().get(key);
            if (state == State.IN) {
                if (test == null && effectLevel > 0.3f) {
                    Global.getCombatEngine().getCustomData().put(key, new Object());
                    ship.getEngineController().getExtendLengthFraction().advance(1f);
                    for (ShipEngineAPI engine : ship.getEngineController().getShipEngines()) {
                        if (engine.isSystemActivated()) {
                            ship.getEngineController().setFlameLevel(engine.getEngineSlot(), 1f);
                        }
                    }
                }
            } else {
                Global.getCombatEngine().getCustomData().remove(key);
            }
            if (ship == Global.getCombatEngine().getPlayerShip())
            {
                Global.getCombatEngine().maintainStatusForPlayerShip(STATUSKEY1, "graphics/icons/hullsys/maneuvering_jets.png",
                        ship.getPhaseCloak().getDisplayName(),
                        "Maneuverability Boost",
                        false);
                Global.getCombatEngine().maintainStatusForPlayerShip(STATUSKEY2, "graphics/icons/hullsys/maneuvering_jets.png",
                        ship.getPhaseCloak().getDisplayName(),
                        "Speed Boost",
                        false);
            }
        }
    }

    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData("Maneuverability Boost", false);
        } else if (index == 1) {
            return new StatusData("Speed Boost", false);
        }
        return null;
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getMaxSpeed().unmodify(id);
        stats.getMaxTurnRate().unmodify(id);
        stats.getTurnAcceleration().unmodify(id);
        stats.getAcceleration().unmodify(id);
        stats.getDeceleration().unmodify(id);
    }
}
