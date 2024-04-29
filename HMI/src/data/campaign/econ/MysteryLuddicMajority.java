package data.campaign.econ;

import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin;
import com.fs.starfarer.api.impl.campaign.econ.ConditionData;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Industries;

import java.util.Arrays;

public class MysteryLuddicMajority extends BaseMarketConditionPlugin {

	private static String [] luddicFactions = new String [] {
			"ashen_keepers",
			"knights_of_ludd",
			"luddic_church",
			"luddic_path",
			"knights_of_eva",

	};
	public void apply(String id) {
		if (Arrays.asList(luddicFactions).contains(market.getFactionId())) {
			market.getStability().modifyFlat(id, ConditionData.STABILITY_LUDDIC_MAJORITY_BONUS, "Luddic majority");
		} else {
			market.getStability().modifyFlat(id, ConditionData.STABILITY_LUDDIC_MAJORITY_PENALTY, "Luddic majority");
		}
		Industry industry = market.getIndustry(Industries.POPULATION);
		if(industry!=null){
			industry.getDemand(Commodities.DRUGS).getQuantity().modifyMult(id + "_0", 0);
		}

		industry = market.getIndustry(Industries.MINING);
		if(industry!=null){
			industry.getDemand(Commodities.DRUGS).getQuantity().modifyMult(id + "_0", 0);
		}
	}

	public void unapply(String id) {
		market.getStability().unmodify(id);
	}

}
