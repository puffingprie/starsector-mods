package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

public class AegisCoreStats extends BaseShipSystemScript {

	protected Object STATUSKEY1 = new Object();
	protected Object STATUSKEY2 = new Object();

	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {

		stats.getHullDamageTakenMult().modifyMult(id, 0.1f * effectLevel);
		stats.getArmorDamageTakenMult().modifyMult(id, 0.1f  * effectLevel);
		stats.getEmpDamageTakenMult().modifyMult(id, 0.1f * effectLevel);
		stats.getEnergyWeaponFluxCostMod().modifyMult(id,0.6f*effectLevel);
		stats.getBallisticWeaponFluxCostMod().modifyMult(id,0.6f*effectLevel);
		stats.getBeamWeaponFluxCostMult().modifyMult(id,0.6f*effectLevel);

		ShipAPI ship = null;
		boolean player = false;
		if (stats.getEntity() instanceof ShipAPI) {
			ship = (ShipAPI) stats.getEntity();
			player = ship == Global.getCombatEngine().getPlayerShip();
		}
		if (player) {
			ShipSystemAPI system = ship.getSystem();
			if (system != null) {
				float percent = 0.8f * effectLevel * 100;
				Global.getCombatEngine().maintainStatusForPlayerShip(
						STATUSKEY1,
						"graphics/icons/hullsys/damper_field.png",
						"Aegis Core",
						(int) Math.round(percent) + "% less damage taken", false);
				Global.getCombatEngine().maintainStatusForPlayerShip(
						STATUSKEY2,
						"graphics/icons/hullsys/damper_field.png",
						"Aegis Core",
						"Weapon Flux Cost Reduced", false);
			}
		}
	}
	public void unapply(MutableShipStatsAPI stats, String id) {
		stats.getHullDamageTakenMult().unmodify(id);
		stats.getArmorDamageTakenMult().unmodify(id);
		stats.getEmpDamageTakenMult().unmodify(id);
		stats.getBallisticWeaponFluxCostMod().unmodify(id);
		stats.getBeamWeaponFluxCostMult().unmodify(id);
		stats.getEnergyWeaponFluxCostMod().unmodify(id);
	}
}





