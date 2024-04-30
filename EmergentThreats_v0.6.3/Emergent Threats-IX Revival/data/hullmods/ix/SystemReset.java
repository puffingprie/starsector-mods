package data.hullmods.ix;

import java.util.LinkedHashSet;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class SystemReset extends BaseHullMod {

	private static String EFFECT = "Removes all s-mods";
	private static String HANDLER = "ix_smod_handler";
	
    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		LinkedHashSet<String> sMods = stats.getVariant().getSMods();
		LinkedHashSet<String> toDelete = new LinkedHashSet<String>();
		for (String mod : sMods) {
			toDelete.add(mod);
		}
		for (String mod : toDelete) {
			stats.getVariant().getSMods().remove(mod);
			stats.getVariant().getPermaMods().remove(mod);
			stats.getVariant().getHullMods().remove(mod);
		}
		stats.getVariant().getPermaMods().remove(HANDLER);
		stats.getVariant().getHullMods().remove(HANDLER);
		stats.getVariant().getPermaMods().remove(id);
		stats.getVariant().getHullMods().remove(id);
    }
	
	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		if (!isApplicableToShip(ship)) return;
		String s = "Warning: s-mods will be removed immediately upon applying this hullmod";
		tooltip.addPara("%s", 10f, Misc.getNegativeHighlightColor(), s);
	}
	
	private boolean hasSMods(ShipAPI ship) {
		LinkedHashSet<String> sMods = ship.getMutableStats().getVariant().getSMods();
		return (!sMods.isEmpty());
	}
	
	@Override
    public boolean isApplicableToShip(ShipAPI ship) {
		if (!hasSMods(ship)) return false;
		return (ship.getVariant().getHullMods().contains(HANDLER));
	}
	
	public String getUnapplicableReason(ShipAPI ship) {
		if (!hasSMods(ship)) return "Ship has no s-mods";
		if (!ship.getVariant().getHullMods().contains(HANDLER)) return "Reset device is missing from hull";
		return null;
	}	
	
	@Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return EFFECT;
        return null;
    }
}