package data.campaign.skills;

import com.fs.starfarer.api.campaign.FleetDataAPI;
import com.fs.starfarer.api.characters.*;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.skills.BaseSkillEffectDescription;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import data.utils.I18nUtil;

import java.util.ArrayList;
import java.util.List;

public class FM_ShieldExpand_skill {

    public static float SHIELD_ARC_EXPAND = 60f;
    public static float SHIELD_BONUS_RATE = 20f;

    public static class Level1 extends BaseSkillEffectDescription implements AfterShipCreationSkillEffect, FleetTotalSource {

        @Override
        public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
//            if (ship.getShield().getType() == ShieldAPI.ShieldType.NONE ||
//            ship.getShield().getType() == ShieldAPI.ShieldType.PHASE)return;
//            if (!isCivilian(ship.getMutableStats()) && ship.getVariant().hasHullMod("FantasyBasicMod")) {
//                float effect = computeBonusForFMCombatShips(ship.getMutableStats(),"FM_ShieldExpand_skill",SHIELD_ARC_EXPAND);
//                ship.getMutableStats().getShieldArcBonus().modifyFlat(id,effect);
//            }
        }

        @Override
        public void unapplyEffectsAfterShipCreation(ShipAPI ship, String id) {
//            ship.getMutableStats().getShieldArcBonus().unmodifyFlat(id);
        }

        @Override
        public FleetTotalItem getFleetTotalItem() {
//            final CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
//            final MutableCharacterStatsAPI stats = Global.getSector().getPlayerStats();
//            FleetTotalItem item = new FleetTotalItem();
//            item.label = "Combat ship ordnance points";
//            if (USE_RECOVERY_COST) {
//                item.label = I18nUtil.getString("skill","FM_ShiledExpandSkillItemLable");
//            }
//            item.value = "" + (int) getFMCombatShipDP(fleet.getFleetData(),stats);
//            item.sortOrder = 100;
//
//            item.tooltipCreator = getTooltipCreator(new TooltipCreatorSkillEffectPlugin() {
//                public void addDescription(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
//                    float opad = 10f;
//                    tooltip.addPara(I18nUtil.getString("skill","FM_ShiledExpandSkillItemTooltip"), 0f);
//                }
//                public List<FleetMemberPointContrib> getContributors() {
//                    return getFMCombatShipDPDetail(fleet.getFleetData(), stats);
//                }
//            });
//
//            return item;
            return getCombatOPTotal();
        }

        public void createCustomDescription(MutableCharacterStatsAPI stats, SkillSpecAPI skill,
                                            TooltipMakerAPI info, float width) {
            init(stats, skill);

            FleetDataAPI data = getFleetData(null);
            //float effect = computeBonusForFMCombatShips(data, stats, "FM_ShieldExpand_skill", SHIELD_ARC_EXPAND);
            float effect = computeAndCacheThresholdBonus(data, stats, "FM_ShieldExpand_skill", SHIELD_BONUS_RATE, ThresholdBonusType.OP);
            info.addPara(I18nUtil.getString("skill", "FM_ShiledExpandSkillInfo"), 0f, hc, hc,
                    (int) (effect) + "%",
                    (int) (SHIELD_BONUS_RATE) + "%");
            addOPThresholdInfo(info, data, stats, OP_THRESHOLD);
        }

        @Override
        public void apply(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id, float level) {
            if (!isCivilian(stats) && stats.getVariant().hasHullMod("FantasyBasicMod")) {
                //float effect = computeBonusForFMCombatShips(stats,"FM_ShieldExpand_skill",SHIELD_ARC_EXPAND);
                float effect = computeAndCacheThresholdBonus(stats, "FM_ShieldExpand_skill", SHIELD_BONUS_RATE, ThresholdBonusType.OP);
                //stats.getShieldArcBonus().modifyFlat(id,effect);
                stats.getShieldArcBonus().modifyMult(id, 1f + effect * 0.01f, "FM_ShiledExpandSkillDes");
            }

        }

        @Override
        public void unapply(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
            stats.getShieldArcBonus().unmodifyMult(id);
        }

        protected float computeBonusForFMCombatShips(MutableShipStatsAPI stats,
                                                     String key, float maxBonus) {
            FleetDataAPI data = getFleetData(stats);
            MutableCharacterStatsAPI cStats = getCommanderStats(stats);
            return computeBonusForFMCombatShips(data, cStats, key, maxBonus);
        }

        protected float computeBonusForFMCombatShips(FleetDataAPI data, MutableCharacterStatsAPI cStats,
                                                     String key, float maxBonus) {
//		if (key.equals("pc_peak")) {
//			System.out.println("efwfwefwe");
//		}
            if (data == null) return maxBonus;
            if (cStats.getFleet() == null) return maxBonus;

            Float bonus = (Float) data.getCacheClearedOnSync().get(key);
            if (bonus != null) return bonus;

            float currValue = getFMCombatShipDP(data, cStats);
            float threshold = OP_THRESHOLD;

            bonus = getThresholdBasedRoundedBonus(maxBonus, currValue, threshold);

            data.getCacheClearedOnSync().put(key, bonus);
            return bonus;
        }

        public static float getFMCombatShipDP(FleetDataAPI data, MutableCharacterStatsAPI stats) {
            float op = 0;
            for (FleetMemberAPI curr : data.getMembersListCopy()) {
                if (curr.isMothballed()) continue;
                if (isCivilian(curr)) continue;
                if (!curr.getVariant().hasHullMod("FantasyBasicMod")) continue;
                op += getPoints(curr, stats);
            }
            return Math.round(op);
        }

        public static List<FleetMemberPointContrib> getFMCombatShipDPDetail(FleetDataAPI data, MutableCharacterStatsAPI stats) {
            List<FleetMemberPointContrib> result = new ArrayList<FleetMemberPointContrib>();
            for (FleetMemberAPI curr : data.getMembersListCopy()) {
                if (curr.isMothballed()) continue;
                if (isCivilian(curr)) continue;
                if (!curr.getVariant().hasHullMod("FantasyBasicMod")) continue;
                int pts = Math.round(getPoints(curr, stats));
                result.add(new FleetMemberPointContrib(curr, pts));
            }
            return result;
        }
    }
}
