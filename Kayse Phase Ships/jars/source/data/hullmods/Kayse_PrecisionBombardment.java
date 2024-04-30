package data.hullmods;

import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.hullmods.BaseLogisticsHullMod;

public class Kayse_PrecisionBombardment extends BaseLogisticsHullMod {

	public final Map<HullSize, Float> mag = new HashMap<>();
	{
		mag.put(HullSize.FRIGATE, 50f);
		mag.put(HullSize.DESTROYER, 100f);
		mag.put(HullSize.CRUISER, 200f);
		mag.put(HullSize.CAPITAL_SHIP, 300f);
	}
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		//stats.getDynamic().getMod(Stats.FLEET_GROUND_SUPPORT).modifyFlat(id, mag.get(hullSize));
		stats.getDynamic().getMod(Stats.FLEET_BOMBARD_COST_REDUCTION).modifyFlat(id, mag.get(hullSize));
	}

	@Override
	public boolean isApplicableToShip(ShipAPI ship) {
		if (ship.getVariant().hasHullMod("ground_support") || ship.getVariant().hasHullMod("advanced_ground_support") || ship.getVariant().hasHullMod("MHMods_BombardmentPackage"))
			return false;
		return super.isApplicableToShip(ship);
	}

	@Override
	public String getUnapplicableReason(ShipAPI ship) {
		if (ship.getVariant().hasHullMod("ground_support") || ship.getVariant().hasHullMod("advanced_ground_support") || ship.getVariant().hasHullMod("MHMods_BombardmentPackage"))
			return "Incompatible with Ground Support or Bombardment mods";
		return super.getUnapplicableReason(ship);
	}

	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (mag.get(HullSize.FRIGATE)).intValue();
		if (index == 1) return "" + (mag.get(HullSize.DESTROYER)).intValue();
		if (index == 2) return "" + (mag.get(HullSize.CRUISER)).intValue();
		if (index == 3) return "" + (mag.get(HullSize.CAPITAL_SHIP)).intValue();
		return null;
	}
}




