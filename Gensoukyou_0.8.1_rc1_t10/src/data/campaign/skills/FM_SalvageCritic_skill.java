package data.campaign.skills;

import com.fs.starfarer.api.characters.FleetStatsSkillEffect;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.SkillSpecAPI;
import com.fs.starfarer.api.fleet.MutableFleetStatsAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.skills.BaseSkillEffectDescription;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import data.utils.I18nUtil;

public class FM_SalvageCritic_skill {

    public static final float NOT_RARE_MULT = 0.5f;
    public static final float TOTAL_BONUS = 0.5f;

    public static class Level1 extends BaseSkillEffectDescription implements FleetStatsSkillEffect {

        public void createCustomDescription(MutableCharacterStatsAPI stats, SkillSpecAPI skill,
                                            TooltipMakerAPI info, float width) {
            init(stats, skill);

            //float effect = computeBonusForFMCombatShips(data, stats, "FM_ShieldExpand_skill", SHIELD_ARC_EXPAND);
            info.addPara(I18nUtil.getString("skill", "FM_SalvageCriticInfo"), 0f, hc, hc,
                    (int) (TOTAL_BONUS * 100f) + "%",
                    (int) (NOT_RARE_MULT * 100f) + "%");
        }

        @Override
        public void apply(MutableFleetStatsAPI stats, String id, float level) {
            stats.getDynamic().getStat(Stats.SALVAGE_VALUE_MULT_FLEET_NOT_RARE).modifyMult(id,NOT_RARE_MULT);
            stats.getDynamic().getStat(Stats.SALVAGE_VALUE_MULT_FLEET_INCLUDES_RARE).modifyFlat(id,TOTAL_BONUS);
        }

        @Override
        public void unapply(MutableFleetStatsAPI stats, String id) {
            stats.getDynamic().getStat(Stats.SALVAGE_VALUE_MULT_FLEET_NOT_RARE).unmodify(id);
            stats.getDynamic().getStat(Stats.SALVAGE_VALUE_MULT_FLEET_INCLUDES_RARE).unmodify(id);
        }
    }
    
}
