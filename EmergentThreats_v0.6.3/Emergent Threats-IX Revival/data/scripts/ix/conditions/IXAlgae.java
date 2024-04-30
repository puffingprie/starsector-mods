package data.scripts.ix.conditions;

import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.impl.campaign.econ.BaseHazardCondition;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class IXAlgae extends BaseHazardCondition{

    private static int DRUG_BONUS = 2;
	private static int FOOD_PENALTY = -3;
    
    public void apply(String id) {
		super.apply(id);
        Industry industry = market.getIndustry(Industries.LIGHTINDUSTRY);
        if(industry != null) {
			if (industry.isFunctional()) industry.supply(id + "_0", Commodities.DRUGS, DRUG_BONUS, "Toxic algae");
			else industry.getSupply(Commodities.DRUGS).getQuantity().unmodifyFlat(id + "_0");
		}
		
		Industry industry2 = market.getIndustry(Industries.AQUACULTURE);
		if(industry2 != null) {
			if (industry2.isFunctional()) industry2.supply(id + "_0", Commodities.FOOD, FOOD_PENALTY, "Toxic algae");
			else industry2.getSupply(Commodities.FOOD).getQuantity().unmodifyFlat(id + "_0");
		}
    }
    
    @Override
    public void unapply(String id) {
		super.unapply(id);
		Industry industry = market.getIndustry(Industries.LIGHTINDUSTRY);
        if(industry != null) industry.getSupply(Commodities.DRUGS).getQuantity().unmodifyFlat(id + "_0");
		
		Industry industry2 = market.getIndustry(Industries.AQUACULTURE);
		if(industry2 != null) industry2.getSupply(Commodities.FOOD).getQuantity().unmodifyFlat(id + "_0");
	}
    
    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);
		float opad = 10f;
		
		tooltip.addPara("Increases drug production by %s",  
				opad, Misc.getHighlightColor(), 
				"" + DRUG_BONUS);
				
		tooltip.addPara("Decreases food production by %s (Aquaculture)",
				opad, Misc.getHighlightColor(), 
				"" + FOOD_PENALTY);
    }
}
