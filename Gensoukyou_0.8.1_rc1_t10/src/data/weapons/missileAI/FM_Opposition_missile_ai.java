package data.weapons.missileAI;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.combat.NegativeExplosionVisual;
import com.fs.starfarer.api.util.Misc;
import data.utils.FM_Colors;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class FM_Opposition_missile_ai implements MissileAIPlugin {

    private CombatEngineAPI engine;
    private final MissileAPI missile;
    private final ShipAPI ship;
    private static final float SECOND_STAGE_RANGE = 1300f;

    private static final Color EFFECT = new Color(44, 217, 203, 228);


    public FM_Opposition_missile_ai(MissileAPI missile, ShipAPI ship) {
        this.missile = missile;
        this.ship = ship;
    }

    public void advance(float amount) {

        engine = Global.getCombatEngine();
        if (engine == null) return;
        if (missile.getWeapon() == null) return;

        ShipAPI target = findTarget(ship, missile);

        if (target == null) {
            return;
        }


        float missile_facing = Misc.normalizeAngle(missile.getFacing());

        Vector2f best_strike = target.getLocation();
        Vector2f intercept = AIUtils.getBestInterceptPoint(missile.getLocation(), 900f, target.getLocation(), target.getVelocity());
        if (intercept != null) {
            best_strike = intercept;
        }
        if (best_strike == null) return;
        float attack_line = Misc.normalizeAngle(VectorUtils.getAngle(missile.getLocation(), best_strike));

        float diff = attack_line - missile_facing;
        //engine.addFloatingText(missile.getLocation(),"" + diff,20f,Color.WHITE,missile,0f,0f);

        if (Misc.normalizeAngle(diff) <= 1f) {
            missile.setFacing(attack_line);
        } else {
            if (diff <= 180f && diff > 0f) {
                //missile.giveCommand(ShipCommand.TURN_LEFT);
                missile_facing = missile_facing + amount * missile.getMaxTurnRate();
                missile.setFacing(missile_facing);
            } else {
                //missile.giveCommand(ShipCommand.TURN_RIGHT);
                missile_facing = missile_facing - amount * missile.getMaxTurnRate();
                missile.setFacing(missile_facing);
            }
        }

        if (missile.getElapsed() >= 1f && MathUtils.getShortestRotation(missile_facing, attack_line) <= 0.5f &&
                MathUtils.isWithinRange(missile.getLocation(), target.getLocation(), SECOND_STAGE_RANGE)) {

            //主要二阶相关
            engine.spawnProjectile(ship, missile.getWeapon(), "FM_Opposition_warhead_weapon", missile.getLocation(), missile_facing, null);
            //engine.spawnExplosion(missile.getLocation(),new Vector2f(), Color.WHITE,30f,1f);

            //视觉相关弹幕生成
//            for (int i = 0; i < 3 ; i = i + 1){
//                Vector2f begin = new Vector2f(VectorUtils.resize(missile.getVelocity(),20f,new Vector2f()));
//                VectorUtils.rotate(begin,60f + i * 120f,begin);
//                Vector2f mid = MathUtils.getMidpoint(begin,new Vector2f());
//                Vector2f.add(mid,missile.getLocation(),mid);
//                for (int k = 0 ; k < 12 ; k = k + 1){
//                    Vector2f spawnPoint = MathUtils.getPoint(mid,10f,VectorUtils.getFacing(begin) + k * 15f);
//                    float spawnFacing = VectorUtils.getFacing(begin) + 90f - (7.5f * k);
//
//                    engine.spawnProjectile(ship,missile.getWeapon(),"FM_ice_weapon_s",spawnPoint,spawnFacing,new Vector2f());
//                    engine.addSmoothParticle(spawnPoint,new Vector2f(),4f,1f,-2f,4f,EFFECT);
//                }
//            }


            engine.removeEntity(missile);

            NegativeExplosionVisual.NEParams neEffect = new NegativeExplosionVisual.NEParams();
            neEffect.fadeOut = 0.6f;
            neEffect.radius = 20f;
            neEffect.thickness = 4f;
            neEffect.color = EFFECT;
            neEffect.underglow = FM_Colors.FM_DARK_BLUE;


            CombatEntityAPI visual = engine.addLayeredRenderingPlugin(new NegativeExplosionVisual(neEffect));

            visual.getLocation().set(missile.getLocation());

            Global.getSoundPlayer().playSound("FM_Opposition_expand", 1f, 1f, missile.getLocation(), new Vector2f());


        }


    }

    private ShipAPI findTarget(ShipAPI ship, MissileAPI missile) {
        ShipAPI target = ship.getShipTarget();
        if (target == null) {
            target = Misc.findClosestShipEnemyOf(ship, missile.getLocation(), ShipAPI.HullSize.FRIGATE, SECOND_STAGE_RANGE, true);

        }
//        if (target != null && target.isFighter()){
//            target = null;
//        }

        return target;
    }

}
