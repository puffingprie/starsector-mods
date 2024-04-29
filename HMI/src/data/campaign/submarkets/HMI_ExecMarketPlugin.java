package data.campaign.submarkets;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.FactionAPI.ShipPickMode;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.submarkets.BaseSubmarketPlugin;
import com.fs.starfarer.api.impl.campaign.submarkets.OpenMarketPlugin;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.util.Highlights;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

import java.awt.*;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class HMI_ExecMarketPlugin extends BaseSubmarketPlugin {

    public static final WeightedRandomPicker<String> SCRAPYARD_FACTIONS = new WeightedRandomPicker<>();

    static {
        SCRAPYARD_FACTIONS.add(Factions.HEGEMONY, 2f);
        SCRAPYARD_FACTIONS.add(Factions.DIKTAT, 2f);
        SCRAPYARD_FACTIONS.add(Factions.INDEPENDENT, 4f);
        SCRAPYARD_FACTIONS.add(Factions.LUDDIC_CHURCH, 2f);
        SCRAPYARD_FACTIONS.add(Factions.LUDDIC_PATH, 2f);
        SCRAPYARD_FACTIONS.add(Factions.PIRATES, 5f);
        SCRAPYARD_FACTIONS.add(Factions.TRITACHYON, 2f);
        SCRAPYARD_FACTIONS.add(Factions.PERSEAN, 2f);
        SCRAPYARD_FACTIONS.add("hmi_exec", 6f);
    }

    @Override
    public void init(SubmarketAPI submarket) {
        super.init(submarket);
    }


    public void updateCargoPrePlayerInteraction() {
        float seconds = Global.getSector().getClock().convertToSeconds(sinceLastCargoUpdate);
        addAndRemoveStockpiledResources(seconds, false, true, true);
        sinceLastCargoUpdate = 0f;

        if (okToUpdateShipsAndWeapons()) {
            sinceSWUpdate = 0f;

            pruneWeapons(0f);

            int weapons = 12 + Math.max(0, market.getSize() - 1) + (Misc.isMilitary(market) ? 5 : 0);
            int fighters = 3 + Math.max(0, (market.getSize() - 3) / 2) + (Misc.isMilitary(market) ? 2 : 0);

            WeightedRandomPicker<String> factionPicker = new WeightedRandomPicker<>();
            int index = 0;
            for (String item : SCRAPYARD_FACTIONS.getItems()) {
                FactionAPI f;
                try {
                    f = Global.getSector().getFaction(item);
                } catch (Exception e) {
                    f = null;
                }
                if (f != null) {
                    factionPicker.add(f.getId(), SCRAPYARD_FACTIONS.getWeight(index));
                }
                index++;
            }

            addWeapons(weapons, weapons + 6, 2, factionPicker);
            addFighters(fighters, fighters + 2, 2, factionPicker);


            getCargo().getMothballedShips().clear();
            float pOther = 0.1f;

            for (int i = 0; i < 4; i++) {
                FactionDoctrineAPI doctrineOverride = submarket.getFaction().getDoctrine().clone();
                doctrineOverride.setWarships(4);
                doctrineOverride.setPhaseShips(1);
                doctrineOverride.setCarriers(2);
                doctrineOverride.setCombatFreighterProbability(1f);
                doctrineOverride.setShipSize(4);
                addShips(factionPicker.pick(itemGenRandom),
                        20f, // combat
                        itemGenRandom.nextFloat() > pOther ? 0f : 10f, // freighter
                        itemGenRandom.nextFloat() > pOther ? 0f : 10f, // tanker
                        itemGenRandom.nextFloat() > pOther ? 0f : 10f, // transport
                        itemGenRandom.nextFloat() > pOther ? 0f : 10f, // liner
                        itemGenRandom.nextFloat() > pOther ? 0f : 10f, // utilityPts
                        0f,
                        0f, // qualityMod
                        ShipPickMode.PRIORITY_THEN_ALL,
                        doctrineOverride);
            }

            float freighters = 10f;
            CommodityOnMarketAPI com = market.getCommodityData(Commodities.SHIPS);
            freighters += com.getMaxSupply() * 2f;
            if (freighters > 30) freighters = 30;

            addShips(market.getFactionId(),
                    10f, // combat
                    freighters, // freighter
                    0f, // tanker
                    10f, // transport
                    10f, // liner
                    5f, // utilityPts
                    null, // qualityOverride
                    0f, // qualityMod
                    ShipPickMode.PRIORITY_THEN_ALL,
                    null);

            float tankers = 10f;
            com = market.getCommodityData(Commodities.FUEL);
            tankers += com.getMaxSupply() * 2f;
            if (tankers > 40) tankers = 40;
            //tankers = 40;
            addShips(market.getFactionId(),
                    0f, // combat
                    0f, // freighter
                    tankers, // tanker
                    0, // transport
                    0f, // liner
                    0f, // utilityPts
                    null, // qualityOverride
                    0f, // qualityMod
                    ShipPickMode.PRIORITY_THEN_ALL,
                    null);


            addHullMods(1, 1 + itemGenRandom.nextInt(3));
        }

        getCargo().sort();
    }

    protected Object writeReplace() {
        if (okToUpdateShipsAndWeapons()) {
            pruneWeapons(0f);
            getCargo().getMothballedShips().clear();
        }
        return this;
    }


    public boolean shouldHaveCommodity(CommodityOnMarketAPI com) {
        return !market.isIllegal(com);
    }

    @Override
    public int getStockpileLimit(CommodityOnMarketAPI com) {

        float limit = OpenMarketPlugin.getBaseStockpileLimit(com);

        Random random = new Random(market.getId().hashCode() + submarket.getSpecId().hashCode() + Global.getSector().getClock().getMonth() * 170000);
        limit *= 0.9f + 0.2f * random.nextFloat();

        float sm = market.getStabilityValue() / 10f;
        limit *= (0.25f + 0.75f * sm);

        if (com.getCommodity().getId().equals(Commodities.ORE)
                || com.getCommodity().getId().equals(Commodities.RARE_ORE)
                || com.getCommodity().getId().equals(Commodities.ORGANICS)
                || com.getCommodity().getId().equals(Commodities.VOLATILES)
                || com.getCommodity().getId().equals(Commodities.ORGANS)
                || com.getCommodity().getId().equals(Commodities.DRUGS)
        ) {
            limit *= 2.00f;
        } else {
            limit *= 0.50f;
        }
        if (limit < 0) limit = 0;

        return (int) limit;
    }

    @Override
    public PlayerEconomyImpactMode getPlayerEconomyImpactMode() {
        return PlayerEconomyImpactMode.PLAYER_SELL_ONLY;
    }


    @Override
    public boolean isOpenMarket() {
        return true;
    }


    @Override
    public String getTooltipAppendix(CoreUIAPI ui) {
        if (ui.getTradeMode() == CampaignUIAPI.CoreUITradeMode.SNEAK) {
            return "Requires: proper docking authorization (transponder on)";
        }
        return super.getTooltipAppendix(ui);
    }


    @Override
    public Highlights getTooltipAppendixHighlights(CoreUIAPI ui) {
        if (ui.getTradeMode() == CampaignUIAPI.CoreUITradeMode.SNEAK) {
            String appendix = getTooltipAppendix(ui);
            if (appendix == null) return null;

            Highlights h = new Highlights();
            h.setText(appendix);
            h.setColors(Misc.getNegativeHighlightColor());
            return h;
        }
        return super.getTooltipAppendixHighlights(ui);
    }


}