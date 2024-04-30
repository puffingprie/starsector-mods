package data.scripts.ix.listeners;

import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.campaign.BaseCampaignEventListener;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;

//reduces Banteng (IX) and Buffalo (IX) to no more than 4 per market
//also adds Panopticon Interface checker to IX warships so players won't randomly spawn interfaces if they buy IX hulls then split off the ships into their own fleets or put them into task groups
public class PruneHaulerMarketListener extends BaseCampaignEventListener {
	
	private static String BANTENG_ID = "banteng_ix";
	private static String BUFFALO_ID = "buffalo_ix";
	private static int MAX_SHIP_COUNT = 4;
	
	private static String IX_MOD_ID = "ix_ninth";
	private static String CHECKER_ID = "ix_panoptic_checker";	
	
	public PruneHaulerMarketListener() {
		super(true);
	}	
	
	public static void pruneMarket(MarketAPI market) {
		List<SubmarketAPI> submarkets = market.getSubmarketsCopy();
		for (SubmarketAPI s : submarkets) {
			int shipCountBant = 0;
			int shipCountBuff = 0;
			if (s.getSpecId().equals(Submarkets.SUBMARKET_STORAGE)) return;
			List<FleetMemberAPI> shipsToDelete = new ArrayList<FleetMemberAPI>();
			List<FleetMemberAPI> ships = s.getCargo().getMothballedShips().getMembersListCopy();
			for (FleetMemberAPI ship : ships) {
				//add panoptic interface checker
				if (ship.getVariant().hasHullMod(IX_MOD_ID)) ship.getVariant().addPermaMod(CHECKER_ID);
				//prune extra haulers
				if (ship.getHullSpec().getHullId().equals(BANTENG_ID)) {
					if (shipCountBant < MAX_SHIP_COUNT) shipCountBant++;
					else shipsToDelete.add(ship);
				}
				else if (ship.getHullSpec().getHullId().equals(BUFFALO_ID)) {
					if (shipCountBuff < MAX_SHIP_COUNT) shipCountBuff++;
					else shipsToDelete.add(ship);
				}
			}
			if (!shipsToDelete.isEmpty()) {
				for (FleetMemberAPI ship : shipsToDelete) {
					s.getCargo().getMothballedShips().removeFleetMember(ship);
				}
			}
			s.getPlugin().updateCargoPrePlayerInteraction();
		}
	}

	@Override
	public void reportPlayerOpenedMarket(MarketAPI market) {
		pruneMarket(market);
	}
}