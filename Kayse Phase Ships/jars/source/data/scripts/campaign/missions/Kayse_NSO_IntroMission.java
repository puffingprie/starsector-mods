package data.scripts.campaign.missions;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.PersonImportance;
import com.fs.starfarer.api.campaign.RuleBasedDialog;
import java.awt.Color;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.MissionCompletionRep;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActions;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.missions.hub.BaseMissionHub;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithBarEvent;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD.RaidDangerLevel;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

public class Kayse_NSO_IntroMission extends HubMissionWithBarEvent {
    //This is heavily inspired (read politely copied) from the Nexerelin missions
    //Nexerelin code is available under MIT License ( https://github.com/Histidine91/Nexerelin/ )
    //Huge thanks to Histidine!
	
	public static Logger log = Global.getLogger(Kayse_NSO_IntroMission.class);

	public static enum Stage {
		RETRIEVE_CORES,
		RETURN_CORES,
		COMPLETED,
		FAILED,
		FAILED_DECIV
	}
	
	protected PersonAPI havelock;
	protected MarketAPI market;
	protected RaidDangerLevel danger;
        
        public void quickStart(){
            create(Global.getSector().getEconomy().getMarket("nso_tombstone"), true);
        }
	
	@Override
	protected boolean create(MarketAPI createdAt, boolean barEvent) {
		// if already accepted by the player, abort
                log.info("Attempting to create mission with parameters: " + createdAt.getName() + ".");
                log.info("Bar event: "+ barEvent);
		if (!setGlobalReference("$nso_introMission_ref")) {
                    log.info("Failed to set '$nso_introMission_ref'");
                    return false;
		}
		
		// if Prism Freeport exists, the mission must be created there
		if (!createdAt.getId().equals("nso_tombstone") && Global.getSector().getEconomy().getMarket("nso_tombstone") != null)
		{
                    log.info("Aborting due to not at Tombstone");
                    return false;
		}
                
                if (!createdAt.getFactionId().equals("no_such_org") && Global.getSector().getEconomy().getMarket("nso_tombstone") != null)
		{
                    log.info("Aborting due to Tombstone not belonging to NSO");
                    return false;
		}
		
		if (Global.getSector().getImportantPeople().getData("nso_havelock") == null) 
		{
                    log.info("Adding Havelock");
                    havelock = Global.getFactory().createPerson();
                    havelock.setFaction("no_such_org");
                    havelock.setGender(FullName.Gender.MALE);
                    havelock.setPostId(Ranks.AGENT);
                    havelock.setRankId(Ranks.AGENT);
                    havelock.getName().setFirst("Havelock");
                    havelock.getName().setLast("Meserole");
                    havelock.setPortraitSprite("graphics/kayse/portraits/havelock.png");
                    //havelock.setContactWeight(100f);
                    havelock.setId("nso_havelock");
                    havelock.setImportance(PersonImportance.HIGH);
                    //havelock.
                    havelock.addTag("military");
                    Global.getSector().getImportantPeople().addPerson(havelock);
                    /*BaseMissionHub.set(admin, new BaseMissionHub(admin));
                    havelock.getMemoryWithoutUpdate().set(BaseMissionHub.NUM_BONUS_MISSIONS, 1);*/
                    //log.info( havelock.getPortraitSprite() );
                    //createdAt.setAdmin(havelock);
                    createdAt.getCommDirectory().addPerson(havelock, 0);
                    createdAt.addPerson(havelock);
                    
//                    log.info("Person is null");
//                    return false;
		}else{
                    havelock = Global.getSector().getImportantPeople().getPerson("nso_havelock");
                }
                
                if (Global.getSector().getImportantPeople().getData("nso_havelock") == null) 
		{
                    log.info("Person is still null");
                    return false;
                }
		personOverride = havelock;
		
		setStoryMission();
		
		requireMarketFaction(Factions.HEGEMONY);
		requireMarketIsNot(createdAt);
		requireMarketNotHidden();
		requireMarketNotInHyperspace();
		preferMarketSizeAtLeast(3);
		preferMarketSizeAtMost(6);
		market = pickMarket();
		danger = RaidDangerLevel.HIGH;
		if (market == null) {
                    log.info("Failed to find market");
                    return false;
		}
		
		if (!setMarketMissionRef(market, "$nso_introMission_ref")) {
                    log.info("Mission ref already set");
                    return false;
		}
		
		int marines = getMarinesRequiredForCustomObjective(market, danger);
		if (!isOkToOfferMissionRequiringMarines(marines)) {
                    //return false;
		}
		
		makeImportant(market, "$nso_introMission_target", Stage.RETRIEVE_CORES);
		makeImportant(havelock, "$nso_introMission_returnHere", Stage.RETURN_CORES);
		log.info("Setting stage: " + Stage.RETRIEVE_CORES);
		setStartingStage(Stage.RETRIEVE_CORES);
		addSuccessStages(Stage.COMPLETED);
		addFailureStages(Stage.FAILED);
		
		connectWithMemoryFlag(Stage.RETRIEVE_CORES, Stage.RETURN_CORES, market, "$nso_introMission_needToReturn");
		setStageOnMemoryFlag(Stage.COMPLETED, havelock, "$nso_introMission_completed");
		
		setStageOnMemoryFlag(Stage.FAILED, havelock, "$nso_introMission_failed");
		
		addNoPenaltyFailureStages(Stage.FAILED_DECIV);
		connectWithMarketDecivilized(Stage.RETRIEVE_CORES, Stage.FAILED_DECIV, market);
		setStageOnMarketDecivilized(Stage.FAILED_DECIV, createdAt);
		
		/*
		beginStageTrigger(Stage.COMPLETED);
		triggerSetGlobalMemoryValue("$nso_introMission_missionCompleted", true);
		endTrigger();
		*/

		this.setRepPersonChangesHigh();
		this.setRepFactionChangesMedium();
		setCreditReward(CreditReward.HIGH);
		
		if (true) {
                    triggerCreateMediumPatrolAroundMarket(market, Stage.RETRIEVE_CORES, 0f);
		}
		
		return true;
	}
	
	@Override
	protected void updateInteractionDataImpl() {
            set("$nso_introMission_personName", havelock.getNameString());
            set("$nso_introMission_manOrWoman", havelock.getManOrWoman());
            set("$nso_introMission_reward", Misc.getWithDGS(getCreditsReward()));
            set("$nso_introMission_corePriceStr", Misc.getWithDGS(getCreditsReward()*.75f));
            set("$nso_introMission_corePriceVal", (getCreditsReward()*.75f));

            set("$nso_introMission_systemName", market.getStarSystem().getNameWithLowercaseTypeShort());
            set("$nso_introMission_marketName", market.getName());
            set("$nso_introMission_marketOnOrAt", market.getOnOrAt());
            set("$nso_introMission_dist", getDistanceLY(market));

            set("$nso_introMission_danger", danger);
            set("$nso_introMission_marines", Misc.getWithDGS(getMarinesRequiredForCustomObjective(market, danger)));
            set("$nso_introMission_stage", getCurrentStage());
	}
	
	@Override
	public boolean callEvent(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
            String action = params.get(0).getString(memoryMap);
            log.info("Inside callEvent: " + action);

            if (null != action) switch (action) {
                case "beginBlank":
                    dialog.getInteractionTarget().setActivePerson(havelock);
                    //dialog.getVisualPanel().showPersonInfo(havelock, true);
                    havelock.setPortraitSprite("graphics/portraits/portraits_generic.png");
                    //dialog.getVisualPanel().setVisualFade(0, 0);
                    updateInteractionData(dialog, memoryMap);
                    return false;
                case "beginIntro":
                    dialog.getInteractionTarget().setActivePerson(havelock);
                    dialog.getVisualPanel().showPersonInfo(havelock, true);
                    havelock.setPortraitSprite("graphics/kayse/portraits/havelock.png");
                    updateInteractionData(dialog, memoryMap);
                    return false;
                case "accept":
                    setStartingStage(Stage.RETRIEVE_CORES);
                    accept(dialog, memoryMap);
                    return true;
                case "cancel":
                    //MarketAPI market = dialog.getInteractionTarget().getMarket();
                    market.removePerson(havelock);
                    abort();
                    return false;
                case "raidComplete":
                    havelock.getMarket().getCommDirectory().addPerson(havelock);
                    return true;
                case "hasCores":
                    return Global.getSector().getPlayerFleet().getCargo().getCommodityQuantity(Commodities.SHIPS) >= 5;
                case "forceShowPerson":
                    dialog.getVisualPanel().showPersonInfo(havelock);
                    return true;
                case "complete":
                    BaseMissionHub.set(havelock, new BaseMissionHub(havelock));
                    havelock.getMemoryWithoutUpdate().set(BaseMissionHub.NUM_BONUS_MISSIONS, 1);

                    havelock.getMemoryWithoutUpdate().set("$nso_introMission_completed", true);
                    ((RuleBasedDialog)dialog.getPlugin()).updateMemory();
                    return true;
                case "complete2":
                    //havelock.getName().setFirst(getString("dissonantName1"));
                    //havelock.getName().setLast(getString("dissonantName2"));
                    return true;
                case "betray":
                    PersonAPI person = dialog.getInteractionTarget().getActivePerson();
                    FactionAPI pFaction = person.getFaction();

                    // rep with faction you gave the cores to
                    float repMult = pFaction.getCustomFloat("AICoreRepMult");
                    MissionCompletionRep repPerson = new MissionCompletionRep(
                                    getRepRewardSuccessPerson() * repMult, getRewardLimitPerson(),
                                    -getRepPenaltyFailurePerson(), getPenaltyLimitPerson());
                    MissionCompletionRep repFaction = new MissionCompletionRep(
                                    getRepRewardSuccessFaction() * repMult, getRewardLimitFaction(),
                                    -getRepPenaltyFailureFaction(), getPenaltyLimitFaction());

                    Global.getSector().adjustPlayerReputation(
                            new CoreReputationPlugin.RepActionEnvelope(RepActions.MISSION_SUCCESS, 
                                            repPerson, dialog.getTextPanel(), true), person);
                    Global.getSector().adjustPlayerReputation(
                            new CoreReputationPlugin.RepActionEnvelope(RepActions.MISSION_SUCCESS, 
                                            repFaction, dialog.getTextPanel(), true), pFaction.getId());

                    // credits
                    float bounty = getCreditsReward()*.5f;//Betrayl doesn't pay well
                    Global.getSector().getPlayerFleet().getCargo().getCredits().add(bounty);
                    AddRemoveCommodity.addCreditsGainText((int)bounty, dialog.getTextPanel());

                    // fall through to next level
                case "refuse":
                    havelock.getMarket().getCommDirectory().removePerson(havelock);
                    havelock.getMarket().removePerson(havelock);
                    havelock.getMemoryWithoutUpdate().set("$nso_introMission_failed", true);
                    return false;
                default:
                    break;
            }

            return super.callEvent(ruleId, dialog, params, memoryMap);
	}
	
	@Override
	public void addDescriptionForNonEndStage(TooltipMakerAPI info, float width, float height) {
		float opad = 10f;
		Color h = Misc.getHighlightColor();
		String pName = havelock.getNameString();
		FactionAPI heg = Global.getSector().getFaction(Factions.HEGEMONY);
		FactionAPI pl = Global.getSector().getFaction(Factions.PERSEAN);
		
		if (currentStage == Stage.RETRIEVE_CORES) {
			info.addPara("Recover an asset stored on %s", opad, h, market.getName());
		}
		else if (currentStage == Stage.RETURN_CORES) {
			LabelAPI label = info.addPara("Return the Asset you've recovered to %s at %s. Alternatively, you might choose to bring them to either the %s or the %s.", opad, h, pName, 
					havelock.getMarket().getName(), heg.getDisplayNameWithArticle(),
					pl.getDisplayNameLongWithArticle());
			label.setHighlight(pName, havelock.getMarket().getName(), 
					heg.getDisplayNameWithArticleWithoutArticle(),
					pl.getDisplayNameWithArticleWithoutArticle());
			label.setHighlightColors(h, havelock.getMarket().getFaction().getBaseUIColor(), 
					heg.getBaseUIColor(), pl.getBaseUIColor());
		}
	}

	@Override
	public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad) {
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.RETRIEVE_CORES) {
			info.addPara("Recover the asset from %s", pad, tc, h, market.getName());
			return true;
		}
		else if (currentStage == Stage.RETURN_CORES) {
			info.addPara("Deliver the asset to %s", pad, tc,
					havelock.getMarket().getTextColorForFactionOrPlanet(), 
					havelock.getMarket().getName());
		}
		return false;
	}

	@Override
	public String getBaseName() {
		return "No Such Audition";
	}

	@Override
	public String getPostfixForState() {
		if (startingStage != null) {
			return "";
		}
		return super.getPostfixForState();
	}
	
}





