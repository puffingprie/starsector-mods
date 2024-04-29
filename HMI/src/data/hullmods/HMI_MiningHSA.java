package data.hullmods;

import java.awt.Color;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.listeners.DamageDealtModifier;
import com.fs.starfarer.api.combat.listeners.WeaponBaseRangeModifier;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class HMI_MiningHSA extends BaseHullMod {

	public static float DAMAGE_BONUS_PERCENT = 15f;
		
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		boolean sMod = isSMod(stats);
		if (sMod) {
		stats.getBeamWeaponDamageMult().modifyPercent(id, DAMAGE_BONUS_PERCENT);
		}
	}

	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		ship.addListener(new HMI_MiningDamageDealtMod(ship));
	}
	public void applyEffectsToFighterSpawnedByShip(ShipAPI fighter, ShipAPI ship, String id) {
		ship.addListener(new HMI_MiningDamageDealtMod(ship));
	}

	public static class HMI_MiningDamageDealtMod implements DamageDealtModifier {
		protected ShipAPI ship;

		public HMI_MiningDamageDealtMod(ShipAPI ship) {
			this.ship = ship;
		}

		public String modifyDamageDealt(Object param,
										CombatEntityAPI target, DamageAPI damage,
										Vector2f point, boolean shieldHit) {
			for(WeaponAPI w:ship.getAllWeapons())
				if (w.getSpec().getMountType() == WeaponAPI.WeaponType.HYBRID) {
					if (!(param instanceof DamagingProjectileAPI) && param instanceof BeamAPI) {
					damage.setForceHardFlux(true);
					}
			}
				return null;
			}
	}

	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "hybrid beam weapons deal hard flux";
		if (index == 1) return "fighters launched by the ship";
		return null;
	}
	
	public String getSModDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) DAMAGE_BONUS_PERCENT + "%";
		return null;
	}
}


