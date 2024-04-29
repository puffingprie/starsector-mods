//////////////////////
//Initially created by Histidine and modified from Nexelerin
//////////////////////
package scripts.kissa.LOST_SECTOR.campaign.rulecmd;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class nskr_IsBaseOfficial extends BaseCommandPlugin {
	
	public static final Set<String> COMMAND_POSTS = new HashSet<>();
	public static final Set<String> MILITARY_POSTS = new HashSet<>();
	public static final Set<String> TRADER_POSTS = new HashSet<>();
	public static final Set<String> ADMIN_POSTS = new HashSet<>();
	public static final Set<String> OP_POSTS = new HashSet<>();
	public static final Set<String> RESEARCH_POSTS = new HashSet<>();
	public static final Set<String> INTELLIGENCE_POSTS = new HashSet<>();

	static {
		COMMAND_POSTS.add(Ranks.POST_BASE_COMMANDER);
		COMMAND_POSTS.add(Ranks.POST_STATION_COMMANDER);
		COMMAND_POSTS.add(Ranks.POST_OUTPOST_COMMANDER);
		COMMAND_POSTS.add(Ranks.POST_PORTMASTER);
		
		MILITARY_POSTS.add(Ranks.POST_BASE_COMMANDER);
		MILITARY_POSTS.add(Ranks.POST_STATION_COMMANDER);
		MILITARY_POSTS.add(Ranks.POST_OUTPOST_COMMANDER);
		MILITARY_POSTS.add(Ranks.POST_ADMINISTRATOR);
		
		ADMIN_POSTS.add(Ranks.POST_BASE_COMMANDER);
		ADMIN_POSTS.add(Ranks.POST_STATION_COMMANDER);
		ADMIN_POSTS.add(Ranks.POST_OUTPOST_COMMANDER);
		ADMIN_POSTS.add(Ranks.POST_PORTMASTER);
		ADMIN_POSTS.add(Ranks.POST_ADMINISTRATOR);
		
		TRADER_POSTS.add(Ranks.POST_STATION_COMMANDER);
		TRADER_POSTS.add(Ranks.POST_OUTPOST_COMMANDER);
		TRADER_POSTS.add(Ranks.POST_PORTMASTER);
		TRADER_POSTS.add(Ranks.POST_SUPPLY_MANAGER);
		TRADER_POSTS.add(Ranks.POST_SUPPLY_OFFICER);

		OP_POSTS.add("kSpaceOperations");

		RESEARCH_POSTS.add("kResearch");

		INTELLIGENCE_POSTS.add("kIntelligence");
	}
	
	// note: administrator appears in the absence of all of the following: 
	//	base commander (military base)
	//	station commander (orbital station)
	//  outpost commander (outpost)
	//	portmaster (spaceport)
	//	supply officer (base, station, outpost, spaceport)
	
	@Override
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) 
	{
		String post;
		try {	// stupid-ass workaround for unexplained NPE when interacting with Remnant stations while non-hostile
			post = memoryMap.get(MemKeys.LOCAL).getString("$postId");
		} catch (NullPointerException ex) {
			return false;
		}
		
		if (post == null) return false;
		
		String arg = params.get(0).getString(memoryMap);
		switch (arg.toLowerCase(Locale.ROOT))
		{
			case "command":
				return COMMAND_POSTS.contains(post);
			case "military":
				return MILITARY_POSTS.contains(post);
			case "admin":
				return ADMIN_POSTS.contains(post);
			case "ttadmin":
				//for alt ending, include Arroyo
				if (dialog.getInteractionTarget().getActivePerson()==null) return ADMIN_POSTS.contains(post);
				return ADMIN_POSTS.contains(post) || dialog.getInteractionTarget().getActivePerson().getId().equals("arroyo");
			case "trade":
			case "op":
				return OP_POSTS.contains(post);
			case "research":
				return RESEARCH_POSTS.contains(post);
			case "intelligence":
				return INTELLIGENCE_POSTS.contains(post);
			case "trader":
				return TRADER_POSTS.contains(post);
			case "any":
			default:
				return COMMAND_POSTS.contains(post) || MILITARY_POSTS.contains(post) 
						|| TRADER_POSTS.contains(post) || ADMIN_POSTS.contains(post);
		}
	}
}
