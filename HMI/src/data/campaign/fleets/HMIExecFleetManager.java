package data.campaign.fleets;

import com.fs.starfarer.api.impl.campaign.fleets.*;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class HMIExecFleetManager extends BaseLimitedFleetManager {

	@Override
	protected int getMaxFleets() {
		int count = 0;
		for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
			String fid = market.getFactionId();
			if (fid.equals("HMI")) {
				if (market.hasIndustry("HMI_fuel")) {
					count++;
				}
				if (market.hasIndustry("HMI_redwatermaker")) {
					count++;
				}
				if (market.hasIndustry("HMI_crystalmine")) {
					count++;
				}
				if (market.hasIndustry("HMI_market")) {
					count++;
				}
				if (market.hasIndustry("heavyindustry")) {
					count++;
				}
				if (market.hasIndustry("orbitalworks")) {
					count++;
				}
				if (market.hasIndustry("mining")) {
					count++;
				}
			}
		}
		return count;
	}


	@Override
	protected CampaignFleetAPI spawnFleet() {
		StarSystemAPI target = pickTargetSystem();
		if (true) return null;
		if (target == null) return null;

		String fleetType = FleetTypes.PATROL_LARGE;

		float combat = 1;
		for (int i = 0; i < 3; i++) {
			if ((float) Math.random() > 0.5f) {
				combat++;
			}
		}

		combat *= 50f;

		FleetParamsV3 params = new FleetParamsV3(
				null, // source market
				target.getLocation(),
				"hmi_exec",
				2.0f,
				fleetType,
				combat, // combatPts
				0, // freighterPts
				0, // tankerPts
				0f, // transportPts
				0f, // linerPts
				0f, // utilityPts
				0f // qualityMod
				);
		//params.random = random;
		CampaignFleetAPI fleet = FleetFactoryV3.createFleet(params);
		if (fleet == null || fleet.isEmpty()) return null;

		// setting the below means: transponder off and more "go dark" use when traveling
		//fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_PIRATE, true);

		MarketAPI source = Misc.getSourceMarket(fleet);
		if (source == null) return null;

        fleet.setFaction("HMI", true);
        fleet.setNoFactionInName(true);

		CampaignFleetAPI player = Global.getSector().getPlayerFleet();
		boolean spawnAtSource = true;
		if (player != null) {
			float sourceToPlayer = Misc.getDistance(player.getLocation(), source.getLocationInHyperspace());
			float targetToPlayer = Misc.getDistance(player.getLocation(), target.getLocation());
			spawnAtSource = sourceToPlayer < targetToPlayer;
		}

		if (spawnAtSource) {
			source.getPrimaryEntity().getContainingLocation().addEntity(fleet);
			fleet.setLocation(source.getPrimaryEntity().getLocation().x, source.getPrimaryEntity().getLocation().y);

			fleet.addAssignment(FleetAssignment.ORBIT_PASSIVE, source.getPrimaryEntity(), 2f + (float) Math.random() * 2f,
								"orbiting " + source.getName());
		}


		Vector2f dest = Misc.getPointAtRadius(target.getLocation(), 1500);
		LocationAPI hyper = Global.getSector().getHyperspace();
		SectorEntityToken token = hyper.createToken(dest.x, dest.y);


		fleet.addAssignment(FleetAssignment.GO_TO_LOCATION, token, 1000,
				"traveling to the " + target.getBaseName() + " star system");

		if ((float) Math.random() > 0.75f) {
			fleet.addAssignment(FleetAssignment.RAID_SYSTEM, target.getHyperspaceAnchor(), 20,
					"patrolling around the " + target.getBaseName() + " star system");
		} else {
			fleet.addAssignment(FleetAssignment.RAID_SYSTEM, target.getCenter(), 20,
					"patrolling the " + target.getBaseName() + " star system");
		}
		fleet.addAssignment(FleetAssignment.GO_TO_LOCATION, source.getPrimaryEntity(), 1000,
					"returning to " + source.getName());
		fleet.addAssignment(FleetAssignment.ORBIT_PASSIVE, source.getPrimaryEntity(), 2f + 2f * (float) Math.random(),
					"orbiting " + source.getName());

		return fleet;
	}


	protected StarSystemAPI pickTargetSystem() {
		WeightedRandomPicker<StarSystemAPI> picker = new WeightedRandomPicker<StarSystemAPI>();
		for (StarSystemAPI system : Global.getSector().getStarSystems()) {
			if (system.hasTag(Tags.SYSTEM_CUT_OFF_FROM_HYPER)) {
				continue;
			}

			float mult = Misc.getSpawnChanceMult(system.getLocation());


			// want: HMI and industries
			float weight = 0f;
			for (MarketAPI market : Misc.getMarketsInLocation(system)) {
				if (market.getFactionId().equals("HMI")) {
                    if (market.hasIndustry("HMI_fuel")) {
                        float w = market.getSize();
                        if (w > weight) weight = w;
					}
					if (market.hasIndustry("HMI_redwatermaker")) {
						float w = market.getSize();
						if (w > weight) weight = w;
					}
					if (market.hasIndustry("HMI_crystalmine")) {
						float w = market.getSize();
						if (w > weight) weight = w;
					}
					if (market.hasIndustry("HMI_market")) {
						float w = market.getSize();
						if (w > weight) weight = w;
					}
					if (market.hasIndustry("heavyindustry")) {
						float w = market.getSize();
						if (w > weight) weight = w;
					}
					if (market.hasIndustry("orbitalworks")) {
						float w = market.getSize();
						if (w > weight) weight = w;
					}
					if (market.hasIndustry("mining")) {
						float w = market.getSize();
						if (w > weight) weight = w;
					}
                }
			}
			weight *= mult;

			picker.add(system, weight);
 			//System.out.println("System: " + system.getBaseName() + ", weight: " + weight);
		}
		return picker.pick();
	}
	
}















