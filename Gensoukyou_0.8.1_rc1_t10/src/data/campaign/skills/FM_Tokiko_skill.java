package data.campaign.skills;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetDataAPI;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.OfficerDataAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.characters.ShipSkillEffect;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.plugins.OfficerLevelupPlugin;
import data.utils.I18nUtil;

public class FM_Tokiko_skill {

    public static final int DP_BOUND = 5;
    public static final int DP_BONUS = 1;
    public static String FM_Tokiko_skill_buffId = "_FM_Tokiko_skill_buffId";

    public static class Level1 implements ShipSkillEffect {

        public void apply(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id, float level) {

            try {
                FleetMemberAPI member = stats.getFleetMember();
                FleetDataAPI fleet = member.getFleetData();

                if (fleet.getCommander().isPlayer()) {
                    PersonAPI player = fleet.getCommander();
                    if (!player.getStats().hasSkill("FM_Tokiko_skillCom")) {
                        fleet.getCommander().getStats().increaseSkill("FM_Tokiko_skillCom");
                    } else {
                    }
                }
            } catch (Exception ignored) {
            }

        }

        public void unapply(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {

        }

        public String getEffectDescription(float level) {
            return "-" + (DP_BONUS) + I18nUtil.getString("skill", "FM_TokikoSkillInfo0");
        }

        public String getEffectPerLevelDescription() {
            return null;
        }

        public ScopeDescription getScopeDescription() {
            return ScopeDescription.ALL_SHIPS;
        }
    }

    public static void backDoorTokiko() {// runcode data.campaign.skills.FM_Tokiko_skill.backDoorTokiko()
        PersonAPI officer = Global.getFactory().createPerson();
        OfficerDataAPI officer_data = Global.getFactory().createOfficerData(officer);
        officer.setPortraitSprite(Global.getSector().getPlayerPerson().getPortraitSprite());
        officer.getStats().setSkillLevel("FM_Tokiko_skill", 1);
        OfficerLevelupPlugin plugin = (OfficerLevelupPlugin) Global.getSettings().getPlugin("officerLevelUp");
        officer.getStats().addXP(plugin.getXPForLevel(1));
        officer.setPersonality(Personalities.AGGRESSIVE);
        officer.setName(new FullName("\"Unnamed\"", "Tokiko", FullName.Gender.FEMALE));
        officer.setRankId("FM_cadetGraduate");
        CampaignFleetAPI player_fleet = Global.getSector().getPlayerFleet();
        player_fleet.getFleetData().addOfficer(officer);

    }

}
