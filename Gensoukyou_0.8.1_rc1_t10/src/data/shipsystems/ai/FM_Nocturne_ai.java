package data.shipsystems.ai;

import com.fs.starfarer.api.combat.*;
import data.shipsystems.FM_Nocturne;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

import java.util.ArrayList;
import java.util.List;

public class FM_Nocturne_ai implements ShipSystemAIScript {

    private ShipwideAIFlags flags;
    private ShipAPI ship;
    private ShipSystemAPI system;
    private final ArrayList<MissileAPI> dangerMissiles = new ArrayList<>();
    private final ArrayList<MissileAPI> removedMissiles = new ArrayList<>();
    private MissileAPI missile;

    private final float timer = 0f;

    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.flags = flags;
        this.system = system;
    }

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {

        if (system.getAmmo() == 0 || system.getState() == ShipSystemAPI.SystemState.COOLDOWN) {
            dangerMissiles.clear();
            return;
        }
//        MissileAPI nearestEnemyMissile = AIUtils.getNearestEnemyMissile(ship);
//        if (nearestEnemyMissile == null)return;
        List<MissileAPI> nearbyEnemyMissiles = AIUtils.getNearbyEnemyMissiles(ship, FM_Nocturne.RANGE);
        if (nearbyEnemyMissiles.isEmpty()) return;
//        if (!dangerMissiles.contains(nearestEnemyMissile) && nearestEnemyMissile.getMaxFlightTime() < nearestEnemyMissile.getFlightTime()){
//            dangerMissiles.add(nearestEnemyMissile);
//        }
        dangerMissiles.addAll(nearbyEnemyMissiles);
        missile = AIUtils.getNearestEnemyMissile(ship);
//        MissileAPI nearestEnemyMissile = AIUtils.getNearestEnemyMissile(ship);
//        if (nearestEnemyMissile != null){
//            if (MathUtils.isWithinRange(nearestEnemyMissile.getLocation(),ship.getLocation(),FM_Nocturne.RANGE) &&
//                    !nearestEnemyMissile.isFading() &&
//                    nearestEnemyMissile.getMaxFlightTime() > nearestEnemyMissile.getFlightTime() &&
//                    !nearestEnemyMissile.isDecoyFlare() &&
//                    !nearestEnemyMissile.isFlare()){
//                dangerMissiles.add(nearestEnemyMissile);
//            }
//        }

        float maxDanger = 0;
        for (MissileAPI dangerMissile : dangerMissiles) {
            if (dangerMissile.isFading() || dangerMissile.getMaxFlightTime() < dangerMissile.getFlightTime()
                    || MathUtils.isWithinRange(ship.getLocation(), dangerMissile.getLocation(), FM_Nocturne.RANGE) || dangerMissile.isDecoyFlare() || dangerMissile.isFlare()) {
                removedMissiles.add(dangerMissile);
                continue;
            }
            if (maxDanger < dangerMissile.getDamageAmount() + dangerMissile.getEmpAmount() * ((FM_Nocturne.RANGE - MathUtils.getDistance(dangerMissile, ship)) / FM_Nocturne.RANGE)) {
                maxDanger = (dangerMissile.getDamageAmount() + dangerMissile.getEmpAmount()) * ((FM_Nocturne.RANGE - MathUtils.getDistance(dangerMissile, ship)) / FM_Nocturne.RANGE);
                missile = dangerMissile;
            }
        }
        dangerMissiles.removeAll(removedMissiles);

        if (missile == null) return;
        if (!MathUtils.isWithinRange(missile, ship, FM_Nocturne.RANGE)
                || missile.isFlare()
                || missile.isFading()
                || missile.isExpired()
                || missile.getMaxFlightTime() <= missile.getFlightTime()) return;
        Vector2f missileLoc = missile.getLocation();
        ship.getMouseTarget().set(missileLoc);
        if (!CombatUtils.getMissilesWithinRange(ship.getMouseTarget(), FM_Nocturne.EFFECT_RANGE).isEmpty()) {
            ship.useSystem();
        }
        flags.setFlag(ShipwideAIFlags.AIFlags.SYSTEM_TARGET_COORDS, 0.25f, missileLoc);


        //debug
        //Global.getCombatEngine().addHitParticle(ship.getMouseTarget(),new Vector2f(),20f,100f,1f, Color.BLUE);


    }
}
