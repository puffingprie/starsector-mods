package data.scripts.vice;

import java.util.List;
import lunalib.lunaRefit.LunaRefitManager;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;

import data.scripts.vice.hullmods.EnemyEncounterListener;
import data.scripts.vice.luna.SignalMaskerInstallButton;
import data.scripts.vice.luna.SignalMaskerRemoveButton;

public class ETModPlugin extends BaseModPlugin {
	
	@Override
	public void onGameLoad(boolean newGame) {
		SectorAPI sector = Global.getSector();
		sector.getListenerManager().addListener(new EnemyEncounterListener());
		
			sector.getMemoryWithoutUpdate().set("$mission_picker_tri_tachyon", true);
			sector.getMemoryWithoutUpdate().set("$mission_picker_cabal", true);
			sector.getMemoryWithoutUpdate().set("$mission_picker_tri_tachyon_expired", false);
			sector.getMemoryWithoutUpdate().set("$mission_picker_cabal_expired", false);
	}
	
	@Override
	public void beforeGameSave() {
		//if (Global.getSector().getPlayerFleet() == null || Global.getSector().getPlayerFleet().getCargo() == null) return;
		//if (Global.getSector().getPlayerFleet().getMembersWithFightersCopy() == null) return;
		
		//hack to see if the player is starting in Diktat faction from nex
		if (Global.getSector().getMemoryWithoutUpdate().is("$give_diktat_hullmods", false)) return;

		List<FleetMemberAPI> fleetList = Global.getSector().getPlayerFleet().getMembersWithFightersCopy();
		boolean isDiktat = true;
		for (FleetMemberAPI member : fleetList) {
			if (!member.getVariant().isFighter() && !member.getShipName().startsWith("SDS")) isDiktat = false;
		}
		
		if (isDiktat) {
			CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
			cargo.addHullmods("vice_attuned_emitter_diodes", 1);
			cargo.addHullmods("vice_convert_shuttle", 1);	
		}
		Global.getSector().getMemoryWithoutUpdate().set("$give_diktat_hullmods", false);
	}
	
	@Override
    public void onNewGame() {
		SectorAPI sector = Global.getSector();
		setRelationships(sector);
		sector.getMemoryWithoutUpdate().set("$give_diktat_hullmods", true);
		FactionAPI sindrian_diktat = sector.getFaction("sindrian_diktat");
		FactionAPI lions_guard = sector.getFaction("lions_guard");
		sindrian_diktat.getKnownFighters().remove("talon_wing");
		lions_guard.getKnownFighters().remove("talon_wing");
	}
	
	private static void setRelationships(SectorAPI sector) {
		FactionAPI diamond_nexus = sector.getFaction("diamond_nexus");
		FactionAPI vantage_group = sector.getFaction("vantage_group");
		FactionAPI vice_diktat_navy = sector.getFaction("vice_diktat_navy");
		FactionAPI vice_lions_guard = sector.getFaction("vice_lions_guard"); //LG 1st Division
		
		List<FactionAPI> factions = sector.getAllFactions();
		
		for (FactionAPI faction : factions) {
            if (faction.isNeutralFaction()) continue;
            diamond_nexus.setRelationship(faction.getId(), RepLevel.VENGEFUL);
			vantage_group.setRelationship(faction.getId(), RepLevel.HOSTILE);
			vice_diktat_navy.setRelationship(faction.getId(), RepLevel.HOSTILE);
			vice_lions_guard.setRelationship(faction.getId(), RepLevel.HOSTILE);
        }
		diamond_nexus.setRelationship("remnant", RepLevel.NEUTRAL);
		diamond_nexus.setRelationship("diamond_nexus", 1f);
		vantage_group.setRelationship("vantage_group", 1f);
		vice_diktat_navy.setRelationship("vice_diktat_navy", 1f);
		vice_lions_guard.setRelationship("vice_lions_guard", 1f);
		vice_lions_guard.setRelationship("vantage_group", RepLevel.NEUTRAL); //prevents both bounties killing each other
	}
	
	@Override
	public void onApplicationLoad() {
		LunaRefitManager.addRefitButton(new SignalMaskerInstallButton());
		LunaRefitManager.addRefitButton(new SignalMaskerRemoveButton());
	}
}