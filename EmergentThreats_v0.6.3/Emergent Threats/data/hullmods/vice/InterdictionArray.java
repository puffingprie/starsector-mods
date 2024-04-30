package data.hullmods.vice;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class InterdictionArray extends BaseHullMod {
 
	private static String NEW_SYSTEM = "Interdictor Array"; //text only, switch done by HANDLER_HULLMOD
	private static int RANGE = 2000; //text only, range set in InterdictorPulseAI, InterdictorPulseTargeting
	private static int KINETIC_DAMAGE = 2000; //text only, damage set in weapons.csv
	private static int SMOD_BURN_BONUS = 1;
	
	private static float INTERVAL = 0.2f; //seconds between each decorative EMP arc
	private static int BOLTS = 2;
	public static String DATA_KEY = "vice_paragon_data_key";
	private static Color EFFECT_COLOR = new Color(100,165,255,75);
	
	private static String PARAGON_ID = "paragon";
	private static String PARAGON_IX_ID = "paragon_ix";
	private static String THIS_MOD_ID = "vice_interdiction_array";
	private static String HANDLER_HULLMOD = "vice_interdiction_handler";
	private static String SYSTEM_INTERDICTOR = "vice_interdictor";
	private static String SYSTEM_FORTRESS = "fortressshield";
	private static String SYSTEM_ZEUS = "swp_boss_zeusshield";
	
	//actual system swap handled by HANDLER_HULLMOD
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getVariant().addPermaMod(HANDLER_HULLMOD);
		if (isSMod(stats)) stats.getMaxBurnLevel().modifyFlat(id, SMOD_BURN_BONUS);
	}
	
	@Override
    public boolean isApplicableToShip(ShipAPI ship) {
		String id = ship.getVariant().getHullSpec().getHullId();
		if (id.equals(PARAGON_IX_ID)) return false;
		if (ship.getVariant().hasHullMod(THIS_MOD_ID)) return true;
		if (ship.getSystem() == null) return false;
		String sysId = ship.getSystem().getId();
		return (id.contains(PARAGON_ID)	&& (sysId.equals(SYSTEM_FORTRESS) 
					|| sysId.equals(SYSTEM_INTERDICTOR)
					|| sysId.equals(SYSTEM_ZEUS)));
	}
	
	public String getUnapplicableReason(ShipAPI ship) {
		String id = ship.getVariant().getHullSpec().getHullId();
		if (id.equals(PARAGON_IX_ID)) return "Already installed";
		if (ship.getVariant().hasHullMod(THIS_MOD_ID)) return null;
		if (ship.getSystem() == null) return null;
		String sysId = ship.getSystem().getId();
		if (!sysId.equals(SYSTEM_FORTRESS) 
				&& !sysId.equals(SYSTEM_INTERDICTOR)
				&& !sysId.equals(SYSTEM_ZEUS)) return "Incompatible hull";
		if (!id.contains(PARAGON_ID)) return "Incompatible hull";
		return null;
	}
	
	public static class ParagonIXData {
		Map<String, Float> stats = new HashMap<String, Float>();
	}
	
	@Override
    public void advanceInCombat(ShipAPI ship, float amount) {
		super.advanceInCombat(ship, amount);
        if (!ship.isAlive() || !ship.getSystem().isChargeup()) return;
		CombatEngineAPI engine = Global.getCombatEngine();
		String key = DATA_KEY + "_" + ship.getId();
		ParagonIXData data = (ParagonIXData) engine.getCustomData().get(key);
		if (data == null) {
			data = new ParagonIXData();
			engine.getCustomData().put(key, data);
			data.stats.put("timer", 0f);
			data.stats.put("jitters", 0f);
		}
		float timer = (Float) data.stats.get("timer");
		timer += amount;
		data.stats.put("timer", timer);
		if (timer >= INTERVAL) {		
			for (int i = 0; i < BOLTS; i++) {
				spawnEmpArc(ship, ship, ship.getLocation(), engine);
			}
			data.stats.put("timer", 0f);
		}
	}
	
	private void spawnEmpArc(ShipAPI source, ShipAPI target, Vector2f point, CombatEngineAPI engine) {
		engine.spawnEmpArc(source, //damageSource
					point, //hit location
					source, //CombatEntityAPI point anchor
					source, //CombatEntityAPI target
					DamageType.ENERGY,
					0f, // damage
					0f, // emp damage
					300f, // range
					"", // sound
					26f, // thickness
					new Color(25,100,155,255),
					Color.white);
	}
	
	public String getSModDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + SMOD_BURN_BONUS;
		return null;
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return NEW_SYSTEM;
		if (index == 1) return "" + RANGE;
		if (index == 2) return "" + KINETIC_DAMAGE;
		return null;
	}
}