package data.shipsystems.scripts.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.util.IntervalUtil;
import java.util.HashMap;
import java.util.Map;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class pn_leapAI implements ShipSystemAIScript
{
    private CombatEngineAPI engine;
    private ShipAPI ship;
    private ShipSystemAPI system;
    
    private final int LEAP_RANGE = 1000;
    
    private final IntervalUtil checkAgain = new IntervalUtil (0.5f,1.5f);

    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine)
    {
        this.ship = ship;
        this.system = system;
    }

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target)
    {        
        if (engine != Global.getCombatEngine()) {
            this.engine = Global.getCombatEngine();
        }

        if (engine.isPaused() || ship.getShipAI()==null) {
            return;
        }
        
        checkAgain.advance(amount);
        if (checkAgain.intervalElapsed() && AIUtils.canUseSystemThisFrame(ship) ) {
            
            //use the system if the ship is retreating
            if (ship.isRetreating()){
                ship.useSystem();
            } else if (ship.getHullLevel()>0.5)
            {   
                //if the ship don't have a valid target, check the surrounding
                if (ship.getShipTarget()==null || !MathUtils.isWithinRange(ship, ship.getShipTarget(), LEAP_RANGE) || !ship.getShipTarget().isAlive()){
                    //nobody's nearby, just jump forward
                    if (AIUtils.getNearbyEnemies(ship, 1.5f*LEAP_RANGE).isEmpty()){
                        ship.useSystem();                        
                    } else {
                        //check for shieldless enemies nearby
                        Map< Integer , ShipAPI > MEMBERS = new HashMap();
                        int nbKey = 0;
                        for ( ShipAPI tmp : AIUtils.getNearbyEnemies(ship, LEAP_RANGE)) {
                            if ( tmp!= null && !tmp.isFighter() && !tmp.isDrone() && (tmp.getShield()==null || tmp.getShield().isOff())) {
                                MEMBERS.put(nbKey, tmp);
                                nbKey++;
                            }
                        }                    
                        if (!MEMBERS.isEmpty()) {
                            //there are potential targets nearby? choose one and ram it    
                            int chooser = (int)(Math.round( Math.random() * nbKey));
                            ship.setShipTarget(MEMBERS.get(chooser));
                            ship.useSystem();
                        }
                    }
                //if it's out of range, check for other targets nearby
                } else if (ship.getShipTarget().getShield()==null || ship.getShipTarget().getShield().isOff()){
                    ship.useSystem();
                }                
            }
                
//                //if the target is in range, jump as soon as it to lower it's shield
//                if (ship.getShipTarget()!=null && MathUtils.isWithinRange(ship, ship.getShipTarget(), LEAP_RANGE)){
//                    if (ship.getShipTarget().getShield().isOff()){
//                        ship.useSystem();
//                    }
//                //if it's out of range, check for other targets nearby
//                } else {
//                    //nobody's nearby, just jump forward
//                    if (AIUtils.getNearbyEnemies(ship, LEAP_RANGE).isEmpty()){
//                        ship.useSystem();                        
//                    } else {
//                        //check for shieldless enemies nearby
//                        Map< Integer , ShipAPI > MEMBERS = new HashMap();
//                        int nbKey = 0;
//                        for ( ShipAPI tmp : AIUtils.getNearbyEnemies(ship, LEAP_RANGE)) {
//                            if ( tmp!= null && !tmp.isFighter() && !tmp.isDrone() && tmp.getShield().isOff()) {
//                                MEMBERS.put(nbKey, tmp);
//                                nbKey++;
//                            }
//                        }                    
//                        if (!MEMBERS.isEmpty()) {
//                            //there are potential targets nearby? choose one and ram it    
//                            int chooser = (int)(Math.round( Math.random() * nbKey));
//                            ship.setShipTarget(MEMBERS.get(chooser));
//                            ship.useSystem();
//                        }
//                    }                    
//                }                
//            }
        }
    }
}
