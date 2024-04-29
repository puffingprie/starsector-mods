package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.util.IntervalUtil;

import java.util.HashSet;
import java.util.Set;

//Code clean up thanks to Atlanticaccent!

public class HMI_PeriodicMissileReload extends BaseHullMod {

	private final IntervalUtil interval = new IntervalUtil(25f, 25f);
	boolean isCountdown = false;


	private static final float WEAPON_MALFUNCTION_PROB = 0.02f;
	private static final Set<String> BLOCKED_HULLMODS = new HashSet<>();

	static {
		// These hullmods will automatically be removed
		BLOCKED_HULLMODS.add("missleracks");
		BLOCKED_HULLMODS.add("missile_autoloader");
	}

	private float check = 0;
	private String id, ERROR = "IncompatibleHullmodWarning";


	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
//        stats.getWeaponMalfunctionChance().modifyFlat(id, WEAPON_MALFUNCTION_PROB);
		stats.getCombatWeaponRepairTimeMult().modifyMult(id, 1.5f);
//		stats.getMissileAmmoBonus().modifyMult(id, 0.25f);
	}

	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {

		if (check > 0) {
			check -= 1;
			if (check < 1) {
				ship.getVariant().removeMod(ERROR);
			}
		}

		for (String tmp : BLOCKED_HULLMODS) {
			if (ship.getVariant().getHullMods().contains(tmp)) {
				ship.getVariant().removeMod(tmp);
				ship.getVariant().addMod(ERROR);
				check = 3;
			}
		}
	}

	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) {
			return "fully reloads";
		}
		if (index == 1) {
			return "the missile weapons malfunctioning on being reloaded";
		}
		if (index == 2) {
			return "20 seconds";
		}
		if (index == 3) {
			return "Incompatible with Expanded Missile Racks and Missile Autoloader";
		}
		return null;
	}

	@Override
	public void advanceInCombat(ShipAPI ship, float amount) {
		if (!ship.isAlive()) return;
		ShipAPI playerShip = Global.getCombatEngine().getPlayerShip();
		String id = "HMI_Autoforge";
		CombatEngineAPI engine = Global.getCombatEngine();

		if (isCountdown) {
			interval.advance(amount);
			if (interval.intervalElapsed()) {
				isCountdown = false;
				for (WeaponAPI w2 : ship.getAllWeapons()) {
					if (w2.getType() != WeaponAPI.WeaponType.MISSILE) continue;
					if (w2.usesAmmo() && w2.getAmmo() < w2.getMaxAmmo()) {
						w2.setAmmo(w2.getMaxAmmo());
						w2.disable(false);
					}
				}
			}
		}
		for (WeaponAPI w : ship.getAllWeapons()) {
			if (w.getType() != WeaponAPI.WeaponType.MISSILE) continue;
			if (w.usesAmmo() && w.getAmmo() <= ((w.getMaxAmmo())/2)) {
				if (!isCountdown) {
					isCountdown = true;
					interval.advance(amount);
				}
				float timer = (interval.getIntervalDuration() - interval.getElapsed());
				if (ship == playerShip) {
					Global.getCombatEngine().maintainStatusForPlayerShip(
							"HMI_Autoforge",
							"graphics/icons/hullsys/missile_racks.png",
							"Faulty Missile Autoforge",
							"Missiles reloaded and disabled in " + Math.round(timer) + " seconds!",
							false);
				}
			}
		}
	}
}
