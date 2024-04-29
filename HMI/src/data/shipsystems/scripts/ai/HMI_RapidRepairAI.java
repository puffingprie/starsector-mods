package data.shipsystems.scripts.ai;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipEngineControllerAPI.ShipEngineAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import org.magiclib.util.MagicRender;
import java.awt.Color;
import java.util.List;
import java.util.Set;

import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class HMI_RapidRepairAI implements ShipSystemAIScript {
    private CombatEngineAPI engine;
    private ShipAPI ship;
    private ShipSystemAPI system;
    private IntervalUtil timer=new IntervalUtil(1,2);
    private boolean flameout;
    private boolean weapondown;

    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.system = system;
        this.engine = engine;
        timer.randomize();
    }

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
        if(engine.isPaused()){
            return;
        }
        
        timer.advance(amount);
        if(timer.intervalElapsed()){
            List <ShipEngineAPI> engines = ship.getEngineController().getShipEngines();
            List <WeaponAPI> weapons = ship.getAllWeapons();

            if (!engines.isEmpty()){
                Integer fraction=0;
                for(ShipEngineAPI e : engines){
                    if (e.isDisabled()){
                        fraction++;
                    }
                }
                flameout = fraction==engines.size();
            }

            if (!weapons.isEmpty()){
                Integer fraction1=0;
                for(WeaponAPI e : weapons){
                    if (e.isDisabled()){
                        fraction1++;
                    }
                }
                weapondown = fraction1==weapons.size() + 0.5;
            }



            if (flameout && !system.isActive() && AIUtils.canUseSystemThisFrame(ship)){
                ship.giveCommand(ShipCommand.USE_SYSTEM, null, 0);
            }

            if (weapondown && !system.isActive() && AIUtils.canUseSystemThisFrame(ship)) {
                ship.giveCommand(ShipCommand.USE_SYSTEM, null, 0);
            }
        }
    }
}
