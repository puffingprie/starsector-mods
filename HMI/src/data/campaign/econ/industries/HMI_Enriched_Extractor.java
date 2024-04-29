package data.campaign.econ.industries;

import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;

public class HMI_Enriched_Extractor extends BaseIndustry {

    @Override
    public void apply() {

        super.apply(true);
        int size = market.getSize();

        demand(Commodities.HEAVY_MACHINERY, size + 2);
        demand(Commodities.SUPPLIES, size + 2);
        demand(Commodities.CREW, size );

        supply(Commodities.VOLATILES, size);
        supply(Commodities.FUEL, size + 1);

    }

    @Override
    public void unapply() {
        super.unapply();
    }

    @Override
    public boolean isAvailableToBuild() {
        return false;
    }

    @Override
    public boolean showWhenUnavailable() {
        return false;
    }
}
