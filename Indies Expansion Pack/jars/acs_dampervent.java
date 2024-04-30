package data.scripts.hullmods;

// import com.fs.graphics.util.Fader.State;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.combat.DamageType;

import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;

// import com.fs.starfarer.api.Global;
// import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEngineAPI;
// import com.fs.starfarer.api.combat.ShipAPI;
// import com.fs.starfarer.api.combat.ShipAPI.HullSize;
// import com.fs.starfarer.api.combat.WeaponAPI;
// import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
// import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
// import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
// import com.fs.starfarer.api.util.Misc;
import java.awt.Color;
import com.fs.starfarer.api.combat.ShipCommand;
// import java.util.HashMap;
// import java.util.Map;

import com.fs.starfarer.api.combat.ShipSystemAPI;				//ADDED
import com.fs.starfarer.api.combat.ShipSystemSpecAPI;


public class acs_dampervent extends BaseHullMod {

    //private static Color damcwp = new Color(185,20,30,100);

    private static Map mag = new HashMap();
	static {
		mag.put(HullSize.FIGHTER, 0.33f);
		mag.put(HullSize.FRIGATE, 0.33f);
		mag.put(HullSize.DESTROYER, 0.33f);
		mag.put(HullSize.CRUISER, 0.5f);
		mag.put(HullSize.CAPITAL_SHIP, 0.5f);
	}

    private static float disabledTimer = 5f;

    public static class WeaponDisabledTimerData {
        IntervalUtil interval = new IntervalUtil(100f, 100f);
        public WeaponDisabledTimerData(float interval) {
            this.interval=new IntervalUtil(interval,interval);
        }

        boolean runOnces = false;
    }
	
	//protected Object STATUSKEY1 = new Object();

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) { 

        super.advanceInCombat(ship, amount);
        if (!ship.isAlive() || ship.getCurrentCR() == 0f) {return;}

        String id = ship.getId();
        MutableShipStatsAPI stats = ship.getMutableStats();

        CombatEngineAPI engine = Global.getCombatEngine();
        String key = "acs_dampervent" + "_" + ship.getId();
        // boolean runOnce = false;
        // ShipSystemAPI systemapi = ship.getSystem();

        // ShipSystemSpecAPI systemspecapi = systemapi.getSpecAPI();

        WeaponDisabledTimerData dataTimer = (WeaponDisabledTimerData) engine.getCustomData().get(key);

        if (dataTimer == null) {
            
            dataTimer = new WeaponDisabledTimerData((float) disabledTimer);
            engine.getCustomData().put(key, dataTimer);

        }

        if (ship != engine.getPlayerShip() || (ship == engine.getPlayerShip() && engine.getPlayerShip().getAI() != null)) {
			if (ship.getCurrFlux() > (0.7*ship.getMaxFlux())) {
				ship.giveCommand(ShipCommand.VENT_FLUX, ship, 1);
			}
		}

        if (ship.getFluxTracker().isVenting()) {
            float effectLevel = 1f;

            // float mult = (Float) mag.get(HullSize.CRUISER);
            float mult = 0.5f;
            // if (stats.getVariant() != null) {
            //     mult = (Float) mag.get(stats.getVariant().getHullSize());
            // }
            stats.getHullDamageTakenMult().modifyMult(id, 1f - (1f - mult) * effectLevel);
            stats.getArmorDamageTakenMult().modifyMult(id, 1f - (1f - mult) * effectLevel);
            stats.getEmpDamageTakenMult().modifyMult(id, 1f - (1f - mult) * effectLevel);


            ship.setJitter(stats, new Color(185,20,30,100), 1f, 1, 0, 7);
            ship.setJitterUnder(stats, new Color(185,20,30,100), 1f, 2, 0, 5);
            Global.getSoundPlayer().playSound("system_damper", 1.1f, 0.3f, ship.getLocation(), ship.getVelocity());
            //Global.getSoundPlayer().playSound(key, effectLevel, mult, null, null);

            // ShipAPI empTarget = ship;
            // for (int x = 0; x < 30; x++) {
            //     Global.getCombatEngine().spawnEmpArc(ship, ship.getLocation(),
            //                        empTarget,
            //                        empTarget, DamageType.ENERGY, 0, 200,
            //                        2000, null, 30f, new Color(230,40,40,0),
            //                        new Color(255,255,255,0));
            // }	            

            // if (stats.getEntity() instanceof ShipAPI) {
            //     stats.getEntity();
            // }

            dataTimer.runOnces = true;
            // runOnce = true;

            // if (!ship.getFluxTracker().isVenting()) {

            //     dataTimer.runOnces = true;

            //     stats.getHullDamageTakenMult().unmodify(id);
            //     stats.getArmorDamageTakenMult().unmodify(id);
            //     stats.getEmpDamageTakenMult().unmodify(id);

            //     ship.setJitter(stats, new Color(185,20,30,100), 1f, 25, 0, 7);

            // // // for (WeaponAPI weapon : ship.getAllWeapons()) {

            // // //     weapon.setRefireDelay(10f);

            // // // }
                
            // //     //runOnce = true;
            // //     // for (WeaponAPI weapon : ship.getAllWeapons()) {

            // //     //     weapon.setRemainingCooldownTo(50);
    
            // //     // }

            // //     // ShipAPI empTarget = ship;
            // //     // for (int x = 0; x < 2; x++) {
            // //     //     Global.getCombatEngine().spawnEmpArc(ship, ship.getLocation(),
            // //     //                     empTarget,
            // //     //                     empTarget, DamageType.ENERGY, 0, 200,
            // //     //                     2000, null, 30f, new Color(230,40,40,0),
            // //     //                     new Color(255,255,255,0));
            // //     // }	   
            // } 
            // runOnce = true;
            
        } 

        else if (!ship.getFluxTracker().isVenting() && dataTimer.runOnces == true) {

            stats.getHullDamageTakenMult().unmodify(id);
		    stats.getArmorDamageTakenMult().unmodify(id);
		    stats.getEmpDamageTakenMult().unmodify(id);

            // ship.setJitter(stats, new Color(185,20,30,100), 1f, 25, 0, 7);

            // ShipAPI empTarget = ship;
            // for (int x = 0; x < 1; x++) {
            //     engine.spawnEmpArc(ship, ship.getLocation(),
            //                        empTarget,
            //                        empTarget, DamageType.ENERGY, 0, 200,
            //                        2000, null, 30f, new Color(230,40,40,0),
            //                        new Color(255,255,255,0));
            // }	      
            for (WeaponAPI weapon : ship.getAllWeapons()) {

                weapon.disable();

            }

            // systemspecapi.setFiringAllowed(false);
            dataTimer.interval.advance(amount);

            

            if (dataTimer.interval.intervalElapsed()) {
                for (WeaponAPI weapon : ship.getAllWeapons()) {

                    weapon.repair();
                    
                    ship.syncWeaponDecalsWithArmorDamage();
    
                }

                // systemspecapi.setFiringAllowed(true);
            }

            

            // for (WeaponAPI weapon : ship.getAllWeapons()) {

            //     weapon.setRemainingCooldownTo(10);

            // }

            dataTimer.runOnces = false;
        } else {

            dataTimer.interval.setElapsed(0f);

        }

        // if (advance) {
            
        //     float effectLevel = 1f;

        //     //float mult = (Float) mag.get(HullSize.CRUISER);
        //     float mult = 0.33f;
        //     if (stats.getVariant() != null) {
        //         mult = (Float) mag.get(stats.getVariant().getHullSize());
        //     }
        //     stats.getHullDamageTakenMult().modifyMult(id, 1f - (1f - mult) * effectLevel);
        //     stats.getArmorDamageTakenMult().modifyMult(id, 1f - (1f - mult) * effectLevel);
        //     stats.getEmpDamageTakenMult().modifyMult(id, 1f - (1f - mult) * effectLevel);



        //     // ShipAPI ship = null;
        //     // boolean player = false;
        //     // if (stats.getEntity() instanceof ShipAPI) {
        //     //     ship = (ShipAPI) stats.getEntity();
        //     //     // player = ship == engine.getPlayerShip();
        //     // }
        //     // if (player) {
        //     //     ShipSystemAPI system = ship.getSystem();
        //     //     if (system != null) {
        //     //         float percent = (1f - mult) * effectLevel * 100;
        //     //         engine.maintainStatusForPlayerShip(STATUSKEY1,
        //     //             system.getSpecAPI().getIconSpriteName(), system.getDisplayName(),
        //     //             (int) Math.round(percent) + "% less damage taken", false);
        //     //     }
        //     // }
        // }

        // if (ship.getFluxTracker().isVenting()) {

        //     effectLevel = 1f;
		
        //     float mult = (Float) mag.get(HullSize.CRUISER);
        //     if (stats.getVariant() != null) {
        //         mult = (Float) mag.get(stats.getVariant().getHullSize());
        //     }
        //     stats.getHullDamageTakenMult().modifyMult(id, 1f - (1f - mult) * effectLevel);
        //     stats.getArmorDamageTakenMult().modifyMult(id, 1f - (1f - mult) * effectLevel);
        //     stats.getEmpDamageTakenMult().modifyMult(id, 1f - (1f - mult) * effectLevel);
            
            
        //     boolean player = false;
        //     if (stats.getEntity() instanceof ShipAPI) {
        //         ship = (ShipAPI) stats.getEntity();
        //         player = ship == Global.getCombatEngine().getPlayerShip();
        //     }
        //     if (player) {
        //         ShipSystemAPI system = ship.getSystem();
        //         if (system != null) {
        //             float percent = (1f - mult) * effectLevel * 100;
        //             Global.getCombatEngine().maintainStatusForPlayerShip(STATUSKEY1,
        //                 system.getSpecAPI().getIconSpriteName(), system.getDisplayName(),
        //                 (int) Math.round(percent) + "% less damage taken", false);
        //         }
        //     }
        // } else {

        //     stats.getHullDamageTakenMult().unmodify(id);
		//     stats.getArmorDamageTakenMult().unmodify(id);
		//     stats.getEmpDamageTakenMult().unmodify(id);

        //     // ShipAPI empTarget = ship;

        //     for (WeaponAPI weapon : ship.getAllWeapons()) {

        //         weapon.setRemainingCooldownTo(5f);

        //     }

            // for (int x = 0; x < 5; x++) {
            //     Global.getCombatEngine().spawnEmpArc(ship, ship.getLocation(),
            //     empTarget,
            //     empTarget, DamageType.ENERGY, 0, 200,
            //     2000, null, 30f, new Color(230,40,40,0),
            //     new Color(255,255,255,0));

            //     for (WeaponAPI weapon : ship.getAllWeapons()) {

            //         weapon.disable();

            //         float timer = 0f;
            //         for (int s = 0; s < 5; x++){
            //             timer += 1f;
            //             //weapon.disable();
            //         }
            //         if (timer == 5f) {
            //             weapon.repair();
            //         }
                    
            //     }
            // }	
        // }

     }


}


