package data.hullmods.vice;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class UniversalInterface extends BaseHullMod {

	private static float FLUX_PER_OP = 5f;
	private static float BALLISTIC_ROF_PENALTY = 25f;

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		ShipVariantAPI var = stats.getVariant();
		WeaponSpecAPI spec001 = null;
		WeaponSpecAPI spec002 = null;
		WeaponSpecAPI spec004 = null;
		WeaponType type001 = null;
		WeaponType type002 = null;
		WeaponType type004 = null;
		float missileOP = 0f;
		boolean allBallistic = false;
		
		MutableCharacterStatsAPI charStats = Global.getFactory().createPerson().getStats();
		
		if (var.getWeaponSpec("WS 001") != null) {
			spec001 = var.getWeaponSpec("WS 001");
			type001 = spec001.getType();
		}
		if (var.getWeaponSpec("WS 002") != null) {
			spec002 = var.getWeaponSpec("WS 002");
			type002 = spec002.getType();
		}
		if (var.getWeaponSpec("WS 004") != null) {
			spec004 = var.getWeaponSpec("WS 004");
			type004 = spec004.getType();
		}
		
		if ((spec001 != null && WeaponType.BALLISTIC.equals(type001))
					&& (spec002 != null && WeaponType.BALLISTIC.equals(type002))
					&& (spec004 != null && WeaponType.BALLISTIC.equals(type004))) allBallistic = true;
		
		if (spec001 != null && WeaponType.MISSILE.equals(type001)) {
			missileOP += spec001.getOrdnancePointCost(charStats, stats);
		}
		if (spec002 != null && WeaponType.MISSILE.equals(type002)) {
			missileOP += spec002.getOrdnancePointCost(charStats, stats);
		}
		if (spec004 != null && WeaponType.MISSILE.equals(type004)) {
			missileOP += spec004.getOrdnancePointCost(charStats, stats);
		}
		
		if (!allBallistic) stats.getBallisticRoFMult().modifyPercent(id, -BALLISTIC_ROF_PENALTY);
		
		float flux = FLUX_PER_OP * missileOP;
		stats.getFluxDissipation().modifyFlat(id, -flux);
	}
	
	@Override
	public boolean shouldAddDescriptionToTooltip(HullSize hullSize, ShipAPI ship, boolean isForModSpec) {
		return true;
	}
		
	//done here because results do not update properly with getDescriptionParam
	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		if (ship == null) return;
		MutableShipStatsAPI stats = ship.getMutableStats();
		float flux = stats.getFluxDissipation().getFlatStatMod("vice_universal_interface") == null 
			? 0f : stats.getFluxDissipation().getFlatStatMod("vice_universal_interface").getValue();
		float rof = stats.getBallisticRoFMult().getPercentStatMod("vice_universal_interface") == null 
			? 0f : stats.getBallisticRoFMult().getPercentStatMod("vice_universal_interface").getValue();
		
		String missilePenaltyBase = "-" + (int) FLUX_PER_OP;
		String missilePenaltyActual = flux != 0f ? "" + (int) flux + " total flux dissipation" : "inactive";
		String ballisticPenaltybase = "-" + (int) BALLISTIC_ROF_PENALTY + "%";
		String ballisticPenaltyActual = rof != 0f ? "active" : "inactive";
		
		String s1 = "%s flux dissipation per ordnance point spent on missile weapons. The penalty is %s.";
		String s2 = "%s ballistic weapon firing speed unless all universal slots are fitted with ballistic weapons. The penalty is %s.";
		tooltip.addPara(s1, 10f, Misc.getHighlightColor(), missilePenaltyBase, missilePenaltyActual);
		tooltip.addPara(s2, 10f, Misc.getHighlightColor(), ballisticPenaltybase, ballisticPenaltyActual);
	}
}