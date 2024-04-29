package data.campaign.procgen;

import java.util.Random;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.AICoreOfficerPlugin;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.BaseGenericPlugin;
import com.fs.starfarer.api.impl.campaign.DModManager;
import com.fs.starfarer.api.impl.campaign.fleets.DefaultFleetInflater;
import com.fs.starfarer.api.impl.campaign.fleets.DefaultFleetInflaterParams;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageGenFromSeed.SDMParams;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageGenFromSeed.SalvageDefenderModificationPlugin;
import com.fs.starfarer.api.loading.VariantSource;
import com.fs.starfarer.api.util.Misc;
import data.campaign.procgen.HMIThemeGenerator;;import static com.fs.starfarer.api.campaign.PersonImportance.VERY_HIGH;
import static com.fs.starfarer.api.characters.FullName.Gender.MALE;

public class BlackLuddStationPluginImpl extends BaseGenericPlugin implements SalvageDefenderModificationPlugin {
	
	public float getStrength(SDMParams p, float strength, Random random, boolean withOverride) {
		// doesn't matter, just something non-zero so we end up with a fleet
		// the auto-generated fleet will get replaced by this anyway
		return strength;
	}
	public float getMinSize(SDMParams p, float minSize, Random random, boolean withOverride) {
		return minSize;
	}
	
	public float getMaxSize(SDMParams p, float maxSize, Random random, boolean withOverride) {
		return maxSize; 
	}
	
	public float getProbability(SDMParams p, float probability, Random random, boolean withOverride) {
		return probability;
	}
	
	public void reportDefeated(SDMParams p, SectorEntityToken entity, CampaignFleetAPI fleet) {
	}
	
	public void modifyFleet(SDMParams p, CampaignFleetAPI fleet, Random random, boolean withOverride) {


		fleet.setNoFactionInName(true);
		fleet.setName("The Audience");
		fleet.getFleetData().clear();
		fleet.getFleetData().setShipNameRandom(random);

		FleetMemberAPI member = fleet.getFleetData().addFleetMember("doom_lpblack");
		member.setId("lpb_" + random.nextLong());

		PersonAPI person = createBlackHoleLPCaptain();
		member.setCaptain(person);

		ShipVariantAPI v = member.getVariant().clone();
		v.setSource(VariantSource.REFIT);
		v.addTag(Tags.TAG_NO_AUTOFIT);
		member.setVariant(v, false, true);
		fleet.setCommander(person);
		fleet.getMemoryWithoutUpdate().set("$hmiblackludd_Fleet", true);

		fleet.getFleetData().addFleetMember("harbinger_lpblack");
		fleet.getFleetData().addFleetMember("harbinger_lpblack");
		fleet.getFleetData().addFleetMember("harbinger_lpblack");
		fleet.getFleetData().addFleetMember("afflictor_lpblack");
		fleet.getFleetData().addFleetMember("afflictor_lpblack");
		fleet.getFleetData().addFleetMember("afflictor_lpblack");
		fleet.getFleetData().addFleetMember("shade_lpblack");
		fleet.getFleetData().addFleetMember("shade_lpblack");
		fleet.getFleetData().addFleetMember("shade_lpblack");
		fleet.getFleetData().addFleetMember("gremlin_luddic_path_Strike");
		fleet.getFleetData().addFleetMember("gremlin_luddic_path_Strike");
		fleet.getFleetData().addFleetMember("gremlin_luddic_path_Strike");
		fleet.getFleetData().addFleetMember("gremlin_luddic_path_Strike");
		fleet.getFleetData().addFleetMember("gremlin_luddic_path_Strike");
		fleet.getFleetData().addFleetMember("afflictor_d_pirates_Strike");
		fleet.getFleetData().addFleetMember("afflictor_d_pirates_Strike");
		fleet.getFleetData().addFleetMember("shade_d_pirates_Assault");
		fleet.getFleetData().addFleetMember("shade_d_pirates_Assault");

		for (FleetMemberAPI curr : fleet.getFleetData().getMembersListCopy()) {
			curr.getRepairTracker().setCR(curr.getRepairTracker().getMaxCR());
		}
		
		for (FleetMemberAPI curr : fleet.getFleetData().getMembersListCopy()) {
			v = curr.getVariant().clone();
			v.setSource(VariantSource.REFIT);
			curr.setVariant(v, false, false);
		}

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
			if (curr.isFlagship()) {
				curr.getVariant().addTag(Tags.VARIANT_ALWAYS_RECOVERABLE);
				variant.addPermaMod("hmi_blackholepathboost_flag");
			}
			if (curr.getHullSpec().isPhase() && !curr.isFlagship()) {
				variant.addPermaMod("hmi_blackholepathboost");
			}
		}
	}

	public static PersonAPI createBlackHoleLPCaptain() {
		PersonAPI person = Global.getFactory().createPerson();
		person.setId("hmi_lpblackhole_francis");
		person.getName().setFirst("Brother");
		person.getName().setLast("Francis");
		person.setFaction(Factions.LUDDIC_PATH);
		person.setGender(MALE);
		person.setPortraitSprite(Global.getSettings().getSpriteName("characters", "hmi_lucia"));
		person.setPersonality(Personalities.RECKLESS);
		person.setRankId(Ranks.SPACE_CAPTAIN);
		person.setPostId(null);

		person.getStats().setSkipRefresh(true);

		person.getStats().setLevel(10);
		person.getStats().setSkillLevel(Skills.IMPACT_MITIGATION, 2);
		person.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 2);
		person.getStats().setSkillLevel(Skills.POLARIZED_ARMOR, 2);
		person.getStats().setSkillLevel(Skills.MISSILE_SPECIALIZATION, 2);
		person.getStats().setSkillLevel(Skills.FIELD_MODULATION, 2);
		person.getStats().setSkillLevel(Skills.DAMAGE_CONTROL, 2);
		person.getStats().setSkillLevel(Skills.PHASE_CORPS, 2);

		person.getStats().setSkipRefresh(false);

		return person;
	}

	@Override
	public int getHandlingPriority(Object params) {
		if (!(params instanceof SDMParams)) return 0;
		SDMParams p = (SDMParams) params;
		
		if (p.entity != null && p.entity.getMemoryWithoutUpdate().contains(
				HMIThemeGenerator.BLACKLUDDDEFENDER_KEY)) {
			return 2;
		}
		return -1;
	}
	public float getQuality(SDMParams p, float quality, Random random, boolean withOverride) {
		return quality;
	}
}



