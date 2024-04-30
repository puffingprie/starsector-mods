package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.shipsystems.FM_PhaseRumia;
import data.utils.I18nUtil;

public class FantasyRumiaMod extends BaseHullMod {

//    public FM_MaskAndGlow.FM_HullGlowParam param = null;
//    public FM_MaskAndGlow manager = null;
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        tooltip.addSpacer(10f);
        //描述与评价
        tooltip.addSectionHeading(I18nUtil.getHullModString("FM_DescriptionAndEvaluation"), Alignment.TMID, 4f);
        tooltip.addPara(I18nUtil.getHullModString("FantasyRumiaMod_DAE_0"), Misc.getTextColor(), 4f);
        tooltip.addSpacer(10f);
        tooltip.addPara(I18nUtil.getHullModString("FantasyRumiaMod_DAE_1"), Misc.getGrayColor(), 4f);
    }

//    @Override
//    public void advanceInCombat(ShipAPI ship, float amount) {
//        if (manager == null){
//            manager = FM_ParticleManager.getMaskAndGlowTestManager(Global.getCombatEngine());
//        }
//        if (param == null){
//            param = manager.addMaskAndFlow(
//                    ship.getLocation(),
//                    0.2f,0.8f,0.2f,
//                    Color.WHITE,
//                    FM_Colors.FM_PURPLE_RED_SPRITE,
//                    200f,
//                    30f,
//                    ship.getFacing(),
//                    1f,
//                    Global.getSettings().getSprite("misc","rings_ice0"),
//                    Global.getSettings().getSprite(Global.getSettings().getHullSpec("FM_Rumia").getSpriteName())
//                    );
//            manager.addParamToRender(param);
//        } else {
//            manager.setLocation(ship.getLocation(),param);
//        }
//    }

    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) return "" + FM_PhaseRumia.DAMAGE_TO_SPELL + "%";
        if (index == 1) return I18nUtil.getHullModString("FantasySpellMod_Des");
        return null;
    }
}
