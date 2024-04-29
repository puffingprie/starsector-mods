package data.campaign.econ;

import com.fs.starfarer.api.impl.campaign.econ.BaseHazardCondition;
import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;


public class MessSwarm extends BaseHazardCondition {

	public static final float ACCESS_BONUS = -100f;
	public static final float STAB_BONUS = -10f;
	public static final float HAZARD_MALUS = 10f;

	@Override
	public void apply(String id) {
		super.apply(id);
		market.getAccessibilityMod().modifyFlat(id, ACCESS_BONUS/100f, "Mess Infestation");
		market.getStability().modifyFlat (id, STAB_BONUS, "Mess Infestation");
		market.getHazard().modifyFlat (id, HAZARD_MALUS, "Mess Infestation");
	}
	@Override
	public void unapply(String id) {
		super.unapply(id);
		market.getStability().unmodify(id);
		market.getAccessibilityMod().unmodifyFlat(id);
		market.getHazard().unmodify(id);
	}
}