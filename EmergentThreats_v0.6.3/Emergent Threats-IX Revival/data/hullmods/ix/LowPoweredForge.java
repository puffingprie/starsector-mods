package data.hullmods.ix;

import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class LowPoweredForge extends BaseHullMod {

	private static float FIGHTER_OP_LIMIT = 12f;
	private static float FIGHTER_REPLACE_PENALTY = 30f;
	private static String THIS_MOD = "ix_low_powered_forge";
	
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		int penaltySquads = 0;
		FighterWingSpecAPI wing0 = stats.getVariant().getWing(0);
		FighterWingSpecAPI wing1 = stats.getVariant().getWing(1);
		FighterWingSpecAPI wing2 = stats.getVariant().getWing(2);
		FighterWingSpecAPI wing3 = stats.getVariant().getWing(3);
		List<FighterWingSpecAPI> wings = new ArrayList<FighterWingSpecAPI>();
		wings.add(wing0);
		wings.add(wing1);
		wings.add(wing2);
		wings.add(wing3);
		for (FighterWingSpecAPI f : wings) {
			if (f != null && f.getOpCost(stats) > FIGHTER_OP_LIMIT) penaltySquads++;
		}
		float penalty = penaltySquads * FIGHTER_REPLACE_PENALTY;
		stats.getFighterRefitTimeMult().modifyMult(id, 1f + penalty * 0.01f);
	}
	
	@Override
	public boolean shouldAddDescriptionToTooltip(HullSize hullSize, ShipAPI ship, boolean isForModSpec) {
		return true;
	}
	
	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		if (ship == null) return;
		MutableStat.StatMod mod = ship.getMutableStats().getFighterRefitTimeMult().getMultStatMod(THIS_MOD);
		String penalty = (mod == null) ? "inactive" : "" + (int) Math.ceil(mod.getValue() * 100f - 100f) + "%";
		String s = "The penalty is currently %s.";
		tooltip.addPara(s, 10f, Misc.getHighlightColor(), "" + penalty);
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) FIGHTER_OP_LIMIT;
		if (index == 1) return "" + (int) FIGHTER_REPLACE_PENALTY + "%";
		return null;
	}
}