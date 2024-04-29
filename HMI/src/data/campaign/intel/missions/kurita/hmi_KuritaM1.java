package data.campaign.intel.missions.kurita;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;

import java.awt.Color;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.missions.hub.BaseMissionHub;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithBarEvent;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import static com.fs.starfarer.api.impl.campaign.ids.FleetTypes.PATROL_MEDIUM;
import java.util.List;
import java.util.Map;
import data.campaign.intel.missions.kurita.HMIQuestUtils;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin.DerelictType;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.ui.SectorMapAPI;
import exerelin.campaign.intel.SpecialContactIntel;
import org.apache.log4j.Logger;

public class hmi_KuritaM1 extends HubMissionWithBarEvent {

	public static Logger log = Global.getLogger(hmi_KuritaM1.class);

	// chance of also spawning a Remnant fleet
	public static float PROB_SCAV = 0.5f;
	// time we have to complete the mission
	public static float MISSION_DAYS = 120f;

	public static enum Stage {
		FIND_CLUE,
		KILL_FLEET,
		COMPLETED,
		FAILED,
	}

	protected SectorEntityToken derelict;
	protected SectorEntityToken cache;
	protected CampaignFleetAPI target;
	protected PersonAPI executive;
	protected StarSystemAPI system;
	protected StarSystemAPI system2;

	protected PersonAPI kurita;
	protected MarketAPI market;


	@Override
	protected boolean create(MarketAPI createdAt, boolean barEvent) {
		// if already accepted by the player? it's a zombie, kill it
		// this happens if we see the option in the bar but don't pick it, causing the previous global reference to hang around forever
		boolean scav = false;

		if (!setGlobalReference("$hmi_kuritaM1_ref")) {
			hmi_KuritaM1 existing = (hmi_KuritaM1) Global.getSector().getMemoryWithoutUpdate().get("$hmi_kuritaM1_ref");
			existing.abort();
			setGlobalReference("$hmi_kuritaM1_ref");
			//return false;
		}

		// if Fuyutsuki exists, the mission must be created there
		if (!createdAt.getId().equals("fuyutsuki")
				&& Global.getSector().getEconomy().getMarket("fuyutsuki") != null) {
			return false;
		}

		String mktFactionId = createdAt.getFactionId();
		if (!"hmi".equals(mktFactionId)) {
			return false;
		}

		if (Global.getSector().getImportantPeople().getData(HMIQuestUtils.PERSON_KURITA) == null) {
			HMIQuestUtils.createKurita(createdAt);
		}
		kurita = getImportantPerson(HMIQuestUtils.PERSON_KURITA);

		if (kurita == null) {
			log.info("Person is null");
			return false;
		}
		personOverride = kurita;
		if (!setPersonMissionRef(kurita, "$hmi_kuritaM1_ref")) {
			return false;
		}

		setStoryMission();

		// set up the disgraced executive
		executive = Global.getSector().getFaction("hmi").createRandomPerson();
		executive.setRankId(Ranks.SPACE_ADMIRAL);
		executive.setPostId(Ranks.POST_SENIOR_EXECUTIVE);
		executive.getMemoryWithoutUpdate().set("$hmi_kuritaM1_exec", true);

		// pick the system with the clues inside
		requireSystemInterestingAndNotUnsafeOrCore();
		preferSystemInInnerSector();
		preferSystemUnexplored();
		preferSystemInDirectionOfOtherMissions();

		system = pickSystem(true);
		if (system == null) return false;

		// pick the target fleet's system
		requireSystemInterestingAndNotUnsafeOrCore();
		preferSystemWithinRangeOf(system.getLocation(), 3f);
		preferSystemUnexplored();
		requireSystemNot(system);

		system2 = pickSystem(true);
		if (system2 == null) return false;

		// roll to see if we're adding Remnants
		if (rollProbability(PROB_SCAV)) {
			scav = true;
		}

		// determine the faction and ship type of the derelict
		String derelict_faction = "hmi";
		DerelictType derelict_type = DerelictType.MEDIUM;
		if (scav) {
			derelict_faction = Factions.INDEPENDENT;
			derelict_type = DerelictType.SMALL;
		}

		// spawn a supply cache and derelict ship, both serving as clues. They have memory flags that are checked for in rules.csv
		cache = spawnEntity(Entities.SUPPLY_CACHE, new LocData(EntityLocationType.HIDDEN, null, system));
		derelict = spawnDerelict(derelict_faction, derelict_type, new LocData(EntityLocationType.HIDDEN, null, system));
		cache.getMemoryWithoutUpdate().set("$hmi_kuritaM1_clue", true);
		setEntityMissionRef(cache, "$hmi_kuritaM1_ref");
		derelict.getMemoryWithoutUpdate().set("$hmi_kuritaM1_clue", true);
		setEntityMissionRef(derelict, "$hmi_kuritaM1_ref");

		setStartingStage(Stage.FIND_CLUE);
		addSuccessStages(Stage.COMPLETED);
		addFailureStages(Stage.FAILED);

		setStageOnMemoryFlag(Stage.COMPLETED, kurita, "$hmi_kuritaM1_completed");

		setStageOnMemoryFlag(Stage.FAILED, kurita, "$hmi_kuritaM1_failed");

		// set up the target fleet. I've done this using the old style, because the trigger-system doesn't support event listeners by default,
		// and we need to know when this fleet dies or despawns
		FleetParamsV3 params = new FleetParamsV3(
				null,
				null,
				"hmi",
				null,
				PATROL_MEDIUM,
				50f, // combatPts
				10f, // freighterPts
				10f, // tankerPts
				0f, // transportPts
				0f, // linerPts
				0f, // utilityPts
				-0.25f // qualityMod
		);
		// toughen them up, in exchange for the shoddy ship quality we set
		params.averageSMods = 2;
		target = FleetFactoryV3.createFleet(params);

		target.setName(executive.getNameString() + "'s Fleet");
		target.setNoFactionInName(true);

		target.setCommander(executive);
		target.getFlagship().setCaptain(executive);

		Misc.makeHostile(target);
		Misc.makeNoRepImpact(target, "$hmi_kuritaM1");
		Misc.makeImportant(target, "$hmi_kuritaM1");

		target.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, "$hmi_kuritaM1");
		target.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE_ONE_BATTLE_ONLY, "$hmi_kuritaM1");
		target.getMemoryWithoutUpdate().set(MemFlags.FLEET_IGNORED_BY_OTHER_FLEETS, "$hmi_kuritaM1");
		target.getMemoryWithoutUpdate().set(MemFlags.FLEET_IGNORES_OTHER_FLEETS, "$hmi_kuritaM1");

		target.getMemoryWithoutUpdate().set("$hmi_kuritaM1_execfleet", true);
		target.getAI().addAssignment(FleetAssignment.PATROL_SYSTEM, system2.getCenter(), 200f, null);
		//target.addEventListener(this);
		system2.addEntity(target);

		// optionally spawn a Scavenger fleet when we close in on the executive's system
		if (scav) {
			beginWithinHyperspaceRangeTrigger(system2, 1f, false, Stage.FIND_CLUE, Stage.KILL_FLEET);
			triggerCreateFleet(FleetSize.LARGE, FleetQuality.VERY_HIGH, Factions.INDEPENDENT, PATROL_MEDIUM, system2);
			triggerMakeHostileAndAggressive();
			triggerAutoAdjustFleetStrengthMajor();
			//triggerSetRemnantConfig();
			//triggerMakeFleetIgnoredByOtherFleets();
			triggerPickLocationAtInSystemJumpPoint(system2);
			triggerSpawnFleetAtPickedLocation(null, null);
			triggerOrderFleetPatrol(system2, true, Tags.JUMP_POINT, Tags.SALVAGEABLE, Tags.PLANET);
			endTrigger();
		}

		// set a global reference we can use, useful for once-off missions.
		if (!setGlobalReference("$hmi_kuritaM1_ref")) return false;

		// set our starting, success and failure stages
		setStartingStage(Stage.FIND_CLUE);
		setSuccessStage(Stage.COMPLETED);
		setFailureStage(Stage.FAILED);

		// set stage transitions when certain global flags are set, and when certain flags are set on the questgiver
		setStageOnGlobalFlag(Stage.KILL_FLEET, "$hmi_kuritaM1_foundclue");
		setStageOnMemoryFlag(Stage.COMPLETED, kurita, "$hmi_kuritaM1_completed");
		setStageOnMemoryFlag(Stage.FAILED, kurita, "$hmi_kuritaM1_failed");
		// set time limit and credit reward
		setTimeLimit(Stage.FAILED, MISSION_DAYS, system2);
		setCreditReward(CreditReward.LOW);

		return true;
	}


	@Override
	protected boolean callAction(String action, String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
		if (action.equals("unsetClues")) {
			// make the other clue no longer a clue
			if (cache != null) {
				cache.getMemoryWithoutUpdate().unset("$hmi_kuritaM1_clue");
			}
			if (derelict != null) {
				derelict.getMemoryWithoutUpdate().unset("$hmi_kuritaM1_clue");
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean callEvent(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
		String action = params.get(0).getString(memoryMap);

		if (null != action) switch (action) {
			case "beginIntro":
				dialog.getInteractionTarget().setActivePerson(kurita);
				dialog.getVisualPanel().showPersonInfo(kurita, true);
				updateInteractionData(dialog, memoryMap);
				return false;
			case "accept":
				setMarketMissionRef(market, "$hmi_kuritaM1_ref");
				accept(dialog, memoryMap);
				return true;
			case "cancel":
				MarketAPI market = dialog.getInteractionTarget().getMarket();
				market.removePerson(kurita);
				abort();
				return false;
			case "forceShowPerson":
				dialog.getVisualPanel().showPersonInfo(kurita);
				return true;
			case "complete":
				BaseMissionHub.set(kurita, new BaseMissionHub(kurita));
				kurita.getMemoryWithoutUpdate().set(BaseMissionHub.NUM_BONUS_MISSIONS, 1);
				kurita.getMemoryWithoutUpdate().set("$hmi_kuritaM1_completed", true);
				((RuleBasedDialog)dialog.getPlugin()).updateMemory();
				return true;
			case "complete2":
				kurita.getName().setFirst("Bob");
				kurita.getName().setLast("Kurita");
				SpecialContactIntel intel = new SpecialContactIntel(kurita, kurita.getMarket());
				Global.getSector().getIntelManager().addIntel(intel, false, dialog.getTextPanel());
				return true;
				// fall through to next level
			case "refuse":
				kurita.getMarket().getCommDirectory().removePerson(kurita);
				kurita.getMarket().removePerson(kurita);
				kurita.getMemoryWithoutUpdate().set("$nex_remM1_failed", true);
				return false;
			default:
				break;
		}

		return super.callEvent(ruleId, dialog, params, memoryMap);
	}

	@Override
	protected void updateInteractionDataImpl() {
		set("$hmi_kuritaM1_reward", Misc.getWithDGS(getCreditsReward()));

		set("$hmi_kuritaM1_execName", executive.getNameString());
		set("$hmi_kuritaM1_systemName", system.getNameWithLowercaseTypeShort());
		set("$hmi_kuritaM1_system2Name", system2.getNameWithLowercaseTypeShort());
		set("$hmi_kuritaM1_dist", getDistanceLY(system));

	}

	// used to detect when the executive's fleet is destroyed and complete the mission
	public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {
		if (isDone() || result != null) return;

		// also credit the player if they're in the same location as the fleet and nearby
		float distToPlayer = Misc.getDistance(fleet, Global.getSector().getPlayerFleet());
		boolean playerInvolved = battle.isPlayerInvolved() || (fleet.isInCurrentLocation() && distToPlayer < 2000f);

		if (battle.isInvolved(fleet) && !playerInvolved) {
			if (fleet.getFlagship() == null || fleet.getFlagship().getCaptain() != target) {
				fleet.setCommander(fleet.getFaction().createRandomPerson());
				getPerson().getMemoryWithoutUpdate().set("$hmi_kuritaM1_dead", true);
				return;
			}
		}

		if (!playerInvolved || !battle.isInvolved(fleet) || battle.onPlayerSide(fleet)) {
			return;
		}

		// didn't destroy the original flagship
		if (fleet.getFlagship() != null && fleet.getFlagship().getCaptain() == target) return;

		getPerson().getMemoryWithoutUpdate().set("$hmi_kuritaM1_completed", true);
	}

	// if the fleet despawns for whatever reason, fail the mission
	public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, CampaignEventListener.FleetDespawnReason reason, Object param) {
		if (isDone() || result != null) return;

		if (fleet.getMemoryWithoutUpdate().contains("$hmi_kuritaM1_execfleet")) {
			getPerson().getMemoryWithoutUpdate().set("$hmi_kuritaM1_failed", true);
		}
	}

	// description when selected in intel screen
	@Override
	public void addDescriptionForNonEndStage(TooltipMakerAPI info, float width, float height) {
		float opad = 10f;
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.FIND_CLUE) {
			info.addPara("Look for clues as to the whereabouts of Bob's competitor in the " +
					system.getNameWithLowercaseTypeShort() + ".", opad);
		} else if (currentStage == Stage.KILL_FLEET) {
			info.addPara("Hunt down and eliminate Bob's competitor in the " +
					system2.getNameWithLowercaseTypeShort() + ".", opad);
			if (isDevMode()) {
				info.addPara("DEVMODE: EXECUTIVE IS LOCATED IN THE " +
						system2.getNameWithLowercaseTypeShort() + ".", opad);
			}
		}
	}
		// short description in popups and the intel entry
		@Override
		public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad) {
			Color h = Misc.getHighlightColor();
			if (currentStage == Stage.FIND_CLUE) {
				info.addPara("Look for clues in the " +
						system.getNameWithLowercaseTypeShort(), tc, pad);
				return true;
			} else if (currentStage == Stage.KILL_FLEET) {
				info.addPara("Hunt down the disgraced executive in the " +
						system2.getNameWithLowercaseTypeShort(), tc, pad);
				return true;
			}
			return false;
		}

		@Override
		public SectorEntityToken getMapLocation (SectorMapAPI map){
			if (currentStage == Stage.FIND_CLUE) {
				return getMapLocationFor(system.getCenter());
			} else if (currentStage == Stage.KILL_FLEET) {
				return getMapLocationFor(system2.getCenter());
			}
			return null;
		}

		// mission name
		@Override
		public String getBaseName () {
			return "Cleaning Solution";
		}

		@Override
		public String getPostfixForState () {
			if (startingStage != null) {
				return "";
			}
			return super.getPostfixForState();
		}

	}






