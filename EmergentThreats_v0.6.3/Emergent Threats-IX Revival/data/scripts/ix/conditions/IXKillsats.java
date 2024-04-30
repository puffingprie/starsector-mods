package data.scripts.ix.conditions;

import com.fs.starfarer.api.impl.campaign.econ.BaseHazardCondition;
import com.fs.starfarer.api.impl.campaign.intel.deciv.DecivTracker;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class IXKillsats extends BaseHazardCondition {
	
    private static int POPULATION_MAXIMUM = 3;
    //private static int STABILITY_PENALTY = -4;
	
    @Override
    public void apply(String id) {
		/*
		if (market.getSize() <= POPULATION_MAXIMUM) market.getStability().modifyFlat(id, STABILITY_PENALTY, "Automated killsats");
		else DecivTracker.decivilize(market, true, true);
		*/
		if (market.getSize() > POPULATION_MAXIMUM) DecivTracker.decivilize(market, true, true);
	}
    
    @Override
    public void unapply(String id) {
		market.getStability().unmodify(id);
	}
    
    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        float opad = 10f;
		/*
		tooltip.addPara("%s stability.",  
				opad, Misc.getHighlightColor(), 
				"" + STABILITY_PENALTY);
		*/
		tooltip.addPara("Colony will be destroyed if population ever exceeds size %s",  
				opad, Misc.getHighlightColor(), 
				"" + POPULATION_MAXIMUM);
    }
}
