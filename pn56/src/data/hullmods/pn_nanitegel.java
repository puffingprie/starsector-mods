package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ArmorGridAPI;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Random;
import org.lwjgl.util.vector.Vector2f;
import com.fs.starfarer.api.combat.BaseHullMod;

// script credit: Sundog


public class pn_nanitegel extends BaseHullMod
{
    private static final String FACTION_PREFIX = "pn_trundler";
    private static final Set BLOCKED_HULLMODS = new HashSet();
    private static final float ARMOR_REPAIR_MULTIPLIER = 1000.0f;
    //private static final float PERCENT_HEALED_PER_SECOND = 0.6f;

    private static final Random rand = new Random();

    static
    {
        // These hullmods will automatically be removed
        // Not as elegant as blocking them in the first place, but
        // this method doesn't require editing every hullmod's script
        BLOCKED_HULLMODS.add("advancedshieldemitter");
        BLOCKED_HULLMODS.add("extendedshieldemitter");
        BLOCKED_HULLMODS.add("frontemitter");
        BLOCKED_HULLMODS.add("frontshield");
        BLOCKED_HULLMODS.add("hardenedshieldemitter");
        BLOCKED_HULLMODS.add("adaptiveshields");
        BLOCKED_HULLMODS.add("stabilizedshieldemitter");		
    }
	
    public static Vector2f getCellLocation(ShipAPI ship, float x, float y) {
        x -= ship.getArmorGrid().getGrid().length / 2f;
        y -= ship.getArmorGrid().getGrid()[0].length / 2f;
        float cellSize = ship.getArmorGrid().getCellSize();
        Vector2f cellLoc = new Vector2f();
        float theta = (float)(((ship.getFacing() - 90) / 350f) * (Math.PI * 2));
        cellLoc.x = (float)(x * Math.cos(theta) - y * Math.sin(theta)) * cellSize + ship.getLocation().x;
        cellLoc.y = (float)(x * Math.sin(theta) + y * Math.cos(theta)) * cellSize + ship.getLocation().y;

        return cellLoc;
    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        super.advanceInCombat(ship, amount);
        
        if(Global.getCombatEngine().isPaused()) return;

        ArmorGridAPI armorGrid = ship.getArmorGrid();
        int x = rand.nextInt(armorGrid.getGrid().length);
        int y = rand.nextInt(armorGrid.getGrid()[0].length);
        float newArmor = armorGrid.getArmorValue(x, y);
        float cellSize = armorGrid.getCellSize();

        if(newArmor == armorGrid.getMaxArmorInCell()) return;

        newArmor += ARMOR_REPAIR_MULTIPLIER * amount * (1 - ship.getFluxTracker().getFluxLevel());
        armorGrid.setArmorValue(x, y, Math.min(armorGrid.getMaxArmorInCell(), newArmor));
        
    }
	public static final float BONUS_PERCENT = 15f;
	public static final float NEG_PERCENT = 20f;	

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize,
            MutableShipStatsAPI stats, String id)
    {
		stats.getBallisticWeaponRangeBonus().modifyPercent(id, -NEG_PERCENT);
		stats.getEnergyWeaponRangeBonus().modifyPercent(id, -NEG_PERCENT);

//        stats.getHullCombatRepairRatePercentPerSecond().modifyFlat(id,
//                PERCENT_HEALED_PER_SECOND);
//
//        // Compensate for other systems that heal over time (basically, don't heal over 100%)
//        float hullRepairFractionBonus = 1f
//                - stats.getMaxCombatHullRepairFraction().getModifiedValue();
//        // Ignore this system's effect in the calculation
//        StatMod tmp = stats.getMaxCombatHullRepairFraction().getFlatStatMod(id);
//        if (tmp != null)
//        {
//            hullRepairFractionBonus += tmp.getValue();
//        }
//        stats.getMaxCombatHullRepairFraction().modifyFlat(id,
//                hullRepairFractionBonus);
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
    public String getDescriptionParam(int index, HullSize hullSize)
    {
//        if (index == 0)
//        {
//            return "" + PERCENT_HEALED_PER_SECOND;
//        }

        return null;
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship)
    {
        return (ship.getHullSpec().getHullId().startsWith(FACTION_PREFIX));
    }

}