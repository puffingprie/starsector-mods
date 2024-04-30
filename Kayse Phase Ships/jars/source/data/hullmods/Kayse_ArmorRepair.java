package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ArmorGridAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
//import data.scripts.util.MagicRender;
import org.magiclib.util.MagicRender;
import java.awt.Color;
import java.util.Random;
import org.apache.log4j.Logger;
import org.lwjgl.util.vector.Vector2f;

// Original script credit: Sundog
// Horribly hacked by: Kayse
public class Kayse_ArmorRepair extends BaseHullMod {
    
    public static Logger log = Global.getLogger(Kayse_ArmorRepair.class);

    private static final float ARMOR_REPAIR_MULTIPLIER = 300.0f/15f;
//Repairing the armor cell approximately one Shepherd's cell's worth (200 armor/15 cells) for Capital ships
    private static final float MAX_ARMOR_RECOVERED = 0.50f;
    private static final float FLUX_COST_TO_REPAIR = 20f;
    private static final float FLUX_CUTOFF = 0.5f;
    
    public static float PHASE_COOLDOWN_SLOWDOWN = 100f;
    
//    private static final float SPARK_BRIGHTNESS = 0.75f;
    private static final float REPAIR_CHANCE = 0.25f;//25% chance each tick
    //private static final Color REPAIR_COLOR = new Color(240, 64, 10);
    private static final Color REPAIR_COLOR = new Color(255,175,255);//To match vanilla phaseCloak color
//    private static final Color AFTER_COLOR = new Color(255,255,255);//To match vanilla phaseCloak color
//    private static final float SPARK_DURATION = 0.25f;
//    private static final float SPARK_MAX_RADIUS = 40f;

//    @SuppressWarnings("AssignmentToMethodParameter")
//    public static Vector2f getCellLocation(ShipAPI ship, float x, float y) {
//        x -= ship.getArmorGrid().getGrid().length / 2f;
//        y -= ship.getArmorGrid().getGrid()[0].length / 2f;
//        float cellSize = ship.getArmorGrid().getCellSize();
//        Vector2f cellLoc = new Vector2f();
//        float theta = (float) (((ship.getFacing() - 90) / 350f) * (Math.PI * 2));
//        cellLoc.x = (float) (x * Math.cos(theta) - y * Math.sin(theta)) * cellSize + ship.getLocation().x;
//        cellLoc.y = (float) (x * Math.sin(theta) + y * Math.cos(theta)) * cellSize + ship.getLocation().y;
//
//        return cellLoc;
//    }

    private final Random rand = new Random();

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        if (Global.getCombatEngine().isPaused() || !ship.isAlive()) {
            return;
        }
        
        if (ship.isPhased()||ship.getFluxTracker().isOverloadedOrVenting()){
            return;//Doesn't work while ship is phased, overloaded or venting.
        }
        
        if (ship.getFluxTracker().getFluxLevel() > FLUX_CUTOFF){
            return;
        }

        ArmorGridAPI armorGrid = ship.getArmorGrid();
        int maxX = armorGrid.getGrid().length;
        int maxY = armorGrid.getGrid()[0].length;
        int x = rand.nextInt(maxX);
        int y = -1;
        float newArmor = armorGrid.getMaxArmorInCell();
        for (int i = maxY-1; i >= 0; i-- ){//Pick random X, and check Ys from front to back until finding damaged one.
            newArmor = armorGrid.getArmorValue(x, i);
            if (Float.compare(newArmor, armorGrid.getMaxArmorInCell()*MAX_ARMOR_RECOVERED) < 0) {
                y = i;
                break;
            }
        }
        if (y == -1){
            return;
        }
        //int y = rand.nextInt(maxY);
//        armorGrid.
//        ship.
        
        float cellSize = armorGrid.getCellSize();

        if (Float.compare(newArmor, armorGrid.getMaxArmorInCell()*MAX_ARMOR_RECOVERED) >= 0) {
            return;
        }
        float shipSizeModifier = .25f;//For Frigate and Fighters
        if (ship.isCapital() || ship.getVariant().getHullSpec().getBaseHullId().contains("kayse_deathknight")) {
            //log.info("Capital: getBaseHullID was: " + ship.getVariant().getHullSpec().getBaseHullId());
            shipSizeModifier = 1f;
        }
        if (ship.isCruiser()) {
            shipSizeModifier = .75f;
        }
        if (ship.isDestroyer()) {
            shipSizeModifier = .5f;
        }
        
        
        
        float armorRepaired = ARMOR_REPAIR_MULTIPLIER * shipSizeModifier * (30f * amount);

        //newArmor += ARMOR_REPAIR_MULTIPLIER * amount * (1 - ship.getFluxTracker().getFluxLevel());//Flux based
        newArmor += armorRepaired;//Non-Flux based, normalizing amount (seconds)

        if (Math.random() < (REPAIR_CHANCE * shipSizeModifier)) {
            //Only heal if there is a repair decal 
            armorGrid.setArmorValue(x, y, Math.min(armorGrid.getMaxArmorInCell()*MAX_ARMOR_RECOVERED, newArmor));
            float excessRepair = armorGrid.getMaxArmorInCell()*MAX_ARMOR_RECOVERED - newArmor;
            armorRepaired -= excessRepair;
            if (armorRepaired < 2){
                return;//Scratch damage, don't worry about displaying or costing flux
            }
            float fluxCost = armorRepaired * FLUX_COST_TO_REPAIR;
            ship.getFluxTracker().increaseFlux(fluxCost, true);
//            Vector2f cellLoc = getCellLocation(ship, x, y);
//            cellLoc.x += cellSize * 0.5f - cellSize * (float) Math.random();
//            cellLoc.y += cellSize * 0.5f - cellSize * (float) Math.random();
            MagicRender.objectspace(Global.getSettings().getSprite("fx","kayse_triangle"),
                    ship,
                    //cellLoc,
                    new Vector2f((y - armorGrid.getGrid()[0].length / 2f) * armorGrid.getCellSize()+8f, (-x+ armorGrid.getGrid().length / 2f) * armorGrid.getCellSize()+8f),
                    new Vector2f(0,0),// ship.getVelocity(),
                    new Vector2f(16,16),//size
                    new Vector2f(16,16),//growth
                    (float)Math.random()*40+160,//angle
                    0f,//(float)(Math.random()-0.5f)*10,
                    true,//Parent
                    REPAIR_COLOR,//Color
                    true,//Additive
                    16f,//JitterRange
                    0f,//JitterTilt
                    0f,//Flicker Range
                    0f,//Flicker Median
                    0.1f,//MaxDelay
                    0.2f,//Fade In
                    0.2f+(float)Math.random()*0.5f,//Full
                    0.2f,//Fade out
                    false,
                    CombatEngineLayers.BELOW_INDICATORS_LAYER
                    );
//            Global.getCombatEngine().addSmoothParticle(
//                    cellLoc,
//                    ship.getVelocity(),
//                    SPARK_MAX_RADIUS * (float) (Math.random() + .25f),
//                    SPARK_BRIGHTNESS,
//                    SPARK_DURATION,
//                    REPAIR_COLOR);
//            //Global.getCombatEngine().addHitParticle(
//            Global.getCombatEngine().addSmoothParticle(
//                    cellLoc,
//                    ship.getVelocity(),
//                    SPARK_MAX_RADIUS * .25f *(float) Math.random(),
//                    SPARK_BRIGHTNESS * .25f,
//                    SPARK_DURATION*2,
//                    AFTER_COLOR);
        }
    }
    
    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getPhaseCloakCooldownBonus().modifyMult(id, 1f + PHASE_COOLDOWN_SLOWDOWN / 100f);
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return "" + (int) 20;//20 seconds is a wag.
        if (index == 1) return "" + (int) Math.round(MAX_ARMOR_RECOVERED * 100f) + "%";
        if (index == 2) return "" + (int) Math.round(PHASE_COOLDOWN_SLOWDOWN) + "%";
        if (index == 3) return "" + (int) Math.round(FLUX_COST_TO_REPAIR);
        if (index == 4) return "" + (int) 85 + "%";
        if (index == 5) return "" + (float) (FLUX_COST_TO_REPAIR*.15);
        if (index == 6) return "" + (int) Math.round(FLUX_CUTOFF * 100f) + "%";
        return null;
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        if (! ship.getVariant().hasHullMod("phasefield"))
            return false;
        if (ship.getVariant().hasHullMod("kayse_superiorarmorrepair"))
            return false;
        return super.isApplicableToShip(ship);
    }

    @Override
    public String getUnapplicableReason(ShipAPI ship) {
        if (! ship.getVariant().hasHullMod("phasefield"))
            return "Requires Phase Field";
        if (ship.getVariant().hasHullMod("kayse_superiorarmorrepair"))
            return "Cannot install two armor repair systems";
        return super.getUnapplicableReason(ship);
    }

}
