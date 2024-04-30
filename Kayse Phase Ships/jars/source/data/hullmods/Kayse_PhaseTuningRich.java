package data.hullmods;

import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
//import com.fs.starfarer.api.impl.hullmods.BaseLogisticsHullMod;
import com.fs.starfarer.api.combat.BaseHullMod;

public class Kayse_PhaseTuningRich extends BaseHullMod {
    
    float timeBonusEffect = 10f;
    float cloakSpeedEffect = 10f;

	public final Map<HullSize, Float> mag = new HashMap<>();
	{
		mag.put(HullSize.FRIGATE, 50f);
		mag.put(HullSize.DESTROYER, 100f);
		mag.put(HullSize.CRUISER, 200f);
		mag.put(HullSize.CAPITAL_SHIP, 300f);
	}
	
        @Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		//stats.getDynamic().getMod(Stats.FLEET_GROUND_SUPPORT).modifyFlat(id, mag.get(hullSize));
		//stats.getDynamic().getMod(Stats.FLEET_BOMBARD_COST_REDUCTION).modifyFlat(id, mag.get(hullSize));
                stats.getDynamic().getStat(Stats.PHASE_TIME_BONUS_MULT).modifyMult(id, (float) (1+ timeBonusEffect*.01));
                //stats.getDynamic().getStat(Stats.PHASE_CLOAK_SPEED_MOD).modifyMult(id, cloakSpeedEffect);
                        //.modifyFlat(id, mag.get(hullSize));
	}

	@Override
	public boolean isApplicableToShip(ShipAPI ship) {
                if (! ship.getVariant().hasHullMod("phasefield"))
                        return false;
		if (ship.getVariant().hasHullMod("kayse_phasetuninglean") || ship.getVariant().hasHullMod("phasecoilinstability") || ship.getVariant().hasHullMod("ex_phase_coils"))
			return false;
		return super.isApplicableToShip(ship);
	}

	@Override
	public String getUnapplicableReason(ShipAPI ship) {
                if (! ship.getVariant().hasHullMod("phasefield"))
                        return "Requires Phase Coils and Phase Field";
		if (ship.getVariant().hasHullMod("kayse_phasetuninglean") || ship.getVariant().hasHullMod("phasecoilinstability") || ship.getVariant().hasHullMod("ex_phase_coils"))
			return "Incompatible with other Phase Coil modules";
		return super.getUnapplicableReason(ship);
	}

        @Override
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) timeBonusEffect + "%";
		if (index == 1) return "" + cloakSpeedEffect;
//		if (index == 2) return "" + (mag.get(HullSize.CRUISER)).intValue();
//		if (index == 3) return "" + (mag.get(HullSize.CAPITAL_SHIP)).intValue();;;
		return null;
	}
}




