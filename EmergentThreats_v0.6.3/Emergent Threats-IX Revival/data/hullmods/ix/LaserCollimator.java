package data.hullmods.ix;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.listeners.DamageDealtModifier;

public class LaserCollimator extends BaseHullMod {

	private static Map mag = new HashMap();
	static {
		mag.put(HullSize.FRIGATE, 40f);
		mag.put(HullSize.DESTROYER, 80f);
		mag.put(HullSize.CRUISER, 120f);
		mag.put(HullSize.CAPITAL_SHIP, 200f);
	}
	
	public static Map sMag = new HashMap();
	static {
		sMag.put(HullSize.FRIGATE, 20f);
		sMag.put(HullSize.DESTROYER, 40f);
		sMag.put(HullSize.CRUISER, 60f);
		sMag.put(HullSize.CAPITAL_SHIP, 100f);
	}
	
	private static String WEAPON_S_NAME = "Tactical Lasers";
	private static String WEAPON_M_NAME = "Twin Tactical Lasers";
	private static String WEAPON_L_NAME = "High Intensity Lasers";
	private static String WEAPON_S_ID = "taclaser";
	private static String WEAPON_M_ID = "twin_tactical_laser_ix";
	private static String WEAPON_L_ID = "hil";
	
	private static String CONFLICT_MOD_1 = "advancedoptics";
	private static String CONFLICT_MOD_2 = "high_scatter_amp";
	private static String CONFLICT_MOD_3 = "vice_adaptive_emitter_diodes";
	private static String CONFLICT_MOD_4 = "vice_attuned_emitter_diodes";
	
	
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		float dissipation = isSMod(stats) ? (Float) sMag.get(hullSize) : (Float) mag.get(hullSize);
		stats.getFluxDissipation().modifyFlat(id, -dissipation);
		if (isSMod(stats)) {
			stats.getVariant().getHullMods().remove(CONFLICT_MOD_1);
			stats.getVariant().getHullMods().remove(CONFLICT_MOD_2);
			stats.getVariant().getHullMods().remove(CONFLICT_MOD_3);
		}
	}
	
	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		ship.addListener(new IBCHardFlux(ship));
	}
	
	public static class IBCHardFlux implements DamageDealtModifier {
		protected ShipAPI ship;
		public IBCHardFlux(ShipAPI ship) {
			this.ship = ship;
		}
		
		public String modifyDamageDealt(Object param,
								   		CombatEntityAPI target, DamageAPI damage,
								   		Vector2f point, boolean shieldHit) {
			if (param instanceof BeamAPI) {
				BeamAPI beam = (BeamAPI) param;
				String weaponId = beam.getWeapon().getSpec().getWeaponId();
				if (weaponId.equals(WEAPON_S_ID) 
						|| weaponId.equals(WEAPON_M_ID) 
						|| weaponId.equals(WEAPON_L_ID)) damage.setForceHardFlux(true);
			}
			return null;
		}
	}

	private boolean hasEmitterModOverlap(ShipAPI ship) {
		return (ship.getVariant().getHullMods().contains(CONFLICT_MOD_1) 
				|| ship.getVariant().getHullMods().contains(CONFLICT_MOD_2)
				|| ship.getVariant().getHullMods().contains(CONFLICT_MOD_3)
				|| ship.getVariant().getHullMods().contains(CONFLICT_MOD_4));
	}
	
	@Override
    public boolean isApplicableToShip(ShipAPI ship) {
		return (!hasEmitterModOverlap(ship));
	}
	
	public String getUnapplicableReason(ShipAPI ship) {
		if (hasEmitterModOverlap(ship)) return "Incompatible emitter modification present";
		return null;
	}	
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return WEAPON_S_NAME;
		if (index == 1) return WEAPON_M_NAME;
		if (index == 2) return WEAPON_L_NAME;
		if (index == 3) return "" + ((Float) mag.get(HullSize.FRIGATE)).intValue();
		if (index == 4) return "" + ((Float) mag.get(HullSize.DESTROYER)).intValue();
		if (index == 5) return "" + ((Float) mag.get(HullSize.CRUISER)).intValue();
		if (index == 6) return "" + ((Float) mag.get(HullSize.CAPITAL_SHIP)).intValue();
		return null;
	}
	
	public String getSModDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + ((Float) sMag.get(HullSize.FRIGATE)).intValue();
		if (index == 1) return "" + ((Float) sMag.get(HullSize.DESTROYER)).intValue();
		if (index == 2) return "" + ((Float) sMag.get(HullSize.CRUISER)).intValue();
		if (index == 3) return "" + ((Float) sMag.get(HullSize.CAPITAL_SHIP)).intValue();
		return null;
	}
}
