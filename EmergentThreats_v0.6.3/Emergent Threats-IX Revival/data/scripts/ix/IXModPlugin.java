package data.scripts.ix;

import java.util.List;
import lunalib.lunaRefit.LunaRefitManager;
import lunalib.lunaSettings.LunaSettings;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CharacterDataAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.SectorGeneratorPlugin;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Tags;

import data.scripts.ix.IXCampaignPlugin;
import data.scripts.ix.IXSystemCreation;
import data.scripts.ix.listeners.IXEncounterListener;
import data.scripts.ix.listeners.PruneHaulerMarketListener;
import data.scripts.ix.luna.BiochipSotFButton;
import data.scripts.ix.luna.PanopticCommandRefitButton;
import data.scripts.ix.luna.PanopticStrategicRefitButton;
import data.scripts.ix.luna.PanopticTacticalRefitButton;
import data.scripts.ix.luna.SalvagePanopticonCoreButton;

//must be imported last, not sure why
import data.scripts.ix.listeners.IXReputationListener; 
import data.scripts.ix.NameListUtil;

public class IXModPlugin extends BaseModPlugin implements SectorGeneratorPlugin {

	private static String IX_SKILL_ID = "ix_sword_of_the_fleet";
	
	@Override
    public void onNewGame() {
        initializeFaction(Global.getSector());
    }
	
	@Override
	public void beforeGameSave() {
		//doing it here otherwise personnel faction won't update
		applyMarzannaChanges(Global.getSector());
		
		//prune excess IX freighters from Vertex Station on startup
		if (Global.getSector().getEconomy().getMarket("ix_vertex_market") == null) return;
		MarketAPI vertexMarket = Global.getSector().getEconomy().getMarket("ix_vertex_market");
		PruneHaulerMarketListener.pruneMarket(vertexMarket);
		
		//hack to see if the player is starting in IX Battlegroup faction from nex
		if (Global.getSector().getMemoryWithoutUpdate().is("$give_IX_hullmods", false)) return;
		
		List<FleetMemberAPI> fleetList = Global.getSector().getPlayerFleet().getMembersWithFightersCopy();
		boolean isIX = true;
		boolean isHonorGuard = false;
		FactionAPI ix =  Global.getSector().getFaction("ix_battlegroup");
		if (ix.getRelationship("player") < 0) isIX = false;
		for (FleetMemberAPI member : fleetList) {
			if (!isIX) continue;
			if (!member.getVariant().isFighter() 
					&& (!member.getShipName().startsWith("DSS") 
					&& !member.getShipName().startsWith("HGS"))) isIX = false;
			String id = member.getVariant().getHullVariantId();
			if (id.equals("hyperion_ix_special") 
					|| id.equals("tigershark_ix_custom")
					|| id.equals("odyssey_ix_custom")) isHonorGuard = true;
			if (id.equals("hyperion_ix_special")) {
				Global.getSector().getMemoryWithoutUpdate().set("$hyperion_ix_start", true);
			}
		}
		if (isIX) {
			CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
			CharacterDataAPI player = Global.getSector().getCharacterData();
			player.getHullMods().add("ix_ground_invasion_conversion");
			player.getHullMods().add("ix_laser_collimator");
			player.getHullMods().add("ix_reactive_combat_shields");
			player.getHullMods().add("ix_terminus_relay");
			if (Global.getSettings().getModManager().isModEnabled("EmergentThreats_Vice")) {
				player.getHullMods().add("vice_interdiction_array");
				player.getHullMods().add("vice_adaptive_entropy_arrester");
				player.getHullMods().add("vice_adaptive_flux_dissipator");
			}
			if (isHonorGuard) {
				player.getHullMods().add("ix_antecedent");
				cargo.addSpecial(new SpecialItemData("ix_core_bp_package", ""), 1);
			}
			cargo.addSpecial(new SpecialItemData("ix_bp_package", ""), 1);
			cargo.addCommodity("ix_panopticon_core", 1);
			cargo.addSpecial(new SpecialItemData("ix_biochip_sotf", ""), 1);
			MutableCharacterStatsAPI stats = Global.getSector().getPlayerPerson().getStats();
			if (!stats.hasSkill(IX_SKILL_ID)) stats.setSkillLevel(IX_SKILL_ID, 2f);
		}
		Global.getSector().getMemoryWithoutUpdate().set("$give_IX_hullmods", false);
	}
	
	private void applyMarzannaChanges(SectorAPI sector) {
		//run once
		if (Global.getSector().getMemoryWithoutUpdate().is("$apply_ix_marzanna_changes", false)) return;
		FactionAPI ix_battlegroup = Global.getSector().getFaction("ix_battlegroup");
		FactionAPI ix_marzanna = Global.getSector().getFaction("ix_marzanna");
		ix_marzanna.setRelationship("player", ix_battlegroup.getRelationship("player"));
		if (sector.getEconomy().getMarket("ix_marzanna_market") != null) {
			MarketAPI market = sector.getEconomy().getMarket("ix_marzanna_market");
			if (market.getPlanetEntity().getFaction() == ix_marzanna) {
				List<PersonAPI> people = market.getPeopleCopy();
				for (PersonAPI p : people) {
					if (p.getFaction() == ix_battlegroup) p.setFaction(ix_marzanna.getId());
					if (p.getRankId().equals("citizen")) p.setRankId("groundCaptain");
				}
			}
		}
		if (sector.getStarSystem("Zorya") != null 
				&& sector.getStarSystem("Zorya").getEntitiesWithTag(Tags.STATION) != null) {
			List <SectorEntityToken> stations = sector.getStarSystem("Zorya").getEntitiesWithTag(Tags.STATION);
			for (SectorEntityToken s : stations) {
				if (s.getName().equals(NameListUtil.Marzanna_Station) && s.getFaction().getId().equals("ix_battlegroup")) {
					s.setName(NameListUtil.Overseer_Station);
					s.setFaction("ix_marzanna");
					s.setInteractionImage("illustrations", "ix_marzanna_illus");
					s.setCustomDescriptionId("ix_zorya_overseer");
				}
			}
		}
		Global.getSector().getMemoryWithoutUpdate().set("$apply_ix_marzanna_changes", false);
	}
	
	private void initializeFaction(SectorAPI sector) {
		generate(sector);
		Global.getSector().getMemoryWithoutUpdate().set("$give_IX_hullmods", true);
        Global.getSector().getFaction("ix_battlegroup").setShowInIntelTab(true);
        FactionAPI ix = sector.getFaction("ix_battlegroup");
		FactionAPI hvb = sector.getFaction("ix_remnant_hvb");
		FactionAPI marzanna = sector.getFaction("ix_marzanna");
        List<FactionAPI> factionList = sector.getAllFactions();
        factionList.remove(ix);
		factionList.remove(hvb);
        for (FactionAPI faction : factionList) {
            ix.setRelationship(faction.getId(), -0.50f);
			hvb.setRelationship(faction.getId(), -1f);
        }
        ix.setRelationship("player", -0.50f);
		ix.setRelationship("ix_core", 1f);
		ix.setRelationship("ix_marzanna", 1f);
		ix.setRelationship("rat_exotech", 0f);
		ix.setRelationship(Factions.INDEPENDENT, 0f);
		ix.setRelationship(Factions.PIRATES, 0f);
		ix.setRelationship("ix_remnant_hvb", -1f);
		if (Global.getSettings().getModManager().isModEnabled("lunalib")) {
			if (LunaSettings.getBoolean("EmergentThreats_IX_Revival", "ix_brighton_neutral")) {
				ix.setRelationship("brighton", 0f);
			}
		}
		factionList.remove(marzanna);
		for (FactionAPI faction : factionList) {
			marzanna.setRelationship(faction.getId(), ix.getRelationship(faction.getId()));
		}
		marzanna.setRelationship(Factions.PIRATES, 0f);
		marzanna.setRelationship(Factions.INDEPENDENT, 0f);
	}
	
	@Override
	public void onGameLoad(boolean newGame) {
		SectorAPI sector = Global.getSector();
		FactionAPI pirates = Global.getSector().getFaction(Factions.PIRATES);
		FactionAPI ix_battlegroup = Global.getSector().getFaction("ix_battlegroup");
		FactionAPI ix_honor_guard = Global.getSector().getFaction("ix_core");
		FactionAPI ix_marzanna = Global.getSector().getFaction("ix_marzanna");
		ix_battlegroup.getKnownFighters().remove("talon_wing");
		ix_honor_guard.getKnownFighters().remove("talon_wing");
		ix_marzanna.getKnownFighters().remove("talon_wing");
		sector.registerPlugin(new IXCampaignPlugin());
		sector.getListenerManager().addListener(new IXEncounterListener());
		sector.getListenerManager().addListener(new IXReputationListener());
		sector.getListenerManager().addListener(new PruneHaulerMarketListener());
	}
	
	@Override
	public void generate(SectorAPI sector) {
		IXSystemCreation.generate(sector);
	}
	
	@Override
	public void onApplicationLoad() {
		LunaRefitManager.addRefitButton(new BiochipSotFButton());
		LunaRefitManager.addRefitButton(new PanopticCommandRefitButton());
		LunaRefitManager.addRefitButton(new PanopticStrategicRefitButton());
		LunaRefitManager.addRefitButton(new PanopticTacticalRefitButton());
		LunaRefitManager.addRefitButton(new SalvagePanopticonCoreButton());
	}
}