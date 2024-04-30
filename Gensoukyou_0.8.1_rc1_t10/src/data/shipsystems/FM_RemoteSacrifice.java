package data.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import data.shipsystems.ai.FM_suicidepact_ai;
import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicFakeBeam;

import java.awt.*;

public class FM_RemoteSacrifice extends BaseShipSystemScript {

    public static final float SPEED_BONUS = 100f;
    public static final float ACC_BONUS = 100f;
    public static final float BEAM_RANGE = 400f;

    public CombatEngineAPI engine;
    public ShipAPI enemy;

    public static final Color EXP_COLOR = new Color(77, 223, 255, 255);

    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {

        ShipAPI the_ship = (ShipAPI) stats.getEntity();
        engine = Global.getCombatEngine();
        if (engine == null) return;

        if (state == State.OUT) {
            stats.getMaxSpeed().unmodify(id); // to slow down ship to its regular top speed while powering drive down
        } else {
            stats.getAcceleration().modifyPercent(id, ACC_BONUS);
            stats.getMaxSpeed().modifyFlat(id, SPEED_BONUS);
        }

        if (!(the_ship.getSystem().getSpecAPI().getAIScript() instanceof FM_suicidepact_ai)) return;

        the_ship.setJitter(the_ship, Color.CYAN, 1, 5, 2);

        enemy = AIUtils.getNearestEnemy(the_ship);
        if (enemy == null) return;

        Vector2f point = CollisionUtils.getNearestPointOnBounds(the_ship.getLocation(), enemy);

        //debug
        //engine.addFloatingText(point,"TEST",10f,Color.WHITE,enemy,0f,0f);

        if ((enemy.getOwner() != the_ship.getOwner() && !(enemy.isAlly()) && MathUtils.isWithinRange(point, the_ship.getLocation(), BEAM_RANGE - 50f))
                || the_ship.getSystem().isCoolingDown()
        ) {

            MagicFakeBeam.spawnFakeBeam(
                    engine,
                    the_ship.getLocation(),
                    BEAM_RANGE + 150f,
                    VectorUtils.getAngle(the_ship.getLocation(), point),
                    14f,
                    1f,
                    0.2f,
                    70f,
                    Color.WHITE,
                    EXP_COLOR,
                    300f,
                    DamageType.ENERGY,
                    300f,
                    the_ship

            );

            engine.spawnExplosion(the_ship.getLocation(), (Vector2f) the_ship.getVelocity().scale(0.25f), EXP_COLOR, 90f, 1f);
            Global.getSoundPlayer().playSound("hit_heavy_energy", 2f, 0.4f, the_ship.getLocation(), new Vector2f());
            engine.removeEntity(the_ship);
        }

    }

    public void unapply(MutableShipStatsAPI stats, String id) {
        engine = null;
        stats.getMaxSpeed().unmodify(id);
        stats.getAcceleration().unmodify(id);

    }


}
