package mmm.missions;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.PersonImportance;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.econ.CommodityIconCounts;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.missions.ProcurementMission;
import com.fs.starfarer.api.impl.campaign.submarkets.BaseSubmarketPlugin;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import mmm.Utils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.magiclib.util.MagicSettings;

import java.util.Collections;
import java.util.Map;

// Changes how procurement mission works for player contacts. The main thing is that the procurement amount is the
// higher of vanilla amount and market deficit amount. Also, doing the mission resolves the deficit (unless smuggling).
public class MmmProcurementMission extends ProcurementMission {
    private static final String MOD_ID = Utils.MOD_ID;
    private static final Logger log = Global.getLogger(MmmProcurementMission.class);
    static {
        if (MagicSettings.getBoolean(MOD_ID, "MmmDebug")) {
            log.setLevel(Level.ALL);
        }
    }

    // constants
    public static final String MISSION_ID = "proCom";
    public static final float TRADE_IMPACT_DAYS = BaseSubmarketPlugin.TRADE_IMPACT_DAYS;

    // Memory flags
    public static final String ACTIVE_PREFIX = "$mmm_comActive_";
    // Prevents cheap commodity mission soon after a procurement
    public static final String CHEAP_COM_TIMEOUT_PREFIX = "$mmm_cheapCom_timeout_";
    public static final String PRO_COM_TIMEOUT_PREFIX = "$mmm_proCom_timeout_";

    public boolean barEvent = false;

    public static boolean IsVanillaLogic(PersonAPI person) {
        return !person.getFaction().getId().equals(Factions.INDEPENDENT) &&
                !person.getMarket().getFactionId().equals(person.getFaction().getId());
    }

    @Override
    protected boolean create(MarketAPI createdAt, boolean barEvent) {
//        log.debug((barEvent ? "bar" : "contact") + " event; create called with mission_id=" + getMissionId());
        this.barEvent = barEvent;

        // Make no change to bar events, except that we want to lock the delivery market for the commodity.
        // Also make no change if the quest giver is not independent, and their faction does not match the faction of
        // the market.
        boolean vanilla_logic;
        if (barEvent) {
            vanilla_logic = true;
        } else {
            if (createdAt == null || getPerson() == null || getPerson().getMarket() != createdAt) {
                return false;  // Sanity check
            }
            vanilla_logic = IsVanillaLogic(getPerson());
        }

        if (vanilla_logic) {
            boolean created = super.create(createdAt, true);
            if (!created || commodityId == null || deliveryMarket == null) {
                return false;
            }
            return setMarketMissionRef(deliveryMarket, ACTIVE_PREFIX + commodityId);
        }

        if (!getMissionId().equals(MISSION_ID)) {
            log.error("Unexpected mission id: " + getMissionId());
            return false;
        }

        PersonAPI person = getPerson();
        if (!setPersonMissionRef(person, "$proCom_ref")) {
            return false;
        }

        boolean preferExpensive = getQuality() >= PersonImportance.HIGH.getValue();
        variation = Variation.LOCAL;

        // Normal contacts requires legal commodities. Underworld contacts allows all commodities on pirate worlds, and
        // prefer illegal on non-pirate worlds
        boolean preferIllegal;
        boolean requireLegal;
        if (person.hasTag(Tags.CONTACT_UNDERWORLD)) {
            preferIllegal = !createdAt.getFactionId().equals(Factions.PIRATES);
            requireLegal = false;
        } else {
            preferIllegal = false;
            requireLegal = true;
        }
//        log.debug(MessageFormat.format("preferIllegal={0}: requireLegal={1}, preferExpensive={2}",
//                preferIllegal, requireLegal, preferExpensive));

        // First pick a commodity in deficit that's not in timeout, based on the value of the deficit. See
        // ProcurementMission.create
        MemoryAPI memory = createdAt.getMemoryWithoutUpdate();
        WeightedRandomPicker<CommodityOnMarketAPI> picker = new WeightedRandomPicker<>(genRandom);
        for (CommodityOnMarketAPI com : createdAt.getAllCommodities()) {
            if (com.isMeta()) continue;
            if (com.isNonEcon()) continue;
            if (com.isPersonnel()) continue;
            if (requireLegal && com.isIllegal()) continue;
            if (!com.isIllegal() && com.getMaxDemand() <= 0) continue;

            // Check whether the commodity is already active or is in timeout on this market.
            if (memory.contains(PRO_COM_TIMEOUT_PREFIX + com.getId()) || memory.contains(ACTIVE_PREFIX + com.getId())) {
                continue;
            }

            CommodityIconCounts counts = new CommodityIconCounts(com);
            if (counts.extra > 0 || counts.deficit <= 0) continue;  // Deficit and no surplus

            CommoditySpecAPI spec = com.getCommodity();
            float weight = counts.deficit * spec.getBasePrice() * spec.getEconUnit();
//            weight = weight * weight;
            boolean expensive = com.getCommodity().getTags().contains(Commodities.TAG_EXPENSIVE);
            if (preferExpensive && expensive) weight *= 2;
            if (preferIllegal && com.isIllegal()) weight *= 10;
//            log.debug(MessageFormat.format(
//                    "Commodity {0}: deficits={1}, basePrice={2}, econUnits={3}, expensive={4}, weight={5}",
//                    com.getId(), counts.deficit, spec.getBasePrice(), spec.getEconUnit(), expensive, weight));
            picker.add(com, weight);
        }
        if (picker.isEmpty()) {
//            log.debug("no matching commodities in deficit");
            return false;
        }

        CommodityOnMarketAPI com = picker.pick();
        CommodityIconCounts counts = new CommodityIconCounts(com);
        CommoditySpecAPI spec = com.getCommodity();

        // Lock the market and prevent it from accepting other proCom/cheapCom missions while this mission is active.
        if (!setMarketMissionRef(createdAt, ACTIVE_PREFIX + com.getId())) return false;

        // Below is adapted from ProcurementMission.create(), where only Variation.LOCAL variation is possible, and
        // the quantity is the max of the vanilla quantity and (deficit X econ units).
        deliveryMarket = createdAt;
        commodityId = com.getId();

        float value = MIN_BASE_VALUE + (MAX_BASE_VALUE - MIN_BASE_VALUE) * getQuality();
        quantity = getRoundNumber(value / com.getCommodity().getBasePrice());
        if (com.isIllegal()) {
            quantity *= ILLEGAL_QUANTITY_MULT;
        }

        // Compute amount, which should be the max of vanilla units (computed above), and deficit amount.
        quantity = Math.max(quantity, Math.round(counts.deficit * spec.getEconUnit()));
        quantity = Math.max(quantity, 10);

        // Compute price.
        pricePerUnit = (int) (com.getMarket().getSupplyPrice(com.getId(), quantity, true) / (float) quantity *
                BASE_PRICE_MULT / getRewardMult());
        pricePerUnit = Math.max(getRoundNumber(pricePerUnit), 2);

        deliveryContact = person;

        setPersonDoGenericPortAuthorityCheck(deliveryContact);
        makeImportant(deliveryContact, "$proCom_needsCommodity", Stage.TALK_TO_PERSON);

        setStartingStage(Stage.TALK_TO_PERSON);
        setSuccessStage(Stage.COMPLETED);
        setFailureStage(Stage.FAILED);

        setStageOnMemoryFlag(Stage.COMPLETED, deliveryContact, "$proCom_completed");
        setTimeLimit(Stage.FAILED, MISSION_DAYS, null);

        if (getQuality() < 0.5f) {
            setRepFactionChangesVeryLow();
        } else {
            setRepFactionChangesLow();
        }
        setRepPersonChangesMedium();

        return true;
    }

    @Override
    public void acceptImpl(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
        super.acceptImpl(dialog, memoryMap);
        OrbitalMissionBase.acceptCommon(MISSION_ID, getPerson());
    }

    @Override
    protected void endSuccessImpl(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
        super.endSuccessImpl(dialog, memoryMap);

        // Allows player faction contact at 40% probability
        if (barEvent && getPerson().getFaction().isPlayerFaction()) {
            OrbitalMissionBase.addPotentialContact(this, dialog, 1f, 0.79f);
        }

        // Apply trade mods
        boolean smuggling = deliveryMarket.getCommodityData(commodityId).isIllegal() &&
                !deliveryMarket.getFaction().getId().equals(Factions.PIRATES) &&
                deliveryContact.getFaction().getId().equals(Factions.PIRATES);
        OrbitalMissionBase.applyTradeMods(deliveryMarket, smuggling, true, true,
                Collections.singletonMap(commodityId, quantity), null);

        // Set mission timeouts such that a successful cheapCom isn't followed by an immediate proCom or vice versa for
        // the same commodity.
        MemoryAPI memory = deliveryMarket.getMemoryWithoutUpdate();
        OrbitalMissionBase.extendFlag(memory, PRO_COM_TIMEOUT_PREFIX + commodityId, 1f);
        OrbitalMissionBase.extendFlag(memory, CHEAP_COM_TIMEOUT_PREFIX + commodityId, TRADE_IMPACT_DAYS);
    }
}
