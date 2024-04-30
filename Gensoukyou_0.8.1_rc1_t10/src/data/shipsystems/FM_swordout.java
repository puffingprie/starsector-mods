package data.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import data.utils.I18nUtil;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicRender;

import java.awt.*;

public class FM_swordout extends BaseShipSystemScript {
    //public static final float WEAPON_RANGE_PERCENT = 30f;
    public static final float ROF_BONUS = 0.5f;
    public static final float FLUX_REDUCTION = 25f;

    public static final Color EFFECT = new Color(255, 255, 255, 75);

    private boolean RENDER = false;
    private Vector2f RENDER_OFFSET = null;
    private float TIMER = 0;
    private Vector2f VEL = null;


    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        //float weaponRangePercent = WEAPON_RANGE_PERCENT * effectLevel;
        float mult = 1f + ROF_BONUS * effectLevel;

//        stats.getBallisticWeaponRangeBonus().modifyPercent(id, -weaponRangePercent);
//        stats.getEnergyWeaponRangeBonus().modifyPercent(id, -weaponRangePercent);
        stats.getBallisticRoFMult().modifyMult(id, mult);
        stats.getBallisticWeaponFluxCostMod().modifyPercent(id, -FLUX_REDUCTION);
        stats.getEnergyRoFMult().modifyMult(id, mult);
        stats.getEnergyWeaponFluxCostMod().modifyPercent(id, -FLUX_REDUCTION);


        // 简单的残影效果.gif
        ShipAPI ship = (ShipAPI) stats.getEntity();
        TIMER = TIMER + 1;
        if (RENDER_OFFSET == null) {
            RENDER_OFFSET = ship.getRenderOffset();
            VEL = ship.getVelocity().negate(VEL);
        }
        if (TIMER >= 10 && RENDER) {
            RENDER_OFFSET = ship.getRenderOffset();
            VEL = ship.getVelocity().negate(VEL);
            TIMER = 0;
            RENDER = !RENDER;
        }

        if (effectLevel > 0 && ship != null && !RENDER) {
            Vector2f size = new Vector2f(ship.getSpriteAPI().getWidth(), ship.getSpriteAPI().getHeight());

            SpriteAPI sprite = Global.getSettings().getSprite(ship.getHullSpec().getSpriteName());
            //SpriteAPI sprite = Global.getSettings().getSprite("fx","FM_Konpaku_effect");

            MagicRender.objectspace(sprite, ship, RENDER_OFFSET, VEL, size, new Vector2f(), ship.getFacing() + 180f, 0, false, EFFECT, true
                    , 0, 0.1f, 0.5f, true);
            RENDER = !RENDER;
        }


    }

    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getSightRadiusMod().unmodify(id);
        stats.getBallisticRoFMult().unmodify(id);
        stats.getEnergyRoFMult().unmodify(id);
        stats.getEnergyWeaponFluxCostMod().unmodify(id);
        stats.getBallisticWeaponFluxCostMod().unmodify(id);

//        stats.getBallisticWeaponRangeBonus().unmodify(id);
//        stats.getEnergyWeaponRangeBonus().unmodify(id);
    }

    public StatusData getStatusData(int index, State state, float effectLevel) {
        float mult = 1f + ROF_BONUS * effectLevel;
        if (index == 0) {
            return new StatusData(I18nUtil.getShipSystemString("FM_SwordOutInfo2") + mult * 100f + "%", false);
        } else if (index == 1) {
            return new StatusData(I18nUtil.getShipSystemString("FM_SwordOutInfo3") + FLUX_REDUCTION + "%", false);
        }
        return null;
    }
}
