package data.scripts.campaign.iirules;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;
import java.util.List;
import java.util.Map;

public class II_JustitiaTokens extends BaseCommandPlugin {

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
        memoryMap.get(MemKeys.LOCAL).set("$iiGrandchild", "grandchild", 0);

        PersonAPI player = Global.getSector().getPlayerPerson();
        if (player == null) {
            return false;
        }

        PersonAPI playerPerson = Global.getSector().getPlayerPerson();
        String honorific = Global.getSector().getCharacterData().getHonorific();
        if (playerPerson != null) {
            if (playerPerson.isMale()) {
                memoryMap.get(MemKeys.LOCAL).set("$iiGrandchild", "grandson", 0);
            } else {
                memoryMap.get(MemKeys.LOCAL).set("$iiGrandchild", "granddaughter", 0);
            }

            if ((honorific != null) && !honorific.isEmpty()) {
                if (Misc.CAPTAIN.toLowerCase().equals(honorific.toLowerCase())) {
                    memoryMap.get(MemKeys.LOCAL).set("$iiGrandchild", "grandchild", 0);
                }
            }
        }

        return true;
    }
}
