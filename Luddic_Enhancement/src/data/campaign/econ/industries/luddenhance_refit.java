package data.campaign.econ.industries;

import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.util.Pair;

public class luddenhance_refit extends BaseIndustry {
        
        @Override
        public void apply() {
		super.apply(true);
                int size = market.getSize();
                
                demand(Commodities.RARE_METALS, size);
				demand(Commodities.METALS, size + 2);
				demand(Commodities.ORGANICS, size + 2);
				demand(Commodities.VOLATILES, size + 1);

                supply(Commodities.SHIPS, size - 3);
				supply(Commodities.HEAVY_MACHINERY, size - 3);
				supply(Commodities.HAND_WEAPONS, size);
				supply(1, Commodities.SHIPS, 1, "Cerberus Manufactory");

                
                    Pair<String, Integer> deficit = getMaxDeficit(Commodities.RARE_METALS, Commodities.METALS, Commodities.VOLATILES, Commodities.ORGANICS);
                    int maxDeficit = size -3;
                    if (deficit.two > maxDeficit) deficit.two = maxDeficit;

                    applyDeficitToProduction(2, deficit,
					Commodities.SHIPS,
					Commodities.HEAVY_MACHINERY);

				market.getStats().getDynamic().getMod(Stats.PRODUCTION_QUALITY_MOD).modifyFlat(getModId(1), 0.15f, "Cerberus Manufactory");

			float stability = market.getPrevStability();
			if (stability < 5) {
				float stabilityMod = (stability - 5f) / 5f;
				stabilityMod *= 0.5f;
				//market.getStats().getDynamic().getMod(Stats.PRODUCTION_QUALITY_MOD).modifyFlat(getModId(0), stabilityMod, "Low stability at production source");
				market.getStats().getDynamic().getMod(Stats.PRODUCTION_QUALITY_MOD).modifyFlat(getModId(0), stabilityMod, getNameForModifier() + " - low stability");
			}

			if (!isFunctional()) {
				supply.clear();
				unapply();
			}
        }

		    @Override
			public void unapply() {
			super.unapply();
				market.getStats().getDynamic().getMod(Stats.PRODUCTION_QUALITY_MOD).unmodifyFlat(getModId(0));
				market.getStats().getDynamic().getMod(Stats.PRODUCTION_QUALITY_MOD).unmodifyFlat(getModId(1));
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
