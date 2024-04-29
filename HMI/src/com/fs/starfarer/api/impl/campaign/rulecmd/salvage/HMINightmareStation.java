package com.fs.starfarer.api.impl.campaign.rulecmd.salvage;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.procgen.SalvageEntityGenDataSpec.DropData;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.BaseSalvageSpecial;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.BaseSalvageSpecial.ExtraSalvage;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 
 */
public class HMINightmareStation extends BaseCommandPlugin {

	protected CampaignFleetAPI playerFleet;
	protected SectorEntityToken entity;
	protected FactionAPI playerFaction;
	protected FactionAPI entityFaction;
	protected TextPanelAPI text;
	protected OptionPanelAPI options;
	protected CargoAPI playerCargo;
	protected MemoryAPI memory;
	protected MarketAPI market;
	protected InteractionDialogAPI dialog;
	protected Map<String, MemoryAPI> memoryMap;
	protected FactionAPI faction;

	public HMINightmareStation() {
	}
	
	public HMINightmareStation(SectorEntityToken entity) {
		init(entity);
	}
	
	protected void init(SectorEntityToken entity) {
		memory = entity.getMemoryWithoutUpdate();
		this.entity = entity;
		playerFleet = Global.getSector().getPlayerFleet();
		playerCargo = playerFleet.getCargo();
		
		playerFaction = Global.getSector().getPlayerFaction();
		entityFaction = entity.getFaction();
		faction = entity.getFaction();
		market = entity.getMarket();
	}

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		this.dialog = dialog;
		this.memoryMap = memoryMap;
		
		String command = params.get(0).getString(memoryMap);
		if (command == null) return false;
		
		entity = dialog.getInteractionTarget();
		init(entity);
		
		memory = getEntityMemory(memoryMap);
		
		text = dialog.getTextPanel();
		options = dialog.getOptionPanel();
		
		if (command.equals("genNightmareLoot")) {
			genNightmareLoot();
		}
		
		return true;
	}


	protected void genNightmareLoot() {

		MemoryAPI memory = entity.getMemoryWithoutUpdate();
		long seed = memory.getLong(MemFlags.SALVAGE_SEED);
		Random random = Misc.getRandom(seed, 100);

		DropData d = new DropData();
		d.chances = 5;
		d.group = "blueprints";
		entity.addDropRandom(d);
		
		d = new DropData();
		d.chances = 1;
		d.group = "rare_tech";
		entity.addDropRandom(d);

		d = new DropData();
		d.chances = 800;
		d.group = "basic";
		entity.addDropRandom(d);

		d = new DropData();
		d.chances = 2;
		d.group = "any_hullmod_high";
		entity.addDropRandom(d);

		d = new DropData();
		d.chances = 2;
		d.group = "blueprints_low";
		entity.addDropRandom(d);

		d = new DropData();
		d.chances = 2;
		d.group = "blueprints_guaranteed";
		entity.addDropRandom(d);

		CargoAPI salvage = SalvageEntity.generateSalvage(random, 1f, 1f, 1f, 1f, entity.getDropValue(), entity.getDropRandom());

		ExtraSalvage extra = BaseSalvageSpecial.getExtraSalvage(memoryMap);
		if (extra != null) {
			salvage.addAll(extra.cargo);
			BaseSalvageSpecial.clearExtraSalvage(memoryMap);
		}
		//salvage.addSpecial(new SpecialItemData("industry_bp", "HMI_mindcontrol"), 1);
		salvage.addSpecial(new SpecialItemData("modspec", "hmi_subliminal"), 1);
		salvage.addCommodity(Commodities.GAMMA_CORE, 20);
		salvage.sort();
	}

}






















