package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.combat.listeners.DamageTakenModifier;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.utils.FM_Colors;
import data.utils.FM_Misc;
import data.utils.I18nUtil;
import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicIncompatibleHullmods;

import java.awt.*;

public class FantasyMirrorProtocolMod extends BaseHullMod {
    public static final float SPEED_EFFECT = 50f;
    public static final float ACC_EFFECT = 100f;
//    public static final float EFFECT_IN_TIME = 2f;
    public static final float EFFECT_OUT_TIME = 5f;

    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        super.applyEffectsBeforeShipCreation(hullSize, stats, id);
        if (stats.getVariant().getHullMods().contains("unstable_injector")) {
            MagicIncompatibleHullmods.removeHullmodWithWarning(stats.getVariant(), "FantasyMirrorProtocolMod", "unstable_injector");
        }
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        super.applyEffectsAfterShipCreation(ship, id);
        ship.addListener(new BeamDamageComputeAndBuffEffect(ship));
    }

    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) return "" + (int)(SPEED_EFFECT) + "%";
        if (index == 1) return "" + (int)(ACC_EFFECT) + "%";
        return null;
    }

    public String getUnapplicableReason(ShipAPI ship) {
        if (!ship.getVariant().hasHullMod(FantasyBasicMod.FANTASYBASICMOD)) {
            return I18nUtil.getHullModString("FM_HullModRequireBasicMod");
        }
        //插件冲突
        if (ship.getVariant().hasHullMod("unstable_injector")) {
            return I18nUtil.getHullModString("FM_HullModProblem");
        }
        return null;
    }

    public boolean isApplicableToShip(ShipAPI ship) {
        return ship.getVariant().hasHullMod(FantasyBasicMod.FANTASYBASICMOD);
    }

    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        tooltip.addSpacer(10f);
        //说明
        tooltip.addSectionHeading(I18nUtil.getHullModString("FM_Instruction"), Alignment.TMID, 4f);
        tooltip.addPara(I18nUtil.getHullModString("FantasyMirrorProtocolMod_I_0"), 4f, Misc.getHighlightColor(), EFFECT_OUT_TIME + "s");
        tooltip.addPara(I18nUtil.getHullModString("FantasyMirrorProtocolMod_I_1"), Misc.getTextColor(), 4f);
        tooltip.addSpacer(10f);

        //描述与评价
        tooltip.addSectionHeading(I18nUtil.getHullModString("FM_DescriptionAndEvaluation"), Alignment.TMID, 4f);
        if (Keyboard.isKeyDown(Keyboard.getKeyIndex("F1"))) {
        tooltip.addPara(I18nUtil.getHullModString("FantasyMirrorProtocolMod_DAE_0"), Misc.getTextColor(), 4f);
        tooltip.addSpacer(10f);
        tooltip.addPara(I18nUtil.getHullModString("FantasyMirrorProtocolMod_DAE_1"), Misc.getGrayColor(), 4f);
    }
        if (!Keyboard.isKeyDown(Keyboard.getKeyIndex("F1"))) {
        tooltip.addPara("Press and hold [%s] to view this information.", Float.valueOf(10.0f), Misc.getGrayColor(), Misc.getStoryBrightColor(), new String[]{"F1"}).setAlignment(Alignment.MID);
    }
        tooltip.addPara(I18nUtil.getHullModString("FantasyMirrorProtocolMod_DAE_2"), Misc.getNegativeHighlightColor(), 4f);
    }


    public static class BeamDamageComputeAndBuffEffect implements DamageTakenModifier, AdvanceableListener {

        public ShipAPI ship;
        public float speedEffectPercent;
        public float accEffectPercent;
        public Color innerShieldColor;
        public String buffId;
        private float effectLevel = 0f;
        private float timerForVisual = 0f;
        public Object Key = new Object();
        public BeamDamageComputeAndBuffEffect(ShipAPI ship){
            this.ship = ship;
            this.buffId = ship.getId() + "_FantasyBeamBuffListener";
            if (ship.getShield() != null){
                this.innerShieldColor = ship.getShield().getInnerColor();
            }else {
                this.innerShieldColor = FM_Colors.FM_TEXT_RED;
            }

        }

        @Override
        public String modifyDamageTaken(Object param, CombatEntityAPI target, DamageAPI damage, Vector2f point, boolean shieldHit) {
            if (!damage.isDps()) {
                return null;
            }
            if (!(param instanceof BeamAPI))return null;
            float damageAmount = damage.getDamage();
            speedEffectPercent = SPEED_EFFECT;
            accEffectPercent = ACC_EFFECT;

            //debug
            //Global.getCombatEngine().addFloatingText(ship.getLocation(),"" + damageAmount,20f, Color.WHITE,ship,0f,0f);
            effectLevel = effectLevel + damageAmount/ship.getMutableStats().getFluxDissipation().modified * 1.5f * Global.getCombatEngine().getElapsedInLastFrame();
            return null;
        }

        @Override
        public void advance(float amount) {
            effectLevel = effectLevel - 1/EFFECT_OUT_TIME * amount;
            if (effectLevel >= 1f){
                effectLevel = 1f;
            }
            if (effectLevel <= 0f){
                effectLevel = 0f;
            }

            if (effectLevel <= 0){
                ship.getMutableStats().getMaxSpeed().unmodifyPercent(buffId);
                ship.getMutableStats().getAcceleration().unmodifyPercent(buffId);
                ship.getMutableStats().getMaxTurnRate().unmodifyPercent(buffId);
                ship.getMutableStats().getTurnAcceleration().unmodifyPercent(buffId);
                ship.getMutableStats().getDeceleration().unmodifyPercent(buffId);
            }else {
                timerForVisual = timerForVisual + amount;
                if (timerForVisual >= 0.1f){
                    timerForVisual = timerForVisual - 0.1f;
                    Global.getCombatEngine().addNebulaParticle(ship.getLocation(), FM_Misc.ZERO,80f * effectLevel,2f,0.3f,0.3f,0.5f, Misc.scaleAlpha(FM_Colors.FM_GREEN_EMP_FRINGE,effectLevel * 0.5f));
                }
                if (ship.getShield() != null){
                    ship.getShield().setInnerColor(
                            Misc.interpolateColor(innerShieldColor,Misc.scaleAlpha(FM_Colors.FM_GREEN_EMP_CORE,0.5f),effectLevel)
                    );
                }

                ship.getMutableStats().getMaxSpeed().modifyPercent(buffId,effectLevel * speedEffectPercent);
                ship.getMutableStats().getAcceleration().modifyPercent(buffId,effectLevel * accEffectPercent);
                ship.getMutableStats().getMaxTurnRate().modifyPercent(buffId,effectLevel * accEffectPercent);
                ship.getMutableStats().getTurnAcceleration().modifyPercent(buffId,effectLevel * accEffectPercent);
                ship.getMutableStats().getDeceleration().modifyPercent(buffId,effectLevel * accEffectPercent);
            }
            if (ship == Global.getCombatEngine().getPlayerShip()){
                Global.getCombatEngine().maintainStatusForPlayerShip(
                        Key,
                        Global.getSettings().getShipSystemSpec("maneuveringjets").getIconSpriteName(),
                        I18nUtil.getHullModString("FantasyMirrorProtocolMod_Info_Title"),
                        I18nUtil.getHullModString("FantasyMirrorProtocolMod_Info_Data") + Misc.getRoundedValueFloat(effectLevel * speedEffectPercent) + "%",
                        false
                );
            }
        }
    }
}
