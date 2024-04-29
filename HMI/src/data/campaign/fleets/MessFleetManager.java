package data.campaign.fleets;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.fleets.BaseLimitedFleetManager;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.fleets.SourceBasedFleetManager;
import com.fs.starfarer.api.impl.campaign.ids.Abilities;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import data.campaign.procgen.MessRemnAssignmentAI;
import org.lazywizard.lazylib.MathUtils;

import java.util.Random;

import static com.fs.starfarer.api.util.Misc.random;

public class MessFleetManager extends SourceBasedFleetManager {


	protected int minPts;
	protected int maxPts;
	protected int totalLost;

	public MessFleetManager(SectorEntityToken source, float thresholdLY, int minFleets, int maxFleets, float respawnDelay,
									   int minPts, int maxPts) {
		super(source, thresholdLY, minFleets, maxFleets, respawnDelay);
		this.minPts = minPts;
		this.maxPts = maxPts;
	}

	@Override
	protected CampaignFleetAPI spawnFleet() {
		SectorEntityToken spawnEntityMess = Get_Messy();
		if (spawnEntityMess == null) {
			return null;
		}

		Random random = new Random();

		int combatPoints = minPts + random.nextInt(maxPts - minPts + 1);

		int bonus = totalLost * 1;
		if (bonus > maxPts) bonus = maxPts;

		combatPoints += bonus;

		String type = FleetTypes.PATROL_SMALL;
		if (combatPoints > 12) type = FleetTypes.PATROL_MEDIUM;
		if (combatPoints > 24) type = FleetTypes.PATROL_LARGE;

		combatPoints *= 6f;

		MarketAPI messmarket = Global.getFactory().createMarket(String.valueOf((new Random()).nextLong()),
				String.valueOf((new Random()).nextLong()), 5);

		FleetParamsV3 params = new FleetParamsV3(
				null, // market
				spawnEntityMess.getLocation(), // location
				"mess", // fleet's faction, if different from above, which is also used for source market picking
				1.5f,
				type,
				combatPoints, // combatPts
				0f, // freighterPts
				0f, // tankerPts
				0f, // transportPts
				0f, // linerPts
				0f, // utilityPts
				0 // qualityBonus
		);
		params.random = random;

		LocationAPI location = spawnEntityMess.getContainingLocation();  // a star system, or hyperspace
		CampaignFleetAPI fleet = FleetFactoryV3.createFleet(params);
		if (fleet == null) return null;;
		location.addEntity(fleet);// adds our new fleet into the containing location
		fleet.setLocation(spawnEntityMess.getLocation().x, spawnEntityMess.getLocation().y);// positions fleet on top of the spawn entity

		if ((fleet == null) || fleet.isEmpty()) {
			return null;
		}
		fleet.removeAbility(Abilities.EMERGENCY_BURN);
		fleet.removeAbility(Abilities.SENSOR_BURST);
		fleet.removeAbility(Abilities.GO_DARK);

		// to make sure they attack the player on sight when player's transponder is off
		fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_SAW_PLAYER_WITH_TRANSPONDER_ON, true);
		fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_PATROL_FLEET, true);
		fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_ALLOW_DISENGAGE, false);
		fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, true);
		fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_NO_JUMP, true);
		fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_FIGHT_TO_THE_LAST, true);
		fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_HOLD_VS_STRONGER, true);

// 		Commenting this out for now, it might be that doing this both here and in the faction file results in a double negative?

		fleet.addScript(new MessRemnAssignmentAI(fleet, (StarSystemAPI) spawnEntityMess.getContainingLocation(), spawnEntityMess));




		return fleet;
	}

	protected SectorEntityToken Get_Messy() {
		WeightedRandomPicker<SectorEntityToken> picker = new WeightedRandomPicker<SectorEntityToken>();
		for (StarSystemAPI system : Global.getSector().getStarSystems()) {
			float weight = 0f;
			if (system.getName().equals("Opuntia")) continue;
			float w = 11f;
			if (w > weight) weight = w;

			for (SectorEntityToken entity : system.getAllEntities()) {
				if (entity.getId().equals("mess_station1")) {
					picker.add(entity, weight);
				}
			}
		}

		return picker.pick();
	}

	@Override
	public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, CampaignEventListener.FleetDespawnReason reason, Object param) {
		super.reportFleetDespawnedToListener(fleet, reason, param);
		if (reason == CampaignEventListener.FleetDespawnReason.DESTROYED_BY_BATTLE) {
			totalLost++;
		}
	}

}













