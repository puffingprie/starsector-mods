package data.scripts.shipsystems;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipEngineControllerAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipEngineControllerAPI.ShipEngineAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI.SystemState;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.FindShipFilter;

public class Kayse_PhaseDisruptorStats extends BaseShipSystemScript {
    public static final Object SHIP_KEY = new Object();
    public static final Object TARGET_KEY = new Object();

    public static final float WING_EFFECT_RANGE = 200f;
    public static float DAM_MULT = 1.5f;
    protected static float RANGE = 1500f;
    public static final Color EFFECT_COLOR = new Color(165,100,255,75);
    public static Color TEXT_COLOR = new Color(165,55,255,255);
	
    public static Color JITTER_COLOR = new Color(255,50,255,75);
    public static Color JITTER_UNDER_COLOR = new Color(255,100,255,155);
	
	
    public static class TargetData {
		public ShipAPI ship;
		public ShipAPI target;
		public EveryFrameCombatPlugin targetEffectPlugin;
		public float currDamMult;
		public float elaspedAfterInState;
		public TargetData(ShipAPI ship, ShipAPI target) {
			this.ship = ship;
			this.target = target;
		}
	}
	
    @Override
    public void apply(MutableShipStatsAPI stats, final String id, State state, float effectLevel) {
        ShipAPI ship = null;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }

        final String targetDataKey = ship.getId() + "_phase_disruptor_target_data";

        Object targetDataObj = Global.getCombatEngine().getCustomData().get(targetDataKey); 
        if (state == State.IN && targetDataObj == null) {
                ShipAPI target = findTarget(ship);
                Global.getCombatEngine().getCustomData().put(targetDataKey, new TargetData(ship, target));
                if (target != null) {
                        if (target.getFluxTracker().showFloaty() || 
                                        ship == Global.getCombatEngine().getPlayerShip() ||
                                        target == Global.getCombatEngine().getPlayerShip()) {
                                target.getFluxTracker().showOverloadFloatyIfNeeded("Phase Disrupted!", TEXT_COLOR, 4f, true);
                        }
                }
        } else if (state == State.IDLE && targetDataObj != null) {
                Global.getCombatEngine().getCustomData().remove(targetDataKey);
                ((TargetData)targetDataObj).currDamMult = 1f;
                targetDataObj = null;
        }
        if (targetDataObj == null || ((TargetData) targetDataObj).target == null) return;

        final TargetData targetData = (TargetData) targetDataObj;
        targetData.currDamMult = 1f + (DAM_MULT - 1f) * effectLevel;
        if (targetData.targetEffectPlugin == null) {
            targetData.targetEffectPlugin = new BaseEveryFrameCombatPlugin() {
                    @Override
                    public void advance(float amount, List<InputEventAPI> events) {
                            if (Global.getCombatEngine().isPaused()) return;
                            if (targetData.target == Global.getCombatEngine().getPlayerShip()) { 
                                    Global.getCombatEngine().maintainStatusForPlayerShip(TARGET_KEY, 
                                                    targetData.ship.getSystem().getSpecAPI().getIconSpriteName(),
                                                    targetData.ship.getSystem().getDisplayName(), 
                                                    "Phase Coils Disabled!", true);
                            }

                            if (targetData.currDamMult <= 1f || !targetData.ship.isAlive()) {
                                
                                targetData.target.getMutableStats().getHullDamageTakenMult().unmodify(id);
                                targetData.target.getMutableStats().getArmorDamageTakenMult().unmodify(id);
                                targetData.target.getMutableStats().getShieldDamageTakenMult().unmodify(id);
                                targetData.target.getMutableStats().getEmpDamageTakenMult().unmodify(id);
                                Global.getCombatEngine().removePlugin(targetData.targetEffectPlugin);
                            } else {
                                ShipSystemAPI cloak = targetData.target.getPhaseCloak();
                                if (cloak != null){
                                    cloak.deactivate();
                                }
//                                targetData.target.getMutableStats().getHullDamageTakenMult().modifyMult(id, targetData.currDamMult);
//                                targetData.target.getMutableStats().getArmorDamageTakenMult().modifyMult(id, targetData.currDamMult);
//                                targetData.target.getMutableStats().getShieldDamageTakenMult().modifyMult(id, targetData.currDamMult);
//                                targetData.target.getMutableStats().getEmpDamageTakenMult().modifyMult(id, targetData.currDamMult);
                            }
                    }
            };
            Global.getCombatEngine().addPlugin(targetData.targetEffectPlugin);
        }


        if (effectLevel > 0) {
            if (state != State.IN) {
                targetData.elaspedAfterInState += Global.getCombatEngine().getElapsedInLastFrame();
            }
            float shipJitterLevel = 0;
            if (state == State.IN) {
                shipJitterLevel = effectLevel;
            } else {
                float durOut = 0.5f;
                shipJitterLevel = Math.max(0, durOut - targetData.elaspedAfterInState) / durOut;
            }
            float targetJitterLevel = effectLevel;

            float maxRangeBonus = 50f;
            float jitterRangeBonus = shipJitterLevel * maxRangeBonus;

            Color color = JITTER_COLOR;
            if (shipJitterLevel > 0) {
                //ship.setJitterUnder(KEY_SHIP, JITTER_UNDER_COLOR, shipJitterLevel, 21, 0f, 3f + jitterRangeBonus);
                ship.setJitter(SHIP_KEY, color, shipJitterLevel, 4, 0f, 0 + jitterRangeBonus * 1f);
            }

            if (targetJitterLevel > 0) {
                //target.setJitterUnder(KEY_TARGET, JITTER_UNDER_COLOR, targetJitterLevel, 5, 0f, 15f);
                targetData.target.setJitter(TARGET_KEY, color, targetJitterLevel, 3, 0f, 5f);
            }
        }
    }
    
    	
	

    protected Color getEffectColor(ShipAPI ship) {
        if (ship.getEngineController().getShipEngines().isEmpty()) {
            return EFFECT_COLOR;
        }
        return Misc.setAlpha(ship.getEngineController().getShipEngines().get(0).getEngineColor(), EFFECT_COLOR.getAlpha());
    }
	
	
    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {

    }
	
    protected ShipAPI findTarget(ShipAPI ship) {
        FindShipFilter filter = new FindShipFilter() {
            public boolean matches(ShipAPI ship) {
                return ship.getPhaseCloak() != null;
                //return ship.getPhaseCloak() != null && ship.getPhaseCloak().isActive();
                //return !ship.getEngineController().isFlamedOut();
            }
        };

        float range = getMaxRange(ship);
        boolean player = ship == Global.getCombatEngine().getPlayerShip();
        ShipAPI target = ship.getShipTarget();
        if (target != null) {
            float dist = Misc.getDistance(ship.getLocation(), target.getLocation());
            float radSum = ship.getCollisionRadius() + target.getCollisionRadius();
            if (dist > range + radSum) target = null;
        } else {
            if (target == null || target.getOwner() == ship.getOwner()) {
                if (player) {
                    target = Misc.findClosestShipEnemyOf(ship, ship.getMouseTarget(), HullSize.FIGHTER, range, true, filter);
                } else {
                    Object test = ship.getAIFlags().getCustom(AIFlags.MANEUVER_TARGET);
                    if (test instanceof ShipAPI) {
                        target = (ShipAPI) test;
                        float dist = Misc.getDistance(ship.getLocation(), target.getLocation());
                        float radSum = ship.getCollisionRadius() + target.getCollisionRadius();
                        if (dist > range + radSum) target = null;
                    }
                }
            }
            if (target == null) {
                target = Misc.findClosestShipEnemyOf(ship, ship.getLocation(), HullSize.FIGHTER, range, true, filter);
            }
        }
//        return target;
        if (target != null && target.getPhaseCloak() != null){
            return target;
        }
        return null;
    }
	
	
    protected float getMaxRange(ShipAPI ship) {
        return RANGE;
    }

	
    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
//		if (effectLevel > 0) {
//			if (index == 0) {
//				float damMult = 1f + (DAM_MULT - 1f) * effectLevel;
//				return new StatusData("" + (int)((damMult - 1f) * 100f) + "% more damage to target", false);
//			}
//		}
        return null;
    }


    @Override
    public String getInfoText(ShipSystemAPI system, ShipAPI ship) {
        if (system.isOutOfAmmo()) return null;
        if (system.getState() != SystemState.IDLE) return null;

        ShipAPI target = findTarget(ship);
        if (target != null && target != ship) {
            return "READY";
        }
        if (target == null && ship.getShipTarget() != null) {
            return "NO VALID TARGET";
        }
        return "NO TARGET";
    }


    @Override
    public boolean isUsable(ShipSystemAPI system, ShipAPI ship) {
        if (system.isActive()) return true;
        ShipAPI target = findTarget(ship);
        return target != null && target != ship;
    }

}








