package data.campaign.econ;

import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;


public class HardenedPolity extends BaseMarketConditionPlugin {

	public static final float ACCESS_BONUS = -10f;
	public static final float STAB_BONUS = 6f;
	public static float DEFENSE_BONUS = 500;

	@Override
	public void apply(String id) {
		super.apply(id);
		float mult = market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).getBonusMult();
		market.getAccessibilityMod().modifyFlat(id, ACCESS_BONUS/ 100f, "Hardened Polity");
		market.getStability().modifyFlat (id, STAB_BONUS, "Hardened Polity");
		market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).modifyFlat(id, DEFENSE_BONUS/mult, "Hardened Polity");
	}
	
	@Override
	public void unapply(String id) {
		super.unapply(id);
		market.getStability().unmodify(id);
		market.getAccessibilityMod().unmodifyFlat(id);
		market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).unmodify(id);
	}
	
    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);

//        Object test = Global.getSettings().getSpec(ConditionGenDataSpec.class, condition.getId(), true);
//        if (test instanceof ConditionGenDataSpec) {
//            ConditionGenDataSpec spec = (ConditionGenDataSpec) test;
//            float hazard = spec.getHazard();
//            if (hazard != 0) {
//                String pct = "" + (int)(hazard * 100f) + "%";
//                if (hazard > 0) pct = "+" + pct;
//                tooltip.addPara(
//                        "%s hazard rating.",
//                        10f,
//                        Misc.getHighlightColor(),
//                        pct
//                );
//            }
//        }

        tooltip.addPara(
                "%s defense rating.",
                10f,
                Misc.getHighlightColor(),
                "+" + (int)DEFENSE_BONUS
        );
    }
	
}