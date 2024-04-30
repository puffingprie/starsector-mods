package data.campaign.intel.bar.events;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity;
import com.fs.starfarer.api.ui.IntelUIAPI;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.utils.I18nUtil;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.Set;

//rules.csv,很神奇吧...
public class FM_ContactWithTokikoIntel extends BaseIntelPlugin {

    public enum FM_TokikoStage {
        WAIT,
        RESCUE,
        DONE,
        FAILED,
    }

    public static int FINISHED_XP = 20000;
    public static float TOTAL_TIME = 75f;
    public static float WAITING_TIME = 15f;

    protected MarketAPI market;
    protected FM_ContactWithTokiko event;
    protected PersonAPI pather;

    protected FM_TokikoStage stage;
    protected int rescueCredits;
    protected int rescueStoryPoints;
    protected int rescueMarines;

    protected float timer = 0f;

    protected boolean update = false;


    public FM_ContactWithTokikoIntel(MarketAPI market, FM_ContactWithTokiko event) {
        this.event = event;
        this.market = market;
        this.pather = event.getPather();

        market.addPerson(pather);
        market.getCommDirectory().addPerson(pather);

        rescueCredits = Math.round(Global.getSector().getPlayerFleet().getCargo().getCredits().get() * 0.2f);
        rescueStoryPoints = 1;
        rescueMarines = 100;
        //时间对应
        pather.getMemoryWithoutUpdate().set("$FM_Tokiko_timer", timer);
        //人物确认
        pather.getMemoryWithoutUpdate().set("$FM_Tokiko_isPather", true);
        //动作状态集合(action)相关(action本身来自rules)
        pather.getMemoryWithoutUpdate().set("$FM_Tokiko_eventRef", this);
        Global.getSector().addScript(this);

        stage = FM_TokikoStage.WAIT;
    }

    @Override
    public void advanceImpl(float amount) {
        super.advanceImpl(amount);
//        Logger log = Global.getLogger(this.getClass());
//        log.info("isEnding" + (isEnding()));

        float days = Misc.getDays(amount);
        if (stage == FM_TokikoStage.WAIT) {
            timer = timer + days;
        }
        pather.getMemoryWithoutUpdate().set("$FM_Tokiko_timer", timer);
        if (timer >= WAITING_TIME && stage == FM_TokikoStage.WAIT) {
            Misc.makeImportant(pather, "FM_Tokiko");
            Misc.makeImportant(market.getPlanetEntity(), "FM_Tokiko");
            if (!update) {
                this.sendUpdateIfPlayerHasIntel(FM_TokikoStage.WAIT, false);
                update = true;
            }
        } else {
            Misc.makeUnimportant(pather, "FM_Tokiko");
            Misc.makeUnimportant(market.getPlanetEntity(), "FM_Tokiko");
        }

        if (timer >= TOTAL_TIME && stage == FM_TokikoStage.WAIT) {
            stage = FM_TokikoStage.FAILED;
            this.sendUpdateIfPlayerHasIntel(FM_TokikoStage.FAILED, false);
            market.removePerson(pather);
            endAfterDelay();
        }

    }

    @Override
    protected void notifyEnded() {
        super.notifyEnded();
        Global.getSector().removeScript(this);

    }

    @Override
    public boolean callEvent(String ruleId, InteractionDialogAPI dialog,
                             List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        String action = params.get(0).getString(memoryMap);

        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
        CargoAPI cargo = playerFleet.getCargo();
        //MemoryAPI memory = planet.getMemoryWithoutUpdate();

        if (action.equals("prepare")) {
            pather.getMemoryWithoutUpdate().set("$FM_Tokiko_creditsRe", Misc.getDGSCredits(rescueCredits), 0);
            pather.getMemoryWithoutUpdate().set("$FM_Tokiko_spsRe", 1, 0);
            pather.getMemoryWithoutUpdate().set("$FM_Tokiko_MarinesRe", rescueMarines, 0);
            pather.getMemoryWithoutUpdate().set("$FM_Tokiko_playerCredits", Misc.getDGSCredits(cargo.getCredits().get()), 0);
            pather.getMemoryWithoutUpdate().set("$FM_Tokiko_playerSps", Global.getSector().getPlayerPerson().getStats().getStoryPoints(), 0);
            pather.getMemoryWithoutUpdate().set("$FM_Tokiko_playerMarines", Global.getSector().getPlayerFleet().getCargo().getMarines(), 0);
        } else if (action.equals("canRescue")) {
            return cargo.getMarines() >= rescueMarines;
        } else if (action.equals("canPaySp")) {
            return Global.getSector().getPlayerPerson().getStats().getStoryPoints() >= rescueStoryPoints && Global.getSector().getPlayerFleet().getCargo().getCredits().get() >= rescueCredits;
        } else if (action.equals("Rescue")) {
            market.removePerson(pather);
            market.getCommDirectory().removePerson(pather);
            for (FleetMemberAPI curr : Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy()) {
                curr.getRepairTracker().applyCREvent(-0.2f, "FM_TokikoBarEventIntel_RESCUE_CrDes");
                AddRemoveCommodity.addCRLossText(curr, dialog.getTextPanel(), 0.2f);
            }
//            int lostMarines = Math.round(rescueMarines * 0.5f);
//            Global.getSector().getPlayerFleet().getCargo().removeCommodity(Commodities.MARINES,lostMarines);
//            AddRemoveCommodity.addCommodityLossText(Commodities.MARINES,lostMarines, dialog.getTextPanel());

            Global.getSector().getPlayerPerson().getStats().addXP(FINISHED_XP, dialog.getTextPanel());
            market.getFaction().getRelToPlayer().adjustRelationship(-0.5f, RepLevel.VENGEFUL);
            stage = FM_TokikoStage.RESCUE;
            this.sendUpdate(FM_TokikoStage.RESCUE, dialog.getTextPanel());
            Misc.increaseMarketHostileTimeout(market, 60f);


        } else if (action.equals("paySpForRescue")) {
            market.removePerson(pather);
            market.getCommDirectory().removePerson(pather);
            cargo.getCredits().subtract(rescueCredits);
            AddRemoveCommodity.addCommodityLossText(Commodities.CREDITS, rescueCredits, dialog.getTextPanel());
            Global.getSector().getPlayerPerson().getStats().spendStoryPoints(1, true, dialog.getTextPanel(), true,
                    2f, "Tokiko Story Finished");
            Global.getSector().getPlayerPerson().getStats().addXP(FINISHED_XP, dialog.getTextPanel());
            stage = FM_TokikoStage.RESCUE;
            this.sendUpdate(FM_TokikoStage.RESCUE, dialog.getTextPanel());

        }
        return true;
    }

    @Override
    public void endAfterDelay() {
        Misc.makeUnimportant(pather, "FM_Tokiko");
        Misc.makeUnimportant(market.getPlanetEntity(), "FM_Tokiko");
        super.endAfterDelay(0f);
    }

    @Override
    protected void notifyEnding() {
        super.notifyEnding();
    }


    protected void addBulletPoints(TooltipMakerAPI info, ListInfoMode mode) {

        Color h = Misc.getHighlightColor();
        Color g = Misc.getGrayColor();
        float pad = 3f;
        float opad = 10f;

        float initPad = pad;
        if (mode == ListInfoMode.IN_DESC) initPad = opad;
        Color tc = getBulletColorForMode(mode);
        bullet(info);

        if (stage == FM_TokikoStage.WAIT && timer >= WAITING_TIME) {
            info.addPara(I18nUtil.getString("event", "FM_TokikoBarEventIntel_WAIT_BulletPoint_0"), initPad, tc);
            info.addPara(I18nUtil.getString("event", "FM_TokikoBarEventIntel_WAIT_BulletPoint_1"), initPad, tc, Misc.getHighlightColor(), market.getStarSystem().getName());
        } else if (stage == FM_TokikoStage.FAILED) {
            info.addPara(I18nUtil.getString("event", "FM_TokikoBarEventIntel_FAILED_BulletPoint"), initPad);
        }
        initPad = 0f;
        unindent(info);
    }


    @Override
    public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
        Color c = getTitleColor(mode);
        info.setParaSmallInsignia();
        info.addPara(getName(), c, 0f);
        info.setParaFontDefault();
        addBulletPoints(info, mode);
    }

    @Override
    public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
        Color h = Misc.getHighlightColor();
        Color g = Misc.getGrayColor();
        Color tc = Misc.getTextColor();
        float pad = 3f;
        float opad = 10f;

        if (stage == FM_TokikoStage.WAIT) {
            if (timer <= 15f) {
                info.addPara(I18nUtil.getString("event", "FM_TokikoBarEventIntel_WAIT_BEFORE"), opad);
                addDays(info, I18nUtil.getString("event", "FM_addDays_After"), timer);
            } else {
                info.addPara(I18nUtil.getString("event", "FM_TokikoBarEventIntel_WAIT_AFTER_P0"), opad);
                info.addPara(I18nUtil.getString("event", "FM_TokikoBarEventIntel_WAIT_AFTER_P1"), opad);
                info.addPara(I18nUtil.getString("event", "FM_TokikoBarEventIntel_WAIT_AFTER_P2"), opad);
                info.addPara(I18nUtil.getString("event", "FM_TokikoBarEventIntel_WAIT_AFTER_P3"), opad);
//                info.addPara(I18nUtil.getString("event","FM_TokikoBarEventIntel_WAIT_AFTER_P4"), opad);
                addDays(info, I18nUtil.getString("event", "FM_addDays_Remain"), TOTAL_TIME - timer);
            }
        } else if (stage == FM_TokikoStage.RESCUE) {
            info.addPara(I18nUtil.getString("event", "FM_TokikoBarEventIntel_RESCUE_P0"), opad);
            info.addPara(I18nUtil.getString("event", "FM_TokikoBarEventIntel_RESCUE_P1"), opad);
            addGenericButton(info, width, I18nUtil.getString("event", "FM_Button_Accept"), "Accept");
        }
        addBulletPoints(info, ListInfoMode.IN_DESC);
    }

    @Override
    public void buttonPressConfirmed(Object buttonId, IntelUIAPI ui) {
        if (buttonId == "Accept") {
            ui.showDialog(null, new FM_ContactWithTokikoFinalDialog());
            stage = FM_TokikoStage.DONE;
            Global.getSector().getMemoryWithoutUpdate().set(FM_ContactWithTokiko.TokikoKey, true);
            endAfterDelay();
        }
        super.buttonPressConfirmed(buttonId, ui);
    }

    @Override
    public String getIcon() {
        return Global.getSettings().getSpriteName("intel", "FM_crest");
    }

    @Override
    public Set<String> getIntelTags(SectorMapAPI map) {
        Set<String> tags = super.getIntelTags(map);
        tags.add(Tags.INTEL_STORY);
        tags.add(Tags.INTEL_ACCEPTED);
        tags.add(Tags.INTEL_MISSIONS);
        return tags;
    }

    @Override
    public IntelSortTier getSortTier() {
        return IntelSortTier.TIER_2;
    }

    public String getSortString() {
        return "Tokiko";
    }

    public String getName() {
        if (isEnded() || isEnding()) {
            if (stage == FM_TokikoStage.DONE) {
                return I18nUtil.getString("event", "FM_TokikoBarEventIntel_NameCompleted");
            } else {
                return I18nUtil.getString("event", "FM_TokikoBarEventIntel_NameFailed");
            }

        }
        return I18nUtil.getString("event", "FM_TokikoBarEventIntel_Name");
    }

    @Override
    public FactionAPI getFactionForUIColors() {
        return super.getFactionForUIColors();
    }

    public String getSmallDescriptionTitle() {
        return getName();
    }

    @Override
    public SectorEntityToken getMapLocation(SectorMapAPI map) {
        return market.getStarSystem().getStar();
    }

    @Override
    public boolean shouldRemoveIntel() {
        return super.shouldRemoveIntel();
    }

    @Override
    public String getCommMessageSound() {
        return getSoundMajorPosting();
    }

//    public static void BackDoorTokikoEventCompleted(){
//        Global.getSector().getMemoryWithoutUpdate().set(FM_ContactWithTokiko.TokikoKey, true);
//
//        List<String> tokiko = new ArrayList<>();
//        tokiko.add(Skills.DAMAGE_CONTROL);
//        tokiko.add(Skills.COMBAT_ENDURANCE);
//        tokiko.add("FM_Tokiko_skill");
//
//
//        PersonAPI officer = Global.getFactory().createPerson();
//        OfficerDataAPI officer_data = Global.getFactory().createOfficerData(officer);
//
//        for (String tokikoSkill : tokiko) {
//            officer.getStats().setSkillLevel(tokikoSkill, 1);
//        }
//
//        OfficerLevelupPlugin plugin = (OfficerLevelupPlugin) Global.getSettings().getPlugin("officerLevelUp");
//
//        officer.getStats().addXP(plugin.getXPForLevel(1));
//        officer.setPortraitSprite(Global.getSettings().getSpriteName("intel", "FM_Tokiko"));
//        officer.setName(new FullName("\"Unnamed\"", "Tokiko", FullName.Gender.FEMALE));
//        officer.setGender(FullName.Gender.FEMALE);
//        officer.setPersonality(Personalities.AGGRESSIVE);
//        officer.setRankId("FM_cadetGraduate");
//        //一个特别的Tag用于判断
//        officer.addTag(FM_ContactWithTokiko.TokikoOfficerTag);
//
////                if (!Global.getSector().getMemoryWithoutUpdate().contains(officerId)){
////                    Global.getSector().getMemoryWithoutUpdate().set(officerId,officer);
////                }
//
//        CampaignFleetAPI player_fleet = Global.getSector().getPlayerFleet();
//        player_fleet.getFleetData().addOfficer(officer);
//
//
//        //data.campaign.intel.bar.events.FM_ContactWithTokikoIntel.BackDoorTokikoEventCompleted()
//    }
//

}
