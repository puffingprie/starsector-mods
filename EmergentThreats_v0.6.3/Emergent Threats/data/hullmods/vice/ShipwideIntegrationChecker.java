package data.hullmods.vice;

import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;

public class ShipwideIntegrationChecker extends BaseHullMod {
	
	private static String HULLMOD_1 = "vice_abomination_interface";
	private static String HULLMOD_2 = "vice_ai_subsystem_integration";
	private static String SHIPWIDE_INTEGRATION_HULLMOD = "vice_shipwide_integration";
	
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		ShipVariantAPI variant = stats.getVariant();
		if (!variant.hasHullMod(HULLMOD_1) && !variant.hasHullMod(HULLMOD_2)) {
			if (variant.getModuleSlots() != null) {
				for (String slot : variant.getModuleSlots()) {
					if (variant.getModuleVariant(slot) != null) removeModules(variant, slot);
				}
			}
		}
	}
	
	private void removeModules(ShipVariantAPI variant, String slot) {
		ShipVariantAPI module = variant.getModuleVariant(slot);
		module.getPermaMods().remove(SHIPWIDE_INTEGRATION_HULLMOD);
		module.getHullMods().remove(SHIPWIDE_INTEGRATION_HULLMOD);
		
		List<String> modsToDelete = new ArrayList<String>();
		for (String mod : module.getHullMods()) {
			if (mod.startsWith("vice_adaptive")) modsToDelete.add(mod);
		}
		if (modsToDelete.isEmpty()) return;
		for (String mod : modsToDelete) {
			module.getPermaMods().remove(mod);
			module.getHullMods().remove(mod);
		}
	}
}