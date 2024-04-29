package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

import java.util.HashSet;
import java.util.Set;

public class HMISubliminalEncouragement extends BaseHullMod {
	
	public static final float CASUALTY_INCREASE = 4f;
	public static final float CR_BONUS = 10f;
	private static final Set<String> BLOCKED_HULLMODS = new HashSet<>();
	static
	{
		// These hullmods will automatically be removed
		BLOCKED_HULLMODS.add("hardened_subsystems");
	}
	private float check=0;
	private String id, ERROR="IncompatibleHullmodWarning";

	private static final Set<String> SWITCH_HULLMODS = new HashSet<>();
	static
	{
		// These hullmods will automatically be removed
		SWITCH_HULLMODS.add("automated");
	}
	private float AIcheck=0;
	private String AIid, AISWITCH="hmi_subliminal_ai";
	private String AIid2, AISWITCH2="hmi_subliminal";


	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id){

		if (check>0) {
			check-=1;
			if (check<1){
				ship.getVariant().removeMod(ERROR);
			}
		}

		for (String tmp : BLOCKED_HULLMODS) {
			if (ship.getVariant().getHullMods().contains(tmp)) {
				ship.getVariant().removeMod(tmp);
				ship.getVariant().addMod(ERROR);
				check=3;
			}
		}

		if (AIcheck>0) {
			AIcheck-=1;
			if (AIcheck<1){
				ship.getVariant().removeMod(AISWITCH);
				ship.getVariant().removeMod(AISWITCH2);
			}
		}

		for (String tmp2 : SWITCH_HULLMODS) {
			if (ship.getVariant().getHullMods().contains(tmp2)) {
				ship.getVariant().removeMod(AISWITCH2);
				ship.getVariant().addMod(AISWITCH);
				AIcheck=3;
			}
		}


	}

	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getMaxCombatReadiness().modifyFlat(id, CR_BONUS * 0.01f);
		stats.getCRLossPerSecondPercent().modifyMult(id, CASUALTY_INCREASE);
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) CR_BONUS + "%";
		if (index == 1) return "" + (int) CASUALTY_INCREASE;
		if (index == 2) return "" + "Hardened Subsystems";
		return null;
	}
}



