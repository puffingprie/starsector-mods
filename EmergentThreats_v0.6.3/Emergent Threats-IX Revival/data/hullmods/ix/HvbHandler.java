package data.hullmods.ix;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

import data.scripts.ix.PanopticonCorePlugin;

public class HvbHandler extends BaseHullMod {
	
	private static String CONFLICT_MOD = "autorepair";
	private static String TO_ADD_MOD_R = "vice_adaptive_entropy_arrester"; //radiant
	private static String TO_ADD_MOD_T = "vice_adaptive_flux_dissipator"; //tigershark
	private static String CORE_ID = "ix_panopticon_core";
	private boolean isFirst = true;
	
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getDynamic().getMod(Stats.INDIVIDUAL_SHIP_RECOVERY_MOD).modifyFlat(id, 1000f);
		stats.getBreakProb().modifyMult(id, 0f);
		
		boolean hasAdaptiveMod = false;
		for (String s : stats.getVariant().getHullMods()) {
			if (s.startsWith("vice_adaptive")) hasAdaptiveMod = true;
		}
		
		if (Global.getSettings().getModManager().isModEnabled("EmergentThreats_Vice")
					&& !hasAdaptiveMod 
					&& !stats.getVariant().hasHullMod(CONFLICT_MOD) 
					&& !stats.getVariant().hasHullMod(TO_ADD_MOD_R) 
					&& !stats.getVariant().hasHullMod(TO_ADD_MOD_T)) {
			if (HullSize.CAPITAL_SHIP.equals(hullSize)) stats.getVariant().addMod(TO_ADD_MOD_R);
			else stats.getVariant().addMod(TO_ADD_MOD_T);
		}
		
		if (stats.getFleetMember() != null 
				&& !CORE_ID.equals(stats.getFleetMember().getCaptain().getAICoreId())) {
			PersonAPI p = new PanopticonCorePlugin().createPerson(CORE_ID, "ix_battlegroup", null);
			stats.getFleetMember().setCaptain(p);
		}
			
		//delete after combat since IXEncounterListener uses hullmod to apply one time changes
		if (stats.getVariant().hasDMods()) stats.getVariant().getHullMods().remove(id);
	}
}