package data.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import data.hullmods.FantasySpellMod;
import data.utils.FM_Misc;
import data.utils.I18nUtil;
import org.dark.shaders.distortion.DistortionShader;
import org.dark.shaders.distortion.WaveDistortion;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicLensFlare;

import java.awt.*;

public class FM_crossborder extends BaseShipSystemScript {
    //public static final Color EFFECT = new Color(255, 106, 106, 218);
    public static final Color FLARE_1 = new Color(255, 60, 60, 176);
    public static final Color FLARE_2 = new Color(255, 146, 146, 255);
    public static final float BASE_SPELL_CONSUME = 0.2f;

    //public static final float CANCEL_RANGE = 500f;

    private WaveDistortion wave = null;

    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {

        ShipAPI the_ship = (ShipAPI) stats.getEntity();
        Vector2f ship_loc = the_ship.getLocation();
        //int owner = the_ship.getOwner();

        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine == null) return;
/*
        List<CombatEntityAPI> projects = CombatUtils.getEntitiesWithinRange(ship_loc, CANCEL_RANGE);
        for (CombatEntityAPI project : projects) {
            if (project.getOwner() != owner && project instanceof DamagingProjectileAPI) {
                engine.removeEntity(project);
                engine.spawnExplosion(project.getLocation(), new Vector2f(), EFFECT, project.getCollisionRadius(), 1f);

            }
        }
 */
        if (!the_ship.getVariant().hasHullMod("FantasySpellMod")) return;
        FantasySpellMod.SpellModState modState = FM_Misc.getSpellModState(engine, the_ship);


        MagicLensFlare.createSharpFlare(engine, the_ship, ship_loc, 9f, the_ship.getCollisionRadius() * 3, the_ship.getFacing() + 90f, FLARE_1, FLARE_2);
        the_ship.setExtraAlphaMult(1f - effectLevel);

        if (state == State.IN) {

            if (wave == null) {

                wave = new WaveDistortion(ship_loc, FM_Misc.ZERO);
                wave.setSize(the_ship.getCollisionRadius() * 1.5f);
                wave.setIntensity(10f);
                wave.setArc(0, 360);
                wave.flip(true);

                DistortionShader.addDistortion(wave);

                if (the_ship.getSystem().getAmmo() <= 0) {
                    modState.spellPower = modState.spellPower - BASE_SPELL_CONSUME;
                    the_ship.getSystem().setAmmo(1);
                }


//                MagicRender.battlespace(
//                        Global.getSettings().getSprite("misc","FM_PetaFlare_project_sprite_p"),
//                        ship_loc,
//                        FM_Misc.ZERO,
//                        new Vector2f(256f,256),
//                        FM_Misc.ZERO,
//                        the_ship.getFacing() + 90f,
//                        0,
//                        Misc.scaleAlpha(Color.WHITE,0.8f),
//                        true,
//                        0f,
//                        0f,
//                        0f,
//                        0f,0f,
//                        0.05f,0.45f,0.3f,
//                        CombatEngineLayers.ABOVE_PARTICLES
//                );

                //engine.addSmoothParticle(ship_loc, FM_Misc.ZERO,the_ship.getCollisionRadius() * 3f,255f,0.7f ,1f,FM_Colors.FM_ORANGE_FLARE_FRINGE);

            } else {
                float intensity = (float) (Math.sqrt(effectLevel) * 60f);
                wave.setLocation(ship_loc);
                wave.setSize(the_ship.getCollisionRadius() - effectLevel * 40f);
                wave.setIntensity(intensity + 10);
            }

        }

        if (state == State.OUT && wave != null) {
            wave.fadeOutSize(0.3f);
            wave = null;
        }


    }

    public void unapply(MutableShipStatsAPI stats, String id) {


    }

    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData(I18nUtil.getShipSystemString("FM_CrossBorderInfo"), false);
        }
        return null;
    }

    @Override
    public boolean isUsable(ShipSystemAPI system, ShipAPI ship) {

        if (ship.getSystem().getAmmo() >= 2) {
            return true;
        } else return FM_Misc.getSpellModState(Global.getCombatEngine(), ship).spellPower >= BASE_SPELL_CONSUME;
    }
}
