package data.scripts.campaign.econ;

import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class II_HostileTerrain extends BaseMarketConditionPlugin {

    public static final float HOSTILE_TERRAIN_ACCESSIBILITY_PENALTY = 20f;

    @Override
    public void apply(String id) {
        super.apply(id);
        market.getAccessibilityMod().modifyFlat(id, -HOSTILE_TERRAIN_ACCESSIBILITY_PENALTY / 100f, "Hostile terrain");
    }

    @Override
    public void unapply(String id) {
        super.unapply(id);
        market.getAccessibilityMod().unmodifyFlat(id);
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);

        tooltip.addPara("%s accessibility",
                10f, Misc.getHighlightColor(),
                "-" + (int) HOSTILE_TERRAIN_ACCESSIBILITY_PENALTY + "%");
    }
}
