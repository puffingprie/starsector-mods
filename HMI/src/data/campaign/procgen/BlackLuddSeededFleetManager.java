package data.campaign.procgen;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.loading.VariantSource;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.campaign.FleetEncounterContextPlugin.DataForEncounterSide;
import com.fs.starfarer.api.campaign.FleetEncounterContextPlugin.FleetMemberData;
import com.fs.starfarer.api.combat.BattleCreationContext;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.FleetEncounterContext;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl.FIDConfig;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl.FIDConfigGen;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl.FIDDelegate;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.fleets.SeededFleetManager;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.SalvageEntityGenDataSpec.DropData;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageEntity;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class BlackLuddSeededFleetManager extends SeededFleetManager {


	protected int minPts;
	protected int maxPts;
	protected float activeChance;

	public BlackLuddSeededFleetManager(StarSystemAPI system, int minFleets, int maxFleets, int minPts, int maxPts, float activeChance) {
		super(system, 1f);
		this.minPts = minPts;
		this.maxPts = maxPts;
		this.activeChance = activeChance;
		
		int num = minFleets + StarSystemGenerator.random.nextInt(maxFleets - minFleets + 1);
		for (int i = 0; i < num; i++) {
			long seed = StarSystemGenerator.random.nextLong();
			addSeed(seed);
		}
	}

	@Override
	protected CampaignFleetAPI spawnFleet(long seed) {
		Random random = new Random(seed);
		
		int combatPoints = minPts + random.nextInt(maxPts - minPts + 1);
		
		String type = FleetTypes.PATROL_SMALL;
		if (combatPoints > 25) type = FleetTypes.PATROL_MEDIUM;
		
		combatPoints *= 8f; // 8 is fp cost of remnant frigate
		
		FleetParamsV3 params = new FleetParamsV3(
				system.getLocation(),
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
		params.averageSMods = 2;
		params.forceAllowPhaseShipsEtc = true;
		params.modeOverride = FactionAPI.ShipPickMode.PRIORITY_THEN_ALL;
		params.quality = 0.4f;

		CampaignFleetAPI fleet = FleetFactoryV3.createFleet(params);
		if (fleet == null) return null;

//		for (FleetMemberAPI curr : fleet.getFleetData().getMembersListCopy()) {
//			curr.getVariant().addPermaMod(HullMods.SOLAR_SHIELDING);
//			curr.getVariant().addTag(Tags.TAG_RETAIN_SMODS_ON_RECOVERY);
//		}

		for (FleetMemberAPI curr : fleet.getFleetData().getMembersListCopy()) {
			if (curr.getVariant().getSource() != VariantSource.REFIT) {
				ShipVariantAPI variant = curr.getVariant().clone();
				variant.setOriginalVariant(null);
				variant.setHullVariantId(Misc.genUID());
				variant.setSource(VariantSource.REFIT);
				curr.setVariant(variant, false, true);
			}
			curr.getVariant().addPermaMod(HullMods.SOLAR_SHIELDING);
			curr.getVariant().addTag(Tags.TAG_RETAIN_SMODS_ON_RECOVERY);
		}

		system.addEntity(fleet);
		fleet.setFacing(random.nextFloat() * 360f);

		boolean dormant = random.nextFloat() >= activeChance;
		//dormant = false;
		int numActive = 0;
		for (SeededFleet f : fleets) {
			if (f.fleet != null) numActive++;
		}
		if (numActive == 0 && activeChance > 0) { // first fleet is not dormant, to ensure one active fleet always
			dormant = false;
		}
		initBlackLuddFleetProperties(random, fleet, dormant);
		
		if (dormant) {
			SectorEntityToken target = pickEntityToGuard(random, system, fleet);
			if (target != null) {
//				Vector2f loc = Misc.getPointAtRadius(target.getLocation(), 300f, random);
//				fleet.setLocation(loc.x, loc.y);
				
				fleet.setCircularOrbit(target, 
						random.nextFloat() * 360f, 
						fleet.getRadius() + target.getRadius() + 100f + 100f * random.nextFloat(),
						25f + 5f * random.nextFloat());
			} else {
				Vector2f loc = Misc.getPointAtRadius(new Vector2f(), 4000f, random);
				fleet.setLocation(loc.x, loc.y);
			}
		} else {
			fleet.addScript(new BlackLuddAssignmentAI(fleet, system, null));
		}

		return fleet;
	}
	
	
	public static SectorEntityToken pickEntityToGuard(Random random, StarSystemAPI system, CampaignFleetAPI fleet) {
		WeightedRandomPicker<SectorEntityToken> picker = new WeightedRandomPicker<SectorEntityToken>(random);
		
		for (SectorEntityToken entity : system.getEntitiesWithTag(Tags.SALVAGEABLE)) {
			float w = 1f;
			if (entity.hasTag(Tags.NEUTRINO_HIGH)) w = 3f;
			if (entity.hasTag(Tags.NEUTRINO_LOW)) w = 0.33f;
			picker.add(entity, w);
		}
		
		for (SectorEntityToken entity : system.getJumpPoints()) {
			picker.add(entity, 3f);
		}
		
		return picker.pick();
	}
	
	
	
	public static void initBlackLuddFleetProperties(Random random, CampaignFleetAPI fleet, boolean dormant) {
		if (random == null) random = new Random();
		
		fleet.removeAbility(Abilities.GO_DARK);
		// to make sure they attack the player on sight when player's transponder is off
		fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_SAW_PLAYER_WITH_TRANSPONDER_ON, true);
		fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_PATROL_FLEET, true);
		fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_ALLOW_LONG_PURSUIT, true);
		fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_HOSTILE, true);
		fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_NO_REP_IMPACT, true);

		//Apparently this works to bypass the tithe request.
		fleet.getMemoryWithoutUpdate().set("$LP_titheAskedFor", true);
		fleet.addTag("blackhole_LP_Fleet");

	}
}







