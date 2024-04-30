package data.campaign.skills;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MonthlyReport;
import com.fs.starfarer.api.campaign.listeners.EconomyTickListener;
import com.fs.starfarer.api.characters.CharacterStatsSkillEffect;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import com.fs.starfarer.api.util.Misc;
import data.utils.I18nUtil;

public class FM_PriceOfLife_skill {

    public static class Level1 implements CharacterStatsSkillEffect {
        @Override
        public String getEffectDescription(float level) {
            return I18nUtil.getString("skill", "FM_PriceOfLifeDes");
        }

        @Override
        public String getEffectPerLevelDescription() {
            return null;
        }

        @Override
        public ScopeDescription getScopeDescription() {
            return ScopeDescription.CUSTOM;
        }

        @Override
        public void apply(MutableCharacterStatsAPI stats, String id, float level) {
            if (!Global.getSector().getListenerManager().hasListenerOfClass(PriceOfLifeCheckMonths.class)) {
                Global.getSector().getListenerManager().addListener(new PriceOfLifeCheckMonths(stats));
                Global.getLogger(this.getClass()).info("FM_PriceOfLife_skill_info");
            }
        }

        @Override
        public void unapply(MutableCharacterStatsAPI stats, String id) {

        }
    }

    public static class PriceOfLifeCheckMonths implements EconomyTickListener {

        public MutableCharacterStatsAPI stats;

        public PriceOfLifeCheckMonths(MutableCharacterStatsAPI stats) {
            this.stats = stats;
        }

        @Override
        public void reportEconomyTick(int iterIndex) {
            SharedData data = SharedData.getData();
            MonthlyReport.FDNode fleet = data.getCurrentReport().getNode(MonthlyReport.FLEET);
            MonthlyReport.FDNode sp = data.getCurrentReport().getNode(fleet, "FM_PriceOfLife");
            float level = Global.getSector().getPlayerPerson().getStats().getLevel();
            float credits = 0f;
            if (Misc.getCommissionIntel() != null) {
                credits = Global.getSettings().getFloat("factionCommissionStipendBase") +
                        Global.getSettings().getFloat("factionCommissionStipendPerLevel") * level;
            }
            int numIter = (int) Global.getSettings().getFloat("economyIterPerMonth");
            sp.income += credits / numIter;
            sp.name = I18nUtil.getString("skill", "FM_PriceOfLifeInfo");
            sp.icon = Global.getSettings().getSkillSpec("FM_PriceOfLife_skill").getSpriteName();
            //Global.getLogger(this.getClass()).info("FM_PriceOfLife_skill_income");
            if (stats.getSkillLevel("FM_PriceOfLife_skill") <= 0) {
                Global.getSector().getListenerManager().removeListener(this);
            }
        }

        @Override
        public void reportEconomyMonthEnd() {

        }
    }
}
