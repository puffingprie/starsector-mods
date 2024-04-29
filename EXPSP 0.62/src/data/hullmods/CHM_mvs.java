package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class CHM_mvs extends BaseHullMod {

    private static final Map class1 = new HashMap();
    static {
        class1.put(HullSize.FIGHTER, 0.40f);
        class1.put(HullSize.FRIGATE, 0.40f);
        class1.put(HullSize.DESTROYER, 0.40f);
        class1.put(HullSize.CRUISER, 0.30f);
        class1.put(HullSize.CAPITAL_SHIP, 0.30f);
        class1.put(HullSize.DEFAULT, 0.300f);
    }
    private static final Map class2 = new HashMap();
    static {
        class2.put(HullSize.FIGHTER, 40.0f);
        class2.put(HullSize.FRIGATE, 40.0f);
        class2.put(HullSize.DESTROYER, 40.0f);
        class2.put(HullSize.CRUISER, 30.0f);
        class2.put(HullSize.CAPITAL_SHIP, 30.0f);
        class2.put(HullSize.DEFAULT, 30.0f);
    }

    public static final float DEGRADE_DECREASE_PERCENT = 0.2f;
	//This above is kinda important, you have to define HullSize.FIGHTER and HullSize.DEFAULT because for some reason people are spawning old precursor fighters and the mod is randomly summoning these cringe gargoyles and CTDing the game. If you don't want them to get the bonus, I would just set it to 0f or something...
    public static final float ARMOR_BONUS = 5.0f;


    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
         stats.getCRPerDeploymentPercent().modifyMult(id, 1-(Float)class1.get(hullSize));
         stats.getCRLossPerSecondPercent().modifyMult(id,1-DEGRADE_DECREASE_PERCENT);

    }


    
    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return "" + (40);
        if (index == 1) return "" + (40);
        if (index == 2) return "" + (30);
        if (index == 3) return "" + (30);
        if (index == 4) return "" + 20;
        return null;
    }

	//Oh these are cool colors below introduced in 0.95a, to match with your tech type and stuff. Just nice to have!

    @Override
    public Color getBorderColor() {
        return new Color(32,142,193,0);
    }

    @Override
    public Color getNameColor() {
        return new Color(32,142,193,255);
    }
    @Override
    public void addPostDescriptionSection(final TooltipMakerAPI tooltip, final ShipAPI.HullSize hullSize, final ShipAPI ship, final float width, final boolean isForModSpec) {
        final Color flavor = new Color(110,110,110,255);
        float padList = 6f;
        float padSig = 1f;
        tooltip.addPara("%s", padList, flavor, new String[] { "Their numbers are legion, but we shall fulfill our duty regardless." });
        tooltip.addPara("%s", padSig, flavor, new String[] { "         \u2014 Anonymous member of the 5th Task Force" });
    }
}
