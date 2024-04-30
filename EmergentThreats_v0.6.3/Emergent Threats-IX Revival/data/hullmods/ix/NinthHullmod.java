package data.hullmods.ix;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

import data.hullmods.ix.DModHandler;

public class NinthHullmod extends BaseHullMod {

	private static float ZERO_FLUX_SPEED_BOOST = 5f;
	private static float SHIELD_UPGRADES = 35f;
	private static float ACCELERATION_BONUS = 15f;
	private static float CR_PENALTY = 25f;
	private static String DUPLICATE = "ae_ninth";
	
	private static String SENTINEL_S = "ix_point_defense_small";
	private static String SENTINEL_SH = "ix_point_defense_small_handler";
	private static String SENTINEL_M = "ix_point_defense_medium";
	private static String SENTINEL_MH = "ix_point_defense_medium_handler";
	private static String SMOD_TAG = "variant_always_retain_smods_on_salvage";
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getZeroFluxSpeedBoost().modifyFlat(id, ZERO_FLUX_SPEED_BOOST);
		stats.getShieldUnfoldRateMult().modifyMult(id, 1f + SHIELD_UPGRADES * 0.01f);
		stats.getShieldTurnRateMult().modifyMult(id, 1f + SHIELD_UPGRADES * 0.01f);
		stats.getAcceleration().modifyMult(id, 1f + ACCELERATION_BONUS * 0.01f);
		stats.getDeceleration().modifyMult(id, 1f + ACCELERATION_BONUS * 0.01f);
		stats.getTurnAcceleration().modifyMult(id, 1f + ACCELERATION_BONUS * 0.01f);
		stats.getCRLossPerSecondPercent().modifyMult(id, 1f + CR_PENALTY * 0.01f);
		stats.getVariant().getSMods().remove(DUPLICATE);
		stats.getVariant().getPermaMods().remove(DUPLICATE);
		stats.getVariant().getHullMods().remove(DUPLICATE);
		stats.getVariant().addTag(SMOD_TAG);
		DModHandler.clearDModsFromStrikeFleetShip(stats);
		
		//adds pd mode to Flamebreaker (IX) empty hulls sold on the market
		if (stats.getVariant().getHullSpec().getHullId().startsWith("flamebreaker_ix") 
					&& !stats.getVariant().hasHullMod(SENTINEL_S) 
					&& !stats.getVariant().hasHullMod(SENTINEL_M)) {
			stats.getVariant().addMod(SENTINEL_MH);
		}
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) ZERO_FLUX_SPEED_BOOST;
		if (index == 1) return "" + (int) SHIELD_UPGRADES + "%";
		if (index == 2) return "" + (int) ACCELERATION_BONUS + "%";
		if (index == 3) return "" + (int) CR_PENALTY + "%";
		return null;
	}
}