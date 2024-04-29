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
import data.campaign.procgen.HMIThemeGenerator;
import data.scripts.world.HMI_procgen;;import static com.fs.starfarer.api.campaign.PersonImportance.VERY_HIGH;
import static com.fs.starfarer.api.characters.FullName.Gender.ANY;
import static com.fs.starfarer.api.characters.FullName.Gender.MALE;
import static data.campaign.procgen.HMIThemeGenerator.HMI_BLACKSITE_RANDOMSHIPSELECT_KEY;
import static data.campaign.procgen.HMIThemeGenerator.HMI_MYSTERYBLACKHOLESITE_SYSTEM_KEY;

public class MysteryDockyardsStationPluginImpl extends BaseGenericPlugin implements SalvageDefenderModificationPlugin {
	
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
		fleet.setName("Mercenary Defense Fleet");

		fleet.getFleetData().clear();
		fleet.getFleetData().setShipNameRandom(random);
		PersonAPI person = createBlackHoleMercCaptain();

		float shipselect = HMIThemeGenerator.randomNumberGetter.getRandomNumber();
		int shipselect2 = (int) shipselect;

//		if (Global.getSector().getMemoryWithoutUpdate().getInt(HMI_BLACKSITE_RANDOMSHIPSELECT_KEY) == 1){

//		if (HMIThemeGenerator.random_var == 1){

			if (shipselect2 == 1){
			FleetMemberAPI member = fleet.getFleetData().addFleetMember("pegasus_hmi_blackmerc");
			member.setId("bhm_" + random.nextLong());

			member.setCaptain(person);

			ShipVariantAPI v = member.getVariant().clone();
			v.setSource(VariantSource.REFIT);
			v.addTag(Tags.TAG_NO_AUTOFIT);
			v.addTag(Tags.VARIANT_UNRESTORABLE);
			v.addTag(Tags.VARIANT_ALWAYS_RECOVERABLE);
			v.addPermaMod("missile_reload");
			member.setVariant(v, false, true);
			fleet.setCommander(person);

			fleet.getFleetData().addFleetMember("gryphon_DEM");
			fleet.getFleetData().addFleetMember("gryphon_DEM");
			fleet.getFleetData().addFleetMember("champion_Elite");
			fleet.getFleetData().addFleetMember("eradicator_hmimerc");
			fleet.getFleetData().addFleetMember("eradicator_hmimerc");

			fleet.getFleetData().addFleetMember("enforcer_hmimerc");
			fleet.getFleetData().addFleetMember("enforcer_hmimerc");
			fleet.getFleetData().addFleetMember("drover_Strike");
			fleet.getFleetData().addFleetMember("drover_Strike");
			fleet.getFleetData().addFleetMember("drover_Strike");

			fleet.getFleetData().addFleetMember("centurion_hmimerc");
			fleet.getFleetData().addFleetMember("centurion_hmimerc");
			fleet.getFleetData().addFleetMember("centurion_hmimerc");
			fleet.getFleetData().addFleetMember("brawler_hmimerc");
			fleet.getFleetData().addFleetMember("brawler_hmimerc");

			fleet.getFleetData().addFleetMember("afflictor_Strike");
			fleet.getFleetData().addFleetMember("afflictor_Strike");
		for (FleetMemberAPI curr : fleet.getFleetData().getMembersListCopy()) {
					curr.getRepairTracker().setCR(curr.getRepairTracker().getMaxCR());
				}
		for (FleetMemberAPI curr : fleet.getFleetData().getMembersListCopy()) {
				v = curr.getVariant().clone();
				v.setSource(VariantSource.REFIT);
				curr.setVariant(v, false, false);
			}

		}

//		if (Global.getSector().getMemoryWithoutUpdate().getInt(HMI_BLACKSITE_RANDOMSHIPSELECT_KEY) == 2) {
		if (shipselect2 == 2){
			FleetMemberAPI member = fleet.getFleetData().addFleetMember("invictus_hmimerc");
			member.setId("bhm_" + random.nextLong());

			member.setCaptain(person);

			ShipVariantAPI v = member.getVariant().clone();
			v.setSource(VariantSource.REFIT);
			v.addTag(Tags.TAG_NO_AUTOFIT);
			v.addTag(Tags.TAG_AUTOMATED_NO_PENALTY);
			v.addTag(Tags.VARIANT_UNRESTORABLE);
			v.addTag(Tags.VARIANT_ALWAYS_RECOVERABLE);
			v.addPermaMod(HullMods.AUTOMATED);
			member.setVariant(v, false, true);
			fleet.setCommander(person);

			fleet.getFleetData().addFleetMember("heron_Strike");
			fleet.getFleetData().addFleetMember("heron_Attack1");
			fleet.getFleetData().addFleetMember("eradicator_hmimerc");
			fleet.getFleetData().addFleetMember("eradicator_hmimerc");

			fleet.getFleetData().addFleetMember("enforcer_hmimerc");
			fleet.getFleetData().addFleetMember("enforcer_hmimerc");
			fleet.getFleetData().addFleetMember("drover_Strike");
			fleet.getFleetData().addFleetMember("drover_Support");
			fleet.getFleetData().addFleetMember("drover_Support");

			fleet.getFleetData().addFleetMember("centurion_hmimerc");
			fleet.getFleetData().addFleetMember("monitor_Escort");
			fleet.getFleetData().addFleetMember("monitor_Escort");
			fleet.getFleetData().addFleetMember("monitor_Escort");
			fleet.getFleetData().addFleetMember("monitor_Escort");
			fleet.getFleetData().addFleetMember("centurion_hmimerc");


			for (FleetMemberAPI curr : fleet.getFleetData().getMembersListCopy()) {
				v = curr.getVariant().clone();
				v.setSource(VariantSource.REFIT);
				curr.setVariant(v, false, false);
			}

			for (FleetMemberAPI curr : fleet.getFleetData().getMembersListCopy()) {
				curr.getRepairTracker().setCR(curr.getRepairTracker().getMaxCR());
			}

			if (fleet.getInflater() instanceof DefaultFleetInflater) {
				DefaultFleetInflater dfi = (DefaultFleetInflater) fleet.getInflater();
				DefaultFleetInflaterParams dfip = (DefaultFleetInflaterParams) dfi.getParams();
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
				curr.getVariant().addTag(Tags.TAG_RETAIN_SMODS_ON_RECOVERY);
				if (curr.isFlagship()) {
					curr.getVariant().addTag(Tags.VARIANT_ALWAYS_RECOVERABLE);
				}
			}
		}
		if (shipselect2 >= 3){
			FleetMemberAPI member = fleet.getFleetData().addFleetMember("conquest_hmimerc");
			member.setId("bhm_" + random.nextLong());

			member.setCaptain(person);

			ShipVariantAPI v = member.getVariant().clone();
			v.setSource(VariantSource.REFIT);
			v.addTag(Tags.TAG_NO_AUTOFIT);
			v.addTag(Tags.VARIANT_UNRESTORABLE);
			v.addTag(Tags.VARIANT_ALWAYS_RECOVERABLE);
			v.addPermaMod(HullMods.AUTOMATED);
			member.setVariant(v, false, true);
			fleet.setCommander(person);

			fleet.getFleetData().addFleetMember("heron_Attack1");
			fleet.getFleetData().addFleetMember("champion_Elite");
			fleet.getFleetData().addFleetMember("eagle_Assault");
			fleet.getFleetData().addFleetMember("eagle_Assault");
			fleet.getFleetData().addFleetMember("aurora_Attack");

			fleet.getFleetData().addFleetMember("hammerhead_DEM");
			fleet.getFleetData().addFleetMember("hammerhead_DEM");
			fleet.getFleetData().addFleetMember("sunder_CS");
			fleet.getFleetData().addFleetMember("sunder_CS");
			fleet.getFleetData().addFleetMember("medusa_Attack");
			fleet.getFleetData().addFleetMember("medusa_Attack");

			fleet.getFleetData().addFleetMember("brawler_hmimerc");
			fleet.getFleetData().addFleetMember("brawler_hmimerc");
			fleet.getFleetData().addFleetMember("brawler_hmimerc");
			fleet.getFleetData().addFleetMember("wolf_Strike");
			fleet.getFleetData().addFleetMember("wolf_Strike");

			for (FleetMemberAPI curr : fleet.getFleetData().getMembersListCopy()) {
				curr.getRepairTracker().setCR(1);
			}

			for (FleetMemberAPI curr : fleet.getFleetData().getMembersListCopy()) {
				v = curr.getVariant().clone();
				v.setSource(VariantSource.REFIT);
				curr.setVariant(v, false, false);
			}


			if (fleet.getInflater() instanceof DefaultFleetInflater) {
				DefaultFleetInflater dfi = (DefaultFleetInflater) fleet.getInflater();
				DefaultFleetInflaterParams dfip = (DefaultFleetInflaterParams) dfi.getParams();
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
				curr.getVariant().addTag(Tags.TAG_RETAIN_SMODS_ON_RECOVERY);
				if (curr.isFlagship()) {
					curr.getVariant().addTag(Tags.VARIANT_ALWAYS_RECOVERABLE);
				}
			}
		}
	}

	public static PersonAPI createBlackHoleMercCaptain() {
		PersonAPI person = Global.getFactory().createPerson();
		person.getName().setFirst("Blutarch");
		person.setFaction(Factions.MERCENARY);
		person.setGender(ANY);
		person.setPortraitSprite(Global.getSettings().getSpriteName("characters", "hmi_blackmerc"));
		person.setPersonality(Personalities.AGGRESSIVE);
		person.setRankId(Ranks.SPACE_CAPTAIN);
		person.setPostId(null);

		person.getStats().setSkipRefresh(true);

		person.getStats().setLevel(10);
		person.getStats().setSkillLevel(Skills.IMPACT_MITIGATION, 2);
		person.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 2);
		person.getStats().setSkillLevel(Skills.HELMSMANSHIP, 2);
		person.getStats().setSkillLevel(Skills.MISSILE_SPECIALIZATION, 2);
		person.getStats().setSkillLevel(Skills.FIELD_MODULATION, 2);
		person.getStats().setSkillLevel(Skills.DAMAGE_CONTROL, 2);
		person.getStats().setSkillLevel(Skills.GUNNERY_IMPLANTS, 2);
		person.getStats().setSkipRefresh(false);

		return person;
	}

	@Override
	public int getHandlingPriority(Object params) {
		if (!(params instanceof SDMParams)) return 0;
		SDMParams p = (SDMParams) params;
		
		if (p.entity != null && p.entity.getMemoryWithoutUpdate().contains(
				HMIThemeGenerator.HMI_MYSTERYBLACKHOLEDEFENDER_KEY)) {
			return 2;
		}
		return -1;
	}
	public float getQuality(SDMParams p, float quality, Random random, boolean withOverride) {
		return quality;
	}
}



