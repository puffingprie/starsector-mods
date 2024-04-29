package data.campaign.procgen;

import java.util.Random;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.AICoreOfficerPlugin;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.BaseGenericPlugin;
import com.fs.starfarer.api.impl.campaign.DModManager;
import com.fs.starfarer.api.impl.campaign.fleets.DefaultFleetInflater;
import com.fs.starfarer.api.impl.campaign.fleets.DefaultFleetInflaterParams;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.procgen.themes.MiscellaneousThemeGenerator;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageGenFromSeed.SDMParams;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageGenFromSeed.SalvageDefenderModificationPlugin;
import com.fs.starfarer.api.loading.VariantSource;
import com.fs.starfarer.api.util.Misc;
import data.scripts.world.systems.HMI_seele;

import static com.fs.starfarer.api.characters.FullName.Gender.ANY;
import static com.fs.starfarer.api.characters.FullName.Gender.MALE;

//Remember - you need to activate the script in the modplugin for it to work

public class NightmareDefenderPluginImpl extends BaseGenericPlugin implements SalvageDefenderModificationPlugin {
	
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
		fleet.setName("Abyssal Horror");

//		AICoreOfficerPlugin plugin = Misc.getAICoreOfficerPlugin(Commodities.ALPHA_CORE);
		
		fleet.getFleetData().clear();
		fleet.getFleetData().setShipNameRandom(random);
		
		
		FleetMemberAPI member = fleet.getFleetData().addFleetMember("hmi_spookyboi_base_var");
		member.setId("spook_" + random.nextLong());

		///Throwing this in to see if it stops a null pointer crash
		// Edit: no it doesn't
//		PersonAPI person = createNightmareCaptain();
//		member.setCaptain(person);

		ShipVariantAPI v = member.getVariant().clone();
		v.setSource(VariantSource.REFIT);
		v.addTag(Tags.TAG_NO_AUTOFIT);
		v.addTag(Tags.SHIP_LIMITED_TOOLTIP);
		member.setVariant(v, false, true);
//		fleet.setCommander(person);

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
			dfip.averageSMods = 3;
			dfip.quality = 0.4f;
			
			// what a HACK
			DModManager.assumeAllShipsAreAutomated = true;
			fleet.inflateIfNeeded();
			fleet.setInflater(null);
			DModManager.assumeAllShipsAreAutomated = false;
		}
	}

//	public static PersonAPI createNightmareCaptain() {
//		PersonAPI person = Global.getFactory().createPerson();
//		person.getName().setFirst("???");
//		person.setFaction("hmi_nightmare");
//		person.setGender(ANY);
//		person.setPortraitSprite(Global.getSettings().getSpriteName("characters", "hmi_nightmare"));
//		person.setPersonality(Personalities.RECKLESS);
//		person.setRankId(Ranks.SPACE_CAPTAIN);
//		person.setPostId(null);

//		person.getStats().setSkipRefresh(true);

//		person.getStats().setLevel(10);
//		person.getStats().setSkillLevel(Skills.IMPACT_MITIGATION, 2);
//		person.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 2);
//		person.getStats().setSkillLevel(Skills.POLARIZED_ARMOR, 2);
//		person.getStats().setSkillLevel(Skills.MISSILE_SPECIALIZATION, 2);
//		person.getStats().setSkillLevel(Skills.FIELD_MODULATION, 2);
//		person.getStats().setSkillLevel(Skills.DAMAGE_CONTROL, 2);
//		person.getStats().setSkillLevel(Skills.PHASE_CORPS, 2);
//		person.getStats().setSkipRefresh(false);
//
//		return person;
//	}

	@Override
	public int getHandlingPriority(Object params) {
		if (!(params instanceof SDMParams)) return 0;
		SDMParams p = (SDMParams) params;
		
		if (p.entity != null && p.entity.getMemoryWithoutUpdate().contains(
				HMI_seele.HMINIGHTMARE_KEY)) {
			return 2;
		}
		return -1;
	}
	public float getQuality(SDMParams p, float quality, Random random, boolean withOverride) {
		return quality;
	}
}



