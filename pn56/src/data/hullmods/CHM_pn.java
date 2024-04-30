package data.hullmods;

import java.util.HashMap;
import java.util.Map;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;



public class CHM_pn extends BaseHullMod {
    public static final float DISSIPATION_BONUS = 5f;	
    public static float SHIELD_BONUS = 5f;
    public static final float SHIELD_UPKEEP_BONUS = 5f;
    
    private final boolean runOnce=false;
    private final float maxRange=0;

//this modifyFlat basically says it gives a flat 5% hard flux dissipation without referencing the "DISSIPATION_BONUS" stated above. If it would say modifyMult like the others it would have to reference the "DISSIPATION_BONUS" and it would function as a multiple.
    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getHardFluxDissipationFraction().modifyFlat(id,0.05f);
        stats.getShieldDamageTakenMult().modifyMult(id, 1f - SHIELD_BONUS * 0.01f);
        stats.getShieldUpkeepMult().modifyMult(id, 1f - SHIELD_UPKEEP_BONUS * 0.01f);
    }
        
      	public String getDescriptionParam(int index, HullSize hullSize) {
            if (index == 0) return "" + (int) DISSIPATION_BONUS + "%";
            if (index == 1) return "" + (int) SHIELD_BONUS + "%";
            if (index == 3) return "" + (int) SHIELD_UPKEEP_BONUS + "%";
		return null;
	}

	public boolean isApplicableToShip(ShipAPI ship) {
		return ship != null && ship.getShield() != null;
	}
	
	public String getUnapplicableReason(ShipAPI ship) {
		return "Ship has no shields";
	}
      

}