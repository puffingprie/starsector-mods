package data.campaign.skills;

import com.fs.starfarer.api.characters.ShipSkillEffect;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import data.utils.I18nUtil;


public class FM_Reimu_skill {

    public static final int SPEED_BONUS = 15;
    public static final int DAMAGE_BONUS = 25;
    public static final int DEFENSE_REDUCTION = 15;


    public static class Level1 implements ShipSkillEffect {

        public void apply(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id, float level) {

            float bonus = 0f;

            if (hullSize == ShipAPI.HullSize.FRIGATE || hullSize == ShipAPI.HullSize.DESTROYER) {
                bonus = SPEED_BONUS;
            }

            //Global.getLogger(this.getClass()).info(hullSize);
            stats.getMaxSpeed().modifyPercent(id, bonus);
        }

        public void unapply(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {

            stats.getMaxSpeed().unmodifyPercent(id);

        }

        public String getEffectDescription(float level) {
            return "+" + (SPEED_BONUS) + I18nUtil.getString("skill", "FM_ReimuSkillInfo0");
        }

        public String getEffectPerLevelDescription() {
            return null;
        }

        public ScopeDescription getScopeDescription() {
            return ScopeDescription.PILOTED_SHIP;
        }
    }

    public static class Level2 implements ShipSkillEffect {

        public void apply(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id, float level) {
            float bonus = 0f;

            if (hullSize == ShipAPI.HullSize.FRIGATE || hullSize == ShipAPI.HullSize.DESTROYER) {
                bonus = DAMAGE_BONUS;
            }
            stats.getDamageToCapital().modifyPercent(id, bonus);
            stats.getDamageToCruisers().modifyPercent(id, bonus);
        }

        public void unapply(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {

            stats.getDamageToCruisers().unmodifyPercent(id);
            stats.getDamageToCapital().unmodifyPercent(id);

        }

        public String getEffectDescription(float level) {
            return "+" + DAMAGE_BONUS + I18nUtil.getString("skill", "FM_ReimuSkillInfo1");
        }

        public String getEffectPerLevelDescription() {
            return null;
        }

        public ScopeDescription getScopeDescription() {
            return ScopeDescription.PILOTED_SHIP;
        }

    }

    public static class Level3 implements ShipSkillEffect {

        public void apply(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id, float level) {
            stats.getHullBonus().modifyMult(id, 1 - DEFENSE_REDUCTION / 100f);
            stats.getArmorBonus().modifyMult(id, 1 - DEFENSE_REDUCTION / 100f);

        }

        public void unapply(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
            stats.getHullBonus().unmodifyMult(id);
            stats.getArmorBonus().unmodifyMult(id);
        }

        public String getEffectDescription(float level) {
            return "-" + DEFENSE_REDUCTION + I18nUtil.getString("skill", "FM_ReimuSkillInfo2");
        }

        public String getEffectPerLevelDescription() {
            return null;
        }

        public ScopeDescription getScopeDescription() {
            return ScopeDescription.PILOTED_SHIP;
        }
    }
}
