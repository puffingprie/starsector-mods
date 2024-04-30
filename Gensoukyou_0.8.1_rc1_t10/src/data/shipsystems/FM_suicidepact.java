package data.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
import data.shipsystems.ai.FM_suicidepact_ai;
import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class FM_suicidepact extends BaseShipSystemScript {

    public static final float SPEED_BONUS = 300f;
    public static final float ACC_BONUS = 300f;
    public static final float DAMAGE_RADIUS = 50f;

    public CombatEngineAPI engine;
    public ShipAPI enemy;

    public static Color EXP_COLOR = new Color(210, 88, 88, 255);

    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {

        ShipAPI the_ship = (ShipAPI) stats.getEntity();
        engine = Global.getCombatEngine();
        if (engine == null) return;

        if (state == ShipSystemStatsScript.State.OUT) {
            stats.getMaxSpeed().unmodify(id); // to slow down ship to its regular top speed while powering drive down
        } else {
            stats.getAcceleration().modifyPercent(id, ACC_BONUS);
            stats.getMaxSpeed().modifyFlat(id, SPEED_BONUS);
        }

        if (!(the_ship.getSystem().getSpecAPI().getAIScript() instanceof FM_suicidepact_ai)) return;

        the_ship.setJitter(the_ship, Color.RED, 5, 5, 5);

        enemy = AIUtils.getNearestEnemy(the_ship);
        if (enemy == null) return;

        Vector2f point = CollisionUtils.getNearestPointOnBounds(the_ship.getLocation(), enemy);

        //debug
        //engine.addFloatingText(point,"TEST",10f,Color.WHITE,enemy,0f,0f);

        if ((enemy.getOwner() != the_ship.getOwner() && !(enemy.isAlly()) && MathUtils.isWithinRange(point, the_ship.getLocation(), DAMAGE_RADIUS))
                || the_ship.getSystem().isCoolingDown()
        ) {

            spawnMine(the_ship, the_ship.getLocation());
            engine.spawnExplosion(the_ship.getLocation(), (Vector2f) the_ship.getVelocity().scale(0.25f), EXP_COLOR, 120f, 1f);
            Global.getSoundPlayer().playSound("hit_heavy_energy", 2f, 0.4f, the_ship.getLocation(), new Vector2f());
            engine.removeEntity(the_ship);
        }

    }

    public void unapply(MutableShipStatsAPI stats, String id) {
        engine = null;
        stats.getMaxSpeed().unmodify(id);
        stats.getAcceleration().unmodify(id);

    }


    public void spawnMine(ShipAPI source, Vector2f mineLoc) {
        CombatEngineAPI engine = Global.getCombatEngine();

        MissileAPI mine = (MissileAPI) engine.spawnProjectile(source, null,
                "FM_Suicidepact_minelayer",
                mineLoc,
                (float) Math.random() * 360f, null);

        // "spawned" does not include this mine

        if (source != null) {
            Global.getCombatEngine().applyDamageModifiersToSpawnedProjectileWithNullWeapon(
                    source, WeaponAPI.WeaponType.MISSILE, false, mine.getDamage());
        }

        float fadeInTime = 0f;
        mine.getVelocity().scale(0);
        mine.fadeOutThenIn(fadeInTime);

        float liveTime = 0f;
        //liveTime = 0.01f;
        mine.setFlightTime(mine.getMaxFlightTime() - liveTime);
        mine.addDamagedAlready(source);
        mine.setNoMineFFConcerns(true);

    }

}
