package data.hullmods.vice;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.listeners.DamageDealtModifier;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class IncursionHull extends BaseHullMod {

	private static float ECM_BONUS = 6f;
	
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getDynamic().getMod(Stats.ELECTRONIC_WARFARE_FLAT).modifyFlat(id, ECM_BONUS);
		stats.getVariant().getHullMods().remove("ecm");
	}
	
	//also grants hard flux damage to built-in Neutron Autolances
	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		ShipVariantAPI variant = ship.getVariant();
		if (variant.hasHullMod("high_scatter_amp")
				|| variant.hasHullMod("vice_adaptive_emitter_diodes")
				|| variant.hasHullMod("vice_attuned_emitter_diodes")) return;
		ship.addListener(new IncursionHardFlux(ship));
	}
	
	public static class IncursionHardFlux implements DamageDealtModifier {
		protected ShipAPI ship;
		public IncursionHardFlux(ShipAPI ship) {
			this.ship = ship;
		}
		
		public String modifyDamageDealt(Object param,
								   		CombatEntityAPI target, DamageAPI damage,
								   		Vector2f point, boolean shieldHit) {
			
			if (!(param instanceof DamagingProjectileAPI) && param instanceof BeamAPI) {
				if (((BeamAPI) param).getWeapon().getId().equals("vice_neutron_autolance")) {
					damage.setForceHardFlux(true);
				}
			}
			return null;
		}
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) ECM_BONUS + "%";
		return null;
	}
}
