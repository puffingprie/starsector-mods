package data.hullmods.ix;

import java.awt.Color;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class ArchaeotechMastery extends BaseHullMod {

    private static float COOLDOWN_BONUS = 5f;

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getSystemCooldownBonus().modifyMult(id, 1f - COOLDOWN_BONUS / 100f);
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return "" + (int) COOLDOWN_BONUS + "%";
        return null;
    }
	
    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		String s = "\"Yes, the underlying principles are well understood, but the practical applications might as well be magic.\"";
        tooltip.addPara("%s", 6f, Misc.getGrayColor(), s);
    }
    
	/*
    @Override
    public Color getBorderColor() {
        return new Color(147, 102, 50, 0);
    }
	*/
	
    @Override
    public Color getNameColor() {
        return new Color(0, 245, 0, 255);
    }
}
