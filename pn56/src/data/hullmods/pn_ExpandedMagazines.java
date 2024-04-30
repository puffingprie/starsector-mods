package data.hullmods;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Random;
import com.fs.starfarer.api.combat.BaseHullMod;


public class pn_ExpandedMagazines extends BaseHullMod {

	private static final String FACTION_PREFIX = "pn_samaa-ebm";
	public static final float AMMO_BONUS = 100f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getBallisticAmmoBonus().modifyPercent(id, AMMO_BONUS);
		stats.getEnergyAmmoBonus().modifyPercent(id, AMMO_BONUS);
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) AMMO_BONUS;
		return null;
	}
    private static final Set BLOCKED_HULLMODS = new HashSet();

    private static final Random rand = new Random();

    static
    {
        // These hullmods will automatically be removed
        // Not as elegant as blocking them in the first place, but
        // this method doesn't require editing every hullmod's script
        BLOCKED_HULLMODS.add("magazines");
        BLOCKED_HULLMODS.add("extendedshieldemitter");
	
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id)
    {
        String tmp;
        for (Iterator iter = BLOCKED_HULLMODS.iterator(); iter.hasNext();)
        {
            tmp = (String) iter.next();
            if (ship.getVariant().getHullMods().contains(tmp))
            {
                ship.getVariant().removeMod(tmp);
            }
        }
    }
    @Override
    public boolean isApplicableToShip(ShipAPI ship)
    {
        return (ship.getHullSpec().getHullId().startsWith(FACTION_PREFIX));
    }
}
