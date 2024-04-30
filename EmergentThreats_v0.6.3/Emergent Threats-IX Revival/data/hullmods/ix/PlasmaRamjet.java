package data.hullmods.ix;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class PlasmaRamjet extends BaseHullMod {

	//dummy hullmod, this hullmod's presence allows the Tigershark (HG) bounty variant to equip the Adaptive Flux Dissipator or Adaptive Entropy Arrester subsystems, improved ship system is assigned directly to hull
	private static float DURATION_INCREASE = 33f;
	private static float SPEED_INCREASE = 20f;
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) DURATION_INCREASE + "%";
		if (index == 1) return "" + (int) SPEED_INCREASE + "%";
		return null;
	}
}