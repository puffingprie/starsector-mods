package data.campaign.intel.missions.kurita;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PersonImportance;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Voices;
import exerelin.utilities.StringHelper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HMIQuestUtils {
		
	public static final String PERSON_KURITA = "hmi_kurita";
	public static final List<String> TAG_AS_KURITA_MISSION = new ArrayList<>(Arrays.asList(new String[]{
		"proCom",
		"sShip", 
		"cheapCom",
		"mcb",
		"seco", 
		"ssat",
	}));
	
	public static void createKurita(MarketAPI market) {
		PersonAPI person = Global.getFactory().createPerson();
		person.setId(PERSON_KURITA);
		person.setImportance(PersonImportance.MEDIUM);
		person.setVoice(Voices.ARISTO);
		person.setFaction("hmi");
		person.setGender(FullName.Gender.MALE);
		person.setRankId(Ranks.SPECIAL_AGENT);
		person.setPostId(Ranks.POST_SENIOR_EXECUTIVE);
		person.getName().setFirst("Bob");
		person.getName().setLast("Kurita");
		person.setPortraitSprite(Global.getSettings().getSpriteName("characters", "hmi_kurita"));
		person.addTag("hmi_kurita");
		Global.getSector().getImportantPeople().addPerson(person);
		market.addPerson(person);
	}
	
	public static void setupKuritaContactMissions() {
		for (String id : TAG_AS_KURITA_MISSION) {
			Global.getSettings().getMissionSpec(id).getTagsAny().add("kurita");
		}
	}
}
