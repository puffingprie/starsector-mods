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

public class MysterySiteStationFleetManager extends SourceBasedFleetManager {

	protected int minPts;
	protected int maxPts;
	protected int totalLost;

	public MysterySiteStationFleetManager(SectorEntityToken source, float thresholdLY, int minFleets, int maxFleets, float respawnDelay,
									  int minPts, int maxPts) {
		super(source, thresholdLY, minFleets, maxFleets, respawnDelay);
		this.minPts = minPts;
		this.maxPts = maxPts;
	}
	
	@Override
	protected CampaignFleetAPI spawnFleet() {
		Random random = new Random();
		
		int combatPoints = minPts + random.nextInt(maxPts - minPts + 1);
		
		int bonus = totalLost * 3;
		if (bonus > maxPts) bonus = maxPts;
		
		combatPoints += bonus;

		String type = FleetTypes.PATROL_SMALL;
		if (combatPoints > 8) type = FleetTypes.PATROL_MEDIUM;
		if (combatPoints > 16) type = FleetTypes.PATROL_LARGE;
		
		combatPoints *= 8f;
		
		FleetParamsV3 params = new FleetParamsV3(
				source.getMarket(),
				source.getLocationInHyperspace(),
				Factions.MERCENARY,
				1.0f,
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
		params.officerLevelBonus = 2;
		params.officerNumberBonus = 2;
		params.averageSMods = 2;
		params.modeOverride = FactionAPI.ShipPickMode.PRIORITY_THEN_ALL;
		params.quality = 1.0f;

		CampaignFleetAPI fleet = FleetFactoryV3.createFleet(params);
		if (fleet == null) return null;

//		for (FleetMemberAPI curr : fleet.getFleetData().getMembersListCopy()) {
//			curr.getVariant().addPermaMod(HullMods.SOLAR_SHIELDING);
//			curr.getVariant().addTag(Tags.TAG_RETAIN_SMODS_ON_RECOVERY);
//		}

		if (fleet.getInflater() instanceof DefaultFleetInflater) {
			DefaultFleetInflater dfi = (DefaultFleetInflater) fleet.getInflater();
			DefaultFleetInflaterParams dfip = (DefaultFleetInflaterParams)dfi.getParams();
			dfip.allWeapons = true;
			dfip.averageSMods = 2;
			dfip.quality = 1.0f;

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
		}

		LocationAPI location = source.getContainingLocation();
		location.addEntity(fleet);

		MysterySiteSeededFleetManager.initMysteryFleetProperties(random, fleet, false);
		
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
