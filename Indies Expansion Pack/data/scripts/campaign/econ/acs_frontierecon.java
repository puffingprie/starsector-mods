package data.scripts.campaign.econ;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketImmigrationModifier;
import com.fs.starfarer.api.campaign.econ.MutableCommodityQuantity;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.econ.BaseHazardCondition;
import com.fs.starfarer.api.impl.campaign.population.PopulationComposition;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
// import data.campaign.ids.bbplus_Industries;
/**
 *
 */
public class acs_frontierecon extends BaseHazardCondition {
    //public static float HAZARD_PENALTY = 0;
    //private final int MARINES_BONUS = 2;      // They are militarized and always ready to fight till the bitter end
    // public static final float IMMIGRATION_PENALTY = 35F;
    //public static float DEFENSE_BONUS = 5000;
    // public static final float STAB_BONUS = 10f; // Since they are just a small of rag-tag escaped Blade Breakers...
						// They are tight-knit and wary of outsiders resulting to more stabilized colony
						// And they don't want immigration, so fuck off
    @Override
    public void apply(final String id) {
        super.apply(id);

        if (this.market.getFaction() != null) {
            for (Industry industry : market.getIndustries()) {
                for (MutableCommodityQuantity mutableCommodityQuantity : industry.getAllDemand()) {
                    // MutableStat quantity = industry.getDemand(mutableCommodityQuantity.getCommodityId()).getQuantity();
                    industry.getDemand(mutableCommodityQuantity.getCommodityId()).getQuantity().modifyMult("acs_no_demand",-100,"No Demand Industry");
                }

            }
        } else {
            for (Industry industry : market.getIndustries()) {
                for (MutableCommodityQuantity mutableCommodityQuantity : industry.getAllDemand()) {
                    industry.getDemand(mutableCommodityQuantity.getCommodityId()).getQuantity().unmodify();
                }
    
            }
        }


    //     if (this.market.getFaction() != null) {
    //         if (this.market.getFaction().getId().contains("the_deserter_ex")) {
    //         this.market.getStability().modifyFlat(id, STAB_BONUS, "Closed Door Policy");

    //         Industry industry = market.getIndustry(Industries.POPULATION);
    //         if(industry!=null){
    //             industry.getDemand(Commodities.DRUGS).getQuantity().modifyFlat(id, -5);
    //             industry.getDemand(Commodities.ORGANS).getQuantity().modifyFlat(id, -5);
    //             industry.getDemand(Commodities.ORGANICS).getQuantity().modifyFlat(id, -5);
    //             industry.getDemand(Commodities.DOMESTIC_GOODS).getQuantity().modifyFlat(id, -5);
    //             industry.getDemand(Commodities.LUXURY_GOODS).getQuantity().modifyFlat(id, -5);
    //             industry.getDemand(Commodities.FOOD).getQuantity().modifyFlat(id, -1);
    //             industry.getDemand(Commodities.SUPPLIES).getQuantity().modifyFlat(id, -3);
	//  //if (industry.isFunctional()) {
    //      //industry.supply(id + "_0", Commodities.MARINES, MARINES_BONUS, "Closed Door Protocols");
    //      //}else {
    //      //industry.getSupply(Commodities.MARINES).getQuantity().unmodifyFlat(id + "_0");
    //      //}
    //         }
		
    //         industry = market.getIndustry(Industries.SPACEPORT);
    //         if(industry!=null){
    //             industry.getDemand(Commodities.FUEL).getQuantity().modifyFlat(id, -2);
    //             industry.getDemand(Commodities.SHIPS).getQuantity().modifyFlat(id, -2);
    //             industry.getSupply(Commodities.CREW).getQuantity().modifyFlat(id, -3);
    //         }
		
    //         industry = market.getIndustry(Industries.WAYSTATION);
    //         if(industry!=null){
    //             industry.getDemand(Commodities.FUEL).getQuantity().modifyFlat(id, -4);
    //             industry.getDemand(Commodities.SUPPLIES).getQuantity().modifyFlat(id, -2);
    //             industry.getDemand(Commodities.CREW).getQuantity().modifyFlat(id, -2);
    //         }

    //         industry = market.getIndustry(Industries.MINING);
    //         if(industry!=null){
    //             industry.getDemand(Commodities.DRUGS).getQuantity().modifyFlat(id, -5);
    //         }
		
    //         industry = market.getIndustry(Industries.STARFORTRESS_HIGH);
    //         if(industry!=null){
    //             industry.getDemand(Commodities.CREW).getQuantity().modifyFlat(id, -4);
    //             industry.getDemand(Commodities.SUPPLIES).getQuantity().modifyFlat(id, -5);
    //         }

    //         industry = market.getIndustry(Industries.HEAVYBATTERIES);
    //         if(industry!=null){
    //             industry.getDemand(Commodities.MARINES).getQuantity().modifyFlat(id, -4);
    //             industry.getDemand(Commodities.SUPPLIES).getQuantity().modifyFlat(id, -3);
    //             industry.getDemand(Commodities.HAND_WEAPONS).getQuantity().modifyFlat(id, -1);
    //         }
	
    //         industry = market.getIndustry(Industries.SPACEPORT);
    //         if(industry!=null){
    //             industry.getDemand(Commodities.FUEL).getQuantity().modifyFlat(id, -2);
    //             industry.getDemand(Commodities.SHIPS).getQuantity().modifyFlat(id, -2);
    //         }
		
    //     }
    //     else {
    //         this.market.getStability().unmodify(id);
    //         market.removeTransientImmigrationModifier(this);
    //     }
    }

    @Override
    public void unapply(final String id) {
        super.unapply(id);

        for (Industry industry : market.getIndustries()) {
            for (MutableCommodityQuantity mutableCommodityQuantity : industry.getAllDemand()) {
                industry.getDemand(mutableCommodityQuantity.getCommodityId()).getQuantity().unmodify();
            }

        }
	    // this.market.getStability().unmodify(id);
        // market.removeTransientImmigrationModifier(this);
    }

    // @Override
    // public void modifyIncoming(final MarketAPI market, final PopulationComposition incoming) {
    //     incoming.add(Factions.POOR, 10f);
    //     incoming.getWeight().modifyFlat(getModId(), getThisImmigrationBonus(), Misc.ucFirst(condition.getName().toLowerCase()));
    // }
	
    // private float getThisImmigrationBonus() {
    //     return -12*market.getSize();
    // }
    
    // @Override
    // protected void createTooltipAfterDescription(final TooltipMakerAPI tooltip, final boolean expanded) {
    //     super.createTooltipAfterDescription(tooltip, expanded);	
    //     tooltip.addPara("%s stability",
    //             10f, Misc.getHighlightColor(),
    //             "+" + (int) STAB_BONUS
	// );
    //     // tooltip.addPara(
    //     //         "%s population growth (based on market size).",
    //     //         10f, 
    //     //         Misc.getHighlightColor(),
    //     //         "" + (int) getThisImmigrationBonus()
    //     // );
    //     tooltip.addPara(
    //             "These effects only applies to %s market(s).",
    //             10f,
    //             Misc.getHighlightColor(),
    //             "Deserters"
    //     );        
    // }

}