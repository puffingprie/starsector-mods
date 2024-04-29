package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import org.lazywizard.lazylib.MathUtils;
import java.util.Iterator;

import java.util.HashMap;
import java.util.Map;

public class SystemJammerStats extends BaseShipSystemScript {

	private static CombatEngineAPI engine = null;

	private static final float RANGE = 1500f;
	private static final float ROF_BONUS = -30f;
	private static final float RANGE_BONUS = -40f;
	private static final float SPEED_BONUS = -50f;
	private static final Map<ShipAPI, ShipAPI> jamming = new HashMap<>();

	private static final String staticID = "systemjamming";

	@Override
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		if (engine != Global.getCombatEngine()) {
			engine = Global.getCombatEngine();
			jamming.clear();
		}

		//Declares two objects of type ShipAPI. 'ship' is just a generic holder for ships that are cycled through. 'host_ship' is the ship that is using the system.
		ShipAPI host_ship = (ShipAPI) stats.getEntity();

		for (ShipAPI ship : engine.getShips()) {
			if (!ship.isAlive()) {
				continue; //We don't want to bother modifying stats of the ship if it's disabled.
			}
			if (ship == host_ship) {
				continue;
			}

			if ((host_ship.getOwner() != ship.getOwner()) && (MathUtils.getDistance(ship, host_ship) <= (RANGE))) {
				//Modify this ship's stats.
				ship.getMutableStats().getEnergyRoFMult().modifyPercent(staticID, ROF_BONUS);
				ship.getMutableStats().getBeamWeaponDamageMult().modifyPercent(staticID, ROF_BONUS);
				ship.getMutableStats().getBallisticRoFMult().modifyPercent(staticID, ROF_BONUS);
				ship.getMutableStats().getBallisticWeaponRangeBonus().modifyPercent(staticID, RANGE_BONUS);
				ship.getMutableStats().getEnergyWeaponRangeBonus().modifyPercent(staticID, RANGE_BONUS);
				ship.getMutableStats().getBeamWeaponRangeBonus().modifyPercent(staticID, RANGE_BONUS);
				ship.getMutableStats().getMaxSpeed().modifyPercent(staticID, SPEED_BONUS);

				//Adds the ship to the hashmap, and associates it with the host ship.
				jamming.put(ship, host_ship);
				//If the ship isn't in range but is contained in the hashmap, and the host ship of the ship is indeed this one...
			} else if ((jamming.containsKey(ship)) && (jamming.get(ship) == host_ship)) {
				//removes all benefits
				ship.getMutableStats().getEnergyRoFMult().unmodify(staticID);
				ship.getMutableStats().getBeamWeaponDamageMult().unmodify(staticID);
				ship.getMutableStats().getBallisticRoFMult().unmodify(staticID);
				ship.getMutableStats().getBallisticWeaponRangeBonus().unmodify(staticID);
				ship.getMutableStats().getBeamWeaponRangeBonus().unmodify(staticID);
				ship.getMutableStats().getEnergyWeaponRangeBonus().unmodify(staticID);
				ship.getMutableStats().getMaxSpeed().unmodify(staticID);

				//Removes the ship from the hashmap.
				jamming.remove(ship);
			}
		}
	}

	@Override
	public void unapply(MutableShipStatsAPI stats, String id) {
		//same objects as before.
		ShipAPI host_ship = (ShipAPI) stats.getEntity();
		//Loops through all the ships in the hashmap.
		Iterator<Map.Entry<ShipAPI, ShipAPI>> iter = jamming.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<ShipAPI, ShipAPI> entry = iter.next();
			ShipAPI ship = entry.getKey();

			//(This makes it so that one host ship bringing down its system doesn't remove benefits that are being applied to other ships by host ships elsewhere.
			if (entry.getValue() == host_ship) {
				//removes all benefits
				ship.getMutableStats().getEnergyRoFMult().unmodify(staticID);
				ship.getMutableStats().getBeamWeaponDamageMult().unmodify(staticID);
				ship.getMutableStats().getBallisticRoFMult().unmodify(staticID);
				ship.getMutableStats().getBallisticWeaponRangeBonus().unmodify(staticID);
				ship.getMutableStats().getBeamWeaponRangeBonus().unmodify(staticID);
				ship.getMutableStats().getEnergyWeaponRangeBonus().unmodify(staticID);
				ship.getMutableStats().getMaxSpeed().unmodify(staticID);


				iter.remove();
			}
		}
	}

	@Override
	public StatusData getStatusData(int index, State state, float effectLevel) {
		if (index == 0) {
			return new StatusData("Jamming Target", false);
		}
		return null;
	}
}