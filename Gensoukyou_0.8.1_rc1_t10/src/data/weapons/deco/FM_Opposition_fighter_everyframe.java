package data.weapons.deco;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicRender;

import java.awt.*;

public class FM_Opposition_fighter_everyframe implements EveryFrameWeaponEffectPlugin {

    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (weapon.getSlot().isHidden() && weapon.getAmmo() != 0) {
            SpriteAPI sprite = Global.getSettings().getSprite("misc", "FM_Opposition_proj_sprite");
            Vector2f size = new Vector2f(sprite.getWidth(), sprite.getHeight());
            MagicRender.singleframe(sprite, weapon.getLocation(), size, weapon.getCurrAngle() - 90, Color.WHITE, false, CombatEngineLayers.STATION_WEAPONS_LAYER);
        }

    }
}
