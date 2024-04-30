package data.campaign.missions;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.missions.hub.BaseHubMission;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

public class FM_BaseHubMission extends BaseHubMission {

    public boolean create(MarketAPI createdAt, boolean barEvent) {
        return barEvent;
    }

    public void addDescriptionForCurrentStage(TooltipMakerAPI info, float width, float height) {
        float opad = 10f;
        String text = getStageDescriptionText();
        if (text != null) {
            info.addPara(text, opad);
        } else {
            String noun = getMissionTypeNoun();
            String verb = getMissionCompletionVerb();
            if (isSucceeded()) {
                info.addPara("You have successfully " + verb + " this " + noun + ".", opad);
            } else if (isFailed()) {
                info.addPara("You have failed this " + noun + ".", opad);
            } else if (isAbandoned()) {
                info.addPara("You have abandoned this " + noun + ".", opad);
            } else {
                addDescriptionForNonEndStage(info, width, height);
            }
        }
    }


}
