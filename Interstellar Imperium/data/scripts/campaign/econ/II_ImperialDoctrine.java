package data.scripts.campaign.econ;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketImmigrationModifier;
import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.population.PopulationComposition;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class II_ImperialDoctrine extends BaseMarketConditionPlugin implements MarketImmigrationModifier {

    public static final float STABILITY_IMPERIAL_DOCTRINE = 2f;
    public static final float DEFENSE_BONUS_IMPERIAL_DOCTRINE = 0.25f;
    public static final float FLEET_SIZE_BONUS_IMPERIAL_DOCTRINE = 0.1f;

    @Override
    public void apply(String id) {
        super.apply(id);

        if (!market.getFactionId().contentEquals("interstellarimperium")) {
            unapply(id);
            return;
        }

        int size = market.getSize();

        /* Create supply/demand in the Population industry */
        Industry population = market.getIndustry(Industries.POPULATION);
        if (population != null) {
            if (population.isFunctional()) {
                if (size > 1) {
                    population.getDemand(Commodities.MARINES).getQuantity().modifyFlat(id, size - 1, Misc.ucFirst(condition.getName().toLowerCase()));
                    if (!population.getDemandReduction().isUnmodified()) {
                        population.getDemand(Commodities.MARINES).getQuantity().modifyFlat("ind_dr", -population.getDemandReduction().getModifiedInt());
                    } else {
                        population.getDemand(Commodities.MARINES).getQuantity().unmodifyFlat("ind_dr");
                    }
                } else {
                    population.getDemand(Commodities.MARINES).getQuantity().unmodifyFlat(id);
                }
                if (size > 2) {
                    population.getDemand(Commodities.HAND_WEAPONS).getQuantity().modifyFlat(id, size - 2, Misc.ucFirst(condition.getName().toLowerCase()));
                    if (!population.getDemandReduction().isUnmodified()) {
                        population.getDemand(Commodities.HAND_WEAPONS).getQuantity().modifyFlat("ind_dr", -population.getDemandReduction().getModifiedInt());
                    } else {
                        population.getDemand(Commodities.HAND_WEAPONS).getQuantity().unmodifyFlat("ind_dr");
                    }
                } else {
                    population.getDemand(Commodities.HAND_WEAPONS).getQuantity().unmodifyFlat(id);
                }

                population.supply(id, Commodities.MARINES, size - 3, Misc.ucFirst(condition.getName().toLowerCase()));
            } else {
                population.getDemand(Commodities.MARINES).getQuantity().unmodifyFlat(id);
                population.getDemand(Commodities.HAND_WEAPONS).getQuantity().unmodifyFlat(id);

                population.getSupply(Commodities.MARINES).getQuantity().unmodifyFlat(id);
            }
        }

        /* Boost supply in all relevant industries */
        for (Industry industry : market.getIndustries()) {
            if (industry == population) {
                continue;
            }
            if (industry.isFunctional()) {
                if (industry.getSupply(Commodities.MARINES).getQuantity().getModifiedValue() > 0) {
                    industry.supply(id, Commodities.MARINES, 1, Misc.ucFirst(condition.getName().toLowerCase()));
                }
            } else {
                industry.getSupply(Commodities.MARINES).getQuantity().unmodifyFlat(id);
            }
        }

        market.addImmigrationModifier(this);

        market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD)
                .modifyMult(id, 1f + DEFENSE_BONUS_IMPERIAL_DOCTRINE, Misc.ucFirst(condition.getName().toLowerCase()));
        market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT)
                .modifyMultAlways(id, 1f + FLEET_SIZE_BONUS_IMPERIAL_DOCTRINE, Misc.ucFirst(condition.getName().toLowerCase()));

        market.getStability().modifyFlat(id, STABILITY_IMPERIAL_DOCTRINE, Misc.ucFirst(condition.getName().toLowerCase()));
    }

    @Override
    public boolean showIcon() {
        return market.getFactionId().contentEquals("interstellarimperium");
    }

    @Override
    public void unapply(String id) {
        super.unapply(id);

        Industry population = market.getIndustry(Industries.POPULATION);
        if (population != null) {
            population.getDemand(Commodities.MARINES).getQuantity().unmodifyFlat(id);
            population.getDemand(Commodities.HAND_WEAPONS).getQuantity().unmodifyFlat(id);

            population.getSupply(Commodities.MARINES).getQuantity().unmodifyFlat(id);
        }

        for (Industry industry : market.getIndustries()) {
            industry.getSupply(Commodities.MARINES).getQuantity().unmodifyFlat(id);
        }

        market.removeImmigrationModifier(this);

        market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).unmodify(id);
        market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).unmodify(id);

        market.getStability().unmodify(id);
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);

        if (market == null) {
            return;
        }

        tooltip.addPara("%s stability",
                10f, Misc.getHighlightColor(),
                "+" + (int) STABILITY_IMPERIAL_DOCTRINE);

        tooltip.addPara("%s ground defenses",
                10f, Misc.getHighlightColor(),
                "+" + (int) Math.round(DEFENSE_BONUS_IMPERIAL_DOCTRINE * 100f) + "%");

        tooltip.addPara("%s fleet size",
                10f, Misc.getHighlightColor(),
                "+" + (int) Math.round(FLEET_SIZE_BONUS_IMPERIAL_DOCTRINE * 100f) + "%");

        Industry population = market.getIndustry(Industries.POPULATION);
        if (population != null) {
            if (population.isFunctional()) {
                if (population.getDemand(Commodities.MARINES).getQuantity().getModifiedValue() > 0) {
                    String str = "" + (int) population.getDemand(Commodities.MARINES).getQuantity().getModifiedValue();
                    String text = "" + str + " " + Global.getSettings().getCommoditySpec(Commodities.MARINES).getName().toLowerCase() + " demand (" + population.getNameForModifier() + ")";
                    float pad = 10f;
                    tooltip.addPara(text, pad, Misc.getHighlightColor(), str);
                }
                if (population.getDemand(Commodities.HAND_WEAPONS).getQuantity().getModifiedValue() > 0) {
                    String str = "" + (int) population.getDemand(Commodities.HAND_WEAPONS).getQuantity().getModifiedValue();
                    String text = "" + str + " " + Global.getSettings().getCommoditySpec(Commodities.HAND_WEAPONS).getName().toLowerCase() + " demand (" + population.getNameForModifier() + ")";
                    float pad = 10f;
                    tooltip.addPara(text, pad, Misc.getHighlightColor(), str);
                }
                if (population.getSupply(Commodities.MARINES).getQuantity().getModifiedValue() > 0) {
                    String str = "" + (int) population.getSupply(Commodities.MARINES).getQuantity().getModifiedValue();
                    String text = "" + str + " " + Global.getSettings().getCommoditySpec(Commodities.MARINES).getName().toLowerCase() + " production (" + population.getNameForModifier() + ")";
                    float pad = 10f;
                    tooltip.addPara(text, pad, Misc.getHighlightColor(), str);
                }
            }
        }

        for (Industry industry : market.getIndustries()) {
            if (industry == population) {
                continue;
            }
            if (industry.isFunctional()) {
                if (industry.getSupply(Commodities.MARINES).getQuantity().getModifiedValue() > 0) {
                    String str = "+" + 1;
                    String text = "" + str + " " + Global.getSettings().getCommoditySpec(Commodities.MARINES).getName().toLowerCase() + " production (" + industry.getNameForModifier() + ")";
                    float pad = 10f;
                    tooltip.addPara(text, pad, Misc.getHighlightColor(), str);
                }
            }
        }
    }

    @Override
    public void modifyIncoming(MarketAPI market, PopulationComposition incoming) {
        incoming.set(Factions.HEGEMONY, incoming.get(Factions.HEGEMONY) / 2f);
        incoming.add(Factions.INDEPENDENT, 5f);
        incoming.add(Factions.PERSEAN, 5f);
    }
}
