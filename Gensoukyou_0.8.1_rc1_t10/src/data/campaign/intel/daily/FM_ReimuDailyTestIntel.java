package data.campaign.intel.daily;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseMissionIntel;
import com.fs.starfarer.api.ui.IntelUIAPI;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import org.apache.log4j.Logger;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FM_ReimuDailyTestIntel extends BaseMissionIntel {

    public static Logger log = Global.getLogger(FM_ReimuDailyTestIntel.class);

    protected int reward;
    protected FactionAPI faction = Global.getSector().getFaction("fantasy_manufacturing");

    public FM_ReimuDailyTestIntel() {

        setDuration(365f);

        log.info("Created FM_ReimuDailyTestIntel");

        initRandomCancel();

        Global.getSector().getIntelManager().queueIntel(this);


    }

    @Override
    public void notifyPlayerAboutToOpenIntelScreen() {

    }

    @Override
    protected MissionResult createAbandonedResult(boolean withPenalty) {
//        CoreReputationPlugin.MissionCompletionRep rep = new CoreReputationPlugin.MissionCompletionRep(CoreReputationPlugin.RepRewards.HIGH, RepLevel.WELCOMING,
//                -CoreReputationPlugin.RepRewards.TINY, RepLevel.INHOSPITABLE);
//        ReputationActionResponsePlugin.ReputationAdjustmentResult result = Global.getSector().adjustPlayerReputation(
//                new CoreReputationPlugin.RepActionEnvelope(CoreReputationPlugin.RepActions.MISSION_FAILURE, rep,
//                        null, null, true, false),
//                faction.getId());
//        return new MissionResult(0, result);
        return new MissionResult();
    }

    @Override
    protected MissionResult createTimeRanOutFailedResult() {
        return createAbandonedResult(true);
    }

    @Override
    public void missionAccepted() {

        setMissionState(MissionState.COMPLETED);

        endMission();

    }

    @Override
    public void buttonPressConfirmed(Object buttonId, IntelUIAPI ui) {
        if (buttonId == BUTTON_ACCEPT) {
            ui.showDialog(null, new FM_ReimuDailyTestDialog());
        }
        super.buttonPressConfirmed(buttonId, ui);
    }


    @Override
    public void endMission() {

        endAfterDelay();
    }

    @Override
    public void advanceMission(float amount) {
    }

    @Override
    public boolean callEvent(String ruleId, final InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
//        String action = params.get(0).getString(memoryMap);
//
//        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
//        CargoAPI cargo = playerFleet.getCargo();
//
//        if (action.equals("runPackage")) {
//            AddRemoveCommodity.addCreditsGainText(reward, dialog.getTextPanel());
//            cargo.getCredits().add(reward);
//
//            CoreReputationPlugin.MissionCompletionRep rep = new CoreReputationPlugin.MissionCompletionRep(CoreReputationPlugin.RepRewards.HIGH, RepLevel.WELCOMING,
//                    -CoreReputationPlugin.RepRewards.TINY, RepLevel.INHOSPITABLE);
//
//            ReputationActionResponsePlugin.ReputationAdjustmentResult result = Global.getSector().adjustPlayerReputation(
//                    new CoreReputationPlugin.RepActionEnvelope(CoreReputationPlugin.RepActions.MISSION_SUCCESS, rep,
//                            null, dialog.getTextPanel(), true, false),
//                    faction.getId());
//            setMissionResult(new MissionResult(reward, result));
//            setMissionState(MissionState.COMPLETED);
//            endMission();
//        }

        return false;
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
        boolean isUpdate = getListInfoParam() != null;

        if (isUpdate) {
            // 3 possible updates: de-posted/expired, failed, completed
            if (isFailed() || isCancelled()) {
                return;
            } else if (isCompleted()) {
                info.addPara("TEST,Mission Completed", pad);
            }
        } else {
            // either in small description, or in tooltip/intel list
            if (missionResult != null) {

                info.addPara("TEST,Mission Completed, but no update ?", pad);

            } else {
                if (mode != ListInfoMode.IN_DESC) {
                    info.addPara("TEST, IN_DESC", pad);
                }
                if (isCompleted()) {
                    info.addPara("TEST,Mission Completed", initPad);
                } else {
                    info.addPara("Did not Completed, yet", initPad);
                    addDays(info, "to complete", duration - elapsedDays, tc, 0f);
                }

            }
        }

        unindent(info);
    }

    @Override
    public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
        Color c = getTitleColor(mode);
        info.addPara(getName(), c, 0f);

        addBulletPoints(info, mode);
    }

    public String getSortString() {
        return "Story";
    }

    public String getName() {
        String name;
//		if (entity.getCustomEntitySpec() != null) {
//			name = entity.getCustomEntitySpec().getNameInText();
//		} else {
        name = "TEST"; // we want caps on every word since this is a title, so no getNameInText()
//		}

        return "Story " + name + getPostfixForState();
    }


    @Override
    public FactionAPI getFactionForUIColors() {
        return faction;
    }

    public String getSmallDescriptionTitle() {
        return getName();
    }


    @Override
    public void createSmallDescription(TooltipMakerAPI info, float width, float height) {

        float opad = 10f;

        info.addPara("TEST", opad);


        if (isPosted()) {
            addBulletPoints(info, ListInfoMode.IN_DESC);

            addGenericMissionState(info);

            addAcceptButton(info, width, "Accept");

        } else {

            addGenericMissionState(info);

            addBulletPoints(info, ListInfoMode.IN_DESC);
        }

    }

    public String getIcon() {
        return Global.getSettings().getSpriteName("campaignMissions", "analyze_entity");
    }

    public Set<String> getIntelTags(SectorMapAPI map) {
        Set<String> tags = super.getIntelTags(map);
        tags.add(Tags.INTEL_STORY);
        return tags;
    }

    @Override
    public SectorEntityToken getMapLocation(SectorMapAPI map) {
        return null;
    }

    @Override
    protected void addGenericMissionState(TooltipMakerAPI info) {
        float opad = 10f;
        String noun = getMissionTypeNoun();
        if (isAccepted()) {
            info.addPara("You have accepted this " + noun + ".", opad);
        } else if (isFailed()) {
            info.addPara("You have failed this " + noun + ".", opad);
        } else if (isCompleted()) {
            info.addPara("You have completed this " + noun + ".", opad);
        } else if (isCancelled()) {
            info.addPara("This " + noun + " is no longer being offered.", opad);
        } else if (isAbandoned()) {
            info.addPara("You have abandoned this " + noun + ".", opad);
        } else if (isPosted()) {
            info.addPara("This " + noun + " posting may be withdrawn at any time unless it's accepted.",
                    Misc.getHighlightColor(), opad);
            //Misc.getGrayColor(), opad);
        }
    }

}
