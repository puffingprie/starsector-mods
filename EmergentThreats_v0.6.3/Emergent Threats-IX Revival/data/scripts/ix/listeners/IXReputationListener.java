package data.scripts.ix.listeners;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCampaignEventListener;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;

import data.scripts.ix.NameListUtil;

public class IXReputationListener extends BaseCampaignEventListener {
	
	public IXReputationListener() {
		super(true);
	}	
	
	@Override
	public void reportPlayerReputationChange(String faction, float delta)  {
		if (faction.equals("ix_battlegroup") && Global.getSector().getFaction("ix_marzanna") != null) {
			String factionId = Global.getSector().getPlayerFaction().getId();
			float ixRep = Global.getSector().getFaction("ix_battlegroup").getRelToPlayer().getRel();
			Global.getSector().getFaction("ix_marzanna").setRelationship(factionId, ixRep);
		}
		else if (faction.equals("ix_marzanna") && Global.getSector().getFaction("ix_battlegroup") != null) {
			String factionId = Global.getSector().getPlayerFaction().getId();
			float marRep = Global.getSector().getFaction("ix_marzanna").getRelToPlayer().getRel();
			Global.getSector().getFaction("ix_battlegroup").setRelationship(factionId, marRep);
		}
		
		//Give ship for turning in pk
		if (faction.equals("ix_battlegroup") && delta > 0.24f) {
			if (Global.getSector().getPlayerMemoryWithoutUpdate().is("$receivedHyperionIX", true)) {
				giveHyperionIX();
				Global.getSector().getPlayerMemoryWithoutUpdate().set("$receivedHyperionIX", false);
			}
			if (Global.getSector().getPlayerMemoryWithoutUpdate().is("$receivedRadiantIX", true)) {
				giveRadiantIX();
				Global.getSector().getPlayerMemoryWithoutUpdate().set("$receivedRadiantIX", false);
			}
		}
	}
	
	private void giveHyperionIX() {
		ShipVariantAPI v = Global.getSettings().getVariant("hyperion_ix_special").clone();
		FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, v);
		member.setShipName(NameListUtil.HGS_Judicator);
		Global.getSector().getPlayerFleet().getFleetData().addFleetMember(member);
	}
	
	private void giveRadiantIX() {
		ShipVariantAPI v = Global.getSettings().getVariant("radiant_ix_custom_2").clone();
		FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, v);
		member.setShipName(NameListUtil.HGS_Judicator);
		Global.getSector().getPlayerFleet().getFleetData().addFleetMember(member);
	}
}