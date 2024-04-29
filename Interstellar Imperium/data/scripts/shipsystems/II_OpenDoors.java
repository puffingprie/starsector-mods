package data.scripts.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import data.scripts.weapons.II_LightsEveryFrame;
import java.util.List;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

public class II_OpenDoors extends BaseShipSystemScript {

    private boolean started = false;
    private boolean done = false;
    private boolean pushed = false;
    private List<ShipAPI> children = null;

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        ShipAPI ship = (ShipAPI) stats.getEntity();
        if (ship == null) {
            return;
        }

        if (state == State.IN) {
            if (!started) {
                started = true;
                Global.getSoundPlayer().playSound("ii_apocalypse_doors", 1f, 1f, stats.getEntity().getLocation(), stats.getEntity().getVelocity());
                stats.getEntity().setCollisionClass(CollisionClass.SHIP);
                for (ShipAPI module : ship.getChildModulesCopy()) {
                    module.getMutableStats().getDynamic().getMod(II_LightsEveryFrame.LIGHTS_ALPHA_ID).modifyMult("ii_opendoors", 0f);
                }
            }

            if (effectLevel >= 0.5f) {
                if (!done) {
                    done = true;
                    children = ship.getChildModulesCopy();
                    for (ShipAPI child : children) {
                        child.setParentStation(null);
                        child.setStationSlot(null);
                        child.setCollisionClass(CollisionClass.FIGHTER);
                        child.setOwner(100);
                        child.getEngineController().forceFlameout(true);
                    }
                } else if (!pushed) {
                    pushed = true;
                    stats.getMissileRoFMult().unmodify(id);
                    for (ShipAPI child : children) {
                        Vector2f vel = new Vector2f(50f, 0f);
                        float angle = MathUtils.clampAngle(child.getFacing() + 90f);
                        VectorUtils.rotate(vel, angle, vel);
                        Vector2f.add(vel, ship.getVelocity(), vel);
                        Vector2f.add(vel, MathUtils.getRandomPointInCircle(null, 20f), vel);
                        child.setAngularVelocity(MathUtils.getRandomNumberInRange(-20f, 20f));
                        child.getVelocity().set(vel);
                    }
                }
            }
        } else if (!started) {
            stats.getMissileRoFMult().modifyMult(id, 0f);
            for (WeaponAPI weapon : ship.getUsableWeapons()) {
                weapon.setRemainingCooldownTo(1f);
            }
        }
    }

    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 1) {
            return new StatusData("Opening launch doors", false);
        }
        return null;
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        ShipAPI ship = (ShipAPI) stats.getEntity();
        if (ship == null) {
            return;
        }
        started = false;
        done = false;
        pushed = false;
        children = null;
        stats.getMissileRoFMult().unmodify(id);
    }
}
