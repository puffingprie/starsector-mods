package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.utils.FM_Misc;
import data.utils.I18nUtil;
import data.utils.visual.FM_ChainVisual;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FantasyAliceMod extends BaseHullMod {

    public static final String KEY = "FantasyAliceModVisual";

    public static final float FIGHTER_TIME_MULT = 1.5f;
    public static final float EFFECT_RANGE = 1000f;
    public static final float CREW_LOSS_EFFECT = 0f;
    public static final Color EFFECT_COLOR = new Color(44, 177, 212, 196);

    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize,
                                               MutableShipStatsAPI stats, String id) {

        stats.getDynamic().getStat(Stats.FIGHTER_CREW_LOSS_MULT).modifyMult(id, CREW_LOSS_EFFECT);


    }


    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine == null || engine.isPaused()) return;
        if (!ship.isAlive()) return;
        if (!ship.hasLaunchBays()) return;

        if (!engine.getCustomData().containsKey("FantasyAliceMod")) {
            engine.getCustomData().put("FantasyAliceMod", new HashMap<>());
        }

        Map<ShipAPI, FantasyAliceMod.ModState> currState = (Map) engine.getCustomData().get("FantasyAliceMod");


        if (!currState.containsKey(ship)) {
            currState.put(ship, new FantasyAliceMod.ModState());
        }

        //engine.addFloatingText(ship.getLocation(),String.valueOf(currState.get(ship).render),10f, Color.WHITE,ship,0f,0f);


        if (!currState.get(ship).render) {

            for (WeaponAPI weapon : ship.getAllWeapons()) {
                if (!weapon.getId().equals("FM_Alice")) continue;

                FM_ChainVisual visual = new FM_ChainVisual(weapon);
                engine.addLayeredRenderingPlugin(visual);

                //currState.get(ship).visualMap.put(weapon,visual);

            }


            currState.get(ship).render = true;

        }

        List<ShipAPI> fighters = FM_Misc.getFighters(ship, true);

        for (ShipAPI fighter : fighters) {

            if (fighter == null) continue;

            if (!fighter.isFighter()) continue;
            if (fighter.getWing() == null) continue;
            if (fighter.getWing().getSourceShip() != ship) continue;


            if (!fighter.isAlive()) {
                //engine.addFloatingText(fighter.getLocation(),"DEAD",10f,Color.WHITE,fighter,1f,1f);
                continue;
            }
            if (MathUtils.isWithinRange(fighter, ship, EFFECT_RANGE)) {
                fighter.getMutableStats().getTimeMult().modifyMult(fighter.getFleetMemberId() + "_FantasyAliceMod", FIGHTER_TIME_MULT);

                fighter.setJitterUnder(fighter, EFFECT_COLOR, 1f, 6, 10f);

            } else {
                fighter.getMutableStats().getTimeMult().unmodify(fighter.getFleetMemberId() + "_FantasyAliceMod");
            }


        }
    }

    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        tooltip.addSpacer(10f);
        //描述与评价
        tooltip.addSectionHeading(I18nUtil.getHullModString("FM_DescriptionAndEvaluation"), Alignment.TMID, 4f);
        if (Keyboard.isKeyDown(Keyboard.getKeyIndex("F1"))) {
            tooltip.addPara (
                    I18nUtil.getHullModString ( "FantasyAliceMod_DAE_0" )
                    , Misc.getTextColor (), 4f );
            tooltip.addSpacer ( 10f );
            tooltip.addPara ( I18nUtil.getHullModString ( "FantasyAliceMod_DAE_1" ), Misc.getGrayColor (), 4f );
        }
        if (!Keyboard.isKeyDown(Keyboard.getKeyIndex("F1"))) {
            tooltip.addPara("Press and hold [%s] to view this information.", Float.valueOf(10.0f), Misc.getGrayColor(), Misc.getStoryBrightColor(), new String[]{"F1"}).setAlignment(Alignment.MID);
        }
    }

    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) return "" + FIGHTER_TIME_MULT + "x";
        if (index == 1) return "" + (int) EFFECT_RANGE;
        if (index == 2) return I18nUtil.getHullModString("FantasyAliceMod_HL_2");
        return null;
    }


    private final static class ModState {
        //HashMap<WeaponAPI,FM_ChainVisual> visualMap;
        boolean render;

        private ModState() {

            //visualMap = new HashMap<>();

            render = false;

        }
    }
}
