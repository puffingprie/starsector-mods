package data.scripts.ix.submarkets;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.CoreUIAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.FactionDoctrineAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.SubmarketPlugin;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.submarkets.MilitarySubmarketPlugin;
import com.fs.starfarer.api.util.Highlights;
import com.fs.starfarer.api.util.Misc;

public class IXHonorGuardMarket extends MilitarySubmarketPlugin {
	
	private static RepLevel MIN_REPUTATION = RepLevel.FRIENDLY;
	private static RepLevel MAX_REPUTATION = RepLevel.COOPERATIVE;
	
	@Override
	public void init(SubmarketAPI submarket) {
		super.init(submarket);
	}
	
	@Override
	public float getTariff() {
		return 0.3f;
	}
	
	@Override
	public String getName() {
		return "Honor Guard" + "\n" + "Armory";
	}
	
	@Override
	public boolean shouldHaveCommodity(CommodityOnMarketAPI com) {
		return false;
	}
	
	@Override
	public boolean isIllegalOnSubmarket(String commodityId, TransferAction action) {
		if (action == TransferAction.PLAYER_SELL) return true;
		else return false;
	}
	
	@Override
	public String getIllegalTransferText(String commodityId, TransferAction action) {
		return "The Honor Guard does not make purchases from outsiders";
	}
	
	@Override
	public Highlights getIllegalTransferTextHighlights(String commodityId, TransferAction action) {
		Highlights h = new Highlights();
		h.append("The Honor Guard does not make purchases from outsiders", Misc.getNegativeHighlightColor());
		return h;
	}
	
	@Override
	public boolean isIllegalOnSubmarket(CargoStackAPI stack, TransferAction action) {
		if (action == TransferAction.PLAYER_SELL) return true;
		else return false;
	}
	
	@Override
	public String getIllegalTransferText(CargoStackAPI stack, TransferAction action) {
		return "The Honor Guard does not make purchases from outsiders";
	}
	
	@Override
	public Highlights getIllegalTransferTextHighlights(CargoStackAPI stack, TransferAction action) {
		Highlights h = new Highlights();
		h.append("The Honor Guard does not make purchases from outsiders", Misc.getNegativeHighlightColor());
		return h;
	}
	
	@Override
	public boolean isIllegalOnSubmarket(FleetMemberAPI member, TransferAction action) {
		if (action == TransferAction.PLAYER_SELL) return true;
		
		RepLevel req = getRequiredLevelAssumingLegal(member, action);
		if (req == null) return false;
		
		RepLevel level = submarket.getFaction().getRelationshipLevel(Global.getSector().getFaction("player"));
		
		boolean legal = level.isAtWorst(req);
		if (requiresCommission(req)) {
			legal &= hasCommission();
		}
		
		return !legal;
	}
	
	@Override
	public String getIllegalTransferText(FleetMemberAPI member, TransferAction action) {
		if (action == TransferAction.PLAYER_SELL) {
			return "No sales";
		}
		else {
			RepLevel req = getRequiredLevelAssumingLegal(member, action);
			String str = "";
			RepLevel level = submarket.getFaction().getRelationshipLevel(Global.getSector().getFaction("player"));
			if (!level.isAtWorst(req)) {
				str += "Req: " + submarket.getFaction().getDisplayName() + " - " + req.getDisplayName().toLowerCase();				
			}
			if (requiresCommission(req) && !hasCommission()) {
				if (!str.isEmpty()) str += "\n";
				str += "Req: " + submarket.getFaction().getDisplayName() + " - " + "commission";
			}
			return str;
		}
	}
	
	@Override
	public String getTooltipAppendix(CoreUIAPI ui) {
		RepLevel level = this.submarket.getFaction().getRelationshipLevel(Global.getSector().getFaction("player"));
			if (!level.isAtWorst(MIN_REPUTATION)) {
				return "Requires: " + this.submarket.getFaction().getDisplayName() + " - " + MIN_REPUTATION.getDisplayName().toLowerCase(); 
			}
		if (!Global.getSector().getPlayerFleet().isTransponderOn())
			return "Materiel transfers from the Honor Guard must be submitted on the record"; 
		return super.getTooltipAppendix(ui);
	}
	
	@Override
	public boolean isEnabled(CoreUIAPI ui) {
		if (!Global.getSector().getPlayerFleet().isTransponderOn()) return false; 
		RepLevel level = this.submarket.getFaction().getRelationshipLevel(Global.getSector().getFaction("player"));
		return level.isAtWorst(MIN_REPUTATION);
	}
	
	@Override
	private RepLevel getRequiredLevelAssumingLegal(FleetMemberAPI member, TransferAction action) {
		if (action == TransferAction.PLAYER_BUY) {
			HullSize size = member.getHullSpec().getHullSize();
			if (size == HullSize.CAPITAL_SHIP) return MAX_REPUTATION;
			else return MIN_REPUTATION;
		}
		return null;
	}
	
	@Override
	public void updateCargoPrePlayerInteraction() {
		float seconds = Global.getSector().getClock().convertToSeconds(sinceLastCargoUpdate);
		addAndRemoveStockpiledResources(seconds, false, true, true);
		sinceLastCargoUpdate = 0f;
		
		if (okToUpdateShipsAndWeapons()) {
			sinceSWUpdate = 0f;
			pruneWeapons(0f);
			
			int weapons = 7 + Math.max(0, market.getSize() - 1) * 2;
			addWeapons(weapons, weapons + 2, 3, submarket.getFaction().getId());
			addFighters(4, 4, 3, submarket.getFaction().getId());
			addHullMods(4, 2 + itemGenRandom.nextInt(4), submarket.getFaction().getId());
			
			float stability = market.getStabilityValue();
			float sMult = Math.max(0.1f, stability / 10f);
			getCargo().getMothballedShips().clear();

			FactionAPI honorGuard = Global.getSector().getFaction("ix_core");
			FactionDoctrineAPI doctrineOverride = honorGuard.getDoctrine().clone();
			doctrineOverride.setShipSize(5);
			
			addShips("ix_core", 50.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 2.0f, 0.0f, 
					FactionAPI.ShipPickMode.PRIORITY_THEN_ALL, doctrineOverride);
			
			//25% chance per inventory update cycle to have a core for sale if none are present
			if (getCargo().getCommodityQuantity("ix_panopticon_core") < 1) {
				if (Math.random() <= 0.25f) getCargo().addCommodity("ix_panopticon_core", 1);
			}
		}
		getCargo().sort();
	}
}