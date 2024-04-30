package data.hullmods.vice;

import java.util.LinkedHashSet;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;

import data.scripts.vice.hullmods.RemnantSubsystemsUtil;

public class AbominationInterface extends BaseHullMod {
	
	private static String ADAPTIVE_SUBSYSTEMS = "adaptive subsystems";
	private static String THIS_MOD = "vice_abomination_interface";
	private static String EXEMPTION_MOD = "vice_self_evolving_hull";
	private static String INTERFACE_PENALTY_MOD = "vice_abomination_interface_penalty";
	private static String SHIPWIDE_INTEGRATION_CHECKER = "vice_shipwide_integration_checker";
	
	//Utility variables
	private RemnantSubsystemsUtil util = new RemnantSubsystemsUtil();
	
	//dummy hullmod, exists for other hullmods to check for compatibility through RemnantSubsystemsUtil
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		ShipVariantAPI variant = stats.getVariant();
		util.applyShipwideIntegration(variant);
		if (!variant.getModuleSlots().isEmpty()) variant.addPermaMod(SHIPWIDE_INTEGRATION_CHECKER);
		if (isSMod(stats)) variant.addMod(INTERFACE_PENALTY_MOD);
		else variant.getHullMods().remove(INTERFACE_PENALTY_MOD);
	}

	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		ShipVariantAPI variant = ship.getVariant();
		if (variant.hasHullMod(id)
				&& getInterfaceCount(ship.getMutableStats()) > 1
				&& !variant.getHullSpec().isBuiltInMod(id)) {
			variant.getHullMods().remove(id);
		}
	}
	
	//gets the number of non-built in abomination interfaces in the fleet
	private int getInterfaceCount(MutableShipStatsAPI stats) {
		int interfaceCount = 0;
		if (Global.getSector() == null || Global.getSector().getPlayerFleet() == null) return interfaceCount;
		CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
		List<FleetMemberAPI> fleetList = fleet.getMembersWithFightersCopy();

		List<MarketAPI> marketList = Global.getSector().getEconomy().getMarketsCopy();
		for (MarketAPI market : marketList) {
			if (market.getSubmarket(Submarkets.SUBMARKET_STORAGE) != null) {
				CargoAPI storage = market.getSubmarket(Submarkets.SUBMARKET_STORAGE).getCargo();
				List<FleetMemberAPI> storageList = storage.getMothballedShips().getMembersListCopy();
				if (!storageList.isEmpty()) {
					for (FleetMemberAPI ship : storageList) fleetList.add(ship);
				}
			}
		}

		for (FleetMemberAPI member : fleetList) {
			if (member.getVariant().hasHullMod(THIS_MOD) && !member.getVariant().hasHullMod(EXEMPTION_MOD)) interfaceCount++;
			LinkedHashSet<String> sMods = member.getVariant().getSMods();
			if (sMods.size() == 0) continue;
			for (String mod : sMods) {
				if (mod.equals(THIS_MOD)) interfaceCount--;
			}
		}
		return interfaceCount;
	}
	
	@Override
    public boolean isApplicableToShip(ShipAPI ship) {
		if (getInterfaceCount(ship.getMutableStats()) >= 1 && !ship.getVariant().hasHullMod(THIS_MOD)) return false;
		if (util.isModuleCheck(ship)) return false;
		return (util.isAbomination(ship));
	}
	
	public String getUnapplicableReason(ShipAPI ship) {
		if (getInterfaceCount(ship.getMutableStats()) >= 1) return "Interface is installed elsewhere in the fleet";
		if (util.isModuleCheck(ship)) return "Interfacing is performed on the central hub";
		if (!util.isAbomination(ship)) return "This ship is unsuitable for the Abomination Interface. Upgrade with AI Subsystem Integration instead";
		return null;
	}
	
	public String getSModDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "Clone";
		if (index == 1) return "does not refund its OP cost";
		return null;
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + ADAPTIVE_SUBSYSTEMS;
		return null;
	}
}