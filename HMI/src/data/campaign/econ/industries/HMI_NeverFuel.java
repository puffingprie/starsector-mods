package data.campaign.econ.industries;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.util.Pair;


public class HMI_NeverFuel extends BaseIndustry {

	public void apply() {
		super.apply(true);
		supplyBonus.modifyFlat(getModId(2), market.getAdmin().getStats().getDynamic().getValue(Stats.FUEL_SUPPLY_BONUS_MOD, 0), "Administrator");
		
		int size = market.getSize();

		demand(Commodities.VOLATILES, size);
		demand(Commodities.HEAVY_MACHINERY, size);
		
		supply(Commodities.FUEL, size + 2);
		
		Pair<String, Integer> deficit = getMaxDeficit(Commodities.VOLATILES);
		
		applyDeficitToProduction(1, deficit, Commodities.FUEL);
		
		if (!isFunctional()) {
			supply.clear();
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

