package data.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.Misc;
import data.utils.FM_Misc;
import data.utils.I18nUtil;

import java.awt.*;
import java.util.List;

public class
FM_cursedpd extends BaseShipSystemScript {
    public static final float RANGE = 2000f;

    public static final Object KEY_SHIP = new Object();


    public static final Object KEY_TARGET = new Object();

    public static final float RANGE_MULT = 0.5f;


    public static final Color TEXT_COLOR = new Color(0, 255, 255, 255);

    public static final Color JITTER_COLOR = new Color(71, 233, 255, 109);


    public static class TargetData {
        public ShipAPI ship;
        public ShipAPI target;
        public EveryFrameCombatPlugin targetEffectPlugin;
        public float currPDRange;
        public float elaspedAfterInState;

        public TargetData(ShipAPI ship, ShipAPI target) {
            this.ship = ship;
            this.target = target;
        }
    }


    public void apply(MutableShipStatsAPI stats, final String id, State state, float effectLevel) {
        ShipAPI ship;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }

        final String targetDataKey = ship.getId() + "_cursedpd_target_data";

        if (Global.getCombatEngine() == null) return;

        Object targetDataObj = Global.getCombatEngine().getCustomData().get(targetDataKey);
        if (state == State.IN && targetDataObj == null) {
            ShipAPI target = findTarget(ship);
            Global.getCombatEngine().getCustomData().put(targetDataKey, new FM_cursedpd.TargetData(ship, target));
            if (target != null) {
                if (target.getFluxTracker().showFloaty() ||
                        ship == Global.getCombatEngine().getPlayerShip() ||
                        target == Global.getCombatEngine().getPlayerShip()) {
                    target.getFluxTracker().showOverloadFloatyIfNeeded(I18nUtil.getShipSystemString("FM_CursedPd_FloatText"), TEXT_COLOR, 4f, true);
                }
            }
        } else if (state == State.IDLE && targetDataObj != null) {
            Global.getCombatEngine().getCustomData().remove(targetDataKey);
            ((FM_cursedpd.TargetData) targetDataObj).currPDRange = 1f;
            targetDataObj = null;
        }
        if (targetDataObj == null || ((FM_cursedpd.TargetData) targetDataObj).target == null) return;

        final FM_cursedpd.TargetData targetData = (FM_cursedpd.TargetData) targetDataObj;
        targetData.currPDRange = 1f + (RANGE_MULT - 1f) * effectLevel;
        if (targetData.targetEffectPlugin == null) {
            targetData.targetEffectPlugin = new BaseEveryFrameCombatPlugin() {
                @Override
                public void advance(float amount, List<InputEventAPI> events) {
                    if (Global.getCombatEngine().isPaused()) return;
                    if (targetData.target == Global.getCombatEngine().getPlayerShip()) {
                        Global.getCombatEngine().maintainStatusForPlayerShip(KEY_TARGET,
                                targetData.ship.getSystem().getSpecAPI().getIconSpriteName(),
                                targetData.ship.getSystem().getDisplayName(),
                                (int) ((targetData.currPDRange - 1f) * 100f) + I18nUtil.getShipSystemString("FM_CursedPd_Target"), true);
                    }

                    if (targetData.currPDRange >= 1f || !targetData.ship.isAlive()) {
                        targetData.target.getMutableStats().getNonBeamPDWeaponRangeBonus().unmodify(id);
                        targetData.target.getMutableStats().getBeamPDWeaponRangeBonus().unmodify(id);

                        Global.getCombatEngine().removePlugin(targetData.targetEffectPlugin);
                    } else {

                        targetData.target.getMutableStats().getNonBeamPDWeaponRangeBonus().modifyMult(id, targetData.currPDRange);
                        targetData.target.getMutableStats().getBeamPDWeaponRangeBonus().modifyMult(id, targetData.currPDRange);

                    }
                }
            };
            Global.getCombatEngine().addPlugin(targetData.targetEffectPlugin);
        }
        if (effectLevel > 0) {
            if (state != State.IN) {
                targetData.elaspedAfterInState += Global.getCombatEngine().getElapsedInLastFrame();
            }
            float shipJitterLevel;
            if (state == State.IN) {
                shipJitterLevel = effectLevel;
            } else {
                float durOut = 0.5f;
                shipJitterLevel = Math.max(0, durOut - targetData.elaspedAfterInState) / durOut;
            }

            float maxRangeBonus = 50f;
            float jitterRangeBonus = shipJitterLevel * maxRangeBonus;

            Color color = JITTER_COLOR;
            if (shipJitterLevel > 0) {
                //ship.setJitterUnder(KEY_SHIP, JITTER_UNDER_COLOR, shipJitterLevel, 21, 0f, 3f + jitterRangeBonus);
                ship.setJitter(KEY_SHIP, color, shipJitterLevel, 10, 4f, 0 + jitterRangeBonus);
            }

            if (effectLevel > 0) {
                //target.setJitterUnder(KEY_TARGET, JITTER_UNDER_COLOR, targetJitterLevel, 5, 0f, 15f);
                targetData.target.setJitterUnder(KEY_TARGET, color, effectLevel, 10, 4f, 5f);
            }
        }


    }


    public void unapply(MutableShipStatsAPI stats, String id) {

    }

    protected ShipAPI findTarget(ShipAPI ship) {

        float range = FM_Misc.getSystemRange(ship,RANGE);

        boolean player = ship == Global.getCombatEngine().getPlayerShip();
        ShipAPI target = ship.getShipTarget();
        if (target != null) {
            float dist = Misc.getDistance(ship.getLocation(), target.getLocation());
            float radSum = ship.getCollisionRadius() + target.getCollisionRadius();
            if (dist > range + radSum || target.isFighter() || target.isDrone()) target = null;
        } else {
            if (player) {
                target = Misc.findClosestShipEnemyOf(ship, ship.getMouseTarget(), ShipAPI.HullSize.FIGHTER, range, true);
            } else {
                Object test = ship.getAIFlags().getCustom(ShipwideAIFlags.AIFlags.CARRIER_FIGHTER_TARGET);
                if (test instanceof ShipAPI && ((ShipAPI) test).getOwner() != ship.getOwner()) {
                    target = (ShipAPI) test;
                    float dist = Misc.getDistance(ship.getLocation(), target.getLocation());
                    float radSum = ship.getCollisionRadius() + target.getCollisionRadius();
                    if (dist > range + radSum) target = null;
                }
            }
            if (target == null) {
                target = Misc.findClosestShipEnemyOf(ship, ship.getLocation(), ShipAPI.HullSize.FIGHTER, range, true);
            }
        }
        return target;
    }


    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (effectLevel > 0) {
            if (index == 0) {
                float rangeMult = 1f + (RANGE_MULT - 1f) * effectLevel;
                return new StatusData((int) ((rangeMult - 1f) * 100f) + I18nUtil.getShipSystemString("FM_CursedPd_Player"), false);
            }
        }
        return null;
    }


    @Override
    public String getInfoText(ShipSystemAPI system, ShipAPI ship) {
        if (system.isOutOfAmmo()) return null;
        if (system.getState() != ShipSystemAPI.SystemState.IDLE) return null;

        ShipAPI target = findTarget(ship);
        if (target != null && target != ship) {
            return "READY";
        }
        if ((target == null) && ship.getShipTarget() != null) {
            return "OUT OF RANGE";
        }
        return "NO TARGET";
    }


    @Override
    public boolean isUsable(ShipSystemAPI system, ShipAPI ship) {
        //if (true) return true;
        ShipAPI target = findTarget(ship);
        return target != null && target != ship;
    }

}
