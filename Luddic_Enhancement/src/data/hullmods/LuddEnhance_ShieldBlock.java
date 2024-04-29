package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShieldAPI.ShieldType;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static com.fs.starfarer.api.combat.ShipAPI.HullSize.FRIGATE;

public class LuddEnhance_ShieldBlock extends BaseHullMod {


    private static final Set<String> BLOCKED_HULLMODS = new HashSet<>(7);
    public static final float HEALTH_BONUS = 200f;
    public static final float REPAIR_BONUS = 100f;
    protected Object LE_CHARGES = new Object();

    static {
        BLOCKED_HULLMODS.add("frontshield");
        BLOCKED_HULLMODS.add("frontemitter");
        BLOCKED_HULLMODS.add("adaptiveshields");
        BLOCKED_HULLMODS.add("stabilizedshieldemitter");
        BLOCKED_HULLMODS.add("hardenedshieldemitter");
        BLOCKED_HULLMODS.add("advancedshieldemitter");
        BLOCKED_HULLMODS.add("extendedshieldemitter");
    }
	
	private float check=0;
	private String id, ERROR="LE_IncompatibleHullmodWarning";


    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getWeaponHealthBonus().modifyPercent(id, HEALTH_BONUS);
        stats.getEngineHealthBonus().modifyPercent(id, HEALTH_BONUS);

        stats.getCombatWeaponRepairTimeMult().modifyPercent(id, REPAIR_BONUS);
        stats.getCombatEngineRepairTimeMult().modifyPercent(id, REPAIR_BONUS);
    }


    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
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
	}

    public void advanceInCombat(ShipAPI ship, float amount) {
        ShipAPI playerShip = Global.getCombatEngine().getPlayerShip();
        String id = "LE_CHARGES";
        if (ship == playerShip) {
            Global.getCombatEngine().maintainStatusForPlayerShip(
                    LE_CHARGES,
                    "graphics/icons/hullsys/damper_field.png",
                    "Damper Field",
                    (int) ship.getPhaseCloak().getAmmo() + " Damper Charges Remaining", false);
        }
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0)  return "" + "reduces incoming damage by 80%";
        if (index == 1)  return "" + "installing a shield is impossible";
        if (index == 2)  return "" + (int) HEALTH_BONUS + "%";
        if (index == 3)  return "" + (int) REPAIR_BONUS + "%";
        return null;
    }

}
