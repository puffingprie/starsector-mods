package data.weapons.missileAI;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MissileAIPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import org.lwjgl.util.vector.Vector2f;

/**
 * Well, just for decelerate................
 */


public class FM_Nightbug_missile_ai implements MissileAIPlugin {


    private CombatEngineAPI engine;
    private final MissileAPI missile;


    public FM_Nightbug_missile_ai(MissileAPI missile) {
        this.missile = missile;
    }

    public void advance(float amount) {

        engine = Global.getCombatEngine();
        if (engine == null) return;
        if (missile.getWeapon() == null) return;

        if (missile.getVelocity().length() >= 60) {
            missile.giveCommand(ShipCommand.DECELERATE);
        } else {
            missile.getVelocity().set(new Vector2f());
        }
    }

}
