package data.campaign.intel.bar.events;


import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.OfficerDataAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.DebugFlags;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BarEventManager;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BaseBarEventWithPerson;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity;
import com.fs.starfarer.api.plugins.OfficerLevelupPlugin;
import data.utils.FM_Colors;
import data.utils.FM_Person;
import data.utils.I18nUtil;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static data.campaign.intel.bar.events.FM_ReimuBarEvent.OptionId.*;

public class FM_ReimuBarEvent extends BaseBarEventWithPerson {


    protected CampaignFleetAPI player_fleet;
    //protected PersonAPI officer;
    protected PersonAPI personSecond;
    protected OfficerDataAPI officer_data;
    protected boolean reimugot=false;
    public static String ReimuKey = "$FM_ReimuCompleted";
    public static String ReimuOfficerTag = "FM_ReimuOfficerTag";
    //testing
    //protected PersonAPI contact;

    public enum OptionId {
        INIT,
        ANSWER,
        CONTACT_1,
        CONTACT_2,
        TELL_STORY_1,
        TELL_STORY_2,
        TELL_STORY_3A,
        TELL_STORY_3B,
        DEAL_1,
        DEAL_2,
        DEAL_3,
        FINAL,
        LEAVE,
    }


    public FM_ReimuBarEvent() {
        super();
    }

    public boolean shouldShowAtMarket(MarketAPI market) {
        if (!super.shouldShowAtMarket(market)) {
            return false;
        }
        if (!FM_Person.hasMetCharacter(I18nUtil.getString("person","FM_TokikoId"))) {
            return false;
        }
        if (Global.getSector().getPlayerFleet().getFleetData().getOfficerData(FM_Person.getPerson(I18nUtil.getString("person","FM_TokikoId")))==null) {
            return false;
        }
        if (!Global.getSector().getMemoryWithoutUpdate().contains(FM_ContactWithTokiko.TokikoKey)) {
            return false;
        }
        if (!(Boolean) Global.getSector().getMemoryWithoutUpdate().get(FM_ContactWithTokiko.TokikoKey)) {
            return false;
        }
        if (!market.getFactionId().equals("fantasy_manufacturing")) {
            return false;
        }
        boolean b = Global.getSector ().getPlayerStats ().getLevel () >= 0 || DebugFlags.BAR_DEBUG;
        return b;
    }

    @Override
    protected void regen(MarketAPI market) {
        if (this.market == market) return;
        super.regen(market);
        person.setPortraitSprite(Global.getSettings().getSpriteName("intel", "FM_Reimu"));
        person.setName(new FullName("???", "???", FullName.Gender.FEMALE));
        person.setId(I18nUtil.getString("person","FM_ReimuId"));
        person.setRankId("FM_miko");

        personSecond = FM_Person.getPerson(I18nUtil.getString("person","FM_TokikoId"));

//        if (personSecond == null) {
//            personSecond = Global.getFactory().createPerson();
//        }
//        personSecond.setPortraitSprite(Global.getSettings().getSpriteName("intel", "FM_Tokiko"));
//        personSecond.setName(new FullName("\"Unnamed\"", "Tokiko", FullName.Gender.FEMALE));
//        personSecond.setRankId("FM_cadetGraduate");
//        personSecond.getRelToPlayer().setRel(0.6f);

    }

    @Override
    public void addPromptAndOption(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
        super.addPromptAndOption(dialog, memoryMap);

        regen(dialog.getInteractionTarget().getMarket());

        TextPanelAPI text = dialog.getTextPanel();
        text.addPara(I18nUtil.getString("event", "FM_ReimuBarEvent_BarScene"));

        Color R;
        R = FM_Colors.FM_TEXT_RED;

        dialog.getOptionPanel().addOption(I18nUtil.getString("event", "FM_ReimuBarEvent_BarScene_Option"), this,
                R, null);
    }

    @Override
    public void init(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
        super.init(dialog, memoryMap);

        player_fleet = Global.getSector().getPlayerFleet();
        done = false;

        optionSelected(null, OptionId.INIT);
    }

    @Override
    public void optionSelected(String optionText, Object optionData) {
        if (!(optionData instanceof FM_ReimuBarEvent.OptionId)) {
            return;
        }
        FM_ReimuBarEvent.OptionId option = (FM_ReimuBarEvent.OptionId) optionData;

        OptionPanelAPI options = dialog.getOptionPanel();
        TextPanelAPI text = dialog.getTextPanel();
        options.clearOptions();
        if (FM_Person.addCharacter(person,market,false))
        {
            Global.getSector().getImportantPeople().addPerson(person);
        }
        //SetStoryOption.StoryOptionParams story = new SetStoryOption.StoryOptionParams(STAGE_2,1,"hireMerc",Sounds.STORY_POINT_SPEND_INDUSTRY,"主角光环时刻");
        //SetStoryOption.BaseOptionStoryPointActionDelegate spd = new SetStoryOption.BaseOptionStoryPointActionDelegate(dialog,story);
        //SetStoryOption.set(dialog,story,spd);


        //"涉及到东方内容的时候大可把所有剧情bug的修补推给八云紫".jpg

        switch (option) {
            case INIT:
                dialog.getVisualPanel().showPersonInfo(personSecond, true);
                text.addPara(I18nUtil.getString("event", "FM_ReimuBarEvent_INIT_TEXT_0"));
                text.addPara(I18nUtil.getString("event", "FM_ReimuBarEvent_INIT_TEXT_1"));
                text.addPara(I18nUtil.getString("event", "FM_ReimuBarEvent_INIT_TEXT_2"));

                options.addOption(I18nUtil.getString("event", "FM_ReimuBarEvent_INIT_ANSWER"), ANSWER);
                break;

            case ANSWER:
                text.addPara(I18nUtil.getString("event", "FM_ReimuBarEvent_ANSWER_TEXT_0"), FM_Colors.FM_TEXT_BLUE);
                text.addPara(I18nUtil.getString("event", "FM_ReimuBarEvent_ANSWER_TEXT_1"));
                text.addPara(I18nUtil.getString("event", "FM_ReimuBarEvent_ANSWER_TEXT_2"), FM_Colors.FM_TEXT_BLUE);
                text.addPara(I18nUtil.getString("event", "FM_ReimuBarEvent_ANSWER_TEXT_3"));
                text.addPara(I18nUtil.getString("event", "FM_ReimuBarEvent_ANSWER_TEXT_4"), FM_Colors.FM_TEXT_BLUE);
                text.addPara(I18nUtil.getString("event", "FM_ReimuBarEvent_ANSWER_TEXT_5"), FM_Colors.FM_TEXT_BLUE);
                text.addPara(I18nUtil.getString("event", "FM_ReimuBarEvent_ANSWER_TEXT_6"));

                options.addOption(I18nUtil.getString("event", "FM_ReimuBarEvent_ANSWER_CONTACT_1"), CONTACT_1);
                options.addOption(I18nUtil.getString("event", "FM_ReimuBarEvent_ANSWER_LEAVE"), LEAVE);
                break;
            case CONTACT_1:
                dialog.getVisualPanel().showPersonInfo(person, true);
                dialog.getVisualPanel().showSecondPerson(personSecond);
                text.addPara(I18nUtil.getString("event", "FM_ReimuBarEvent_CONTACT_1_TEXT_0"));
                text.addPara(I18nUtil.getString("event", "FM_ReimuBarEvent_CONTACT_1_TEXT_1"));
                text.addPara(I18nUtil.getString("event", "FM_ReimuBarEvent_CONTACT_1_TEXT_2"), FM_Colors.FM_TEXT_RED);

                options.addOption(I18nUtil.getString("event", "FM_ReimuBarEvent_CONTACT_1_CONTACT_2"), CONTACT_2);
                break;
            case CONTACT_2:
                text.addPara(I18nUtil.getString("event", "FM_ReimuBarEvent_CONTACT_2_TEXT_0"));
                text.addPara(I18nUtil.getString("event", "FM_ReimuBarEvent_CONTACT_2_TEXT_1"), FM_Colors.FM_TEXT_RED);
                text.addPara(I18nUtil.getString("event", "FM_ReimuBarEvent_CONTACT_2_TEXT_2"));
                text.addPara(I18nUtil.getString("event", "FM_ReimuBarEvent_CONTACT_2_TEXT_3"));
                text.addPara(I18nUtil.getString("event", "FM_ReimuBarEvent_CONTACT_2_TEXT_4"));

                options.addOption(I18nUtil.getString("event", "FM_ReimuBarEvent_CONTACT_2_TELL_STORY_1"), TELL_STORY_1);
                break;
            case TELL_STORY_1:
                text.addPara(I18nUtil.getString("event", "FM_ReimuBarEvent_TELL_STORT_1_TEXT_0"));
                text.addPara(I18nUtil.getString("event", "FM_ReimuBarEvent_TELL_STORT_1_TEXT_1"), FM_Colors.FM_TEXT_RED);
                text.addPara(I18nUtil.getString("event", "FM_ReimuBarEvent_TELL_STORT_1_TEXT_2"), FM_Colors.FM_TEXT_RED);

                options.addOption(I18nUtil.getString("event", "FM_ReimuBarEvent_TELL_STORT_1_TELL_STORY_2"), TELL_STORY_2);
                break;
            case TELL_STORY_2:
                text.addPara(I18nUtil.getString("event", "FM_ReimuBarEvent_TELL_STORT_2_TEXT_0"));
                text.addPara(I18nUtil.getString("event", "FM_ReimuBarEvent_TELL_STORT_2_TEXT_1"), FM_Colors.FM_TEXT_RED);
                text.addPara(I18nUtil.getString("event", "FM_ReimuBarEvent_TELL_STORT_2_TEXT_2"));
                text.addPara(I18nUtil.getString("event", "FM_ReimuBarEvent_TELL_STORT_2_TEXT_3"), FM_Colors.FM_TEXT_RED);
                text.addPara(I18nUtil.getString("event", "FM_ReimuBarEvent_TELL_STORT_2_TEXT_4"), FM_Colors.FM_TEXT_BLUE);
                text.addPara(I18nUtil.getString("event", "FM_ReimuBarEvent_TELL_STORT_2_TEXT_5"));
                text.addPara(I18nUtil.getString("event", "FM_ReimuBarEvent_TELL_STORT_2_TEXT_6"), FM_Colors.FM_TEXT_RED);
                text.addPara(I18nUtil.getString("event", "FM_ReimuBarEvent_TELL_STORT_2_TEXT_7"));
                text.addPara(I18nUtil.getString("event", "FM_ReimuBarEvent_TELL_STORT_2_TEXT_8"), FM_Colors.FM_TEXT_BLUE);
                text.addPara(I18nUtil.getString("event", "FM_ReimuBarEvent_TELL_STORT_2_TEXT_9"), FM_Colors.FM_TEXT_RED);
                text.addPara(I18nUtil.getString("event", "FM_ReimuBarEvent_TELL_STORT_2_TEXT_10"));
                text.addPara(I18nUtil.getString("event", "FM_ReimuBarEvent_TELL_STORT_2_TEXT_11"));

                options.addOption(I18nUtil.getString("event", "FM_ReimuBarEvent_TELL_STORT_2_TELL_STORY_3A"), TELL_STORY_3A);
                options.addOption(I18nUtil.getString("event", "FM_ReimuBarEvent_TELL_STORT_2_TELL_STORY_3B"), TELL_STORY_3B);
                break;
            case TELL_STORY_3A:
                text.addPara(I18nUtil.getString("event", "FM_ReimuBarEvent_TELL_STORT_3A_TEXT_0"));
                text.addPara(I18nUtil.getString("event", "FM_ReimuBarEvent_TELL_STORT_3A_TEXT_1"));
                text.addPara(I18nUtil.getString("event", "FM_ReimuBarEvent_TELL_STORT_3A_TEXT_2"), FM_Colors.FM_TEXT_RED);
                text.addPara(I18nUtil.getString("event", "FM_ReimuBarEvent_TELL_STORT_3A_TEXT_3"), FM_Colors.FM_TEXT_BLUE);
                text.addPara(I18nUtil.getString("event", "FM_ReimuBarEvent_TELL_STORT_3A_TEXT_4"), FM_Colors.FM_TEXT_RED);
                text.addPara(I18nUtil.getString("event", "FM_ReimuBarEvent_TELL_STORT_3A_TEXT_5"), FM_Colors.FM_TEXT_BLUE);
                text.addPara(I18nUtil.getString("event", "FM_ReimuBarEvent_TELL_STORT_3A_TEXT_6"), FM_Colors.FM_TEXT_RED);
                text.addPara(I18nUtil.getString("event", "FM_ReimuBarEvent_TELL_STORT_3A_TEXT_7"), FM_Colors.FM_TEXT_BLUE);
                text.addPara(I18nUtil.getString("event", "FM_ReimuBarEvent_TELL_STORT_3A_TEXT_8"));
                text.addPara(I18nUtil.getString("event", "FM_ReimuBarEvent_TELL_STORT_3A_TEXT_9"));
                text.addPara(I18nUtil.getString("event", "FM_ReimuBarEvent_TELL_STORT_3A_TEXT_10"));

                options.addOption(I18nUtil.getString("event", "FM_ReimuBarEvent_TELL_STORT_3AB_DEAL_1"), DEAL_1);
                break;
            case TELL_STORY_3B:
                text.addPara(I18nUtil.getString("event", "FM_ReimuBarEvent_TELL_STORT_3B_TEXT_0"));
                text.addPara(I18nUtil.getString("event", "FM_ReimuBarEvent_TELL_STORT_3B_TEXT_1"));
                text.addPara(I18nUtil.getString("event", "FM_ReimuBarEvent_TELL_STORT_3B_TEXT_2"));
                text.addPara(I18nUtil.getString("event", "FM_ReimuBarEvent_TELL_STORT_3B_TEXT_3"), FM_Colors.FM_TEXT_BLUE);
                text.addPara(I18nUtil.getString("event", "FM_ReimuBarEvent_TELL_STORT_3B_TEXT_4"));
                text.addPara(I18nUtil.getString("event", "FM_ReimuBarEvent_TELL_STORT_3B_TEXT_5"), FM_Colors.FM_TEXT_BLUE);
                text.addPara(I18nUtil.getString("event", "FM_ReimuBarEvent_TELL_STORT_3B_TEXT_6"), FM_Colors.FM_TEXT_RED);
                text.addPara(I18nUtil.getString("event", "FM_ReimuBarEvent_TELL_STORT_3B_TEXT_7"), FM_Colors.FM_TEXT_BLUE);
                text.addPara(I18nUtil.getString("event", "FM_ReimuBarEvent_TELL_STORT_3B_TEXT_8"), FM_Colors.FM_TEXT_RED);
                text.addPara(I18nUtil.getString("event", "FM_ReimuBarEvent_TELL_STORT_3B_TEXT_9"), FM_Colors.FM_TEXT_BLUE);
                text.addPara(I18nUtil.getString("event", "FM_ReimuBarEvent_TELL_STORT_3B_TEXT_10"), FM_Colors.FM_TEXT_RED);
                text.addPara(I18nUtil.getString("event", "FM_ReimuBarEvent_TELL_STORT_3B_TEXT_11"));
                text.addPara(I18nUtil.getString("event", "FM_ReimuBarEvent_TELL_STORT_3B_TEXT_12"));
                text.addPara(I18nUtil.getString("event", "FM_ReimuBarEvent_TELL_STORT_3B_TEXT_13"));

                options.addOption(I18nUtil.getString("event", "FM_ReimuBarEvent_TELL_STORT_3AB_DEAL_1"), DEAL_1);
                break;
            case DEAL_1:
                text.addPara(I18nUtil.getString("event", "FM_ReimuBarEvent_DEAL_1_TEXT_0"));
                text.addPara(I18nUtil.getString("event", "FM_ReimuBarEvent_DEAL_1_TEXT_1"));

                options.addOption(I18nUtil.getString("event", "FM_ReimuBarEvent_DEAL_1_DEAL_2"), DEAL_2);
                break;
            case DEAL_2:
                text.addPara(I18nUtil.getString("event", "FM_ReimuBarEvent_DEAL_2_TEXT_0"));
                text.addPara(I18nUtil.getString("event", "FM_ReimuBarEvent_DEAL_2_TEXT_1"), FM_Colors.FM_TEXT_RED);
                text.addPara(I18nUtil.getString("event", "FM_ReimuBarEvent_DEAL_2_TEXT_2"));
                text.addPara(I18nUtil.getString("event", "FM_ReimuBarEvent_DEAL_2_TEXT_3"), FM_Colors.FM_TEXT_RED);
                text.addPara(I18nUtil.getString("event", "FM_ReimuBarEvent_DEAL_2_TEXT_4"));
                text.addPara(I18nUtil.getString("event", "FM_ReimuBarEvent_DEAL_2_TEXT_5"));
                text.addPara(I18nUtil.getString("event", "FM_ReimuBarEvent_DEAL_2_TEXT_6"));

                options.addOption(I18nUtil.getString("event", "FM_ReimuBarEvent_DEAL_2_DEAL_3"), DEAL_3);
                break;
            case DEAL_3:
                text.addPara(I18nUtil.getString("event", "FM_ReimuBarEvent_DEAL_3_TEXT_0"), FM_Colors.FM_TEXT_RED);
                text.addPara(I18nUtil.getString("event", "FM_ReimuBarEvent_DEAL_3_TEXT_1"));
                text.addPara(I18nUtil.getString("event", "FM_ReimuBarEvent_DEAL_3_TEXT_2"), FM_Colors.FM_TEXT_RED);
                text.addPara(I18nUtil.getString("event", "FM_ReimuBarEvent_DEAL_3_TEXT_3"), FM_Colors.FM_TEXT_RED);
                text.addPara(I18nUtil.getString("event", "FM_ReimuBarEvent_DEAL_3_TEXT_4"));
                text.addPara(I18nUtil.getString("event", "FM_ReimuBarEvent_DEAL_3_TEXT_5"));
                text.addPara(I18nUtil.getString("event", "FM_ReimuBarEvent_DEAL_3_TEXT_6"), FM_Colors.FM_TEXT_BLUE);
                text.addPara(I18nUtil.getString("event", "FM_ReimuBarEvent_DEAL_3_TEXT_7"));
                text.addPara(I18nUtil.getString("event", "FM_ReimuBarEvent_DEAL_3_TEXT_8"), FM_Colors.FM_TEXT_BLUE);
                text.addPara(I18nUtil.getString("event", "FM_ReimuBarEvent_DEAL_3_TEXT_9"), FM_Colors.FM_TEXT_BLUE);
                text.addPara(I18nUtil.getString("event", "FM_ReimuBarEvent_DEAL_3_TEXT_10"));
                text.addPara(I18nUtil.getString("event", "FM_ReimuBarEvent_DEAL_3_TEXT_11"));

                options.addOption(I18nUtil.getString("event", "FM_ReimuBarEvent_DEAL_3_FINAL"), FINAL);
                break;
            case FINAL:

                BarEventManager.getInstance().notifyWasInteractedWith(this);

                person.setName(new FullName("Hakurei", "Reimu", FullName.Gender.FEMALE));

                text.addPara(I18nUtil.getString("event", "FM_ReimuBarEvent_FINAL_TEXT_0"));
                text.addPara(I18nUtil.getString("event", "FM_ReimuBarEvent_FINAL_TEXT_1"), FM_Colors.FM_TEXT_RED);
                text.addPara(I18nUtil.getString("event", "FM_ReimuBarEvent_FINAL_TEXT_2"));
                text.addPara(I18nUtil.getString("event", "FM_ReimuBarEvent_FINAL_TEXT_3"), FM_Colors.FM_TEXT_RED);
                text.addPara(I18nUtil.getString("event", "FM_ReimuBarEvent_FINAL_TEXT_4"), FM_Colors.FM_TEXT_RED);
                text.addPara(I18nUtil.getString("event", "FM_ReimuBarEvent_FINAL_TEXT_5"));

                options.addOption(I18nUtil.getString("event", "FM_ReimuBarEvent_FINAL_LEAVE"), LEAVE);

                //军官生成相关内容
                //军官生成相关内容

                List<String> reimu = new ArrayList<>();
                reimu.add(Skills.TARGET_ANALYSIS);
                reimu.add(Skills.HELMSMANSHIP);
                reimu.add("FM_Reimu_skill");

                //testing
                //doExtraConfirmActions();
                if (!Global.getSector().getMemoryWithoutUpdate().contains(ReimuKey)) {
                    Global.getSector().getMemoryWithoutUpdate().set(ReimuKey, true);
                }

                officer_data = Global.getFactory().createOfficerData(person);

                for (String reimu_skill : reimu) {
                    person.getStats().setSkillLevel(reimu_skill, 3);
                }
                OfficerLevelupPlugin plugin = (OfficerLevelupPlugin) Global.getSettings().getPlugin("officerLevelUp");

                person.getStats().addXP(plugin.getXPForLevel(1));
                person.setPersonality(Personalities.AGGRESSIVE);
                player_fleet.getFleetData().addOfficer(person);
                AddRemoveCommodity.addOfficerGainText(person, text);
                reimugot = true;
                //FM_Misc.addRating(3,text);
                //特殊tag用于判断角色
                person.addTag(ReimuOfficerTag);

//
//                List<String> reimu = new ArrayList<>();
//                reimu.add(Skills.TARGET_ANALYSIS);
//                reimu.add(Skills.HELMSMANSHIP);
//                reimu.add("FM_Reimu_skill");
//
//                //testing
//                //doExtraConfirmActions();
//                if (!Global.getSector().getMemoryWithoutUpdate().contains(ReimuKey)) {
//                    Global.getSector().getMemoryWithoutUpdate().set(ReimuKey, true);
//                }
//
//                officer = Global.getFactory().createPerson();
//                officer_data = Global.getFactory().createOfficerData(officer);
//
//                for (String reimu_skill : reimu) {
//                    officer.getStats().setSkillLevel(reimu_skill, 3);
//                }
//                OfficerLevelupPlugin plugin = (OfficerLevelupPlugin) Global.getSettings().getPlugin("officerLevelUp");
//
//                officer.getStats().addXP(plugin.getXPForLevel(1));
//
//                officer.setPersonality(Personalities.AGGRESSIVE);
//                officer.setName(person.getName());
//                officer.setPortraitSprite(person.getPortraitSprite());
//                officer.setGender(person.getGender());
//                officer.setRankId("FM_miko");
//                officer.setPostId("FM_miko");
//                player_fleet.getFleetData().addOfficer(officer);
//                AddRemoveCommodity.addOfficerGainText(officer, text);

                break;


            case LEAVE:
                if (!reimugot)
                {
                    FM_Person.setStored(person,true);
                }
                noContinue = true;
                done = true;
                break;

        }
    }

    @Override
    protected String getPersonFaction() {
        return "fantasy_manufacturing";
    }

    @Override
    protected String getPersonRank() {
        return Ranks.SPACE_SAILOR;
    }

    @Override
    protected String getPersonPost() {
        return Ranks.CITIZEN;
    }

    @Override
    protected String getPersonPortrait() {
        return null;
    }

    @Override
    protected FullName.Gender getPersonGender() {
        return FullName.Gender.ANY;
    }

    /*
    protected void doExtraConfirmActions() {
        //testing
        person.setName(new FullName("TEST","TEST", FullName.Gender.FEMALE));
        person.setPortraitSprite(Global.getSettings().getSpriteName("intel", "FM_Reimu"));
        person.setImportanceAndVoice(PersonImportance.VERY_HIGH,random);
        person.addTag(Tags.CONTACT_TRADE);
        person.setFaction("fantasy_manufacturing");
        person.setMarket(market);

        ContactIntel.getContactIntel(person);

        Global.getLogger(this.getClass()).info(person.getNameString());
        Global.getLogger(this.getClass()).info(person.getMarket().getMemoryWithoutUpdate().getBoolean(ContactIntel.NO_CONTACTS_ON_MARKET));
        Global.getLogger(this.getClass()).info(person.getFaction().getCustomBoolean(Factions.CUSTOM_NO_CONTACTS));

        ContactIntel.addPotentialContact(1f,person,person.getMarket(), dialog.getTextPanel());
    }

     */


}
