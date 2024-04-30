package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import org.apache.log4j.Logger;

public class mi_MiRepairGantry extends BaseHullMod {
    
    public static final float REPAIR_RATE_BONUS = 15f;
    public static Logger log = Global.getLogger(mi_MiRepairGantry.class);
    
    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        return ship.getHullSpec().getHullId().startsWith("mi_");
    }
    
    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) {
            return "" + (int) REPAIR_RATE_BONUS + "%";
        }
        return null;
    }
    
    @Override
    public void advanceInCampaign(FleetMemberAPI member, float amount) {
        mi_MiRepairTracker tracker = mi_MiRepairTracker.getInstance();
        if (tracker == null) {
            Global.getSector().addScript(new mi_MiRepairTracker());
            log.info("don't have a repair tracker, adding new one");
        }
    }
}
