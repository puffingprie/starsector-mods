package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.econ.MonthlyReport;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import data.scripts.util.UW_Util;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

/**
 * CabalPickExtortionMethod
 */
public class CabalPickExtortionMethod extends BaseCommandPlugin {

    public static Logger log = Global.getLogger(CabalPickExtortionMethod.class);

    public static double extortionAmount(float credits) {
        double tithe = 0.0;
        double prevBracket = 0.0;
        double bracket = 200000.0;
        double incrementalDiv = 2.0;
        double tithePerBracket = 100000.0; // Each bracket increases the tithe by 100000 credits
        int limit = 1000;
        do {
            tithe += Math.min(bracket, credits - prevBracket) / incrementalDiv;
            prevBracket = bracket;
            incrementalDiv += 1.0;
            bracket += incrementalDiv * tithePerBracket;
            limit--;
        } while (credits > bracket && limit > 0);
        return tithe;
    }

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params,
            Map<String, MemoryAPI> memoryMap) {
        if (dialog == null) {
            return false;
        }

        CampaignFleetAPI fleet;
        if (dialog.getInteractionTarget() instanceof CampaignFleetAPI) {
            fleet = (CampaignFleetAPI) dialog.getInteractionTarget();
        } else {
            return false;
        }

        WeightedRandomPicker<String> extortionMethods = new WeightedRandomPicker<>();
        float credits = Global.getSector().getPlayerFleet().getCargo().getCredits().get();
        float shieldedFraction = Misc.getShieldedCargoFraction(Global.getSector().getPlayerFleet());
        float totalCargoValue = CabalCargoCalc.totalCargoValue(Global.getSector().getPlayerFleet().getCargo(), shieldedFraction);
        float totalWeaponsValue = CabalWeaponCalc.totalWeaponsValue(Global.getSector().getPlayerFleet(), false, shieldedFraction);
        float totalFightersValue = CabalFighterCalc.totalFightersValue(Global.getSector().getPlayerFleet(), false, shieldedFraction);
        float totalShipsValue = CabalShipCalc.totalShipsValue(Global.getSector().getPlayerFleet());

        MonthlyReport report = SharedData.getData().getCurrentReport();
        report.computeTotals();
        float profit = Math.max(0f, report.getRoot().totalIncome - report.getRoot().totalUpkeep);

        float netWorth = CabalPickContributionMethod.playerNetWorth();
        float targetExtortion = (float) CabalPickExtortionMethod.extortionAmount(netWorth);

        float powerLevel = UW_Util.calculatePowerLevel(fleet);

        log.info("Seen credit value of " + credits);
        log.info("Calculated cargo value of " + totalCargoValue);
        log.info("Calculated loose weapons value of " + totalWeaponsValue);
        log.info("Calculated loose fighters value of " + totalFightersValue);
        log.info("Calculated ships value of " + totalShipsValue);
        log.info("Seen monthly profit of " + profit);
        log.info("Evaluated net worth at " + netWorth);
        log.info("Targeting extortion value at " + targetExtortion);
        log.info("Evaluated Cabal fleet power level of " + powerLevel);

        // de-emphasize extortion on poor players
        if ((targetExtortion < 25000f) && (Math.random() > 0.5)) {
            memoryMap.get(MemKeys.LOCAL).set("$Cabal_extortionMethod", "none", 0);
            return false;
        }
        if ((targetExtortion < 50000f) && (Math.random() > 0.5)) {
            memoryMap.get(MemKeys.LOCAL).set("$Cabal_extortionMethod", "none", 0);
            return false;
        }
        if ((targetExtortion < 100000f) && (Math.random() > 0.5)) {
            memoryMap.get(MemKeys.LOCAL).set("$Cabal_extortionMethod", "none", 0);
            return false;
        }

        float lowerThreshold = Math.min(200000f, powerLevel * 1500f);
        float upperThreshold = powerLevel * 3500f;
        if (credits > lowerThreshold) {
            float weight = (float) Math.sqrt(Math.min(credits, upperThreshold) / targetExtortion);
            extortionMethods.add("tithe", weight);
            log.info("Tithe extortion method at weight " + weight);
        }

        lowerThreshold = Math.min(25000f, powerLevel * 1500f);
        upperThreshold = powerLevel * 2500f;
        float cargoValue = CabalCargoCalc.valueOfBiggestStack(Global.getSector().getPlayerFleet().getCargo(), shieldedFraction);
        log.info("Calculated biggest cargo stack value of " + cargoValue);
        if (cargoValue > lowerThreshold) {
            float weight = (float) Math.sqrt(Math.min(cargoValue, upperThreshold) / targetExtortion) * 1.5f;
            extortionMethods.add("cargo", weight);
            log.info("Cargo extortion method at weight " + weight);
        }

        lowerThreshold = Math.min(50f, powerLevel * 3.5f);
        float weaponFanciness = CabalWeaponCalc.bestWeaponFanciness(Global.getSector().getPlayerFleet(), shieldedFraction);
        log.info("Calculated best weapon fanciness of " + weaponFanciness);
        if (weaponFanciness > lowerThreshold) {
            float weight = (float) Math.sqrt(weaponFanciness / (targetExtortion / 500f)) * 1.5f;
            extortionMethods.add("weapon", weight);
            log.info("Weapon extortion method at weight " + weight);
        }

        lowerThreshold = Math.min(40f, powerLevel * 2.5f);
        float fighterFanciness = CabalFighterCalc.bestFighterFanciness(Global.getSector().getPlayerFleet(), shieldedFraction);
        log.info("Calculated best fighter fanciness of " + weaponFanciness);
        if (fighterFanciness > lowerThreshold) {
            float weight = (float) Math.sqrt(fighterFanciness / (targetExtortion / 500f)) * 1.5f;
            extortionMethods.add("fighter", weight);
            log.info("Fighter extortion method at weight " + weight);
        }

        lowerThreshold = Math.min(25000f, powerLevel * 350f);
        upperThreshold = powerLevel * 1000f;
        float shipValue = CabalShipCalc.bestShipValue(Global.getSector().getPlayerFleet(), upperThreshold);
        log.info("Calculated best ship value of " + shipValue);
        if (shipValue > lowerThreshold && CabalShipCalc.usableShips(Global.getSector().getPlayerFleet()) > 1) {
            float weight = (float) Math.sqrt(Math.min(shipValue, upperThreshold) / targetExtortion) * 0.25f;
            extortionMethods.add("ship", weight);
            log.info("Ship extortion method at weight " + weight);
        }

        if (extortionMethods.isEmpty()) {
            memoryMap.get(MemKeys.LOCAL).set("$Cabal_extortionMethod", "none", 0);
            return false;
        }

        memoryMap.get(MemKeys.LOCAL).set("$Cabal_extortionMethod", extortionMethods.pick(), 7);
        memoryMap.get(MemKeys.LOCAL).set("$Cabal_netWorthString", Misc.getDGSCredits(
                UW_Util.roundToSignificantFiguresLong(netWorth, 4)), 0);
        return true;
    }
}
