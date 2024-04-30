package data.campaign.econ;

import com.fs.starfarer.api.impl.campaign.econ.BaseHazardCondition;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.utils.I18nUtil;

public class FM_PhantomFactory extends BaseHazardCondition {

    public static final float HAZARD_BUFF = -0.25f;

    @Override
    public void apply(String id) {
        market.getHazard().modifyFlat(id, HAZARD_BUFF, I18nUtil.getString("condition", "FM_PhantomFactory"));
    }

    @Override
    public void unapply(String id) {
        market.getHazard().unmodifyFlat(id);
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);

        String hazardinfo = "" + (int) (HAZARD_BUFF * 100f) + "%";

        tooltip.addPara(I18nUtil.getString("condition", "FM_HazardRate"), 10f, Misc.getTextColor(), Misc.getHighlightColor(), hazardinfo);

    }

}
