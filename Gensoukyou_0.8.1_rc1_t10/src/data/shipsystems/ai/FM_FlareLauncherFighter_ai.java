package data.shipsystems.ai;

import com.fs.starfarer.api.combat.*;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

import java.util.List;

public class FM_FlareLauncherFighter_ai implements ShipSystemAIScript {

    public ShipwideAIFlags flags;
    public ShipAPI ship;
    public CombatEngineAPI engine;
    private float checkTimer = 0.2f;

    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.flags = flags;
        this.engine = engine;
    }

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
        if (!ship.isFighter()) return;
        if (ship.getSystem().isCoolingDown()) return;
        checkTimer = checkTimer - amount;
        if (checkTimer <= 0){
            List<MissileAPI> nearestMissiles = AIUtils.getNearbyEnemyMissiles(ship, 800f);
            boolean missileDanger = !nearestMissiles.isEmpty();
            if (flags.hasFlag(ShipwideAIFlags.AIFlags.IN_ATTACK_RUN) || missileDanger) {
                ship.useSystem();
            }
            checkTimer = checkTimer + 0.2f;
        }
    }
}
