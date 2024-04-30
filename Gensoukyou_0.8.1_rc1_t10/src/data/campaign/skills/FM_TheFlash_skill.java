package data.campaign.skills;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.AfterShipCreationSkillEffect;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.ShipSkillEffect;
import com.fs.starfarer.api.characters.SkillSpecAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.impl.campaign.skills.BaseSkillEffectDescription;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import data.utils.I18nUtil;

public class FM_TheFlash_skill {

    public static final float OVERLOAD_FLUX_MULT = 2f;
    public static final float VENT_BONUS = 50f;
    public static final float DAMAGE_TAKEN_MULT = 0.85f;

    public static class Level1 extends BaseSkillEffectDescription implements AfterShipCreationSkillEffect {

//        public String getEffectDescription(float level) {
//            return I18nUtil.getString("skill","FM_FUMOSInfo1") + DPH_CEIL + I18nUtil.getString("skill","FM_FUMOSInfo2");
//        }

        @Override
        public void createCustomDescription(MutableCharacterStatsAPI stats, SkillSpecAPI skill, TooltipMakerAPI info, float width) {
            init(stats, skill);
            String[] hlight = {"" + (int) OVERLOAD_FLUX_MULT + "x", "" + (int) (VENT_BONUS) + "%"};
            info.addPara(I18nUtil.getString("skill", "FM_TheFlashInfo0"), 0f, hc, hc, hlight);
        }

        public ScopeDescription getScopeDescription() {
            return ScopeDescription.PILOTED_SHIP;
        }

        @Override
        public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
            ship.addListener(new TheFlashListener(ship));
        }

        @Override
        public void unapplyEffectsAfterShipCreation(ShipAPI ship, String id) {
            ship.removeListenerOfClass(TheFlashListener.class);
        }

        @Override
        public void apply(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id, float level) {
            stats.getVentRateMult().modifyPercent(id, VENT_BONUS);
        }

        @Override
        public void unapply(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
            stats.getVentRateMult().unmodify(id);
        }
    }

    public static class Level2 implements ShipSkillEffect {

        public void apply(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id, float level) {
            stats.getHullDamageTakenMult().modifyMult(id, DAMAGE_TAKEN_MULT);
        }

        public void unapply(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
            stats.getHullDamageTakenMult().unmodify(id);
        }

        public String getEffectDescription(float level) {
            return "-" + (int) (100f - 100f * DAMAGE_TAKEN_MULT) + "%" + I18nUtil.getString("skill", "FM_TheFlashInfo1");
        }

        public String getEffectPerLevelDescription() {
            return null;
        }

        public ScopeDescription getScopeDescription() {
            return ScopeDescription.PILOTED_SHIP;
        }

    }

    public static class TheFlashListener implements AdvanceableListener {

        protected ShipAPI ship;

        public TheFlashListener(ShipAPI ship) {
            this.ship = ship;
        }

        @Override
        public void advance(float amount) {
            if (Global.getCombatEngine() == null) return;
            if (Global.getCombatEngine().isPaused()) return;
            if (!ship.isAlive() && ship.getListenerManager().hasListenerOfClass(this.getClass())) {
                ship.removeListenerOfClass(this.getClass());
                return;
            }
//            CombatEngineAPI engine = Global.getCombatEngine();
            if (ship.getFluxTracker().isOverloaded()) {
                ship.getMutableStats().getFluxDissipation().modifyMult(ship.getId() + "_TheFlashListener", OVERLOAD_FLUX_MULT);
            } else {
                ship.getMutableStats().getFluxDissipation().unmodify(ship.getId() + "_TheFlashListener");
            }
        }
    }
}
