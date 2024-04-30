package data.scripts.ix;

import java.awt.Color;
import java.util.Random;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.AICoreOfficerPlugin;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.FullName.Gender;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.BaseAICoreOfficerPluginImpl;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

public class PanopticonCorePlugin extends BaseAICoreOfficerPluginImpl implements AICoreOfficerPlugin {
	
	private static String IX_SKILL_ID = "ix_sword_of_the_fleet";
	private static String PANOPTICON_CORE_ID = "ix_panopticon_core";
	private static String COMMAND_CORE_ID = "ix_command_core";
	private static float P_CORE_MULT = 3.5f;
	
	public PersonAPI createPerson(String aiCoreId, String factionId, Random random) {
		if (random == null) random = new Random();
		PersonAPI person = Global.getFactory().createPerson();
		person.setFaction(factionId);
		person.setAICoreId(aiCoreId);
		CommoditySpecAPI spec = Global.getSettings().getCommoditySpec(aiCoreId);
		boolean isPanopticonCore = PANOPTICON_CORE_ID.equals(aiCoreId);
		boolean isCommandCore = COMMAND_CORE_ID.equals(aiCoreId);
		person.getStats().setSkipRefresh(true);
		float mult = 1f;
		if (isPanopticonCore) {
			person.setName(new FullName(spec.getName(), "", Gender.ANY));
			person.setPortraitSprite("graphics/portraits/ix_panopticon_core.png");
			person.getStats().setLevel(6);
			person.getStats().setSkillLevel(Skills.FIELD_MODULATION, 2);
			person.getStats().setSkillLevel(Skills.SYSTEMS_EXPERTISE, 2);
			person.getStats().setSkillLevel(Skills.MISSILE_SPECIALIZATION, 2);
			person.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 2);
			person.getStats().setSkillLevel(Skills.GUNNERY_IMPLANTS, 2);
			person.getStats().setSkillLevel(IX_SKILL_ID, 2);
			person.setRankId(Ranks.SPACE_CAPTAIN);
			if (!Global.getSettings().getModManager().isModEnabled("TrulyAutomatedShips")) mult = P_CORE_MULT;
		}
		else if (isCommandCore) {
			person.setName(new FullName("Panopticon Core (Command)", "", Gender.ANY));
			person.setPortraitSprite("graphics/portraits/ix_panopticon_core.png");
			person.getStats().setLevel(4);
			person.getStats().setSkillLevel(Skills.FIELD_MODULATION, 1);
			person.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 1);
			person.getStats().setSkillLevel(Skills.GUNNERY_IMPLANTS, 1);
			person.getStats().setSkillLevel(IX_SKILL_ID, 1);
			person.setRankId(Ranks.SPACE_LIEUTENANT);
			mult = 0f;
		}
		person.getMemoryWithoutUpdate().set("$autoPointsMult", mult);
		person.setPersonality(Personalities.AGGRESSIVE);
		person.setPostId(null);
		person.getStats().setSkipRefresh(false);
        return person;
	}
	
	@Override
	public void createPersonalitySection(PersonAPI person, TooltipMakerAPI tooltip) {
		float opad = 10f; 
		Color text = person.getFaction().getBaseUIColor();
		Color bg = person.getFaction().getDarkUIColor();
		CommoditySpecAPI spec = Global.getSettings().getCommoditySpec(person.getAICoreId());
		tooltip.addSectionHeading("Personality: aggressive", text, bg, Alignment.MID, 20f);
		tooltip.addPara("In combat, the " + spec.getName() + " maintains an aggressive posture, " +
				"without exhibiting the reckless abandon typical of other AI cores.", opad);
	}
}