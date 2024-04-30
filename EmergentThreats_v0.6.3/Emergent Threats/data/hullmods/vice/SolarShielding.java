package data.hullmods.vice;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.hullmods.BaseLogisticsHullMod;

public class SolarShielding extends BaseLogisticsHullMod {

	public static float CORONA_EFFECT_MULT = 0.25f;
	public static float ENERGY_DAMAGE_MULT = 0.9f;
	public static float SMOD_CORONA_EFFECT_MULT = 0f;
	
	private static String OP_REVERTER_MOD = "vice_diktat_op_reverter";
	
	private static Map OP_REFUND = new HashMap();
	static {
		OP_REFUND.put(HullSize.FIGHTER, 0);
		OP_REFUND.put(HullSize.FRIGATE, 3);
		OP_REFUND.put(HullSize.DESTROYER, 6);
		OP_REFUND.put(HullSize.CRUISER, 9);
		OP_REFUND.put(HullSize.CAPITAL_SHIP, 15);
	}
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getEnergyDamageTakenMult().modifyMult(id, ENERGY_DAMAGE_MULT);
		stats.getEnergyShieldDamageTakenMult().modifyMult(id, ENERGY_DAMAGE_MULT);
		
		float mult = CORONA_EFFECT_MULT;
		ShipVariantAPI variant = stats.getVariant();
		if (isSMod(stats)) {
			mult = SMOD_CORONA_EFFECT_MULT;
			boolean apply = false;
			String manufacturer = variant.getHullSpec().getManufacturer();
			LinkedHashSet<String> sModBuiltIns = variant.getSModdedBuiltIns();
			for (String mod : sModBuiltIns) {
				if (mod.equals("solar_shielding")) apply = true;
			}
			if (!manufacturer.equals("Lion's Guard") && !manufacturer.equals("Sindrian Diktat")) apply = false;
			if (apply) variant.addMod(OP_REVERTER_MOD);
		}
		else variant.getHullMods().remove(OP_REVERTER_MOD);
		stats.getDynamic().getStat(Stats.CORONA_EFFECT_MULT).modifyMult(id, mult);
	}
	
	public String getSModDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) Math.round((1f - SMOD_CORONA_EFFECT_MULT) * 100f) + "%";
		if (index == 1) return "" + (Integer) OP_REFUND.get(HullSize.FRIGATE);
		if (index == 2) return "" + (Integer) OP_REFUND.get(HullSize.DESTROYER);
		if (index == 3) return "" + (Integer) OP_REFUND.get(HullSize.CRUISER);
		if (index == 4) return "" + (Integer) OP_REFUND.get(HullSize.CAPITAL_SHIP);
		return null;
	}
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) Math.round((1f - CORONA_EFFECT_MULT) * 100f) + "%";
		if (index == 1) return "" + (int) Math.round((1f - ENERGY_DAMAGE_MULT) * 100f) + "%";
		return null;
	}


}
