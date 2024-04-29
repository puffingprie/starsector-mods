package data.campaign.intel.missions.kurita;

import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.missions.cb.*;

public class KuritaCustomBounty extends BaseCustomBounty {

	public static List<CustomBountyCreator> CREATORS = new ArrayList<CustomBountyCreator>();
	static {
		CREATORS.add(new CBTrader());
		CREATORS.add(new CBPatrol());
		CREATORS.add(new CBMerc());
		CREATORS.add(new CBPather());
		CREATORS.add(new CBPirate());
		CREATORS.add(new CBRemnant());
		CREATORS.add(new CBRemnantPlus());
		CREATORS.add(new CBDerelict());
		CREATORS.add(new CBDeserter());
		CREATORS.add(new CBEnemyStation());
	}
	
	@Override
	public List<CustomBountyCreator> getCreators() {
		return CREATORS;
	}

	@Override
	protected boolean create(MarketAPI createdAt, boolean barEvent) {
		return super.create(createdAt, barEvent);
	}

	@Override
	protected void updateInteractionDataImpl() {
		super.updateInteractionDataImpl();
		
		String id = getMissionId();
		if (showData != null && showCreator != null) {
			if (showData.fleet != null) {
				PersonAPI p = showData.fleet.getCommander();
				set("$" + id + "_targetRank", p.getRank());
				//set("$bcb_targetRank", p.getRank());
			}
		}
	}
}











