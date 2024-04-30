package data.shipsystems.scripts;
    
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

public class RamperFieldStats extends BaseShipSystemScript {

    /**
     *
     */
     public static final float INCOMING_DAMAGE_MULT = 0.10f;
     public static final float INCOMING_ARMORDAMAGE_MULT = 0.40f;
     public static final float INCOMING_EMP_NERF = 0.9f;

	
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		effectLevel = 1f;
	/*	stats.getMaxSpeed().modifyMult(id, 1f + INCOMING_BUFF_MULT);
		stats.getAcceleration().modifyMult(id, 1f + INCOMING_BUFF_MULT);
		stats.getHullDamageTakenMult().modifyMult(id, 1f - INCOMING_DAMAGE_MULT); */
		stats.getArmorDamageTakenMult().modifyMult(id, 1f - INCOMING_ARMORDAMAGE_MULT);
		stats.getEmpDamageTakenMult().modifyMult(id, 1f - INCOMING_EMP_NERF);
                

			//stats.getAcceleration().modifyPercent(id, 200f * effectLevel);
		}
		
	public void unapply(MutableShipStatsAPI stats, String id) {
		stats.getHullDamageTakenMult().unmodify(id);
		stats.getArmorDamageTakenMult().unmodify(id);
		stats.getEmpDamageTakenMult().unmodify(id);
               
		stats.getMaxSpeed().unmodify(id);
		stats.getMaxTurnRate().unmodify(id);
		stats.getTurnAcceleration().unmodify(id);
		stats.getAcceleration().unmodify(id);
		stats.getDeceleration().unmodify(id);
 
	}
	
	
        public StatusData getStatusData(int index, State state, float effectLevel) {
		if (index == 0) {
			return new StatusData("Armor Dmg -40% | Hull Dmg - 10%", false);
		}
   
               
        return null;
	}
	
	/*public StatusData getStatusData(int index, State state, float effectLevel) {
		effectLevel = 1f;
		float percent = (1f - INCOMING_DAMAGE_MULT) * effectLevel * 100;
		if (index == 0) {
			return new StatusData((int) percent + "% less damage taken", false);
			}
		return null;
	} */
}