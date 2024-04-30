package data.hullmods.ix;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.listeners.WeaponBaseRangeModifier;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import data.scripts.ix.PanopticInterfaceUtil;

public class PanopticInterfaceTactical extends BaseHullMod {
	
	private static float BONUS_SPEED_FRIGATE = 15f;
	private static float BONUS_SPEED_DESTROYER = 10f;
	private static float BONUS_SPEED_CRUISER = 5f;
	private static float BONUS_SPEED_CAPITAL = 5f;
	private static float BONUS_DAMAGE_RANK_1 = 10f;
	private static float BONUS_RANGE_RANK_2 = 200f;
	private static float CR_PENALTY_MAX = 30f;
	
	public static float RANGE_THRESHOLD = 700f;
	public static float RANGE_BONUS = 200f;
		
	private static String SKILL_ID = "ix_sword_of_the_fleet";
	
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		if (PanopticInterfaceUtil.hasConflictMod(stats.getVariant())) return;
		float speedBonus = getspeedBonusForHull(hullSize);
		PersonAPI person = null;
		stats.getMaxSpeed().modifyFlat(id, speedBonus);
		if (stats.getFleetMember() != null) person = stats.getFleetMember().getCaptain();
		if (hasRank1(person)) stats.getEnergyWeaponDamageMult().modifyPercent(id, BONUS_DAMAGE_RANK_1);
		if (stats.getFleetMember() == null || stats.getFleetMember().getOwner() != 0) return;
		float crPenalty = PanopticInterfaceUtil.getReadinessPenalty(stats.getFleetMember(), hullSize);
		stats.getMaxCombatReadiness().modifyFlat(id, -crPenalty * 0.01f, "Panoptic Interface");
	}
	
	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		PersonAPI person = ship.getCaptain();
		if (person == null || !hasRank2(person)) return;
		ship.addListener(new TacticalInterfaceRangeMod());
	}
	
	public static class TacticalInterfaceRangeMod implements WeaponBaseRangeModifier {
		public TacticalInterfaceRangeMod() {}
		public float getWeaponBaseRangePercentMod(ShipAPI ship, WeaponAPI weapon) {
			return 0f;
		}
		public float getWeaponBaseRangeMultMod(ShipAPI ship, WeaponAPI weapon) {
			return 1f;
		}
		public float getWeaponBaseRangeFlatMod(ShipAPI ship, WeaponAPI weapon) {
			if (weapon.isBeam()) {
				float range = weapon.getSpec().getMaxRange();
				if (range > RANGE_THRESHOLD) return 0f;
				else if (range <= RANGE_THRESHOLD) {
					if (range + RANGE_BONUS > RANGE_THRESHOLD) return RANGE_THRESHOLD - range;
					else return RANGE_BONUS;
				}
			}
			return 0f;
		}
	}
	
	@Override
	public boolean shouldAddDescriptionToTooltip(HullSize hullSize, ShipAPI ship, boolean isForModSpec) {
		return true;
	}
	
	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		PersonAPI person = ship.getCaptain();
		Color line1Color = Misc.getHighlightColor();
		Color line2Color = Misc.getHighlightColor();
		Color line3Color = Misc.getHighlightColor();
		String a1 = "Active: ";
		String a2 = "Active: ";
		String a3 = "Active: ";
		
		if (person == null || !hasRank1(person)) {
			line2Color = Misc.getGrayColor();
			line3Color = Misc.getGrayColor();
			a2 = "Inactive: ";
			a3 = "Inactive: ";
		}
		else if (!hasRank2(person)) {
			line3Color = Misc.getGrayColor();
			a3 = "Inactive: ";
		}
		
		float speedBonus = getspeedBonusForHull(hullSize);
		String s1 = a1 + "Increase speed by " + (int) speedBonus + " su/second";
		String s2 = a2 + "+" + (int)BONUS_DAMAGE_RANK_1 + "% energy weapon damage";
		String s3 = a3 + "+" + (int) RANGE_BONUS + " su beam weapon base range, up to " + (int) (RANGE_THRESHOLD + RANGE_BONUS) + " su";
		
		tooltip.addPara(s1, line1Color, 10f);
		tooltip.addSpacer(-10f); 
		tooltip.addPara(s2, line2Color, 10f);
		tooltip.addSpacer(-10f);
		tooltip.addPara(s3, line3Color, 10f);
		
		if (ship == null) return;
		String s = "";
		float crPenalty = 0f;
		if (PanopticInterfaceUtil.hasConflictMod(ship.getVariant())) s = "Warning: Incompatible AI system present. Interface is inactive.";
		else if (Global.getSector() != null) {
			crPenalty = PanopticInterfaceUtil.getReadinessPenalty(ship.getFleetMember(), hullSize);
			int fleetCount = PanopticInterfaceUtil.getPanopticShipCount(ship.getFleetMember());
			s = "Combat Readiness reduced by " + (int) crPenalty + "%";
			String word = fleetCount > 1 ? "ships" : "ship";
			String append = " from " + fleetCount + " interfaced " + word;
			s += append;
		}
		if (!s.isEmpty()) {
			if (crPenalty == 0f) {
				s = "CR penalty is currently negated throughout the fleet";
				tooltip.addPara(s, Misc.getPositiveHighlightColor(), 10f);
			}
			else tooltip.addPara(s, Misc.getNegativeHighlightColor(), 10f);
		}
	}
	
	private boolean hasRank1(PersonAPI person) {
		if (person == null) return false;
		return person.getStats().hasSkill(SKILL_ID);
	}
	
	private boolean hasRank2(PersonAPI person) {
		if (person == null) return false;
		if (!hasRank1(person)) return false;
		return person.getStats().getSkillLevel(SKILL_ID) > 1;
	}
	
	private float getspeedBonusForHull(HullSize hullSize) {
		float speedBonus = 0f;
		if (hullSize.equals(HullSize.FRIGATE)) speedBonus = BONUS_SPEED_FRIGATE;
		else if (hullSize.equals(HullSize.DESTROYER)) speedBonus = BONUS_SPEED_DESTROYER;
		else if (hullSize.equals(HullSize.CRUISER)) speedBonus = BONUS_SPEED_CRUISER;
		else if (hullSize.equals(HullSize.CAPITAL_SHIP)) speedBonus = BONUS_SPEED_CAPITAL;
		return speedBonus;
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) PanopticInterfaceUtil.PENALTY_FRIGATE;
		if (index == 1) return "" + (int) PanopticInterfaceUtil.PENALTY_DESTROYER;
		if (index == 2) return "" + (int) PanopticInterfaceUtil.PENALTY_CRUISER;
		if (index == 3) return "" + (int) PanopticInterfaceUtil.PENALTY_CAPITAL + "%";
		if (index == 4) return "" + (int) PanopticInterfaceUtil.CR_PENALTY_MAX + "%";
		return null;
	}
}