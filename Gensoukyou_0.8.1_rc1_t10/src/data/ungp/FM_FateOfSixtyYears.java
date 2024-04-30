package data.ungp;

import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import data.utils.I18nUtil;
import ungp.api.rules.UNGP_BaseRuleEffect;
import ungp.api.rules.tags.UNGP_EconomyTag;
import ungp.scripts.campaign.specialist.UNGP_SpecialistSettings;

public class FM_FateOfSixtyYears extends UNGP_BaseRuleEffect implements UNGP_EconomyTag {

    public static final float CREW_PRICE = 500f;
    public static final int CREW_SUPPLY_REDUCTION_LARGE = 4;
    public static final int CREW_SUPPLY_REDUCTION_MEDIUM = 2;
    public static final int CREW_SUPPLY_REDUCTION_SMALL = 1;

    @Override
    public void applyPlayerMarket(MarketAPI market) {

    }

    @Override
    public void unapplyPlayerMarket(MarketAPI market) {

    }

    @Override
    public void applyAllMarket(MarketAPI market) {
        market.getCommodityData(Commodities.CREW).getCommodity().setBasePrice(CREW_PRICE);
        for (Industry industry : market.getIndustries()) {
            if (market.getSize() >= 6) {
                industry.getSupply(Commodities.CREW).getQuantity().modifyFlat(buffID, -CREW_SUPPLY_REDUCTION_LARGE, I18nUtil.getString("ungp", "FM_FateOfSixtyYears_Desc"));
            } else if (market.getSize() >= 4) {
                industry.getSupply(Commodities.CREW).getQuantity().modifyFlat(buffID, -CREW_SUPPLY_REDUCTION_MEDIUM, I18nUtil.getString("ungp", "FM_FateOfSixtyYears_Desc"));
            } else {
                industry.getSupply(Commodities.CREW).getQuantity().modifyFlat(buffID, -CREW_SUPPLY_REDUCTION_SMALL, I18nUtil.getString("ungp", "FM_FateOfSixtyYears_Desc"));
            }
        }
    }

    @Override
    public void unapplyAllMarket(MarketAPI market) {
        market.getCommodityData(Commodities.CREW).getCommodity().setBasePrice(50);
        for (Industry industry : market.getIndustries()) {

            industry.getSupply(Commodities.CREW).getQuantity().unmodifyFlat(buffID);

        }
    }

    @Override
    public String getDescriptionParams(int index, UNGP_SpecialistSettings.Difficulty difficulty) {
        if (index == 0) return String.valueOf((int) CREW_PRICE);
        if (index == 1) return String.valueOf(CREW_SUPPLY_REDUCTION_LARGE);
        if (index == 2) return String.valueOf(CREW_SUPPLY_REDUCTION_MEDIUM);
        if (index == 3) return String.valueOf(CREW_SUPPLY_REDUCTION_SMALL);
        return null;
    }

}
