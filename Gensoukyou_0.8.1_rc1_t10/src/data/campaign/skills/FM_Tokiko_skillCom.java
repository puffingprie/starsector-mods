package data.campaign.skills;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FleetDataAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.characters.ShipSkillEffect;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class FM_Tokiko_skillCom {

    public static String FM_TokikoSkillCheck = "$FM_TokikoSkillCheck";

    public static class Level1 implements ShipSkillEffect {

        public void apply(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id, float level) {

            boolean officerCheck = false;
            if (Global.getSector().getMemoryWithoutUpdate().contains(FM_TokikoSkillCheck)) {
                officerCheck = true;
            } else {
                try {
                    FleetDataAPI data = stats.getFleetMember().getFleetData();
                    for (FleetMemberAPI all : data.getMembersListCopy()) {
                        PersonAPI officer = all.getCaptain();
                        if (officer == null || officer.isDefault()) continue;

                        if (officer.getStats().hasSkill("FM_Tokiko_skill")) {
                            officerCheck = true;
                            Global.getSector().getMemoryWithoutUpdate().set(FM_TokikoSkillCheck, true);
                            break;
                        }
                    }
                } catch (Exception ignored) {
                }
            }


            if (officerCheck && stats.getSuppliesToRecover().getBaseValue() >= FM_Tokiko_skill.DP_BOUND) {
                stats.getDynamic().getMod(Stats.DEPLOYMENT_POINTS_MOD).modifyFlat(FM_Tokiko_skill.FM_Tokiko_skill_buffId, -FM_Tokiko_skill.DP_BONUS);
            } else {
                stats.getDynamic().getMod(Stats.DEPLOYMENT_POINTS_MOD).unmodifyFlat(FM_Tokiko_skill.FM_Tokiko_skill_buffId);
            }
        }

        public void unapply(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
            Global.getSector().getMemoryWithoutUpdate().expire(FM_TokikoSkillCheck, 0f);
            stats.getDynamic().getMod(Stats.DEPLOYMENT_POINTS_MOD).unmodifyFlat(FM_Tokiko_skill.FM_Tokiko_skill_buffId);

        }

        public String getEffectDescription(float level) {
            return "";
        }

        public String getEffectPerLevelDescription() {
            return null;
        }

        public ScopeDescription getScopeDescription() {
            return ScopeDescription.ALL_SHIPS;
        }
    }
}
