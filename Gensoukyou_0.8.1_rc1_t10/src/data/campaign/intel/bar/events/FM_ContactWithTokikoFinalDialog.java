package data.campaign.intel.bar.events;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity;
import com.fs.starfarer.api.plugins.OfficerLevelupPlugin;
import com.fs.starfarer.api.util.Misc;
import data.utils.FM_Colors;
import data.utils.FM_Misc;
import data.utils.FM_Person;
import data.utils.I18nUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FM_ContactWithTokikoFinalDialog implements InteractionDialogPlugin {

    public InteractionDialogAPI dialog;
    public PersonAPI person;

    protected Map<String, MemoryAPI> memoryMap;
//    protected PersonAPI officer;
//    protected OfficerDataAPI officer_data;

    protected enum OptionId {
        INIT,
        STAGE1,
        STAGE2,
        STAGE3A,
        STAGE3B,
        STAGE3C,
        STAGE4,
        STAGE5,
        STAGE6,
        DONE,
    }

    @Override
    public void init(InteractionDialogAPI dialog) {

        if (person == null) {
            person = FM_Person.getPerson(I18nUtil.getString("person","FM_TokikoId"));
            person.setPortraitSprite(Global.getSettings().getSpriteName("intel", "FM_Tokiko"));
            person.setName(new FullName("\"Unnamed\"", "Tokiko", FullName.Gender.FEMALE));
            person.setRankId("FM_cadetGraduate");
            FM_Misc.changeRelPerson(person,0.5f, dialog.getTextPanel());
        }
//
//        if (person == null) {
//
//            person = Global.getFactory().createPerson();
//            person.setPortraitSprite(Global.getSettings().getSpriteName("intel", "FM_Tokiko"));
//            person.setName(new FullName("\"Unnamed\"", "Tokiko", FullName.Gender.FEMALE));
//            person.setRankId("FM_cadetGraduate");
//            person.getRelToPlayer().setRel(0.5f);
//        }
        this.dialog = dialog;

        dialog.getVisualPanel().showPersonInfo(person, false);

        optionSelected(null, OptionId.INIT);

    }

    @Override
    public void optionSelected(String optionText, Object optionData) {

        if (!(optionData instanceof OptionId)) {
            return;
        }
        OptionId option = (OptionId) optionData;

        OptionPanelAPI options = dialog.getOptionPanel();
        TextPanelAPI text = dialog.getTextPanel();
        options.clearOptions();

        if (optionText != null) {
            text.addPara(optionText, Misc.getBasePlayerColor());
        }

        switch (option) {

            case INIT: {
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEventFinal_INIT_TEXT_0"));

                options.addOption(I18nUtil.getString("event", "FM_TokikoBarEventFinal_INIT_STAGE1"), OptionId.STAGE1);
                break;
            }
            case STAGE1: {
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEventFinal_STAGE1_TEXT_0"), FM_Colors.FM_TEXT_BLUE);
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEventFinal_STAGE1_TEXT_1"));
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEventFinal_STAGE1_TEXT_2"), FM_Colors.FM_TEXT_BLUE);
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEventFinal_STAGE1_TEXT_3"));
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEventFinal_STAGE1_TEXT_4"));
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEventFinal_STAGE1_TEXT_5"), FM_Colors.FM_TEXT_BLUE);

                options.addOption(I18nUtil.getString("event", "FM_TokikoBarEventFinal_STAGE1_STAGE2"), OptionId.STAGE2);
                break;
            }
            case STAGE2: {
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEventFinal_STAGE2_TEXT_0"));
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEventFinal_STAGE2_TEXT_1"));
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEventFinal_STAGE2_TEXT_2"));
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEventFinal_STAGE2_TEXT_3"), FM_Colors.FM_TEXT_BLUE);
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEventFinal_STAGE2_TEXT_4"));
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEventFinal_STAGE2_TEXT_5"), FM_Colors.FM_TEXT_BLUE);

                options.addOption(I18nUtil.getString("event", "FM_TokikoBarEventFinal_STAGE2_STAGE3A"), OptionId.STAGE3A);
                options.addOption(I18nUtil.getString("event", "FM_TokikoBarEventFinal_STAGE2_STAGE3B"), OptionId.STAGE3B);
                options.addOption(I18nUtil.getString("event", "FM_TokikoBarEventFinal_STAGE2_STAGE3C"), OptionId.STAGE3C);
                break;
            }
            case STAGE3A: {
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEventFinal_STAGE3A_TEXT_0"), FM_Colors.FM_TEXT_BLUE);
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEventFinal_STAGE3A_TEXT_1"));
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEventFinal_STAGE3A_TEXT_2"), FM_Colors.FM_TEXT_BLUE);
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEventFinal_STAGE3A_TEXT_3"), FM_Colors.FM_TEXT_BLUE);
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEventFinal_STAGE3A_TEXT_4"));
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEventFinal_STAGE3A_TEXT_5"), FM_Colors.FM_TEXT_BLUE);
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEventFinal_STAGE3A_TEXT_6"), FM_Colors.FM_TEXT_BLUE);
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEventFinal_STAGE3A_TEXT_7"));

                options.addOption(I18nUtil.getString("event", "FM_TokikoBarEventFinal_STAGE3ABC_STAGE4"), OptionId.STAGE4);
                break;
            }
            case STAGE3B: {
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEventFinal_STAGE3B_TEXT_0"), FM_Colors.FM_TEXT_BLUE);
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEventFinal_STAGE3B_TEXT_1"));
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEventFinal_STAGE3B_TEXT_2"), FM_Colors.FM_TEXT_BLUE);
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEventFinal_STAGE3B_TEXT_3"), FM_Colors.FM_TEXT_BLUE);
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEventFinal_STAGE3B_TEXT_4"));
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEventFinal_STAGE3B_TEXT_5"), FM_Colors.FM_TEXT_BLUE);
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEventFinal_STAGE3B_TEXT_6"), FM_Colors.FM_TEXT_BLUE);

                options.addOption(I18nUtil.getString("event", "FM_TokikoBarEventFinal_STAGE3ABC_STAGE4"), OptionId.STAGE4);
                break;
            }
            case STAGE3C: {
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEventFinal_STAGE3C_TEXT_0"), FM_Colors.FM_TEXT_BLUE);
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEventFinal_STAGE3C_TEXT_1"));
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEventFinal_STAGE3C_TEXT_2"), FM_Colors.FM_TEXT_BLUE);
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEventFinal_STAGE3C_TEXT_3"), FM_Colors.FM_TEXT_BLUE);
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEventFinal_STAGE3C_TEXT_4"));
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEventFinal_STAGE3C_TEXT_5"), FM_Colors.FM_TEXT_BLUE);
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEventFinal_STAGE3C_TEXT_6"), FM_Colors.FM_TEXT_BLUE);
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEventFinal_STAGE3C_TEXT_7"));
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEventFinal_STAGE3C_TEXT_8"), FM_Colors.FM_TEXT_BLUE);

                options.addOption(I18nUtil.getString("event", "FM_TokikoBarEventFinal_STAGE3ABC_STAGE4"), OptionId.STAGE4);
                break;
            }
            case STAGE4: {
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEventFinal_STAGE4_TEXT_0"), FM_Colors.FM_TEXT_BLUE);
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEventFinal_STAGE4_TEXT_1"));
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEventFinal_STAGE4_TEXT_2"), FM_Colors.FM_TEXT_BLUE);
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEventFinal_STAGE4_TEXT_3"));
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEventFinal_STAGE4_TEXT_4"));

                options.addOption(I18nUtil.getString("event", "FM_TokikoBarEventFinal_STAGE4_STAGE5"), OptionId.STAGE5);
                break;
            }
            case STAGE5: {
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEventFinal_STAGE5_TEXT_0"), FM_Colors.FM_TEXT_BLUE);
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEventFinal_STAGE5_TEXT_1"));
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEventFinal_STAGE5_TEXT_2"), FM_Colors.FM_TEXT_BLUE);
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEventFinal_STAGE5_TEXT_3"));
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEventFinal_STAGE5_TEXT_4"));

                options.addOption(I18nUtil.getString("event", "FM_TokikoBarEventFinal_STAGE5_STAGE6"), OptionId.STAGE6);
                break;
            }
            case STAGE6: {
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEventFinal_STAGE6_TEXT_0"));
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEventFinal_STAGE6_TEXT_1"));
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEventFinal_STAGE6_TEXT_2"));
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEventFinal_STAGE6_TEXT_3"), FM_Colors.FM_TEXT_BLUE);


                options.addOption(I18nUtil.getString("event", "FM_TokikoBarEventFinal_STAGE6_DONE"), OptionId.DONE);
                //军官生成相关内容
                List<String> tokiko = new ArrayList<>();
                tokiko.add(Skills.DAMAGE_CONTROL);
                tokiko.add(Skills.COMBAT_ENDURANCE);
                tokiko.add("FM_Tokiko_skill");

                //testing
                //doExtraConfirmActions();

//                officer = Global.getFactory().createPerson();
//                officer_data = Global.getFactory().createOfficerData(officer);
//                OfficerLevelupPlugin plugin = (OfficerLevelupPlugin) Global.getSettings().getPlugin("officerLevelUp");
//
//                officer.getStats().addXP(plugin.getXPForLevel(1));
//
//                officer.setPersonality(Personalities.AGGRESSIVE);
//                officer.setName(person.getName());
//                officer.setPortraitSprite(person.getPortraitSprite());
//                officer.setGender(person.getGender());
//                officer.setRankId("FM_cadetGraduate");
//                //一个特别的Tag用于判断
//                officer.addTag(FM_ContactWithTokiko.TokikoOfficerTag);
//
////                if (!Global.getSector().getMemoryWithoutUpdate().contains(officerId)){
////                    Global.getSector().getMemoryWithoutUpdate().set(officerId,officer);
////                }
//
//                CampaignFleetAPI player_fleet = Global.getSector().getPlayerFleet();
//                player_fleet.getFleetData().addOfficer(officer);
//                AddRemoveCommodity.addOfficerGainText(officer, text);

                for (String tokikoSkill : tokiko) {
                    person.getStats().setSkillLevel(tokikoSkill, 1);
                }

                OfficerLevelupPlugin plugin = (OfficerLevelupPlugin) Global.getSettings().getPlugin("officerLevelUp");
                person.getStats().addXP(plugin.getXPForLevel(1));
                person.setPersonality(Personalities.AGGRESSIVE);
                CampaignFleetAPI player_fleet = Global.getSector().getPlayerFleet();
                player_fleet.getFleetData().addOfficer(person);
                AddRemoveCommodity.addOfficerGainText(person, text);
                FM_Misc.changeRelPerson(person,0.6f,text);
                //FM_Misc.addRating(2,text);
                //一个特别的Tag用于判断
                person.addTag(FM_ContactWithTokiko.TokikoOfficerTag);
                break;
            }

            case DONE: {

                dialog.dismiss();
                break;
            }
        }

    }

    @Override
    public void advance(float amount) {
    }

    @Override
    public void backFromEngagement(EngagementResultAPI battleResult) {
    }

    @Override
    public void optionMousedOver(String optionText, Object optionData) {
    }

    public Object getContext() {
        return null;
    }

    public Map<String, MemoryAPI> getMemoryMap() {
        return memoryMap;
    }
}
