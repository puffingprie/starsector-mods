package data.hullmods.ix;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class TerminusRelay extends BaseHullMod {
	
	//display texts only
	private static String SHIP_NAME = "Odyssey (IX)";
	private static String DRONE_NAME = "Terminus (IX)";
	private static int DAMAGE_MISSILES_PERCENT = 100; //private static int DAMAGE_FIGHTERS_PERCENT = 100;
	private static int WEAPON_RANGE_BONUS = 300;
	
	private static int RELAY_RANGE = 800;
	private static int DAMAGE = 300;
	private static int EMP_DAMAGE = 300;
	
	private static String SENSORS = "hiressensors";
	
	@Override	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		ShipVariantAPI variant = stats.getVariant();
		if (variant == null || variant.getModuleVariant("SM 1") == null) return;
		ShipVariantAPI module = variant.getModuleVariant("SM 1");
		module.setVariantDisplayName("Drone");
		if (isSMod(stats)) module.removeMod("ix_system_inhibitor");
		else module.addMod("ix_system_inhibitor");
	}
	
	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		if (ship == null) return;
		boolean display = false;
		for (String mod : ship.getVariant().getSModdedBuiltIns()) {
			if (mod.equals(SENSORS)) display = true;
		}
		if (display) {
			String s = "Warning: By adding this hullmod, the High Resolution Sensors s-mod will be removed";
			tooltip.addPara("%s", 10f, Misc.getNegativeHighlightColor(), s);
		}
	}
	
	@Override
    public boolean isApplicableToShip(ShipAPI ship) {
		return (ship.getVariant().hasHullMod("ix_odyssey_retrofit"));
	}
	
	public String getUnapplicableReason(ShipAPI ship) {
		if (ship.getVariant().hasHullMod("ix_terminus_relay_built_in")) return "Relay is already installed";
		if (!ship.getVariant().hasHullMod("ix_odyssey_retrofit")) return "Can only be fitted to Odyssey (IX)";
		return null;
	}	
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return SHIP_NAME;
		if (index == 1) return DRONE_NAME;
		if (index == 2) return "" + DAMAGE_MISSILES_PERCENT + "%";
		if (index == 3) return "" + WEAPON_RANGE_BONUS;
		return null;
	}
	
	public String getSModDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + RELAY_RANGE;
		if (index == 1) return "" + DAMAGE;
		if (index == 2) return "" + EMP_DAMAGE;
		return null;
	}
}