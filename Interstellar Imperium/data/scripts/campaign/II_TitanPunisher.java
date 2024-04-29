package data.scripts.campaign;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.ColonyPlayerHostileActListener;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD.TempData;
import com.fs.starfarer.api.util.Misc;
import data.scripts.hullmods.II_TitanBombardment;
import data.scripts.util.II_Util;

public class II_TitanPunisher implements ColonyPlayerHostileActListener {

    @Override
    public void reportRaidForValuablesFinishedBeforeCargoShown(InteractionDialogAPI dialog, MarketAPI market, TempData actionData, CargoAPI cargo) {
    }

    @Override
    public void reportRaidToDisruptFinished(InteractionDialogAPI dialog, MarketAPI market, TempData actionData, Industry industry) {
    }

    @Override
    public void reportTacticalBombardmentFinished(InteractionDialogAPI dialog, MarketAPI market, TempData actionData) {
        handleBombardment(dialog, market, "Tactical bombardment");
    }

    @Override
    public void reportSaturationBombardmentFinished(InteractionDialogAPI dialog, MarketAPI market, TempData actionData) {
        handleBombardment(dialog, market, "Saturation bombardment");
    }

    private static void handleBombardment(InteractionDialogAPI dialog, MarketAPI market, String desc) {
        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
        if (fleet == null) {
            return;
        }

        int baseBombardCost = MarketCMD.getBombardmentCost(market, null);

        for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
            if (member.isMothballed()) {
                continue;
            }

            if (II_Util.getNonDHullId(member.getHullSpec()).contentEquals("ii_olympus")) {
                if (member.getRepairTracker().getCR() < II_TitanBombardment.getCRPenalty(member.getVariant())) {
                    continue;
                }

                if ((dialog != null) && (dialog.getTextPanel() != null)) {
                    String penaltyStr = "" + (int) Math.round(-II_TitanBombardment.getCRPenalty(member.getVariant()) * 100f) + "%";
                    dialog.getTextPanel().addPara(member.getShipName() + ": " + penaltyStr + "% CR", Misc.getNegativeHighlightColor(), penaltyStr);
                }
                member.getRepairTracker().applyCREvent(-II_TitanBombardment.getCRPenalty(member.getVariant()), desc + " of " + market.getName());
                fleet.getFleetData().setSyncNeeded();
                fleet.getFleetData().syncIfNeeded();

                baseBombardCost -= II_TitanBombardment.BOMBARD_BONUS;
                if (baseBombardCost <= 0) {
                    break;
                }
            }
        }
    }
}
