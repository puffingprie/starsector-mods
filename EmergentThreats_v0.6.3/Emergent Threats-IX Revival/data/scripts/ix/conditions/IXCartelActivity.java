package data.scripts.ix.conditions;

import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin;

public class IXCartelActivity extends BaseMarketConditionPlugin {

	private static int STABILITY = -2;
	private static String CARTEL_HQ = "ix_marzanna_base";

	public void apply(String id) {
		if (!market.hasIndustry(CARTEL_HQ)) market.getStability().modifyFlat(id, STABILITY, "Organized crime");
	}

	public void unapply(String id) {
		market.getStability().unmodify(id);
	}
}