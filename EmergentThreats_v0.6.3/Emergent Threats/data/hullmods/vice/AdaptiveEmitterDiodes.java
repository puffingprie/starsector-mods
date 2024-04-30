package data.hullmods.vice;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.listeners.DamageDealtModifier;

import data.scripts.vice.hullmods.RemnantSubsystemsUtil;

public class AdaptiveEmitterDiodes extends BaseHullMod {

	private static float BEAM_RANGE_PENALTY = 100f;
	private static float BEAM_DAMAGE_PENALTY = 10f;
	private static String FIRST_BONUS_TEXT = "hard flux";
	private static String CONFLICT_MOD_1 = "advancedoptics";
	private static String CONFLICT_MOD_2 = "high_scatter_amp";
	private static String CONFLICT_MOD_3 = "ix_laser_collimator";
	private static String OTHER_EMITTER = "vice_attuned_emitter_diodes";
	
	//Utility variables
	private RemnantSubsystemsUtil util = new RemnantSubsystemsUtil();
	
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getBeamWeaponRangeBonus().modifyFlat(id, -BEAM_RANGE_PENALTY);
		stats.getBeamWeaponDamageMult().modifyPercent(id, -BEAM_DAMAGE_PENALTY);
	}
	
	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		ship.addListener(new AdaptiveHardFlux(ship));
	}
	
	public static class AdaptiveHardFlux implements DamageDealtModifier {
		protected ShipAPI ship;
		public AdaptiveHardFlux(ShipAPI ship) {
			this.ship = ship;
		}
		
		public String modifyDamageDealt(Object param,
								   		CombatEntityAPI target, DamageAPI damage,
								   		Vector2f point, boolean shieldHit) {
			
			if (!(param instanceof DamagingProjectileAPI) && param instanceof BeamAPI) {
				damage.setForceHardFlux(true);
			}
			return null;
		}
	}

	private boolean hasEmitterModOverlap(ShipAPI ship) {
		return (ship.getVariant().getHullMods().contains(CONFLICT_MOD_1) 
				|| ship.getVariant().getHullMods().contains(CONFLICT_MOD_2)
				|| ship.getVariant().getHullMods().contains(CONFLICT_MOD_3)
				|| ship.getVariant().getHullMods().contains(OTHER_EMITTER));
	}
	
	@Override
    public boolean isApplicableToShip(ShipAPI ship) {
		if (hasEmitterModOverlap(ship)) return false;
		return (util.isApplicable(ship) && util.isOnlyRemnantMod(ship));
	}
	
	public String getUnapplicableReason(ShipAPI ship) {
		if (hasEmitterModOverlap(ship)) return "Incompatible emitter modification present";
		if (!util.isApplicable(ship)) return util.getIncompatibleCauseString("manufacturer");
		if (!util.isOnlyRemnantMod(ship)) return util.getIncompatibleCauseString("modcount");
		return null;
	}	
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) BEAM_RANGE_PENALTY;
		if (index == 1) return "" + (int) BEAM_DAMAGE_PENALTY + "%";
		if (index == 2) return FIRST_BONUS_TEXT;
		return null;
	}
}
