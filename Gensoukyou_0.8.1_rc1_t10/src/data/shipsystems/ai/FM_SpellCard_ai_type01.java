package data.shipsystems.ai;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

import java.util.List;

//如果我能找到原版的EMP ai 和让这一堆东西正常工作的方法…………………………

//为了使带有友伤的某些战术系统没那么容易打到自己人.jpg

public class FM_SpellCard_ai_type01 implements ShipSystemAIScript {

    private ShipAPI ship;
    private CombatEngineAPI engine;
    private ShipwideAIFlags flags;
    private ShipSystemAPI system;

    private final IntervalUtil tracker = new IntervalUtil(0.5f, 1f);

    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {

        this.ship = ship;
        this.flags = flags;
        this.engine = engine;
        this.system = system;

    }

    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {


        tracker.advance(amount);
        float range = system.getSpecAPI().getRange(ship.getMutableStats());

        if (ship == null || engine == null) return;


        //engine.addFloatingText(ship.getLocation(),String.valueOf(range),10f,Color.WHITE,ship,1f,1f);

        if (tracker.intervalElapsed()) {
            if (system.getCooldownRemaining() > 0) return;
            if (system.isOutOfAmmo()) return;
            if (system.isActive()) return;
            if (flags.hasFlag(ShipwideAIFlags.AIFlags.HARASS_MOVE_IN_COOLDOWN) ||
                    flags.hasFlag(ShipwideAIFlags.AIFlags.BACK_OFF) ||
                    flags.hasFlag(ShipwideAIFlags.AIFlags.BACKING_OFF)
            ) return;


            List<ShipAPI> ships_around = CombatUtils.getShipsWithinRange(ship.getLocation(), range * 1.5f);

            float totalWeight = 0f;

            for (ShipAPI enemy_or_ally : ships_around) {

                if (enemy_or_ally.getOwner() != ship.getOwner() && enemy_or_ally.isAlive()) {
                    if ((enemy_or_ally.isCapital() || enemy_or_ally.isStation() || enemy_or_ally.isStationModule())
                            && MathUtils.isWithinRange(ship.getLocation(), enemy_or_ally.getLocation(), range)) {
                        totalWeight = totalWeight + 4f;
                    }
                    if ((enemy_or_ally.isDestroyer() || enemy_or_ally.isCruiser())
                            && MathUtils.isWithinRange(ship.getLocation(), enemy_or_ally.getLocation(), range)) {
                        totalWeight = totalWeight + 4f;
                    }
                    if (((enemy_or_ally.isFrigate() || enemy_or_ally.isFighter() || enemy_or_ally.isDrone()) &&
                            MathUtils.isWithinRange(ship.getLocation(), enemy_or_ally.getLocation(), range))) {
                        totalWeight = totalWeight + 4f;
                    }
                }

                if (enemy_or_ally.getOwner() == ship.getOwner()) {
                    //在友军处于脆弱状态时禁止使用
                    if (enemy_or_ally.getFluxTracker().isOverloadedOrVenting() && !ship.isFighter() &&
                            (enemy_or_ally.getFluxTracker().getOverloadTimeRemaining() > 5f ||
                                    enemy_or_ally.getFluxTracker().getTimeToVent() > 5f)) {
                        return;
                    } else {
                        if (!enemy_or_ally.isFighter() && !enemy_or_ally.isDrone() && !ship.isFighter()) {
                            totalWeight = totalWeight - 1f;

                            if (MathUtils.isWithinRange(enemy_or_ally, ship, range) && !ship.isFighter()) {

                                totalWeight = totalWeight - 2f;

                            }
                        }

                    }
                }

                if ((flags.hasFlag(ShipwideAIFlags.AIFlags.HARASS_MOVE_IN) ||
                        flags.hasFlag(ShipwideAIFlags.AIFlags.PURSUING) ||
                        flags.hasFlag(ShipwideAIFlags.AIFlags.MAINTAINING_STRIKE_RANGE))
                        && MathUtils.isWithinRange(ship.getLocation(), enemy_or_ally.getLocation(), range)) {
                    totalWeight = totalWeight + 2f;
                }

            }


            //自机电网相关
            float fluxLevel = ship.getFluxTracker().getFluxLevel();
            float remainingFluxLevel = 1f - fluxLevel;
            float fluxFractionPerUse = system.getFluxPerUse() / ship.getFluxTracker().getMaxFlux();
            float fluxLevelAfterUse = fluxLevel + fluxFractionPerUse;
            //预计幅能超出时不使用系统
            if (fluxFractionPerUse > remainingFluxLevel) return;
            if (fluxLevelAfterUse > 0.9f) return;
            //目标脆弱性判断
            /*
            boolean targetIsVulnerable = target != null && target.getFluxTracker().isOverloadedOrVenting() &&
                    (target.getFluxTracker().getOverloadTimeRemaining() > 5f ||
                            target.getFluxTracker().getTimeToVent() > 5f);

             */

            //debug
            //engine.addFloatingText(ship.getLocation(),String.valueOf(tracker.getElapsed()),10f, Color.WHITE,ship,1f,1f);
            //engine.addFloatingText(ship.getLocation(),String.valueOf(totalWeight),10f, Color.WHITE,ship,1f,1f);

            if (totalWeight >= 1f) {
                ship.useSystem();

            }


        }


    }
}
