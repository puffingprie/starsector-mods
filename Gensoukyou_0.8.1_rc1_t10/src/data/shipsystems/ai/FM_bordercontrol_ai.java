package data.shipsystems.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.util.IntervalUtil;
import data.shipsystems.FM_bordercontrol;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class FM_bordercontrol_ai implements ShipSystemAIScript {

    private static final float RANGE = FM_bordercontrol.RANGE;

    private ShipAPI ship;
    private ShipSystemAPI system;


    private float active = 0f;

    private final IntervalUtil tracker = new IntervalUtil(0.5f, 1f);

    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.system = system;
    }

    public float getSystemRange(ShipAPI ship) {
        return ship.getMutableStats().getSystemRangeBonus().computeEffective(
                RANGE
        );
    }

    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {

        if (ship == null || target == null) return;
        if (Global.getCombatEngine() == null) return;

        if (ship.hasLaunchBays()) {
            if (!ship.isPullBackFighters()) {

                if (target.areSignificantEnemiesInRange() && getSystemRange(ship) > MathUtils.getDistance(ship.getLocation(), target.getLocation())) {
                    active = active + 1;
                }

                if (active > 100) {
                    ship.useSystem();
                }


                if (system.getState() != ShipSystemAPI.SystemState.IDLE || MathUtils.getDistance(ship.getLocation(), target.getLocation()) < 1000f) {
                    active = 0;
                }
            } else {
                active = 0;
            }
        }

        //engine.addFloatingText(ship.getLocation(), String.valueOf(active),10, Color.WHITE,ship,1f,1f);

    }
}
