package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.utils.I18nUtil;
import org.lwjgl.input.Keyboard;
import org.magiclib.util.MagicIncompatibleHullmods;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class FantasyTraditionalMod extends BaseHullMod {

    public static float PEAK_DEBUFF = 0.5f;
    public static float WEAPON_FLUX_BONUS = 0.5f;

    public static final Color TRAIL_BEGIN = new Color(234, 71, 71, 52);
    public static final Color TRAIL_END = new Color(224, 36, 36, 255);

    public static final float CR_PER_DEBUFF = 25f;


    public static Map<ShipAPI.HullSize, Float> RANGES = new HashMap();

    static {

        RANGES.put(ShipAPI.HullSize.FRIGATE, 300f);
        RANGES.put(ShipAPI.HullSize.DESTROYER, 400f);

        RANGES.put(ShipAPI.HullSize.CRUISER, 0f);
        RANGES.put(ShipAPI.HullSize.CAPITAL_SHIP, 0f);
        RANGES.put(ShipAPI.HullSize.FIGHTER, 0f);

    }

    public static final float RANGE_CEIL = 1200f;
    public static final float OUT_OF_RANGE = 0.1f;


    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {


        if (hullSize == ShipAPI.HullSize.CAPITAL_SHIP || hullSize == ShipAPI.HullSize.CRUISER) {
            Global.getLogger(this.getClass()).warn(hullSize);
            if (stats.getEntity() instanceof ShipAPI) {
                ShipAPI ship = (ShipAPI) stats.getEntity();
                Global.getLogger(this.getClass()).info(ship.getHullSpec().getHullId());
                Global.getLogger(this.getClass()).info(ship.getHullSpec().getHullId());
            }
            return;
        }

        stats.getEnergyWeaponRangeBonus().modifyFlat(id, RANGES.get(hullSize));
        stats.getWeaponRangeThreshold().modifyFlat(id, RANGE_CEIL);
        stats.getWeaponRangeMultPastThreshold().modifyMult(id, OUT_OF_RANGE);

        stats.getPeakCRDuration().modifyMult(id, PEAK_DEBUFF);

        stats.getEnergyWeaponFluxCostMod().modifyMult(id, 1 - WEAPON_FLUX_BONUS);

        stats.getCRLossPerSecondPercent().modifyPercent(id, CR_PER_DEBUFF);


        if (stats.getVariant().getHullMods().contains("advancedoptics")) {

            MagicIncompatibleHullmods.removeHullmodWithWarning(stats.getVariant(), "advancedoptics", "FantasyTraditionalMod");
            MagicIncompatibleHullmods.removeHullmodWithWarning(stats.getVariant(), "FantasyTraditionalMod", "advancedoptics");

        }

        if (stats.getVariant().getHullMods().contains("FantasyBulletMod")) {

            MagicIncompatibleHullmods.removeHullmodWithWarning(stats.getVariant(), "FantasyBulletMod", "FantasyTraditionalMod");
            MagicIncompatibleHullmods.removeHullmodWithWarning(stats.getVariant(), "FantasyTraditionalMod", "FantasyBulletMod");

        }


    }


    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {

//        ShipAPI.HullSize hullSize = ship.getHullSize();
//        ShipAPI player = Global.getCombatEngine().getPlayerShip();

        CombatEngineAPI engine = Global.getCombatEngine();

        if (ship == null) return;
        if (engine == null) return;


        if (!ship.isAlive()) {
            return;
        }

        ship.getEngineController().fadeToOtherColor(ship, TRAIL_END, TRAIL_BEGIN, 1f, 2f);



/*
        //id一样的情况下导致了替代.......所以.......
        String id = ship.getFleetMemberId() + "_FTM";


        Vector2f center = ship.getLocation();
        List<CombatEntityAPI> entities = CombatUtils.getEntitiesWithinRange(center, (Float) FantasyBasicMod.magRANGE.get(hullSize));
        List<CombatEntityAPI> project_in_range = new ArrayList<>();

        for (CombatEntityAPI project : entities) {
            if (project.getOwner() != ship.getOwner() && project instanceof DamagingProjectileAPI) {
                project_in_range.add(project);
            }
        }
        int BULLET_NUMBER = project_in_range.size();


        if (BULLET_NUMBER > 5) {

            float mult = N_LEVEL_1;

            ship.getMutableStats().getAcceleration().modifyMult(id, mult);
            ship.getMutableStats().getDeceleration().modifyMult(id, mult);
            ship.getMutableStats().getTurnAcceleration().modifyMult(id, mult);
            ship.getMutableStats().getMaxTurnRate().modifyMult(id, mult);

            ship.getMutableStats().getMaxSpeed().modifyMult(id, mult);

            if (ship == player) {
                Global.getCombatEngine().maintainStatusForPlayerShip(INFO, Global.getSettings().getSpriteName("ui", "icon_kinetic"), "传统增益", "最高航速与机动性乘" + mult, false);
            }

            ship.addAfterimage(DANGER_LEVEL_1, 0, 0, -ship.getVelocity().x * 0.1f, -ship.getVelocity().y * 0.1f, 1f, 0f, 0.1f, 0.5f,
                    true, true, false);

            ship.setJitterUnder(id, DANGER_LEVEL_1, 2, 3, 1);
        } else {

            ship.getMutableStats().getAcceleration().unmodify(id);
            ship.getMutableStats().getDeceleration().unmodify(id);
            ship.getMutableStats().getTurnAcceleration().unmodify(id);
            ship.getMutableStats().getMaxTurnRate().unmodify(id);

            ship.getMutableStats().getMaxSpeed().unmodify(id);

        }
*/

    }


    public String getUnapplicableReason(ShipAPI ship) {
        if (!ship.getVariant().hasHullMod(FantasyBasicMod.FANTASYBASICMOD)) {
            return I18nUtil.getHullModString("FM_HullModRequireBasicMod");
        }
        //插件冲突
        if (ship.getVariant().hasHullMod("advancedoptics") || ship.getVariant().hasHullMod("FantasyBulletMod")) {
            return I18nUtil.getHullModString("FM_HullModProblem");
        }
        if (!(ship.getHullSize().equals(ShipAPI.HullSize.FRIGATE) || ship.getHullSize().equals(ShipAPI.HullSize.DESTROYER))) {
            return I18nUtil.getHullModString("FantasyTraditionalMod_SpecialRequire");
        }
        return null;
    }

    public boolean isApplicableToShip(ShipAPI ship) {
        if (ship.getVariant().hasHullMod(FantasyBasicMod.FANTASYBASICMOD)) {
            return ship.getHullSize().equals(ShipAPI.HullSize.FRIGATE) || ship.getHullSize().equals(ShipAPI.HullSize.DESTROYER);
        } else return false;
    }

    @Override

    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        tooltip.addSpacer(10f);
        //射程修正说明
        tooltip.addSectionHeading(I18nUtil.getHullModString("FantasyTraditionalMod_RangeInstruction"), Alignment.TMID, 4f);
        tooltip.addPara(I18nUtil.getHullModString("FantasyTraditionalMod_RangeInstruction_0"), Misc.getHighlightColor(), 4f);
        tooltip.addPara(I18nUtil.getHullModString("FantasyTraditionalMod_RangeInstruction_1"), 4f, Misc.getHighlightColor(),
                String.valueOf(RANGES.get(ShipAPI.HullSize.FRIGATE).intValue()), String.valueOf(RANGES.get(ShipAPI.HullSize.DESTROYER).intValue()));
        tooltip.addPara(I18nUtil.getHullModString("FantasyTraditionalMod_RangeInstruction_2"), 4f, Misc.getHighlightColor(),
                String.valueOf((int) RANGE_CEIL), String.valueOf(OUT_OF_RANGE));
        //限制与其他功能说明
        tooltip.addSectionHeading(I18nUtil.getHullModString("FantasyTraditionalMod_SP"), Alignment.TMID, 4f);
        if (Keyboard.isKeyDown(Keyboard.getKeyIndex("F1"))) {
        tooltip.addPara(I18nUtil.getHullModString("FantasyTraditionalMod_SP_0"), Misc.getTextColor(), 4f);
        tooltip.addPara(I18nUtil.getHullModString("FantasyTraditionalMod_SP_1"), Misc.getTextColor(), 4f);
        tooltip.addPara(I18nUtil.getHullModString("FantasyTraditionalMod_SP_2"), Misc.getGrayColor(), 4f);
        tooltip.addSpacer(10f);
    }
        if (!Keyboard.isKeyDown(Keyboard.getKeyIndex("F1"))) {
        tooltip.addPara("Press and hold [%s] to view this information.", Float.valueOf(10.0f), Misc.getGrayColor(), Misc.getStoryBrightColor(), new String[]{"F1"}).setAlignment(Alignment.MID);
    }
        //描述与评价
        tooltip.addSectionHeading(I18nUtil.getHullModString("FM_DescriptionAndEvaluation"), Alignment.TMID, 4f);
        if (Keyboard.isKeyDown(Keyboard.getKeyIndex("F1"))) {
        tooltip.addPara(I18nUtil.getHullModString("FantasyTraditionalMod_DAE_0")
                , Misc.getTextColor(), 4f);
        tooltip.addSpacer(10f);
        tooltip.addPara(I18nUtil.getHullModString("FantasyTraditionalMod_DAE_1"), Misc.getGrayColor(), 4f);
    }
        if (!Keyboard.isKeyDown(Keyboard.getKeyIndex("F1"))) {
        tooltip.addPara("Press and hold [%s] to view this information.", Float.valueOf(10.0f), Misc.getGrayColor(), Misc.getStoryBrightColor(), new String[]{"F1"}).setAlignment(Alignment.MID);
    }
    }

    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) return (int) (PEAK_DEBUFF * 100f) + "%";
        if (index == 1) return "" + (int) (CR_PER_DEBUFF) + "%";
        if (index == 2) return I18nUtil.getHullModString("FantasyTraditionalMod_HL_3");
        if (index == 3) return (int) ((WEAPON_FLUX_BONUS) * 100f) + "%";
        return null;
    }


}
