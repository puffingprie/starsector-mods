package data.scripts.campaign.iirules;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc.Token;
import data.scripts.util.II_Util;
import java.util.List;
import java.util.Map;

public class II_TitanStrikeAvailable extends BaseCommandPlugin {

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
        if (playerFleet == null) {
            return false;
        }

        for (FleetMemberAPI member : playerFleet.getFleetData().getMembersListCopy()) {
            if (II_Util.getNonDHullId(member.getHullSpec()).contentEquals("ii_olympus")) {
                return true;
            }
        }

        return false;
    }
}
