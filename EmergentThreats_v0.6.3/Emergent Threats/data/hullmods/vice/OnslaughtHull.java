package data.hullmods.vice;

import java.awt.Color;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class OnslaughtHull extends BaseHullMod {

	private static float COST_REDUCTION = 10f;
	private static float SHIELD_BONUS = 20f;
	private static Color SHIELD_INNER = new Color(125,125,255,75);
	
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getDynamic().getMod(Stats.LARGE_ENERGY_MOD).modifyFlat(id, -COST_REDUCTION);
		stats.getDynamic().getMod(Stats.LARGE_MISSILE_MOD).modifyFlat(id, -COST_REDUCTION);
		stats.getShieldDamageTakenMult().modifyMult(id, 1f - SHIELD_BONUS * 0.01f);
		stats.getCrewLossMult().unmodifyPercent("coherer");
		stats.getDynamic().getMod(Stats.INDIVIDUAL_SHIP_RECOVERY_MOD).modifyFlat(id, 1000f);
		stats.getBreakProb().modifyMult(id, 0f);
	}
	
	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		ship.getVariant().getPermaMods().remove("hbi");
		ship.getVariant().getHullMods().remove("hbi");
	}
	
	@Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        if (!ship.isAlive()) return;
		ship.getShield().setInnerColor(SHIELD_INNER);
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) COST_REDUCTION + "";
		if (index == 1) return "" + (int) SHIELD_BONUS + "%";
		if (index == 2) return "negates";
		return null;
	}

	@Override
	public boolean affectsOPCosts() {
		return true;
	}
}