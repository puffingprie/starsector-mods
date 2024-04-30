package data.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
import com.fs.starfarer.api.util.Misc;
import data.utils.FM_Colors;
import data.utils.I18nUtil;
import data.utils.visual.FM_ByakurenSystemVisual;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicLensFlare;

import java.awt.*;

public class FM_byakurensystem extends BaseShipSystemScript {

    public static final Color EFFECT = new Color(255, 255, 255, 255);
    public static final Color JITTER = new Color(92, 217, 255, 164);
    public static final float SPEED_BONUS = 360f;

    public static final float MASS_BASE = 3400f;
    public static final float MASS_BUFF = 6800f;

    private boolean SYSTEM = true;

    private CombatEntityAPI visual0 = null;
    private CombatEntityAPI visual1 = null;
    private CombatEntityAPI visual2 = null;
    private float flareTimer = 0f;


    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {

        ShipAPI ship = (ShipAPI) stats.getEntity();

        if (ship == null) return;

        ship.setMass(MASS_BASE + effectLevel * MASS_BUFF);

        if (state != State.OUT && SYSTEM) {

            if (visual0 == null) {
                FM_ByakurenSystemVisual.FM_BSVParam param = new FM_ByakurenSystemVisual.FM_BSVParam();
                param.fadeIn = ship.getSystem().getChargeUpDur();
                param.full = ship.getSystem().getChargeActiveDur();
                param.fadeOut = ship.getSystem().getChargeDownDur();
                param.width = 500f;
                param.length = 700f;
                param.fluidSpeed = 1.25f;
                param.texPiece = 2;
                param.color = FM_Colors.FM_BLUE_FLARE_CORE;
                param.tex = Global.getSettings().getSprite("fx", "FM_StreamFx0");
                param.layers = CombatEngineLayers.ABOVE_SHIPS_LAYER;
                param.loc = MathUtils.getPoint(ship.getLocation(), ship.getCollisionRadius(), ship.getFacing());
                visual0 = Global.getCombatEngine().addLayeredRenderingPlugin(new FM_ByakurenSystemVisual(param, ship));
            }
            if (visual1 == null) {
                FM_ByakurenSystemVisual.FM_BSVParam param = new FM_ByakurenSystemVisual.FM_BSVParam();
                param.fadeIn = ship.getSystem().getChargeUpDur() + 0.25f;
                param.full = ship.getSystem().getChargeActiveDur() - 0.25f;
                param.fadeOut = ship.getSystem().getChargeDownDur();
                param.width = 680f;
                param.length = 360f;
                param.fluidSpeed = 0.75f;
                param.texPiece = 1;
                param.color = Misc.scaleAlpha(FM_Colors.FM_PURPLE_RED_SPRITE, 0.5f);
                param.tex = Global.getSettings().getSprite("fx", "FM_StreamFx2");
                param.layers = CombatEngineLayers.CONTRAILS_LAYER;
                param.loc = MathUtils.getPoint(ship.getLocation(), ship.getCollisionRadius(), ship.getFacing());
                visual1 = Global.getCombatEngine().addLayeredRenderingPlugin(new FM_ByakurenSystemVisual(param, ship));
            }
            if (visual2 == null) {
                FM_ByakurenSystemVisual.FM_BSVParam param = new FM_ByakurenSystemVisual.FM_BSVParam();
                param.fadeIn = ship.getSystem().getChargeUpDur() + 0.1f;
                param.full = ship.getSystem().getChargeActiveDur() - 0.1f;
                param.fadeOut = ship.getSystem().getChargeDownDur();
                param.width = 600f;
                param.length = 500f;
                param.fluidSpeed = 0.5f;
                param.texPiece = 1;
                param.color = FM_Colors.FM_BLUE_FLARE_FRINGE;
                param.tex = Global.getSettings().getSprite("fx", "FM_StreamFx1");
                param.layers = CombatEngineLayers.CONTRAILS_LAYER;
                param.loc = MathUtils.getPoint(ship.getLocation(), ship.getCollisionRadius(), ship.getFacing());
                visual2 = Global.getCombatEngine().addLayeredRenderingPlugin(new FM_ByakurenSystemVisual(param, ship));
            }

            SYSTEM = false;
        }
        if (state == ShipSystemStatsScript.State.OUT) {
            stats.getMaxSpeed().unmodify(id); // to slow down ship to its regular top speed while powering drive down
        } else {
            stats.getMaxSpeed().modifyFlat(id, SPEED_BONUS * effectLevel);
            stats.getAcceleration().modifyFlat(id, SPEED_BONUS * effectLevel);
        }


        //视觉相关
        ship.setJitter(ship, JITTER, 0.4f, 1, 1f);
        ship.setJitterUnder(ship, JITTER, 0.8f, 2, 3f);
        Vector2f visualLoc = MathUtils.getPoint(ship.getLocation(), ship.getCollisionRadius(), ship.getFacing());
        if (visual0 != null) {
            visual0.getLocation().set(visualLoc);
        }
        if (visual1 != null) {
            visual1.getLocation().set(visualLoc);
        }
        if (visual2 != null) {
            visual2.getLocation().set(visualLoc);
        }
        flareTimer = flareTimer + Global.getCombatEngine().getElapsedInLastFrame();
        if (flareTimer >= 0.05f) {
            MagicLensFlare.createSharpFlare(Global.getCombatEngine(), ship, visualLoc, 20f, 20f, 0, JITTER, EFFECT);
            flareTimer = 0f;
        }
        for (int i = 0; i < 30; i = i + 1) {
            Global.getCombatEngine().addHitParticle(
                    MathUtils.getRandomPointInCircle(ship.getLocation(), ship.getCollisionRadius() * 0.8f),
                    new Vector2f(),
                    MathUtils.getRandomNumberInRange(4f, 12f),
                    1f,
                    0.05f,
                    0.3f,
                    JITTER
            );
        }
    }

    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getMaxSpeed().unmodify(id);
        stats.getMaxTurnRate().unmodify(id);
        stats.getTurnAcceleration().unmodify(id);
        stats.getAcceleration().unmodify(id);
        stats.getDeceleration().unmodify(id);
        SYSTEM = true;

        if (stats.getEntity() instanceof ShipAPI) {
            ShipAPI ship = (ShipAPI) stats.getEntity();
            ship.setMass(MASS_BASE);
        }

        visual0 = null;
        visual1 = null;
        visual2 = null;
        flareTimer = 0f;
    }

    public StatusData getStatusData(int index, State state, float effectLevel) {

        if (index == 0) {
            return new StatusData(I18nUtil.getShipSystemString("FM_ByakurenSystemInfo"), false);
        }

        return null;
    }
}
