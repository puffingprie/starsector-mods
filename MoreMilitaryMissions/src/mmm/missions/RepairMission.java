package mmm.missions;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.econ.EconomyAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import mmm.Utils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.magiclib.util.MagicSettings;

import java.awt.*;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.*;

public class RepairMission extends OrbitalMissionBase {
    private static final Logger log = Global.getLogger(RepairMission.class);
    static {
        if (Utils.DEBUG) {
            log.setLevel(Level.ALL);
        }
    }
    public enum Stage {
        ACCEPTED,
        COMPLETED,
        FAILED
    }

    public static final String MISSION_ID = "mmm_rm";
    // The time limit, computed as the ratio of days repaired: disruption days * REPAIR_RATIO * TIME_LIMIT_RATIO
    public static final float TIME_LIMIT_RATIO = 0.75f;
    // Minimum/maximum time limit. Also the minimum repair that can be done on a station; you don't get the mission if
    // the station is disrupted for less than MIN_TIME_LIMIT_DAYS / REPAIR_RATIO days.
    public static final float MIN_TIME_LIMIT_DAYS = 15f;
    public static final float MAX_TIME_LIMIT_DAYS = 60f;
    // Repair cost per fleet point in terms of commodities when repairing at maximum disruption.
    public static final Map<String, Integer> REPAIR_COST_PER_FP = new TreeMap<>();
    static {
        REPAIR_COST_PER_FP.put(Commodities.SUPPLIES, 8);
        REPAIR_COST_PER_FP.put(Commodities.HEAVY_MACHINERY, 2);
        REPAIR_COST_PER_FP.put(Commodities.METALS, 11);
        REPAIR_COST_PER_FP.put(Commodities.RARE_METALS, 1);
    }

    // Repair that can be done on a station, as a ratio of the remaining disruptions days.
    public static final float REPAIR_RATIO = MagicSettings.getFloat(MOD_ID, "RmRepairRatio");
    // You will be paid this profit margin above the base price.
    public static final float PROFIT_MARGIN = MagicSettings.getFloat(MOD_ID, "RmProfitMargin");

    public Industry industry = null;
    // Map<commodity ID, units>
    public Map<String, Integer> repair_cost = new TreeMap<>();
    public String station_name = "";
    // star fortress, battlestation, etc
    public String station_desc = "";

    // If the market is eligible for this mission right now, returns the station Industry.
    public static Industry getStationIndustryIfEligible(MarketAPI market) {
        // Check to ensure that the market has a station industry and fleet and is functional.
        Industry industry = Misc.getStationIndustry(market);
        if (industry == null) {
//            log.debug(market.getName() + " market has no station");
            return null;
        }
        if (industry.getDisruptedDays() * REPAIR_RATIO < MIN_TIME_LIMIT_DAYS) {
//            log.debug(MessageFormat.format("Station is disrupted for only {0} days.",
//                    industry.getDisruptedDays()));
            return null;
        }
        return industry;
    }

    @Override
    public boolean shouldShowAtMarket(MarketAPI market) {
//        log.debug("shouldShowAtMarket called for " + market.getName());
        return getStationIndustryIfEligible(market) != null;
    }

    @Override
    protected boolean create(MarketAPI createdAt, boolean barEvent) {
        String id = getMissionId();
//        log.debug((isBarEvent() ? "bar" : "contact") + " event; create called with mission_id=" + id);
        if (!id.equals(MISSION_ID)) {
            log.error("Unexpected mission id: " + id);
            return false;
        }

        industry = getStationIndustryIfEligible(createdAt);
        if (industry == null) return false;  // market not eligible

        if (barEvent) createBarGiver(createdAt);

        PersonAPI person = getPerson();
        if (person == null || person.getMarket() != createdAt) return false;  // sanity check

        // Make sure the faction matches
        if (person.getFaction() != createdAt.getFaction()) {
            log.error(MessageFormat.format(
                    "{0} is in {1} faction instead of {2}",
                    person.getNameString(), person.getFaction().getDisplayName(),
                    createdAt.getFaction().getDisplayName()));
            return false;
        }

        // Only the station commander can give out this mission.
        if (!person.getPostId().equals(Ranks.POST_STATION_COMMANDER)) {
            log.error(person.getNameString() + " is not the station commander");
            return false;
        }

        // This line, among other things, prevents the same person from accepting multiple missions, which effectively
        // prevents concurrent missions on the same market as long as the same person is picked.
        final String KEY = "$mmm_rm_ref";
        if (!setPersonMissionRef(person, KEY)) {
            log.debug(KEY + " already set for " + person.getNameString());
            return false;
        }

        // Computes our mission data.
        float max_repair_days = industry.getSpec().getBuildTime() * 0.5f * REPAIR_RATIO;
        float repair_days = industry.getDisruptedDays() * REPAIR_RATIO;
        float cost_ratio = repair_days / max_repair_days;
        float time_limit_days =
                Math.min(Math.max(repair_days * TIME_LIMIT_RATIO, MIN_TIME_LIMIT_DAYS), MAX_TIME_LIMIT_DAYS);

        // We can find the station's fleet point from the variant of the station.
        String variantId;
        try {
            JSONObject json = new JSONObject(industry.getSpec().getData());
            variantId = json.getString("variant");
            station_name = station_desc = json.getString("fleetName");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, variantId);
        if (member == null) {
            log.error("Failed to create FleetMemberAPI for station variant " + variantId);
            return false;
        }
        int fp = member.getFleetPointCost();

        CampaignFleetAPI fleet = Misc.getStationBaseFleet(createdAt);
        if (fleet != null) {
            station_name = fleet.getNameWithFaction();
        }

        EconomyAPI econ = Global.getSector().getEconomy();
        float base_price = 0;
        for (Map.Entry<String, Integer> entry : REPAIR_COST_PER_FP.entrySet()) {
            CommoditySpecAPI spec = econ.getCommoditySpec(entry.getKey());
            // Round to the nearest 10s
            int cost = myGetRoundNumber(fp * entry.getValue() * cost_ratio);
            repair_cost.put(entry.getKey(), cost);
            base_price += spec.getBasePrice() * cost;
        }
        log.info(MessageFormat.format(
                "Station variant {0} has fp={1}, max_repair_days={2}, repair_days={3}, cost_ratio={4}" +
                        ", time_limit_days={4}, repair_cost={5}",
                variantId, fp, max_repair_days, repair_days, cost_ratio, time_limit_days, repair_cost));

        // set our starting, success and failure stages
        setStartingStage(Stage.ACCEPTED);
        setSuccessStage(Stage.COMPLETED);
        setFailureStage(Stage.FAILED);
        setStageOnMemoryFlag(Stage.COMPLETED, person, "$mmm_rm_completed");
        setTimeLimit(Stage.FAILED, time_limit_days, null);

        addTag(Tags.INTEL_MILITARY);
        ensurePersonIsInCommDirectory(createdAt, person);
        //setPersonDoGenericPortAuthorityCheck(person);

        // mission rewards
        setCreditReward(Math.round(base_price + base_price * PROFIT_MARGIN));
        setRepPersonChangesMedium();
        setRepFactionChangesLow();

        // needed by rules.csv
        makeImportant(person, "$mmm_rm_needsCommodity", Stage.ACCEPTED);
        return true;
    }

    @Override
    protected void endSuccessImpl(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
        // Same setting as DefenseMission
        addPotentialContact(this, dialog, 0.67f, 0.36f);
        int repair_days = (int) Math.round(Math.ceil(industry.getDisruptedDays() * REPAIR_RATIO));
        industry.setDisrupted(industry.getDisruptedDays() - repair_days);

        String reduction_str = "reduced by " + repair_days + (repair_days > 1 ? " days" : " day");
        TextPanelAPI text = dialog.getTextPanel();
        text.setFontSmallInsignia();
        text.addParagraph(station_name + " disruption " + reduction_str, Misc.getGrayColor());
        text.highlightInLastPara(Misc.getPositiveHighlightColor(), reduction_str);
    }

    @Override
    protected void updateInteractionDataImpl() {
        // These are used in rules.csv
        set("$mmm_rm_station_desc", station_desc);
        set("$mmm_rm_totalPrice", Misc.getDGSCredits(getCreditsReward()));
        set("$mmm_rm_supplies", Misc.getWithDGS(repair_cost.get(Commodities.SUPPLIES)));
        set("$mmm_rm_heavy_machinery", Misc.getWithDGS(repair_cost.get(Commodities.HEAVY_MACHINERY)));
        set("$mmm_rm_metals", Misc.getWithDGS(repair_cost.get(Commodities.METALS)));
        set("$mmm_rm_rare_metals", Misc.getWithDGS(repair_cost.get(Commodities.RARE_METALS)));
        if (timeLimit != null) {
            set("$mmm_rm_time_limit", Misc.getWithDGS(timeLimit.days - elapsed));
        }

        NumberFormat format = NumberFormat.getPercentInstance();
        format.setMinimumFractionDigits(0);
        set("$mmm_rm_profit_margin", format.format(PROFIT_MARGIN));

        boolean enough = true;
        for (Map.Entry<String, Integer> entry : repair_cost.entrySet()) {
            if (!playerHasEnough(entry.getKey(), entry.getValue())) {
                enough = false;
                break;
            }
        }
        set("$mmm_rm_playerHasEnough", enough);
    }

    // where on the map the intel screen tells us to go
    @Override
    public SectorEntityToken getMapLocation(SectorMapAPI map) {
        return industry.getMarket().getPrimaryEntity();
    }

    // mission name on intel screen
    @Override
    public String getBaseName() { return "Repair Materials Delivery"; }

    @Override
    protected void addBulletPointsPre(TooltipMakerAPI info, Color tc, float initPad, ListInfoMode mode) {
        // Note that ListInfoMode.IN_DESC is intel information on RHS with person portrait
        if (mode != ListInfoMode.IN_DESC || currentStage != Stage.ACCEPTED) return;

        EconomyAPI econ = Global.getSector().getEconomy();
        for (Map.Entry<String, Integer> entry : repair_cost.entrySet()) {
            String name = econ.getCommoditySpec(entry.getKey()).getLowerCaseName();
            String hl = Misc.getWithDGS(entry.getValue());
            info.addPara(MessageFormat.format("acquire {0} {1}.", hl, name),
                    0, tc, Misc.getHighlightColor(), hl);
        }
    }
}
