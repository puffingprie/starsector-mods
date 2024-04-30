package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.loading.VariantSource;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class UW_RestoreShip extends BaseCommandPlugin {

    private static final String numberNames[] = {"zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten"};

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
        if (!Global.getSector().getMemoryWithoutUpdate().contains("$uwTIMMember")) {
            return false;
        }

        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
        if (playerFleet == null) {
            return false;
        }

        String timId = Global.getSector().getMemoryWithoutUpdate().getString("$uwTIMMember");
        FleetMemberAPI targetMember = null;
        for (FleetMemberAPI member : playerFleet.getFleetData().getMembersListCopy()) {
            if (member.getId().contentEquals(timId)) {
                targetMember = member;
                break;
            }
        }
        if (targetMember == null) {
            return false;
        }

        int numDMods = 0;
        for (String modId : targetMember.getVariant().getHullMods()) {
            HullModSpecAPI modSpec = Global.getSettings().getHullModSpec(modId);
            if ((modSpec != null) && modSpec.hasTag(Tags.HULLMOD_DMOD)) {
                numDMods++;
            }
        }
        long startingDMods = 7;
        if (Global.getSector().getMemoryWithoutUpdate().contains("$uwStartingDMods")) {
            startingDMods = Global.getSector().getMemoryWithoutUpdate().getLong("$uwStartingDMods");
        }
        if (numDMods == 0) {
            return false;
        }

        Random rand;
        if (!Global.getSector().getMemoryWithoutUpdate().contains("$uwRestoreSeed")) {
            rand = new Random();
        } else {
            rand = Misc.getRandom(Global.getSector().getMemoryWithoutUpdate().getLong("$uwRestoreSeed"), numDMods);
        }

        float needed = targetMember.getHullSpec().getBaseHull().getBaseValue() * Global.getSettings().getFloat("baseRestoreCostMult");
        for (int i = 0; i < numDMods; i++) {
            needed *= Global.getSettings().getFloat("baseRestoreCostMultPerDMod");
        }
        needed /= numDMods;
        needed *= 7f / (float) startingDMods;
        if (needed > 0) {
            needed = Math.max(1, Math.round(needed));
        }

        if (targetMember.getVariant().isStockVariant()) {
            ShipVariantAPI v = targetMember.getVariant().clone();
            v.setSource(VariantSource.REFIT);
            v.setHullVariantId(Misc.genUID());
            targetMember.setVariant(v, false, false);
        }
        int randDMod = rand.nextInt(numDMods);
        int idx = 0;
        for (String modId : targetMember.getVariant().getHullMods()) {
            HullModSpecAPI modSpec = Global.getSettings().getHullModSpec(modId);
            if ((modSpec != null) && modSpec.hasTag(Tags.HULLMOD_DMOD)) {
                if (idx == randDMod) {
                    targetMember.getVariant().removePermaMod(modId);
                    memoryMap.get(MemKeys.GLOBAL).set("$uwRestoreRemovedDMod", modSpec.getDisplayName(), 0);
                    break;
                }
                idx++;
            }
        }

        if (numDMods > 1) {
            memoryMap.get(MemKeys.GLOBAL).set("$uwRestoreAfterStr", "partially restored", 0);
        } else {
            memoryMap.get(MemKeys.GLOBAL).set("$uwRestoreAfterStr", "fully restored", 0);
        }
        if (numDMods > 11) {
            memoryMap.get(MemKeys.GLOBAL).set("$uwRestoreNumDModsStrHighlight", numDMods - 1, 0);
            memoryMap.get(MemKeys.GLOBAL).set("$uwRestoreNumDModsStr", (numDMods - 1) + " d-mods remain.", 0);
        } else if (numDMods > 2) {
            memoryMap.get(MemKeys.GLOBAL).set("$uwRestoreNumDModsStrHighlight", Misc.ucFirst(numberNames[(numDMods - 1)]), 0);
            memoryMap.get(MemKeys.GLOBAL).set("$uwRestoreNumDModsStr", Misc.ucFirst(numberNames[(numDMods - 1)]) + " d-mods remain.", 0);
        } else if (numDMods == 2) {
            memoryMap.get(MemKeys.GLOBAL).set("$uwRestoreNumDModsStrHighlight", Misc.ucFirst(numberNames[(numDMods - 1)]), 0);
            memoryMap.get(MemKeys.GLOBAL).set("$uwRestoreNumDModsStr", Misc.ucFirst(numberNames[(numDMods - 1)]) + " d-mod remains.", 0);
        } else {
            memoryMap.get(MemKeys.GLOBAL).set("$uwRestoreNumDModsStrHighlight", "", 0);
            memoryMap.get(MemKeys.GLOBAL).set("$uwRestoreNumDModsStr", "", 0);
        }
        if (needed > 0) {
            playerFleet.getCargo().getCredits().subtract(needed);
            MemoryAPI memory = Global.getSector().getCharacterData().getMemory();
            memory.set("$credits", (int) Global.getSector().getPlayerFleet().getCargo().getCredits().get(), 0);
            memory.set("$creditsStr", Misc.getWithDGS(Global.getSector().getPlayerFleet().getCargo().getCredits().get()), 0);
        }

        return true;
    }
}
