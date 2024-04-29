package data.campaign.econ;

import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin;


public class DistantPolity extends BaseMarketConditionPlugin {

	public static final float ACCESS_BONUS = -10f;
	public static final float STAB_BONUS = 5f;

	@Override
	public void apply(String id) {
		super.apply(id);
		market.getAccessibilityMod().modifyFlat(id, ACCESS_BONUS/ 100f, "Distant Polity");
		market.getStability().modifyFlat (id, STAB_BONUS, "Distant Polity");
	}
	@Override
	public void unapply(String id) {
		super.unapply(id);
		market.getStability().unmodify(id);
		market.getAccessibilityMod().unmodifyFlat(id);
	}

}