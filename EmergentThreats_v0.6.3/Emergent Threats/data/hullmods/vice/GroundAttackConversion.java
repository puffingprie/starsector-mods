package data.hullmods.vice;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.hullmods.BaseLogisticsHullMod;

public class GroundAttackConversion extends BaseLogisticsHullMod {

	private static float CARGO_PENALTY = 80f;
	private static float MAX_CREW_INCREASE_SMALL = 200f;
	private static float MAX_CREW_INCREASE_LARGE = 400f;
	private static float GROUND_ATTACK_BONUS_SMALL = 50f;
	private static float GROUND_ATTACK_BONUS_LARGE = 100f;
	private static String BUFFALO = "buffalo";
	private static String BANTENG = "banteng";
	
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getCargoMod().modifyMult(id, 1f - CARGO_PENALTY * 0.01f);
		HullSize size = stats.getVariant().getHullSize();
		float crew = (size == HullSize.DESTROYER) ? MAX_CREW_INCREASE_SMALL : MAX_CREW_INCREASE_LARGE;
		float ground = (size == HullSize.DESTROYER) ? GROUND_ATTACK_BONUS_SMALL : GROUND_ATTACK_BONUS_LARGE;
		stats.getMaxCrewMod().modifyFlat(id, crew);
		stats.getDynamic().getMod(Stats.FLEET_GROUND_SUPPORT).modifyFlat(id, ground);		
	}

	@Override
    public boolean isApplicableToShip(ShipAPI ship) {
		if (!ship.getVariant().isFreighter() || !ship.getVariant().hasHullMod(HullMods.MILITARIZED_SUBSYSTEMS)) return false;
		if (ship.getVariant().getHullSpec().getHullId().contains(BUFFALO)) return true;
		if (ship.getVariant().getHullSpec().getHullId().contains(BANTENG)) return true;
		return false;
	}
	
	public String getUnapplicableReason(ShipAPI ship) {
		if (!ship.getVariant().isFreighter()) return "Incompatible hull";
		String id = ship.getVariant().getHullSpec().getHullId();
		if (!id.contains(BUFFALO) && !id.contains(BANTENG)) return "Incompatible hull";
		if (!ship.getVariant().hasHullMod(HullMods.MILITARIZED_SUBSYSTEMS)) return "Freighter lacks militarized subsystems";		
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