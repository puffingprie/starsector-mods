package data.shipsystems.ai;

import com.fs.starfarer.api.combat.*;
import data.shipsystems.FM_FluxBurn;
import data.utils.FM_Misc;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

import java.util.ArrayList;
import java.util.List;

public class FM_FluxBurn_ai implements ShipSystemAIScript {

    private ShipAPI ship;
    private ShipSystemAPI system;
    private final List<ShipAPI> enemyToRemove = new ArrayList<>();

    private float checkTimer = 0.2f;
    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.system = system;
    }

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
        if (ship == null) return;
        if (!ship.isAlive())return;
        if (system.isCoolingDown())return;
        checkTimer = checkTimer - amount;
        if (checkTimer <= 0f){
            if (ship.getFluxTracker().getHardFlux()/ship.getFluxTracker().getMaxFlux() >= 0.5f){
                ship.useSystem();
            }
            List<ShipAPI> enemies = AIUtils.getNearbyEnemies(ship, FM_Misc.getSystemRange(ship, FM_FluxBurn.RANGE));
            for (ShipAPI enemy : enemies){
                if (!enemy.isFighter()){
                    enemyToRemove.add(enemy);
                }
            }
            enemies.removeAll(enemyToRemove);
            if (enemies.size() >= 3){
                ship.useSystem();
            }
            checkTimer = checkTimer + 0.2f;
        }

    }
}
