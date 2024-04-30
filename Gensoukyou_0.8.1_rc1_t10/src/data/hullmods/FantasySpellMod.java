package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.utils.FM_Misc;
import data.utils.I18nUtil;
import org.magiclib.util.MagicUI;

public class FantasySpellMod extends BaseHullMod {

    public static final String SpellModId = "FantasySpellMod";
    public static final float SpellAttenuation = 0.01f;

    public static String KeyForUi = "FantasySpellMod_UI";

    //private float uiTimer = 0f;

//    private boolean uiOn = false;

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        super.advanceInCombat(ship, amount);

        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine == null) return;

        SpellModState state = FM_Misc.getSpellModState(engine, ship);

        if (!ship.isAlive()) return;

        state.spellPower = state.spellPower - engine.getElapsedInLastFrame() * SpellAttenuation;

        if (state.spellPower >= 1) {
            state.spellPower = 1f;
        }

        if (state.spellPower < 0) {
            state.spellPower = 0;
        }
        if (ship == Global.getCombatEngine().getPlayerShip()){
//            MagicUI.drawInterfaceStatusBar(
//                    ship,
//                    KeyForUi,
//                    MagicUI.getInterfaceOffsetFromStatusBars(ship,ship.getVariant()),
//                    state.spellPower,
//                    MagicUI.GREENCOLOR,
//                    MagicUI.GREENCOLOR,
//                    0f,
//                    I18nUtil.getHullModString("FantasySpellMod_StatusBarString"),
//                    (int) (state.spellPower * 100f)
//            );

            MagicUI.drawHUDStatusBar(
                    ship,
                    state.spellPower,
                    MagicUI.GREENCOLOR,
                    MagicUI.GREENCOLOR,
                    0f,
                    I18nUtil.getHullModString("FantasySpellMod_StatusBarString"),
                    Misc.getRoundedValueOneAfterDecimalIfNotWhole(state.spellPower * 100f) + "%",
                    false
            );

        }
    }

    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        tooltip.addSpacer(10f);
        //描述与评价
        tooltip.addSectionHeading(I18nUtil.getHullModString("FM_DescriptionAndEvaluation"), Alignment.TMID, 4f);
        tooltip.addPara(I18nUtil.getHullModString("FantasySpellMod_DAE_0"), Misc.getTextColor(), 4f);
        tooltip.addSpacer(10f);
        tooltip.addPara(I18nUtil.getHullModString("FantasySpellMod_DAE_1"), Misc.getGrayColor(), 4f);
    }

    public final static class SpellModState {
        public float spellPower;

        public SpellModState() {
            spellPower = 0f;
        }
    }

    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) return I18nUtil.getHullModString("FantasySpellMod_Des");
        if (index == 1) return (int) (SpellAttenuation * 100f) + "%";
        return null;
    }


}
