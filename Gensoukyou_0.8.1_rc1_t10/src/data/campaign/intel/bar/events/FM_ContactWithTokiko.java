package data.campaign.intel.bar.events;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.BattleCreationContext;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.DebugFlags;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BarEventManager;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BaseBarEventWithPerson;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import data.utils.FM_Colors;
import data.utils.FM_Person;
import data.utils.I18nUtil;
import exerelin.campaign.SectorManager;
import org.lazywizard.lazylib.campaign.CampaignUtils;
import org.magiclib.util.MagicCampaign;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;


public class FM_ContactWithTokiko extends BaseBarEventWithPerson {


    protected CampaignFleetAPI player_fleet;
    protected CampaignFleetAPI playerStoryFleet;
    protected CampaignFleetAPI TokikoStoryFleet;

//    protected PortsideBarEvent event;

    protected BattleAPI storyBattle;
    protected boolean TRY_TO_HELP_LEAVE = false;
    protected boolean DECIDE_LEAVE = false;


    public static String TokikoKey = "$FM_TokikoCompleted";
    public static String PlayerFleetOriginal = "$FM_PlayerFleetOriginal";
    public static float CREDITS = 10000;
    public static String TokikoOfficerTag = "FM_TokikoOfficerTag";
    //testing
    //protected PersonAPI contact;

    public enum OptionId {
        INIT,
        TRY_TO_HELP,
        DECIDE_LEAVE_OR_NOT,
        CONTACT_1,
        CONTACT_2,
        BEFORE_BATTLE_1,
        BEFORE_BATTLE_2,
        STORY_BATTLE,
        BATTLE_END,
        LAST,
        LEAVE,
        DONE,
    }


    public FM_ContactWithTokiko() {
        super();
    }

    public boolean shouldShowAtMarket(MarketAPI market) {
        if (!super.shouldShowAtMarket(market)) {
            return false;
        }
        if(DebugFlags.BAR_DEBUG) {
            return true;
        }
//        if (!market.getFactionId().equals("fantasy_manufacturing")) {
//            return false;
//        }
//        Logger log = Global.getLogger(this.getClass());
//        log.info(market.getPlanetEntity().getId());
//        if (market.getPlanetEntity() != null){
//            if (!market.getPlanetEntity().getId().equals("FM_planet_hakurei"))return false;
//        }
        if(!market.getPrimaryEntity().getId().equals("FM_planet_hakurei")) {
            return false;
        }
        if(FM_Person.hasMetCharacter(I18nUtil.getString("person","FM_TokikoId"))) {
            return false;
        }
        boolean b = Global.getSector ().getPlayerStats ().getLevel () >= 0 || DebugFlags.BAR_DEBUG;
        return b;
    }

    protected PersonAPI pather;
    protected MarketAPI patherMarket;

    @Override
    protected void regen(MarketAPI market) {
        if (this.market == market) return;
        super.regen(market);
        person.setPortraitSprite(Global.getSettings().getSpriteName("intel", "FM_Tokiko"));
        person.setName(new FullName("???", "???", FullName.Gender.FEMALE));
        person.setId(I18nUtil.getString("person","FM_TokikoId"));

        pather = Global.getSector().getFaction(Factions.LUDDIC_PATH).createRandomPerson();
        pather.setRankId(Ranks.GROUND_CAPTAIN);

        if (!Global.getSettings().getModManager().isModEnabled("nexerelin") || SectorManager.getManager().isCorvusMode()) {
            patherMarket = Global.getSector().getStarSystem("Gensokyo").getEntityById("FM_planet_inverted_castle").getMarket();
        } else {
            WeightedRandomPicker<MarketAPI> picker = new WeightedRandomPicker<MarketAPI>(random);
            for (MarketAPI curr : Global.getSector().getEconomy().getMarketsInGroup(null)) {
                if (curr == market) continue;
                if (curr.isPlayerOwned()) continue;
                if (curr.isHidden()) continue;
                if (curr.getStabilityValue() <= 0) continue;
                if (!curr.getFactionId().equals(Factions.LUDDIC_PATH)) continue;

                float w = curr.getSize();
                picker.add(curr, w);
            }
            if (picker.isEmpty()) picker.add(market, 1f);

            patherMarket = picker.pick();

        }
    }

    @Override
    public void addPromptAndOption(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
        super.addPromptAndOption(dialog, memoryMap);

        regen(dialog.getInteractionTarget().getMarket());

        TextPanelAPI text = dialog.getTextPanel();
        text.addPara(I18nUtil.getString("event", "FM_TokikoBarEvent_BarScene"));

        Color R;
        R = FM_Colors.FM_TEXT_BLUE;

        dialog.getOptionPanel().addOption(I18nUtil.getString("event", "FM_TokikoBarEvent_BarScene_Option"), this,
                R, null);
    }

    @Override
    public void init(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
        super.init(dialog, memoryMap);

//        event = this;
        this.dialog = dialog;
        done = false;
        player_fleet = Global.getSector().getPlayerFleet();

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
                TRY_TO_HELP_LEAVE = true;
                DECIDE_LEAVE = true;
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEvent_INIT_TEXT_0"));
                options.addOption(I18nUtil.getString("event", "FM_TokikoBarEvent_INIT_TRY_TO_HELP"), OptionId.TRY_TO_HELP);
                break;

            case TRY_TO_HELP:
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEvent_TRY_TO_HELP_TEXT_0"));
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEvent_TRY_TO_HELP_TEXT_1"));
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEvent_TRY_TO_HELP_TEXT_2"), Misc.getHighlightColor(), Misc.getDGSCredits(CREDITS));
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEvent_TRY_TO_HELP_TEXT_3"));
                if (Global.getSector().getPlayerFleet().getCargo().getCredits().get() >= CREDITS) {
                    options.addOption(I18nUtil.getString("event", "FM_TokikoBarEvent_TRY_TO_HELP_DECIDE"), OptionId.DECIDE_LEAVE_OR_NOT);
                } else {
                    text.addPara(I18nUtil.getString("event", "FM_TokikoBarEvent_TRY_TO_HELP_NEED_CREDITS"));
                }
                options.addOption(I18nUtil.getString("event", "FM_TokikoBarEvent_TRY_TO_HELP_LEAVE"), OptionId.LEAVE);
                break;

            case DECIDE_LEAVE_OR_NOT:
                TRY_TO_HELP_LEAVE = false;
                Global.getSector().getPlayerFleet().getCargo().getCredits().subtract(CREDITS);
                AddRemoveCommodity.addCreditsLossText((int) CREDITS, dialog.getTextPanel());

                if (!Global.getSector().getMemoryWithoutUpdate().contains(TokikoKey)) {
                    Global.getSector().getMemoryWithoutUpdate().set(TokikoKey, false);
                }
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEvent_DECIDE_TEXT_0"));
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEvent_DECIDE_TEXT_1"));
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEvent_DECIDE_TEXT_2"), FM_Colors.FM_TEXT_BLUE);
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEvent_DECIDE_TEXT_3"));
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEvent_DECIDE_TEXT_4"));
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEvent_DECIDE_TEXT_5"));

                options.addOption(I18nUtil.getString("event", "FM_TokikoBarEvent_DECIDE_CONTACT_1"),
                        OptionId.CONTACT_1);
                options.addOption(I18nUtil.getString("event", "FM_TokikoBarEvent_DECIDE_LEAVE"),
                        OptionId.LEAVE, I18nUtil.getString("event", "FM_TokikoBarEvent_DECIDE_LEAVE_TIP"));

                BarEventManager.getInstance().notifyWasInteractedWith(this);

                break;
            case CONTACT_1:
                DECIDE_LEAVE = false;
                dialog.getVisualPanel().showPersonInfo(person, true);
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEvent_CONTACT_1_TEXT_0"), FM_Colors.FM_TEXT_BLUE);
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEvent_CONTACT_1_TEXT_1"));
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEvent_CONTACT_1_TEXT_2"));
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEvent_CONTACT_1_TEXT_3"));
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEvent_CONTACT_1_TEXT_4"));
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEvent_CONTACT_1_TEXT_5"));

                options.addOption(I18nUtil.getString("event", "FM_TokikoBarEvent_CONTACT_1_CONTACT_2"),
                        OptionId.CONTACT_2);
                break;
            case CONTACT_2:
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEvent_CONTACT_2_TEXT_0"), FM_Colors.FM_TEXT_BLUE);
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEvent_CONTACT_2_TEXT_1"));
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEvent_CONTACT_2_TEXT_2"), FM_Colors.FM_TEXT_BLUE);
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEvent_CONTACT_2_TEXT_3"), FM_Colors.FM_TEXT_BLUE);
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEvent_CONTACT_2_TEXT_4"));
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEvent_CONTACT_2_TEXT_5"), FM_Colors.FM_TEXT_BLUE);

                options.addOption(I18nUtil.getString("event", "FM_TokikoBarEvent_CONTACT_2_BEFORE_BATTLE_1"),
                        OptionId.BEFORE_BATTLE_1);
                break;
            case BEFORE_BATTLE_1:
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEvent_BEFORE_BATTLE_1_TEXT_0"), FM_Colors.FM_TEXT_BLUE);
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEvent_BEFORE_BATTLE_1_TEXT_1"));
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEvent_BEFORE_BATTLE_1_TEXT_2"), FM_Colors.FM_TEXT_BLUE);
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEvent_BEFORE_BATTLE_1_TEXT_3"), FM_Colors.FM_TEXT_BLUE);
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEvent_BEFORE_BATTLE_1_TEXT_4"), FM_Colors.FM_TEXT_BLUE);
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEvent_BEFORE_BATTLE_1_TEXT_5"), FM_Colors.FM_TEXT_BLUE);
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEvent_BEFORE_BATTLE_1_TEXT_6"), FM_Colors.FM_TEXT_BLUE);
                options.addOption(I18nUtil.getString("event", "FM_TokikoBarEvent_BEFORE_BATTLE_1_BATTLE_2"),
                        OptionId.BEFORE_BATTLE_2);

                break;
            case BEFORE_BATTLE_2:
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEvent_BEFORE_BATTLE_2_TEXT_0"), FM_Colors.FM_TEXT_BLUE);
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEvent_BEFORE_BATTLE_2_TEXT_1"));
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEvent_BEFORE_BATTLE_2_TEXT_2"), FM_Colors.FM_TEXT_BLUE);
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEvent_BEFORE_BATTLE_2_TEXT_3"));
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEvent_BEFORE_BATTLE_2_TEXT_4"), FM_Colors.FM_TEXT_BLUE);
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEvent_BEFORE_BATTLE_2_TEXT_5"), FM_Colors.FM_TEXT_BLUE);
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEvent_BEFORE_BATTLE_2_TEXT_6"), FM_Colors.FM_TEXT_BLUE);
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEvent_BEFORE_BATTLE_2_TEXT_7"));
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEvent_BEFORE_BATTLE_2_TEXT_8"), FM_Colors.FM_TEXT_BLUE);
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEvent_BEFORE_BATTLE_2_TEXT_9"));
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEvent_BEFORE_BATTLE_2_TEXT_10"), FM_Colors.FM_TEXT_BLUE);
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEvent_BEFORE_BATTLE_2_TEXT_11"), FM_Colors.FM_TEXT_BLUE);
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEvent_BEFORE_BATTLE_2_TEXT_12"));
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEvent_BEFORE_BATTLE_2_TEXT_13"), FM_Colors.FM_TEXT_BLUE);
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEvent_BEFORE_BATTLE_2_TEXT_14"));
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEvent_BEFORE_BATTLE_2_TEXT_15"));
                options.addOption(I18nUtil.getString("event", "FM_TokikoBarEvent_BEFORE_BATTLE_2_STORY_BATTLE"),
                        OptionId.STORY_BATTLE);
                break;
            case STORY_BATTLE:
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEvent_STORY_BATTLE_TEXT_0"), FM_Colors.FM_TEXT_BLUE);
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEvent_STORY_BATTLE_TEXT_1"));
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEvent_STORY_BATTLE_TEXT_2"), FM_Colors.FM_TEXT_BLUE);
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEvent_STORY_BATTLE_TEXT_3"));
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEvent_STORY_BATTLE_TEXT_4"));

                if (!Global.getSector().getMemoryWithoutUpdate().contains(PlayerFleetOriginal)) {
                    Global.getSector().getMemoryWithoutUpdate().set(PlayerFleetOriginal, player_fleet);
                }
                options.addOption(I18nUtil.getString("event", "FM_TokikoBarEvent_STORY_BATTLE_BATTLE_END"), OptionId.BATTLE_END);

                break;

            case BATTLE_END:

                //Logger log = Global.getLogger(FM_ContactWithTokiko.class);
                org.magiclib.campaign.MagicFleetBuilder builderPlayer = MagicCampaign.createFleetBuilder();
                builderPlayer.setFleetName("player Test");
                builderPlayer.setFleetFaction(Factions.NEUTRAL);
                builderPlayer.setFleetType(FleetTypes.PATROL_SMALL);
                builderPlayer.setFlagshipName("ISS Player");
                builderPlayer.setFlagshipVariant("dominator_Tokiko");
                builderPlayer.setCaptain(Global.getSector().getPlayerPerson());
                builderPlayer.setSupportFleet(new HashMap<String, Integer>());
                builderPlayer.setMinFP(0);
                builderPlayer.setReinforcementFaction(Factions.INDEPENDENT);
                builderPlayer.setQualityOverride(2f);
                builderPlayer.setSpawnLocation(player_fleet.getInteractionTarget());
                builderPlayer.setAssignment(FleetAssignment.ORBIT_PASSIVE);
                builderPlayer.setAssignmentTarget(player_fleet.getInteractionTarget());
                builderPlayer.setIsImportant(false);
                builderPlayer.setTransponderOn(true);
                playerStoryFleet = builderPlayer.create();

                org.magiclib.campaign.MagicFleetBuilder builderTokiko = MagicCampaign.createFleetBuilder();
                builderTokiko.setFleetName("Tokiko Test");
                builderTokiko.setFleetFaction(Factions.PIRATES);
                builderTokiko.setFleetType(FleetTypes.PATROL_SMALL);
                builderTokiko.setFlagshipName("GMS Tokiko");
                builderTokiko.setFlagshipVariant("FM_Miracle_Tokiko");
                builderTokiko.setCaptain(person);
                builderTokiko.setSupportFleet(new HashMap<String, Integer>());
                builderTokiko.setMinFP(0);
                builderTokiko.setReinforcementFaction("fantasy_manufacturing");
                builderTokiko.setQualityOverride(2f);
                builderTokiko.setSpawnLocation(player_fleet.getInteractionTarget());
                builderTokiko.setAssignment(FleetAssignment.ORBIT_PASSIVE);
                builderTokiko.setAssignmentTarget(player_fleet.getInteractionTarget());
                builderTokiko.setIsImportant(false);
                builderTokiko.setTransponderOn(true);
                TokikoStoryFleet = builderTokiko.create();

                player_fleet.getContainingLocation().spawnFleet(
                        player_fleet.getInteractionTarget(),
                        player_fleet.getLocation().getX(),
                        player_fleet.getLocation().getY(),
                        playerStoryFleet);
                player_fleet.getContainingLocation().spawnFleet(
                        player_fleet.getInteractionTarget(),
                        player_fleet.getLocation().getX(),
                        player_fleet.getLocation().getY(),
                        TokikoStoryFleet);
                //player fleet
                CampaignUtils.addShipToFleet("dominator_Tokiko", FleetMemberType.SHIP, playerStoryFleet);
                CampaignUtils.addShipToFleet("colossus3_Tokiko", FleetMemberType.SHIP, playerStoryFleet);
                CampaignUtils.addShipToFleet("colossus3_Tokiko", FleetMemberType.SHIP, playerStoryFleet);
                CampaignUtils.addShipToFleet("colossus3_Tokiko", FleetMemberType.SHIP, playerStoryFleet);
                CampaignUtils.addShipToFleet("enforcer_Tokiko", FleetMemberType.SHIP, playerStoryFleet);
                CampaignUtils.addShipToFleet("enforcer_Tokiko", FleetMemberType.SHIP, playerStoryFleet);
                CampaignUtils.addShipToFleet("enforcer_Tokiko", FleetMemberType.SHIP, playerStoryFleet);

                //Tokiko fleet
                CampaignUtils.addShipToFleet("FM_Miracle_Tokiko", FleetMemberType.SHIP, TokikoStoryFleet);
                CampaignUtils.addShipToFleet("FM_Witch_Tokiko", FleetMemberType.SHIP, TokikoStoryFleet);
                CampaignUtils.addShipToFleet("FM_Witch_Tokiko", FleetMemberType.SHIP, TokikoStoryFleet);
                CampaignUtils.addShipToFleet("FM_Witch_Tokiko", FleetMemberType.SHIP, TokikoStoryFleet);

                Global.getSector().setPlayerFleet(playerStoryFleet);

                //debug
//                log.info("TokikoStoryFleet" + (TokikoStoryFleet == null));
//                log.info("playerStoryFleet" + (playerStoryFleet == null));
//                log.info("event_1" + (event == null));
//                log.info("getFactory" + (Global.getFactory() == null));

                if (storyBattle == null) {
                    BattleCreationContext battleContext = new BattleCreationContext(playerStoryFleet, FleetGoal.ATTACK, TokikoStoryFleet, FleetGoal.ATTACK);
                    battleContext.setPlayerCommandPoints(6);
                    battleContext.aiRetreatAllowed = false;
                    Global.getSector().getCampaignUI().startBattle(battleContext);
                    storyBattle = playerStoryFleet.getBattle();
                }

                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEvent_BATTLE_END_TEXT_0"));
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEvent_BATTLE_END_TEXT_1"));
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEvent_BATTLE_END_TEXT_2"));
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEvent_BATTLE_END_TEXT_3"));

                options.addOption(I18nUtil.getString("event", "FM_TokikoBarEvent_BATTLE_END_LAST"), OptionId.LAST);

                break;

            case LAST:
                if (storyBattle == null) {
                    Global.getSector().setPlayerFleet((CampaignFleetAPI) Global.getSector().getMemoryWithoutUpdate().get(PlayerFleetOriginal));
                    if (playerStoryFleet != null) {
                        playerStoryFleet.despawn();
                    }
                    if (TokikoStoryFleet != null) {
                        TokikoStoryFleet.despawn();
                    }
                }
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEvent_LAST_TEXT_0"));
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEvent_LAST_TEXT_1"), FM_Colors.FM_TEXT_BLUE);
                text.addPara(I18nUtil.getString("event", "FM_TokikoBarEvent_LAST_TEXT_2"));
                options.addOption(I18nUtil.getString("event", "FM_TokikoBarEvent_LAST_LEAVE"), OptionId.LEAVE);

                break;

            case LEAVE:
                if (TRY_TO_HELP_LEAVE && DECIDE_LEAVE) {
                    text.addPara(I18nUtil.getString("event", "FM_TokikoBarEvent_LEAVE_0"), Misc.getHighlightColor());
                } else if (DECIDE_LEAVE) {
                    text.addPara(I18nUtil.getString("event", "FM_TokikoBarEvent_LEAVE_1_P0"));
                    text.addPara(I18nUtil.getString("event", "FM_TokikoBarEvent_LEAVE_1_P1"), FM_Colors.FM_TEXT_BLUE);
                    text.addPara(I18nUtil.getString("event", "FM_TokikoBarEvent_LEAVE_1_P2"));
                    text.addPara(I18nUtil.getString("event", "FM_TokikoBarEvent_LEAVE_1_P3"));
                    text.addPara(I18nUtil.getString("event", "FM_TokikoBarEvent_LEAVE_1_P4"));
                } else {
                    person.setName(new FullName("\"Unnamed\"", "Tokiko", FullName.Gender.FEMALE));
                    person.setRankId("FM_cadetGraduate");
                    text.addPara(I18nUtil.getString("event", "FM_TokikoBarEvent_LEAVE_2_TEXT_0"));
                    text.addPara(I18nUtil.getString("event", "FM_TokikoBarEvent_LEAVE_2_TEXT_1"), FM_Colors.FM_TEXT_BLUE);
                    text.addPara(I18nUtil.getString("event", "FM_TokikoBarEvent_LEAVE_2_TEXT_2"));
                    text.addPara(I18nUtil.getString("event", "FM_TokikoBarEvent_LEAVE_2_TEXT_3"), FM_Colors.FM_TEXT_BLUE);
                    text.addPara(I18nUtil.getString("event", "FM_TokikoBarEvent_LEAVE_2_TEXT_4"), FM_Colors.FM_TEXT_BLUE);
                    text.addPara(I18nUtil.getString("event", "FM_TokikoBarEvent_LEAVE_2_TEXT_5"));
                    addIntel();
                }
                options.addOption(I18nUtil.getString("event", "FM_TokikoBarEvent_LEAVE_DONE"), OptionId.DONE);

                break;

            case DONE:
                noContinue = true;
                done = true;
                break;
        }
    }

    protected void addIntel() {
        TextPanelAPI text = dialog.getTextPanel();

        MarketAPI market = patherMarket;
        if (market != null) {
            FM_ContactWithTokikoIntel intel = new FM_ContactWithTokikoIntel(market, this);
            if (!intel.isDone()) {
                Global.getSector().getIntelManager().addIntel(intel, false, text);
            }
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
        return super.getPersonPost();
    }

    @Override
    protected String getPersonPortrait() {
        return super.getPersonPortrait();
    }

    @Override
    protected FullName.Gender getPersonGender() {
        return super.getPersonGender();
    }

    public PersonAPI getPather() {
        return pather;
    }

    public MarketAPI getTargetMarket() {
        return patherMarket;
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
