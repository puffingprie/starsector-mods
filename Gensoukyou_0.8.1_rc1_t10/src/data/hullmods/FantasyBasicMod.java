package data.hullmods;


import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.utils.I18nUtil;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicIncompatibleHullmods;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class FantasyBasicMod extends BaseHullMod {

    public static Map<ShipAPI.HullSize, Float> magRANGE = new HashMap();

    static {
        magRANGE.put(ShipAPI.HullSize.FIGHTER, 0f);
        magRANGE.put(ShipAPI.HullSize.FRIGATE, 300f);
        magRANGE.put(ShipAPI.HullSize.DESTROYER, 400f);
        magRANGE.put(ShipAPI.HullSize.CRUISER, 550f);
        magRANGE.put(ShipAPI.HullSize.CAPITAL_SHIP, 700f);
    }

    public static final Object INFO2 = new Object();
    public static final Object INFO3 = new Object();
    public static final Object INFO4 = new Object();
    public static final Color DANGER_LEVEL_3 = new Color(255, 14, 14, 181);

    public static final float ROF_BONUS = 25f;
    public static final float DAMAGE_REDUCTION = 16.7f;
    public static final String FANTASYBASICMOD = "FantasyBasicMod";

    public static final float EFFECT_TIME = 7f;
    public static final float CD_TIME = 14f;

    //public static final float LEVEL_3 = 0.8f;
    public static final int BULLET_NUMBER_REQUIREMENT_FOR_EFFECT = 7;

    public static final float SPEED_AND_OTHER_BONUS = 25f;


    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        float percent1 = (ROF_BONUS);
        float mult2 = 1f - (DAMAGE_REDUCTION * 0.01f);
        stats.getBallisticRoFMult().modifyPercent(id, percent1);
        stats.getEnergyRoFMult().modifyPercent(id, percent1);
        stats.getEnergyWeaponDamageMult().modifyMult(id, mult2);
        stats.getBallisticWeaponDamageMult().modifyMult(id, mult2);
        stats.getEnergyWeaponFluxCostMod().modifyMult(id, mult2);
        stats.getBallisticWeaponFluxCostMod().modifyMult(id, mult2);
        stats.getBeamDamageTakenMult().modifyMult(id, 1f);

        if (stats.getVariant().getHullMods().contains("safetyoverrides")) {
            MagicIncompatibleHullmods.removeHullmodWithWarning(stats.getVariant(), "safetyoverrides", "FantasyBasicMod");
        }
    }


    public void advanceInCombat(ShipAPI ship, float amount) {

        CombatEngineAPI engine = Global.getCombatEngine();

        if (engine == null) return;

        if (!engine.getCustomData().containsKey("FantasyBasicMod")) {
            engine.getCustomData().put("FantasyBasicMod", new HashMap<>());
        }


        Map<ShipAPI, FantasyBasicMod.ModState> currState = (Map) engine.getCustomData().get("FantasyBasicMod");

        if (!currState.containsKey(ship)) {
            currState.put(ship, new ModState());
        }

        if (!ship.isAlive()) return;


        ShipAPI.HullSize hullSize = ship.getHullSize();
        String the_ship_buff = ship.getFleetMemberId() + "_FantasyBasicMod";

        Vector2f center = ship.getLocation();
        List<CombatEntityAPI> entities = CombatUtils.getEntitiesWithinRange(center, magRANGE.get(hullSize));
        List<CombatEntityAPI> project_in_range = new ArrayList<>();

        for (CombatEntityAPI project : entities) {
            if (project.getOwner() != ship.getOwner() && project instanceof DamagingProjectileAPI) {
                project_in_range.add(project);
            }
        }
        int BULLET_NUMBER = project_in_range.size();


        //侦测与加成
        ShipAPI player = Global.getCombatEngine().getPlayerShip();

        if (ship == player) {

            if (currState.get(ship).effect) {
                Global.getCombatEngine().maintainStatusForPlayerShip(INFO4, Global.getSettings().getSpriteName("ui", "icon_kinetic"), I18nUtil.getHullModString("FantasyBasicMod_Combat_0_T"), I18nUtil.getHullModString("FantasyBasicMod_Combat_0_D") + Misc.getRoundedValueMaxOneAfterDecimal(currState.get(ship).timer), false);
            } else {
                if (currState.get(ship).cd >= 0.1f) {
                    Global.getCombatEngine ().maintainStatusForPlayerShip ( INFO3, Global.getSettings ().getSpriteName ( "ui", "icon_kinetic" ), I18nUtil.getHullModString ( "FantasyBasicMod_Combat_1_T" ), I18nUtil.getHullModString ( "FantasyBasicMod_Combat_1_D" ) + Misc.getRoundedValueMaxOneAfterDecimal ( currState.get ( ship ).cd ), true );
                }
            }
            if (currState.get(ship).cd <= 0f & !currState.get(ship).effect) {
                Global.getCombatEngine ().maintainStatusForPlayerShip ( INFO2, Global.getSettings ().getSpriteName ( "ui", "icon_kinetic" ), I18nUtil.getHullModString ( "FantasyBasicMod_Combat_2_T" ), I18nUtil.getHullModString ( "FantasyBasicMod_Combat_2_D" ) + BULLET_NUMBER, false );
            }
        }


        if (BULLET_NUMBER > BULLET_NUMBER_REQUIREMENT_FOR_EFFECT && currState.get(ship).cd <= 0f) {
            if (currState.get(ship).timer <= 0f) {
                currState.get(ship).timer = EFFECT_TIME;
                currState.get(ship).effect = true;
            }
        }

        if (currState.get(ship).effect) {
            currState.get(ship).timer = currState.get(ship).timer - amount;
            ship.getMutableStats().getMaxSpeed().modifyPercent(the_ship_buff, SPEED_AND_OTHER_BONUS);
            ship.getMutableStats().getAcceleration().modifyPercent(the_ship_buff, SPEED_AND_OTHER_BONUS);
            ship.getMutableStats().getMaxTurnRate().modifyPercent(the_ship_buff, SPEED_AND_OTHER_BONUS);
            ship.getMutableStats().getTurnAcceleration().modifyPercent(the_ship_buff, SPEED_AND_OTHER_BONUS);

            //ship.getMutableStats().getShieldDamageTakenMult().modifyMult(the_ship_buff,LEVEL_3);

            if (ship.getShield() != null) {
                ship.getShield().setRadius(ship.getShield().getRadius(),
                        Global.getSettings().getSpriteName("fx", "FM_modeffect_1"),
                        Global.getSettings().getSpriteName("fx", "FM_shieldring")
                );
                ship.setJitterUnder(the_ship_buff, Misc.setAlpha(ship.getShield().getInnerColor(),180), 2, 3, 2);
            }else {
                ship.setJitterUnder(the_ship_buff, Misc.setAlpha(DANGER_LEVEL_3,180), 2, 3, 2);
            }


            //if (ship == player) {
            //    Global.getCombatEngine().maintainStatusForPlayerShip(INFO5, Global.getSettings().getSpriteName("ui", "icon_kinetic"), "底力爆发", "护盾所受伤害" + mult + "倍", false);
            //}

            if (currState.get(ship).timer <= 0f) {
                currState.get(ship).effect = false;
                currState.get(ship).cd = CD_TIME;
            }

        } else {

            if (currState.get(ship).cd > 0f) {
                currState.get(ship).cd = currState.get(ship).cd - amount;
            }

            if (ship.getShield() != null) {

                ship.getShield().setRadius(ship.getShield().getRadius(),
                        Global.getSettings().getSpriteName("fx", "FM_shieldinner"),
                        Global.getSettings().getSpriteName("fx", "FM_shieldring")
                );

            }

            ship.getMutableStats().getMaxSpeed().unmodify(the_ship_buff);
            ship.getMutableStats().getAcceleration().unmodify(the_ship_buff);
            ship.getMutableStats().getMaxTurnRate().unmodify(the_ship_buff);
            ship.getMutableStats().getTurnAcceleration().unmodify(the_ship_buff);

            //ship.getMutableStats().getShieldDamageTakenMult().unmodify(the_ship_buff);

        }
    }

    private final static class ModState {
        float timer;
        float cd;
        boolean effect;

        private ModState() {
            effect = false;
            cd = 0f;
            timer = 0f;
        }
    }

    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        tooltip.addSpacer(10f);
        //说明
        tooltip.addSectionHeading(I18nUtil.getHullModString("FM_Instruction"), Alignment.TMID, 4f);
        tooltip.addPara(I18nUtil.getHullModString("FantasyBasicMod_I_0"), Misc.getHighlightColor(), 4f);
        tooltip.addPara(I18nUtil.getHullModString("FantasyBasicMod_I_1"), 4f, Misc.getTextColor(), Misc.getHighlightColor(),
                "2%",
                "25%"
        );
        tooltip.addSpacer(10f);
        tooltip.addSectionHeading(I18nUtil.getHullModString("FantasyBasicMod_SP"), Alignment.TMID, 4f);
        tooltip.addPara(I18nUtil.getHullModString("FantasyBasicMod_SP_0"), 4f, Misc.getTextColor(), Misc.getHighlightColor(),
                String.valueOf(magRANGE.get(ShipAPI.HullSize.FRIGATE).intValue()),
                String.valueOf(magRANGE.get(ShipAPI.HullSize.DESTROYER).intValue()),
                String.valueOf(magRANGE.get(ShipAPI.HullSize.CRUISER).intValue()),
                String.valueOf(magRANGE.get(ShipAPI.HullSize.CAPITAL_SHIP).intValue()),
                String.valueOf(BULLET_NUMBER_REQUIREMENT_FOR_EFFECT),
                (int) SPEED_AND_OTHER_BONUS + "%",
                String.valueOf((int) EFFECT_TIME),
                String.valueOf((int) CD_TIME)
        );
        tooltip.addSpacer(10f);
        //描述与评价
        tooltip.addSectionHeading(I18nUtil.getHullModString("FM_DescriptionAndEvaluation"), Alignment.TMID, 4f);
        if (Keyboard.isKeyDown(Keyboard.getKeyIndex("F1"))) {
            tooltip.addPara(I18nUtil.getHullModString("FantasyBasicMod_DAE_0"), Misc.getTextColor(), 4f);
            tooltip.addSpacer(10f);
            tooltip.addPara(I18nUtil.getHullModString("FantasyBasicMod_DAE_1"), Misc.getGrayColor(), 4f);
            tooltip.addSpacer(10f);
            tooltip.addPara(I18nUtil.getHullModString("FantasyBasicMod_DAE_2"), Misc.getGrayColor(), 4f);
        }
        if (!Keyboard.isKeyDown(Keyboard.getKeyIndex("F1"))) {
            tooltip.addPara("Press and hold [%s] to view this information.", Float.valueOf(10.0f), Misc.getGrayColor(), Misc.getStoryBrightColor(), new String[]{"F1"}).setAlignment(Alignment.MID);
        }
        tooltip.addSpacer(10f);
        tooltip.addPara(I18nUtil.getHullModString("FantasyBasicMod_SP_1"), Misc.getNegativeHighlightColor(), 4f);
    }


    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) return "" + (int) ROF_BONUS + "%";
        if (index == 1) return "" + Misc.getRoundedValueMaxOneAfterDecimal(DAMAGE_REDUCTION) + "%";
        return null;
    }

}
