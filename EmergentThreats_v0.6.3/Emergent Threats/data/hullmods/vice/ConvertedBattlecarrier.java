package data.hullmods.vice;

import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class ConvertedBattlecarrier extends BaseHullMod {
	
	private static float FITTING_PENALTY = 10f; //text only, actual value 340 set in vice_exhortation.skin

	private static int SHUTTLE_INDEX = 3;
	private static String BUILT_IN_FIGHTERS = "Gladius (LG)";	
	private static String SHUTTLE = "Kite (LG)";
	private static String CONVERT_HULLMOD_NAME = "Convert Shuttle";
	private static String WING_ID = "vice_kite_lg_wing";
	private static String WING_ID_BACKUP = "gladius_wing";
	private static String WING_VARIANT = "vice_kite_defender";
	
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		List<String> wings = stats.getVariant().getWings();
		for (int i = 0; i < wings.size(); i++) {
			if (i == SHUTTLE_INDEX) continue;
			if (wings.get(i).equals(WING_ID)) stats.getVariant().setWingId(i, WING_ID_BACKUP);
		}
	}
	
	@Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		try {
			CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
			for (CargoStackAPI s : cargo.getStacksCopy()) {
				if (s.isFighterWingStack() && s.getFighterWingSpecIfWing().getVariantId().equals(WING_VARIANT)) cargo.removeStack(s);
			}
		}
		catch (Exception e) {}
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) FITTING_PENALTY;
		if (index == 1) return BUILT_IN_FIGHTERS;
		if (index == 2) return SHUTTLE;
		if (index == 3) return CONVERT_HULLMOD_NAME;
		return null;
	}
}