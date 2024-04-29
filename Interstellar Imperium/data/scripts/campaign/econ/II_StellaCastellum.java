package data.scripts.campaign.econ;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.AICoreOfficerPlugin;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.characters.ImportantPeopleAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.econ.impl.OrbitalStation;
import com.fs.starfarer.api.impl.campaign.events.OfficerManagerEvent;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RemnantOfficerGeneratorPlugin;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public class II_StellaCastellum extends OrbitalStation {

    public boolean firstTick = true;
    public int lastDayChecked = 0;
    public static float DEFENSE_BONUS_CASTELLUM = 3f;

    // Those Variables indicate if structure is in process of construction or should progres futher with development
    public boolean haveStartedRecon = false;
    public boolean haveFinishedRecon = false;
    float yesterdayProgress = 0f;
    boolean haltedBuilding = false;

    // Those Variables indicate demand for tier 2 resources from Ashes
    public int demandRefinedMetal = 7;
    public int demandPurifiedTransplutonics = 6;

    @Override
    public void apply() {
        super.apply(false);
        // I added here first layer of protection so Babylon wont have the treatment of rebuilding itself
        if (market.getFactionId().equals("interstellarimperium")) {
            haveFinishedRecon = true;
            haveStartedRecon = true;
        }
        int size = 9;
        if (!Global.getSettings().getModManager().isModEnabled("aod_core")) {
            stationApplier(size);
        } else {
            if (haveFinishedRecon) {
                stationApplier(9);
            }
            // I added here second  layer of protection so Babylon wont have the treatment of rebuilding itself because sometimes it passes first i dont know why
            if (!haveStartedRecon && !market.getFactionId().equals("interstellarimperium")) {
                // Here we start true construction of Stella Castellum , downside is that player for 320 days have no station
                haveStartedRecon = true;
                float prevValue = this.getSpec().getBuildTime();
                removeStationEntityAndFleetIfNeeded();
                this.getSpec().setBuildTime(320);
                this.startBuilding();
                this.getSpec().setBuildTime(prevValue);
                yesterdayProgress = this.buildProgress;
            }
            if (!haveFinishedRecon && haveStartedRecon) {

                // We check if its still constructing if it is then we demand tier 2 resources to build further, demand here is much different than normal demand method
                // as here i got id of demand and i can manually erase it , when construction finish giving more control
                if (!isFunctional()) {
                    this.getDemand("refined_metal").getQuantity().modifyFlat("Construction1", demandRefinedMetal, "Stella Castellum Construction");
                    this.getDemand("purified_rare_metal").getQuantity().modifyFlat("Construction2", demandPurifiedTransplutonics, "Stella Castellum Construction");
                }
                if (yesterdayProgress != this.buildProgress) {
                    Pair<String, Integer> deficit = getMaxDeficit("refined_metal",
                            "purified_rare_metal");
                    if (deficit.two > 0) {
                        this.buildProgress = yesterdayProgress;
                        haltedBuilding = true;

                    } else {
                        haltedBuilding = false;
                    }
                    yesterdayProgress = this.buildProgress;

                }
            }

        }

    }

    @Override
    public void unapply() {
        super.unapply();

    }

    // i have moved here that method to avoid bloating code as this would simply repeating istelf
    private void stationApplier(int size) {
        modifyStabilityWithBaseMod();
        applyIncomeAndUpkeep(size);
        demand(Commodities.CREW, size);
        demand(Commodities.SUPPLIES, size);
        market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).modifyMult(getModId(), 1f + DEFENSE_BONUS_CASTELLUM, getNameForModifier());
        matchCommanderToAICore(aiCoreId);
        if (!isFunctional()) {
            supply.clear();
            unapply();
        } else {
            applyCRToStation();
        }
    }

    @Override
    protected void addPostDemandSection(TooltipMakerAPI tooltip, boolean hasDemand, IndustryTooltipMode mode) {
        if (mode != IndustryTooltipMode.NORMAL || (isFunctional())) {
            Color h = Misc.getHighlightColor();
            float opad = 10f;

            float cr = getCR();
            tooltip.addPara("Station combat readiness: %s", opad, h, "" + Math.round(cr * 100f) + "%");

            addStabilityPostDemandSection(tooltip, hasDemand, mode);

            addGroundDefensesImpactSection(tooltip, DEFENSE_BONUS_CASTELLUM, Commodities.SUPPLIES);
        }
        // Information about that station is being constructed
        if ((isBuilding() && haltedBuilding && haveStartedRecon)) {
            float opad = 10f;
            Color h = Misc.getNegativeHighlightColor();
            tooltip.addPara("Due to deficit of resources needed to build station construction has been halted!", h, opad);
        }

    }

    @Override
    public String getBuildOrUpgradeProgressText() {
//		float f = buildProgress / spec.getBuildTime();
//		return "" + (int) Math.round(f * 100f) + "%";
        if (isBuilding()) {
            //return "" + (int) Math.round(Misc.getMarketSizeProgress(market) * 100f) + "%";
            if ((buildTime - buildProgress) <= 1) {
                return "Building " + (int) (buildTime - buildProgress) + ": day left";
            }
            return "Building " + (int) (buildTime - buildProgress) + ": days left";
        }

        return super.getBuildOrUpgradeProgressText();
    }

    @Override
    protected int getBaseStabilityMod() {
        return 4;
    }

    @Override
    public boolean isAvailableToBuild() {
        // Here uses Ashes of The Domain API to determine if it was researched or not
        if (Global.getSettings().getModManager().isModEnabled("aod_core")) {
            Map<String, Boolean> researchSaved = (HashMap<String, Boolean>) Global.getSector().getPersistentData().get("researchsaved");
            if (researchSaved != null) {
                Boolean rtn = researchSaved.get(id);
                if (rtn == null) {
                    rtn = false;
                }
                return rtn;
            }
            return false;
        }
        return false;
    }

    @Override
    public boolean showWhenUnavailable() {
        return false;
    }

    // To avoid nasty bug of dupping stations we block shutdown of Stella Castellum as influencing shutdown is nearly impossible to do
    @Override
    public boolean canShutDown() {
        return false;
    }

    @Override
    public String getCanNotShutDownReason() {
        return "Due to being massive structure this station must be downgraded first to be shutten down";
    }

    @Override
    protected void matchCommanderToAICore(String aiCore) {
        if (stationFleet == null) {
            return;
        }

        PersonAPI commander = null;
        if (Commodities.ALPHA_CORE.equals(aiCore)) {
            if (market.getFactionId().contentEquals("interstellarimperium")) {
                ImportantPeopleAPI ip = Global.getSector().getImportantPeople();
                commander = ip.getPerson("ii_minerva");
            } else {
                AICoreOfficerPlugin plugin = Misc.getAICoreOfficerPlugin(Commodities.ALPHA_CORE);
                commander = plugin.createPerson(Commodities.ALPHA_CORE, Factions.REMNANTS, null);
                if (stationFleet.getFlagship() != null) {
                    RemnantOfficerGeneratorPlugin.integrateAndAdaptCoreForAIFleet(stationFleet.getFlagship());
                }
            }
        } else {
            if (stationFleet.getFlagship() != null) {
                int level = getHumanCommanderLevel();
                PersonAPI current = stationFleet.getFlagship().getCaptain();
                if (level > 0) {
                    if (current.isAICore() || current.getStats().getLevel() != level) {
                        commander = OfficerManagerEvent.createOfficer(
                                Global.getSector().getFaction(market.getFactionId()), level, true);
                    }
                } else {
                    if (stationFleet.getFlagship() == null || stationFleet.getFlagship().getCaptain() == null
                            || !stationFleet.getFlagship().getCaptain().isDefault()) {
                        commander = Global.getFactory().createPerson();
                    }
                }
            }

        }

        if (commander != null) {
            if (stationFleet.getFlagship() != null) {
                stationFleet.getFlagship().setCaptain(commander);
                stationFleet.getFlagship().setFlagship(false);
            }
        }
    }

    // Here entire method has been rewriten to me to match vanila one with my tweaks so we are ensured that everything spawns correctly
    @Override
    public void finishBuildingOrUpgrading() {
        building = false;
        buildProgress = 0;
        buildTime = 1f;
        if (upgradeId == null) {
            haveFinishedRecon = true;
            demandRefinedMetal = 0;
            demandPurifiedTransplutonics = 0;
            removeStationEntityAndFleetIfNeeded();

            // I added second type of "Babylon" station that would have custom descrp etc and here we are basically replacing model of normal station with
            // Babylon one
            SectorEntityToken entity = market.getContainingLocation().addCustomEntity(
                    null, market.getName() + " Station", "ii_station_babylon_aotd", market.getFactionId());

            SectorEntityToken primary = market.getPrimaryEntity();
            entity.setCircularOrbitPointingDown(primary, 45, 400, 17);
            market.getConnectedEntities().add(entity);
            entity.setMarket(market);
            ensureStationEntityIsSetOrCreated();
            this.reapply();
            haveFinishedRecon = true;
            buildingFinished();
            //As i said here comes in handy that we manually assigned demand as now its very easy to just erase it
            this.getDemand("refined_metal").getQuantity().unmodifyFlat("Construction1");
            this.getDemand("purified_rare_metal").getQuantity().unmodifyFlat("Construction2");
            reapply();

        } else {
            // Those two loops are very much essential , as vanila method is not accurate with it in 100%
            // Basically we remove all traces of station from market, map etc then we replace industry manually
            // So game treats like station is being build first time and spawns correctly so we avoid bug that space station is not visible
            for (SectorEntityToken s : market.getConnectedEntities()) {
                if (s.getCustomDescriptionId() == null) {
                    continue;
                }
                if (s.getCustomDescriptionId().equals("ii_station_babylon_aotd")) {
                    market.getConnectedEntities().remove(s);
                    s.setMarket(null);
                    s.clearTags();
                    s.setExpired(true);
                    Misc.fadeAndExpire(s, 0.1f);
                    break;
                }
            }
            for (SectorEntityToken s : market.getConnectedEntities()) {
                if (s.getCustomEntityType() == null) {
                    continue;
                }
                if (s.getCustomEntityType().equals(Entities.STATION_BUILT_FROM_INDUSTRY)) {
                    market.getConnectedEntities().remove(s);
                    s.setMarket(null);
                    s.clearTags();
                    s.setExpired(true);
                    Misc.fadeAndExpire(s, 0.1f);
                    break;
                }
            }
            market.removeIndustry(getId(), null, true);
            market.addIndustry(upgradeId);
            BaseIndustry industry = (BaseIndustry) market.getIndustry(upgradeId);
            industry.setAICoreId(getAICoreId());
            industry.setImproved(isImproved());
            industry.reapply();

        }
    }
}
