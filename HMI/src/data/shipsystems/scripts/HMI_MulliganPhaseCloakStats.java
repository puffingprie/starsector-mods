package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.PhaseCloakSystemAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import static com.fs.starfarer.api.impl.combat.PhaseCloakStats.MAX_TIME_MULT;

import com.fs.starfarer.api.util.IntervalUtil;
import java.awt.Color;
import org.lwjgl.util.vector.Vector2f;

public class HMI_MulliganPhaseCloakStats extends BaseShipSystemScript {

	private static float MAX_TIME_MULT = 3f;
	private final Object STATUSKEY1 = new Object();
    private final Object STATUSKEY2 = new Object();
	private final Object STATUSKEY3 = new Object();
	private final Object STATUSKEY4 = new Object();
    private final Object JUTTER1 = new Object();
    private final Object JUTTER2 = new Object();
    private final float SHIP_ALPHA_MULT = 0.2f;
    private final IntervalUtil interval = new IntervalUtil(0.1f, 0.1f);
	public static float VULNERABLE_FRACTION = 0f;

	public static boolean FLUX_LEVEL_AFFECTS_SPEED = true;
	public static float MIN_SPEED_MULT = 0.33f;
	public static float BASE_FLUX_LEVEL_FOR_MIN_SPEED = 0.5f;
	
	public static float getMaxTimeMult(MutableShipStatsAPI stats) {
		return 1f + (MAX_TIME_MULT - 1f) * stats.getDynamic().getValue(Stats.PHASE_TIME_BONUS_MULT);
	}
	
	protected boolean isDisruptable(ShipSystemAPI cloak) {
		return cloak.getSpecAPI().hasTag(Tags.DISRUPTABLE);
	}
	
	protected float getDisruptionLevel(ShipAPI ship) {
		//return disruptionLevel;
		//if (true) return 0f;
		if (FLUX_LEVEL_AFFECTS_SPEED) {
			float threshold = ship.getMutableStats().getDynamic().getMod(
					Stats.PHASE_CLOAK_FLUX_LEVEL_FOR_MIN_SPEED_MOD).computeEffective(BASE_FLUX_LEVEL_FOR_MIN_SPEED);
			if (threshold <= 0) return 1f;
			float level = ship.getHardFluxLevel() / threshold;
			if (level > 1f) level = 1f;
			return level;
		}
		return 0f;
	}

		protected void maintainStatus(ShipAPI playerShip, State state, float effectLevel) {
		float level = effectLevel;
		float f = VULNERABLE_FRACTION;
		
		ShipSystemAPI cloak = playerShip.getPhaseCloak();
		if (cloak == null) cloak = playerShip.getSystem();
		if (cloak == null) return;
		
		if (level > f) {
//			Global.getCombatEngine().maintainStatusForPlayerShip(STATUSKEY1,
//					cloak.getSpecAPI().getIconSpriteName(), cloak.getDisplayName(), "can not be hit", false);
			Global.getCombatEngine().maintainStatusForPlayerShip(STATUSKEY2,
					cloak.getSpecAPI().getIconSpriteName(), cloak.getDisplayName(), "time flow altered", false);
		} else {
//			float INCOMING_DAMAGE_MULT = 0.25f;
//			float percent = (1f - INCOMING_DAMAGE_MULT) * getEffectLevel() * 100;
//			Global.getCombatEngine().maintainStatusForPlayerShip(STATUSKEY3,
//					spec.getIconSpriteName(), cloak.getDisplayName(), "damage mitigated by " + (int) percent + "%", false);
		}
		
		if (FLUX_LEVEL_AFFECTS_SPEED) {
			if (level > f) {
				if (getDisruptionLevel(playerShip) <= 0f) {
					Global.getCombatEngine().maintainStatusForPlayerShip(STATUSKEY3,
							cloak.getSpecAPI().getIconSpriteName(), "phase coils stable", "top speed at 100%", false);
				} else {
					//String disruptPercent = "" + (int)Math.round((1f - disruptionLevel) * 100f) + "%";
					//String speedMultStr = Strings.X + Misc.getRoundedValue(getSpeedMult());
					String speedPercentStr = (int) Math.round(getSpeedMult(playerShip, effectLevel) * 100f) + "%";
					Global.getCombatEngine().maintainStatusForPlayerShip(STATUSKEY3,
							cloak.getSpecAPI().getIconSpriteName(),
							//"phase coils at " + disruptPercent, 
							"phase coil stress", 
							"top speed at " + speedPercentStr, true);
				}
			}
		}
	}
	
//	protected float disruptionLevel = 0f;
//	//protected Set<CombatEntityAPI> hitBy = new LinkedHashSet<CombatEntityAPI>();
//	protected TimeoutTracker<Object> hitBy = new TimeoutTracker<Object>();
//	protected float sinceHit = 1000f;
	
	public float getSpeedMult(ShipAPI ship, float effectLevel) {
		if (getDisruptionLevel(ship) <= 0f) return 1f;
		return MIN_SPEED_MULT + (1f - MIN_SPEED_MULT) * (1f - getDisruptionLevel(ship) * effectLevel); 
	}
	
	
    @Override
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		ShipAPI ship = null;
		boolean player = false;
		if (stats.getEntity() instanceof ShipAPI) {
			ship = (ShipAPI) stats.getEntity();
			player = ship == Global.getCombatEngine().getPlayerShip();
			id = id + "_" + ship.getId();
		} else {
			return;
		}

        if (player) {
            maintainStatus(ship, state, effectLevel);
        }
        if (Global.getCombatEngine().isPaused()) {
            return;
        }
		
		ShipSystemAPI cloak = ship.getPhaseCloak();
		if (cloak == null) cloak = ship.getSystem();
		if (cloak == null) return;
		
		if (FLUX_LEVEL_AFFECTS_SPEED) {
			if (state == State.ACTIVE || state == State.OUT || state == State.IN) {
				float mult = getSpeedMult(ship, effectLevel);
				if (mult < 1f) {
					stats.getMaxSpeed().modifyMult(id + "_2", mult);
				} else {
					stats.getMaxSpeed().unmodifyMult(id + "_2");
				}
				((PhaseCloakSystemAPI)cloak).setMinCoilJitterLevel(getDisruptionLevel(ship));
			}
		}
		
        if (state == State.IDLE || state == State.COOLDOWN) {
            unapply(stats, id);
            return;
        }
		
		
		float speedPercentMod = stats.getDynamic().getMod(Stats.PHASE_CLOAK_SPEED_MOD).computeEffective(0f);
		float accelPercentMod = stats.getDynamic().getMod(Stats.PHASE_CLOAK_ACCEL_MOD).computeEffective(0f);
		stats.getMaxSpeed().modifyPercent(id, speedPercentMod * effectLevel);
		stats.getAcceleration().modifyPercent(id, accelPercentMod * effectLevel);
		stats.getDeceleration().modifyPercent(id, accelPercentMod * effectLevel);
		
		float speedMultMod = stats.getDynamic().getMod(Stats.PHASE_CLOAK_SPEED_MOD).getMult();
		float accelMultMod = stats.getDynamic().getMod(Stats.PHASE_CLOAK_ACCEL_MOD).getMult();
		stats.getMaxSpeed().modifyMult(id, speedMultMod * effectLevel);
		stats.getAcceleration().modifyMult(id, accelMultMod * effectLevel);
		stats.getDeceleration().modifyMult(id, accelMultMod * effectLevel);
		
		float level = effectLevel;
//        if (state == State.COOLDOWN) {
//            ship.setPhased(false);
//        }


        ship.setPhased(true);

        ship.setExtraAlphaMult(1f - (1f - SHIP_ALPHA_MULT) * effectLevel);
		
        ship.setApplyExtraAlphaToEngines(true);
        float shipTimeMult;
        float maxTimeMult = getMaxTimeMult(stats);
        if (state == State.ACTIVE || state == State.IN) {
            shipTimeMult = 1f + (maxTimeMult - 1f) * effectLevel;
            ship.getMutableStats().getFighterRefitTimeMult().modifyMult(id, 1/shipTimeMult);
        } else {
            float cooldown = cloak.getCooldownRemaining();
            float cooldownLevel = cooldown / cloak.getCooldown();
            shipTimeMult = Math.min(maxTimeMult, 1f + (maxTimeMult - 1f) * cooldownLevel);
            ship.getMutableStats().getFighterRefitTimeMult().unmodify(id);
        }
        stats.getTimeMult().modifyMult(id, shipTimeMult);
        if (player) {
            Global.getCombatEngine().getTimeMult().modifyMult(id, 1f / shipTimeMult);
        } else {
            Global.getCombatEngine().getTimeMult().unmodify(id);
        }
        ship.setApplyExtraAlphaToEngines(true);
		
        //VFX
//        interval.advance(Global.getCombatEngine().getElapsedInLastFrame());
//        int alpha = 100 - (int)(80 * effectLevel);
//        ship.setJitter(JUTTER1 + ship.toString(), new Color(255, 175, 255, alpha), 0.2f, 1, 20);
//        ship.setJitterUnder(JUTTER1 + ship.toString(), new Color(255, 175, 255, alpha), 0.2f, 1, 20);
//        if (interval.intervalElapsed()) {

//            if (state == State.OUT || state == State.IN || state == State.ACTIVE) {
//                            int alpha = (int) (50 + effectLevel * 100);
//                Vector2f random1 = MathUtils.getRandomPointInCircle(null, 20f);
//                ship.addAfterimage(new Color(255, 175, 255, alpha), random1.x, random1.y, random1.x, random1.y, 10, 0.1f, 0.2f, 0.2f, false, false, true);
//                ship.addAfterimage(new Color(255, 175, 255, alpha), random1.x, random1.y, random1.x, random1.y, 10, 0.1f, 0.2f, 0.2f, false, false, false);
//            }
//        }
//        if (state != State.ACTIVE) {

//            float jitterLevel = effectLevel;
//            jitterLevel = (float) Math.sqrt(jitterLevel);
//           ship.setJitter(JUTTER2 + ship.toString(), new Color(255, 0, 0, 100), jitterLevel, 2, 3);
//            ship.setJitterUnder(JUTTER2 + ship.toString(), new Color(73, 227, 68, 100), jitterLevel, 10, 7);
//            Global.getSoundPlayer().playLoop("system_temporalshell_loop", STATUSKEY1, 1, 1, ship.getLocation(), new Vector2f(0, 0));
//        }
//        int alpha2 = 45;
//        float duration = effectLevel * 0.3f;
//        Vector2f vel = ship.getVelocity();
//        duration *= ship.getVelocity().length() / ship.getMaxSpeedWithoutBoost();
//        duration *= duration;
//        float length = -1.5f;
//        ship.addAfterimage(new Color(255, 175, 255, alpha), 0, 0, vel.getX() * length, vel.getY() * length, 1, 0, duration, duration, true, true, true);
//        if (!ship.getEngineController().isFlamedOut() && !ship.getEngineController().isFlamingOut()) {
//            ship.addAfterimage(new Color(187, 255, 175, alpha2), 0, 0, vel.getX() * length, vel.getY() * length, 3, 0, duration, duration, false, true, false);
//        }
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        ShipAPI ship = null;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }
		stats.getMaxSpeed().unmodify(id);
		stats.getMaxSpeed().unmodifyMult(id + "_2");
		stats.getAcceleration().unmodify(id);
		stats.getDeceleration().unmodify(id);
		
        Global.getCombatEngine().getTimeMult().unmodify(id);
        stats.getTimeMult().unmodify(id);
		
        ship.setPhased(false);
        ship.setExtraAlphaMult(1f);
		
		ShipSystemAPI cloak = ship.getPhaseCloak();
		if (cloak == null) cloak = ship.getSystem();
		if (cloak != null) {
			((PhaseCloakSystemAPI)cloak).setMinCoilJitterLevel(0f);
		}
		
    }

    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        return null;
    }

}
