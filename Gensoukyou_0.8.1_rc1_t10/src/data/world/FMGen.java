package data.world;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.SectorGeneratorPlugin;
import com.fs.starfarer.api.campaign.econ.EconomyAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import data.world.systems.FM_Gensokyo;

import java.util.ArrayList;

public class FMGen implements SectorGeneratorPlugin {


    //Shorthand function for adding a market, just copy it
    public static MarketAPI addMarketplace(String factionID, SectorEntityToken primaryEntity, ArrayList<SectorEntityToken> connectedEntities, String name,
                                           int size, ArrayList<String> marketConditions, ArrayList<String> submarkets, ArrayList<String> industries, float tarrif,
                                           boolean freePort, boolean withJunkAndChatter) {
        EconomyAPI globalEconomy = Global.getSector().getEconomy();
        String planetID = primaryEntity.getId();
        String marketID = planetID + "_market";

        MarketAPI newMarket = Global.getFactory().createMarket(marketID, name, size);
        newMarket.setFactionId(factionID);
        newMarket.setPrimaryEntity(primaryEntity);
        newMarket.getTariff().modifyFlat("generator", tarrif);

        //Adds submarkets
        if (null != submarkets) {
            for (String market : submarkets) {
                newMarket.addSubmarket(market);
            }
        }

        //Adds market conditions
        for (String condition : marketConditions) {
            newMarket.addCondition(condition);
        }

        //Add market industries
        for (String industry : industries) {
            newMarket.addIndustry(industry);
        }

        //Sets us to a free port, if we should
        newMarket.setFreePort(freePort);

        //Adds our connected entities, if any
        if (null != connectedEntities) {
            for (SectorEntityToken entity : connectedEntities) {
                newMarket.getConnectedEntities().add(entity);
            }
        }

        globalEconomy.addMarket(newMarket, withJunkAndChatter);
        primaryEntity.setMarket(newMarket);
        primaryEntity.setFaction(factionID);

        if (null != connectedEntities) {
            for (SectorEntityToken entity : connectedEntities) {
                entity.setMarket(newMarket);
                entity.setFaction(factionID);
            }
        }

        //Finally, return the newly-generated market
        return newMarket;
    }

    @Override
    public void generate(SectorAPI sector) {
        FactionAPI fantasy = sector.getFaction("fantasy_manufacturing");
        //Generate your system
        new FM_Gensokyo().generate(sector);
        //Add faction to bounty system
        SharedData.getData().getPersonBountyEventData().addParticipatingFaction("fantasy_manufacturing");
        //Add illegal for something does not exist...
        if (Global.getSettings().getModManager().isModEnabled("capturecrew")) {
            fantasy.makeCommodityIllegal("capturedcrew");
            fantasy.makeCommodityIllegal("capturedslaves");
        }
        //set relationship

        fantasy.setRelationship(Factions.LUDDIC_PATH, -0.75f);
        fantasy.setRelationship(Factions.LUDDIC_CHURCH, -0.6f);
        fantasy.setRelationship(Factions.TRITACHYON, 0.05f);
        fantasy.setRelationship(Factions.PERSEAN, 0.05f);
        fantasy.setRelationship(Factions.PIRATES, -0.75f);
        fantasy.setRelationship(Factions.REMNANTS, -0.6f);

        if (Global.getSettings().getModManager().isModEnabled("BlueSeaFisher")) {
            fantasy.setRelationship("BlueSeaFisher", +0.05f);
        }
        if (Global.getSettings().getModManager().isModEnabled("LLI")) {
            fantasy.setRelationship("LLI", -0.05f);
        }
        if (Global.getSettings().getModManager().isModEnabled("Prasrity_Scrap_Master")) {
            fantasy.setRelationship("Prasrity_Scrap_Master", +0.15f);
        }


    }


}

