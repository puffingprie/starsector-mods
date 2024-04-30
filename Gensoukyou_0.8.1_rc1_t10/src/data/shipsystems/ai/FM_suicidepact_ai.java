package data.shipsystems.ai;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class FM_suicidepact_ai implements ShipSystemAIScript {

    public static final float HULL_LEVEL_CHECK = 0.5f;

    public ShipAPI enemy = null;

    private ShipAPI ship;
    private float direction;
    private float active = 0f;
    private final IntervalUtil tracker = new IntervalUtil(0.5f, 1f);

    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
    }

    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
        if (ship == null) return;

        float hull_level = ship.getHullLevel();
        if (hull_level <= HULL_LEVEL_CHECK) {

            if (ship != null) {
                float ship_facing = ship.getFacing();
                enemy = AIUtils.getNearestEnemy(ship);

                Vector2f ship_loc = ship.getLocation();


                if (enemy != null) {
                    Vector2f enemy_loc = enemy.getLocation();
                    Vector2f best_intercept_loc = AIUtils.getBestInterceptPoint(ship_loc, ship.getMaxSpeed(), enemy_loc, enemy.getVelocity());


                    if (best_intercept_loc != null) {
                        direction = VectorUtils.getAngle(ship_loc, best_intercept_loc);

                        //强制舰船转向到目标方向
                        if (ship_facing > direction) {
                            ship.giveCommand(ShipCommand.TURN_RIGHT, enemy, 1);
                        } else {
                            ship.giveCommand(ShipCommand.TURN_LEFT, enemy, 1);
                        }

                        if (ship_facing < direction + 2f && ship_facing > direction - 2f) {
                            active = active + 1f;
                        }
                        if (active > 5f) {
                            ship.useSystem();
                            active = 0f;
                        }

                    }
                }
                //engine.addFloatingText(ship.getLocation(), String.valueOf(active),10, Color.WHITE,ship,1f,1f);
            }


        }
    }
}
