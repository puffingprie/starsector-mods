package data.hullmods.ix;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.hullmods.BaseLogisticsHullMod;

public class GroundInvasionConversion extends BaseLogisticsHullMod {

	private static float CARGO_PENALTY = 80f;
	private static float MAX_CREW_INCREASE_SMALL = 200f;
	private static float MAX_CREW_INCREASE_LARGE = 400f;
	private static float GROUND_ATTACK_BONUS_SMALL = 75f;
	private static float GROUND_ATTACK_BONUS_LARGE = 150f;
	private static String BUFFALO = "buffalo_ix";
	private static String BANTENG = "banteng_ix";
	private static String CONFLICT_MOD = "vice_ground_attack_conversion";
	
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		ShipVariantAPI variant = stats.getVariant();
		if (variant.hasHullMod(CONFLICT_MOD)) variant.getHullMods().remove(CONFLICT_MOD);
		stats.getCargoMod().modifyMult(id, 1f - CARGO_PENALTY * 0.01f);
		HullSize size = stats.getVariant().getHullSize();
		float crew = (size == HullSize.DESTROYER) ? MAX_CREW_INCREASE_SMALL : MAX_CREW_INCREASE_LARGE;
		float ground = (size == HullSize.DESTROYER) ? GROUND_ATTACK_BONUS_SMALL : GROUND_ATTACK_BONUS_LARGE;
		stats.getMaxCrewMod().modifyFlat(id, crew);
		stats.getDynamic().getMod(Stats.FLEET_GROUND_SUPPORT).modifyFlat(id, ground);		
	}

	@Override
    public boolean isApplicableToShip(ShipAPI ship) {
		if (ship.getVariant().hasHullMod(CONFLICT_MOD)) return false;
		String id = ship.getVariant().getHullSpec().getHullId();
		if (id.equals(BUFFALO) || id.equals(BANTENG)) return true;
		return false;
	}
	
	public String getUnapplicableReason(ShipAPI ship) {
		String id = ship.getVariant().getHullSpec().getHullId();
		if (!id.equals(BUFFALO) && !id.equals(BANTENG)) return "Incompatible hull";
		if (ship.getVariant().hasHullMod(CONFLICT_MOD)) return "Ship is already modified for ground attack";
		return null;
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) CARGO_PENALTY + "%";
		if (index == 1) return "" + (int) MAX_CREW_INCREASE_SMALL;
		if (index == 2) return "" + (int) GROUND_ATTACK_BONUS_SMALL;
		if (index == 3) return "" + (int) MAX_CREW_INCREASE_LARGE;
		if (index == 4) return "" + (int) GROUND_ATTACK_BONUS_LARGE;
		
		return null;
	}
}