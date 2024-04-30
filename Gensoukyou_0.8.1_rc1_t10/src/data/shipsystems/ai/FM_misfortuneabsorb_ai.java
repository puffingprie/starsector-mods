package data.shipsystems.ai;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.util.IntervalUtil;
import data.shipsystems.FM_misfortuneabsorb;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

import java.util.ArrayList;
import java.util.List;

public class FM_misfortuneabsorb_ai implements ShipSystemAIScript {
    public static final float RANGE = FM_misfortuneabsorb.RANGE;

    private ShipAPI ship;
    private ShipSystemAPI system;
    private final List friendly_ships = new ArrayList();

    private final IntervalUtil tracker = new IntervalUtil(0.5f, 1f);

    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.system = system;
    }


    private float active = 0f;

    public float getSystemRange(ShipAPI ship) {
        return ship.getMutableStats().getSystemRangeBonus().computeEffective(
                RANGE
        );
    }


    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
        tracker.advance(amount);
        if (ship == null) return;


        List<ShipAPI> ship_in_range = CombatUtils.getShipsWithinRange(ship.getLocation(), getSystemRange(ship) + 100f);
        for (ShipAPI other_ship : ship_in_range) {
            if (other_ship.getOwner() == ship.getOwner() && other_ship != ship && other_ship.isAlive() && other_ship.getAIFlags().hasFlag(ShipwideAIFlags.AIFlags.NEEDS_HELP) &&
                    !other_ship.isFighter() && !other_ship.isDrone()) {
                friendly_ships.add(other_ship);
            }
        }

        int friendly_ships_num = friendly_ships.size();


        if (!system.isCoolingDown()) {
            active = friendly_ships_num + active;
        } else {
            active = 0f;
        }

        if (active > 1000f && !system.isCoolingDown() && !ship.getAIFlags().hasFlag(ShipwideAIFlags.AIFlags.NEEDS_HELP)) {
            ship.useSystem();
            active = 0f;

        }

        //engine.addFloatingText(ship.getLocation(), String.valueOf(friendly_ships_num),10, Color.WHITE,ship,1f,1f);

        //engine.addFloatingText(ship.getLocation(), String.valueOf(active),10, Color.WHITE,ship,1f,1f);


    }
}
