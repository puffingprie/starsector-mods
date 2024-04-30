package data.scripts.ix.skills;

import com.fs.starfarer.api.characters.ShipSkillEffect;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class SwordOfTheFleet {
	
	private static float SHIELD_BONUS_UNFOLD = 100f;
	private static float ENERGY_REGEN_BONUS = 50f;
	private static float ENERGY_ROF_BONUS = 10f;
	private static String IX_HULLMOD = "ix_ninth";

	public static class Level1 implements ShipSkillEffect {
		
		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			stats.getShieldUnfoldRateMult().modifyPercent(id, SHIELD_BONUS_UNFOLD);
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getShieldUnfoldRateMult().unmodify(id);
		}
		
		public String getEffectDescription(float level) {
			return "+" + (int)(SHIELD_BONUS_UNFOLD) + "% to raise shield speed";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.PILOTED_SHIP;
		}
	}
	
	public static class Level2 implements ShipSkillEffect {
		
		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			boolean isIX = false;
			boolean isRegenAlreadyBoosted = false;
			if (stats != null) {
				isIX = (stats.getVariant().getHullSpec().getManufacturer().equals("IX Battlegroup") 
						|| stats.getVariant().hasHullMod(IX_HULLMOD));
				isRegenAlreadyBoosted = (!stats.getEnergyAmmoRegenMult().isUnmodified() 
						&& !stats.getEnergyAmmoRegenMult().isNegative());
			}
			if (isIX && !isRegenAlreadyBoosted) stats.getEnergyAmmoRegenMult().modifyPercent(id, ENERGY_REGEN_BONUS);
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getEnergyAmmoRegenMult().unmodify(id);
		}
		
		public String getEffectDescription(float level) {
			return "+" + (int)(ENERGY_REGEN_BONUS) + "% energy weapon ammo regeneration rate when piloting IX Battlegroup ships without an ammo regeneration bonus";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.PILOTED_SHIP;
		}
	}
	
	public static class Level3 implements ShipSkillEffect {

		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			stats.getEnergyRoFMult().modifyPercent(id, ENERGY_ROF_BONUS);
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getEnergyRoFMult().unmodify(id);
		}
		
		public String getEffectDescription(float level) {
			return "+" + (int)(ENERGY_ROF_BONUS) + "% energy weapon rate of fire";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.PILOTED_SHIP;
		}
	}
}