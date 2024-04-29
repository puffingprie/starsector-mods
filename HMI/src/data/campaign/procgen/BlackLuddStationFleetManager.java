package data.campaign.procgen;

import java.util.Random;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.DModManager;
import com.fs.starfarer.api.impl.campaign.fleets.*;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.loading.VariantSource;
import com.fs.starfarer.api.util.Misc;

public class BlackLuddStationFleetManager extends SourceBasedFleetManager {

	protected int minPts;
	protected int maxPts;
	protected int totalLost;

	public BlackLuddStationFleetManager(SectorEntityToken source, float thresholdLY, int minFleets, int maxFleets, float respawnDelay,
										int minPts, int maxPts) {
		super(source, thresholdLY, minFleets, maxFleets, respawnDelay);
		this.minPts = minPts;
		this.maxPts = maxPts;
	}

	@Override
	protected CampaignFleetAPI spawnFleet() {

		Random random = new Random();

		int combatPoints = minPts + random.nextInt(maxPts - minPts + 1);

		int bonus = totalLost * 4;
		if (bonus > maxPts) bonus = maxPts;

		combatPoints += bonus;

		String type = FleetTypes.PATROL_SMALL;
		if (combatPoints > 30) type = FleetTypes.PATROL_MEDIUM;

		combatPoints *= 8f;

		FleetParamsV3 params = new FleetParamsV3(
				source.getMarket(),
				source.getLocationInHyperspace(),
				Factions.LUDDIC_PATH,
				0.4f,
				type,
				combatPoints, // combatPts
				0f, // freighterPts 
				0f, // tankerPts
				0f, // transportPts
				0f, // linerPts
				0f, // utilityPts
				0f // qualityMod
		);
		params.random = random;
		FactionDoctrineAPI doctrine = Global.getSector().getFaction(Factions.LUDDIC_PATH).getDoctrine().clone();
		doctrine.setShipSize(3);
		doctrine.setWarships(2);
		doctrine.setCarriers(2);
		doctrine.setPhaseShips(4);
		params.doctrineOverride = doctrine;
		params.officerLevelBonus = 2;
		params.officerNumberBonus = 2;
		params.forceAllowPhaseShipsEtc = true;
		params.modeOverride = FactionAPI.ShipPickMode.PRIORITY_THEN_ALL;

		CampaignFleetAPI fleet = FleetFactoryV3.createFleet(params);
		if (fleet == null) return null;

//		for (FleetMemberAPI curr : fleet.getFleetData().getMembersListCopy()) {
//			curr.getVariant().addPermaMod(HullMods.SOLAR_SHIELDING);
//			curr.getVariant().addTag(Tags.TAG_RETAIN_SMODS_ON_RECOVERY);
//		}

//This is where the s-mod code used to be

		if (fleet.getInflater() instanceof DefaultFleetInflater) {
			DefaultFleetInflater dfi = (DefaultFleetInflater) fleet.getInflater();
			DefaultFleetInflaterParams dfip = (DefaultFleetInflaterParams)dfi.getParams();
			dfip.allWeapons = true;
			dfip.averageSMods = 1;
			dfip.quality = 0.4f;

			// what a HACK
			DModManager.assumeAllShipsAreAutomated = true;
			fleet.inflateIfNeeded();
			fleet.setInflater(null);
			DModManager.assumeAllShipsAreAutomated = false;
		}

		for (FleetMemberAPI curr : fleet.getFleetData().getMembersListCopy()) {
			ShipVariantAPI variant = curr.getVariant().clone();
			variant.setOriginalVariant(null);
			variant.setHullVariantId(Misc.genUID());
			variant.setSource(VariantSource.REFIT);
			variant.addPermaMod(HullMods.SOLAR_SHIELDING, true);
			variant.addTag(Tags.TAG_RETAIN_SMODS_ON_RECOVERY);
			curr.setVariant(variant, false, true);
			curr.getRepairTracker().setCR(curr.getRepairTracker().getMaxCR());
			if (curr.getHullSpec().isPhase()) {
				variant.addPermaMod("hmi_blackholepathboost");
			}
		}

		LocationAPI location = source.getContainingLocation();
		location.addEntity(fleet);

		BlackLuddSeededFleetManager.initBlackLuddFleetProperties(random, fleet, false);


		fleet.setLocation(source.getLocation().x, source.getLocation().y);
		fleet.setFacing(random.nextFloat() * 360f);
		
		fleet.addScript(new BlackLuddAssignmentAI(fleet, (StarSystemAPI) source.getContainingLocation(), source));

		return fleet;
	}

	@Override
	public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, FleetDespawnReason reason, Object param) {
		super.reportFleetDespawnedToListener(fleet, reason, param);
		if (reason == FleetDespawnReason.DESTROYED_BY_BATTLE) {
			totalLost++;
		}
	}
}
