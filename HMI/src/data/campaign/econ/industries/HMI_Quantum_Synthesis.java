package data.campaign.econ.industries;

import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import data.campaign.econ.HMI_items;

public class HMI_Quantum_Synthesis extends BaseIndustry {

	public void apply() {
		super.apply(true);

		int size = market.getSize();

		demand(Commodities.SUPPLIES, size );
		demand(Commodities.CREW, size );
		demand(Commodities.METALS, size - 1);
		demand(Commodities.RARE_METALS, size - 3);
		demand(Commodities.HEAVY_MACHINERY, size );

		supply(HMI_items.HMICRYSTAL, size );
		supply(Commodities.ORE, size + 2);
		supply(Commodities.RARE_ORE, size );

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

