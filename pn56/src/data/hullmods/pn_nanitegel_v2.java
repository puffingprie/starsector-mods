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

import com.fs.starfarer.api.impl.campaign.ids.Stats;

import java.util.*;
// script credit: Sundog & Sinosauropteryx


public class pn_nanitegel_v2 extends BaseHullMod
{
    private static final String FACTION_PREFIX = "pn_trundler";
    private static final Set BLOCKED_HULLMODS = new HashSet();
    private static final float ARMOR_REPAIR_MULTIPLIER = 1000.0f;
    //private static final float PERCENT_HEALED_PER_SECOND = 0.6f;

    private static final Random rand = new Random();

    
//This part is taken from Sinosauropteryxes BlastDamperners, basically the parameters for resistance and for how long it should last            
    public static final float RESISTANCE = 0.1f;
    public static final float RESIST_TIME = 0.1f; //in seconds

    private Set<ShipAPI> nearbyShips = new HashSet<>();
    private float resisting = 0f;
//To here        
    
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
    
        
        
//This part is taken from Sinosauropteryxes BlastDamperners        
        if (Global.getCombatEngine() == null)
            return;


        List<ShipAPI> shipPlusModules = ship.getChildModulesCopy();
        shipPlusModules.add(ship);

        resisting -= amount;
        for (ShipAPI s : Global.getCombatEngine().getShips()){
            if (s != ship && s.getHullSize() != HullSize.FIGHTER && s.isAlive()){
                float distance = Vector2f.sub(ship.getLocation(),s.getLocation(),new Vector2f()).length() - ship.getCollisionRadius();
                for (ShipAPI child : ship.getChildModulesCopy()){
                    float newDistance = Vector2f.sub(child.getLocation(),s.getLocation(),new Vector2f()).length() - child.getCollisionRadius();
                    distance = Math.min(distance,newDistance);
                }
                float mult = s.getMutableStats().getDynamic().getValue(Stats.EXPLOSION_RADIUS_MULT);
                float radius = s.getCollisionRadius() + Math.min(200f, s.getCollisionRadius()) * mult;
                if (distance <= radius){
                    nearbyShips.add(s);
                } else {
                    nearbyShips.remove(s);
                }
            }
        }
        Iterator<ShipAPI> iter = nearbyShips.iterator();
        while (iter.hasNext()){
            ShipAPI t = iter.next();
            if (t == null || !t.isAlive()){
                iter.remove();
                for (ShipAPI s : shipPlusModules) {
                    s.getMutableStats().getHighExplosiveDamageTakenMult().modifyMult("pn_nanitegel_v2", RESISTANCE);
                    s.getMutableStats().getHighExplosiveShieldDamageTakenMult().modifyMult("pn_nanitegel_v2", RESISTANCE);
                }
                resisting = RESIST_TIME;
            }
        }
        if (resisting <= 0f){
            for (ShipAPI s : shipPlusModules) {
                s.getMutableStats().getHighExplosiveDamageTakenMult().unmodify("pn_nanitegel_v2");
                s.getMutableStats().getHighExplosiveShieldDamageTakenMult().unmodify("pn_nanitegel_v2");
            }
        }
    //Down to here is from Sinosauropteryx BlastDampeners    
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