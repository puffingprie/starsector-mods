package data.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.Misc;
import data.utils.FM_Colors;
import data.utils.FM_Misc;
import data.utils.I18nUtil;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicRender;

public class FM_reactorexcursion extends BaseShipSystemScript {

    public static final float ENERGY_WEAPON_DAMAGE_BONUS = 0.5f;
    public static final float WEAPON_DEBUFF = 0.2f;
    private float TIMER = 0f;
    private float EFFECT_ALPHA = 1f;
    private boolean BROKEN = false;
    private boolean DEPEND = false;

    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {

        if (stats.getEntity() == null || !(stats.getEntity() instanceof ShipAPI) || Global.getCombatEngine() == null) {
            return;
        }
        ShipAPI ship = (ShipAPI) stats.getEntity();
        CombatEngineAPI engine = Global.getCombatEngine();
        TIMER = TIMER + engine.getElapsedInLastFrame();
        SpriteAPI effect = Global.getSettings().getSprite("fx","FM_BazhiwuSystemVisual_00");
        Vector2f size = new Vector2f(effect.getWidth(),effect.getHeight());
        if (effectLevel > 0){
            stats.getEnergyWeaponDamageMult().modifyFlat(id, effectLevel * ENERGY_WEAPON_DAMAGE_BONUS);
            if (!DEPEND) {
                if (Math.random() <= WEAPON_DEBUFF) {
                    EFFECT_ALPHA = 0.25f;
                    BROKEN = true;
                    for (WeaponAPI weapon : ship.getAllWeapons()) {
                        weapon.disable(false);
                        stats.getCombatWeaponRepairTimeMult().modifyMult(id, 0.1f);
                    }
                }
                DEPEND = true;
                //engine.addFloatingText(ship.getLocation(),"DISABLE",20f,Color.WHITE,ship,0f,0f);
            }

            for (WeaponAPI weapon : ship.getAllWeapons()) {
                if (DEPEND && !weapon.isDisabled()) {
                    stats.getCombatWeaponRepairTimeMult().unmodifyMult(id);
                }
            }

            if (BROKEN){
                effect = Global.getSettings().getSprite("fx","FM_BazhiwuSystemVisual_00");
            }else {
                effect = Global.getSettings().getSprite("fx","FM_BazhiwuSystemVisual_01");
            }

        }else {
            if (state == State.COOLDOWN){
                unapply(stats,id);
            }
        }
        if (TIMER >= 1f && MagicRender.screenCheck(0.5f,ship.getLocation())){
            MagicRender.objectspace(
                    effect,
                    ship,
                    FM_Misc.ZERO,
                    FM_Misc.ZERO,
                    size,
                    FM_Misc.ZERO,
                    -180f,
                    0f,
                    true,
                    Misc.scaleAlpha(FM_Colors.FM_ORANGE_FLARE_FRINGE,EFFECT_ALPHA),
                    2f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0.25f,
                    0.5f,
                    0.2f,
                    true,
                    CombatEngineLayers.ABOVE_SHIPS_LAYER,
                    GL11.GL_SRC_ALPHA, GL11.GL_ONE
            );
            TIMER = 0f;
        }
    }

    public void unapply(MutableShipStatsAPI stats, String id) {
        DEPEND = false;
        EFFECT_ALPHA = 1f;
        BROKEN = false;
        stats.getEnergyWeaponDamageMult().unmodify(id);
        stats.getCombatWeaponRepairTimeMult().unmodifyMult(id);
    }

    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (effectLevel > 0) {
            if (index == 0) {
                return new StatusData(I18nUtil.getShipSystemString("FM_ReactoreExcursionInfo0") + (int) (ENERGY_WEAPON_DAMAGE_BONUS * effectLevel * 100f) + "%", false);
            } else if (index == 1) {
                return new StatusData(I18nUtil.getShipSystemString("FM_ReactoreExcursionInfo1") + (int) (WEAPON_DEBUFF * 100f) + "%", true);
            }
        }

        return null;
    }
}
