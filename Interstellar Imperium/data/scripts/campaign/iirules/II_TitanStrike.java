package data.scripts.campaign.iirules;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.RuleBasedDialog;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.MutableStat.StatMod;
import com.fs.starfarer.api.combat.StatBonus;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.CustomRepImpact;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActionEnvelope;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActions;
import com.fs.starfarer.api.impl.campaign.DebugFlags;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.ids.Strings;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.deciv.DecivTracker;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.impl.campaign.rulecmd.FireAll;
import com.fs.starfarer.api.impl.campaign.rulecmd.ShowDefaultVisual;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.StatModValueGetter;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;
import data.scripts.IIModPlugin;
import data.scripts.hullmods.II_TitanBombardment;
import data.scripts.util.II_Util;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector2f;
import starship_legends.campaign.rulecmd.SL_ShowMarketDefenses;

public class II_TitanStrike extends BaseCommandPlugin {

    public static class TempData {

        public boolean canTitanStrike;
        public float defenderStr;
        public List<FactionAPI> willBecomeHostile = new ArrayList<>();
    }

    protected CampaignFleetAPI playerFleet;
    protected SectorEntityToken entity;
    protected FactionAPI playerFaction;
    protected FactionAPI entityFaction;
    protected TextPanelAPI text;
    protected OptionPanelAPI options;
    protected CargoAPI playerCargo;
    protected MemoryAPI memory;
    protected MarketAPI market;
    protected InteractionDialogAPI dialog;
    protected Map<String, MemoryAPI> memoryMap;
    protected FactionAPI faction;

    protected TempData temp = new TempData();

    public II_TitanStrike() {
    }

    public II_TitanStrike(SectorEntityToken entity) {
        init(entity);
    }

    protected void clearTemp() {
        if (temp != null) {
            temp.willBecomeHostile.clear();
        }
    }

    private void init(SectorEntityToken entity) {
        memory = entity.getMemoryWithoutUpdate();
        this.entity = entity;
        playerFleet = Global.getSector().getPlayerFleet();
        playerCargo = playerFleet.getCargo();

        playerFaction = Global.getSector().getPlayerFaction();
        entityFaction = entity.getFaction();

        faction = entity.getFaction();

        market = entity.getMarket();

        String key = "$II_TitanStrike_temp";
        MemoryAPI mem = market.getMemoryWithoutUpdate();
        if (mem.contains(key)) {
            temp = (TempData) mem.get(key);
        } else {
            mem.set(key, temp, 0f);
        }
    }

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
        if (IIModPlugin.hasStarshipLegends) {
            new SL_ShowMarketDefenses().execute(ruleId, dialog, params, memoryMap);
        } else if (!IIModPlugin.isExerelin) {
            if (!(new MarketCMD(dialog.getInteractionTarget()).execute(ruleId, dialog, params, memoryMap))) {
                return false;
            }
        }

        this.dialog = dialog;
        this.memoryMap = memoryMap;

        String command = params.get(0).getString(memoryMap);
        if (command == null) {
            return false;
        }

        entity = dialog.getInteractionTarget();
        init(entity);

        memory = getEntityMemory(memoryMap);

        text = dialog.getTextPanel();
        options = dialog.getOptionPanel();

        switch (command) {
            case "showDefenses":
            case "goBackToDefenses": {
                /* Inject new options into existing menu! */
                clearTemp();
                showDefenses();
                break;
            }
            case "iiTitanStrikeMenu":
                titanStrikeMenu();
                break;
            case "iiTitanStrikeConfirm":
                titanStrikeConfirm();
                break;
            case "iiTitanStrikeResult":
                titanStrikeResult();
                break;
            default:
                break;
        }

        return true;
    }

    /* This must be called after an actual showDefenses from a MarketCMD script */
    protected void showDefenses() {
        String key = "$MarketCMD_temp";
        MemoryAPI mem = market.getMemoryWithoutUpdate();
        MarketCMD.TempData marketCMDtemp = null;
        if (mem.contains(key)) {
            marketCMDtemp = (MarketCMD.TempData) mem.get(key);
        }

        temp.canTitanStrike = false;
        if (marketCMDtemp != null) {
            for (FleetMemberAPI member : playerFleet.getFleetData().getMembersListCopy()) {
                if (member.isMothballed()) {
                    continue;
                }

                if (II_Util.getNonDHullId(member.getHullSpec()).contentEquals("ii_olympus")) {
                    temp.canTitanStrike = true;
                    break;
                }
            }
            temp.canTitanStrike = temp.canTitanStrike && marketCMDtemp.canBombard;
        }

        if (DebugFlags.MARKET_HOSTILITIES_DEBUG) {
            if (!temp.canTitanStrike) {
                text.addPara("(DEBUG mode: can Titan strike anyway)");
            }
            temp.canTitanStrike = true;
        }

        options.addOption("Consider the eradication of " + market.getName() + " (Titan strike)", "iiTitanStrikeMenu");

        if (!temp.canTitanStrike) {
            options.setEnabled("iiTitanStrikeMenu", false);
            options.setTooltip("iiTitanStrikeMenu", "All defenses must be defeated to make eradication possible.");
        }
    }

    public static float getDefenderStr(MarketAPI market) {
        StatBonus stat = market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD);
        float defenderStr = (int) Math.round(stat.computeEffective(0f));
        return defenderStr;
    }

    protected void addConfirmOptions() {
        options.clearOptions();
        options.addOption("Eradicate " + market.getName(), "iiTitanStrikeConfirm");
        options.addOption("Never mind", "iiTitanStrikeGoBack");
        options.setShortcut("iiTitanStrikeGoBack", Keyboard.KEY_ESCAPE, false, false, false, true);

        List<FactionAPI> nonHostile = new ArrayList<>();
        for (FactionAPI fac : temp.willBecomeHostile) {
            boolean hostile = fac.isHostileTo(Factions.PLAYER);
            if (!hostile) {
                nonHostile.add(fac);
            }
        }

        if (nonHostile.size() == 1) {
            FactionAPI fac = nonHostile.get(0);
            options.addOptionConfirmation("iiTitanStrikeConfirm",
                    "The " + fac.getDisplayNameLong()
                    + " " + fac.getDisplayNameIsOrAre()
                    + " not currently hostile, and will become hostile if you carry out the eradication. "
                    + "Are you sure?", "Yes", "Never mind");
        } else if (nonHostile.size() > 1) {
            options.addOptionConfirmation("iiTitanStrikeConfirm",
                    "Multiple factions that are not currently hostile "
                    + "will become hostile if you carry out the eradication. "
                    + "Are you sure?", "Yes", "Never mind");
        }
    }

    public static StatModValueGetter statPrinter(final boolean withNegative) {
        return new StatModValueGetter() {
            @Override
            public String getPercentValue(StatMod mod) {
                String prefix = mod.getValue() > 0 ? "+" : "";
                return prefix + (int) (mod.getValue()) + "%";
            }

            @Override
            public String getMultValue(StatMod mod) {
                return Strings.X + "" + Misc.getRoundedValue(mod.getValue());
            }

            @Override
            public String getFlatValue(StatMod mod) {
                String prefix = mod.getValue() > 0 ? "+" : "";
                return prefix + (int) (mod.getValue()) + "";
            }

            @Override
            public Color getModColor(StatMod mod) {
                if (withNegative && mod.getValue() < 1f) {
                    return Misc.getNegativeHighlightColor();
                }
                return null;
            }
        };
    }

    public static boolean canTitanStrike(MarketAPI market, CampaignFleetAPI fleet) {
        float str = getDefenderStr(market);
        int result = (int) (str);
        if (result < 2) {
            result = 2;
        }
        if (fleet != null) {
            float eradicationPower = Misc.getFleetwideTotalMod(fleet, "ii_eradication_power", 0f);
            result -= eradicationPower;
        }
        return result <= 0;
    }

    protected void titanStrikeMenu() {
        float width = 350;
        float opad = 10f;
        float small = 5f;

        Color h = Misc.getHighlightColor();

        dialog.getVisualPanel().showImagePortion("illustrations", "bombard_prepare", 640, 400, 0, 0, 480, 300);

        StatBonus defender = market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD);

        float eradicationPower = Misc.getFleetwideTotalMod(playerFleet, "ii_eradication_power", 0f);

        float defenderStr = (int) Math.round(defender.computeEffective(0f));
        if (defenderStr < 0) {
            defenderStr = 0;
        }

        temp.defenderStr = defenderStr;

        TooltipMakerAPI info = text.beginTooltip();

        info.setParaSmallInsignia();

        String is = faction.getDisplayNameIsOrAre();
        boolean hostile = faction.isHostileTo(Factions.PLAYER);
        boolean tOn = playerFleet.isTransponderOn();
        float initPad = 0f;
        if (!hostile) {
            if (tOn) {
                info.addPara(Misc.ucFirst(faction.getDisplayNameWithArticle()) + " " + is
                        + " not currently hostile. Eradication is a major enough hostile action that it can't be concealed, "
                        + "regardless of transponder status.",
                        initPad, faction.getBaseUIColor(), faction.getDisplayNameWithArticleWithoutArticle());
            }
            initPad = opad;
        }

        info.addPara("The true purpose of the Olympus is to launch a Titan strike against a hostile population center, rendering "
                + "it uninhabitable. More commonly known as a \"planetkiller\", the Titan is capable of utterly destroying "
                + "a hostile market and causing enough environmental damage that further habitation becomes impractical.", initPad);

        info.addPara("Ground defense strength: %s", opad, h, "" + (int) defenderStr);
        info.addStatModGrid(width, 50, opad, small, defender, true, statPrinter(true));
        info.addPara("Titan strength: %s", opad, h, "" + (int) eradicationPower);

        text.addTooltip();

        boolean canTitanStrike = canTitanStrike(market, playerFleet);

        options.clearOptions();

        if (DebugFlags.MARKET_HOSTILITIES_DEBUG) {
            canTitanStrike = true;
        }

        temp.willBecomeHostile.clear();
        temp.willBecomeHostile.add(faction);

        List<FactionAPI> nonHostile = new ArrayList<>();
        for (FactionAPI fac : Global.getSector().getAllFactions()) {
            if (temp.willBecomeHostile.contains(fac)) {
                continue;
            }

            if (fac.getCustomBoolean(Factions.CUSTOM_CARES_ABOUT_ATROCITIES)) {
                boolean facHostile = fac.isHostileTo(Factions.PLAYER);
                temp.willBecomeHostile.add(fac);
                if (!facHostile) {
                    nonHostile.add(fac);
                }
            }
        }

        if (nonHostile.isEmpty()) {
            text.addPara("An atrocity of this scale can not be hidden, but any factions that would "
                    + "be dismayed by such actions are already hostile to you.");
        } else {
            text.addPara("An atrocity of this scale can not be hidden, "
                    + "and will make the following factions hostile:");
        }

        if (!nonHostile.isEmpty()) {
            TooltipMakerAPI hostileInfo = text.beginTooltip();
            hostileInfo.setParaFontDefault();

            hostileInfo.setBulletedListMode(BaseIntelPlugin.INDENT);
            float hostileInitPad = 0f;
            for (FactionAPI fac : nonHostile) {
                hostileInfo.addPara(Misc.ucFirst(fac.getDisplayName()), fac.getBaseUIColor(), hostileInitPad);
                hostileInitPad = 3f;
            }
            hostileInfo.setBulletedListMode(null);

            text.addTooltip();
        }

        addConfirmOptions();

        if (!canTitanStrike) {
            options.setEnabled("iiTitanStrikeConfirm", false);
            options.setTooltip("iiTitanStrikeConfirm", "Defenses are too strong.");
        }
    }

    protected void titanStrikeConfirm() {
        dialog.getVisualPanel().showImagePortion("illustrations", "bombard_saturation_result", 640, 400, 0, 0, 480, 300);

        if (!DebugFlags.MARKET_HOSTILITIES_DEBUG) {
            Misc.increaseMarketHostileTimeout(market, 180f);
        }

        int baseTitanStrikeCost = (int) getDefenderStr(market);

        for (FactionAPI curr : temp.willBecomeHostile) {
            CustomRepImpact impact = new CustomRepImpact();
            impact.delta = market.getSize() * -0.01f * 2f;
            impact.ensureAtBest = RepLevel.HOSTILE;
            if (curr == faction) {
                impact.ensureAtBest = RepLevel.VENGEFUL;
            }
            Global.getSector().adjustPlayerReputation(
                    new RepActionEnvelope(RepActions.CUSTOM,
                            impact, null, text, true, true),
                    curr.getId());
        }

        List<String> eradicationDesc = new ArrayList<>();

        /* Pollution is irrelevant */
        if (market.hasCondition(Conditions.POLLUTION)) {
            market.removeCondition(Conditions.POLLUTION);
        }

        /* Uninhabitable */
        if (market.hasCondition(Conditions.HABITABLE)) {
            market.removeCondition(Conditions.HABITABLE);
            eradicationDesc.add("rendered uninhabitable");

            if ((market.getPrimaryEntity() != null) && (market.getPrimaryEntity() instanceof PlanetAPI)) {
                PlanetAPI planet = (PlanetAPI) market.getPrimaryEntity();
                Color newAtmosphere = II_Util.interpolateColor255(planet.getSpec().getAtmosphereColor(), new Color(150, 75, 0, planet.getSpec().getAtmosphereColor().getAlpha()), 0.33f);
                planet.getSpec().setAtmosphereColor(newAtmosphere);
                Color newClouds = II_Util.interpolateColor255(planet.getSpec().getCloudColor(), new Color(150, 75, 0, planet.getSpec().getCloudColor().getAlpha()), 0.33f);
                planet.getSpec().setCloudColor(newClouds);
                Color newPlanet = II_Util.interpolateColor255(planet.getSpec().getPlanetColor(), new Color(159, 129, 112, 255), 0.25f);
                planet.getSpec().setPlanetColor(newPlanet);
            }
        } else {
            if ((market.getPrimaryEntity() != null) && (market.getPrimaryEntity() instanceof PlanetAPI)) {
                PlanetAPI planet = (PlanetAPI) market.getPrimaryEntity();
                Color newAtmosphere = II_Util.interpolateColor255(planet.getSpec().getAtmosphereColor(), new Color(150, 125, 100, planet.getSpec().getAtmosphereColor().getAlpha()), 0.25f);
                planet.getSpec().setAtmosphereColor(newAtmosphere);
                Color newClouds = II_Util.interpolateColor255(planet.getSpec().getCloudColor(), new Color(150, 125, 100, planet.getSpec().getCloudColor().getAlpha()), 0.25f);
                planet.getSpec().setCloudColor(newClouds);
                Color newPlanet = II_Util.interpolateColor255(planet.getSpec().getPlanetColor(), new Color(150, 125, 100, 255), 0.15f);
                planet.getSpec().setPlanetColor(newPlanet);
            }
        }

        /* No biosphere */
        if (market.hasCondition(Conditions.INIMICAL_BIOSPHERE)) {
            market.removeCondition(Conditions.INIMICAL_BIOSPHERE);
        }
        if (market.hasCondition("VIC_VBomb_scar")) {
            market.removeCondition("VIC_VBomb_scar");
            eradicationDesc.add("wiped out contaminants");

            if ((market.getPrimaryEntity() != null) && (market.getPrimaryEntity() instanceof PlanetAPI)) {
                PlanetAPI planet = (PlanetAPI) market.getPrimaryEntity();
                Color newAtmosphere = II_Util.interpolateColor255(planet.getSpec().getAtmosphereColor(), new Color(255, 100, 255, 255), 0.5f);
                planet.getSpec().setAtmosphereColor(newAtmosphere);
                planet.getSpec().setAtmosphereThickness(0.2f);
                planet.getSpec().setAtmosphereThicknessMin(62f);
                Color newClouds = II_Util.interpolateColor255(planet.getSpec().getCloudColor(), new Color(255, 100, 255, 255), 0.5f);
                planet.getSpec().setCloudColor(newClouds);
            }
        }

        /* Climate destroyed */
        if (market.hasCondition(Conditions.MILD_CLIMATE)) {
            market.removeCondition(Conditions.MILD_CLIMATE);
        }

        /* Boils the fucking ocean, creating incredible greenhouse conditions */
        if (market.hasCondition(Conditions.WATER_SURFACE)) {
            market.removeCondition(Conditions.WATER_SURFACE);
            eradicationDesc.add("oceans boiled away");

            if (market.hasCondition(Conditions.NO_ATMOSPHERE)) {
                market.removeCondition(Conditions.NO_ATMOSPHERE);
                eradicationDesc.add("thickened atmosphere due to escaped water vapor");

                if ((market.getPrimaryEntity() != null) && (market.getPrimaryEntity() instanceof PlanetAPI)) {
                    PlanetAPI planet = (PlanetAPI) market.getPrimaryEntity();
                    Color newAtmosphere = II_Util.interpolateColor255(planet.getSpec().getAtmosphereColor(), new Color(150, 255, 75, 255), 0.5f);
                    planet.getSpec().setAtmosphereColor(newAtmosphere);
                    planet.getSpec().setAtmosphereThickness(0.2f);
                    planet.getSpec().setAtmosphereThicknessMin(62f);
                    Color newClouds = II_Util.interpolateColor255(planet.getSpec().getCloudColor(), new Color(150, 255, 75, 255), 0.25f);
                    planet.getSpec().setCloudColor(newClouds);
                }
            } else if (market.hasCondition(Conditions.THIN_ATMOSPHERE)) {
                market.removeCondition(Conditions.THIN_ATMOSPHERE);
                eradicationDesc.add("thickened atmosphere due to escaped water vapor");

                if ((market.getPrimaryEntity() != null) && (market.getPrimaryEntity() instanceof PlanetAPI)) {
                    PlanetAPI planet = (PlanetAPI) market.getPrimaryEntity();
                    Color newAtmosphere = II_Util.interpolateColor255(planet.getSpec().getAtmosphereColor(), new Color(150, 255, 75, 255), 0.33f);
                    planet.getSpec().setAtmosphereColor(newAtmosphere);
                    planet.getSpec().setAtmosphereThickness(0.2f);
                    planet.getSpec().setAtmosphereThicknessMin(62f);
                    Color newClouds = II_Util.interpolateColor255(planet.getSpec().getCloudColor(), new Color(150, 255, 75, 255), 0.2f);
                    planet.getSpec().setCloudColor(newClouds);
                }
            } else if (!market.hasCondition(Conditions.TOXIC_ATMOSPHERE) && !market.hasCondition(Conditions.DENSE_ATMOSPHERE)) {
                market.addCondition(Conditions.DENSE_ATMOSPHERE);
                eradicationDesc.add("thickened atmosphere due to escaped water vapor");

                if ((market.getPrimaryEntity() != null) && (market.getPrimaryEntity() instanceof PlanetAPI)) {
                    PlanetAPI planet = (PlanetAPI) market.getPrimaryEntity();
                    Color newAtmosphere = II_Util.interpolateColor255(planet.getSpec().getAtmosphereColor(), new Color(150, 255, 75, 255), 0.25f);
                    planet.getSpec().setAtmosphereColor(newAtmosphere);
                    planet.getSpec().setAtmosphereThickness(0.3f);
                    planet.getSpec().setAtmosphereThicknessMin(70f);
                    Color newClouds = II_Util.interpolateColor255(planet.getSpec().getCloudColor(), new Color(150, 255, 75, 255), 0.15f);
                    planet.getSpec().setCloudColor(newClouds);
                }
            }

            boolean hasStar = false;
            if (market.getLocation() instanceof StarSystemAPI) {
                StarSystemAPI marketSystem = (StarSystemAPI) market.getLocation();
                for (PlanetAPI planet : marketSystem.getPlanets()) {
                    if (planet.getSpec().isStar() && !planet.getSpec().isNebulaCenter() && !planet.getSpec().isBlackHole()) {
                        hasStar = true;
                        break;
                    }
                }
            }

            if (hasStar) {
                if (market.hasCondition(Conditions.VERY_COLD)) {
                    market.removeCondition(Conditions.VERY_COLD);
                    market.addCondition(Conditions.COLD);
                    eradicationDesc.add("experienced an extreme greenhouse gas effect");
                } else if (market.hasCondition(Conditions.COLD)) {
                    market.removeCondition(Conditions.COLD);
                    eradicationDesc.add("experienced an extreme greenhouse gas effect");
                } else if (market.hasCondition(Conditions.HOT)) {
                    market.removeCondition(Conditions.HOT);
                    market.addCondition(Conditions.VERY_HOT);
                    eradicationDesc.add("experienced an extreme greenhouse gas effect");
                } else if (market.hasCondition(Conditions.VERY_HOT)) {
                } else {
                    market.addCondition(Conditions.HOT);
                    eradicationDesc.add("experienced an extreme greenhouse gas effect");
                }
            }

            if ((market.getPrimaryEntity() != null) && (market.getPrimaryEntity() instanceof PlanetAPI)) {
                PlanetAPI planet = (PlanetAPI) market.getPrimaryEntity();
                Color newAtmosphere = II_Util.interpolateColor255(planet.getSpec().getAtmosphereColor(),
                        new Color(150, 255, 0, II_Util.clamp255((int) (planet.getSpec().getAtmosphereColor().getAlpha() * 1.5f))), 0.5f);
                planet.getSpec().setAtmosphereColor(newAtmosphere);
                Color newClouds = II_Util.interpolateColor255(planet.getSpec().getCloudColor(),
                        new Color(150, 255, 0, II_Util.clamp255((int) (planet.getSpec().getCloudColor().getAlpha() * 1.5f))), 0.25f);
                planet.getSpec().setCloudColor(newClouds);
                Color newPlanet = II_Util.interpolateColor255(planet.getSpec().getPlanetColor(), new Color(150, 255, 0, 255), 0.33f);
                planet.getSpec().setPlanetColor(newPlanet);
            }
        }

        /* Frees volatiles and thickens the atmosphere */
        if (market.hasCondition(Conditions.VOLATILES_TRACE)) {
            market.removeCondition(Conditions.VOLATILES_TRACE);
            eradicationDesc.add("vaporized all volatile deposits");
        } else if (market.hasCondition(Conditions.VOLATILES_DIFFUSE)) {
            market.removeCondition(Conditions.VOLATILES_DIFFUSE);
            market.removeCondition(Conditions.VOLATILES_DIFFUSE);
            eradicationDesc.add("vaporized all volatile deposits");

            if ((market.getPrimaryEntity() != null) && (market.getPrimaryEntity() instanceof PlanetAPI) && !((PlanetAPI) market.getPrimaryEntity()).isGasGiant()) {
                if (market.hasCondition(Conditions.NO_ATMOSPHERE)) {
                    market.removeCondition(Conditions.NO_ATMOSPHERE);
                    market.addCondition(Conditions.THIN_ATMOSPHERE);
                    eradicationDesc.add("thickened atmosphere due to escaped gases");

                    PlanetAPI planet = (PlanetAPI) market.getPrimaryEntity();
                    Color newAtmosphere = II_Util.interpolateColor255(planet.getSpec().getAtmosphereColor(), new Color(125, 255, 175, 255), 0.25f);
                    planet.getSpec().setAtmosphereColor(newAtmosphere);
                    planet.getSpec().setAtmosphereThickness(0.07f);
                    planet.getSpec().setAtmosphereThicknessMin(12f);
                    Color newClouds = II_Util.interpolateColor255(planet.getSpec().getCloudColor(), new Color(125, 255, 175, 255), 0.15f);
                    planet.getSpec().setCloudColor(newClouds);
                }
            }
        } else if (market.hasCondition(Conditions.VOLATILES_ABUNDANT) || market.hasCondition(Conditions.VOLATILES_PLENTIFUL)) {
            if (market.hasCondition(Conditions.VOLATILES_ABUNDANT)) {
                market.removeCondition(Conditions.VOLATILES_ABUNDANT);
                market.addCondition(Conditions.VOLATILES_TRACE);
            } else {
                market.removeCondition(Conditions.VOLATILES_PLENTIFUL);
                market.addCondition(Conditions.VOLATILES_DIFFUSE);
            }
            eradicationDesc.add("vaporized many volatile deposits");

            if (market.hasCondition(Conditions.NO_ATMOSPHERE) && (market.getPrimaryEntity() != null) && (market.getPrimaryEntity() instanceof PlanetAPI)) {
                market.removeCondition(Conditions.NO_ATMOSPHERE);
                market.addCondition(Conditions.THIN_ATMOSPHERE);
                eradicationDesc.add("thickened atmosphere due to escaped gases");

                PlanetAPI planet = (PlanetAPI) market.getPrimaryEntity();
                Color newAtmosphere = II_Util.interpolateColor255(planet.getSpec().getAtmosphereColor(), new Color(125, 255, 150, 255), 0.25f);
                planet.getSpec().setAtmosphereColor(newAtmosphere);
                planet.getSpec().setAtmosphereThickness(0.07f);
                planet.getSpec().setAtmosphereThicknessMin(12f);
                Color newClouds = II_Util.interpolateColor255(planet.getSpec().getCloudColor(), new Color(125, 255, 150, 255), 0.15f);
                planet.getSpec().setCloudColor(newClouds);
            } else if (market.hasCondition(Conditions.THIN_ATMOSPHERE)) {
                market.removeCondition(Conditions.THIN_ATMOSPHERE);
                market.addCondition(Conditions.EXTREME_WEATHER);
                eradicationDesc.add("thickened atmosphere due to escaped gases");
                eradicationDesc.add("dangerously destabilized weather patterns");

                if ((market.getPrimaryEntity() != null) && (market.getPrimaryEntity() instanceof PlanetAPI)) {
                    PlanetAPI planet = (PlanetAPI) market.getPrimaryEntity();
                    Color newAtmosphere = II_Util.interpolateColor255(planet.getSpec().getAtmosphereColor(), new Color(125, 255, 150, 255), 0.33f);
                    planet.getSpec().setAtmosphereColor(newAtmosphere);
                    planet.getSpec().setAtmosphereThickness(0.2f);
                    planet.getSpec().setAtmosphereThicknessMin(30f);
                    Color newClouds = II_Util.interpolateColor255(planet.getSpec().getCloudColor(), new Color(125, 255, 150, 255), 0.2f);
                    planet.getSpec().setCloudColor(newClouds);
                }
            } else if (!market.hasCondition(Conditions.TOXIC_ATMOSPHERE) && !market.hasCondition(Conditions.DENSE_ATMOSPHERE)) {
                market.addCondition(Conditions.TOXIC_ATMOSPHERE);
                eradicationDesc.add("thickened atmosphere due to escaped gases");

                if ((market.getPrimaryEntity() != null) && (market.getPrimaryEntity() instanceof PlanetAPI)) {
                    PlanetAPI planet = (PlanetAPI) market.getPrimaryEntity();
                    Color newAtmosphere = II_Util.interpolateColor255(planet.getSpec().getAtmosphereColor(), new Color(125, 255, 150, 255), 0.5f);
                    planet.getSpec().setAtmosphereColor(newAtmosphere);
                    planet.getSpec().setAtmosphereThickness(0.3f);
                    planet.getSpec().setAtmosphereThicknessMin(50f);
                    Color newClouds = II_Util.interpolateColor255(planet.getSpec().getCloudColor(), new Color(125, 255, 150, 255), 0.25f);
                    planet.getSpec().setCloudColor(newClouds);
                }
            }

            boolean hasStar = false;
            if (market.getLocation() instanceof StarSystemAPI) {
                StarSystemAPI marketSystem = (StarSystemAPI) market.getLocation();
                for (PlanetAPI planet : marketSystem.getPlanets()) {
                    if (planet.getSpec().isStar() && !planet.getSpec().isNebulaCenter() && !planet.getSpec().isBlackHole()) {
                        hasStar = true;
                        break;
                    }
                }
            }

            if (hasStar) {
                if (market.hasCondition(Conditions.VERY_COLD)) {
                    market.removeCondition(Conditions.VERY_COLD);
                    market.addCondition(Conditions.COLD);
                    eradicationDesc.add("experienced an extreme greenhouse gas effect");
                } else if (market.hasCondition(Conditions.COLD)) {
                    market.removeCondition(Conditions.COLD);
                    eradicationDesc.add("experienced an extreme greenhouse gas effect");
                } else if (market.hasCondition(Conditions.HOT)) {
                    market.removeCondition(Conditions.HOT);
                    market.addCondition(Conditions.VERY_HOT);
                    eradicationDesc.add("experienced an extreme greenhouse gas effect");
                } else if (market.hasCondition(Conditions.VERY_HOT)) {
                } else {
                    market.addCondition(Conditions.HOT);
                    eradicationDesc.add("experienced an extreme greenhouse gas effect");
                }
            }
        } else if (market.hasCondition(Conditions.THIN_ATMOSPHERE)) {
            /* Remaining atmosphere blown into space */
            market.removeCondition(Conditions.THIN_ATMOSPHERE);
            eradicationDesc.add("dispersed the remaining atmosphere into space");
            market.addCondition(Conditions.NO_ATMOSPHERE);

            if ((market.getPrimaryEntity() != null) && (market.getPrimaryEntity() instanceof PlanetAPI)) {
                PlanetAPI planet = (PlanetAPI) market.getPrimaryEntity();
                planet.getSpec().setAtmosphereThickness(0f);
                planet.getSpec().setCloudColor(new Color(0, 0, 0, 0));
            }
        } else if (!market.hasCondition(Conditions.NO_ATMOSPHERE)
                && !market.hasCondition(Conditions.TOXIC_ATMOSPHERE) && !market.hasCondition(Conditions.DENSE_ATMOSPHERE)
                && (market.getPrimaryEntity() != null) && (market.getPrimaryEntity() instanceof PlanetAPI)) {
            /* Messes up a standard atmosphere */
            market.addCondition(Conditions.EXTREME_WEATHER);
            eradicationDesc.add("dangerously destabilized weather patterns");
        }

        /* Tectonic plates reinvigorated */
        if (!market.hasCondition(Conditions.TECTONIC_ACTIVITY) && !market.hasCondition(Conditions.EXTREME_TECTONIC_ACTIVITY)
                && (market.getPrimaryEntity() != null) && (market.getPrimaryEntity() instanceof PlanetAPI) && !((PlanetAPI) market.getPrimaryEntity()).isGasGiant()) {
            market.addCondition(Conditions.TECTONIC_ACTIVITY);
            eradicationDesc.add("restarted geological activity");
        }

        /* Farmland ruined forever */
        if (market.hasCondition(Conditions.FARMLAND_POOR)) {
            market.removeCondition(Conditions.FARMLAND_POOR);
            eradicationDesc.add("eradicated all arable land");
        } else if (market.hasCondition(Conditions.FARMLAND_ADEQUATE)) {
            market.removeCondition(Conditions.FARMLAND_ADEQUATE);
            eradicationDesc.add("eradicated all arable land");
        } else if (market.hasCondition(Conditions.FARMLAND_RICH)) {
            market.removeCondition(Conditions.FARMLAND_RICH);
            eradicationDesc.add("eradicated all arable land");
        } else if (market.hasCondition(Conditions.FARMLAND_BOUNTIFUL)) {
            market.removeCondition(Conditions.FARMLAND_BOUNTIFUL);
            eradicationDesc.add("eradicated all arable land");
        }

        if (market.hasCondition(Conditions.VOLTURNIAN_LOBSTER_PENS)) {
            market.removeCondition(Conditions.VOLTURNIAN_LOBSTER_PENS);
        }
        if (market.hasCondition("ii_vineyards")) {
            market.removeCondition("ii_vineyards");
        }

        /* Surface-layer organics incinerated */
        if (market.hasCondition(Conditions.ORGANICS_TRACE)) {
            market.removeCondition(Conditions.ORGANICS_TRACE);
            eradicationDesc.add("burned all organic deposits");
        } else if (market.hasCondition(Conditions.ORGANICS_COMMON)) {
            market.removeCondition(Conditions.ORGANICS_COMMON);
            market.addCondition(Conditions.ORGANICS_TRACE);
            eradicationDesc.add("burned many organic deposits");
        } else if (market.hasCondition(Conditions.ORGANICS_ABUNDANT)) {
            market.removeCondition(Conditions.ORGANICS_ABUNDANT);
            market.addCondition(Conditions.ORGANICS_COMMON);
            eradicationDesc.add("burned many organic deposits");
        } else if (market.hasCondition(Conditions.ORGANICS_PLENTIFUL)) {
            market.removeCondition(Conditions.ORGANICS_PLENTIFUL);
            market.addCondition(Conditions.ORGANICS_ABUNDANT);
            eradicationDesc.add("burned many organic deposits");
        }

        if (!eradicationDesc.isEmpty()) {
            text.addPara(market.getName() + " experienced the following effects: " + Misc.getAndJoined(eradicationDesc) + ".");
        }

        if ((market.getPrimaryEntity() != null) && (market.getPrimaryEntity() instanceof PlanetAPI)) {
            PlanetAPI planet = (PlanetAPI) market.getPrimaryEntity();
            planet.applySpecChanges();
        }
        DecivTracker.decivilize(market, true);
        text.addPara(market.getName() + " destroyed.");

        for (MarketConditionAPI condition : market.getConditions()) {
            condition.setSurveyed(true);
        }

        for (FleetMemberAPI member : playerFleet.getFleetData().getMembersListCopy()) {
            if (member.isMothballed()) {
                continue;
            }

            if (II_Util.getNonDHullId(member.getHullSpec()).contentEquals("ii_olympus")) {
                if (member.getRepairTracker().getCR() < II_TitanBombardment.getEradicationCRPenalty(member.getVariant())) {
                    continue;
                }

                if ((dialog != null) && (dialog.getTextPanel() != null)) {
                    String penaltyStr = "" + (int) Math.round(-II_TitanBombardment.getEradicationCRPenalty(member.getVariant()) * 100f) + "%";
                    dialog.getTextPanel().addPara(member.getShipName() + ": " + penaltyStr + "% CR", Misc.getNegativeHighlightColor(), penaltyStr);
                }
                member.getRepairTracker().applyCREvent(-II_TitanBombardment.getEradicationCRPenalty(member.getVariant()), "Eradication of " + market.getName());
                playerFleet.getFleetData().setSyncNeeded();
                playerFleet.getFleetData().syncIfNeeded();

                baseTitanStrikeCost -= II_TitanBombardment.ERADICATION_POWER;
                if (baseTitanStrikeCost <= 0) {
                    break;
                }
            }
        }

        if (dialog != null && dialog.getPlugin() instanceof RuleBasedDialog) {
            if (dialog.getInteractionTarget() != null
                    && dialog.getInteractionTarget().getMarket() != null) {
                Global.getSector().setPaused(false);
                dialog.getInteractionTarget().getMarket().getMemoryWithoutUpdate().advance(0.0001f);
                Global.getSector().setPaused(true);
            }
            ((RuleBasedDialog) dialog.getPlugin()).updateMemory();
        }

        Misc.setFlagWithReason(market.getMemoryWithoutUpdate(), MemFlags.RECENTLY_BOMBARDED,
                Factions.PLAYER, true, 30f);

        if (dialog != null && dialog.getPlugin() instanceof RuleBasedDialog) {
            ((RuleBasedDialog) dialog.getPlugin()).updateMemory();
        }

        addTitanStrikeVisual(market.getPrimaryEntity());

        addTitanStrikeContinueOption();

        Global.getSoundPlayer().playUISound("ii_titan_explode_charge", 1f, 1f);
    }

    protected void titanStrikeResult() {
        clearTemp();

        new ShowDefaultVisual().execute(null, dialog, Misc.tokenize(""), memoryMap);

        dialog.getInteractionTarget().getMemoryWithoutUpdate().set("$menuState", "main", 0);
        if (dialog.getInteractionTarget().getMemoryWithoutUpdate().contains("$tradeMode")) {
            if (market.isPlanetConditionMarketOnly()) {
                dialog.getInteractionTarget().getMemoryWithoutUpdate().unset("$hasMarket");
            }
            dialog.getInteractionTarget().getMemoryWithoutUpdate().set("$tradeMode", "NONE", 0);
        } else {
            dialog.getInteractionTarget().getMemoryWithoutUpdate().set("$tradeMode", "OPEN", 0);
        }

        FireAll.fire(null, dialog, memoryMap, "PopulateOptions");
    }

    protected void addTitanStrikeContinueOption() {
        options.clearOptions();
        options.addOption("Continue", "iiTitanStrikeResult");
    }

    public static void addTitanStrikeVisual(SectorEntityToken target) {
        if (target != null && target.isInCurrentLocation()) {
            int num = (int) (target.getRadius() * target.getRadius() / 300f);
            num *= 6;
            if (num > 300) {
                num = 300;
            }
            if (num < 10) {
                num = 10;
            }
            target.addScript(new TitanStrikeAnimation(num, target));
        }
    }

    public static class TitanStrikeAnimation implements EveryFrameScript {

        public TitanStrikeAnimation(int num, SectorEntityToken target) {
            this.num = num;
            this.target = target;
        }
        int num = 0;
        SectorEntityToken target;
        int added = 0;
        float elapsed = 0;

        @Override
        public boolean runWhilePaused() {
            return false;
        }

        @Override
        public boolean isDone() {
            return added >= num;
        }

        @Override
        public void advance(float amount) {
            elapsed += amount * (float) Math.random();
            if (elapsed < 0.03f) {
                return;
            }

            elapsed = 0f;

            int curr = (int) Math.round(Math.random() * 8);
            if (curr < 1) {
                curr = 0;
            }

            Color color = new Color(255, 165, 100, 255);

            Vector2f vel = new Vector2f();

            if (target.getOrbit() != null
                    && target.getCircularOrbitRadius() > 0
                    && target.getCircularOrbitPeriod() > 0
                    && target.getOrbitFocus() != null) {
                float circumference = 2f * (float) Math.PI * target.getCircularOrbitRadius();
                float speed = circumference / target.getCircularOrbitPeriod();

                float dir = Misc.getAngleInDegrees(target.getLocation(), target.getOrbitFocus().getLocation()) + 90f;
                vel = Misc.getUnitVectorAtDegreeAngle(dir);
                vel.scale(speed / Global.getSector().getClock().getSecondsPerDay());
            }

            for (int i = 0; i < curr; i++) {
                float glowSize = 100f + 100f * (float) Math.random();
                float angle = (float) Math.random() * 360f;
                float dist = (float) Math.sqrt(Math.random()) * target.getRadius();

                float factor = 0.5f + 0.5f * (1f - (float) Math.sqrt(dist / target.getRadius()));
                glowSize *= factor;
                Vector2f loc = Misc.getUnitVectorAtDegreeAngle(angle);
                loc.scale(dist);
                Vector2f.add(loc, target.getLocation(), loc);

                Color c2 = Misc.scaleColor(color, factor);
                Misc.addHitGlow(target.getContainingLocation(), loc, vel, glowSize, c2);

                if (added == 0) {
                    dist = Misc.getDistance(loc, Global.getSector().getPlayerFleet().getLocation());
                    if (dist < (HyperspaceTerrainPlugin.STORM_STRIKE_SOUND_RANGE * 2f)) {
                        float volumeMult = 1f - (dist / (HyperspaceTerrainPlugin.STORM_STRIKE_SOUND_RANGE * 2f));
                        volumeMult = (float) Math.sqrt(volumeMult);
                        if (volumeMult > 0) {
                            Global.getSoundPlayer().playSound("ii_titan_explode_distant", 1f, 1f * volumeMult, loc, Misc.ZERO);
                        }
                    }
                }

                added++;
            }
        }
    }
}
