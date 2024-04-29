package data.scripts.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.SoundAPI;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import data.scripts.everyframe.II_TitanPlugin;
import data.scripts.weapons.II_LightsEveryFrame;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;

public class II_EMPBurst extends BaseShipSystemScript {

    private static final Color JITTER_COLOR = new Color(204, 0, 255, 75);

    private boolean exploded = false;
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

        ship.setJitter(stats.getEntity(), JITTER_COLOR, effectLevel, 5 + Math.round(effectLevel * 5f), effectLevel * 5f, 10f + (effectLevel * 20f));
        ship.getMutableStats().getDynamic().getMod(II_LightsEveryFrame.LIGHTS_ALPHA_ID).modifyFlat("ii_empburst", 1f);

        if (!started) {
            started = true;

            float distanceToHead = MathUtils.getDistance(stats.getEntity(),
                    Global.getCombatEngine().getViewport().getCenter());
            float refDist = 1000f;
            float vol = refDist / Math.max(refDist, distanceToHead);
            sound = Global.getSoundPlayer().playUISound("ii_titanx_burst_charge", 1f, vol);
            stats.getEntity().setCollisionClass(CollisionClass.SHIP);
        }

        if ((state == State.ACTIVE) && !exploded) {
            exploded = true;
            II_TitanPlugin.burstTitanX(ship, effectLevel);
        }
    }

    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 1) {
            return new StatusData("Arming spare flux core detonator", false);
        }
        return null;
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        ShipAPI ship = (ShipAPI) stats.getEntity();
        if (ship == null) {
            return;
        }
        ship.getMutableStats().getDynamic().getMod(II_LightsEveryFrame.LIGHTS_ALPHA_ID).modifyFlat("ii_empburst", 0f);
        exploded = false;
        started = false;
        if (sound != null) {
            sound.stop();
        }
    }
}
