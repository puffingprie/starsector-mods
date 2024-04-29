package data.campaign.econ.industries;

import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;

public class anvil_lab extends BaseIndustry {
        
        @Override
        public void apply() {
		super.apply(true);


			demand(Commodities.CREW, 3);

                supply(Commodities.ORGANS,  2);


			if (!isFunctional()) {
				supply.clear();
				unapply();
			}
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
