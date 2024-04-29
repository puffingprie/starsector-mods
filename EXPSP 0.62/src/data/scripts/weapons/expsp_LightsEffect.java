package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.FaderUtil;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;

public class expsp_LightsEffect implements EveryFrameWeaponEffectPlugin {

	private Color base = null;
	private FaderUtil fader = new FaderUtil(1f, 0.5f, 0.5f);
	private FaderUtil pulse = new FaderUtil(1f, 2f, 2f, true, true);
	
	public expsp_LightsEffect() {
		fader.fadeIn();
		pulse.fadeIn();
	}


	public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
		ShipAPI ship = weapon.getShip();

		 // Refit screen check
        if (ship.getOriginalOwner() == -1)
        {
            weapon.getSprite().setColor(new Color(92, 92, 92, 255)); // Blinker is off
            return;
        }

		if (engine.isPaused()) return;

		fader.advance(amount);
		pulse.advance(amount);
		
		SpriteAPI sprite = weapon.getSprite();
		if (base == null) {
			base = sprite.getColor();
		}
		if (ship.isHulk()) {
			fader.fadeOut();
		} else {
			if (ship.getFluxTracker().isVenting()) {
				fader.fadeOut();
			} else {
				fader.fadeIn();
			}
		}

		
		float alphaMult = fader.getBrightness() * (0.75f + pulse.getBrightness() * 0.25f);
		if (ship.getFluxTracker().isOverloaded()) {
			alphaMult = (float) Math.random() * fader.getBrightness();
		}
		
		Color color = Misc.scaleAlpha(base, alphaMult);
		//System.out.println(alphaMult);
		sprite.setColor(color);
	}
	
	
	
	
}
