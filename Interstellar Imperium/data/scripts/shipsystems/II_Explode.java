package data.scripts.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.SoundAPI;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.everyframe.II_TitanPlugin;
import data.scripts.hullmods.II_BasePackage;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;

public class II_Explode extends BaseShipSystemScript {

    private static final Color JITTER_COLOR = new Color(255, 204, 0, 50);
    private static final Color JITTER_COLOR_ELITE = new Color(204, 0, 255, 75);

    private boolean exploded = false;
    private final IntervalUtil interval = new IntervalUtil(0.2f, 0.2f);
    private SoundAPI sound = null;
    private boolean started = false;

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        ShipAPI ship = (ShipAPI) stats.getEntity();
        if (ship == null) {
            return;
        }

        if (Global.getCombatEngine().isPaused()) {
            return;
        }

        interval.advance(Global.getCombatEngine().getElapsedInLastFrame());

        if (ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
            ship.setJitter(stats.getEntity(), JITTER_COLOR_ELITE, effectLevel, 5 + Math.round(effectLevel * 5f), effectLevel * 5f, 10f + (effectLevel * 20f));
        } else {
            ship.setJitter(stats.getEntity(), JITTER_COLOR, effectLevel, 5 + Math.round(effectLevel * 5f), effectLevel * 5f, 10f + (effectLevel * 20f));
        }

        if (!started) {
            started = true;

            float distanceToHead = MathUtils.getDistance(stats.getEntity(),
                    Global.getCombatEngine().getViewport().getCenter());
            float refDist = 1500f;
            float vol = refDist / Math.max(refDist, distanceToHead);
            sound = Global.getSoundPlayer().playUISound("ii_titan_explode_charge", 1f, vol);
            stats.getEntity().setCollisionClass(CollisionClass.SHIP);
        }

        if ((state == State.IN && !exploded) && stats.getEntity().getHullLevel() < 0.5f) {
            if (interval.intervalElapsed() && ((Math.random() * Math.random()) > (stats.getEntity().getHullLevel() * 2f))) {
                if (sound != null) {
                    sound.stop();
                }
                exploded = true;
                if (ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
                    II_TitanPlugin.explodeElite(ship, effectLevel);
                } else {
                    II_TitanPlugin.explode(ship, effectLevel);
                }
            }
        }

        if ((state == State.ACTIVE) && !exploded) {
            exploded = true;
            if (ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
                II_TitanPlugin.explodeElite(ship, 1f);
            } else {
                II_TitanPlugin.explode(ship, 1f);
            }
        }
    }

    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 1) {
            return new StatusData("Arming flux core detonator", false);
        }
        return null;
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        exploded = false;
        started = false;
        if (sound != null) {
            sound.stop();
        }
    }
}
