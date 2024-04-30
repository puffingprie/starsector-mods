package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.OfficerDataAPI;
import com.fs.starfarer.api.util.Misc;

import java.util.List;
import java.util.Map;

public class FM_OfficerCheck extends BaseCommandPlugin{
    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        if (dialog == null) return false;

        List<OfficerDataAPI> officerList = Global.getSector().getPlayerFleet().getFleetData().getOfficersCopy();
        for (OfficerDataAPI officerData : officerList){
            if (officerData.getPerson().hasTag(params.get(0).getStringWithTokenReplacement(ruleId, dialog, memoryMap)))return true;
        }
        return false;
    }
}
