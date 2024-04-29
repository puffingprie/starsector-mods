package data.scripts.campaign.econ;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketImmigrationModifier;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.population.PopulationComposition;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;

public class II_InterstellarBazaar extends BaseIndustry implements MarketImmigrationModifier {

    public static final float UPKEEP_MULT_PER_DEFICIT = 0.1f;
    public static final float BASE_ACCESSIBILITY = 0.1f;
    public static final float ALPHA_CORE_ACCESSIBILITY = 0.1f;

    protected transient SubmarketAPI saved = null;

    @Override
    public void apply() {
        super.apply(true);

        if (isFunctional()) {
            SubmarketAPI ebay = market.getSubmarket("ii_ebay");
            if (ebay == null) {
                if (saved != null) {
                    market.addSubmarket(saved);
                } else {
                    market.addSubmarket("ii_ebay");
                    Global.getSector().getEconomy().forceStockpileUpdate(market);
                }
            }
        } else {
            market.removeSubmarket("ii_ebay");
        }

        int size = market.getSize();

        demand(Commodities.SUPPLIES, size);
        demand(Commodities.CREW, size);

        Pair<String, Integer> deficit = getMaxDeficit(Commodities.SUPPLIES, Commodities.CREW);

        if (deficit.two > 0) {
            float loss = getUpkeepPenalty(deficit);
            getUpkeep().modifyMult("deficit", 1f + loss, getDeficitText(deficit.one));
        } else {
            getUpkeep().unmodifyMult("deficit");
        }

        market.getAccessibilityMod().modifyFlat(getModId(0), BASE_ACCESSIBILITY, getNameForModifier());

        if (!isFunctional()) {
            unapply();
        }
    }

    @Override
    public void unapply() {
        super.unapply();

        SubmarketAPI ebay = market.getSubmarket("ii_ebay");
        saved = ebay;
        market.removeSubmarket("ii_ebay");

        market.getAccessibilityMod().unmodifyFlat(getModId(0));
    }

    protected float getUpkeepPenalty(Pair<String, Integer> deficit) {
        float loss = deficit.two * UPKEEP_MULT_PER_DEFICIT;
        if (loss < 0) {
            loss = 0;
        }
        return loss;
    }

    @Override
    protected int getBaseStabilityMod() {
        return 1;
    }

    @Override
    protected boolean hasPostDemandSection(boolean hasDemand, IndustryTooltipMode mode) {
        return mode != IndustryTooltipMode.NORMAL || isFunctional();
    }

    @Override
    protected void addRightAfterDescriptionSection(TooltipMakerAPI tooltip, IndustryTooltipMode mode) {
        tooltip.addPara("Adds a \'Starship Bazaar\' that sells a wide variety of ships and weapons.", 10f);
    }

    @Override
    protected void addPostDemandSection(TooltipMakerAPI tooltip, boolean hasDemand, IndustryTooltipMode mode) {
        if (mode != IndustryTooltipMode.NORMAL || isFunctional()) {
            MutableStat fake = new MutableStat(0);

            String desc = getNameForModifier();
            fake.modifyFlat(getModId(0), BASE_ACCESSIBILITY, desc);
            float total = BASE_ACCESSIBILITY;

            String totalStr = "+" + (int) Math.round(total * 100f) + "%";
            Color h = Misc.getHighlightColor();
            if (total < 0) {
                h = Misc.getNegativeHighlightColor();
                totalStr = "" + (int) Math.round(total * 100f) + "%";
            }
            float opad = 10f;
            if (total >= 0) {
                tooltip.addPara("Accessibility bonus: %s", opad, h, totalStr);
            } else {
                tooltip.addPara("Accessibility penalty: %s", opad, h, totalStr);
            }
        }
    }

    @Override
    public void modifyIncoming(MarketAPI market, PopulationComposition incoming) {
        incoming.add(Factions.INDEPENDENT, 10f);
    }

    @Override
    protected void applyAlphaCoreModifiers() {
        market.getAccessibilityMod().modifyFlat(getModId(2), ALPHA_CORE_ACCESSIBILITY, "Alpha core (" + getNameForModifier() + ")");
    }

    @Override
    protected void applyNoAICoreModifiers() {
        market.getAccessibilityMod().unmodifyFlat(getModId(2));
    }

    @Override
    protected void applyAlphaCoreSupplyAndDemandModifiers() {
        demandReduction.modifyFlat(getModId(0), DEMAND_REDUCTION, "Alpha core");
    }

    @Override
    protected void addAlphaCoreDescription(TooltipMakerAPI tooltip, AICoreDescriptionMode mode) {
        float opad = 10f;
        Color highlight = Misc.getHighlightColor();

        String pre = "Alpha-level AI core currently assigned. ";
        if (mode == AICoreDescriptionMode.MANAGE_CORE_DIALOG_LIST || mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            pre = "Alpha-level AI core. ";
        }
        float a = ALPHA_CORE_ACCESSIBILITY;
        String aStr = "" + (int) Math.round(a * 100f) + "%";

        if (mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            CommoditySpecAPI coreSpec = Global.getSettings().getCommoditySpec(aiCoreId);
            TooltipMakerAPI text = tooltip.beginImageWithText(coreSpec.getIconName(), 48);
            text.addPara(pre + "Reduces upkeep cost by %s. Reduces demand by %s unit. "
                    + "Increases accessibility by %s.", 0f, highlight,
                    "" + (int) ((1f - UPKEEP_MULT) * 100f) + "%", "" + DEMAND_REDUCTION,
                    aStr);
            tooltip.addImageWithText(opad);
            return;
        }

        tooltip.addPara(pre + "Reduces upkeep cost by %s. Reduces demand by %s unit. "
                + "Increases accessibility by %s.", opad, highlight,
                "" + (int) ((1f - UPKEEP_MULT) * 100f) + "%", "" + DEMAND_REDUCTION,
                aStr);
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
