package data.hullmods.vice;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

import data.scripts.vice.hullmods.RemnantSubsystemsUtil;
import data.scripts.vice.DistanceUtil;

public class AdaptiveEntropyProjector extends BaseHullMod {

	private Map<ShipAPI.HullSize, Integer> MAX_PULSES = new HashMap<ShipAPI.HullSize, Float>();
	{
		MAX_PULSES.put(HullSize.DEFAULT, 0);
		MAX_PULSES.put(HullSize.FIGHTER, 0);
		MAX_PULSES.put(HullSize.FRIGATE, 2);
		MAX_PULSES.put(HullSize.DESTROYER, 2);
		MAX_PULSES.put(HullSize.CRUISER, 3);
		MAX_PULSES.put(HullSize.CAPITAL_SHIP, 4);
	}
	private boolean IS_DEGRADED = false;
	
	private static Color GLOW_COLOR = new Color(255, 0, 100, 155);
	private static Color GLOW_COLOR_DEGRADED = new Color(70, 225, 225, 100);
	private static float CHARGE_INTERVAL_DEFAULT = 10f;
	private static float CHARGE_INTERVAL_CRONOS = 7f;
	
	private static int CHAIN_COUNT = 1; //text only, need to implement if chaining to multiple targets
	private static float EXTRA_EFFECT_ODDS = 20f;
	
	private static float DAMAGE_PER_PULSE = 300f;
	private static float DAMAGE_PER_PULSE_DEGRADED = 300f;
	private static float EMP_PER_PULSE = 400f;
	private static float EMP_PER_PULSE_DEGRADED = 400f;
	private static float ENTROPY_PROJECTOR_ARC_RANGE_DEFAULT = 1000f;
	private static float ENTROPY_PROJECTOR_ARC_RANGE_COSMOS = 1400f;
	private static float INTERVAL_DECORATIVE_DURATION = 0.5f;
	
	private static String ABYSSAL_HULLMOD = "rat_abyssal_grid";
	private static String ABYSSAL_TYPE = "Abyssal";
	private static String SERAPH_TYPE = "Seraph";
	private static String ENTROPIC_DISCHARGE = "entropic discharge";
	private static String HULLMOD_STANDARD = "vice_adaptive_entropy_projector";
	private static String HULLMOD_ABYSSAL = "vice_adaptive_entropy_projector_abyssal";
	
	public static String DATA_KEY = "entropy_projector_data_key";

	//Utility variables
	private RemnantSubsystemsUtil util = new RemnantSubsystemsUtil();
	
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		IS_DEGRADED = (isDegraded(stats)) ? true : false; //used for display text only, okay for static
		if (isDegraded(stats)) {
			stats.getVariant().getHullMods().remove(HULLMOD_ABYSSAL);
			stats.getVariant().getHullMods().add(HULLMOD_STANDARD);
		}
		else {
			stats.getVariant().getHullMods().remove(HULLMOD_STANDARD);
			stats.getVariant().getHullMods().add(HULLMOD_ABYSSAL);
		}
	}
	
	private boolean isDegraded(MutableShipStatsAPI stats) {
		return (!stats.getVariant().hasHullMod(ABYSSAL_HULLMOD));
	}
	
	private boolean isDegraded(ShipAPI ship) {
		return (!ship.getVariant().hasHullMod(ABYSSAL_HULLMOD));
	}
	
	private String getCoreType (ShipAPI ship) {
		String coreId = null;
		try { 
			coreId = ship.getFleetMember().getCaptain().getAICoreId();
		}
		catch (Exception e) {
			return "other";
		}
		if (isDegraded(ship)) return "degraded"; //overrides abyssal AI core bonus if hull is not abyssal
		else if (("rat_chronos_core").equals(coreId) || (ship.getVariant().hasHullMod("rat_chronos_conversion"))) return "chronos";
		else if (("rat_cosmos_core").equals(coreId) || (ship.getVariant().hasHullMod("rat_cosmos_conversion"))) return "cosmos";
		else return "other";
	}
	
	public class EntropyProjectorData {
		Map<String, Float> stats = new HashMap<String, Float>();
	}
	
	@Override
    public void advanceInCombat(ShipAPI ship, float amount) {
		super.advanceInCombat(ship, amount);
        if (!ship.isAlive() || (Integer) MAX_PULSES.get(ship.getVariant().getHullSize()) == 0) return;
		CombatEngineAPI engine = Global.getCombatEngine();
		String key = DATA_KEY + "_" + ship.getId();
		EntropyProjectorData data = new EntropyProjectorData();
		data = (EntropyProjectorData) engine.getCustomData().get(key);
		if (data == null) {
			data = new EntropyProjectorData();
			engine.getCustomData().put(key, data);
			String coreType = getCoreType(ship);
			data.stats.put("decorative", 0f);
			if (coreType.equals ("chronos")) {
				data.stats.put("interval", CHARGE_INTERVAL_CRONOS);
				data.stats.put("range", ENTROPY_PROJECTOR_ARC_RANGE_DEFAULT);
			}
			else if (coreType.equals ("cosmos")) {
				data.stats.put("interval", CHARGE_INTERVAL_DEFAULT);
				data.stats.put("range", ENTROPY_PROJECTOR_ARC_RANGE_COSMOS);
			}
			else {
				data.stats.put("interval", CHARGE_INTERVAL_DEFAULT);
				data.stats.put("range", ENTROPY_PROJECTOR_ARC_RANGE_DEFAULT);
			}
			data.stats.put("pulses", 0f);
			data.stats.put("timer", 0f);
		}
		float timer = (Float) data.stats.get("timer");
		float interval = (Float) data.stats.get("interval");
		float decorative = (Float) data.stats.get("decorative");
		timer += amount;
		data.stats.put("timer", timer);
		if (timer >= interval) {
			float pulses = (Float) data.stats.get("pulses");
			float range = (Float) data.stats.get("range");
			//getNearestNotAbyssal also excludes entropy arrester hulls
			ShipAPI target = DistanceUtil.getNearestNotAbyssal(ship, range, "enemies");
			if (target != null) {
				spawnEMP (target, ship, range);
				pulses++;
				data.stats.put("pulses", pulses);
			}
			if (pulses >= (Integer) MAX_PULSES.get(ship.getVariant().getHullSize())) {
				data.stats.put("pulses", 0f);
				data.stats.put("timer", 0f);
				data.stats.put("decorative", 0f);
			}
			else if (decorative + INTERVAL_DECORATIVE_DURATION < timer) {
				spawnEMP (ship, ship, true);
				spawnEMP (ship, ship, true);
				data.stats.put("decorative", timer); 
			}
		}
	}
	
	private void spawnEMP(ShipAPI target, ShipAPI ship, float range) {
		boolean degraded = isDegraded(ship);
		Color color = degraded ? GLOW_COLOR_DEGRADED : GLOW_COLOR;
		float damage = degraded ? DAMAGE_PER_PULSE_DEGRADED : DAMAGE_PER_PULSE;
		float emp = degraded ? EMP_PER_PULSE_DEGRADED : EMP_PER_PULSE;
		
		CombatEngineAPI engine = Global.getCombatEngine();
		engine.spawnEmpArc(ship,
						ship.getLocation(),
						ship,
						target,
						DamageType.ENERGY,
						damage, // damage
						emp, // emp damage
						range + 500f, // extra range due to ship geometry
						"system_emp_emitter_impact", // sound
						26f, // thickness
						color, // fringe
						Color.white // core color
						);
		/*
		if (!isDegraded(ship) && Math.random() <= EXTRA_EFFECT_ODDS * 0.01f) {
			target.setCurrentCR(target.getCurrentCR() - CR_PENALTY * 0.01f);
			target.fadeToColor(target, GLOW_COLOR, 0.7f, 0.3f, 0.5f);
		}
		*/
		if (!isDegraded(ship) && Math.random() <= EXTRA_EFFECT_ODDS * 0.01f) {
			ShipAPI chain = DistanceUtil.getNearestNotAbyssal(target, range, "friends");
			if (chain != null) {
				engine.spawnEmpArc(target,
						target.getLocation(),
						target,
						chain,
						DamageType.ENERGY,
						damage, // damage
						emp, // emp damage
						range + 500f, // extra range due to ship geometry
						"system_emp_emitter_impact", // sound
						26f, // thickness
						color, // fringe
						Color.white // core color
						);
			}
		}
	}
	
	private void spawnEMP(ShipAPI target, ShipAPI ship, boolean decorative) {
		boolean degraded = isDegraded(ship);
		Color color = degraded ? GLOW_COLOR_DEGRADED : GLOW_COLOR;
		
		CombatEngineAPI engine = Global.getCombatEngine();
		engine.spawnEmpArcPierceShields(ship,
						ship.getLocation(),
						ship,
						target,
						DamageType.ENERGY,
						0f, // damage
						0f, // emp damage
						100f, // range
						"", // sound
						26f, // thickness
						color, // fringe
						Color.white // core color
						);
	}
	
	@Override
    public boolean isApplicableToShip(ShipAPI ship) {
		if (util.isModuleCheck(ship)) return false;
		if (ship.getHullSpec().getManufacturer().equals(ABYSSAL_TYPE)
				|| ship.getHullSpec().getManufacturer().equals(SERAPH_TYPE)) return true;
		return (util.isApplicable(ship) && util.isOnlyRemnantMod(ship));
	}

	public String getUnapplicableReason(ShipAPI ship) {
		if (util.isModuleCheck(ship)) return util.getIncompatibleCauseString("hub");
		if (!util.isApplicable(ship) 
				&& !ship.getHullSpec().getManufacturer().equals(ABYSSAL_TYPE)
				&& !ship.getHullSpec().getManufacturer().equals(SERAPH_TYPE)) return util.getIncompatibleCauseString("manufacturer");
		if (!util.isOnlyRemnantMod(ship)) return util.getIncompatibleCauseString("modcount");
		return null;
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		String pulses = "" + (Integer) MAX_PULSES.get(HullSize.FRIGATE) + "/"
					+ (Integer) MAX_PULSES.get(HullSize.DESTROYER) + "/"
					+ (Integer) MAX_PULSES.get(HullSize.CRUISER) + "/"
					+ (Integer) MAX_PULSES.get(HullSize.CAPITAL_SHIP);
		String damage = IS_DEGRADED ? ("" + (int) DAMAGE_PER_PULSE_DEGRADED) : ("" + (int) DAMAGE_PER_PULSE);
		String emp = IS_DEGRADED ? ("" + (int) EMP_PER_PULSE_DEGRADED) : ("" + (int) EMP_PER_PULSE);
		
		if (index == 0) return "" + (int) CHARGE_INTERVAL_DEFAULT;
		if (index == 1) return "" + (int) ENTROPY_PROJECTOR_ARC_RANGE_DEFAULT;
		if (index == 2) return pulses;
		if (index == 3) return damage;
		if (index == 4) return emp;
		if (index == 5) return "" + (int) EXTRA_EFFECT_ODDS + "%";
		if (index == 6) return "" + (int) ENTROPY_PROJECTOR_ARC_RANGE_DEFAULT;
		if (index == 7) return "" + (int) CHARGE_INTERVAL_CRONOS;
		if (index == 8) return "" + (int) ENTROPY_PROJECTOR_ARC_RANGE_COSMOS;
		if (index == 9) return ABYSSAL_TYPE;
		return null;
	}
}