package data.scripts.ix.conditions;

import com.fs.starfarer.api.impl.campaign.econ.BaseHazardCondition;
import com.fs.starfarer.api.impl.campaign.intel.deciv.DecivTracker;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class IXMonitored extends BaseHazardCondition {
	
    private static int STABILITY_BONUS = 5;
	private static String IX_FACTION = "ix_battlegroup";
	private static String MONITORED = "ix_monitored";
	
    @Override
    public void apply(String id) {
		if (!market.getFactionId().equals(IX_FACTION)) {
			market.getStability().unmodify("Panopticon monitoring");
			market.removeCondition(MONITORED);
		} 
		else market.getStability().modifyFlat(id, STABILITY_BONUS, "Panopticon monitoring");
	}
    
    @Override
    public void unapply(String id) {
		market.getStability().unmodify(id);
	}
    
    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        float opad = 10f;
				
		tooltip.addPara("%s stability",  
				opad, Misc.getHighlightColor(), 
				"+" + STABILITY_BONUS);
    }
}
