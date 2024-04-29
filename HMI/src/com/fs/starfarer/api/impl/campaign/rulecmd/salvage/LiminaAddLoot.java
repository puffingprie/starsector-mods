// adds a ship to the player's fleet, with appropriate text
package com.fs.starfarer.api.impl.campaign.rulecmd.salvage;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.DModManager;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.lazylib.MathUtils;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class LiminaAddLoot extends BaseCommandPlugin
{
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

    public LiminaAddLoot() {
    }

    public LiminaAddLoot(SectorEntityToken entity) {
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

    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        this.dialog = dialog;
        this.memoryMap = memoryMap;

        String command = params.get(0).getString(memoryMap);
        if (command == null) return false;

        entity = dialog.getInteractionTarget();
        init(entity);

        memory = getEntityMemory(memoryMap);

        text = dialog.getTextPanel();
        options = dialog.getOptionPanel();

        if (command.equals("LiminaAddLoot")) {
            genLiminaAddLoot();
        }

        return true;
    }

    protected void genLiminaAddLoot() {
        CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
            cargo.addCommodity(Commodities.HEAVY_MACHINERY, MathUtils.getRandomNumberInRange(50f, 100f));
            cargo.addCommodity(Commodities.SUPPLIES, MathUtils.getRandomNumberInRange(125f, 350f));
            cargo.addCommodity(Commodities.GAMMA_CORE, MathUtils.getRandomNumberInRange(5f, 10f));
            cargo.addCommodity(Commodities.BETA_CORE, MathUtils.getRandomNumberInRange(3f, 6f));
            cargo.addCommodity(Commodities.ALPHA_CORE, MathUtils.getRandomNumberInRange(1f, 2f));
            cargo.addFuel(MathUtils.getRandomNumberInRange(125f, 350f));
            cargo.addCommodity("mess_nano", MathUtils.getRandomNumberInRange(300f, 600f));

    }
}
